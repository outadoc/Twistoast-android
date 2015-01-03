/*
 * Twistoast - TimeoStopReferenceUpdater
 * Copyright (C) 2013-2014  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import fr.outadev.android.timeo.ProgressListener;
import fr.outadev.android.timeo.TimeoException;
import fr.outadev.android.timeo.TimeoLine;
import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoStop;

/**
 * Fetches and updates all the references of the stops saved in our database.
 * Useful since they periodically change, and this class should allow the user
 * to update his list of stops without having to delete/re-add them.
 */
public class TimeoStopReferenceUpdater {

	private TwistoastDatabase db;

	public TimeoStopReferenceUpdater(Context context) {
		this.db = new TwistoastDatabase(TwistoastDatabaseOpenHelper.getInstance(context));
	}

	/**
	 * Updates all the references of the bus stops in the database.
	 * Read the classe's Javadoc for some more context.
	 *
	 * @param progressListener a progress listener that will be notified of the progress, line by line.
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws fr.outadev.android.timeo.TimeoException
	 */
	public void updateAllStopReferences(ProgressListener progressListener) throws XmlPullParserException, IOException,
			TimeoException {
		List<TimeoStop> stopList = db.getAllStops();
		TimeoLine lastLine = null;

		progressListener.onProgress(0, stopList.size());

		int i = 0;

		for(TimeoStop stop : stopList) {
			//we only want to load it for each line, so we skip any additional stops that we already processed
			if(stop.getLine().equals(lastLine)) {
				continue;
			}

			//update the progress
			Log.d(Utils.TAG, "updating stops for line " + stop.getLine());
			progressListener.onProgress(i, stopList.size());

			//get the stops for the current line
			lastLine = stop.getLine();
			List<TimeoStop> newStops = TimeoRequestHandler.getStops(lastLine);

			//update all the stops we received.
			//obviously, only the ones existing in the database will be updated.
			for(TimeoStop newStop : newStops) {
				db.updateStopReference(newStop);
			}

			i++;
		}

	}

}
