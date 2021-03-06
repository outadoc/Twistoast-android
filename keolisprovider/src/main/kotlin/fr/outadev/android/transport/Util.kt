/*
 * Twistoast - Util.kt
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

package fr.outadev.android.transport

import org.apache.commons.lang3.StringUtils.capitalize
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.Minutes
import java.util.*

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
    val now = currentDate
    var currDate = currentDate.toLocalDate()

    // If the time is less than ten minutes in the past, we'll assume that it's still supposed to be
    // today. Otherwise, we suppose it's a time tomorrow.
    if (scheduledTime.isBefore(now.toLocalTime())) {
        if (Minutes.minutesBetween(scheduledTime.toDateTimeToday(), now) > Minutes.minutes(10)) {
            currDate = currDate.plusDays(1)
        }
    }

    return currDate.toDateTime(scheduledTime)
}

/**
 * Capitalizes the first letter of every word, like WordUtils.capitalize(); except it does it WELL.
 * The determinants will not be capitalized, whereas some acronyms will.
 *
 * @return The capitalized text.
 */
fun String.smartCapitalize(): String {
    val capitalizedOut: StringBuilder = StringBuilder()

    //explode the string with both spaces and apostrophes
    val str = this.toLowerCase()
            .trim({ it <= ' ' })
            .replace(" {2,}".toRegex(), " ")

    val words = str.split("( |-|'|/)".toRegex())
            .dropLastWhile(String::isEmpty)
            .toTypedArray()

    // These words will never be capitalized
    val alwaysLower = listOf("de", "du", "des", "au", "aux", "à", "la", "le", "les", "d", "et", "l")

    // These words will always be capitalized
    val alwaysUpper = listOf("sncf", "chu", "chr", "chs", "crous", "suaps", "fpa", "za", "zi", "zac", "cpam", "efs", "mjc", "paj", "ab")

    words.forEach { word ->
        if (alwaysLower.contains(word)) {
            //if the word should not be capitalized, just append it to the new string
            capitalizedOut.append(word)
        } else if (alwaysUpper.contains(word)) {
            //if the word should be in upper case, do eet
            capitalizedOut.append(word.toUpperCase(Locale.FRENCH))
        } else {
            //if it's a normal word, just capitalize it
            capitalizedOut.append(capitalize(word))
        }

        if (capitalizedOut.length < str.length) {
            //we don't know if the next character is a blank space or an apostrophe, so we check that
            val delimiter = str[capitalizedOut.length]
            capitalizedOut.append(delimiter)
        }
    }

    return capitalize(capitalizedOut.toString())
}
