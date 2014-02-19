package fr.outadev.twistoast;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.json.*;

import fr.outadev.twistoast.timeo.TimeoIDNameObject;
import fr.outadev.twistoast.timeo.TimeoRequestHandler;
import fr.outadev.twistoast.timeo.TimeoRequestObject;
import fr.outadev.twistoast.timeo.TimeoResultParser;
import fr.outadev.twistoast.timeo.TimeoRequestHandler.EndPoints;
import fr.outadev.twistoast.timeo.TimeoScheduleObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class AddStopActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// we'll want to show a loading spinning wheel, we have to request that
		// feature
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// setup everything
		setContentView(R.layout.activity_add_stop);
		setProgressBarIndeterminateVisibility(false);

		databaseHandler = new TwistoastDatabase(this);

		// get all the UI elements we'll need in the future

		// spinners (dropdown menus)
		spinLine = (Spinner) findViewById(R.id.spin_line);
		spinDirection = (Spinner) findViewById(R.id.spin_direction);
		spinStop = (Spinner) findViewById(R.id.spin_stop);

		spinLine.setEnabled(false);
		spinDirection.setEnabled(false);
		spinStop.setEnabled(false);

		// labels
		lbl_line = (TextView) findViewById(R.id.lbl_line_id);
		lbl_stop = (TextView) findViewById(R.id.lbl_stop_name);
		lbl_direction = (TextView) findViewById(R.id.lbl_direction_name);

		// line view (to set its background color)
		view_line_id = (FrameLayout) findViewById(R.id.view_line_id);

		// schedule labels, most important of all
		lbl_schedule_1 = (TextView) findViewById(R.id.lbl_schedule_1);
		lbl_schedule_2 = (TextView) findViewById(R.id.lbl_schedule_2);

		// start fetching
		fetchDataFromAPI(EndPoints.LINES, (new TimeoRequestObject()));
	}

	@Override
	public void onStart() {
		super.onStart();

		spinLine.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
				// set loading labels
				lbl_line.setText(getResources().getString(R.string.unknown_line_id));
				lbl_direction.setText(getResources().getString(R.string.loading_data));
				lbl_stop.setText(getResources().getString(R.string.loading_data));

				lbl_schedule_1.setText(getResources().getString(R.string.loading_data));
				lbl_schedule_2.setText(getResources().getString(R.string.loading_data));

				// disable adapters
				spinDirection.setEnabled(false);
				spinStop.setEnabled(false);

				item_next.setEnabled(false);

				// get the selected line
				TimeoIDNameObject item = getCurrentLine();

				if(item != null && item.getId() != null) {
					// set the line view
					lbl_line.setText(item.getId());
					view_line_id.setBackgroundColor(TwistoastDatabase.getColorFromLineID(item.getId()));

					// adapt the size based on the size of the line ID
					if(lbl_line.getText().length() > 3) {
						lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					} else if(lbl_line.getText().length() > 2) {
						lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
					} else {
						lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
					}

					// fetch the directions
					fetchDataFromAPI(EndPoints.DIRECTIONS, (new TimeoRequestObject(item.getId())));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});

		spinDirection.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
				// set loading labels
				lbl_direction.setText(getResources().getString(R.string.loading_data));
				lbl_schedule_1.setText(getResources().getString(R.string.loading_data));
				lbl_schedule_2.setText(getResources().getString(R.string.loading_data));

				// disable adapters
				spinStop.setEnabled(false);

				item_next.setEnabled(false);

				if(getCurrentLine() != null && getCurrentDirection() != null && getCurrentLine().getId() != null && getCurrentDirection().getId() != null) {
					lbl_direction.setText(getResources().getString(R.string.direction_name, getCurrentDirection().getName()));
					fetchDataFromAPI(EndPoints.STOPS, (new TimeoRequestObject(getCurrentLine().getId(), getCurrentDirection().getId())));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});

		spinStop.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
				lbl_stop.setText(getResources().getString(R.string.loading_data));
				lbl_schedule_1.setText(getResources().getString(R.string.loading_data));
				lbl_schedule_2.setText(getResources().getString(R.string.loading_data));

				TimeoIDNameObject stop = getCurrentStop();
				TimeoIDNameObject line = getCurrentLine();
				TimeoIDNameObject direction = getCurrentDirection();

				item_next.setEnabled(true);

				if(line != null && direction != null && stop != null && line.getId() != null && direction.getId() != null && stop
						.getId() != null) {
					lbl_stop.setText(getResources().getString(R.string.stop_name, stop.getName()));

					fetchDataFromAPI(EndPoints.SCHEDULE, (new TimeoRequestObject(line.getId(), direction.getId(), stop.getId())));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_stop, menu);
		item_next = (MenuItem) menu.findItem(R.id.action_ok);
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void registerStopToDatabase() {
		TimeoIDNameObject stop = getCurrentStop();
		TimeoIDNameObject line = getCurrentLine();
		TimeoIDNameObject direction = getCurrentDirection();
		
		TwistoastDatabase.DBStatus status = databaseHandler.addStopToDatabase(line, direction, stop);

		if(status != TwistoastDatabase.DBStatus.SUCCESS) {
			// meh, something went wrong
			Toast.makeText(this, getResources().getString(R.string.error_toast, status), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, getResources()
					.getString(R.string.added_toast, line.getName(), direction.getName(), stop.getName()), Toast.LENGTH_SHORT)
					.show();
			this.finish();
		}
	}

	public void fetchDataFromAPI(final EndPoints endPoint, final TimeoRequestObject data) {
		setProgressBarIndeterminateVisibility(true);

		final TimeoRequestHandler handler = new TimeoRequestHandler();

		if(endPoint == EndPoints.SCHEDULE) {
			new AsyncTask<Void, Void, TimeoScheduleObject>() {
				@Override
				protected TimeoScheduleObject doInBackground(Void... params) {
					
					try {
						try {
							return handler.getSingleSchedule(new TimeoScheduleObject(getCurrentStop(), getCurrentLine(), getCurrentDirection(), null));
						} catch(ClassCastException e) {
							AddStopActivity.this.runOnUiThread(new Runnable() {
								public void run() {
									try {
										TimeoResultParser.displayErrorMessageFromTextResult(handler.getLastWebResponse(), (Activity) AddStopActivity.this);
									} catch(JSONException e) {
										Toast.makeText(AddStopActivity.this, handler.getLastWebResponse(), Toast.LENGTH_LONG)
												.show();
										e.printStackTrace();
									}
								}
							});
						}
					} catch(JSONException e) {
						AddStopActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(AddStopActivity.this, handler.getLastWebResponse(), Toast.LENGTH_LONG).show();
							}
						});
					} catch(final Exception e) {
						if(e instanceof IOException || e instanceof SocketTimeoutException) {
							AddStopActivity.this.runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(AddStopActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
								}
							});
						}

						e.printStackTrace();
					}

					return null;
				}

				@Override
				protected void onPostExecute(TimeoScheduleObject result) {
					setProgressBarIndeterminateVisibility(false);

					String[] scheduleArray = result.getSchedule();

					// set the schedule labels, if we need to
					if(scheduleArray != null) {
						if(scheduleArray[0] != null)
							lbl_schedule_1.setText("- " + scheduleArray[0]);
						if(scheduleArray[1] != null)
							lbl_schedule_2.setText("- " + scheduleArray[1]);
						else
							lbl_schedule_2.setText("");
					}

					result.setSchedule(scheduleArray);
				}

			}.execute();
		} else {
			// start loading the requested data
			new AsyncTask<Void, Void, ArrayList<TimeoIDNameObject>>() {
				@Override
				protected ArrayList<TimeoIDNameObject> doInBackground(Void... params) {

					if(endPoint == EndPoints.LINES || endPoint == EndPoints.DIRECTIONS || endPoint == EndPoints.STOPS || endPoint == EndPoints.SCHEDULE) {
						try {
							try {
								if(endPoint == EndPoints.LINES) {
									spinner = spinLine;
									return handler.getLines(data);
								} else if(endPoint == EndPoints.DIRECTIONS) {
									spinner = spinDirection;
									return handler.getDirections(data);
								} else if(endPoint == EndPoints.STOPS) {
									spinner = spinStop;
									return handler.getStops(data);
								}

							} catch(ClassCastException e) {
								AddStopActivity.this.runOnUiThread(new Runnable() {
									public void run() {
										try {
											TimeoResultParser.displayErrorMessageFromTextResult(handler.getLastWebResponse(), (Activity) AddStopActivity.this);
										} catch(JSONException e) {
											Toast.makeText(AddStopActivity.this, handler.getLastWebResponse(), Toast.LENGTH_LONG)
													.show();
											e.printStackTrace();
										}
									}
								});
							}
						} catch(JSONException e) {
							AddStopActivity.this.runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(AddStopActivity.this, handler.getLastWebResponse(), Toast.LENGTH_LONG).show();
								}
							});
						} catch(final Exception e) {
							if(e instanceof IOException || e instanceof SocketTimeoutException) {
								AddStopActivity.this.runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(AddStopActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
									}
								});
							}

							e.printStackTrace();
						}
					}

					return null;
				}

				@Override
				protected void onPostExecute(ArrayList<TimeoIDNameObject> result) {
					setProgressBarIndeterminateVisibility(false);

					// when we're done loading
					if((endPoint == EndPoints.LINES || endPoint == EndPoints.DIRECTIONS || endPoint == EndPoints.STOPS) && spinner != null) {
						if(result != null) {
							// load the data into our ArrayAdapter to
							// populate the list
							ArrayAdapter<TimeoIDNameObject> adapter = new ArrayAdapter<TimeoIDNameObject>(AddStopActivity.this, android.R.layout.simple_spinner_item, result
									.toArray(new TimeoIDNameObject[result.size()]));

							adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							spinner.setAdapter(adapter);
							spinner.setEnabled(true);
						} else {
							Toast.makeText(AddStopActivity.this, handler.getLastWebResponse(), Toast.LENGTH_LONG).show();
						}
					}
				}

				private Spinner spinner;

			}.execute();
		}
	}
	
	public TimeoIDNameObject getCurrentStop() {
		return (TimeoIDNameObject) spinStop.getItemAtPosition(spinStop.getSelectedItemPosition());
	}
	
	public TimeoIDNameObject getCurrentDirection() {
		return (TimeoIDNameObject) spinDirection.getItemAtPosition(spinDirection.getSelectedItemPosition());
	}
	
	public TimeoIDNameObject getCurrentLine() {
		return (TimeoIDNameObject) spinLine.getItemAtPosition(spinLine.getSelectedItemPosition());
	}

	private Spinner spinLine;
	private Spinner spinDirection;
	private Spinner spinStop;

	private TextView lbl_line;
	private FrameLayout view_line_id;
	private TextView lbl_stop;
	private TextView lbl_direction;
	private TextView lbl_schedule_1;
	private TextView lbl_schedule_2;

	private MenuItem item_next;

	TwistoastDatabase databaseHandler;

}
