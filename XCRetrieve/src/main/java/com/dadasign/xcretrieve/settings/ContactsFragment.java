package com.dadasign.xcretrieve.settings;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.Contact;
import com.dadasign.xcretrieve.ContactAdapter;
import com.dadasign.xcretrieve.R;

public class ContactsFragment extends Fragment  {
	public static final int PICK_CONTACT = 0;
	private ListView contact_list;
	private ContactAdapter adapter;
	private CheckBox allow_all;
	private Button add_contact;
	private ArrayList<Contact> contacts = new ArrayList<Contact>();
	private SharedPreferences settings;
    private static final int PERMISSION_REQUEST_SERVICE_ON=1;
    private static final int PERMISSION_REQUEST_SERVICE_CONTACT_ADD=2;
    String [] PROJECTION = new String [] {  ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID };
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = (View) inflater.inflate(R.layout.settings_contacts_fragment,
				container, false);

			contact_list = (ListView) v.findViewById(R.id.contacts);
			settings = getActivity().getSharedPreferences("CloudSettings", Context.MODE_PRIVATE);
			add_contact = (Button) v.findViewById(R.id.add_contact);
			allow_all = (CheckBox) v.findViewById(R.id.allow_all);
		
		if(settings == null){
			return v;
		}
		//Load data
			set_allow_all(settings.getBoolean("allow_all", false));
			//service_on.setChecked(isMyServiceRunning());

			adapter = new ContactAdapter(getActivity(), R.layout.contact_list_item, R.id.name, contacts);
            populateContacts();
			contact_list.setAdapter(adapter);
			
			add_contact.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                        String[] perms = {Manifest.permission.READ_CONTACTS};
                        requestPermissions(perms,PERMISSION_REQUEST_SERVICE_CONTACT_ADD);
                    }else {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, PICK_CONTACT);
                    }
				}
			});
			allow_all.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					settings.edit().putBoolean("allow_all", isChecked).commit();
					set_allow_all(isChecked);
				}
			});
			
			if(settings.contains("contacts")){
				//allow_all.setEnabled(settings.);
			}
		
		return v;
	}
    private void populateContacts(){
        if(settings.contains("contacts")){
            try {
                JSONArray contact_data = new JSONArray(settings.getString("contacts", ""));
                if(contact_data.length()>0 && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                    String[] perms = {Manifest.permission.READ_CONTACTS};
                    requestPermissions(perms,PERMISSION_REQUEST_SERVICE_ON);
                }else {
                    for (int x = 0; x < contact_data.length(); x++) {
                        String id = contact_data.getString(x);
                        Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, id);
                        Uri res = ContactsContract.Contacts.lookupContact(getActivity().getContentResolver(), lookupUri);
                        try {
                            Cursor c = getActivity().getContentResolver().query(res, PROJECTION, null, null, null);
                            if (c.moveToFirst()) {
                                int hasPhone = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                                if (hasPhone == 0) {
                                    continue;
                                }
                                Contact contact = new Contact();
                                contact.id = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                                contact.name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                contacts.add(contact);
                            }
                            c.close();
                        }catch (NullPointerException e){
                            Log.e("ContactsFragment","Null pointer exception on getContentResolver");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                settings.edit().remove("contacts");
            } catch (IllegalArgumentException e){
                settings.edit().remove("contacts");
            }
        }
        adapter.notifyDataSetChanged();
    }
    private void openContactPicker(){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(requestCode == PERMISSION_REQUEST_SERVICE_CONTACT_ADD){
                openContactPicker();
            }else {
                populateContacts();
            }
        }else{
            Toast.makeText(getContext(),R.string.contact_access_needed,Toast.LENGTH_LONG).show();
        }
    }

    public void set_allow_all(boolean on){
		allow_all.setChecked(on);
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
	  super.onActivityResult(reqCode, resultCode, data);
	  switch (reqCode) {
	    case (PICK_CONTACT) :
	      if (resultCode == Activity.RESULT_OK) {
	        Uri contactData = data.getData();
	        String [] PROJECTION = new String [] {  ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.DISPLAY_NAME };
	        Cursor c =  getActivity().getContentResolver().query(contactData, PROJECTION, null, null, null);
	        if (c.moveToFirst()) {
	        	int hasPhone = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
	        	if(hasPhone==0){
	        		//Log.v("MainActivity add contact","No phone number for this contact");
	        		Toast.makeText(getActivity(), R.string.no_phone, Toast.LENGTH_LONG).show();
	        		return;
	        	}
	        	Contact contact = new Contact(); 
	        	contact.id = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
	        	contact.name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	        	//contacts.add(contact);
	        	adapter.add(contact);
	        	JSONArray arr = new JSONArray();
	        	for(Contact con:contacts){
	        		arr.put(con.id);
	        	}
	        	String jsonEnc = arr.toString();
	        	settings.edit().putString("contacts", jsonEnc).commit();
	        }
	      }
	      break;
	  }
	}
}
