/*
 * Twistoast - TimeoStopObject
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
 * Created by outadoc on 23/08/14.
 */
public class TimeoStopObject extends TimeoIDNameObject {

	private String ref;

	/**
	 * Creates an ID/name object.
	 *
	 * @param id   the id of the object
	 * @param name the name of the object
	 * @see fr.outadev.android.timeo.TimeoRequestObject
	 * @see fr.outadev.android.timeo.TimeoScheduleObject
	 */
	public TimeoStopObject(String id, String name, String ref) {
		super(id, name);
		this.ref = ref;
	}

	public String getReference() {
		return ref;
	}

	public void setReference(String ref) {
		this.ref = ref;
	}
}
