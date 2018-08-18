package com.dadasign.xcretrieve;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
public class TestActivity extends BaseActivity{
	private int status=0;
	private TextView status_txt;
	private TextView response_txt;
	private EditText msg_txt;
	private Button send_btn;
	private CheckBox display_map;
    private static String FROM_STRING = "Test";
	
	@SuppressLint("DefaultLocale")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		status_txt = (TextView) findViewById(R.id.status);
		send_btn = (Button) findViewById(R.id.send);
		response_txt = (TextView) findViewById(R.id.response);
		msg_txt = (EditText) findViewById(R.id.simulatedMsgText);
		display_map = (CheckBox) findViewById(R.id.map_display);
		msg_txt.setText("xc@"+settings.getString(SharedPreferenceKeys.password, ""));
		send_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(status==0){
					if(msg_txt.getText().toString().toLowerCase().startsWith(("xc@"+settings.getString(SharedPreferenceKeys.password,"")).toLowerCase())){
						Intent service = new Intent(TestActivity.this, TrackingService.class);
                        service.putExtra("content", msg_txt.getText().toString().substring(3+settings.getString(SharedPreferenceKeys.password,"").length()));
                        service.putExtra("from",FROM_STRING);
                        service.putExtra("is_test",true);

						ResultReceiver r = new ResultReceiver(null){
							@Override
							protected void onReceiveResult(int resultCode, final Bundle resultData) {
								status = resultData.getInt("status");
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										switch(status){
											case 0:
												status_txt.setText(R.string.ready);
											break;
											case 1:
												status_txt.setText(R.string.locating_gps);
											break;
											case 2:
												status_txt.setText(R.string.locating_network);
											break;
											case 3:
												status_txt.setText(R.string.position_1_received);
											break;
										}
										if(resultData.containsKey("response")){
											//settings.edit().putString("last_content", resultData.getString("response")).commit();
											response_txt.setText(resultData.getString("response"));
											if(display_map.isChecked()){
												String content = response_txt.getText().toString();
												if(content.startsWith("lat:")){
													TextTrackingReciever ttr = new TextTrackingReciever();
													ttr.ctx = TestActivity.this;
													ttr.parseResponseData(content,FROM_STRING,System.currentTimeMillis(),true);
												}
											}
										}
									}
								});
								
							}
						};
						
						service.putExtra("target", r);
						startService(service);
						response_txt.setText("");
					}else{
						response_txt.setText(R.string.invalid_message);
					}
				}else{
					//TODO: Please wait message.
				}
			}
		});
	}
}
