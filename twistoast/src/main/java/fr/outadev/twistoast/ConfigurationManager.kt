/*
 * Twistoast - ConfigurationManager
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

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Manages application preferences. Use this to read or write application settings instead
 * of directly calling SharedPreferences.get*.
 */
class ConfigurationManager(context: Context = ApplicationTwistoast.instance) {

    private val preferences: SharedPreferences

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    val applicationThemeColor: Int
        get() = preferences.getInt("pref_app_theme", -1)

    val nightMode: String
        get() = preferences.getString("pref_night_mode", "system")

    val autoRefresh: Boolean
        get() = preferences.getBoolean("pref_auto_refresh", true)

    var adsAreRemoved: Boolean
        get() = preferences.getBoolean("pref_disable_ads", false)
        set(removed) = preferences.edit().putBoolean("pref_disable_ads", removed).apply()

    val useColorInPebbleApp: Boolean
        get() = preferences.getBoolean("pref_pebble_use_color", true)

    val trafficNotificationsEnabled: Boolean
        get() = preferences.getBoolean("pref_enable_notif_traffic", true)

    val trafficNotificationsRing: Boolean
        get() = preferences.getBoolean("pref_notif_traffic_ring", true)

    val trafficNotificationsVibrate: Boolean
        get() = preferences.getBoolean("pref_notif_traffic_vibrate", true)

    val watchNotificationsRing: Boolean
        get() = preferences.getBoolean("pref_notif_watched_ring", true)

    val watchNotificationsVibrate: Boolean
        get() = preferences.getBoolean("pref_notif_watched_vibrate", true)

    var lastTrafficNotificationId: Int
        get() = preferences.getInt("last_traffic_notif_id", -1)
        set(id) = preferences.edit().putInt("last_traffic_notif_id", id).apply()

    var listSortOrder: Database.SortBy
        get() = stringToSortCriteria(preferences.getString("pref_list_sortby", "line"))
        set(sortOrder) = preferences.edit().putString("pref_list_sortby", sortCriteriaToString(sortOrder)).apply()

    private fun stringToSortCriteria(sortBy: String): Database.SortBy {
        when (sortBy.toLowerCase()) {
            "stop" -> return Database.SortBy.STOP
            "line" -> return Database.SortBy.LINE
            else -> return Database.SortBy.LINE
        }
    }

    private fun sortCriteriaToString(sortBy: Database.SortBy): String {
        return sortBy.name.toLowerCase()
    }

}
