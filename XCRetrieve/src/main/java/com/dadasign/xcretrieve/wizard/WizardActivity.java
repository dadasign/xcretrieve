package com.dadasign.xcretrieve.wizard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.SharedPreferenceKeys;

public class WizardActivity extends BaseActivity {
	static final int NUM_ITEMS = 5;
	
	MyAdapter mAdapter;
    NonSwipePager mPager;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_wizard);
		
		mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (NonSwipePager)findViewById(R.id.pager);
        mPager.setPagingEnabled(false);
        mPager.setAdapter(mAdapter);
        actionBar.hide();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	public void nextPage(){
		mPager.setCurrentItem(mPager.getCurrentItem()+1);
	}
	public void prevPage(){
		mPager.setCurrentItem(mPager.getCurrentItem()-1);
	}
	public void exit(){
		finish();
	}
	public class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
        	if(position==0){
	            StartFragment sf =  new StartFragment();
	            sf.wizAct=WizardActivity.this;
	            return sf;
        	}else if(position==1){
        		CodeFragment f =  new CodeFragment();
	            f.wizAct=WizardActivity.this;
	            return f;
        	}else if(position==2){
        		AllowContactsFragment f =  new AllowContactsFragment();
	            f.wizAct=WizardActivity.this;
	            return f;
        	}else if(position==3){
        		ContactsFragment f =  new ContactsFragment();
	            f.wizAct=WizardActivity.this;
	            return f;
        	}else{
        		CompleteFragment f =  new CompleteFragment();
	            return f;
        	}
        }
    }
	@Override
	public void onBackPressed() {
		if(mPager.getCurrentItem()==0){
			super.onBackPressed();
		}else if(mPager.getCurrentItem()==4){
			if(settings.getBoolean(SharedPreferenceKeys.allowAllContacts, false)){
				mPager.setCurrentItem(2);
			}else{
				prevPage();
			}
		}else{
			prevPage();
		}
		
	}
}
