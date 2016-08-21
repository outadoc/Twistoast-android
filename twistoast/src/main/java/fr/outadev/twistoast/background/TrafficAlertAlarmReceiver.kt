/*
 * Twistoast - TrafficAlertAlarmReceiver
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
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log

/**
 * A broadcast receiver called at regular intervals to check
 * if there are traffic problems and the user should be notified.
 */
class TrafficAlertAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        TrafficAlertTask(context).execute()
    }

    companion object {
        val TAG: String = TrafficAlertAlarmReceiver::class.java.simpleName

        const val ALARM_TYPE = AlarmManager.ELAPSED_REALTIME
        const val ALARM_FREQUENCY = AlarmManager.INTERVAL_HALF_HOUR

        /**
         * Enables the regular checks performed every X minutes by this receiver.
         * They should be disabled once not needed anymore, as they can be battery and network hungry.

         * @param context a context
         */
        internal fun enable(context: Context) {
            Log.d(TAG, "enabling " + TrafficAlertAlarmReceiver::class.java.simpleName)

            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.setInexactRepeating(ALARM_TYPE,
                    SystemClock.elapsedRealtime() + 10 * 1000, ALARM_FREQUENCY, getBroadcast(context))
        }

        /**
         * Disables the regular checks performed every X minutes by this receiver.

         * @param context a context
         */
        internal fun disable(context: Context) {
            Log.d(TAG, "disabling " + TrafficAlertAlarmReceiver::class.java.simpleName)

            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.cancel(getBroadcast(context))
        }

        /**
         * Returns the PendingIntent that will be called by the alarm every X minutes.

         * @param context a context
         * *
         * @return the PendingIntent corresponding to this class
         */
        private fun getBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, TrafficAlertAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }
    }


}
