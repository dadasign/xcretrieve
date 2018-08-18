package com.dadasign.xcretrieve.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Jakub on 2015-03-20.
 */
public class LocationMessageFormatter {
    private final Context context;
    public Integer bearing;
    public Double speed;

    public LocationMessageFormatter(Context ctx){
        context = ctx;
    }
    public String getMessageForLocation(Location loc){
        LocationDataFormats locationDataFormats = new LocationDataFormats();
        int dataFormat = locationDataFormats.getDefaultFormat(context);
        return getMessageForLocation(loc,dataFormat,false);
    }

    public String getMessageForLocation(Location loc,int dataFormat, boolean isGPSBased){
        String text;
        switch(dataFormat){
            case LocationDataFormats.FORMAT_DEG_MIN:
                text = "lat:"+ Location.convert(loc.getLatitude(), Location.FORMAT_MINUTES).replace(",", ".")
                        +" lng:"+Location.convert(loc.getLongitude(), Location.FORMAT_MINUTES).replace(",", ".");
                break;
            case LocationDataFormats.FORMAT_DEG_MIN_SEC:
                text = "lat:"+Location.convert(loc.getLatitude(), Location.FORMAT_SECONDS).replace(",", ".")
                        +" lng:"+Location.convert(loc.getLongitude(), Location.FORMAT_SECONDS).replace(",", ".");
                break;
            case LocationDataFormats.FORMAT_GOOGLE_MAPS:
                text = "https://maps.google.com/?q="+String.format(Locale.ENGLISH,"%.7f", loc.getLatitude())+","+String.format(Locale.ENGLISH,"%.7f", loc.getLongitude());
                break;
            default:
                text = "lat:"+String.format(Locale.ENGLISH,"%.7f", loc.getLatitude())+" lng:"+String.format(Locale.ENGLISH,"%.7f", loc.getLongitude());
                break;
        }
        if(loc.hasAltitude()){
            text+=" alt:"+Math.round(loc.getAltitude())+"m";
        }
        if(!isGPSBased){
            text+=" NON-GPS";
        }
        if(loc.hasAccuracy()){
            text+=" accu:"+Math.round(loc.getAccuracy());
        }
        return text;
    }
    public String getHeadingMessagePart(Location[] locations) {
        String text="";
        for(int c=locations.length-1; c>0; c--){
            Location loc1 = locations[c-1];
            Location loc2 = locations[c];
            long time = Math.round(((loc2.getTime()-loc1.getTime())/1000));
            bearing = Math.round(loc1.bearingTo(loc2));
            bearing = bearing<0?bearing+360:bearing;
            float dist = loc1.distanceTo(loc2);
            if(dist>0.5){
                speed = (double) Math.round(((dist/1000)*(3600/time))*10)/10;
                String speed_str;
                if(speed<.05){
                    text+=" 0km/h";
                }else{
                    if(speed<.95){
                        speed_str=" 0."+String.valueOf(Math.round(speed*10));
                    }else{
                        speed_str=String.valueOf(Math.round(speed));
                    }
                    text+=" "+bearing+"deg "+speed_str+"km/h";
                }
            }else{
                text+=" 0km/h";
            }
        }
        return text;
    }
    public int getMovementIndex(float total,float total2){
        if(total<2 && total2<1){
            return 0;
        }else if(total<4 && total2<1.5){
            return 1;
        }else if(total<8 && total2<4){
            return 2;
        }else if(total<8 || total2<4){
            return 3;
        }else if(total<15 || total2<10){
            return 4;
        }else if(total<25 || total2<20){
            return 5;
        }else{
            return 6;
        }
    }
    public String getMovementIndexMessagePart(float total,float total2){
        String sensor_result=" acc:";
        sensor_result+=this.getMovementIndex(total,total2);
        return sensor_result;
    }

    public String getBaroAltitudeMessage(int alt) {
        String sensor_result=" altb:"+alt+"m";
        return sensor_result;
    }
}
