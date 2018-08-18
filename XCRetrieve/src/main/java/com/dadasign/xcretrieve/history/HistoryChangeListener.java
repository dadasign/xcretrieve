package com.dadasign.xcretrieve.history;

import com.dadasign.xcretrieve.model.TrackingMessage;

/**
 * Created by Jakub on 2015-10-19.
 */
public interface HistoryChangeListener {
    void onEditListItem(TrackingMessage msg);
    void onNewListItem(TrackingMessage msg);
    void onRemoveItem(long id);
    void onClearListItems();
}
