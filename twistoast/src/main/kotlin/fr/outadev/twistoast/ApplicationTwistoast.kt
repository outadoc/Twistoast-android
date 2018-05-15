/*
 * Twistoast - ApplicationTwistoast.kt
 * Copyright (C) 2013-2018 Baptiste Candellier
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

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import fr.outadev.twistoast.background.BackgroundTasksManager

/**
 * Global application class for Twistoast.
 */
class ApplicationTwistoast : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        val db = Database(DatabaseOpenHelper())
        val config = ConfigurationManager()

        val nightModeCode = getNightModeForPref(config.nightMode)
        AppCompatDelegate.setDefaultNightMode(nightModeCode)

        // Turn the notifications back off if necessary
        if (db.watchedStopsCount > 0) {
            BackgroundTasksManager.enableStopAlarmJob()
        }
    }

    private fun getNightModeForPref(pref: String): Int {
        return when (pref) {
            "day" -> AppCompatDelegate.MODE_NIGHT_NO
            "night" -> AppCompatDelegate.MODE_NIGHT_YES
            "auto" -> AppCompatDelegate.MODE_NIGHT_AUTO
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    companion object {
        lateinit var instance: ApplicationTwistoast
            private set
    }

}
