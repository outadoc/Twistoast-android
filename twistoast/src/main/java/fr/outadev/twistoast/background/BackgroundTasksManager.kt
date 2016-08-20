/*
 * Twistoast - BackgroundTasksManager
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

import android.content.Context

/**
 * Manages the enabled/disable state of the background jobs ran by the application.
 * This proxy will run the correct, most efficient job depending on the version of Android running.
 */
object BackgroundTasksManager {

    /**
     * Enables the periodic background traffic alert job/receiver.
     * @param context
     */
    fun enableTrafficAlertJob(context: Context) = TrafficAlertAlarmReceiver.enable(context)

    /**
     * Disables the periodic background traffic alert job/receiver.
     * @param context
     */
    fun disableTrafficAlertJob(context: Context) = TrafficAlertAlarmReceiver.disable(context)

    /**
     * Enables the periodic stop arrival time alarm receiver.
     * @param context
     */
    fun enableStopAlarmJob(context: Context) = NextStopAlarmReceiver.enable(context)

    /**
     * Disables the periodic stop arrival time alarm receiver.
     * @param context
     */
    fun disableStopAlarmJob(context: Context) = NextStopAlarmReceiver.disable(context)

}
