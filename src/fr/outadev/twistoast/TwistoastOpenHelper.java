package fr.outadev.twistoast;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TwistoastOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "twistoast";
	private static final String LINES_TABLE_CREATE = "CREATE TABLE lines(line_id INTEGER PRIMARY KEY, line_timeoID TEXT UNIQUE NOT NULL, line_name TEXT);";
	private static final String STOPS_TABLE_CREATE = "CREATE TABLE stops(stop_id INTEGER PRIMARY KEY, stop_timeoID TEXT NOT NULL, stop_name TEXT, line_id INTEGER NOT NULL, direction_id INTEGER NOT NULL);";
	private static final String DIRECTIONS_TABLE_CREATE = "CREATE TABLE directions(direction_id INTEGER PRIMARY KEY, direction_timeoID TEXT NOT NULL, direction_name TEXT, line_id INTEGER NOT NULL);";

	TwistoastOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LINES_TABLE_CREATE);
		db.execSQL(STOPS_TABLE_CREATE);
		db.execSQL(DIRECTIONS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
}