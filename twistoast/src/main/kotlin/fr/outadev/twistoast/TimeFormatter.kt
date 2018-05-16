/*
 * Twistoast - TimeFormatter.kt
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

package fr.outadev.twistoast


import android.content.Context
import android.preference.PreferenceManager
import fr.outadev.twistoast.TimeFormatter.TimeDisplayMode.*

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat

/**
 * Time formatter class. Used to turn strings returned by the API (e.g. "13:42") into readable Calendar objects and/or
 * processed strings to display information to the user.
 *
 * @author outadoc
 */
object TimeFormatter {

    private const val IMMINENT_THRESHOLD_MINUTES = 1
    private const val COUNTDOWN_THRESHOLD_MINUTES = 45

    /**
     * Formats a date into a more user-friendly fashion.
     * It converts the time parameter to a string that is either this time, a countdown to this time,
     * or messages that warn the user the time has almost come (no, that's not a threat).

     * @param context a context (used to fetch strings and prefs)
     * @param time    a time in a string: e.g. "14:53"

     * @return if time is less than one minute in the future: "imminent arrival"-ish, if less than 45 minutes in the future: "in
     * xx minutes", if more than that: the untouched time parameter
     */
    fun formatTime(context: Context, time: DateTime): String {
        // Display absolute time in the user's time zone
        val zone = DateTimeZone.getDefault()
        val strTime = time.toString(DateTimeFormat.shortTime().withZone(zone))

        return when (getTimeDisplayMode(time, context)) {
            CURRENTLY_AT_STOP -> context.getString(R.string.schedule_time_currently_at_stop)
            ARRIVAL_IMMINENT -> context.getString(R.string.schedule_time_arrival_imminent)
            COUNTDOWN -> {
                when {
                    isRelative(context) -> context.getString(R.string.schedule_time_countdown, getDurationUntilBus(time).standardMinutes)
                    else -> strTime
                }

            }
            FULL -> strTime
        }
    }

    private fun isRelative(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_relative_time", true)
    }

    /**
     * Computes the interval of time after which the )yàuèbus shall arrive.
     *
     * @param schedule the time at which the bus will arrive
     * @return the difference between now and then
     */
    fun getDurationUntilBus(schedule: DateTime): Duration {
        return Duration(null, schedule)
    }

    /**
     * Decides which mode the app should use to show the time to the user.
     *
     * If time is less than one minute in the future: ARRIVAL_IMMINENT; if less than 45 minutes in the future: COUNTDOWN; if
     * more than that: FULL; if it was in the past, CURRENTLY_AT_STOP
     *
     * @param schedule the time at which the bus will arrive
     * @return a TimeDisplayMode constant to tell you the right mode
     */
    fun getTimeDisplayMode(schedule: DateTime, context: Context): TimeDisplayMode {
        val offset = getDurationUntilBus(schedule).millis / 1000 / 60

        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_relative_time", true)) {
            return FULL
        }

        return when {
            offset <= 0 -> CURRENTLY_AT_STOP
            offset <= IMMINENT_THRESHOLD_MINUTES -> ARRIVAL_IMMINENT
            offset <= COUNTDOWN_THRESHOLD_MINUTES -> COUNTDOWN
            else -> FULL
        }
    }

    enum class TimeDisplayMode {
        CURRENTLY_AT_STOP, ARRIVAL_IMMINENT, COUNTDOWN, FULL
    }

}
