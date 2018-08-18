package com.dadasign.xcretrieve.history;

import com.dadasign.xcretrieve.model.TrackingMessage;

/**
 * Created by Jakub on 2015-09-07.
 */
public interface DetailHandler {
    void displayDetailItem(TrackingMessage item);
    void setDetailItem(TrackingMessage item);
    TrackingMessage getDetailItem();
}
