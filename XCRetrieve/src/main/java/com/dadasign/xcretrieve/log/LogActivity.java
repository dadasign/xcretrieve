package com.dadasign.xcretrieve.log;

import android.os.Bundle;
import android.widget.ListView;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.model.LogMessage;

import java.util.List;

public class LogActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        LogMessage msg = new LogMessage("Log was opened",1);
        persistence.saveLogMessage(msg);
        List<LogMessage> messages = persistence.getLogMessages();
        ListView list = (ListView)findViewById(R.id.logList);

        LogMessagesAdapter adapter = new LogMessagesAdapter(this,R.layout.log_list_item, R.id.log_message, messages);
        list.setAdapter(adapter);
    }
}
