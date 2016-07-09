/*
 * Twistoast - TimeoRequestHandler
 * Copyright (C) 2013-2016 Baptiste Candellier
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

package fr.outadev.android.transport.timeo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import fr.outadev.android.transport.Util;

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
public class TimeoRequestHandler implements ITimeoRequestHandler {

    public final static String TAG = TimeoRequestHandler.class.getSimpleName();

    public final static int DEFAULT_NETWORK_CODE = 147;

    public final static String API_BASE_URL = "http://timeo3.keolis.com/relais/";
    public final static String PRE_HOME_URL = "http://twisto.fr/module/mobile/App2014/utils/getPreHome.php";

    /**
     * Shorthand methods for requesting data from the default city's API (Twisto/Caen)
     */

    @Override
    @NonNull
    public List<TimeoLine> getLines() throws XmlPullParserException, IOException, TimeoException {
        return getLines(DEFAULT_NETWORK_CODE);
    }

    @Override
    @NonNull
    public List<TimeoStop> getStops(TimeoLine line) throws XmlPullParserException, IOException,
            TimeoException {
        return getStops(line.getNetworkCode(), line);
    }

    @Override
    @NonNull
    public TimeoStopSchedule getSingleSchedule(TimeoStop stop) throws TimeoException, IOException,
            XmlPullParserException {
        return getSingleSchedule(stop.getLine().getNetworkCode(), stop);
    }

    @Override
    @NonNull
    public List<TimeoStopSchedule> getMultipleSchedules(List<TimeoStop> stops) throws
            TimeoException, XmlPullParserException, IOException {
        //if we don't specify any network code when calling getMultipleSchedules, we'll have to figure them out ourselves.
        //we can only fetch a list of schedules that are all part of the same network.
        //therefore, we'll have to separate them in different lists and request them individually.

        //a list of all the different network codes we'll have to check
        List<Integer> networks = new ArrayList<>();
        //the final list that will contain all of our schedules
        List<TimeoStopSchedule> finalScheduleList = new ArrayList<>();

        //list all the required network codes, and add them to the list
        for (TimeoStop stop : stops) {
            if (!networks.contains(stop.getLine().getNetworkCode())) {
                networks.add(stop.getLine().getNetworkCode());
            }
        }

        Log.i(TAG, networks.size() + " different bus networks to refresh");

        //for each network
        for (Integer network : networks) {
            List<TimeoStop> stopsForThisNetwork = new ArrayList<>();

            //get the list of stops we'll have to request
            for (TimeoStop stop : stops) {
                if (stop.getLine().getNetworkCode() == network) {
                    stopsForThisNetwork.add(stop);
                }
            }

            //request the schedules and add them to the final list
            finalScheduleList.addAll(getMultipleSchedules(network, stopsForThisNetwork));
        }

        return finalScheduleList;
    }


    @Override
    @NonNull
    public List<TimeoLine> getLines(int networkCode) throws XmlPullParserException, IOException,
            TimeoException {
        String params = "xml=1";
        String result = HttpRequester.getInstance().requestWebPage(getEndpointUrl(networkCode),
                params, true);

        XmlPullParser parser = getParserForXMLString(result);
        int eventType = parser.getEventType();

        TimeoLine tmpLine = null;
        TimeoIDNameObject tmpDirection;

        ArrayList<TimeoLine> lines = new ArrayList<>();

        String errorCode = null;
        String text = null;

        boolean isInLineTag = false;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagname = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:

                    if (tagname.equals("ligne")) {
                        isInLineTag = true;
                        tmpDirection = new TimeoIDNameObject();
                        tmpLine = new TimeoLine(new TimeoIDNameObject(), tmpDirection, networkCode);

                    } else if (tagname.equals("arret")) {
                        isInLineTag = false;

                    } else if (tagname.equals("erreur")) {
                        errorCode = parser.getAttributeValue(null, "code");
                    }

                    break;

                case XmlPullParser.TEXT:
                    text = parser.getText();
                    break;

                case XmlPullParser.END_TAG:
                    if (tagname.equals("ligne")) {
                        lines.add(tmpLine);

                    } else if (tmpLine != null && tagname.equals("code") && isInLineTag) {
                        tmpLine.getDetails().setId(text);

                    } else if (tmpLine != null && tagname.equals("nom") && isInLineTag) {
                        tmpLine.getDetails().setName(Util.smartCapitalize(text));

                    } else if (tmpLine != null && tagname.equals("sens") && isInLineTag) {
                        tmpLine.getDirection().setId(text);

                    } else if (tmpLine != null && tagname.equals("vers") && isInLineTag) {
                        tmpLine.getDirection().setName(Util.smartCapitalize(text));

                    } else if (tmpLine != null && tagname.equals("couleur") && isInLineTag) {
                        tmpLine.setColor("#" + StringUtils.leftPad(Integer.toHexString(Integer.valueOf(text)), 6, '0'));

                    } else if (tagname.equals("erreur")) {
                        if ((errorCode != null && !errorCode.equals("000")) || (text != null && !text.trim().isEmpty())) {
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

    @Override
    @NonNull
    public List<TimeoStop> getStops(int networkCode, TimeoLine line) throws XmlPullParserException,
            IOException, TimeoException {
        String params = "xml=1&ligne=" + line.getId() + "&sens=" + line.getDirection().getId();
        String result = HttpRequester.getInstance().requestWebPage(getEndpointUrl(networkCode),
                params, true);

        XmlPullParser parser = getParserForXMLString(result);
        int eventType = parser.getEventType();

        TimeoStop tmpStop = null;
        ArrayList<TimeoStop> stops = new ArrayList<>();

        String errorCode = null;
        String text = null;

        boolean isInStopTag = true;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagname = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:

                    if (tagname.equals("ligne")) {
                        isInStopTag = false;

                    } else if (tagname.equals("arret")) {
                        isInStopTag = true;
                        tmpStop = new TimeoStop(line);

                    } else if (tagname.equals("erreur")) {
                        errorCode = parser.getAttributeValue(null, "code");
                    }

                    break;

                case XmlPullParser.TEXT:
                    text = parser.getText();
                    break;

                case XmlPullParser.END_TAG:
                    if (tagname.equals("als")) {
                        stops.add(tmpStop);

                    } else if (tmpStop != null && tagname.equals("code") && isInStopTag) {
                        tmpStop.setId(text);

                    } else if (tmpStop != null && tagname.equals("nom") && isInStopTag) {
                        tmpStop.setName(Util.smartCapitalize(text));

                    } else if (tmpStop != null && tagname.equals("refs")) {
                        tmpStop.setReference(text);

                    } else if (tagname.equals("erreur")) {
                        if ((errorCode != null && !errorCode.equals("000")) || (text != null && !text.trim().isEmpty())) {
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

    @Override
    @NonNull
    public TimeoStopSchedule getSingleSchedule(int networkCode, TimeoStop stop) throws
            TimeoException, IOException, XmlPullParserException {
        List<TimeoStop> list = new ArrayList<>();
        list.add(stop);
        List<TimeoStopSchedule> schedules = getMultipleSchedules(networkCode, list);

        if (schedules.size() > 0) {
            return schedules.get(0);
        } else {
            throw new TimeoException("No schedules were returned.");
        }
    }

    @Override
    @NonNull
    public List<TimeoStopSchedule> getMultipleSchedules(int networkCode, List<TimeoStop> stops)
            throws TimeoException, XmlPullParserException, IOException {
        //final schedules to return
        List<TimeoStopSchedule> schedules = new ArrayList<>();

        //the list of stop references we'll be sending to the api
        String refs = "";

        //append the stop references to the refs
        for (TimeoStop stop : stops) {
            if (stop.getReference() != null) {
                refs += stop.getReference() + ";";
            }
        }

        //if no stops are in the list or all refs are null
        if (stops.isEmpty() || refs.isEmpty()) {
            return schedules;
        }

        //don't keep the last semicolon
        refs = refs.substring(0, refs.length() - 1);

        String params = "xml=3&refs=" + URLEncoder.encode(refs, "UTF-8") + "&ran=1";
        String result = HttpRequester.getInstance().requestWebPage(getEndpointUrl(networkCode),
                params, false);

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
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagname = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:

                    if (tagname.equals("horaire")) {
                        //if this is the start of the stop schedule's list, instantiate it with empty values
                        tmpSchedule = new TimeoStopSchedule();

                    } else if (tagname.equals("passage")) {
                        //if this is a new schedule time
                        tmpSingleSchedule = new TimeoSingleSchedule();

                    } else if (tagname.equals("reseau")) {
                        //if this is a network-wide traffic message
                        tmpBlockingException = new TimeoBlockingMessageException();

                    } else if (tagname.equals("erreur")) {
                        //if this is an API error, remember the error code
                        errorCode = parser.getAttributeValue(null, "code");
                    } else if (tmpSchedule != null && tagname.equals("message")) {
                        tmpSchedule.setStopTrafficAlert(new TimeoStopTrafficAlert());
                    }

                    break;

                case XmlPullParser.TEXT:
                    text = parser.getText();
                    break;

                case XmlPullParser.END_TAG:
                    if (tmpSchedule != null && tagname.equals("code")) {
                        tmpStopId = text;

                    } else if (tmpSchedule != null && tagname.equals("ligne")) {
                        tmpLineId = text;

                    } else if (tmpSchedule != null && tagname.equals("sens")) {

                        //try to find the stop we're currently looking at in the list
                        for (TimeoStop i : stops) {
                            //if this one has got the right stop id, line id, and direction id, looks like we've found it
                            if (i.getId().equals(tmpStopId) && i.getLine().getId().equals(tmpLineId)
                                    && i.getLine().getDirection().getId().equals(text)) {
                                //remember the stop
                                tmpSchedule.setStop(i);
                            }
                        }

                        //if we didn't find it, just set the schedule to null and carry on.
                        if (tmpSchedule.getStop() == null) {
                            tmpSchedule = null;
                        }

                    } else if (tmpSingleSchedule != null && tagname.equals("duree")) {
                        tmpSingleSchedule.setScheduleTime(text);

                    } else if (tmpSingleSchedule != null && tagname.equals("destination")) {
                        tmpSingleSchedule.setDirection(Util.smartCapitalize(text));

                    } else if (tmpSingleSchedule != null && tmpSchedule != null && tagname.equals("passage")) {
                        tmpSchedule.getSchedules().add(tmpSingleSchedule);

                    } else if (tmpSchedule != null && tagname.equals("horaire")) {
                        schedules.add(tmpSchedule);

                    } else if (tagname.equals("erreur")) {
                        if ((errorCode != null && !errorCode.equals("000")) || (text != null && !text.trim().isEmpty())) {
                            throw new TimeoException(errorCode, text);
                        }

                    } else if (tagname.equals("titre") && !text.isEmpty()) {
                        if (tmpBlockingException != null) {
                            // global network-wide message
                            tmpBlockingException.setMessageTitle(text);
                        } else {
                            // traffic alert localised to this stop
                            tmpSchedule.getStopTrafficAlert().setMessageTitle(text);
                        }

                    } else if (tagname.equals("texte") && !text.isEmpty()) {
                        if (tmpBlockingException != null) {
                            // global network-wide message
                            tmpBlockingException.setMessageBody(text);
                        } else {
                            // traffic alert localised to this stop
                            tmpSchedule.getStopTrafficAlert().setMessageBody(text);
                        }

                    } else if (tmpBlockingException != null && tagname.equals("bloquant")) {
                        if (text.equals("true")) {
                            //if we met with a blocking network-wide traffic message
                            throw tmpBlockingException;
                        } else {
                            // reset the message to make place for more processing
                            tmpBlockingException = null;
                        }
                    }

                    break;

                default:
                    break;
            }

            eventType = parser.next();
        }

        return schedules;
    }

    @Override
    public int checkForOutdatedStops(List<TimeoStop> stops, List<TimeoStopSchedule> schedules) throws
            TimeoException {
        if (stops == null || schedules == null) {
            throw new TimeoException();
        }

        if (stops.size() == schedules.size()) {
            return 0;
        }

        int count = 0;

        for (TimeoStop stop : stops) {
            boolean outdated = true;

            if (stop.getReference() == null) {
                count++;
            }

            for (TimeoStopSchedule schedule : schedules) {
                if (schedule.getStop() == stop) {
                    outdated = false;
                    count++;
                    break;
                }
            }

            stop.setOutdated(outdated);
        }

        return count;
    }

    @Override
    @Nullable
    public TimeoTrafficAlert getGlobalTrafficAlert() {
        try {
            String source = HttpRequester.getInstance().requestWebPage(PRE_HOME_URL, true);

            if (!source.isEmpty()) {
                JSONObject obj = (JSONObject) new JSONTokener(source).nextValue();

                if (obj.has("alerte")) {
                    JSONObject alert = obj.getJSONObject("alerte");
                    return new TimeoTrafficAlert(
                            alert.getInt("id_alerte"),
                            alert.getString("libelle_alerte").trim().replaceAll("  ", " - "),
                            alert.getString("url_alerte"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    /**
     * Gets an XmlPullParser for an XML string.
     *
     * @param xml an xml document in a String, sexy
     * @return an XmlPullParser ready to parse the document
     */
    private XmlPullParser getParserForXMLString(String xml) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xml));
        parser.nextTag();

        return parser;
    }

    @Override
    public SparseArray<String> getNetworksList() {
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
    private static String getEndpointUrl(int networkCode) {
        return API_BASE_URL + networkCode + ".php";
    }

}
