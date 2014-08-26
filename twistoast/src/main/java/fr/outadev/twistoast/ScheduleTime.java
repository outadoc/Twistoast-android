/*
 * Twistoast - ScheduleTime
 * Copyright (C) 2013-2014  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast;

import android.content.Context;

import java.util.Calendar;

/**
 * Created by outadoc on 26/08/14.
 */
public abstract class ScheduleTime {

	public enum TimeDisplayMode {
		CURRENTLY_AT_STOP, ARRIVAL_IMMINENT, COUNTDOWN, FULL
	}

	public static String formatDate(Context context, String time) {
		Calendar schedule = getNextDateForTime(time);

		switch(getTimeDisplayMode(schedule)) {
			case CURRENTLY_AT_STOP:
				return context.getString(R.string.schedule_time_currently_at_stop);
			case ARRIVAL_IMMINENT:
				return context.getString(R.string.schedule_time_arrival_imminent);
			case COUNTDOWN:
				return context.getString(R.string.schedule_time_countdown, getMinutesUntilBus(schedule));
			default:
			case FULL:
				return time;
		}
	}

	public static long getMillisUntilBus(Calendar schedule) {
		Calendar now = Calendar.getInstance();
		return schedule.getTimeInMillis() - now.getTimeInMillis();
	}

	public static long getMinutesUntilBus(Calendar schedule) {
		return (long) Math.ceil(getMillisUntilBus(schedule) / 1000 / 60);
	}

	public static TimeDisplayMode getTimeDisplayMode(Calendar schedule) {
		long offset = getMinutesUntilBus(schedule);

		if(offset <= 0) {
			return TimeDisplayMode.CURRENTLY_AT_STOP;
		} else if(offset <= 1) {
			return TimeDisplayMode.ARRIVAL_IMMINENT;
		} else if(offset <= 45) {
			return TimeDisplayMode.COUNTDOWN;
		} else {
			return TimeDisplayMode.FULL;
		}
	}

	public static Calendar getNextDateForTime(String time) {
		String[] splitTime = time.split(":");

		int hours = Integer.valueOf(splitTime[0]);
		int minutes = Integer.valueOf(splitTime[1]);

		Calendar scheduledTime = Calendar.getInstance();
		Calendar now = Calendar.getInstance();

		scheduledTime.set(Calendar.HOUR_OF_DAY, hours);
		scheduledTime.set(Calendar.MINUTE, minutes);

		if(now.get(Calendar.HOUR_OF_DAY) > hours
				|| now.get(Calendar.HOUR_OF_DAY) == hours && now.get(Calendar.MINUTE) > minutes) {
			scheduledTime.add(Calendar.DAY_OF_YEAR, 1);
		}

		return scheduledTime;
	}

}
