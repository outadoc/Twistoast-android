/*
 * Twistoast - TimeoLine
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

/**
 * Created by outadoc on 23/08/14.
 */
public class TimeoLine {

	private TimeoIDNameObject line;
	private TimeoIDNameObject direction;

	private String color;

	public TimeoLine(TimeoIDNameObject line, TimeoIDNameObject direction, String color) {
		this(line, direction);
		this.color = color;
	}

	public TimeoLine(TimeoIDNameObject line, TimeoIDNameObject direction) {
		this.line = line;
		this.direction = direction;
	}

	public TimeoIDNameObject getDetails() {
		return line;
	}

	public void setDetails(TimeoIDNameObject line) {
		this.line = line;
	}

	public TimeoIDNameObject getDirection() {
		return direction;
	}

	public void setDirection(TimeoIDNameObject direction) {
		this.direction = direction;
	}

	public String getColor() {
		return (color == null) ? "#34495E" : color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public String toString() {
		return line.getName();
	}

}
