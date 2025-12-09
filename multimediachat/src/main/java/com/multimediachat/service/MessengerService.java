package com.multimediachat.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;

import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImPluginHelper;
import com.multimediachat.app.NetworkConnectivityListener;
import com.multimediachat.app.NetworkConnectivityListener.State;
import com.multimediachat.app.im.IConnectionCreationListener;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.IRemoteImService;
import com.multimediachat.app.im.ImService;
import com.multimediachat.app.im.engine.ConnectionFactory;
import com.multimediachat.app.im.engine.ImConnection;
import com.multimediachat.app.im.engine.ImException;
import com.multimediachat.app.im.plugin.ImPluginInfo;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.ConnectionState;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.util.Debug;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MessengerService extends Service implements ImService {
	private static final String[] ACCOUNT_PROJECTION = { Imps.Account._ID, Imps.Account.PROVIDER,
		Imps.Account.USERNAME,
		Imps.Account.PASSWORD, };
	private static final int ACCOUNT_ID_COLUMN = 0;
	private static final int ACCOUNT_PROVIDER_COLUMN = 1;

	private static final int EVENT_SHOW_TOAST = 100;

	private StatusBarNotifier mStatusBarNotifier;
	private ServiceHandler mServiceHandler;
	private Handler		   mHandler;
	private int mNetworkType;
	private boolean mNeedCheckAutoLogin;

	private ImPluginHelper mPluginHelper;
	private Hashtable<String, ImConnectionAdapter> mConnections;

	private Imps.ProviderSettings.QueryMap mGlobalSettings;

	final RemoteCallbackList<IConnectionCreationListener> mRemoteListeners = new RemoteCallbackList<IConnectionCreationListener>();
	public long mHeartbeatInterval;
	private WakeLock mWakeLock;
	private State mNetworkState;
	private Handler handler ;

	private int versionCode = 0;		// as low as it gets
	private String latestVersionName;
	private Integer latestVersionCode;
	private String latestUrl;

	private NetworkConnectivityListener mNetworkConnectivityListener;
	private static final int EVENT_NETWORK_STATE_CHANGED = 200;
	
	private PendingIntent mPendingIntent;
	final String HEARTBEAT_ACTION = "com.multimediachat.app.im.SERVICE.HEARTBEAT";
	final long HEARTBEAT_INTERVAL = 1000 * 60;
	
	
	private static final String TAG = "GB.ImService";

	public long getHeartbeatInterval() {
		return mHeartbeatInterval;
	}

	public static void debug(String msg) {
		DebugConfig.debug(TAG,msg);
	}

	public static void debug(String msg, Exception e) {
		DebugConfig.error(TAG,msg,e);
	}

	private Imps.ProviderSettings.QueryMap getGlobalSettings() {
		if (mGlobalSettings == null) {

			try {
				ContentResolver contentResolver = getContentResolver();

				Cursor cursor = contentResolver.query(Imps.ProviderSettings.CONTENT_URI,new String[] {Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE},Imps.ProviderSettings.PROVIDER + "=?",new String[] { Long.toString(Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS)},null);

				if (cursor == null)
					return null;

				mGlobalSettings = new Imps.ProviderSettings.QueryMap(cursor, contentResolver, Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, true, handler);
			}catch(Exception e){}
		}

		return mGlobalSettings;
	}

	@Override
	public void onCreate() {
		GlobalVariable.WEBAPI_URL = mPref.getString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header) + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain)+"/rsvcs/";

		mConnections = new Hashtable<String, ImConnectionAdapter>();
		Debug.onServiceStart();
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IM_WAKELOCK");

		clearConnectionStatii();
		mStatusBarNotifier = new StatusBarNotifier(this);
		mServiceHandler = new ServiceHandler();
		handler = new Handler();

		mPluginHelper = ImPluginHelper.getInstance(this);
		mPluginHelper.loadAvailablePlugins();

		// Have the heartbeat start autoLogin, unless onStart turns this off
		mNeedCheckAutoLogin = true;

		//change date event receive
		IntentFilter s_intentFilter = new IntentFilter();
		s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
		s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

		mNetworkConnectivityListener = new NetworkConnectivityListener();
        NetworkConnectivityListener.registerHandler(mServiceHandler, EVENT_NETWORK_STATE_CHANGED);
        mNetworkConnectivityListener.startListening(this);
        
        mPendingIntent = PendingIntent.getService(this, 0, new Intent(HEARTBEAT_ACTION, null,this, MessengerService.class), 0);
        startHeartbeat(HEARTBEAT_INTERVAL);
	}
	
	void startHeartbeat(long interval) {
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(mPendingIntent);
        if (interval > 0)
        {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, interval, mPendingIntent);
        }
    }

	public void sendHeartbeat() {
		try {
			if (mNeedCheckAutoLogin && mNetworkState != State.NOT_CONNECTED) {
				mNeedCheckAutoLogin = false;
				autoLogin();
			}

			if (getGlobalSettings() != null)
				mHeartbeatInterval = getGlobalSettings().getHeartbeatInterval();
			else
				mHeartbeatInterval = 1;

			if (mNetworkState == State.NOT_CONNECTED)
				return;

			for (ImConnectionAdapter conn : mConnections.values())
			{
				conn.sendHeartbeat();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if ( intent == null )
			return START_STICKY;

		if (HEARTBEAT_ACTION.equals(intent.getAction())) {
			try {
				mWakeLock.acquire();
				sendHeartbeat();
			}catch(Exception e) {
				e.printStackTrace();
			} finally {
				mWakeLock.release();
			}
			return START_STICKY;
		}

		if (intent.hasExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN))
			mNeedCheckAutoLogin = intent.getBooleanExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, true);
		else
			mNeedCheckAutoLogin = true;

		// Check and login accounts if network is ready, otherwise it's checked
		// when the network becomes available.
		if (mNeedCheckAutoLogin && mNetworkState != State.NOT_CONNECTED) {
			mNeedCheckAutoLogin = false;
			autoLogin();
		}

		return START_STICKY;
	}



	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}


	private void clearConnectionStatii() {
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues(2);

		values.put(Imps.AccountStatus.PRESENCE_STATUS, Imps.Presence.OFFLINE);
		values.put(Imps.AccountStatus.CONNECTION_STATUS, Imps.ConnectionStatus.OFFLINE);

		try
		{
			//insert on the "account_status" uri actually replaces the existing value 
			cr.update(Imps.AccountStatus.CONTENT_URI, values, null, null);
		}
		catch (Exception e)
		{
			//this can throw NPE on restart sometimes if database has not been unlocked
			debug("database is not unlocked yet. caught NPE from mDbHelper in ImpsProvider");
		}
	}


	private void autoLogin() {
		/*if ( mConnections != null && !mConnections.isEmpty()) {
			return;
		}*/
		ContentResolver resolver = getContentResolver();
		String where = Imps.Account.KEEP_SIGNED_IN + "=1 AND " + Imps.Account.ACTIVE + "=1";
		Cursor cursor = resolver.query(Imps.Account.CONTENT_URI, ACCOUNT_PROJECTION, where, null, null);
		if (cursor == null) {
			return;
		}
		while (cursor.moveToNext()) {
			long accountId = 0;
			long providerId = 0;

			try
			{
				accountId = cursor.getLong(ACCOUNT_ID_COLUMN);
				providerId = cursor.getLong(ACCOUNT_PROVIDER_COLUMN);
				if ( accountId > 0 && providerId > 0 ) {
					IImConnection conn = createConnection(providerId, accountId);

					if (conn != null && conn.getState() != ImConnection.LOGGED_IN)
					{
						try {
							conn.login(null, true, true);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		cursor.close();
	}

	private Map<String, String> loadProviderSettings(long providerId) {
		ContentResolver cr = getContentResolver();
		Map<String, String> settings = Imps.ProviderSettings.queryProviderSettings(cr, providerId);
		return settings;
	}

	@Override
	public void onDestroy() {
		for (ImConnectionAdapter conn : mConnections.values()) {
			conn.logout();
		}
		mConnections.clear();

		if (mGlobalSettings != null)
			mGlobalSettings.close();

		NetworkConnectivityListener.unregisterHandler(mServiceHandler);
        mNetworkConnectivityListener.stopListening();
        mNetworkConnectivityListener = null;

		
		Intent intent = new Intent("com.multimediachat.start");
        sendBroadcast(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void showToast(CharSequence text, int duration) {
		Message msg = Message.obtain(mHandler, EVENT_SHOW_TOAST, duration, 0, text);
		msg.sendToTarget();
	}

	public StatusBarNotifier getStatusBarNotifier() {
		return mStatusBarNotifier;
	}
	
	public void scheduleReconnect(long delay) {
		if (!isNetworkAvailable()) {
			return;
		}
		final Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			public void run() {
				reestablishConnections();
			}
		}, delay);
	}

	IImConnection createConnection(final long providerId, final long accountId) {
		final IImConnection[] results = new IImConnection[1];
		Debug.wrapExceptions(new Runnable() {
			@Override
			public void run() {
				results[0] = do_createConnection(providerId, accountId);
			}
		});
		return results[0];
	}

	IImConnection do_createConnection(long providerId, long accountId) {
		String strConnKey = getConnectionKey(providerId, accountId);
		if ( strConnKey != null ) {
			ImConnectionAdapter connAdapter = mConnections.get(strConnKey);
			if ( connAdapter != null )
				return connAdapter;
		}
		
		try {
			Map<String, String> settings = loadProviderSettings(providerId);
			ConnectionFactory factory = ConnectionFactory.getInstance();

			ImConnection conn = factory.createConnection(settings, this);

			if ( conn == null )
				return null;

			conn.initUser(providerId, accountId);
			ImConnectionAdapter imConnectionAdapter = new ImConnectionAdapter(providerId, accountId, conn, this);


			ContentResolver contentResolver = getContentResolver();

			Cursor cursor = contentResolver.query(Imps.ProviderSettings.CONTENT_URI,new String[] {Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE},Imps.ProviderSettings.PROVIDER + "=?",new String[] { Long.toString(providerId)},null);

			if (cursor == null)
				throw new ImException ("unable to query the provider settings");

			Imps.ProviderSettings.QueryMap providerSettings = new Imps.ProviderSettings.QueryMap(
					cursor, contentResolver, providerId, false, null);
			String userName = Imps.Account.getUserName(contentResolver, accountId);
			String domain = providerSettings.getDomain();
			providerSettings.close();

			mConnections.put(userName + '@' + domain,imConnectionAdapter);

			final int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IConnectionCreationListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onConnectionCreated(imConnectionAdapter);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			mRemoteListeners.finishBroadcast();

			return imConnectionAdapter;
		} catch (ImException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	void removeConnection(ImConnectionAdapter connection) {

		mConnections.remove(connection);

		if (mConnections.size() == 0)
			if (getGlobalSettings().getUseForegroundPriority())
				stopForeground(true);
	}

	public boolean isNetworkAvailable() {
		if ( mNetworkState == null )
			return false;
		else
			return mNetworkState == State.CONNECTED;
	}

	void networkStateChanged(NetworkInfo networkInfo, State networkState) {
		mNetworkState = networkState;
		int oldType = mNetworkType;
		mNetworkType = networkInfo != null ? networkInfo.getType() : -1;
		
		if (mNetworkType != oldType && isNetworkAvailable()) {
			for (ImConnectionAdapter conn : mConnections.values()) {
				if ( conn != null )
					conn.networkTypeChanged();
			}
		}

		NetworkInfo.State state = networkInfo != null ? networkInfo.getState() : NetworkInfo.State.DISCONNECTED;
		switch (state) {
		case CONNECTED:
			if (mNeedCheckAutoLogin) 
			{
				mNeedCheckAutoLogin = false;
				autoLogin();
				break;
			}

			reestablishConnections();
			sendHeartbeat();

			//sendInformationAsync();
			if (GlobalFunc.getXmppConnStatus() != ConnectionState.eCONNSTAT_XMPP_CONNECTED) {
				GlobalFunc.SetXmppConnectStatPref(this, ConnectionState.eCONNSTAT_XMPP_CONNECTED);
//				StatusBarNotifier.notifyServerState(this, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED, getString(R.string.server_connection_error));
			}
			break;

		case DISCONNECTED:
			if (!isNetworkAvailable()) {
				suspendConnections();
			}
			//            mNeedCheckAutoLogin = true;
			//            sendHeartbeat();
			if (GlobalFunc.getXmppConnStatus() != ConnectionState.eCONNSTAT_XMPP_DISCONNECTED) {
				GlobalFunc.SetXmppConnectStatPref(this, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED);
				GlobalFunc.SetXmppConnectErrPref(-1, "Network Failed");
//				StatusBarNotifier.notifyServerState(this, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED, getString(R.string.server_connection_error));
			}
			break;
		default:
			break;
		}
	}

	void reestablishConnections() {
		if (!isNetworkAvailable()) {
			return;
		}

		for (ImConnectionAdapter conn : mConnections.values()) {
			int connState = conn.getState();
			if (connState == ImConnection.SUSPENDED) {
				conn.reestablishSession();
			}

			if ( connState == ImConnection.DISCONNECTED ) {
				conn.login(null, true, true);
			}
		}

	}

	private void suspendConnections() {
		for (ImConnectionAdapter conn : mConnections.values()) {
			if (conn.getState() != ImConnection.LOGGED_IN) {
				continue;
			}
			conn.suspend();
		}
	}


	public ImConnectionAdapter getConnection(String username) {
		return mConnections.get(username);
	}

	private final IRemoteImService.Stub mBinder = new IRemoteImService.Stub() {

		@Override
		public List<ImPluginInfo> getAllPlugins() {
			return new ArrayList<ImPluginInfo>(mPluginHelper.getPluginsInfo());
		}

		@Override
		public void addConnectionCreatedListener(IConnectionCreationListener listener) {
			if (listener != null) {
				mRemoteListeners.register(listener);
			}
		}

		@Override
		public void removeConnectionCreatedListener(IConnectionCreationListener listener) {
			if (listener != null) {
				mRemoteListeners.unregister(listener);
			}
		}

		@Override
		public IImConnection createConnection(long providerId, long accountId) {
			return MessengerService.this.createConnection(providerId, accountId);
		}

		@Override
		public List<IBinder> getActiveConnections() {
			ArrayList<IBinder> result = new ArrayList<IBinder>(mConnections.size());
			for (IImConnection conn : mConnections.values()) {
				result.add(conn.asBinder());
			}
			return result;
		}

		@Override
		public void dismissNotifications(long providerId) {
			mStatusBarNotifier.dismissNotifications(providerId);
		}

		@Override
		public void dismissChatNotification(long providerId, String username) {
			mStatusBarNotifier.dismissChatNotification(providerId, username);
		}


		@Override
		public void setKillProcessOnStop (boolean killProcessOnStop)
		{
			//            mKillProcessOnStop = killProcessOnStop;
		}

		@Override
		public void enableDebugLogging (boolean debug)
		{
			Debug.DEBUG_ENABLED = debug;
		}

		@Override
		public void removeConnection(long providerId, long accountId) throws RemoteException {
			String strConnKey = getConnectionKey(providerId, accountId);
			if ( strConnKey != null ) {
				mConnections.remove(strConnKey);
			}
		}
	};
	
	public String getConnectionKey(long providerId, long accountId) {
		String res = null;
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = null;
		try{
			cursor = contentResolver.query(Imps.ProviderSettings.CONTENT_URI,new String[] {Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE},Imps.ProviderSettings.PROVIDER + "=?",new String[] { Long.toString(providerId)},null);
			if ( cursor != null ) {
				Imps.ProviderSettings.QueryMap providerSettings = new Imps.ProviderSettings.QueryMap(
						cursor, contentResolver, providerId, false, null);
				String userName = Imps.Account.getUserName(contentResolver, accountId);
				String domain = providerSettings.getDomain();
				providerSettings.close();
				res = userName + '@' + domain;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null && !cursor.isClosed() ) {
				cursor.close();
				cursor = null;
			}
		}
		return res;
	}

	private final class ServiceHandler extends Handler {

		@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_NETWORK_STATE_CHANGED:
            	if ( mNetworkConnectivityListener != null ) {
            		networkStateChanged(mNetworkConnectivityListener.getNetworkInfo(), mNetworkConnectivityListener.getState() );
            	}
                break;

            default:
            }
        }
	}
}
