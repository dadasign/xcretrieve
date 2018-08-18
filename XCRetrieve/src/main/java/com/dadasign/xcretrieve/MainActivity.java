package com.dadasign.xcretrieve;

import org.json.JSONArray;
import org.json.JSONException;

import com.dadasign.xcretrieve.utils.PermissionHelper;
import com.dadasign.xcretrieve.wizard.WizardActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends BaseActivity{
	private JSONArray contact_data;
	private TextView top_message;
	private ToggleButton service_on;
    private ImageButton instant_position_btn;
    private ImageButton instant_request_btn;
    private static final int PERMISSION_REQUEST_SERVICE_ON=1;
    private static final int PERMISSION_REQUEST_SEND_REQUEST=2;
    private static final int PERMISSION_REQUEST_SEND_POSITION=3;
    private static final String REQUEST_SENT = "SMS_TRACKING_REQUEST_SENT";
    private PermissionHelper permissionHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_main);
		service_on = (ToggleButton) findViewById(R.id.toggle_service);
		top_message = (TextView) findViewById(R.id.top_message);
		top_message.setVisibility(View.GONE);
        instant_position_btn = (ImageButton) findViewById(R.id.instant_position_btn);
        instant_request_btn = (ImageButton) findViewById(R.id.instant_request_btn);

        permissionHelper = new PermissionHelper(this);
		if(!settings.contains(SharedPreferenceKeys.isFirstRun)){
			settings.edit().putBoolean(SharedPreferenceKeys.isFirstRun, false).commit();
			startActivity(new Intent(this,WizardActivity.class));
		}
        if(settings.getBoolean(SharedPreferenceKeys.trackingServiceEnabled,false)){
            if(!permissionHelper.areNeededPermissionsSatisfied()){
                settings.edit().putBoolean(SharedPreferenceKeys.trackingServiceEnabled,false).commit();
                service_on.setChecked(false);
            }else{
                service_on.setChecked(true);
            }
        }else{
            service_on.setChecked(false);
        }

		setupContacts();
		//Add listeners
		service_on.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					if(checkParams()) {
                        settings.edit().putBoolean(SharedPreferenceKeys.trackingServiceEnabled, isChecked).commit();
                    }
				}else{
					settings.edit().putBoolean(SharedPreferenceKeys.trackingServiceEnabled, false).commit();
					top_message.setVisibility(View.GONE);
				}
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		top_message.setVisibility(View.GONE);
        boolean enabled = settings.getBoolean(SharedPreferenceKeys.trackingServiceEnabled, false);
        if(enabled) {
            service_on.setChecked(enabled);
        }
        checkInstantButtons();
	}
    private void setupContacts(){
        if(settings.contains(SharedPreferenceKeys.contacts)){
            try {
                contact_data = new JSONArray(settings.getString(SharedPreferenceKeys.contacts, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e){
                settings.edit().remove(SharedPreferenceKeys.contacts);
            }
        }
    }
    private void checkInstantButtons(){
        if(settings.getBoolean(SharedPreferenceKeys.oneClickPositionEnabled,false)) {
            instant_position_btn.setVisibility(View.VISIBLE);
            instant_position_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(permissionHelper.areNeededPermissionsSatisfied()) {
                        sendPosition();
                    }else{
                        String[] perm = permissionHelper.getRequiredPermissions();
                        ActivityCompat.requestPermissions(MainActivity.this,perm,PERMISSION_REQUEST_SEND_POSITION);
                    }
                }
            });
        }else{
            instant_position_btn.setVisibility(View.GONE);
        }
        if(settings.getBoolean(SharedPreferenceKeys.oneClickRequestEnabled,false)) {
            instant_request_btn.setVisibility(View.VISIBLE);
            instant_request_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(permissionHelper.canSendSms()) {
                        sendSMSRequest();
                    }else{
                        String[] perm = {Manifest.permission.SEND_SMS};
                        ActivityCompat.requestPermissions(MainActivity.this,perm,PERMISSION_REQUEST_SEND_REQUEST);
                    }
                }
            });
        }else{
            instant_request_btn.setVisibility(View.GONE);
        }
    }
    private void sendPosition(){
        Toast.makeText(MainActivity.this, R.string.sending_position, Toast.LENGTH_SHORT).show();
        Intent service = new Intent(MainActivity.this, InstantPositionService.class);
        startService(service);
    }
    private void sendSMSRequest(){
        String phoneNum = settings.getString(SharedPreferenceKeys.oneClickRequestContactNum,"");
        String pass = settings.getString(SharedPreferenceKeys.oneClickRequestPass,"");
        boolean instant = settings.getBoolean(SharedPreferenceKeys.oneClickRequestInstant,true);
        if(phoneNum==""){
            Toast.makeText(MainActivity.this,R.string.no_phone,Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(MainActivity.this, R.string.sending_request_sms, Toast.LENGTH_SHORT).show();
            PendingIntent sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(REQUEST_SENT), 0);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(MainActivity.this, R.string.sent_request_sms, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(MainActivity.this, R.string.send_request_sms_failed, Toast.LENGTH_LONG).show();
                            break;
                    }
                    unregisterReceiver(this);
                }
            }, new IntentFilter(REQUEST_SENT));

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNum, null, getString(R.string.xc_at) + pass + (instant ? " -i" : ""), sentPI, null);
        }
    }
	private boolean checkParams(){
        setupContacts();
		LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if(settings.getString(SharedPreferenceKeys.password, "").length()==0 && settings.getBoolean(SharedPreferenceKeys.allowAllContacts, false)){
			top_message.setText(R.string.no_password_defined);
			top_message.setVisibility(View.VISIBLE);
            return false;
		}else if(!settings.getBoolean(SharedPreferenceKeys.allowAllContacts, false) && (contact_data==null || contact_data.length()==0)){
			top_message.setText(R.string.empty_contacts_warning);
			top_message.setVisibility(View.VISIBLE);
            return false;
		}else if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		    builder.setMessage(R.string.turn_on_gps)
		           .setCancelable(false)
		           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
		               public void onClick( final DialogInterface dialog, final int id) {
		                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		               }
		           })
		           .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
		               public void onClick(final DialogInterface dialog, final int id) {
		            	   top_message.setText(R.string.gps_off_warning);
		            	   top_message.setVisibility(View.VISIBLE);
		                   dialog.cancel();
		               }
		           });
		    final AlertDialog alert = builder.create();
		    alert.show();
            return false;
		}else if (!permissionHelper.areNeededPermissionsSatisfied()) {
            service_on.setChecked(false);
            settings.edit().putBoolean(SharedPreferenceKeys.trackingServiceEnabled, false).commit();
            ActivityCompat.requestPermissions(this,permissionHelper.getRequiredPermissions(),PERMISSION_REQUEST_SERVICE_ON);
            return false;
        }
        return true;
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_REQUEST_SERVICE_ON) {
            if(permissionHelper.areNeededPermissionsSatisfied()) {
                service_on.setChecked(true);
                settings.edit().putBoolean(SharedPreferenceKeys.trackingServiceEnabled, true).commit();
            }else{
                Toast.makeText(this,R.string.need_permissions_for_service,Toast.LENGTH_LONG).show();
            }
        }else if(requestCode==PERMISSION_REQUEST_SEND_REQUEST){
            if(permissionHelper.canSendSms()){
                sendSMSRequest();
            }
        }else if(requestCode==PERMISSION_REQUEST_SEND_POSITION){
            if(permissionHelper.areNeededPermissionsSatisfied()){
                sendPosition();
            }
        }
    }
}
