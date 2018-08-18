package com.dadasign.xcretrieve.history;

import android.os.Bundle;
import android.os.Message;

import com.dadasign.xcretrieve.model.TrackingMessage;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Created by Jakub on 2017-06-12.
 */
public class HistoryListFragmentTest extends TestCase {
    HistoryListFragment historyListFragment;
    @Mock
    DetailHandler detailHandler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        historyListFragment = new HistoryListFragment();
    }

    public void testOnNewListItem_WithoutOnCreate() throws Exception {
        historyListFragment.detailHandler = detailHandler;
        Bundle bundle = Mockito.mock(Bundle.class);
        historyListFragment.onCreate(bundle);
        TrackingMessage msg = Mockito.mock(TrackingMessage.class);
        historyListFragment.onNewListItem(msg);
    }

}