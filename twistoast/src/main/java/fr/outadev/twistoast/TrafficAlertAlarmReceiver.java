/*
 * Twistoast - TrafficAlertAlarmReceiver
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

package fr.outadev.twistoast;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TrafficAlertAlarmReceiver extends BroadcastReceiver {

	private static final int ALARM_TYPE = AlarmManager.ELAPSED_REALTIME;
	private static final int ALARM_FREQUENCY = 60 * 1000;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(Utils.TAG, "checking traffic alert");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		int lastTrafficId = prefs.getInt("last_traffic_notif_id", -1);
		int newTrafficId = -1;

		if(lastTrafficId != newTrafficId) {
			NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(context)
							.setSmallIcon(R.drawable.ic_stat_notify_twistoast)
							.setContentTitle("Alerte trafic")
							.setContentText("Titre random")
							.setSubText("Hello World!")
							.setCategory(NotificationCompat.CATEGORY_TRANSPORT)
							.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
							.addAction(R.drawable.ic_action_web_site_small, "Plus d'infos", null)
							.setOnlyAlertOnce(true);

			manager.notify(newTrafficId, mBuilder.build());
			prefs.edit().putInt("last_traffic_notif_id", newTrafficId).apply();
		}
	}

	public static void enable(Context context) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setInexactRepeating(ALARM_TYPE,
				SystemClock.elapsedRealtime() + 60 * 1000, ALARM_FREQUENCY, getBroadcast(context));
	}

	public static void disable(Context context) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(getBroadcast(context));
	}

	protected static PendingIntent getBroadcast(Context context) {
		Intent intent = new Intent(context, TrafficAlertAlarmReceiver.class);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

}
