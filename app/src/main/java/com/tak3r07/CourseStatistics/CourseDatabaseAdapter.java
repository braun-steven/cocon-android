package com.tak3r07.CourseStatistics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by tak3r07 on 12/27/14.
 */
public class CourseDatabaseAdapter {

    //Instance of inner class
    CourseDatabaseHelper courseDatabaseHelper;

    /**
     * Constructor to create instance of inner class "helper"
     * @param context
     */
    public CourseDatabaseAdapter(Context context) {
        courseDatabaseHelper = new CourseDatabaseHelper(context);
    }

    /**
     * inserts new data into the database
     * @param name
     * @param password
     * @return
     */
    public long insertData(String name, String password) {
        SQLiteDatabase db = courseDatabaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CourseDatabaseHelper.NAME, name);
        contentValues.put(CourseDatabaseHelper.PASSWORD, password);

        long id = db.insert(CourseDatabaseHelper.TABLE_NAME, null, contentValues);
        return id;
    }

    public String getAllData() {
        SQLiteDatabase db = courseDatabaseHelper.getWritableDatabase();
        String[] columns = {CourseDatabaseHelper.UID, CourseDatabaseHelper.NAME, CourseDatabaseHelper.PASSWORD};
        Cursor cursor = db.query(CourseDatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            int uidIndex = cursor.getColumnIndex(CourseDatabaseHelper.UID);
            int nameIndex = cursor.getColumnIndex(CourseDatabaseHelper.NAME);
            int passwordIndex = cursor.getColumnIndex(CourseDatabaseHelper.PASSWORD);

            int cid = cursor.getInt(uidIndex);
            String name = cursor.getString(nameIndex);
            String password = cursor.getString(passwordIndex);

            buffer.append(cid + " " + name + " " + password + "\n");

        }
        return buffer.toString();
    }

    static class CourseDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "courses.db";
        private static final String TABLE_NAME = "Courses";
        private static final String NAME = "Name";
        private static final String PASSWORD = "Password";
        private static final String UID = "_id";
        private static final int DATABASE_VERSION = 7;
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " VARCHAR(255), " + PASSWORD + " VARCHAR(255));";
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        private Context context;

        CourseDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
            message("constructor called");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            message("oncreate called");
            db.execSQL(CREATE_TABLE);


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                message("onupgrade called");
                db.execSQL(DROP_TABLE);
                onCreate(db);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void message(String string) {
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
        }
    }
}