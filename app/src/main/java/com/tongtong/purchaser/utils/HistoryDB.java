package com.tongtong.purchaser.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by zxy on 2018/4/9.
 */

public class HistoryDB extends SQLiteOpenHelper {
    private static final int VERSION = 2;
    private static final String SQL_NAME = "history_select.db";
    private String sqlInfo = "create table history_select(_id integer primary key autoincrement,region_name char,region_id int)";
    private String searchSql="create table history_search(_id integer primary key autoincrement,name char)";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlInfo);
        db.execSQL(searchSql);
    }
    public HistoryDB(Context context){
        super(context, SQL_NAME, null, VERSION);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(searchSql);
    }
}
