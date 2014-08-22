/*
 * Twistoast - TimeoScheduleObject
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

package fr.outadev.android.timeo;

import java.util.Arrays;

/**
 * Used to store a schedule, with its corresponding line, direction, and stop
 * objects.
 *
 * @author outadoc
 */
public class TimeoScheduleObject {

	public TimeoScheduleObject(TimeoIDNameObject line, TimeoIDNameObject direction, TimeoIDNameObject stop, String[] schedule) {
		this.line = line;
		this.direction = direction;
		this.stop = stop;
		this.schedule = schedule;
	}

	public TimeoIDNameObject getLine() {
		return line;
	}

	public void setLine(TimeoIDNameObject line) {
		this.line = line;
	}

	public TimeoIDNameObject getDirection() {
		return direction;
	}

	public void setDirection(TimeoIDNameObject direction) {
		this.direction = direction;
	}

	public TimeoIDNameObject getStop() {
		return stop;
	}

	public void setStop(TimeoIDNameObject stop) {
		this.stop = stop;
	}

	public String[] getSchedule() {
		return schedule;
	}

	public void setSchedule(String[] schedule) {
		this.schedule = schedule;
	}

	public String getMessageTitle() {
		return messageTitle;
	}

	public void setMessageTitle(String messageTitle) {
		this.messageTitle = messageTitle;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	@Override
	public String toString() {
		if(messageTitle != null && messageBody != null) {
			return "TimeoScheduleObject [line=" + line + ", direction=" + direction + ", stop=" + stop + ", schedule="
					+ Arrays.toString(schedule) + ", messageTitle=" + messageTitle + ", messageBody=" + messageBody + "]";
		} else {
			return "TimeoScheduleObject [line=" + line + ", direction=" + direction + ", stop=" + stop + ", schedule="
					+ Arrays.toString(schedule) + "]";
		}

	}

	@Override
	public TimeoScheduleObject clone() {
		return new TimeoScheduleObject((line != null) ? line.clone() : null, (direction != null) ? direction.clone() : null,
				(stop != null) ? stop.clone() : null, (schedule != null) ? schedule.clone() : null);
	}

	private TimeoIDNameObject line;
	private TimeoIDNameObject direction;
	private TimeoIDNameObject stop;

	private String[] schedule;

	private String messageTitle;
	private String messageBody;

}
