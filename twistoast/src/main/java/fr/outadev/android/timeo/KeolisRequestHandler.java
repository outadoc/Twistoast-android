/*
 * Twistoast - KeolisRequestHandler
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

import android.util.Xml;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import fr.outadev.android.timeo.model.TimeoIDNameObject;
import fr.outadev.android.timeo.model.TimeoLine;
import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.android.timeo.model.TimeoStopSchedule;
import fr.outadev.android.timeo.model.TimeoTrafficAlert;

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
public class KeolisRequestHandler {

	private final static String BASE_URL = "http://timeo3.keolis.com/relais/147.php";
	private final static String BASE_PRE_HOME_URL = "http://twisto.fr/module/mobile/App2014/utils/getPreHome.php";

	private final static int REQUEST_TIMEOUT = 10000;

	private String lastHTTPResponse;

	public enum EndPoints {
		LINES, DIRECTIONS, STOPS, SCHEDULE
	}

	/**
	 * Creates a Timeo request handler.
	 */
	public KeolisRequestHandler() {
		this.lastHTTPResponse = null;
	}

	private String requestWebPage(String url, String params, boolean useCaches) throws HttpRequestException {
		lastHTTPResponse = HttpRequest.get(url + "?" + params)
				.useCaches(useCaches)
				.readTimeout(REQUEST_TIMEOUT)
				.body();

		return lastHTTPResponse;
	}

	private String requestWebPage(String url, boolean useCaches) throws HttpRequestException {
		return requestWebPage(url, "", useCaches);
	}


	public List<TimeoLine> getLines() throws HttpRequestException, XmlPullParserException, IOException {
		String params = "xml=1";
		String result = requestWebPage(BASE_URL, params, true);

		XmlPullParser parser = getParserForXMLString(result);

		if(parser == null) {
			return null;
		}

		int eventType = parser.getEventType();

		TimeoLine tmpLine = null;
		TimeoIDNameObject tmpDirection;

		ArrayList<TimeoLine> lines = new ArrayList<TimeoLine>();

		String text = null;
		boolean isInLineTag = false;

		while(eventType != XmlPullParser.END_DOCUMENT) {
			String tagname = parser.getName();

			switch(eventType) {
				case XmlPullParser.START_TAG:

					if(tagname.equalsIgnoreCase("ligne")) {
						isInLineTag = true;
						tmpDirection = new TimeoIDNameObject();
						tmpLine = new TimeoLine(new TimeoIDNameObject(), tmpDirection);
					} else if(tagname.equalsIgnoreCase("arret")) {
						isInLineTag = false;
					}

					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if(tagname.equalsIgnoreCase("ligne")) {
						// add employee object to list
						lines.add(tmpLine);
					} else if(tmpLine != null && tagname.equalsIgnoreCase("code") && isInLineTag) {
						tmpLine.getDetails().setId(text);
					} else if(tmpLine != null && tagname.equalsIgnoreCase("nom") && isInLineTag) {
						tmpLine.getDetails().setName(smartCapitalize(text));
					} else if(tmpLine != null && tagname.equalsIgnoreCase("sens") && isInLineTag) {
						tmpLine.getDirection().setId(text);
					} else if(tmpLine != null && tagname.equalsIgnoreCase("vers") && isInLineTag) {
						tmpLine.getDirection().setName(smartCapitalize(text));
					}

					break;

				default:
					break;
			}

			eventType = parser.next();
		}

		return lines;
	}

	public List<TimeoStop> getStops(TimeoLine line) throws HttpRequestException, XmlPullParserException, IOException {
		String params = "xml=1&ligne=" + line.getDetails().getId() + "&sens=" + line.getDirection().getId();
		String result = requestWebPage(BASE_URL, params, true);

		XmlPullParser parser = getParserForXMLString(result);

		if(parser == null) {
			return null;
		}

		int eventType = parser.getEventType();

		TimeoStop tmpStop = null;
		ArrayList<TimeoStop> stops = new ArrayList<TimeoStop>();

		String text = null;
		boolean isInStopTag = true;

		while(eventType != XmlPullParser.END_DOCUMENT) {
			String tagname = parser.getName();

			switch(eventType) {
				case XmlPullParser.START_TAG:

					if(tagname.equalsIgnoreCase("ligne")) {
						isInStopTag = false;
					} else if(tagname.equalsIgnoreCase("arret")) {
						isInStopTag = true;
						tmpStop = new TimeoStop(line);
					}

					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if(tagname.equalsIgnoreCase("als")) {
						// add employee object to list
						stops.add(tmpStop);
					} else if(tmpStop != null && tagname.equalsIgnoreCase("code") && isInStopTag) {
						tmpStop.setId(text);
					} else if(tmpStop != null && tagname.equalsIgnoreCase("nom") && isInStopTag) {
						tmpStop.setName(smartCapitalize(text));
					} else if(tmpStop != null && tagname.equalsIgnoreCase("refs")) {
						tmpStop.setReference(Long.valueOf(text));
					}

					break;

				default:
					break;
			}

			eventType = parser.next();
		}

		return stops;
	}

	public TimeoStopSchedule getSingleSchedule(TimeoStop stop) throws HttpRequestException {
		String params = "xml=3&refs=" + stop.getReference() + "&ran=1";
		String result = requestWebPage(BASE_URL, params, true);

		//TODO: parsing

		return null;
	}

	public List<TimeoStopSchedule> getMultipleSchedules(List<TimeoStop> stops) throws HttpRequestException {
		String refs = "";

		for(TimeoStop stop : stops) {
			refs += stop.getReference();
		}

		refs = refs.substring(0, refs.length());

		String params = "xml=1";
		String result = requestWebPage(BASE_URL, params, true);

		//TODO: parsing

		return null;
	}


	public TimeoTrafficAlert getGlobalTrafficAlert() {
		String response = requestWebPage(BASE_PRE_HOME_URL, true);
		return parseGlobalTrafficAlert(response);
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
		String[] words = str.split("( |\\-|')");

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

	public XmlPullParser getParserForXMLString(String xml) {
		try {
			XmlPullParser parser = Xml.newPullParser();

			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(new StringReader(xml));
			parser.nextTag();

			return parser;
		} catch(XmlPullParserException e) {
			e.printStackTrace();
			return null;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
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
