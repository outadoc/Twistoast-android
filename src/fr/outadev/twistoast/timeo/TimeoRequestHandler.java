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
	
	public TimeoRequestHandler() {
		this.lastWebResponse = null;
	}

	private String requestWebPage(URL url) throws IOException, SocketTimeoutException {
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

	public ArrayList<TimeoScheduleObject> getMultipleSchedules(TimeoRequestObject[] request, ArrayList<TimeoScheduleObject> stopsList) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		String cookie = new String();
		String result = new String();

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
			TimeoResultParser.parseMultipleSchedules(result, stopsList);

			return stopsList;
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return stopsList;
	}

	public TimeoScheduleObject getSingleSchedule(TimeoRequestObject request, TimeoScheduleObject stopSchedule) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		
		String result = null;
		
		try {
			URL url = new URL(baseUrl + "?func=getSchedule&line=" + URLEncoder.encode(request.getLine(), charset) + "&direction=" + URLEncoder
					.encode(request.getDirection(), charset) + "&stop=" + URLEncoder.encode(request.getStop(), charset));
			result = requestWebPage(url);
			stopSchedule.setSchedule(TimeoResultParser.parseSchedule(result));

			return stopSchedule;
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return stopSchedule;
	}

	public ArrayList<TimeoIDNameObject> getLines(TimeoRequestObject request) throws ClassCastException, JSONException, SocketTimeoutException, IOException {
		try {
			return getGenericList(new URL(baseUrl + "?func=getLines"));
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}

		return null;
	}

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

	private ArrayList<TimeoIDNameObject> getGenericList(URL url) throws ClassCastException, JSONException, SocketTimeoutException, IOException  {
		String result = requestWebPage(url);
		return TimeoResultParser.parseList(result);
	}

	// Reads an InputStream and converts it to a String.
	private String readStream(InputStream stream) throws IOException, UnsupportedEncodingException {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public String getLastWebResponse() {
		return lastWebResponse;
	}

	public void setLastWebResponse(String lastWebResponse) {
		this.lastWebResponse = lastWebResponse;
	}

	private final static String baseUrl = "http://apps.outadoc.fr/twisto-realtime/twisto-api.php";
	private final static String charset = "UTF-8";
	
	private String lastWebResponse;
		
	public enum EndPoints {
		LINES, DIRECTIONS, STOPS, SCHEDULE
	}

}
