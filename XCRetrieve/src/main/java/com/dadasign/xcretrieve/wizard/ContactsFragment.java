package com.dadasign.xcretrieve.wizard;

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
import android.widget.ListView;
import android.widget.Toast;

import com.dadasign.xcretrieve.Contact;
import com.dadasign.xcretrieve.ContactAdapter;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.SharedPreferenceKeys;

public class ContactsFragment extends Fragment {
    private static final int PICK_CONTACT = 0;
	WizardActivity wizAct;
	private ListView contact_list;
	private ContactAdapter adapter;
	private SharedPreferences settings;
	private Button add_contact;
	private ArrayList<Contact> contacts = new ArrayList<Contact>();
    private static final int PERMISSION_REQUEST_SERVICE_ON=1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = (View) inflater.inflate(R.layout.wizard_contacts, container, false);
		Button ok_btn = (Button) v.findViewById(R.id.turn_on_btn);
		Button no_btn = (Button) v.findViewById(R.id.turn_off_btn);
		settings = getActivity().getSharedPreferences(SharedPreferenceKeys.preferencesName, Context.MODE_PRIVATE);
		add_contact = (Button) v.findViewById(R.id.add_contact);
		contact_list = (ListView) v.findViewById(R.id.contacts);
		
		ok_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(contacts.size()==0){
					Toast.makeText(getActivity(), R.string.wizard_empty_contacts, Toast.LENGTH_LONG).show();
				}else{
					wizAct.nextPage();
				}
			}
		});
		no_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				wizAct.prevPage();
			}
		});
		
		if(settings.contains(SharedPreferenceKeys.contacts)){
			try {
				JSONArray contact_data = new JSONArray(settings.getString(SharedPreferenceKeys.contacts, ""));
				for(int x=0; x<contact_data.length(); x++){
					String id = contact_data.getString(x);
					Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, id);
					Uri res = ContactsContract.Contacts.lookupContact(getActivity().getContentResolver(), lookupUri);
					Cursor c =  getActivity().getContentResolver().query(res, null, null, null, null);
			        if (c.moveToFirst()) {
			        	int hasPhone = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			        	if(hasPhone==0){
			        		Log.v("Wizard add contact","No phone number for this contact");
			        		continue;
			        	}
			        	Contact contact = new Contact(); 
			        	contact.id = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
			        	contact.name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			        	contacts.add(contact);
			        }
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e){
				settings.edit().remove(SharedPreferenceKeys.contacts);
			}
		}
		
		
		adapter = new ContactAdapter(getActivity(), R.layout.contact_list_item, R.id.name, contacts);
		contact_list.setAdapter(adapter);
		
		add_contact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                    String[] perms = {Manifest.permission.READ_CONTACTS};
                    requestPermissions(perms,PERMISSION_REQUEST_SERVICE_ON);
                }else{
                    openContactPicker();
                }
			}
		});
		
		return v;
	}
    private void openContactPicker(){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_SERVICE_ON && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openContactPicker();
        }else{
            Toast.makeText(getContext(),R.string.contact_access_needed,Toast.LENGTH_LONG).show();
        }
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
		        		Toast.makeText(getActivity(), R.string.no_phone, Toast.LENGTH_LONG).show();
		        		return;
		        	}
		        	Contact contact = new Contact(); 
		        	contact.id = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
		        	contact.name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		        	adapter.add(contact);
		        	JSONArray arr = new JSONArray();
		        	for(Contact con:contacts){
		        		arr.put(con.id);
		        	}
		        	String jsonEnc = arr.toString();
		        	settings.edit().putString(SharedPreferenceKeys.contacts, jsonEnc).commit();
		        }
		      }
		      break;
		  }
		}
}
