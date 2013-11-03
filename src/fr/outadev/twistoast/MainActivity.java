package fr.outadev.twistoast;

import java.util.ArrayList;

import fr.outadev.twistoast.timeo.TimeoScheduleObject;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.list);
		databaseHandler = new TwistoastDatabase(this);

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new ModeCallback());

		ArrayList<TimeoScheduleObject> stopsList = databaseHandler.getAllStops();
		listAdapter = new TwistoastArrayAdapter(this, android.R.layout.simple_list_item_activated_1, stopsList);
		listView.setAdapter(listAdapter);
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
			listAdapter.updateScheduleData(true);
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
		listAdapter.clear();
		ArrayList<TimeoScheduleObject> stopsList = databaseHandler.getAllStops();
		listAdapter = new TwistoastArrayAdapter(this, android.R.layout.simple_list_item_1, stopsList);
		listView.setAdapter(listAdapter);
	}

	private ListView listView;

	private TwistoastDatabase databaseHandler;
	private TwistoastArrayAdapter listAdapter;

	private class ModeCallback implements ListView.MultiChoiceModeListener {

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.main_edit, menu);
			mode.setTitle(MainActivity.this.getResources().getString(R.string.select_items));
			setSubtitle(mode);
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
			switch(item.getItemId()) {
				case R.id.action_delete:
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					
					// Add the buttons
					builder.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.confirm_delete_success), Toast.LENGTH_SHORT).show();
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
					builder.setTitle(MainActivity.this.getResources().getString(R.string.confirm_delete_title));
					
					if(listView.getCheckedItemCount() > 1) {
						builder.setMessage(String.format(MainActivity.this.getResources().getString(R.string.confirm_delete_msg_multi), listView.getCheckedItemCount()));
					} else {
						builder.setMessage(MainActivity.this.getResources().getString(R.string.confirm_delete_msg_single));
					}
					
					// Create the AlertDialog
					AlertDialog dialog = builder.create();
					dialog.show();
					
					break;
			}

			return true;
		}

		public void onDestroyActionMode(ActionMode mode) {
		}

		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			setSubtitle(mode);
		}

		private void setSubtitle(ActionMode mode) {
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
	}

}
