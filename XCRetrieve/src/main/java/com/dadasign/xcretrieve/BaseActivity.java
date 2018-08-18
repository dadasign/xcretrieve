package com.dadasign.xcretrieve;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.dadasign.xcretrieve.history.HistoryActivity;
import com.dadasign.xcretrieve.log.Logger;
import com.dadasign.xcretrieve.sender.SenderTabedActivity;
import com.dadasign.xcretrieve.settings.TabedSettingsActivity;
import com.dadasign.xcretrieve.utils.Persistence;

public class BaseActivity extends AppCompatActivity {
	protected SharedPreferences settings;
	protected ActionBar actionBar;
    public Persistence persistence;
	public Logger logger;
    protected static final int PERMISSION_REQUEST_SENDER=102;

	@Override
	protected void onCreate(Bundle arg0) {
		settings=getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
		super.onCreate(arg0);
		setContentView(R.layout.activity_wizard);
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setIcon(R.drawable.logo);
        actionBar.show();

        persistence = new Persistence(this);
		logger = new Logger(persistence);
	}
	@Override
	  public void onStop() {
	    super.onStop();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_settings:
			startActivity(new Intent(this,TabedSettingsActivity.class));
		break;
		case R.id.menu_reader:
			startActivity(new Intent(this,HistoryActivity.class));
		break;
		case R.id.menu_sender:
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] perms = {android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this,perms,PERMISSION_REQUEST_SENDER);
                return true;
            }else {
                startActivity(new Intent(this, SenderTabedActivity.class));
            }
		break;
		case android.R.id.home:
		case R.id.menu_home:
			startActivity(new Intent(this,MainActivity.class));
		break;
		case R.id.menu_help:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url)));
			startActivity(browserIntent);
		break;
		}
		return true;
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(requestCode==PERMISSION_REQUEST_SENDER) {
                startActivity(new Intent(this, SenderTabedActivity.class));
            }
        }
    }
}
