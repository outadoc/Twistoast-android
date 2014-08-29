/*
 * Twistoast - StopsListFragment
 * Copyright (C) 2013-2014  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import fr.outadev.twistoast.IStopsListContainer;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.Utils;
import fr.outadev.twistoast.database.TwistoastDatabase;
import fr.outadev.twistoast.ui.StopsListArrayAdapter;
import fr.outadev.twistoast.ui.SwipeDismissListViewTouchListener;
import fr.outadev.twistoast.ui.activities.AddStopActivity;
import fr.outadev.twistoast.ui.activities.MainActivity;

public class StopsListFragment extends Fragment implements IStopsListContainer {

	private ListView listView;
	private SwipeRefreshLayout swipeRefreshLayout;

	private MenuItem menuItemRefresh;
	private View noContentView;

	private boolean isRefreshing;
	private final long REFRESH_INTERVAL = 60000L;

	private final Handler handler = new Handler();
	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if(autoRefresh) {
				refreshListFromDB(false);
			}
		}
	};

	private TwistoastDatabase databaseHandler;
	private StopsListArrayAdapter listAdapter;

	private boolean autoRefresh;
	private boolean isInBackground;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_stops_list, container, false);

		// get pull to refresh view
		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.ptr_layout);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refreshListFromDB(false);
			}

		});

		swipeRefreshLayout.setColorSchemeResources(R.color.twisto_primary, R.color.twisto_secondary,
				R.color.twisto_primary, R.color.twisto_secondary);

		listView = (ListView) view.findViewById(R.id.stops_list);
		noContentView = view.findViewById(R.id.view_no_content);

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {

					@Override
					public boolean canDismiss(int position) {
						return !isRefreshing;
					}

					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
						for(int position : reverseSortedPositions) {
							databaseHandler.deleteStop(listAdapter.getItem(position));
							listAdapter.remove(listAdapter.getItem(position));
						}

						if(listAdapter.isEmpty()) {
							noContentView.setVisibility(View.VISIBLE);
						}

						listAdapter.notifyDataSetChanged();
						Toast.makeText(getActivity(), R.string.confirm_delete_success, Toast.LENGTH_SHORT).show();
					}

				});

		listView.setOnTouchListener(touchListener);
		listView.setOnScrollListener(touchListener.makeScrollListener());

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// yes hello please, I'd like to be inflated?
		setHasOptionsMenu(true);

		databaseHandler = new TwistoastDatabase(getActivity());

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		autoRefresh = sharedPref.getBoolean("pref_auto_refresh", true);

		isRefreshing = false;
		isInBackground = false;
	}

	@Override
	public void onStart() {
		super.onStart();

		if(getView() != null) {
			final AdView adView = (AdView) getView().findViewById(R.id.adView);
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

			if(getActivity().getResources().getBoolean(R.bool.enableAds)) {
				// if we want ads, check for availability and load them
				int hasGPS = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

				if(hasGPS == ConnectionResult.SUCCESS) {
					AdRequest adRequest = new AdRequest.Builder()
							.addTestDevice("4A75A651AD45105DB97E1E0ECE162D0B")
							.addTestDevice("29EBDB460C20FD273BADF028945C56E2").build();
					adView.loadAd(adRequest);
				}
			} else {
				// if we don't want ads, remove the view from the layout
				adView.setVisibility(View.GONE);
			}
		}

		refreshListFromDB(true);
	}

	@Override
	public void onResume() {
		super.onResume();

		isInBackground = false;
		// when the activity is resuming, refresh
		refreshListFromDB(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		// when the activity is pausing, stop refreshing automatically
		Log.i(Utils.TAG, "stopping automatic refresh, app paused");
		isInBackground = true;
		handler.removeCallbacks(runnable);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.main, menu);
		menuItemRefresh = menu.findItem(R.id.action_refresh);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch(item.getItemId()) {
			case R.id.action_add: {
				Intent intent = new Intent(getActivity(), AddStopActivity.class);
				startActivityForResult(intent, 0);
				return true;
			}
			case R.id.action_refresh:
				// refresh the list
				refreshListFromDB(false);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0) {
			refreshListFromDB(true);
		}
	}

	public void refreshListFromDB(boolean resetList) {
		// we don't want to try to refresh if we're already refreshing (causes
		// bugs)
		if(isRefreshing) {
			return;
		} else {
			isRefreshing = true;
		}

		// show the refresh animation
		swipeRefreshLayout.setRefreshing(true);

		if(menuItemRefresh != null) {
			menuItemRefresh.setEnabled(false);
		}

		// we have to reset the adapter so it correctly loads the stops
		// if we don't do that, bugs will appear when the database has been
		// modified
		if(resetList) {
			listAdapter = new StopsListArrayAdapter(getActivity(), getActivity(), android.R.layout.simple_list_item_1,
					databaseHandler.getAllStops(), this);
			listView.setAdapter(listAdapter);
		}

		// finally, get the schedule
		listAdapter.updateScheduleData();
	}

	@Override
	public void endRefresh() {
		// notify the pull to refresh view that the refresh has finished
		isRefreshing = false;
		swipeRefreshLayout.setRefreshing(false);

		if(menuItemRefresh != null) {
			menuItemRefresh.setEnabled(true);
		}

		Log.i(Utils.TAG, "refreshed, " + listAdapter.getCount() + " stops in db");

		if(getActivity() != null && !listAdapter.isEmpty()) {
			Toast.makeText(getActivity(), getResources().getString(R.string.refreshed_stops), Toast.LENGTH_SHORT).show();
		}

		noContentView.setVisibility((listAdapter.isEmpty()) ? View.VISIBLE : View.GONE);

		// reset the timer loop, and start it again
		// this ensures the list is refreshed automatically every 60 seconds
		handler.removeCallbacks(runnable);

		if(!isInBackground) {
			handler.postDelayed(runnable, REFRESH_INTERVAL);
		}
	}

	@Override
	public void loadFragmentFromDrawerPosition(int index) {
		((MainActivity) getActivity()).loadFragmentFromDrawerPosition(index);
	}

}
