package fr.outadev.twistoast;

import java.util.ArrayList;

import fr.outadev.twistoast.timeo.TimeoIDNameObject;
import fr.outadev.twistoast.timeo.TimeoScheduleObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

public class TwistoastDatabase {

	public TwistoastDatabase(Context context) {
		databaseOpenHelper = new TwistoastOpenHelper(context);
	}

	public DBStatus addStopToDatabase(TimeoIDNameObject line,
			TimeoIDNameObject direction, TimeoIDNameObject stop) {
		if(line != null && direction != null && stop != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

			addLineToDatabase(line);
			addDirectionToDatabase(line, direction);

			// select all lines that have this timeo id
			Cursor linesCursor = db
					.rawQuery(
							"SELECT line_id FROM lines WHERE UPPER(line_timeoID)=UPPER(?)",
							new String[] { line.getId() });
			linesCursor.moveToFirst();

			// select all the directions that have this timeo id and correspond
			// to this line
			Cursor directionsCursor = db
					.rawQuery(
							"SELECT direction_id FROM directions WHERE UPPER(direction_timeoID)=UPPER(?) AND UPPER(line_id)=UPPER(?)",
							new String[] {
									direction.getId(),
									linesCursor.getString(linesCursor
											.getColumnIndex("line_id")) });
			directionsCursor.moveToFirst();

			// and select all the stops that have this timeo id, this line and
			// this direction
			Cursor stopsCursor = db
					.rawQuery(
							"SELECT stop_id FROM stops WHERE UPPER(stop_timeoID)=UPPER(?) AND UPPER(line_id)=UPPER(?) AND UPPER(direction_id)=UPPER(?)",
							new String[] {
									stop.getId(),
									linesCursor.getString(linesCursor
											.getColumnIndex("line_id")),
									directionsCursor.getString(directionsCursor
											.getColumnIndex("direction_id")) });
			stopsCursor.moveToFirst();

			// if the stop we're trying to add doesn't exist yet
			if(stopsCursor.getCount() == 0) {
				ContentValues values = new ContentValues();

				values.put("stop_timeoID", stop.getId());
				values.put("stop_name", stop.getName());
				values.put("line_id", linesCursor.getString(linesCursor
						.getColumnIndex("line_id")));
				values.put("direction_id", directionsCursor
						.getString(directionsCursor
								.getColumnIndex("direction_id")));

				// insert the stop with the specified columns
				db.insert("stops", null, values);

				// close everything
				linesCursor.close();
				directionsCursor.close();
				stopsCursor.close();

				db.close();
				return DBStatus.SUCCESS;
			} else {
				// if the stop already exists, close everything anyway
				linesCursor.close();
				directionsCursor.close();
				stopsCursor.close();

				db.close();
				return DBStatus.ERROR_DUPLICATE;
			}
		} else {
			// something is null
			return DBStatus.ERROR_NOT_ENOUGH_PARAMETERS;
		}
	}

	private void addLineToDatabase(TimeoIDNameObject line) {
		if(line != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

			// check if the line already exists
			Cursor linesCursor = db
					.rawQuery(
							"SELECT line_id FROM lines WHERE UPPER(line_timeoID)=UPPER(?)",
							new String[] { line.getId() });
			linesCursor.moveToFirst();

			// if it doesn't, add it
			if(linesCursor.getCount() == 0) {
				ContentValues values = new ContentValues();

				values.put("line_timeoID", line.getId());
				values.put("line_name", line.getName());

				db.insert("lines", null, values);
			}

			linesCursor.close();
		}
	}

	private void addDirectionToDatabase(TimeoIDNameObject line,
			TimeoIDNameObject direction) {
		if(line != null && direction != null) {
			SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

			// get the line (it should exist)
			Cursor linesCursor = db
					.rawQuery(
							"SELECT line_id FROM lines WHERE UPPER(line_timeoID)=UPPER(?)",
							new String[] { line.getId() });
			linesCursor.moveToFirst();

			// check if the direction already exists
			Cursor directionsCursor = db
					.rawQuery(
							"SELECT direction_id FROM directions WHERE UPPER(direction_timeoID)=UPPER(?) AND UPPER(line_id)=UPPER(?)",
							new String[] {
									direction.getId(),
									linesCursor.getString(linesCursor
											.getColumnIndex("line_id")) });
			directionsCursor.moveToFirst();

			// if it doesn't, add it
			if(directionsCursor.getCount() == 0) {
				ContentValues values = new ContentValues();

				values.put("direction_timeoID", direction.getId());
				values.put("direction_name", direction.getName());
				values.put("line_id", linesCursor.getString(linesCursor
						.getColumnIndex("line_id")));

				db.insert("directions", null, values);
			}

			linesCursor.close();
			directionsCursor.close();
		}
	}

	public ArrayList<TimeoScheduleObject> getAllStops() {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		Cursor results = db.rawQuery(
			"SELECT "
				+ "lines.line_name, "
				+ "lines.line_timeoID, "
				+ "directions.direction_name, "
				+ "directions.direction_timeoID, "
				+ "stops.stop_id, "
				+ "stops.stop_name, "
				+ "stops.stop_timeoID "
				+ "FROM stops "
				+ "JOIN directions USING(direction_id, line_id) "
				+ "JOIN lines USING(line_id) "
				+ "ORDER BY CAST(lines.line_timeoID AS INTEGER), stops.stop_name, directions.direction_name",
			null);

		ArrayList<TimeoScheduleObject> stopsList = new ArrayList<TimeoScheduleObject>();

		while(results.moveToNext()) {
			stopsList.add(new TimeoScheduleObject(new TimeoIDNameObject(results
					.getString(results.getColumnIndex("line_timeoID")), results
					.getString(results.getColumnIndex("line_name"))),
					new TimeoIDNameObject(results.getString(results
							.getColumnIndex("direction_timeoID")),
							results.getString(results
									.getColumnIndex("direction_name"))),
					new TimeoIDNameObject(results.getString(results
							.getColumnIndex("stop_timeoID")), results
							.getString(results.getColumnIndex("stop_name"))),
					null));
		}

		results.close();
		db.close();

		return stopsList;
	}

	public void deleteStop(TimeoScheduleObject stop) {
		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

		Cursor results = db
				.rawQuery(
						"SELECT stops.stop_id FROM stops NATURAL JOIN directions NATURAL JOIN lines WHERE stop_timeoID=? AND direction_timeoID=? AND line_timeoID=?",
						new String[] { stop.getStop().getId(),
								stop.getDirection().getId(),
								stop.getLine().getId() });

		while(results.moveToNext()) {
			String id = results.getString(results.getColumnIndex("stop_id"));
			db.delete("stops", "stop_id=?", new String[] { id });
		}

		results.close();
		db.close();
	}

	public static int getColorFromLineID(String line) {
		String color;

		if(line.equalsIgnoreCase("TRAM"))
			color = "#EB6909";
		else if(line.equalsIgnoreCase("CEX"))
			color = "#642175";
		else if(line.equalsIgnoreCase("1"))
			color = "#CC007B";
		else if(line.equalsIgnoreCase("2"))
			color = "#008BCF";
		else if(line.equalsIgnoreCase("3"))
			color = "#27A22D";
		else if(line.equalsIgnoreCase("4"))
			color = "#EC7497";
		else if(line.equalsIgnoreCase("5"))
			color = "#B97CAF";
		else if(line.equalsIgnoreCase("6"))
			color = "#8F87C7";
		else if(line.equalsIgnoreCase("7"))
			color = "#87BBC3";
		else if(line.equalsIgnoreCase("8"))
			color = "#BCCF2F";
		else if(line.equalsIgnoreCase("9"))
			color = "#DFAE00";
		else if(line.equalsIgnoreCase("10"))
			color = "#60BFE8";
		else if(line.equalsIgnoreCase("11"))
			color = "#BC7419";
		else if(line.equalsIgnoreCase("14"))
			color = "#FFDD00";
		else if(line.equalsIgnoreCase("15"))
			color = "#FECC00";
		else if(line.equalsIgnoreCase("16"))
			color = "#D8B083";
		else if(line.equalsIgnoreCase("17"))
			color = "#E585B1";
		else if(line.equalsIgnoreCase("18"))
			color = "#F9B200";
		else if(line.equalsIgnoreCase("19"))
			color = "#E3004F";
		else if(line.equalsIgnoreCase("20"))
			color = "#94A6B0";
		else if(line.equalsIgnoreCase("21"))
			color = "#9C917F";
		else if(line.equalsIgnoreCase("22"))
			color = "#28338A";
		else if(line.equalsIgnoreCase("23"))
			color = "#172055";
		else if(line.equalsIgnoreCase("24"))
			color = "#E85761";
		else if(line.equalsIgnoreCase("25"))
			color = "#A5A213";
		else if(line.equalsIgnoreCase("26"))
			color = "#03B4E6";
		else if(line.equalsIgnoreCase("28"))
			color = "#F6A924";
		else if(line.equalsIgnoreCase("29"))
			color = "#807CAE";
		else if(line.equalsIgnoreCase("31"))
			color = "#EB6C68";
		else if(line.equalsIgnoreCase("32"))
			color = "#8B2412";
		else if(line.equalsIgnoreCase("33"))
			color = "#FFDD05";
		else if(line.equalsIgnoreCase("61"))
			color = "#005478";
		else if(line.equalsIgnoreCase("62"))
			color = "#857AB3";
		else
			color = "#34495E";

		return Color.parseColor(color);
	}

	private final TwistoastOpenHelper databaseOpenHelper;

	public enum DBStatus {
		SUCCESS, ERROR_NOT_ENOUGH_PARAMETERS, ERROR_DUPLICATE
	}

}
