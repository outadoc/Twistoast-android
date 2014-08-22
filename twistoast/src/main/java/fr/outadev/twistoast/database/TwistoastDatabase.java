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
import android.graphics.Color;

import java.util.ArrayList;

import fr.outadev.android.timeo.TimeoIDNameObject;
import fr.outadev.android.timeo.TimeoScheduleObject;

public class TwistoastDatabase {

	public TwistoastDatabase(Context context) {
		databaseOpenHelper = new TwistoastDatabaseOpenHelper(context);
	}

	public void addStopToDatabase(TimeoIDNameObject line, TimeoIDNameObject direction,
	                              TimeoIDNameObject stop) throws IllegalArgumentException, SQLiteConstraintException {
		if(line != null && direction != null && stop != null) {
			// when we want to add a stop, we add the line first, then the
			// direction
			addLineToDatabase(line);
			addDirectionToDatabase(line, direction);

			// then, open the database, and start enumerating when we'll need to
			// add
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("stop_id", stop.getId());
			values.put("line_id", line.getId());
			values.put("dir_id", direction.getId());
			values.put("stop_name", stop.getName());

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

	private void addLineToDatabase(TimeoIDNameObject line) {
		if(line != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("line_id", line.getId());
			values.put("line_name", line.getName());

			db.insert("twi_line", null, values);
			db.close();
		}
	}

	private void addDirectionToDatabase(TimeoIDNameObject line, TimeoIDNameObject direction) {
		if(line != null && direction != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("dir_id", direction.getId());
			values.put("line_id", line.getId());
			values.put("dir_name", direction.getName());

			db.insert("twi_direction", null, values);
			db.close();
		}
	}

	public ArrayList<TimeoScheduleObject> getAllStops() {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();

		// that's a nice query you got tthhhere
		Cursor results = db
				.rawQuery(
						"SELECT stop.stop_id, stop.stop_name, line.line_id, line.line_name, dir.dir_id, " +
								"dir.dir_name FROM twi_stop stop JOIN twi_direction dir USING(dir_id, " +
								"line_id) JOIN twi_line line USING(line_id) ORDER BY CAST(line.line_id AS INTEGER), " +
								"stop.stop_name, dir.dir_name",
						null);

		ArrayList<TimeoScheduleObject> stopsList = new ArrayList<TimeoScheduleObject>();

		// while there's a stop available
		while(results.moveToNext()) {
			// add it to the list
			stopsList.add(new TimeoScheduleObject(new TimeoIDNameObject(results.getString(results.getColumnIndex("line_id")),
					results.getString(results.getColumnIndex("line_name"))), new TimeoIDNameObject(results.getString(results
					.getColumnIndex("dir_id")), results.getString(results.getColumnIndex("dir_name"))),
					new TimeoIDNameObject(results.getString(results.getColumnIndex("stop_id")), results.getString(results
							.getColumnIndex("stop_name"))), null));
		}

		// close the cursor and the database
		results.close();
		db.close();

		return stopsList;
	}

	public TimeoScheduleObject getStopAtIndex(int index) {
		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		String indexStr = String.valueOf(index);
		TimeoScheduleObject stop = null;

		// that's a nice query you got tthhhere
		Cursor results = db
				.rawQuery(
						"SELECT stop.stop_id, stop.stop_name, line.line_id, line.line_name, dir.dir_id, " +
								"dir.dir_name FROM twi_stop stop JOIN twi_direction dir USING(dir_id, " +
								"line_id) JOIN twi_line line USING(line_id) ORDER BY CAST(line.line_id AS INTEGER), " +
								"stop.stop_name, dir.dir_name LIMIT ? OFFSET ?",
						new String[]{"1", indexStr});

		// while there's a stop available
		while(results.moveToNext()) {
			// add it to the list
			stop = new TimeoScheduleObject(new TimeoIDNameObject(results.getString(results.getColumnIndex("line_id")),
					results.getString(results.getColumnIndex("line_name"))), new TimeoIDNameObject(results.getString(results
					.getColumnIndex("dir_id")), results.getString(results.getColumnIndex("dir_name"))),
					new TimeoIDNameObject(results.getString(results.getColumnIndex("stop_id")), results.getString(results
							.getColumnIndex("stop_name"))), null);
		}

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

	public void deleteStop(TimeoScheduleObject stop) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		db.delete("twi_stop", "stop_id=? AND line_id=? AND dir_id=?", new String[]{stop.getStop().getId(),
				stop.getLine().getId(), stop.getDirection().getId()});

		db.close();
	}

	public static int getColorFromLineID(String line) {
		// every stop has a specific color: we can get it here
		String color;

		if(line.equalsIgnoreCase("TRAM")) {
			color = "#EB6909";
		} else if(line.equalsIgnoreCase("CEX")) {
			color = "#642175";
		} else if(line.equalsIgnoreCase("1")) {
			color = "#CC007B";
		} else if(line.equalsIgnoreCase("2")) {
			color = "#008BCF";
		} else if(line.equalsIgnoreCase("3")) {
			color = "#27A22D";
		} else if(line.equalsIgnoreCase("4")) {
			color = "#EC7497";
		} else if(line.equalsIgnoreCase("5")) {
			color = "#B97CAF";
		} else if(line.equalsIgnoreCase("6")) {
			color = "#8F87C7";
		} else if(line.equalsIgnoreCase("7")) {
			color = "#87BBC3";
		} else if(line.equalsIgnoreCase("8")) {
			color = "#BCCF2F";
		} else if(line.equalsIgnoreCase("9")) {
			color = "#DFAE00";
		} else if(line.equalsIgnoreCase("10")) {
			color = "#60BFE8";
		} else if(line.equalsIgnoreCase("11")) {
			color = "#BC7419";
		} else if(line.equalsIgnoreCase("14")) {
			color = "#FFDD00";
		} else if(line.equalsIgnoreCase("15")) {
			color = "#FECC00";
		} else if(line.equalsIgnoreCase("16")) {
			color = "#D8B083";
		} else if(line.equalsIgnoreCase("17")) {
			color = "#E585B1";
		} else if(line.equalsIgnoreCase("18")) {
			color = "#F9B200";
		} else if(line.equalsIgnoreCase("19")) {
			color = "#E3004F";
		} else if(line.equalsIgnoreCase("20")) {
			color = "#94A6B0";
		} else if(line.equalsIgnoreCase("21")) {
			color = "#9C917F";
		} else if(line.equalsIgnoreCase("22")) {
			color = "#28338A";
		} else if(line.equalsIgnoreCase("23")) {
			color = "#172055";
		} else if(line.equalsIgnoreCase("24")) {
			color = "#E85761";
		} else if(line.equalsIgnoreCase("25")) {
			color = "#A5A213";
		} else if(line.equalsIgnoreCase("26")) {
			color = "#03B4E6";
		} else if(line.equalsIgnoreCase("28")) {
			color = "#F6A924";
		} else if(line.equalsIgnoreCase("29")) {
			color = "#807CAE";
		} else if(line.equalsIgnoreCase("31")) {
			color = "#EB6C68";
		} else if(line.equalsIgnoreCase("32")) {
			color = "#8B2412";
		} else if(line.equalsIgnoreCase("33")) {
			color = "#FFDD05";
		} else if(line.equalsIgnoreCase("61")) {
			color = "#005478";
		} else if(line.equalsIgnoreCase("62")) {
			color = "#857AB3";
		} else if(line.equalsIgnoreCase("NUIT")) {
			color = "#23255F";
		} else {
			color = "#34495E";
		}

		return Color.parseColor(color);
	}

	private final TwistoastDatabaseOpenHelper databaseOpenHelper;

}