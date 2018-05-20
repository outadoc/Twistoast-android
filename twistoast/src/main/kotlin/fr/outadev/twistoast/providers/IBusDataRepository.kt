/*
 * Twistoast - IBusDataRepository.kt
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

package fr.outadev.twistoast.providers

import fr.outadev.android.transport.KeolisDao
import fr.outadev.twistoast.model.Line
import fr.outadev.twistoast.model.Result
import fr.outadev.twistoast.model.Stop
import fr.outadev.twistoast.model.StopSchedule

/**
 * Request data from the transportation network's API.
 */
interface IBusDataRepository {

    /**
     * Fetches the bus stops for the specified line.
     */
    fun getStops(line: Line): Result<List<Stop>>

    /**
     * Fetches the next bus schedules for the specified bus stop.
     */
    fun getSingleSchedule(stop: Stop): Result<StopSchedule>

    /**
     * Fetches the next bus schedules for the specified list of bus stops.
     */
    fun getMultipleSchedules(stops: List<Stop>): Result<List<StopSchedule>>

    fun getLines(networkCode: Int = KeolisDao.DEFAULT_NETWORK_CODE): Result<List<Line>>

    fun getStops(networkCode: Int, line: Line): Result<List<Stop>>

    /**
     * Retrieve a list of stops by their code.
     * Useful to get a stop's info when they're only known by their code.
     */
    fun getStopsByCode(networkCode: Int = KeolisDao.DEFAULT_NETWORK_CODE, codes: List<Int>): Result<List<Stop>>

    /**
     * Fetches the next bus schedules for the specified bus stop.
     */
    fun getSingleSchedule(networkCode: Int, stop: Stop): Result<StopSchedule>

    /**
     * Fetches the next bus schedules for the specified list of bus stops.
     */
    fun getMultipleSchedules(networkCode: Int, stops: List<Stop>): Result<List<StopSchedule>>

    /**
     * Checks if there are outdated stops amongst those in the database,
     * by comparing them to a list of schedules returned by the API.
     */
    fun checkForOutdatedStops(stops: List<Stop>, schedules: List<StopSchedule>): Result<Int>

}