package fr.outadev.twistoast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PrefsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.main_prefs);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("pref_pebble")) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
			boolean usePebble = sharedPref.getBoolean("pref_pebble", false);

			if(usePebble) {
				Intent intent = new Intent(getActivity(), TwistoastPebbleService.class);
				getActivity().startService(intent);
			}
		}
	}

}
