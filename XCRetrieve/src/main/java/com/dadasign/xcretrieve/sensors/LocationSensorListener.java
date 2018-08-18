package com.dadasign.xcretrieve.sensors;

import android.location.Location;

/**
 * Created by Jakub on 2015-09-13.
 */
public interface LocationSensorListener {
    void updateStatus(int status);
    void onLocationComplete(Location[] locations,boolean isGPSBased);
}
