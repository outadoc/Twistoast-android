/*
 * Twistoast - BootReceiver
 * Copyright (C) 2013-2015  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import fr.outadev.twistoast.Database;
import fr.outadev.twistoast.DatabaseOpenHelper;

/**
 * The boot receiver of the application. It enables the notification receivers if needed.
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			// Turn the notifications back off if necessary
			Database db = new Database(DatabaseOpenHelper.getInstance(context));
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

			if(db.getWatchedStopsCount() > 0) {
				NextStopAlarmReceiver.enable(context.getApplicationContext());
			}

			if(prefs.getBoolean("pref_enable_notif_traffic", true)) {
				TrafficAlertAlarmReceiver.enable(context.getApplicationContext());
			}

		}
	}

}
