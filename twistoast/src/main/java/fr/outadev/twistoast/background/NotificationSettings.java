package fr.outadev.twistoast.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

/**
 * Created by outadoc on 2016-03-07.
 */
public abstract class NotificationSettings {

    /**
     * Gets suitable notification defaults for the notifications of this receiver.
     * Use them with NotificationCompat.Builder.setDefaults().
     *
     * @param context a context
     * @return an integer to pass to the builder
     */
    public static int getNotificationDefaults(Context context, String prefix) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean prefVibrate = prefs.getBoolean("pref_notif_" + prefix + "_vibrate", true);
        boolean prefRing = prefs.getBoolean("pref_notif_" + prefix + "_ring", true);

        int defaults = NotificationCompat.DEFAULT_LIGHTS;

        if (prefVibrate) {
            defaults = defaults | NotificationCompat.DEFAULT_VIBRATE;
        }

        if (prefRing) {
            defaults = defaults | NotificationCompat.DEFAULT_SOUND;
        }

        return defaults;
    }

}
