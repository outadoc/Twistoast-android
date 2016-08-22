/*
 * Twistoast - IProgressListener
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

package fr.outadev.android.transport.timeo

/**
 * Defines a progress listener.
 * Current process and maximum progress will be updated at will.
 *
 * @author outadoc
 */
interface IProgressListener {

    /**
     * Updates the current progress.

     * @param current current progress, can't be greater than total
     * @param total   maximum progress
     */
    fun onProgress(current: Int, total: Int)

}
