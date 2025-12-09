package com.multimediachat.util;

import java.net.URLEncoder;

import com.multimediachat.app.DebugConfig;

public class LogCleaner {
    @SuppressWarnings("deprecation")
	public static String clean (String msg)
    {
        if (DebugConfig.DEBUG)
            return msg;
        else
            return URLEncoder.encode(msg);
    }
    
    public static void warn (String tag, String msg)
    {
        
            DebugConfig.debug(tag, clean(msg));
    }
    
    public static void debug (String tag, String msg)
    {
        	DebugConfig.debug(tag, clean(msg));
    }
    
    public static void error (String tag, String msg, Exception e)
    {
    	DebugConfig.error(tag, clean(msg) + " Exception : " +  e.getLocalizedMessage());
    }
    

    public static void error (String tag, String msg, Throwable e)
    {
    	DebugConfig.error(tag, clean(msg)+ " Exception : " +  e.getLocalizedMessage());
    }
}
