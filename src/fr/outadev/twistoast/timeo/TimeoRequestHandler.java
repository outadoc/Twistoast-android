package fr.outadev.twistoast.timeo;

import java.io.BufferedInputStream;
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

public class TimeoRequestHandler {

	/**
	 * Create a Timeo request handler.
	 */
	public TimeoRequestHandler() {
		this.lastWebResponse = null;
		this.parser = new TimeoResultParser();
	}

	protected String requestWebPage(URL url) throws IOException, SocketTimeoutException {
		HttpURLConnection urlConnection = null;

		try {
			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setConnectTimeout(15000);
			urlConnection.setReadTimeout(30000);

			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			lastWebResponse = readStream(in);

			return lastWebResponse;
		} finally {
			urlConnection.disconnect();
		}
	}

	/**
	 * Get multiple schedules from the API, using an ArrayList of TimeoScheduleObjects.
	 * 
	 * @param request the TimeoRequestObject array that will be used to make the calls
	 * @param stopsList the TimeoScheduleObject ArrayList that will be returned along with the corresponding schedules
	 * @return the ArrayList that was passed as a parameter, containing the schedules that were requested from the API
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
	public ArrayList<TimeoScheduleObject> getMultipleSchedules(TimeoRequestObject[] request, ArrayList<TimeoScheduleObject> stopsList) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		String cookie = new String();
		String result = new String();
		
		@SuppressWarnings("unchecked")
		ArrayList<TimeoScheduleObject> newStopsList = (ArrayList<TimeoScheduleObject>) stopsList.clone();

		// craft a cookie in the form
		// STOP_ID|LINE_ID|DIRECTION_ID;STOP_ID|LINE_ID|DIRECTION_ID;...
		for(int i = 0; i < request.length; i++) {
			if(i != 0)
				cookie += ';';
			cookie += request[i].getStop() + '|' + request[i].getLine() + '|' + request[i].getDirection();
		}

		try {
			URL url = new URL(baseUrl + "?func=getSchedule&data=" + URLEncoder.encode(cookie, charset));
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
	 * Get a single schedule from the API, using a TimeoScheduleObject.
	 * 
	 * @param request the TimeoRequestObject that will be used to make the call
	 * @param stopSchedule the TimeoScheduleObject that will be returned along with the corresponding schedule
	 * @return the TimeoScheduleObject that was passed as a parameter, containing the schedule that was requested from the API
	 * 
	 * @throws ClassCastException
	 * @throws JSONException
	 * @throws SocketTimeoutException
	 * @throws IOException
	 * 
	 * @see TimeoScheduleObject
	 * @see TimeoRequestObject
	 */
	public TimeoScheduleObject getSingleSchedule(TimeoRequestObject request, TimeoScheduleObject stopSchedule) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		String result = null;
		TimeoScheduleObject newSchedule = stopSchedule.clone();

		try {
			URL url = new URL(baseUrl + "?func=getSchedule&line=" + URLEncoder.encode(request.getLine(), charset) + "&direction=" + URLEncoder
					.encode(request.getDirection(), charset) + "&stop=" + URLEncoder.encode(request.getStop(), charset));
			result = requestWebPage(url);
			newSchedule.setSchedule(parser.parseSchedule(result));

			return newSchedule;
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return stopSchedule;
	}

	/**
	 * Get a list of lines that are available from the API.
	 * 
	 * @param request the TimeoRequestObject that will be used to make the call 
	 * @return an ArrayList of TimeoIDNameObject, containing the lines (id and name)
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
	public ArrayList<TimeoIDNameObject> getLines(TimeoRequestObject request) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		try {
			return getGenericList(new URL(baseUrl + "?func=getLines"));
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get a list of directions that are available from the API for the specified line.
	 * 
	 * @param request the TimeoRequestObject that will be used to make the call 
	 * @return an ArrayList of TimeoIDNameObject, containing the directions (id and name)
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
	public ArrayList<TimeoIDNameObject> getDirections(TimeoRequestObject request) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		try {
			return getGenericList(new URL(baseUrl + "?func=getDirections&line=" + URLEncoder.encode(request.getLine(), charset)));
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get a list of stops that are available from the API for the specified line and direction.
	 * 
	 * @param request the TimeoRequestObject that will be used to make the call 
	 * @return an ArrayList of TimeoIDNameObject, containing the stops (id and name)
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
	public ArrayList<TimeoIDNameObject> getStops(TimeoRequestObject request) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		try {
			return getGenericList(new URL(baseUrl + "?func=getStops&line=" + URLEncoder.encode(request.getLine(), charset) + "&direction=" + URLEncoder
					.encode(request.getDirection(), charset)));
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	protected ArrayList<TimeoIDNameObject> getGenericList(URL url) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		String result = requestWebPage(url);
		return parser.parseList(result);
	}

	// Reads an InputStream and converts it to a String.
	protected String readStream(InputStream stream) throws IOException, UnsupportedEncodingException {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	/**
	 * Get the last plain text web response that was returned by the API.
	 * @return last result of the HTTP request
	 */
	public String getLastWebResponse() {
		return lastWebResponse;
	}

	private final static String baseUrl = "http://apps.outadoc.fr/twisto-realtime/twisto-api.php";
	private final static String charset = "UTF-8";

	private String lastWebResponse;
	
	private TimeoResultParser parser;

	public enum EndPoints {
		LINES, DIRECTIONS, STOPS, SCHEDULE
	}

}
