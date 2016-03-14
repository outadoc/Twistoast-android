/*
 * Twistoast - TrafficAlertTask
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

package fr.outadev.twistoast.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import fr.outadev.android.transport.timeo.TimeoRequestHandler;
import fr.outadev.android.transport.timeo.TimeoTrafficAlert;
import fr.outadev.twistoast.ConfigurationManager;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.utils.Utils;

/**
 * Created by outadoc on 2016-03-07.
 */
public class TrafficAlertTask extends AsyncTask<Void, Void, TimeoTrafficAlert> {

    private int mLastTrafficId;

    private final Context mContext;
    private final ConfigurationManager mConfig;

    private NotificationManager mNotificationManager;

    public TrafficAlertTask(Context context) {
        mContext = context;
        mConfig = new ConfigurationManager(mContext);
    }

    @Override
    protected TimeoTrafficAlert doInBackground(Void... params) {
        try {
            return TimeoRequestHandler.getGlobalTrafficAlert(mContext.getString(R.string.url_pre_home_info));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mLastTrafficId = mConfig.getLastTrafficNotificationId();
        Log.d(Utils.TAG, "checking traffic alert");
    }

    @Override
    protected void onPostExecute(TimeoTrafficAlert trafficAlert) {
        if (trafficAlert != null) {
            Log.d(Utils.TAG, "found traffic alert #" + trafficAlert.getId());

            if (mLastTrafficId != trafficAlert.getId()) {
                Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trafficAlert.getUrl()));
                PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(mContext)
                                .setSmallIcon(R.drawable.ic_traffic_cone_white)
                                .setContentTitle(mContext.getString(R.string.notifs_traffic_title))
                                .setContentText(trafficAlert.getLabel())
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(trafficAlert.getLabel()))
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setColor(mContext.getResources().getColor(R.color.traffic_alert_background))
                                .setContentIntent(contentIntent)
                                .setAutoCancel(true)
                                .setOnlyAlertOnce(true)
                                .setDefaults(NotificationSettings.getNotificationDefaults(mContext,
                                        mConfig.getTrafficNotificationsVibrate(), mConfig.getTrafficNotificationsRing()));

                mNotificationManager.notify(trafficAlert.getId(), mBuilder.build());
                mConfig.setLastTrafficNotificationId(trafficAlert.getId());

                return;
            }
        }

        Log.d(Utils.TAG, "checked traffic: nothing new!");
    }

}
