package com.dadasign.xcretrieve.log;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.model.LogMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Jakub on 2015-06-22.
 */
public class LogMessagesAdapter extends ArrayAdapter<LogMessage> {
    private List<LogMessage> logMessages;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public LogMessagesAdapter(Context context, int resource, int textViewResourceId, List<LogMessage> objects) {
        super(context, resource, textViewResourceId, objects);
        logMessages = objects;
        if(logMessages.size()==0){
            logMessages.add(new LogMessage("No messages here yet",1));
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View out = super.getView(position, convertView, parent);
        TextView time = (TextView) out.findViewById(R.id.time);
        TextView msgView = (TextView) out.findViewById(R.id.log_message);
        LogMessage msg = logMessages.get(position);
        Date t = new Date(msg.time);
        time.setText(dateFormat.format(t));
        if(msg.level==0) {
            msgView.setTextColor(R.color.text_light_grey);
        }else if(msg.level==1){
            msgView.setTextColor(R.color.text_grey);
        }else if(msg.level==2){
            msgView.setTextColor(R.color.orange);
        }else{
            msgView.setTextColor(R.color.red);
        }
        msgView.setText(msg.message);
        return out;
    }
}
