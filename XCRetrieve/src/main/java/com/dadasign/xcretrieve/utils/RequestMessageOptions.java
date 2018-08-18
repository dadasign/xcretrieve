package com.dadasign.xcretrieve.utils;

/**
 * Created by Jakub on 2015-10-13.
 */
public class RequestMessageOptions {
    private String content;
    public boolean singleLocationOnly=false;
    public void parseMessage(String _content){
        content = _content;
        if(content==null){
            return;
        }
        String low_content = content.toLowerCase();
        if(low_content.contains("-instant") || low_content.contains("-i")){
            singleLocationOnly = true;
        }
    }
}
