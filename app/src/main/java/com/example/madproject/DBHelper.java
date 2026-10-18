package com.example.madproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AthletixDB";
    private static final int DATABASE_VER = 1;

    public DBHelper(Context applicationContext) {
        super(applicationContext, DATABASE_NAME, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_query = "CREATE TABLE workout_info (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT, " +
                "intensity TEXT, " +
                "warmup TEXT, " +
                "notes TEXT, " +
                "datetime TEXT)";
        db.execSQL(create_query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS workout_info");
        onCreate(db);
    }

    public void registerWorkoutHelper(String cat, String intensity, String warmup, String notes, String dt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", cat);
        contentValues.put("intensity", intensity);
        contentValues.put("warmup", warmup);
        contentValues.put("notes", notes);
        contentValues.put("datetime", dt);
        db.insert("workout_info", null, contentValues);
    }

    public String readRecordHelper() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] colnames = {"id", "category", "intensity", "warmup", "notes", "datetime"};
        Cursor cursor = db.query("workout_info", colnames, null, null, null, null, null);
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String category = cursor.getString(1);
            String intensity = cursor.getString(2);
            String warmup = cursor.getString(3);
            String notes = cursor.getString(4);
            String dt = cursor.getString(5);

            buffer.append("Workout ID: ").append(id)
                    .append(" | ").append(category)
                    .append(" (").append(intensity).append(")\n")
                    .append("Notes: ").append(notes)
                    .append(" | Warmup: ").append(warmup).append("\n")
                    .append("Logged: ").append(dt).append("\n")
                    .append("------------------------------------\n");
        }
        cursor.close();
        return buffer.toString();
    }

    public Cursor getAllDataCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM workout_info", null);
    }

    public void updateRecordHelper(int id, String newNotes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("notes", newNotes);
        String[] whereArgs = {String.valueOf(id)};
        db.update("workout_info", contentValues, "id=?", whereArgs);
    }

    public void deleterecordHelper(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] nameArgs = {String.valueOf(id)};
        db.delete("workout_info", "id=?", nameArgs);
    }
}