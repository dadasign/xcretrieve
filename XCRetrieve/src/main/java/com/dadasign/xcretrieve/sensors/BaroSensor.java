package com.dadasign.xcretrieve.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.dadasign.xcretrieve.utils.LocationMessageFormatter;

/**
 * Created by Jakub on 2015-09-22.
 */
public class BaroSensor {
    private static double REFFERENCE_PRESSURE = 1013.25;
    private SensorManager sensorManager;
    private LocationMessageFormatter msgformatter;
    private BaroSensorListener listener;

    public BaroSensor(SensorManager _sensorManager, LocationMessageFormatter _msgformatter,BaroSensorListener _listener){
        sensorManager = _sensorManager;
        msgformatter = _msgformatter;
        listener = _listener;
    }
    public void checkAltitude(){
        final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(sensor!=null){
            final SensorEventListener sel = new SensorEventListener() {

                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    float pressure = sensorEvent.values[0];
                    int alt = ((Float) sensorManager.getAltitude((float) REFFERENCE_PRESSURE, pressure)).intValue();
                    Log.v("BaroSensor", "Got barometric altitude: "+alt);
                    listener.onBaroListenerResult(alt,msgformatter.getBaroAltitudeMessage(alt));
                    sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };
            sensorManager.registerListener(sel, sensor, 500);

        }
    }
}
