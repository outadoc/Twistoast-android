/*
 * Twistoast - TimeoSingleSchedule
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

package fr.outadev.android.timeo.model;

import android.content.Context;

/**
 * Created by outadoc on 23/08/14.
 */
public class TimeoSingleSchedule {

	private String time;
	private String direction;

	public TimeoSingleSchedule(String time, String direction) {
		this.time = time;
		this.direction = direction;
	}

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
		return getTime();
	}
}
