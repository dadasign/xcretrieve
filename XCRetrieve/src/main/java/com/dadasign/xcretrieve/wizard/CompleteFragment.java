package com.dadasign.xcretrieve.wizard;

import com.dadasign.xcretrieve.MainActivity;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.SharedPreferenceKeys;
import com.dadasign.xcretrieve.utils.PermissionHelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CompleteFragment extends Fragment {
	PermissionHelper permissionHelper;
    private static final int PERMISSION_REQUEST = 1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        permissionHelper = new PermissionHelper(getContext());
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = (View) inflater.inflate(R.layout.wizard_complete, container, false);
		Button ok_btn = (Button) v.findViewById(R.id.yes_btn);
		Button no_btn = (Button) v.findViewById(R.id.no_btn);
		TextView complete_msg = (TextView) v.findViewById(R.id.complete_msg);
		SharedPreferences settings = getActivity().getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
		complete_msg.setText(getString(R.string.wizard_complete).replace("code", "xc@"+settings.getString("pass", "")));
		ok_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                if(permissionHelper.areNeededPermissionsSatisfied()) {
                    turnServiceOnAndCloseWizard();
                }else{
                    ActivityCompat.requestPermissions(getActivity(),permissionHelper.getRequiredPermissions(),PERMISSION_REQUEST);
                }
			}
		});
		no_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences settings = getActivity().getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
				settings.edit().putBoolean(SharedPreferenceKeys.trackingServiceEnabled, false).commit();
				getActivity().startActivity(new Intent(getActivity(),MainActivity.class));
				getActivity().finish();
			}
		});
		return v;
	}
    private void turnServiceOnAndCloseWizard(){
        SharedPreferences settings = getActivity().getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
        settings.edit().putBoolean(SharedPreferenceKeys.trackingServiceEnabled, true).commit();
        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_REQUEST) {
            if(permissionHelper.areNeededPermissionsSatisfied()) {
                turnServiceOnAndCloseWizard();
            }else{
                Toast.makeText(getContext(),R.string.need_permissions_for_service,Toast.LENGTH_LONG).show();
            }
        }
    }
}
