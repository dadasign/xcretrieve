package com.dadasign.xcretrieve.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.TestActivity;
import com.dadasign.xcretrieve.wizard.WizardActivity;

public class SettingsFragment extends Fragment{
	private SharedPreferences settings;
	private RadioGroup radioGroup;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = (View) inflater.inflate(R.layout.settings_settings_fragment,
				container, false);
		radioGroup = (RadioGroup) v.findViewById(R.id.format_selector);
		
		final EditText pass = (EditText) v.findViewById(R.id.pass);
			settings = getActivity().getSharedPreferences("CloudSettings", Context.MODE_PRIVATE);
		
		if(settings == null){
			return v;
		}

		pass.setText(settings.getString("pass", ""));
		
		pass.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                settings.edit().putString("pass", pass.getText().toString()).commit();
            }
        });
		int restore_format = settings.getInt("format", 0);
		switch(restore_format){
			case 0:
				radioGroup.check(R.id.format_d);
			break;
			case 1:
				radioGroup.check(R.id.format_d_m);
			break;
			case 2:
				radioGroup.check(R.id.format_d_m_s);
			break;
			case 3:
				radioGroup.check(R.id.format_link);
			break;
		}
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int data_format = 0;
                switch (checkedId) {
                    case R.id.format_d:
                        data_format = 0;
                        break;
                    case R.id.format_d_m:
                        data_format = 1;
                        break;
                    case R.id.format_d_m_s:
                        data_format = 2;
                        break;
                    case R.id.format_link:
                        data_format = 3;
                        break;
                }
                settings.edit().putInt("format", data_format).commit();
            }
        });
        Button clearHistoryBtn =(Button) v.findViewById(R.id.clear_history);
        clearHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.confirm).setMessage(R.string.confirm_clear_history).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((BaseActivity) getActivity()).persistence.clearTrackingMessages();
                        Toast.makeText(getActivity(),R.string.history_was_cleared,Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(R.string.no,null).show();
            }
        });
        Button wizardBtn = (Button) v.findViewById(R.id.wizard_btn);
        wizardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),WizardActivity.class));
            }
        });
        Button simBtn = (Button) v.findViewById(R.id.simulator_btn);
        simBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),TestActivity.class));
            }
        });
		return v;
	}
}
