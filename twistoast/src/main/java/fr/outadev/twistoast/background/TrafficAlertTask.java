package fr.outadev.twistoast.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import fr.outadev.android.transport.timeo.TimeoRequestHandler;
import fr.outadev.android.transport.timeo.TimeoTrafficAlert;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.utils.Utils;

/**
 * Created by outadoc on 2016-03-07.
 */
public class TrafficAlertTask extends AsyncTask<Void, Void, TimeoTrafficAlert> {

    private int mLastTrafficId;

    private final Context mContext;
    private final String mPrefix;

    private SharedPreferences mPreferences;
    private NotificationManager mNotificationManager;

    public TrafficAlertTask(Context context, String prefix) {
        mContext = context;
        mPrefix = prefix;
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
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mLastTrafficId = mPreferences.getInt("last_traffic_notif_id", -1);

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
                                .setContentIntent(contentIntent)
                                .setAutoCancel(true)
                                .setOnlyAlertOnce(true)
                                .setDefaults(NotificationSettings.getNotificationDefaults(mContext, mPrefix));

                mNotificationManager.notify(trafficAlert.getId(), mBuilder.build());
                mPreferences.edit().putInt("last_traffic_notif_id", trafficAlert.getId()).apply();
                return;
            }
        }

        Log.d(Utils.TAG, "checked traffic: nothing new!");
    }

}
