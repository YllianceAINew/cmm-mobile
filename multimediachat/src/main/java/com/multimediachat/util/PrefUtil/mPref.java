package com.multimediachat.util.PrefUtil;

import android.content.ContentResolver;

import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.provider.Imps;

public class mPref {
	private static ContentResolver cr;
	
	private static ContentResolver getResolver(){
		if ( cr == null )
			cr = ImApp.getInstance().getContentResolver();
		return cr;
	}
	
	public static String getString(String key, String initVal) {
		
		String value = null;
		value = Imps.ProviderSettings.getStringValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key);
		
		if ( value == null )
			value = initVal;
		
		return value;
	}
	
	public static void putString(String key, String value) {
		try{
			Imps.ProviderSettings.putStringValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key, value);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static int getInt(String key, int initVal) {
		int value = -1;
		value = Imps.ProviderSettings.getIntValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key);

		if ( value == - 1)
			value = initVal;

		return value;
	}

	public static void putInt(String key, int value) {
		Imps.ProviderSettings.putIntValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key, value);
	}

	public static boolean getBoolean(String key) {
		return Imps.ProviderSettings.getBooleanValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key);
	}

	public static void putBoolean(String key, boolean value) {
		Imps.ProviderSettings.putBooleanValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key, value);
	}
	
	
	public static long getLong(String key, long initVal) {
		long value = -1;
		value = Imps.ProviderSettings.getLongValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key);
		
		if ( value == -1)
			value = initVal;
		
		return value;
	}
	
	public static void putLong(String key, long value) {
		try{
			Imps.ProviderSettings.putLongValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key, value);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void remove(String key) {
		Imps.ProviderSettings.putNullValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key);
	}
	
	public static void putFloat(String key, float value){
		Imps.ProviderSettings.putFloatValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key, value);
	}
	
	public static float getFloat(String key, float initVal){
		float value = 0;
		value = Imps.ProviderSettings.getFloatValue(getResolver(), Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, key);
		return value;
	}

}
