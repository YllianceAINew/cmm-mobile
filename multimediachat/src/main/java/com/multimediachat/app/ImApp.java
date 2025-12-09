/*
 * Copyright (C) 2007-2008 Esmertec AG. Copyright (C) 2007-2008 The Android Open
 * Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.multimediachat.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.multimediachat.R;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IConnectionCreationListener;
import com.multimediachat.app.im.IContactList;
import com.multimediachat.app.im.IContactListListener;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.IInvitationListener;
import com.multimediachat.app.im.IRemoteImService;
import com.multimediachat.app.im.app.adapter.ConnectionListenerAdapter;
import com.multimediachat.app.im.engine.Address;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ContactListListener;
import com.multimediachat.app.im.engine.ImConnection;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.ConnectionState;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.service.ChatSessionAdapter;
import com.multimediachat.service.FtpService;
import com.multimediachat.service.IPlaybackService;
import com.multimediachat.service.ImServiceConstants;
import com.multimediachat.service.StatusBarNotifier;
import com.multimediachat.ui.CallingActivity;
import com.multimediachat.ui.ChatRoomActivity;
import com.multimediachat.ui.IncomingActivity;
import com.multimediachat.ui.LoginActivity;
import com.multimediachat.ui.MainActivity;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.ui.OutgoingActivity;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;
import com.multimediachat.util.datamodel.ProgressValue;

import org.apache.http.Header;
import org.jivesoftware.smack.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.ContactsManager;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PayloadType;
import org.linphone.core.Reason;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ImApp extends MultiDexApplication {

    public static final String TAG = "GB.ImApp";
    public static final String LOG_TAG = "GB.ImApp";

    public static final String IMPS_CATEGORY = "com.multimediachat.app.im.IMPS_CATEGORY";

    // ACCOUNT SETTINGS Imps defaults
    public static final String DEFAULT_XMPP_RESOURCE = "PicaTalk";
    public static final int DEFAULT_XMPP_PRIORITY = 20;
    public static final String DEFAULT_XMPP_OTR_MODE = "auto";

    private Locale locale = null;

    IRemoteImService mImService;

    public static HashMap<Long, IImConnection> mConnections;
    MyConnListener mConnectionListener;
    HashMap<Long, ProviderDef> mProviders;

    Map<String, ProgressValue> mapUploadProgress;
    Map<String, ProgressValue> mapDownloadProgress;
    Map<String, String> mapDownloadThumbnails = new HashMap<String, String>();

    Broadcaster mBroadcaster;

    /**
     * A queue of messages that are waiting to be sent when service is
     * connected.
     */
    ArrayList<Message> mQueue = new ArrayList<Message>();

    /**
     * A flag indicates that we have called tomServiceStarted start the service.
     */
    // private boolean mServiceStarted;
    private Context mApplicationContext;

    public static final int EVENT_SERVICE_CONNECTED = 100;
    public static final int EVENT_CONNECTION_CREATED = 150;
    public static final int EVENT_CONNECTION_LOGGING_IN = 200;
    public static final int EVENT_CONNECTION_LOGGED_IN = 201;
    public static final int EVENT_CONNECTION_LOGGING_OUT = 202;
    public static final int EVENT_CONNECTION_DISCONNECTED = 203;
    public static final int EVENT_CONNECTION_SUSPENDED = 204;
    public static final int EVENT_USER_PRESENCE_UPDATED = 300;
    public static final int EVENT_UPDATE_USER_PRESENCE_ERROR = 301;

    private static final String[] PROVIDER_PROJECTION = {Imps.Provider._ID, Imps.Provider.NAME, Imps.Provider.FULLNAME,
            Imps.Provider.SIGNUP_URL,};

    private static final String[] ACCOUNT_PROJECTION = {Imps.Account._ID, Imps.Account.PROVIDER, Imps.Account.NAME,
            Imps.Account.USERNAME, Imps.Account.PASSWORD,};

    private IPlaybackService myPlaybackService = null;
    private Intent serviceIntent = null;
    private ServiceConnection serviceConnection = null;

    private static ImApp mInstance;

    public static volatile Context applicationContext = null;
    public static volatile Handler applicationHandler = null;
    public static volatile boolean applicationUIInited = false;

    private IntentFilter intentFilter = new IntentFilter();

    public static String INFO_IMEI = "";
    public boolean useEvent = false;
    public String userName = null;

    public long last_notices_id = 0;
    public String api_key = null;

    public static volatile boolean imageLoaderInited = false;

    public boolean isRinging = false;

    public Toast mToast;

    @Override
    public ContentResolver getContentResolver() {
        if (mApplicationContext == this) {
            return super.getContentResolver();
        }

        return mApplicationContext.getContentResolver();
    }

    public ImApp() {
        super();
        mApplicationContext = this;
        mInstance = this;
    }

    public ImApp(Context context) {
        super();
        mApplicationContext = context;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (locale != null) {
            Configuration myConfig = new Configuration(newConfig);
            myConfig.locale = locale;
            Locale.setDefault(locale);
            getResources().updateConfiguration(myConfig, getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void onCreate(Bundle arguments) {
        MultiDex.install(getBaseContext());
        super.onCreate();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = getApplicationContext();
        applicationHandler = new Handler(getMainLooper());
        mBroadcaster = new Broadcaster();
        mConnections = new HashMap<Long, IImConnection>();
        mapUploadProgress = new HashMap<String, ProgressValue>();
        mapDownloadProgress = new HashMap<String, ProgressValue>();
        mapDownloadThumbnails = new HashMap<String, String>();
        mToast = new Toast(this);

        initTestConfig();

        GlobalFunc.makeLocalDir();

        startImServiceIfNeed();
        startFtpService();

        GlobalFunc.SetXmppConnectStatPref(mApplicationContext, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED);
        GlobalFunc.SetSipConnectStatPref(mApplicationContext, ConnectionState.eCONNSTAT_SIP_DISCONNECTED);
        GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
        GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
        GlobalFunc.updateDBNoProgressUpDownloading(this);

        initApplicationUI();
    }

    public void initTestConfig() {
        String apiServer = mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
        String fileServer = mPref.getString(GlobalConstrants.FILE_SERVER, GlobalConstrants.file_server_domain);
        String voipServer = mPref.getString(GlobalConstrants.VOIP_SERVER, GlobalConstrants.voip_server_domain);

        GlobalConstrants.server_domain = apiServer;
        GlobalConstrants.voip_server_domain = voipServer;

        GlobalVariable.FILE_SERVER_URL = mPref.getString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header) + fileServer + "/rsvcs/";
        GlobalVariable.WEBAPI_URL = mPref.getString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header) + apiServer + "/rsvcs/";

        mPref.putString(GlobalConstrants.API_URL, mPref.getString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header) + apiServer + "/rsvcs/");
        mPref.putString(GlobalConstrants.FILE_SERVER_URL, mPref.getString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header) + fileServer + "/rsvcs/");

        DebugConfig.SECURITY = mPref.getInt(GlobalConstrants.TLS_SECURITY, DebugConfig.SECURITY ? 1 : 0) == 1;
    }

    public void initApplicationUI() {
        if (applicationUIInited) {
            return;
        }
        applicationUIInited = true;

        intentFilter.addAction(MainTabNavigationActivity.BROADCAST_FILE_UP_DOWN_LOAD);
        intentFilter.addAction(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
        intentFilter.addAction(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED);
        applicationContext.registerReceiver(mRecevier, intentFilter);

        int font_scale_index = mPref.getInt(GlobalConstrants.FONT_SIZE_INDEX, 2);
        setFontScale(font_scale_index);
    }

    public static ImApp getInstance() {
        return mInstance;
    }

    public void startFtpService() {
        if (isServiceExisted(this, "com.antisip.service.FtpService") != null) {
            if (myPlaybackService == null) {
                serviceIntent = new Intent(this, FtpService.class);
                serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        myPlaybackService = IPlaybackService.Stub.asInterface(service);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        myPlaybackService = null;
                    }
                };
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        } else {
            newServiceConnection();
        }
    }

    public synchronized void newServiceConnection() {
        serviceIntent = new Intent(this, FtpService.class);
        startService(serviceIntent);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myPlaybackService = IPlaybackService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                myPlaybackService = null;
            }
        };

        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    public static ComponentName isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return null;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;

            if (serviceName.getClassName().equals(className)) {
                return serviceName;
            }
        }
        return null;
    }

	/*
     * public synchronized void addFileSendProgress(String packetId) {
	 * ProgressValue progressValue = new ProgressValue();
	 * mapUploadProgress.put(packetId, progressValue); }
	 */

    public synchronized void deleteFileSendProgress(String offerId) {
        mapUploadProgress.remove(offerId);
    }

	/*
     * public synchronized void addFileReceiveProgress(String offerId) {
	 * ProgressValue progressValue = new ProgressValue();
	 * mapDownloadProgress.put(offerId, progressValue); }
	 */

    public synchronized void deleteFileReceiveProgress(String offerId) {
        mapDownloadProgress.remove(offerId);
    }

    /**
     * @param fromUser: sender
     * @param toUser    : receiver
     * @param chatType  : 1: single chatting, 2: group 3: facebook
     * @param msgId     : message id
     * @param filePath  : file path
     * @param type      : 1: jpg, 2:audio, 3:mpeg 4: file
     */
    public synchronized void uploadHandler(String fromUser, String toUser, int chatType, int msgId, String packetId,
                                           String filePath, String thumbnailPath, int type, long providerId, String contactName, int sendCount, int totalCount) {
        try {
            if (myPlaybackService == null || mapUploadProgress.containsKey(msgId))
                return;
            Imps.updateOperMessageError(ImApp.getInstance().getContentResolver(), packetId, Imps.FileErrorCode.UPLOADING);
            Imps.updateMessageTypeInDb(ImApp.getInstance().getContentResolver(), packetId, Imps.MessageType.OUTGOING);
            myPlaybackService.uploadHandler(fromUser, toUser, chatType, msgId, packetId, filePath, thumbnailPath, type,
                    providerId, contactName, sendCount, totalCount);
            ProgressValue progressValue = new ProgressValue();
            mapUploadProgress.put(packetId, progressValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public synchronized void deleteUploadHandler(String packetId) {
        try {
            if (myPlaybackService == null)
                return;
            myPlaybackService.deleteUploadHandler(packetId);
            mapUploadProgress.remove(packetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public boolean containUploadThread(String packetId) {

        ProgressValue uploadProgress = mapUploadProgress.get(packetId);
        return uploadProgress != null;
    }

    public ProgressValue getUploadThread(String packetId) {
        return mapUploadProgress.get(packetId);
    }

	/*
	 * public void setUploadProgressValue(String msgId, String progressValue){
	 * ProgressValue uploadProgress = mapUploadProgress.get(msgId); if (
	 * uploadProgress != null) uploadProgress.progressValue = progressValue; }
	 */

    public ProgressValue getUploadProgress(String msgId) {
        ProgressValue uploadProgress = mapUploadProgress.get(msgId);
        return uploadProgress;
    }

    // download

    /**
     * @param msgId
     * @param filePath
     * @param type
     * @param to
     */
    public synchronized void downloadHandler(String chatId, String msgId, String filePath, String type, String to, String nickName,
                                             String thumbnailPath, int recvCount, int totalCount) {
        GlobalFunc.setDownloadingStatus(GlobalConstrants.DOWNLOADING);
        Imps.updateDeliveryStateInDb(getContentResolver(), msgId, 2);
        try {
            if (myPlaybackService == null || mapDownloadProgress.containsKey(msgId))
                return;
            myPlaybackService.downloadHandler(chatId, msgId, filePath, type, to, nickName, thumbnailPath, recvCount, totalCount);
            ProgressValue progressValue = new ProgressValue();
            mapDownloadProgress.put(msgId, progressValue);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
    }

    public boolean containDownloadThread(String msgId) {
        ProgressValue downloadProgress = mapDownloadProgress.get(msgId);
        return downloadProgress != null;
    }

    public synchronized void deleteDownloadHandler(String msgId) {
        try {
            if (myPlaybackService == null)
                return;
            myPlaybackService.deleteDownloadHandler(msgId);
            mapDownloadProgress.remove(msgId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public ProgressValue getDownloadProgress(String msgId) {
        ProgressValue downloadProgress = mapDownloadProgress.get(msgId);
        return downloadProgress;
    }

    public boolean setFontScale(int index) {
        mPref.putInt(GlobalConstrants.FONT_SIZE_INDEX, index);

        Configuration config = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        if (index == 0) {
            config.fontScale = 0.7f;
        } else if (index == 1) {
            config.fontScale = 0.85f;
        } else if (index == 2) {
            config.fontScale = 1f;
        } else if (index == 3) {
            config.fontScale = 1.15f;
        } else if (index == 4) {
            config.fontScale = 1.3f;
        }

        getResources().updateConfiguration(config, metrics);

        return true;
    }

    @Override
    public void onTerminate() {
        stopImServiceIfInactive();
        if (mImService != null) {
            try {
                mImService.removeConnectionCreatedListener(mConnCreationListener);
            } catch (Exception e) {
                DebugConfig.warn(TAG, "failed to cii_remove ConnectionCreatedListener");
            }
        }

        super.onTerminate();
    }

    public synchronized void startImServiceIfNeed() {
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(ImServiceConstants.IM_SERVICE_COMPONENT);
        serviceIntent.putExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, true);

        if (mImService == null) {
            mApplicationContext.startService(serviceIntent);
        }

        if (mConnectionListener == null) {
            mConnectionListener = new MyConnListener(new Handler());
        }

        if (mImServiceConn != null && mImService == null) {
            mApplicationContext.bindService(serviceIntent, mImServiceConn, Context.BIND_AUTO_CREATE);
        }
    }

    public boolean hasActiveConnections() {
        return !mConnections.isEmpty();
    }

    public synchronized void stopImServiceIfInactive() {
        boolean hasActiveConnection = true;
        hasActiveConnection = !mConnections.isEmpty();
        DebugConfig.error("****", "stopImServiceIfInactive");
        if (!hasActiveConnection) {
            DebugConfig.error(TAG, "stop ImService because there's no active connections");

            if (mImService != null) {
                DebugConfig.error("*****", "mApplicationContext.unbindService(mImServiceConn)");
                mApplicationContext.unbindService(mImServiceConn);
                mImService = null;
            }
            Intent intent = new Intent();
            intent.setComponent(ImServiceConstants.IM_SERVICE_COMPONENT);
            DebugConfig.error("*****", "mApplicationContext.stopService(intent)");
            mApplicationContext.stopService(intent);
        }
    }

    public synchronized void forceStopImService() {
        if (mImService != null) {
            DebugConfig.debug(TAG, "stop ImService");

            mApplicationContext.unbindService(mImServiceConn);
            mImService = null;

            Intent intent = new Intent();
            intent.setComponent(ImServiceConstants.IM_SERVICE_COMPONENT);
            mApplicationContext.stopService(intent);
        }
    }

    public synchronized void forceStopFtpService() {
        if (serviceConnection != null) {
            try {
                if (myPlaybackService != null)
                    myPlaybackService.removeConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mApplicationContext.unbindService(serviceConnection);
            serviceConnection = null;

            Intent intent = new Intent();
            intent.setComponent(ImServiceConstants.FTP_SERVICE_COMPONENT);
            mApplicationContext.stopService(intent);
        }
    }

    private ServiceConnection mImServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            DebugConfig.debug(TAG, "service connected");

            mImService = IRemoteImService.Stub.asInterface(service);
            fetchActiveConnections();

            synchronized (mQueue) {
                for (Message msg : mQueue) {
                    msg.sendToTarget();
                }
                mQueue.clear();
            }
            Message msg = Message.obtain(null, EVENT_SERVICE_CONNECTED);
            mBroadcaster.broadcast(msg);

			/*
			 * if (mKillServerOnStart) { forceStopImService(); }
			 */
        }

        public void onServiceDisconnected(ComponentName className) {
            DebugConfig.debug(TAG, "service disconnected");

            mConnections.clear();
            mImService = null;
        }
    };

    public boolean serviceConnected() {
        return mImService != null;
    }

    public boolean isNetworkAvailableAndConnected() {
        ConnectivityManager manager = (ConnectivityManager) mApplicationContext.getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo nInfo = manager.getActiveNetworkInfo();

        if (nInfo != null) {
            return nInfo.isAvailable() && nInfo.isConnected();
        } else
            return false; // no network info is a bad idea
    }

    Thread loginThread = null;

    public boolean isServerLogined() {
        boolean ret = false;
        try {
            if (!isNetworkAvailableAndConnected())
                return ret;

            if (mImService == null)
                return ret;

            long mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
            IImConnection conn = getConnection(mProviderId);

            if (conn != null) {
                int state = conn.getState();
                if (state == ImConnection.LOGGED_IN)
                    ret = true;
            }

            if (ret == false) {
                if (loginThread == null) {
                    loginThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                long mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
                                long mAccountId = mPref.getLong(GlobalConstrants.store_picaAccountId, -1);
                                createConnection(mProviderId, mAccountId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            loginThread = null;
                        }
                    });
                    loginThread.start();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static long insertOrUpdateAccount(ContentResolver cr, long providerId, String userName, String pw) {
        /*String selection = Imps.Account.USERNAME + "= ?";
        String[] selectionArgs = {userName};
        Cursor c = null;
        try {
            c = cr.query(Imps.Account.CONTENT_URI, ACCOUNT_PROJECTION, selection, selectionArgs, null);
            if (c != null && c.moveToFirst()) {
                long id = c.getLong(c.getColumnIndexOrThrow(Imps.Account._ID));
                ContentValues values = new ContentValues(1);
                values.put(Imps.Account.PROVIDER, 1);
                values.put(Imps.Account.NAME, userName);
                values.put(Imps.Account.USERNAME, userName);
                values.put(Imps.Account.PASSWORD, pw);
                values.put(Imps.Account.ACTIVE, 1);
                values.put(Imps.Account.KEEP_SIGNED_IN, 0);
                Uri accountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, id);
                cr.update(accountUri, values, null, null);
                c.close();
                c = null;
                return id;
            } else {
                ContentValues values = new ContentValues(4);
                values.put(Imps.Account.PROVIDER, 1);
                values.put(Imps.Account.NAME, userName);
                values.put(Imps.Account.USERNAME, userName);
                values.put(Imps.Account.PASSWORD, pw);
                values.put(Imps.Account.ACTIVE, 1);
                values.put(Imps.Account.KEEP_SIGNED_IN, 0);
                Uri result = cr.insert(Imps.Account.CONTENT_URI, values);
                if (c != null) {
                    c.close();
                    c = null;
                }
                if (result == null)
                    return 0;
                return ContentUris.parseId(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                c.close();
                c = null;
            }
            return 0;
        }*/

        /// -----   Truncate Account table and Insert New Account ( ICICICI 20190322 )   ------ ///
        cr.delete(Imps.Account.CONTENT_URI, "1", null);     //Truncate Table
        ContentValues values = new ContentValues(4);
        values.put(Imps.Account.PROVIDER, 1);
        values.put(Imps.Account.NAME, userName);
        values.put(Imps.Account.USERNAME, userName);
        values.put(Imps.Account.PASSWORD, pw);
        values.put(Imps.Account.ACTIVE, 1);
        values.put(Imps.Account.KEEP_SIGNED_IN, 0);
        Uri result = cr.insert(Imps.Account.CONTENT_URI, values);
        if (result == null)
            return 0;
        return ContentUris.parseId(result);
        // ------------------------------------------------------------------------------------- //
    }

    public static void insertOrUpdateAccountStatus(ContentResolver cr, long accountId) {
        Uri uri = Imps.AccountStatus.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(Imps.AccountStatus.ACCOUNT, accountId);
        values.put(Imps.AccountStatus.PRESENCE_STATUS, Imps.Presence.OFFLINE);
        values.put(Imps.AccountStatus.CONNECTION_STATUS, Imps.ConnectionStatus.OFFLINE);
        cr.insert(uri, values);
    }

    /**
     * Used to reset the provider settings if a reload is required.
     */
    public void resetProviderSettings() {
        mProviders = null;
    }

    // For testing
    public void setImProviderSettings(HashMap<Long, ProviderDef> providers) {
        mProviders = providers;
    }

    private void loadImProviderSettings() {

        mProviders = new HashMap<Long, ProviderDef>();
        ContentResolver cr = getContentResolver();

        String selectionArgs[] = new String[1];
        selectionArgs[0] = ImApp.IMPS_CATEGORY;

        Cursor c = null;
        try {
            c = cr.query(Imps.Provider.CONTENT_URI, PROVIDER_PROJECTION, Imps.Provider.CATEGORY + "=?", selectionArgs,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                c.close();
                c = null;
            }
            return;
        }

        if (c == null) {
            return;
        }

        try {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                String providerName = c.getString(1);
                String fullName = c.getString(2);
                String signUpUrl = c.getString(3);

                mProviders.put(id, new ProviderDef(id, providerName, fullName, signUpUrl));
            }
        } finally {
            c.close();
        }
    }

    public long getProviderId(String name) {
        loadImProviderSettings();
        for (ProviderDef provider : mProviders.values()) {
            if (provider.mName.equals(name)) {
                return provider.mId;
            }
        }
        return -1;
    }

    public ProviderDef getProvider(long id) {
        loadImProviderSettings();
        return mProviders.get(id);
    }

    public List<ProviderDef> getProviders() {
        loadImProviderSettings();
        ArrayList<ProviderDef> result = new ArrayList<ProviderDef>();
        result.addAll(mProviders.values());
        return result;
    }

    public IImConnection getConnection(long providerId, long accountId) {

        IImConnection conn = getConnection(providerId);
        try {
            conn = createConnection(providerId, accountId);
        } catch (Exception ex) {
            DebugConfig.error(TAG, "Create Connection Error");
        }
        return conn;
    }

    public IImConnection createConnection(long providerId, long accountId) throws RemoteException {
        if (mImService == null) {
            // Service hasn't been connected or has died.
            return null;
        }

        if (providerId == 0 || accountId == 0)
            return null;

//		DebugConfig.error("*****", String.format("createConnection:%d,%d", providerId, accountId));

        IImConnection conn = null;

        conn = getConnection(providerId);

        if (conn != null && conn.getAccountId() != accountId) {
            DebugConfig.error("*****", "removeConnectionExisting");
            mConnections.remove(providerId);
            conn = null;
        }

        if (conn == null) {
            try {
                conn = mImService.createConnection(providerId, accountId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!mConnections.containsKey(providerId)) {
            mConnections.put(providerId, conn);
        }

        if (conn.getState() == ImConnection.LOGGED_IN) {
            if (applicationUIInited) {
                NotificationCenter.getInstance()
                        .postNotificationName(NotificationCenter.pica_connection_logined, 0);
            }
        } else if (conn.getState() == ImConnection.DISCONNECTED) {
            conn.login("", true, true);
        }
        conn.registerConnectionListener(mConnectionListener);
        conn.getContactListManager().registerContactListListener(mContactListListener);
        return conn;
    }

    public IImConnection getConnection(long providerId) {
        synchronized (mConnections) {
            IImConnection im = mConnections.get(providerId);
            if (im != null) {
                try {
                    im.getState();
                } catch (Exception doe) {
                    mConnections.clear();
                    fetchActiveConnections();
                    im = mConnections.get(providerId);
                }
            } else {
                fetchActiveConnections();
                im = mConnections.get(providerId);
            }

            return im;
        }
    }

    public IImConnection getConnectionByAccount(long accountId) {
        synchronized (mConnections) {
            for (IImConnection conn : mConnections.values()) {
                try {
                    if (conn.getAccountId() == accountId) {
                        return conn;
                    }
                } catch (Exception e) {
                    // No server!
                }
            }
            return null;
        }
    }

    public Collection<IImConnection> getActiveConnections() {

        return mConnections.values();
    }

	/*
	 * public void removeConnection(long providerId, long accountId) { try{
	 * mConnections.cii_remove(providerId); mImService.removeConnection(providerId,
	 * accountId); }catch(Exception e){ e.printStackTrace(); } }
	 */

    public void callWhenServiceConnected(Handler target, Runnable callback) {
        Message msg = Message.obtain(target, callback);
        if (serviceConnected() && msg != null) {
            msg.sendToTarget();
        } else {
            startImServiceIfNeed();
            synchronized (mQueue) {
                mQueue.add(msg);
            }
        }
    }

	/*
	 * @author : lightsky
	 * 
	 * @date : 2014/04/07
	 * 
	 * @description : delete account, when facebook login is failed.
	 */

    /***
     *
     * @param resolver
     *            : resolver
     * @param accountId
     *            : accountId
     * @param providerId
     *            : providerId
     */
    public static void deleteFailedAccount(ContentResolver resolver, long accountId, long providerId) {
        Uri accountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, accountId);
        resolver.delete(accountUri, null, null);

        Uri providerUri = ContentUris.withAppendedId(Imps.Provider.CONTENT_URI, providerId);
        resolver.delete(providerUri, null, null);

        Uri.Builder builder = Imps.Contacts.CONTENT_URI_CONTACTS_BY.buildUpon();
        ContentUris.appendId(builder, providerId);
        ContentUris.appendId(builder, accountId);
        resolver.delete(builder.build(), null, null);

    }
    // end - lightsky

    public void deleteAccount(long accountId, long providerId) {

        try {
            IImConnection conn = getConnection(providerId, accountId);
            if (conn != null)
                conn.logout();

            mConnections.remove(providerId);
            mImService.removeConnection(providerId, accountId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ContentResolver resolver = getContentResolver();
        Uri baseUri = Imps.Contacts.CONTENT_URI_CHAT_CONTACTS;
        Cursor cursor = null;
        try {
            String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.PROVIDER + "=?";
            String[] selectionArgs = {GlobalVariable.account_id, Long.toString(providerId)};

            cursor = resolver.query(baseUri, new String[]{Imps.Contacts._ID}, select,
                    selectionArgs, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    Imps.Notifications.removeNotificationCount(resolver, Imps.Notifications.CAT_CHATTING,
                            Imps.Notifications.FIELD_CHAT, id);
                }
                Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
                mApplicationContext.sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        Uri accountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, accountId);
        resolver.delete(accountUri, null, null);

        Uri providerUri = ContentUris.withAppendedId(Imps.Provider.CONTENT_URI, providerId);
        resolver.delete(providerUri, null, null);

        Uri.Builder builder = Imps.Contacts.CONTENT_URI_CONTACTS_BY.buildUpon();
        ContentUris.appendId(builder, providerId);
        ContentUris.appendId(builder, accountId);
        resolver.delete(builder.build(), null, null);

    }

    public void removePendingCall(Handler target) {
        synchronized (mQueue) {
            Iterator<Message> iter = mQueue.iterator();
            while (iter.hasNext()) {
                Message msg = iter.next();
                if (msg.getTarget() == target) {
                    iter.remove();
                }
            }
        }
    }

    public void registerForBroadcastEvent(int what, Handler target) {
        mBroadcaster.request(what, target, what);
    }

    public void unregisterForBroadcastEvent(int what, Handler target) {
        mBroadcaster.cancelRequest(what, target, what);
    }

    void broadcastConnEvent(int what, long providerId, ImErrorInfo error) {
        DebugConfig.debug(TAG, "broadcasting connection event " + what + ", provider id " + providerId);
        android.os.Message msg = android.os.Message.obtain(null, what, (int) (providerId >> 32), (int) providerId,
                error);
        mBroadcaster.broadcast(msg);
    }

    public void dismissNotifications(long providerId) {
        if (mImService != null) {
            try {
                mImService.dismissNotifications(providerId);
            } catch (Exception e) {
            }
        }
    }

    public void dismissChatNotification(long providerId, String username) {
        if (mImService != null) {
            try {
                mImService.dismissChatNotification(providerId, username);
            } catch (Exception e) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fetchActiveConnections() {
        if (mImService != null) {
            try {
                // register the listener before fetch so that we won't miss any
                // connection.
                mImService.addConnectionCreatedListener(mConnCreationListener);
                synchronized (mConnections) {
                    for (IBinder binder : (List<IBinder>) mImService.getActiveConnections()) {
                        IImConnection conn = IImConnection.Stub.asInterface(binder);
                        if (conn == null)
                            continue;
                        long providerId = conn.getProviderId();
                        if (!mConnections.containsKey(providerId)) {
                            mConnections.put(providerId, conn);
                        }
                        if (conn.getState() == ImConnection.LOGGED_IN) {
                            if (applicationUIInited) {
                                NotificationCenter.getInstance()
                                        .postNotificationName(NotificationCenter.pica_connection_logined, 0);
                            }
                        }
                        conn.registerConnectionListener(mConnectionListener);
                        conn.getContactListManager().registerContactListListener(mContactListListener);
                    }
                }
            } catch (Exception e) {
                DebugConfig.error(TAG, "fetching active connections", e);
            }
        }
    }

    private final IConnectionCreationListener mConnCreationListener = new IConnectionCreationListener.Stub() {
        public void onConnectionCreated(IImConnection conn) throws RemoteException {
            long providerId = conn.getProviderId();
            synchronized (mConnections) {
                if (!mConnections.containsKey(providerId)) {
                    mConnections.put(providerId, conn);
                }

                conn.registerConnectionListener(mConnectionListener);
                conn.getContactListManager().registerContactListListener(mContactListListener);

            }
            broadcastConnEvent(EVENT_CONNECTION_CREATED, providerId, null);
        }
    };

    private IContactListListener mContactListListener = new IContactListListener.Stub() {
        public void onAllContactListsLoaded() {
        }

        public void onContactChange(int type, IContactList list, Contact contact) {
            if (contact == null || contact.getAddress() == null || contact.getAddress().getAddress() == null) {
                return;
            }

            if (type == ContactListListener.CONTACT_AVATAR_UPDATED) {
                if (applicationUIInited) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.friend_list_reload, 0);
                }
            } else if (type == ContactListListener.CONTACT_STATUS_UPDATED) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.friend_list_reload, 0);
            } else if (type == ContactListListener.LIST_CONTACT_REMOVED) {
                if (applicationUIInited) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.friend_list_reload, 0);
                }
            }
        }

        public void onContactError(int errorType, ImErrorInfo error, String listName, Contact contact) {
        }

        public void onContactsPresenceUpdate(Contact[] contacts) {
        }
    };

    @SuppressWarnings("unused")
    private final IInvitationListener.Stub mInvitationListener = new IInvitationListener.Stub() {
        @Override
        public void onGroupInvitation(long id) throws RemoteException {

            NotificationCenter.getInstance().postNotificationName(NotificationCenter.group_chat_invited, id);
        }
    };

    private final class MyConnListener extends ConnectionListenerAdapter {
        public MyConnListener(Handler handler) {
            super(handler);
        }

        @Override
        public void onConnectionStateChange(IImConnection conn, int state, ImErrorInfo error) {
            try {
                if (state == ImConnection.LOGGED_IN) {
                    if (conn != null) {

                        NotificationCenter.getInstance()
                                .postNotificationName(NotificationCenter.pica_connection_logined, 0);
                        // conn.setInvitationListener(mInvitationListener);
                        conn.getContactListManager().registerContactListListener(mContactListListener);

                    }
                } else if (state == ImConnection.SUSPENDED) {
                    if (isNetworkAvailableAndConnected()) {
                        fetchActiveConnections();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpdateSelfPresenceError(IImConnection connection, ImErrorInfo error) {
            DebugConfig.debug(TAG, "onUpdateUserPresenceError(" + error + ")");
            try {
                long providerId = connection.getProviderId();
                broadcastConnEvent(EVENT_UPDATE_USER_PRESENCE_ERROR, providerId, error);
            } catch (Exception e) {
                DebugConfig.error(TAG, "onUpdateUserPresenceError", e);
            }
        }

        @Override
        public void onSelfPresenceUpdated(IImConnection connection) {
            DebugConfig.debug(TAG, "onUserPresenceUpdated");

            try {
                long providerId = connection.getProviderId();
                broadcastConnEvent(EVENT_USER_PRESENCE_UPDATED, providerId, null);
            } catch (Exception e) {
                DebugConfig.error(TAG, "onUserPresenceUpdated", e);
            }
        }
    }

    public IRemoteImService getRemoteImService() {
        return mImService;
    }

    public IChatSession getChatSession(long providerId, String remoteAddress) {
        IImConnection conn = getConnection(providerId);

        IChatSessionManager chatSessionManager = null;
        if (conn != null) {
            try {
                chatSessionManager = conn.getChatSessionManager();
            } catch (Exception e) {
                DebugConfig.error(TAG, "error in getting ChatSessionManager", e);
            }
        }

        if (chatSessionManager != null) {
            try {
                return chatSessionManager.getChatSession(remoteAddress);
            } catch (Exception e) {
                DebugConfig.error(TAG, "error in getting ChatSession", e);
            }
        }

        return null;
    }

    public void clearApplicationData() {
        try {
            File cache = getCacheDir();
            File appDir = new File(cache.getParent());
            if (appDir.exists()) {
                String[] children = appDir.list();
                for (String s : children) {
                    if (!s.equals("lib") && !s.equals("databases")) {
                        deleteDir(new File(appDir, s));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public void deleteLocalDir() {
        File file = new File(GlobalConstrants.LOCAL_PATH);
        deleteDir(file);
    }

    public void truncateDatabase() {
//		deleteLocalDir();

        ContentResolver resolver = getContentResolver();

//		try {
//			resolver.delete(Imps.Messages.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

//		try {
//			resolver.delete(Imps.ProviderSettings.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

//		try {
//			resolver.delete(Imps.Profile.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		try {
//			resolver.delete(Imps.Account.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		try {
//			resolver.delete(Imps.AccountStatus.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

        try {
            resolver.delete(Imps.Provider.CONTENT_URI, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

//		try {
//			resolver.delete(Imps.Contacts.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		try {
//			resolver.delete(Imps.ContactList.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		try {
//			resolver.delete(Imps.Invitation.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		try {
//			resolver.delete(Imps.GroupMembers.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		try {
//			resolver.delete(Imps.Chats.CONTENT_URI, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

        try {
            resolver.delete(Imps.Notifications.CONTENT_URI, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/*
	 * public void restartApp(Context context){
	 * 
	 * try{ EmojiManager.getInstance(context).clear(); }catch(Exception e){
	 * e.printStackTrace(); }
	 * 
	 * try{ Intent i = new Intent(context, MainActivity.class); ComponentName cn
	 * = i.getComponent(); Intent mainIntent =
	 * IntentCompat.makeRestartActivityTask(cn); startActivity(mainIntent);
	 * }catch(Exception e){ e.printStackTrace(); } }
	 */

    public void restartAppSoon(Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
        if (GlobalFunc.getXmppConnStatus() != ConnectionState.eCONNSTAT_XMPP_DISCONNECTED) {
            GlobalFunc.SetXmppConnectStatPref(this, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED);
        }

        if (GlobalFunc.getSipConnStatus() != ConnectionState.eCONNSTAT_SIP_DISCONNECTED) {
            GlobalFunc.SetSipConnectStatPref(this, ConnectionState.eCONNSTAT_SIP_DISCONNECTED);
        }
        System.exit(0);
    }

    public synchronized void stopService() {
        Collection<IImConnection> activeConns = getActiveConnections();

        for (IImConnection conn : activeConns) {
            try {
                long accountId = conn.getAccountId();
                long providerId = conn.getProviderId();
                conn.logout();
                DebugConfig.error("*****", String.format("removeConnection:%d,%d", providerId, accountId));
                mConnections.remove(providerId);
                mImService.removeConnection(providerId, accountId);
            } catch (Exception e) {
                DebugConfig.error(TAG, "ImApp.java / stopService() / conn.logout Exception : " + e.toString());
            }
        }
        mConnections.clear();
        activeConns.clear();
        forceStopImService();
        forceStopFtpService();
    }

    public static void RunOnUIThread(Runnable runnable) {
        RunOnUIThread(runnable, 0);
    }

    public static void RunOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            applicationHandler.post(runnable);
        } else {
            applicationHandler.postDelayed(runnable, delay);
        }
    }

    int lastClassGuid = 1;

    public int generateClassGuid() {
        int guid = lastClassGuid++;
        return guid;
    }

    public BroadcastReceiver mRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainTabNavigationActivity.BROADCAST_FILE_UP_DOWN_LOAD)) {
                String str = intent.getStringExtra("msg");
                final String[] params = intent.getStringArrayExtra("content");
                if (str.startsWith(GlobalConstrants.UPLOAD_SUCCESS)) {
                    // params[1]:msgId, params[2]:downloadpath,
                    // params[3]:providerId, params[4]:contactName,
                    // params[5]:filePath;
                    deleteUploadHandler(params[1]);
					/*
					 * long providerId = 0; try{ providerId =
					 * Integer.valueOf(params[3]); IImConnection conn =
					 * getConnection(providerId); if ( conn != null) {
					 * IChatSession session =
					 * conn.getChatSessionManager().getChatSession(params[4]);
					 * if ( session != null ) { String messageBody = params[2];
					 * session.sendMessage(messageBody, params[1], params[5]); }
					 * } }catch(Exception e) { e.printStackTrace(); }
					 */
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                } else if (str.startsWith(GlobalConstrants.UPLOAD_FAILED)) {
                    // params[1]:packetId, params[2]:msgId, params[3]:filePath
                    deleteUploadHandler(params[1]);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                } else if (str.startsWith(GlobalConstrants.UPLOAD_PROGRESS)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressValue pv = ImApp.getInstance().getUploadProgress(params[1]);
                            if (pv != null) {
                                try {
                                    pv.progressValue = Integer.valueOf(params[2]);
                                    // NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update,
                                    // 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        }
                    }).start();
                } else if (str.startsWith(GlobalConstrants.DOWNLOAD_SUCCESS)) {
                    // params[0]: filePath, params[1]:type, params[2]:msgId,
                    // params[3]:nickName(group chatting)
                    deleteDownloadHandler(params[2]);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);

                    //            send seen message after download finished         //
                    String address = params[3];
                    String serverIP = mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
                    if (!address.isEmpty())
                        address = address + "@" + serverIP;
                    try {
                        IImConnection iConn = ImApp.getInstance().getConnection(mPref.getLong(GlobalConstrants.store_picaProviderId, -1));
                        IChatSessionManager manager = null;
                        manager = iConn.getChatSessionManager();
                        IChatSession session = manager.getChatSession(address);
                        if (session == null)
                            session = manager.createChatSession(Address.stripResource(address));
                        if (session != null)
                            session.sendMessageSeen(params[2], null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (str.startsWith(GlobalConstrants.DOWNLOAD_FAILED)) {
                    // params[0]: msgid
                    deleteDownloadHandler(params[0]);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                } else if (str.startsWith(GlobalConstrants.DOWNLOAD_PROGRESS)) {
                    ProgressValue pv = ImApp.getInstance().getDownloadProgress(params[1]);
                    if (pv != null) {
                        try {
                            pv.progressValue = Integer.valueOf(params[2]);
                            // NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update,
                            // 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            } else if (intent.getAction().equals(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED)) {
                Cursor cursor;
                if (intent.hasExtra("packetId")) {
                    String packetId = intent.getStringExtra("packetId");
                    cursor = getContentResolver().query(Imps.Messages.CONTENT_URI, null,
                            Imps.Messages.PACKET_ID + "=? AND " + Imps.Messages.ERROR_CODE + "=?",
                            new String[]{packetId, String.valueOf(Imps.FileErrorCode.UPLOADING)}, null);
                } else {
                    cursor = getContentResolver().query(Imps.Messages.CONTENT_URI, null,
                            Imps.Messages.TYPE + "=? AND " + Imps.Messages.MIME_TYPE + " NOTNULL",
                            new String[]{String.valueOf(Imps.MessageType.POSTPONED)},
                            null);
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        final int mChatID = cursor.getInt(cursor.getColumnIndex(Imps.Messages.THREAD_ID));
                        final String filePath = cursor.getString(cursor.getColumnIndex(Imps.Messages.BODY));
                        final String mimeType = cursor.getString(cursor.getColumnIndex(Imps.Messages.MIME_TYPE));
                        final String receiverAddress = GlobalFunc.getUserNameFromThreadID(getContentResolver(), mChatID);
                        final String packetId = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
                        final int sendCount = cursor.getInt(cursor.getColumnIndex(Imps.Messages.FILE_SUB_COUNT));
                        final int totalCount = cursor.getInt(cursor.getColumnIndex(Imps.Messages.FILE_TOTAL_COUNT));

                        if (!GlobalFunc.isStorageWrittable()) {
                            Imps.updateMessageTypeInDb(getContentResolver(), packetId, Imps.MessageType.OUTGOING);
                            Imps.updateOperMessageError(getContentResolver(), packetId, Imps.FileErrorCode.UPLOADFAILED);
                            GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                            sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED));
                            return;
                        }

                        if (!GlobalConstrants.FILE_SPLITTING) {
                            if ((GlobalFunc.getUploadingStatus() == GlobalConstrants.NO_UPLOADING) || intent.hasExtra("packetId")) {
                                if (!intent.hasExtra("packetId") && !mimeType.equals("audio/4")) {
                                    PicaApiUtility.checkMimetype(getApplicationContext(), filePath, new MyXMLResponseHandler() {
                                        @Override
                                        public void onMySuccess(JSONObject response) {
                                            Log.e("ICICICICI", String.format("Check Mimetype Success: %s", response.toString()));

                                            Imps.updateOperMessageError(getContentResolver(), packetId, Imps.FileErrorCode.UPLOADING);

                                            Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                                            intent.putExtra("packetId", packetId);
                                            sendBroadcast(intent);
                                        }

                                        @Override
                                        public void onMyFailure(int errcode) {
                                            Log.e("ICICICICI", String.format("Check Mimetype Error Code: %d", errcode));

                                            Imps.updateOperMessageError(getContentResolver(), packetId, Imps.FileErrorCode.UPLOADFAILED);
                                            Imps.updateMessageTypeInDb(getContentResolver(), packetId, Imps.MessageType.OUTGOING);
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                            sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED));
                                        }
                                    });
                                } else {
                                    GlobalConstrants.FILE_SPLITTING = true;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String splitPath = GlobalFunc.makeSplitFile(filePath, sendCount);
                                            GlobalConstrants.FILE_SPLITTING = false;
                                            if ((Imps.getErrorByPacketId(packetId) == Imps.FileErrorCode.UPLOADCANCELLED) || (Imps.getErrorByPacketId(packetId) == -1)) {
                                                splitPath = null;
                                            }
                                            if (splitPath != null) {
                                                GlobalFunc.handleUpload(ImApp.getInstance().getApplicationContext(), mChatID, Uri.fromFile(new File(filePath)), splitPath, Integer.parseInt(mimeType.substring(6, 7)),
                                                        mimeType, receiverAddress, packetId, sendCount, totalCount);
                                            } else {
                                                if (Imps.getErrorByPacketId(packetId) != Imps.FileErrorCode.UPLOADCANCELLED) {
                                                    Imps.updateOperMessageError(getContentResolver(), packetId, Imps.FileErrorCode.UPLOADFAILED);
                                                    Imps.updateMessageTypeInDb(getContentResolver(), packetId, Imps.MessageType.OUTGOING);
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                                }
                                                sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED));
                                            }
                                        }
                                    }).start();
                                }
                            }
                        }
                    } else if (cursor.getCount() == 0) {
                        GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                    }
                    cursor.close();
                }
            } else if (intent.getAction().equals(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED)) {
                Cursor cursor;
                if (intent.hasExtra("packetid")) {
                    String packetId = intent.getStringExtra("packetid");
                    cursor = getContentResolver().query(Imps.Messages.CONTENT_URI, null,
                            Imps.Messages.PACKET_ID + "=? AND " + Imps.Messages.ERROR_CODE + "=?",
                            new String[]{packetId, String.valueOf(Imps.FileErrorCode.DOWNLOADING)}, null);
                } else {
                    cursor = getContentResolver().query(Imps.Messages.CONTENT_URI, null,
                            Imps.Messages.ERROR_CODE + "=? AND " + Imps.Messages.IS_DELIVERED + "=?",
                            new String[]{String.valueOf(Imps.FileErrorCode.DOWNLOADING), String.valueOf(0)},
                            null);
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int mChatID = cursor.getInt(cursor.getColumnIndex(Imps.Messages.THREAD_ID));
                        String filePath = cursor.getString(cursor.getColumnIndex(Imps.Messages.BODY));
                        String mimeType = cursor.getString(cursor.getColumnIndex(Imps.Messages.MIME_TYPE));
                        String receiverAddress = mPref.getString("username", "");
                        String packetId = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
                        int recvCount = cursor.getInt(cursor.getColumnIndex(Imps.Messages.FILE_SUB_COUNT));
                        int totalCount = cursor.getInt(cursor.getColumnIndex(Imps.Messages.FILE_TOTAL_COUNT));
                        String other = GlobalFunc.getUserNameFromThreadID(getContentResolver(), mChatID);
                        other = other.substring(0, other.lastIndexOf("@"));
                        if (!GlobalFunc.isStorageWrittable()) {
                            Imps.updateOperMessageError(getContentResolver(), packetId, Imps.FileErrorCode.DOWNLOADFAILED);
                            GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
                            sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
                        } else {
                            if (GlobalFunc.getDownloadingStatus() == GlobalConstrants.NO_DOWNLOADING || intent.hasExtra("packetid")) {
                                ImApp.getInstance().downloadHandler(String.valueOf(mChatID), packetId, filePath, mimeType.substring(6, 7), receiverAddress, other, null, recvCount, totalCount);
                            }
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                    }
                    cursor.close();
                }
            }
        }
    };


    /*Start Sip Variable*/
    private LinphonePreferences mPrefs;
    private LinphoneCoreListenerBase mListener;
    private LinphoneAddress address;
    private boolean newProxyConfig;
    private boolean accountCreated = false;
	/*End Sip Variable*/

    /* Start Sip Code*/
    public void initSipLogin() {

        while ( LinphonePreferences.instance().getAccountCount() > 0 ) {
            LinphonePreferences.instance().deleteAccount(0);
            accountCreated = false;
        }

//        ContactsManager.getInstance().initializeSyncAccount(getApplicationContext(), getContentResolver());
        mPrefs = LinphonePreferences.instance();
        mPrefs.useRandomPort(false);
        mPrefs.enableVideo(true);
        mPrefs.setAutomaticallyAcceptVideoRequests(true);
        mPrefs.setEchoCancellation(true);
        mPrefs.setRingtone(getFilesDir().getAbsolutePath() + "/defaultringtone.mp3");

        initAudioSetting();
        initVideoSetting();
        initCallSetting();

        if (!GlobalConstrants.stun_server_domain.isEmpty()) {
            mPrefs.setStunServer(GlobalConstrants.stun_server_domain);
            mPrefs.setIceEnabled(true);
        } else {
            mPrefs.setStunServer("");
            mPrefs.setIceEnabled(false);
        }

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();

        if (lc != null && lc.getCalls().length > 0) {
            if (TextUtils.isEmpty(GlobalFunc.hasUploading(this)) && TextUtils.isEmpty(GlobalFunc.hasDownloading(this))) {
                startActivity(new Intent(MainTabNavigationActivity.instance(), IncomingActivity.class));
            } else {
                if (LinphoneManager.isInstanciated()) {
                    LinphoneCall mCall = LinphoneManager.getLc().getCurrentCall();
                    if (mCall != null) {
                        LinphoneManager.getLc().terminateCall(mCall);
                    }
                }
            }
        }

        mListener = new LinphoneCoreListenerBase() {
            @Override
            public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneChatMessage message) {
            }

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig proxy, LinphoneCore.RegistrationState state, String smessage) {
                if (address != null && address.asString().equals(proxy.getIdentity())) {
                    if (state == LinphoneCore.RegistrationState.RegistrationOk) {
                        if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
                            launchEchoCancellerCalibration(true);
                        }
                        if (!MainTabNavigationActivity.isInstanciated())
                            onStartMainTabNavActivity();
                    }
                }

                if (state.equals(LinphoneCore.RegistrationState.RegistrationCleared)) {
                    if (lc != null) {
                        LinphoneAuthInfo authInfo = lc.findAuthInfo(proxy.getIdentity(), proxy.getRealm(), proxy.getDomain());
                        if (authInfo != null)
                            lc.removeAuthInfo(authInfo);
                    }
                }

                if (state.equals(LinphoneCore.RegistrationState.RegistrationFailed) && newProxyConfig) {
                    newProxyConfig = false;
                    if (proxy.getError() == Reason.BadCredentials) {
                        displayCustomToast(getString(R.string.error_bad_credentials), Toast.LENGTH_LONG);
                    }
                    if (proxy.getError() == Reason.Unauthorized) {
                        displayCustomToast(getString(R.string.error_unauthorized), Toast.LENGTH_LONG);
                    }
                    if (proxy.getError() == Reason.IOError) {
                        displayCustomToast(getString(R.string.error_io_error), Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
                if (state == LinphoneCall.State.IncomingReceived) {
                    if (TextUtils.isEmpty(GlobalFunc.hasUploading(getApplicationContext())) && TextUtils.isEmpty(GlobalFunc.hasDownloading(getApplicationContext()))) {
                        MainTabNavigationActivity.instance().startActivity(new Intent(MainTabNavigationActivity.instance(), IncomingActivity.class));
                    } else {
                        if (LinphoneManager.isInstanciated()) {
                            LinphoneCall mCall = LinphoneManager.getLc().getCurrentCall();
                            if (mCall != null) {
                                LinphoneManager.getLc().terminateCall(mCall);
                            }
                        }
                    }
                } else if (state == LinphoneCall.State.Error || state == LinphoneCall.State.CallEnd) {
                    sendCallMessage(call, message, ChatRoomActivity.isVideoCalling);
                    if (OutgoingActivity.isInstanciated())
                        OutgoingActivity.instance().finish();
                    if (IncomingActivity.isInstanciated())
                        IncomingActivity.instance().finish();
                    if (CallingActivity.isInstanciated())
                        CallingActivity.instance().finish();

                    // If up-download file in standby, start
                    sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED));
                    sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));

                } else if (state == LinphoneCall.State.CallEnd || state == LinphoneCall.State.Error || state == LinphoneCall.State.CallReleased) {

                }
                if (state == LinphoneCall.State.OutgoingRinging)
                    isRinging = true;
                else
                    isRinging = false;
            }
        };
        if (lc != null) {
            lc.addListener(mListener);
            lc.setAudioPortRange(35000, 50000);
            lc.setVideoPortRange(50000, 65000);
        }

        String username = mPref.getString("username", "");
        String password = mPref.getString("password", "");
        if (mPrefs.getAccountCount() == 0)
            genericLogIn(username, password, mPref.getString(GlobalConstrants.VOIP_SERVER, GlobalConstrants.voip_server_domain));
    }

    public void displayCustomToast(final String message, final int duration) {
    }

    public void genericLogIn(String username, String password, String domain) {
        logIn(username, password, domain, false);
    }

    private void logIn(String username, String password, String domain, boolean sendEcCalibrationResult) {
        saveCreatedAccount(username, password, domain);
        if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
            launchEchoCancellerCalibration(sendEcCalibrationResult);
        }
    }

    private void launchEchoCancellerCalibration(boolean sendEcCalibrationResult) {
        boolean needsEchoCalibration = LinphoneManager.getLc().needsEchoCalibration();
        if (needsEchoCalibration && mPrefs.isFirstLaunch()) {
            isEchoCalibrationFinished();
        } else {
            success();
        }
    }

    public void isEchoCalibrationFinished() {
        mPrefs.setAccountEnabled(mPrefs.getAccountCount() - 1, true);
        success();
    }

    public void success() {
        mPrefs.firstLaunchSuccessful();
        isNewProxyConfig();
    }

    public void isNewProxyConfig() {
        newProxyConfig = true;
    }

    public void saveCreatedAccount(String username, String password, String domain) {
        if (accountCreated)
            return;
        if (username.startsWith("sip:")) {
            username = username.substring(4);
        }
        if (username.contains("@"))
            username = username.split("@")[0];

        if (domain.startsWith("sip:")) {
            domain = domain.substring(4);
        }

        String identity = "sip:" + username + "@" + domain;
        try {
            address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
        LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setPassword(password);

        try {
            builder.saveNewAccount();
            accountCreated = true;
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    private void initAudioSetting() {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        for (final PayloadType pt : lc.getAudioCodecs()) {
            if (pt.getMime().equals("G729")) {
                try {
                    //lc.setPayloadTypeBitrate(pt, 8);
                    if (pt.toString().contains("[8000]"))
                        lc.enablePayloadType(pt, true);
                    else
                        lc.enablePayloadType(pt, false);
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    lc.enablePayloadType(pt, false);
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initVideoSetting() {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        for (final PayloadType pt : lc.getVideoCodecs()) {
            if (pt.getMime().equals("H264")) {
                try {
                    lc.setUploadBandwidth(mPref.getInt(GlobalConstrants.VIDEO_BITRATE, 100));
                    lc.setDownloadBandwidth(mPref.getInt(GlobalConstrants.VIDEO_BITRATE, 100));
                    if (pt.toString().contains("[90000]"))
                        lc.enablePayloadType(pt, true);
                    else
                        lc.enablePayloadType(pt, false);
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    lc.enablePayloadType(pt, false);
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                }
            }
        }

        lc.setPreferredFramerate(10);
        String preset = mPref.getString(GlobalConstrants.PRESET, getString(R.string.video_quality_high));
        if (preset.equals(getString(R.string.video_quality_high))) {
            lc.setPreferredVideoSizeByName("vga");
//            lc.setPreferredFramerate(10);
        } else if (preset.equals(getString(R.string.video_quality_medium))) {
            lc.setPreferredVideoSizeByName("qvga");
//            lc.setPreferredFramerate(9);
        } else if (preset.equals(getString(R.string.video_quality_low))) {
            lc.setPreferredVideoSizeByName("qvga");
//            lc.setPreferredFramerate(10);
        }
        mPref.putString(GlobalConstrants.PRESET, preset);
    }

    private void initCallSetting() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		boolean hasSrtp = lc.mediaEncryptionSupported(LinphoneCore.MediaEncryption.SRTP);
		if ( hasSrtp ){
			mPrefs.setMediaEncryption(LinphoneCore.MediaEncryption.SRTP);
		}
    }

    @SuppressLint("DefaultLocale")
    public boolean sendCallMessage(LinphoneCall call, String msg, boolean isVideo)    //messagetype-0:Declined
    {
        int status = Imps.StatusType.UNKNOWN;

        int callduration = call.getDuration();
        if (msg.equals("Call declined.")) {
            msg = getString(R.string.declined);
        } else if (msg.equals("Call terminated") && callduration == 0) {
            if (TextUtils.isEmpty(GlobalFunc.hasUploading(this)) && TextUtils.isEmpty(GlobalFunc.hasDownloading(this))) {
                msg = getString(R.string.cancelled);
            } else {
                msg = getString(R.string.not_accept);
                status = Imps.StatusType.CALL_STATUS_MISSED;
            }
        } else if (callduration > 0 && (msg.equals("Call ended") || msg.equals("Call terminated"))) {
            msg = String.format("%s %02d:%02d", getString(R.string.duration), callduration / 60, callduration % 60);
        } else if (callduration == 0 && msg.equals("Call ended")) {
            msg = getString(R.string.not_accept);
            status = Imps.StatusType.CALL_STATUS_MISSED;
        } else if (callduration == 0 && call.getState() == LinphoneCall.State.Error) {
            switch (msg.toLowerCase()) {
                case GlobalConstrants.CALL_REQUEST_TIMEOUT:
//                    msg = getString(R.string.call_msg_network_error);
                    msg = getString(R.string.no_answer);
                    break;
                case GlobalConstrants.CALL_BUSY_HERE:
                    if (isRinging)
                        msg = getString(R.string.declined);
                    else
                        msg = getString(R.string.busy);
                    break;
                case GlobalConstrants.CALL_BAD_NUMBER:
//                    msg = getString(R.string.bad_number);
                    msg = getString(R.string.call_failure);
                    break;
                case GlobalConstrants.CALL_LEVEL_NOT_ALLOWED:
//                    msg = getString(R.string.call_msg_level_not_allowed);
                    msg = getString(R.string.call_failure);
                    break;
                default:
                    msg = getString(R.string.call_failure);
                    break;
            }

        } else {
            return false;
        }

        if (!msg.equals(getString(R.string.cancelled))) {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }

        String sip = call.getRemoteAddress().asStringUriOnly();
        sip = sip.substring(sip.indexOf(':') + 1);
        sip = StringUtils.parseName(sip);
        String address = sip + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);

        if (isVideo)
            msg = String.format("[%s] %s", getString(R.string.video_call), msg);
        else
            msg = String.format("[%s] %s", getString(R.string.audio_call), msg);

        int messageType = (call.getDirection() == CallDirection.Outgoing) ? Imps.MessageType.OUTGOING : Imps.MessageType.INCOMING;

        IImConnection conn = ImApp.getInstance().getConnection(mPref.getLong(GlobalConstrants.store_picaProviderId, -1));

        if (conn != null) {
            try {
                IChatSessionManager manager = conn.getChatSessionManager();
                if (manager != null) {
                    IChatSession session = manager.getChatSession(address);
                    if (session == null) {
                        session = manager.createChatSession(address);
                    }
                    if (session != null) {
                        session.insertCallMessageInDb(msg, messageType, isVideo, callduration);
                    }
                }
            } catch (Exception e) {
            }
        }

        /*try{
            MainTabNavigationActivity.instance().updateViewMoreMenuState();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        if (ChatRoomActivity.instance() != null) {
            if (status == Imps.StatusType.CALL_STATUS_MISSED) {
                if (!address.equals(ChatRoomActivity.instance().contactName)) {
                    StatusBarNotifier.notifyMissedCall(mApplicationContext, call.getRemoteAddress().getUserName());
                }
            }

            try{
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
            } catch (Exception e) {e.printStackTrace();}

            ChatRoomActivity.instance().mChatView.setFocusHistoryView(500);
        } else {
            if (status == Imps.StatusType.CALL_STATUS_MISSED) {
                StatusBarNotifier.notifyMissedCall(mApplicationContext, call.getRemoteAddress().getUserName());
            }
        }

        return true;
    }

    private void onStartMainTabNavActivity() {
        Intent intent = new Intent(this, MainTabNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void startCallingActivity() {
        Intent intent = new Intent(this, CallingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
	/* End Sip Code*/
}
