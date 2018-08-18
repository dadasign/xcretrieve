package com.dadasign.xcretrieve.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.dadasign.xcretrieve.R;

/**
 * Created by Jakub on 2016-08-19.
 */
public class NotificationHelper {
    private final Context ctx;
    private static final int SENT_NOTIFICATION_ID = 1;
    private static final int ERROR_ID = 1;

    public NotificationHelper(Context _ctx){
        ctx=_ctx;
    }
    public void displayErrorNotification(String title,String text,Intent resultIntent){
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        ctx,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        Notification.Builder builder = new Notification.Builder(ctx);
        NotificationManager mNotifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = builder.setSmallIcon(R.drawable.exclamation_icon)
                .setContentTitle(title)
                .setColor(Color.argb(255,216,182,42))
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(resultPendingIntent)
                .build();
        mNotifyMgr.notify(ERROR_ID,n);
    }
    public void displayNotification(String title,String text,Intent resultIntent){
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        ctx,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        Notification.Builder builder = new Notification.Builder(ctx);
        NotificationManager mNotifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = builder.setSmallIcon(R.drawable.small_share_icon)
                .setContentTitle(title)
                .setColor(Color.argb(255,216,182,42))
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(resultPendingIntent)
                .build();
        mNotifyMgr.notify(SENT_NOTIFICATION_ID,n);
    }
}
