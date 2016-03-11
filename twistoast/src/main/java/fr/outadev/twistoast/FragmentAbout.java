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

package fr.outadev.twistoast;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * A preferences fragment for the preferences of the app.
 *
 * @author outadoc
 */
public class FragmentAbout extends PreferenceFragment {

    private ConfigurationManager mConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConfig = new ConfigurationManager(getActivity());

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.about_prefs);
        findPreference("version").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            private int mEasterEggCount = 5;
            private Toast mToast;

            @SuppressLint("ShowToast")
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mEasterEggCount--;

                if (mToast != null) {
                    mToast.cancel();
                }

                if (mConfig.getAdsAreRemoved()) {
                    // Already disabled the ads
                    (mToast = Toast.makeText(getActivity(), R.string.prefs_ads_already_disabled, Toast.LENGTH_SHORT))
                            .show();

                } else if (mEasterEggCount == 0) {
                    // Ready to disable the ads
                    mConfig.setAdsAreRemoved(true);
                    (mToast = Toast.makeText(getActivity(), R.string.prefs_ads_disabled, Toast.LENGTH_SHORT)).show();

                } else if (mEasterEggCount <= 3) {
                    // Decrement teh counter
                    (mToast = Toast.makeText(getActivity(),
                            getActivity().getString(R.string.prefs_ads_step_count, mEasterEggCount), Toast.LENGTH_SHORT)).show();
                }

                return true;
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            findPreference("version").setSummary(getString(R.string.app_name) + " v" + info.versionName);
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
    }

}
