/*
 * Twistoast - TimeoRequestObject
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

/**
 * Contains the data necessary to make a call to the API.
 *
 * @author outadoc
 */
public class TimeoRequestObject {

	public TimeoRequestObject(String line, String direction, String stop) {
		this.line = line;
		this.direction = direction;
		this.stop = stop;
	}

	public TimeoRequestObject(String line, String direction) {
		this.line = line;
		this.direction = direction;
	}

	public TimeoRequestObject(String line) {
		this.line = line;
	}

	public TimeoRequestObject() {

	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getStop() {
		return stop;
	}

	public void setStop(String stop) {
		this.stop = stop;
	}

	private String line;
	private String direction;
	private String stop;

}
