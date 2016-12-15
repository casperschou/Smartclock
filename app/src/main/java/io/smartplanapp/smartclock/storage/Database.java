package io.smartplanapp.smartclock.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String TABLE_NAME = "Shifts";
    private static final String KEY_BEGIN = "begin";
    private static final String KEY_END = "end";
    private static final String KEY_LOCATION = "location";

    private final Context context;
    private SQLiteDatabase db;

    public Database(Context context) {
        this.context = context;
    }

    public Database open() throws SQLException {
        DatabaseOpenHelper dbOpenHelper = new DatabaseOpenHelper(context);
        db = dbOpenHelper.getWritableDatabase();
        return this;
    }

    public long createShift(String begin, String end, String location) {
        ContentValues values = new ContentValues();
        values.put(KEY_BEGIN, begin);
        values.put(KEY_END, end);
        values.put(KEY_LOCATION, location);
        return db.insert(TABLE_NAME, null, values);
    }

    public List<Shift> getAllShifts() {
        List<Shift> shifts = new ArrayList<>();
        Cursor cursor = getShiftsCursor();
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            Shift shift = new Shift(cursor.getString(0), cursor.getString(1), cursor.getString(2));
            shifts.add(shift);
        }
        cursor.close();
        return shifts;
    }

    public int deleteAllShifts() {
        return db.delete(TABLE_NAME, null, null);
    }

    private Cursor getShiftsCursor() {
        return db.query(TABLE_NAME, new String[] {KEY_BEGIN, KEY_END, KEY_LOCATION}, null, null, null, null, null);
    }

    // Database with table of shifts
    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "Smartclock.db";

        private static final String DATABASE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        KEY_BEGIN + " text, " +
                        KEY_END + " text, " +
                        KEY_LOCATION + " text, " +
                        " UNIQUE (" + KEY_BEGIN +"));";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}
