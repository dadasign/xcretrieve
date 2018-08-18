package com.dadasign.xcretrieve.history;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dadasign.xcretrieve.XcApplication;
import com.dadasign.xcretrieve.model.TrackingMessage;

/**
 * Created by Jakub on 2015-10-19.
 */
public class HistoryUpdateBroadcastReceiver extends BroadcastReceiver {
    public static String HISTORY_UPDATE_FILTER = "history_update";
    public static String REMOVE_ALL_ACTION = "remove_all";
    public static String REMOVE_ACTION = "remove_message";
    public static String UPDATE_ACTION = "update_message";
    public static String INSERT_ACTION = "insert_message";
    @Override
    public void onReceive(Context context, Intent intent) {
        XcApplication app = (XcApplication) context;
        HistoryChangeListener act = app.getHistoryChangeListener();
        if(act!=null) {
            if (intent.hasExtra(UPDATE_ACTION)) {
                TrackingMessage msg = new TrackingMessage();
                msg.fromBundle(intent.getBundleExtra(UPDATE_ACTION));
                act.onEditListItem(msg);
            } else if (intent.hasExtra(INSERT_ACTION)) {
                TrackingMessage msg = new TrackingMessage();
                msg.fromBundle(intent.getBundleExtra(INSERT_ACTION));
                act.onNewListItem(msg);
            } else if (intent.hasExtra(REMOVE_ALL_ACTION)) {
                act.onClearListItems();
            } else if (intent.hasExtra(REMOVE_ACTION)) {
                act.onRemoveItem(intent.getLongExtra(REMOVE_ACTION,0));
            }
        }
    }
}
