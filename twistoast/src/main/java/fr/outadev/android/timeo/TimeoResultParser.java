/*
 * Twistoast - TimeoResultParser
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

package fr.outadev.android.timeo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import fr.outadev.twistoast.R;

/**
 * Contains methods used to parse the data retreived from the Twisto Realtime
 * API.
 *
 * @author outadoc
 */
public class TimeoResultParser {

	/**
	 * Parses a schedule from a JSON response from the API.
	 *
	 * @param source the JSON array returned by the API
	 * @return a String array containing the two schedules
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
	 * Parses a traffic message from a JSON response from the API.
	 *
	 * @param source the JSON array returned by the API
	 * @return a String array containing the two schedules
	 * @throws JSONException
	 * @throws ClassCastException
	 */
	public void parseTrafficMessage(String source, TimeoScheduleObject schedule) throws JSONException, ClassCastException {
		if(source != null) {
			// parse the whole JSON array
			JSONArray resultArray = (JSONArray) new JSONTokener(source).nextValue();

			try {
				if(resultArray != null && resultArray.getJSONObject(0) != null
						&& resultArray.getJSONObject(0).getJSONObject("message") != null) {
					JSONObject messageObject = resultArray.getJSONObject(0).getJSONObject("message");

					schedule.setMessageTitle(messageObject.getString("title").trim());
					schedule.setMessageBody(messageObject.getString("body").trim());
				}
			} catch(JSONException ignored) {

			}
		}
	}

	/**
	 * Parses multiple schedules from a JSON response from the API.
	 *
	 * @param source    the JSON array returned by the API
	 * @param stopsList the List containing the TimeoScheduleObjects that we're
	 *                  getting schedules for
	 * @throws JSONException
	 * @throws ClassCastException
	 * @see TimeoScheduleObject
	 */
	public void parseMultipleSchedules(Context context, String source, ArrayList<TimeoScheduleObject> stopsList) throws
			JSONException,
			ClassCastException {
		if(source != null) {
			int indexShift = 0;
			JSONArray resultArray = (JSONArray) new JSONTokener(source).nextValue();

			for(int i = 0; i < resultArray.length(); i++) {
				if(resultArray.getJSONObject(i) != null) {

					String sched[] = new String[2];
					String messageTitle = null;
					String messageBody = null;

					if(resultArray.getJSONObject(i).getJSONArray("next") != null) {
						JSONArray scheduleJSONArray = resultArray.getJSONObject(i).getJSONArray("next");

						for(int j = 0; j < scheduleJSONArray.length() && j < 2; j++) {
							sched[j] = scheduleJSONArray.getString(j);
						}
					}

					try {
						if(resultArray.getJSONObject(i).getJSONObject("message") != null) {
							JSONObject messageObject = resultArray.getJSONObject(i).getJSONObject("message");

							messageTitle = messageObject.getString("title").trim();
							messageBody = messageObject.getString("body").trim();
						}
					} catch(JSONException ignored) {

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
							stopsList.get(i + indexShift).setSchedule(new String[]{context.getResources().getString(R.string
									.loading_error)});
							indexShift++;
						}
					}

					TimeoScheduleObject current = stopsList.get(i + indexShift);

					current.setSchedule(sched);
					current.setMessageTitle(messageTitle);
					current.setMessageBody(messageBody);
				}
			}
		}
	}

	/**
	 * Parses a list of ID/Names from a JSON response from the API.
	 *
	 * @param source the JSON array returned by the API
	 * @return an ArrayList of TimeoIDNameObjects containing the parsed values
	 * @throws JSONException
	 * @throws ClassCastException
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
	 * @param source   the JSON array returned by the API
	 * @param activity the Activity on which we're going to display the message
	 * @throws JSONException
	 * @see Activity
	 */
	public static void displayErrorMessageFromTextResult(String source, Activity activity) throws JSONException {

		try {
			JSONObject obj = (JSONObject) new JSONTokener(source).nextValue();

			if(obj != null && obj.has("message")) {
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
			} else if(obj != null && obj.has("error")) {
				Toast.makeText(activity, obj.getString("error"), Toast.LENGTH_LONG).show();
			}
		} catch(ClassCastException e) {
			Toast.makeText(activity, activity.getResources().getString(R.string.loading_error), Toast.LENGTH_LONG).show();
		}

	}

}
