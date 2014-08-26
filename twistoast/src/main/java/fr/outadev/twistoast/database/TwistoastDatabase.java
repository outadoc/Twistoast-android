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

package fr.outadev.twistoast.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import fr.outadev.android.timeo.model.TimeoIDNameObject;
import fr.outadev.android.timeo.model.TimeoLine;
import fr.outadev.android.timeo.model.TimeoStop;

public class TwistoastDatabase {

	private final TwistoastDatabaseOpenHelper databaseOpenHelper;

	public TwistoastDatabase(Context context) {
		databaseOpenHelper = new TwistoastDatabaseOpenHelper(context);
	}

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
			values.put("line_id", stop.getLine().getDetails().getId());
			values.put("dir_id", stop.getLine().getDirection().getId());
			values.put("stop_name", stop.getName());
			values.put("stop_ref", stop.getReference());

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

	private void addLineToDatabase(TimeoLine line) {
		if(line != null && line.getDetails() != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("line_id", line.getDetails().getId());
			values.put("line_name", line.getDetails().getName());
			values.put("line_color", line.getColor());

			db.insert("twi_line", null, values);
			db.close();

			addDirectionToDatabase(line);
		}
	}

	private void addDirectionToDatabase(TimeoLine line) {
		if(line != null && line.getDirection() != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("dir_id", line.getDirection().getId());
			values.put("line_id", line.getDetails().getId());
			values.put("dir_name", line.getDirection().getName());

			db.insert("twi_direction", null, values);
			db.close();
		}
	}

	public ArrayList<TimeoStop> getAllStops() {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();

		// that's a nice query you got tthhhere
		Cursor results = db
				.rawQuery(
						"SELECT stop.stop_id, stop.stop_name, stop.stop_ref, line.line_id, line.line_name, " +
								"line.line_color, dir.dir_id, dir.dir_name FROM twi_stop stop JOIN twi_direction dir USING" +
								"(dir_id, line_id) JOIN twi_line line USING(line_id) ORDER BY CAST(line.line_id AS INTEGER), " +
								"stop.stop_name, dir.dir_name",
						null);

		ArrayList<TimeoStop> stopsList = new ArrayList<TimeoStop>();

		// while there's a stop available
		while(results.moveToNext()) {
			TimeoLine line = new TimeoLine(
					new TimeoIDNameObject(
							results.getString(results.getColumnIndex("line_id")),
							results.getString(results.getColumnIndex("line_name"))),
					new TimeoIDNameObject(
							results.getString(results.getColumnIndex("dir_id")),
							results.getString(results.getColumnIndex("dir_name"))),
					results.getString(results.getColumnIndex("line_color")));

			TimeoStop stop = new TimeoStop(
					results.getString(results.getColumnIndex("stop_id")),
					results.getString(results.getColumnIndex("stop_name")),
					results.getString(results.getColumnIndex("stop_ref")),
					line);

			// add it to the list
			stopsList.add(stop);
		}

		// close the cursor and the database
		results.close();
		db.close();

		return stopsList;
	}

	public TimeoStop getStopAtIndex(int index) {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		String indexStr = String.valueOf(index);

		// that's a nice query you got tthhhere
		Cursor results = db
				.rawQuery(
						"SELECT stop.stop_id, stop.stop_name, stop.stop_ref, line.line_id, line.line_name, line.line_color, " +
								"dir.dir_id, dir.dir_name FROM twi_stop stop JOIN twi_direction dir USING(dir_id, " +
								"line_id) JOIN twi_line line USING(line_id) ORDER BY CAST(line.line_id AS INTEGER), " +
								"stop.stop_name, dir.dir_name LIMIT ? OFFSET ?",
						new String[]{"1", indexStr});

		results.moveToFirst();

		TimeoLine line = new TimeoLine(
				new TimeoIDNameObject(
						results.getString(results.getColumnIndex("line_id")),
						results.getString(results.getColumnIndex("line_name"))),
				new TimeoIDNameObject(
						results.getString(results.getColumnIndex("dir_id")),
						results.getString(results.getColumnIndex("dir_name"))),
				results.getString(results.getColumnIndex("line_color")));

		TimeoStop stop = new TimeoStop(
				results.getString(results.getColumnIndex("stop_id")),
				results.getString(results.getColumnIndex("stop_name")),
				results.getString(results.getColumnIndex("stop_ref")),
				line);

		// close the cursor and the database
		results.close();
		db.close();

		return stop;
	}

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

	public void deleteStop(TimeoStop stop) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		db.delete("twi_stop", "stop_id=? AND line_id=? AND dir_id=?", new String[]{
				stop.getId(),
				stop.getLine().getDetails().getId(),
				stop.getLine().getDirection().getId()
		});

		db.close();
	}

}
