/*
 * Twistoast - Database
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

package fr.outadev.twistoast

import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteOpenHelper
import fr.outadev.android.transport.timeo.TimeoIDNameObject
import fr.outadev.android.transport.timeo.TimeoLine
import fr.outadev.android.transport.timeo.TimeoStop
import org.joda.time.DateTime
import java.util.*

/**
 * Database management class.

 * @author outadoc
 */
class Database(private val databaseOpenHelper: SQLiteOpenHelper) {

    enum class SortBy {
        LINE, STOP
    }

    /**
     * Adds a bus stop to the database.

     * @param stop the bus stop to add
     *
     * @throws IllegalArgumentException  if the stop is not valid
     * @throws SQLiteConstraintException if a constraint failed
     */
    @Throws(IllegalArgumentException::class, SQLiteConstraintException::class)
    fun addStopToDatabase(stop: TimeoStop?) {
        if (stop != null) {
            // when we want to add a stop, we add the line first, then the
            // direction
            addLineToDatabase(stop.line)

            // then, open the database, and start enumerating when we'll need to
            // add
            val db = databaseOpenHelper.writableDatabase
            val values = ContentValues()

            values.put("stop_id", stop.id)
            values.put("line_id", stop.line.id)
            values.put("dir_id", stop.line.direction.id)
            values.put("stop_name", stop.name)
            values.put("stop_ref", stop.reference)
            values.put("network_code", stop.line.networkCode)

            try {
                // insert the stop with the specified columns
                db.insertOrThrow("twi_stop", null, values)
            } finally {
                // we want to close the database afterwards either way
                db.close()
            }
        } else {
            throw IllegalArgumentException()
        }
    }

    /**
     * Adds a bus line to the database.

     * @param line the bus line to add
     */
    private fun addLineToDatabase(line: TimeoLine?) {
        if (line != null && line.details != null) {
            val db = databaseOpenHelper.writableDatabase
            val values = ContentValues()

            values.put("line_id", line.id)
            values.put("line_name", line.name)
            values.put("line_color", line.color)
            values.put("network_code", line.networkCode)

            db.insert("twi_line", null, values)
            db.close()

            addDirectionToDatabase(line)
        }
    }

    /**
     * Adds a direction to the database.

     * @param line the bus line whose direction to add
     */
    private fun addDirectionToDatabase(line: TimeoLine?) {
        if (line != null && line.direction != null) {
            val db = databaseOpenHelper.writableDatabase
            val values = ContentValues()

            values.put("dir_id", line.direction.id)
            values.put("line_id", line.id)
            values.put("dir_name", line.direction.name)
            values.put("network_code", line.networkCode)

            db.insert("twi_direction", null, values)
            db.close()
        }
    }

    /**
     * Gets all stops currently stored in the database.

     * @return a list of all the stops
     */
    fun getAllStops(sortCriteria: SortBy): List<TimeoStop> {
        // Clean notification flags that have timed out so they don't interfere
        cleanOutdatedWatchedStops()

        val db = databaseOpenHelper.readableDatabase

        val sortBy: String

        when (sortCriteria) {
            Database.SortBy.STOP -> sortBy = "stop.stop_name, CAST(line.line_id AS INTEGER)"
            else -> sortBy = "CAST(line.line_id AS INTEGER), stop.stop_name"
        }

        val results = db.rawQuery(
                "SELECT stop.stop_id, stop.stop_name, stop.stop_ref, line.line_id, line.line_name, line.line_color, " +
                        "dir.dir_id, dir.dir_name, line.network_code, ifnull(notif_active, 0) as notif " +
                        "FROM twi_stop stop " +
                        "INNER JOIN twi_direction dir USING(dir_id, line_id, network_code) " +
                        "INNER JOIN twi_line line USING(line_id, network_code) " +
                        "LEFT JOIN twi_notification notif ON (notif.stop_id = stop.stop_id " +
                        "AND notif.line_id = line.line_id AND notif.dir_id = dir.dir_id " +
                        "AND notif.network_code = line.network_code AND notif.notif_active = 1) " +
                        "ORDER BY line.network_code, " + sortBy + ", dir.dir_name",
                null)

        val stopsList = ArrayList<TimeoStop>()

        // while there's a stop available
        while (results.moveToNext()) {
            val line = TimeoLine(
                    TimeoIDNameObject(
                            results.getString(results.getColumnIndex("line_id")),
                            results.getString(results.getColumnIndex("line_name"))),
                    TimeoIDNameObject(
                            results.getString(results.getColumnIndex("dir_id")),
                            results.getString(results.getColumnIndex("dir_name"))),
                    results.getString(results.getColumnIndex("line_color")),
                    results.getInt(results.getColumnIndex("network_code")))

            val stop = TimeoStop(
                    results.getString(results.getColumnIndex("stop_id")),
                    results.getString(results.getColumnIndex("stop_name")),
                    results.getString(results.getColumnIndex("stop_ref")),
                    line,
                    results.getInt(results.getColumnIndex("notif")) == 1)

            // add it to the list
            stopsList.add(stop)
        }

        // close the cursor and the database
        results.close()
        db.close()

        return stopsList
    }

    /**
     * Gets a bus stop at a specific index. Useful for Pebble, for example.

     * @param index the index of the stop in the database, sorted by line id, stop name, and direction name
     * *
     * @return the corresponding stop object
     */
    fun getStopAtIndex(index: Int): TimeoStop? {
        val stopsList = getAllStops(SortBy.STOP)

        if (stopsList.size >= index + 1) {
            return stopsList[index]
        }

        return null
    }

    /**
     * Gets a bus stop with the corresponding primary key.

     * @param stopId the ID of the stop to get
     * *
     * @param lineId the ID of the line of the stop to get
     * *
     * @param dirId the ID of the direction of the stop to get
     * *
     * @return the corresponding stop object
     */
    fun getStop(stopId: String, lineId: String, dirId: String, networkCode: Int): TimeoStop? {
        val db = databaseOpenHelper.readableDatabase
        var stop: TimeoStop? = null

        val results = db.rawQuery(
                "SELECT * FROM twi_stop " +
                        "JOIN twi_line USING (line_id, network_code) " +
                        "JOIN twi_direction USING (line_id, dir_id, network_code) " +
                        "WHERE stop_id = ? AND line_id = ? AND dir_id = ? AND network_code = ?",
                arrayOf(stopId, lineId, dirId, networkCode.toString()))

        if (results.moveToFirst()) {
            val line = TimeoLine(
                    TimeoIDNameObject(
                            results.getString(results.getColumnIndex("line_id")),
                            results.getString(results.getColumnIndex("line_name"))),
                    TimeoIDNameObject(
                            results.getString(results.getColumnIndex("dir_id")),
                            results.getString(results.getColumnIndex("dir_name"))),
                    results.getString(results.getColumnIndex("line_color")),
                    results.getInt(results.getColumnIndex("network_code")))

            stop = TimeoStop(
                    results.getString(results.getColumnIndex("stop_id")),
                    results.getString(results.getColumnIndex("stop_name")),
                    results.getString(results.getColumnIndex("stop_ref")),
                    line,
                    results.getInt(results.getColumnIndex("notif")) == 1)
        }

        // close the cursor and the database
        results.close()
        db.close()

        return stop
    }

    /**
     * Gets the number of stops in the database.

     * @return the number of bus stops
     */
    // that's a nice query you got tthhhere
    // close the cursor and the database
    val stopsCount: Int
        get() {
            val db = databaseOpenHelper.readableDatabase
            val results = db.rawQuery("SELECT stop_id FROM twi_stop", null)

            val count = results.count
            results.close()
            db.close()

            return count
        }

    val networksCount: Int
        get() {
            val db = databaseOpenHelper.readableDatabase

            val results = db.rawQuery("SELECT COUNT(*), network_code FROM twi_stop GROUP BY (network_code)", null)
            results.moveToFirst()

            val count = results.count

            results.close()
            db.close()

            return count
        }

    /**
     * Deletes a bus stop from the database.

     * @param stop the bus stop to delete
     */
    fun deleteStop(stop: TimeoStop) {
        val db = databaseOpenHelper.writableDatabase

        db.delete("twi_stop", "stop_id = ? AND line_id = ? AND dir_id = ? AND stop_ref = ? AND network_code = ?",
                arrayOf(stop.id, stop.line.id, stop.line.direction.id, stop.reference, stop.line.networkCode.toString()))

        db.close()
    }

    /**
     * Update the reference of a stop in the database.

     * @param stop the bus stop whose reference is to be updated
     */
    fun updateStopReference(stop: TimeoStop) {
        val db = databaseOpenHelper.writableDatabase

        val updateClause = ContentValues()
        updateClause.put("stop_ref", stop.reference)

        db.update("twi_stop", updateClause, "stop_id = ? AND line_id = ? AND dir_id = ? AND network_code = ?",
                arrayOf(stop.id, stop.line.id, stop.line.direction.id, stop.line.networkCode.toString()))

        db.close()
    }

    /**
     * Removes outdated watched stops from the database.
     * If a stop notification request was added more than three hours ago, it will be deleted.
     */
    private fun cleanOutdatedWatchedStops() {
        val db = databaseOpenHelper.writableDatabase

        val updateClause = ContentValues()
        updateClause.put("notif_active", 0)

        db.update("twi_notification", updateClause, "date('now','-3 hours') > notif_creation_time", null)
        db.close()
    }

    /**
     * Fetches the list of stops that we are currently watching (that is to say, we wanted to be notified when they're incoming).

     * @return a list containing the stops to process
     */
    // Clean notification flags that have timed out so they don't interfere
    // while there's a stop available
    // add it to the list
    // close the cursor and the database
    val watchedStops: List<TimeoStop>
        get() {
            cleanOutdatedWatchedStops()

            val db = databaseOpenHelper.readableDatabase

            val results = db.rawQuery(
                    "SELECT stop.stop_id, stop.stop_name, stop.stop_ref, line.line_id, line.line_name, " +
                            "line.line_color, dir.dir_id, dir.dir_name, line.network_code, notif.notif_last_estim " +
                            "FROM twi_stop stop " +
                            "INNER JOIN twi_direction dir USING(dir_id, line_id, network_code) " +
                            "INNER JOIN twi_line line USING(line_id, network_code) " +
                            "INNER JOIN twi_notification notif USING (stop_id, line_id, dir_id, network_code) " +
                            "WHERE notif_active = 1 " +
                            "ORDER BY line.network_code, CAST(line.line_id AS INTEGER), stop.stop_name, dir.dir_name",
                    null)

            val stopsList = ArrayList<TimeoStop>()
            while (results.moveToNext()) {
                val line = TimeoLine(
                        TimeoIDNameObject(
                                results.getString(results.getColumnIndex("line_id")),
                                results.getString(results.getColumnIndex("line_name"))),
                        TimeoIDNameObject(
                                results.getString(results.getColumnIndex("dir_id")),
                                results.getString(results.getColumnIndex("dir_name"))),
                        results.getString(results.getColumnIndex("line_color")),
                        results.getInt(results.getColumnIndex("network_code")))

                val stop = TimeoStop(
                        results.getString(results.getColumnIndex("stop_id")),
                        results.getString(results.getColumnIndex("stop_name")),
                        results.getString(results.getColumnIndex("stop_ref")),
                        line,
                        true,
                        DateTime(results.getLong(results.getColumnIndex("notif_last_estim"))))
                stopsList.add(stop)
            }
            results.close()
            db.close()

            return stopsList
        }

    /**
     * Registers a stop to be watched for notifications.

     * @param stop the bus stop to add to the list
     */
    fun addToWatchedStops(stop: TimeoStop) {
        val db = databaseOpenHelper.writableDatabase
        val values = ContentValues()

        values.put("stop_id", stop.id)
        values.put("line_id", stop.line.id)
        values.put("dir_id", stop.line.direction.id)
        values.put("network_code", stop.line.networkCode)

        db.insert("twi_notification", null, values)
    }

    /**
     * Unregisters a stop from the list of watched stops.
     * No notifications should be sent for this stop anymore, until it's been added back in.

     * @param stop the bus stop that we should stop watching
     */
    fun stopWatchingStop(stop: TimeoStop) {
        val db = databaseOpenHelper.writableDatabase

        val updateClause = ContentValues()
        updateClause.put("notif_active", 0)

        db.update("twi_notification", updateClause,
                "stop_id = ? AND line_id = ? AND dir_id = ? AND network_code = ?",
                arrayOf(stop.id, stop.line.id, stop.line.direction.id, stop.line.networkCode.toString()))

        db.close()
    }

    /**
     * Updated the last time of arrival returned by the API for this bus.

     * @param stop    the bus stop we want to update
     * *
     * @param lastETA a UNIX timestamp for the last know ETA for this bus
     */
    fun updateWatchedStopETA(stop: TimeoStop, lastETA: DateTime) {
        val db = databaseOpenHelper.writableDatabase

        val updateClause = ContentValues()
        updateClause.put("notif_last_estim", lastETA.millis)

        db.update("twi_notification", updateClause,
                "stop_id = ? AND line_id = ? AND dir_id = ? AND network_code = ? AND notif_active = 1",
                arrayOf(stop.id, stop.line.id, stop.line.direction.id, stop.line.networkCode.toString()))

        db.close()
    }

    /**
     * Counts the number of bus stops we are currently watching.

     * @return the number of watched stops in the database
     */
    val watchedStopsCount: Int
        get() {
            val db = databaseOpenHelper.readableDatabase

            val results = db.rawQuery("SELECT COUNT(*) as nb_watched FROM twi_notification WHERE notif_active = 1", null)
            results.moveToFirst()

            val count = results.getInt(results.getColumnIndex("nb_watched"))

            results.close()
            db.close()

            return count
        }

}
