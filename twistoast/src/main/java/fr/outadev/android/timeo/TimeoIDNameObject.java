/*
 * Twistoast - TimeoIDNameObject
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
 * Associates an ID to a name. Used to associate a line ID with its name, for
 * example.
 *
 * @author outadoc
 */
public class TimeoIDNameObject implements TimeoIDName {

	private String id;
	private String name;

	/**
	 * Creates an ID/name object.
	 *
	 * @param id   the id of the object
	 * @param name the name of the object
	 */
	public TimeoIDNameObject(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public TimeoIDNameObject() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object object) {
		if(object == this) {
			return true;
		}

		if(object instanceof TimeoIDNameObject) {
			if(((TimeoIDNameObject) object).getId().equals(this.getId())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return id + " - " + name;
	}

}
