package com.multimediachat.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.ui.fragment.CallAudioFragment;
import com.multimediachat.ui.fragment.CallVideoFragment;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.PrefUtil.mPref;

import org.linphone.BluetoothManager;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneUtils;
import org.linphone.UIThreadDispatcher;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphonePlayer;

import java.util.Arrays;
import java.util.List;


public class CallingActivity extends Activity implements OnClickListener, SensorEventListener, ActivityCompat.OnRequestPermissionsResultCallback {
	private final static int SECONDS_BEFORE_HIDING_CONTROLS = 4000;
	private final static int SECONDS_BEFORE_DENYING_CALL_UPDATE = 30000;
	private static final int PERMISSIONS_REQUEST_CAMERA = 202;
	private static final int PERMISSIONS_ENABLED_CAMERA = 203;

	private static CallingActivity instance;

	private Handler mControlsHandler = new Handler();
	private Runnable mControls;
	private RelativeLayout mActiveCallHeader, avatar_layout;
	private CircularImageView mImgProfile;
	private LinearLayout mBottomBtnLyt;
	private LinearLayout mNoCurrentCall, mCallPaused;
	private ImageView mBtnMute, mBtnSpeaker, mBtnSwitchCamera;
	private LinearLayout mLytSpeaker, mLytSwitchCamera;
    private RelativeLayout callInfo;
	private CallAudioFragment audioCallFragment;
	private CallVideoFragment videoCallFragment;
	private boolean isSpeakerEnabled = false, isMicMuted = false;
	private CountDownTimer timer;
	private boolean isVideoCallPaused = false;

	private static PowerManager powerManager;
	private static PowerManager.WakeLock wakeLock;
	private static int field = 0x00000020;

	private LinearLayout callsList;
	private LayoutInflater inflater;
	private ViewGroup container;
	private boolean isConferenceRunning = false;
	private LinphoneCoreListenerBase mListener;
	private MainProgress waiting_progress = null;

	public static CallingActivity instance() {
		return instance;
	}

	public static boolean isInstanciated() {
		return instance != null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setContentView(R.layout.incall_activity);

		try {
			field = PowerManager.class.getClass().getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
		} catch (Throwable ignored) {
		}

		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(field, getLocalClassName());

		mListener = new LinphoneCoreListenerBase(){
			@Override
			public void callState(LinphoneCore lc, final LinphoneCall call, State state, String message) {
				waiting_progress.dismiss();
				if (LinphoneManager.getLc().getCallsNb() == 0) {
					finish();
					return;
				}

				if (state == State.IncomingReceived) {
					return;
				}

				if (state == State.Paused || state == State.PausedByRemote ||  state == State.Pausing) {
					if(LinphoneManager.getLc().getCurrentCall() != null) {
					}
					if(isVideoEnabled(call)){
						showAudioView();
					}
				}

				if (state == State.Resuming) {
					if(LinphonePreferences.instance().isVideoEnabled()){
						if(call.getCurrentParamsCopy().getVideoEnabled()){
							showVideoView();
						}
					}
					if(LinphoneManager.getLc().getCurrentCall() != null) {
					}
				}

				if (state == State.StreamsRunning) {
					switchVideo(isVideoEnabled(call));
					enableAndRefreshInCallActions();
				}

				if (state == State.CallUpdatedByRemote) {
					// If the correspondent proposes video while audio call
					boolean videoEnabled = LinphonePreferences.instance().isVideoEnabled();
					if (!videoEnabled) {
						acceptCallUpdate(false);
					}

					boolean remoteVideo = call.getRemoteParams().getVideoEnabled();
					boolean localVideo = call.getCurrentParamsCopy().getVideoEnabled();
					boolean autoAcceptCameraPolicy = LinphonePreferences.instance().shouldAutomaticallyAcceptVideoRequests();
					if (remoteVideo && !localVideo && !autoAcceptCameraPolicy && !LinphoneManager.getLc().isInConference()) {
							timer = new CountDownTimer(SECONDS_BEFORE_DENYING_CALL_UPDATE, 1000) {
								public void onTick(long millisUntilFinished) { }
								public void onFinish() {
									//TODO dismiss dialog
									acceptCallUpdate(false);
								}
							}.start();
					}
				}

				refreshIncallUi();
			}

			@Override
			public void callEncryptionChanged(LinphoneCore lc, final LinphoneCall call, boolean encrypted, String authenticationToken) {

			}

		};

		if (findViewById(R.id.fragmentContainer) != null) {
			initUI();

			if (LinphoneManager.getLc().getCallsNb() > 0) {
				LinphoneCall call = LinphoneManager.getLc().getCalls()[0];

				if (LinphoneUtils.isCallEstablished(call)) {
					enableAndRefreshInCallActions();
				}
			}
			if (savedInstanceState != null) {
				// Fragment already created, no need to create it again (else it will generate a memory leak with duplicated fragments)
				isSpeakerEnabled = savedInstanceState.getBoolean("Speaker");
				isMicMuted = savedInstanceState.getBoolean("Mic");
				isVideoCallPaused = savedInstanceState.getBoolean("VideoCallPaused");
				refreshInCallActions();
				return;
			} else {
				isSpeakerEnabled = LinphoneManager.getLc().isSpeakerEnabled();
				isMicMuted = LinphoneManager.getLc().isMicMuted();
			}

			mBtnSwitchCamera.setSelected(isSpeakerEnabled);
			mBtnMute.setSelected(isMicMuted);

			Fragment callFragment;
			if (isVideoEnabled(LinphoneManager.getLc().getCurrentCall())) {
				callFragment = new CallVideoFragment();
				videoCallFragment = (CallVideoFragment) callFragment;
				displayVideoCall(false);
				mLytSwitchCamera.setVisibility(View.VISIBLE);
				mLytSpeaker.setVisibility(View.GONE);
			} else {
				callFragment = new CallAudioFragment();
				audioCallFragment = (CallAudioFragment) callFragment;
				mLytSwitchCamera.setVisibility(View.GONE);
				mLytSpeaker.setVisibility(View.VISIBLE);
			}

			callFragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction().add(R.id.fragmentContainer, callFragment).commitAllowingStateLoss();

		}

		waiting_progress = new MainProgress(this);
		waiting_progress.setMessage(getString(R.string.waiting_convert));

	}

	private boolean isVideoEnabled(LinphoneCall call) {
		if(call != null){
			return call.getCurrentParamsCopy().getVideoEnabled();
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("Speaker", LinphoneManager.getLc().isSpeakerEnabled());
		outState.putBoolean("Mic", LinphoneManager.getLc().isMicMuted());
		outState.putBoolean("VideoCallPaused", isVideoCallPaused);

		super.onSaveInstanceState(outState);
	}

	private void initUI() {
		inflater = LayoutInflater.from(this);
		container = findViewById(R.id.topLayout);
		callsList = findViewById(R.id.calls_list);

		mBtnMute = findViewById(R.id.btn_mute);
		mBtnMute.setOnClickListener(this);

		mBtnSpeaker = findViewById(R.id.btn_speaker);
		mBtnSpeaker.setOnClickListener(this);

		mBtnSwitchCamera = findViewById(R.id.btn_switch_camera);
		mBtnSwitchCamera.setOnClickListener(this);

		mLytSpeaker = findViewById(R.id.lyt_speaker);
		mLytSwitchCamera = findViewById(R.id.lyt_switch_camera);

		mBottomBtnLyt = findViewById(R.id.lyt_bottom);

		callInfo = findViewById(R.id.active_call_info);

		mActiveCallHeader = findViewById(R.id.active_call);
		mNoCurrentCall = findViewById(R.id.no_current_call);
		mCallPaused = findViewById(R.id.remote_pause);

		mImgProfile = findViewById(R.id.profileImg);
		avatar_layout = findViewById(R.id.avatar_layout);

		findViewById(R.id.btn_hangup).setOnClickListener(this);
		findViewById(R.id.btn_switch_camera).setOnClickListener(this);

		LinphoneManager.getInstance().changeStatusToOnThePhone();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST_CAMERA:
				UIThreadDispatcher.dispatch(new Runnable() {
					@Override
					public void run() {
						acceptCallUpdate(true);
					}
				});
				break;
			case PERMISSIONS_ENABLED_CAMERA:
				UIThreadDispatcher.dispatch(new Runnable() {
					@Override
					public void run() {
						enabledOrDisabledVideo(false);
					}
				});
				break;
		}
		LinphonePreferences.instance().neverAskCameraPerm();
	}


	private void refreshIncallUi(){
		refreshInCallActions();
		refreshCallList(getResources());
		enableAndRefreshInCallActions();
	}

	private void refreshInCallActions() {
        mBtnSpeaker.setSelected(isSpeakerEnabled);
        mBtnMute.setSelected(isMicMuted);
	}

	private void enableAndRefreshInCallActions() {
		mBtnMute.setEnabled(true);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.btn_mute) {
			toggleMicro();
		} else if (id == R.id.btn_speaker) {
			toggleSpeaker();
		} else if (id == R.id.btn_hangup) {
			hangUp();
		} else if (id == R.id.btn_switch_camera) {
			if (videoCallFragment != null) {
				videoCallFragment.switchCamera();
			}
		}
	}

	private void enabledOrDisabledVideo(final boolean isVideoEnabled) {
		final LinphoneCall call = LinphoneManager.getLc().getCurrentCall();
		if (call == null) {
			return;
		}

		if (isVideoEnabled) {
			LinphoneCallParams params = call.getCurrentParamsCopy();
			params.setVideoEnabled(false);
			LinphoneManager.getLc().updateCall(call, params);
		} else {
			if (!call.getRemoteParams().isLowBandwidthEnabled()) {
				LinphoneManager.getInstance().addVideo();
			} else {
				displayCustomToast(getString(R.string.error_low_bandwidth), Toast.LENGTH_LONG);
			}
		}
	}

	public void displayCustomToast(final String message, final int duration) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastRoot));

		TextView toastText = layout.findViewById(R.id.toastMessage);
		toastText.setText(message);

		final Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(duration);
		toast.setView(layout);
		toast.show();
	}

	private void switchVideo(final boolean displayVideo) {
		final LinphoneCall call = LinphoneManager.getLc().getCurrentCall();
		if (call == null) {
			return;
		}

		//Check if the call is not terminated
		if(call.getState() == State.CallEnd || call.getState() == State.CallReleased) return;
		
		if (!displayVideo) {
			showAudioView();
		} else {
			if (!call.getRemoteParams().isLowBandwidthEnabled()) {
				LinphoneManager.getInstance().addVideo();
				if (videoCallFragment == null || !videoCallFragment.isVisible())
					showVideoView();
			} else {
				displayCustomToast(getString(R.string.error_low_bandwidth), Toast.LENGTH_LONG);
			}
		}
	}

	private void showAudioView() {
		replaceFragmentVideoByAudio();
		displayAudioCall();
		removeCallbacks();
		findViewById(R.id.txt_waiting_capture).setVisibility(View.GONE);
	}

	private void showVideoView() {
		refreshInCallActions();
		findViewById(R.id.txt_waiting_capture).setVisibility(View.VISIBLE);
		replaceFragmentAudioByVideo();
	}

	private void displayAudioCall(){
		mBottomBtnLyt.setVisibility(View.VISIBLE);
		mActiveCallHeader.setVisibility(View.VISIBLE);
		callInfo.setVisibility(View.VISIBLE);
		avatar_layout.setVisibility(View.VISIBLE);
		mBtnSwitchCamera.setVisibility(View.GONE);
	}

	private void replaceFragmentVideoByAudio() {
		audioCallFragment = new CallAudioFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragmentContainer, audioCallFragment);
		try {
			transaction.commitAllowingStateLoss();
		} catch (Exception e) {
		}
	}

	private void replaceFragmentAudioByVideo() {
//		Hiding controls to let displayVideoCallControlsIfHidden add them plus the callback
		videoCallFragment = new CallVideoFragment();

		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragmentContainer, videoCallFragment);
		try {
			transaction.commitAllowingStateLoss();
		} catch (Exception e) {
		}
	}

	private void toggleMicro() {
		isMicMuted = !isMicMuted;
		mBtnMute.setSelected(isMicMuted);
		LinphoneManager.getLc().muteMic(isMicMuted);
	}

	private void toggleSpeaker() {
		isSpeakerEnabled =! isSpeakerEnabled;
		mBtnSpeaker.setSelected(isSpeakerEnabled);
		if ( isSpeakerEnabled )
			LinphoneManager.getInstance().routeAudioToSpeaker();
		else
			LinphoneManager.getInstance().routeAudioToReceiver();
		LinphoneManager.getLc().enableSpeaker(isSpeakerEnabled);
	}

	private void hangUp() {
		LinphoneCore lc = LinphoneManager.getLc();
		LinphoneCall currentCall = lc.getCurrentCall();

		if (currentCall != null) {
			lc.terminateCall(currentCall);
		} else if (lc.isInConference()) {
			lc.terminateConference();
		} else {
			lc.terminateAllCalls();
		}
	}

	public void displayVideoCall(boolean display){
		avatar_layout.setVisibility(View.GONE);

		if(display) {
			mBottomBtnLyt.setVisibility(View.VISIBLE);
			mActiveCallHeader.setVisibility(View.VISIBLE);
			callInfo.setVisibility(View.VISIBLE);
			callsList.setVisibility(View.VISIBLE);
		} else {
			mBottomBtnLyt.setVisibility(View.GONE);
			callInfo.setVisibility(View.GONE);
			mActiveCallHeader.setVisibility(View.GONE);
			callsList.setVisibility(View.GONE);
		}
	}

	public void removeCallbacks() {
		if (mControlsHandler != null && mControls != null) {
			mControlsHandler.removeCallbacks(mControls);
		}
		mControls = null;
	}

	public void displayVideoCallControlsIfHidden() {
		if (mBottomBtnLyt != null) {
			if (mBottomBtnLyt.getVisibility() != View.VISIBLE) {
				displayVideoCall(true);
				resetControlsHidingCallBack();
			} else {
				displayVideoCall(false);
				if (mControlsHandler != null && mControls != null) {
					mControlsHandler.removeCallbacks(mControls);
				}
				mControls = null;
			}
		}
	}

	public void resetControlsHidingCallBack() {
		if (mControlsHandler != null && mControls != null) {
			mControlsHandler.removeCallbacks(mControls);
		}
		mControls = null;

		if (isVideoEnabled(LinphoneManager.getLc().getCurrentCall()) && mControlsHandler != null) {
			mControlsHandler.postDelayed(mControls = new Runnable() {
				public void run() {
					displayVideoCall(false);
				}
			}, SECONDS_BEFORE_HIDING_CONTROLS);
		}
	}

	public void acceptCallUpdate(boolean accept) {
		if (timer != null) {
			timer.cancel();
		}

		LinphoneCall call = LinphoneManager.getLc().getCurrentCall();
		if (call == null) {
			return;
		}

		LinphoneCallParams params = call.getCurrentParamsCopy();
		if (accept) {
			params.setVideoEnabled(true);
			LinphoneManager.getLc().enableVideo(true, true);
		}
		if (params.getVideoEnabled()) { //added by kimsui-20191001
			String custom_info = params.getCustomHeader("sentsize");
			String sent_info = "0";
			if (params.getSentVideoSize().width == 480 || params.getSentVideoSize().height == 480) {
				sent_info = "0";
			}
			else if (LinphoneManager.getLc().getPreferredFramerate() == 9) {
				sent_info = "1";
			}
			else {
				sent_info = "2";
			}

			if (!custom_info.equals(sent_info)) {
				if (Integer.parseInt(custom_info) > Integer.parseInt(sent_info)) {
					if (custom_info.equals("2")) {
						LinphoneManager.getLc().setPreferredVideoSizeByName("qvga");
						//LinphoneManager.getLc().setPreferredFramerate(10);
					}
					else if (custom_info.equals("1")) {
						LinphoneManager.getLc().setPreferredVideoSizeByName("qvga");
						//LinphoneManager.getLc().setPreferredFramerate(9);
					}

				}
			}
			params = call.getCurrentParamsCopy();
		}


		try {
			LinphoneManager.getLc().acceptCallUpdate(call, params);
		} catch (LinphoneCoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		instance = this;
		super.onResume();

		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {

			List<LinphoneCall> calls = LinphoneUtils.getLinphoneCalls(LinphoneManager.getLc());
			if (calls.size() < 1) {
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

			lc.addListener(mListener);
		}

		refreshIncallUi();
		handleViewIntent();

		if (!isSpeakerEnabled) {
			removeCallbacks();
		}
	}

	private void handleViewIntent() {
		Intent intent = getIntent();
		if(intent != null && intent.getAction() == "android.intent.action.VIEW") {
			LinphoneCall call = LinphoneManager.getLc().getCurrentCall();
			if(call != null && isVideoEnabled(call)) {
				LinphonePlayer player = call.getPlayer();
				String path = intent.getData().getPath();
				int openRes = player.open(path, new LinphonePlayer.Listener() {

					@Override
					public void endOfFile(LinphonePlayer player) {
						player.close();
					}
				});
				if(openRes == -1) {
					String message = "Could not open " + path;
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					return;
				}
				if(player.start() == -1) {
					player.close();
					String message = "Could not start playing " + path;
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	protected void onPause() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.removeListener(mListener);
		}

		super.onPause();

		if (mControlsHandler != null && mControls != null) {
			mControlsHandler.removeCallbacks(mControls);
		}
		mControls = null;
	}

	@Override
	protected void onDestroy() {
		LinphoneManager.getInstance().changeStatusToOnline();

		if (mControlsHandler != null && mControls != null) {
			mControlsHandler.removeCallbacks(mControls);
		}
		mControls = null;
		mControlsHandler = null;

		if (wakeLock != null) {
			if (wakeLock.isHeld()) {
				wakeLock.release();
			}
		}

		unbindDrawables(findViewById(R.id.topLayout));
		instance = null;
		super.onDestroy();
		System.gc();
	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ImageView) {
			view.setOnClickListener(null);
		}
		if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (LinphoneUtils.onKeyVolumeAdjust(keyCode)) return true;
		if (LinphoneUtils.onKeyBackGoHome(this, keyCode, event)) return true;
		return super.onKeyDown(keyCode, event);
	}

	public void bindAudioFragment(CallAudioFragment fragment) {
		audioCallFragment = fragment;
	}

	public void bindVideoFragment(CallVideoFragment fragment) {
		videoCallFragment = fragment;
	}

	private void displayNoCurrentCall(boolean display){
		if(!display) {
			mActiveCallHeader.setVisibility(View.VISIBLE);
			mNoCurrentCall.setVisibility(View.GONE);
		} else {
			mActiveCallHeader.setVisibility(View.GONE);
			mNoCurrentCall.setVisibility(View.VISIBLE);
		}
	}

	public void refreshCallList(Resources resources) {
		isConferenceRunning = LinphoneManager.getLc().isInConference();
		List<LinphoneCall> pausedCalls = LinphoneUtils.getCallsInState(LinphoneManager.getLc(), Arrays.asList(State.PausedByRemote));

		//MultiCalls
		if(LinphoneManager.getLc().getCallsNb() > 1){
			callsList.setVisibility(View.VISIBLE);
		}

		//Active call
		if (LinphoneManager.getLc().getCurrentCall() != null) {
			displayNoCurrentCall(false);
			if (isVideoEnabled(LinphoneManager.getLc().getCurrentCall()) && !isConferenceRunning && pausedCalls.size() == 0) {
				displayVideoCall(false);
			} else {
				displayAudioCall();
			}
		} else {
			showAudioView();
			displayNoCurrentCall(true);
			if (LinphoneManager.getLc().getCallsNb() == 1) {
				callsList.setVisibility(View.VISIBLE);
			}
		}

		if (callsList != null) {
			callsList.removeAllViews();
			int index = 0;

			if (LinphoneManager.getLc().getCallsNb() == 0) {
//				goBackToDialer();
				return;
			}

			boolean isConfPaused = false;
			for (LinphoneCall call : LinphoneManager.getLc().getCalls()) {
				if (call.isInConference() && !isConferenceRunning) {
					isConfPaused = true;
					index++;
				} else {
					if (call != LinphoneManager.getLc().getCurrentCall() && !call.isInConference()) {
//						displayPausedCalls(resources, call, index);
						index++;
					} else {
						displayCurrentCall(call);
					}
				}
			}

			if (!isConferenceRunning) {
				if (isConfPaused) {
					callsList.setVisibility(View.VISIBLE);
//					displayPausedCalls(resources, null, index);
				}
			}
		}

		//Paused by remote
		if (pausedCalls.size() == 1) {
			displayCallPaused(true);
		} else {
			displayCallPaused(false);
		}
	}

	private void displayCallPaused(boolean display){
		if(display){
			mCallPaused.setVisibility(View.VISIBLE);
		} else {
			mCallPaused.setVisibility(View.GONE);
		}
	}

	//CALL INFORMATION
	private void displayCurrentCall(LinphoneCall call){
		LinphoneAddress lAddress = call.getRemoteAddress();
		TextView contactName = (TextView) findViewById(R.id.current_contact_name);
		setContactInformation(contactName, lAddress);
		registerCallDurationTimer(null, call);
	}

	private void setContactInformation(TextView contactName, LinphoneAddress lAddress) {
		String useraddr = lAddress.getUserName() + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
		Contact contact = DatabaseUtils.getContactInfo(getContentResolver(), useraddr);
		if (contact == null) {
			contactName.setText(LinphoneUtils.getAddressDisplayName(lAddress));
			mImgProfile.setImageResource(R.drawable.profilephoto);
		} else {
			contactName.setText(contact.getName());
			GlobalFunc.showAvatar(this, useraddr, mImgProfile);
		}
		contactName.setSelected(true);
	}

	private void registerCallDurationTimer(View v, LinphoneCall call) {
		int callDuration = call.getDuration();
		if (callDuration == 0 && call.getState() != State.StreamsRunning) {
			return;
		}

		Chronometer timer = null;
		if (v == null) {
			timer = findViewById(R.id.current_call_timer);
		} else {
//			timer = v.findViewById(R.id.call_timer);
		}

		if (timer == null) {
			throw new IllegalArgumentException("no callee_duration view found");
		}

		timer.setBase(SystemClock.elapsedRealtime() - 1000 * callDuration);
		timer.start();
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

}