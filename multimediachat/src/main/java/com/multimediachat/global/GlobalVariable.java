package com.multimediachat.global;

import com.multimediachat.util.PrefUtil.mPref;

public class GlobalVariable {
	public static String account_id = null;	//added for multi user support. This value is _id in accounts table where the user login

	public static String PhoneNumber = "";
	public static String Region = "";
	public static int authorizedState = 0;
	public static String DeviceNumber = "";

	public static String WEBAPI_URL = mPref.getString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header) + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain)+"/rsvcs/";
	public static String FILE_SERVER_URL = mPref.getString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header) + mPref.getString(GlobalConstrants.FILE_SERVER_URL, GlobalConstrants.file_server_domain)+"/rsvcs/";
	public static String HELP_URL = "file:///android_asset/www/";
}
