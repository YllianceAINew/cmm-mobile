package com.multimediachat.app;


import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static long getCreationDate(String filePath) {
        File file = new File(filePath);
        return file.lastModified();
    }

    public static boolean isValidVideoFile(String filePath, MediaMetadataRetriever retriever)
    {
        try {
            retriever.setDataSource(filePath);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static String getDurationMark(String filePath, MediaMetadataRetriever retriever) {
        try {
            retriever.setDataSource(filePath);
        } catch (Exception e) {
            return "?:??";
        }
        String time = null;

        //fix for the gallery picker crash
        // if it couldn't detect the media file
        try {
            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //fix for the gallery picker crash
        // if it couldn't extractMetadata() of a media file
        //time was null
        time = time == null ? "0" : time.isEmpty() ? "0" : time;
        //bam crash - no more :)
        int timeInMillis = Integer.parseInt(time);
        int duration = timeInMillis / 1000;
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(":");
        }
        if (minutes < 10) {
            sb.append("0").append(minutes);
        } else {
            sb.append(minutes);
        }
        sb.append(":");
        if (seconds < 10) {
            sb.append("0").append(seconds);
        } else {
            sb.append(seconds);
        }
        return sb.toString();
    }

    public static String getPhoneNumber(){
        String strImei = getIMEI();
        return "191"+strImei.substring(strImei.length()-7);
//        return "1916175847";
        /*TelephonyManager mTelMgr = (TelephonyManager) ImApp.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        Log.e("ICICI", mTelMgr.getLine1Number());
        return mTelMgr.getLine1Number();*/
    }

    public static String getIMSI() {
        return getIMEI();
//        return "123456789012345";
        /*final TelephonyManager tele =(TelephonyManager) ImApp.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        final String actualSubscriberId = tele.getSubscriberId();
        Log.e("ICICI", SystemProperties.get("test.subscriberid", actualSubscriberId));
        return SystemProperties.get("test.subscriberid", actualSubscriberId);*/
    }

    public static String getIMEI() {
        TelephonyManager tm = (TelephonyManager) ImApp.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        // get IMEI
        String m_imei = tm.getDeviceId();
        String androidId = Settings.Secure.getString(ImApp.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(m_imei)) {
            m_imei = androidId;
        }
        return m_imei;
    }

    public static boolean isValidChatID(String str) {
        Pattern pattern = Pattern.compile("[a-zA-Z]{1,1}" +  "[a-zA-Z0-9\\_]{5,31}" );
        boolean a = pattern.matcher(str).matches();
        return !TextUtils.isEmpty(str)&&pattern.matcher(str).matches();
    }
}
