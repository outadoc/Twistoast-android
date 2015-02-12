/*
 * Twistoast - NextStopAlarmReceiver
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
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import fr.outadev.android.timeo.ScheduleTime;
import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoStop;
import fr.outadev.android.timeo.TimeoStopSchedule;
import fr.outadev.twistoast.MainActivity;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.TwistoastDatabase;
import fr.outadev.twistoast.TwistoastDatabaseOpenHelper;
import fr.outadev.twistoast.Utils;

public class NextStopAlarmReceiver extends BroadcastReceiver {

	private static final int ALARM_TYPE = AlarmManager.ELAPSED_REALTIME_WAKEUP;
	private static final int ALARM_FREQUENCY = 60 * 1000;

	private Context context;

	@Override
	public void onReceive(final Context context, Intent intent) {
		this.context = context;

		(new AsyncTask<Void, Void, List<TimeoStopSchedule>>() {

			private TwistoastDatabase db;

			@Override
			protected void onPreExecute() {
				db = new TwistoastDatabase(TwistoastDatabaseOpenHelper.getInstance(context));
				Log.d(Utils.TAG, "checking stop schedules for notifications");
			}

			@Override
			protected List<TimeoStopSchedule> doInBackground(Void... params) {
				try {
					List<TimeoStop> stops = db.getWatchedStops();
					return TimeoRequestHandler.getMultipleSchedules(stops);
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(List<TimeoStopSchedule> stopSchedules) {
				if(stopSchedules != null) {

					// Look through each schedule
					for(TimeoStopSchedule schedule : stopSchedules) {

						// If there are stops scheduled for this bus
						if(schedule.getSchedules() != null && !schedule.getSchedules().isEmpty()) {
							Calendar busTime = ScheduleTime.getNextDateForTime(schedule.getSchedules().get(0).getTime());

							// THE BUS IS COMIIIING
							if(Calendar.getInstance().getTimeInMillis() + 2 * 60 * 1000 > busTime.getTimeInMillis()) {
								// Remove from database, and send a notification
								notifyForBusStop(schedule);
								db.stopWatchingStop(schedule.getStop());
								schedule.getStop().setWatched(false);

								Log.d(Utils.TAG, "less than two minutes till " + busTime.toString() + ": " + schedule.getStop());
							} else if(schedule.getStop().getLastETA() != -1) {
								// Check if there's more than five minutes of difference between the last estimation and the new
								// one. If that's the case, send the notification anyways; it may already be too late!

								// This is to work around the fact that we actually can't know if a bus has passed already,
								// we have to make assumptions instead.
								if(busTime.getTimeInMillis() - schedule.getStop().getLastETA() > 5 * 60 * 1000) {
									// Remove from database, and send a notification
									notifyForBusStop(schedule);
									db.stopWatchingStop(schedule.getStop());
									schedule.getStop().setWatched(false);

									Log.d(Utils.TAG, "last time we saw " + schedule.getStop() + " the bus was scheduled for " +
											schedule.getStop().getLastETA() + ", but now the ETA is "
											+ busTime.getTimeInMillis() + ", so we're notifying");
								}

							} else {
								db.updateWatchedStopETA(schedule.getStop(), busTime.getTimeInMillis());
							}
						}
					}
				}

				if(db.getWatchedStopsCount() == 0) {
					NextStopAlarmReceiver.disable(context.getApplicationContext());
				}
			}

		}).execute();
	}

	private void notifyForBusStop(TimeoStopSchedule schedule) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent notificationIntent = new Intent(context, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		// Get the data we need for the notification
		String stop = schedule.getStop().getName();
		String direction = schedule.getStop().getLine().getDirection().getName();
		String lineName = schedule.getStop().getLine().getName();
		String time = schedule.getSchedules().get(0).getTime();

		// Make a nice notification to inform the user of the bus's imminence
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_stat_notify_twistoast)
						.setContentTitle("Passage " + lineName + " imminent")
						.setContentText(stop + " vers " + direction)
						.setStyle(new NotificationCompat.InboxStyle()
								.addLine("Direction " + direction)
								.addLine("Arrêt " + stop)
								.setSummaryText("Prévu pour " + time))
						.setCategory(NotificationCompat.CATEGORY_MESSAGE)
						.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
						.setPriority(NotificationCompat.PRIORITY_MAX)
						.setContentIntent(contentIntent)
						.setAutoCancel(true)
						.setOnlyAlertOnce(true);

		notificationManager.notify(Integer.valueOf(schedule.getStop().getId()), builder.build());
	}

	public static void enable(Context context) {
		Log.d(Utils.TAG, "enabling " + TrafficAlertAlarmReceiver.class.getSimpleName());

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setInexactRepeating(ALARM_TYPE,
				SystemClock.elapsedRealtime() + 60 * 1000, ALARM_FREQUENCY, getBroadcast(context));
	}

	public static void disable(Context context) {
		Log.d(Utils.TAG, "disabling " + TrafficAlertAlarmReceiver.class.getSimpleName());

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(getBroadcast(context));
	}

	public static PendingIntent getBroadcast(Context context) {
		Intent intent = new Intent(context, NextStopAlarmReceiver.class);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

}
