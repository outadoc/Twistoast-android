/*
 * Twistoast - FragmentFactory.kt
 * Copyright (C) 2013-2016 Baptiste Candellier
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

package fr.outadev.twistoast

import android.app.Fragment
import android.content.Context
import android.os.Bundle

/**
 * A factory that instantiates fragments for the navigation drawer, using the menu item ID.
 */
object FragmentFactory {

    /**
     * Gets a new Fragment corresponding to the specified menu item ID.
     *
     * @param context A context
     * @param itemId  The menu item's identifier
     *
     * @return A new fragment, or null if no corresponding fragment could be found.
     */
    fun getFragmentFromMenuItem(context: Context, itemId: Int): Fragment? {
        when (itemId) {
            R.id.drawer_realtime -> return FragmentRealtime()
            R.id.drawer_timetables -> return getWebViewFragment(context.getString(R.string.url_drawer_timetables))
            R.id.drawer_routes -> return getWebViewFragment(context.getString(R.string.url_drawer_navigation))
            R.id.drawer_map -> return getWebViewFragment(context.getString(R.string.url_drawer_map))
            R.id.drawer_traffic -> return getWebViewFragment(context.getString(R.string.url_drawer_traffic))
            R.id.drawer_news -> return getWebViewFragment(context.getString(R.string.url_drawer_news))
            R.id.drawer_social -> return getWebViewFragment(context.getString(R.string.url_drawer_social))
            R.id.drawer_pricing -> return getWebViewFragment(context.getString(R.string.url_drawer_pricing))
            R.id.drawer_settings -> return FragmentPreferences()
            R.id.drawer_about -> return FragmentAbout()
            else -> return null
        }
    }

    private fun getWebViewFragment(url: String): Fragment {
        val frag = FragmentWebView()
        val args = Bundle()
        args.putString("url", url)
        frag.arguments = args
        return frag
    }

}
