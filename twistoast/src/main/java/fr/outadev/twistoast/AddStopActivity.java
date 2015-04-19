/*
 * Twistoast - AddStopActivity
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

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.ArrayList;
import java.util.List;

import fr.outadev.android.timeo.TimeoBlockingMessageException;
import fr.outadev.android.timeo.TimeoException;
import fr.outadev.android.timeo.TimeoIDNameObject;
import fr.outadev.android.timeo.TimeoLine;
import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoSingleSchedule;
import fr.outadev.android.timeo.TimeoStop;
import fr.outadev.android.timeo.TimeoStopSchedule;

/**
 * Activity that allows the user to add a bus stop to the app.
 *
 * @author outadoc
 */
public class AddStopActivity extends ThemedActivity {

	public static final int NO_STOP_ADDED = 0;
	public static final int STOP_ADDED = 1;

	private Spinner spinLine;
	private Spinner spinDirection;
	private Spinner spinStop;

	private List<TimeoLine> lineList;
	private List<TimeoIDNameObject> directionList;
	private List<TimeoStop> stopList;

	private List<TimeoLine> filteredLineList;

	private ArrayAdapter<TimeoLine> lineAdapter;
	private ArrayAdapter<TimeoIDNameObject> directionAdapter;
	private ArrayAdapter<TimeoStop> stopAdapter;

	private TextView lbl_line;
	private FrameLayout view_line_id;
	private TextView lbl_stop;
	private TextView lbl_direction;
	private TextView lbl_schedule_direction;

	private LinearLayout view_schedule_container;

	private MenuItem item_next;

	private Database databaseHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// we'll want to show a loading spinning wheel, we have to request that feature
		//TODO: fix this, this is broken when using AppCompat for some reason
		//supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// setup everything
		setContentView(R.layout.activity_add_stop);

		setResult(NO_STOP_ADDED);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSupportProgressBarIndeterminateVisibility(false);

		databaseHandler = new Database(DatabaseOpenHelper.getInstance(this));

		// get all the UI elements we'll need in the future

		// spinners (dropdown menus)
		spinLine = (Spinner) findViewById(R.id.spin_line);
		spinDirection = (Spinner) findViewById(R.id.spin_direction);
		spinStop = (Spinner) findViewById(R.id.spin_stop);

		//lists
		lineList = new ArrayList<>();
		directionList = new ArrayList<>();
		stopList = new ArrayList<>();

		filteredLineList = new ArrayList<>(lineList);

		// labels
		lbl_line = (TextView) findViewById(R.id.lbl_line_id);
		lbl_stop = (TextView) findViewById(R.id.lbl_stop_name);
		lbl_direction = (TextView) findViewById(R.id.lbl_direction_name);

		// line view (to set its background color)
		view_line_id = (FrameLayout) findViewById(R.id.view_line_id);

		view_schedule_container = (LinearLayout) findViewById(R.id.view_schedule_labels_container);

		spinLine.setEnabled(false);
		spinDirection.setEnabled(false);
		spinStop.setEnabled(false);

		//setup spinners here
		setupLineSpinner();
		setupDirectionSpinner();
		setupStopSpinner();

		getLinesFromAPI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_stop, menu);
		item_next = menu.findItem(R.id.action_ok);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_ok:
				// add the current stop to the database
				registerStopToDatabase();
				return true;
			case android.R.id.home:
				// go back
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Initialises, sets up the even listeners, and populates the line spinner.
	 */
	public void setupLineSpinner() {
		lineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredLineList);
		lineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinLine.setAdapter(lineAdapter);

		// when a line has been selected
		spinLine.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
				// set loading labels
				lbl_line.setText("");
				lbl_direction.setText(getResources().getString(R.string.loading_data));
				lbl_stop.setText(getResources().getString(R.string.loading_data));

				view_schedule_container.removeAllViewsInLayout();
				view_schedule_container.setVisibility(View.GONE);

				spinStop.setEnabled(false);

				if(item_next != null) {
					item_next.setEnabled(false);
				}

				// get the selected line
				TimeoLine item = getCurrentLine();

				if(item != null && item.getId() != null) {
					// set the line view
					lbl_line.setText(item.getId());

					GradientDrawable lineDrawable = (GradientDrawable) view_line_id.getBackground();
					lineDrawable.setColor(Colors.getBrighterColor(Color.parseColor(item.getColor())));

					spinDirection.setEnabled(true);

					// adapt the size based on the size of the line ID
					if(lbl_line.getText().length() > 3) {
						lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					} else if(lbl_line.getText().length() > 2) {
						lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
					} else {
						lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
					}

					directionList.clear();
					directionList.addAll(getDirectionsList());
					setupDirectionSpinner();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});
	}

	/**
	 * Initialises, sets up the even listeners, and populates the direction spinner.
	 */
	public void setupDirectionSpinner() {
		directionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, directionList);
		directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinDirection.setAdapter(directionAdapter);

		// when a line has been selected
		spinDirection.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
				// set loading labels
				lbl_direction.setText(getResources().getString(R.string.loading_data));
				view_schedule_container.removeAllViewsInLayout();
				view_schedule_container.setVisibility(View.GONE);

				item_next.setEnabled(false);
				spinStop.setEnabled(false);

				if(getCurrentLine() != null && getCurrentDirection() != null && getCurrentLine().getId() != null
						&& getCurrentDirection().getId() != null) {
					lbl_direction.setText(getResources().getString(R.string.direction_name, getCurrentDirection().getName()));

					(new AsyncTask<Void, Void, List<TimeoStop>>() {

						@Override
						protected void onPreExecute() {
							setSupportProgressBarIndeterminateVisibility(true);
						}

						@Override
						protected List<TimeoStop> doInBackground(Void... voids) {
							try {
								getCurrentLine().setDirection(getCurrentDirection());
								return TimeoRequestHandler.getStops(getCurrentLine());
							} catch(Exception e) {
								handleAsyncExceptions(e);
							}

							return null;
						}

						@Override
						protected void onPostExecute(List<TimeoStop> timeoStops) {
							setSupportProgressBarIndeterminateVisibility(false);

							if(timeoStops != null) {
								spinStop.setEnabled(true);

								stopList.clear();
								stopList.addAll(timeoStops);
								setupStopSpinner();
							}
						}

					}).execute();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});
	}

	/**
	 * Initialises, sets up the even listeners, and populates the stop spinner.
	 */
	public void setupStopSpinner() {
		stopAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stopList);
		stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinStop.setAdapter(stopAdapter);

		// when a stop has been selected
		spinStop.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
				lbl_stop.setText(getResources().getString(R.string.loading_data));
				view_schedule_container.removeAllViewsInLayout();
				view_schedule_container.setVisibility(View.GONE);

				TimeoIDNameObject stop = getCurrentStop();
				item_next.setEnabled(true);

				if(stop != null && stop.getId() != null) {
					lbl_stop.setText(getResources().getString(R.string.stop_name, stop.getName()));
					updateSchedulePreview();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});
	}

	public void updateSchedulePreview() {
		(new AsyncTask<Void, Void, TimeoStopSchedule>() {

			@Override
			protected void onPreExecute() {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected TimeoStopSchedule doInBackground(Void... voids) {
				try {
					return TimeoRequestHandler.getSingleSchedule(getCurrentStop());
				} catch(Exception e) {
					handleAsyncExceptions(e);
				}

				return null;
			}

			@Override
			protected void onPostExecute(TimeoStopSchedule schedule) {
				setSupportProgressBarIndeterminateVisibility(false);
				LayoutInflater inflater = (LayoutInflater) AddStopActivity.this.getSystemService(Context
						.LAYOUT_INFLATER_SERVICE);

				if(schedule != null) {
					List<TimeoSingleSchedule> schedList = schedule.getSchedules();

					// set the schedule labels, if we need to
					if(schedList != null) {
						for(TimeoSingleSchedule currSched : schedList) {
							View singleScheduleView = inflater.inflate(R.layout.single_schedule_label, null);

							TextView lbl_schedule = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule);
							TextView lbl_schedule_direction = (TextView) singleScheduleView.findViewById(R.id
									.lbl_schedule_direction);

							lbl_schedule.setText(currSched.getFormattedTime(AddStopActivity.this));
							lbl_schedule_direction.setText(" — " + currSched.getDirection());

							view_schedule_container.addView(singleScheduleView);
						}

						if(schedList.size() > 0) {
							view_schedule_container.setVisibility(View.VISIBLE);
						}
					}
				}
			}

		}).execute();
	}

	/**
	 * Fetches the bus lines from the API, and populates the line spinner when done.
	 */
	public void getLinesFromAPI() {

		// fetch the directions
		(new AsyncTask<Void, Void, List<TimeoLine>>() {

			@Override
			protected void onPreExecute() {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected List<TimeoLine> doInBackground(Void... voids) {
				try {
					return TimeoRequestHandler.getLines();
				} catch(Exception e) {
					handleAsyncExceptions(e);
				}

				return null;
			}

			@Override
			protected void onPostExecute(List<TimeoLine> timeoLines) {
				setSupportProgressBarIndeterminateVisibility(false);

				if(timeoLines != null) {
					lineList.clear();
					lineList.addAll(timeoLines);

					filteredLineList.clear();
					filteredLineList.addAll(lineList);

					spinLine.setEnabled(true);
					spinDirection.setEnabled(true);

					for(int i = filteredLineList.size() - 1; i >= 0; i--) {
						//if the last line in the list is the same line (but with a different direction)
						if(i > 0 && filteredLineList.get(i).getId().equals(filteredLineList.get(i - 1).getDetails()
								.getId())) {
							filteredLineList.remove(i);
						}
					}

					lineAdapter.notifyDataSetChanged();
				}
			}

		}).execute();
	}

	/**
	 * Displays an exception in a toast on the UI thread.
	 *
	 * @param e the exception to display
	 */
	public void handleAsyncExceptions(final Exception e) {
		e.printStackTrace();

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if(e instanceof TimeoBlockingMessageException) {
					((TimeoBlockingMessageException) e).getAlertMessage(AddStopActivity.this).show();
				} else {
					String message;

					if(e instanceof TimeoException) {
						message = getString(R.string.error_toast_twisto, ((TimeoException) e).getErrorCode());
					} else {
						message = getString(R.string.loading_error);
					}

					Snackbar.with(AddStopActivity.this)
							.text(message)
							.actionLabel(R.string.error_retry)
							.actionColor(Colors.getColorAccent(AddStopActivity.this))
							.actionListener(new ActionClickListener() {

								@Override
								public void onActionClicked() {
									getLinesFromAPI();
								}

							})
							.show(AddStopActivity.this);
				}
			}

		});
	}

	/**
	 * Stores the selected bus stop in the database.
	 */
	public void registerStopToDatabase() {
		TimeoStop stop = getCurrentStop();

		try {
			databaseHandler.addStopToDatabase(stop);
			Toast.makeText(this, getResources().getString(R.string.added_toast, stop.toString()), Toast.LENGTH_SHORT).show();
			setResult(STOP_ADDED);
			finish();
		} catch(SQLiteConstraintException e) {
			// stop already in database
			Toast.makeText(this, getResources().getString(R.string.error_toast, getResources().getString(R.string
					.add_error_duplicate)), Toast.LENGTH_LONG).show();
		} catch(IllegalArgumentException e) {
			// one of the fields was null
			Toast.makeText(this, getResources().getString(R.string.error_toast, getResources().getString(R.string
					.add_error_illegal_argument)), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Gets a list of directions for the selected bus line, as they're stored in the same object.
	 *
	 * @return a list of ID/name objects containing the id and name of the directions to display
	 */
	private List<TimeoIDNameObject> getDirectionsList() {
		List<TimeoIDNameObject> directionsList = new ArrayList<>();

		for(TimeoLine line : lineList) {
			if(line.getId().equals(getCurrentLine().getId())) {
				directionsList.add(line.getDirection());
			}
		}

		return directionsList;
	}

	/**
	 * Gets the bus stop that's currently selected.
	 *
	 * @return a stop
	 */
	public TimeoStop getCurrentStop() {
		return (TimeoStop) spinStop.getItemAtPosition(spinStop.getSelectedItemPosition());
	}

	/**
	 * Gets the bus line direction that's currently selected.
	 *
	 * @return an ID/name object for the direction
	 */
	public TimeoIDNameObject getCurrentDirection() {
		return (TimeoIDNameObject) spinDirection.getItemAtPosition(spinDirection.getSelectedItemPosition());
	}

	/**
	 * Gets the bus line that's currently selected.
	 *
	 * @return a line
	 */
	public TimeoLine getCurrentLine() {
		return (TimeoLine) spinLine.getItemAtPosition(spinLine.getSelectedItemPosition());
	}

}
