package fr.outadev.android.timeo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

/**
 * Handles all connections to the Twisto Realtime API.
 * 
 * @author outadoc
 * 
 */
public class TimeoRequestHandler {

	/**
	 * Creates a Timeo request handler.
	 */
	public TimeoRequestHandler() {
		this.lastHTTPResponse = null;
		this.parser = new TimeoResultParser();
	}

	protected String requestWebPage(URL url, Map<String, String> params) throws HttpRequestException {
		lastHTTPResponse = HttpRequest.post(url.toExternalForm()).readTimeout(REQUEST_TIMEOUT).form(params).body();
		return lastHTTPResponse;
	}

	private String requestWebPage(URL url) throws HttpRequestException {
		return requestWebPage(url, null);
	}

	private String requestWebPage(Map<String, String> params) throws HttpRequestException {
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
	 * @param stopsList
	 *            the TimeoScheduleObject ArrayList that will be returned along
	 *            with the corresponding schedules
	 * @return the ArrayList that was passed as a parameter, containing the
	 *         schedules that were requested from the API
	 * 
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * 
	 * @see TimeoScheduleObject
	 * @see TimeoRequestObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoScheduleObject> getMultipleSchedules(ArrayList<TimeoScheduleObject> stopsList)
	        throws ClassCastException, JSONException, HttpRequestException {
		@SuppressWarnings("unchecked")
		ArrayList<TimeoScheduleObject> newStopsList = (ArrayList<TimeoScheduleObject>) stopsList.clone();

		String cookie = new String();
		String result = new String();

		// craft a cookie in the form
		// STOP_ID|LINE_ID|DIRECTION_ID;STOP_ID|LINE_ID|DIRECTION_ID;...
		for(int i = 0; i < stopsList.size(); i++) {
			if(i != 0) cookie += ';';
			cookie += newStopsList.get(i).getStop().getId() + '|' + newStopsList.get(i).getLine().getId() + '|'
			        + newStopsList.get(i).getDirection().getId();
		}

		Map<String, String> data = new HashMap<String, String>();
		data.put("func", "getSchedule");
		data.put("data", cookie);

		result = requestWebPage(data);
		parser.parseMultipleSchedules(result, newStopsList);

		return newStopsList;
	}

	/**
	 * Gets a single schedule from the API, using a TimeoScheduleObject.
	 * 
	 * @param stopSchedule
	 *            the TimeoScheduleObject that will be returned along with the
	 *            corresponding schedule
	 * @return the TimeoScheduleObject that was passed as a parameter,
	 *         containing the schedule that was requested from the API
	 * 
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * 
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

		result = requestWebPage(data);
		newSchedule.setSchedule(parser.parseSchedule(result));

		return newSchedule;
	}

	/**
	 * Gets a list of lines that are available from the API.
	 * 
	 * @param request
	 *            the TimeoRequestObject that will be used to make the call
	 * @return an ArrayList of TimeoIDNameObject, containing the lines (id and
	 *         name)
	 * 
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * 
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
	 * @param request
	 *            the TimeoRequestObject that will be used to make the call
	 * @return an ArrayList of TimeoIDNameObject, containing the directions (id
	 *         and name)
	 * 
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * 
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
	 * @param request
	 *            the TimeoRequestObject that will be used to make the call
	 * @return an ArrayList of TimeoIDNameObject, containing the stops (id and
	 *         name)
	 * 
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws HttpRequestException
	 * 
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

	protected ArrayList<TimeoIDNameObject> getGenericList(URL url, String params) throws ClassCastException, JSONException,
	        HttpRequestException {
		String result = requestWebPage(url);
		return parser.parseList(result);
	}

	private ArrayList<TimeoIDNameObject> getGenericList(Map<String, String> params) throws ClassCastException, JSONException,
	        HttpRequestException {
		String result = requestWebPage(params);
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
