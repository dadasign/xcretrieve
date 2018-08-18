package com.dadasign.xcretrieve.history;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.SupportMapFragment;

public class MyMapFragment extends SupportMapFragment {
	public OnMapCompleteListener listener;
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		if(listener!=null) listener.onMapComplete();
	}
	public static interface OnMapCompleteListener {
	    public abstract void onMapComplete();
	}
}

