/*
 * Twistoast - FragmentRealtime
 * Copyright (C) 2013-2016 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

import fr.outadev.android.timeo.IProgressListener;
import fr.outadev.android.timeo.TimeoStop;
import fr.outadev.twistoast.background.NextStopAlarmReceiver;

public class FragmentRealtime extends Fragment implements IStopsListContainer {

    //Refresh automatically every 60 seconds.
    private static final long REFRESH_INTERVAL = 60000L;

    private final Handler mPeriodicRefreshHandler = new Handler();
    private Runnable mPeriodicRefreshRunnable;

    private RecyclerView mStopsRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mNoContentView;
    private FloatingActionButton mFloatingActionButton;
    private AdView mAdView;

    private List<TimeoStop> mStopList;

    private Database mDatabaseHandler;
    private RecyclerAdapterRealtime mListAdapter;
    private boolean mAutoRefresh;

    private boolean mIsRefreshing;
    private boolean mIsInBackground;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == ActivityNewStop.STOP_ADDED) {
            refreshAllStopSchedules(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // yes hello please, I'd like to be inflated?
        setHasOptionsMenu(true);

        mDatabaseHandler = new Database(DatabaseOpenHelper.getInstance(getActivity()));

        mPeriodicRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (mAutoRefresh) {
                    refreshAllStopSchedules(false);
                }
            }
        };

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAutoRefresh = sharedPref.getBoolean("pref_auto_refresh", true);

        mIsRefreshing = false;
        mIsInBackground = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        // get pull to refresh view
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshAllStopSchedules(false);
            }

        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.twisto_primary, R.color.twisto_secondary,
                R.color.twisto_primary, R.color.twisto_secondary);

        mStopsRecyclerView = (RecyclerView) view.findViewById(R.id.stops_list);
        mNoContentView = view.findViewById(R.id.view_no_content);

        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        mAdView = (AdView) view.findViewById(R.id.adView);

        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);

        mStopsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mStopsRecyclerView.setLayoutManager(layoutManager);
        mStopsRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        if (getActivity() != null) {
                            int viewWidth = mStopsRecyclerView.getMeasuredWidth();
                            float cardViewWidth = getActivity().getResources().getDimension(R.dimen.schedule_row_max_size);
                            int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);

                            layoutManager.setSpanCount(newSpanCount);
                            layoutManager.requestLayout();
                        }
                    }

                });

        setupAdvertisement();
        setupListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshAllStopSchedules(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        mIsInBackground = false;
        // when the activity is resuming, refresh
        refreshAllStopSchedules(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // when the activity is pausing, stop refreshing automatically
        Log.i(Utils.TAG, "stopping automatic refresh, app paused");
        mIsInBackground = true;
        mPeriodicRefreshHandler.removeCallbacks(mPeriodicRefreshRunnable);
    }

    private void setupListeners() {
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityNewStop.class);
                startActivityForResult(intent, 0);
            }

        });

        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdFailedToLoad(int errorCode) {
                mAdView.setVisibility(View.GONE);
                super.onAdFailedToLoad(errorCode);
            }

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }

        });

        IWatchedStopChangeListener watchedStopStateListener = new IWatchedStopChangeListener() {

            @Override
            public void onStopWatchingStateChanged(TimeoStop dismissedStop, boolean watched) {
                if (dismissedStop == null) {
                    return;
                }

                for (TimeoStop stop : mStopList) {
                    if (dismissedStop.equals(stop)) {
                        stop.setWatched(watched);
                        mListAdapter.notifyDataSetChanged();
                    }
                }
            }

        };

        NextStopAlarmReceiver.setWatchedStopDismissalListener(watchedStopStateListener);
    }

    private void setupAdvertisement() {
        if (!getActivity().getResources().getBoolean(R.bool.enableAds)
                || PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pref_disable_ads", false)) {
            // If we don't want ads, hide the view
            mAdView.setVisibility(View.GONE);
        } else {
            // If we want ads, check for availability and load them
            int hasGPS = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

            if (hasGPS == ConnectionResult.SUCCESS) {
                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice(getString(R.string.admob_test_device)).build();
                mAdView.loadAd(adRequest);
            }
        }
    }

    /**
     * Refreshes the list's schedules and displays them to the user.
     *
     * @param reloadFromDatabase true if we want to reload the stops completely, or false if we only want
     *                           to update the schedules
     */
    public void refreshAllStopSchedules(boolean reloadFromDatabase) {
        // we don't want to try to refresh if we're already refreshing (causes bugs)
        if (mIsRefreshing) {
            return;
        } else {
            mIsRefreshing = true;
        }

        // show the refresh animation
        mSwipeRefreshLayout.setRefreshing(true);

        // we have to reset the adapter so it correctly loads the stops
        // if we don't do that, bugs will appear when the database has been
        // modified
        if (reloadFromDatabase) {
            mStopList = mDatabaseHandler.getAllStops();
            mListAdapter = new RecyclerAdapterRealtime(getActivity(), mStopList, this, mStopsRecyclerView);
            mStopsRecyclerView.setAdapter(mListAdapter);
        }

        // finally, get the schedule
        mListAdapter.updateScheduleData();
    }

    @Override
    public void endRefresh(boolean success) {
        // notify the pull to refresh view that the refresh has finished
        mIsRefreshing = false;
        mSwipeRefreshLayout.setRefreshing(false);

        mNoContentView.setVisibility((mListAdapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);

        // reset the timer loop, and start it again
        // this ensures the list is refreshed automatically every 60 seconds
        mPeriodicRefreshHandler.removeCallbacks(mPeriodicRefreshRunnable);

        if (!mIsInBackground) {
            mPeriodicRefreshHandler.postDelayed(mPeriodicRefreshRunnable, REFRESH_INTERVAL);
        }

        if (success) {
            int mismatch = mListAdapter.checkSchedulesMismatchCount();

            Log.i(Utils.TAG, "refreshed, " + mListAdapter.getItemCount() + " stops in db");

            if (mismatch > 0) {
                Snackbar.make(mStopsRecyclerView, R.string.stop_ref_update_message_title, Snackbar.LENGTH_LONG)
                        .setAction(R.string.stop_ref_update_message_action, new View.OnClickListener() {

	                        @Override
	                        public void onClick(View view) {
		                        (new ReferenceUpdateTask()).execute();
	                        }

                        })
                        .show();
            }
        }
    }

    @Override
    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    @Override
    public void setNoContentViewVisible(boolean visible) {
        mNoContentView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void loadFragmentForDrawerItem(int index) {
        ((ActivityRealtime) getActivity()).loadFragmentForDrawerItem(index);
    }

    private class ReferenceUpdateTask extends AsyncTask<Void, Void, Exception> {

        private ProgressDialog mProgressDialog;
        private TimeoStopReferenceUpdater mReferenceUpdater;

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                mReferenceUpdater.updateAllStopReferences(mStopList, new IProgressListener() {

                    @Override
                    public void onProgress(int current, int total) {
                        mProgressDialog.setIndeterminate(false);
                        mProgressDialog.setMax(total);
                        mProgressDialog.setProgress(current);
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            mReferenceUpdater = new TimeoStopReferenceUpdater(getActivity());
            mProgressDialog = new ProgressDialog(getActivity());

            mProgressDialog.setTitle(R.string.stop_ref_update_progress_title);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Exception e) {
            mProgressDialog.hide();
            refreshAllStopSchedules(true);

            if (e != null) {
                Snackbar.make(mStopsRecyclerView, R.string.stop_ref_update_error_text, Snackbar.LENGTH_LONG)
                        .setAction(R.string.error_retry, new View.OnClickListener() {

	                        @Override
	                        public void onClick(View view) {
		                        (new ReferenceUpdateTask()).execute();
	                        }

                        })
                        .show();
            }
        }
    }

}
