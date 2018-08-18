package com.dadasign.xcretrieve;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.telephony.SmsManager;
import android.util.Log;

import com.dadasign.xcretrieve.history.HistoryActivity;
import com.dadasign.xcretrieve.log.Logger;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.sensors.BaroSensor;
import com.dadasign.xcretrieve.sensors.BaroSensorListener;
import com.dadasign.xcretrieve.sensors.LocationSensor;
import com.dadasign.xcretrieve.sensors.LocationSensorListener;
import com.dadasign.xcretrieve.sensors.MovementSensor;
import com.dadasign.xcretrieve.sensors.MovementSensorListener;
import com.dadasign.xcretrieve.utils.ContactHelper;
import com.dadasign.xcretrieve.utils.LocationDataFormats;
import com.dadasign.xcretrieve.utils.LocationMessageFormatter;
import com.dadasign.xcretrieve.utils.NotificationHelper;
import com.dadasign.xcretrieve.utils.Persistence;
import com.dadasign.xcretrieve.utils.RequestMessageOptions;

import java.text.SimpleDateFormat;

public class LocationTracker implements LocationSensorListener, MovementSensorListener, BaroSensorListener{
    public TrackingMessage trackingMessage = new TrackingMessage();

	public int data_format = 0;
	private String from;
	private boolean sent;
	private String content;
	private ResultReceiver target;
	private Context ctx;
	private Handler handler;
    private String movementSensorMessage;
    private String baroAltMessage;
    private LocationMessageFormatter msgformatter;
    private RequestMessageOptions requestMessageOptions;
    private int onSMSSentStatus;


    private Persistence persistence;
    private SmsBroadcastReceiverFactory smsBroadcastReceiverFactory;
    private SmsManager smsManager;
    private Logger logger;

	public LocationTracker(Context _ctx,Persistence persistence,SmsBroadcastReceiverFactory smsBroadcastReceiverFactory, SmsManager smsManager, String _from, String _content, ResultReceiver _target, Handler _h,ContactHelper _contactHelper, boolean isTest) {
		ctx=_ctx;
        this.persistence = persistence;
        this.smsBroadcastReceiverFactory = smsBroadcastReceiverFactory;
        this.smsManager = smsManager;
        msgformatter = new LocationMessageFormatter(ctx);
		from=_from;
        Contact contact = _contactHelper.getContactLookup(from);
        if(contact!=null){
            trackingMessage.requestFromId = contact.id;
            trackingMessage.requestFrom = contact.name;
        }else {
            trackingMessage.requestFrom = from;
        }
        trackingMessage.status = TrackingMessage.STATUS_LOC_IN_PROGRESS;

        content=_content;
        trackingMessage.requestMessage = _content;

        requestMessageOptions = new RequestMessageOptions();
        requestMessageOptions.parseMessage(content);
        if(isTest){
            trackingMessage.messageType = TrackingMessage.MSG_TYPE_TEST;
        }else if(requestMessageOptions.singleLocationOnly){
            trackingMessage.messageType = TrackingMessage.MSG_TYPE_SMS_REQ_INSTANT;
        }else {
            trackingMessage.messageType = TrackingMessage.MSG_TYPE_SMS_REQ;
        }

		target=_target;
		handler = _h;
        logger = new Logger(persistence);
        persistence.saveTrackingMessage(trackingMessage);
	}

    @Override
    public void onMovementIndexCalculated(int index, String msg) {
        trackingMessage.movementIndex = index;
        persistence.saveTrackingMessage(trackingMessage);
        movementSensorMessage = msg;
    }

    @Override
    public void onBaroListenerResult(int alt, String msg) {
        trackingMessage.altBaro = alt;
        baroAltMessage = msg;
        persistence.saveTrackingMessage(trackingMessage);
    }

    public void runLocation() {
        logger.log("Starting location", 0);
		this.updateStatus(1);
        LocationDataFormats locationDataFormats = new LocationDataFormats();
        data_format = locationDataFormats.getFormatFromMessage(content,ctx);

        SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);

        LocationSensor locationSensor = new LocationSensor(persistence, logger, handler);
        locationSensor.listener = this;
        locationSensor.runLocation(ctx, requestMessageOptions.singleLocationOnly?1:2, 60, 180, trackingMessage);

        MovementSensor movementSensor = new MovementSensor(sensorManager, msgformatter,this);
        movementSensor.checkMovement();
        BaroSensor baroSensor = new BaroSensor(sensorManager, msgformatter,this);
        baroSensor.checkAltitude();
	}

    public void updateStatus(int status){
        if(target!=null){
            Bundle data = new Bundle();
            data.putInt("status", status);
            target.send(0, data);
        }
    }

    @Override
    public void onLocationComplete(Location[] locations, boolean isGPSBased) {
        if(locations.length==0){
            sendFailedMessage();
        }else{
            sendLocations(locations, isGPSBased);
        }
    }

    protected void sendLocations(Location[] locations, boolean isGPSBased) {
        logger.log("Sending location coordinates",1);

		Location lastLoc = locations[locations.length-1];
        String text = msgformatter.getMessageForLocation(lastLoc,data_format,isGPSBased);
        text += msgformatter.getHeadingMessagePart(locations);
		if(movementSensorMessage !=null && movementSensorMessage.length()!=0){
			text+=" "+ movementSensorMessage;
		}
        if(baroAltMessage !=null && baroAltMessage.length()!=0){
            text+=" "+ baroAltMessage;
        }
        trackingMessage.speed = msgformatter.speed;
        trackingMessage.direction = msgformatter.bearing;
        trackingMessage.status = TrackingMessage.STATUS_SENDING;
        trackingMessage.responseMessage =text;
        trackingMessage.lat = lastLoc.getLatitude();
        trackingMessage.lng = lastLoc.getLongitude();
        if(lastLoc.hasAltitude()){
            trackingMessage.alt =(int) lastLoc.getAltitude();
        }
        trackingMessage.sentTo = from;
        trackingMessage.timeSent = System.currentTimeMillis();
        persistence.saveTrackingMessage(trackingMessage);
        onSMSSentStatus = TrackingMessage.STATUS_COMPLETE;
		sendRawMessage(text, 0);

        persistence.saveTrackingMessage(trackingMessage);
	}

	protected void sendFailedMessage() {
        logger.log("Sending failed location message",1);
		String text = ctx.getString(R.string.failed_msg);
		if(movementSensorMessage !=null && movementSensorMessage.length()!=0){
			text+=" "+ movementSensorMessage;
		}
        trackingMessage.status = TrackingMessage.STATUS_LOC_FAILED;
        onSMSSentStatus = TrackingMessage.STATUS_SENT_FAILED;
        trackingMessage.timeSent = System.currentTimeMillis();
        trackingMessage.sentTo = from;
        trackingMessage.responseMessage = text;
        persistence.saveTrackingMessage(trackingMessage);
        sendRawMessage(text, 0);
	}
	private void sendRawMessage(String text, int status){
		if(!sent){
			if(text!=null && !text.equals("")) sent=true;
			if(target!=null){
                trackingMessage.status = TrackingMessage.STATUS_COMPLETE;
                persistence.saveTrackingMessage(trackingMessage);
				Bundle data = new Bundle();
				data.putString("response", text);
				data.putInt("status", 0);
				target.send(status, data);
			}else{
                PendingIntent sentPI;
                String SENT = "SMS_SENT";
                sentPI = PendingIntent.getBroadcast(ctx, 0,new Intent(SENT), 0);
                ctx.registerReceiver(smsBroadcastReceiverFactory.createBroadcastReceiver(trackingMessage,(Service) ctx,persistence,onSMSSentStatus), new IntentFilter(SENT));
                smsManager.sendTextMessage(from, null, text, sentPI, null);
			}
		}
	}
}