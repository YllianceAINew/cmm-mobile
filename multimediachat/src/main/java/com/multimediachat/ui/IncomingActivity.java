/*
IncomingCallActivity.java
Copyright (C) 2011  Belledonne Communications, Grenoble, France

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
package com.multimediachat.ui;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.ui.views.CircularImageView;

import org.jivesoftware.smack.util.StringUtils;
import org.linphone.BluetoothManager;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneUtils;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreListenerBase;

import java.util.List;

/**
 * Activity displayed when a call comes in.
 * It should bypass the screen cii_lock mechanism.
 *
 * @author Guillaume Beraudo
 */
public class IncomingActivity extends BaseActivity implements View.OnClickListener , SensorEventListener {

	private static IncomingActivity instance;

	private TextView mNameView;
	private TextView mTxtIncomingType;
	private CircularImageView mImgProfile;

	private LinphoneCall mCall;
	private LinphoneCoreListenerBase mListener;

	public static IncomingActivity instance() {
		return instance;
	}

	public static boolean isInstanciated() {
		return instance != null;
	}

	private String useraddr;

	private boolean isEnding = false;

	private static PowerManager powerManager;
	private static PowerManager.WakeLock wakeLock;
	private static int field = 0x00000020;
	private SensorManager mSensorManager;
	private Sensor mProximity;

	private AudioManager mAudioManager;
	private ComponentName mAudioReceiver;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.incoming_activity);
		hideActionBar();

		if(!BluetoothManager.getInstance().isBluetoothHeadsetAvailable())
			BluetoothManager.getInstance().initBluetooth();

		mNameView = findViewById(R.id.txt_name);
		mTxtIncomingType = findViewById(R.id.txt_incoming_type);
		mImgProfile = findViewById(R.id.profileImg);

		findViewById(R.id.btn_accept_request).setOnClickListener(this);
		findViewById(R.id.btn_decline_request).setOnClickListener(this);

        // set this flag so this activity will stay in front of the keyguard
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flags);

        mListener = new LinphoneCoreListenerBase(){
        	@Override
        	public void callState(LinphoneCore lc, LinphoneCall call, State state, String message) {
        		if (call == mCall && State.CallEnd == state) {
//        			finish();
					if ( !isEnding ) {
						isEnding = true;
//						txtPlacePhoneTip.setText(getResources().getString(R.string.cancelled_by_caller));
//						txtPlacePhoneTip.setVisibility(View.VISIBLE);

						mTxtIncomingType.setVisibility(View.INVISIBLE);

						finish();
					}
				}
        		if (state == State.StreamsRunning) {
        			// The following should not be needed except some devices need it (e.g. Galaxy S).
        			LinphoneManager.getLc().enableSpeaker(LinphoneManager.getLc().isSpeakerEnabled());
        		}
        	}
        };

        if (GlobalFunc.hasUploading(this).length() != 0) {
			if (LinphoneManager.isInstanciated()) {
				mCall = LinphoneManager.getLc().getCurrentCall();
				if (mCall != null) {
					LinphoneManager.getLc().terminateCall(mCall);
				}
			}
			finish();
			return;
		}

		if (GlobalFunc.hasDownloading(this).length() != 0) {
			if (LinphoneManager.isInstanciated()) {
				mCall = LinphoneManager.getLc().getCurrentCall();
				if (mCall != null) {
					LinphoneManager.getLc().terminateCall(mCall);
				}
			}
			finish();
			return;
		}

		try {
			// Yeah, this is hidden field.
			field = PowerManager.class.getClass().getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
		} catch (Throwable ignored) {
		}

		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(field, getLocalClassName());

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioReceiver =  new ComponentName(getPackageName(),
				MediaButtonIntentReceiver.class.getName());

		instance = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		instance = this;
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.addListener(mListener);
		}
		
		// Only one call ringing at a time is allowed
		if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
			List<LinphoneCall> calls = LinphoneUtils.getLinphoneCalls(LinphoneManager.getLc());
			for (LinphoneCall call : calls) {
				if (State.IncomingReceived == call.getState()) {
					mCall = call;

					if ( mCall.getRemoteParams().getVideoEnabled() ) {
						mTxtIncomingType.setText(getResources().getString(R.string.invite_video_call));
						ChatRoomActivity.isVideoCalling = true;
					}
					else {
						mTxtIncomingType.setText(getResources().getString(R.string.invite_voice_call));
						ChatRoomActivity.isVideoCalling = false;
					}
					break;
				}
			}
		}
		if (mCall == null) {
			Intent intent;
			if (MainTabNavigationActivity.isInstanciated()) {
				intent = new Intent(this, MainTabNavigationActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			} else {
				intent = new Intent(this, MainActivity.class);
			}
            startActivity(intent);
            finish();
            return;
		}
		// To be done after findUriPictureOfContactAndSetDisplayName called
		LinphoneAddress address = mCall.getRemoteAddress();
		// May be greatly sped up using a drawable cache


		useraddr = address.asStringUriOnly();
		useraddr = useraddr.substring(useraddr.indexOf(':')+1);
		//useraddr = "ub"+useraddr;
		useraddr = StringUtils.parseName(useraddr)+"@"+ mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);

		Contact contact = DatabaseUtils.getContactInfo(getContentResolver(), useraddr);

		if ( contact != null ) {
			mNameView.setText(contact.getName());
			mNameView.setSelected(true);
			GlobalFunc.showAvatar(this, useraddr, mImgProfile);
		}

		mAudioManager.registerMediaButtonEventReceiver(mAudioReceiver);
	}

	public static final String[] CONTACT_PROJECTION = { Imps.Contacts._ID, Imps.Contacts.PROVIDER,
			Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME, Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
			Imps.Contacts.SUBSCRIPTION_TYPE, Imps.Contacts.SUBSCRIPTION_STATUS, Imps.Presence.PRESENCE_STATUS,
			Imps.Chats.LAST_MESSAGE_DATE, Imps.Chats.LAST_UNREAD_MESSAGE, Imps.Contacts.AVATAR_DATA,
			Imps.Contacts.STATUSMESSAGE, Imps.Contacts.GENDER, Imps.Contacts.REGION,
			Imps.Contacts.FAVORITE, Imps.Contacts.SUBSCRIPTIONMESSAGE

	};

	@Override
	protected void onPause() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.removeListener(mListener);
		}
		super.onPause();

		mAudioManager.unregisterMediaButtonEventReceiver(mAudioReceiver);
	}
	
	@Override
	protected void onDestroy() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
		if (wakeLock != null) {
			if (wakeLock.isHeld()) {
				wakeLock.release();
			}
		}
		instance = null;
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_POWER) {
			LinphoneManager.getInstance().stopRinging();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void decline() {
		LinphoneManager.getLc().terminateCall(mCall);

		isEnding = true;

		mTxtIncomingType.setVisibility(View.INVISIBLE);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, 3000);
	}
	
	private void answer() {
		LinphoneCallParams params = LinphoneManager.getLc().createCallParams(mCall);
		
		boolean isLowBandwidthConnection = !LinphoneUtils.isHighBandwidthConnection(this);
		if (isLowBandwidthConnection) {
			params.enableLowBandwidth(true);
		}

		if (!LinphoneManager.getInstance().acceptCallWithParams(mCall, params)) {
			// the above method takes care of Samsung Galaxy S
			Toast.makeText(this, R.string.couldnt_accept_call, Toast.LENGTH_LONG).show();
		} else {
			final LinphoneCallParams remoteParams = mCall.getRemoteParams();
			if (remoteParams != null && remoteParams.getVideoEnabled() && LinphonePreferences.instance().shouldAutomaticallyAcceptVideoRequests()) {
				LinphoneManager.getLc().enableSpeaker(true);
			}
			mApp.startCallingActivity();
		}
	}

	@Override
	public void onClick(View view) {
		if ( isEnding )
			return;

		switch (view.getId()){
			case R.id.btn_accept_request:
				answer();
				finish();
				break;
			case R.id.btn_decline_request:
				decline();
				break;
		}
	}

	@Override
	public void onBackPressed() {

	}

	public static Boolean isProximitySensorNearby(final SensorEvent event) {
		float threshold = 4.001f; // <= 4 cm is near

		final float distanceInCm = event.values[0];
		final float maxDistance = event.sensor.getMaximumRange();

		if (maxDistance <= threshold) {
			// Case binary 0/1 and short sensors
			threshold = maxDistance;
		}
		return distanceInCm < threshold;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.timestamp == 0) return;
		if(isProximitySensorNearby(event)){
			if(!wakeLock.isHeld()) {
				wakeLock.acquire();
			}
		} else {
			if(wakeLock.isHeld()) {
				wakeLock.release();
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	/*
	 * added by JHK(2019.11.07)
	 * class "MediaButtonIntentReceiver" receive signal from bluetooth device and process it.
	 */
	public static class MediaButtonIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (IncomingActivity.instance() != null) {
				IncomingActivity.instance().answer();
				IncomingActivity.instance().finish();
			}
		}
	}
}
