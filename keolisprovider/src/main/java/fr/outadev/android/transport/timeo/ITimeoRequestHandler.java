/*
 * Twistoast - ITimeoRequestHandler
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
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by outadoc on 2016-03-25.
 */
public interface ITimeoRequestHandler {

    /**
     * Fetch the bus lines from the API.
     *
     * @return a list of lines
     * @throws XmlPullParserException if a parsing exception occurred
     * @throws IOException            if an I/O exception occurred whilst parsing the XML
     * @throws TimeoException         if the API returned an error
     */
    @NonNull
    List<TimeoLine> getLines() throws XmlPullParserException, IOException, TimeoException;

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
    List<TimeoStop> getStops(TimeoLine line) throws XmlPullParserException, IOException,
            TimeoException;

    /**
     * Fetches a schedule for a single bus stop from the API.
     *
     * @param stop the bus stop to fetch the schedule for
     * @return a NavitiaStopSchedule containing said schedule
     * @throws XmlPullParserException if a parsing exception occurred
     * @throws IOException            if an I/O exception occurred whilst parsing the XML
     * @throws TimeoException         if the API returned an error
     */
    @NonNull
    TimeoStopSchedule getSingleSchedule(TimeoStop stop) throws TimeoException, IOException,
            XmlPullParserException;

    /**
     * Fetches schedules for multiple bus stops from the API.
     *
     * @param stops a list of bus stops we should fetch the schedules for
     * @return a list of NavitiaStopSchedule containing said schedules
     * @throws XmlPullParserException if a parsing exception occurred
     * @throws IOException            if an I/O exception occurred whilst parsing the XML
     * @throws TimeoException         if the API returned an error
     */
    @NonNull
    List<TimeoStopSchedule> getMultipleSchedules(List<TimeoStop> stops) throws
            TimeoException, XmlPullParserException, IOException;

    /**
     * Fetch the bus lines from the API.
     *
     * @param networkCode the code for the city's bus network
     * @return a list of lines
     * @throws XmlPullParserException if a parsing exception occurred
     * @throws TimeoException         if the API returned an error
     */
    @NonNull
    List<TimeoLine> getLines(int networkCode) throws XmlPullParserException, IOException,
            TimeoException;

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
    List<TimeoStop> getStops(int networkCode, TimeoLine line) throws XmlPullParserException,
            IOException, TimeoException;

    /**
     * Fetches a schedule for a single bus stop from the API.
     *
     * @param networkCode the code for the city's bus network
     * @param stop        the bus stop to fetch the schedule for
     * @return a NavitiaStopSchedule containing said schedule
     * @throws XmlPullParserException if a parsing exception occurred
     * @throws TimeoException         if the API returned an error
     */
    @NonNull
    TimeoStopSchedule getSingleSchedule(int networkCode, TimeoStop stop) throws
            TimeoException, IOException, XmlPullParserException;

    /**
     * Fetches schedules for multiple bus stops from the API.
     *
     * @param networkCode the code for the city's bus network
     * @param stops       a list of bus stops we should fetch the schedules for
     * @return a list of NavitiaStopSchedule containing said schedules
     * @throws XmlPullParserException if a parsing exception occurred
     * @throws TimeoException         if the API returned an error
     */
    @NonNull
    List<TimeoStopSchedule> getMultipleSchedules(int networkCode, List<TimeoStop> stops)
            throws TimeoException, XmlPullParserException, IOException;

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
    int checkForOutdatedStops(List<TimeoStop> stops, List<TimeoStopSchedule> schedules) throws
            TimeoException;

    /**
     * Fetches the current global traffic alert message. Might or might not be null.
     *
     * @return a NavitiaTrafficAlert if an alert is currently broadcasted on the website, else null
     */
    @Nullable
    TimeoTrafficAlert getGlobalTrafficAlert();

    /**
     * Gets the list of the supported bus networks.
     *
     * @return an array containing the network names; the index is their code, and they're associated with their name
     */
    SparseArray<String> getNetworksList();
}
