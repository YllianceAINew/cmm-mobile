package com.multimediachat.util;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;

import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.global.GlobalConstrants;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PhoneContactsUtil {
	public static Bitmap getPhoneContactImg(Context mContext, String phoneNumber) {

		if ( phoneNumber == null || phoneNumber.equals("") )
			return null;

		int phoneContactID = getContactIDFromNumber(mContext, phoneNumber);

		if ( phoneContactID > 0 ) {
			return openPhoto(mContext, phoneContactID);
		}

		return null;
	}

	public static int getContactIDFromNumber(Context mContext, String contactNumber)
	{
		contactNumber = Uri.encode(contactNumber);
		int phoneContactID = 0;
		Cursor contactLookupCursor = null;
		try{
			contactLookupCursor = mContext.getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,Uri.encode(contactNumber)),new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID}, null, null, null);
			while(contactLookupCursor.moveToNext()){
				phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup._ID));
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( contactLookupCursor != null )
				contactLookupCursor.close();
			contactLookupCursor = null;
		}

		return phoneContactID;
	}

	public static Bitmap openPhoto(Context mContext, long contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = null;

		try {
			cursor = mContext.getContentResolver().query(photoUri,
					new String[] {Contacts.Photo.PHOTO}, null, null, null);
			if (cursor.moveToFirst()) {
				byte[] data = cursor.getBlob(0);
				if (data != null) {
					return DatabaseUtils.decodeAvatar(data, GlobalConstrants.IMAGE_SIZE, GlobalConstrants.IMAGE_SIZE);
				}
			}
		} finally {
			if ( cursor != null )
				cursor.close();
			cursor = null;
		}
		return null;
	}
}
