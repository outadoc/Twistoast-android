/*
 * Twistoast - NavigationDrawerFragmentItem
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

package fr.outadev.twistoast.ui.drawer;

import android.app.Fragment;

/**
 * A navigation drawer item designed to open a fragment when selected.
 *
 * @author outadoc
 */
public class NavigationDrawerFragmentItem extends NavigationDrawerItem {

	private Class classToInstantiate;

	/**
	 * Creates a new NavigationDrawerFragmentItem.
	 *
	 * @param titleResId         the id of the string resource for the title
	 * @param classToInstantiate the Class object of the Fragment to return with getFragment
	 */
	public NavigationDrawerFragmentItem(int titleResId, Class classToInstantiate) {
		super(titleResId);
		this.classToInstantiate = classToInstantiate;
	}

	@Override
	public Fragment getFragment() throws IllegalAccessException, InstantiationException {
		return (Fragment) classToInstantiate.newInstance();
	}
}
