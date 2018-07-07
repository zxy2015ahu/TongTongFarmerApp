package com.tongtong.purchaser.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by zxy on 2018/4/9.
 */

public class ProduceTypesDB extends SQLiteOpenHelper {
    private static final int VERSION = 3;
    private static final String SQL_NAME = "produces.db";
    private String produce_sqlInfo = "create table produce(id int,name char,parent_id int,level int,pinyin char,suoxie char,icon_url char,first_letter char)";
    private String specification_sqlInfo = "create table history_produce(_id INTEGER PRIMARY KEY AUTOINCREMENT,id int,name char,level int)";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(produce_sqlInfo);
        db.execSQL(specification_sqlInfo);
    }
    public ProduceTypesDB(Context context){
        super(context, SQL_NAME, null, VERSION);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(specification_sqlInfo);
    }
}
