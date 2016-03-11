/*
 * Twistoast - TimeoTrafficAlert
 * Copyright (C) 2013-2015 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.android.timeo;

import java.io.Serializable;

/**
 * Traffic alert. Used to inform the user of traffic perturbations.
 *
 * @author outadoc
 */
public class TimeoTrafficAlert implements Serializable {

	private int id;
	private String label;
	private String url;

	/**
	 * Creates a new traffic alert.
	 *
	 * @param id    the id of the alert
	 * @param label the label (title) of the alert
	 * @param url   the URL to redirect to, to get more info
	 */
	public TimeoTrafficAlert(int id, String label, String url) {
		this.id = id;
		this.label = label;
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "TimeoTrafficAlert{" +
				"id=" + id +
				", label='" + label + '\'' +
				", url='" + url + '\'' +
				'}';
	}
}
