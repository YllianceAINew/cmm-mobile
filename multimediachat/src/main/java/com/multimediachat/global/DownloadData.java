/*
 * @author : lightsky
 * @date : 2014/04/08
 * @description : download profile, account information from server
 */
package com.multimediachat.global;

import android.content.Context;

import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.im.IImConnection;

public class DownloadData {
	static IImConnection conn = null;
	private static String TAG = "DownloadData";
	public static DownloadData instance = null;
	public DownloadData(IImConnection conn,  Context _mContext) {
		DownloadData.conn = conn;
	}
	
	public static DownloadData getInstance(IImConnection con, Context _mContext) {
		if (instance == null)
			instance = new DownloadData(con, _mContext);
		conn = con;
		return instance;
	}
	
	public String[] setQueryForResult(String[] param) {
	    String res[] = null;
       
	    try{
        	res = conn.getFindFriendManager().setQueryForResult(param);
        }catch(Exception e) {
        	DebugConfig.debug(TAG,"DownloadData.java / registerData exception.");
        }
        return res;
	}
	public void setQuery(String[] param) {
	    try{
	    	conn.getFindFriendManager().setQuery(param);
        }catch(Exception e) {
        	DebugConfig.debug(TAG,"DownloadData.java / registerData exception.");
        }
	}
	
	public String[] getQueryResult(String[] param) {
	    String res[] = null;
       
	    try{
        	res = conn.getFindFriendManager().getQueryResult(param);
        }catch(Exception e) {
        	DebugConfig.debug(TAG,"DownloadData.java / registerData exception.");
        }
        return res;
	}
	public static String getTagValue(String tag, String text) {
		int st, en;
		st = text.indexOf("<" + tag + ">");
		en = text.indexOf("</" + tag + ">");
		st += tag.length() + 2;
		
		String res = null;
		try{
			res = text.substring(st, en); 
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return res; 
	}
}
