package com.dadasign.xcretrieve;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.telephony.SmsManager;

import com.dadasign.xcretrieve.history.HistoryActivity;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.sensors.GoogleLocationListener;
import com.dadasign.xcretrieve.sensors.GoogleLocationUpdateListener;
import com.dadasign.xcretrieve.utils.LocationMessageFormatter;
import com.dadasign.xcretrieve.utils.NotificationHelper;
import com.dadasign.xcretrieve.utils.Persistence;
import com.google.android.gms.location.LocationRequest;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jakub on 2016-08-18.
 */
public class InstantPositionService extends IntentService implements GoogleLocationUpdateListener{
    private static final int WAIT_FOR_ACCURATE_POSITION_TIME = 60000;
    private static final int MAX_LOCATION_AGE = 30000;
    private static final int MAX_POSITION_SENDING_INTERVAL = 30000;
    private static final int REQUIRED_ACCURACY = 50;
    private static final String POSITION_SENT = "SMS_POSITION_SENT";
    private Location location;
    private Location inaccurateLocation;
    private Timer timer = new Timer();
    private SharedPreferences settings;
    private TrackingMessage msg;
    private Persistence persistence;
    private GoogleLocationListener googleLocationListener;
    private long lastTimeCalled=0;
    private long lastTimeStarted=0;


    public  InstantPositionService(){
        super("InstantPositionService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        settings = getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
        lastTimeStarted = settings.getLong(SharedPreferenceKeys.lastTimePositionShared,0);
        if(lastTimeStarted>System.currentTimeMillis()-MAX_POSITION_SENDING_INTERVAL){
            stopSelf();
            return;
        }else{
            lastTimeStarted = System.currentTimeMillis();
            settings.edit().putLong(SharedPreferenceKeys.lastTimePositionShared,lastTimeStarted).apply();
        }
        persistence = new Persistence(this);
        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(0);
        request.setInterval(1000);
        googleLocationListener = new GoogleLocationListener(request,this,this);
        googleLocationListener.connect();
        msg = new TrackingMessage();
        msg.messageType = TrackingMessage.MSG_TYPE_ONE_CLICK_SHARE;
        msg.status = TrackingMessage.STATUS_LOC_IN_PROGRESS;
        persistence.saveTrackingMessage(msg);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(inaccurateLocation!=null){
                    sendLocation(inaccurateLocation);
                }else{
                    msg.status = TrackingMessage.STATUS_LOC_FAILED;
                    persistence.saveTrackingMessage(msg);
                    stopSelf();
                }
            }
        },WAIT_FOR_ACCURATE_POSITION_TIME);
    }

    @Override
    public void onLocationUpdate(Location _location) {
        if(_location.hasAccuracy() && _location.getAccuracy()<REQUIRED_ACCURACY && _location.getTime()>System.currentTimeMillis()-MAX_LOCATION_AGE){
            timer.cancel();
            location = _location;
            sendLocation(location);
        }else{
            inaccurateLocation = _location;
        }
    }
    private void sendLocation(final Location loc){
        if(lastTimeCalled>System.currentTimeMillis()-MAX_POSITION_SENDING_INTERVAL){
            stopSelf();
            return;
        }else{
            lastTimeCalled = System.currentTimeMillis();
        }
        msg.lat = loc.getLatitude();
        msg.lng = loc.getLongitude();
        msg.accuracy = (int) loc.getAccuracy();
        msg.gpsUsed = msg.accuracy<REQUIRED_ACCURACY && loc.hasAltitude();
        msg.alt = (int) loc.getAltitude();
        msg.status = TrackingMessage.STATUS_LOC_COMPLETE;
        String phoneNum = settings.getString(SharedPreferenceKeys.oneClickPositionContactNum,"");
        String name = settings.getString(SharedPreferenceKeys.oneClickPositionContactName,"");
        msg.sentTo = phoneNum+" ("+name+")";

        LocationMessageFormatter msgFormatter = new LocationMessageFormatter(getBaseContext());
        msg.responseMessage = msgFormatter.getMessageForLocation(loc);

        PendingIntent sentPI = PendingIntent.getBroadcast(InstantPositionService.this, 0,new Intent(POSITION_SENT), 0);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String time;
                String content;
                Intent resultIntent;
                NotificationHelper notifHelper = new NotificationHelper(InstantPositionService.this);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        resultIntent = new Intent(InstantPositionService.this, HistoryActivity.class);
                        msg.status = TrackingMessage.STATUS_COMPLETE;
                        time = timeFormat.format(System.currentTimeMillis());
                        content = getString(R.string.sent_position_content).replace("[content]",msg.responseMessage).replace("[time]",time).replace("[user]",msg.sentTo);
                        notifHelper.displayNotification(getString(R.string.sent_position_title),content,resultIntent);
                        break;
                    default:
                        resultIntent = new Intent(InstantPositionService.this, MainActivity.class);
                        time = timeFormat.format(System.currentTimeMillis());
                        content = getString(R.string.sent_position_failed).replace("[time]",time);
                        notifHelper.displayNotification(getString(R.string.sent_position_failed_title),content,resultIntent);
                        msg.status = TrackingMessage.STATUS_FAILED_SENDING_SMS;
                        break;
                }
                persistence.saveTrackingMessage(msg);
                unregisterReceiver(this);
                stopSelf();
            }
        }, new IntentFilter(POSITION_SENT));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNum, null, msg.responseMessage, sentPI, null);
        persistence.saveTrackingMessage(msg);
        googleLocationListener.disconnect();
    }
}
