package com.example.Codeforces_Progress.SQLiteDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "HandleInfos";
    private static final String DATABASE_NAME = "HandleInfos.db";
    private static final String HANDLE = "_handle";
    private static final String IMAGE_URL = "Imageurl";
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "( " + HANDLE + " VARCHAR(300) PRIMARY KEY, " + IMAGE_URL + " VARCHAR(200)); ";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
    private static Integer VERSION_NUMBER = 1;

    private Context context;

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            // exception
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DROP_TABLE);
            onCreate(db);
        } catch (Exception e) {
            // exception
        }
    }

    /*
     * inserting in SQLite db
     * returns -1 if inserting is unsuccessful
     * else returns inserted row number in 1 based index
     */
    public long insertHandle(String handle, String imageUrl) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HANDLE, handle);
        contentValues.put(IMAGE_URL, imageUrl);
        return sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
    }

    public Cursor getAllHandleInfo() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery(SELECT_ALL, null);
    }

    /*
     * deleting in SQLite dp
     * returns 0 if deletion is unsuccessful
     * else returns deleted row number
     */
    public Integer deleteHandle(String handle) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(TABLE_NAME, HANDLE + " = ?", new String[]{handle});
    }
}
