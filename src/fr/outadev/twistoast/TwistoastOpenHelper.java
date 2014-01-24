package fr.outadev.twistoast;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TwistoastOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "twistoast";

	private static final String LINES_TABLE_CREATE = "CREATE TABLE twi_line(line_id TEXT PRIMARY KEY, line_name TEXT);";
	private static final String DIRECTIONS_TABLE_CREATE = "CREATE TABLE twi_direction(dir_id TEXT, line_id TEXT, dir_name TEXT, PRIMARY KEY(dir_id, line_id));";
	private static final String STOPS_TABLE_CREATE = "CREATE TABLE twi_stop(stop_id INTEGER, line_id TEXT, dir_id TEXT, stop_name TEXT, PRIMARY KEY(stop_id, line_id, dir_id));";

	TwistoastOpenHelper(Context context) {
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
		// TODO Auto-generated method stub

	}
}