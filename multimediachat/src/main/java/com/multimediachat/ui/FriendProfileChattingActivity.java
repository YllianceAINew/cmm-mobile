package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.plugin.xmpp.XmppAddress;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.ChatRoomMediaUtil;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.jivesoftware.smack.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;

import java.io.File;
import java.util.ArrayList;


@SuppressLint("NewApi")
public class FriendProfileChattingActivity extends BaseActivity implements OnClickListener {

    private Context mContext = null;
    public static Contact contact;
    public static ArrayList<String> mTags = new ArrayList<>();
    private boolean mFlag;
    private final static int REQUEST_CODE_PHOTO = 52;
    String userAddr;
    String userNickname = "";
    long contactId = -1;

    MainProgress mProgressDlg;
    CircularImageView mImgProfile;

    ImageView mQrCodeView;
    Bitmap bmMyQRCode;

    TextView mTxtName;
    TextView mTxtChatId;
    TextView mTxtPhoneNum;
    LinearLayout mBtnMesssages;
    LinearLayout mBtnFreeCall;
    LinearLayout mBtnVideoCall;
    TextView mTxtTags;

    View lyt_tags;
    View separator_tags;

    String photoPath = "";
    Boolean avatar_existence = false;


    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.friendprofilechatting);
        setActionBarTitle(getString(R.string.title_friend_profile));
        mContext = this;
        mImgProfile = findViewById(R.id.imgProfile);
        mQrCodeView = findViewById(R.id.img_qrcode);
        mTxtName = findViewById(R.id.txt_friend_name);
        mTxtChatId = findViewById(R.id.txt_friend_chatid);
        mTxtPhoneNum = findViewById(R.id.txt_phone);
        mTxtTags = findViewById(R.id.txt_tags);
        mBtnMesssages = findViewById(R.id.btn_message);
        mBtnFreeCall = findViewById(R.id.btn_freecall);
        mBtnVideoCall = findViewById(R.id.btn_videocall);

        lyt_tags = findViewById(R.id.lyt_tags);
        separator_tags = findViewById(R.id.separator_tags);

        mQrCodeView.setOnClickListener(this);
        mBtnMesssages.setOnClickListener(this);
        mBtnFreeCall.setOnClickListener(this);
        mBtnVideoCall.setOnClickListener(this);

        if (contact == null) {
            userAddr = getIntent().getStringExtra("username");

            if (userAddr == null || userAddr.isEmpty()) {
                finish();
                return;
            }
        } else {
            userAddr = contact.getAddress().getAddress();
        }

        mProgressDlg = new MainProgress(this);
        mProgressDlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                if (arg2.getKeyCode() == KeyEvent.KEYCODE_BACK && arg2.getAction() == KeyEvent.ACTION_UP) {
                    onBackPressed();
                }
                return false;
            }
        });

        if (contact != null)
            updateButtons();

        contact = DatabaseUtils.getContactInfo(cr, userAddr);

        updateButtons();
        mTags = DatabaseUtils.getUserTags(cr, userAddr);
        Intent i = getIntent();
        mFlag = i.getBooleanExtra("mFlag", true);
        if (mFlag)
            GetFriendProfileTask();
        else {
            initData();
            mImgProfile.setOnClickListener(this);
        }

//        GlobalFunc.setProfileImage(mImgProfile, this, userAddr);

        registerReceiver(mDeletedContactReceiver, new IntentFilter(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD));
    }

    private BroadcastReceiver mDeletedContactReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(GlobalConstrants.DELETED_CONTACT_ADDRESS)) {
                if (userAddr.equals(intent.getStringExtra(GlobalConstrants.DELETED_CONTACT_ADDRESS))) {
                    finish();
                }
            }
        }
    };

    private void updateButtons() {
        if (contact != null && contact.sub_type != Imps.Contacts.SUBSCRIPTION_TYPE_BOTH) {
            findViewById(R.id.btn_message).setVisibility(View.GONE);
            findViewById(R.id.btn_freecall).setVisibility(View.GONE);
            findViewById(R.id.btn_videocall).setVisibility(View.GONE);
            findViewById(R.id.btn_add).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_add).setOnClickListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("SetTextI18n")
    void initData() {
        if (contact == null)
            return;

        String name = contact.getName();

        if (name != null) {
            mTxtName.setText(name);
            mTxtName.setSelected(true);
            userNickname = name;
        } else {
            mTxtName.setText("");
            userNickname = "";
        }

        GlobalFunc.showAvatar(this, userAddr, mImgProfile);
        GlobalFunc.showQRCode(userAddr, mQrCodeView);

        if (contact.sub_type != Imps.Contacts.SUBSCRIPTION_TYPE_BOTH) {
            if (mProgressDlg != null && mProgressDlg.isShowing() && !FriendProfileChattingActivity.this.isFinishing())
                mProgressDlg.dismiss();
        }

        mTxtChatId.setText(getString(R.string.user_id) + ": " + contact.userid);
        mTxtPhoneNum.setText(contact.getPhoneNum());
        updateTags();

    }

    private void updateTags() {
        if (mTags == null || mTags.size() == 0) {
            lyt_tags.setVisibility(View.GONE);
            separator_tags.setVisibility(View.GONE);
            mTxtTags.setText("");
        } else {
            lyt_tags.setVisibility(View.VISIBLE);
            separator_tags.setVisibility(View.VISIBLE);
            mTxtTags.setText(TextUtils.join( ", ", mTags));
            mTxtTags.setSelected(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_message:
                startChat();
                break;
            case R.id.btn_freecall:
                startDial();
                break;
            case R.id.btn_videocall:
                startVideoCall();
                break;
            case R.id.btn_add:
                if (Imps.Contacts.getReceivedRequestsCount(cr) > 0) {
                    if (GlobalFunc.isAlreadyReceivedFriendRequest(cr, contact.userid + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain))) {
                        GlobalFunc.showAlertDialog(this, getString(R.string.information), getString(R.string.already_received_friend_request));
                        break;
                    }
                }
                Intent intent = new Intent(this, FriendRequestActivity.class);
                FriendRequestActivity.contact = contact;
                startActivity(intent);
                finish();
                break;
            case R.id.imgProfile:
    			intent = new Intent(this, FriendProfileImageActivity.class);
    			intent.putExtra("address", userAddr);
    			startActivity(intent);
                break;

            case R.id.img_qrcode:
                intent = new Intent(this, MyQRCodeActivity.class);
                intent.putExtra("isFriend", true);
                intent.putExtra("address", userAddr);
                startActivity(intent);
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    private void startDial() {
        if (GlobalFunc.hasUploading(this).length() != 0) {
            GlobalFunc.showToast(this, getString(R.string.disable_call_in_uploading), true);
            return;
        }

        if (GlobalFunc.hasDownloading(this).length() != 0) {
            GlobalFunc.showToast(this, getString(R.string.disable_call_in_downloading), true);
            return;
        }

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showToast(this, R.string.network_error, false);
            return;
        }

        if (LinphoneManager.isInstanciated()) {
            if (LinphoneManager.getLc().getCallsNb() > 0) {
                GlobalFunc.showToast(this, R.string.disable_call, false);
                return;
            }
        }

        LinphonePreferences.instance().setInitiateVideoCall(false);
        String address = userAddr.substring(0, userAddr.indexOf('@'));
        ChatRoomActivity.isVideoCalling = false;
        Intent i = new Intent(this, OutgoingActivity.class);
        i.putExtra("VideoEnabled", false);
        i.putExtra("address", address);
        startActivity(i);
    }

    private void startVideoCall() {
        if (GlobalFunc.hasUploading(this).length() != 0) {
            GlobalFunc.showToast(this, getString(R.string.disable_call_in_uploading), true);
            return;
        }

        if (GlobalFunc.hasDownloading(this).length() != 0) {
            GlobalFunc.showToast(this, getString(R.string.disable_call_in_downloading), true);
            return;
        }

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showToast(this, R.string.network_error, false);
            return;
        }

        if (LinphoneManager.isInstanciated()) {
            if (LinphoneManager.getLc().getCallsNb() > 0) {
                GlobalFunc.showToast(this, R.string.disable_call, false);
                return;
            }
            else if ( LinphoneManager.getLc().getMaxCalls() == 0) {
                GlobalFunc.showToast(this, R.string.disable_ps_call, false);
                return;
            }
        }
        LinphonePreferences.instance().setInitiateVideoCall(true);
        String address = userAddr.substring(0, userAddr.indexOf('@'));
        ChatRoomActivity.isVideoCalling = true;
        Intent i = new Intent(this, OutgoingActivity.class);
        i.putExtra("VideoEnabled", true);
        i.putExtra("address", address);
        startActivity(i);
    }

    private void startChat() {
        IImConnection conn = mApp.getConnection(mPref.getLong(GlobalConstrants.store_picaProviderId, -1));

        if (conn != null) {
            try {

                IChatSessionManager manager = conn.getChatSessionManager();

                if (manager != null) {

                    IChatSession session = manager.getChatSession(userAddr);

                    if (session == null) {
                        session = manager.createChatSession(userAddr);
                    }
                    if (session != null) {
                        Intent i = new Intent(this, ChatRoomActivity.class);

                        if (contactId > 0)
                            i.putExtra("chatContactId", contactId);
                        else
                            i.putExtra("chatContactId", session.getId());

                        i.putExtra("contactName", userAddr);

                        String name = contact.getName();

                        i.putExtra("nickname", name);

                        i.putExtra("providerId", mPref.getLong(GlobalConstrants.store_picaProviderId, -1));

                        startActivity(i);

                        setResult(RESULT_OK);
                        finish();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unbindDrawables(View view) {
        if (view == null)
            return;
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            if (view instanceof AdapterView) {

            } else {
                ((ViewGroup) view).removeAllViews();
            }
        }
    }

    private void GetFriendProfileTask() {

        if (mProgressDlg != null) {
            mProgressDlg.setMessage(getString(R.string.loading));
            if (!mProgressDlg.isShowing() && !FriendProfileChattingActivity.this.isFinishing())
                mProgressDlg.show();
        }

        PicaApiUtility.getProfile(this, StringUtils.parseName(userAddr), new MyXMLResponseHandler() {
            @Override
            public void onMySuccess(JSONObject response) {
                mProgressDlg.dismiss();
                try {
                    if (contact == null) {
                        String nickName = null;
                        nickName = response.getString(Imps.Contacts.NICKNAME);
                        if (nickName == null)
                            nickName = "";

                        contact = new Contact(new XmppAddress(userAddr), nickName);
                        String gender = response.getString(Imps.Contacts.GENDER).equals("0") ? "Male" : "Female";
                        String region = response.getString(Imps.Contacts.REGION);
                        String status = response.getString(Imps.Contacts.STATUS);
                        String hash = response.getString(Imps.Contacts.HASH);
                        String phoneNumber = response.getString(Imps.Contacts.PHONE_NUMBER);

                        contact.setPhoneNum(phoneNumber);
                        contact.setProfile(status, gender, region);
                        contact.setHash(hash);
                        contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;
                        contact.userid = response.getString(Imps.Contacts.USERID);
                        contact.mFullName = nickName;
                    } else {
                        contact.userid = response.getString(Imps.Contacts.USERID);
                        contact.phoneNum = response.getString(Imps.Contacts.PHONE_NUMBER);
                        String region = response.getString(Imps.Contacts.REGION);
                        if (region == null)
                            contact.region = "";
                        else
                            contact.region = region;
                    }
                    initData();

                } catch (JSONException e) {
                    e.printStackTrace();
                    onMyFailure(-1);
                }
            }

            @Override
            public void onMyFailure(int errcode) {
                mProgressDlg.dismiss();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindDrawables(findViewById(R.id.rootView));
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(mDeletedContactReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        GlobalFunc.setProfileImage(mImgProfile, this, userAddr);

        if (!LinphoneManager.isInstanciated()) {
            finish();
        }
    }
/*
 * added by JHK(2019.11.07)
 * make thumbnail, update contacts table, reload contactlist
 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        String avatarPath = "";
        if (requestCode == REQUEST_CODE_PHOTO) {
            String samplePath = null;
            avatarPath = mContext.getFilesDir().getAbsolutePath() + "/" + GlobalConstrants.AVATAR_DIR_NAME + "/";
            File avatarDir = new File(avatarPath);
            if (!avatarDir.exists())
                avatarDir.mkdirs();
            if (avatar_existence) {
                samplePath = Imps.getAvatarPath(getContentResolver(), userAddr);
                File lastAvatar = new File(samplePath);
                if (lastAvatar.exists())
                    lastAvatar.delete();
            }
            samplePath = avatarPath + "thumbnail_" + System.currentTimeMillis() + ".jpg";
            ChatRoomMediaUtil.sampleImage(this.getResources(), photoPath, samplePath);

            File file = new File(photoPath);
            if (file.exists())
                file.delete();
            Imps.updateContactsInDb(this.getContentResolver(), userAddr, samplePath);
            Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
            mContext.sendBroadcast(intent);
        }
    }

}
