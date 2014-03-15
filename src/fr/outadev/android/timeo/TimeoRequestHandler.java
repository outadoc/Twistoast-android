package fr.outadev.android.timeo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONException;

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
		this.lastWebResponse = null;
		this.parser = new TimeoResultParser();
	}

	protected String requestWebPage(URL url) throws IOException, SocketTimeoutException {
		HttpURLConnection urlConnection = null;

		try {
			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setConnectTimeout(SOCKET_TIMEOUT);
			urlConnection.setReadTimeout(REQUEST_TIMEOUT);

			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			lastWebResponse = readStream(in);

			return lastWebResponse;
		} finally {
			urlConnection.disconnect();
		}
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
	 * @throws SocketTimeoutException
	 * @throws IOException
	 * 
	 * @see TimeoScheduleObject
	 * @see TimeoRequestObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoScheduleObject> getMultipleSchedules(ArrayList<TimeoScheduleObject> stopsList)
	        throws ClassCastException, JSONException, SocketTimeoutException, IOException {
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

		try {
			URL url = new URL(BASE_URL + "?func=getSchedule&data=" + URLEncoder.encode(cookie, CHARSET));
			result = requestWebPage(url);
			parser.parseMultipleSchedules(result, newStopsList);

			return newStopsList;
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

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
	 * @throws SocketTimeoutException
	 * @throws IOException
	 * 
	 * @see TimeoScheduleObject
	 * @see TimeoRequestObject
	 */
	public TimeoScheduleObject getSingleSchedule(TimeoScheduleObject stopSchedule) throws ClassCastException, JSONException,
	        SocketTimeoutException, IOException {
		String result = null;
		TimeoScheduleObject newSchedule = stopSchedule.clone();

		try {
			URL url = new URL(BASE_URL + "?func=getSchedule&line=" + URLEncoder.encode(newSchedule.getLine().getId(), CHARSET)
			        + "&direction=" + URLEncoder.encode(newSchedule.getDirection().getId(), CHARSET) + "&stop="
			        + URLEncoder.encode(newSchedule.getStop().getId(), CHARSET));

			result = requestWebPage(url);
			newSchedule.setSchedule(parser.parseSchedule(result));

			return newSchedule;
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

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
	 * @throws SocketTimeoutException
	 * @throws IOException
	 * 
	 * @see TimeoRequestObject
	 * @see TimeoIDNameObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoIDNameObject> getLines(TimeoRequestObject request) throws ClassCastException, JSONException,
	        SocketTimeoutException, IOException {
		try {
			return getGenericList(new URL(BASE_URL + "?func=getLines"));
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}

		return null;
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
	 * @throws SocketTimeoutException
	 * @throws IOException
	 * 
	 * @see TimeoRequestObject
	 * @see TimeoIDNameObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoIDNameObject> getDirections(TimeoRequestObject request) throws ClassCastException, JSONException,
	        SocketTimeoutException, IOException {
		try {
			return getGenericList(new URL(BASE_URL + "?func=getDirections&line=" + URLEncoder.encode(request.getLine(), CHARSET)));
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
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
	 * @throws SocketTimeoutException
	 * @throws IOException
	 * 
	 * @see TimeoRequestObject
	 * @see TimeoIDNameObject
	 * @see ArrayList
	 */
	public ArrayList<TimeoIDNameObject> getStops(TimeoRequestObject request) throws ClassCastException, JSONException,
	        SocketTimeoutException, IOException {
		try {
			return getGenericList(new URL(BASE_URL + "?func=getStops&line=" + URLEncoder.encode(request.getLine(), CHARSET)
			        + "&direction=" + URLEncoder.encode(request.getDirection(), CHARSET)));
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	protected ArrayList<TimeoIDNameObject> getGenericList(URL url) throws ClassCastException, JSONException,
	        SocketTimeoutException, IOException {
		String result = requestWebPage(url);
		return parser.parseList(result);
	}

	// Reads an InputStream and converts it to a String.
	private String readStream(InputStream is) {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			int i = is.read();
			
			while(i != -1) {
				bo.write(i);
				i = is.read();
			}
			
			return bo.toString();
		} catch(IOException e) {
			return "";
		}
	}

	/**
	 * Gets the last plain text web response that was returned by the API.
	 * 
	 * @return last result of the HTTP request
	 */
	public String getLastWebResponse() {
		return lastWebResponse;
	}

	private final static String BASE_URL = "http://apps.outadoc.fr/twisto-realtime/twisto-api.php";
	private final static String CHARSET = "UTF-8";

	private final static int SOCKET_TIMEOUT = 10000;
	private final static int REQUEST_TIMEOUT = 20000;

	private String lastWebResponse;

	private TimeoResultParser parser;

	public enum EndPoints {
		LINES, DIRECTIONS, STOPS, SCHEDULE
	}

}