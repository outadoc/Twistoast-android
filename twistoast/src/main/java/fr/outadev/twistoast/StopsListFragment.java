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

package fr.outadev.twistoast;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import java.util.List;

import fr.outadev.android.timeo.ProgressListener;
import fr.outadev.android.timeo.TimeoStop;

public class StopsListFragment extends Fragment implements StopsListContainer {

	//Refresh automatically every 60 seconds.
	private final long REFRESH_INTERVAL = 60000L;

	private final Handler periodicRefreshHandler = new Handler();
	private Runnable periodicRefreshRunnable;

	private ListView listView;
	private SwipeRefreshLayout swipeRefreshLayout;
	private View noContentView;

	List<TimeoStop> stops;

	private TwistoastDatabase databaseHandler;
	private StopsListArrayAdapter listAdapter;
	private boolean autoRefresh;

	private boolean isRefreshing;
	private boolean isInBackground;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0 && resultCode == AddStopActivity.STOP_ADDED) {
			refreshAllStopSchedules(true);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// yes hello please, I'd like to be inflated?
		setHasOptionsMenu(true);

		databaseHandler = new TwistoastDatabase(TwistoastDatabaseOpenHelper.getInstance(getActivity()));

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
		View view = inflater.inflate(R.layout.fragment_stops_list, container, false);

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

		listView = (ListView) view.findViewById(R.id.stops_list);
		noContentView = view.findViewById(R.id.view_no_content);
		final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				if(!isRefreshing) {
					final TimeoStop stopToDelete = listAdapter.getItem(position);

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setItems(R.array.stop_actions, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							if(which == 0) {
								databaseHandler.deleteStop(stopToDelete);
								listAdapter.remove(stopToDelete);

								if(listAdapter.isEmpty()) {
									noContentView.setVisibility(View.VISIBLE);
								}

								listAdapter.notifyDataSetChanged();
								fab.show(true);

								Snackbar.with(getActivity())
										.text(R.string.confirm_delete_success)
										.actionLabel(R.string.cancel_stop_deletion)
										.actionColor(Colors.getColorAccent(getActivity()))
										.attachToAbsListView(listView)
										.actionListener(new ActionClickListener() {

											@Override
											public void onActionClicked() {
												Log.i(Utils.TAG, "restoring stop " + stopToDelete);
												databaseHandler.addStopToDatabase(stopToDelete);
												listAdapter.insert(stopToDelete, position);
												listAdapter.notifyDataSetChanged();
											}

										})
										.show(getActivity());
							}
						}

					});

					builder.show();
				}

				return true;
			}

		});

		fab.attachToListView(listView);
		fab.setColorNormal(Colors.getColorAccent(getActivity()));
		fab.setColorPressedResId(R.color.twisto_secondary);
		fab.setColorRippleResId(R.color.twisto_secondary);

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

	/**
	 * Refreshes the list's schedules and displays them to the user.
	 *
	 * @param reloadFromDatabase true if we want to reload the stops completely, or false if we only want
	 *                           to update the schedules
	 */
	public void refreshAllStopSchedules(boolean reloadFromDatabase) {
		// we don't want to try to refresh if we're already refreshing (causes
		// bugs)
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
			listAdapter = new StopsListArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, stops, this);
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

		noContentView.setVisibility((listAdapter.isEmpty()) ? View.VISIBLE : View.GONE);

		// reset the timer loop, and start it again
		// this ensures the list is refreshed automatically every 60 seconds
		periodicRefreshHandler.removeCallbacks(periodicRefreshRunnable);

		if(!isInBackground) {
			periodicRefreshHandler.postDelayed(periodicRefreshRunnable, REFRESH_INTERVAL);
		}

		if(success) {
			Log.i(Utils.TAG, "refreshed, " + listAdapter.getCount() + " stops in db");

			if(getActivity() != null && !listAdapter.isEmpty()) {
				Toast.makeText(getActivity(), getResources().getString(R.string.refreshed_stops), Toast.LENGTH_SHORT).show();
			}

			int mismatch = listAdapter.checkSchedulesMismatchCount();

			if(mismatch > 0) {
				Snackbar.with(getActivity())
						.text(R.string.stop_ref_update_message_title)
						.actionLabel(R.string.stop_ref_update_message_action)
						.actionColor(Colors.getColorAccent(getActivity()))
						.actionListener(new ActionClickListener() {

							@Override
							public void onActionClicked() {
								(new ReferenceUpdateTask()).execute();
							}

						})
						.duration(7000)
						.swipeToDismiss(true)
						.show(getActivity());
			}
		}
	}

	@Override
	public void loadFragmentFromDrawerPosition(int index) {
		((MainActivity) getActivity()).loadFragmentFromDrawerPosition(index);
	}

	private class ReferenceUpdateTask extends AsyncTask<Void, Void, Exception> {

		private ProgressDialog dialog;
		private TimeoStopReferenceUpdater referenceUpdater;

		@Override
		protected Exception doInBackground(Void... params) {
			try {
				referenceUpdater.updateAllStopReferences(stops, new ProgressListener() {

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
				Snackbar.with(getActivity())
						.text(R.string.stop_ref_update_error_text)
						.actionLabel(R.string.error_retry)
						.actionColor(Colors.getColorAccent(getActivity()))
						.attachToAbsListView(listView)
						.actionListener(new ActionClickListener() {

							@Override
							public void onActionClicked() {
								(new ReferenceUpdateTask()).execute();
							}

						})
						.show(getActivity());
			}
		}
	}

}
