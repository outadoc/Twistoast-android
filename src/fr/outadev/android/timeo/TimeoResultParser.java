package fr.outadev.android.timeo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.outadev.twistoast.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

/**
 * Contains methods used to parse the data retreived from the Twisto Realtime
 * API.
 * 
 * @author outadoc
 * 
 */
public class TimeoResultParser {

	/**
	 * Parses a schedule from a JSON response from the API.
	 * 
	 * @param source
	 *            the JSON array returned by the API
	 * @return a String array containing the two schedules
	 * 
	 * @throws JSONException
	 * @throws ClassCastException
	 */
	public String[] parseSchedule(String source) throws JSONException, ClassCastException {
		if(source != null) {
			String[] scheduleArray = new String[2];

			// parse the whole JSON array
			JSONArray resultArray = (JSONArray) new JSONTokener(source).nextValue();

			if(resultArray != null && resultArray.getJSONObject(0) != null
			        && resultArray.getJSONObject(0).getJSONArray("next") != null) {
				JSONArray scheduleJSONArray = resultArray.getJSONObject(0).getJSONArray("next");

				for(int i = 0; i < scheduleJSONArray.length() && i < 2; i++) {
					scheduleArray[i] = scheduleJSONArray.getString(i);
				}

				return scheduleArray;
			}
		}

		return null;
	}

	/**
	 * Parses multiple schedules from a JSON response from the API.
	 * 
	 * @param source
	 *            the JSON array returned by the API
	 * @param stopsList
	 *            the List containing the TimeoScheduleObjects that we're
	 *            getting schedules for
	 * 
	 * @throws JSONException
	 * @throws ClassCastException
	 * 
	 * @see TimeoScheduleObject
	 */
	public void parseMultipleSchedules(String source, ArrayList<TimeoScheduleObject> stopsList) throws JSONException,
	        ClassCastException {
		if(source != null) {
			int indexShift = 0;
			JSONArray resultArray = (JSONArray) new JSONTokener(source).nextValue();

			for(int i = 0; i < resultArray.length(); i++) {
				if(resultArray != null && resultArray.getJSONObject(i) != null
				        && resultArray.getJSONObject(i).getJSONArray("next") != null) {

					JSONArray scheduleJSONArray = resultArray.getJSONObject(i).getJSONArray("next");
					String sched[] = new String[2];

					for(int j = 0; j < scheduleJSONArray.length() && j < 2; j++) {
						sched[j] = scheduleJSONArray.getString(j);
					}

					if(stopsList.size() != resultArray.length()) {
						// sometimes, the API isn't not going to return the
						// right number of stops: some may disappear. so, while
						// the current stop we're parsing isn't really the
						// current stop in our list, increase the shift
						while(!stopsList.get(i + indexShift).getLine().getName()
						        .equalsIgnoreCase(resultArray.getJSONObject(i).getString("line"))
						        && !stopsList.get(i + indexShift).getDirection().getName()
						                .equalsIgnoreCase(resultArray.getJSONObject(i).getString("direction"))
						        && !stopsList.get(i + indexShift).getStop().getName()
						                .equalsIgnoreCase(resultArray.getJSONObject(i).getString("stop"))) {
							Log.d("Twistoast", "missing schedule for " + stopsList.get(i + indexShift) + ", shifting");
							indexShift++;
						}
					}

					stopsList.get(i + indexShift).setSchedule(sched);
				}
			}
		}
	}

	/**
	 * Parses a list of ID/Names from a JSON response from the API.
	 * 
	 * @param source
	 *            the JSON array returned by the API
	 * @return an ArrayList of TimeoIDNameObjects containing the parsed values
	 * 
	 * @throws JSONException
	 * @throws ClassCastException
	 * 
	 * @see TimeoIDNameObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoIDNameObject> parseList(String source) throws JSONException, ClassCastException {
		if(source != null) {
			JSONArray resultArray = (JSONArray) new JSONTokener(source).nextValue();

			if(resultArray != null) {
				ArrayList<TimeoIDNameObject> dataList = new ArrayList<TimeoIDNameObject>();

				for(int i = 0; i < resultArray.length(); i++) {
					String id = resultArray.optJSONObject(i).getString("id");
					String name = resultArray.optJSONObject(i).getString("name");

					if(!id.equals("0")) {
						TimeoIDNameObject item = new TimeoIDNameObject(id, name);
						dataList.add(item);
					}
				}

				return dataList;
			}
		}

		return null;
	}

	/**
	 * Displays an error message based on the JSON message returned by the API,
	 * if present.
	 * 
	 * @param source
	 *            the JSON array returned by the API
	 * @param activity
	 *            the Activity on which we're going to display the message
	 * @throws JSONException
	 * 
	 * @see Activity
	 */
	public static void displayErrorMessageFromTextResult(String source, Activity activity) throws JSONException {

		try {
			JSONObject obj = (JSONObject) new JSONTokener(source).nextValue();

			try {
				String errorMessage = obj.getString("message");
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);

				// add the buttons
				builder.setPositiveButton("OK", null);

				// set dialog title
				builder.setTitle(obj.getString("error"));
				builder.setMessage(errorMessage);

				// create the AlertDialog and show it
				AlertDialog dialog = builder.create();
				dialog.show();
				
			} catch(ClassCastException ex) {
				Toast.makeText(activity, obj.getString("error"), Toast.LENGTH_LONG).show();
			}

		} catch(ClassCastException e) {
			Toast.makeText(activity, activity.getResources().getString(R.string.loading_error), Toast.LENGTH_LONG).show();
		}

	}

}
