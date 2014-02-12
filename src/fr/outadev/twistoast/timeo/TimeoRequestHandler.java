package fr.outadev.twistoast.timeo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.util.Log;

public abstract class TimeoRequestHandler {

	public static String requestWebPage(String urlString) throws IOException {		
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();

		try {
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			return readStream(in);
		} finally {
			urlConnection.disconnect();
		}
	}

	public static String getFullUrlFromEndPoint(EndPoints endPoint, TimeoRequestObject[] data) {
		if(endPoint == EndPoints.LINES || endPoint == EndPoints.DIRECTIONS || endPoint == EndPoints.STOPS || endPoint == EndPoints.SCHEDULE || endPoint == EndPoints.FULL_SCHEDULE) {

			String url = baseUrl;
			String charset = "UTF-8";

			try {
				// adapt the request URL depending on the API end point
				// requested
				if(endPoint == EndPoints.LINES) {
					url += "?func=getLines";
				} else if(endPoint == EndPoints.DIRECTIONS && data[0].getLine() != null) {
					url += "?func=getDirections&line=" + URLEncoder
							.encode(data[0].getLine(), charset);
				} else if(endPoint == EndPoints.STOPS && data[0].getLine() != null && data[0]
						.getDirection() != null) {
					url += "?func=getStops&line=" + URLEncoder.encode(data[0]
							.getLine(), charset) + "&direction=" + URLEncoder
							.encode(data[0].getDirection(), charset);
				} else if(endPoint == EndPoints.SCHEDULE && data[0].getLine() != null && data[0]
						.getDirection() != null && data[0].getStop() != null) {
					url += "?func=getSchedule&line=" + URLEncoder
							.encode(data[0].getLine(), charset) + "&direction=" + URLEncoder
							.encode(data[0].getDirection(), charset) + "&stop=" + URLEncoder
							.encode(data[0].getStop(), charset);
				} else if(endPoint == EndPoints.FULL_SCHEDULE) {
					String cookie = "";

					// craft a cookie in the form
					// STOP_ID|LINE_ID|DIRECTION_ID;STOP_ID|LINE_ID|DIRECTION_ID;...
					for(int i = 0; i < data.length; i++) {
						if(i != 0)
							cookie += ';';
						cookie += data[i].getStop() + '|' + data[i].getLine() + '|' + data[i]
								.getDirection();
					}

					url += "?func=getSchedule&data=" + URLEncoder
							.encode(cookie, charset);
					Log.i("twistoast", "sending request for " + url);
				}

				return url;
			} catch(UnsupportedEncodingException e) {
			}
		}

		return "";
	}

	// Reads an InputStream and converts it to a String.
	public static String readStream(InputStream stream) throws IOException, UnsupportedEncodingException {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private final static String baseUrl = "http://apps.outadoc.fr/twisto-realtime/twisto-api.php";

	public enum EndPoints {
		LINES, DIRECTIONS, STOPS, SCHEDULE, FULL_SCHEDULE
	}

}
