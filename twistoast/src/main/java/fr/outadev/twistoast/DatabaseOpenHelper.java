/*
 * Twistoast - DatabaseOpenHelper
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

package fr.outadev.twistoast;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.outadev.android.transport.timeo.TimeoRequestHandler;
import fr.outadev.twistoast.utils.Utils;

/**
 * Opens, creates and manages database versions.
 *
 * @author outadoc
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "twistoast.db";
    private static final String DATABASE_V2_UPGRADE_NAME = "db_upgrade_v2.db";
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
    private static DatabaseOpenHelper sInstance;
    private Context mContext;


    private DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static DatabaseOpenHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseOpenHelper(context);
        }

        return sInstance;
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

        switch (oldVersion) {
            case 1:
                upgradeToV2(db);
            case 2:
                upgradeToV3(db);
        }

        Log.i(Utils.TAG, "successful database upgrade!");
    }

    /**
     * Upgrade the database to version 2.
     *
     * @param db the database to upgrade
     */
    private void upgradeToV2(SQLiteDatabase db) {
        SQLiteDatabase db_upgrade = null;

        try {
            db_upgrade = getV2UpgradeDatabase();
            Cursor linesCur = db_upgrade.rawQuery("SELECT * FROM twi_v2_line", null);

            //upgrade tables by adding the required columns
            db.execSQL("ALTER TABLE twi_line ADD COLUMN line_color TEXT");
            db.execSQL("ALTER TABLE twi_stop ADD COLUMN stop_ref TEXT DEFAULT NULL");

            db.execSQL("ALTER TABLE twi_line ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler
                    .DEFAULT_NETWORK_CODE);
            db.execSQL("ALTER TABLE twi_direction ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler
                    .DEFAULT_NETWORK_CODE);
            db.execSQL("ALTER TABLE twi_stop ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler
                    .DEFAULT_NETWORK_CODE);


            //set the colour of the lines using the old database
            while (linesCur.moveToNext()) {
                db.execSQL("UPDATE twi_line SET line_color = ? WHERE line_id = ?",
                        new Object[]{linesCur.getString(linesCur.getColumnIndex("line_color")),
                                linesCur.getString(linesCur.getColumnIndex("line_id"))});
            }

            linesCur.close();

            //move the old tables to old_*
            db.execSQL("ALTER TABLE twi_stop RENAME TO old_twi_stop");
            db.execSQL("ALTER TABLE twi_direction RENAME TO old_twi_direction");
            db.execSQL("ALTER TABLE twi_line RENAME TO old_twi_line");

            //create the new tables
            onCreate(db);

            //copy the data from the old tables to the new ones
            db.execSQL("INSERT INTO twi_line SELECT * FROM old_twi_line");
            db.execSQL("INSERT INTO twi_direction SELECT * FROM old_twi_direction");
            db.execSQL("INSERT INTO twi_stop SELECT * FROM old_twi_stop");

            //delete the old tables
            db.execSQL("DROP TABLE old_twi_stop");
            db.execSQL("DROP TABLE old_twi_direction");
            db.execSQL("DROP TABLE old_twi_line");
        } catch (Exception e) {
            e.printStackTrace();

            deleteAllData(db);
            onCreate(db);
        } finally {
            if (db_upgrade != null) {
                db_upgrade.close();
            }
        }
    }

    /**
     * Get the v2 upgrade database.
     *
     * @return a database containing the info necessary for a v1 -> v2 upgrade
     * @throws IOException if we couldn't copy the database to the databases folder
     */
    private SQLiteDatabase getV2UpgradeDatabase() throws IOException {
        // Check if the database exists before copying
        boolean initialiseDatabase = (mContext.getDatabasePath(DATABASE_V2_UPGRADE_NAME)).exists();

        if (!initialiseDatabase) {
            // Open the .db file in your assets directory
            InputStream is = mContext.getAssets().open(DATABASE_V2_UPGRADE_NAME);

            // Copy the database into the destination
            OutputStream os = new FileOutputStream(mContext.getDatabasePath(DATABASE_V2_UPGRADE_NAME));
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            os.flush();

            os.close();
            is.close();
        }

        return mContext.openOrCreateDatabase(DATABASE_V2_UPGRADE_NAME, Context.MODE_PRIVATE, null);
    }

    private void upgradeToV3(SQLiteDatabase db) {
        try {
            db.execSQL(NOTIFICATIONS_TABLE_CREATE);
        } catch (Exception e) {
            e.printStackTrace();

            deleteAllData(db);
            onCreate(db);
        }
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
        } catch (Exception ignored) {
        }
    }
}