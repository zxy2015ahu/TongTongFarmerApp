package com.tongtong.purchaser.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.tongtong.purchaser.model.AddressModel;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.model.Region;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxy on 2018/4/9.
 */

public class DBManager {
    private static final String ASSETS_NAME = "cities_data.db";
    private static final String DB_NAME = "cities_data.db";
    private static final String NAME = "region";
    private static final int BUFFER_SIZE = 1024;
    private String DB_PATH;
    private Context mContext;

    //    public static DBManager init(){
    //        if (mInstance == null){
    //            synchronized (DBManager.class){
    //                if (mInstance != null){
    //                    mInstance = new DBManager();
    //                }
    //            }
    //        }
    //        return mInstance;
    //    }

    public interface OnDataRecieveListener{
        void recieveData(List<Region> regions);
    }
    public interface OnDataSearchListener{
        void recieveData(List<Region> regions);
    }
    public DBManager(Context context) {
        this.mContext = context;
        DB_PATH = File.separator + "data"
                + Environment.getDataDirectory().getAbsolutePath() + File.separator
                + context.getPackageName() + File.separator + "databases" + File.separator;
    }

    public void getData(final int parent_id,final OnDataRecieveListener listener){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj instanceof List){
                    List<Region> regions=(List<Region>)msg.obj;
                    if(listener!=null){
                        listener.recieveData(regions);
                    }
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                List<Region> regions=new ArrayList<>();
                String sql="select id,region_name,adcode from region where parent_id="+parent_id;
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
                Cursor cursor=db.rawQuery(sql, null);
                while (cursor.moveToNext()){
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    int adcode = cursor.getInt(cursor.getColumnIndex("adcode"));
                    String region_name = cursor.getString(cursor.getColumnIndex("region_name"));
                    Region region=new Region();
                    region.setId(id);
                    region.setAdcode(adcode);
                    region.setRegion_name(region_name);
                    regions.add(region);
                }
                Message msg=new Message();
                msg.obj=regions;
                handler.sendMessage(msg);
                close(cursor,db);
            }
        }.start();
    }
    public int getCityAdcode(String adcode){
        String sql="SELECT adcode FROM region WHERE id=(SELECT parent_id FROM region WHERE adcode="+adcode+")";
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
        Cursor cursor=db.rawQuery(sql,null);
        if(cursor.moveToNext()){
            int temp=cursor.getInt(0);
            close(cursor,db);
            return temp;
        }
        close(cursor,db);
        return 0;
    }
    public void searchData(final String region_name,final OnDataSearchListener listener){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj instanceof List){
                    List<Region> regions=(List<Region>)msg.obj;
                    if(listener!=null){
                        listener.recieveData(regions);
                    }
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                List<Region> regions=new ArrayList<>();
                String sql="SELECT  r.* FROM region r WHERE (SELECT r1.parent_id FROM region r1 WHERE r1.id=r.parent_id)=-1 AND r.region_name LIKE '%"+region_name+"%'";
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
                Cursor cursor=db.rawQuery(sql, null);
                while (cursor.moveToNext()){
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    int adcode = cursor.getInt(cursor.getColumnIndex("adcode"));
                    String region_name = cursor.getString(cursor.getColumnIndex("region_name"));
                    Region region=new Region();
                    region.setId(id);
                    region.setAdcode(adcode);
                    region.setRegion_name(region_name);
                    regions.add(region);
                }
                Message msg=new Message();
                msg.obj=regions;
                handler.sendMessage(msg);
                close(cursor,db);
            }
        }.start();
    }

    public void copyDBFile() {
        File dir = new File(DB_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dbFile = new File(DB_PATH + DB_NAME);
        if (!dbFile.exists()) {
            InputStream is;
            OutputStream os;
            try {
                is = mContext.getResources().getAssets().open(ASSETS_NAME);
                os = new FileOutputStream(dbFile);
                byte[] buffer = new byte[BUFFER_SIZE];
                int length;
                while ((length = is.read(buffer, 0, buffer.length)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                os.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public AddressModel getshengshiqu(int adcode){
        AddressModel address=new AddressModel();
        String sql="SELECT r.id AS district,r.parent_id AS city,(SELECT r2.parent_id prent FROM region r2 WHERE r2.id=r.parent_id) province FROM region r WHERE r.adcode="+adcode;
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
        Cursor cursor=db.rawQuery(sql, null);
        if(cursor.moveToNext()){
            address.setDistrict(cursor.getInt(0));
            address.setCity(cursor.getInt(1));
            address.setProvince(cursor.getInt(2));
        }
        cursor.close();
        db.close();
        return address;
    }
    private void close(Cursor cursor, SQLiteDatabase db) {
        cursor.close();
        db.close();
    }
    public Region getRegionByAdcode(int adcode){
        Region region=new Region();
        String sql="select id,region_name,adcode from region where adcode="+adcode;
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
        Cursor cursor=db.rawQuery(sql,null);
        if(cursor.moveToNext()){
            region.setId(cursor.getInt(0));
            region.setRegion_name(cursor.getString(1));
            region.setAdcode(cursor.getInt(2));
        }
        cursor.close();
        db.close();
        return region;
    }
}
