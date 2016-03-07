package fr.outadev.twistoast.background;

import android.content.Context;
import android.os.Build;

/**
 * Manages the enabled/disable state of the background jobs ran by the application.
 * This proxy will run the correct, most efficient job depending on the version of Android running.
 */
public class BackgroundTasksManager {

    /**
     * Enables the periodic background traffic alert job/receiver.
     * @param context
     */
    public static void enableTrafficAlertJob(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TrafficAlertJobService.enable(context);
        } else {
            TrafficAlertAlarmReceiver.enable(context);
        }
    }

    /**
     * Disables the periodic background traffic alert job/receiver.
     * @param context
     */
    public static void disableTrafficAlertJob(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TrafficAlertJobService.disable(context);
        } else {
            TrafficAlertAlarmReceiver.disable(context);
        }
    }

    /**
     * Enables the periodic stop arrival time alarm receiver.
     * @param context
     */
    public static void enableStopAlarmJob(Context context) {
        NextStopAlarmReceiver.enable(context);
    }

    /**
     * Disables the periodic stop arrival time alarm receiver.
     * @param context
     */
    public static void disableStopAlarmJob(Context context) {
        NextStopAlarmReceiver.disable(context);
    }

}
