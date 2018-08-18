package com.dadasign.xcretrieve.model;

public class LogMessage {

    public String message;
    public long time;
    public int level;

    public LogMessage(String _msg, int _level){
        build(_msg,_level,System.currentTimeMillis());
    }
    public LogMessage(String _msg, int _level, long _time){
        build(_msg,_level,_time);
    }
    private void build(String _msg, int _level, long _time){
        time = _time;
        level = _level;
        message = _msg;
    }
}
