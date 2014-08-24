/*
 * Twistoast - TimeoTest
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

package fr.outadev.android.timeo.test;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import fr.outadev.android.timeo.KeolisRequestHandler;
import fr.outadev.android.timeo.model.TimeoException;
import fr.outadev.android.timeo.model.TimeoLine;
import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.android.timeo.model.TimeoStopNotReturnedException;
import fr.outadev.android.timeo.model.TimeoStopSchedule;

public class TimeoTest {

	private KeolisRequestHandler handler;

	public TimeoTest() {
		handler = new KeolisRequestHandler();
		test1();
	}

	private void test1() {
		(new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... voids) {
				try {

					//lines
					List<TimeoLine> lines = handler.getLines();

					for(TimeoLine line : lines) {
						System.out.println(line.getDetails());
					}

					//stops
					List<TimeoStop> stops = handler.getStops(lines.get(5));

					for(TimeoStop stop : stops) {
						System.out.println(stop);
					}

					//multiple schedules
					List<TimeoStopSchedule> schedules = handler.getMultipleSchedules(stops);

					for(TimeoStopSchedule schedule : schedules) {
						System.out.println(schedule);
					}

					//single schedules)àç
					TimeoStopSchedule schedule = handler.getSingleSchedule(stops.get(0));
					System.out.println(schedule);

				} catch(XmlPullParserException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				} catch(TimeoStopNotReturnedException e) {
					e.printStackTrace();
				} catch(TimeoException e) {
					e.printStackTrace();
				}
				return null;
			}

		}).execute();
	}

}
