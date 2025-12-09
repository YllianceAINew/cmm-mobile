package org.linphone;

/*
 LinphoneActivity.java
 Copyright (C) 2012  Belledonne Communications, Grenoble, France

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

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.R;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.Reason;
import org.linphone.mediastream.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sylvain Berfini
 */
public class LinphoneActivity extends Activity implements ContactPicked, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String PREF_FIRST_LAUNCH = "pref_first_launch";
    private static final int SETTINGS_ACTIVITY = 123;
    private static final int FIRST_LOGIN_ACTIVITY = 101;
    private static final int REMOTE_PROVISIONING_LOGIN_ACTIVITY = 102;
    private static final int CALL_ACTIVITY = 19;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 200;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 201;
    private static final int PERMISSIONS_REQUEST_CAMERA = 202;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL = 203;

    private static LinphoneActivity instance;

    private TextView missedCalls, missedChats;
    private RelativeLayout contacts, history, dialer, chat;
    private View contacts_selected, history_selected, dialer_selected, chat_selected;
    private RelativeLayout mTopBar;
    private ImageView cancel;
    private FragmentsAvailable currentFragment, nextFragment;
    private List<FragmentsAvailable> fragmentsHistory;
    private Fragment dialerFragment, chatListFragment, historyListFragment, contactListFragment;
    private Fragment.SavedState dialerSavedState;
    private boolean newProxyConfig;
    private boolean isAnimationDisabled = true, preferLinphoneContacts = false, emptyFragment = false, permissionAsked = false;
    private OrientationEventListener mOrientationHelper;
    private LinphoneCoreListenerBase mListener;
    private LinearLayout mTabBar;

    private DrawerLayout sideMenu;
    private String[] sideMenuItems;
    private RelativeLayout sideMenuContent, quitLayout, defaultAccount;
    private ListView accountsList, sideMenuItemList;
    private ImageView menu;
    private Dialog authInfoPassword;

    static final boolean isInstanciated() {
        return instance != null;
    }

    public static final LinphoneActivity instance() {
        if (instance != null)
            return instance;
        throw new RuntimeException("LinphoneActivity not instantiated yet");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (!LinphoneManager.isInstanciated()) {
            Log.e("No service running: avoid crash by starting the launch", this.getClass().getName());
            finish();
            startActivity(getIntent().setClass(this, LinphoneLauncherActivity.class));
            return;
        }

        boolean useFirstLoginActivity = getResources().getBoolean(R.bool.display_account_wizard_at_first_start);
        if (LinphonePreferences.instance().isProvisioningLoginViewEnabled()) {
            Intent wizard = new Intent();
//			wizard.setClass(this, RemoteProvisioningLoginActivity.class);
            wizard.putExtra("Domain", LinphoneManager.getInstance().wizardLoginViewDomain);
            startActivityForResult(wizard, REMOTE_PROVISIONING_LOGIN_ACTIVITY);
        } else if (useFirstLoginActivity && LinphonePreferences.instance().isFirstLaunch() || LinphoneManager.getLc().getProxyConfigList().length == 0) {
            if (LinphonePreferences.instance().getAccountCount() > 0) {
                LinphonePreferences.instance().firstLaunchSuccessful();
            } else {
//				startActivityForResult(new Intent().setClass(this, AssistantActivity.class), FIRST_LOGIN_ACTIVITY);
            }
        }

        //TODO rework
        if (getResources().getBoolean(R.bool.use_linphone_tag) && getPackageManager().checkPermission(Manifest.permission.WRITE_SYNC_SETTINGS, getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            ContactsManager.getInstance().initializeSyncAccount(getApplicationContext(), getContentResolver());
        } else {
            ContactsManager.getInstance().initializeContactManager(getApplicationContext(), getContentResolver());
        }

        instance = this;
        fragmentsHistory = new ArrayList<FragmentsAvailable>();

        initButtons();

        currentFragment = nextFragment = FragmentsAvailable.DIALER;
        fragmentsHistory.add(currentFragment);

        mListener = new LinphoneCoreListenerBase() {
            @Override
            public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneChatMessage message) {
                if (!displayChatMessageNotification(message.getFrom().asStringUriOnly())) {
                    cr.markAsRead();
                }
                displayMissedChats(getUnreadMessageCount());
//		        if (chatListFragment != null && chatListFragment.isVisible()) {
//		            ((ChatListFragment) chatListFragment).refresh();
//		        }
            }

            @Override
            public void authInfoRequested(LinphoneCore lc, String realm, String username, String domain) {
                //authInfoPassword = displayWrongPasswordDialog(username, realm, domain);
                //authInfoPassword.show();
            }

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig proxy, LinphoneCore.RegistrationState state, String smessage) {
                if (state.equals(RegistrationState.RegistrationCleared)) {
                    if (lc != null) {
                        LinphoneAuthInfo authInfo = lc.findAuthInfo(proxy.getIdentity(), proxy.getRealm(), proxy.getDomain());
                        if (authInfo != null)
                            lc.removeAuthInfo(authInfo);
                    }
                }

                if (state.equals(RegistrationState.RegistrationFailed) && newProxyConfig) {
                    newProxyConfig = false;
                    if (proxy.getError() == Reason.BadCredentials) {
                        //displayCustomToast(getString(R.string.error_bad_credentials), Toast.LENGTH_LONG);
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
                if (state == State.IncomingReceived) {
                    if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
//                        startActivity(new Intent(LinphoneActivity.instance(), CallIncomingActivity.class));
                    } else {
                        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL);
                    }
                } else if (state == State.OutgoingInit || state == State.OutgoingProgress) {
                    if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
//                        startActivity(new Intent(LinphoneActivity.instance(), CallOutgoingActivity.class));
                    } else {
                        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO);
                    }
                } else if (state == State.CallEnd || state == State.Error || state == State.CallReleased) {
                    // Convert LinphoneCore message for internalization
                    if (message != null && call.getErrorInfo().getReason() == Reason.Declined) {
                        displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_SHORT);
                    } else if (message != null && call.getErrorInfo().getReason() == Reason.NotFound) {
                        displayCustomToast(getString(R.string.error_user_not_found), Toast.LENGTH_SHORT);
                    } else if (message != null && call.getErrorInfo().getReason() == Reason.Media) {
                        displayCustomToast(getString(R.string.error_incompatible_media), Toast.LENGTH_SHORT);
                    } else if (message != null && state == State.Error) {
                        displayCustomToast(getString(R.string.error_unknown) + " - " + message, Toast.LENGTH_SHORT);
                    }
                    resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
                }

                int missedCalls = LinphoneManager.getLc().getMissedCallsCount();
                displayMissedCalls(missedCalls);
            }
        };

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }

        int missedCalls = LinphoneManager.getLc().getMissedCallsCount();
        displayMissedCalls(missedCalls);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                rotation = 0;
                break;
            case Surface.ROTATION_90:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 180;
                break;
            case Surface.ROTATION_270:
                rotation = 270;
                break;
        }

        LinphoneManager.getLc().setDeviceRotation(rotation);
        mAlwaysChangingPhoneAngle = rotation;

        updateAnimationsState();
    }

    private void initButtons() {
//		mTabBar = (LinearLayout)  findViewById(R.id.footer);
//		mTopBar = (RelativeLayout) findViewById(R.id.top_bar);
//
//		cancel = (ImageView) findViewById(R.id.cancel);
//		cancel.setOnClickListener(this);
//
//		history = (RelativeLayout) findViewById(R.id.history);
//		history.setOnClickListener(this);
//		contacts = (RelativeLayout) findViewById(R.id.contacts);
//		contacts.setOnClickListener(this);
//		dialer = (RelativeLayout) findViewById(R.id.dialer);
//		dialer.setOnClickListener(this);
//		chat = (RelativeLayout) findViewById(R.id.chat);
//		chat.setOnClickListener(this);
//
//		history_selected = findViewById(R.id.history_select);
//		contacts_selected = findViewById(R.id.contacts_select);
//		dialer_selected = findViewById(R.id.dialer_select);
//		chat_selected = findViewById(R.id.chat_select);
//
//		missedCalls = (TextView) findViewById(R.id.missed_calls);
//		missedChats = (TextView) findViewById(R.id.missed_chats);
    }

    private boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    public void isNewProxyConfig() {
        newProxyConfig = true;
    }

    private void changeCurrentFragment(FragmentsAvailable newFragmentType, Bundle extras) {
    }

    private void updateAnimationsState() {
        isAnimationDisabled = getResources().getBoolean(R.bool.disable_animations) || !LinphonePreferences.instance().areAnimationsEnabled();
    }

    public boolean isAnimationDisabled() {
        return isAnimationDisabled;
    }

    private void changeFragment(Fragment newFragment, FragmentsAvailable newFragmentType, boolean withoutAnimation) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

		/*if (!withoutAnimation && !isAnimationDisabled && currentFragment.shouldAnimate()) {
            if (newFragmentType.isRightOf(currentFragment)) {
				transaction.setCustomAnimations(R.anim.slide_in_right_to_left,
						R.anim.slide_out_right_to_left,
						R.anim.slide_in_left_to_right,
						R.anim.slide_out_left_to_right);
			} else {
				transaction.setCustomAnimations(R.anim.slide_in_left_to_right,
						R.anim.slide_out_left_to_right,
						R.anim.slide_in_right_to_left,
						R.anim.slide_out_right_to_left);
			}
		}*/

        if (newFragmentType != FragmentsAvailable.DIALER
                || newFragmentType != FragmentsAvailable.CONTACTS_LIST
                || newFragmentType != FragmentsAvailable.CHAT_LIST
                || newFragmentType != FragmentsAvailable.HISTORY_LIST) {
            transaction.addToBackStack(newFragmentType.toString());
        }
        transaction.replace(R.id.fragmentContainer, newFragment, newFragmentType.toString());
        transaction.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();

        currentFragment = newFragmentType;
    }

    public void displayChatList() {
        Bundle extras = new Bundle();
        changeCurrentFragment(FragmentsAvailable.CHAT_LIST, extras);
    }

    public void displayContactsForEdition(String sipAddress) {
        Bundle extras = new Bundle();
        extras.putBoolean("EditOnClick", true);
        extras.putString("SipAddress", sipAddress);
        changeCurrentFragment(FragmentsAvailable.CONTACTS_LIST, extras);
    }

    public void displayAbout() {
        changeCurrentFragment(FragmentsAvailable.ABOUT, null);
    }

    public void displayAssistant() {
//		startActivity(new Intent(LinphoneActivity.this, AssistantActivity.class));
    }

    public boolean displayChatMessageNotification(String address) {
//		if(chatFragment != null) {
//			if(chatFragment.getSipUri().equals(address)) {
//				return false;
//			}
//		}
        return true;
    }

    public int getUnreadMessageCount() {
        int count = 0;
        LinphoneChatRoom[] chats = LinphoneManager.getLc().getChatRooms();
        for (LinphoneChatRoom chatroom : chats) {
            count += chatroom.getUnreadMessagesCount();
        }
        return count;
    }

    private void resetSelection() {
        history_selected.setVisibility(View.GONE);
        contacts_selected.setVisibility(View.GONE);
        dialer_selected.setVisibility(View.GONE);
        chat_selected.setVisibility(View.GONE);
    }

    public void hideTabBar(Boolean hide) {
        if (hide) {
            mTabBar.setVisibility(View.GONE);
        } else {
            mTabBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideTopBar() {
        mTopBar.setVisibility(View.GONE);
    }

    @SuppressWarnings("incomplete-switch")
    public void selectMenu(FragmentsAvailable menuToSelect) {
        currentFragment = menuToSelect;
        resetSelection();

        switch (menuToSelect) {
            case HISTORY_LIST:
            case HISTORY_DETAIL:
                history_selected.setVisibility(View.VISIBLE);
                break;
            case CONTACTS_LIST:
            case CONTACT_DETAIL:
            case CONTACT_EDITOR:
                contacts_selected.setVisibility(View.VISIBLE);
                break;
            case DIALER:
                dialer_selected.setVisibility(View.VISIBLE);
                break;
            case SETTINGS:
            case ACCOUNT_SETTINGS:
                hideTabBar(true);
                mTopBar.setVisibility(View.VISIBLE);
                break;
            case ABOUT:
                hideTabBar(true);
                break;
            case CHAT_LIST:
            case CHAT:
                chat_selected.setVisibility(View.VISIBLE);
                break;
        }
    }


    public void updateMissedChatCount() {
        displayMissedChats(getUnreadMessageCount());
    }

    public void displayMissedCalls(final int missedCallsCount) {
        if (missedCallsCount > 0) {
            missedCalls.setText(missedCallsCount + "");
            missedCalls.setVisibility(View.VISIBLE);
            if (!isAnimationDisabled) {
//				missedCalls.startAnimation(AnimationUtils.loadAnimation(LinphoneActivity.this, R.anim.bounce));
            }
        } else {
            LinphoneManager.getLc().resetMissedCallsCount();
            missedCalls.clearAnimation();
            missedCalls.setVisibility(View.GONE);
        }
    }

    private void displayMissedChats(final int missedChatCount) {
        if (missedChatCount > 0) {
            missedChats.setText(missedChatCount + "");
            missedChats.setVisibility(View.VISIBLE);
            if (!isAnimationDisabled) {
//				missedChats.startAnimation(AnimationUtils.loadAnimation(LinphoneActivity.this, R.anim.bounce));
            }
            if (missedChatCount > 99) {
                //TODO
            }
        } else {
            missedChats.clearAnimation();
            missedChats.setVisibility(View.GONE);
        }
    }


    public void displayCustomToast(final String message, final int duration) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastRoot));

        TextView toastText = (TextView) layout.findViewById(R.id.toastMessage);
        toastText.setText(message);

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    public Dialog displayDialog(String text) {
        Dialog dialog = new Dialog(this);
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		Drawable d = new ColorDrawable(getResources().getColor(R.color.colorC));
//		d.setAlpha(200);
//		dialog.setContentView(R.layout.dialog);
//		dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//		dialog.getWindow().setBackgroundDrawable(d);
//
//		TextView customText = (TextView) dialog.findViewById(R.id.customText);
//		customText.setText(text);
        return dialog;
    }

    @Override
    public void setAddresGoToDialerAndCall(String number, String name, Uri photo) {
//		Bundle extras = new Bundle();
//		extras.putString("SipUri", number);
//		extras.putString("DisplayName", name);
//		extras.putString("Photo", photo == null ? null : photo.toString());
//		changeCurrentFragment(FragmentsAvailable.DIALER, extras);

//		AddressType address = new AddressText(this, null);
//		address.setDisplayedName(name);
//		address.setText(number);
//		LinphoneManager.getInstance().newOutgoingCall(address);
    }

    public void setAddressAndGoToDialer(String number) {
        Bundle extras = new Bundle();
        extras.putString("SipUri", number);
        changeCurrentFragment(FragmentsAvailable.DIALER, extras);
    }

    @Override
    public void goToDialer() {
        changeCurrentFragment(FragmentsAvailable.DIALER, null);
    }

    public void startVideoActivity(LinphoneCall currentCall) {
//        Intent intent = new Intent(this, CallActivity.class);
//        intent.putExtra("VideoEnabled", true);
//        startOrientationSensor();
//        startActivityForResult(intent, CALL_ACTIVITY);
    }

    public void startIncallActivity(LinphoneCall currentCall) {
//        Intent intent = new Intent(this, CallActivity.class);
//        intent.putExtra("VideoEnabled", false);
//        startOrientationSensor();
//        startActivityForResult(intent, CALL_ACTIVITY);
    }

    public void sendLogs(Context context, String info) {
        final String appName = context.getString(R.string.app_name);

        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getString(R.string.about_bugreport_email)});
        i.putExtra(Intent.EXTRA_SUBJECT, appName + " Logs");
        i.putExtra(Intent.EXTRA_TEXT, info);
        i.setType("application/zip");

        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(ex);
        }
    }

    /**
     * Register a sensor to track phoneOrientation changes
     */
    private synchronized void startOrientationSensor() {
        if (mOrientationHelper == null) {
            mOrientationHelper = new LocalOrientationEventListener(this);
        }
        mOrientationHelper.enable();
    }

    private int mAlwaysChangingPhoneAngle = -1;

    private class LocalOrientationEventListener extends OrientationEventListener {
        public LocalOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(final int o) {
            if (o == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            int degrees = 270;
            if (o < 45 || o > 315)
                degrees = 0;
            else if (o < 135)
                degrees = 90;
            else if (o < 225)
                degrees = 180;

            if (mAlwaysChangingPhoneAngle == degrees) {
                return;
            }
            mAlwaysChangingPhoneAngle = degrees;

            int rotation = (360 - degrees) % 360;
            LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            if (lc != null) {
                lc.setDeviceRotation(rotation);
                LinphoneCall currentCall = lc.getCurrentCall();
                if (currentCall != null && currentCall.cameraEnabled() && currentCall.getCurrentParamsCopy().getVideoEnabled()) {
                    lc.updateCall(currentCall, null);
                }
            }
        }
    }

    private void initInCallMenuLayout(boolean callTransfer) {
        selectMenu(FragmentsAvailable.DIALER);
//		if (dialerFragment != null) {
//			((DialerFragment) dialerFragment).resetLayout(callTransfer);
//		}
    }

    public void resetClassicMenuLayoutAndGoBackToCallIfStillRunning() {
//		if (dialerFragment != null) {
//			((DialerFragment) dialerFragment).resetLayout(false);
//		}
//
//        if (LinphoneManager.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
//            LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
//            if (call.getState() == LinphoneCall.State.IncomingReceived) {
//                startActivity(new Intent(LinphoneActivity.this, CallIncomingActivity.class));
//            } else if (call.getCurrentParamsCopy().getVideoEnabled()) {
//                startVideoActivity(call);
//            } else {
//                startIncallActivity(call);
//            }
//        }
    }

    public FragmentsAvailable getCurrentFragment() {
        return currentFragment;
    }

//	public ChatStorage getChatStorage() {
//		return ChatStorage.getInstance();
//	}

    public void addContact(String displayName, String sipUri) {
        Bundle extras = new Bundle();
        extras.putSerializable("NewSipAdress", sipUri);
        changeCurrentFragment(FragmentsAvailable.CONTACT_EDITOR, extras);
    }

    public void editContact(Contact contact) {
        Bundle extras = new Bundle();
        extras.putSerializable("Contact", contact);
        changeCurrentFragment(FragmentsAvailable.CONTACT_EDITOR, extras);

    }

    public void editContact(Contact contact, String sipAddress) {

        Bundle extras = new Bundle();
        extras.putSerializable("Contact", contact);
        extras.putSerializable("NewSipAdress", sipAddress);
        changeCurrentFragment(FragmentsAvailable.CONTACT_EDITOR, extras);
    }

    public void quit() {
        finish();
        stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_FIRST_USER && requestCode == SETTINGS_ACTIVITY) {
            if (data.getExtras().getBoolean("Exit", false)) {
                quit();
            } else {
                FragmentsAvailable newFragment = (FragmentsAvailable) data.getExtras().getSerializable("FragmentToDisplay");
                selectMenu(newFragment);
            }
        } else if (resultCode == Activity.RESULT_FIRST_USER && requestCode == CALL_ACTIVITY) {
            getIntent().putExtra("PreviousActivity", CALL_ACTIVITY);
            boolean callTransfer = data == null ? false : data.getBooleanExtra("Transfer", false);
            boolean chat = data == null ? false : data.getBooleanExtra("chat", false);
            if (chat) {
                displayChatList();
            }
            if (LinphoneManager.getLc().getCallsNb() > 0) {
                initInCallMenuLayout(callTransfer);
            } else {
                resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        getIntent().putExtra("PreviousActivity", 0);
        super.onPause();
    }

    public void checkAndRequestPermission(String permission, int result) {
        if (getPackageManager().checkPermission(permission, getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission) && !permissionAsked) {
                permissionAsked = true;
                if (LinphonePreferences.instance().shouldInitiateVideoCall() ||
                        LinphonePreferences.instance().shouldAutomaticallyAcceptVideoRequests()) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, permission}, result);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, result);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
//                startActivity(new Intent(this, CallOutgoingActivity.class));
                LinphonePreferences.instance().neverAskAudioPerm();
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL:
//                startActivity(new Intent(this, CallIncomingActivity.class));
                LinphonePreferences.instance().neverAskAudioPerm();
                break;
        }
        permissionAsked = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!LinphoneService.isReady()) {
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }

        if (getPackageManager().checkPermission(Manifest.permission.READ_CONTACTS, getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            ContactsManager.getInstance().enabledContactsAccess();
            ContactsManager.getInstance().prepareContactsInBackground();
        } else {
            checkAndRequestPermission(Manifest.permission.READ_CONTACTS, PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        updateMissedChatCount();

        displayMissedCalls(LinphoneManager.getLc().getMissedCallsCount());

        LinphoneManager.getInstance().changeStatusToOnline();

        if (getIntent().getIntExtra("PreviousActivity", 0) != CALL_ACTIVITY) {
            if (LinphoneManager.getLc().getCalls().length > 0) {
                LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
                LinphoneCall.State callState = call.getState();
                if (callState == State.IncomingReceived) {
                    if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
//                        startActivity(new Intent(this, CallIncomingActivity.class));
                    } else {
                        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL);
                    }
                } else if (callState == State.OutgoingInit || callState == State.OutgoingProgress || callState == State.OutgoingRinging) {
                    if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
//                        startActivity(new Intent(this, CallOutgoingActivity.class));
                    } else {
                        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO);
                    }
                } else {
                    if (call.getCurrentParamsCopy().getVideoEnabled()) {
                        startVideoActivity(call);
                    } else {
                        startIncallActivity(call);
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mOrientationHelper != null) {
            mOrientationHelper.disable();
            mOrientationHelper = null;
        }

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }

        instance = null;
        super.onDestroy();

        unbindDrawables(findViewById(R.id.topLayout));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view != null && view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        if (extras != null && extras.getBoolean("GoToChat", false)) {
            LinphoneService.instance().removeMessageNotification();
            String sipUri = extras.getString("ChatContactSipUri");
        } else if (extras != null && extras.getBoolean("Notification", false)) {
            if (LinphoneManager.getLc().getCallsNb() > 0) {
                LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
                if (call.getCurrentParamsCopy().getVideoEnabled()) {
                    startVideoActivity(call);
                } else {
                    startIncallActivity(call);
                }
            }
        } else {
//			if (dialerFragment != null) {
//				if (extras != null && extras.containsKey("SipUriOrNumber")) {
//					if (getResources().getBoolean(R.bool.automatically_start_intercepted_outgoing_gsm_call)) {
//						((DialerFragment) dialerFragment).newOutgoingCall(extras.getString("SipUriOrNumber"));
//					} else {
//						((DialerFragment) dialerFragment).displayTextInAddressBar(extras.getString("SipUriOrNumber"));
//					}
//				} else {
//					((DialerFragment) dialerFragment).newOutgoingCall(intent);
//				}
//			}
            if (LinphoneManager.getLc().getCalls().length > 0) {
                LinphoneCall calls[] = LinphoneManager.getLc().getCalls();
                if (calls.length > 0) {
                    LinphoneCall call = calls[0];

                    if (call != null && call.getState() != LinphoneCall.State.IncomingReceived) {
                        if (call.getCurrentParamsCopy().getVideoEnabled()) {
                            //startVideoActivity(call);
                        } else {
                            //startIncallActivity(call);
                        }
                    }
                }

                // If a call is ringing, start incomingcallactivity
                Collection<LinphoneCall.State> incoming = new ArrayList<LinphoneCall.State>();
                incoming.add(LinphoneCall.State.IncomingReceived);
                if (LinphoneUtils.getCallsInState(LinphoneManager.getLc(), incoming).size() > 0) {
//                    if (CallActivity.isInstanciated()) {
//                        CallActivity.instance().startIncomingCallActivity();
//                    } else {
//                        if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
//                            startActivity(new Intent(this, CallIncomingActivity.class));
//                        } else {
//                            checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL);
//                        }
//                    }
                }
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentFragment == FragmentsAvailable.DIALER
                    || currentFragment == FragmentsAvailable.CONTACTS_LIST
                    || currentFragment == FragmentsAvailable.HISTORY_LIST
                    || currentFragment == FragmentsAvailable.CHAT_LIST) {
                boolean isBackgroundModeActive = LinphonePreferences.instance().isBackgroundModeEnabled();
                if (!isBackgroundModeActive) {
                    stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
                    finish();
                } else if (LinphoneUtils.onKeyBackGoHome(this, keyCode, event)) {
                    return true;
                }
            } else {
                if (isTablet()) {
                    if (currentFragment == FragmentsAvailable.SETTINGS) {
                        updateAnimationsState();
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}

interface ContactPicked {
    void setAddresGoToDialerAndCall(String number, String name, Uri photo);

    void goToDialer();
}
