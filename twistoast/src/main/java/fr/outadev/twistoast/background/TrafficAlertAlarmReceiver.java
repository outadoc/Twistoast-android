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

package fr.outadev.twistoast.background;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoTrafficAlert;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.Utils;

public class TrafficAlertAlarmReceiver extends BroadcastReceiver {

	private static final int ALARM_TYPE = AlarmManager.ELAPSED_REALTIME;
	private static final long ALARM_FREQUENCY = AlarmManager.INTERVAL_HALF_HOUR;

	@Override
	public void onReceive(final Context context, Intent intent) {

		(new AsyncTask<Void, Void, TimeoTrafficAlert>() {

			private int lastTrafficId;

			private SharedPreferences prefs;
			private NotificationManager notificationManager;

			@Override
			protected void onPreExecute() {
				prefs = PreferenceManager.getDefaultSharedPreferences(context);
				notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

				lastTrafficId = prefs.getInt("last_traffic_notif_id", -1);

				Log.d(Utils.TAG, "checking traffic alert");
			}

			@Override
			protected TimeoTrafficAlert doInBackground(Void... params) {
				return TimeoRequestHandler.getGlobalTrafficAlert(context.getString(R.string.url_pre_home_info));
			}

			@Override
			protected void onPostExecute(TimeoTrafficAlert trafficAlert) {
				if(trafficAlert != null) {
					Log.d(Utils.TAG, "found traffic alert #" + trafficAlert.getId());

					if(lastTrafficId != trafficAlert.getId()) {
						Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trafficAlert.getUrl()));
						PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

						NotificationCompat.Builder mBuilder =
								new NotificationCompat.Builder(context)
										.setSmallIcon(R.drawable.ic_stat_notify_twistoast)
										.setContentTitle(context.getString(R.string.notifs_traffic_title))
										.setContentText(trafficAlert.getLabel())
										.setCategory(NotificationCompat.CATEGORY_MESSAGE)
										.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
										.setContentIntent(contentIntent)
										.setAutoCancel(true)
										.setOnlyAlertOnce(true);

						notificationManager.notify(trafficAlert.getId(), mBuilder.build());
						prefs.edit().putInt("last_traffic_notif_id", trafficAlert.getId()).apply();
						return;
					}
				}

				Log.d(Utils.TAG, "checked traffic: nothing new!");
			}

		}).execute();

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
