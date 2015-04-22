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

package fr.outadev.twistoast;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import fr.outadev.android.timeo.TimeoRequestHandler;

/**
 * Opens, creates and manages database versions.
 *
 * @author outadoc
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

	private static DatabaseOpenHelper instance;
	private Context context;

	private static final int DATABASE_VERSION = 3;

	private static final String DATABASE_NAME = "twistoast.db";

	private static final String LINES_TABLE_CREATE =
			"CREATE TABLE twi_line(" +
					"line_id TEXT, " +
					"line_name TEXT, " +
					"line_color TEXT, " +
					"network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY (line_id, network_code))";

	private static final String DIRECTIONS_TABLE_CREATE =
			"CREATE TABLE twi_direction(" +
					"dir_id TEXT, " +
					"line_id TEXT, " +
					"dir_name TEXT, " +
					"network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY(dir_id, line_id, network_code), " +
					"FOREIGN KEY(line_id, network_code) REFERENCES twi_line(line_id, network_code))";

	private static final String STOPS_TABLE_CREATE =
			"CREATE TABLE twi_stop(" +
					"stop_id INTEGER, " +
					"line_id TEXT, " +
					"dir_id TEXT, " +
					"stop_name TEXT, " +
					"stop_ref TEXT DEFAULT NULL, " +
					"network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY(stop_id, line_id, dir_id, network_code), " +
					"FOREIGN KEY(dir_id, line_id, network_code) REFERENCES twi_direction(dir_id, line_id, network_code))";

	private static final String NOTIFICATIONS_TABLE_CREATE =
			"CREATE TABLE twi_notification(" +
					"stop_id INTEGER," +
					"line_id TEXT," +
					"dir_id TEXT," +
					"network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"notif_creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
					"notif_active INTEGER DEFAULT 1, " +
					"notif_last_estim INTEGER DEFAULT -1, " +
					"PRIMARY KEY(stop_id, line_id, dir_id, network_code, notif_active, notif_creation_time), " +
					"FOREIGN KEY(stop_id, line_id, dir_id, network_code) " +
					"REFERENCES twi_stop(stop_id, line_id, dir_id, network_code))";


	private DatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	public static DatabaseOpenHelper getInstance(Context context) {
		if(instance == null) {
			instance = new DatabaseOpenHelper(context);
		}

		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LINES_TABLE_CREATE);
		db.execSQL(DIRECTIONS_TABLE_CREATE);
		db.execSQL(STOPS_TABLE_CREATE);
		db.execSQL(NOTIFICATIONS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(Utils.TAG, "upgrading database to v" + newVersion + ", was v" + oldVersion);

		Log.i(Utils.TAG, "successful database upgrade!");
	}

	/**
	 * Deletes all the tables in the database.
	 *
	 * @param db the database to clean up
	 */
	private void deleteAllData(SQLiteDatabase db) {
		try {
			db.execSQL("DROP TABLE twi_stop");
			db.execSQL("DROP TABLE twi_direction");
			db.execSQL("DROP TABLE twi_line");
			db.execSQL("DROP TABLE twi_notification");
		} catch(Exception ignored) {
		}
	}
}