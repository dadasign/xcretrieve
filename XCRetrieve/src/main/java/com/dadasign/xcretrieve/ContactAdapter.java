package com.dadasign.xcretrieve;

import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class ContactAdapter extends ArrayAdapter<Contact> {
	private List<Contact> contacts;

	
	public ContactAdapter(Context context, int resource, int textViewResourceId, List<Contact> objects) {
		super(context, resource, textViewResourceId, objects);
		contacts = objects;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View out = super.getView(position, convertView, parent);
		Button remove_btn = (Button) out.findViewById(R.id.remove_btn);
		remove_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContactAdapter.this.remove(getItem(position));
				JSONArray arr = new JSONArray();
				for(Contact con:contacts){
	        		arr.put(con.id);
	        	}
	        	String jsonEnc = arr.toString();
	        	getContext().getSharedPreferences("CloudSettings", Context.MODE_PRIVATE).edit().putString("contacts", jsonEnc).commit();
			}
		});
		return out;
	}
}
