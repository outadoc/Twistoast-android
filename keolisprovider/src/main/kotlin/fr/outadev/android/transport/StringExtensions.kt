/*
 * Twistoast - StringExtensions.kt
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

import org.apache.commons.lang3.StringUtils.capitalize
import java.util.*

/**
 * Capitalizes the first letter of every word, like WordUtils.capitalize(); except it does it WELL.
 * The determinants will not be capitalized, whereas some acronyms will.
 *
 * @return The capitalized text.
 */
fun String.smartCapitalize(): String {
    val capitalizedOut = StringBuilder()

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
        when {
            alwaysLower.contains(word) -> //if the word should not be capitalized, just append it to the new string
                capitalizedOut.append(word)
            alwaysUpper.contains(word) -> //if the word should be in upper case, do eet
                capitalizedOut.append(word.toUpperCase(Locale.FRENCH))
            else -> //if it's a normal word, just capitalize it
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
