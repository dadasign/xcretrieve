package com.dadasign.xcretrieve.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dadasign.xcretrieve.history.HistoryUpdateBroadcastReceiver;
import com.dadasign.xcretrieve.model.LogMessage;
import com.dadasign.xcretrieve.model.TrackingMessage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub on 2015-06-23.
 */
public class Persistence extends SQLiteOpenHelper{

    private static final String DB_NAME = "xcretrieve.db";
    private static final int DB_VERSION = 1;

    private Context ctx;

    public Persistence(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        ctx = context;
    }

    private static final String LOG_DB = "log";
    private static final String MSG_DB = "messages";

    private static final String MSG_ID = "id";

    private static final String LOG_TIME = "time";
    private static final String LOG_MSG = "message";
    private static final String LOG_LVL = "level";

    private static final  String TYPE_INT = " INTEGER";
    private static final  String TYPE_FLOAT = " REAL";
    private static final  String TYPE_TEXT = " TEXT";

    private SQLiteDatabase db;

    @Override
    public void onCreate(SQLiteDatabase _db) {
        db = _db;
        String logFields =
            LOG_TIME+TYPE_INT+", "+
            LOG_MSG+TYPE_TEXT+", "+
            LOG_LVL+TYPE_INT;
        db.execSQL("CREATE TABLE "+LOG_DB+" ("+logFields+")");

        String msgFields = "";
        for (Field f : TrackingMessage.class.getDeclaredFields()) {
            if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                if (f.getType().isAssignableFrom(Integer.class) || f.getType().isAssignableFrom(Long.class) || f.getType().isAssignableFrom(Boolean.class)) {
                    if(f.getName().equals(MSG_ID)) {
                        msgFields += f.getName() +" INTEGER PRIMARY KEY, ";
                    }else{
                        msgFields += f.getName() + TYPE_INT + ", ";
                    }
                } else if (f.getType().isAssignableFrom(Double.class)) {
                    msgFields += f.getName() + TYPE_FLOAT + ", ";
                } else if (f.getType().isAssignableFrom(String.class)) {
                    msgFields += f.getName() + TYPE_TEXT + ", ";
                }
            }
        }
        msgFields = msgFields.substring(0,msgFields.length()-2);
        //Log.v("create table", "CREATE TABLE " + MSG_DB + " (" + msgFields + ")");
        db.execSQL("CREATE TABLE " + MSG_DB + " (" + msgFields + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase _db, int from, int to) {
        db = _db;
        if(from<16 && from>=13 && to>=16){
            addAccuracyColumn();
        }else {
            onCreate(db);
        }
    }

    private void addAccuracyColumn() {
        db = getWritableDatabase();
        db.execSQL("ALTER TABLE " + MSG_DB + " ADD COLUMN accuracy" + TYPE_INT + ";");
    }

    public void saveLogMessage(LogMessage msg){
        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LOG_LVL,msg.level);
        values.put(LOG_MSG,msg.message);
        values.put(LOG_TIME, msg.time);
        db.insert(LOG_DB, null, values);
    }
    public List<LogMessage> getLogMessages(){
        db = getReadableDatabase();
        List<LogMessage> list = new ArrayList<LogMessage>();
        String[] cols = {LOG_MSG,LOG_LVL,LOG_TIME};
        Cursor cursor = db.query(LOG_DB, cols, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LogMessage msg = new LogMessage(cursor.getString(0),cursor.getInt(1),cursor.getLong(2));
            list.add(msg);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }
    public void saveTrackingMessage(TrackingMessage msg){
        msg.timeLastUpdate = System.currentTimeMillis();
        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            for (Field f : TrackingMessage.class.getDeclaredFields()) {
                if(!java.lang.reflect.Modifier.isStatic(f.getModifiers()) && !f.getName().equals(MSG_ID)) {
                    if (f.getType().isAssignableFrom(Integer.class)) {
                        if(f.get(msg)!=null) {
                            values.put(f.getName(), (Integer) f.get(msg));
                        }
                    } else if (f.getType().isAssignableFrom(Long.class)) {
                        if(f.get(msg)!=null) {
                            values.put(f.getName(), (Long) f.get(msg));
                        }
                    } else if (f.getType().isAssignableFrom(Double.class)) {
                        if(f.get(msg)!=null) {
                            values.put(f.getName(), (Double) f.get(msg));
                        }
                    } else if (f.getType().isAssignableFrom(String.class)) {
                        if(f.get(msg)!=null){
                            values.put(f.getName(), (String) f.get(msg));
                        }

                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if(msg.id!=null){
            try{
                db.update(MSG_DB, values, "id=" + msg.id, null);
            }catch(SQLiteException e){
                if(e.getMessage().contains("accuracy")){
                    addAccuracyColumn();
                    db.update(MSG_DB, values, "id=" + msg.id, null);
                }else{
                    throw e;
                }
            }

            Log.v("Persistence", "Updated: id=" + msg.id);
            broadcastTrackingMessagesEvent(HistoryUpdateBroadcastReceiver.UPDATE_ACTION, msg);
        }else{
            try {
                msg.id = db.insert(MSG_DB, null, values);
            }catch(SQLiteException e){
                if(e.getMessage().contains("accuracy")){
                    addAccuracyColumn();
                    msg.id = db.insert(MSG_DB, null, values);
                }else{
                    throw e;
                }
            }
            Log.v("Persistence", "Saved as new message: id=" + msg.id);
            broadcastTrackingMessagesEvent(HistoryUpdateBroadcastReceiver.INSERT_ACTION, msg);
        }
    }
    public List<TrackingMessage> getTrackingMessages() {
        return getTrackingMessages(0,0);
    }

    public List<TrackingMessage> getTrackingMessages(int limit,int offset) {
        db = getReadableDatabase();
        List<TrackingMessage> list = new ArrayList<TrackingMessage>();

        String[] cols = new String[22];
        int c = 0;
        for (Field f : TrackingMessage.class.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                if (f.getType().isAssignableFrom(Integer.class) || f.getType().isAssignableFrom(Long.class) || f.getType().isAssignableFrom(Boolean.class) || f.getType().isAssignableFrom(Float.class) || f.getType().isAssignableFrom(Double.class) || f.getType().isAssignableFrom(String.class)) {
                    cols[c] = f.getName();
                    c++;
                } else {
                    Log.v("Persistence", "Will skip: " + f.getName() + " " + f.getType().toString());
                }
            }
        }
        Cursor cursor;
        try {
            cursor = db.query(MSG_DB, cols, null, null, null, null, "id DESC", limit > 0 ? offset + ", " + limit : null);
        }catch(SQLiteException e){
            if(e.getMessage().contains("accuracy")){
                addAccuracyColumn();
                cursor = db.query(MSG_DB, cols, null, null, null, null, "id DESC", limit > 0 ? offset + ", " + limit : null);
            }else{
                throw e;
            }
        }

        cursor.moveToFirst();
        try {
            String[] tableCols = cursor.getColumnNames();
            while (!cursor.isAfterLast()) {
                TrackingMessage msg = new TrackingMessage();
                c = 0;
                for (String colName : tableCols) {
                    Field f = TrackingMessage.class.getDeclaredField(colName);
                    if (!cursor.isNull(c)) {
                        if (f.getType().isAssignableFrom(Integer.class)) {
                            f.set(msg, cursor.getInt(c));
                        } else if (f.getType().isAssignableFrom(Boolean.class)) {
                            f.set(msg, cursor.getInt(c) == 1);
                        } else if (f.getType().isAssignableFrom(Long.class)) {
                            f.set(msg, cursor.getLong(c));
                        } else if (f.getType().isAssignableFrom(Double.class)) {
                            f.set(msg, cursor.getDouble(c));
                        } else if (f.getType().isAssignableFrom(String.class)) {
                            f.set(msg, cursor.getString(c));
                        }
                    }
                    c++;
                }
                list.add(msg);
                cursor.moveToNext();
            }
        }catch(IllegalAccessException e){
            Log.e("Persistence", "IllegalAccessException");
        }catch(NoSuchFieldException e){
            Log.e("Persistence", "No such field: " + cursor.getColumnName(c));
            e.printStackTrace();
        }

        cursor.close();
        return list;
    }
    public void clearTrackingMessages(){
        db = getReadableDatabase();
        db.delete(MSG_DB, null, null);
        broadcastTrackingMessagesEvent(HistoryUpdateBroadcastReceiver.REMOVE_ALL_ACTION, null);
    }
    public void deleteTrackingMessage(long id){
        db = getReadableDatabase();
        db.delete(MSG_DB, "id=" + id, null);
        Intent intent = new Intent(HistoryUpdateBroadcastReceiver.HISTORY_UPDATE_FILTER);
        intent.putExtra(HistoryUpdateBroadcastReceiver.REMOVE_ACTION, id);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }
    private void broadcastTrackingMessagesEvent(String evt,TrackingMessage msg){
        Intent intent = new Intent(HistoryUpdateBroadcastReceiver.HISTORY_UPDATE_FILTER);
        if(msg==null) {
            intent.putExtra(evt, true);
        }else{
            intent.putExtra(evt, msg.getBundle());
        }
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }
}
