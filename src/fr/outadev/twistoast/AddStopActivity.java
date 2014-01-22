package fr.outadev.twistoast;

import java.util.ArrayList;

import org.json.*;

import fr.outadev.twistoast.timeo.TimeoIDNameObject;
import fr.outadev.twistoast.timeo.TimeoRequestHandler;
import fr.outadev.twistoast.timeo.TimeoRequestObject;
import fr.outadev.twistoast.timeo.TimeoResultParser;
import fr.outadev.twistoast.timeo.TimeoRequestHandler.EndPoints;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

public class AddStopActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_add_stop);
		setupActionBar();

		setProgressBarIndeterminateVisibility(false);

		databaseHandler = new TwistoastDatabase(this);

		spinLine = (Spinner) findViewById(R.id.spin_line);
		spinDirection = (Spinner) findViewById(R.id.spin_direction);
		spinStop = (Spinner) findViewById(R.id.spin_stop);

		fetchDataFromAPI(EndPoints.LINES, (new TimeoRequestObject()));
	}

	@Override
	public void onStart() {
		super.onStart();

		spinLine.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View view,
					int position, long id) {
				TimeoIDNameObject item = (TimeoIDNameObject) parentView
						.getItemAtPosition(position);

				if(item.getId() != null) {
					fetchDataFromAPI(EndPoints.DIRECTIONS,
							(new TimeoRequestObject(item.getId())));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});

		spinDirection.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View view,
					int position, long id) {
				TimeoIDNameObject direction = (TimeoIDNameObject) parentView
						.getItemAtPosition(position);
				TimeoIDNameObject line = (TimeoIDNameObject) spinLine
						.getItemAtPosition(spinLine.getSelectedItemPosition());

				if(line.getId() != null && direction.getId() != null) {
					fetchDataFromAPI(EndPoints.STOPS, (new TimeoRequestObject(
							line.getId(), direction.getId())));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});

		spinStop.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View view,
					int position, long id) {
				TimeoIDNameObject stop = (TimeoIDNameObject) spinStop
						.getItemAtPosition(spinStop.getSelectedItemPosition());
				TimeoIDNameObject line = (TimeoIDNameObject) spinLine
						.getItemAtPosition(spinLine.getSelectedItemPosition());
				TimeoIDNameObject direction = (TimeoIDNameObject) spinDirection
						.getItemAtPosition(spinDirection
								.getSelectedItemPosition());

				if(line.getId() != null && direction.getId() != null
						&& stop.getId() != null) {
					fetchDataFromAPI(
							EndPoints.SCHEDULE,
							(new TimeoRequestObject(line.getId(), direction
									.getId(), stop.getId())));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_stop, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_ok:
			registerStopToDatabase();
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void registerStopToDatabase() {
		TimeoIDNameObject line = (TimeoIDNameObject) spinLine
				.getItemAtPosition(spinLine.getSelectedItemPosition());
		TimeoIDNameObject direction = (TimeoIDNameObject) spinDirection
				.getItemAtPosition(spinDirection.getSelectedItemPosition());
		TimeoIDNameObject stop = (TimeoIDNameObject) spinStop
				.getItemAtPosition(spinStop.getSelectedItemPosition());

		TwistoastDatabase.DBStatus status = databaseHandler.addStopToDatabase(
				line, direction, stop);

		if(status != TwistoastDatabase.DBStatus.SUCCESS) {
			Toast.makeText(this, "Erreur : " + status, Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(
					this,
					"Ajouté : " + line.getName() + " - " + direction.getName()
							+ " - " + stop.getName(), Toast.LENGTH_SHORT)
					.show();
			this.finish();
		}
	}

	public void fetchDataFromAPI(EndPoints endPoint, TimeoRequestObject data) {
		setProgressBarIndeterminateVisibility(true);

		currentRequestedUrl = TimeoRequestHandler.getFullUrlFromEndPoint(
				endPoint, new TimeoRequestObject[]{data});
		GetTimeoDataFromAPITask task = new GetTimeoDataFromAPITask();
		task.execute(endPoint);
	}

	private class GetTimeoDataFromAPITask extends
			AsyncTask<EndPoints, Void, String> {
		@Override
		protected String doInBackground(EndPoints... params) {
			this.endPoint = params[0];
			this.appContext = getApplicationContext();

			if(endPoint == EndPoints.LINES || endPoint == EndPoints.DIRECTIONS
					|| endPoint == EndPoints.STOPS
					|| endPoint == EndPoints.SCHEDULE) {
				if(endPoint == EndPoints.LINES) {
					spinner = spinLine;
				} else if(endPoint == EndPoints.DIRECTIONS) {
					spinner = spinDirection;
				} else if(endPoint == EndPoints.STOPS) {
					spinner = spinStop;
				}

				return TimeoRequestHandler.requestWebPage(currentRequestedUrl);
			} else {
				return null;
			}
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			setProgressBarIndeterminateVisibility(false);

			if(result != null) {
				if((endPoint == EndPoints.LINES
						|| endPoint == EndPoints.DIRECTIONS || endPoint == EndPoints.STOPS)
						&& spinner != null) {
					try {
						ArrayList<TimeoIDNameObject> dataList = new ArrayList<TimeoIDNameObject>();

						dataList = TimeoResultParser.parseList(result);

						if(dataList != null) {
							ArrayAdapter<TimeoIDNameObject> adapter = new ArrayAdapter<TimeoIDNameObject>(
									AddStopActivity.this,
									android.R.layout.simple_spinner_item,
									dataList.toArray(new TimeoIDNameObject[dataList
											.size()]));
							adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							spinner.setAdapter(adapter);
						}
					} catch(JSONException e) {
						Toast.makeText(appContext, result, Toast.LENGTH_LONG)
								.show();
						e.printStackTrace();
					} catch(ClassCastException e) {
						Toast.makeText(appContext, result, Toast.LENGTH_LONG)
								.show();
						e.printStackTrace();
					}
				} else if(endPoint == EndPoints.SCHEDULE) {
					try {
						try {
							String[] scheduleArray = TimeoResultParser
									.parseSchedule(result);

							if(scheduleArray != null) {
								String scheduleString = "";

								for(int i = 0; i < scheduleArray.length
										&& scheduleArray[i] != null; i++) {
									if(i != 0)
										scheduleString += "\n";
									scheduleString += scheduleArray[i];
								}

								Toast.makeText(appContext, scheduleString,
										Toast.LENGTH_SHORT).show();
							}
						} catch(ClassCastException e) {
							Toast.makeText(
									appContext,
									((JSONObject) new JSONTokener(result)
											.nextValue()).getString("error"),
									Toast.LENGTH_LONG).show();
						}
					} catch(JSONException e) {
						e.printStackTrace();
					} catch(ClassCastException e) {
						e.printStackTrace();
					}

				}
			}
		}

		private Context appContext;
		private EndPoints endPoint;
		private Spinner spinner;
	}

	private Spinner spinLine;
	private Spinner spinDirection;
	private Spinner spinStop;

	private String currentRequestedUrl;

	TwistoastDatabase databaseHandler;

}
