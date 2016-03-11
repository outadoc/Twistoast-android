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

package fr.outadev.twistoast;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import fr.outadev.twistoast.background.BackgroundTasksManager;

/**
 * A preferences fragment for the preferences of the app.
 *
 * @author outadoc
 */
public class FragmentPreferences extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    public static final int PERM_REQUEST_LOCATION = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.main_prefs);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceStates();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // If we're changing the theme, automatically restart the app
        switch (key) {
            case "pref_night_mode":
                updatePreferenceStates();

                if (sharedPreferences.getString("pref_night_mode", "system").equals("auto")) {
                    int check = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (check == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(
                                getActivity(), new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERM_REQUEST_LOCATION);
                    }
                }
            case "pref_app_theme":
                restartApp();
                break;
            case "pref_enable_notif_traffic":
                if (sharedPreferences.getBoolean("pref_enable_notif_traffic", true)) {
                    BackgroundTasksManager.enableTrafficAlertJob(getActivity().getApplicationContext());
                } else {
                    BackgroundTasksManager.disableTrafficAlertJob(getActivity().getApplicationContext());
                }

                updatePreferenceStates();
                break;
        }
    }

    private void restartApp() {
        (new AlertDialog.Builder(getActivity())
            .setTitle(R.string.pref_restart_required_title)
            .setMessage(R.string.pref_restart_required_message)
            .setPositiveButton(R.string.pref_restart_required_positive, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int mPendingIntentId = 1;
                    Intent i = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
                    PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId, i, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);

                    System.exit(0);
                }

            })
            .setNegativeButton(R.string.pref_restart_required_negative, null)
            .create()).show();
    }

    /**
     * Updates the state of preferences that rely on other preferences.
     * For example, this will disable "ring" and "vibrate" options for traffic notifications if the latter are disabled.
     */
    private void updatePreferenceStates() {
        boolean enabled = getPreferenceScreen().getSharedPreferences().getBoolean("pref_enable_notif_traffic", true);

        findPreference("pref_notif_traffic_ring").setEnabled(enabled);
        findPreference("pref_notif_traffic_vibrate").setEnabled(enabled);

        ListPreference nmPref = (ListPreference)findPreference("pref_night_mode");
        nmPref.setSummary(nmPref.getEntry());
    }

}
