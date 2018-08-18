package com.dadasign.xcretrieve.history;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.XcApplication;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.utils.PermissionHelper;
import com.dadasign.xcretrieve.wizard.NonSwipePager;

import java.util.ArrayList;

public class HistoryActivity extends BaseActivity implements DetailHandler, HistoryChangeListener{
    ArrayList<DetailHandler> detailDisplayTargets = new ArrayList<DetailHandler>(3);
    private HistoryListFragment listFragment;
    private NonSwipePager pager;
    private TrackingMessage currentMessage;
    HistoryUpdateBroadcastReceiver historyUpdateBroadcastReceiver = new HistoryUpdateBroadcastReceiver();
    PermissionHelper permissionHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionHelper = new PermissionHelper(this);
        if(!permissionHelper.isLocationAvailable() || !permissionHelper.canAccessStorage()){
            String [] perms = permissionHelper.getLocationAndStoragePermissions();
            ActivityCompat.requestPermissions(this, perms,0);
        }

        setContentView(R.layout.activity_reader_tabed);
        String[] titles = getResources().getStringArray(R.array.history_tab_titles);


        pager = (NonSwipePager) findViewById(R.id.pager);
        pager.setPagingEnabled(false);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        final TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        for(int c=0; c<titles.length; c++){
            TabHost.TabSpec ts = tabHost.newTabSpec(titles[c]);
            ts.setIndicator(getTabView(titles[c]));
            ts.setContent(new TCF());
            tabHost.addTab(ts);
        }
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                //When top button is clicked move to the selected page on pager.
                pager.setCurrentItem(tabHost.getCurrentTab());
            }
        });
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

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
        checkForReceivedMessage(getIntent());

        LocalBroadcastManager.getInstance(this).registerReceiver(historyUpdateBroadcastReceiver, new IntentFilter(HistoryUpdateBroadcastReceiver.HISTORY_UPDATE_FILTER));
        XcApplication app = (XcApplication) this.getApplication();
        app.setHistoryChangeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(!permissionHelper.canAccessStorage()){
            Toast.makeText(this,R.string.no_storage_access,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(historyUpdateBroadcastReceiver);
        XcApplication app = (XcApplication) this.getApplication();
        app.setHistoryChangeListener(null);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkForReceivedMessage(intent);
    }
    private boolean checkForReceivedMessage(Intent intent){
        if(intent.hasExtra("received_message")){
            TrackingMessage tmpMsg = new TrackingMessage();
            tmpMsg.fromBundle(intent.getBundleExtra("received_message"));
            if(tmpMsg.messageType!=TrackingMessage.MSG_TYPE_TEST) {
                persistence.saveTrackingMessage(tmpMsg);
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    private View getTabView(String title){
        View v = getLayoutInflater().inflate(R.layout.tab_indicator, null);
        ((TextView) v.findViewById(R.id.title)).setText(title);
        return v;
    }

    public void displayDetailItem(TrackingMessage item){
        this.currentMessage = item;
        for(DetailHandler d : detailDisplayTargets){
            d.displayDetailItem(item);
        }
        pager.setCurrentItem(1, true);
    }
    public void setDetailItem(TrackingMessage item){
        this.currentMessage = item;
        for(DetailHandler d : detailDisplayTargets){
            d.displayDetailItem(item);
        }
    }

    @Override
    public TrackingMessage getDetailItem() {
        return this.currentMessage;
    }

    @Override
    public void onEditListItem(TrackingMessage msg) {
        getHistoryListFragment().onEditListItem(msg);
    }

    @Override
    public void onNewListItem(TrackingMessage msg) {
        getHistoryListFragment().onNewListItem(msg);
    }

    @Override
    public void onRemoveItem(long id) {
        getHistoryListFragment().onRemoveItem(id);
    }

    @Override
    public void onBackPressed() {
        if(pager.getCurrentItem()==0) {
            super.onBackPressed();
        }else{
            pager.setCurrentItem(0);
        }
    }

    @Override
    public void onClearListItems() {
        finish();
    }

    private HistoryListFragment getHistoryListFragment(){
        if(this.listFragment==null){
            listFragment = new HistoryListFragment();
            listFragment.detailHandler = HistoryActivity.this;

        };
        return listFragment;
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int tabIndex) {
            if(tabIndex==0){
                return getHistoryListFragment();
            }else if(tabIndex==1){
                HistoryDetailsFragment detailsFragment = new HistoryDetailsFragment();
                detailDisplayTargets.add(detailsFragment);
                return detailsFragment;
            }else if(tabIndex==2){
                GoogleMapFragment mapFragment = new GoogleMapFragment();
                detailDisplayTargets.add(mapFragment);
                mapFragment.setDetailItem(currentMessage);
                return  mapFragment;
            }else{
                OSMFragment osmFragment = new OSMFragment();
                detailDisplayTargets.add(osmFragment);
                osmFragment.setDetailItem(currentMessage);
                if(!permissionHelper.canAccessStorage()){
                    Toast.makeText(HistoryActivity.this,R.string.no_storage_access,Toast.LENGTH_LONG).show();
                }
                return osmFragment;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }


    }

    private class TCF implements TabHost.TabContentFactory{

        @Override
        public View createTabContent(String tag) {
            return new View(getApplicationContext());
        }

    }
}
