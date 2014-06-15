package fr.outadev.twistoast.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import fr.outadev.twistoast.R;

public class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

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
		
		try {
        	PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
	        findPreference("version").setSummary("Twistoast v" + info.versionName);
        } catch(NameNotFoundException e1) {
	        e1.printStackTrace();
        }
	}

	@Override
	public void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
	}

}
