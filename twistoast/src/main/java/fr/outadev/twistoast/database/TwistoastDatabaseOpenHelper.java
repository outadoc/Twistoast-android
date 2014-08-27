/*
 * Twistoast - TwistoastDatabaseOpenHelper
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

package fr.outadev.twistoast.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import fr.outadev.android.timeo.KeolisRequestHandler;

/**
 * Opens, creates and manages database versions.
 *
 * @author outadoc
 */
public class TwistoastDatabaseOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "twistoast.db";

	private static final String LINES_TABLE_CREATE =
			"CREATE TABLE twi_line(" +
					"line_id TEXT, " +
					"line_name TEXT, " +
					"line_color TEXT, " +
					"network_code INTEGER DEFAULT " + KeolisRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY (line_id, network_code));";

	private static final String DIRECTIONS_TABLE_CREATE =
			"CREATE TABLE twi_direction(" +
					"dir_id TEXT, " +
					"line_id TEXT, " +
					"dir_name TEXT, " +
					"network_code INTEGER DEFAULT " + KeolisRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY(dir_id, line_id, network_code), " +
					"FOREIGN KEY(line_id, network_code) REFERENCES twi_line(line_id, network_code));";

	private static final String STOPS_TABLE_CREATE =
			"CREATE TABLE twi_stop(" +
					"stop_id INTEGER, " +
					"line_id TEXT, " +
					"dir_id TEXT, " +
					"stop_name TEXT, " +
					"stop_ref TEXT, " +
					"network_code INTEGER DEFAULT " + KeolisRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY(stop_id, line_id, dir_id, network_code), " +
					"FOREIGN KEY(dir_id, line_id, network_code) REFERENCES twi_direction(dir_id, line_id, network_code));";

	TwistoastDatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LINES_TABLE_CREATE);
		db.execSQL(DIRECTIONS_TABLE_CREATE);
		db.execSQL(STOPS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}