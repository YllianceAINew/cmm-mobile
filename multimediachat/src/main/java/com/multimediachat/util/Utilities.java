package com.multimediachat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import com.multimediachat.R;

public class Utilities {

	public static boolean copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (Exception e) {
			return false;
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
		return true;
	}

	public static String MD5(String md5) {
		if (md5 == null) {
			return null;
		}
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte anArray : array) {
				sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	public static String formatFileSize(long size) {
		if (size < 1024) {
			return String.format("%d B", size);
		} else if (size < 1024 * 1024) {
			return String.format("%.1f KB", size / 1024.0f);
		} else if (size < 1024 * 1024 * 1024) {
			return String.format("%.1f MB", size / 1024.0f / 1024.0f);
		} else {
			return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
		}
	}

	public static int dpToPx(Context context, int dp) {
		float scale = context.getResources().getDisplayMetrics().density;
	    return((int) (dp * scale + 0.5f));
	}
	
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay(); 
		return display.getWidth();
	}

	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		return display.getHeight();
	}
}
