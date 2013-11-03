package fr.outadev.twistoast.timeo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public abstract class TimeoRequestHandler {

	public static String requestWebPage(String url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);

		try {
			HttpResponse response = client.execute(request);
			return readIt(response.getEntity().getContent(), 2000);
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getFullUrlFromEndPoint(EndPoints endPoint,
			TimeoRequestObject data) {
		if(endPoint == EndPoints.LINES || endPoint == EndPoints.DIRECTIONS
				|| endPoint == EndPoints.STOPS
				|| endPoint == EndPoints.SCHEDULE) {
			String url = baseUrl;
			String charset = "UTF-8";

			try {
				if(endPoint == EndPoints.LINES) {
					url += "?func=getLines";
				} else if(endPoint == EndPoints.DIRECTIONS
						&& data.getLine() != null) {
					url += "?func=getDirections&line="
							+ URLEncoder.encode(data.getLine(), charset);
				} else if(endPoint == EndPoints.STOPS && data.getLine() != null
						&& data.getDirection() != null) {
					url += "?func=getStops&line="
							+ URLEncoder.encode(data.getLine(), charset)
							+ "&direction="
							+ URLEncoder.encode(data.getDirection(), charset);
				} else if(endPoint == EndPoints.SCHEDULE
						&& data.getLine() != null
						&& data.getDirection() != null
						&& data.getStop() != null) {
					url += "?func=getSchedule&line="
							+ URLEncoder.encode(data.getLine(), charset)
							+ "&direction="
							+ URLEncoder.encode(data.getDirection(), charset)
							+ "&stop="
							+ URLEncoder.encode(data.getStop(), charset);
				}

				return url;
			} catch(UnsupportedEncodingException e) {
			}
		}

		return null;
	}

	// Reads an InputStream and converts it to a String.
	public static String readIt(InputStream stream, int len)
			throws IOException, UnsupportedEncodingException {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private final static String baseUrl = "http://apps.outadoc.fr/twisto-realtime/twisto-api.php";

	public enum EndPoints {
		LINES, DIRECTIONS, STOPS, SCHEDULE
	}

}
