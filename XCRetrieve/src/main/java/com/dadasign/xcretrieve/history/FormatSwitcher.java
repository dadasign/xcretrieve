package com.dadasign.xcretrieve.history;

import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Jakub on 2015-10-15.
 */
public class FormatSwitcher {
    private int current_format=0;
    private Button button;
    private TextView latView;
    private TextView lngView;
    private double lat;
    private double lng;

    public FormatSwitcher(Button _button, TextView _latView, TextView _lngView, double _lat, double _lng){
        button = _button;
        latView = _latView;
        lngView = _lngView;
        setCoords(_lat,_lng);
    }
    public void setCoords(double _lat, double _lng){
        lat = _lat;
        lng = _lng;
    }
    public void registerFormatSwitcher(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_format++;
                if(current_format>2){
                    current_format=0;
                }

                String lat = getCurrentLat();
                String lng = getCurrentLng();
                latView.setText(lat);
                lngView.setText(lng);
            }
        });
    }
    public String getCurrentLat(){
        if(current_format==0){
            return Double.toString(lat);
        }else if(current_format==1){
            return Location.convert(lat,Location.FORMAT_MINUTES);
        }else{
            return Location.convert(lat,Location.FORMAT_SECONDS);
        }
    }
    public String getCurrentLng(){
        if(current_format==0){
            return Double.toString(lng);
        }else if(current_format==1){
            return Location.convert(lng,Location.FORMAT_MINUTES);
        }else{
            return Location.convert(lng,Location.FORMAT_SECONDS);
        }
    }
}
