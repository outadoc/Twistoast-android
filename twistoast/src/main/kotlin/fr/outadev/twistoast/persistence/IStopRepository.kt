/*
 * Twistoast - IStopRepository.kt
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

package fr.outadev.twistoast.persistence

import android.database.sqlite.SQLiteConstraintException
import fr.outadev.android.transport.timeo.TimeoStop
import org.joda.time.DateTime

interface IStopRepository {
    enum class SortBy {
        LINE, STOP
    }

    /**
     * Gets the number of stops in the database.
     *
     * @return the number of bus stops
     */
    val stopsCount: Int
    val networksCount: Int

    /**
     * Fetches the list of stops that we are currently watching (that is to say, we wanted to be notified when they're incoming).
     * @return a list containing the stops to process
     */

    val watchedStops: List<TimeoStop>

    /**
     * Counts the number of bus stops we are currently watching.
     * @return the number of watched stops in the database
     */
    val watchedStopsCount: Int

    /**
     * Adds a bus stop to the database.

     * @param stop the bus stop to add
     *
     * @throws IllegalArgumentException  if the stop is not valid
     * @throws SQLiteConstraintException if a constraint failed
     */
    @Throws(IllegalArgumentException::class, SQLiteConstraintException::class)
    fun addStopToDatabase(stop: TimeoStop?)

    /**
     * Gets all stops currently stored in the database.

     * @return a list of all the stops
     */
    fun getAllStops(sortCriteria: SortBy): List<TimeoStop>

    /**
     * Gets a bus stop at a specific index. Useful for Pebble, for example.

     * @param index the index of the stop in the database, sorted by line id, stop name, and direction name
     * @return the corresponding stop object
     */
    fun getStopAtIndex(index: Int): TimeoStop?

    /**
     * Gets a bus stop with the corresponding primary key.

     * @param stopId the ID of the stop to get
     * @param lineId the ID of the line of the stop to get
     * @param dirId the ID of the direction of the stop to get
     *
     * @return the corresponding stop object
     */
    fun getStop(stopId: String, lineId: String, dirId: String, networkCode: Int): TimeoStop?

    /**
     * Deletes a bus stop from the database.
     * @param stop the bus stop to delete
     */
    fun deleteStop(stop: TimeoStop)

    /**
     * Update the reference of a stop in the database.
     * @param stop the bus stop whose reference is to be updated
     * @return number of stops that were updated
     */
    fun updateStopReference(stop: TimeoStop): Int

    /**
     * Registers a stop to be watched for notifications.
     * @param stop the bus stop to add to the list
     */
    fun addToWatchedStops(stop: TimeoStop)

    /**
     * Unregisters a stop from the list of watched stops.
     * No notifications should be sent for this stop anymore, until it's been added back in.
     * @param stop the bus stop that we should stop watching
     */
    fun stopWatchingStop(stop: TimeoStop)

    /**
     * Updated the last time of arrival returned by the API for this bus.
     *
     * @param stop    the bus stop we want to update
     * @param lastETA a UNIX timestamp for the last know ETA for this bus
     */
    fun updateWatchedStopETA(stop: TimeoStop, lastETA: DateTime)
}