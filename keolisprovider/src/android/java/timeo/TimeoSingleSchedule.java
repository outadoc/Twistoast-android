/*
 * Twistoast - TimeoSingleSchedule
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

import org.joda.time.DateTime;

/**
 * Stores a single schedule, containing a time and a direction.
 *
 * @author outadoc
 */
public class TimeoSingleSchedule {

    private DateTime mScheduleTime;
    private String mDirection;

    /**
     * Create a new schedule.
     *
     * @param scheduleTime the time at which the bus should arrive (e.g. "13:53")
     * @param direction    the direction towards which the bus is heading
     */
    public TimeoSingleSchedule(String scheduleTime, String direction) {
        this.mScheduleTime = ScheduleTime.getNextDateForTime(scheduleTime);
        this.mDirection = direction;
    }

    /**
     * Create a new empty schedule.
     */
    public TimeoSingleSchedule() {
    }

    public DateTime getScheduleTime() {
        return mScheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.mScheduleTime = ScheduleTime.getNextDateForTime(scheduleTime);
    }

    public String getDirection() {
        return mDirection;
    }

    public void setDirection(String direction) {
        this.mDirection = direction;
    }

    public String getFormattedTime(Context context) {
        return ScheduleTime.formatTime(context, mScheduleTime);
    }

}
