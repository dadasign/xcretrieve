package com.dadasign.xcretrieve.wizard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.SharedPreferenceKeys;

public class CodeFragment extends Fragment {
WizardActivity wizAct;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = (View) inflater.inflate(R.layout.wizard_code, container, false);
		Button ok_btn = (Button) v.findViewById(R.id.turn_on_btn);
		Button no_btn = (Button) v.findViewById(R.id.turn_off_btn);
		final EditText pass_txt = (EditText) v.findViewById(R.id.pass);
		ok_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                hideKeyboard(pass_txt);
				wizAct.nextPage();
				SharedPreferences settings = getActivity().getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
				settings.edit().putString(SharedPreferenceKeys.password, pass_txt.getText().toString()).commit();
			}
		});
		no_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                hideKeyboard(pass_txt);
				wizAct.prevPage();
			}
		});
		return v;
	}
    private void hideKeyboard(EditText pass_txt){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pass_txt.getWindowToken(), 0);
    }
}
