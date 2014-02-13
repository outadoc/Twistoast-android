package fr.outadev.twistoast;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;

import fr.outadev.twistoast.timeo.TimeoScheduleObject;
import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends SherlockActivity implements MultiChoiceModeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// get pull to refresh view
		pullToRefresh = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

		// set it up
		ActionBarPullToRefresh.from(this).allChildrenArePullable()
				.listener(new OnRefreshListener() {

					@Override
					public void onRefreshStarted(View view) {
						refreshListFromDB(false);
					}

				}).setup(pullToRefresh);

		listView = (ListView) findViewById(R.id.list);
		databaseHandler = new TwistoastDatabase(this);

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(this);

		isRefreshing = false;
		refreshListFromDB(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch(item.getItemId()) {
		case R.id.action_add:
			// add a new stop
			addBusStop();
			return true;
		case R.id.action_refresh:
			// refresh the list
			refreshListFromDB(false);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void addBusStop() {
		// start an intent to AddStopActivity
		Intent intent = new Intent(this, AddStopActivity.class);
		startActivityForResult(intent, 0);
	}
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if(requestCode == 0) {
            refreshListFromDB(true);
        }
    }

	@Override
	protected void onResume() {
		super.onResume();
		// when the activity is resuming, refresh
		refreshListFromDB(false);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// when the activity is pausing, stop refreshing automatically
		Log.i("twistoast", "stopping automatic refresh, app paused");
		handler.removeCallbacks(runnable);
	}

	public void refreshListFromDB(boolean resetList) {
		// we don't want to try to refresh if we're already refreshing (causes
		// bugs)
		if(isRefreshing)
			return;
		else
			isRefreshing = true;

		// show the refresh animation
		pullToRefresh.setRefreshing(true);

		// we have to reset the adapter so it correctly loads the stops
		// if we don't do that, bugs will appear when the database has been
		// modified
		if(resetList) {
			listAdapter = new TwistoastArrayAdapter(this, android.R.layout.simple_list_item_1, databaseHandler
					.getAllStops());
			listView.setAdapter(listAdapter);
		}

		// finally, get the schedule
		listAdapter.updateScheduleData();
	}

	public void endRefresh() {
		// notify the pull to refresh view that the refresh has finished
		pullToRefresh.setRefreshComplete();
		isRefreshing = false;

		Log.i("twistoast", "refreshed, " + listAdapter.getObjects().size() + " stops in db");
		Toast.makeText(this, "Horaires rafra”chis", Toast.LENGTH_SHORT).show();

		// reset the timer loop, and start it again
		// this ensures the list is refreshed automatically every 60 seconds
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, REFRESH_INTERVAL);
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, android.view.MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_delete:
			// if we want to remove a bus stop, we'll have to ask a confirmation
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

			// add the buttons
			builder.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// get the positions of the selected elements
					SparseBooleanArray checked = listView
							.getCheckedItemPositions();
					ArrayList<TimeoScheduleObject> objectsToDelete = new ArrayList<TimeoScheduleObject>();

					// add every stop we want to delete to the list
					for(int i = 0; i < checked.size(); i++) {
						if(checked.valueAt(i)) {
							objectsToDelete.add(listAdapter.getItem(checked
									.keyAt(i)));
						}
					}

					// DELETE EVERYTHING AAAAAAAAA-
					for(int i = 0; i < objectsToDelete.size(); i++) {
						databaseHandler.deleteStop(objectsToDelete.get(i));
						listAdapter.remove(objectsToDelete.get(i));
					}

					// this was a triumph, say we've deleted teh stuff
					Toast.makeText(MainActivity.this, getResources()
							.getString(R.string.confirm_delete_success), Toast.LENGTH_SHORT)
							.show();

					mode.finish();
				}
			});

			// on the other hand, if we don't actually want to delete anything,
			// well.
			builder.setNegativeButton(R.string.confirm_no, null);

			// set dialog title
			builder.setTitle(getResources()
					.getString(R.string.confirm_delete_title));

			// correctly set the message of the dialog
			if(listView.getCheckedItemCount() > 1) {
				builder.setMessage(String.format(getResources()
						.getString(R.string.confirm_delete_msg_multi), listView
						.getCheckedItemCount()));
			} else {
				builder.setMessage(getResources()
						.getString(R.string.confirm_delete_msg_single));
			}

			// create the AlertDialog and show it
			AlertDialog dialog = builder.create();
			dialog.show();

			break;
		}

		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
		// yay, inflation
		MenuInflater inflater = getSupportMenuInflater();

		inflater.inflate(R.menu.main_edit, (Menu) menu);
		mode.setTitle(MainActivity.this.getResources()
				.getString(R.string.select_items));
		setSubtitle(mode);

		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {

	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
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
			mode.setSubtitle(MainActivity.this.getResources()
					.getString(R.string.one_stop_selected));
			break;
		default:
			mode.setSubtitle(String.format(MainActivity.this.getResources()
					.getString(R.string.multi_stops_selected), checkedCount));
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
			refreshListFromDB(false);
		}
	};

	private TwistoastDatabase databaseHandler;
	private TwistoastArrayAdapter listAdapter;

}
