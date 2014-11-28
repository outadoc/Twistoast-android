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
import android.util.SparseArray;
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

import fr.outadev.android.timeo.model.TimeoBlockingMessageException;
import fr.outadev.android.timeo.model.TimeoException;
import fr.outadev.android.timeo.model.TimeoIDNameObject;
import fr.outadev.android.timeo.model.TimeoLine;
import fr.outadev.android.timeo.model.TimeoSingleSchedule;
import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.android.timeo.model.TimeoStopSchedule;
import fr.outadev.android.timeo.model.TimeoTrafficAlert;
import fr.outadev.twistoast.Utils;

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
public abstract class TimeoRequestHandler {

	public final static int DEFAULT_NETWORK_CODE = 147;
	private final static int REQUEST_TIMEOUT = 10000;

	private final static String API_BASE_URL = "http://timeo3.keolis.com/relais/";
	private final static String PRE_HOME_URL = "http://twisto.fr/module/mobile/App2014/utils/getPreHome.php";

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
		String finalUrl = url + "?" + params;
		Log.i(Utils.TAG, "requesting " + finalUrl);

		return HttpRequest.get(finalUrl)
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
		//if we don't specify any network code when calling getMultipleSchedules, we'll have to figure them out ourselves.
		//we can only fetch a list of schedules that are all part of the same network.
		//therefore, we'll have to separate them in different lists and request them individually.

		//a list of all the different network codes we'll have to check
		List<Integer> networks = new ArrayList<Integer>();
		//the final list that will contain all of our schedules
		List<TimeoStopSchedule> finalScheduleList = new ArrayList<TimeoStopSchedule>();

		//list all the required network codes, and add them to the list
		for(TimeoStop stop : stops) {
			if(!networks.contains(stop.getLine().getNetworkCode())) {
				networks.add(stop.getLine().getNetworkCode());
			}
		}

		Log.i(Utils.TAG, networks.size() + " different bus networks to refresh");

		//for each network
		for(Integer network : networks) {
			List<TimeoStop> stopsForThisNetwork = new ArrayList<TimeoStop>();

			//get the list of stops we'll have to request
			for(TimeoStop stop : stops) {
				if(stop.getLine().getNetworkCode() == network) {
					stopsForThisNetwork.add(stop);
				}
			}

			//request the schedules and add them to the final list
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
		String result = requestWebPage(API_BASE_URL + getPageNameForNetworkCode(networkCode), params, true);

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

					if(tagname.equals("ligne")) {
						isInLineTag = true;
						tmpDirection = new TimeoIDNameObject();
						tmpLine = new TimeoLine(new TimeoIDNameObject(), tmpDirection, networkCode);
					} else if(tagname.equals("arret")) {
						isInLineTag = false;
					}

					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if(tagname.equals("ligne")) {
						lines.add(tmpLine);
					} else if(tmpLine != null && tagname.equals("code") && isInLineTag) {
						tmpLine.getDetails().setId(text);
					} else if(tmpLine != null && tagname.equals("nom") && isInLineTag) {
						tmpLine.getDetails().setName(smartCapitalize(text));
					} else if(tmpLine != null && tagname.equals("sens") && isInLineTag) {
						tmpLine.getDirection().setId(text);
					} else if(tmpLine != null && tagname.equals("vers") && isInLineTag) {
						tmpLine.getDirection().setName(smartCapitalize(text));
					} else if(tmpLine != null && tagname.equals("couleur") && isInLineTag) {
						tmpLine.setColor("#" + StringUtils.leftPad(Integer.toHexString(Integer.valueOf(text)), 6, '0'));
					} else if(tagname.equals("erreur") && text != null && !text.trim().isEmpty()) {
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
		String params = "xml=1&ligne=" + line.getId() + "&sens=" + line.getDirection().getId();
		String result = requestWebPage(API_BASE_URL + getPageNameForNetworkCode(networkCode), params, true);

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

					if(tagname.equals("ligne")) {
						isInStopTag = false;
					} else if(tagname.equals("arret")) {
						isInStopTag = true;
						tmpStop = new TimeoStop(line);
					}

					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if(tagname.equals("als")) {
						stops.add(tmpStop);
					} else if(tmpStop != null && tagname.equals("code") && isInStopTag) {
						tmpStop.setId(text);
					} else if(tmpStop != null && tagname.equals("nom") && isInStopTag) {
						tmpStop.setName(smartCapitalize(text));
					} else if(tmpStop != null && tagname.equals("refs")) {
						tmpStop.setReference(text);
					} else if(tagname.equals("erreur") && text != null && !text.trim().isEmpty()) {
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
			throw new TimeoException("No schedules were returned.");
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
		String result = requestWebPage(API_BASE_URL + getPageNameForNetworkCode(networkCode), params, false);

		XmlPullParser parser = getParserForXMLString(result);
		int eventType = parser.getEventType();

		//final schedules to return
		List<TimeoStopSchedule> schedules = new ArrayList<TimeoStopSchedule>();

		//temporary schedule (associated with a stop and a few schedules)
		TimeoStopSchedule tmpSchedule = null;
		//temporary single schedule (one time, one destination)
		TimeoSingleSchedule tmpSingleSchedule = null;
		//temporary blocking exception, in case we meet a blocking network message
		TimeoBlockingMessageException tmpBlockingException = null;

		String text = null;

		while(eventType != XmlPullParser.END_DOCUMENT) {
			String tagname = parser.getName();

			switch(eventType) {
				case XmlPullParser.START_TAG:

					if(tagname.equals("horaire")) {
						tmpSchedule = new TimeoStopSchedule(null, new ArrayList<TimeoSingleSchedule>());
					} else if(tagname.equals("passage")) {
						tmpSingleSchedule = new TimeoSingleSchedule();
					} else if(tagname.equals("reseau")) {
						tmpBlockingException = new TimeoBlockingMessageException();
					}

					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if(tmpSchedule != null && tagname.equals("code")) {
						tmpSchedule.setStop(stops.get(schedules.size()));
					} else if(tmpSingleSchedule != null && tagname.equals("duree")) {
						tmpSingleSchedule.setTime(text);
					} else if(tmpSingleSchedule != null && tagname.equals("destination")) {
						tmpSingleSchedule.setDirection(smartCapitalize(text));
					} else if(tmpSingleSchedule != null && tmpSchedule != null && tagname.equals("passage")) {
						tmpSchedule.getSchedules().add(tmpSingleSchedule);
					} else if(tmpSchedule != null && tagname.equals("horaire")) {
						schedules.add(tmpSchedule);
					} else if(tagname.equals("erreur") && text != null && !text.trim().isEmpty()) {
						throw new TimeoException(text);
					} else if(tmpBlockingException != null && tagname.equals("titre") && !text.isEmpty()) {
						tmpBlockingException.setMessageTitle(text);
					} else if(tmpBlockingException != null && tagname.equals("texte") && !text.isEmpty()) {
						tmpBlockingException.setMessageBody(text);
					} else if(tmpBlockingException != null && tagname.equals("bloquant") && text.equals("true")) {
						throw tmpBlockingException;
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
		String source = requestWebPage(PRE_HOME_URL, true);

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

		return StringUtils.capitalize(newStr);
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

	public static SparseArray<String> getNetworksList() {
		SparseArray<String> networks = new SparseArray<String>();

		networks.put(105, "Le Mans");
		networks.put(117, "Pau");
		networks.put(120, "Soissons");
		networks.put(135, "Aix-en-Provence");
		networks.put(147, "Caen");
		networks.put(217, "Dijon");
		networks.put(297, "Brest");
		networks.put(402, "Pau-Agen");
		networks.put(416, "Blois");
		networks.put(422, "Saint-Étienne");
		networks.put(440, "Nantes");
		networks.put(457, "Montargis");
		networks.put(497, "Angers");
		networks.put(691, "Macon-Villefranche");
		networks.put(910, "Épinay-sur-Orge");
		networks.put(999, "Rennes");

		return networks;
	}

	private static String getPageNameForNetworkCode(int networkCode) {
		return networkCode + ".php";
	}

}
