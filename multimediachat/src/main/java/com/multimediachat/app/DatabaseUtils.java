
package com.multimediachat.app;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ContactList;
import com.multimediachat.app.im.plugin.ImConfigNames;
import com.multimediachat.app.im.plugin.xmpp.XmppAddress;
import com.multimediachat.app.im.provider.Imps;

import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseUtils {

	private static final String TAG = "DatabaseUtils";
	public static String mAccountID = "";

	private DatabaseUtils() {
	}

	public static Bitmap decodeAvatar(byte[] data, int width, int height) {
		if( data == null )
			return null;

		Bitmap b = null;
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length,options);               
			options.inSampleSize = calculateInSampleSize(options, width, height);
			options.inJustDecodeBounds = false;
			//lightsky : test
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inScaled = false;
			//
			b = BitmapFactory.decodeByteArray(data, 0, data.length,options);

			return b;
		} catch(OutOfMemoryError ome) {
			if (b != null && !b.isRecycled() ) {
				b.recycle();
				b = null;
			}

			return b;
		} catch(Exception e) {
			return null;
		}
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	/**
	 * Update IM provider database for a plugin using newly loaded information.
	 * 
	 * @param cr the resolver
	 * @param providerName the plugin provider name
	 * @param providerFullName the full name
	 * @param signUpUrl the plugin's service signup URL
	 * @param config the plugin's settings
	 * @return the provider ID of the plugin
	 */
	public static long updateProviderDb(ContentResolver cr, String providerName,
			String providerFullName, String signUpUrl, Map<String, String> config) {
		//boolean versionChanged;

		// query provider data
		long providerId = Imps.Provider.getProviderIdForName(cr, providerName);
		if (providerId > 0) {
			// already loaded, check if version changed
			String pluginVersion = config.get(ImConfigNames.PLUGIN_VERSION);
			if (!isPluginVersionChanged(cr, providerId, pluginVersion)) {
				// no change, just return
				return providerId;
			}
			// changed, update provider meta data
			updateProviderRow(cr, providerId, providerFullName, signUpUrl);

			DebugConfig.debug(TAG, "Plugin " + providerName + "(" + providerId
					+ ") has a version change. Database updated.");
		} else {
			// new plugin, not loaded before, insert the provider data
			providerId = insertProviderRow(cr, providerName, providerFullName, signUpUrl);

			DebugConfig.debug(TAG, "Plugin " + providerName + "(" + providerId
					+ ") is new. Provider added to IM db.");
		}

		// plugin provider has been inserted/updated, we need to update settings
		saveProviderSettings(cr, providerId, config);

		return providerId;
	}


	/** Insert the plugin settings into the database. */
	private static int saveProviderSettings(ContentResolver cr, long providerId,
			Map<String, String> config) {
		ContentValues[] settingValues = new ContentValues[config.size()];
		int index = 0;
		for (Map.Entry<String, String> entry : config.entrySet()) {
			ContentValues settingValue = new ContentValues();
			settingValue.put(Imps.ProviderSettings.PROVIDER, providerId);
			settingValue.put(Imps.ProviderSettings.NAME, entry.getKey());
			settingValue.put(Imps.ProviderSettings.VALUE, entry.getValue());
			settingValues[index++] = settingValue;
		}
		return cr.bulkInsert(Imps.ProviderSettings.CONTENT_URI, settingValues);
	}

	/** Insert a new plugin provider to the provider table. */
	private static long insertProviderRow(ContentResolver cr, String providerName,
			String providerFullName, String signUpUrl) {
		ContentValues values = new ContentValues(3);
		values.put(Imps.Provider.NAME, providerName);
		values.put(Imps.Provider.FULLNAME, providerFullName);
		values.put(Imps.Provider.CATEGORY, ImApp.IMPS_CATEGORY);
		values.put(Imps.Provider.SIGNUP_URL, signUpUrl);
		Uri result = cr.insert(Imps.Provider.CONTENT_URI, values);
		return ContentUris.parseId(result);
	}

	/** Update the data of a plugin provider. */
	private static int updateProviderRow(ContentResolver cr, long providerId,
			String providerFullName, String signUpUrl) {
		// Update the full name, signup url and category each time when the plugin change
		// instead of specific version change because this is called only once.
		// It's ok to update them even the values are not changed.
		// Note that we don't update the provider name because it's used as
		// identifier at some place and the plugin should never change it.
		ContentValues values = new ContentValues(3);
		values.put(Imps.Provider.FULLNAME, providerFullName);
		values.put(Imps.Provider.SIGNUP_URL, signUpUrl);
		values.put(Imps.Provider.CATEGORY, ImApp.IMPS_CATEGORY);
		Uri uri = ContentUris.withAppendedId(Imps.Provider.CONTENT_URI, providerId);
		return cr.update(uri, values, null, null);
	}

	/**
	 * Compare the saved version of a plugin provider with the newly loaded
	 * version.
	 */
	private static boolean isPluginVersionChanged(ContentResolver cr, long providerId,
			String newVersion) {
		String oldVersion = Imps.ProviderSettings.getStringValue(cr, providerId,
				ImConfigNames.PLUGIN_VERSION);
		if (oldVersion == null) {
			return true;
		}
		return !oldVersion.equals(newVersion);
	}

	public static int getSubscriptionType(ContentResolver cr, String username) {
		Cursor cursor = null;
		int sub_type = -1;
		try{
			String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, username};

			cursor = cr.query(Imps.Contacts.CONTENT_URI,
					new String[]{Imps.Contacts.SUBSCRIPTION_TYPE}, 
					selection,
					selectionArgs, null);
			
			if (cursor != null && cursor.moveToFirst()) {
				sub_type = cursor.getInt(0);
			}
		}catch(Exception e){
			DebugConfig.error("DatabaseUtils", "getSubscriptionType", e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return sub_type;
	}


	public static boolean deleteSubscription(ContentResolver cr, String username) {
		String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, username};

		int res= cr.delete(Imps.Contacts.CONTENT_URI, 
				select, selectionArgs);
		return res > 0;
	}
	public static int deleteMembersFromContact(ContentResolver cr, String username) {
		String select = Imps.Tags.USERNAME + "=?";/*OR " + Imps.Tags.USERNAME + "=?";*/
		String[] selectionArgs = {username};
		int res= cr.delete(Imps.Tags.CONTENT_URI,
				select, selectionArgs);
		return res;
	}

	public static int updateFullName(ContentResolver cr, String address, String nickName){
		int res = 0;
		Log.e("ChatGroup", "updateNickName.Address="+address+",NickName="+nickName);
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(4);
		values.put(Imps.Contacts.ACCOUNT, mAccountID);
		values.put(Imps.Contacts.FULLNAME, nickName);
		values.put(Imps.Contacts.USERNAME, address);

		Cursor cursor = cr.query(Imps.Contacts.CONTENT_URI, new String[]{Imps.Contacts.FULLNAME}, selection, selectionArgs, null);

		if ( cursor != null && cursor.moveToFirst() ) {
			res = cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs);
			cursor.close();
		} else {
			if ( cr.insert(Imps.Contacts.CONTENT_URI, values) != null )
				res = 1;
		}

		return res;
	}

	public static int updateNickName(ContentResolver cr, String address, String nickName){
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(1);
		values.put(Imps.Contacts.NICKNAME, nickName);
		return cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs);
	}

	public static int updateStatus(ContentResolver cr, String address, String status){
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(1);
		values.put(Imps.Contacts.STATUS, status);
		return cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs);
	}

	public static int updateGender(ContentResolver cr, String address, String gender){
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(1);
		values.put(Imps.Contacts.GENDER, gender);
		return cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs);
	}

	public static int updateRegion(ContentResolver cr, String address, String region){
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(1);
		values.put(Imps.Contacts.REGION, region);
		return cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs);
	}

	public static int updateUserId(ContentResolver cr, String address, String userid){
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(1);
		values.put(Imps.Contacts.USERID, userid);
		return cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs);
	}

	public static boolean updateDeliverType(ContentResolver cr, String address, int deliver_type){

		if ( deliver_type < 0 )
			deliver_type = 0;

		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(2);

		if ( deliver_type == Imps.Contacts.DELIVER_TYPE_NORMAL ) {
			values.putNull(Imps.Contacts.ISDELIVERD);
			values.putNull(Imps.Contacts.DELIVERTYPE);
		}
		else{
			values.put(Imps.Contacts.ISDELIVERD, -1);
			values.put(Imps.Contacts.DELIVERTYPE, deliver_type);
		}

		return cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs) > 0;
	}

	public static int getDeliverType(ContentResolver cr, String address){
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};

		String[] projection = {Imps.Contacts.ISDELIVERD, Imps.Contacts.DELIVERTYPE};

		Cursor cursor = null;
		int deliverType = Imps.Contacts.DELIVER_TYPE_NORMAL;
		try{
			cursor = cr.query(Imps.Contacts.CONTENT_URI, projection, selection, selectionArgs, null );
			
			if ( cursor != null )
			{
				int isDelivered = 0;
				if ( cursor.moveToFirst() ) {
					try{
						isDelivered = cursor.getInt(0);
						deliverType = cursor.getInt(1);
					}catch(Exception e){}
				}
				if ( isDelivered == 0 )
					deliverType = Imps.Contacts.DELIVER_TYPE_NORMAL;
			}
		}catch(Exception e){
			DebugConfig.error("DatabaseUtils", "getDeliverType", e);
		}finally{
			if ( cursor != null )
				cursor.close();
			cursor = null;
		}
		return deliverType;
	}

	public static int getIsLoadedVcard(ContentResolver cr, String address) {
		Cursor cursor = null;
		int result = 0;
		try{
			String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, address};

			cursor = cr.query(Imps.Contacts.CONTENT_URI,
					new String[]{Imps.Contacts.ISLOADEDVCARD}, 
					selection,
					selectionArgs , null );
			
			if ( cursor != null && cursor.moveToFirst() ) {
				result = cursor.getInt(0);
			}
		}catch(Exception e){
			DebugConfig.error("DatabaseUtils", "getIsLoadedVcard", e);
		}finally{
			if ( cursor != null )
				cursor.close();
			cursor = null;
		}
		return result;
	}
	
	public static String getHash(ContentResolver cr, String address) {
		String result = null;
		
		Cursor cursor = null;
		try{
			String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, address};

			cursor = cr.query(Imps.Contacts.CONTENT_URI,
					new String[]{Imps.Contacts.HASH}, 
					selection,
					selectionArgs , null );
			
			if ( cursor != null && cursor.moveToFirst() ) {
				result = cursor.getString(0);
			}
			else{
				return "nocontact";
			}
		}catch(Exception e){
			DebugConfig.error("DatabaseUtil", "getHash", e);
		}finally{
			if ( cursor != null )
				cursor.close();
			cursor = null;
		}
		return result;
	}

	//set isloadedvcard value to contacts table
	public static boolean updateIsLoadedVcard(ContentResolver cr, String address, int isloadedvcard){
		if ( isloadedvcard < 0 )
			isloadedvcard = 0;

		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(2);
		values.put(Imps.Contacts.ISLOADEDVCARD, isloadedvcard);
		return cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs) > 0;
	}
	
	public static int updateHash(ContentResolver cr, String address, String hash){
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {mAccountID, address};
		ContentValues values = new ContentValues(2);
		values.put(Imps.Contacts.HASH, hash);
		return cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs);
	}


	/**
	 * 
	 * @param cr
	 * @param list
	 * @param mProviderId
	 * @param accountId
	 * @param username
	 * @param nickname
	 * @param subscriptionType
	 * @param subscriptionStatus
	 * @return -1: failed, 0:update, 1:insert
	 */
	public static int insertOrUpdateSubscription(ContentResolver cr, 
			ContactList list, 
			long mProviderId,
			long accountId,
			String username, 
			String nickname, 
			int subscriptionType,
			int subscriptionStatus,
			String greeting,
			String userid,
			String region,
			String phoneNum
			) {

		int res = -1;
		int listId = 0;

		if ( list != null )
		{
			String selection = Imps.ContactList.NAME + "=? AND " + 
					Imps.ContactList.PROVIDER + "=? AND " + 
					Imps.ContactList.ACCOUNT + "=?";

			String[] selectionArgs = { list.getName(), 
					Long.toString(mProviderId),
					Long.toString(accountId) 
			};

			Cursor cursor = null; 
			try {
				cursor = cr.query(Imps.ContactList.CONTENT_URI, 
						new String[]{Imps.ContactList._ID},
						selection, selectionArgs, null);
				if (cursor.moveToFirst()) {
					listId = cursor.getInt(0);
				}
			}catch(Exception e){
				DebugConfig.debug("DatabaseUtils", "insertOrUpdateSubscription---1",e);
			}finally {
				if ( cursor != null )
					cursor.close();
			}
		}

		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {Long.toString(accountId), username};
		Cursor cursor = cr.query(Imps.Contacts.CONTENT_URI, new String[] { Imps.Contacts._ID, Imps.Contacts.SUBSCRIPTION_TYPE },
				selection, selectionArgs, null);

		Uri uri = null;
		if (cursor != null && cursor.moveToFirst()) {
			ContentValues values = new ContentValues(5);
			values.put(Imps.Contacts.SUBSCRIPTION_TYPE, subscriptionType);
			values.put(Imps.Contacts.SUBSCRIPTION_STATUS, subscriptionStatus);
			values.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_NORMAL);
			if ( nickname != null ) 
				values.put(Imps.Contacts.NICKNAME, nickname);
			if ( greeting != null )
				values.put(Imps.Contacts.SUBSCRIPTIONMESSAGE, greeting);
			if ( userid != null )
				values.put(Imps.Contacts.USERID, userid);
			if ( region != null)
				values.put(Imps.Contacts.REGION, region);
			if (phoneNum != null)
				values.put(Imps.Contacts.PHONE_NUMBER, phoneNum);

			long contactId = cursor.getLong(cursor.getColumnIndexOrThrow(Imps.Contacts._ID));
			int  sub_type = cursor.getInt(1);

			uri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId);
			if ( cr.update(uri, values, null, null) > 0 ) {
				if ( sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_NONE )
					res = 1;
				else
					res = 0;
			}
		} else {
			ContentValues values = new ContentValues();
			values.put(Imps.Contacts.USERNAME, username);
			values.put(Imps.Contacts.NICKNAME, nickname);
			values.put(Imps.Contacts.FULLNAME, nickname);
			values.put(Imps.Contacts.PROVIDER, String.valueOf(mProviderId));
			values.put(Imps.Contacts.ACCOUNT, String.valueOf(accountId));
			values.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_NORMAL);
			values.put(Imps.Contacts.CONTACTLIST, listId);
			values.put(Imps.Contacts.SUBSCRIPTION_TYPE, subscriptionType);
			values.put(Imps.Contacts.SUBSCRIPTION_STATUS, subscriptionStatus);
			values.put(Imps.Contacts.USERID, userid);

			if (phoneNum != null)
				values.put(Imps.Contacts.PHONE_NUMBER, phoneNum);

			if ( region != null)
				values.put(Imps.Contacts.REGION, region);

			if ( greeting != null )
				values.put(Imps.Contacts.SUBSCRIPTIONMESSAGE, greeting);

			uri = cr.insert(Imps.Contacts.CONTENT_URI, values);

			if ( uri != null )
				res = 1;
		}

		if ( cursor != null)
			cursor.close();
		return res;
	}

	public static int updateSubscriptionMessage(ContentResolver cr,
			Long accountId, 
			String message,
			String username
			) {

		int res = -1;
		Cursor cursor = null;

		try{
			String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {Long.toString(accountId), username};
			cursor = cr.query(Imps.Contacts.CONTENT_URI, new String[] { Imps.Contacts._ID },
					selection, selectionArgs, null);

			Uri uri = null;
			if (cursor != null && cursor.moveToFirst()) {
				ContentValues values = new ContentValues(2);
				values.put(Imps.Contacts.SUBSCRIPTIONMESSAGE, message);
				long contactId = cursor.getLong(cursor.getColumnIndexOrThrow(Imps.Contacts._ID));
				uri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId);
				res = cr.update(uri, values, null, null); 
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "updateSubscriptionMessage",e);
		}finally{
			if ( cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return res;
	}












	///////////**********ë‚˜ì�˜ í”„ë¡œí•„ ê´€ë ¨ ìž�ë£Œê¸°ì§€ í•¨ìˆ˜ë“¤ ***************************/////////
	public static boolean update_UserProfile_DeliveredType(ContentResolver cr, int isDelivered){
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.IS_DELIVERED, isDelivered);

		try{
			return cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
		}catch(Exception e) {
			DebugConfig.debug("DatabaseUtils", "update_UserProfile_DeliveredType",e);
		}
		return false;
	}

	public static boolean insertOrUpdateUserNickName(ContentResolver cr, String _nickName) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.NICKNAME, _nickName);
		values.put(Imps.Profile.PUBLICID, mAccountID);
		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.NICKNAME}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
			}
			else 
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserNickName",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return res; 
	}

	public static boolean insertOrUpdateUserID(ContentResolver cr, String _userID) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.USERID, _userID);

		Cursor cursor = null;

		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.USERID}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res =  cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
			}
			else 
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserID",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return res; 
	}
	
	public static boolean insertOrUpdateUserPhotoHash(ContentResolver cr, String _photoHash) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.HASH, _photoHash);

		Cursor cursor = null;

		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.HASH}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res =  cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
			}
			else 
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserPhotoHash",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return res; 
	}

	/**
	 * ì‚¬ìš©ìž�Genderë³´ê´€
	 * @param cr
	 * @param _status
	 * @return
	 */
	public static boolean insertOrUpdateUserGender(ContentResolver cr, String _gender) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.GENDER, _gender);

		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.GENDER}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res =  cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
			}
			else{
				res =  cr.insert(Imps.Profile.CONTENT_URI, values) != null;
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserGender",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}

	/**
	 * ì‚¬ìš©ìž�Regionë³´ê´€
	 * @param cr
	 * @param _status
	 * @return
	 */
	public static boolean insertOrUpdateUserRegion(ContentResolver cr, String _region) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.REGION, _region);
		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.REGION}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
			}
			else{
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserRegion",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return res; 
	}

	/**
	 * ì „í™”ë²ˆí˜¸ë³´ê´€
	 * @param cr
	 * @param _phone
	 * @return
	 */
	public static boolean insertOrUpdateUserPhone(ContentResolver cr, String _phone) {
		//modified for multi user support. This function need to be called after creating or getting account id.
		if ( mAccountID == null )
			return false;

		ContentValues values = new ContentValues(2);
		values.put(Imps.Profile.PHONE, _phone);
		values.put(Imps.Profile.PUBLICID, mAccountID);

		Cursor cursor = null;

		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.PHONE}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0; 
			}
			else{
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserPhone",e);
		}finally{
			if ( cursor != null ){
				cursor.close();
				cursor = null;
			}
		}
		return res; 
	}

	public static boolean insertOrUpdateUserEmail(ContentResolver cr, String _email) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.EMAIL, _email);

		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.EMAIL}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
			}
			else{
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserEmail",e);
		}finally{
			if ( cursor != null ){
				cursor.close();
				cursor = null;
			}
		}
		return res;
	}

	
	public static boolean insertOrUpdateUserEmailPwd(ContentResolver cr, String _emailPwd) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.EMAIL_PASSWORD, _emailPwd);

		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.EMAIL_PASSWORD}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0; 
			}
			else{
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserEmailPwd",e);
		}finally{
			if ( cursor != null ){
				cursor.close();
				cursor = null;
			}
		}
		return res; 
	}
	
	public static boolean insertOrUpdateUserEmailLogined(ContentResolver cr, String _emailLogined) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.EMAIL_LOGINED, _emailLogined);

		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.EMAIL_LOGINED}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0; 
			}
			else{
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserEmailLogined",e);
		}finally{
			if ( cursor != null ){
				cursor.close();
				cursor = null;
			}
		}
		return res; 
	}
	
	public static boolean insertOrUpdateUserAllowAddMe(ContentResolver cr, String _allowAddMe) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Profile.ALLOW_ADD_ME, _allowAddMe);

		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.ALLOW_ADD_ME}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0; 
			}
			else{
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserAllowAddMe",e);
		}finally{
			if ( cursor != null ){
				cursor.close();
				cursor = null;
			}
		}
		return res; 
	}
	
	public static String getUserNickName(ContentResolver cr) {
		Cursor cursor = null;
		String nickName = null;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.NICKNAME}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				nickName = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "insertOrUpdateUserAllowAddMe",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return nickName;
	}

	/*
	 * ì‚¬ìš©ìž�ì „í™”ë²ˆí˜¸ì–»ê¸°
	 */
	public static String getUserPhone(ContentResolver cr) {
		Cursor cursor = null;
		String phone = null;

		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.PHONE}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				phone = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserPhone",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return phone;
	}

	/*
	 * ì‚¬ìš©ìž�genderì–»ê¸°
	 */
	public static String getUserGender(ContentResolver cr) {
		Cursor cursor = null;
		String phone = null;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.GENDER}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				phone = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserGender",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return phone;
	}
	
	public static String getUserID(ContentResolver cr) {
		Cursor cursor = null;
		String id = null;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.USERID}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				id = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserID",e);
		}finally{
			if ( cursor != null ){
				cursor.close();
				cursor = null;
			}
		}
		return id;
	}

	/*
	 * ì‚¬ìš©ìž�ì�˜ publicì •ë³´ ì–»ê¸°
	 * 
	 */

	/**
	 * get user photo hash
	 */
	public static String getUserPhotoHash(ContentResolver cr) {
		Cursor cursor = null;
		String hash = null;

		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.HASH}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				hash = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserPhotoHash",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return hash;
	}
	/**
	 * get user email
	 */
	public static String getUserEmail(ContentResolver cr) {
		Cursor cursor = null;
		String email = null;

		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};
			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.EMAIL}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				email = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserEmail",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return email;
	}
	
	/**
	 * get user email
	 */
	public static String getUserEmailPassword(ContentResolver cr) {
		Cursor cursor = null;
		String emailPwd = null;

		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.EMAIL_PASSWORD}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				emailPwd = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserEmailPassword",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return emailPwd;
	}
	
	public static String getUserEmailLogined(ContentResolver cr) {
		Cursor cursor = null;
		String emailLogined = null;

		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.EMAIL_LOGINED}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				emailLogined = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserEmailLogined",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return emailLogined;
	}
	
	public static String getUserAllowAddMe(ContentResolver cr) {
		Cursor cursor = null;
		String allowAddMe = null;

		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, new String[]{Imps.Profile.ALLOW_ADD_ME}, select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				allowAddMe = cursor.getString(0);
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserAllowAddMe",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return allowAddMe;
	}
	

	/**
	 * ì‚¬ìš©ìž�ì�˜ ì •ë³´ë¥¼ ì–»ëŠ”ë‹¤
	 * @param cr
	 * @param userInfo[0]:Gender, userInfo[1]:nickName, userInfo[2]:phone, userInfo[3]:publicId, userInfo[4]:region, userInfo[5]:status
	 * @return boolean
	 */
	public static Map<String, String> getUserInfo(ContentResolver cr) {
		Cursor cursor = null;

		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, 
					new String[]{Imps.Profile.GENDER, 
					Imps.Profile.NICKNAME, 
					Imps.Profile.PHONE, 
					Imps.Profile.PUBLICID, 
					Imps.Profile.REGION, 
					Imps.Profile.HASH,
					Imps.Profile.USERID
					}, 
					select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				Map<String, String> result = new HashMap<String, String>();
				result.put(Imps.Profile.GENDER, cursor.getString(0));
				result.put(Imps.Profile.NICKNAME, cursor.getString(1));
				result.put(Imps.Profile.PHONE, cursor.getString(2));
				result.put(Imps.Profile.PUBLICID, cursor.getString(3));
				result.put(Imps.Profile.REGION, cursor.getString(4));
				result.put(Imps.Profile.HASH, cursor.getString(5));
				result.put(Imps.Profile.USERID, cursor.getString(6));
				return result;
			}
		}catch(Exception e){
			DebugConfig.debug("DatabaseUtils", "getUserInfo",e);
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}
		return null;
	}


	/**
	 * ì‚¬ìš©ìž�ì�˜ ì •ë³´ë¥¼ ìž�ë£Œê¸°ì§€ì—� ë³´ê´€í•œë‹¤
	 * @param cr
	 * @param userInfo[0]:Gender, userInfo[1]:nickName, userInfo[2]:phone, userInfo[3]:publicId, userInfo[4]:region, userInfo[5]:status
	 * @return boolean
	 */
	public static boolean insertOrUpdateUserInfo(ContentResolver cr, Map<String, String> userInfo) {
		
		if ( userInfo == null )
			return false;

		if ( mAccountID == null )
			return false;
		
		ContentValues values = new ContentValues(13);
		values.put(Imps.Profile.GENDER, userInfo.get(Imps.Profile.GENDER));
		values.put(Imps.Profile.NICKNAME, userInfo.get(Imps.Profile.NICKNAME));
		values.put(Imps.Profile.PHONE, userInfo.get(Imps.Profile.PHONE));
		values.put(Imps.Profile.PUBLICID, mAccountID);
		values.put(Imps.Profile.REGION, userInfo.get(Imps.Profile.REGION));
		values.put(Imps.Profile.HASH, userInfo.get(Imps.Profile.HASH));
		values.put(Imps.Profile.USERID, userInfo.get(Imps.Profile.USERID));

		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI, 
					new String[]{Imps.Profile.NICKNAME},
					select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
			}
			else {
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
				Imps.Profile.setProfileDefaultValues(cr);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}

	public static boolean insertOrUpdateUserInfo(ContentResolver cr, JSONObject userInfo) {

		if ( userInfo == null )
			return false;

		if ( mAccountID == null )
			return false;

		ContentValues values = new ContentValues(13);
		try {
			values.put(Imps.Profile.GENDER, userInfo.getString(Imps.Profile.GENDER));
			values.put(Imps.Profile.NICKNAME, userInfo.getString(Imps.Profile.NICKNAME));
			values.put(Imps.Profile.PHONE, userInfo.getString(Imps.Profile.PHONE));
			values.put(Imps.Profile.PUBLICID, mAccountID);
			values.put(Imps.Profile.REGION, userInfo.getString(Imps.Profile.REGION));
			values.put(Imps.Profile.HASH, userInfo.getString(Imps.Profile.HASH));
			values.put(Imps.Profile.USERID, userInfo.getString(Imps.Profile.USERID));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Profile.CONTENT_URI,
					new String[]{Imps.Profile.NICKNAME},
					select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Profile.CONTENT_URI, values, null, null) > 0;
			}
			else {
				res = cr.insert(Imps.Profile.CONTENT_URI, values) != null;
				Imps.Profile.setProfileDefaultValues(cr);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}

	public static boolean insertOrUpdateContactInfo(ContentResolver cr, Contact contact) {

		if ( contact == null )
			return false;

		if ( mAccountID == null )
			return false;

		String username = contact.getAddress().getAddress();

		if ( username == null || username.isEmpty() )
			return false;

		ContentValues values = new ContentValues(14);
		values.put(Imps.Contacts.ACCOUNT, mAccountID);
		values.put(Imps.Contacts.PROVIDER, "1");
		values.put(Imps.Contacts.USERNAME, username);
		values.put(Imps.Contacts.NICKNAME, contact.getName());
		values.put(Imps.Contacts.FULLNAME, contact.mFullName);
		values.put(Imps.Contacts.DESCRIPTION, contact.mDescription);
		values.put(Imps.Contacts.MOBILE, contact.mMobile);
		values.put(Imps.Contacts.STAR, contact.star);
		values.put(Imps.Contacts.GENDER, contact.gender);
		values.put(Imps.Contacts.REGION, contact.region);
		values.put(Imps.Contacts.STATUS, contact.status);
		values.put(Imps.Contacts.HASH, contact.hash);
		values.put(Imps.Contacts.USERID, contact.userid);
		values.put(Imps.Contacts.PHONE_NUMBER, contact.phoneNum);

		Cursor cursor = null;
		boolean res = false;
		try{
			String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, username};

			cursor = cr.query(Imps.Contacts.CONTENT_URI,
					new String[]{Imps.Contacts.NICKNAME},
					select, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Contacts.CONTENT_URI, values, select, selectionArgs) > 0;
			}
			else
				res = cr.insert(Imps.Contacts.CONTENT_URI, values) != null;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}

	public static boolean isExistTag(ContentResolver cr, String tag)
	{
		if ( mAccountID == null )
			return false;

		Cursor cursor = null;
		boolean res = false;

		String[] TAGS_PROJECTION = { Imps.Tags._ID, Imps.Tags.USERNAME};

		try{
			String selection = Imps.Tags.ACCOUNT + "=? AND " + Imps.Tags.TAG + "=?";
			String[] selectionArgs = {mAccountID, tag};

			cursor = cr.query(Imps.Tags.CONTENT_URI,
					TAGS_PROJECTION,
					selection, selectionArgs, null);
			if ( cursor != null ) {
				while (cursor.moveToNext()) {
					res = true;
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}

	public static ArrayList<String> getTagUsers(ContentResolver cr, String tag)
	{
		ArrayList<String> users = new ArrayList<>();

		if ( mAccountID == null )
			return users;

		Cursor cursor = null;
		int res;

		String[] TAGS_PROJECTION = { Imps.Tags._ID, Imps.Tags.USERNAME};

		try{
			String selection = Imps.Tags.ACCOUNT + "=? AND " + Imps.Tags.TAG + "=?";
			String[] selectionArgs = {mAccountID, tag};

			cursor = cr.query(Imps.Tags.CONTENT_URI,
					TAGS_PROJECTION,
					selection, selectionArgs, null);
			if ( cursor != null ) {
				while (cursor.moveToNext()) {
					String username = cursor.getString(cursor.getColumnIndex(Imps.Tags.USERNAME));

					if (username != null && !username.isEmpty())
						users.add(username);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return users;
	}

	public static ArrayList<String> getUserTags(ContentResolver cr, String username)
	{
		ArrayList<String> tags = new ArrayList<>();

		if ( mAccountID == null )
			return tags;

		Cursor cursor = null;
		int res;

		String[] TAGS_PROJECTION = { Imps.Tags._ID, Imps.Tags.TAG};

		try{
			String selection = Imps.Tags.ACCOUNT + "=? AND " + Imps.Tags.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, username};

			cursor = cr.query(Imps.Tags.CONTENT_URI,
					TAGS_PROJECTION,
					selection, selectionArgs, null);
			if ( cursor != null ) {
				while (cursor.moveToNext()) {
					String tag = cursor.getString(cursor.getColumnIndex(Imps.Tags.TAG));
					tags.add(tag);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return tags;
	}

	public static HashMap<String,ArrayList<String>> getAllTags(ContentResolver cr)
	{
		HashMap<String,ArrayList<String>> hashTags = new HashMap<>();

		if ( mAccountID == null )
			return hashTags;

		Cursor cursor = null;
		int res;

		String[] TAGS_PROJECTION = { Imps.Tags._ID, Imps.Tags.TAG, Imps.Tags.USERNAME};

		try{
			String selection = Imps.Tags.ACCOUNT + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Tags.CONTENT_URI,
					TAGS_PROJECTION,
					selection, selectionArgs, Imps.Tags.TAG);
			if ( cursor != null )
			{
				while ( cursor.moveToNext() )
				{
					String tag = cursor.getString(cursor.getColumnIndex(Imps.Tags.TAG));
					String username = cursor.getString(cursor.getColumnIndex(Imps.Tags.USERNAME));
					if ( !hashTags.containsKey(tag) )
						hashTags.put(tag,new ArrayList<String>());
					if ( username != null && !username.isEmpty() )
						hashTags.get(tag).add(username);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return hashTags;
	}

	public static HashMap<String,ArrayList<String>> getAllTagsWithName(ContentResolver cr)
	{
		HashMap<String,ArrayList<String>> hashTags = new HashMap<>();

		if ( mAccountID == null )
			return hashTags;

		Cursor cursor = null;
		int res;

		String[] TAGS_PROJECTION = { Imps.Tags._ID, Imps.Tags.TAG, Imps.Tags.USERNAME};

		try{
			String selection = Imps.Tags.ACCOUNT + "=?";
			String[] selectionArgs = {mAccountID};

			cursor = cr.query(Imps.Tags.CONTENT_URI,
					TAGS_PROJECTION,
					selection, selectionArgs, null);
			if ( cursor != null )
			{
				while ( cursor.moveToNext() )
				{
					String tag = cursor.getString(cursor.getColumnIndex(Imps.Tags.TAG));
					String username = cursor.getString(cursor.getColumnIndex(Imps.Tags.USERNAME));
					if ( !hashTags.containsKey(tag) )
						hashTags.put(tag,new ArrayList<String>());
					if ( username != null && !username.isEmpty() ) {
						Contact contact = getContactInfo(cr, username);

						if ( contact != null )
							hashTags.get(tag).add(contact.getName());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return hashTags;
	}

	public static boolean insertOrUpdateUserTags(ContentResolver cr, String username, ArrayList<String> tags)
	{
		if ( mAccountID == null )
			return false;

		Cursor cursor = null;
		int res;

		String selection;
		String[] selectionArgs;

		try{
			selection = Imps.Tags.ACCOUNT + "=? AND " + Imps.Tags.USERNAME + "=?";
			selectionArgs = new String[]{mAccountID, username};

			cr.delete(Imps.Tags.CONTENT_URI, selection, selectionArgs);

			ContentValues values = new ContentValues(3);

			for ( int i = 0; i < tags.size(); i ++ )
			{
				addTag(cr, tags.get(i));

				values.put(Imps.Tags.TAG, tags.get(i));
				values.put(Imps.Tags.USERNAME, username);
				values.put(Imps.Tags.ACCOUNT, mAccountID);

				cr.insert(Imps.Tags.CONTENT_URI, values);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return true;
	}

	public static boolean insertOrUpdateTagUsers(ContentResolver cr, String tag, List<String> users)
	{
		if ( mAccountID == null )
			return false;

		Cursor cursor = null;
		int res;
		String selection;
		String[] selectionArgs;

		try{
			selection = Imps.Tags.ACCOUNT + "=? AND " + Imps.Tags.TAG + "=?";
			selectionArgs = new String[]{mAccountID, tag};

			cr.delete(Imps.Tags.CONTENT_URI, selection, selectionArgs);

			addTag(cr, tag);

			ContentValues values = new ContentValues(3);

			for ( int i = 0; i < users.size(); i ++ )
			{
				values.put(Imps.Tags.TAG, tag);
				values.put(Imps.Tags.USERNAME, users.get(i));
				values.put(Imps.Tags.ACCOUNT, mAccountID);

				cr.insert(Imps.Tags.CONTENT_URI, values);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return true;
	}

	public static boolean deleteTag(ContentResolver cr, String tag)
	{
		if ( mAccountID == null )
			return false;

		Cursor cursor = null;
		boolean res = false;
		String selection;
		String[] selectionArgs;

		try{
			selection = Imps.Tags.ACCOUNT + "=? AND " + Imps.Tags.TAG + "=?";
			selectionArgs = new String[]{mAccountID, tag};

			res = cr.delete(Imps.Tags.CONTENT_URI, selection, selectionArgs) > 0;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}

	public static boolean addTag(ContentResolver cr, String tag)
	{
		if ( mAccountID == null )
			return false;

		Cursor cursor = null;
		boolean res = false;
		String selection;
		String[] selectionArgs;

		try{
			selection = Imps.Tags.ACCOUNT + "=? AND " + Imps.Tags.TAG + "=? AND " + Imps.Tags.USERNAME + "=?";
			selectionArgs = new String[]{mAccountID, tag, ""};

			cursor = cr.query(Imps.Tags.CONTENT_URI, new String[]{Imps.Tags.USERNAME}, selection, selectionArgs, null);

			if ( cursor == null || !cursor.moveToFirst() )
			{
				ContentValues values = new ContentValues(3);

				values.put(Imps.Tags.TAG, tag);
				values.put(Imps.Tags.USERNAME, "");
				values.put(Imps.Tags.ACCOUNT, mAccountID);

				cr.insert(Imps.Tags.CONTENT_URI, values);
			}

			res = true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}

	public static boolean editTagUsers(ContentResolver cr, String org_tag, String tag, ArrayList<String> users)
	{
		if ( mAccountID == null )
			return false;

		deleteTag(cr, org_tag);

		return insertOrUpdateTagUsers(cr, tag, users);
	}

	public static Contact getContactInfo(ContentResolver cr, String username)
	{
		Contact contact = null;

		if ( mAccountID == null )
			return null;

		Cursor cursor = null;
		int res;

		String[] CONTACT_PROJECTION = { Imps.Contacts._ID, Imps.Contacts.PROVIDER,
				Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME, Imps.Contacts.NICKNAME,
				Imps.Contacts.FULLNAME, Imps.Contacts.DESCRIPTION, Imps.Contacts.MOBILE,
				Imps.Contacts.STAR, Imps.Contacts.USERID, Imps.Contacts.REGION, Imps.Contacts.SUBSCRIPTION_TYPE,
				Imps.Contacts.TYPE, Imps.Contacts.PHONE_NUMBER
		};

		try{
			String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, username};

			cursor = cr.query(Imps.Contacts.CONTENT_URI,
					CONTACT_PROJECTION,
					selection, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				String address = cursor.getString(cursor.getColumnIndex(Imps.Contacts.USERNAME));
				String nickName = cursor.getString(cursor.getColumnIndex(Imps.Contacts.NICKNAME));
				String fullName = cursor.getString(cursor.getColumnIndex(Imps.Contacts.FULLNAME));
				String description = cursor.getString(cursor.getColumnIndex(Imps.Contacts.DESCRIPTION));
				String mobile = cursor.getString(cursor.getColumnIndex(Imps.Contacts.MOBILE));
				int star = cursor.getInt(cursor.getColumnIndex(Imps.Contacts.STAR));
				String userid = cursor.getString(cursor.getColumnIndex(Imps.Contacts.USERID));
				String region = cursor.getString(cursor.getColumnIndex(Imps.Contacts.REGION));
				int sub_type = cursor.getInt(cursor.getColumnIndex(Imps.Contacts.SUBSCRIPTION_TYPE));
				int type = cursor.getInt(cursor.getColumnIndex(Imps.Contacts.TYPE));
				String phoneNum = cursor.getString(cursor.getColumnIndex(Imps.Contacts.PHONE_NUMBER));

				contact = new Contact(new XmppAddress(address), nickName);

/*
				if ( fullName == null || fullName.isEmpty() )
					fullName = nickName;
*/

				contact.mFullName = fullName;
				contact.mDescription = description;
				contact.mMobile = mobile;
				contact.star = star;
				contact.userid = userid;
				contact.region = region;
				contact.sub_type = sub_type;
				contact.contact_type = type;
				contact.phoneNum = phoneNum;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return contact;
	}

	public static boolean updateFriendInfo(ContentResolver cr, String username, Map<String, String> userInfo) {

		if ( userInfo == null )
			return false;

		if ( mAccountID == null )
			return false;

		ContentValues values = new ContentValues(13);
		values.put(Imps.Contacts.NICKNAME, userInfo.get(Imps.Contacts.NICKNAME));
		values.put(Imps.Contacts.MOBILE, userInfo.get(Imps.Contacts.MOBILE));
		values.put(Imps.Contacts.DESCRIPTION, userInfo.get(Imps.Contacts.DESCRIPTION));

		Cursor cursor = null;
		boolean res = false;
		try{
			String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, username};

			cursor = cr.query(Imps.Contacts.CONTENT_URI,
					new String[]{Imps.Contacts.NICKNAME},
					selection, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs) > 0;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}
	public static boolean updateFriendInfo(ContentResolver cr, String username, JSONObject userInfo) {

		if ( userInfo == null )
			return false;

		if ( mAccountID == null )
			return false;

		ContentValues values = new ContentValues(13);

		values.put(Imps.Contacts.USERNAME, username);
		values.put(Imps.Contacts.ACCOUNT, mAccountID);

		try {
			values.put(Imps.Contacts.NICKNAME, userInfo.getString(Imps.Params.ALIAS));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			values.put(Imps.Contacts.MOBILE, userInfo.getString(Imps.Params.MOBILES));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			values.put(Imps.Contacts.DESCRIPTION, userInfo.getString(Imps.Params.DESCRIPTION));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Cursor cursor = null;
		boolean res = false;
		try{
			String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, username};

			cursor = cr.query(Imps.Contacts.CONTENT_URI,
					new String[]{Imps.Contacts.NICKNAME},
					selection, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs) > 0;
			} else {
				res = cr.insert(Imps.Contacts.CONTENT_URI, values) != null;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}
	public static boolean updateFriendStar(ContentResolver cr, String username, int star) {

		if ( mAccountID == null )
			return false;

		ContentValues values = new ContentValues(1);
		values.put(Imps.Contacts.STAR, String.valueOf(star));

		Cursor cursor = null;
		boolean res = false;
		try{
			String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {mAccountID, username};

			cursor = cr.query(Imps.Contacts.CONTENT_URI,
					new String[]{Imps.Contacts.NICKNAME},
					selection, selectionArgs, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.Contacts.CONTENT_URI, values, selection, selectionArgs) > 0;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}

	/**
	 * ê·¸ë£¹ì�˜ ì •ë³´ë¥¼ ìž�ë£Œê¸°ì§€ì—� ë³´ê´€í•œë‹¤   ë¯¸ì™„ì„±
	 * @param cr
	 * @param userInfo[0]:Gender, userInfo[1]:nickName, userInfo[2]:phone, userInfo[3]:publicId, userInfo[4]:region, userInfo[5]:status
	 * @return boolean
	 */
/*	public static boolean updateGroupInfo(ContentResolver cr, Map<String, String> groupInfo) {
		
		if ( groupInfo == null )
			return false;
		
		ContentValues values = new ContentValues(2);
		values.put(Imps.GroupProfile.HASH, groupInfo.get(Imps.GroupProfile.HASH));
		values.put(Imps.GroupProfile.GROUPID, groupInfo.get(Imps.GroupProfile.GROUPID));
		
		Cursor cursor = null;
		boolean res = false;
		try{
			cursor = cr.query(Imps.GroupProfile.CONTENT_URI, 
					new String[]{Imps.GroupProfile.GROUPID}, 
					null, null, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				res = cr.update(Imps.GroupProfile.CONTENT_URI, values, null, null) > 0;
			}
			else
				res = cr.insert(Imps.GroupProfile.CONTENT_URI, values) != null;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		return res;
	}
*/
	/*public static void  debugDatabase(ContentResolver resolver)  throws SQLException
    {
    	if (false) {
    		String[] projection =  {Imps.Avatars._ID, Imps.Avatars.ACCOUNT, Imps.Avatars.CONTACT,Imps.Avatars.HASH,Imps.Avatars.PROVIDER};
    	        Cursor cursor = resolver.query(Imps.Avatars.CONTENT_URI, projection, null,null, null);

    	        try {
        	        while (cursor.moveToNext()) {
        	        		DebugConfig.debug(TAG,"Avatar Id : " + cursor.getString(0) + " Account :" + cursor.getString(1) + " Contact : " + cursor.getString(2) + " Provider : " + cursor.getString(3) + " Hash : " + cursor.getString(4));
        	        }

    	        } finally {
    	            cursor.close();
    	        }

    	        cursor = resolver.query(Imps.Big_Avatars.CONTENT_URI, projection, null,null, null);

    	        try {
        	        while (cursor.moveToNext()) {
        	        	DebugConfig.debug(TAG,"Avatar Id : " + cursor.getString(0) + " Account :" + cursor.getString(1) + " Contact : " + cursor.getString(2) + " Provider : " + cursor.getString(3) + " Hash : " + cursor.getString(4));
        	        }

    	        } finally {
    	            cursor.close();
    	        }

    	}
    }*/
}
