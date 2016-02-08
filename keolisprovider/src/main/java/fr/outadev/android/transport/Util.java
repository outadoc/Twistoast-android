/*
 * Twistoast - Util
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

package fr.outadev.android.transport;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author outadoc
 */
public abstract class Util {

    /**
     * Converts a time string to a Calendar object.
     * This method will return a calendar of the next day this time will occur; for example, if the string is "13:37" but it's
     * currently 15:45, the method will assume this time is tomorrow's, and set the date of the calendar object in consequence.
     *
     * @param time a time in a string, separated with a colon: e.g. "14:53"
     * @return a calendar object, with the time in the string set for the next valid day
     */
    public static DateTime getNextDateForTime(String time) {
        String[] splitTime = time.split(":");

        int hours = Integer.valueOf(splitTime[0]);
        int minutes = Integer.valueOf(splitTime[1]);

        LocalTime scheduledTime = new LocalTime(hours, minutes);
        LocalDate currDate = LocalDate.now();

        DateTime now = new DateTime();

        if (now.getHourOfDay() > hours || (now.getHourOfDay() == hours && now.getMinuteOfHour() > minutes)) {
            currDate.plusDays(1);
        }

        return currDate.toDateTime(scheduledTime);
    }

    /**
     * Capitalizes the first letter of every word, like WordUtils.capitalize(); except it does it WELL.
     * The determinants will not be capitalized, whereas some acronyms will.
     *
     * @param str The text to capitalize.
     * @return The capitalized text.
     */
    private static String smartCapitalize(String str) {
        String newStr = "";
        str = str.toLowerCase().trim();

        //these words will never be capitalized
        String[] determinants = new String[]{"de", "du", "des", "au", "aux", "à", "la", "le", "les", "d", "et", "l"};
        //these words will always be capitalized
        String[] specialWords = new String[]{"sncf", "chu", "chr", "chs", "crous", "suaps", "fpa", "za", "zi", "zac", "cpam",
                "efs", "mjc"};

        //explode the string with both spaces and apostrophes
        String[] words = str.split("( |\\-|'|\\/)");

        for (String word : words) {
            if (Arrays.asList(determinants).contains(word)) {
                //if the word should not be capitalized, just append it to the new string
                newStr += word;
            } else if (Arrays.asList(specialWords).contains(word)) {
                //if the word should be in upper case, do eet
                newStr += word.toUpperCase(Locale.FRENCH);
            } else {
                //if it's a normal word, just capitalize it
                newStr += StringUtils.capitalize(word);
            }

            try {
                //we don't know if the next character is a blank space or an apostrophe, so we check that
                char delimiter = str.charAt(newStr.length());
                newStr += delimiter;
            } catch (StringIndexOutOfBoundsException ignored) {
                //will be thrown for the last word of the string
            }
        }

        return StringUtils.capitalize(newStr);
    }

}
