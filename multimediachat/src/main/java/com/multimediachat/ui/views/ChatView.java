package com.multimediachat.ui.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.SimpleAlertHandler;
import com.multimediachat.app.im.IChatListener;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IContactList;
import com.multimediachat.app.im.IContactListListener;
import com.multimediachat.app.im.IContactListManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.app.adapter.ChatListenerAdapter;
import com.multimediachat.app.im.engine.Address;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ContactListListener;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.engine.ImException;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.ChatRoomActivity;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.ui.emotic.IpMessageEmoticonPanel;
import com.multimediachat.ui.emotic.SmileyParser;
import com.multimediachat.util.LogCleaner;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.datamodel.MessageItem;

import org.jivesoftware.smack.util.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ChatView extends LinearLayout implements View.OnClickListener {

    // This projection and index are set for the query of active chats
    public static final String[] CHAT_PROJECTION = {Imps.Contacts._ID, Imps.Contacts.ACCOUNT,
            Imps.Contacts.PROVIDER, Imps.Contacts.USERNAME,
            Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
            Imps.Presence.PRESENCE_STATUS,
            Imps.Chats.LAST_UNREAD_MESSAGE,
            Imps.Chats._ID
    };

    public static final int CONTACT_ID_COLUMN = 0;
    public static final int ACCOUNT_COLUMN = 1;
    public static final int PROVIDER_COLUMN = 2;
    public static final int USERNAME_COLUMN = 3;
    public static final int NICKNAME_COLUMN = 4;
    public static final int TYPE_COLUMN = 5;
    static final int PRESENCE_STATUS_COLUMN = 6;
    static final int LAST_UNREAD_MESSAGE_COLUMN = 7;
    static final int CHAT_ID_COLUMN = 8;
    static final int MIME_TYPE_COLUMN = 9;

    static final String[] MESSAGES_PROJECT = {
            Imps.Messages._ID,
            Imps.Messages.NICKNAME,
            Imps.Messages.BODY,
            Imps.Messages.DATE,
            Imps.Messages.TYPE,
            Imps.Messages.ERROR_CODE,
            Imps.Messages.IS_DELIVERED,
            Imps.Messages.MIME_TYPE,
            Imps.Messages.PACKET_ID,
            Imps.Messages.THUMBNAIL_WIDTH,
            Imps.Messages.THUMBNAIL_HEIGHT,
            Imps.Messages.SAMPLE_IMAGE_PATH,
            Imps.Messages.DISPLAY_SENT_TIME,
            Imps.Messages.SERVER_TIME
    };
    public static int mIdColumn = 0;
    public static int mNicknameColumn = 1;
    public static int mBodyColumn = 2;
    public static int mDateColumn = 3;
    public static int mTypeColumn = 4;
    public static int mErrCodeColumn = 5;
    public static int mDeliveredColumn = 6;
    public static int mMimeTypeColumn = 7;
    public static int mPacketIdColumn = 8;
    public static int mThumbnailWidthColumn = 9;
    public static int mThumbnailHeightColumn = 10;
    public static int mSampleImagePathColumn = 11;
    public static int mDisplaySentTimeColumn = 12;
    public static int mServerTimeColumn = 13;

    ChatRoomActivity mActivity;
    ImApp mApp;
    SimpleAlertHandler mHandler;
    Cursor mCursor;
    IImConnection mConn;
    ContentResolver cr;

    //***********************************//
    //load more chat history variable
    int nChatHistoryCount = 20;
    int nChatHistoryMoreCount = 10;
    int isLoadingMore = 0;
    int nChatHeaderHeight = 0;
    //**********************************//

    //controls
    //*********************************//
    public ListView mHistory;
    public EditText mComposeMessage;

    View mLayoutInputText;
    View mLayoutHoldToTalk;
    View mLayoutMicMask;
    LinearLayout mImgMaxAmpl;
    TextView mTxtRecordSec;
    ProgressBar mRecordProgress;

    int progress;
    public View mSendbarLayout;

    public ImageView mBtnVoiceMessage;
    public ImageView mBtnEmoticon;
    public ImageView mBtnSend;

    boolean mShowEmoticon = false;
    IpMessageEmoticonPanel mEmoticonPanel = null;
    //***********************************************************//

    //recording
    //*******************//
    private String audioFilePath = null;
    private double mTimes = 0;
    public MediaRecorder recorder = null;

    private int mAudioStatus = NORMAL_STATUS;
    private static final int NORMAL_STATUS = 0;
    private static final int RECORD_STATUS = 1;

    public String userXmppAddress = "";

    public String last_packet_id = null;

    private String mBeforeText = "";


    public MessageAdapter mMessageAdapter;
    private boolean isServiceUp;
    private IChatSession mCurrentChatSession;

    int isPlusFriend = 0;
    long mChatId = -1;
    int mType;
    public String mRemoteNickname;
    public String mRemoteAddress;

    long mProviderId;
    long mAccountId;
    private Context mContext;

    private static final long DEFAULT_QUERY_INTERVAL = 2000;
    private static final long FAST_QUERY_INTERVAL = 200;
    private static final int QUERY_TOKEN = 10;

    private long recordStartTime = 0;
    private long recordEndTime = 0;

    @SuppressLint("HandlerLeak")
    public Handler mRecordingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAudioStatus == RECORD_STATUS) {
                mTimes = mTimes + 0.2;
                int ampl =  (recorder.getMaxAmplitude() / 70);
                if (ampl > 120) ampl = 120;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, ampl);
                mImgMaxAmpl.setLayoutParams(params);
                mTxtRecordSec.setText(String.format("%s%s", (int) mTimes, getResources().getString(R.string.str_second)));
                mRecordProgress.setProgress((int) (mTimes*5));
                progress++;

                if (progress >= 300) {
                    stopRecording();
                    try {
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sendRecording();
                } else {
                    sendEmptyMessageDelayed(0, 200);
                }
            }
        }
    };

    public void startRecording() {

        mLayoutMicMask.setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(0.3f, 0.3f);
        animation.setDuration(0);
        animation.setFillAfter(true);
        mLayoutHoldToTalk.startAnimation(animation);

        try {

            if (mAudioStatus == RECORD_STATUS)
                return;

            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }

            progress = 0;
            GlobalFunc.makeChatDir();
            String chatPath = GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME + "/" + DatabaseUtils.mAccountID + "/" + mChatId + "/";
            File file = new File(chatPath);
            if (!file.exists())
                file.mkdirs();

            audioFilePath = chatPath + System.currentTimeMillis();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    recorder.setOutputFile(audioFilePath);
                    try {
                        recorder.prepare();
                        recorder.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            mAudioStatus = RECORD_STATUS;

            mTimes = 0;

            mRecordingHandler.sendEmptyMessageDelayed(0, 200);

            mActivity.applyRecordingStatus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        mLayoutMicMask.setVisibility(View.GONE);

        AlphaAnimation animation = new AlphaAnimation(1f, 1f);
        animation.setDuration(0);
        mLayoutHoldToTalk.startAnimation(animation);

        if (mAudioStatus == NORMAL_STATUS)
            return;

        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            recorder = null;
        }
        mAudioStatus = NORMAL_STATUS;
        mRecordingHandler.removeMessages(0);
        progress = 0;
        mActivity.applyStopRecordingStatus();
    }

    public void sendRecording() {
        File file = new File(audioFilePath);
        mActivity.sendVoiceMsg(Uri.fromFile(file));
    }

    private void checkConnection() throws ImException, RemoteException {
        if (mConn == null) {
            mConn = ImApp.getInstance().getConnection(mProviderId);
        }
        if (mConn == null)
            throw new ImException("unable to get connection");

    }

    @SuppressLint("HandlerLeak")
    private final class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(Context context) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {

            if (c != null) {
                Cursor cursor = new DeltaCursor(c);
                int preCount = mMessageAdapter.getCount();

                if (mMessageAdapter != null && cursor != null) {
                    setChatCount(cursor.getCount());
                    mMessageAdapter.changeCursor(cursor);

                    if (getChatCount() > preCount) {
                        if (mActivity.mScrollPos != -1) {
                            mHistory.setSelection(mActivity.mScrollPos);
                            mActivity.mScrollPos = -1;
                        }
                        else
                            mHistory.setSelection(getChatCount() - 1);
                    }
                }
            }
        }
    }

    private QueryHandler mQueryHandler;

    public SimpleAlertHandler getHandler() {
        return mHandler;
    }

    private class RequeryCallback implements Runnable {
        public void run() {
            requeryCursor();
        }
    }

    private RequeryCallback mRequeryCallback = null;

    private IChatListener mChatListener = new ChatListenerAdapter() {
        @Override
        public void onContactJoined(IChatSession ses, Contact contact) {
            scheduleRequery(FAST_QUERY_INTERVAL);
        }

        @Override
        public void onContactLeft(IChatSession ses, Contact contact) {
            scheduleRequery(FAST_QUERY_INTERVAL);
        }

        @Override
        public void onSendMessageError(IChatSession ses,
                                       com.multimediachat.app.im.engine.Message msg, ImErrorInfo error) {
            scheduleRequery(FAST_QUERY_INTERVAL);
        }

        @Override
        public void onIncomingOper(IChatSession ses, String packetId, String operType, String operMessage, String operMsgId) throws RemoteException {
            scheduleRequery(FAST_QUERY_INTERVAL);
        }

        @Override
        public void onStatusChanged(IChatSession ses) throws RemoteException {
            scheduleRequery(DEFAULT_QUERY_INTERVAL);
        }

        @Override
        public void onIncomingData(IChatSession ses, byte[] data) {
            try {
                DebugConfig.info("OTR_DATA", "incoming data " + new String(data, "UTF8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessagePostPoned(IChatSession ses, String packetId)
                throws RemoteException {
            scheduleRequery(DEFAULT_QUERY_INTERVAL);
        }

        @Override
        public void onSentMessage(IChatSession ses, String packetId) throws RemoteException {
            scheduleRequery(DEFAULT_QUERY_INTERVAL);
        }

    };

    private IContactListListener mContactListListener = new IContactListListener.Stub() {
        public void onAllContactListsLoaded() {
        }

        public void onContactChange(int type, IContactList list, Contact contact) {
            if (type == ContactListListener.LIST_CONTACT_REMOVED) {
                if (contact != null && contact.getAddress() != null && contact.getAddress().getAddress() != null) {
                    if (contact.getAddress().getAddress().equals(mRemoteAddress))
                        mActivity.finish();
                }
            }
        }

        public void onContactError(int errorType, ImErrorInfo error, String listName,
                                   Contact contact) {
        }

        public void onContactsPresenceUpdate(Contact[] contacts) {

        }
    };

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (ChatRoomActivity) context;
        mApp = (ImApp) mActivity.getApplication();
        mHandler = new ChatViewHandler(mActivity);
        mContext = context;
        cr = mContext.getContentResolver();
        nChatHeaderHeight = mContext.getResources().getDimensionPixelSize(R.dimen.chat_header_height);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHistory = (ListView) findViewById(R.id.history);
        mComposeMessage = (EditText) findViewById(R.id.composeMessage);
        mBtnSend = (ImageView) findViewById(R.id.btnSend);
        mBtnSend.setEnabled(false);
        mBtnSend.setAlpha((float) 0.7);
        mBtnVoiceMessage = (ImageView) findViewById(R.id.btnVoiceMessage);
        mBtnEmoticon = (ImageView) findViewById(R.id.btnEmoji);
        mSendbarLayout = findViewById(R.id.sendbar_layout);

        mBtnVoiceMessage.setOnClickListener(this);
        mBtnVoiceMessage.setSelected(false);

        // hold to talk layout
        mLayoutHoldToTalk = findViewById(R.id.layoutHoldTalk);
        mLayoutHoldToTalk.setVisibility(View.GONE);

        // input text layout
        mLayoutInputText = findViewById(R.id.layout_inputtext);

        mLayoutMicMask = findViewById(R.id.layout_mic_mask);
        mLayoutMicMask.setVisibility(View.GONE);

        mImgMaxAmpl = findViewById(R.id.img_max_amplitude);
        mTxtRecordSec = findViewById(R.id.txt_record_second);
        mRecordProgress = findViewById(R.id.record_time_progress);

        initShareAndEmoticonRessource();
        mEmoticonPanel.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
            mBtnEmoticon.setVisibility(View.GONE);

        mBtnEmoticon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mShowEmoticon) {
                    showEmoticon();
                } else {
                    hideEmoticon();
                }
            }
        });

        mLayoutHoldToTalk.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!GlobalFunc.isStorageWrittable()) {
                    GlobalFunc.showToast(mContext, R.string.cannot_use_storage, false);
                    return true;
                }

                if (!GlobalFunc.checkStorageFreeSpace(mContext)) {
                    GlobalFunc.showToast(mContext, R.string.error_message_low_storage, false);
                    return true;
                }

                if (System.currentTimeMillis() - recordEndTime < 50)
                    return true;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    recordEndTime = 0;
                    recordStartTime = System.currentTimeMillis();

                    startRecording();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (mAudioStatus == RECORD_STATUS) {
                        if (System.currentTimeMillis() - recordStartTime < 1000) {
                            try {
                                Thread.sleep(200);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            stopRecording();
                        } else {
                            stopRecording();
                        }
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (event.getX() < 0 || event.getX() > v.getWidth()
                                || event.getY() < 0 || event.getY() > v.getHeight()) {
                            File file = null;
                            if (audioFilePath != null)
                                file = new File(audioFilePath);
                            if (file != null && file.isFile())
                                file.delete();
                        } else {
                            if (mTimes >= 1) {
                                sendRecording();
                            } else {
                                GlobalFunc.showToast(mContext, R.string.error_voice_length_short, false);

                                File file = null;
                                if (audioFilePath != null)
                                    file = new File(audioFilePath);
                                if (file != null && file.isFile())
                                    file.delete();
                            }
                        }
                    }
                    recordEndTime = System.currentTimeMillis();
                    recordStartTime = 0;
                }

                return true;
            }
        });

        mHistory.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mLayoutHoldToTalk.getVisibility() == GONE) {
                    if (mShowEmoticon) {
                        mBtnEmoticon.setImageResource(R.drawable.chatctrl_emoticon);
                        mEmoticonPanel.setVisibility(View.GONE);
                        mShowEmoticon = false;
                    }
                    AndroidUtility.hideKeyboard(mComposeMessage);
                }
                return false;
            }

        });

        mComposeMessage.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            sendMessage();
                            return true;

                        case KeyEvent.KEYCODE_ENTER:
                            if (Imps.Profile.getProfileBoolean(cr, Imps.Profile.PRESS_ENTER_TO_SEND)) {
                                sendMessage();
                                return true;
                            }
                            if (event.isAltPressed()) {
                                mComposeMessage.append("\n");
                                return true;
                            }
                    }
                }
                return false;
            }
        });

        mComposeMessage.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setFocusHistoryView(500);
                    if (mShowEmoticon) return true;
                    mBtnEmoticon.setImageResource(R.drawable.chatctrl_emoticon);
                    mEmoticonPanel.setVisibility(View.GONE);
                    mShowEmoticon = false;
                }
                return false;
            }
        });

        mComposeMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                mBeforeText = mComposeMessage.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputed = mComposeMessage.getText().toString();

                if (inputed.length() > 0) {
                    mBtnSend.setEnabled(true);
                    mBtnSend.setAlpha((float) 1.0);
                } else {
                    mBtnSend.setEnabled(false);
                    mBtnSend.setAlpha((float) 0.7);
                }

                if (inputed.getBytes().length > GlobalConstrants.MAX_MESSAGE_LENGTH) {
                    GlobalFunc.showToast(mActivity, R.string.overload_text_string, false);
                    mComposeMessage.onPreDraw();
                    SpannableString spannablecontent = new SpannableString(SmileyParser.getInstance(mContext).addSmileySpans(mBeforeText, mComposeMessage.getLineHeight()));
                    mComposeMessage.setText(spannablecontent);
                    mComposeMessage.setSelection(mComposeMessage.getText().length());
                    return;
                }
                if (!mBeforeText.equals(inputed)) {
                    int selection = mComposeMessage.getSelectionStart();
                    SpannableString spannablecontent = new SpannableString(SmileyParser.getInstance(mContext).addSmileySpans(inputed, mComposeMessage.getLineHeight()));
                    ImageSpan afSpans[] = spannablecontent.getSpans(0, inputed.length(), ImageSpan.class);
                    SpannableString before = new SpannableString(SmileyParser.getInstance(mContext).addSmileySpans(mBeforeText, mComposeMessage.getLineHeight()));
                    ImageSpan beSpans[] = before.getSpans(0, inputed.length(), ImageSpan.class);
                    if (afSpans.length != beSpans.length) {
                        mComposeMessage.setText(spannablecontent);
                        mComposeMessage.setSelection(selection);
                    }
                }
            }
        });

        mBtnSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mAudioStatus == NORMAL_STATUS)
                    sendMessage();
            }
        });

        mMessageAdapter = new MessageAdapter(mActivity, null);
        mHistory.setAdapter(mMessageAdapter);
        mSendbarLayout = findViewById(R.id.sendbar_layout);
        mSendbarLayout.setVisibility(mActivity.isSelecteMode ? GONE : VISIBLE);
    }

    private void hideEmoticon() {
        setFocusHistoryView(500);
        showKeyBoard(true);
        mBtnEmoticon.setImageResource(R.drawable.chatctrl_emoticon);
        mEmoticonPanel.setVisibility(View.GONE);
        mLayoutHoldToTalk.setVisibility(View.GONE);
        mLayoutInputText.setVisibility(View.VISIBLE);
        mShowEmoticon = false;
    }

    private void showEmoticon() {
        showKeyBoard(false);
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mEmoticonPanel.setVisibility(View.VISIBLE);
                mBtnEmoticon.setImageResource(R.drawable.chatctrl_keyboard);
                mShowEmoticon = true;
            }
        }, 300);
    }

    public void hideSendBar() {
        mSendbarLayout.setVisibility(View.GONE);
    }

    public void startListening() {
        if (!isServiceUp)
            return;
        Cursor cursor = getMessageCursor();
        if (cursor == null) {
            long chatId = getChatId();
            if (chatId != -1)
                startQuery(chatId);
        } else {
            requeryCursor();
        }

        registerChatListener();
    }

    public void stopListening() {
        cancelRequery();
        unregisterChatListener();
    }

    private void updateContactInfo() {
        mProviderId = mCursor.getLong(PROVIDER_COLUMN);
        mAccountId = mCursor.getLong(ACCOUNT_COLUMN);
        mType = mCursor.getInt(TYPE_COLUMN);
        mRemoteNickname = mCursor.getString(NICKNAME_COLUMN);
        mRemoteAddress = mCursor.getString(USERNAME_COLUMN);
        userXmppAddress = Imps.Account.getUserName(cr, mAccountId);
    }

    public void bindChat(long chatId, int _isPlusFriend) {
        mChatId = chatId;

        isPlusFriend = _isPlusFriend;

        if (isPlusFriend == 1)
            hideSendBar();

        Uri contactUri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, chatId);
        mCursor = cr.query(contactUri, CHAT_PROJECTION, null, null, null);

        if (!mCursor.moveToFirst()) {
            mChatId = -1;
            mActivity.finish();
        } else {
            updateContactInfo();
            mCurrentChatSession = getChatSession();

            if (mCurrentChatSession == null)
                mCurrentChatSession = createChatSession();

            if (mCurrentChatSession != null) {
                isServiceUp = true;
                startListening();
            }
        }
    }

    private void startQuery(long chatId) {
        if (mQueryHandler == null) {
            mQueryHandler = new QueryHandler(mContext);
        } else {
            // Cancel any pending queries
            mQueryHandler.cancelOperation(QUERY_TOKEN);
        }

        Uri uri = Imps.Messages.getContentUriByThreadId(chatId);
        nChatHistoryCount = Imps.getMessageCount(cr, chatId);

        //          ici (20180819)          //
        int chatLimit = Imps.Profile.getProfileInt(cr, Imps.Profile.CHAT_LIMIT);
        if (!Imps.Profile.getProfileBoolean(cr, Imps.Profile.DELETE_OLD_MESSAGES))
            chatLimit = 500;
        /*        Remove old messages except nChatHistoryCount messages     */
        if (nChatHistoryCount > chatLimit) {
            deleteLimitQuery(chatId, nChatHistoryCount - chatLimit);

            uri = Imps.Messages.getContentUriByThreadId(chatId);
            nChatHistoryCount = Imps.getMessageCount(cr, chatId);
        }

        uri = uri.buildUpon().appendQueryParameter("OFFSET", String.valueOf(0)).build();
        uri = uri.buildUpon().appendQueryParameter("LIMIT", String.valueOf(nChatHistoryCount)).build();

        mQueryHandler.startQuery(QUERY_TOKEN, null, uri, MESSAGES_PROJECT,
                null,
                null, Imps.Messages.DATE);
    }

    private void deleteLimitQuery(long chatId, int limit) {
        if (mQueryHandler == null) {
            mQueryHandler = new QueryHandler(mContext);
        } else {
            // Cancel any pending queries
            mQueryHandler.cancelOperation(QUERY_TOKEN);
        }

        Uri uri = Imps.Messages.getContentUriByThreadId(chatId);
        Cursor cursor = cr.query(uri, new String[]{Imps.Messages._ID, Imps.Messages.ERROR_CODE, Imps.Messages.PACKET_ID}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0 ; i < limit ; i ++) {
                int messageId = cursor.getInt(cursor.getColumnIndex(Imps.Messages._ID));
                int errorCode = cursor.getInt(cursor.getColumnIndex(Imps.Messages.ERROR_CODE));
                String packetId = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
                if (ImApp.getInstance().containDownloadThread(packetId))
                    ImApp.getInstance().deleteDownloadHandler(packetId);
                if (ImApp.getInstance().containUploadThread(packetId))
                    ImApp.getInstance().deleteUploadHandler(packetId);
                if (errorCode == Imps.FileErrorCode.UPLOADING) {
                    GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                }
                if (errorCode == Imps.FileErrorCode.DOWNLOADING) {
                    GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
                }
                Uri remove = Imps.Messages.getContentUriById(messageId);
                mQueryHandler.startDelete(QUERY_TOKEN, null, remove, null, null);
                cursor.moveToNext();
            }
            cursor.close();
            if (GlobalFunc.getUploadingStatus() == GlobalConstrants.NO_UPLOADING) {
                Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                mActivity.sendBroadcast(intent);
            }
            if (GlobalFunc.getDownloadingStatus() == GlobalConstrants.NO_DOWNLOADING) {
                Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED);
                mActivity.sendBroadcast(intent);
            }
        }
    }


    void scheduleRequery(long interval) {
        if (mRequeryCallback == null) {
            mRequeryCallback = new RequeryCallback();
        } else {
            mHandler.removeCallbacks(mRequeryCallback);
        }
        mHandler.postDelayed(mRequeryCallback, interval);
    }

    public void cancelRequery() {
        if (mRequeryCallback != null) {
            mHandler.removeCallbacks(mRequeryCallback);
            mRequeryCallback = null;
        }
    }

    public void requeryCursor() {
        if (mMessageAdapter.isScrolling()) {
            mMessageAdapter.setNeedRequeryCursor(true);
            return;
        }

        startQuery(getChatId());
    }

    public void updateChatList() {
        mMessageAdapter.notifyDataSetChanged();
    }

    private Cursor getMessageCursor() {
        return mMessageAdapter == null ? null : mMessageAdapter.getCursor();
    }

    public long getProviderId() {
        return mProviderId;
    }

    public long getAccountId() {
        return mAccountId;
    }


    public long getChatId() {
        return mChatId;
    }

    public int getChatCount() {
        return nChatHistoryCount;
    }

    public void setChatCount(int count) {
        nChatHistoryCount = count;
    }

    private IChatSession createChatSession() {

        try {
            checkConnection();
            if (mConn != null) {
                IChatSessionManager sessionMgr = mConn.getChatSessionManager();
                if (sessionMgr != null) {

                    IChatSession session = sessionMgr.createChatSession(Address.stripResource(mRemoteAddress));
                    return session;

                }
            }

        } catch (Exception e) {
            LogCleaner.error(ImApp.TAG, "send message error", e);
        }

        return null;
    }

    public IChatSession getChatSession() {
        try {
            checkConnection();
            if (mConn != null) {
                IChatSessionManager sessionMgr = mConn.getChatSessionManager();
                if (sessionMgr != null) {
                    IChatSession session = sessionMgr.getChatSession(Address.stripResource(mRemoteAddress));
                    return session;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isGroupChat() {
        return (mActivity.isGroupChat != 0);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("NewApi")
    public void copyMessage(String message) {
        if (message == null || message.length() == 0)
            return;

        String textToCopy = message;

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(textToCopy);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("chat", textToCopy);
            clipboard.setPrimaryClip(clip);
        }
    }

    public void quoteMessage(String message) throws Exception {
        if (message == null || message.length() == 0)
            return;

        String msg = mComposeMessage.getText().toString();
        int startSel = mComposeMessage.getSelectionStart();
        int endSel = mComposeMessage.getSelectionEnd();
        String result = msg.substring(0, startSel) + message + msg.substring(endSel, msg.length());
        mComposeMessage.setText(result);
        if (result != null && result.length() > 0) {
            mComposeMessage.setSelection(startSel + message.length());
        }
    }

    public void deleteMessage(final String packetId) {
        final Dialog dlg = GlobalFunc.createDialog(mContext, R.layout.confirm_dialog, true);

        TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
        title.setText(mContext.getString(R.string.delete));
        TextView content = (TextView) dlg.findViewById(R.id.msgcontent);
        content.setText(mContext.getString(R.string.delete_message));
        Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
        dlg_btn_ok.setText(mContext.getString(R.string.yes));
        Button dlg_btn_cancel = (Button) dlg.findViewById(R.id.btn_cancel);
        dlg_btn_cancel.setText(mContext.getString(R.string.no));
        dlg_btn_cancel.setVisibility(View.VISIBLE);

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

        dlg_btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                int errorCode = Imps.getErrorByPacketId(packetId);
                int deliveryState = GlobalFunc.getDeliveryState(packetId);
                if ( Imps.deleteMessage(mActivity, cr, packetId)  > 0 ) {
                    requeryCursor();
                    if (Imps.getChatCount(cr, getChatId()) == 0) {      // No more messages in this chatroom, Delete this chat room.
                        Imps.Chats.deleteChat(cr, getChatId());
                        mActivity.finish();
                    }
                }

                if (errorCode == Imps.FileErrorCode.UPLOADING) {
                    GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                    mActivity.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED));
                }
                if (errorCode == Imps.FileErrorCode.DOWNLOADING && deliveryState == 2) {
                    GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
                    mActivity.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
                }

                Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
                mActivity.sendBroadcast(intent);
            }
        });

        dlg.show();

    }

    public void editMessage(String packetId, String msg) {
        if (getChatSession() != null) {
            try {
                getChatSession().editMessage(packetId, null, msg);
                requeryCursor();
            } catch (Exception e) {
                LogCleaner.error(ImApp.TAG, "send message error", e);
            }
        }
    }

    public void sendMessage(String message) {
        if (getChatSession() != null) {
            try {
                getChatSession().sendMessage(message, null, null);
                requeryCursor();
            } catch (Exception e) {
                LogCleaner.error(ImApp.TAG, "send message error", e);
            }
        }
    }

    private void sendMessage() {
        final String msg = mComposeMessage.getText().toString();

        if (TextUtils.isEmpty(msg)) {
            return;
        }

        IChatSession session = getChatSession();

        if (session != null) {
            try {
                session.sendMessage(msg, null, null);
                mComposeMessage.setText("");
                mComposeMessage.requestFocus();
                requeryCursor();
            } catch (Exception e) {
                mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.TAG, "send message error", e);
            }
        } else {
            mComposeMessage.setText("");
            mComposeMessage.requestFocus();
            GlobalFunc.showErrorMessageToast(mContext, 200, false);
        }

        if (mHistory != null && mHistory.getCount() > 0)
            mHistory.setSelection(mHistory.getCount() - 1);
    }

    public void sendMessagesSeen() {
        if (getChatSession() != null) {
            try {
                getChatSession().sendMessagesSeen();
            } catch (Exception e) {
                LogCleaner.error(ImApp.TAG, "send message seen error", e);
            }
        }
    }

    public boolean sendMessage(String msg, String offerId) {

        if (TextUtils.isEmpty(msg.trim())) {
            return false;
        }

        if (getChatSession() != null) {
            try {
                getChatSession().sendMessage(msg, offerId, null);
                requeryCursor();
                return true;
            } catch (Exception e) {
                mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.TAG, "send message error", e);
            }
        }
        return false;
    }

    public void setFocusHistoryView(long delay) {
        if (delay == 0) {
            if (mHistory != null && mHistory.getCount() > 0) {
                mHistory.setSelection(mHistory.getCount() - 1);
                mHistory.invalidate();
            }
            return;
        }
        mHistory.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mHistory != null && mHistory.getCount() > 0) {
                    mHistory.setSelection(mHistory.getCount() - 1);
                    mHistory.invalidate();
                }
            }
        }, delay);
    }

    private void registerChatListener() {
        try {
            if (getChatSession() != null) {
                getChatSession().registerChatListener(mChatListener);
            }

            checkConnection();

            if (mConn != null) {
                IContactListManager listMgr = mConn.getContactListManager();
                listMgr.registerContactListListener(mContactListListener);
            }

        } catch (Exception e) {
        }
    }

    private void unregisterChatListener() {
        try {
            if (getChatSession() != null) {
                getChatSession().unregisterChatListener(mChatListener);
            }
            checkConnection();

            if (mConn != null) {
                IContactListManager listMgr = mConn.getContactListManager();
                listMgr.unregisterContactListListener(mContactListListener);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public boolean dispatchTrackballEvent(MotionEvent ev) {
        return super.dispatchTrackballEvent(ev);
    }


    @SuppressLint("HandlerLeak")
    private final class ChatViewHandler extends SimpleAlertHandler {
        public ChatViewHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            long providerId = ((long) msg.arg1 << 32) | msg.arg2;
            if (providerId != mProviderId) {
                return;
            }

            switch (msg.what) {

                case ImApp.EVENT_CONNECTION_DISCONNECTED:
                    promptDisconnectedEvent(msg);
                    return;
            }

            super.handleMessage(msg);
        }
    }

    public static class DeltaCursor implements Cursor {
        static final String DELTA_COLUMN_NAME = "delta";

        private Cursor mInnerCursor;
        private String[] mColumnNames;
        private int mDateColumn = -1;
        private int mDeltaColumn = -1;

        DeltaCursor(Cursor cursor) {
            mInnerCursor = cursor;

            String[] columnNames = cursor.getColumnNames();
            int len = columnNames.length;

            mColumnNames = new String[len + 1];

            for (int i = 0; i < len; i++) {
                mColumnNames[i] = columnNames[i];
                if (mColumnNames[i].equals(Imps.Messages.DATE)) {
                    mDateColumn = i;
                }
            }

            mDeltaColumn = len;
            mColumnNames[mDeltaColumn] = DELTA_COLUMN_NAME;

        }

        public int getCount() {
            if (mInnerCursor != null)
                return mInnerCursor.getCount();
            else
                return 0;
        }

        public int getPosition() {
            return mInnerCursor.getPosition();
        }

        public boolean move(int offset) {
            return mInnerCursor.move(offset);
        }

        public boolean moveToPosition(int position) {
            return mInnerCursor.moveToPosition(position);
        }

        public boolean moveToFirst() {
            return mInnerCursor.moveToFirst();
        }

        public boolean moveToLast() {
            return mInnerCursor.moveToLast();
        }

        public boolean moveToNext() {
            return mInnerCursor.moveToNext();
        }

        public boolean moveToPrevious() {
            return mInnerCursor.moveToPrevious();
        }

        public boolean isFirst() {
            return mInnerCursor.isFirst();
        }

        public boolean isLast() {
            return mInnerCursor.isLast();
        }

        public boolean isBeforeFirst() {
            return mInnerCursor.isBeforeFirst();
        }

        public boolean isAfterLast() {
            return mInnerCursor.isAfterLast();
        }

        public int getColumnIndex(String columnName) {
            if (DELTA_COLUMN_NAME.equals(columnName)) {
                return mDeltaColumn;
            }

            int columnIndex = mInnerCursor.getColumnIndex(columnName);
            return columnIndex;
        }

        public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
            if (DELTA_COLUMN_NAME.equals(columnName)) {
                return mDeltaColumn;
            }

            return mInnerCursor.getColumnIndexOrThrow(columnName);
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == mDeltaColumn) {
                return DELTA_COLUMN_NAME;
            }

            return mInnerCursor.getColumnName(columnIndex);
        }

        public int getColumnCount() {
            return mInnerCursor.getColumnCount() + 1;
        }

        @SuppressWarnings("deprecation")
        public void deactivate() {
            mInnerCursor.deactivate();
        }

        @SuppressWarnings("deprecation")
        public boolean requery() {
            return mInnerCursor.requery();
        }

        public void close() {
            mInnerCursor.close();
        }

        public boolean isClosed() {
            return mInnerCursor.isClosed();
        }

        public void registerContentObserver(ContentObserver observer) {
            mInnerCursor.registerContentObserver(observer);
        }

        public void unregisterContentObserver(ContentObserver observer) {
            mInnerCursor.unregisterContentObserver(observer);
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            mInnerCursor.registerDataSetObserver(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            mInnerCursor.unregisterDataSetObserver(observer);
        }

        public void setNotificationUri(ContentResolver cr, Uri uri) {
            mInnerCursor.setNotificationUri(cr, uri);
        }

        public boolean getWantsAllOnMoveCalls() {
            return mInnerCursor.getWantsAllOnMoveCalls();
        }

        @Override
        public void setExtras(Bundle extras) {
            mInnerCursor.setExtras(extras);
        }

        public Bundle getExtras() {
            return mInnerCursor.getExtras();
        }

        public Bundle respond(Bundle extras) {
            return mInnerCursor.respond(extras);
        }

        public String[] getColumnNames() {
            return mColumnNames;
        }

        private void checkPosition() {
            int pos = 0;
            int count = 0;
            if (mInnerCursor != null) {
                pos = mInnerCursor.getPosition();
                count = mInnerCursor.getCount();
            }

            if (-1 == pos || count == pos) {
                throw new CursorIndexOutOfBoundsException(pos, count);
            }
        }

        public byte[] getBlob(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return null;
            }

            return mInnerCursor.getBlob(column);
        }

        public String getString(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                long value = getDeltaValue();
                return Long.toString(value);
            }

            return mInnerCursor.getString(column);
        }

        public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
            checkPosition();

            if (columnIndex == mDeltaColumn) {
                long value = getDeltaValue();
                String strValue = Long.toString(value);
                int len = strValue.length();
                char[] data = buffer.data;
                if (data == null || data.length < len) {
                    buffer.data = strValue.toCharArray();
                } else {
                    strValue.getChars(0, len, data, 0);
                }
                buffer.sizeCopied = strValue.length();
            } else {
                mInnerCursor.copyStringToBuffer(columnIndex, buffer);
            }
        }

        public short getShort(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return (short) getDeltaValue();
            }

            return mInnerCursor.getShort(column);
        }

        public int getInt(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return (int) getDeltaValue();
            }

            return mInnerCursor.getInt(column);
        }

        public long getLong(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getLong(column);
        }

        public float getFloat(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getFloat(column);
        }

        public double getDouble(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getDouble(column);
        }

        public boolean isNull(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return false;
            }

            return mInnerCursor.isNull(column);
        }

        private long getDeltaValue() {
            int pos = mInnerCursor.getPosition();
            long t2, t1;
            if (pos == getCount() - 1) {
                t1 = mInnerCursor.getLong(mDateColumn);
                t2 = System.currentTimeMillis();
            } else {
                mInnerCursor.moveToPosition(pos + 1);
                t2 = mInnerCursor.getLong(mDateColumn);
                mInnerCursor.moveToPosition(pos);
                t1 = mInnerCursor.getLong(mDateColumn);
            }
            return t2 - t1;
        }

        public int getType(int arg0) {
            return 0;
        }

        public Uri getNotificationUri() {
            return null;
        }
    }


    public class MessageAdapter extends CursorAdapter implements ListView.OnScrollListener {
        private int mScrollState;
        private boolean mNeedRequeryCursor;
        private LayoutInflater mInflater;

        public MessageAdapter(Activity context, Cursor c) {
            super(context, c, false);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (c != null) {
                resolveColumnIndex(c);
            }
        }

        private void resolveColumnIndex(Cursor c) {
            mNicknameColumn = c.getColumnIndexOrThrow(Imps.Messages.NICKNAME);
            mBodyColumn = c.getColumnIndexOrThrow(Imps.Messages.BODY);
            mDateColumn = c.getColumnIndexOrThrow(Imps.Messages.DATE);
            mTypeColumn = c.getColumnIndexOrThrow(Imps.Messages.TYPE);
            mErrCodeColumn = c.getColumnIndexOrThrow(Imps.Messages.ERROR_CODE);
            mDeliveredColumn = c.getColumnIndexOrThrow(Imps.Messages.IS_DELIVERED);
            mMimeTypeColumn = c.getColumnIndexOrThrow(Imps.Messages.MIME_TYPE);
            mIdColumn = c.getColumnIndexOrThrow(Imps.Messages._ID);
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (getCursor() != null && (!getCursor().isClosed()))
                getCursor().close();
            super.changeCursor(cursor);
        }

        @Override
        public int getItemViewType(int position) {
            Cursor c = getCursor();
            c.moveToPosition(position);
            int type = c.getInt(mTypeColumn);
            boolean isLeft = (type == Imps.MessageType.INCOMING) || (type == Imps.MessageType.DELETED_INCOMING);
            if (isLeft)
                return 0;
            else
                return 1;
        }

        @Override
        public int getViewTypeCount() {
            if (isPlusFriend == 1)
                return 1;
            else
                return 2;
        }

        @SuppressLint("InflateParams")
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View result;
            if (isPlusFriend == 1) {
                result = mInflater.inflate(R.layout.message_view_left, null);
            } else {
                int type = getItemViewType(cursor.getPosition());
                if (type == 0)
                    result = mInflater.inflate(R.layout.message_view_left, null);
                else
                    result = mInflater.inflate(R.layout.message_view_right, null);
            }
            return result;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            MessageView messageView = (MessageView) view;

            int type = cursor.getInt(mTypeColumn);
            String nickname = isGroupChat() ? cursor.getString(mNicknameColumn) : mRemoteNickname;
            String mimeType = cursor.getString(mMimeTypeColumn);
            int id = cursor.getInt(mIdColumn);
            long timestamp = cursor.getLong(mDateColumn);
            String body = cursor.getString(mBodyColumn);
            String packetId = cursor.getString(mPacketIdColumn);
            Date date = new Date(timestamp);
            int thumbnailWidth = cursor.getInt(mThumbnailWidthColumn);
            int thumbnailHeight = cursor.getInt(mThumbnailHeightColumn);
            int delivery = cursor.getInt(mDeliveredColumn);
            String samplePath = cursor.getString(mSampleImagePathColumn);
            int errCode = cursor.getInt(mErrCodeColumn);
            int displaySentTime = cursor.getInt(mDisplaySentTimeColumn);
            long servertime = cursor.getLong(mServerTimeColumn);
            Date serverDate = new Date(servertime);
            if (servertime == 0)
                serverDate = null;
            boolean showDate = false;

            if (!cursor.isFirst()) {
                DateFormat prevDateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
                DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
                TimeZone serverTimeZone = TimeZone.getTimeZone(GlobalConstrants.DEFAULT_TIMEZONE);
                boolean isLeft = (type == Imps.MessageType.INCOMING) || (type == Imps.MessageType.DELETED_INCOMING);
//                if (isLeft) {
//                    dateFormat.setTimeZone(serverTimeZone);
//                }

                if (cursor.moveToPrevious()) {
                    int prevType = cursor.getInt(mTypeColumn);
                    boolean prevIsLeft = (prevType == Imps.MessageType.INCOMING) || (prevType == Imps.MessageType.DELETED_INCOMING);
                    long preTimeStamp = cursor.getLong(mDateColumn);
                    Date preDate = new Date(preTimeStamp);

//                    if (prevIsLeft) {
//                        prevDateFormat.setTimeZone(serverTimeZone);
//                    }

                    if (!dateFormat.format(date).equals(prevDateFormat.format(preDate))) {
                        showDate = true;
                    }

                    cursor.moveToNext();
                }
            } else {
                showDate = true;
            }

            if (cursor.isLast()) {
                last_packet_id = packetId;
            }

            final MessageItem item = new MessageItem();
            item.id = String.valueOf(id);
            item.packetID = packetId;
            item.type = type;

            switch (type) {
                case Imps.MessageType.INCOMING:
                    if (body != null) {
                        if (isGroupChat()) {
                            String jid = null;
                            String name = StringUtils.parseName(nickname);
                            jid = StringUtils.parseResource(nickname) + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
                            messageView.bindIncomingMessage(id, jid, nickname, mimeType, body, date, isScrolling(), isGroupChat(), packetId, showDate,
                                    name, thumbnailWidth, thumbnailHeight, samplePath, displaySentTime, errCode, type, serverDate, delivery,  mActivity.messageItemIsChecked(item));
                        } else
                            messageView.bindIncomingMessage(id, mRemoteAddress, nickname, mimeType, body, date, isScrolling(), isGroupChat(), packetId, showDate,
                                    userXmppAddress, thumbnailWidth, thumbnailHeight, samplePath, displaySentTime, errCode, type, serverDate, delivery, mActivity.messageItemIsChecked(item));
                    }
                    break;

                case Imps.MessageType.OUTGOING:
                case Imps.MessageType.POSTPONED:
                    if (errCode == 0) {
                    }
                    messageView.bindOutgoingMessage(id, mRemoteAddress, nickname, mimeType, body, date, isScrolling(),
                            delivery, packetId, showDate, userXmppAddress, isGroupChat(), thumbnailWidth, thumbnailHeight, samplePath, errCode, type, serverDate, mActivity.messageItemIsChecked(item));
                    break;
                case Imps.MessageType.FRIEND_INVITE:
                    if (body != null) {
                        messageView.bindInviteMessage(body, id, packetId, type, mActivity.messageItemIsChecked(item));
                    }
                    break;

                case Imps.MessageType.DELETED_INCOMING:
                    break;

                case Imps.MessageType.DELETED_OUTGOING:
                    break;

                case Imps.MessageType.OPER_DELETE:
                    // messageView.bindOperMessage();
                    break;

                default:
                    messageView.bindPresenceMessage(nickname, type, isGroupChat(), isScrolling(), date, id, packetId, type, mActivity.messageItemIsChecked(item));
            }

        }


        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            boolean loadMore = firstVisibleItem <= 0;
            if (isLoadingMore != 1 && loadMore && mScrollState != SCROLL_STATE_IDLE) {
                isLoadingMore = 1;
                nChatHistoryCount = nChatHistoryCount + nChatHistoryMoreCount;
                scheduleRequery(100);
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mScrollState = scrollState;
            if (mScrollState == 0) {
                if (mNeedRequeryCursor) {
                    requeryCursor();
                    mNeedRequeryCursor = false;
                } else {
                    notifyDataSetChanged();
                }
            }
        }

        boolean isScrolling() {
            return mScrollState == OnScrollListener.SCROLL_STATE_FLING;
        }

        void setNeedRequeryCursor(boolean requeryCursor) {
            mNeedRequeryCursor = requeryCursor;
        }
    }

    public void onServiceConnected() {
        if (!isServiceUp) {
            bindChat(mChatId, isPlusFriend);
            startListening();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnVoiceMessage:
                if (isGroupChat()) break;
                mBtnVoiceMessage.setSelected(!mBtnVoiceMessage.isSelected());
                if (mShowEmoticon)
                    hideEmoticon();

                if (mBtnVoiceMessage.isSelected()) {
                    if (GlobalFunc.hasCalling()) {
                        GlobalFunc.showToast(mActivity, getResources().getString(R.string.disable_use_recorder_in_calling), true);
                    } else {
                        startMicRecord();
                    }
                } else {
                    endMicRecord();
                }
                break;
        }
    }

    private void endMicRecord() {
        setFocusHistoryView(500);
        mLayoutHoldToTalk.setVisibility(View.GONE);
        mLayoutInputText.setVisibility(View.VISIBLE);
        mBtnEmoticon.setVisibility(VISIBLE);
        mBtnSend.setVisibility(VISIBLE);
        AndroidUtility.hideKeyboard(this);
    }

    private void startMicRecord() {
        setFocusHistoryView(500);
        mLayoutHoldToTalk.setVisibility(View.VISIBLE);
        mLayoutInputText.setVisibility(View.GONE);
        mBtnEmoticon.setVisibility(GONE);
        mBtnSend.setVisibility(GONE);
        mShowEmoticon = false;
        AndroidUtility.hideKeyboard(this);
    }

    public void cancelRecording() {
        if (mAudioStatus == RECORD_STATUS) {
            stopRecording();
            File file = null;
            if (audioFilePath != null)
                file = new File(audioFilePath);
            if (file != null && file.isFile())
                file.delete();
        }
    }

    private void hideInputMethod() {
        if (mActivity.getWindow() != null && mActivity.getWindow().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mActivity.getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showKeyBoard(boolean isShow) {
        if (isShow) {
            mComposeMessage.requestFocus();
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mComposeMessage, 0);
        } else {
            hideInputMethod();
        }
    }

    private void initShareAndEmoticonRessource() {
        LinearLayout container = (LinearLayout)findViewById(R.id.panel_container);
        LayoutInflater factory = LayoutInflater.from(mActivity);
        factory.inflate(R.layout.emoticon_panel_common, container);

        mEmoticonPanel = (IpMessageEmoticonPanel) container.findViewById(R.id.emoticon_panel);

        mEmoticonPanel.setHandler(null);
        mEmoticonPanel.setEditEmoticonListener(new IpMessageEmoticonPanel.EditEmoticonListener() {
            @Override
            public void doAction(int type, String emotionName) {
                switch (type) {
                    case IpMessageEmoticonPanel.EditEmoticonListener.addEmoticon:
                        addMessage(emotionName);
                        break;
                    case IpMessageEmoticonPanel.EditEmoticonListener.delEmoticon:
                        deleteEmoticon();
                        break;
                    case IpMessageEmoticonPanel.EditEmoticonListener.addTemplate:
                        insertText(mComposeMessage, emotionName);
                        break;
                    case IpMessageEmoticonPanel.EditEmoticonListener.addSpace:
                        if (mComposeMessage != null) {
                            KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE);
                            mComposeMessage.onKeyDown(KeyEvent.KEYCODE_SPACE, keyEventDown);
                        }
                        break;
                    case IpMessageEmoticonPanel.EditEmoticonListener.addEnter:
                        if (mComposeMessage != null) {
                            KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
                            mComposeMessage.onKeyDown(KeyEvent.KEYCODE_ENTER, keyEventDown);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void addMessage(String message) {
        int start = Math.max(mComposeMessage.getSelectionStart(), 0);
        int end = Math.max(mComposeMessage.getSelectionEnd(), 0);
        mComposeMessage.getText().replace(Math.min(start, end), Math.max(start, end),
                SmileyParser.getInstance(mContext).addSmileySpans(message, mComposeMessage.getLineHeight()), 0, message.length());
    }

    private void deleteEmoticon() {
        Editable edit = mComposeMessage.getEditableText();
        int length = edit.length();
        int cursor = mComposeMessage.getSelectionStart();
        if (length == 0 || cursor == 0) {
            return;
        }
        ImageSpan[] spans = edit.getSpans(0, cursor, ImageSpan.class);
        ImageSpan span = null;
        int index = 0;
        if (null != spans && spans.length != 0) {
            span = spans[spans.length - 1];
            index = edit.getSpanEnd(span);
        }
        if (index == cursor) {
            int start = edit.getSpanStart(span);
            edit.delete(start, cursor);
        } else {
            if (mComposeMessage != null && !TextUtils.isEmpty(mComposeMessage.getText().toString())) {
                KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                mComposeMessage.onKeyDown(KeyEvent.KEYCODE_DEL, keyEventDown);
            }
        }
    }

    private void insertText(EditText edit, String insertText){
        int where = edit.getSelectionStart();

        if (where == -1) {
            edit.append(insertText);
        } else {
            edit.getText().insert(where, insertText);
        }
    }
}
