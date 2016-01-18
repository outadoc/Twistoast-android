/*
 * Twistoast - FragmentRealtime
 * Copyright (C) 2013-2015 Baptiste Candellier
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
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;

import fr.outadev.android.timeo.IProgressListener;
import fr.outadev.android.timeo.TimeoStop;
import fr.outadev.twistoast.background.NextStopAlarmReceiver;

public class FragmentRealtime extends Fragment implements IStopsListContainer {

	//Refresh automatically every 60 seconds.
	private static final long REFRESH_INTERVAL = 60000L;

	private final Handler periodicRefreshHandler = new Handler();
	private Runnable periodicRefreshRunnable;

	private RecyclerView stopsRecyclerView;
	private SwipeRefreshLayout swipeRefreshLayout;
	private View noContentView;
	private FloatingActionButton fab;
	private AdView adView;

	private List<TimeoStop> stops;

	private Database databaseHandler;
	private RecyclerAdapterRealtime listAdapter;
	private boolean autoRefresh;

	private boolean isRefreshing;
	private boolean isInBackground;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0 && resultCode == ActivityNewStop.STOP_ADDED) {
			refreshAllStopSchedules(true);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// yes hello please, I'd like to be inflated?
		setHasOptionsMenu(true);

		databaseHandler = new Database(DatabaseOpenHelper.getInstance(getActivity()));

		periodicRefreshRunnable = new Runnable() {
			@Override
			public void run() {
				if(autoRefresh) {
					refreshAllStopSchedules(false);
				}
			}
		};

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		autoRefresh = sharedPref.getBoolean("pref_auto_refresh", true);

		isRefreshing = false;
		isInBackground = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_realtime, container, false);

		// get pull to refresh view
		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.ptr_layout);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refreshAllStopSchedules(false);
			}

		});

		swipeRefreshLayout.setColorSchemeResources(R.color.twisto_primary, R.color.twisto_secondary,
				R.color.twisto_primary, R.color.twisto_secondary);

		stopsRecyclerView = (RecyclerView) view.findViewById(R.id.stops_list);
		noContentView = view.findViewById(R.id.view_no_content);

		fab = (FloatingActionButton) view.findViewById(R.id.fab);
		adView = (AdView) view.findViewById(R.id.adView);

		final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
		layoutManager.setOrientation(GridLayoutManager.VERTICAL);

		stopsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
		stopsRecyclerView.setLayoutManager(layoutManager);
		stopsRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						if(getActivity() != null) {
							int viewWidth = stopsRecyclerView.getMeasuredWidth();
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

		isInBackground = false;
		// when the activity is resuming, refresh
		refreshAllStopSchedules(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		// when the activity is pausing, stop refreshing automatically
		Log.i(Utils.TAG, "stopping automatic refresh, app paused");
		isInBackground = true;
		periodicRefreshHandler.removeCallbacks(periodicRefreshRunnable);
	}

	private void setupListeners() {
		fab.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ActivityNewStop.class);
				startActivityForResult(intent, 0);
			}

		});

		adView.setAdListener(new AdListener() {

			@Override
			public void onAdFailedToLoad(int errorCode) {
				adView.setVisibility(View.GONE);
				super.onAdFailedToLoad(errorCode);
			}

			@Override
			public void onAdLoaded() {
				adView.setVisibility(View.VISIBLE);
				super.onAdLoaded();
			}

		});

		IWatchedStopChangeListener watchedStopStateListener = new IWatchedStopChangeListener() {

			@Override
			public void onStopWatchingStateChanged(TimeoStop dismissedStop, boolean watched) {
				if(dismissedStop == null) {
					return;
				}

				for(TimeoStop stop : stops) {
					if(dismissedStop.equals(stop)) {
						stop.setWatched(watched);
						listAdapter.notifyDataSetChanged();
					}
				}
			}

		};

		NextStopAlarmReceiver.setWatchedStopDismissalListener(watchedStopStateListener);
	}

	private void setupAdvertisement() {
		if(!getActivity().getResources().getBoolean(R.bool.enableAds)
				|| PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pref_disable_ads", false)) {
			// If we don't want ads, hide the view
			adView.setVisibility(View.GONE);
		} else {
			// If we want ads, check for availability and load them
			int hasGPS = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

			if(hasGPS == ConnectionResult.SUCCESS) {
				AdRequest adRequest = new AdRequest.Builder()
						.addTestDevice(getString(R.string.admob_test_device)).build();
				adView.loadAd(adRequest);
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
		if(isRefreshing) {
			return;
		} else {
			isRefreshing = true;
		}

		// show the refresh animation
		swipeRefreshLayout.setRefreshing(true);

		// we have to reset the adapter so it correctly loads the stops
		// if we don't do that, bugs will appear when the database has been
		// modified
		if(reloadFromDatabase) {
			stops = databaseHandler.getAllStops();
			listAdapter = new RecyclerAdapterRealtime(getActivity(), stops, this, stopsRecyclerView);
			stopsRecyclerView.setAdapter(listAdapter);
		}

		// finally, get the schedule
		listAdapter.updateScheduleData();
	}

	@Override
	public void endRefresh(boolean success) {
		// notify the pull to refresh view that the refresh has finished
		isRefreshing = false;
		swipeRefreshLayout.setRefreshing(false);

		noContentView.setVisibility((listAdapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);

		// reset the timer loop, and start it again
		// this ensures the list is refreshed automatically every 60 seconds
		periodicRefreshHandler.removeCallbacks(periodicRefreshRunnable);

		if(!isInBackground) {
			periodicRefreshHandler.postDelayed(periodicRefreshRunnable, REFRESH_INTERVAL);
		}

		if(success) {
			int mismatch = listAdapter.checkSchedulesMismatchCount();

			Log.i(Utils.TAG, "refreshed, " + listAdapter.getItemCount() + " stops in db");

			if(mismatch > 0) {
				Snackbar.make(stopsRecyclerView, R.string.stop_ref_update_message_title, Snackbar.LENGTH_LONG)
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
		return isRefreshing;
	}

	@Override
	public void setNoContentViewVisible(boolean visible) {
		noContentView.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	@Override
	public void loadFragmentForDrawerItem(int index) {
		((ActivityRealtime) getActivity()).loadFragmentForDrawerItem(index);
	}

	private class ReferenceUpdateTask extends AsyncTask<Void, Void, Exception> {

		private ProgressDialog dialog;
		private TimeoStopReferenceUpdater referenceUpdater;

		@Override
		protected Exception doInBackground(Void... params) {
			try {
				referenceUpdater.updateAllStopReferences(stops, new IProgressListener() {

					@Override
					public void onProgress(int current, int total) {
						dialog.setIndeterminate(false);
						dialog.setMax(total);
						dialog.setProgress(current);
					}

				});
			} catch(Exception e) {
				e.printStackTrace();
				return e;
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			referenceUpdater = new TimeoStopReferenceUpdater(getActivity());
			dialog = new ProgressDialog(getActivity());

			dialog.setTitle(R.string.stop_ref_update_progress_title);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(false);
			dialog.setIndeterminate(true);
			dialog.show();
		}

		@Override
		protected void onPostExecute(Exception e) {
			dialog.hide();
			refreshAllStopSchedules(true);

			if(e != null) {
				Snackbar.make(stopsRecyclerView, R.string.stop_ref_update_error_text, Snackbar.LENGTH_LONG)
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
