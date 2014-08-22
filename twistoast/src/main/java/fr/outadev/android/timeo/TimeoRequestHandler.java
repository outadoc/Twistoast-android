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

import android.content.Context;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
public class TimeoRequestHandler {

	/**
	 * Creates a Timeo request handler.
	 */
	public TimeoRequestHandler() {
		this.lastHTTPResponse = null;
		this.parser = new TimeoResultParser();
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
	public ArrayList<TimeoScheduleObject> getMultipleSchedules(Context context, ArrayList<TimeoScheduleObject> stopsList)
			throws ClassCastException, JSONException, HttpRequestException {
		@SuppressWarnings("unchecked")
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
		parser.parseMultipleSchedules(context, result, newStopsList);

		return newStopsList;
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
		newSchedule.setSchedule(parser.parseSchedule(result));
		parser.parseTrafficMessage(result, newSchedule);

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
		return parser.parseList(result);
	}

	/**
	 * Gets the last plain text web response that was returned by the API.
	 *
	 * @return last result of the HTTP request
	 */
	public String getLastHTTPResponse() {
		return lastHTTPResponse;
	}

	private final static String BASE_URL = "http://apps.outadoc.fr/twisto-realtime/twisto-api.php";
	private final static int REQUEST_TIMEOUT = 20000;

	private String lastHTTPResponse;
	private final TimeoResultParser parser;

	public enum EndPoints {
		LINES, DIRECTIONS, STOPS, SCHEDULE
	}

}
