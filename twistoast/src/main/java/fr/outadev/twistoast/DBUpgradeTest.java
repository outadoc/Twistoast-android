/*
 * Twistoast - DBUpgradeTest
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

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import fr.outadev.android.timeo.KeolisRequestHandler;
import fr.outadev.android.timeo.model.TimeoLine;
import fr.outadev.android.timeo.model.TimeoStop;

/**
 * Created by outadoc on 28/08/14.
 */
public class DBUpgradeTest extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		(new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... voids) {
				KeolisRequestHandler handler = new KeolisRequestHandler();

				List<TimeoLine> lineList;
				List<TimeoStop> stopList = new ArrayList<TimeoStop>();

				try {
					lineList = handler.getLines();

					for(TimeoLine line : lineList) {
						stopList.addAll(handler.getStops(line));
					}
				} catch(Exception e) {
					e.printStackTrace();
				}

				SQLiteDatabase db = openOrCreateDatabase("twistoast_upgrade_v2.db", Context.MODE_PRIVATE, null);

				db.execSQL("DROP TABLE twi_v2_line");
				db.execSQL("DROP TABLE twi_v2_stop");

				db.execSQL("CREATE TABLE IF NOT EXISTS twi_v2_line (line_id TEXT PRIMARY KEY, line_color TEXT)");
				db.execSQL("CREATE TABLE IF NOT EXISTS twi_v2_stop (line_id TEXT, dir_id TEXT, stop_id TEXT, stop_ref TEXT, " +
						"PRIMARY KEY(line_id, dir_id, stop_id))");

				for(TimeoStop stop : stopList) {
					try {
						db.execSQL("INSERT INTO twi_v2_line (line_id, line_color) VALUES(?, ?)",
								new String[]{stop.getLine().getDetails().getId(), stop.getLine().getColor()});
					} catch(Exception e) {
						e.printStackTrace();
					}

					try {
						db.execSQL("INSERT INTO twi_v2_stop (line_id, dir_id, stop_id, stop_ref) VALUES(?, ?, ?, ?)",
								new String[]{stop.getLine().getDetails().getId(), stop.getLine().getDirection().getId(),
										stop.getId(), stop.getReference()});
					} catch(Exception e) {
						e.printStackTrace();
					}

				}

				db.close();

				return null;
			}

		}).execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

}
