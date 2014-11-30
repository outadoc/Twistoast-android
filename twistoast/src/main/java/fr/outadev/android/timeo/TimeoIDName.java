/*
 * Twistoast - ITimeoIDName
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
 * Defines an object that possesses a name and an identifier.
 */
public interface TimeoIDName {

	/**
	 * Gets the identifier of this object.
	 *
	 * @return the id
	 */
	public String getId();

	/**
	 * Sets the identifier of this object.
	 * @param id the id
	 */
	public void setId(String id);

	/**
	 * Gets the name of this object.
	 * @return the name
	 */
	public String getName();

	/**
	 * Sets the name of this object.
	 * @param name the name
	 */
	public void setName(String name);

}
