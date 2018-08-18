package com.dadasign.xcretrieve.sensors;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.dadasign.xcretrieve.log.Logger;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.utils.Persistence;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jakub on 2015-09-13.
 */
public class LocationSensor implements GoogleLocationUpdateListener{
    public LocationSensorListener listener;
    public int locations_length = 2;
    public int locations_interval = 60;
    public int locations_time_margin = 180;
    private final ArrayList<Location> locations = new ArrayList<Location>();
    private Context ctx;
    private Handler handler;
    private TrackingMessage trackingMessage;
    private Persistence persistence;
    private Logger logger;
    private int min_distance = 0;
    private boolean gps_failed = false;
    private Location alternative_location;
    private GoogleLocationListener googleLocationListener;

    public LocationSensor(Persistence _persistence, Logger _logger, Handler _h){
        persistence = _persistence;
        logger = _logger;
        handler = _h;
    }

    public void runLocation(Context _ctx,int _locations_length,int _locations_interval,int _locations_time_margin,final TrackingMessage _trackingMessage){
        ctx = _ctx;
        locations_length = _locations_length;
        locations_interval = _locations_interval;
        locations_time_margin = _locations_time_margin;
        trackingMessage = _trackingMessage;
        final long timeStarted = System.currentTimeMillis();

        final LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            final LocationListener locationListener = new LocationListener() {
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.v("LocationTracker", "GPS status changed");
                    if(status == LocationProvider.OUT_OF_SERVICE){
                        if(locations.isEmpty()){
                            locationManager.removeUpdates(this);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    runAlternativeLocation();
                                }
                            });
                        }else{
                            listener.onLocationComplete(locations.toArray(new Location[locations.size()]),!gps_failed);
                        }
                    }
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.v("LocationTracker","GPS provider enabled");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.v("LocationTracker","GPS provider disabled");
                    locationManager.removeUpdates(this);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            runAlternativeLocation();
                        }
                    });
                }

                @Override
                public void onLocationChanged(Location location) {
                    Log.v("LocationTracker","GPS data recorded");
                    if((location.hasAccuracy() && location.getAccuracy()>50) && (location.getTime()-timeStarted)<60000){
                        Log.v("LocationTracker","Accuracy above 50m ignored on first 60 sec.");
                        return;
                    }
                    if(locations.isEmpty() || location.getTime()>locations.get(locations.size()-1).getTime()+(locations_interval*1000)){
                        if(locations.isEmpty()){
                            listener.updateStatus(3);
                        }
                        locations.add(location);
                        trackingMessage.lat = location.getLatitude();
                        trackingMessage.lng = location.getLongitude();
                        trackingMessage.alt = (int) location.getAltitude();
                        trackingMessage.accuracy = location.hasAccuracy()?Math.round(location.getAccuracy()):0;
                        trackingMessage.gpsUsed = true;
                        persistence.saveTrackingMessage(trackingMessage);
                    }
                    if(locations.size()>=locations_length){
                        locationManager.removeUpdates(this);
                        listener.onLocationComplete(locations.toArray(new Location[locations.size()]),!gps_failed);
                    }
                }
            };
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    locationManager.removeUpdates(locationListener);
                    if(locations.size()>0){
                        listener.onLocationComplete(locations.toArray(new Location[locations.size()]),!gps_failed);
                    }else{
                        if(alternative_location!=null){
                            Log.v("LocationTracker","Failed GPS, using network");
                            gps_failed = true;
                            locations.add(alternative_location);
                            listener.onLocationComplete(locations.toArray(new Location[locations.size()]),!gps_failed);
                        }else{
                            Log.v("LocationTracker","Failed everything");
                            trackingMessage.status = TrackingMessage.STATUS_LOC_FAILED;
                            listener.onLocationComplete(new Location[0],!gps_failed);
                        }
                    }
                }
            }, (locations_interval*locations_length+locations_time_margin)*1000);
            t.schedule(new TimerTask() {

                @Override
                public void run() {
                    if(locations.size()==0){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v("LocationTracker","Trying network");
                                runAlternativeLocation();
                            }
                        });
                    }
                }
            }, locations_time_margin*1000);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locations_interval*1000, min_distance, locationListener);
        }else{
            gps_failed=true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    runAlternativeLocation();
                }
            });

        }
        Log.v("Location tracker","Started");
    }

    protected void runAlternativeLocation() {
        logger.log("Doing alternative location",0);
        Log.v("LocationTracker","runAlternativeLocation");
        listener.updateStatus(2);
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setSmallestDisplacement(0);
        googleLocationListener = new GoogleLocationListener(request,this,ctx);
        googleLocationListener.connect();

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v("LocationTracker","Remove alternative updates");
                googleLocationListener.disconnect();
                if (gps_failed) {
                    if (alternative_location != null) {
                        locations.add(alternative_location);
                        listener.onLocationComplete(locations.toArray(new Location[locations.size()]),!gps_failed);
                    } else {
                        listener.onLocationComplete(new Location[0],!gps_failed);
                    }
                }
            }
        }, locations_time_margin * 1000);

    }


    @Override
    public void onLocationUpdate(Location location) {
        Log.v("LocationTracker","Got alternative location:"+location.getLatitude()+", "+location.getLongitude());
        if(location.getTime()<System.currentTimeMillis()-30000){
            Log.v("LocationTracker","This time entry is too old: "+((System.currentTimeMillis()-location.getTime())/1000)+" sec");
            return;
        }
        alternative_location =  location;
        if(gps_failed){
            locations.add(alternative_location);
            trackingMessage.lat = location.getLatitude();
            trackingMessage.lng = location.getLongitude();
            trackingMessage.alt = (int) location.getAltitude();
            trackingMessage.gpsUsed = location.hasAccuracy() && location.getAccuracy()<50 && location.hasAltitude();
            listener.onLocationComplete(locations.toArray(new Location[locations.size()]),false);
        }
    }
}
