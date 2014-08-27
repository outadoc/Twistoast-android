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

package fr.outadev.twistoast.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;

import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.twistoast.IStopsListContainer;
import fr.outadev.twistoast.MainActivity;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.database.TwistoastDatabase;

public class StopsListFragment extends Fragment implements IStopsListContainer {

	private ListView listView;
	private SwipeRefreshLayout swipeLayout;

	private MenuItem menuItemRefresh;

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
		swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.ptr_layout);
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refreshListFromDB(false);
			}

		});

		swipeLayout.setColorSchemeResources(R.color.twisto_primary, R.color.twisto_secondary,
				R.color.twisto_primary, R.color.twisto_secondary);

		listView = (ListView) view.findViewById(R.id.stops_list);

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
				switch(item.getItemId()) {
					case R.id.action_delete:
						// if we want to remove a bus stop, we'll have to ask a
						// confirmation
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

						// add the buttons
						builder.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// get the positions of the selected elements
								SparseBooleanArray checked = listView.getCheckedItemPositions();
								List<TimeoStop> objectsToDelete = new ArrayList<TimeoStop>();

								// add every stop we want to delete to the list
								for(int i = 0; i < checked.size(); i++) {
									if(checked.valueAt(i)) {
										objectsToDelete.add(listAdapter.getItem(checked.keyAt(i)));
									}
								}

								// DELETE EVERYTHING AAAAAAAAA-
								for(TimeoStop object : objectsToDelete) {
									databaseHandler.deleteStop(object);
									listAdapter.remove(object);
								}

								// this was a triumph, say we've deleted teh
								// stuff
								Toast.makeText(getActivity(), getResources().getString(R.string.confirm_delete_success),
										Toast.LENGTH_SHORT).show();

								mode.finish();
								refreshListFromDB(true);
							}
						});

						// on the other hand, if we don't actually want to delete
						// anything,
						// well.
						builder.setNegativeButton(R.string.confirm_no, null);

						// set dialog title
						builder.setTitle(getResources().getString(R.string.confirm_delete_title));

						// correctly set the message of the dialog
						if(listView.getCheckedItemCount() > 1) {
							builder.setMessage(String.format(getResources().getString(R.string.confirm_delete_msg_multi),
									listView.getCheckedItemCount()));
						} else {
							builder.setMessage(getResources().getString(R.string.confirm_delete_msg_single));
						}

						// create the AlertDialog and show it
						AlertDialog dialog = builder.create();
						dialog.show();

						break;
				}

				return true;
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// yay, inflation
				MenuInflater inflater = getActivity().getMenuInflater();

				inflater.inflate(R.menu.main_edit, menu);
				mode.setTitle(getActivity().getResources().getString(R.string.select_items));
				setSubtitle(mode);

				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {

			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return true;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				setSubtitle(mode);
			}

			public void setSubtitle(ActionMode mode) {
				// set the action bar subtitle accordingly
				final int checkedCount = listView.getCheckedItemCount();

				switch(checkedCount) {
					case 0:
						mode.setSubtitle(null);
						break;
					case 1:
						mode.setSubtitle(getActivity().getResources().getString(R.string.one_stop_selected));
						break;
					default:
						mode.setSubtitle(String.format(getActivity().getResources().getString(R.string.multi_stops_selected),
								checkedCount));
						break;
				}
			}
		});

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
		Log.i("Twistoast", "stopping automatic refresh, app paused");
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
		swipeLayout.setRefreshing(true);

		if(menuItemRefresh != null) {
			menuItemRefresh.setEnabled(false);
		}

		// we have to reset the adapter so it correctly loads the stops
		// if we don't do that, bugs will appear when the database has been
		// modified
		if(resetList) {
			listAdapter = new StopsListArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,
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
		swipeLayout.setRefreshing(false);

		if(menuItemRefresh != null) {
			menuItemRefresh.setEnabled(true);
		}

		Log.i("Twistoast", "refreshed, " + listAdapter.getCount() + " stops in db");

		if(getActivity() != null && listAdapter.getCount() > 0) {
			Toast.makeText(getActivity(), getResources().getString(R.string.refreshed_stops),
					Toast.LENGTH_SHORT).show();
		} else if(listAdapter.getCount() < 1) {
			Toast.makeText(getActivity(), getResources().getString(R.string.no_content), Toast.LENGTH_SHORT).show();
		}

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
