package com.dadasign.xcretrieve.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import com.dadasign.xcretrieve.utils.LocationMessageFormatter;

import java.util.ArrayList;

/**
 * Created by Jakub on 2015-09-19.
 */
public class MovementSensor {
    private SensorManager sensorManager;
    private LocationMessageFormatter msgformatter;
    private float[] linear_acceleration = new float[3];
    private float[] gravity;
    private ArrayList<Float> g_list = new ArrayList<Float>();
    private long last_update=0;
    private MovementSensorListener listener;

    public MovementSensor(SensorManager _sensorManager, LocationMessageFormatter _msgformatter,MovementSensorListener _listener){
        sensorManager = _sensorManager;
        msgformatter = _msgformatter;
        listener = _listener;
    }
    public void checkMovement(){
        final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensor!=null){
            SensorEventListener sel = new SensorEventListener() {
                private int test_samples = 5;
                private float[] max_values=new float[3];
                private float[] min_values=new float[3];
                @Override
                public void onSensorChanged(SensorEvent event) {
                    final float alpha = (float) .8;
                    if(gravity==null){
                        gravity=event.values;
                        min_values[0]=9999;
                        min_values[1]=9999;
                        min_values[2]=9999;
                        max_values[0]=-9999;
                        max_values[1]=-9999;
                        max_values[2]=-9999;
                    }else{
                        if(SystemClock.elapsedRealtime()>last_update+500){
                            max_values[0]=Math.max(event.values[0], max_values[0]);
                            max_values[1]=Math.max(event.values[1], max_values[1]);
                            max_values[2]=Math.max(event.values[2], max_values[2]);

                            min_values[0]=Math.min(event.values[0], min_values[0]);
                            min_values[1]=Math.min(event.values[1], min_values[1]);
                            min_values[2]=Math.min(event.values[2], min_values[2]);

                            // Isolate the force of gravity with the low-pass filter.
                            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
                            // Remove the gravity contribution with the high-pass filter.
                            linear_acceleration[0] = event.values[0] - gravity[0];
                            linear_acceleration[1] = event.values[1] - gravity[1];
                            linear_acceleration[2] = event.values[2] - gravity[2];

                            last_update=SystemClock.elapsedRealtime();
                            if(test_samples>0){
                                test_samples--;
                            }else{
                                Float total_g = ((float) Math.round(Math.abs(linear_acceleration[0])+Math.abs(linear_acceleration[1])+Math.abs(linear_acceleration[2])*100));
                                g_list.add(total_g);

                                if(g_list.size()==100){
                                    float total=0;
                                    for(int c=0; c<100; c++){
                                        total+=g_list.get(c);
                                    }
                                    total=total/100;
                                    float total2 = (max_values[0]-min_values[0])+(max_values[1]-min_values[1])+(max_values[2]-min_values[2]);
                                    Log.v("Location Tracker", "Total G:" + total + "," + total2);
                                    sensorManager.unregisterListener(this, sensor);
                                    if(listener!=null){
                                        listener.onMovementIndexCalculated(msgformatter.getMovementIndex(total,total2),msgformatter.getMovementIndexMessagePart(total,total2));
                                    }
                                }

                            }
                        }

                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorManager.registerListener(sel, sensor, 500);
        }
    }
}
