package com.dadasign.xcretrieve;

import android.app.Application;

import com.dadasign.xcretrieve.history.HistoryChangeListener;

/**
 * Created by Jakub on 2015-10-29.
 */
public class XcApplication extends Application{

    HistoryChangeListener historyChangeListener;
    public HistoryChangeListener getHistoryChangeListener(){
        return historyChangeListener;
    }
    public void setHistoryChangeListener(HistoryChangeListener chng){
        historyChangeListener = chng;
    }
}
