package com.tongtong.purchaser.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.tongtong.purchaser.model.MyProduceModel;
import com.tongtong.purchaser.model.MySpecificationModel;
import com.tongtong.purchaser.model.ProduceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxy on 2018/4/9.
 */

public class ProduceTypesHelper {
    private static ProduceTypesDB openHelper;
    public interface OnDataRecieveListener{
        void onRecieveData(List<ProduceType> produceTypes);
    }

    private static ProduceTypesDB getInstance(Context context){
            if(openHelper==null){
                openHelper=new ProduceTypesDB(context);
            }
        return openHelper;
    }
    public static void deleteProduceTypes(Context context){
        SQLiteDatabase database = getInstance(context).getWritableDatabase();
        database.delete("produce",null,null);
    }

    public static void addHistotyProduce(Context context,ProduceType produce){
        SQLiteDatabase database = getInstance(context).getWritableDatabase();
        database.delete("history_produce","name=?",new String[]{produce.getName()});
        ContentValues values = new ContentValues();
        values.put("id",produce.getId());
        values.put("name",produce.getName());
        values.put("level",produce.getLevel());
        database.insert("history_produce", null, values);
        database.close();
    }
    public static void getHistoryProduce(final Context context,final OnDataRecieveListener listener){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj!=null){
                    if(msg.obj instanceof List){
                        listener.onRecieveData((List<ProduceType>) msg.obj);
                    }
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                List<ProduceType> types=new ArrayList<>();
                SQLiteDatabase database = getInstance(context).getReadableDatabase();
                String sql="select id,name,level from history_produce order by _id desc limit 20";
                Cursor cursor=database.rawQuery(sql,null);
                while (cursor.moveToNext()){
                    ProduceType type=new ProduceType();
                    int parent_id=cursor.getInt(cursor.getColumnIndex("id"));
                    String parent_name=cursor.getString(cursor.getColumnIndex("name"));
                    int level=cursor.getInt(cursor.getColumnIndex("level"));
                    type.setId(parent_id);
                    type.setName(parent_name);
                    type.setLevel(level);
                    types.add(type);
                }
                Message msg=new Message();
                msg.obj=types;
                handler.sendMessage(msg);
                cursor.close();
                //database.close();
            }
        }.start();
    }
    public static void addProduceType(final Context context,final List<MyProduceModel> produceTypes){
        new Thread(){
            @Override
            public void run() {
                SQLiteDatabase database = getInstance(context).getWritableDatabase();
                for(MyProduceModel produceType:produceTypes){
                    ContentValues values = new ContentValues();
                    values.put("id", produceType.getId());
                    values.put("name", produceType.getName());
                    values.put("parent_id", produceType.getParent_id());
                    values.put("icon_url",produceType.getIcon_url());
                    values.put("level",produceType.getLevel());
                    values.put("pinyin",produceType.getPinyin());
                    values.put("suoxie",produceType.getSuoxie());
                    values.put("first_letter",produceType.getFirst_letter());
                    database.insert("produce", null, values);
                }
                database.close();
            }
        }.start();

    }

    public static void getParentType(final Context context,final OnDataRecieveListener listener){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj!=null){
                    if(msg.obj instanceof List){
                        listener.onRecieveData((List<ProduceType>) msg.obj);
                    }
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                List<ProduceType> types=new ArrayList<>();
                SQLiteDatabase database = getInstance(context).getReadableDatabase();
                String sql="select id,name,level from produce where parent_id=0 and level=1";
                Cursor cursor=database.rawQuery(sql,null);
                while (cursor.moveToNext()){
                    ProduceType type=new ProduceType();
                    int parent_id=cursor.getInt(cursor.getColumnIndex("id"));
                    String parent_name=cursor.getString(cursor.getColumnIndex("name"));
                    int level=cursor.getInt(cursor.getColumnIndex("level"));
                    type.setId(parent_id);
                    type.setName(parent_name);
                    type.setLevel(level);
                    types.add(type);
                }
                Message msg=new Message();
                msg.obj=types;
                handler.sendMessage(msg);
                cursor.close();
                //database.close();
            }
        }.start();
    }
    public static void getSubProduce(final int parent_id,final Context context,final OnDataRecieveListener listener){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj!=null){
                    if(msg.obj instanceof List){
                        listener.onRecieveData((List<ProduceType>) msg.obj);
                    }
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                SQLiteDatabase database = getInstance(context).getReadableDatabase();
                List<ProduceType> ptypes=new ArrayList<>();
                String sql="SELECT id,name,level from produce where  parent_id="+parent_id+" order by pinyin asc";
                Cursor cursor=database.rawQuery(sql,null);
                while(cursor.moveToNext()){
                    ProduceType type=new ProduceType();
                    type.setId(cursor.getInt(0));
                    type.setName(cursor.getString(1));
                    type.setLevel(cursor.getInt(2));
                    ptypes.add(type);
                }
                Message msg=new Message();
                msg.obj=ptypes;
                handler.sendMessage(msg);
                cursor.close();
                database.close();
            }
        }.start();
    }
    public static void getSubType(final int parent_id,final Context context,final OnDataRecieveListener listener){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj!=null){
                    if(msg.obj instanceof List){
                        listener.onRecieveData((List<ProduceType>) msg.obj);
                    }
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                SQLiteDatabase database = getInstance(context).getReadableDatabase();
                List<ProduceType> ptypes=new ArrayList<>();
                String sql="SELECT first_letter from produce where level=2 and parent_id="+parent_id+" group by first_letter order by first_letter asc";
                Cursor cursor=database.rawQuery(sql,null);
                while(cursor.moveToNext()){
                    ProduceType type=new ProduceType();
                    type.setFirst_letter(cursor.getString(0));
                    List<ProduceType> types=new ArrayList<>();
                    sql="SELECT id,name,level,icon_url from produce where level=2 and parent_id="+parent_id+" and first_letter='"+type.getFirst_letter()+"' order by pinyin asc";
                    Cursor cur=database.rawQuery(sql,null);
                    while (cur.moveToNext()){
                        ProduceType stype=new ProduceType();
                        int id=cur.getInt(0);
                        String name=cur.getString(1);
                        int level=cur.getInt(2);
                        String icon_url=cur.getString(3);
                        stype.setId(id);
                        stype.setName(name);
                        stype.setIcon_url(icon_url);
                        stype.setLevel(level);
                        types.add(stype);
                    }
                    cur.close();
                    type.setProduceTypes(types);
                    ptypes.add(type);
                }
                Message msg=new Message();
                msg.obj=ptypes;
                handler.sendMessage(msg);
                cursor.close();
                database.close();
            }
        }.start();
    }
    public static void getSearch(final Context context,final String search_content,final OnDataRecieveListener listener){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj!=null){
                    if(msg.obj instanceof List){
                        listener.onRecieveData((List<ProduceType>) msg.obj);
                    }
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                List<ProduceType> produceTypes=new ArrayList<>();
                SQLiteDatabase database = getInstance(context).getReadableDatabase();
                String sql="SELECT id,name,parent_id,'',level FROM produce WHERE (name LIKE '%"+search_content+"%' OR pinyin LIKE '%"+search_content+"%' OR suoxie LIKE '%"+search_content+"%') AND parent_id=0  UNION " +
                        "SELECT p1.id,p1.name,p2.id as parent_id,p2.name AS parent_name,p1.level FROM produce p1 INNER JOIN produce p2 ON p1.parent_id=p2.id " +
                        "WHERE (p1.name LIKE '%"+search_content+"%' OR p2.name LIKE '%"+search_content+"%'"+
                        "OR p1.pinyin LIKE '%"+search_content+"%' OR p2.pinyin LIKE '%"+search_content+"%'"+
                        "OR p1.suoxie LIKE '%"+search_content+"%' OR p2.suoxie LIKE '%"+search_content+"%') AND p1.parent_id>0 order by level LIMIT 20";
                Cursor cursor=database.rawQuery(sql,null);
                while (cursor.moveToNext()){
                    ProduceType type=new ProduceType();
                    type.setId(cursor.getInt(0));
                    type.setName(cursor.getString(1));
                    type.setParent_id(cursor.getInt(2));
                    type.setParent_name(cursor.getString(3));
                    type.setLevel(cursor.getInt(4));
                    produceTypes.add(type);
                }
                Message msg=new Message();
                msg.obj=produceTypes;
                handler.sendMessage(msg);
                cursor.close();
                database.close();
            }
        }.start();
    }
    public static void getSearchWithoutParent(final Context context,final String search_content,final OnDataRecieveListener listener){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj!=null){
                    if(msg.obj instanceof List){
                        listener.onRecieveData((List<ProduceType>) msg.obj);
                    }
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                List<ProduceType> produceTypes=new ArrayList<>();
                SQLiteDatabase database = getInstance(context).getReadableDatabase();
                String sql= "SELECT p1.id,p1.name,p2.id as parent_id,p2.name AS parent_name,p1.level FROM produce p1 INNER JOIN produce p2 ON p1.parent_id=p2.id " +
                        "WHERE (p1.name LIKE '%"+search_content+"%' OR p2.name LIKE '%"+search_content+"%'"+
                        "OR p1.pinyin LIKE '%"+search_content+"%' OR p2.pinyin LIKE '%"+search_content+"%'"+
                        "OR p1.suoxie LIKE '%"+search_content+"%' OR p2.suoxie LIKE '%"+search_content+"%') AND p1.parent_id>0 order by p1.level LIMIT 20";
                Cursor cursor=database.rawQuery(sql,null);
                while (cursor.moveToNext()){
                    ProduceType type=new ProduceType();
                    type.setId(cursor.getInt(0));
                    type.setName(cursor.getString(1));
                    type.setParent_id(cursor.getInt(2));
                    type.setParent_name(cursor.getString(3));
                    type.setLevel(cursor.getInt(4));
                    produceTypes.add(type);
                }
                Message msg=new Message();
                msg.obj=produceTypes;
                handler.sendMessage(msg);
                cursor.close();
                database.close();
            }
        }.start();
    }
    public static ProduceType getRecmend(Context context,String name){
        ProduceType produce=new ProduceType();
        SQLiteDatabase database = getInstance(context).getReadableDatabase();
        String sql="select p1.id,p1.name,p1.parent_id,p2.name as parent_name from produce p1 inner join produce p2 on p1.parent_id=p2.id where p1.level=3 and (p1.name like'"+name+"' OR p1.pinyin LIKE '"+name+"' OR p1.suoxie LIKE '"+name+"') LIMIT 1";
        Cursor cursor=database.rawQuery(sql,null);
        if(cursor.moveToNext()){
            produce.setId(cursor.getInt(0));
            produce.setName(cursor.getString(1));
            produce.setParent_id(cursor.getInt(2));
            produce.setParent_name(cursor.getString(3));
            produce.setLevel(3);
            cursor.close();
            database.close();
            return produce;
        }
        sql="select p1.id,p1.name,p1.parent_id,p2.name as parent_name from produce p1 inner join produce p2 on p1.parent_id=p2.id where p1.level=3 and (p1.name like'%"+name+"%' OR p1.pinyin LIKE '%"+name+"%' OR p1.suoxie LIKE '%"+name+"%') LIMIT 1";
        cursor=database.rawQuery(sql,null);
        if(cursor.moveToNext()){
            produce.setId(cursor.getInt(0));
            produce.setName(cursor.getString(1));
            produce.setParent_id(cursor.getInt(2));
            produce.setParent_name(cursor.getString(3));
            produce.setLevel(3);
            cursor.close();
            database.close();
            return produce;
        }
        sql="select p1.id,p1.name,p1.parent_id,p2.name as parent_name from produce p1 inner join produce p2 on p1.parent_id=p2.id where p1.level=2 and (p1.name like'"+name+"' OR p1.pinyin LIKE '"+name+"' OR p1.suoxie LIKE '"+name+"') LIMIT 1";
        cursor=database.rawQuery(sql,null);
        if(cursor.moveToNext()){
            produce.setId(cursor.getInt(0));
            produce.setName(cursor.getString(1));
            produce.setParent_id(cursor.getInt(2));
            produce.setParent_name(cursor.getString(3));
            produce.setLevel(2);
            cursor.close();
            database.close();
            return produce;
        }
        sql="select p1.id,p1.name,p1.parent_id,p2.name as parent_name from produce p1 inner join produce p2 on p1.parent_id=p2.id where p1.level=2 and (p1.name like'%"+name+"%' OR p1.pinyin LIKE '%"+name+"%' OR p1.suoxie LIKE '%"+name+"%') LIMIT 1";
        cursor=database.rawQuery(sql,null);
        if(cursor.moveToNext()){
            produce.setId(cursor.getInt(0));
            produce.setName(cursor.getString(1));
            produce.setParent_id(cursor.getInt(2));
            produce.setParent_name(cursor.getString(3));
            produce.setLevel(2);
            cursor.close();
            database.close();
            return produce;
        }
        sql="select id,name from produce where level=1 and (name like'"+name+"' OR pinyin LIKE '"+name+"' OR suoxie LIKE '"+name+"') LIMIT 1";
        cursor=database.rawQuery(sql,null);
        if(cursor.moveToNext()){
            produce.setId(cursor.getInt(0));
            produce.setName(cursor.getString(1));
            produce.setParent_id(0);
            produce.setParent_name("");
            produce.setLevel(1);
            cursor.close();
            database.close();
            return produce;
        }
        sql="select id,name from produce where level=1 and (name like'%"+name+"%' OR pinyin LIKE '%"+name+"%' OR suoxie LIKE '%"+name+"%') LIMIT 1";
        cursor=database.rawQuery(sql,null);
        if(cursor.moveToNext()){
            produce.setId(cursor.getInt(0));
            produce.setName(cursor.getString(1));
            produce.setParent_id(0);
            produce.setParent_name("");
            produce.setLevel(1);
            cursor.close();
            database.close();
            return produce;
        }
       return null;
    }
    public static ProduceType getParentProduce(Context context,int id){
        ProduceType produce=new ProduceType();
        SQLiteDatabase database = getInstance(context).getReadableDatabase();
        String sql="select parent_id from produce where id="+id;
        Cursor cursor=database.rawQuery(sql,null);
        if(cursor.moveToNext()){
            int parent_id=cursor.getInt(0);
            sql="select id,name,level from produce where id="+parent_id;
            cursor=database.rawQuery(sql,null);
            if(cursor.moveToNext()){
                produce.setId(cursor.getInt(0));
                produce.setName(cursor.getString(1));
                produce.setLevel(cursor.getInt(2));
            }
        }
        return produce;
    }
}
