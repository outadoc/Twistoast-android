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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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

import fr.outadev.android.timeo.model.TimeoException;
import fr.outadev.android.timeo.model.TimeoIDNameObject;
import fr.outadev.android.timeo.model.TimeoLine;
import fr.outadev.android.timeo.model.TimeoSingleSchedule;
import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.android.timeo.model.TimeoStopNotReturnedException;
import fr.outadev.android.timeo.model.TimeoStopSchedule;
import fr.outadev.android.timeo.model.TimeoTrafficAlert;

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
public abstract class TimeoRequestHandler {

	private final static String BASE_URL = "http://timeo3.keolis.com/relais/";
	private final static String BASE_PRE_HOME_URL = "http://twisto.fr/module/mobile/App2014/utils/getPreHome.php";

	public final static int DEFAULT_NETWORK_CODE = 117;

	private final static int REQUEST_TIMEOUT = 10000;

	/**
	 * Requests a web page via an HTTP GET request.
	 *
	 * @param url       URL to fetch
	 * @param params    HTTP GET parameters as a string (e.g. foo=bar&bar=foobar)
	 * @param useCaches true if the client can cache the request
	 * @return the raw body of the page
	 * @throws HttpRequestException if an HTTP error occurred
	 */
	private static String requestWebPage(String url, String params, boolean useCaches) throws HttpRequestException {
		Log.i("Twistoast", "requested " + url + " /w params " + params);

		return HttpRequest.get(url + "?" + params)
				.useCaches(useCaches)
				.readTimeout(REQUEST_TIMEOUT)
				.body();
	}

	/**
	 * Requests a web page via an HTTP GET request.
	 *
	 * @param url       URL to fetch
	 * @param useCaches true if the client can cache the request
	 * @return the raw body of the page
	 * @throws HttpRequestException if an HTTP error occurred
	 */
	private static String requestWebPage(String url, boolean useCaches) throws HttpRequestException {
		return requestWebPage(url, "", useCaches);
	}


	/**
	 * Shorthand methods for requesting data from the default city's API (Twisto/Caen)
	 */

	/**
	 * Fetch the bus lines from the API.
	 *
	 * @return a list of lines
	 * @throws HttpRequestException   if an HTTP error occurred
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoLine> getLines() throws HttpRequestException, XmlPullParserException, IOException, TimeoException {
		return getLines(DEFAULT_NETWORK_CODE);
	}

	/**
	 * Fetch a list of bus stops from the API.
	 *
	 * @param line the line for which we should fetch the stops
	 * @return a list of bus stops
	 * @throws HttpRequestException   if an HTTP error occurred
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoStop> getStops(TimeoLine line) throws HttpRequestException, XmlPullParserException, IOException,
			TimeoException {
		return getStops(line.getNetworkCode(), line);
	}

	/**
	 * Fetches a schedule for a single bus stop from the API.
	 *
	 * @param stop the bus stop to fetch the schedule for
	 * @return a TimeoStopSchedule containing said schedule
	 * @throws HttpRequestException   if an HTTP error occurred
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static TimeoStopSchedule getSingleSchedule(TimeoStop stop) throws HttpRequestException, TimeoException, IOException,
			XmlPullParserException {
		return getSingleSchedule(stop.getLine().getNetworkCode(), stop);
	}

	/**
	 * Fetches schedules for multiple bus stops from the API.
	 *
	 * @param stops a list of bus stops we should fetch the schedules for
	 * @return a list of TimeoStopSchedule containing said schedules
	 * @throws HttpRequestException   if an HTTP error occurred
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoStopSchedule> getMultipleSchedules(List<TimeoStop> stops) throws HttpRequestException,
			TimeoException, XmlPullParserException, IOException {
		List<Integer> networks = new ArrayList<Integer>();
		List<TimeoStopSchedule> finalScheduleList = new ArrayList<TimeoStopSchedule>();

		for(TimeoStop stop : stops) {
			if(!networks.contains(stop.getLine().getNetworkCode())) {
				networks.add(stop.getLine().getNetworkCode());
			}
		}

		Log.i("Twistoast", networks.size() + " different bus networks to refresh");

		for(Integer network : networks) {
			List<TimeoStop> stopsForThisNetwork = new ArrayList<TimeoStop>();

			for(TimeoStop stop : stops) {
				if(stop.getLine().getNetworkCode() == network) {
					stopsForThisNetwork.add(stop);
				}
			}

			finalScheduleList.addAll(getMultipleSchedules(network, stopsForThisNetwork));
		}

		return finalScheduleList;
	}


	/**
	 * Fetch the bus lines from the API.
	 *
	 * @param networkCode the code for the city's bus network
	 * @return a list of lines
	 * @throws HttpRequestException   if an HTTP error occurred
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoLine> getLines(int networkCode) throws HttpRequestException, XmlPullParserException, IOException,
			TimeoException {
		String params = "xml=1";
		String result = requestWebPage(BASE_URL + getPageNameForNetworkCode(networkCode), params, true);

		XmlPullParser parser = getParserForXMLString(result);
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
						tmpLine = new TimeoLine(new TimeoIDNameObject(), tmpDirection, networkCode);
					} else if(tagname.equalsIgnoreCase("arret")) {
						isInLineTag = false;
					}

					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if(tagname.equalsIgnoreCase("ligne")) {
						lines.add(tmpLine);
					} else if(tmpLine != null && tagname.equalsIgnoreCase("code") && isInLineTag) {
						tmpLine.getDetails().setId(text);
					} else if(tmpLine != null && tagname.equalsIgnoreCase("nom") && isInLineTag) {
						tmpLine.getDetails().setName(smartCapitalize(text));
					} else if(tmpLine != null && tagname.equalsIgnoreCase("sens") && isInLineTag) {
						tmpLine.getDirection().setId(text);
					} else if(tmpLine != null && tagname.equalsIgnoreCase("vers") && isInLineTag) {
						tmpLine.getDirection().setName(smartCapitalize(text));
					} else if(tmpLine != null && tagname.equalsIgnoreCase("couleur") && isInLineTag) {
						tmpLine.setColor("#" + StringUtils.leftPad(Integer.toHexString(Integer.valueOf(text)), 6, '0'));
					} else if(tagname.equalsIgnoreCase("erreur") && text != null && !text.trim().isEmpty()) {
						throw new TimeoException(text);
					}

					break;

				default:
					break;
			}

			eventType = parser.next();
		}

		return lines;
	}

	/**
	 * Fetch a list of bus stops from the API.
	 *
	 * @param networkCode the code for the city's bus network
	 * @param line        the line for which we should fetch the stops
	 * @return a list of bus stops
	 * @throws HttpRequestException   if an HTTP error occurred
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoStop> getStops(int networkCode, TimeoLine line) throws HttpRequestException, XmlPullParserException,
			IOException, TimeoException {
		String params = "xml=1&ligne=" + line.getDetails().getId() + "&sens=" + line.getDirection().getId();
		String result = requestWebPage(BASE_URL + getPageNameForNetworkCode(networkCode), params, true);

		XmlPullParser parser = getParserForXMLString(result);
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
						stops.add(tmpStop);
					} else if(tmpStop != null && tagname.equalsIgnoreCase("code") && isInStopTag) {
						tmpStop.setId(text);
					} else if(tmpStop != null && tagname.equalsIgnoreCase("nom") && isInStopTag) {
						tmpStop.setName(smartCapitalize(text));
					} else if(tmpStop != null && tagname.equalsIgnoreCase("refs")) {
						tmpStop.setReference(text);
					} else if(tagname.equalsIgnoreCase("erreur") && text != null && !text.trim().isEmpty()) {
						throw new TimeoException(text);
					}

					break;

				default:
					break;
			}

			eventType = parser.next();
		}

		return stops;
	}

	/**
	 * Fetches a schedule for a single bus stop from the API.
	 *
	 * @param networkCode the code for the city's bus network
	 * @param stop        the bus stop to fetch the schedule for
	 * @return a TimeoStopSchedule containing said schedule
	 * @throws HttpRequestException   if an HTTP error occurred
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static TimeoStopSchedule getSingleSchedule(int networkCode, TimeoStop stop) throws HttpRequestException,
			TimeoException, IOException, XmlPullParserException {
		List<TimeoStop> list = new ArrayList<TimeoStop>();
		list.add(stop);
		List<TimeoStopSchedule> schedules = getMultipleSchedules(networkCode, list);

		if(schedules.size() > 0) {
			return schedules.get(0);
		} else {
			throw new TimeoException();
		}
	}

	/**
	 * Fetches schedules for multiple bus stops from the API.
	 *
	 * @param networkCode the code for the city's bus network
	 * @param stops       a list of bus stops we should fetch the schedules for
	 * @return a list of TimeoStopSchedule containing said schedules
	 * @throws HttpRequestException   if an HTTP error occurred
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoStopSchedule> getMultipleSchedules(int networkCode, List<TimeoStop> stops)
			throws HttpRequestException, TimeoException, XmlPullParserException, IOException {
		String refs = "";

		if(stops.isEmpty()) {
			return new ArrayList<TimeoStopSchedule>();
		}

		for(TimeoStop stop : stops) {
			refs += stop.getReference() + ";";
		}

		refs = refs.substring(0, refs.length() - 1);

		String params = "xml=3&refs=" + refs + "&ran=1";
		String result = requestWebPage(BASE_URL + getPageNameForNetworkCode(networkCode), params, true);

		XmlPullParser parser = getParserForXMLString(result);
		int eventType = parser.getEventType();

		//final schedules to return
		List<TimeoStopSchedule> schedules = new ArrayList<TimeoStopSchedule>();

		//temporary schedule (associated with a stop and a few schedules)
		TimeoStopSchedule tmpSchedule = null;
		//temporary single schedule (one time, one destination)
		TimeoSingleSchedule tmpSingleSchedule = null;

		String text = null;

		while(eventType != XmlPullParser.END_DOCUMENT) {
			String tagname = parser.getName();

			switch(eventType) {
				case XmlPullParser.START_TAG:

					if(tagname.equalsIgnoreCase("horaire")) {
						tmpSchedule = new TimeoStopSchedule(null, new ArrayList<TimeoSingleSchedule>());
					} else if(tagname.equalsIgnoreCase("passage")) {
						tmpSingleSchedule = new TimeoSingleSchedule();
					}

					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if(tagname.equalsIgnoreCase("code")) {
						//the next stop returned by the API /isn't/ the next stop in the list, abort
						if(!stops.get(schedules.size()).getId().equals(text)) {
							throw new TimeoStopNotReturnedException("Trying to associate returned stop " + text + " with stop "
									+ stops.get(schedules.size()).getId());
						} else if(tmpSchedule != null) {
							tmpSchedule.setStop(stops.get(schedules.size()));
						}
					} else if(tmpSingleSchedule != null && tagname.equalsIgnoreCase("duree")) {
						tmpSingleSchedule.setTime(text);
					} else if(tmpSingleSchedule != null && tagname.equalsIgnoreCase("destination")) {
						tmpSingleSchedule.setDirection(smartCapitalize(text));
					} else if(tmpSingleSchedule != null && tmpSchedule != null && tagname.equalsIgnoreCase("passage")) {
						tmpSchedule.getSchedules().add(tmpSingleSchedule);
					} else if(tmpSchedule != null && tagname.equalsIgnoreCase("horaire")) {
						schedules.add(tmpSchedule);
					} else if(tagname.equalsIgnoreCase("erreur") && text != null && !text.trim().isEmpty()) {
						throw new TimeoException(text);
					}

					break;

				default:
					break;
			}

			eventType = parser.next();
		}

		return schedules;
	}

	/**
	 * Fetches the current global traffic alert message. Might or might not be null.
	 *
	 * @return a TimeoTrafficAlert if an alert is currently broadcasted on the website, else null
	 */
	@Nullable
	public static TimeoTrafficAlert getGlobalTrafficAlert() {
		String source = requestWebPage(BASE_PRE_HOME_URL, true);

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
	public static String smartCapitalize(String str) {
		String newStr = "";
		str = str.toLowerCase().trim();

		//these words will never be capitalized
		String[] determinants = new String[]{"de", "du", "des", "au", "aux", "à", "la", "le", "les", "d", "et", "l"};
		//these words will always be capitalized
		String[] specialWords = new String[]{"sncf", "chu", "chr", "chs", "crous", "suaps", "fpa", "za", "zi", "zac", "cpam",
				"efs", "mjc"};

		//explode the string with both spaces and apostrophes
		String[] words = str.split("( |\\-|'|\\/)");

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
	 * Gets an XmlPullParser for an XML string.
	 *
	 * @param xml an xml document in a String, sexy
	 * @return an XmlPullParser ready to parse the document
	 */
	private static XmlPullParser getParserForXMLString(String xml) throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();

		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(new StringReader(xml));
		parser.nextTag();

		return parser;
	}

	private static String getPageNameForNetworkCode(int networkCode) {
		return networkCode + ".php";
	}

}
