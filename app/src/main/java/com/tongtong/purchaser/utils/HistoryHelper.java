package com.tongtong.purchaser.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tongtong.purchaser.model.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxy on 2018/4/9.
 */

public class HistoryHelper {
    private static HistoryDB openHelper;
    private Context context;

    private static HistoryDB getInstance(Context context){
        if(openHelper==null){
            openHelper=new HistoryDB(context);
        }
        return openHelper;
    }
    public static void addHistory(Context context,Region region){
        SQLiteDatabase database = getInstance(context).getWritableDatabase();
        database.delete("history_select","region_name=?",new String[]{region.getRegion_name()});
        ContentValues values = new ContentValues();
        values.put("region_id", region.getAdcode());
        values.put("region_name", region.getRegion_name());
        database.insert("history_select", null, values);
        database.close();
    }
    public static void addSearchHistory(Context context,String name){
        SQLiteDatabase database = getInstance(context).getWritableDatabase();
        database.delete("history_search","name=?",new String[]{name});
        ContentValues values = new ContentValues();
        values.put("name", name);
        database.insert("history_search", null, values);
        database.close();
    }
    public static List<Region> getHistory(Context context){
        SQLiteDatabase database = getInstance(context).getWritableDatabase();
        List<Region> regions=new ArrayList<>();
        String sql="select * from history_select order by _id desc limit 0,2";
        Cursor cursor=database.rawQuery(sql,null);
        while (cursor.moveToNext()){
            int region_id=cursor.getInt(cursor.getColumnIndex("region_id"));
            String region_name=cursor.getString(cursor.getColumnIndex("region_name"));
            Region region=new Region();
            region.setAdcode(region_id);
            region.setRegion_name(region_name);
            regions.add(region);
        }
        database.close();
        return  regions;
    }
    public static List<String> getSearchHistory(Context context){
        SQLiteDatabase database = getInstance(context).getWritableDatabase();
        List<String> regions=new ArrayList<>();
        String sql="select name from history_search order by _id desc limit 0,20";
        Cursor cursor=database.rawQuery(sql,null);
        while (cursor.moveToNext()){
            String name=cursor.getString(0);
            regions.add(name);
        }
        database.close();
        return  regions;
    }
    public static void deleteSearchHistory(Context context){
        SQLiteDatabase database = getInstance(context).getWritableDatabase();
        database.delete("history_search",null,null);
        database.close();
    }
}
