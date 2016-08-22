/*
 * Twistoast - NextStopAlarmReceiver
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

package fr.outadev.twistoast.background

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.util.Log
import fr.outadev.android.transport.timeo.TimeoRequestHandler
import fr.outadev.android.transport.timeo.TimeoStop
import fr.outadev.android.transport.timeo.TimeoStopSchedule
import fr.outadev.twistoast.*
import org.jetbrains.anko.doAsync
import org.joda.time.DateTime

/**
 * A broadcast receiver called at regular intervals to check
 * if watched buses are incoming and the user should be notified.
 */
class NextStopAlarmReceiver : BroadcastReceiver() {
    private var context: Context? = null
    private var requestHandler: TimeoRequestHandler? = null

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        requestHandler = TimeoRequestHandler()

        var stopsToCheck: List<TimeoStop>? = null
        val database: Database = Database(DatabaseOpenHelper(context))

        Log.d(TAG, "checking stop schedules for notifications")

        doAsync {
            try {
                stopsToCheck = database.watchedStops
                val stopSchedules = requestHandler!!.getMultipleSchedules(stopsToCheck!!)

                // Look through each schedule
                stopSchedules.filter { it.schedules.isNotEmpty() }.forEach {
                    schedule ->
                    // If there are stops scheduled for this bus
                    val busTime = schedule.schedules[0].scheduleTime
                    updateStopTimeNotification(schedule)

                    // THE BUS IS COMIIIING
                    if (busTime.isBefore(DateTime.now().plus(ALARM_TIME_THRESHOLD_MS.toLong()))) {
                        // Remove from database, and send a notification
                        notifyForIncomingBus(schedule)
                        database.stopWatchingStop(schedule.stop)
                        schedule.stop.isWatched = false

                        Log.d(TAG, "less than two minutes till " + busTime.toString() + ": " + schedule.stop)
                    } else if (schedule.stop.lastETA != null) {
                        // Check if there's more than five minutes of difference between the last estimation and the new
                        // one. If that's the case, send the notification anyways; it may already be too late!

                        // This is to work around the fact that we actually can't know if a bus has passed already,
                        // we have to make assumptions instead; if a bus is announced for 3 minutes, and then for 10
                        // minutes the next time we check, it most likely has passed.

                        if (busTime.isBefore(schedule.stop.lastETA?.plus(5 * 60 * 1000.toLong()))) {
                            // Remove from database, and send a notification
                            notifyForIncomingBus(schedule)
                            database.stopWatchingStop(schedule.stop)
                            schedule.stop.isWatched = false

                            Log.d(TAG, "last time we saw " + schedule.stop + " the bus was scheduled for " +
                                    schedule.stop.lastETA + ", but now the ETA is " + busTime + ", so we're " +
                                    "notifying")
                        }
                    } else {
                        database.updateWatchedStopETA(schedule.stop, busTime)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

                // A network error occurred, or something ;-;
                if (stopsToCheck != null) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    for (stop in stopsToCheck!!) {
                        notificationManager.cancel(Integer.valueOf(stop.id)!!)
                    }
                }

                notifyNetworkError()
            }

            if (database.watchedStopsCount == 0) {
                NextStopAlarmReceiver.disable(context.applicationContext)
            }
        }
    }

    /**
     * Sends a notification to the user informing them that their bus is incoming.

     * @param schedule the schedule to notify about
     */
    private fun notifyForIncomingBus(schedule: TimeoStopSchedule) {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val config = ConfigurationManager(context!!)

        val notificationIntent = Intent(context, ActivityMain::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        // Get the data we need for the notification
        val stop = schedule.stop.name
        val direction = schedule.stop.line.direction.name
        val lineName = schedule.stop.line.name
        val time = TimeFormatter.formatTime(context!!, schedule.schedules[0].scheduleTime)

        // Make a nice notification to inform the user of the bus's imminence
        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_directions_bus_white)
                .setContentTitle(context!!.getString(R.string.notif_watched_content_title, lineName))
                .setContentText(context!!.getString(R.string.notif_watched_content_text, stop, direction))
                .setStyle(NotificationCompat.InboxStyle()
                        .addLine(context!!.getString(R.string.notif_watched_line_stop, stop))
                        .addLine(context!!.getString(R.string.notif_watched_line_direction, direction))
                        .setSummaryText(context!!.getString(R.string.notif_watched_summary_text, time)))
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColor(context!!.resources.getColor(R.color.icon_color))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationSettings.getNotificationDefaults(context!!, config.watchNotificationsVibrate, config.watchNotificationsRing))

        notificationManager.notify(Integer.valueOf(schedule.stop.id)!!, builder.build())

        // We want the rest of the application to know that this stop is not being watched anymore
        if (watchedStopStateListener != null) {
            watchedStopStateListener!!.onStopWatchingStateChanged(schedule.stop, false)
        }
    }

    /**
     * Sends a notification to the user and keeps it updated with the latest bus schedules.

     * @param schedule the bus schedule that will be included in the notification
     */
    private fun updateStopTimeNotification(schedule: TimeoStopSchedule) {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, ActivityMain::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        // Get the data we need for the notification
        val stop = schedule.stop.name
        val direction = schedule.stop.line.direction.name
        val lineName = schedule.stop.line.name

        val inboxStyle = NotificationCompat.InboxStyle()
                .setSummaryText(context!!.getString(R.string.notif_watched_content_text, lineName, direction))
                .setBigContentTitle(context!!.getString(R.string.stop_name, stop))

        for (singleSchedule in schedule.schedules) {
            inboxStyle.addLine(TimeFormatter.formatTime(context!!, singleSchedule.scheduleTime) + " - " + singleSchedule.direction)
        }

        // Make a nice notification to inform the user of the bus's imminence
        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_directions_bus_white)
                .setContentTitle(context!!.getString(R.string.notif_ongoing_content_title, stop, lineName))
                .setContentText(context!!.getString(R.string.notif_ongoing_content_text, TimeFormatter.formatTime(context!!, schedule.schedules[0].scheduleTime)))
                .setStyle(inboxStyle)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(context!!.resources.getColor(R.color.icon_color))
                .setContentIntent(contentIntent)
                .setOngoing(true)

        notificationManager.notify(Integer.valueOf(schedule.stop.id)!!, builder.build())
    }

    /**
     * Updates the schedule notification to the user, informing him that there's something wrong with the network.
     */
    private fun notifyNetworkError() {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, ActivityMain::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        // Make a nice notification to inform the user of an error
        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_directions_bus_white)
                .setContentTitle(context!!.getString(R.string.notif_error_content_title))
                .setContentText(context!!.getString(R.string.notif_error_content_text))
                .setColor(context!!.resources.getColor(R.color.icon_color))
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent).setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID_ERROR, builder.build())
    }

    companion object {

        private val TAG = NextStopAlarmReceiver::class.java.simpleName

        // If the bus is coming in less than ALARM_TIME_THRESHOLD_MS milliseconds, send a notification.
        const val ALARM_TIME_THRESHOLD_MS = 90 * 1000

        const val ALARM_FREQUENCY = 60 * 1000
        const val ALARM_TYPE = AlarmManager.ELAPSED_REALTIME_WAKEUP

        const val NOTIFICATION_ID_ERROR = 42

        private var watchedStopStateListener: IWatchedStopChangeListener? = null

        /**
         * Enables the regular checks performed every minute by this receiver.
         * They should be disabled once not needed anymore, as they can be battery and network hungry.

         * @param context a context
         */
        internal fun enable(context: Context) {
            Log.d(TAG, "enabling " + NextStopAlarmReceiver::class.java.simpleName)

            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.setInexactRepeating(ALARM_TYPE,
                    SystemClock.elapsedRealtime() + 1000, ALARM_FREQUENCY.toLong(), getBroadcast(context))
        }

        /**
         * Disables the regular checks performed every minute by this receiver.

         * @param context a context
         */
        internal fun disable(context: Context) {
            Log.d(TAG, "disabling " + NextStopAlarmReceiver::class.java.simpleName)

            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.cancel(getBroadcast(context))
        }

        /**
         * Returns the PendingIntent that will be called by the alarm every minute.

         * @param context a context
         * *
         * @return the PendingIntent corresponding to this class
         */
        fun getBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NextStopAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        fun setWatchedStopDismissalListener(watchedStopStateListener: IWatchedStopChangeListener) {
            NextStopAlarmReceiver.watchedStopStateListener = watchedStopStateListener
        }
    }

}
