/*
LinphoneService.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.linphone;

import org.linphone.compatibility.Compatibility;
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.multimediachat.app.ImApp;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.global.ConnectionState;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.*;

/**
 * 
 * Linphone service, reacting to Incoming calls, ...<br />
 * 
 * Roles include:<ul>
 * <li>Initializing LinphoneManager</li>
 * <li>Starting C libLinphone through LinphoneManager</li>
 * <li>Reacting to LinphoneManager state changes</li>
 * <li>Delegating GUI state change actions to GUI listener</li>
 * 
 * 
 * @author Guillaume Beraudo
 *
 */
public final class LinphoneService extends Service {
	/* Listener needs to be implemented in the Service as it calls
	 * setLatestEventInfo and startActivity() which needs a context.
	 */
	private static LinphoneService instance;
	
	private final static int INCALL_NOTIF_ID=2;
	private final static int MESSAGE_NOTIF_ID=3;

	public static boolean isReady() {
		return instance != null && instance.mTestDelayElapsed;
	}

	/**
	 * @throws RuntimeException service not instantiated
	 */
	public static LinphoneService instance()  {
		if (isReady()) return instance;

		throw new RuntimeException("LinphoneService not instantiated yet");
	}

	public Handler mHandler = new Handler();

	private boolean mTestDelayElapsed = true; // no timer
	private NotificationManager mNM;

	private Notification mIncallNotif;
	private Notification mMsgNotif;
	private int mMsgNotifCount;
	private PendingIntent mkeepAlivePendingIntent;
	private LinphoneCoreListenerBase mListener;

	@Override
	public void onCreate() {
		super.onCreate();

		// Needed in order for the two next calls to succeed, libraries must have been loaded first
		LinphoneCoreFactory.instance().setLogCollectionPath(getFilesDir().getAbsolutePath());
		LinphoneCoreFactory.instance().enableLogCollection(!(getResources().getBoolean(R.bool.disable_every_log)));
		
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNM.cancel(INCALL_NOTIF_ID); // in case of crash the icon is not removed

		LinphoneManager.createAndStart(LinphoneService.this);

		instance = this; // instance is ready once linphone manager has been created
		LinphoneManager.getLc().addListener(mListener = new LinphoneCoreListenerBase(){

			@Override
			public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
				if (instance == null) {
					return;
				}

				if (mPref.getInt(GlobalConstrants.store_step, GlobalConstrants.STEP_NEED_LOGIN) == GlobalConstrants.STEP_NEED_LOGIN) {
					return;
				}
				
				if (state == LinphoneCall.State.IncomingReceived) {
					onIncomingReceived();
				}

				if (state == LinphoneCall.State.Error || state == LinphoneCall.State.CallEnd) {
                    if (!MainTabNavigationActivity.isInstanciated()) {
                        ImApp.getInstance().sendCallMessage(call, message, ChatRoomActivity.isVideoCalling);
                    }
				}
				
				if (state == State.CallUpdatedByRemote) {
					// If the correspondent proposes video while audio call
					boolean remoteVideo = call.getRemoteParams().getVideoEnabled();
					boolean localVideo = call.getCurrentParamsCopy().getVideoEnabled();
					boolean autoAcceptCameraPolicy = LinphonePreferences.instance().shouldAutomaticallyAcceptVideoRequests();
					if (remoteVideo && !localVideo && !autoAcceptCameraPolicy && !LinphoneManager.getLc().isInConference()) {
						try {
							LinphoneManager.getLc().deferCallUpdate(call);
						} catch (LinphoneCoreException e) {
							e.printStackTrace();
						}
					}
				}

				if (state == State.StreamsRunning) {
					// Workaround bug current call seems to be updated after state changed to streams running
					if (getResources().getBoolean(R.bool.enable_call_notification))
						refreshIncallIcon(call);
				} else {
					if (getResources().getBoolean(R.bool.enable_call_notification))
						refreshIncallIcon(LinphoneManager.getLc().getCurrentCall());
				}
			}
			
			@Override
			public void globalState(LinphoneCore lc,LinphoneCore.GlobalState state, String message) {

			}

			@Override
			public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage) {
//				if (instance == null) {
//					return;
//				}
				if (state == RegistrationState.RegistrationOk && LinphoneManager.getLc().getDefaultProxyConfig() != null && LinphoneManager.getLc().getDefaultProxyConfig().isRegistered()) {
					//launchEchoCancellerCalibration(true);
					if (GlobalFunc.getSipConnStatus() != ConnectionState.eCONNSTAT_SIP_CONNECTED) {
						GlobalFunc.SetSipConnectStatPref(LinphoneService.this, ConnectionState.eCONNSTAT_SIP_CONNECTED);
					}
				}

				if ((state == RegistrationState.RegistrationFailed || state == RegistrationState.RegistrationCleared) && (LinphoneManager.getLc().getDefaultProxyConfig() == null || !LinphoneManager.getLc().getDefaultProxyConfig().isRegistered())) {
					if (GlobalFunc.getSipConnStatus() != ConnectionState.eCONNSTAT_SIP_CONNFAILED) {
						GlobalFunc.SetSipConnectStatPref(LinphoneService.this, ConnectionState.eCONNSTAT_SIP_CONNFAILED);
					}
				}

				if (state == RegistrationState.RegistrationNone) {
					if (GlobalFunc.getSipConnStatus() != ConnectionState.eCONNSTAT_SIP_DISCONNECTED) {
						GlobalFunc.SetSipConnectStatPref(LinphoneService.this, ConnectionState.eCONNSTAT_SIP_DISCONNECTED);
					}
				}
			}
		});
		
		this.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mObserver);

		if (!mTestDelayElapsed) {
			// Only used when testing. Simulates a 5 seconds delay for launching service
			mHandler.postDelayed(new Runnable() {
				@Override public void run() {
					mTestDelayElapsed = true;
				}
			}, 5000);
		}
		
		//make sure the application will at least wakes up every 10 mn
		Intent intent = new Intent(this, KeepAliveHandler.class);
	    mkeepAlivePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
																							, SystemClock.elapsedRealtime()+600000
																							, 600000
																							, mkeepAlivePendingIntent);
	}

	private ContentObserver mObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
		}

	};
		

	private enum IncallIconState {INCALL, PAUSE, AUDIO, VIDEO, IDLE}
	private IncallIconState mCurrentIncallIconState = IncallIconState.IDLE;
	private synchronized void setIncallIcon(IncallIconState state) {
		if (state == mCurrentIncallIconState) return;
		mCurrentIncallIconState = state;

		int notificationTextId = 0;
		int inconId = 0;
		
		switch (state) {
		case IDLE:
			mNM.cancel(INCALL_NOTIF_ID);
			return;
		case INCALL:
			inconId = R.drawable.topbar_call_notification;
			notificationTextId = R.string.incall_notif_active;
			break;
		case PAUSE:
			inconId = R.drawable.topbar_call_notification;
			notificationTextId = R.string.incall_notif_paused;
			break;
		case AUDIO:
			inconId = R.drawable.topbar_call_notification;
			notificationTextId = R.string.incall_notif_audio;
			break;
		case VIDEO:
			inconId = R.drawable.topbar_videocall_notification;
			notificationTextId = R.string.incall_notif_video;
			break;	
		default:
			throw new IllegalArgumentException("Unknown state " + state);
		}
		
		if (LinphoneManager.getLc().getCallsNb() == 0) {
			return;
		}
		
		LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
		String userName = call.getRemoteAddress().getUserName();
		String domain = call.getRemoteAddress().getDomain();
		String displayName = call.getRemoteAddress().getDisplayName();
		LinphoneAddress address = LinphoneCoreFactory.instance().createLinphoneAddress(userName,domain,null);
		address.setDisplayName(displayName);

		Contact contact = ContactsManager.getInstance().findContactWithAddress(getContentResolver(), address);

		Uri pictureUri = contact != null ? contact.getPhotoUri() : null;
		Bitmap bm = null;
		try {
			bm = MediaStore.Images.Media.getBitmap(getContentResolver(), pictureUri);
		} catch (Exception e) {
			bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		}

//		String name = address.getDisplayName() == null ? address.getUserName() : address.getDisplayName();
		String name = "";
		com.multimediachat.app.im.engine.Contact contactInfo = DatabaseUtils.getContactInfo(getContentResolver(), /*"ub" + */address.getUserName() + "@" + address.getDomain());
		if (contactInfo != null)
			name = contactInfo.getName() != null ? contactInfo.getName() : contactInfo.getAddress().getAddress();
		else
			name = address.getUserName();

		Intent notifIntent = null;
		switch (state) {
			case INCALL:
				if (call.getDirection() == CallDirection.Outgoing)
					notifIntent = new Intent(this, OutgoingActivity.class);
				else
					notifIntent = new Intent(this, IncomingActivity.class);
				break;
			case AUDIO:
			case VIDEO:
				notifIntent = new Intent(this, CallingActivity.class);
				break;
		}
//		notifIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT );
		notifIntent.putExtra("Notification", true);
		PendingIntent mIntent = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mIncallNotif = Compatibility.createInCallNotification(getApplicationContext(), getString(R.string.app_name), getString(notificationTextId), inconId, bm, name, mIntent);
		mIncallNotif.flags |= Notification.FLAG_NO_CLEAR;

		notifyWrapper(INCALL_NOTIF_ID, mIncallNotif);
	}

	public void refreshIncallIcon(LinphoneCall currentCall) {
		LinphoneCore lc = LinphoneManager.getLc();
		if (currentCall != null) {
			if (currentCall.getCurrentParamsCopy().getVideoEnabled() && currentCall.cameraEnabled()) {
				// checking first current params is mandatory
				setIncallIcon(IncallIconState.VIDEO);
			} else if (currentCall.getState() == State.StreamsRunning) {
				setIncallIcon(IncallIconState.AUDIO);
			} else {
				setIncallIcon(IncallIconState.INCALL);
			}
		} else if (lc.getCallsNb() == 0) {
			setIncallIcon(IncallIconState.IDLE);
		}  else if (lc.isInConference()) {
			setIncallIcon(IncallIconState.INCALL);
		} else {
			setIncallIcon(IncallIconState.PAUSE);
		}
	}

	public void displayMessageNotification(String fromSipUri, String fromName, String message) {
		Intent notifIntent = new Intent(this, LinphoneActivity.class);
		notifIntent.putExtra("GoToChat", true);
		notifIntent.putExtra("ChatContactSipUri", fromSipUri);
		
		PendingIntent notifContentIntent = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		if (fromName == null) {
			fromName = fromSipUri;
		}
		
		if (mMsgNotif == null) {
			mMsgNotifCount = 1;
		} else {
			mMsgNotifCount++;
		}
		
		Uri pictureUri = null;
		try {
			Contact contact = ContactsManager.getInstance().findContactWithAddress(getContentResolver(), LinphoneCoreFactory.instance().createLinphoneAddress(fromSipUri));
			if (contact != null)
				pictureUri = contact.getThumbnailUri();
		} catch (LinphoneCoreException e1) {
			Log.e("Cannot parse from address ", e1);
		}
		
		Bitmap bm = null;
		if (pictureUri != null) {
			try {
				bm = MediaStore.Images.Media.getBitmap(getContentResolver(), pictureUri);
			} catch (Exception e) {
				bm = BitmapFactory.decodeResource(getResources(), R.drawable.profilephoto);
			}
		} else {
			bm = BitmapFactory.decodeResource(getResources(), R.drawable.profilephoto);
		}
		mMsgNotif = Compatibility.createMessageNotification(getApplicationContext(), mMsgNotifCount, fromName, message, bm, notifContentIntent);
		
		notifyWrapper(MESSAGE_NOTIF_ID, mMsgNotif);
	}
	
	public void removeMessageNotification() {
		mNM.cancel(MESSAGE_NOTIF_ID);
	}

	/**
	 * Wrap notifier to avoid setting the linphone icons while the service
	 * is stopping. When the (rare) bug is triggered, the linphone icon is
	 * present despite the service is not running. To trigger it one could
	 * stop linphone as soon as it is started. Transport configured with TLS.
	 */
	private synchronized void notifyWrapper(int id, Notification notification) {
		if (instance != null && notification != null) {
			mNM.notify(id, notification); //arirangchat
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		LinphoneCore lc = LinphoneManager.getLc();
		if (lc != null) {
			lc.terminateAllCalls();
		}

		Intent restartServiceIntent = new Intent(getApplicationContext(),
				this.getClass());
		restartServiceIntent.setPackage(getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(
				getApplicationContext(), 1, restartServiceIntent,
				PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		alarmService.set(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + 300,
				restartServicePendingIntent);

		super.onTaskRemoved(rootIntent);
	}

	@Override
	public synchronized void onDestroy() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.removeListener(mListener);
		}
		
		instance = null;
		LinphoneManager.destroy();

	    // Make sure our notification is gone.
	    mNM.cancel(INCALL_NOTIF_ID);
	    mNM.cancel(MESSAGE_NOTIF_ID);

	    ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).cancel(mkeepAlivePendingIntent);
		getContentResolver().unregisterContentObserver(mObserver);
		super.onDestroy();
	}
	
	protected void onIncomingReceived() {
		/*	start arirangchat*/
		String username = mPref.getString("username", "");
		if ( username.isEmpty() )
			return;
		//wakeup linphone
		if ( !BaseActivity.isActive ) {
			/*	end arirangchat*/
			if (TextUtils.isEmpty(GlobalFunc.hasUploading(this)) && TextUtils.isEmpty(GlobalFunc.hasDownloading(this))) {
				Intent intent = new Intent(this, IncomingActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else {
				if (LinphoneManager.isInstanciated()) {
					LinphoneCall mCall = LinphoneManager.getLc().getCurrentCall();
					if (mCall != null) {
						LinphoneManager.getLc().terminateCall(mCall);
					}
				}
			}
		}
	}
}

