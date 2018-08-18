package com.dadasign.xcretrieve;

import com.dadasign.xcretrieve.utils.ContactHelper;
import com.dadasign.xcretrieve.utils.Persistence;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.telephony.SmsManager;
import android.util.Log;



public class TrackingService extends Service{
	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		TrackingService getService() {
	        return TrackingService.this;
	    }
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("Tracking service","Service start command");
		if(intent!=null && (intent.hasExtra("from") || intent.hasExtra("target"))){
			Bundle extras = intent.getExtras();
			String from = extras.getString("from");
			String content = extras.containsKey("content")?extras.getString("content"):null;
			ResultReceiver target = (ResultReceiver) (extras.containsKey("target")?extras.get("target"):null);
            boolean isTest = extras.containsKey("is_test")?extras.getBoolean("is_test"):false;
			Handler handler = new Handler();
            ContactHelper contactHelper = new ContactHelper(this);
            Persistence persistence = new Persistence(this);
			LocationTracker lt = new LocationTracker(this,persistence,new SmsBroadcastReceiverFactory(), SmsManager.getDefault(),from,content,target,handler,contactHelper,isTest);
			lt.runLocation();
		}else{
			if(intent==null){
				Log.e("TrackingService", "#44: No intent.");
			}else{
				Log.e("TrackingService", "#47: No target, no recepient.");
			}
			
		}
		return Service.START_REDELIVER_INTENT;//super.onStartCommand(intent, flags, startId);
	}
	
	

	@Override
	public void onCreate() {
		Log.v("Tracking service","Service has been created");
		super.onCreate();
		
	}
	@Override
	public void onDestroy() {
		Log.v("Tracking service","Service has stopped");
		super.onDestroy();
	}
}
