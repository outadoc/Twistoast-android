/*
 * Twistoast - NavigationDrawerWebItem
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
import android.os.Bundle;
import android.support.annotation.StringRes;

import fr.outadev.twistoast.ui.fragments.WebViewFragment;

/**
 * A navigation drawer item designed to open a web page fragment when selected.
 *
 * @author outadoc
 */
public class NavigationDrawerWebItem extends NavigationDrawerFragmentItem {

	private String url;

	/**
	 * Creates a new NavigationDrawerWebItem.
	 *
	 * @param titleResId the id of the string resource for the title
	 * @param url        the URL of the page to open in the fragment
	 */
	public NavigationDrawerWebItem(@StringRes int titleResId, String url) {
		super(titleResId, WebViewFragment.class);
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public Fragment getFragment() throws IllegalAccessException, InstantiationException {
		Fragment frag = super.getFragment();
		Bundle args = new Bundle();
		args.putString("url", url);
		frag.setArguments(args);
		return frag;
	}
}
