package com.dadasign.xcretrieve.model;

import android.os.Bundle;

import java.lang.reflect.Field;

/**
 * Created by Jakub on 2015-06-22.
 */
public class TrackingMessage {
    public static final int MSG_TYPE_SMS_REQ = 1;
    public static final int MSG_TYPE_SHARE = 2;
    public static final int MSG_TYPE_SMS_REQ_INSTANT = 3;
    public static final int MSG_TYPE_TEST = 4;
    public static final int MSG_TYPE_RECEIVED = 5;
    public static final int MSG_TYPE_ONE_CLICK_SHARE = 6;

    public static final int STATUS_LOC_IN_PROGRESS = 1;
    public static final int STATUS_LOC_COMPLETE = 2;
    public static final int STATUS_SENDING = 3;
    public static final int STATUS_COMPLETE = 4;
    public static final int STATUS_LOC_FAILED = 5;
    public static final int STATUS_SENT_FAILED = 6;
    public static final int STATUS_FAILED_SENDING_SMS = 7;

    public Long id;
    public Double lat;
    public Double lng;
    public Double speed;
    public Integer direction;
    public Integer alt;
    public Integer altBaro;
    public Integer accuracy;
    public String requestFromId;
    public String requestFrom;
    public String sentTo;
    public Integer status;
    public Integer messageType;
    public Integer format;
    public String requestMessage;
    public String responseMessage;
    public Boolean gpsUsed;
    public Integer movementIndex;
    public Long timeCreated;
    public Long timeSent;
    public Long timeLastUpdate;
    public String message;

    public TrackingMessage() {
        timeLastUpdate = timeCreated = System.currentTimeMillis();
    }
    public TrackingMessage(Bundle b) {
        this.fromBundle(b);
    }

    public Bundle getBundle(){
        Bundle b = new Bundle();
        try {
            for (Field f : TrackingMessage.class.getDeclaredFields()) {
                if(f.getType().isAssignableFrom(Integer.class)){
                    Object o = f.get(this);
                    if(o!=null) {
                        b.putInt(f.getName(), (Integer) o);
                    }
                }else if(f.getType().isAssignableFrom(Long.class)){
                    Object o = f.get(this);
                    if(o!=null) {
                        b.putLong(f.getName(), (Long) o);
                    }
                }else if(f.getType().isAssignableFrom(Double.class)){
                    Object o = f.get(this);
                    if(o!=null) {
                        b.putDouble(f.getName(), (Double) o);
                    }
                }else if(f.getType().isAssignableFrom(String.class)){
                    Object o = f.get(this);
                    if(o!=null) {
                        b.putString(f.getName(), (String) o);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return b;
    }
    public Bundle fromBundle(Bundle b){
        try {
            for (Field f : TrackingMessage.class.getDeclaredFields()) {
                if(b.containsKey(f.getName())) {
                    if (f.getType().isAssignableFrom(Integer.class)) {
                        f.set(this, b.getInt(f.getName()));
                    } else if (f.getType().isAssignableFrom(Long.class)) {
                        f.set(this, b.getLong(f.getName()));
                    } else if (f.getType().isAssignableFrom(Double.class)) {
                        f.set(this, b.getDouble(f.getName()));
                    } else if (f.getType().isAssignableFrom(String.class)) {
                        f.set(this, b.getString(f.getName()));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return b;
    }
}
