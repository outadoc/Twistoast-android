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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.outadev.android.timeo.TimeoRequestHandler;

/**
 * Opens, creates and manages database versions.
 *
 * @author outadoc
 */
public class TwistoastDatabaseOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "twistoast.db";

	private Context context;

	private static final String LINES_TABLE_CREATE =
			"CREATE TABLE twi_line(" +
					"line_id TEXT, " +
					"line_name TEXT, " +
					"line_color TEXT, " +
					"network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY (line_id, network_code));";

	private static final String DIRECTIONS_TABLE_CREATE =
			"CREATE TABLE twi_direction(" +
					"dir_id TEXT, " +
					"line_id TEXT, " +
					"dir_name TEXT, " +
					"network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY(dir_id, line_id, network_code), " +
					"FOREIGN KEY(line_id, network_code) REFERENCES twi_line(line_id, network_code));";

	private static final String STOPS_TABLE_CREATE =
			"CREATE TABLE twi_stop(" +
					"stop_id INTEGER, " +
					"line_id TEXT, " +
					"dir_id TEXT, " +
					"stop_name TEXT, " +
					"stop_ref TEXT, " +
					"network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
					"PRIMARY KEY(stop_id, line_id, dir_id, network_code), " +
					"FOREIGN KEY(dir_id, line_id, network_code) REFERENCES twi_direction(dir_id, line_id, network_code));";

	public TwistoastDatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LINES_TABLE_CREATE);
		db.execSQL(DIRECTIONS_TABLE_CREATE);
		db.execSQL(STOPS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(newVersion) {
			case 2:
				upgradeToV2(db);
		}
	}

	private void upgradeToV2(SQLiteDatabase db) {

		try {
			SQLiteDatabase db_upgrade = getV2UpgradeDatabase();

			Cursor linesCur = db_upgrade.rawQuery("SELECT * FROM twi_v2_line", null);
			Cursor stopsCur = db_upgrade.rawQuery("SELECT * FROM twi_v2_stop", null);

			//upgrade tables
			db.execSQL("ALTER TABLE twi_line ADD COLUMN line_color TEXT");
			db.execSQL("ALTER TABLE twi_stop ADD COLUMN stop_ref TEXT");

			db.execSQL("ALTER TABLE twi_line ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler
					.DEFAULT_NETWORK_CODE);
			db.execSQL("ALTER TABLE twi_direction ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler
					.DEFAULT_NETWORK_CODE);
			db.execSQL("ALTER TABLE twi_stop ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler
					.DEFAULT_NETWORK_CODE);


			// while there's a stop available
			while(linesCur.moveToNext()) {
				db.execSQL("UPDATE twi_line SET line_color = ? WHERE line_id = ?",
						new Object[]{linesCur.getString(linesCur.getColumnIndex("line_color")),
								linesCur.getString(linesCur.getColumnIndex("line_id"))});
			}

			linesCur.close();

			while(stopsCur.moveToNext()) {
				db.execSQL("UPDATE twi_stop SET stop_ref = ? WHERE line_id = ? AND dir_id = ? AND stop_id = ?",
						new Object[]{stopsCur.getString(stopsCur.getColumnIndex("stop_ref")),
								stopsCur.getString(stopsCur.getColumnIndex("line_id")),
								stopsCur.getString(stopsCur.getColumnIndex("dir_id")),
								stopsCur.getString(stopsCur.getColumnIndex("stop_id"))});
			}

			stopsCur.close();
			db_upgrade.close();
		} catch(Exception e) {
			e.printStackTrace();

			deleteAllData(db);
			onCreate(db);
		}
	}

	private SQLiteDatabase getV2UpgradeDatabase() throws IOException {
		final String DB_DESTINATION = "/data/data/fr.outadev.twistoast/databases/db_upgrade_v2.db";

		// Check if the database exists before copying
		boolean initialiseDatabase = (new File(DB_DESTINATION)).exists();

		if(!initialiseDatabase) {
			// Open the .db file in your assets directory
			InputStream is = context.getAssets().open("db_upgrade_v2.db");

			// Copy the database into the destination
			OutputStream os = new FileOutputStream(DB_DESTINATION);
			byte[] buffer = new byte[1024];
			int length;

			while((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}

			os.flush();

			os.close();
			is.close();
		}

		return context.openOrCreateDatabase("db_upgrade_v2.db", Context.MODE_PRIVATE, null);
	}

	private void deleteAllData(SQLiteDatabase db) {
		db.execSQL("DROP TABLE twi_stop");
		db.execSQL("DROP TABLE twi_direction");
		db.execSQL("DROP TABLE twi_line");
	}
}