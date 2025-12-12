package com.multimediachat.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.output.StringBuilderWriter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class Debug {

	public static boolean DEBUG_ENABLED = false;//DebugConfig.DEBUG;
	public static final boolean DEBUGGER_ATTACH_ENABLED = false;//DebugConfig.DEBUG;//DebugConfig.DEBUG;

	@SuppressLint("SimpleDateFormat")
	public static void recordTrail(Context context, String key, Date date) {
		if (DEBUG_ENABLED) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			recordTrail(context, key, sdf.format(date));
		}
	}

	public static String getTrail(Context context) {
		if (DEBUG_ENABLED) {
			File trail = new File(context.getFilesDir(), "trail.properties");
			try {
				BufferedReader reader = new BufferedReader(new FileReader(trail));
				String line;
				StringBuilder builder = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
					builder.append("\n");
				}
				reader.close();
				return builder.toString();
			} catch (IOException e) {
				return "#notrail";
			}
		} else
			return "#notrail";
	}

	public static void recordTrail(Context context, String key, String value) {
		if (DEBUG_ENABLED) {
			File trail = new File(context.getFilesDir(), "trail.properties");
			Properties props = new Properties();
			try {
				FileReader fr = new FileReader(trail);
				props.load(fr);
				fr.close();
			} catch (IOException e) {
				// ignore
			}
	
			try {
				FileWriter writer = new FileWriter(trail);
				props.put(key, value);
				props.store(writer, "arirangchat debug trail file");
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static String getTrail(Context context, String key) {
		if (DEBUG_ENABLED) {
			File trail = new File(context.getFilesDir(), "trail.properties");
			Properties props = new Properties();
			try {
				FileReader fr = new FileReader(trail);
				props.load(fr);
				fr.close();
				return props.getProperty(key);
			} catch (IOException e) {
				return null;
			}
		} else
			return null;
		
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	public static void onConnectionStart() {
		if (DEBUG_ENABLED) {
			if (android.os.Build.VERSION.SDK_INT > 9) {
				/*StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
						.detectAll().penaltyLog().build());*/
			}
		}
	}

	public static void onAppStart() {
		onConnectionStart();
	}

	public static void onServiceStart() {
		if (DEBUGGER_ATTACH_ENABLED)
			android.os.Debug.waitForDebugger();
	}

	public static void onHeartbeat() {
		if (DEBUG_ENABLED)
			System.gc();
	}

	static public void wrapExceptions(Runnable runnable) {
		if (DEBUG_ENABLED) {
			try {
				runnable.run();
			} catch (Throwable t) {
				StringBuilderWriter writer = new StringBuilderWriter();
				PrintWriter pw = new PrintWriter(writer, true);
				t.printStackTrace(pw);
				writer.flush();
				String err = null;
				
				try{
					err =  writer.getBuilder().toString();
				}catch(Exception e){}
				
				if ( err == null )
					err = "writer.getbuilder().tostring() is null";
				
				
				throw new IllegalStateException("service throwable: "
						+ err);
			}
		} else
		{
			try {
				runnable.run();
			} catch (Throwable t) {
				String err = null ;
				try{
					err = t.getLocalizedMessage();
				}catch(Exception e){}
				
				if ( err == null )
					err = "t.getLocalizedMessage() is null ";
				
				/*throw new IllegalStateException("service throwable: "
						+ err );*/
			}
		}
	}
}
