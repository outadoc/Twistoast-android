/*
 * Twistoast - TimeoSingleSchedule
 * Copyright (C) 2013-2015 Baptiste Candellier
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

/**
 * Stores a single schedule, containing a time and a direction.
 *
 * @author outadoc
 */
public class TimeoSingleSchedule {

	private String time;
	private String direction;

	private static Boolean relative;

	/**
	 * Create a new schedule.
	 *
	 * @param time      the time at which the bus should arrive (e.g. "13:53")
	 * @param direction the direction towards which the bus is heading
	 */
	public TimeoSingleSchedule(String time, String direction) {
		this.time = time;
		this.direction = direction;
	}

	/**
	 * Create a new empty schedule.
	 */
	public TimeoSingleSchedule() {
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getFormattedTime(Context context) {
		return (isRelative(context)) ? ScheduleTime.formatTime(context, getTime()) : getTime();
	}

	public String getShortFormattedTime(Context context) {
		String dir = (getDirection() != null && getDirection().matches("(A|B) .+")) ? getDirection().charAt(0) + " : " : "";
		return (isRelative(context)) ? dir + ScheduleTime.formatTime(context, getTime()) : dir + getTime();
	}

	private static boolean isRelative(Context context) {
		if(relative == null) {
			relative = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_relative_time", true);
		}

		return relative;
	}
}
