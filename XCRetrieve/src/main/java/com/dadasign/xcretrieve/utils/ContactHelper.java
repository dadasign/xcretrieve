package com.dadasign.xcretrieve.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;

import com.dadasign.xcretrieve.Contact;

/**
 * Created by Jakub on 2015-09-22.
 */
public class ContactHelper {
    private Context ctx;
    public  ContactHelper(Context _ctx){
        ctx=_ctx;
    }
    public Contact getContactLookup(final String phoneNumber)
    {
        if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            return null;
        }
        Uri uri = Uri.parse("content://com.android.contacts/phone_lookup");
        String [] projection = new String [] {  ContactsContract.Contacts.LOOKUP_KEY , ContactsContract.Contacts.DISPLAY_NAME};

        uri = Uri.withAppendedPath(uri, Uri.encode(phoneNumber));
        Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);

        Contact c = new Contact();
        if (cursor.moveToFirst())
        {
            c.id = cursor.getString(0);
            c.name = cursor.getString(1);
        }else{
            c.name = phoneNumber;
        }

        cursor.close();

        return c;
    }
}
