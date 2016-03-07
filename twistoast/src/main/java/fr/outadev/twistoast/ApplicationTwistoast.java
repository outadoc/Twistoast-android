package fr.outadev.twistoast;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

/**
 * Global application class for Twistoast.
 */
public class ApplicationTwistoast extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String nightMode = prefs.getString("pref_night_mode", "system");
        int nightModeCode = getNightModeForPref(nightMode);

        //noinspection WrongConstant
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
