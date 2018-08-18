package com.dadasign.xcretrieve.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jakub on 2015-03-20.
 */
public class LocationDataFormats {
    public static final int FORMAT_DEGREES=0;
    public static final int FORMAT_DEG_MIN=1;
    public static final int FORMAT_DEG_MIN_SEC=2;
    public static final int FORMAT_GOOGLE_MAPS=3;

    public int getFormatFromMessage(String msg,Context ctx){
        if(msg==null || msg.equals("")){
            return getDefaultFormat(ctx);
        }
        try{
            Matcher format_match = Pattern.compile("(?<=-f:)(d|m|s|l)").matcher(msg);
            if(format_match.find()){
                String format_code = format_match.group();
                if(format_code.equals("d")){
                    return FORMAT_DEGREES;
                }else if(format_code.equals("m")){
                    return FORMAT_DEG_MIN;
                }else if(format_code.equals("s")){
                    return  FORMAT_DEG_MIN_SEC;
                }else if(format_code.equals("l")){
                    return FORMAT_GOOGLE_MAPS;
                }
            }
        }catch(NullPointerException e){
            Log.v("LocationDataFormats", "Error matching pattern: "+e.getMessage());
        }
        return getDefaultFormat(ctx);
    }
    public int getDefaultFormat(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences("CloudSettings", Context.MODE_PRIVATE);
        if(settings!=null){
            return settings.getInt("format", 0);
        }
        return FORMAT_DEGREES;
    }
}
