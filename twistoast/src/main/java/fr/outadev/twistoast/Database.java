/*
 * Twistoast - TwistoastDatabase
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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import fr.outadev.android.timeo.TimeoIDNameObject;
import fr.outadev.android.timeo.TimeoLine;
import fr.outadev.android.timeo.TimeoStop;

/**
 * Database management class.
 *
 * @author outadoc
 */
public class Database {

	private final SQLiteOpenHelper databaseOpenHelper;

	public Database(SQLiteOpenHelper openHelper) {
		databaseOpenHelper = openHelper;
	}

	/**
	 * Adds a bus stop to the database.
	 *
	 * @param stop the bus stop to add
	 * @throws IllegalArgumentException  if the stop is not valid
	 * @throws SQLiteConstraintException if a constraint failed
	 */
	public void addStopToDatabase(TimeoStop stop) throws IllegalArgumentException, SQLiteConstraintException {
		if(stop != null) {
			// when we want to add a stop, we add the line first, then the
			// direction
			addLineToDatabase(stop.getLine());

			// then, open the database, and start enumerating when we'll need to
			// add
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("stop_id", stop.getId());
			values.put("line_id", stop.getLine().getId());
			values.put("dir_id", stop.getLine().getDirection().getId());
			values.put("stop_name", stop.getName());
			values.put("stop_ref", stop.getReference());
			values.put("network_code", stop.getLine().getNetworkCode());

			try {
				// insert the stop with the specified columns
				db.insertOrThrow("twi_stop", null, values);
			} finally {
				// we want to close the database afterwards either way
				db.close();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Adds a bus line to the database.
	 *
	 * @param line the bus line to add
	 */
	private void addLineToDatabase(TimeoLine line) {
		if(line != null && line.getDetails() != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("line_id", line.getId());
			values.put("line_name", line.getName());
			values.put("line_color", line.getColor());
			values.put("network_code", line.getNetworkCode());

			db.insert("twi_line", null, values);
			db.close();

			addDirectionToDatabase(line);
		}
	}

	/**
	 * Adds a direction to the database.
	 *
	 * @param line the bus line whose direction to add
	 */
	private void addDirectionToDatabase(TimeoLine line) {
		if(line != null && line.getDirection() != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("dir_id", line.getDirection().getId());
			values.put("line_id", line.getId());
			values.put("dir_name", line.getDirection().getName());
			values.put("network_code", line.getNetworkCode());

			db.insert("twi_direction", null, values);
			db.close();
		}
	}

	/**
	 * Gets all stops currently stored in the database.
	 *
	 * @return a list of all the stops
	 */
	public List<TimeoStop> getAllStops() {
		// Clean notification flags that have timed out so they don't interfere
		cleanOutdatedWatchedStops();

		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();

		Cursor results = db
				.rawQuery(
						"SELECT stop.stop_id, stop.stop_name, stop.stop_ref, line.line_id, line.line_name, line.line_color, " +
								"dir.dir_id, dir.dir_name, line.network_code, ifnull(notif_active, 0) as notif " +
								"FROM twi_stop stop " +
								"INNER JOIN twi_direction dir USING(dir_id, line_id, network_code) " +
								"INNER JOIN twi_line line USING(line_id, network_code) " +
								"LEFT JOIN twi_notification notif ON (notif.stop_id = stop.stop_id " +
								"AND notif.line_id = line.line_id AND notif.dir_id = dir.dir_id " +
								"AND notif.network_code = line.network_code AND notif.notif_active = 1) " +
								"ORDER BY line.network_code, CAST(line.line_id AS INTEGER), stop.stop_name, dir.dir_name",
						null);

		ArrayList<TimeoStop> stopsList = new ArrayList<>();

		// while there's a stop available
		while(results.moveToNext()) {
			TimeoLine line = new TimeoLine(
					new TimeoIDNameObject(
							results.getString(results.getColumnIndex("line_id")),
							results.getString(results.getColumnIndex("line_name"))),
					new TimeoIDNameObject(
							results.getString(results.getColumnIndex("dir_id")),
							results.getString(results.getColumnIndex("dir_name"))),
					results.getString(results.getColumnIndex("line_color")),
					results.getInt(results.getColumnIndex("network_code")));

			TimeoStop stop = new TimeoStop(
					results.getString(results.getColumnIndex("stop_id")),
					results.getString(results.getColumnIndex("stop_name")),
					results.getString(results.getColumnIndex("stop_ref")),
					line,
					(results.getInt(results.getColumnIndex("notif")) == 1));

			// add it to the list
			stopsList.add(stop);
		}

		// close the cursor and the database
		results.close();
		db.close();

		return stopsList;
	}

	/**
	 * Gets a bus stop at a specific index. Useful for Pebble, for example.
	 *
	 * @param index the index of the stop in the database, sorted by line id, stop name, and direction name
	 * @return the corresponding stop object
	 */
	public TimeoStop getStopAtIndex(int index) {
		List<TimeoStop> stopsList = getAllStops();

		if(stopsList != null && stopsList.size() >= index + 1) {
			return stopsList.get(index);
		}

		return null;
	}

	/**
	 * Gets a bus stop with the corresponding primary key.
	 *
	 * @param stopId the ID of the stop to get
	 * @param lineId the ID of the line of the stop to get
	 * @param dirId the ID of the direction of the stop to get
	 * @return the corresponding stop object
	 */
	public TimeoStop getStop(String stopId, String lineId, String dirId, int networkCode) {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		TimeoStop stop = null;

		Cursor results = db.rawQuery(
				"SELECT * FROM twi_stop " +
						"JOIN twi_line USING (line_id, network_code) " +
						"JOIN twi_direction USING (line_id, dir_id, network_code) " +
						"WHERE stop_id = ? AND line_id = ? AND dir_id = ? AND network_code = ?",
				new String[]{stopId, lineId, dirId, String.valueOf(networkCode)});

		if(results.moveToFirst()) {
			TimeoLine line = new TimeoLine(
					new TimeoIDNameObject(
							results.getString(results.getColumnIndex("line_id")),
							results.getString(results.getColumnIndex("line_name"))),
					new TimeoIDNameObject(
							results.getString(results.getColumnIndex("dir_id")),
							results.getString(results.getColumnIndex("dir_name"))),
					results.getString(results.getColumnIndex("line_color")),
					results.getInt(results.getColumnIndex("network_code")));

			stop = new TimeoStop(
					results.getString(results.getColumnIndex("stop_id")),
					results.getString(results.getColumnIndex("stop_name")),
					results.getString(results.getColumnIndex("stop_ref")),
					line,
					(results.getInt(results.getColumnIndex("notif")) == 1));
		}

		// close the cursor and the database
		results.close();
		db.close();

		return stop;
	}

	/**
	 * Gets the number of stops in the database.
	 *
	 * @return the number of bus stops
	 */
	public int getStopsCount() {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();

		// that's a nice query you got tthhhere
		Cursor results = db.rawQuery("SELECT stop_id FROM twi_stop", null);

		int count = results.getCount();

		// close the cursor and the database
		results.close();
		db.close();

		return count;
	}

	public int getNetworksCount() {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();

		Cursor results = db.rawQuery("SELECT COUNT(*), network_code FROM twi_stop GROUP BY (network_code)", null);
		results.moveToFirst();

		int count = results.getCount();

		results.close();
		db.close();

		return count;
	}

	/**
	 * Deletes a bus stop from the database.
	 *
	 * @param stop the bus stop to delete
	 */
	public void deleteStop(TimeoStop stop) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		db.delete("twi_stop", "stop_id = ? AND line_id = ? AND dir_id = ? AND stop_ref = ? AND network_code = ?", new String[]{
				stop.getId(),
				stop.getLine().getId(),
				stop.getLine().getDirection().getId(),
				stop.getReference(),
				stop.getLine().getNetworkCode() + ""
		});

		db.close();
	}

	/**
	 * Update the reference of a stop in the database.
	 *
	 * @param stop the bus stop whose reference is to be updated
	 */
	public void updateStopReference(TimeoStop stop) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		ContentValues updateClause = new ContentValues();
		updateClause.put("stop_ref", stop.getReference());

		db.update("twi_stop", updateClause, "stop_id = ? AND line_id = ? AND dir_id = ? AND network_code = ?", new String[]{
				stop.getId(),
				stop.getLine().getId(),
				stop.getLine().getDirection().getId(),
				stop.getLine().getNetworkCode() + ""
		});

		db.close();
	}

	/**
	 * Removes outdated watched stops from the database.
	 * If a stop notification request was added more than three hours ago, it will be deleted.
	 */
	private void cleanOutdatedWatchedStops() {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		ContentValues updateClause = new ContentValues();
		updateClause.put("notif_active", 0);

		db.update("twi_notification", updateClause, "date('now','-3 hours') > notif_creation_time", null);
		db.close();
	}

	/**
	 * Fetches the list of stops that we are currently watching (that is to say, we wanted to be notified when they're incoming).
	 *
	 * @return a list containing the stops to process
	 */
	public List<TimeoStop> getWatchedStops() {
		// Clean notification flags that have timed out so they don't interfere
		cleanOutdatedWatchedStops();

		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();

		Cursor results = db
				.rawQuery(
						"SELECT stop.stop_id, stop.stop_name, stop.stop_ref, line.line_id, line.line_name, " +
								"line.line_color, dir.dir_id, dir.dir_name, line.network_code, notif.notif_last_estim " +
								"FROM twi_stop stop " +
								"INNER JOIN twi_direction dir USING(dir_id, line_id, network_code) " +
								"INNER JOIN twi_line line USING(line_id, network_code) " +
								"INNER JOIN twi_notification notif USING (stop_id, line_id, dir_id, network_code) " +
								"WHERE notif_active = 1 " +
								"ORDER BY line.network_code, CAST(line.line_id AS INTEGER), stop.stop_name, dir.dir_name",
						null);

		ArrayList<TimeoStop> stopsList = new ArrayList<>();

		// while there's a stop available
		while(results.moveToNext()) {
			TimeoLine line = new TimeoLine(
					new TimeoIDNameObject(
							results.getString(results.getColumnIndex("line_id")),
							results.getString(results.getColumnIndex("line_name"))),
					new TimeoIDNameObject(
							results.getString(results.getColumnIndex("dir_id")),
							results.getString(results.getColumnIndex("dir_name"))),
					results.getString(results.getColumnIndex("line_color")),
					results.getInt(results.getColumnIndex("network_code")));

			TimeoStop stop = new TimeoStop(
					results.getString(results.getColumnIndex("stop_id")),
					results.getString(results.getColumnIndex("stop_name")),
					results.getString(results.getColumnIndex("stop_ref")),
					line,
					true,
					results.getLong(results.getColumnIndex("notif_last_estim")));

			// add it to the list
			stopsList.add(stop);
		}

		// close the cursor and the database
		results.close();
		db.close();

		return stopsList;
	}

	/**
	 * Registers a stop to be watched for notifications.
	 *
	 * @param stop the bus stop to add to the list
	 */
	public void addToWatchedStops(TimeoStop stop) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put("stop_id", stop.getId());
		values.put("line_id", stop.getLine().getId());
		values.put("dir_id", stop.getLine().getDirection().getId());
		values.put("network_code", stop.getLine().getNetworkCode());

		db.insert("twi_notification", null, values);
	}

	/**
	 * Unregisters a stop from the list of watched stops.
	 * No notifications should be sent for this stop anymore, until it's been added back in.
	 *
	 * @param stop the bus stop that we should stop watching
	 */
	public void stopWatchingStop(TimeoStop stop) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		ContentValues updateClause = new ContentValues();
		updateClause.put("notif_active", 0);

		db.update("twi_notification", updateClause,
				"stop_id = ? AND line_id = ? AND dir_id = ? AND network_code = ?", new String[]{
						stop.getId(),
						stop.getLine().getId(),
						stop.getLine().getDirection().getId(),
						stop.getLine().getNetworkCode() + ""
				});

		db.close();
	}

	/**
	 * Updated the last time of arrival returned by the API for this bus.
	 *
	 * @param stop    the bus stop we want to update
	 * @param lastETA a UNIX timestamp for the last know ETA for this bus
	 */
	public void updateWatchedStopETA(TimeoStop stop, long lastETA) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		ContentValues updateClause = new ContentValues();
		updateClause.put("notif_last_estim", lastETA);

		db.update("twi_notification", updateClause,
				"stop_id = ? AND line_id = ? AND dir_id = ? AND network_code = ? AND notif_active = 1", new String[]{
						stop.getId(),
						stop.getLine().getId(),
						stop.getLine().getDirection().getId(),
						stop.getLine().getNetworkCode() + ""
				});

		db.close();
	}

	/**
	 * Counts the number of bus stops we are currently watching.
	 *
	 * @return the number of watched stops in the database
	 */
	public int getWatchedStopsCount() {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();

		Cursor results = db.rawQuery("SELECT COUNT(*) as nb_watched FROM twi_notification WHERE notif_active = 1", null);
		results.moveToFirst();

		int count = results.getInt(results.getColumnIndex("nb_watched"));

		results.close();
		db.close();

		return count;
	}

}
