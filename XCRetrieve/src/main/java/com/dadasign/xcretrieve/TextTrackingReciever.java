package com.dadasign.xcretrieve;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.dadasign.xcretrieve.history.HistoryActivity;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.settings.TabedSettingsActivity;
import com.dadasign.xcretrieve.utils.ContactHelper;
import com.dadasign.xcretrieve.utils.NotificationHelper;

@SuppressLint("DefaultLocale")
public class TextTrackingReciever extends BroadcastReceiver{
	public Context ctx;
    private ContactHelper contactHelper;

	@Override
	public void onReceive(Context _ctx, Intent intent) {
		ctx =_ctx;
        contactHelper = new ContactHelper(ctx);
		SharedPreferences settings = ctx.getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
		SmsMessage sms = getMessagesFromIntent(intent)[0];
		String content = sms.getMessageBody();
		String from = sms.getOriginatingAddress();
		if(settings.getBoolean(SharedPreferenceKeys.trackingServiceEnabled,false)){
			if(content.toLowerCase().startsWith(("xc@"+settings.getString(SharedPreferenceKeys.password,"")).toLowerCase())){
				boolean has_permission=false;
				
				if(settings.getBoolean(SharedPreferenceKeys.allowAllContacts, false)){
					has_permission = true;
				}else{


					JSONArray contact_data;

					try {
						contact_data = new JSONArray(settings.getString(SharedPreferenceKeys.contacts, ""));
                        if(contact_data.length()>0){
                            if(ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                                Toast.makeText(ctx,R.string.permission_contacts_error,Toast.LENGTH_LONG).show();
                                NotificationHelper notifHelper = new NotificationHelper(ctx);
                                Intent resInt = new Intent(ctx, TabedSettingsActivity.class);
                                notifHelper.displayErrorNotification(ctx.getString(R.string.app_name)+" "+ctx.getString(R.string.error),ctx.getString(R.string.permission_contacts_error),resInt);
                                return;
                            }
                            String lookup = contactHelper.getContactLookup(from).id;
                            for(int x=0; x<contact_data.length(); x++){
                                if(contact_data.getString(x).equals(lookup)){
                                    has_permission = true;
                                    break;
                                }
                            }
                        }
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if(has_permission){
					Intent service = new Intent(ctx, TrackingService.class);
					service.putExtra("content", content.substring(3+settings.getString(SharedPreferenceKeys.password,"").length()));
					service.putExtra("from", from);
					ctx.startService(service);
				}
			}
		}
		//Show response messages
		//TODO: Add show position condition.
		if(content.startsWith("lat:") || content.startsWith("https://maps.google.com/?q=")){
			settings.edit().putString("last_content",content).commit();
			settings.edit().putString("last_contact",from).commit();
			parseResponseData(content,from, sms.getTimestampMillis());
		}
	}
    public void parseResponseData(String content, String from, long timestampMilis) {
        parseResponseData(content,from,timestampMilis,false);
    }
	public void parseResponseData(String content, String from, long timestampMilis,boolean isTest) {
		Matcher lats[] = new Matcher[4];
		Matcher lngs[] = new Matcher[4];
		lats[0] = Pattern.compile("(?<=lat:)-?[\\d.]+[,\\.]?[\\d]*").matcher(content);
		lngs[0] = Pattern.compile("(?<=lng:)-?[\\d.]+[,\\.]?[\\d]*").matcher(content);
		//DM
		lats[1] = Pattern.compile("(?<=lat:)-?[\\d]+\\:[\\d]*[,\\.]?[\\d]*").matcher(content);
		lngs[1] = Pattern.compile("(?<=lng:)-?[\\d]+\\:[\\d]*[,\\.]?[\\d]*").matcher(content);
		//DMS
		lats[2] = Pattern.compile("(?<=lat:)-?[\\d]+\\:[\\d]+:[\\d]+[,\\.]?[\\d]*").matcher(content);
		lngs[2] = Pattern.compile("(?<=lng:)-?[\\d]+\\:[\\d]+:[\\d]+[,\\.]?[\\d]*").matcher(content);
		//Link
		lats[3] = Pattern.compile("(?<=https?://maps\\.google\\.com/\\?q=)-?[\\d.]+\\.?[\\d]*").matcher(content);
		lngs[3] = Pattern.compile("(?<=,)-?[\\d.]+\\.?[\\d]*").matcher(content);

        Matcher alt_match = Pattern.compile("(?<=alt:)-?[\\d]+").matcher(content);
        Matcher altb_match = Pattern.compile("(?<=altb:)-?[\\d]+").matcher(content);
        //Matcher bar_alt_match = Pattern.compile("(?<=b_alt:)[\\d]*.?[\\d]*").matcher(content);
        Matcher speed_match = Pattern.compile("[\\d]*\\.?[\\d]*(?=km/h)").matcher(content);
		Matcher dir_match = Pattern.compile("[\\d]*\\.?[\\d]*(?=deg)").matcher(content);
        Matcher acc_match = Pattern.compile("(?<=acc:)[\\d]*").matcher(content);
        Matcher accu_match = Pattern.compile("(?<=accu:)[\\d]*").matcher(content);

        Contact contact;
        //Can be null if parseResponseData is called from test activity.
        if(contactHelper!=null) {
            contact = from == null ? null : contactHelper.getContactLookup(from);
        }else{
            contact = null;
        }
        TrackingMessage trackingMessage = new TrackingMessage();
        trackingMessage.message = content;
        trackingMessage.timeSent = timestampMilis;
        trackingMessage.timeCreated = trackingMessage.timeLastUpdate = System.currentTimeMillis();
        for(int c=3; c>=0; c--){
            if(lats[c].find() && lngs[c].find()){
                trackingMessage.lat = Location.convert(lats[c].group().replace(",","."));
                trackingMessage.lng = Location.convert(lngs[c].group().replace(",","."));
                break;
            }
        }
        if(alt_match.find()) {
            trackingMessage.alt = Integer.parseInt(alt_match.group());
        }
        if(altb_match.find()) {
            trackingMessage.altBaro = Integer.parseInt(altb_match.group());
        }
        if(acc_match.find()) {
            trackingMessage.movementIndex = Integer.parseInt(acc_match.group());
        }
        if(accu_match.find()) {
            trackingMessage.accuracy = Integer.parseInt(accu_match.group());
        }
        if(speed_match.find()) {
            trackingMessage.speed = Double.parseDouble(speed_match.group());
        }
        if(dir_match.find()) {
            String dirStr = dir_match.group();
            trackingMessage.direction = Integer.parseInt(dirStr);
        }
        trackingMessage.gpsUsed = !content.contains("NON-GPS");
        if(contact!=null){
            trackingMessage.requestFrom = contact.name;
            trackingMessage.requestFromId = contact.id;
        } else {
            trackingMessage.requestFrom = from;
        }

		if(trackingMessage.lng!=null && trackingMessage.lat!=null){
			Intent i = new Intent(ctx,HistoryActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(isTest){
                trackingMessage.messageType = TrackingMessage.MSG_TYPE_TEST;
            }else {
                trackingMessage.messageType = TrackingMessage.MSG_TYPE_RECEIVED;
            }
            Bundle b = trackingMessage.getBundle();
			i.putExtra("received_message",b);
			ctx.startActivity(i);
		}
		
	}
	private SmsMessage[] getMessagesFromIntent(Intent intent) {
        if(Build.VERSION.SDK_INT<19) {
            Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
            byte[][] pduObjs = new byte[messages.length][];

            for (int i = 0; i < messages.length; i++) {
                pduObjs[i] = (byte[]) messages[i];
            }
            byte[][] pdus = new byte[pduObjs.length][];
            int pduCount = pdus.length;
            SmsMessage[] msgs = new SmsMessage[pduCount];
            for (int i = 0; i < pduCount; i++) {
                pdus[i] = pduObjs[i];
                msgs[i] = SmsMessage.createFromPdu(pdus[i]);
            }
            return msgs;
        }else{
            return Telephony.Sms.Intents.getMessagesFromIntent(intent);
        }
    }

}
