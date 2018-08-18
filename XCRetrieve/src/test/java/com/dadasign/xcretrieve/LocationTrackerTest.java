package com.dadasign.xcretrieve;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.telephony.SmsManager;

import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.utils.ContactHelper;
import com.dadasign.xcretrieve.utils.Persistence;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Created by Jakub on 2017-06-11.
 */
public class LocationTrackerTest extends TestCase {
    @Mock
    Service ctx;
    String from="";
    String content="";
    @Mock
    ResultReceiver target;
    @Mock
    Handler h;
    @Mock
    ContactHelper contactHelper;
    @Mock
    Persistence persistence;
    @Mock
    SmsManager smsManager;

    LocationTracker locationTracker;

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }
    @Test
    public void testOnLocationComplete_withTargetAndEmptyLocations() throws Exception {
        Location[] locations = new Location[]{};
        locationTracker = new LocationTracker(ctx, persistence,new SmsBroadcastReceiverFactory(),smsManager, from, content, target, h, contactHelper, false);
        locationTracker.onLocationComplete(locations,false);
        verify(target).send(anyInt(),any(Bundle.class));
    }
    @Test
    public void testOnLocationComplete_withoutTargetAndEmptyLocations() throws Exception {
        when(ctx.getString(R.string.sent_position_content)).thenReturn("[content][time][user]");
        when(ctx.getString(R.string.failed_msg)).thenReturn("Failed");

        Location[] locations = new Location[]{};
        locationTracker = new LocationTracker(ctx, persistence,new SmsBroadcastReceiverFactory(),smsManager, from, content, null, h, contactHelper, false);
        locationTracker.onLocationComplete(locations,false);
        ArgumentCaptor<TrackingMessage> argumentCaptor = ArgumentCaptor.forClass(TrackingMessage.class);
        verify(persistence,atLeastOnce()).saveTrackingMessage(argumentCaptor.capture());
        assertNotNull("A response message was assigned.",argumentCaptor.getAllValues().get(argumentCaptor.getAllValues().size()-1).responseMessage);
    }

}