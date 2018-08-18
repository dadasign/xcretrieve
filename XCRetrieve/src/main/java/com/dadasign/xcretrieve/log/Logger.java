package com.dadasign.xcretrieve.log;

import com.dadasign.xcretrieve.model.LogMessage;
import com.dadasign.xcretrieve.utils.Persistence;

/**
 * Created by Jakub on 2015-07-20.
 */
public class Logger {
    private final Persistence persistence;

    public  Logger(Persistence _persistence){
        persistence = _persistence;
    }
    public void log(String content){
        log(content, 0);
    }
    public void log(String content, int level){
        LogMessage msg = new LogMessage(content,level);
        persistence.saveLogMessage(msg);
    }
}
