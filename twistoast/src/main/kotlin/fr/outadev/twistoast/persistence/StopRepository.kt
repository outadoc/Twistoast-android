/*
 * Twistoast - StopRepository.kt
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

package fr.outadev.twistoast.persistence

import fr.outadev.android.transport.timeo.TimeoStop
import org.joda.time.DateTime

class StopRepository : IStopRepository {

    private val dbHandler = StopDao(DatabaseOpenHelper())

    override val stopsCount = dbHandler.stopsCount

    override val networksCount = dbHandler.networksCount

    override val watchedStops = dbHandler.watchedStops

    override val watchedStopsCount = dbHandler.watchedStopsCount

    override fun addStopToDatabase(stop: TimeoStop?) = dbHandler.addStopToDatabase(stop)

    override fun getAllStops(sortCriteria: IStopRepository.SortBy) = dbHandler.getAllStops(sortCriteria)

    override fun getStopAtIndex(index: Int) = dbHandler.getStopAtIndex(index)

    override fun getStop(stopId: String, lineId: String, dirId: String, networkCode: Int): TimeoStop? =
            dbHandler.getStop(stopId, lineId, dirId, networkCode)

    override fun deleteStop(stop: TimeoStop) = dbHandler.deleteStop(stop)

    override fun updateStopReference(stop: TimeoStop) = dbHandler.updateStopReference(stop)

    override fun addToWatchedStops(stop: TimeoStop) = dbHandler.addToWatchedStops(stop)

    override fun stopWatchingStop(stop: TimeoStop) = dbHandler.stopWatchingStop(stop)

    override fun updateWatchedStopETA(stop: TimeoStop, lastETA: DateTime) =
            dbHandler.updateWatchedStopETA(stop, lastETA)

}