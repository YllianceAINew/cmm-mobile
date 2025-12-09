/*
CallOutgoingActivity.java
Copyright (C) 2015  Belledonne Communications, Grenoble, France

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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.multimediachat.app.ImApp;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.global.GlobalConstrants;

import org.linphone.*;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreListenerBase;

import java.util.List;

public class OutgoingActivity extends Activity implements OnClickListener, SensorEventListener {

	private static OutgoingActivity instance;

	private TextView mTxtUsername;
	private CircularImageView mImgProfile;
	private LinphoneCall mCall;
	private LinphoneCoreListenerBase mListener;

	private boolean isSpeakerEnabled;
	private boolean isMuted;

	public static OutgoingActivity instance() {
		return instance;
	}

	public static boolean isInstanciated() {
		return instance != null;
	}

	private boolean isVideo;
	private String mAddress;

	private static PowerManager powerManager;
	private static PowerManager.WakeLock wakeLock;
	private static int field = 0x00000020;
	private SensorManager mSensorManager;
	private Sensor mProximity;
	private AudioManager mAudioManager;
	private ComponentName mAudioReceiver;

	private ImageView mBtnHangup;
	private ImageView mBtnMute;
	private ImageView mBtnSpeaker;
	private ImageView mBtnToVoice;
	private ImageView mBtnSwitchCamera;

	private LinearLayout mLytMute = null;
	private LinearLayout mLytToVoice = null;
	private LinearLayout mLytSwitchCamera = null;
	private LinearLayout mLytSpeaker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(!BluetoothManager.getInstance().isBluetoothHeadsetAvailable())
			BluetoothManager.getInstance().initBluetooth();

		if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		isVideo = getIntent().getBooleanExtra("VideoEnabled", false);
		mAddress = getIntent().getStringExtra("address");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.calloutgoing);

		mTxtUsername = findViewById(R.id.txt_name);
		mImgProfile = findViewById(R.id.profileImg);

		mLytMute = findViewById(R.id.lyt_mute);
		mLytToVoice = findViewById(R.id.lyt_to_voice_call);
		mLytSwitchCamera = findViewById(R.id.lyt_switch_camera);
		mLytSpeaker = findViewById(R.id.lyt_speaker);

		isSpeakerEnabled = false;
		isMuted = false;

		mBtnMute = findViewById(R.id.btn_mute);
		mBtnMute.setOnClickListener(this);
		mBtnToVoice = findViewById(R.id.btn_to_voice_call);
		mBtnToVoice.setOnClickListener(this);
		mBtnSwitchCamera = findViewById(R.id.btn_switch_camera);
		mBtnSwitchCamera.setOnClickListener(this);
		mBtnSpeaker = findViewById(R.id.btn_speaker);
		mBtnSpeaker.setOnClickListener(this);

		// set this flag so this activity will stay in front of the keyguard
		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
		getWindow().addFlags(flags);

		findViewById(R.id.btn_hangup).setOnClickListener(this);

		if (isVideo) {
			isSpeakerEnabled = true;
			mBtnSpeaker.setSelected(isSpeakerEnabled);
            mBtnMute.setSelected(isMuted);
//			mLytToVoice.setVisibility(View.VISIBLE);
			mLytMute.setVisibility(View.VISIBLE);
			mLytSwitchCamera.setVisibility(View.VISIBLE);
		} else {
			isSpeakerEnabled = false;
			mBtnSpeaker.setSelected(isSpeakerEnabled);
            mBtnMute.setSelected(isMuted);
			mLytMute.setVisibility(View.VISIBLE);
			mLytSpeaker.setVisibility(View.VISIBLE);
		}

		if (LinphoneManager.isInstanciated()) {
            LinphoneManager.getLc().enableSpeaker(isSpeakerEnabled);
            LinphoneManager.getLc().muteMic(isSpeakerEnabled);
		}

		mListener = new LinphoneCoreListenerBase(){
			@Override
			public void callState(LinphoneCore lc, LinphoneCall call, State state, String message) {

				if (LinphoneManager.getLc().getCallsNb() == 0) {
					finish();
					return;
				}
				if (call == mCall && State.CallEnd == state) {
					finish();
				}

				if (call == mCall && (State.Connected == state)){
					if (!LinphoneManager.isInstanciated()) {
						return;
					}
					ImApp.getInstance().startCallingActivity();
					finish();
				}
			}
		};

		LinphonePreferences.instance().setInitiateVideoCall(isVideo);
		if (LinphoneManager.getLc().getCurrentCall() == null) {
			LinphoneManager.getInstance().newOutgoingCall(mAddress, mAddress);
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

		if (!isVideo) {
			mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
		}

		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.addListener(mListener);
		}

		instance = this;

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioReceiver =  new ComponentName(getPackageName(),
				OutgoingActivity.MediaButtonIntentReceiver.class.getName());
	}

	@Override
	protected void onResume() {
		super.onResume();
		instance = this;

		// Only one call ringing at a time is allowed
		if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
			List<LinphoneCall> calls = LinphoneUtils.getLinphoneCalls(LinphoneManager.getLc());
			for (LinphoneCall call : calls) {
				if (State.OutgoingInit == call.getState() || State.OutgoingProgress == call.getState() || State.OutgoingRinging == call.getState() || State.OutgoingEarlyMedia == call.getState()) {
					mCall = call;
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

		LinphoneAddress address = mCall.getRemoteAddress();
		String useraddr = /*"ub"+*/ mAddress+"@"+ mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
		Contact contact = DatabaseUtils.getContactInfo(getContentResolver(), useraddr);


		if (contact != null) {
			mTxtUsername.setText(contact.getName());
			GlobalFunc.showAvatar(this, useraddr, mImgProfile);
		} else {
			mTxtUsername.setText(LinphoneUtils.getAddressDisplayName(address));
		}
		mTxtUsername.setSelected(true);

		mAudioManager.registerMediaButtonEventReceiver(mAudioReceiver);
	}

	@Override
	protected void onDestroy() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.removeListener(mListener);
		}

        if (!isVideo) {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
            }
			if (wakeLock != null) {
				if (wakeLock.isHeld()) {
					wakeLock.release();
				}
			}
        }
		instance = null;
		super.onDestroy();
		mAudioManager.unregisterMediaButtonEventReceiver(mAudioReceiver);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btn_speaker:
				isSpeakerEnabled = !isSpeakerEnabled;
				mBtnSpeaker.setSelected(isSpeakerEnabled);
				LinphoneManager.getLc().enableSpeaker(isSpeakerEnabled);
				break;
			case R.id.btn_mute:
				isMuted = !isMuted;
				LinphoneManager.getLc().muteMic(isMuted);
                mBtnMute.setSelected(isMuted);
				break;
			case R.id.btn_to_voice_call:
				break;
			case R.id.btn_switch_camera:
				break;
			case R.id.btn_hangup:
				decline();
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;
		if (LinphoneUtils.onKeyVolumeAdjust(keyCode)) return true;

		return super.onKeyDown(keyCode, event);
	}

	private void decline() {
		LinphoneManager.getLc().terminateCall(mCall);
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

	@Override
	public void onBackPressed() {
		
	}

	/*
	 * added by JHK(2019.11.07)
	 * class "MediaButtonIntentReceiver" receive signal from bluetooth device and process it.
	 */
	public static class MediaButtonIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (OutgoingActivity.instance() != null) {
				OutgoingActivity.instance().decline();
				OutgoingActivity.instance().finish();
			}
		}
	}
}
