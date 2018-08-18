package com.dadasign.xcretrieve.sender;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.utils.LocationDataFormats;
import com.dadasign.xcretrieve.utils.LocationMessageFormatter;
import com.dadasign.xcretrieve.utils.PermissionHelper;
import com.dadasign.xcretrieve.wizard.NonSwipePager;

public class SenderTabedActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_tabed);

        PermissionHelper permissionHelper = new PermissionHelper(this);
        if(!permissionHelper.isLocationAvailable() || !permissionHelper.canAccessStorage()){
            String [] perms = permissionHelper.getLocationAndStoragePermissions();
            ActivityCompat.requestPermissions(this,perms,0);
        }

        String[] titles = getResources().getStringArray(R.array.sender_tab_titles);

        final NonSwipePager pager = (NonSwipePager) findViewById(R.id.pager);
        pager.setPagingEnabled(false);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        
        final TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();
        
        for(int c=0; c<titles.length; c++){
        	TabSpec ts = tabHost.newTabSpec(titles[c]);
        	ts.setIndicator(getTabView(titles[c]));
        	ts.setContent(new TCF());
        	tabHost.addTab(ts);
        }
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				pager.setCurrentItem(tabHost.getCurrentTab());
			}
		});
        pager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				tabHost.setCurrentTab(position);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
    }
    
    private View getTabView(String title){
    	View v = getLayoutInflater().inflate(R.layout.tab_indicator, null);
    	((TextView) v.findViewById(R.id.title)).setText(title);
    	return v;
    }
	
	private class MyPagerAdapter extends FragmentPagerAdapter{
		public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
		
		@Override
		public Fragment getItem(int arg0) {
			if(arg0==0){
				return new SenderGoogleMapFragment();
			}else{
				return new SenderOSMFragment();
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
		
		
	}
	private class TCF implements TabHost.TabContentFactory{

		@Override
		public View createTabContent(String tag) {
			// TODO Auto-generated method stub
			return new View(getApplicationContext());
		}
		
	}
    public void sendPosition(Location loc){
        if(loc==null){
            Toast.makeText(this,R.string.wait_for_gps,Toast.LENGTH_SHORT).show();
            return;
        }
        LocationMessageFormatter locationMessageFormatter = new LocationMessageFormatter(SenderTabedActivity.this);
        LocationDataFormats locationDataFormats = new LocationDataFormats();
        TrackingMessage sentTrackingMessage;
        sentTrackingMessage = new TrackingMessage();
        sentTrackingMessage.messageType = TrackingMessage.MSG_TYPE_SHARE;
        sentTrackingMessage.lat = loc.getLatitude();
        sentTrackingMessage.lng = loc.getLongitude();
        if(loc.hasAltitude()) {
            sentTrackingMessage.alt = (int) loc.getAltitude();
        }
        sentTrackingMessage.status = TrackingMessage.STATUS_COMPLETE;
        sentTrackingMessage.timeCreated = sentTrackingMessage.timeSent = sentTrackingMessage.timeLastUpdate = System.currentTimeMillis();
        sentTrackingMessage.gpsUsed = loc.getProvider().equals(LocationManager.GPS_PROVIDER);
        sentTrackingMessage.format = locationDataFormats.getDefaultFormat(getApplication());
        String text = locationMessageFormatter.getMessageForLocation(loc,sentTrackingMessage.format,true);
        sentTrackingMessage.message = text;
        persistence.saveTrackingMessage(sentTrackingMessage);

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");


        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.send_using)));
    }
}
