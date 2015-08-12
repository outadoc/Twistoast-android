/*
 * Twistoast - CommonAlarmReceiver
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

package fr.outadev.twistoast.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

/**
 * An abstract alarm receiver that implements a few common methods to be used by its children.
 */
public abstract class CommonAlarmReceiver extends BroadcastReceiver {

	/**
	 * Gets suitable notification defaults for the notifications of this receiver.
	 * Use them with NotificationCompat.Builder.setDefaults().
	 *
	 * @param context a context
	 * @return an integer to pass to the builder
	 */
	protected int getNotificationDefaults(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		boolean prefVibrate = prefs.getBoolean("pref_notif_" + getPreferencesKeyPrefix() + "_vibrate", true);
		boolean prefRing = prefs.getBoolean("pref_notif_" + getPreferencesKeyPrefix() + "_ring", true);

		int defaults = NotificationCompat.DEFAULT_LIGHTS;

		if(prefVibrate) {
			defaults = defaults | NotificationCompat.DEFAULT_VIBRATE;
		}

		if(prefRing) {
			defaults = defaults | NotificationCompat.DEFAULT_SOUND;
		}

		return defaults;
	}

	/**
	 * The prefix used by this receiver for its preference keys.
	 *
	 * @return the format of the preference keys must be of the form
	 * "pref_notif_<prefix>_vibrate" and "pref_notif_<prefix>_ring",
	 * where prefix is the value returned by this method
	 */
	protected abstract String getPreferencesKeyPrefix();

}
