/*
 * Twistoast - BootReceiver
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import fr.outadev.twistoast.ConfigurationManager
import fr.outadev.twistoast.Database
import fr.outadev.twistoast.DatabaseOpenHelper

/**
 * The boot receiver of the application. It enables the notification receivers if needed.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Turn the notifications back on if necessary
            val db = Database(DatabaseOpenHelper.getInstance(context))
            val config = ConfigurationManager(context)

            if (db.watchedStopsCount > 0) {
                BackgroundTasksManager.enableStopAlarmJob(context.applicationContext)
            }

            if (config.trafficNotificationsEnabled) {
                BackgroundTasksManager.enableTrafficAlertJob(context.applicationContext)
            }

        }
    }

}
