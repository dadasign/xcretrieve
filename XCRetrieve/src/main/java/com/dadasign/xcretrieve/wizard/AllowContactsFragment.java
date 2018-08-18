package com.dadasign.xcretrieve.wizard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.SharedPreferenceKeys;

public class AllowContactsFragment extends Fragment {
	WizardActivity wizAct;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.wizard_allow_all, container, false);
		Button ok_btn = (Button) v.findViewById(R.id.ok_btn);
		Button no_btn = (Button) v.findViewById(R.id.turn_off_btn);
        final SharedPreferences settings = getActivity().getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
		ok_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				wizAct.nextPage();
				settings.edit().putBoolean(SharedPreferenceKeys.allowAllContacts, false).commit();
			}
		});
		no_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				settings.edit().putBoolean(SharedPreferenceKeys.allowAllContacts, true).commit();
				wizAct.nextPage();
				wizAct.nextPage();
			}
		});
		return v;
	}
}
