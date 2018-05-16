/*
 * Twistoast - TimeExtensions.kt
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

import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.Minutes

/**
 * Converts a time string to a Calendar object.
 * This method will return a calendar of the next day this time will occur; for example, if the string is "13:37" but it's
 * currently 15:45, the method will assume this time is tomorrow's, and set the date of the calendar object in consequence.
 *
 * @return a calendar object, with the time in the string set for the next valid day
 */
fun String.getNextDateForTime(currentDate: DateTime = DateTime.now()): DateTime {
    val splitTime = this.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()

    val hours = splitTime[0].toInt()
    val minutes = splitTime[1].toInt()

    val scheduledTime = LocalTime(hours, minutes)
    var localDate = currentDate.toLocalDate()

    // If the time is less than ten minutes in the past, we'll assume that it's still supposed to be
    // today. Otherwise, we suppose it's a time tomorrow.
    if (scheduledTime.isBefore(currentDate.toLocalTime())) {
        if (Minutes.minutesBetween(scheduledTime.toDateTimeToday(), currentDate) > Minutes.minutes(10)) {
            localDate = localDate.plusDays(1)
        }
    }

    return localDate.toDateTime(scheduledTime)
}
