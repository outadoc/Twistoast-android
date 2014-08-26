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

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by outadoc on 26/08/14.
 */
public abstract class ScheduleTime {

	public static String formatDate(String time) {
		Calendar now = Calendar.getInstance();
		Calendar schedule = getNextDateForTime(time);

		int offset = (int) (schedule.getTimeInMillis() - now.getTimeInMillis());
		System.out.println(time + " >> " + (new SimpleDateFormat().format(schedule.getTime())));

		if(offset < 0) {
			return "Passage en cours";
		} else if(offset < 60 * 1000) {
			return "Passage imminent";
		} else if(offset < 45 * 60 * 1000) {
			return (offset / 60 / 1000) + " minutes";
		} else {
			return time;
		}
	}

	public static Calendar getNextDateForTime(String time) {
		String[] splitTime = time.split(":");

		int hours = Integer.valueOf(splitTime[0]);
		int minutes = Integer.valueOf(splitTime[1]);

		Calendar now = Calendar.getInstance();
		Calendar scheduledTime = Calendar.getInstance();

		scheduledTime.set(Calendar.HOUR_OF_DAY, hours);
		scheduledTime.set(Calendar.MINUTE, minutes);

		if(now.get(Calendar.HOUR_OF_DAY) > hours
				|| now.get(Calendar.HOUR_OF_DAY) == hours && now.get(Calendar.MINUTE) > minutes + 2) {
			scheduledTime.add(Calendar.DAY_OF_YEAR, 1);
		}

		return scheduledTime;
	}

}
