/*
 * Twistoast - TrafficAlertTask.kt
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

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log

import fr.outadev.android.transport.timeo.TimeoRequestHandler
import fr.outadev.android.transport.timeo.TimeoTrafficAlert
import fr.outadev.twistoast.ConfigurationManager
import fr.outadev.twistoast.R

/**
 * Created by outadoc on 2016-03-07.
 */
class TrafficAlertTask(private val mContext: Context) : AsyncTask<Void, Void, TimeoTrafficAlert>() {
    private val config: ConfigurationManager
    private var notificationManager: NotificationManager? = null
    private val requestHandler: TimeoRequestHandler

    private var lastTrafficId: Int = 0

    init {
        config = ConfigurationManager(mContext)
        requestHandler = TimeoRequestHandler()
    }

    override fun doInBackground(vararg params: Void): TimeoTrafficAlert? {
        try {
            return requestHandler.globalTrafficAlert
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    override fun onPreExecute() {
        notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lastTrafficId = config.lastTrafficNotificationId
        Log.d(TAG, "checking traffic alert")
    }

    override fun onPostExecute(trafficAlert: TimeoTrafficAlert?) {
        if (trafficAlert != null) {
            Log.d(TAG, "found traffic alert #" + trafficAlert.id)

            if (lastTrafficId != trafficAlert.id) {
                val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trafficAlert.url))
                val contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0)

                val mBuilder = NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_traffic_cone_white)
                        .setContentTitle(mContext.getString(R.string.notifs_traffic_title))
                        .setContentText(trafficAlert.label)
                        .setStyle(NotificationCompat.BigTextStyle()
                                .bigText(trafficAlert.label))
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setColor(ContextCompat.getColor(mContext, R.color.traffic_alert_background))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setDefaults(NotificationSettings.getNotificationDefaults(config.trafficNotificationsVibrate, config.trafficNotificationsRing))

                notificationManager!!.notify(trafficAlert.id, mBuilder.build())
                config.lastTrafficNotificationId = trafficAlert.id

                return
            }
        }

        Log.d(TAG, "checked traffic: nothing new!")
    }

    companion object {
        val TAG: String = TrafficAlertTask::class.java.simpleName
    }

}
