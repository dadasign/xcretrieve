package com.dadasign.xcretrieve;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.dadasign.xcretrieve.history.HistoryActivity;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.utils.NotificationHelper;
import com.dadasign.xcretrieve.utils.Persistence;

import java.text.SimpleDateFormat;

/**
 * Created by Jakub on 2017-06-12.
 */

public class SmsBroadcastReceiverFactory {
    BroadcastReceiver createBroadcastReceiver(final TrackingMessage trackingMessage,final Service service,final Persistence persistence,final int onSMSSentStatus){
        return new BroadcastReceiver(){

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        trackingMessage.message = "ERROR: RESULT_ERROR_GENERIC_FAILURE";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        trackingMessage.message = "ERROR: RESULT_ERROR_NO_SERVICE";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        trackingMessage.message = "ERROR: RESULT_ERROR_NULL_PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        trackingMessage.message = "ERROR: RESULT_ERROR_RADIO_OFF";
                        break;
                    default:
                        trackingMessage.message = "ERROR: UNKNOWN RESPONSE CODE: "+getResultCode();
                        break;
                }
                NotificationHelper notifHelper = new NotificationHelper(service);
                Intent resultIntent;
                String time;
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                if(getResultCode()==Activity.RESULT_OK){
                    resultIntent = new Intent(service, HistoryActivity.class);
                    trackingMessage.status = onSMSSentStatus;
                    time = timeFormat.format(System.currentTimeMillis());
                    String content = service.getString(R.string.sent_position_content)
                            .replace("[content]",trackingMessage.responseMessage)
                            .replace("[time]",time)
                            .replace("[user]",trackingMessage.sentTo);
                    notifHelper.displayNotification(service.getString(R.string.sent_position_title),content,resultIntent);
                }else{
                    trackingMessage.status = TrackingMessage.STATUS_FAILED_SENDING_SMS;
                }
                persistence.saveTrackingMessage(trackingMessage);
                service.unregisterReceiver(this);
                service.stopSelf();
            }
        };
    }
}
