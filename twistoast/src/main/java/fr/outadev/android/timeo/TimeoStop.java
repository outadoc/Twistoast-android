/*
 * Twistoast - TimeoStop
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
 * A bus stop.
 *
 * @author outadoc
 */
public class TimeoStop extends TimeoIDNameObject {

	private String ref;
	private TimeoLine line;

	/**
	 * Creates a stop.
	 *
	 * @param id   the id of the stop
	 * @param name the name of the stop
	 * @param ref  the reference of the stop
	 */
	public TimeoStop(String id, String name, String ref, TimeoLine line) {
		super(id, name);
		this.ref = ref;
		this.line = line;
	}

	public TimeoStop(TimeoLine line) {
		this.line = line;
	}

	public String getReference() {
		return ref;
	}

	public void setReference(String ref) {
		this.ref = ref;
	}

	public TimeoLine getLine() {
		return line;
	}

	public void setLine(TimeoLine line) {
		this.line = line;
	}

	@Override
	public String toString() {
		return getName();
	}
}
