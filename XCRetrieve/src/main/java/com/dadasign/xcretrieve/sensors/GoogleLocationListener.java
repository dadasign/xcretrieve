package com.dadasign.xcretrieve.sensors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.dadasign.xcretrieve.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by Jakub on 2016-08-03.
 */
public class GoogleLocationListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private final LocationRequest request;
    private final GoogleLocationUpdateListener listener;
    private GoogleApiClient apiClient;

    public GoogleLocationListener(LocationRequest _request, GoogleLocationUpdateListener _listener, Context ctx) {
        request = _request;
        listener = _listener;
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        apiClient = mGoogleApiClient;
    }

    public void connect() {
        if (!apiClient.isConnected()) {
            apiClient.connect();
        }
    }

    public void disconnect() {
        if (apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
            apiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        if (loc != null) {
            listener.onLocationUpdate(loc);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (apiClient.isConnected()) {
            //Location l = LocationServices.FusedLocationApi.getLastLocation(apiClient);
            //this.onLocationChanged(l);
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}/** End of Class MyLocationListener */
