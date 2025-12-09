package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.MediaController;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.SimpleAlertHandler;
import com.multimediachat.app.im.IContactListManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.ConnectionState;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.service.StatusBarNotifier;
import com.multimediachat.ui.dialog.CustomDialog;
import com.multimediachat.ui.dialog.FileSaveDialog;
import com.multimediachat.ui.views.ChatView;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.ui.views.MessageView;
import com.multimediachat.ui.views.QuickActionPopup;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.datamodel.MessageItem;

import org.jivesoftware.smack.util.StringUtils;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.UIThreadDispatcher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.multimediachat.global.GlobalFunc.addUploadList;

public class ChatRoomActivity extends BaseActivity
        implements OnClickListener, NotificationCenter.NotificationCenterDelegate {
    public final static int REQUEST_CODE_CAMERA = 51;
    public final static int REQUEST_CODE_PHOTO = 52;
    public final static int REQUEST_CODE_VIDEO = 53;
    public final static int REQUEST_EDIT_MESSAGE = 59;

    private static ChatRoomActivity instance = null;

    public static ChatView m_sChatView = null;

    private final int CONFIRM_LEAVE = 1;

    public static boolean isVideoCalling = false; //use this value instead of call.getCurrentParamsCopy().getVideoEnabled() because that's not working correctly.

    public ChatView mChatView;
    public String contactName;
    public long chatContactId;
    String nickName;
    long providerId;
    public int isPlusFriend = 0;
    public int mScrollPos = -1;

    private MyHandler mServiceConnectionHandler;

    public static boolean is_group_member_edit_mode = false;

    LinearLayout sendBarLayout;

    public boolean isbackground = false;

    private long chatSessionId;

    private QuickActionPopup menuOptionQuickActionPopup = null;


    private Dialog mStatusDialog = null;
    private ImageView mStatusBar = null;
    private TextView mStatusContent = null;


    private int mXmppStatus = 0;
    private int mSipStatus = 0;
    private Thread mFlashThread = null;
    private boolean mFlashFlag = true;

    public int touchPos[] = new int[2];

    public boolean isSelecteMode = false;
    public ArrayList<MessageItem> selectedMessages = new ArrayList<MessageItem>();
    private boolean mMenuAllState = false;
    private boolean mMenuNoneState = false;
    private boolean mMenuMultiSelectState = false;
    private Menu mOptionMenu = null;
    private static final int ID_LEAVE_ROOM = 1;
    private static final int ID_MULTI_DELETE = 100;
    private static final int ID_SELECT_ALL = 101;
    private static final int ID_UNSELECT_ALL = 102;

    public LinearLayout mLytDeleteSel;

    private DrawerLayout drawerLayout = null;
    private ListView mGroupList = null;
    private GroupListAdapter mGroupAdapter = null;
    private GroupListCallback mGroupCallback = null;
    public int isGroupChat = 0;

    private boolean fileCopying = true;

    private CircularImageView mImgProfile = null;
    private TextView mTxtMyInfo = null;
    private ImageView mImgStatus = null;
    private TextView mTxtStatus = null;

    private ImageView mBtnSelectAll;

    @SuppressLint("HandlerLeak")
    final class MyHandler extends SimpleAlertHandler {
        public MyHandler(ChatRoomActivity activity) {
            super(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ImApp.EVENT_SERVICE_CONNECTED) {
                ((ChatRoomActivity) mActivity).onServiceConnected();
                return;
            }
            super.handleMessage(msg);
        }
    }

    public static final ChatRoomActivity instance() {
        if (instance != null)
            return instance;
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chat_room_activity);
        findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.onBackPressed();
            }
        });


		/*show login status*/
        mStatusDialog = GlobalFunc.createDialog(this, R.layout.status_dialog, true);
        mStatusContent = mStatusDialog.findViewById(R.id.content);

        mStatusDialog.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusDialog.dismiss();
            }
        });

        mStatusBar = findViewById(R.id.status_bar);
        mStatusBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusDialog.show();
            }
        });

        registerReceiver(mConnStateChangeListener, new IntentFilter(GlobalConstrants.INTENT_CONNSTAT_CHANGED));
        registerReceiver(mDeletedContactReceiver, new IntentFilter(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD));

        findViewById(R.id.bgMain).setBackgroundResource(0);

        Intent intent = getIntent();
        if (intent == null)
            return;

        instance = this;
        chatContactId = intent.getLongExtra("chatContactId", -1);
        contactName = intent.getStringExtra("contactName");
        nickName = intent.getStringExtra("nickname");
        providerId = intent.getLongExtra("providerId", -1);
        mScrollPos = intent.getIntExtra("scrollPos", -1);
        isGroupChat = intent.getIntExtra("isGroupChat", 0);

        initLayout();

        is_group_member_edit_mode = false;

        mServiceConnectionHandler = new MyHandler(this);
        mApp.registerForBroadcastEvent(ImApp.EVENT_SERVICE_CONNECTED, mServiceConnectionHandler);

        mChatView = (ChatView) findViewById(R.id.chatView);
        m_sChatView = mChatView;
        mChatView.bindChat(chatContactId, isPlusFriend);

        updateTitle();

        drawerLayout = findViewById(R.id.drawer_layout);

        mImgProfile = drawerLayout.findViewById(R.id.img_chat_more_profile);
        mTxtMyInfo = drawerLayout.findViewById(R.id.txt_chat_more_my_info);

        mImgStatus = findViewById(R.id.img_chat_more_status);
        mTxtStatus = findViewById(R.id.txt_chat_more_status);

        drawerLayout.findViewById(R.id.lyt_chat_more_voice_call).setOnClickListener(this);
        drawerLayout.findViewById(R.id.lyt_chat_more_video_call).setOnClickListener(this);
        drawerLayout.findViewById(R.id.lyt_chat_more_camera).setOnClickListener(this);
        drawerLayout.findViewById(R.id.lyt_chat_more_file).setOnClickListener(this);
        drawerLayout.findViewById(R.id.lyt_chat_more_setting).setOnClickListener(this);
        drawerLayout.findViewById(R.id.lyt_chat_more_help).setOnClickListener(this);
        drawerLayout.findViewById(R.id.lyt_chat_more_select).setOnClickListener(this);
        drawerLayout.findViewById(R.id.lyt_chat_more_leave).setOnClickListener(this);

        Map<String, String> profileInfo = DatabaseUtils.getUserInfo(cr);
        if (profileInfo != null) {
            mTxtMyInfo.setText(String.format("%s (%s)", profileInfo.get(Imps.Profile.NICKNAME), profileInfo.get(Imps.Profile.USERID)));
            GlobalFunc.showAvatar(this, profileInfo.get(Imps.Profile.USERID), mImgProfile);
        }

        if (isGroupChat == 0) {
            if (nickName != null && !nickName.equals(mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain))) {
                addImageButton(R.drawable.hm_voicecall, R.id.btn_voice_call, POS_RIGHT);
                // addImageButton(R.drawable.hm_videocall, R.id.btn_video_call, POS_RIGHT);
            }
        } else {
            findViewById(R.id.group_list_pane).setVisibility(VISIBLE);

            findViewById(R.id.lyt_chat_more_voice_call).setVisibility(View.GONE);
            findViewById(R.id.lyt_chat_more_video_call).setVisibility(View.GONE);
            findViewById(R.id.lyt_chat_more_camera).setVisibility(View.GONE);
            findViewById(R.id.lyt_chat_more_file).setVisibility(View.GONE);

            mChatView.mBtnVoiceMessage.setAlpha((float) 0.7);
            mChatView.mBtnVoiceMessage.setOnClickListener(null);

            mGroupList = findViewById(R.id.group_list);
            mGroupAdapter = new GroupListAdapter(this, R.layout.group_list_item);
            mGroupList.setAdapter(mGroupAdapter);
            mGroupCallback = new GroupListCallback();
            getLoaderManager().initLoader(3333, null, mGroupCallback);
            addImageButton(R.drawable.group_list_n, R.id.btn_group_list, POS_RIGHT);
            insertChatNotExist((int) chatContactId);
        }

        if (isGroupChat == Imps.Chats.GROUP_LEAVE)
            mChatView.hideSendBar();

        addImageButton(R.drawable.hm_listmenu, R.id.btn_more_setting, POS_RIGHT);

        if (isGroupChat == Imps.Chats.GROUP_LIVE) {
            findViewById(R.id.lyt_chat_more_leave).setVisibility(View.VISIBLE);
        }

        mBtnSelectAll = findViewById(R.id.btn_select_all);
        mBtnSelectAll.setOnClickListener(this);

        findViewById(R.id.lyt_delete_sel).setOnClickListener(this);
        findViewById(R.id.btn_cancel_sel).setOnClickListener(this);

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chat_photos_and_videos_selected);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chat_list_update);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.group_room_leave);

        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);

        flashStatusLed();
    }

    public String getToUserAddress() {
        StringBuilder ret = new StringBuilder();

        if (isGroupChat != 0) {
            Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, chatContactId);
            Cursor cursor = null;

            try {
                cursor = cr.query(uri,
                        new String[]{Imps.GroupMembers.NICKNAME, Imps.GroupMembers.USERNAME,
                                Imps.GroupMembers.TYPE},
                        null, null, null);

                String username = mPref.getString("username", "");

                while (cursor.moveToNext()) {
                    String address = StringUtils.parseName(cursor
                            .getString(cursor.getColumnIndexOrThrow(Imps.GroupMembers.USERNAME)));
                    if (address.equals(username)) //if it's me
                        continue;
                    if (!ret.toString().isEmpty())
                        ret.append(",");
                    ret.append(address);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
                cursor = null;
            }
        } else {
            ret.append(StringUtils.parseName(contactName));
        }

        return ret.toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLastPhoto != null) {
            outState.putString("cameraImageUri", mLastPhoto.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            mLastPhoto = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }

    private void initLayout() {
        sendBarLayout = findViewById(R.id.sendbar_layout);
        if (nickName != null && nickName.equals(getString(R.string.chat_list_picatalk_team_name))) {
            sendBarLayout.setVisibility(View.GONE);
        }

    }

    public void updateTitle() {
        try {
            /*if (nickName != null && nickName.contains("@")) {
                setActionBarTitle(nickName.substring(0, nickName.lastIndexOf("@")));
            } else {*/
                if (nickName != null && nickName.equals(mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain))) {
                    setActionBarTitle(getString(R.string.chat_list_picatalk_team_name));
                } else {
                    setActionBarTitle(nickName);
                }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showConfirmDlg() {

        if (mChatView.mComposeMessage != null) {
            String str = mChatView.mComposeMessage.getText().toString();
            if (!str.trim().isEmpty()) {
                final CustomDialog dlg = new CustomDialog(instance, getString(R.string.information), getString(R.string.draft_remove), getString(R.string.ok), getString(R.string.cancel));
                dlg.setOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });
                dlg.setOnOKClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        m_sChatView = null;
                        instance = null;
                        ChatRoomActivity.super.onBackPressed();
                        dlg.dismiss();
                    }
                });
                dlg.show();
            } else {
                m_sChatView = null;
                instance = null;
                ChatRoomActivity.super.onBackPressed();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_more_setting:
                AndroidUtility.hideKeyboard(mChatView);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drawerLayout.openDrawer(GravityCompat.END);
                break;

            case R.id.btn_select_all:
                mBtnSelectAll.setSelected(!mBtnSelectAll.isSelected());
                if (mBtnSelectAll.isSelected())
                    selectAll();
                else
                    unSelectAll();
                break;
            case R.id.btn_cancel_sel:
                onBackPressed();
                break;
            case R.id.lyt_delete_sel:
                if (selectedMessages.size() > 0) {
                    deleteMessages();
                }
                break;
            case R.id.lyt_chat_more_leave:
                doLeave();
                break;
            case R.id.btn_group_list:
                AndroidUtility.hideKeyboard(mChatView);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.btn_voice_call:
            case R.id.lyt_chat_more_voice_call:
                startChooseVoice();
                break;
            case R.id.btn_video_call:
            case R.id.lyt_chat_more_video_call:
                startChooseVideo();
                break;
            case R.id.lyt_chat_more_camera:
                startCameraApp();
                break;
            case R.id.lyt_chat_more_file:
                startChooseFile();
                break;
            case R.id.lyt_chat_more_setting:
                startActivity(new Intent(this, ChatRoomSettingActivity.class));
                break;
            case R.id.lyt_chat_more_help:
                startActivity(new Intent(this, WebActivity.class));
                break;
            case R.id.lyt_chat_more_select:
                AndroidUtility.hideKeyboard(mChatView);
                isSelecteMode = true;
                showSelectionBar();
                mChatView.mSendbarLayout.setVisibility(GONE);
                mLytDeleteSel = findViewById(R.id.lyt_delete_sel);
                mLytDeleteSel.setEnabled(false);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                break;
            default:
                super.onClick(v);
                break;
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
    }

    void doLeave() {
        showConfirmDialog(CONFIRM_LEAVE);
    }

    public void sendVoiceMsg(Uri uri) {
        addUploadList((int) getChatId(), uri, "audio/4", getToUserAddress());
    }

    public void resendMessage(String packetId, String message) {
        Imps.updateMessageTypeInDb(getContentResolver(), packetId, Imps.MessageType.POSTPONED);
        Imps.updateOperMessageError(getContentResolver(), packetId, 0);
        Imps.updateMessageTimeInDb(getContentResolver(), packetId, System.currentTimeMillis());
        mChatView.sendMessage(message, packetId);
    }

    private static Uri mLastPhoto = null;

    public void onServiceConnected() {
        mChatView.onServiceConnected();
    }

    public void refreshChatList() {
        mChatView.updateChatList();
    }

    public void copyMessage(String message) {
        mChatView.copyMessage(message);
    }

    public void deleteMessage(String packetId) {
        mChatView.deleteMessage(packetId);
    }

    public void quoteMessage(String message) {
        try {
            mChatView.quoteMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getChatId() {
        return mChatView.getChatId();
    }

    @Override
    public void onBackPressed() {
        if (isSelecteMode) {
            isSelecteMode = false;
            selectedMessages.clear();
            if (isGroupChat != Imps.Chats.GROUP_LEAVE)
                mChatView.mSendbarLayout.setVisibility(VISIBLE);

            hideSelectionBar();

            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
            return;
        }

        InputMethodManager ime = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (ime.hideSoftInputFromWindow(mChatView.mComposeMessage.getWindowToken(), 0))
            return;

        showConfirmDlg();

    }

    @Override
    public void onPause() {
        super.onPause();
        isbackground = true;
        MessageView.stopUpDownProgressThread();
        if (mChatView != null) {
            mChatView.cancelRecording();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mChatView.stopListening();

        if (mChatView.recorder != null) {
            mChatView.recorder.stop();
            mChatView.recorder.release();
            mChatView.recorder = null;
        }
        mChatView.mRecordingHandler.removeMessages(0);

        try {
            unbindDrawables(findViewById(R.id.layout_content));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mApp.unregisterForBroadcastEvent(ImApp.EVENT_SERVICE_CONNECTED, mServiceConnectionHandler);
        MessageView.mPlayingVoicePacketId = null;
        if (MessageView.mVoicePlayer != null) {
            MessageView.mVoicePlayer.stop();
            MessageView.mVoicePlayer.release();
            MessageView.mVoicePlayer = null;
        }

        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chat_photos_and_videos_selected);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chat_list_update);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.group_room_leave);
        unregisterReceiver(mConnStateChangeListener);
        unregisterReceiver(mDeletedContactReceiver);
    }

    private void unbindDrawables(View view) {
        if (view == null)
            return;
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }

            ((ViewGroup) view).removeAllViews();
        }
    }

    public boolean showChat(long requestedChatId, String requestedUsername, long requestedProviderId, String nickName) {
        finish();

        Intent i = new Intent(MainTabNavigationActivity.instance(), ChatRoomActivity.class);

        i.putExtra("chatContactId", requestedChatId);

        i.putExtra("nickname", nickName);

        i.putExtra("contactName", requestedUsername);

        i.putExtra("providerId", requestedProviderId);

        i.putExtra("isGroupChat", 1);

        startActivity(i);

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_CODE_CAMERA) {
            String photoPath = data.getStringExtra("path");
            boolean isVideo = data.getBooleanExtra("isVideo", false);
            if (isVideo) {
                try {
                    chatSessionId = mChatView.getChatSession().getId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GlobalFunc.compressVideo(chatSessionId, photoPath);
            } else {
                GlobalFunc.compressImage((int) getChatId(), photoPath);
            }

        }  else if (requestCode == REQUEST_EDIT_MESSAGE) {
            String packetId = data.getStringExtra("packetId");
            String message = data.getStringExtra("message");
            mChatView.editMessage(packetId, message);
        }
    }

    public void showConfirmDialog(int res) {
        final Dialog dlg = GlobalFunc.createDialog(this, R.layout.confirm_dialog, true);

        TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
        title.setText(R.string.information);

        TextView content = (TextView) dlg.findViewById(R.id.msgcontent);

        Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
        Button dlg_btn_cancel = (Button) dlg.findViewById(R.id.btn_cancel);
        dlg_btn_cancel.setVisibility(VISIBLE);

        dlg_btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        String contentStr = "";
        if (res == CONFIRM_LEAVE) {
            dlg_btn_ok.setText(R.string.yes);
            dlg_btn_cancel.setText(R.string.no);
            title.setText(R.string.str_leave);

            contentStr = getResources().getString(R.string.confirm_out_room, nickName);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            SpannableString spannable = new SpannableString(contentStr);

            if (contentStr.contains("\"")) {
                spannable.setSpan(new ForegroundColorSpan(r.getColor(R.color.confirm_dlg_contact_color)),
                        contentStr.indexOf("\""), contentStr.lastIndexOf("\"") + 1, 0);
                spannable.setSpan(new RelativeSizeSpan(1.3f), contentStr.indexOf("\""),
                        contentStr.lastIndexOf("\"") + 1, 0);
                builder.append(spannable);
                content.setText(builder, BufferType.SPANNABLE);
            } else
                content.setText(contentStr);

            dlg_btn_ok.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    IImConnection conn = mApp.getConnection(mChatView.getProviderId());
                    if (conn == null)
                        return;

                    try {
                        if (mChatView.getChatSession().leave() != ImErrorInfo.NO_ERROR)
                            Toast.makeText(instance, R.string.group_leave_error, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                    }

                    if (!ChatRoomActivity.this.isFinishing()) {
                        try {
                            dlg.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        m_sChatView = null;
                        instance = null;
                        finish();
                    }
                }
            });
        }

        dlg.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    unbindDrawables(dlg.findViewById(R.id.rootView));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        content.setText(contentStr);
        dlg.setCanceledOnTouchOutside(false);

        if (this != null && !this.isFinishing()) {
            dlg.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!LinphoneManager.isInstanciated()) {
            finish();
            instance = null;
        }

        isbackground = false;

        if (mChatView != null)
            mChatView.sendMessagesSeen();

        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(StatusBarNotifier.chat_notify_start_id + (int) chatContactId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (chatContactId != 0) {
                    try {
                        if (Imps.Notifications.removeNotificationCount(cr, Imps.Notifications.CAT_CHATTING,
                                Imps.Notifications.FIELD_CHAT, (int) chatContactId) > 0) {
                            Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
                            sendBroadcast(intent);
                        }

                        IImConnection conn = ImApp.getInstance()
                                .getConnection(mPref.getLong(GlobalConstrants.store_picaProviderId, -1));
                        if (conn != null) {
                            try {
                                IContactListManager contactListManager = conn.getContactListManager();

                                Contact contact = contactListManager.getContact(contactName);

                                conn.notifyContactsPresenceUpdated(contact);
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }
        }).start();
        MessageView.startUpDownProgressThread();

        updateLoginStatus(GlobalFunc.getSipConnStatus(), GlobalFunc.getXmppConnStatus());
    }

    private BroadcastReceiver mConnStateChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int sipStatus = GlobalFunc.getSipConnStatus();
            int xmppStatus = GlobalFunc.getXmppConnStatus();
            updateLoginStatus(sipStatus, xmppStatus);
        }
    };

    private BroadcastReceiver mDeletedContactReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(GlobalConstrants.DELETED_CONTACT_ADDRESS)) {
                if (getToUserAddress().equals(intent.getStringExtra(GlobalConstrants.DELETED_CONTACT_ADDRESS))) {
                    finish();
                }
            }
        }
    };

    @Override
    public void didReceivedNotification(int id, final Object... args) {
        if (id == NotificationCenter.chat_photos_and_videos_selected) {
            AndroidUtility.RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<MediaController.PhotoEntry> pathList = (ArrayList<MediaController.PhotoEntry>) args[0];
                    if (pathList != null) {
                        for (int i = 0; i < pathList.size(); i++) {
                            MediaController.PhotoEntry photoEntry = pathList.get(i);
                            if (photoEntry.mediaType == MediaController.MediaType.PHOTO)
                                addUploadList((int) getChatId(), Uri.parse(photoEntry.path), "image/1", getToUserAddress());
                            else if (photoEntry.mediaType == MediaController.MediaType.VIDEO)
                                addUploadList((int) getChatId(), Uri.parse(photoEntry.path), "video/3", getToUserAddress());
                            else if (photoEntry.mediaType == MediaController.MediaType.AUDIO)
                                addUploadList((int) getChatId(), Uri.parse(photoEntry.path), "audio/2", getToUserAddress());
                        }
                    }
                }
            });
        } else if (id == NotificationCenter.chat_list_update) {
            AndroidUtility.RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mChatView != null)
                        mChatView.requeryCursor();
                }
            });
        } else if (id == NotificationCenter.group_room_leave) {
            AndroidUtility.RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    mChatView.hideSendBar();
                    isGroupChat = Imps.Chats.GROUP_LEAVE;
                    if (mGroupCallback != null)
                        getLoaderManager().restartLoader(3333, null, mGroupCallback);
                }
            });

        }
    }

    public void startChooseVoice() {
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
        String address = contactName.substring(0, contactName.indexOf('@'));
        isVideoCalling = false;
        Intent i = new Intent(this, OutgoingActivity.class);
        i.putExtra("VideoEnabled", false);
        i.putExtra("address", address);
        startActivity(i);
    }

    public void startChooseVideo() {
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
            } else if (LinphoneManager.getLc().getMaxCalls() == 0) {
                GlobalFunc.showToast(this, R.string.disable_ps_call, false);
                return;
            }
        }

        LinphonePreferences.instance().setInitiateVideoCall(true);
        String address = contactName.substring(0, contactName.indexOf('@'));
        isVideoCalling = true;
        Intent i = new Intent(this, OutgoingActivity.class);
        i.putExtra("VideoEnabled", true);
        i.putExtra("address", address);
        startActivity(i);
    }

    public void startChooseFile() {
        if (GlobalFunc.hasCalling()) {
            GlobalFunc.showToast(this, getResources().getString(R.string.disable_uploading_in_calling), true);
            return;
        }
        Intent i = new Intent(this, FileSendTabActivity.class);
        i.putExtra("chatid", Long.toString(getChatId()));
        startActivity(i);
    }

    public void startCameraApp() {
        if (GlobalFunc.hasCalling()) {
            GlobalFunc.showToast(this, getResources().getString(R.string.disable_use_camera_in_calling), true);
            return;
        }

        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("needCompressImage", true);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private void updateLoginStatus(int sipStatus, int xmppStatus) {
        mXmppStatus = xmppStatus;
        mSipStatus = sipStatus;

        if (xmppStatus == ConnectionState.eCONNSTAT_XMPP_CONNECTED && sipStatus == ConnectionState.eCONNSTAT_SIP_CONNECTED) {
            mImgStatus.setImageResource(R.drawable.led_connected);
            mTxtStatus.setText(R.string.network_connected);
            if (mStatusBar.getVisibility() == VISIBLE) {
                mStatusBar.setImageResource(R.drawable.led_connected);
                Animation am = AnimationUtils.loadAnimation(this, R.anim.slide_out_left_to_right);
                am.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mStatusBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mStatusBar.startAnimation(am);
            }
            mFlashFlag = false;
        } else if (xmppStatus != ConnectionState.eCONNSTAT_XMPP_CONNECTED && sipStatus != ConnectionState.eCONNSTAT_SIP_CONNECTED) {
            mStatusBar.setImageResource(R.drawable.led_disconnected);
            mImgStatus.setImageResource(R.drawable.led_disconnected);
            mTxtStatus.setText(R.string.network_disconnected);

            if (mStatusBar.getVisibility() == View.GONE) {
                mStatusBar.setVisibility(VISIBLE);
                Animation am = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_to_left);
                mStatusBar.startAnimation(am);
            }

            mFlashFlag = true;
            if (!mFlashThread.isAlive())
                flashStatusLed();
            if (xmppStatus == ConnectionState.eCONNSTAT_MOBILECOM_CONNFAILED || xmppStatus == ConnectionState.eCONNSTAT_MOBILECOM_DISCONNECTED)
                mStatusContent.setText(GlobalFunc.getConnErrMsg());
            else if (xmppStatus == ConnectionState.eCONNSTAT_MOBILECOM_CONNECTING)
                mStatusContent.setText(getResources().getString(R.string.connecting_to_mobilecom));
            else
                mStatusContent.setText(getString(R.string.cant_connect_to_server));
        } else {
            mStatusBar.setImageResource(R.drawable.led_inprogress);
            mImgStatus.setImageResource(R.drawable.led_inprogress);
            mTxtStatus.setText(R.string.network_inprogress);

            if (mStatusBar.getVisibility() == View.GONE) {
                mStatusBar.setVisibility(VISIBLE);
                Animation am = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_to_left);
                mStatusBar.startAnimation(am);
            }

            mFlashFlag = true;
            if (!mFlashThread.isAlive())
                flashStatusLed();

            if (xmppStatus == ConnectionState.eCONNSTAT_XMPP_CONNECTED) {
                mStatusContent.setText(getString(R.string.cant_connect_to_sip_server));
            } else if (sipStatus == ConnectionState.eCONNSTAT_SIP_CONNECTED) {
                mStatusContent.setText(getString(R.string.cant_connect_to_xmpp_server));
            }
        }
    }

    private void flashStatusLed() {
        mFlashThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mFlashFlag) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < 2; i++) {
                        int resId = R.drawable.led_inprogress;
                        if (mXmppStatus != ConnectionState.eCONNSTAT_XMPP_CONNECTED && mSipStatus != ConnectionState.eCONNSTAT_SIP_CONNECTED)
                            resId = R.drawable.led_disconnected;
                        final int resId1 = resId;

                        resId = R.drawable.led_inprogress;
                        if (mXmppStatus != ConnectionState.eCONNSTAT_XMPP_CONNECTED && mSipStatus != ConnectionState.eCONNSTAT_SIP_CONNECTED)
                            resId = R.drawable.led_disconnected;
                        final int resId2 = resId;

                        UIThreadDispatcher.dispatch(new Runnable() {
                            @Override
                            public void run() {
                                mStatusBar.setImageResource(resId2);
                                mImgStatus.setImageResource(resId2);

                            }
                        });
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        UIThreadDispatcher.dispatch(new Runnable() {
                            @Override
                            public void run() {
                                mStatusBar.setImageResource(resId1);
                                mImgStatus.setImageResource(resId1);
                            }
                        });
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mFlashThread.start();
    }

    public boolean messageItemIsChecked(MessageItem currentItem) {
        if (selectedMessages != null && selectedMessages.size() > 0) {
            for (MessageItem item : selectedMessages) {
                if (item.id.equals(currentItem.id) && item.packetID.equals(currentItem.packetID)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addMessage(MessageItem item) {
        if (!messageItemIsChecked(item)) {
            selectedMessages.add(item);
        }
        mLytDeleteSel.setEnabled(selectedMessages.size() > 0);
    }

    public void removeMessage(MessageItem item) {
        for (MessageItem tmp : selectedMessages) {
            if (tmp.id.equals(item.id) && tmp.packetID.equals(item.packetID)) {
                selectedMessages.remove(tmp);
                break;
            }
        }
        mLytDeleteSel.setEnabled(selectedMessages.size() > 0);
    }

    private void selectAll() {
        selectedMessages.clear();
        String selection = Imps.Messages.THREAD_ID + "=? AND (" + Imps.Messages.TYPE + " < 2 OR " + Imps.Messages.TYPE + " == 8)";
        String[] selecttionArgs = new String[]{String.valueOf(getChatId())};
        Cursor cursor = getContentResolver().query(Imps.Messages.CONTENT_URI, new String[]{Imps.Messages.THREAD_ID, Imps.Messages.PACKET_ID, Imps.Messages._ID, Imps.Messages.TYPE},
                selection, selecttionArgs, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                MessageItem item = new MessageItem();
                item.id = cursor.getString(cursor.getColumnIndex(Imps.Messages._ID));
                item.packetID = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
                item.type = cursor.getInt(cursor.getColumnIndex(Imps.Messages.TYPE));
                selectedMessages.add(item);
                mLytDeleteSel.setEnabled(true);
            }
            cursor.close();
        }
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
    }

    private void unSelectAll() {
        selectedMessages.clear();
        mLytDeleteSel.setEnabled(false);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
    }

    private void deleteMessages() {
        final CustomDialog dlg = new CustomDialog(this, r.getString(R.string.delete), r.getString(R.string.delete_messages),
                r.getString(R.string.yes), r.getString(R.string.no));
        dlg.setOnOKClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                ArrayList<MessageItem> tempList = new ArrayList<MessageItem>();
                tempList.addAll(selectedMessages);
                for (MessageItem item : tempList) {
                    int errorCode = Imps.getErrorByPacketId(item.packetID);
                    int deliveryState = GlobalFunc.getDeliveryState(item.packetID);
                    Imps.deleteMessage(ChatRoomActivity.this, cr, item.packetID);
                    selectedMessages.remove(item);

                    if (errorCode == Imps.FileErrorCode.UPLOADING) {
                        GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                        sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED));
                    }
                    if (errorCode == Imps.FileErrorCode.DOWNLOADING && deliveryState == 2) {
                        GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
                        sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
                    }
                }
                if (Imps.getChatCount(cr, getChatId()) == 0) {      // No more messages in this chatroom, Delete this chat room.
                    Imps.Chats.deleteChat(cr, getChatId());
                    finish();
                    instance = null;
                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                mLytDeleteSel.setEnabled(false);
                Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
                sendBroadcast(intent);
            }
        });
        dlg.setOnCancelClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        dlg.show();
    }

    public void copyFile(final String filePath, String mimeType) {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        final File file = new File(filePath);
        if (file.length() > bytesAvailable) {
            GlobalFunc.showAlertDialog(this, getString(R.string.error_message_low_storage_title), getString(R.string.error_message_low_storage));
            return;
        }

        final FileSaveDialog dlg = new FileSaveDialog(this, this.getResources().getString(R.string.save), this.getResources().getString(R.string.file_copy_confirm),
                this.getResources().getString(R.string.ok), this.getResources().getString(R.string.cancel));
        String strHead = "";
        if (mimeType.startsWith("image/"))
            strHead = getResources().getString(R.string.file_head_image);
        else if (mimeType.startsWith("video/"))
            strHead = getResources().getString(R.string.file_head_video);
        else if (mimeType.startsWith("audio/"))
            strHead = getResources().getString(R.string.file_head_audio);

        final String finalStrHead = strHead;

        final Date date = new Date(System.currentTimeMillis());
        final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        final String fileName = file.getName();
        final EditText fileNameEditText = (EditText) dlg.findViewById(R.id.fileNameEditText);
        fileNameEditText.setText(finalStrHead + dateFormat.format(date));
        fileNameEditText.setSelection(0, fileNameEditText.getText().length());

        dlg.setOnOKClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileNameEditText.getText().toString().trim().isEmpty()) {
                    GlobalFunc.showToast(ChatRoomActivity.this, getResources().getString(R.string.input_file_name), true);
                    return;
                }
                if (!file.exists()) {
                    GlobalFunc.showToast(ChatRoomActivity.this, getResources().getString(R.string.file_no_exist), true);
                    dlg.dismiss();
                    return;
                }
                String newFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
                File downloadFolder = new File(newFilePath);
                if (!downloadFolder.exists())
                    downloadFolder.mkdir();
                newFilePath = newFilePath + "/" + getString(R.string.app_name);
                downloadFolder = new File(newFilePath);
                if (!downloadFolder.exists())
                    downloadFolder.mkdir();

                final File newFile = new File(newFilePath + "/" + fileNameEditText.getText() + fileName.substring(fileName.lastIndexOf(".")));

                final Dialog dialog = new Dialog(ChatRoomActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.file_copy_progress);
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView saveFilename = (TextView) dialog.findViewById(R.id.save_file_name);
                final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.save_file_progress);
                saveFilename.setText(newFile.getName());
                seekBar.setMax(100);

                final Thread fileThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileInputStream inputFile = new FileInputStream(file);
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
                            byte[] inputs = new byte[4092];
                            while (inputFile.read(inputs) != -1) {
                                if (!fileCopying)
                                    break;
                                bos.write(inputs);
                                UIThreadDispatcher.dispatch(new Runnable() {
                                    @Override
                                    public void run() {
                                        seekBar.setProgress((int) (newFile.length() * 100 / file.length()));
                                    }
                                });
                            }
                            bos.flush();
                            bos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (fileCopying) {
                            UIThreadDispatcher.dispatch(new Runnable() {
                                @Override
                                public void run() {
                                    GlobalFunc.showToast(ChatRoomActivity.this, getResources().getString(R.string.file_copy_success) +
                                            getString(R.string.save_path) + "/" +
                                            newFile.getName(), true);
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });

                dialog.findViewById(R.id.save_file_cancel).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileCopying = false;
                        if (newFile.exists())
                            newFile.delete();
                        dialog.dismiss();
                    }
                });

                dialog.show();
                fileCopying = true;
                fileThread.start();
                dlg.dismiss();
            }
        });
        dlg.setOnCancelClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        dlg.show();

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AndroidUtility.showKeyboard(fileNameEditText);
            }
        }, 500);

    }

    public void applyRecordingStatus() {
        findViewById(R.id.btn_voice_call).setEnabled(false);
//        findViewById(R.id.btn_video_call).setEnabled(false);
        findViewById(R.id.btn_more_setting).setEnabled(false);
        findViewById(R.id.btn_back).setEnabled(false);
    }

    public void applyStopRecordingStatus() {
        findViewById(R.id.btn_voice_call).setEnabled(true);
//        findViewById(R.id.btn_video_call).setEnabled(true);
        findViewById(R.id.btn_more_setting).setEnabled(true);
        findViewById(R.id.btn_back).setEnabled(true);
    }

    public void insertChatNotExist(int contactId) {
        ContentValues values = new ContentValues(4);
        values.put(Imps.Chats.CONTACT_ID, contactId);
        values.put(Imps.Chats.LAST_MESSAGE_DATE, GlobalFunc.getCurrentUTCTime());
        values.put(Imps.Chats.LAST_UNREAD_MESSAGE, "");
        values.put(Imps.Chats.GROUP_CHAT, isGroupChat);

        Cursor cursor = cr.query(Imps.Chats.CONTENT_URI, new String[]{Imps.Chats._ID}, Imps.Chats.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cursor.close();
                return;
            }
            cursor.close();
        }
        cr.insert(Imps.Chats.CONTENT_URI, values);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
    }

    private class GroupListAdapter extends ResourceCursorAdapter {

        public GroupListAdapter(Context context, int view) {
            super(context, view, null, 0);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String nickname = cursor.getString(cursor.getColumnIndex(Imps.GroupMembers.NICKNAME));
            String username = cursor.getString(cursor.getColumnIndex(Imps.GroupMembers.USERNAME));
            TextView name = view.findViewById(R.id.username);
            CircularImageView avatar = view.findViewById(R.id.imgPropile);
            // GlobalFunc.setProfileImage(avatar, context, username);
            GlobalFunc.showAvatar(context, username, avatar);
            name.setText(nickname);
            name.setSelected(true);
            int type = cursor.getInt(cursor.getColumnIndex(Imps.GroupMembers.TYPE));
            view.findViewById(R.id.onlineStatus).setVisibility(GONE);
            Cursor contactCur = getContentResolver().query(Imps.Contacts.CONTENT_URI, null, Imps.Contacts.USERNAME + "=?", new String[]{username}, Imps.Contacts.DEFAULT_SORT_ORDER);
            if (type == Imps.GroupMembers.TYPE_LEFT)
                view.findViewById(R.id.group_left).setVisibility(VISIBLE);
            else {
                view.findViewById(R.id.group_left).setVisibility(GONE);
                if (contactCur != null) {
                    if (contactCur.moveToFirst()) {
                        int presence = contactCur.getInt(contactCur.getColumnIndex(Imps.Contacts.PRESENCE_STATUS));
                        if (presence != Imps.Presence.OFFLINE)
                            view.findViewById(R.id.onlineStatus).setVisibility(VISIBLE);
                    }
                }
            }

            if (isGroupChat == Imps.Chats.GROUP_LEAVE) {
                view.findViewById(R.id.onlineStatus).setVisibility(GONE);
                view.findViewById(R.id.group_left).setVisibility(GONE);
            }
        }
    }

    private class GroupListCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = Uri.withAppendedPath(Imps.GroupMembers.CONTENT_URI, String.valueOf(chatContactId));
            String orderby = null;
            //orderby = "lower(" + Imps.Contacts.NICKNAME + ") ASC";
            orderby = Imps.Contacts.ORDER_BY_LOCALIZED;
            CursorLoader loader = new CursorLoader(instance, uri, null,
                    null, null, orderby);
            loader.setUpdateThrottle(500L);

            return loader;
        }

        @Override
        public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
            mGroupAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(android.content.Loader<Cursor> loader) {

        }
    }

}
