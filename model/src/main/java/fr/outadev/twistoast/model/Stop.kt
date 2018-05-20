/*
 * Twistoast - Stop.kt
 * Copyright (C) 2013-2018 Baptiste Candellier
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

/*
 * Twistoast - Stop.ktopyright (C) 2013-2018 Baptiste Candellier
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

package fr.outadev.twistoast.model

import org.joda.time.DateTime

/**
 * A bus stop.
 *
 * @author outadoc
 */
data class Stop(val id: Int, val name: String, var line: Line, var reference: String? = null) {

    /**
     * Checks if this stop is outdated and its reference needs to be updated.
     * @return true if it needs to be updated, otherwise false
     */
    var isOutdated: Boolean = false

    /**
     * Checks if notifications are currently active for this bus stop.
     */
    var isWatched: Boolean = false

    /**
     * Gets the last estimated time of arrival for this bus stop.
     * @return a timestamp of an approximation of the arrival of the next bus
     */
    var lastETA: DateTime? = null

    override fun toString(): String = name
}
