/*
 * Twistoast - TimeoStopReferenceUpdater
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

package fr.outadev.twistoast

import android.content.Context
import android.util.Log
import fr.outadev.android.transport.timeo.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * Fetches and updates all the references of the stops saved in our database.
 * Useful since they periodically change, and this class should allow the user
 * to update his list of stops without having to delete/re-add them.
 */
class TimeoStopReferenceUpdater(context: Context = ApplicationTwistoast.instance) {

    private val database: Database
    private val requestHandler: TimeoRequestHandler

    init {
        database = Database(DatabaseOpenHelper(context))
        requestHandler = TimeoRequestHandler()
    }

    /**
     * Updates all the references of the bus stops in the database.
     * Read the classe's Javadoc for some more context.
     *
     * @param progressListener a progress listener that will be notified of the progress, line by line.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    @Throws(XmlPullParserException::class, IOException::class, TimeoException::class)
    fun updateAllStopReferences(stops: List<TimeoStop>, progressListener: IProgressListener?) {
        var lastLine: TimeoLine? = null

        progressListener?.onProgress(0, stops.size)

        stops.filter { stop -> stop.isOutdated && stop.line != lastLine }.forEachIndexed { i, stop ->
            //update the progress
            Log.d(TAG, "updating stops for line " + stop.line)
            progressListener?.onProgress(i, stops.size)

            //get the stops for the current line
            lastLine = stop.line
            val newStops = requestHandler.getStops(lastLine!!)

            //update all the stops we received.
            //obviously, only the ones existing in the database will be updated.
            newStops.forEach { database.updateStopReference(it) }
        }
    }

    companion object {
        private val TAG = TimeoStopReferenceUpdater::class.java.simpleName
    }

}
