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

package fr.outadev.twistoast;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Manages application preferences. Use this to read or write application settings instead
 * of directly calling SharedPreferences.get*.
 */
public class ConfigurationManager {

    private final SharedPreferences mPreferences;

    public ConfigurationManager(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getApplicationThemeColor() {
        return mPreferences.getInt("pref_app_theme", -1);
    }

    public String getNightMode() {
        return mPreferences.getString("pref_night_mode", "system");
    }

    public boolean getAutoRefresh() {
        return mPreferences.getBoolean("pref_auto_refresh", true);
    }

    public boolean getAdsAreRemoved() {
        return mPreferences.getBoolean("pref_disable_ads", false);
    }

    public void setAdsAreRemoved(boolean removed) {
        mPreferences.edit().putBoolean("pref_disable_ads", removed).apply();
    }

    public boolean getUseColorInPebbleApp() {
        return  mPreferences.getBoolean("pref_pebble_use_color", true);
    }

    public boolean getTrafficNotificationsEnabled() {
        return mPreferences.getBoolean("pref_enable_notif_traffic", true);
    }

    public boolean getTrafficNotificationsRing() {
        return mPreferences.getBoolean("pref_notif_traffic_ring", true);
    }

    public boolean getTrafficNotificationsVibrate() {
        return mPreferences.getBoolean("pref_notif_traffic_vibrate", true);
    }

    public boolean getWatchNotificationsRing() {
        return mPreferences.getBoolean("pref_notif_watched_ring", true);
    }

    public boolean getWatchNotificationsVibrate() {
        return mPreferences.getBoolean("pref_notif_watched_vibrate", true);
    }

    public int getLastTrafficNotificationId() {
        return mPreferences.getInt("last_traffic_notif_id", -1);
    }

    public void setLastTrafficNotificationId(int id) {
        mPreferences.edit().putInt("last_traffic_notif_id", id).apply();
    }

    public String getListSortOrder() {
        return mPreferences.getString("pref_list_sortby", "line");
    }

    public void setListSortOrder(String sortOrder) {
        mPreferences.edit().putString("pref_list_sortby", sortOrder).apply();
    }

}
