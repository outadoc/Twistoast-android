/*
 * Twistoast - TimeoRequestHandler
 * Copyright (C) 2013-2015 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
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

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
public abstract class TimeoRequestHandler {

	public final static String TAG = "Timeo";

	public final static int DEFAULT_NETWORK_CODE = 147;
	public final static String API_BASE_URL = "http://timeo3.keolis.com/relais/";

	/**
	 * Requests a web page via an HTTP GET request.
	 *
	 * @param url       URL to fetch
	 * @param params    HTTP GET parameters as a string (e.g. foo=bar&bar=foobar)
	 * @param useCaches true if the client can cache the request
	 * @return the raw body of the page
	 * @throws IOException if an HTTP error occurred
	 */
	private static String requestWebPage(String url, String params, boolean useCaches) throws IOException {
		String finalUrl = url + "?" + params;
		Log.i(TAG, "requesting " + finalUrl);

		OkHttpClient client = new OkHttpClient();

		Request.Builder builder = new Request.Builder()
				.url(finalUrl);

		if(!useCaches) {
			builder.cacheControl(CacheControl.FORCE_NETWORK);
		}

		Response response = client.newCall(builder.build()).execute();
		return response.body().string();
	}

	/**
	 * Requests a web page via an HTTP GET request.
	 *
	 * @param url       URL to fetch
	 * @param useCaches true if the client can cache the request
	 * @return the raw body of the page
	 * @throws IOException if an HTTP error occurred
	 */
	private static String requestWebPage(String url, boolean useCaches) throws IOException {
		return requestWebPage(url, "", useCaches);
	}


	/**
	 * Shorthand methods for requesting data from the default city's API (Twisto/Caen)
	 */

	/**
	 * Fetch the bus lines from the API.
	 *
	 * @return a list of lines
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoLine> getLines() throws XmlPullParserException, IOException, TimeoException {
		return getLines(DEFAULT_NETWORK_CODE);
	}

	/**
	 * Fetch a list of bus stops from the API.
	 *
	 * @param line the line for which we should fetch the stops
	 * @return a list of bus stops
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoStop> getStops(TimeoLine line) throws XmlPullParserException, IOException,
			TimeoException {
		return getStops(line.getNetworkCode(), line);
	}

	/**
	 * Fetches a schedule for a single bus stop from the API.
	 *
	 * @param stop the bus stop to fetch the schedule for
	 * @return a TimeoStopSchedule containing said schedule
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static TimeoStopSchedule getSingleSchedule(TimeoStop stop) throws TimeoException, IOException,
			XmlPullParserException {
		return getSingleSchedule(stop.getLine().getNetworkCode(), stop);
	}

	/**
	 * Fetches schedules for multiple bus stops from the API.
	 *
	 * @param stops a list of bus stops we should fetch the schedules for
	 * @return a list of TimeoStopSchedule containing said schedules
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws IOException            if an I/O exception occurred whilst parsing the XML
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoStopSchedule> getMultipleSchedules(List<TimeoStop> stops) throws
			TimeoException, XmlPullParserException, IOException {
		//if we don't specify any network code when calling getMultipleSchedules, we'll have to figure them out ourselves.
		//we can only fetch a list of schedules that are all part of the same network.
		//therefore, we'll have to separate them in different lists and request them individually.

		//a list of all the different network codes we'll have to check
		List<Integer> networks = new ArrayList<>();
		//the final list that will contain all of our schedules
		List<TimeoStopSchedule> finalScheduleList = new ArrayList<>();

		//list all the required network codes, and add them to the list
		for(TimeoStop stop : stops) {
			if(!networks.contains(stop.getLine().getNetworkCode())) {
				networks.add(stop.getLine().getNetworkCode());
			}
		}

		Log.i(TAG, networks.size() + " different bus networks to refresh");

		//for each network
		for(Integer network : networks) {
			List<TimeoStop> stopsForThisNetwork = new ArrayList<>();

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
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoLine> getLines(int networkCode) throws XmlPullParserException, IOException,
			TimeoException {
		String params = "xml=1";
		String result = requestWebPage(API_BASE_URL + getPageNameForNetworkCode(networkCode), params, true);

		XmlPullParser parser = getParserForXMLString(result);
		int eventType = parser.getEventType();

		TimeoLine tmpLine = null;
		TimeoIDNameObject tmpDirection;

		ArrayList<TimeoLine> lines = new ArrayList<>();

		String errorCode = null;
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

					} else if(tagname.equals("erreur")) {
						errorCode = parser.getAttributeValue(null, "code");
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

					} else if(tagname.equals("erreur")) {
						if((errorCode != null && !errorCode.equals("000")) || (text != null && !text.trim().isEmpty())) {
							throw new TimeoException(errorCode, text);
						}
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
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoStop> getStops(int networkCode, TimeoLine line) throws XmlPullParserException,
			IOException, TimeoException {
		String params = "xml=1&ligne=" + line.getId() + "&sens=" + line.getDirection().getId();
		String result = requestWebPage(API_BASE_URL + getPageNameForNetworkCode(networkCode), params, true);

		XmlPullParser parser = getParserForXMLString(result);
		int eventType = parser.getEventType();

		TimeoStop tmpStop = null;
		ArrayList<TimeoStop> stops = new ArrayList<>();

		String errorCode = null;
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

					} else if(tagname.equals("erreur")) {
						errorCode = parser.getAttributeValue(null, "code");
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

					} else if(tagname.equals("erreur")) {
						if((errorCode != null && !errorCode.equals("000")) || (text != null && !text.trim().isEmpty())) {
							throw new TimeoException(errorCode, text);
						}
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
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static TimeoStopSchedule getSingleSchedule(int networkCode, TimeoStop stop) throws
			TimeoException, IOException, XmlPullParserException {
		List<TimeoStop> list = new ArrayList<>();
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
	 * @throws XmlPullParserException if a parsing exception occurred
	 * @throws TimeoException         if the API returned an error
	 */
	@NonNull
	public static List<TimeoStopSchedule> getMultipleSchedules(int networkCode, List<TimeoStop> stops)
			throws TimeoException, XmlPullParserException, IOException {
		//final schedules to return
		List<TimeoStopSchedule> schedules = new ArrayList<>();

		//the list of stop references we'll be sending to the api
		String refs = "";

		//append the stop references to the refs
		for(TimeoStop stop : stops) {
			if(stop.getReference() != null) {
				refs += stop.getReference() + ";";
			}
		}

		//if no stops are in the list or all refs are null
		if(stops.isEmpty() || refs.isEmpty()) {
			return schedules;
		}

		//don't keep the last semicolon
		refs = refs.substring(0, refs.length() - 1);

		String params = "xml=3&refs=" + URLEncoder.encode(refs, "UTF-8") + "&ran=1";
		String result = requestWebPage(API_BASE_URL + getPageNameForNetworkCode(networkCode), params, false);

		//create a new parser
		XmlPullParser parser = getParserForXMLString(result);
		int eventType = parser.getEventType();

		//temporary schedule (associated with a stop and a few schedules)
		TimeoStopSchedule tmpSchedule = null;
		//temporary single schedule (one time, one destination)
		TimeoSingleSchedule tmpSingleSchedule = null;
		//temporary blocking exception, in case we meet a blocking network message
		TimeoBlockingMessageException tmpBlockingException = null;

		String tmpLineId = null;
		String tmpStopId = null;

		String errorCode = null;

		//text will contain the last text value we encountered during the parsing
		String text = null;

		//parse the whole document
		while(eventType != XmlPullParser.END_DOCUMENT) {
			String tagname = parser.getName();

			switch(eventType) {
				case XmlPullParser.START_TAG:

					if(tagname.equals("horaire")) {
						//if this is the start of the stop schedule's list, instantiate it with empty values
						tmpSchedule = new TimeoStopSchedule();

					} else if(tagname.equals("passage")) {
						//if this is a new schedule time
						tmpSingleSchedule = new TimeoSingleSchedule();

					} else if(tagname.equals("reseau")) {
						//if this is a network-wide traffic message
						tmpBlockingException = new TimeoBlockingMessageException();

					} else if(tagname.equals("erreur")) {
						//if this is an API error, remember the error code
						errorCode = parser.getAttributeValue(null, "code");
					}

					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if(tmpSchedule != null && tagname.equals("code")) {
						tmpStopId = text;

					} else if(tmpSchedule != null && tagname.equals("ligne")) {
						tmpLineId = text;

					} else if(tmpSchedule != null && tagname.equals("sens")) {

						//try to find the stop we're currently looking at in the list
						for(TimeoStop i : stops) {
							//if this one has got the right stop id, line id, and direction id, looks like we've found it
							if(i.getId().equals(tmpStopId) && i.getLine().getId().equals(tmpLineId)
									&& i.getLine().getDirection().getId().equals(text)) {
								//remember the stop
								tmpSchedule.setStop(i);
							}
						}

						//if we didn't find it, just set the schedule to null and carry on.
						if(tmpSchedule.getStop() == null) {
							tmpSchedule = null;
						}

					} else if(tmpSingleSchedule != null && tagname.equals("duree")) {
						tmpSingleSchedule.setTime(text);

					} else if(tmpSingleSchedule != null && tagname.equals("destination")) {
						tmpSingleSchedule.setDirection(smartCapitalize(text));

					} else if(tmpSingleSchedule != null && tmpSchedule != null && tagname.equals("passage")) {
						tmpSchedule.getSchedules().add(tmpSingleSchedule);

					} else if(tmpSchedule != null && tagname.equals("horaire")) {
						schedules.add(tmpSchedule);

					} else if(tagname.equals("erreur")) {
						if((errorCode != null && !errorCode.equals("000")) || (text != null && !text.trim().isEmpty())) {
							throw new TimeoException(errorCode, text);
						}

					} else if(tmpBlockingException != null && tagname.equals("titre") && !text.isEmpty()) {
						tmpBlockingException.setMessageTitle(text);

					} else if(tmpBlockingException != null && tagname.equals("texte") && !text.isEmpty()) {
						tmpBlockingException.setMessageBody(text);

					} else if(tmpBlockingException != null && tagname.equals("bloquant") && text.equals("true")) {
						//if we met with a blocking network-wide traffic message
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
	 * Checks if there are outdated stops amongst those in the database,
	 * by comparing them to a list of schedules returned by the API.
	 * <p/>
	 * The isOutdated property of the bus stops will be set accordingly.
	 *
	 * @param stops     a list of bus stops to check. their isOutdated property may be modified
	 * @param schedules a list of schedules returned by the API and corresponding to the stops
	 * @return the number of outdated stops that have been found
	 * @throws TimeoException if stops or schedules is null
	 */
	public static int checkForOutdatedStops(List<TimeoStop> stops, List<TimeoStopSchedule> schedules) throws
			TimeoException {
		if(stops == null || schedules == null) {
			throw new TimeoException();
		}

		if(stops.size() == schedules.size()) {
			return 0;
		}

		int count = 0;

		for(TimeoStop stop : stops) {
			boolean outdated = true;

			for(TimeoStopSchedule schedule : schedules) {
				if(schedule.getStop() == stop) {
					outdated = false;
					count++;
					break;
				}
			}

			stop.setOutdated(outdated);
		}

		return count;
	}

	/**
	 * Fetches the current global traffic alert message. Might or might not be null.
	 *
	 * @return a TimeoTrafficAlert if an alert is currently broadcasted on the website, else null
	 */
	@Nullable
	public static TimeoTrafficAlert getGlobalTrafficAlert(String preHomeUrl) {
		try {
			String source = requestWebPage(preHomeUrl, true);

			if(source != null && !source.isEmpty()) {
				JSONObject obj = (JSONObject) new JSONTokener(source).nextValue();

				if(obj.has("alerte")) {
					JSONObject alert = obj.getJSONObject("alerte");
					return new TimeoTrafficAlert(alert.getInt("id_alerte"), alert.getString("libelle_alerte"),
							alert.getString("url_alerte"));
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
			return null;
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

	/**
	 * Gets the list of the supported bus networks.
	 *
	 * @return an array containing the network names; the index is their code, and they're associated with their name
	 */
	public static SparseArray<String> getNetworksList() {
		SparseArray<String> networks = new SparseArray<>();

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

	/**
	 * Returns the API endpoint for a given network code.
	 *
	 * @param networkCode the code for the city's bus network
	 * @return the name of the page that has to be called for this specific network
	 */
	private static String getPageNameForNetworkCode(int networkCode) {
		return networkCode + ".php";
	}

}
