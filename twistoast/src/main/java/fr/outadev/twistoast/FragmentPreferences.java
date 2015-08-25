/*
 * Twistoast - FragmentPreferences
 * Copyright (C) 2013-2015 Baptiste Candellier
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import fr.outadev.twistoast.background.TrafficAlertAlarmReceiver;

/**
 * A preferences fragment for the preferences of the app.
 *
 * @author outadoc
 */
public class FragmentPreferences extends PreferenceFragment implements OnSharedPreferenceChangeListener {

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

		updateDependentSwitchesState();
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
		switch(key) {
			case "pref_app_theme":
				Intent i = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				break;
			case "pref_enable_notif_traffic":
				if(sharedPreferences.getBoolean("pref_enable_notif_traffic", true)) {
					TrafficAlertAlarmReceiver.enable(getActivity().getApplicationContext());
				} else {
					TrafficAlertAlarmReceiver.disable(getActivity().getApplicationContext());
				}

				updateDependentSwitchesState();
				break;
		}
	}

	/**
	 * Updates the state of preferences that rely on other preferences.
	 * For example, this will disable "ring" and "vibrate" options for traffic notifications if the latter are disabled.
	 */
	private void updateDependentSwitchesState() {
		boolean enabled = getPreferenceScreen().getSharedPreferences().getBoolean("pref_enable_notif_traffic", true);
		findPreference("pref_notif_traffic_ring").setEnabled(enabled);
	}

}
