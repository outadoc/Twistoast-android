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

package fr.outadev.android.timeo;

/**
 * Stores a bus line and its direction.
 *
 * @author outadoc
 */
public class TimeoLine implements ITimeoIDName {

	private TimeoIDNameObject line;
	private TimeoIDNameObject direction;

	private String color;
	private int networkCode = TimeoRequestHandler.DEFAULT_NETWORK_CODE;

	/**
	 * Create a new line with line details, a direction, and a color.
	 *
	 * @param line        line details: id = line id, name = line name
	 * @param direction   direction details
	 * @param color       line color, as an HTML-like color string (e.g. #123456)
	 * @param networkCode the identifier of the network this line is a part of (e.g. 147 for Twisto)
	 */
	public TimeoLine(TimeoIDNameObject line, TimeoIDNameObject direction, String color, int networkCode) {
		this(line, direction, networkCode);
		this.color = color;
	}

	/**
	 * Create a new line with line details, a direction, and a color.
	 *
	 * @param line        line details: id = line id, name = line name
	 * @param direction   direction details
	 * @param networkCode the identifier of the network this line is a part of (e.g. 147 for Twisto)
	 */
	public TimeoLine(TimeoIDNameObject line, TimeoIDNameObject direction, int networkCode) {
		this.line = line;
		this.direction = direction;
		this.networkCode = networkCode;
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

	public int getNetworkCode() {
		return networkCode;
	}

	public void setNetworkCode(int networkCode) {
		this.networkCode = networkCode;
	}

	@Override
	public String getId() {
		return line.getId();
	}

	@Override
	public void setId(String id) {
		line.setId(id);
	}

	@Override
	public String getName() {
		return line.getName();
	}

	@Override
	public void setName(String name) {
		line.setName(name);
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}

		if(o == null || getClass() != o.getClass()) {
			return false;
		}

		TimeoLine timeoLine = (TimeoLine) o;

		if(networkCode != timeoLine.networkCode) {
			return false;
		}

		if(direction != null ? !direction.equals(timeoLine.direction) : timeoLine.direction != null) {
			return false;
		}

		if(line != null ? !line.equals(timeoLine.line) : timeoLine.line != null) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return line.getName();
	}

}
