package com.multimediachat.global;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.util.EncDecDes;
import com.multimediachat.util.qrcode.BarcodeFormat;
import com.multimediachat.util.qrcode.EncodeHintType;
import com.multimediachat.util.qrcode.common.BitMatrix;
import com.multimediachat.util.qrcode.qrcode.QRCodeWriter;
import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.plugin.xmpp.XmppAddress;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.service.ChatManager;
import com.multimediachat.ui.ChatRoomActivity;
import com.multimediachat.ui.FriendProfileChattingActivity;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.ui.dialog.CustomDialog;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.ChatRoomMediaUtil;
import com.multimediachat.util.ImageLoaderUtil;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.SystemServices;
import com.multimediachat.util.datamodel.FriendItem;
import com.multimediachat.util.datamodel.ProgressValue;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static android.content.Intent.ACTION_MAIN;
import static com.multimediachat.ui.views.FriendListFilterView.CONTACT_PROJECTION;
import static java.lang.Thread.sleep;


public class GlobalFunc {
    public static MainProgress progressDlg;
    private static boolean isCompressSuccess;
    private static String AVATAR_PATH;

    public native static String getDeviceNum(String strKey);

    static {
        System.loadLibrary("ici");
    }

    public static boolean isEnglishChar(char letter) {
        return (letter >= 'A' && letter <= 'Z') || (letter >= 'a' && letter <= 'z');

    }

    public static long getCurrentUTCTime() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        return (now);
    }

    public static long convertUTCToLocalTime(long utcTime) {
        int gmtOffset = TimeZone.getDefault().getRawOffset();
        long time = utcTime + gmtOffset;
        return time;
    }

    public static long convertLocalToUTCTime(long localTime) {
        int gmtOffset = TimeZone.getDefault().getRawOffset();
        long time = localTime - gmtOffset;
        return time;
    }

    public static String getRealPathFromURI(Context mContext, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    String path = cursor.getString(column_index);
                    return path;
                }
            }
        } catch (Exception e) {
            DebugConfig.error("GlobalFunc", "getRealPathFromURI", e);
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        return contentUri.getPath();
    }

    public static String getContactName(Context context, String number) {
        String name = null;

        // define the columns I want the query to return
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        // query time
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst())
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return name;
    }

    public static void updateBadgeCount(Context context, int badgeCount) {
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", badgeCount);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        String launcherClassName = GlobalFunc.getLauncherClassName(context);
        if (launcherClassName != null) {
            intent.putExtra("badge_count_class_name", launcherClassName);
            context.sendBroadcast(intent);
        }
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    public static void showErrorMessageToast(Context context, int errcode, boolean isLong) {

        int stringID = R.string.error_message_network_connect;

        /*switch (errcode) {
            case 1:
                stringID = R.string.error_1;
                break;
            case 2:
                stringID = R.string.error_2;
                break;
            case 3:
                stringID = R.string.error_3;
                break;
            case 4:
                stringID = R.string.error_4;
                break;
            case 5:
                stringID = R.string.error_5;
                break;
            case 6:
                stringID = R.string.error_6;
                break;
            case 7:
                stringID = R.string.error_7;
                break;
            case 8:
                stringID = R.string.error_8;
                break;
            case 9:
                stringID = R.string.error_9;
                break;
            case 10:
                stringID = R.string.error_10;
                break;
            case 11:
                stringID = R.string.error_11;
                break;
            case 12:
                stringID = R.string.error_12;
                break;
            case 13:
                stringID = R.string.error_13;
                break;
            case 14:
                stringID = R.string.error_14;
                break;
            case 15:
                stringID = R.string.error_15;
                break;
            case 16:
                stringID = R.string.error_16;
                break;
            case 17:
                stringID = R.string.error_17;
                break;
            case 100:
                stringID = R.string.error_100;
                break;
            case 101:
                stringID = R.string.error_101;
                break;
            case 102:
                stringID = R.string.error_102;
                break;
            case 104:
                stringID = R.string.error_104;
                break;
            case 105:
                stringID = R.string.error_105;
                break;
            case 200:
                stringID = R.string.error_message_network_connect;
                break;
        }*/

        Toast.makeText(context, stringID, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int stringID, boolean isLong) {
        // get your custom_toast.xml layout
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) ((Activity) context).findViewById(R.id.custom_toast_layout_id));

        // set a message
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(stringID);

        // Toast...
        ImApp.getInstance().mToast.setDuration(isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        ImApp.getInstance().mToast.setView(layout);
        ImApp.getInstance().mToast.show();
    }

    public static void showToast(Context context, String msg, boolean isLong) {
        // get your custom_toast.xml ayout
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) ((Activity) context).findViewById(R.id.custom_toast_layout_id));

        // set a message
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);

        // Toast...
        ImApp.getInstance().mToast.setDuration(isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        ImApp.getInstance().mToast.setView(layout);
        ImApp.getInstance().mToast.show();
    }

    public static void makeLocalDir() {
        File file = new File(GlobalConstrants.LOCAL_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(GlobalConstrants.LOCAL_PATH + GlobalConstrants.OTHER_DIR_NAME + "/" + GlobalConstrants.CAMERA_DIR_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(GlobalConstrants.LOCAL_PATH + GlobalConstrants.OTHER_DIR_NAME + "/" + GlobalConstrants.AVATAR_DIR_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void makeChatDir() {
        File file = new File(GlobalConstrants.LOCAL_PATH);

        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME + "/" + DatabaseUtils.mAccountID);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String[] mapToStringArray(Map<String, String> inParams) {
        String[] results = null;
        if (!inParams.isEmpty()) {
            results = new String[inParams.size() * 2];
            int i = 0;
            for (String key : inParams.keySet()) {
                results[i++] = key;
                results[i++] = inParams.get(key);
            }
        }
        return results;
    }

    public static Map<String, String> stringArrayToMap(String[] inParams) {
        Map<String, String> results = new HashMap<String, String>();
        if (inParams != null && inParams.length > 0) {
            int i = 0;
            while (i < inParams.length) {
                results.put(inParams[i], inParams[i + 1]);
                i += 2;
            }
        }
        return results;
    }

    public static Bitmap get_ninepatch(Drawable drawable, int x, int y, Resources resource) {
        drawable.setBounds(0, 0, x, y);
        Bitmap output_bitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output_bitmap);
        drawable.draw(canvas);
        return output_bitmap;
    }

    public static void doLogOut(final Context context) {

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showErrorMessageToast(context, 200, false);
            return;
        }

        if (GlobalFunc.hasCalling()) {
            GlobalFunc.showToast(context, R.string.disable_logout_in_calling, false);
            return;
        }

        final CustomDialog dlg = new CustomDialog(context, context.getString(R.string.logout), context.getString(R.string.logout_confirm),
                context.getString(R.string.out), context.getString(R.string.cancel), false);

        dlg.setOnCancelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        dlg.setOnOKClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        try {
                            progressDlg = new MainProgress(context);
                            progressDlg.setCanceledOnTouchOutside(false);
                            progressDlg.setMessage(context.getString(R.string.logout_message_wait_for_logging_out));
                            progressDlg.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            //stop service
                            ImApp.getInstance().stopService();
                            ImApp.getInstance().onTerminate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//						mApp.clearApplicationData();
//						mApp.truncateDatabase();

                        Uri mAccountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, Long.parseLong(GlobalVariable.account_id));
                        ContentValues values = new ContentValues();
                        values.put(Imps.AccountColumns.KEEP_SIGNED_IN, 0);
                        context.getContentResolver().update(mAccountUri, values, null, null);

                        mPref.putLong(GlobalConstrants.store_picaProviderId, -1);
                        mPref.putLong(GlobalConstrants.store_picaAccountId, -1);
                        mPref.putInt(GlobalConstrants.store_step, 0);
                        GlobalVariable.account_id = null;

                        // mPref.putString("username", "");
                        // mPref.putString("apikey", "");

                        while (LinphonePreferences.instance().getAccountCount() > 0)
                            LinphonePreferences.instance().deleteAccount(0);

                        context.stopService(new Intent(ACTION_MAIN).setClass(context, LinphoneService.class));

                        updateBadgeCount(context, 0);

                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        if (progressDlg != null) {
                            progressDlg.dismiss();
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.appNeedFinish, 0);
                        ImApp.getInstance().restartAppSoon(context);
                    }
                }.execute();
            }
        });

        dlg.setCanceledOnTouchOutside(false);
        dlg.show();

    }

    public static void SetSipConnectErrPref(int ErrCode, String ErrString) {
        mPref.putInt(GlobalConstrants.KEY_TOTAL_CONNECT_ERRCODE, ErrCode);
        mPref.putString(GlobalConstrants.KEY_TOTAL_CONNECT_ERRMSG, ErrString);

        mPref.putInt(GlobalConstrants.KEY_SIP_CONNECT_ERRCODE, ErrCode);
        mPref.putString(GlobalConstrants.KEY_SIP_CONNECT_ERRMSG, ErrString);
    }

    public static void SetSipConnectStatPref(Context context, int ConnStatus) {
        mPref.putInt(GlobalConstrants.KEY_TOTAL_CONNECT_STATUS, ConnStatus);
        mPref.putInt(GlobalConstrants.KEY_SIP_CONNECT_STATUS, ConnStatus);

        Intent connStatChangeIntent = new Intent(GlobalConstrants.INTENT_CONNSTAT_CHANGED);
        context.sendBroadcast(connStatChangeIntent);
    }

    public static void SetXmppConnectErrPref(int ErrCode, String ErrString) {
        mPref.putInt(GlobalConstrants.KEY_XMPP_CONNECT_ERRCODE, ErrCode);
        mPref.putString(GlobalConstrants.KEY_XMPP_CONNECT_ERRMSG, ErrString);
    }

    public static void SetXmppConnectStatPref(Context context, int ConnStatus) {
        mPref.putInt(GlobalConstrants.KEY_XMPP_CONNECT_STATUS, ConnStatus);
        Intent mobilecomConnStatChangeIntent = new Intent(GlobalConstrants.INTENT_CONNSTAT_CHANGED);
        context.sendBroadcast(mobilecomConnStatChangeIntent);
        if (ConnStatus == ConnectionState.eCONNSTAT_XMPP_CONNECTED) {
            GlobalFunc.updateDBDownloadFailed2Downloading(context);
            context.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
        }
    }

    public static String getConnErrMsg() {
        return mPref.getString(GlobalConstrants.KEY_TOTAL_CONNECT_ERRMSG, "");
    }

    public static int getXmppConnStatus() {
        return mPref.getInt(GlobalConstrants.KEY_XMPP_CONNECT_STATUS, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED);
    }

    public static int getSipConnStatus() {
        return mPref.getInt(GlobalConstrants.KEY_SIP_CONNECT_STATUS, ConnectionState.eCONNSTAT_SIP_DISCONNECTED);
    }

    public static void showFriendProfile(Context mContext, FriendItem item) {
        Intent intent = new Intent(mContext, FriendProfileChattingActivity.class);
        intent.putExtra("mFlag", false);
        Contact contact = new Contact(new XmppAddress(item.userName), item.nickName);

        Cursor cursor = null;
        String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
        String[] selectionArgs = {GlobalVariable.account_id, item.userName};
        cursor = mContext.getContentResolver().query(Imps.Contacts.CONTENT_URI, CONTACT_PROJECTION,
                select, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            String tmp = null;

            String status, gender, region, phone;

            status = cursor.getString(cursor.getColumnIndex(Imps.Contacts.STATUSMESSAGE));
            gender = cursor.getString(cursor.getColumnIndex(Imps.Contacts.GENDER));
            region = cursor.getString(cursor.getColumnIndex(Imps.Contacts.REGION));
            phone = cursor.getString(cursor.getColumnIndex(Imps.Contacts.PHONE_NUMBER));

            contact.setProfile(status, gender, region);
            contact.setPhoneNum(phone);
            contact.favor = cursor.getInt(cursor.getColumnIndex(Imps.Contacts.FAVORITE));
        }

        if (cursor != null)
            cursor.close();

        contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_BOTH;
        FriendProfileChattingActivity.contact = contact;
        ((Activity) mContext).startActivityForResult(intent, MainTabNavigationActivity.REQUEST_SELECT_CHAT_LIST);
    }

    //////////////    get Contacts's username from [inmemorymessages]         ///////////////
    public static String getUserNameFromThreadID(ContentResolver cr, int thread_id) {
        String userName = "";
        Uri conUri = Uri.withAppendedPath(Imps.Contacts.CONTENT_URI, String.valueOf(thread_id));
        Cursor contactsCur = cr.query(conUri, null, null, null, Imps.Contacts.DEFAULT_SORT_ORDER);
        if (contactsCur != null) {
            if (contactsCur.moveToFirst())
                userName = contactsCur.getString(contactsCur.getColumnIndex(Imps.Contacts.USERNAME));
            contactsCur.close();
        }
        return userName;
    }

    public static void compressImage(final int chatId, String photoPath) {
        final ArrayList<String> photos = new ArrayList<String>();
        final String filePath = GlobalConstrants.CAMERA_TEMP_PATH + System.currentTimeMillis() + ".jpg";
        boolean isCompressSuccess = ImageLoaderUtil.compressImage(photoPath, filePath);
        if (isCompressSuccess) {
            new File(photoPath).delete();
            photos.add(filePath);
        } else {
            photos.add(photoPath);
        }
        if (photos.size() > 0) {
            AndroidUtility.RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < photos.size(); i++) {
                        addUploadList(chatId, Uri.parse(photos.get(i)), "image/1", "");
                    }
                }
            });
        }
    }

    public static void compressVideo(final long chatSessionId, final String photoPath) {
        final String offerId = Packet.nextID();
        final String filePath = GlobalConstrants.CAMERA_TEMP_PATH + System.currentTimeMillis() + ".mp4";
        final ContentResolver cr = ImApp.applicationContext.getContentResolver();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    Imps.insertMessageInDb(cr, false, chatSessionId, true, null, photoPath, GlobalFunc.getCurrentUTCTime(),
                            Imps.MessageType.OUTGOING, Imps.FileErrorCode.COMPRESSING, offerId, "video/3");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                publishProgress();

                try {
                    isCompressSuccess = com.yovenny.videocompress.MediaController.getInstance().convertVideo(photoPath, filePath);

                    if (isCompressSuccess)
                        new File(photoPath).delete();

                    return isCompressSuccess ? filePath : null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                final ArrayList<String> photos = new ArrayList<String>();
                photos.add(isCompressSuccess ? filePath : photoPath);
                Imps.deleteMessage(ImApp.applicationContext, cr, offerId);
                if (photos.size() > 0) {
                    AndroidUtility.RunOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < photos.size(); i++) {
                                addUploadList((int) chatSessionId, Uri.parse(photos.get(i)), "video/3", "");
                            }
                        }
                    });
                }
            }
        }.execute();
    }

    public static int getUploadingStatus() {
        return mPref.getInt(GlobalConstrants.KEY_UPLOADING_STATUS, GlobalConstrants.NO_UPLOADING);
    }

    public static void setUploadingStatus(int status) {
        mPref.putInt(GlobalConstrants.KEY_UPLOADING_STATUS, status);
    }

    public static int getDownloadingStatus() {
        return mPref.getInt(GlobalConstrants.KEY_DOWNLOADING_STATUS, GlobalConstrants.NO_DOWNLOADING);
    }

    public static void setDownloadingStatus(int status) {
        mPref.putInt(GlobalConstrants.KEY_DOWNLOADING_STATUS, status);
    }

    public static String hasUploading(Context context) {
        return "";
        /*String packetId = "";
        Cursor cursor = context.getContentResolver().query(Imps.Messages.CONTENT_URI, new String[]{Imps.Messages._ID, Imps.Messages.PACKET_ID}, Imps.Messages.ERROR_CODE + "=?",
                new String[]{String.valueOf(Imps.FileErrorCode.UPLOADING)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                packetId = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
            cursor.close();
        }
        return packetId;*/
    }

    public static String hasDownloading(Context context) {
        return "";
        /*String packetId = "";
        Cursor cursor = context.getContentResolver().query(Imps.Messages.CONTENT_URI, new String[]{Imps.Messages._ID, Imps.Messages.PACKET_ID}, Imps.Messages.ERROR_CODE + "=?",
                new String[]{String.valueOf(Imps.FileErrorCode.DOWNLOADING)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                packetId = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
            cursor.close();
        }
        return packetId;*/
    }

    public static boolean hasCalling() {
        return false;
        /*if (LinphoneManager.isInstanciated())
            return LinphoneManager.getLc().getCallsNb() > 0;
        return false;*/
    }

    public static void updateDBNoProgressUpDownloading(Context context) {
        Cursor cursor = context.getContentResolver().query(Imps.Messages.CONTENT_URI, new String[]{Imps.Messages.PACKET_ID}, Imps.Messages.ERROR_CODE + "=?",
                new String[]{String.valueOf(Imps.FileErrorCode.UPLOADING)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String packetId = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
                    ProgressValue pv = ImApp.getInstance().getUploadProgress(packetId);
                    if (pv == null) {
                        Imps.updateOperMessageError(context.getContentResolver(), packetId, Imps.FileErrorCode.UPLOADFAILED);
                        GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        cursor = context.getContentResolver().query(Imps.Messages.CONTENT_URI, new String[]{Imps.Messages.PACKET_ID}, Imps.Messages.ERROR_CODE + "=?",
                new String[]{String.valueOf(Imps.FileErrorCode.DOWNLOADING)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String packetId = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
                    ProgressValue pv = ImApp.getInstance().getDownloadProgress(packetId);
                    if (pv == null) {
                        Imps.updateOperMessageError(context.getContentResolver(), packetId, Imps.FileErrorCode.DOWNLOADFAILED);
                        GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public static void updateDBDownloadFailed2Downloading(Context context) {
        Cursor cursor = context.getContentResolver().query(Imps.Messages.CONTENT_URI, new String[]{Imps.Messages.PACKET_ID}, Imps.Messages.ERROR_CODE + "=?",
                new String[]{String.valueOf(Imps.FileErrorCode.DOWNLOADFAILED)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String packetId = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
                    Imps.updateOperMessageError(context.getContentResolver(), packetId, Imps.FileErrorCode.DOWNLOADING);
                    Imps.updateDeliveryStateInDb(context.getContentResolver(), packetId, 0);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public static void addUploadList(int mChatID, final Uri uri, final String mimeType, final String receiverAddress) {

        String offerId = Packet.nextID();
        try {
            SystemServices.FileInfo info = null;
            try {
                info = SystemServices.getFileInfoFromURI(ImApp.applicationContext, uri);
            } catch (Exception e) {
                e.printStackTrace();
                info = new SystemServices.FileInfo();
                info.type = mimeType;
            }
            if (info.type == null)
                if (mimeType != null)
                    info.type = mimeType;
                else
                    info.type = "application/octet-stream";

            String filePath = GlobalFunc.getRealPathFromURI(ImApp.applicationContext, uri);

            if (filePath != null && ((uri.toString().startsWith("http://") || uri.toString().startsWith("https://")
                    || uri.toString().startsWith("content://com.")))) {
                filePath = null;
            }

            if (filePath == null) {
                return;
            }

            File fileSend = new File(filePath);
            if (!fileSend.exists()) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.error_message_failed_send_file, true);
                return;
            }

            if (ChatRoomActivity.instance() != null)
                ChatRoomActivity.instance().insertChatNotExist(mChatID);

            int totalCount = 0;
            totalCount = ((int) fileSend.length() / GlobalConstrants.FILE_SPLIT_SIZE);
            if (fileSend.length() > (totalCount * GlobalConstrants.FILE_SPLIT_SIZE))
                totalCount++;

            Imps.insertMessageInDb(ImApp.applicationContext.getContentResolver(), false, mChatID, true, null, filePath, GlobalFunc.getCurrentUTCTime(),
                    Imps.MessageType.POSTPONED, 0, offerId, info.type, totalCount);

            if (ChatRoomActivity.instance() != null) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                ChatRoomActivity.instance().mChatView.setFocusHistoryView(500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (GlobalFunc.getUploadingStatus() == GlobalConstrants.NO_UPLOADING) {
            ImApp.getInstance().sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED));
        }
    }

    public static void handleUpload(final Context context, final int chatID, final Uri uri, final String splitPath, final int fileType, final String mimeType,
                                    final String receiverAddress, final String offerId, final int sendCount, final int totalCount) {
        GlobalFunc.setUploadingStatus(GlobalConstrants.UPLOADING);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (Imps.getErrorByPacketId(offerId) == Imps.FileErrorCode.UPLOADCANCELLED) {    // if upload canceled while splitting file
                    GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                    Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                    context.sendBroadcast(intent);
                    return;
                }

                if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
                    Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                    if (ChatRoomActivity.instance() != null) {
                        GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.network_error, false);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                    }
                    /*GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                    Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                    context.sendBroadcast(intent);*/
                    return;
                }

                IImConnection conn = ImApp.getInstance().getConnection(mPref.getLong(GlobalConstrants.store_picaProviderId, -1));

                if (conn == null) {
                    Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                    if (ChatRoomActivity.instance() != null) {
                        GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.cant_connect_to_server, false);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                    }
                    /*GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                    Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                    context.sendBroadcast(intent);*/
                    return;
                }

                try {
                    SystemServices.FileInfo info = null;
                    try {
                        info = SystemServices.getFileInfoFromURI(context, uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        info = new SystemServices.FileInfo();
                        info.type = mimeType;
                    }
                    if (info.type == null)
                        if (mimeType != null)
                            info.type = mimeType;
                        else
                            info.type = "application/octet-stream";

                    String filePath = GlobalFunc.getRealPathFromURI(context, uri);

                    if (filePath != null && (filePath.startsWith("http://") || filePath.startsWith("https://")
                            || filePath.startsWith("content://com."))) {
                        filePath = null;
                    }

                    if (!isStorageWrittable()) {
                        Imps.updateMessageTypeInDb(context.getContentResolver(), offerId, Imps.MessageType.OUTGOING);
                        Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                        if (ChatRoomActivity.instance() != null) {
                            GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.cannot_use_storage, false);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                        }
                        GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                        Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                        context.sendBroadcast(intent);
                        return;
                    }

                    if (filePath == null) {
                        Imps.deleteMessage(context, context.getContentResolver(), offerId);
                        if (ChatRoomActivity.instance() != null) {
                            GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.error_message_failed_send_file, false);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                        }
                        GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                        Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                        context.sendBroadcast(intent);
                        return;
                    }

                    File fileSend = new File(filePath);
                    if (!fileSend.exists()) {
                        Imps.deleteMessage(context, context.getContentResolver(), offerId);
                        if (ChatRoomActivity.instance() != null) {
                            GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.error_message_failed_send_file, false);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                        }
                        GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                        Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                        context.sendBroadcast(intent);
                        return;
                    }

                    GlobalFunc.makeChatDir();
                    String chatPath = GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME + "/" + DatabaseUtils.mAccountID + "/" + chatID + "/";
                    File fileChat = new File(chatPath);
                    if (!fileChat.exists())
                        fileChat.mkdirs();

                    int[] ret = null;
                    String samplePath = null;
                    boolean needSampleImage = true;

                    samplePath = Imps.getMessageSampleImagePath(context.getContentResolver(), offerId);
                    if (samplePath != null) {
                        File sampleImage = new File(samplePath);
                        if (sampleImage.exists()) {
                            needSampleImage = false;
                        }
                    }

                    if (needSampleImage) {
                        if (info.type.startsWith("image")) {
                            try {
                                samplePath = chatPath + "thumbnail_" + System.currentTimeMillis() + ".jpg";
                                ret = ChatRoomMediaUtil.sampleImage(context.getResources(), filePath, samplePath);
                                if (ret == null) {
                                    Imps.updateMessageTypeInDb(context.getContentResolver(), offerId, Imps.MessageType.OUTGOING);
                                    Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                                    if (ChatRoomActivity.instance() != null) {
                                        GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.error_message_failed_send_file, false);
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                    }
                                    GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                                    Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                                    context.sendBroadcast(intent);
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Imps.updateMessageTypeInDb(context.getContentResolver(), offerId, Imps.MessageType.OUTGOING);
                                Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                                if (ChatRoomActivity.instance() != null) {
                                    GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.error_message_failed_send_file, false);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                }
                                GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                                Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                                context.sendBroadcast(intent);
                                return;
                            }
                        } else if (info.type.startsWith("video")) {
                            try {
                                samplePath = chatPath + "thumbnail_" + System.currentTimeMillis() + ".jpg";
                                ret = ChatRoomMediaUtil.sampleVideo(context.getResources(), filePath, samplePath);
                                if (ret == null) {
                                    Imps.updateMessageTypeInDb(context.getContentResolver(), offerId, Imps.MessageType.OUTGOING);
                                    Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                                    if (ChatRoomActivity.instance() != null) {
                                        GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.error_message_failed_send_file, false);
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                    }
                                    GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                                    Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                                    context.sendBroadcast(intent);
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Imps.updateMessageTypeInDb(context.getContentResolver(), offerId, Imps.MessageType.OUTGOING);
                                Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                                if (ChatRoomActivity.instance() != null) {
                                    GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.error_message_failed_send_file, false);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                }
                                GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                                Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                                context.sendBroadcast(intent);
                                return;
                            }
                        }
                    }

                    int xmppStatus = GlobalFunc.getXmppConnStatus();
                    if (xmppStatus >= ConnectionState.eCONNSTAT_XMPP_CONNECTED) {
                        if (ret != null) {
                            Imps.updateMessageInDb(context.getContentResolver(), offerId, samplePath, ret[0], ret[1]);
                        }

                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);

                        IChatSessionManager manager = conn.getChatSessionManager();
                        IChatSession session = manager.getChatSession(receiverAddress);

                        if (session == null) {
                            Imps.updateMessageTypeInDb(context.getContentResolver(), offerId, Imps.MessageType.OUTGOING);
                            Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                            GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                            Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                            context.sendBroadcast(intent);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                            return;
                        }

                        ChatManager.sendFile(mPref.getLong(GlobalConstrants.store_picaProviderId, -1), mPref.getLong(GlobalConstrants.store_picaAccountId, -1), receiverAddress, filePath, splitPath,
                                samplePath, fileType, offerId, sendCount, totalCount);
                    } else {
                        Imps.updateMessageTypeInDb(context.getContentResolver(), offerId, Imps.MessageType.OUTGOING);
                        Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                        if (ret != null) {
                            Imps.updateMessageInDb(context.getContentResolver(), offerId, samplePath, ret[0], ret[1]);
                        }
                        GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                        Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                        context.sendBroadcast(intent);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                    }
                } catch (Exception e) {
                    Imps.updateMessageTypeInDb(context.getContentResolver(), offerId, Imps.MessageType.OUTGOING);
                    Imps.updateOperMessageError(context.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
                    GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                    Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                    context.sendBroadcast(intent);
                }
            }
        });
    }

    public static void clearHistoryMessages(Context context, int contactId) {
        @SuppressWarnings("deprecation")
        Uri uri = Imps.Messages.getContentUriByThreadId(contactId);
        Cursor cursor = context.getContentResolver().query(uri, new String[]{Imps.Messages.BODY, Imps.Messages.MIME_TYPE, Imps.Messages.SAMPLE_IMAGE_PATH, Imps.Messages.TYPE, Imps.Messages.DATE},
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String body = cursor.getString(0);
                String mime_type = cursor.getString(1);
                String sample_path = cursor.getString(2);
                int type = cursor.getInt(3);

                if (mime_type != null && body != null && type == Imps.MessageType.INCOMING) {
                    Uri mediaUri = Uri.parse(body);
                    String filePath = null;
                    if (mediaUri != null && mediaUri.getScheme() != null) {

                        SystemServices.FileInfo info = null;
                        try {
                            info = SystemServices.getFileInfoFromURI(context, mediaUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (info != null && info.path != null)
                            filePath = info.path;
                        else
                            filePath = GlobalFunc.getRealPathFromURI(context, mediaUri);
                    } else
                        filePath = body;

                    File file = new File(filePath);
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                }

                if (sample_path != null) {
                    File file = new File(sample_path);
                    if (file != null && file.exists())
                        file.delete();
                }
            }
            cursor.close();
        }

        context.getContentResolver().delete(uri, null, null);
    }

    public static int getDeliveryState(String packetID) {
        int deliveryState = -1;
        Cursor cursor = ImApp.getInstance().getApplicationContext().getContentResolver().query(Imps.Messages.CONTENT_URI, new String[]{Imps.Messages.IS_DELIVERED},
                Imps.Messages.PACKET_ID + "=?",
                new String[]{String.valueOf(packetID)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                deliveryState = cursor.getInt(cursor.getColumnIndex(Imps.Messages.IS_DELIVERED));
            }
            cursor.close();
        }
        return deliveryState;
    }

    public static String makeSplitFile(String filePath, int sendCount) {
        File file = new File(filePath);
        if (file.length() < GlobalConstrants.FILE_SPLIT_SIZE)
            return filePath;

        File spDir = new File(GlobalConstrants.SPLIT_DIR_PATH);
        if (!spDir.exists()) {
            spDir.mkdirs();
        } else {
            File[] fs = spDir.listFiles();
            for (File f : fs)
                f.delete();
        }

        String newPath = GlobalConstrants.SPLIT_DIR_PATH + file.getName();

        File newFile = new File(newPath);
        if (newFile.exists())
            newFile.delete();

        if (!GlobalFunc.isStorageWrittable()) {
            return null;
        }

        try {
            InputStream is = new FileInputStream(filePath);
            FileOutputStream fos = new FileOutputStream(newFile);
            is.skip(GlobalConstrants.FILE_SPLIT_SIZE * sendCount);

            byte[] buffer = new byte[16 * 1024];    //16KB
            int read;
            int total = 0;

            while (GlobalFunc.isStorageWrittable() && ((read = is.read(buffer, 0, min(16 * 1024, GlobalConstrants.FILE_SPLIT_SIZE - total))) != -1)) {
                total += read;
                Thread.sleep(GlobalConstrants.FILE_SPLIT_SLEEP);
                fos.write(buffer, 0, read);
                Thread.sleep(GlobalConstrants.FILE_SPLIT_SLEEP);
                if (total >= GlobalConstrants.FILE_SPLIT_SIZE)
                    break;
            }

            if (!GlobalFunc.isStorageWrittable()) {
                return null;
            }

            int null_bytes = (32 - total % 32) % 32;
            if (null_bytes > 0) {
                for (int i = 0; i < null_bytes; i++)
                    buffer[i] = 0;
                fos.write(buffer, 0, null_bytes);
            }

            fos.flush();
            fos.close();
            is.close();

        } catch (Exception e) {
            return null;
        }

        return newPath;
    }

    static int min(int a, int b) {
        return a > b ? b : a;
    }

    public static void doSipLogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!LinphoneManager.isInstanciated()) {
                    try {
                        sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ImApp.getInstance().initSipLogin();
            }
        }).start();
    }

    public static int getContactId(Context context, String username) {
        Cursor cursor = context.getContentResolver().query(Imps.Contacts.CONTENT_URI, new String[]{Imps.Contacts._ID}, Imps.Contacts.USERNAME + "=?",
                new String[]{username}, Imps.Contacts.DEFAULT_SORT_ORDER);
        int res = -1;
        if (cursor != null) {
            if (cursor.moveToFirst())
                res = cursor.getInt(0);
            cursor.close();
        }
        return res;
    }

    public static boolean isStorageWrittable() {
        File localDir = new File(GlobalConstrants.LOCAL_PATH);
        if (!localDir.exists()) {
            localDir.mkdirs();
        }
        return localDir.canWrite();
    }

    public static boolean checkStorageFreeSpace(Context context) {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();

        if (bytesAvailable < GlobalConstrants.MINIMUM_STORAGE_FREE_SPACE) {
            GlobalFunc.showToast(context, context.getString(R.string.error_message_low_storage), true);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isAlreadyReceivedFriendRequest(ContentResolver resolver, String userName) {
        String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.SUBSCRIPTION_TYPE + "=? AND " + Imps.Contacts.USERNAME + "=?";
        String[] selectionArgs = {GlobalVariable.account_id, String.valueOf(Imps.Contacts.SUBSCRIPTION_TYPE_FROM), userName};
        Cursor cursor = null;

        try {
            cursor = resolver.query(Imps.Contacts.CONTENT_URI, null, select, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    public static void showAlertDialog(Context context, String title, String content) {
        final Dialog dlg = GlobalFunc.createDialog(context, R.layout.msgdialog, true);

        TextView titleTextView = (TextView) dlg.findViewById(R.id.msgtitle);
        titleTextView.setText(title);

        TextView contentTextView = (TextView) dlg.findViewById(R.id.msgcontent);

        contentTextView.setText(content);

        Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
        dlg_btn_ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dlg.setCanceledOnTouchOutside(false);

        dlg.show();
    }

    /*
     * added by JHK(2019.11.07)
     * set profile image to ImageView for avatar
     */
    public static void setProfileImage(CircularImageView view, Context context, String userAddr) {
        showAvatar(context, userAddr, view);
        /*if (!view.equals(null) && !context.equals(null)) {
            String samplePath = Imps.getAvatarPath(context.getContentResolver(), userAddr);
            if (!samplePath.equals(null) && !samplePath.isEmpty()) {
                File avatar = new File(samplePath);
                if (!avatar.exists()) {
                    samplePath = null;
                    Imps.updateContactsInDb(context.getContentResolver(), userAddr, "");
                }
            }
            Glide.with(context).setDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).
                    format(DecodeFormat.PREFER_RGB_565).placeholder(R.drawable.profilephoto).error(R.drawable.profilephoto)).load(samplePath).into(view);
        }*/
    }

    public static String parseName(String var0) {
        if (var0 == null) {
            return null;
        } else {
            int var1 = var0.lastIndexOf("@");
            return var1 <= 0 ? var0 : var0.substring(0, var1);
        }
    }

    public static void showAvatar (Context context, String address, CircularImageView imgView) {
        if (address == null) {
            return;
        }
        if ( address.equals("me") )
            ImageLoaderUtil.loadImageUrl(context, Uri.fromFile(new File(GlobalConstrants.USER_PHOTO_PATH)).toString(), imgView);
        else
            ImageLoaderUtil.loadAvatarImage(context,address, imgView, 2);
    }

    public static void showQRCode(String address, ImageView imgView) {
        if (null != address && !address.equals("")) {
            try {
                QRCodeWriter writer = new QRCodeWriter();
                Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
                hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
                BitMatrix bitMatrix = writer.encode(address, BarcodeFormat.QR_CODE, 300, 300, hints);
                int[] pixels = new int[300 * 300];
                for (int y = 0; y < 300; y++) {
                    for (int x = 0; x < 300; x++) {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * 300 + x] = 0xff000000;
                        } else {
                            pixels[y * 300 + x] = 0xffffffff;
                        }

                    }
                }
                Bitmap bmMyQRCode = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
                bmMyQRCode.setPixels(pixels, 0, 300, 0, 0, 300, 300);
                imgView.setImageBitmap(bmMyQRCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Dialog createDialog(Context context, int resId, boolean isBottom) {
        Dialog dlg = new Dialog(context);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(resId);
        dlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Window window = dlg.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (isBottom)
            wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        return dlg;
    }

    public static void removeFriendAvatarFile(String address, boolean fromService) {
        String name = StringUtils.parseName(address);
        if (fromService) {
            for (int i = 0; i < 3; i++) {
                String strType = "small";
                if (i == 1)
                    strType = "medium";
                else if (i == 2)
                    strType = "big";

                String normalName = name + "_" + strType;
                String encname = EncDecDes.getInstance().generateFileName(normalName);
                File file = new File(GlobalConstrants.AVATAR_DIR_PATH + encname);
                if (file.exists()) {
                    file.delete();
                }
            }
        } else {

        }
    }
}
