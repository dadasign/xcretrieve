package com.dadasign.xcretrieve.sender;

import java.util.Locale;

import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.sensors.GoogleLocationListener;
import com.dadasign.xcretrieve.sensors.GoogleLocationUpdateListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SenderGoogleMapFragment extends Fragment implements OnMapReadyCallback, GoogleLocationUpdateListener {
	Button send_pos_btn;
	Button map_btn;
	Button satelite_btn;
	Button terrain_btn;
	Button hybrid_btn;
    MyMapFragment mapFrag;
    GoogleLocationListener locationListener;
    private Location myLocation;
	
	GoogleMap map;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.sender_google_map_fragment, container, false);


        map_btn = (Button) v.findViewById(R.id.map_btn);
        satelite_btn = (Button) v.findViewById(R.id.satelite_btn);
        terrain_btn = (Button) v.findViewById(R.id.terrain_btn);
        hybrid_btn = (Button) v.findViewById(R.id.hybrid_btn);
        send_pos_btn = (Button) v.findViewById(R.id.share_position);

        if (mapFrag == null) {
            mapFrag = new MyMapFragment();

            FragmentTransaction fragmentTransaction =
                    getChildFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.google_map, mapFrag);
            fragmentTransaction.commit();
        }
        mapFrag.getMapAsync(this);
        return v;
    }
    private void manageMap(){
		if(map!=null){
            LocationRequest request = new LocationRequest();
            request.setInterval(15000);
            request.setSmallestDisplacement(0);

            locationListener = new GoogleLocationListener(request, this,getContext());
			locationListener.connect();
			map_btn.setSelected(true);
			//Buttons
			map_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!map_btn.isSelected()){
						map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
						selectMapMode(map_btn);
					}
				}
			});
			satelite_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!satelite_btn.isSelected()){
						map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
						selectMapMode(satelite_btn);
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
			//float zoom = 15;
			//map.moveCamera(CameraUpdateFactory.newLatLngZoom(ltlg, zoom));
			PackageManager pm = getActivity().getPackageManager();
		    boolean hasMultitouch = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
		    if (hasMultitouch) {
		        map.getUiSettings().setZoomControlsEnabled(false);
		    } else {
		        // set zoom buttons
		    }
		    send_pos_btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(map!=null){
						if(myLocation!=null){
                            ((SenderTabedActivity) getActivity()).sendPosition(myLocation);
						}else{
							Toast.makeText(getActivity(), getString(R.string.wait_for_gps), Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
		}

	}
	private void selectMapMode(Button b){
		map_btn.setSelected(false);
		satelite_btn.setSelected(false);
		hybrid_btn.setSelected(false);
		terrain_btn.setSelected(false);
		b.setSelected(true);
	}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        manageMap();
    }

    @Override
    public void onLocationUpdate(Location location) {
        if(myLocation==null || myLocation.getAccuracy()>1000) {
            LatLng ltlg = new LatLng(location.getLatitude(), location.getLongitude());
            float zoom = 12;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ltlg, zoom));
            try {
                map.setMyLocationEnabled(true);
            }catch (SecurityException e){

            }

        }
        myLocation = location;
    }
}
