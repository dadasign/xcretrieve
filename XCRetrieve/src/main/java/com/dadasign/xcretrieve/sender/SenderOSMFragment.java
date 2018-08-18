package com.dadasign.xcretrieve.sender;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.sensors.GoogleLocationListener;
import com.dadasign.xcretrieve.sensors.GoogleLocationUpdateListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.List;

public class SenderOSMFragment extends Fragment implements GoogleLocationUpdateListener{
	Button send_pos_btn;

	MapView map;
	private ItemizedIconOverlay<OverlayItem> myOverlay;
	private GoogleLocationListener listener;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

	private Location my_location;
    private Boolean foundLocation=false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.sender_osm_fragment, container,false);
		//setContentView(R.layout.activity_send_position);
		send_pos_btn = (Button)v.findViewById(R.id.share_position);

		if(map==null){
			map = (MapView) v.findViewById(R.id.map);
		}
		if(map!=null){
            List<OverlayItem> pList = new ArrayList<OverlayItem>();
            OnItemGestureListener<OverlayItem> pOnItemGestureListener = new myItemGestureListener<OverlayItem>();
            myOverlay = new ItemizedIconOverlay<OverlayItem>(getActivity(), pList, pOnItemGestureListener);
            map.getOverlays().add(myOverlay);

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(15000);
            mLocationRequest.setSmallestDisplacement(0);

			map.setClickable(true);
	        map.setBuiltInZoomControls(true);
            map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
            listener = new GoogleLocationListener(mLocationRequest,this,this.getContext());


			if(my_location!=null){
				map.getOverlays().add(myOverlay);
		        map.getController().setZoom(14);
				map.getController().setCenter(new GeoPoint(my_location.getLatitude(), my_location.getLongitude()));
			}

		    send_pos_btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
                    ((SenderTabedActivity) getActivity()).sendPosition(my_location);
				}
			});


        }
		return v;
	}
	
	@Override
	public void onResume() {
        listener.connect();
		super.onResume();
	}
	
	@Override
	public void onStop() {
        listener.disconnect();
		super.onStop();
	}
	
	@Override
	public void onPause() {
        listener.disconnect();
		super.onPause();
	}

    @Override
    public void onLocationUpdate(Location location) {
        my_location = location;
        if(map!=null){
            if(!foundLocation){
                foundLocation=true;
                map.getController().setZoom(14);
                map.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
            }
            myOverlay.removeAllItems();
            Drawable marker = getDrawable(R.drawable.pointer2);
            OverlayItem myItem = new OverlayItem("Marker","Description of my Marker",new GeoPoint(location.getLatitude(), location.getLongitude()));
            myItem.setMarker(marker);
            myOverlay.addItem(myItem);
        }
    }

    private Drawable getDrawable(int resId){
        Resources res = getResources();
        if(android.os.Build.VERSION.SDK_INT >= 21){
            return res.getDrawable(resId,null);
        }else{
            return res.getDrawable(resId);
        }
    }

    public class myItemGestureListener<T extends OverlayItem> implements OnItemGestureListener<T> {
		 
	    @Override
	    public boolean onItemSingleTapUp(int index, T item) {
	        return false;
	    }
	 
	    @Override
	    public boolean onItemLongPress(int index, T item) {
	        return false;
	    }
	 
	}
	
}
