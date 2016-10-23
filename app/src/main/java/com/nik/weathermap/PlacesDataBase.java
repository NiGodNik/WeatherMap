package com.nik.weathermap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by Nik on 23.10.2016.
 */

public class PlacesDataBase {

    private final String TAG = this.getClass().getSimpleName();

    public PlacesDataBase() {
    }

    public static abstract class PlacesEntry implements BaseColumns {
        public static final String TABLE_NAME = "Place";
        public static final String COLUMN_NAME_ENTRY_ID = "PlaceID";
        public static final String COLUMN_NAME_TITLE = "Name";
        public static final String COLUMN_NAME_LAT = "Lat";
        public static final String COLUMN_NAME_LON = "Lon";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlacesEntry.TABLE_NAME + " (" +
                    PlacesEntry._ID + " INTEGER PRIMARY KEY," +
                    PlacesEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    PlacesEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    PlacesEntry.COLUMN_NAME_LAT + TEXT_TYPE + COMMA_SEP +
                    PlacesEntry.COLUMN_NAME_LON + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlacesEntry.TABLE_NAME;

    /**
     * Helper class for accessing the database.
     */
    public static class PlacesDBHelper extends SQLiteOpenHelper {
        private final String TAG = this.getClass().getSimpleName();

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "PlacesDB.db";

        public PlacesDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.d(TAG, "PlacesDBHelper constructor");
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.d(TAG, "onCreate");
            sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
            onCreate(sqLiteDatabase);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
