/*
 * Twistoast - FragmentAbout
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

import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.widget.Toast

/**
 * A preferences fragment for the preferences of the app.

 * @author outadoc
 */
class FragmentAbout : PreferenceFragment() {

    private var easterEggCount = 5
    private var easterEggToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = ConfigurationManager()

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.about_prefs)

        findPreference("version").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            easterEggCount--
            easterEggToast?.cancel()

            if (config.adsAreRemoved) {
                // Already disabled the ads
                easterEggToast = toast(R.string.prefs_ads_already_disabled)

            } else if (easterEggCount == 0) {
                // Ready to disable the ads
                config.adsAreRemoved = true
                easterEggToast = toast(R.string.prefs_ads_disabled)

            } else if (easterEggCount <= 3) {
                // Decrement teh counter
                easterEggToast = toast(activity.getString(R.string.prefs_ads_step_count, easterEggCount))
            }

            true
        }
    }

    fun toast(resId: Int) : Toast? {
        return toast(activity.getString(resId))
    }

    fun toast(str: String) : Toast? {
        val t = Toast.makeText(activity, str, Toast.LENGTH_SHORT)
        t.show()
        return t
    }

    override fun onResume() {
        super.onResume()

        try {
            val info = activity.packageManager.getPackageInfo(activity.packageName, 0)
            findPreference("version").summary = getString(R.string.app_name) + " v" + info.versionName
        } catch (e1: NameNotFoundException) {
            e1.printStackTrace()
        }

    }

}
