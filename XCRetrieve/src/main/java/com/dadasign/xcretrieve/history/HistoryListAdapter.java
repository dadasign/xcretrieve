package com.dadasign.xcretrieve.history;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dadasign.xcretrieve.Contact;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.utils.Persistence;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryListAdapter extends ArrayAdapter<TrackingMessage>  {
    private final Persistence persistence;
    //private List<TrackingMessage> messages = new ArrayList<TrackingMessage>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yy.MM.dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static int DB_REQUEST_LIMIT = 30;
    private boolean hasMore = true;
    public DetailHandler detailHandler;
    public HistoryListAdapter(Context context, int resource, int textViewResourceId, Persistence _persistence, DetailHandler _detailHandler) {
        super(context, resource, textViewResourceId);
        persistence = _persistence;
        List<TrackingMessage> more = persistence.getTrackingMessages(DB_REQUEST_LIMIT,0);
        if(more.size()<DB_REQUEST_LIMIT){
            hasMore=false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.addAll(more);
        }else{
            for(int c=0; c<more.size(); c++){
                this.add(more.get(c));
            }
        }

        detailHandler = _detailHandler;
        if(detailHandler!=null && detailHandler.getDetailItem()==null){
            Log.v("HistoryListAdapter", "Assign first item to the detail handler.");
            if(more.size()>0) {
                detailHandler.setDetailItem(more.get(0));
            }
        }else{
            Log.v("HistoryListAdapter","Got item from the detail handler.");
        }
    }
    private void setDrawable(ImageView imageView, int resourceId){
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            imageView.setImageDrawable(getContext().getResources().getDrawable(resourceId, null));
        }else{
            imageView.setImageDrawable(getContext().getResources().getDrawable(resourceId));
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if(hasMore && position >= this.getCount()-2){
            loadMore();
        }
        if(convertView == null) {
            convertView = new HistoryItemView(getContext());
        }
        TextView msgView = (TextView) convertView.findViewById(R.id.tracking_content);
        TextView titleView = (TextView) convertView.findViewById(R.id.title);
        TextView timeView = (TextView) convertView.findViewById(R.id.time);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.status_icon);
        final TrackingMessage msg = this.getItem(position);
        Date t = new Date(msg.timeCreated);
        if(msg.messageType!=null && msg.messageType==TrackingMessage.MSG_TYPE_RECEIVED){
            setDrawable(imageView,R.drawable.historia_received);
            msgView.setText(getContext().getResources().getString(R.string.from) +" "+ msg.requestFrom);
        }else if(msg.messageType!=null && msg.messageType==TrackingMessage.MSG_TYPE_SHARE){
            msgView.setText("");
            setDrawable(imageView,R.drawable.historia_shared);
        }else{
            if(msg.messageType==TrackingMessage.MSG_TYPE_ONE_CLICK_SHARE){
                if(msg.sentTo==null || msg.sentTo.indexOf("(")==-1 || msg.sentTo.indexOf(")")==-1) {
                    msgView.setText(getContext().getResources().getString(R.string.to) + " " + msg.sentTo);
                }else{
                    String sentTo = msg.sentTo.substring(msg.sentTo.indexOf("(")+1,msg.sentTo.indexOf(")"));
                    msgView.setText(getContext().getResources().getString(R.string.to) + " " + sentTo);
                }
            }else {
                msgView.setText(getContext().getResources().getString(R.string.to) + " " + msg.requestFrom);
            }
            if(msg.messageType!=null && msg.messageType==TrackingMessage.MSG_TYPE_TEST){
                msgView.setText(msg.requestFrom);
                setDrawable(imageView,R.drawable.historia_received);
            }else if(msg.messageType!=null && (msg.messageType==TrackingMessage.MSG_TYPE_SMS_REQ || msg.messageType==TrackingMessage.MSG_TYPE_ONE_CLICK_SHARE || msg.messageType==TrackingMessage.MSG_TYPE_SMS_REQ_INSTANT)){
                if(msg.status==TrackingMessage.STATUS_SENT_FAILED || msg.status==TrackingMessage.STATUS_LOC_FAILED){
                    setDrawable(imageView,R.drawable.historia_failed);
                }else {
                    setDrawable(imageView,R.drawable.historia_sent);
                }
            }
        }
        titleView.setText(dateFormat.format(t));//+"("+msg.messageType+")");
        timeView.setText(timeFormat.format(t));

        return convertView;
    }

    private void loadMore() {
        int oldSize =this.getCount();// messages.size();
        List<TrackingMessage> more = persistence.getTrackingMessages(DB_REQUEST_LIMIT, oldSize);
        if(more.size()==0){
            hasMore=false;
        } else{
            //messages.addAll(more);
            this.addAll(more);
            notifyDataSetChanged();
        }
    }
}
