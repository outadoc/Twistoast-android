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

import android.app.AlertDialog;
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
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.twistoast.IStopsListContainer;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.Utils;
import fr.outadev.twistoast.database.TwistoastDatabase;
import fr.outadev.twistoast.ui.StopsListArrayAdapter;
import fr.outadev.twistoast.ui.SwipeDismissListViewTouchListener;
import fr.outadev.twistoast.ui.activities.AddStopActivity;
import fr.outadev.twistoast.ui.activities.MainActivity;

public class StopsListFragment extends Fragment implements IStopsListContainer {

	private final long REFRESH_INTERVAL = 60000L;
	private final Handler handler = new Handler();
	private ListView listView;
	private SwipeRefreshLayout swipeRefreshLayout;
	private MenuItem menuItemRefresh;
	private View noContentView;
	private FloatingActionButton fab;

	private TwistoastDatabase databaseHandler;
	private StopsListArrayAdapter listAdapter;

	private boolean autoRefresh;
	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if(autoRefresh) {
				refreshListFromDB(false);
			}
		}
	};

	private boolean isRefreshing;
	private boolean isInBackground;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0) {
			refreshListFromDB(true);
		}
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
		fab = (FloatingActionButton) view.findViewById(R.id.fab);

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {

					@Override
					public boolean canDismiss(int position) {
						return !isRefreshing;
					}

					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
						int position = reverseSortedPositions[0];

						final TimeoStop stopToDelete = listAdapter.getItem(position);

						databaseHandler.deleteStop(stopToDelete);
						listAdapter.remove(stopToDelete);

						if(listAdapter.isEmpty()) {
							noContentView.setVisibility(View.VISIBLE);
						}

						listAdapter.notifyDataSetChanged();

						Snackbar.with(getActivity())
								.text(R.string.confirm_delete_success)
								.actionLabel(R.string.cancel_stop_deletion)
								.actionColorResource(R.color.colorAccent)
								.attachToAbsListView(listView)
								.actionListener(new ActionClickListener() {

									@Override
									public void onActionClicked() {
										Log.i(Utils.TAG, "restoring stop " + stopToDelete);
										databaseHandler.addStopToDatabase(stopToDelete);
										refreshListFromDB(true);
									}

								})
								.show(getActivity());
					}

				});

		listView.setOnTouchListener(touchListener);
		listView.setOnScrollListener(touchListener.makeScrollListener());

		fab.attachToListView(listView);
		fab.setColorNormal(getResources().getColor(R.color.colorAccent));
		fab.setColorPressed(getResources().getColor(R.color.twisto_secondary));
		fab.setColorRipple(getResources().getColor(R.color.twisto_secondary));

		fab.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AddStopActivity.class);
				startActivityForResult(intent, 0);
			}

		});

		return view;
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
							.addTestDevice("1176AD77C8CCAB0BE044FA12ACD473B0").build();
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
	public void endRefresh(boolean success) {
		// notify the pull to refresh view that the refresh has finished
		isRefreshing = false;
		swipeRefreshLayout.setRefreshing(false);

		if(menuItemRefresh != null) {
			menuItemRefresh.setEnabled(true);
		}

		noContentView.setVisibility((listAdapter.isEmpty()) ? View.VISIBLE : View.GONE);

		// reset the timer loop, and start it again
		// this ensures the list is refreshed automatically every 60 seconds
		handler.removeCallbacks(runnable);

		if(!isInBackground) {
			handler.postDelayed(runnable, REFRESH_INTERVAL);
		}

		if(success) {
			Log.i(Utils.TAG, "refreshed, " + listAdapter.getCount() + " stops in db");

			if(getActivity() != null && !listAdapter.isEmpty()) {
				Toast.makeText(getActivity(), getResources().getString(R.string.refreshed_stops), Toast.LENGTH_SHORT).show();
			}

			int mismatch = listAdapter.checkSchedulesMismatchCount();

			if(mismatch > 0) {
				(new AlertDialog.Builder(getActivity()))
						.setTitle("Error")
						.setMessage("Woops, mismatch=" + mismatch)
						.create().show();
			}
		}
	}

	@Override
	public void loadFragmentFromDrawerPosition(int index) {
		((MainActivity) getActivity()).loadFragmentFromDrawerPosition(index);
	}

}
