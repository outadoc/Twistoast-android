/*
 * Twistoast - ScheduleTime
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

package fr.outadev.android.timeo;

import android.content.Context;
import android.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import fr.outadev.twistoast.R;

/**
 * Time formatter class. Used to turn strings returned by the API (e.g. "13:42") into readable Calendar objects and/or
 * processed strings to display information to the user.
 *
 * @author outadoc
 */
public abstract class ScheduleTime {

    private final static int IMMINENT_THRESHOLD_MINUTES = 1;
    private final static int COUNTDOWN_THRESHOLD_MINUTES = 45;

    /**
     * Formats a date into a more user-friendly fashion.
     * It converts the time parameter to a string that is either this time, a countdown to this time,
     * or messages that warn the user the time has almost come (no, that's not a threat).
     *
     * @param context a context (used to fetch strings and prefs)
     * @param time    a time in a string: e.g. "14:53"
     * @return if time is less than one minute in the future: "imminent arrival"-ish, if less than 45 minutes in the future: "in
     * xx minutes", if more than that: the untouched time parameter
     */
    public static String formatTime(Context context, DateTime time) {
        switch (getTimeDisplayMode(time, context)) {
            case CURRENTLY_AT_STOP:
                return context.getString(R.string.schedule_time_currently_at_stop);
            case ARRIVAL_IMMINENT:
                return context.getString(R.string.schedule_time_arrival_imminent);
            case COUNTDOWN:
                if (isRelativeTimeUsed(context)) {
                    return context.getString(R.string.schedule_time_countdown, getDurationUntilBus(time).getStandardMinutes());
                }
            default:
            case FULL:
                return time.toString(DateTimeFormat.forPattern("HH:mm"));
        }
    }

    private static boolean isRelativeTimeUsed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_relative_time", true);
    }

    /**
     * Computes the interval of time after which the bus shall arrive.
     *
     * @param schedule the time at which the bus will arrive
     * @return the difference between now and then
     */
    public static Duration getDurationUntilBus(DateTime schedule) {
        return new Duration(null, schedule);
    }

    /**
     * Decides which mode the app should use to show the time to the user.
     * <p/>
     * If time is less than one minute in the future: ARRIVAL_IMMINENT; if less than 45 minutes in the future: COUNTDOWN; if
     * more than that: FULL; if it was in the past, CURRENTLY_AT_STOP
     *
     * @param schedule the time at which the bus will arrive
     * @return a TimeDisplayMode constant to tell you the right mode
     */
    public static TimeDisplayMode getTimeDisplayMode(DateTime schedule, Context context) {
        long offset = getDurationUntilBus(schedule).getMillis() / 1000 / 60;

        if (!isRelativeTimeUsed(context)) {
            return TimeDisplayMode.FULL;
        }

        if (offset <= 0) {
            return TimeDisplayMode.CURRENTLY_AT_STOP;
        } else if (offset <= IMMINENT_THRESHOLD_MINUTES) {
            return TimeDisplayMode.ARRIVAL_IMMINENT;
        } else if (offset <= COUNTDOWN_THRESHOLD_MINUTES) {
            return TimeDisplayMode.COUNTDOWN;
        } else {
            return TimeDisplayMode.FULL;
        }
    }

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

    public enum TimeDisplayMode {
        CURRENTLY_AT_STOP, ARRIVAL_IMMINENT, COUNTDOWN, FULL
    }

}
