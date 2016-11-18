/*
 * Twistoast - PebbleWatchReceiver.kt
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

package fr.outadev.twistoast.background

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.util.Log
import com.getpebble.android.kit.PebbleKit
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver
import com.getpebble.android.kit.util.PebbleDictionary
import fr.outadev.android.transport.timeo.TimeoRequestHandler
import fr.outadev.android.transport.timeo.TimeoSingleSchedule
import fr.outadev.android.transport.timeo.TimeoStopSchedule
import fr.outadev.twistoast.ConfigurationManager
import fr.outadev.twistoast.Database
import fr.outadev.twistoast.DatabaseOpenHelper
import fr.outadev.twistoast.TimeFormatter
import org.jetbrains.anko.doAsync
import java.util.*

/**
 * Receives and handles the Twistoast Pebble app requests in the background.
 *
 * @author outadoc
 */
class PebbleWatchReceiver : PebbleDataReceiver(PebbleWatchReceiver.PEBBLE_UUID) {

    private val requestHandler: TimeoRequestHandler
    private lateinit var database: Database

    init {
        requestHandler = TimeoRequestHandler()
    }

    override fun receiveData(context: Context, transactionId: Int, data: PebbleDictionary) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        Log.d(TAG, "received a message from pebble " + PEBBLE_UUID)

        // open the database and count the stops
        database = Database(DatabaseOpenHelper(context))

        val stopsCount = database.stopsCount
        val messageType : Byte = data.getInteger(KEY_TWISTOAST_MESSAGE_TYPE).toByte()

        // if we want a schedule and we have buses in the database
        if (messageType === BUS_STOP_REQUEST
                && stopsCount > 0 && cm.activeNetworkInfo != null
                && cm.activeNetworkInfo.isConnected) {
            Log.d(TAG, "pebble request acknowledged")
            PebbleKit.sendAckToPebble(context, transactionId)

            // get the bus index (modulo the number of stops there is in the db)
            val busIndex = (data.getInteger(KEY_STOP_INDEX)!!.toShort() % stopsCount).toShort()

            // get the stop that interests us
            val stop = database.getStopAtIndex(busIndex.toInt())

            Log.d(TAG, "loading data for stop #$busIndex...")

            // fetch schedule
            doAsync {
                try {
                    val schedule = requestHandler.getSingleSchedule(stop!!)
                    Log.d(TAG, "got data for stop: " + schedule)
                    craftAndSendSchedulePacket(context, schedule)
                } catch (e: Exception) {
                    e.printStackTrace()
                    PebbleKit.sendNackToPebble(context, transactionId)
                }
            }

        } else {
            PebbleKit.sendNackToPebble(context, transactionId)
        }

    }

    /**
     * Sens a response packet to the Pebble.
     *
     * @param context  a context
     * @param schedule the schedule to send back
     */
    private fun craftAndSendSchedulePacket(context: Context, schedule: TimeoStopSchedule) {
        val config = ConfigurationManager(context)
        val res = PebbleDictionary()

        res.addInt8(KEY_TWISTOAST_MESSAGE_TYPE, BUS_STOP_DATA_RESPONSE)
        res.addString(KEY_BUS_LINE_NAME, processStringForPebble(schedule.stop.line.name, 10))
        res.addString(KEY_BUS_DIRECTION_NAME, processStringForPebble(schedule.stop.line.direction.name, 15))
        res.addString(KEY_BUS_STOP_NAME, processStringForPebble(schedule.stop.name, 15))

        // Add first schedule time and direction to the buffer
        if (!schedule.schedules.isEmpty()) {
            // Time at which the next bus is planned
            val nextSchedule = schedule.schedules[0].scheduleTime

            // Convert to milliseconds and add to the buffer
            res.addInt32(KEY_BUS_NEXT_SCHEDULE, TimeFormatter.getDurationUntilBus(nextSchedule).millis.toInt())
            // Get a short version of the destination if required - e.g. A or B for the tram
            res.addString(KEY_BUS_NEXT_SCHEDULE_DIR, getOptionalShortDirection(schedule.schedules[0]))
        } else {
            res.addInt32(KEY_BUS_NEXT_SCHEDULE, -1)
            res.addString(KEY_BUS_NEXT_SCHEDULE_DIR, " ")
        }

        // Add the second schedule, same process
        if (schedule.schedules.size > 1) {
            val nextSchedule = schedule.schedules[1].scheduleTime

            res.addInt32(KEY_BUS_SECOND_SCHEDULE, TimeFormatter.getDurationUntilBus(nextSchedule).millis.toInt())
            res.addString(KEY_BUS_SECOND_SCHEDULE_DIR, getOptionalShortDirection(schedule.schedules[1]))
        } else {
            res.addInt32(KEY_BUS_SECOND_SCHEDULE, -1)
            res.addString(KEY_BUS_SECOND_SCHEDULE_DIR, " ")
        }

        if (!schedule.schedules.isEmpty()) {
            val scheduleTime = schedule.schedules[0].scheduleTime
            val displayMode = TimeFormatter.getTimeDisplayMode(scheduleTime, context)

            if (displayMode === TimeFormatter.TimeDisplayMode.ARRIVAL_IMMINENT
                    || displayMode === TimeFormatter.TimeDisplayMode.CURRENTLY_AT_STOP) {
                res.addInt8(KEY_SHOULD_VIBRATE, 1.toByte())
            }
        }

        var color = 0x0

        if (config.useColorInPebbleApp) {
            color = Color.parseColor(schedule.stop.line.color)
        }

        res.addInt32(KEY_BACKGROUND_COLOR, color)

        Log.d(TAG, "sending back: " + res)
        PebbleKit.sendDataToPebble(context, PEBBLE_UUID, res)
    }

    private fun getOptionalShortDirection(schedule: TimeoSingleSchedule): String {
        if (schedule.direction != null && (schedule.direction as String).matches("(A|B) .+".toRegex())) {
            return (schedule.direction as String)[0].toString()
        } else {
            return " "
        }
    }

    /**
     * Processes a string for the Pebble's screen.
     *
     * @param str       the string to process
     * @param maxLength the max length of the string
     *
     * @return the processed string, or the original string if no action was required
     */
    private fun processStringForPebble(str: String?, maxLength: Int): String {
        if (str == null) {
            return ""
        }

        if (str.length <= maxLength) {
            //if the string is shorter than the max length, just return the string untouched
            return str
        }

        try {
            //truncate the string to [maxLength] characters, and add an ellipsis character at the end
            return str.substring(0, maxLength).trim { it <= ' ' } + "…"
        } catch (e: IndexOutOfBoundsException) {
            return str
        }

    }

    companion object {
        val TAG: String = PebbleWatchReceiver::class.java.simpleName
        private val PEBBLE_UUID = UUID.fromString("020f9398-c407-454b-996c-6ac341337281")

        // message type key
        const val KEY_TWISTOAST_MESSAGE_TYPE = 0x00

        // message type value
        const val BUS_STOP_REQUEST: Byte = 0x10
        const val BUS_STOP_DATA_RESPONSE: Byte = 0x11

        // message keys
        const val KEY_STOP_INDEX = 0x20
        const val KEY_BUS_STOP_NAME = 0x21
        const val KEY_BUS_DIRECTION_NAME = 0x22
        const val KEY_BUS_LINE_NAME = 0x23
        const val KEY_BUS_NEXT_SCHEDULE = 0x24
        const val KEY_BUS_NEXT_SCHEDULE_DIR = 0x25
        const val KEY_BUS_SECOND_SCHEDULE = 0x26
        const val KEY_BUS_SECOND_SCHEDULE_DIR = 0x27

        const val KEY_SHOULD_VIBRATE = 0x30
        const val KEY_BACKGROUND_COLOR = 0x31
    }

}
