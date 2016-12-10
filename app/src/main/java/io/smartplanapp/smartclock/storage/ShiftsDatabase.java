package io.smartplanapp.smartclock.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ShiftsDatabase {

    public static final String KEY_BEGIN = "begin";
    public static final String KEY_END = "end";

    private static final String KEY_ID = "_id";
    private static final String TABLE_NAME = "Shifts";
    private static final String[] ALL_COLUMNS = new String[] {KEY_ID, KEY_BEGIN, KEY_END};
    private static final String ID_EQUAL = KEY_ID + "=?";
    private static String[] stringArray(long id) {
        return new String[] {Long.toString(id)};
    }

    private final Context context;
    private SQLiteDatabase db;
    private DatabaseOpenHelper dbOpenHelper;

    public ShiftsDatabase(Context context) {
        this.context = context;
    }

    public ShiftsDatabase open() throws SQLException {
        dbOpenHelper = new DatabaseOpenHelper(context);
        db = dbOpenHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (dbOpenHelper != null) {
            dbOpenHelper.close();
        }
    }

    public long createShift(String begin, String end) {
        ContentValues values = new ContentValues();
        values.put(KEY_BEGIN, begin);
        values.put(KEY_END, end);
        return db.insert(TABLE_NAME, null, values);
    }

    public int deleteShift(long id) {
        return db.delete(TABLE_NAME, ID_EQUAL, stringArray(id));
    }

    public int deleteAllShifts() {
        return db.delete(TABLE_NAME, null, null);
    }

    public Cursor getShiftsCursor() {
        return db.query(TABLE_NAME, new String[] {KEY_ID, KEY_BEGIN, KEY_END}, null, null, null, null, null);
    }

    public Cursor getShiftsByDate(String inputText) throws SQLException {
        Cursor mCursor = null;
        if (inputText == null  ||  inputText.length() == 0) {
            mCursor = db.query(TABLE_NAME, new String[]{KEY_ID,
                            KEY_BEGIN, KEY_END},
                    null, null, null, null, null);
        } else {
            mCursor = db.query(true, TABLE_NAME, new String[] {KEY_ID,
                            KEY_BEGIN, KEY_END},
                    KEY_END + " like '%" + inputText + "%'", null,
                    null, null, null, null);
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getAllShifts() {
        Cursor mCursor = db.query(TABLE_NAME, ALL_COLUMNS,
                null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public List<Shift> getAllShiftsList() {
        List<Shift> shifts = new ArrayList<>();
        Cursor cursor = getShiftsCursor();
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            Shift shift = new Shift(cursor.getString(0), cursor.getString(1));
            shifts.add(shift);
        }
        cursor.close();
        return shifts;
    }

    public boolean hasShift(long id) {
        Cursor c = db.query(TABLE_NAME, ALL_COLUMNS, ID_EQUAL, stringArray(id),
                null, null, null);
        if (c.moveToFirst()) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    public void insertSampleShifts() {
        createShift("mandag", "tirsdag");
        createShift("onsdag", "torsdag");
        createShift("fredag", "lørdag");
    }


    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "Smartclock.db";
        private static final int DATABASE_VERSION = 1;

        private static final String DATABASE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_BEGIN + " text, " +
                        KEY_END + " text, " +
                        " UNIQUE (" + KEY_ID +"));";

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