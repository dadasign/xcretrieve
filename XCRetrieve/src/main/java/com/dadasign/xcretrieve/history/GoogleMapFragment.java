package com.dadasign.xcretrieve.history;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapFragment extends Fragment implements DetailHandler, OnMapReadyCallback{
    Button map_btn;
    Button satellite_btn;
    Button terrain_btn;
    Button hybrid_btn;
    Button google_maps_btn;
    Button directions_btn;
    TrackingMessage trackingMessage;

    GoogleMap map;
    private MyMapFragment mMapFragment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = (View) inflater.inflate(R.layout.reader_map_fragment,
                container, false);
        map_btn = (Button) v.findViewById(R.id.map_btn);
        satellite_btn = (Button) v.findViewById(R.id.satelite_btn);
        terrain_btn = (Button) v.findViewById(R.id.terrain_btn);
        hybrid_btn = (Button) v.findViewById(R.id.hybrid_btn);
        google_maps_btn = (Button) v.findViewById(R.id.google_maps_btn);
        directions_btn = (Button) v.findViewById(R.id.get_directions_btn);
        if(mMapFragment==null){
            mMapFragment = new MyMapFragment();//SupportMapFragment.newInstance();
            FragmentTransaction fragmentTransaction =
                    getChildFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.google_map, mMapFragment);
            fragmentTransaction.commit();
        }
        mMapFragment.getMapAsync(this);
        return v;
    }
    private void selectMapMode(Button b){
        map_btn.setSelected(false);
        satellite_btn.setSelected(false);
        hybrid_btn.setSelected(false);
        terrain_btn.setSelected(false);
        b.setSelected(true);
    }

    @Override
    public void displayDetailItem(TrackingMessage item) {
        setDetailItem(item);
        if(map!=null  && item!=null && item.lat !=null && item.lng!=null){
            map.clear();
            displayLocationMarker();
        }
    }

    private void displayLocationMarker() {
        LatLng ltlg = new LatLng(trackingMessage.lat,trackingMessage.lng);
        float zoom = 15;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ltlg, zoom));
        try{
            map.setMyLocationEnabled(true);
        }catch (SecurityException e){

        }
        MarkerOptions mo = new MarkerOptions();
        mo.position(ltlg);
        mo.title(getString(R.string.last_position));
        map.addMarker(mo);
    }

    @Override
    public void setDetailItem(TrackingMessage item) {
        trackingMessage = item;
    }

    @Override
    public TrackingMessage getDetailItem() {
        return trackingMessage;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map_btn.setSelected(true);
        map_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!map_btn.isSelected()){
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    selectMapMode(map_btn);
                }
            }
        });
        satellite_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!satellite_btn.isSelected()){
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    selectMapMode(satellite_btn);
                }
            }
        });
        hybrid_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hybrid_btn.isSelected()){
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    selectMapMode(hybrid_btn);
                }
            }
        });
        terrain_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!terrain_btn.isSelected()){
                    map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    selectMapMode(terrain_btn);
                }
            }
        });

        google_maps_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trackingMessage!=null && trackingMessage.lng!=null && trackingMessage.lat!=null) {
                    Uri uri;
                    uri = Uri.parse("geo:" + trackingMessage.lat + "," + trackingMessage.lng + "?q=" + trackingMessage.lat + "," + trackingMessage.lng + "(" + getString(R.string.received_position) + ")");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
        directions_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trackingMessage!=null && trackingMessage.lng!=null && trackingMessage.lat!=null) {
                    Location myLoc = map.getMyLocation();
                    if (myLoc != null) {
                        Uri uri = Uri
                                .parse("http://maps.google.com/maps?saddr="
                                        + myLoc.getLatitude() + ","
                                        + myLoc.getLongitude() + "&daddr="
                                        + trackingMessage.lat + "," + trackingMessage.lng);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                }
            }
        });

        if(trackingMessage!=null && trackingMessage.lng!=null && trackingMessage.lat!=null) {
            displayLocationMarker();
        }
        map.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }
            @Override
            public View getInfoContents(Marker arg0) {
                return null;
            }
        });
        PackageManager pm = getActivity().getPackageManager();
        boolean hasMultitouch = pm
                .hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
        if (hasMultitouch) {
            map.getUiSettings().setZoomControlsEnabled(false);
        }
    }
}

