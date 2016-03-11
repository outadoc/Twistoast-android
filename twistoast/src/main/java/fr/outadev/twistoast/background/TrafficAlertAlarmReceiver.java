/*
 * Twistoast - TrafficAlertAlarmReceiver
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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

/**
 * A broadcast receiver called at regular intervals to check
 * if there are traffic problems and the user should be notified.
 */
public class TrafficAlertAlarmReceiver extends CommonAlarmReceiver {

	private static final int ALARM_TYPE = AlarmManager.ELAPSED_REALTIME;
	private static final long ALARM_FREQUENCY = AlarmManager.INTERVAL_HALF_HOUR;

	/**
	 * Enables the regular checks performed every X minutes by this receiver.
	 * They should be disabled once not needed anymore, as they can be battery and network hungry.
	 *
	 * @param context a context
	 */
	public static void enable(Context context) {
		Log.d(Utils.TAG, "enabling " + TrafficAlertAlarmReceiver.class.getSimpleName());

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setInexactRepeating(ALARM_TYPE,
				SystemClock.elapsedRealtime() + 60 * 1000, ALARM_FREQUENCY, getBroadcast(context));
	}

	/**
	 * Disables the regular checks performed every X minutes by this receiver.
	 *
	 * @param context a context
	 */
	public static void disable(Context context) {
		Log.d(Utils.TAG, "disabling " + TrafficAlertAlarmReceiver.class.getSimpleName());

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(getBroadcast(context));
	}

	/**
	 * Returns the PendingIntent that will be called by the alarm every X minutes.
	 *
	 * @param context a context
	 * @return the PendingIntent corresponding to this class
	 */
	protected static PendingIntent getBroadcast(Context context) {
		Intent intent = new Intent(context, TrafficAlertAlarmReceiver.class);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	@Override
	public void onReceive(final Context context, Intent intent) {

		(new AsyncTask<Void, Void, TimeoTrafficAlert>() {

			private int lastTrafficId;

			private SharedPreferences prefs;
			private NotificationManager notificationManager;

			@Override
			protected TimeoTrafficAlert doInBackground(Void... params) {
				try {
					return TimeoRequestHandler.getGlobalTrafficAlert(context.getString(R.string.url_pre_home_info));
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPreExecute() {
				prefs = PreferenceManager.getDefaultSharedPreferences(context);
				notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

				lastTrafficId = prefs.getInt("last_traffic_notif_id", -1);

				Log.d(Utils.TAG, "checking traffic alert");
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
										.setSmallIcon(R.drawable.ic_traffic_cone_white)
										.setContentTitle(context.getString(R.string.notifs_traffic_title))
										.setContentText(trafficAlert.getLabel())
										.setStyle(new NotificationCompat.BigTextStyle()
												.bigText(trafficAlert.getLabel()))
										.setCategory(NotificationCompat.CATEGORY_MESSAGE)
										.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
										.setPriority(NotificationCompat.PRIORITY_DEFAULT)
										.setContentIntent(contentIntent)
										.setAutoCancel(true)
										.setOnlyAlertOnce(true)
										.setDefaults(getNotificationDefaults(context));

						notificationManager.notify(trafficAlert.getId(), mBuilder.build());
						prefs.edit().putInt("last_traffic_notif_id", trafficAlert.getId()).apply();
						return;
					}
				}

				Log.d(Utils.TAG, "checked traffic: nothing new!");
			}

		}).execute();

	}

	@Override
	protected String getPreferencesKeyPrefix() {
		return "traffic";
	}

}
