/*
 * Twistoast - FragmentPreferences
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

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import fr.outadev.twistoast.background.BackgroundTasksManager

/**
 * A preferences fragment for the preferences of the app.
 *
 * @author outadoc
 */
class FragmentPreferences : PreferenceFragment(), OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.main_prefs)
    }

    override fun onResume() {
        super.onResume()
        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        updatePreferenceStates()
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // If we're changing the theme, automatically restart the app
        when (key) {
            "pref_night_mode" -> {
                updatePreferenceStates()

                if (sharedPreferences.getString("pref_night_mode", "system") == "auto") {
                    val check = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (check == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(
                                activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERM_REQUEST_LOCATION)
                    }
                }
                restartApp()
            }
            "pref_app_theme" -> restartApp()
            "pref_enable_notif_traffic" -> {
                if (sharedPreferences.getBoolean("pref_enable_notif_traffic", true)) {
                    BackgroundTasksManager.enableTrafficAlertJob(activity.applicationContext)
                } else {
                    BackgroundTasksManager.disableTrafficAlertJob(activity.applicationContext)
                }

                updatePreferenceStates()
            }
        }
    }

    private fun restartApp() {
        val pendingIntentId = 1
        val mgr = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        AlertDialog.Builder(activity)
                .setTitle(R.string.pref_restart_required_title)
                .setMessage(R.string.pref_restart_required_message)
                .setNegativeButton(R.string.pref_restart_required_negative, null)
                .setPositiveButton(R.string.pref_restart_required_positive) {
                    dialog, which ->
                    val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
                    val pendingIntent = PendingIntent.getActivity(activity, pendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
                    System.exit(0)
                }.create().show()
    }

    /**
     * Updates the state of preferences that rely on other preferences.
     * For example, this will disable "ring" and "vibrate" options for traffic notifications if the latter are disabled.
     */
    private fun updatePreferenceStates() {
        val enabled = preferenceScreen.sharedPreferences.getBoolean("pref_enable_notif_traffic", true)
        val nmPref = findPreference("pref_night_mode") as ListPreference

        findPreference("pref_notif_traffic_ring").isEnabled = enabled
        findPreference("pref_notif_traffic_vibrate").isEnabled = enabled

        nmPref.summary = nmPref.entry
    }

    companion object {
        val PERM_REQUEST_LOCATION = 1
    }

}
