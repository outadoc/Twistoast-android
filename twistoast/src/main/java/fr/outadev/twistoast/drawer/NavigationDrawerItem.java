/*
 * Twistoast - NavigationDrawerItem
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
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * An abstract navigation drawer item.
 *
 * @author outadoc
 */
public abstract class NavigationDrawerItem {

	private int titleResId;
	private int iconResId;

	/**
	 * Creates a new NavigationDrawerItem.
	 *
	 * @param titleResId the id of the string resource for the title
	 */
	public NavigationDrawerItem(@DrawableRes int iconResId, @StringRes int titleResId) {
		this.iconResId = iconResId;
		this.titleResId = titleResId;
	}

	public int getTitleResId() {
		return titleResId;
	}

	public int getIconResId() {
		return iconResId;
	}

	/**
	 * Gets a new fragment object for this item.
	 *
	 * @return a fragment corresponding to the view that should be displayed when this item is selected.
	 * @throws IllegalAccessException if we couldn't instantiate the fragment
	 * @throws InstantiationException if we couldn't instantiate the fragment
	 */
	public abstract Fragment getFragment() throws IllegalAccessException, InstantiationException;
}
