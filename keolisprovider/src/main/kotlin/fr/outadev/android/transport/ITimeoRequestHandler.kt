/*
 * Twistoast - ITimeoRequestHandler.kt
 * Copyright (C) 2013-2018 Baptiste Candellier
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

package fr.outadev.android.transport

import fr.outadev.twistoast.model.DataProviderException
import fr.outadev.twistoast.model.Line
import fr.outadev.twistoast.model.Stop
import fr.outadev.twistoast.model.StopSchedule
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * Request data from the transportation network's API.
 */
interface ITimeoRequestHandler {

    /**
     * Fetches the bus stops for the specified line.
     */
    @Throws(IOException::class, DataProviderException::class)
    fun getStops(line: Line): List<Stop>

    /**
     * Fetches the next bus schedules for the specified bus stop.
     */
    @Throws(DataProviderException::class, IOException::class, XmlPullParserException::class)
    fun getSingleSchedule(stop: Stop): StopSchedule

    /**
     * Fetches the next bus schedules for the specified list of bus stops.
     */
    @Throws(DataProviderException::class, IOException::class)
    fun getMultipleSchedules(stops: List<Stop>): List<StopSchedule>

    @Throws(IOException::class, DataProviderException::class)
    fun getLines(networkCode: Int = TimeoRequestHandler.DEFAULT_NETWORK_CODE): List<Line>

    @Throws(IOException::class, DataProviderException::class)
    fun getStops(networkCode: Int, line: Line): List<Stop>

    /**
     * Retrieve a list of stops by their code.
     * Useful to get a stop's info when they're only known by their code.
     */
    @Throws(IOException::class, DataProviderException::class)
    fun getStopsByCode(networkCode: Int = TimeoRequestHandler.DEFAULT_NETWORK_CODE, codes: List<Int>): List<Stop>

    /**
     * Fetches the next bus schedules for the specified bus stop.
     */
    @Throws(DataProviderException::class, IOException::class, XmlPullParserException::class)
    fun getSingleSchedule(networkCode: Int, stop: Stop): StopSchedule

    /**
     * Fetches the next bus schedules for the specified list of bus stops.
     */
    @Throws(DataProviderException::class, IOException::class)
    fun getMultipleSchedules(networkCode: Int, stops: List<Stop>): List<StopSchedule>

    /**
     * Checks if there are outdated stops amongst those in the database,
     * by comparing them to a list of schedules returned by the API.
     */
    @Throws(DataProviderException::class)
    fun checkForOutdatedStops(stops: List<Stop>, schedules: List<StopSchedule>): Int

}