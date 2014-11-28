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

package fr.outadev.android.timeo;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import fr.outadev.android.timeo.model.TimeoException;
import fr.outadev.android.timeo.model.TimeoLine;
import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.twistoast.database.TwistoastDatabase;

/**
 * Created by outadoc on 27/11/14.
 */
public class TimeoStopReferenceUpdater {

	private TwistoastDatabase db;
	private Context context;

	public TimeoStopReferenceUpdater(Context context) {
		this.context = context;
		this.db = new TwistoastDatabase(context);
	}

	public void updateAllStopReferences() throws XmlPullParserException, IOException, TimeoException {
		List<TimeoStop> stopList = db.getAllStops();
		TimeoLine lastLine = null;

		db.beginTransaction();

		for(TimeoStop stop : stopList) {
			if(stop.getLine() == lastLine) {
				continue;
			}

			lastLine = stop.getLine();
			List<TimeoStop> newStops = TimeoRequestHandler.getStops(lastLine);

			for(TimeoStop newStop : newStops) {
				db.updateStopReference(stop, newStop.getReference());
			}
		}

		db.commit();
	}

}
