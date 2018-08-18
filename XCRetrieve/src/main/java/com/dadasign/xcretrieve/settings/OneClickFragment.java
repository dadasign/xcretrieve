package com.dadasign.xcretrieve.settings;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.Contact;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.SharedPreferenceKeys;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Jakub on 2016-08-18.
 */
public class OneClickFragment extends Fragment {
    public static final int PICK_CONTACT_POSITION = 1;
    public static final int PICK_CONTACT_REQUEST = 2;

    private CheckBox oneClickPositionCheckbox;
    private CheckBox oneClickRequestCheckbox;
    private CheckBox requestInstantCheckbox;
    private EditText requestPassInput;
    private View requestDetailsContainer;
    private View positionDetailsContainer;
    private Button changeRequestRecipientBtn;
    private Button changePositionRecipientBtn;
    private TextView requestSentTo;
    private TextView positionSentTo;

    private SharedPreferences settings;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = (View) inflater.inflate(R.layout.settings_oneclick_fragment,
                container, false);
        oneClickPositionCheckbox = (CheckBox) v.findViewById(R.id.position_checkbox);
        oneClickRequestCheckbox = (CheckBox) v.findViewById(R.id.request_checkbox);
        requestInstantCheckbox = (CheckBox) v.findViewById(R.id.request_instant_checkbox);
        requestPassInput = (EditText) v.findViewById(R.id.request_password);
        requestDetailsContainer = v.findViewById(R.id.request_details_container);
        positionDetailsContainer = v.findViewById(R.id.position_details_container);
        changePositionRecipientBtn = (Button) v.findViewById(R.id.change_position_recepient);
        changeRequestRecipientBtn = (Button) v.findViewById(R.id.change_request_recepient);
        requestSentTo = (TextView) v.findViewById(R.id.oneclick_request_sent_to);
        positionSentTo = (TextView) v.findViewById(R.id.oneclick_position_sent_to);

        settings = getActivity().getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);

        boolean positionEnabled = settings.getBoolean(SharedPreferenceKeys.oneClickPositionEnabled,false);
        oneClickPositionCheckbox.setChecked(positionEnabled);
        positionDetailsContainer.setVisibility(positionEnabled?View.VISIBLE:View.GONE);

        if(positionEnabled){
            positionSentTo.setText(getString(R.string.one_click_sending_user)+" "+settings.getString(SharedPreferenceKeys.oneClickPositionContactName,"")+" ("+settings.getString(SharedPreferenceKeys.oneClickPositionContactNum,"")+")");
        }

        boolean requestEnabled = settings.getBoolean(SharedPreferenceKeys.oneClickRequestEnabled,false);
        oneClickRequestCheckbox.setChecked(requestEnabled);
        requestDetailsContainer.setVisibility(requestEnabled?View.VISIBLE:View.GONE);

        if(requestEnabled){
            boolean requestInstant = settings.getBoolean(SharedPreferenceKeys.oneClickRequestInstant,false);
            requestInstantCheckbox.setChecked(requestInstant);
            String pass = settings.getString(SharedPreferenceKeys.oneClickRequestPass,"");
            requestPassInput.setText(pass);
            requestSentTo.setText(getString(R.string.one_click_request_user)+" "+settings.getString(SharedPreferenceKeys.oneClickRequestContactName,"")+" ("+settings.getString(SharedPreferenceKeys.oneClickRequestContactNum,"")+")");
        }

        oneClickPositionCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked) {
                    if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                        String[] perms = {Manifest.permission.READ_CONTACTS};
                        requestPermissions(perms,PICK_CONTACT_POSITION);
                    }else {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, PICK_CONTACT_POSITION);
                    }
                }else{
                    settings.edit().putBoolean(SharedPreferenceKeys.oneClickPositionEnabled,false).apply();
                    positionDetailsContainer.setVisibility(View.GONE);
                }
            }
        });
        changePositionRecipientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_POSITION);
            }
        });

        oneClickRequestCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked) {
                    if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                        String[] perms = {Manifest.permission.READ_CONTACTS};
                        requestPermissions(perms,PICK_CONTACT_REQUEST);
                    }else {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, PICK_CONTACT_REQUEST);
                    }
                }else{
                    settings.edit().putBoolean(SharedPreferenceKeys.oneClickRequestEnabled,false).apply();
                    requestDetailsContainer.setVisibility(View.GONE);
                }
            }
        });
        changeRequestRecipientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        requestPassInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                settings.edit().putString(SharedPreferenceKeys.oneClickRequestPass,charSequence.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        requestInstantCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.edit().putBoolean(SharedPreferenceKeys.oneClickRequestInstant,b).apply();
            }
        });

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PICK_CONTACT_POSITION){
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_POSITION);
            }else{
                Toast.makeText(getContext(),R.string.contact_access_needed,Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == PICK_CONTACT_REQUEST){
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }else{
                Toast.makeText(getContext(),R.string.contact_access_needed,Toast.LENGTH_LONG).show();
            }
        }
    }

    private CharSequence[] getPhoneNumbersForContact(String id){
        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
        ArrayList<String> phoneNums = new ArrayList<>();
        while(phones.moveToNext()) {
            phoneNums.add(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        }
        phones.close();
        return phoneNums.toArray(new CharSequence[phoneNums.size()]);
    }

    private String getPhoneNumberForContact(String id){
        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
        phones.moveToNext();
        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        phones.close();
        return phoneNumber;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_CONTACT_POSITION){
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                String [] PROJECTION = new String [] {  ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID };
                Cursor c =  getActivity().getContentResolver().query(contactData, PROJECTION, null, null, null);
                if (c.moveToFirst()) {
                    int hasPhone = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    if(hasPhone==0){
                        //Log.v("MainActivity add contact","No phone number for this contact");
                        Toast.makeText(getActivity(), R.string.no_phone, Toast.LENGTH_LONG).show();
                        oneClickPositionCheckbox.setChecked(false);
                        return;
                    }else {
                        final Contact contact = new Contact();
                        contact.id = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        contact.name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        final CharSequence[] phoneNums = getPhoneNumbersForContact(c.getString(c.getColumnIndex(ContactsContract.Contacts._ID)));
                        if(phoneNums.length==1) {
                            settings.edit()
                                    .putString(SharedPreferenceKeys.oneClickPositionContactName, contact.name)
                                    .putString(SharedPreferenceKeys.oneClickPositionContactId, contact.id)
                                    .putString(SharedPreferenceKeys.oneClickPositionContactNum, phoneNums[0].toString())
                                    .putBoolean(SharedPreferenceKeys.oneClickPositionEnabled, true)
                                    .apply();
                            positionDetailsContainer.setVisibility(View.VISIBLE);
                            positionSentTo.setText(getString(R.string.one_click_sending_user) + " " + contact.name + " (" + phoneNums[0].toString() + ")");
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.select_phone_num)
                                    .setItems(phoneNums, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            settings.edit()
                                                    .putString(SharedPreferenceKeys.oneClickPositionContactName, contact.name)
                                                    .putString(SharedPreferenceKeys.oneClickPositionContactId, contact.id)
                                                    .putString(SharedPreferenceKeys.oneClickPositionContactNum, phoneNums[which].toString())
                                                    .putBoolean(SharedPreferenceKeys.oneClickPositionEnabled, true)
                                                    .apply();
                                            positionDetailsContainer.setVisibility(View.VISIBLE);
                                            positionSentTo.setText(getString(R.string.one_click_sending_user) + " " + contact.name + " (" + phoneNums[which].toString() + ")");
                                        }
                                    });
                            builder.create().show();

                        }
                    }
                }
            }else{
                oneClickPositionCheckbox.setChecked(false);
            }
        }else if(requestCode == PICK_CONTACT_REQUEST){
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                String [] PROJECTION = new String [] {  ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID };
                Cursor c =  getActivity().getContentResolver().query(contactData, PROJECTION, null, null, null);
                if (c.moveToFirst()) {
                    int hasPhone = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    if(hasPhone==0){
                        //Log.v("MainActivity add contact","No phone number for this contact");
                        Toast.makeText(getActivity(), R.string.no_phone, Toast.LENGTH_LONG).show();
                        oneClickRequestCheckbox.setChecked(false);
                        return;
                    }
                    final Contact contact = new Contact();
                    contact.id = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    contact.name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    final CharSequence[] phoneNums = getPhoneNumbersForContact(c.getString(c.getColumnIndex(ContactsContract.Contacts._ID)));
                    if(phoneNums.length==1) {
                        settings.edit()
                                .putString(SharedPreferenceKeys.oneClickRequestContactName, contact.name)
                                .putString(SharedPreferenceKeys.oneClickRequestContactId, contact.id)
                                .putString(SharedPreferenceKeys.oneClickRequestContactNum, phoneNums[0].toString())
                                .putBoolean(SharedPreferenceKeys.oneClickRequestEnabled, true)
                                .putBoolean(SharedPreferenceKeys.oneClickRequestInstant, requestInstantCheckbox.isChecked())
                                .apply();
                        requestDetailsContainer.setVisibility(View.VISIBLE);
                        requestSentTo.setText(getString(R.string.one_click_sending_user) + " " + contact.name + " (" + phoneNums[0].toString() + ")");
                        requestPassInput.requestFocusFromTouch();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.select_phone_num)
                                .setItems(phoneNums, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        settings.edit()
                                                .putString(SharedPreferenceKeys.oneClickRequestContactName, contact.name)
                                                .putString(SharedPreferenceKeys.oneClickRequestContactId, contact.id)
                                                .putString(SharedPreferenceKeys.oneClickRequestContactNum, phoneNums[which].toString())
                                                .putBoolean(SharedPreferenceKeys.oneClickRequestEnabled, true)
                                                .putBoolean(SharedPreferenceKeys.oneClickRequestInstant, requestInstantCheckbox.isChecked())
                                                .apply();
                                        requestDetailsContainer.setVisibility(View.VISIBLE);
                                        requestSentTo.setText(getString(R.string.one_click_sending_user) + " " + contact.name + " (" + phoneNums[which].toString() + ")");
                                        requestPassInput.requestFocusFromTouch();
                                    }
                                });
                        builder.create().show();
                    }
                }
            }else{
                oneClickRequestCheckbox.setChecked(false);
            }
        }
    }
}
