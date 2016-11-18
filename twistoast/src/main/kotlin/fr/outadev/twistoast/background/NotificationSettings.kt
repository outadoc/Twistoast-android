/*
 * Twistoast - NotificationSettings.kt
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

import android.support.v4.app.NotificationCompat

/**
 * Created by outadoc on 2016-03-07.
 */
object NotificationSettings {

    /**
     * Gets suitable notification defaults for the notifications of this receiver.
     * Use them with NotificationCompat.Builder.setDefaults().
     *
     * @return an integer to pass to the builder
     */
    fun getNotificationDefaults(vibrate: Boolean, ring: Boolean): Int {
        var defaults = NotificationCompat.DEFAULT_LIGHTS

        if (vibrate) {
            defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
        }

        if (ring) {
            defaults = defaults or NotificationCompat.DEFAULT_SOUND
        }

        return defaults
    }

}
