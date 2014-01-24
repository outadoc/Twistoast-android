package fr.outadev.twistoast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import fr.outadev.twistoast.timeo.TimeoScheduleObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.Toast;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends Activity implements MultiChoiceModeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Now find the PullToRefreshLayout to setup
	    mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
	    
	    // Now setup the PullToRefreshLayout
	    ActionBarPullToRefresh.from(this)
	            // Mark All Children as pullable
	            .allChildrenArePullable()
	            // Set the OnRefreshListener
	            .listener(new OnRefreshListener() {

					@Override
					public void onRefreshStarted(View view) {
						// TODO Auto-generated method stub
						refreshListFromDB();
					}
	            	
	            })
	            // Finally commit the setup to our PullToRefreshLayout
	            .setup(mPullToRefreshLayout);

		listView = (ListView) findViewById(R.id.list);
		databaseHandler = new TwistoastDatabase(this);

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(this);
		
		isRefreshing = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch(item.getItemId()) {
		case R.id.action_add:
			addBusStop();
			return true;
		case R.id.action_refresh:
			refreshListFromDB();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void addBusStop() {
		Intent intent = new Intent(this, AddStopActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		refreshListFromDB();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshListFromDB();
	}

	public void refreshListFromDB() {
		if(isRefreshing) return;
		else isRefreshing = true;
		
		mPullToRefreshLayout.setRefreshing(true);
				
		listAdapter = new TwistoastArrayAdapter(this, android.R.layout.simple_list_item_1, databaseHandler.getAllStops());
		listView.setAdapter(listAdapter);
		listAdapter.updateScheduleData();
	}
	
	public void endRefresh() {
		// Notify PullToRefreshLayout that the refresh has finished
        mPullToRefreshLayout.setRefreshComplete();
        isRefreshing = false;
        
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, REFRESH_INTERVAL);
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_delete:
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				
				// Add the buttons
				builder.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Toast.makeText(MainActivity.this, getResources().getString(R.string.confirm_delete_success), Toast.LENGTH_SHORT).show();
						SparseBooleanArray checked = listView.getCheckedItemPositions();
						ArrayList<TimeoScheduleObject> objectsToDelete = new ArrayList<TimeoScheduleObject>();
						
						for(int i = 0; i < checked.size(); i++) {
							if(checked.valueAt(i)) {
								objectsToDelete.add(listAdapter.getItem(checked.keyAt(i)));
							}
						}
						
						for(int i = 0; i < objectsToDelete.size(); i++) {
							databaseHandler.deleteStop(objectsToDelete.get(i));
							listAdapter.remove(objectsToDelete.get(i));
						}
						
						mode.finish();
					}
				});
				
				builder.setNegativeButton(R.string.confirm_no, null);
				
				// Set other dialog properties
				builder.setTitle(getResources().getString(R.string.confirm_delete_title));
				
				if(listView.getCheckedItemCount() > 1) {
					builder.setMessage(String.format(getResources().getString(R.string.confirm_delete_msg_multi), listView.getCheckedItemCount()));
				} else {
					builder.setMessage(getResources().getString(R.string.confirm_delete_msg_single));
				}
				
				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
				
				break;
		}
		
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_edit, menu);
		mode.setTitle(MainActivity.this.getResources().getString(R.string.select_items));
		setSubtitle(mode);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		// TODO Auto-generated method stub
		setSubtitle(mode);
	}
	
	public void setSubtitle(ActionMode mode) {
		final int checkedCount = listView.getCheckedItemCount();
		switch(checkedCount) {
		case 0:
			mode.setSubtitle(null);
			break;
		case 1:
			mode.setSubtitle(MainActivity.this.getResources().getString(
					R.string.one_stop_selected));
			break;
		default:
			mode.setSubtitle(String
					.format(MainActivity.this.getResources().getString(
							R.string.multi_stops_selected), checkedCount));
			break;
		}
	}

	public ListView listView;
	private PullToRefreshLayout mPullToRefreshLayout;
	
	private boolean isRefreshing;
	private final long REFRESH_INTERVAL = 60000L;
	
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable(){
	    public void run() {
	        refreshListFromDB();
	    }
	};

	private TwistoastDatabase databaseHandler;
	private TwistoastArrayAdapter listAdapter;
	
}
