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

package fr.outadev.twistoast

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import fr.outadev.android.transport.timeo.TimeoRequestHandler
import java.io.FileOutputStream
import java.io.IOException

/**
 * Opens, creates and manages database versions.

 * @author outadoc
 */
class DatabaseOpenHelper private constructor(private val context: Context) : SQLiteOpenHelper(context, DatabaseOpenHelper.DATABASE_NAME, null, DatabaseOpenHelper.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(LINES_TABLE_CREATE)
        db.execSQL(DIRECTIONS_TABLE_CREATE)
        db.execSQL(STOPS_TABLE_CREATE)
        db.execSQL(NOTIFICATIONS_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "upgrading database to v$newVersion, was v$oldVersion")

        when (oldVersion) {
            1 -> {
                upgradeToV2(db)
                upgradeToV3(db)
            }
            2 -> upgradeToV3(db)
        }

        Log.i(TAG, "successful database upgrade!")
    }

    /**
     * Upgrade the database to version 2.

     * @param db the database to upgrade
     */
    private fun upgradeToV2(db: SQLiteDatabase) {
        var upgradeDb: SQLiteDatabase? = null

        try {
            upgradeDb = v2UpgradeDatabase
            val linesCur = upgradeDb.rawQuery("SELECT * FROM twi_v2_line", null)

            //upgrade tables by adding the required columns
            db.execSQL("ALTER TABLE twi_line ADD COLUMN line_color TEXT")
            db.execSQL("ALTER TABLE twi_stop ADD COLUMN stop_ref TEXT DEFAULT NULL")

            db.execSQL("ALTER TABLE twi_line ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE)
            db.execSQL("ALTER TABLE twi_direction ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE)
            db.execSQL("ALTER TABLE twi_stop ADD COLUMN network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE)


            //set the colour of the lines using the old database
            while (linesCur.moveToNext()) {
                db.execSQL("UPDATE twi_line SET line_color = ? WHERE line_id = ?",
                        arrayOf<Any>(linesCur.getString(linesCur.getColumnIndex("line_color")), linesCur.getString(linesCur.getColumnIndex("line_id"))))
            }

            linesCur.close()

            //move the old tables to old_*
            db.execSQL("ALTER TABLE twi_stop RENAME TO old_twi_stop")
            db.execSQL("ALTER TABLE twi_direction RENAME TO old_twi_direction")
            db.execSQL("ALTER TABLE twi_line RENAME TO old_twi_line")

            //create the new tables
            onCreate(db)

            //copy the data from the old tables to the new ones
            db.execSQL("INSERT INTO twi_line SELECT * FROM old_twi_line")
            db.execSQL("INSERT INTO twi_direction SELECT * FROM old_twi_direction")
            db.execSQL("INSERT INTO twi_stop SELECT * FROM old_twi_stop")

            //delete the old tables
            db.execSQL("DROP TABLE old_twi_stop")
            db.execSQL("DROP TABLE old_twi_direction")
            db.execSQL("DROP TABLE old_twi_line")
        } catch (e: Exception) {
            e.printStackTrace()

            deleteAllData(db)
            onCreate(db)
        } finally {
            if (upgradeDb != null) {
                upgradeDb.close()
            }
        }
    }

    /**
     * Get the v2 upgrade database.

     * @return a database containing the info necessary for a v1 -> v2 upgrade
     * *
     * @throws IOException if we couldn't copy the database to the databases folder
     */
    private // Check if the database exists before copying
            // Open the .db file in your assets directory
            // Copy the database into the destination
    val v2UpgradeDatabase: SQLiteDatabase
        @Throws(IOException::class)
        get() {
            val initialiseDatabase = context.getDatabasePath(DATABASE_V2_UPGRADE_NAME).exists()

            if (!initialiseDatabase) {
                val source = context.assets.open(DATABASE_V2_UPGRADE_NAME)
                val out = FileOutputStream(context.getDatabasePath(DATABASE_V2_UPGRADE_NAME))

                source.copyTo(out, 1024)

                out.flush()
                out.close()
                source.close()
            }

            return context.openOrCreateDatabase(DATABASE_V2_UPGRADE_NAME, Context.MODE_PRIVATE, null)
        }

    private fun upgradeToV3(db: SQLiteDatabase) {
        try {
            db.execSQL(NOTIFICATIONS_TABLE_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()

            deleteAllData(db)
            onCreate(db)
        }

    }

    /**
     * Deletes all the tables in the database.

     * @param db the database to clean up
     */
    private fun deleteAllData(db: SQLiteDatabase) {
        try {
            db.execSQL("DROP TABLE twi_stop")
            db.execSQL("DROP TABLE twi_direction")
            db.execSQL("DROP TABLE twi_line")
            db.execSQL("DROP TABLE twi_notification")
        } catch (ignored: Exception) {
        }

    }

    companion object {

        private val TAG = DatabaseOpenHelper::class.java.simpleName

        private val DATABASE_VERSION = 3
        private val DATABASE_NAME = "twistoast.db"

        private val DATABASE_V2_UPGRADE_NAME = "db_upgrade_v2.db"

        private val LINES_TABLE_CREATE =
                "CREATE TABLE twi_line(" +
                        "line_id TEXT, " +
                        "line_name TEXT, " +
                        "line_color TEXT, " +
                        "network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
                        "PRIMARY KEY (line_id, network_code))"

        private val DIRECTIONS_TABLE_CREATE =
                "CREATE TABLE twi_direction(" +
                        "dir_id TEXT, " +
                        "line_id TEXT, " +
                        "dir_name TEXT, " +
                        "network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
                        "PRIMARY KEY(dir_id, line_id, network_code), " +
                        "FOREIGN KEY(line_id, network_code) REFERENCES twi_line(line_id, network_code))"

        private val STOPS_TABLE_CREATE =
                "CREATE TABLE twi_stop(" +
                        "stop_id INTEGER, " +
                        "line_id TEXT, " +
                        "dir_id TEXT, " +
                        "stop_name TEXT, " +
                        "stop_ref TEXT DEFAULT NULL, " +
                        "network_code INTEGER DEFAULT " + TimeoRequestHandler.DEFAULT_NETWORK_CODE + ", " +
                        "PRIMARY KEY(stop_id, line_id, dir_id, network_code), " +
                        "FOREIGN KEY(dir_id, line_id, network_code) REFERENCES twi_direction(dir_id, line_id, network_code))"

        private val NOTIFICATIONS_TABLE_CREATE =
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
                        "REFERENCES twi_stop(stop_id, line_id, dir_id, network_code))"

        private var instance: DatabaseOpenHelper? = null

        fun getInstance(context: Context): DatabaseOpenHelper {
            if (instance == null) {
                instance = DatabaseOpenHelper(context)
            }

            return instance!!
        }
    }
}