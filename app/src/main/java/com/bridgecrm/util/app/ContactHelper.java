package com.bridgecrm.util.app;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;

import java.io.InputStream;

public class ContactHelper {

    public static long fetchContactIdFromPhoneNumber(Context context, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri,
                new String[] { PhoneLookup._ID },
                null, null, null);

        long id = 0L;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndex(PhoneLookup._ID));
        }
        cursor.close();

        return id;

    }

    public static String fetchContactName(Context context, long id) {
        Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri,
                new String[] { Contacts.DISPLAY_NAME },
                null, null, null);

        String displayName = null;
        if (cursor.moveToFirst()) {
            displayName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
        }
        cursor.close();

        return displayName;
    }

    public static String fetchContactNameFromPhoneNumber(Context context, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri,
                new String[] { PhoneLookup.DISPLAY_NAME },
                null, null, null);

        String displayName = null;
        if (cursor.moveToFirst()) {
            displayName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        }
        cursor.close();

        return displayName;
    }

    public static Bitmap fetchContactPhoto(Context context, long id) {
        Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
        InputStream input = Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);

        return input == null ? null : BitmapFactory.decodeStream(input);
    }

    public static Bitmap fetchContactPhotoFromPhoneNumber(Context context, String phoneNumber) {
        long id = fetchContactIdFromPhoneNumber(context, phoneNumber);
        if (id == 0)
            return null;
        else {
            Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
            InputStream input = Contacts
                    .openContactPhotoInputStream(context.getContentResolver(), uri);
            return input == null ? null : BitmapFactory.decodeStream(input);
        }
    }
}
