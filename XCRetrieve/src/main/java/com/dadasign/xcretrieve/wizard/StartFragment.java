package com.dadasign.xcretrieve.wizard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.dadasign.xcretrieve.R;

public class StartFragment extends Fragment {
	WizardActivity wizAct;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = (View) inflater.inflate(R.layout.wizard_start, container, false);
		Button ok_btn = (Button) v.findViewById(R.id.ok_btn);
		Button no_btn = (Button) v.findViewById(R.id.turn_off_btn);
		ok_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				wizAct.nextPage();
			}
		});
		no_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				wizAct.exit();
			}
		});
		return v;
	}
}
