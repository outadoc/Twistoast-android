/*
 * Twistoast - TrafficAlertJobService
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

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import fr.outadev.twistoast.utils.Utils;

/**
 * A more efficient version of TrafficAlertAlarmReceiver, only compatible with Lollipop+.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TrafficAlertJobService extends JobService {

    public static final int TRAFFIC_ALERT_JOB_ID = 1;
    private static final long JOB_FREQUENCY = 30 * 60 * 1000;

    /**
     * Enables the regular checks performed every X minutes by this receiver.
     * They should be disabled once not needed anymore, as they can be battery and network hungry.
     *
     * @param context a context
     */
    static void enable(Context context) {
        Log.d(Utils.TAG, "enabling " + TrafficAlertTask.class.getSimpleName());

        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(new JobInfo.Builder(TRAFFIC_ALERT_JOB_ID, new ComponentName(context, TrafficAlertJobService.class))
                            .setPeriodic(JOB_FREQUENCY)
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setPersisted(true)
                            .build());
    }

    /**
     * Disables the regular checks performed every X minutes by this receiver.
     *
     * @param context a context
     */
    static void disable(Context context) {
        Log.d(Utils.TAG, "disabling " + TrafficAlertTask.class.getSimpleName());

        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(TRAFFIC_ALERT_JOB_ID);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        (new TrafficAlertTask(this)).execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
