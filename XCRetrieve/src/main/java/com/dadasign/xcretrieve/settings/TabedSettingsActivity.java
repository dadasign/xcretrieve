package com.dadasign.xcretrieve.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.wizard.NonSwipePager;

public class TabedSettingsActivity extends BaseActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_tabed);
        String[] titles = getResources().getStringArray(R.array.settings_tab_titles);

        final NonSwipePager pager = (NonSwipePager) findViewById(R.id.pager);
        pager.setPagingEnabled(false);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        
        final TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();
        
        for(int c=0; c<titles.length; c++){
        	TabSpec ts = tabHost.newTabSpec(titles[c]);
        	ts.setIndicator(getTabView(titles[c]));
        	//ts.setIndicator(titles[c]);
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
				return new ContactsFragment();
			}else if(arg0==1){
				return new SettingsFragment();
			}else{
                return new OneClickFragment();
            }
		}

		@Override
		public int getCount() {
			return 3;
		}
		
		
	}
	private class TCF implements TabHost.TabContentFactory{

		@Override
		public View createTabContent(String tag) {
			// TODO Auto-generated method stub
			return new View(getApplicationContext());
		}
		
	}
}
