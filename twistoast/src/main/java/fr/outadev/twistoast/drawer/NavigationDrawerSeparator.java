/*
 * Twistoast - NavigationDrawerSeparator
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

package fr.outadev.twistoast.drawer;

import android.app.Fragment;

/**
 * A navigation drawer separator.
 */
public class NavigationDrawerSeparator extends NavigationDrawerItem {

	public NavigationDrawerSeparator() {
		super(-1, -1);
	}

	@Override
	public Fragment getFragment() throws IllegalAccessException, InstantiationException {
		return null;
	}
}
