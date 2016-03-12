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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.List;

import fr.outadev.android.transport.timeo.TimeoStop;
import fr.outadev.twistoast.background.NextStopAlarmReceiver;
import fr.outadev.twistoast.utils.Utils;

public class FragmentRealtime extends Fragment implements IStopsListContainer {

    //Refresh automatically every 60 seconds.
    private static final long REFRESH_INTERVAL = 60000L;

    private final Handler mPeriodicRefreshHandler = new Handler();
    private Runnable mPeriodicRefreshRunnable;

    private RecyclerView mStopsRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mNoContentView;
    private FloatingActionButton mFloatingActionButton;

    private List<TimeoStop> mStopList;

    private Database mDatabaseHandler;
    private ConfigurationManager mConfig;
    private RecyclerAdapterRealtime mListAdapter;

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
                if (mConfig.getAutoRefresh()) {
                    refreshAllStopSchedules(false);
                }
            }
        };

        mIsRefreshing = false;
        mIsInBackground = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);
        mConfig = new ConfigurationManager(getActivity());

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

        setupListeners();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.realtime_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sortby_line:
                mConfig.setListSortOrder("line");
                refreshAllStopSchedules(true);
                return true;
            case R.id.sortby_stop:
                mConfig.setListSortOrder("stop");
                refreshAllStopSchedules(true);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
            Database.SortBy criteria = Utils.getSortCriteria(mConfig.getListSortOrder());

            mStopList = mDatabaseHandler.getAllStops(criteria);
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

        Log.i(Utils.TAG, "refreshed, " + mListAdapter.getItemCount() + " stops in db");
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
        ((ActivityMain) getActivity()).loadFragmentForDrawerItem(index);
    }

}
