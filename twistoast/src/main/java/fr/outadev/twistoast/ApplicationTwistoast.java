/*
 * Twistoast - ApplicationTwistoast
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

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

/**
 * Global application class for Twistoast.
 */
public class ApplicationTwistoast extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ConfigurationManager config = new ConfigurationManager(this);
        int nightModeCode = getNightModeForPref(config.getNightMode());

        //noinspection WrongConstant,ResourceType
        AppCompatDelegate.setDefaultNightMode(nightModeCode);
    }

    private int getNightModeForPref(String pref) {
        switch (pref) {
            case "day":
                return AppCompatDelegate.MODE_NIGHT_NO;
            case "night":
                return AppCompatDelegate.MODE_NIGHT_YES;
            case "auto":
                return AppCompatDelegate.MODE_NIGHT_AUTO;
            case "system":
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

}
