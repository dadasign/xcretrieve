package com.dadasign.xcretrieve.history;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.model.TrackingMessage;

public class OSMFragment extends Fragment implements DetailHandler{
    double lat;
    double lng;

    private MapView mapView;
    private ItemizedIconOverlay<OverlayItem> myOverlay;
    private LocationListener mlocListener;
    private LocationManager mlocManager;
    private TrackingMessage trackingMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.reader_osm_fragment,
                container, false);

        mapView = (MapView) v.findViewById(R.id.map);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.getController().setZoom(14);
        if(trackingMessage!=null && trackingMessage.lat!=null && trackingMessage.lng!=null){
            updateLocationMarker();
        }
        return v;
    }

    @Override
    public void onResume() {
        if(mlocListener==null){
            mlocListener = new MyLocationListener(mapView.getController(), myOverlay);
        }
        if(mlocManager==null){
            mlocManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 10, mlocListener);
            Location loc = mlocManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if(loc!=null){
                mlocListener.onLocationChanged(loc);
            }
        }catch(SecurityException e){

        }
        super.onResume();
    }

    @Override
    public void onPause() {
        try{
            mlocManager.removeUpdates(mlocListener);
        }catch(SecurityException e){

        }
        super.onPause();
    }

    @Override
    public void onStop() {
        try{
            mlocManager.removeUpdates(mlocListener);
        }catch(SecurityException e){

        }
        super.onStop();
    }

    @Override
    public void displayDetailItem(TrackingMessage item) {
        setDetailItem(item);
        if(item!=null && item.lng !=null && item.lat!=null) {
            mapView.getOverlay().clear();
            updateLocationMarker();
        }
    }

    private void updateLocationMarker() {
        GeoPoint p = new GeoPoint(trackingMessage.lat, trackingMessage.lng);

        Drawable marker = getDrawable(R.drawable.pointer);
        OverlayItem myItem = new OverlayItem("Marker","Description of my Marker", p);
        myItem.setMarker(marker);
        List<OverlayItem> pList = new ArrayList<OverlayItem>();
        pList.add(myItem);
        OnItemGestureListener<OverlayItem> pOnItemGestureListener = new myItemGestureListener<OverlayItem>();
        myOverlay = new ItemizedIconOverlay<OverlayItem>(getActivity(), pList, pOnItemGestureListener);
        mapView.getOverlays().add(myOverlay);

        mapView.getController().setCenter(p);
    }

    private Drawable getDrawable(int resId){
        Resources res = getResources();
        if(android.os.Build.VERSION.SDK_INT >= 21){
            return res.getDrawable(resId,null);
        }else{
            return res.getDrawable(resId);
        }
    }

    @Override
    public void setDetailItem(TrackingMessage item) {
        trackingMessage = item;
        if(trackingMessage!=null && trackingMessage.lat!=null && trackingMessage.lng!=null) {
            lat = trackingMessage.lat;
            lng = trackingMessage.lng;
        }
    }

    @Override
    public TrackingMessage getDetailItem() {
        return trackingMessage;
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
    public class MyLocationListener implements LocationListener
    {
        private IMapController mapController;
        private ItemizedIconOverlay<OverlayItem> mapPoint;

        public MyLocationListener(IMapController controller, ItemizedIconOverlay<OverlayItem> myOverlay){
            this.mapController = controller;
            this.mapPoint = myOverlay;
        }
        @Override
        public void onLocationChanged(Location loc)

        {
            if(this.mapPoint!=null && this.mapPoint.size() > 0) {
                this.mapPoint.removeAllItems();
            }else{
                return;
            }



            Drawable marker2 = getDrawable(R.drawable.pointer);
            OverlayItem myItem2 = new OverlayItem("Target","Received location",new GeoPoint(lat, lng));
            myItem2.setMarker(marker2);
            this.mapPoint.addItem(myItem2);


            Drawable marker = getDrawable(R.drawable.pointer2);
            OverlayItem myItem = new OverlayItem("My pos","Your position",new GeoPoint(loc.getLatitude(), loc.getLongitude()));
            myItem.setMarker(marker);
            ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
            items.add(myItem2);
            items.add(myItem);
            this.mapPoint.addItems(items);

        }

        private Drawable getDrawable(int resId){
            Resources res = getResources();
            if(android.os.Build.VERSION.SDK_INT >= 21){
                return res.getDrawable(resId,null);
            }else{
                return res.getDrawable(resId);
            }
        }

        @Override
        public void onProviderDisabled(String provider){}

        @Override
        public void onProviderEnabled(String provider){}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){}

    }/** End of Class MyLocationListener */
}
