package com.dadasign.xcretrieve.history;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.utils.BearingToName;
import com.dadasign.xcretrieve.utils.LocationMessageFormatter;
import com.dadasign.xcretrieve.utils.MovementIndexToName;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.datatype.Duration;

/**
 * Created by Jakub on 2015-07-19.
 */
public class HistoryDetailsFragment extends Fragment implements DetailHandler {
    private HistoryListAdapter adapter;
    private TrackingMessage trackingMsg;
    private TextView relative_pos_txt;
    private BearingToName bearingToName;
    private FormatSwitcher formatSwitcher;
    private LocationManager locationManager;
    private LocationListener listener;
    private static final String timeFormat = "yyyy.MM.dd HH:mm:ss";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = (View) inflater.inflate(R.layout.history_details,
                container, false);
        if(trackingMsg!=null){
            this.displayDetailItem(trackingMsg, v);
        }
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(locationManager!=null && listener!=null) {
            locationManager.removeUpdates(listener);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(locationManager!=null && listener !=null &&
                (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            locationManager.removeUpdates(listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, listener);
        }
    }

    @Override
    public void onDestroyView() {
        if(locationManager!=null && listener!=null){
            locationManager.removeUpdates(listener);
            locationManager = null;
            listener = null;
        }
        super.onDestroyView();
    }

    public void displayDetailItem(final TrackingMessage item, final View v) {
        if(locationManager!=null && listener!=null) {
            locationManager.removeUpdates(listener);
        }
        bearingToName = new BearingToName();
        trackingMsg = item;
        relative_pos_txt = (TextView)v.findViewById(R.id.relative_pos_text);

        TextView from = (TextView) v.findViewById(R.id.from_text);
        if(item.requestFrom!=null) {
            from.setVisibility(View.VISIBLE);
            from.setText(item.requestFrom);
            TextView dateText = (TextView) v.findViewById(R.id.date_text);
            Date t = new Date(trackingMsg.timeCreated);
            SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat);
            dateText.setText(dateFormat.format(t));
        }else if(item.sentTo!=null){
            from.setVisibility(View.VISIBLE);
            from.setText(item.sentTo);
            TextView dateText = (TextView) v.findViewById(R.id.date_text);
            Date t = new Date(trackingMsg.timeCreated);
            SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat);
            dateText.setText(dateFormat.format(t));
        }else{
            from.setVisibility(View.GONE);
        }
        boolean hasPosition = true;
        TextView lat = (TextView) v.findViewById(R.id.lat_text);
        if(item.lat!=null) {
            lat.setText(item.lat.toString());
        }else{
            lat.setText("");
            hasPosition=false;
        }
        TextView lng = (TextView) v.findViewById(R.id.lon_text);
        if(item.lng!=null) {
            lng.setText(item.lng.toString());
        }else{
            lng.setText("");
            hasPosition = false;
        }
        TextView fromLabel = (TextView) v.findViewById(R.id.from_label);
        if(item.messageType != null && (item.messageType == TrackingMessage.MSG_TYPE_SMS_REQ || item.messageType == TrackingMessage.MSG_TYPE_ONE_CLICK_SHARE || item.messageType == TrackingMessage.MSG_TYPE_SMS_REQ_INSTANT)){
            fromLabel.setText(R.string.to);
        }else{
            fromLabel.setText(R.string.from);
        }

        if(
                hasPosition &&
                    (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        ){
            relative_pos_txt.setVisibility(View.VISIBLE);
            relative_pos_txt.setText(getString(R.string.waiting_for_gps));

            if(locationManager==null) {
                locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            }
            listener = new LocationListener() {
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
                @Override
                public void onLocationChanged(Location location) {
                    showRelativeLocation(location);
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, listener);
            v.findViewById(R.id.relative_pos).setVisibility(View.VISIBLE);
            v.findViewById(R.id.copy_btn).setVisibility(View.VISIBLE);
        }else{
            v.findViewById(R.id.relative_pos).setVisibility(View.GONE);
            v.findViewById(R.id.copy_btn).setVisibility(View.GONE);
        }
        View statusHolder = v.findViewById(R.id.message_status);
        if(item.status!=null){
            statusHolder.setVisibility(View.VISIBLE);
            TextView statusText = (TextView) v.findViewById(R.id.status_text);
            if(item.status == TrackingMessage.STATUS_COMPLETE){
                statusHolder.setVisibility(View.GONE);
            }else {
                statusHolder.setVisibility(View.VISIBLE);
                if (item.status == TrackingMessage.STATUS_LOC_FAILED) {
                    statusText.setText(R.string.status_loc_failed);
                } else if (item.status == TrackingMessage.STATUS_SENT_FAILED) {
                    statusText.setTextColor(getActivity().getResources().getColor(R.color.red));
                    statusText.setText(R.string.status_sent_loc_failed);
                } else if (item.status == TrackingMessage.STATUS_LOC_COMPLETE) {
                    statusText.setTextColor(getActivity().getResources().getColor(R.color.black));
                    statusText.setText(R.string.status_loc_complete);
                } else if (item.status == TrackingMessage.STATUS_LOC_IN_PROGRESS) {
                    statusText.setTextColor(getActivity().getResources().getColor(R.color.orange));
                    statusText.setText(R.string.status_loc_in_progress);
                } else if (item.status == TrackingMessage.STATUS_SENDING) {
                    statusText.setTextColor(getActivity().getResources().getColor(R.color.orange));
                    statusText.setText(R.string.status_sending);
                } else {
                    statusText.setTextColor(getActivity().getResources().getColor(R.color.red));
                    statusText.setText("("+item.status+")");
                }
            }
        }else{
            statusHolder.setVisibility(View.GONE);
        }
        if(item.alt!=null) {
            v.findViewById(R.id.alt).setVisibility(View.VISIBLE);
            TextView alt = (TextView) v.findViewById(R.id.alt_text);
            alt.setText(item.alt.toString());
        }else{
            v.findViewById(R.id.alt).setVisibility(View.GONE);
        }
        if(item.direction!=null) {
            v.findViewById(R.id.direction).setVisibility(View.VISIBLE);
            TextView dir = (TextView) v.findViewById(R.id.direction_text);
            dir.setText(bearingToName.convertBearing(item.direction));
        }else{
            v.findViewById(R.id.direction).setVisibility(View.GONE);
        }
        if(item.altBaro!=null) {
            v.findViewById(R.id.alt_baro).setVisibility(View.VISIBLE);
            TextView altb = (TextView) v.findViewById(R.id.alt_baro_text);
            altb.setText(item.altBaro.toString());
        }else{
            v.findViewById(R.id.alt_baro).setVisibility(View.GONE);
        }
        if(item.movementIndex!=null) {
            v.findViewById(R.id.movement).setVisibility(View.VISIBLE);
            TextView mov = (TextView) v.findViewById(R.id.movement_text);
            MovementIndexToName movementIndexToName = new MovementIndexToName(getActivity());
            mov.setText(movementIndexToName.getMovementName(item.movementIndex));
        }else{
            v.findViewById(R.id.movement).setVisibility(View.GONE);
        }
        if(item.accuracy!=null){
            v.findViewById(R.id.location_accuracy).setVisibility(View.VISIBLE);
            TextView accu = (TextView) v.findViewById(R.id.location_accuracy_text);
            if(item.accuracy!=0) {
                accu.setText(item.accuracy.toString() + "m");
            }else{
                accu.setText("-");
            }
        }else{
            v.findViewById(R.id.location_accuracy).setVisibility(View.GONE);
        }
        if(item.gpsUsed!=null) {
            v.findViewById(R.id.source).setVisibility(View.VISIBLE);
            TextView source = (TextView) v.findViewById(R.id.source_text);
            source.setText(item.gpsUsed ? getActivity().getText(R.string.source_gps) : getActivity().getText(R.string.source_network));
        }else{
            v.findViewById(R.id.source).setVisibility(View.GONE);
        }
        if(item.speed!=null) {
            v.findViewById(R.id.speed).setVisibility(View.VISIBLE);
            TextView source = (TextView) v.findViewById(R.id.speed_text);
            source.setText(item.speed.toString() + " " + getActivity().getText(R.string.km_h));
        }else{
            v.findViewById(R.id.speed).setVisibility(View.GONE);
        }
        final View copy_menu = v.findViewById(R.id.copy_menu);
        copy_menu.setVisibility(View.GONE);
        Button copy_button =(Button) v.findViewById(R.id.copy_btn);
        copy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("HistoryDetailsFragment", "Show copy menu");
                if(copy_menu.getVisibility()!=View.VISIBLE){
                    copy_menu.setVisibility(View.VISIBLE);
                }else{
                    copy_menu.setVisibility(View.GONE);
                }
                /*((Button) v.findViewById(R.id.copy_coords_btn)).setText(trackingMsg.lat.toString() + ", " + trackingMsg.lng.toString());
                ((Button) v.findViewById(R.id.copy_lat_btn)).setText(trackingMsg.lat.toString());
                ((Button) v.findViewById(R.id.copy_lng_btn)).setText(trackingMsg.lng.toString());*/
            }
        });
        Button lat_btn = (Button) v.findViewById(R.id.copy_lat_btn);
        Button lng_btn = (Button) v.findViewById(R.id.copy_lng_btn);
        Button both_btn = (Button) v.findViewById(R.id.copy_coords_btn);
        lat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToClipboard(formatSwitcher.getCurrentLat());
            }
        });
        lng_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToClipboard(formatSwitcher.getCurrentLng());
            }
        });
        both_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToClipboard(formatSwitcher.getCurrentLat() + ", " + formatSwitcher.getCurrentLng());
            }
        });
        if(hasPosition) {
            Button changeFormatBtn = (Button) v.findViewById(R.id.change_format_btn);
            formatSwitcher = new FormatSwitcher(changeFormatBtn,lat,lng,trackingMsg.lat,trackingMsg.lng);
            formatSwitcher.registerFormatSwitcher();
        }
        Button map_btn = (Button) v.findViewById(R.id.maps_btn);
        if(hasPosition) {
            map_btn.setVisibility(View.VISIBLE);
            map_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri;
                    uri = Uri.parse("geo:" + item.lat + "," + item.lng + "?q=" + item.lat + "," + item.lng);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }else{
            map_btn.setVisibility(View.GONE);
        }
    }
    private void sendToClipboard(String msg){
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(msg, msg);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(),getActivity().getString(R.string.copy_success)+" \""+msg+"\"", Toast.LENGTH_SHORT).show();
    }
    private void showRelativeLocation(Location location){
        if(trackingMsg==null || trackingMsg.lat==null || trackingMsg.lng == null){
            return;
        }
        Location targetLoc=new Location(location);
        targetLoc.setLatitude(trackingMsg.lat);
        targetLoc.setLongitude(trackingMsg.lng);

        float distance = location.distanceTo(targetLoc);
        if(distance<1){
            relative_pos_txt.setText(getString(R.string.same_place));
        }else{
            int dir = Math.round(location.bearingTo(targetLoc));
            String distanceString;
            if(distance<1000){
                distanceString = Math.round(distance)+"m";
            }else if(distance<10000){
                distanceString = (Math.round(distance/10)/100)+"km";
            }else if(distance<100000){
                distanceString = (Math.round(distance/100)/10)+"km";
            }else{
                distanceString = Math.round(distance/1000)+"km";
            }
            relative_pos_txt.setText(distanceString+" "+bearingToName.convertBearing(dir));
        }
    }
    @Override
    public void displayDetailItem(TrackingMessage item) {
        View v = getView();
        if(v==null){
            trackingMsg = item;
            return;
        }
        this.displayDetailItem(item,v);
    }

    @Override
    public void setDetailItem(TrackingMessage item) {
        this.displayDetailItem(item);
    }

    @Override
    public TrackingMessage getDetailItem() {
        return trackingMsg;
    }
}
