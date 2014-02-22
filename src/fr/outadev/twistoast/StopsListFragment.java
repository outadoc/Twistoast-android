package fr.outadev.twistoast;

import java.util.ArrayList;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import fr.outadev.twistoast.timeo.TimeoScheduleObject;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.widget.LinearLayout;
import android.widget.Toast;

public class StopsListFragment extends Fragment implements MultiChoiceModeListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_stops_list, container, false);

		// get pull to refresh view
		pullToRefresh = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);

		// set it up
		ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable().listener(new OnRefreshListener() {

			@Override
			public void onRefreshStarted(View view) {
				refreshListFromDB(false);
			}

		}).setup(pullToRefresh);

		listView = (ListView) view.findViewById(R.id.stops_list);

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(this);

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
		
		AdView adView = (AdView) getView().findViewById(R.id.adView);

		if(getActivity().getResources().getBoolean(R.bool.enableAds)) {
			//if we want ads, check for availability and load them
			int hasGPS = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

			if(hasGPS != ConnectionResult.SUCCESS) {
				GooglePlayServicesUtil.getErrorDialog(hasGPS, getActivity(), 1).show();
			} else {
				AdRequest adRequest = new AdRequest.Builder().addTestDevice("4A75A651AD45105DB97E1E0ECE162D0B").build();
				adView.loadAd(adRequest);
			}
		} else {
			//if we don't want ads, remove the view from the layout
			LinearLayout linLay = (LinearLayout) getView().findViewById(R.id.main_lin_layout);
			linLay.removeView(adView); 
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

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0) {
			refreshListFromDB(true);
		}
	}

	public void refreshListFromDB(boolean resetList) {
		// we don't want to try to refresh if we're already refreshing (causes
		// bugs)
		if(isRefreshing) return;
		else isRefreshing = true;

		// show the refresh animation
		pullToRefresh.setRefreshing(true);

		// we have to reset the adapter so it correctly loads the stops
		// if we don't do that, bugs will appear when the database has been
		// modified
		if(resetList) {
			listAdapter = new StopsListArrayAdapter(getActivity(), this, android.R.layout.simple_list_item_1,
			        databaseHandler.getAllStops());
			listView.setAdapter(listAdapter);
		}

		// finally, get the schedule
		listAdapter.updateScheduleData();
	}

	public void endRefresh() {
		// notify the pull to refresh view that the refresh has finished
		pullToRefresh.setRefreshComplete();
		isRefreshing = false;

		Log.i("Twistoast", "refreshed, " + listAdapter.getObjects().size() + " stops in db");

		if(getActivity() != null) {
			Toast.makeText(getActivity(), getResources().getString(R.string.refreshed_stops), Toast.LENGTH_SHORT).show();
		}

		// reset the timer loop, and start it again
		// this ensures the list is refreshed automatically every 60 seconds
		handler.removeCallbacks(runnable);
		if(!isInBackground) handler.postDelayed(runnable, REFRESH_INTERVAL);
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_delete:
				// if we want to remove a bus stop, we'll have to ask a
				// confirmation
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

				// add the buttons
				builder.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// get the positions of the selected elements
						SparseBooleanArray checked = listView.getCheckedItemPositions();
						ArrayList<TimeoScheduleObject> objectsToDelete = new ArrayList<TimeoScheduleObject>();

						// add every stop we want to delete to the list
						for(int i = 0; i < checked.size(); i++) {
							if(checked.valueAt(i)) {
								objectsToDelete.add(listAdapter.getItem(checked.keyAt(i)));
							}
						}

						// DELETE EVERYTHING AAAAAAAAA-
						for(int i = 0; i < objectsToDelete.size(); i++) {
							databaseHandler.deleteStop(objectsToDelete.get(i));
							listAdapter.remove(objectsToDelete.get(i));
						}

						// this was a triumph, say we've deleted teh
						// stuff
						Toast.makeText(getActivity(), getResources().getString(R.string.confirm_delete_success),
						        Toast.LENGTH_SHORT).show();

						mode.finish();
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

	public ListView listView;
	private PullToRefreshLayout pullToRefresh;

	private boolean isRefreshing;
	private final long REFRESH_INTERVAL = 60000L;

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
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

}
