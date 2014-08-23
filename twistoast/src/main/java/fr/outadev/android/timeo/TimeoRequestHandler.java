/*
 * Twistoast - TimeoRequestHandler
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

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.outadev.twistoast.R;

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
public class TimeoRequestHandler {

	private final static String BASE_URL = "http://apps.outadoc.fr/twisto-realtime/twisto-api.php";
	private final static String BASE_PRE_HOME_URL = "http://twisto.fr/module/mobile/App2014/utils/getPreHome.php";

	private final static int REQUEST_TIMEOUT = 10000;

	private String lastHTTPResponse;

	public enum EndPoints {
		LINES, DIRECTIONS, STOPS, SCHEDULE
	}

	/**
	 * Creates a Timeo request handler.
	 */
	public TimeoRequestHandler() {
		this.lastHTTPResponse = null;
	}

	private String requestWebPage(URL url, Map<String, String> params, boolean useCaches) throws HttpRequestException {
		lastHTTPResponse = HttpRequest.post(url.toExternalForm()).useCaches(true).readTimeout(REQUEST_TIMEOUT).form(params)
				.body();
		return lastHTTPResponse;
	}

	private String requestWebPage(URL url, Map<String, String> params) throws HttpRequestException {
		return requestWebPage(url, params, true);
	}

	private String requestWebPage(Map<String, String> params, boolean useCaches) throws HttpRequestException {
		try {
			return requestWebPage(new URL(BASE_URL), params);
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Gets multiple schedules from the API, using an ArrayList of
	 * TimeoScheduleObjects.
	 *
	 * @param stopsList the TimeoScheduleObject ArrayList that will be returned along
	 *                  with the corresponding schedules
	 * @return the ArrayList that was passed as a parameter, containing the
	 * schedules that were requested from the API
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * @see TimeoScheduleObject
	 * @see TimeoRequestObject
	 * @see ArrayList
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<TimeoScheduleObject> getMultipleSchedules(Context context, ArrayList<TimeoScheduleObject> stopsList)
			throws ClassCastException, JSONException, HttpRequestException {
		if(stopsList.size() > 0) {
			ArrayList<TimeoScheduleObject> newStopsList = (ArrayList<TimeoScheduleObject>) stopsList.clone();

			String cookie = "";
			String result;

			// craft a cookie in the form
			// STOP_ID|LINE_ID|DIRECTION_ID;STOP_ID|LINE_ID|DIRECTION_ID;...
			for(int i = 0; i < stopsList.size(); i++) {
				if(i != 0) {
					cookie += ';';
				}
				cookie += newStopsList.get(i).getStop().getId() + '|' + newStopsList.get(i).getLine().getId() + '|'
						+ newStopsList.get(i).getDirection().getId();
			}

			Map<String, String> data = new HashMap<String, String>();
			data.put("func", "getSchedule");
			data.put("data", cookie);

			result = requestWebPage(data, false);
			parseMultipleSchedules(context, result, newStopsList);

			return newStopsList;
		} else {
			return stopsList;
		}
	}

	/**
	 * Gets a single schedule from the API, using a TimeoScheduleObject.
	 *
	 * @param stopSchedule the TimeoScheduleObject that will be returned along with the
	 *                     corresponding schedule
	 * @return the TimeoScheduleObject that was passed as a parameter,
	 * containing the schedule that was requested from the API
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * @see TimeoScheduleObject
	 * @see TimeoRequestObject
	 */
	public TimeoScheduleObject getSingleSchedule(TimeoScheduleObject stopSchedule) throws ClassCastException, JSONException,
			HttpRequestException {
		String result = null;
		TimeoScheduleObject newSchedule = stopSchedule.clone();

		Map<String, String> data = new HashMap<String, String>();
		data.put("func", "getSchedule");
		data.put("line", newSchedule.getLine().getId());
		data.put("direction", newSchedule.getDirection().getId());
		data.put("stop", newSchedule.getStop().getId());

		result = requestWebPage(data, false);
		newSchedule.setSchedule(parseSchedule(result));
		parseTrafficMessage(result, newSchedule);

		return newSchedule;
	}

	/**
	 * Gets a list of lines that are available from the API.
	 *
	 * @param request the TimeoRequestObject that will be used to make the call
	 * @return an ArrayList of TimeoIDNameObject, containing the lines (id and
	 * name)
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * @see TimeoRequestObject
	 * @see TimeoIDNameObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoIDNameObject> getLines(TimeoRequestObject request) throws ClassCastException, JSONException,
			HttpRequestException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("func", "getLines");
		return getGenericList(data);
	}

	/**
	 * Gets a list of directions that are available from the API for the
	 * specified line.
	 *
	 * @param request the TimeoRequestObject that will be used to make the call
	 * @return an ArrayList of TimeoIDNameObject, containing the directions (id
	 * and name)
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * @see TimeoRequestObject
	 * @see TimeoIDNameObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoIDNameObject> getDirections(TimeoRequestObject request) throws ClassCastException, JSONException,
			HttpRequestException {
		Map<String, String> data = new HashMap<String, String>();

		data.put("func", "getDirections");
		data.put("line", request.getLine());

		return getGenericList(data);
	}

	/**
	 * Gets a list of stops that are available from the API for the specified
	 * line and direction.
	 *
	 * @param request the TimeoRequestObject that will be used to make the call
	 * @return an ArrayList of TimeoIDNameObject, containing the stops (id and
	 * name)
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * @see TimeoRequestObject
	 * @see TimeoIDNameObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoIDNameObject> getStops(TimeoRequestObject request) throws ClassCastException, JSONException,
			HttpRequestException {
		Map<String, String> data = new HashMap<String, String>();

		data.put("func", "getStops");
		data.put("line", request.getLine());
		data.put("direction", request.getDirection());

		return getGenericList(data);
	}

	private ArrayList<TimeoIDNameObject> getGenericList(Map<String, String> params) throws ClassCastException, JSONException,
			HttpRequestException {
		String result = requestWebPage(params, true);
		return parseList(result);
	}

	public TimeoTrafficAlert getGlobalTrafficAlert() {
		try {
			String response = requestWebPage(new URL(BASE_PRE_HOME_URL), new HashMap<String, String>(), true);
			return parseGlobalTrafficAlert(response);
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Parses a schedule from a JSON response from the API.
	 *
	 * @param source the JSON array returned by the API
	 * @return a String array containing the two schedules
	 * @throws JSONException
	 * @throws ClassCastException
	 */
	private String[] parseSchedule(String source) throws JSONException, ClassCastException {
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
	private void parseTrafficMessage(String source, TimeoScheduleObject schedule) throws JSONException, ClassCastException {
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
	private void parseMultipleSchedules(Context context, String source, ArrayList<TimeoScheduleObject> stopsList) throws
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
	private ArrayList<TimeoIDNameObject> parseList(String source) throws JSONException, ClassCastException {
		if(source != null) {
			JSONArray resultArray = (JSONArray) new JSONTokener(source).nextValue();

			if(resultArray != null) {
				ArrayList<TimeoIDNameObject> dataList = new ArrayList<TimeoIDNameObject>();

				for(int i = 0; i < resultArray.length(); i++) {
					String id = resultArray.optJSONObject(i).getString("id");
					String name = resultArray.optJSONObject(i).getString("name");

					if(!id.equals("0") && !id.equals("GP") && !id.equals("GPR")) {
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
	 * @see android.app.Activity
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

	private TimeoTrafficAlert parseGlobalTrafficAlert(String source) {
		if(source != null && !source.isEmpty()) {
			try {
				JSONObject obj = (JSONObject) new JSONTokener(source).nextValue();

				if(obj.has("alerte")) {
					JSONObject alert = obj.getJSONObject("alerte");
					return new TimeoTrafficAlert(alert.getInt("id_alerte"), alert.getString("libelle_alerte"),
							alert.getString("url_alerte"));
				}
			} catch(JSONException e) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Capitalizes the first letter of every word, like WordUtils.capitalize(); except it does it WELL.
	 * The determinants will not be capitalized, whereas some acronyms will.
	 *
	 * @param str The text to capitalize.
	 * @return The capitalized text.
	 */
	public String smartCapitalize(String str) {
		String newStr = "";
		str = str.toLowerCase();

		//these words will never be capitalized
		String[] determinants = new String[]{"de", "du", "des", "au", "aux", "à", "la", "le", "les", "d", "et", "l"};
		//these words will always be capitalized
		String[] specialWords = new String[]{"sncf", "chu", "chr", "crous", "suaps", "fpa", "za", "zi", "zac", "cpam", "efs",
				"mjc"};

		//explode the string with both spaces and apostrophes
		String[] words = str.split("( |')");

		for(String word : words) {
			if(Arrays.asList(determinants).contains(word)) {
				//if the word should not be capitalized, just append it to the new string
				newStr += word;
			} else if(Arrays.asList(specialWords).contains(word)) {
				//if the word should be in upper case, do eet
				newStr += word.toUpperCase(Locale.FRENCH);
			} else {
				//if it's a normal word, just capitalize it
				newStr += StringUtils.capitalize(word);
			}

			try {
				//we don't know if the next character is a blank space or an apostrophe, so we check that
				char delimiter = str.charAt(newStr.length());
				newStr += delimiter;
			} catch(StringIndexOutOfBoundsException ignored) {
				//will be thrown for the last word of the string
			}
		}

		return newStr;
	}

	/**
	 * Gets the last plain text web response that was returned by the API.
	 *
	 * @return last result of the HTTP request
	 */
	public String getLastHTTPResponse() {
		return lastHTTPResponse;
	}

}
