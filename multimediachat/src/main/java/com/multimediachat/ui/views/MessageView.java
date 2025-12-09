
package com.multimediachat.ui.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.ui.FriendProfileChattingActivity;
import com.multimediachat.util.ImageLoaderUtil;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.ChatRoomActivity;
import com.multimediachat.ui.MyProfileActivity;
import com.multimediachat.ui.ShowChatImageActivity;
import com.multimediachat.util.Utilities;
import com.multimediachat.util.datamodel.MessageItem;
import com.multimediachat.util.datamodel.ProgressData;
import com.multimediachat.util.datamodel.ProgressValue;
import com.multimediachat.util.SampleImageBackgroundLoader;
import com.multimediachat.ui.emotic.SmileyParser;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.multimediachat.global.GlobalConstrants.AUDIO_LOG;
import static com.multimediachat.global.GlobalConstrants.VIDEO_LOG;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MessageView extends LinearLayout {

    public static SampleImageBackgroundLoader mSampleImageBackgroundLoader = null;

    static HashMap<String, String> mGroupMemberNickNameList = null;
    static ImApp mApp;
    static int mVoiceProgress = 0;
    public static String mPlayingVoicePacketId = null;
    static int mVoiceLength = 0;
    public static MediaPlayer mVoicePlayer = null;
    static ShowUpDownProgress mShowUpDownProgress = null;
    static Map<ProgressBar, String> mUpDownProgressViews = null;
    static ShowAudioProgress mShowAudioProgress = null;

    private Context mContext;
    private ChatRoomActivity mActivity;
    public static int image_height = 0;
    public static int image_width = 0;

    private static final int ID_COPY_MESSAGE = 1;
    private static final int ID_QUOTE_MESSAGE = 2;
    private static final int ID_DELETE_MESSAGE = 4;
    private static final int ID_RETRY = 5;
    private static final int ID_SAVE_FILE = 6;
    private static final int ID_PROPERTY = 7;

    public static final int MESSAGE_TYPE_IMAGE = 0;
    public static final int MESSAGE_TYPE_VIDEO = 1;
    public static final int MESSAGE_TYPE_AUDIO = 2;

    private CharSequence lastMessage = null;

    private final DateFormat MESSAGE_DATE_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.LONG, Resources.getSystem().getConfiguration().locale);

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SmileyParser.init(context);
        mContext = context;
        mActivity = (ChatRoomActivity) context;

        if (mGroupMemberNickNameList == null)
            mGroupMemberNickNameList = new HashMap<String, String>();

        if (mSampleImageBackgroundLoader == null)
            mSampleImageBackgroundLoader = new SampleImageBackgroundLoader(context);

        if (image_height == 0)
            image_height = mContext.getResources().getDimensionPixelSize(R.dimen.chat_image_height);
        if (image_width == 0)
            image_width = mContext.getResources().getDimensionPixelSize(R.dimen.chat_image_width);


        if (mVoicePlayer == null)
            mVoicePlayer = new MediaPlayer();

        if (mShowUpDownProgress == null) {
            mShowUpDownProgress = new ShowUpDownProgress();
        }
    }

    public static void startUpDownProgressThread() {
        if (mShowUpDownProgress != null && (!mShowUpDownProgress.isCancelled() || !mShowUpDownProgress.isStopped()))
            mShowUpDownProgress.setStop(true);

        if (mUpDownProgressViews == null) {
            mUpDownProgressViews = new HashMap<ProgressBar, String>();
        }

        mShowUpDownProgress = new ShowUpDownProgress();
        mShowUpDownProgress.execute();
    }

    public static void stopUpDownProgressThread() {
        if (mShowUpDownProgress != null) {
            mShowUpDownProgress.setStop(true);
        }
    }

    private ViewHolder mHolder = null;



    class ViewHolder {
        View mRootView = findViewById(R.id.rootView);
        View mDateLayout = (View) findViewById(R.id.layout_chat_date);
        View mMessageLayout = (View) findViewById(R.id.layout_chat_message);
        View mLayoutForMessages = (View) findViewById(R.id.text_layout);
        TextView mTextViewForMessages = (TextView) findViewById(R.id.message_text);
        ImageView mCallIcon = (ImageView) findViewById(R.id.call_icon);
        ImageView mMediaThumbnail = (ImageView) findViewById(R.id.media_thumbnail);
        ImageView mMediaVideoIcon = (ImageView) findViewById(R.id.media_video_icon);
        View mMediaLayout = (View) findViewById(R.id.media_layout);
        CircularImageView mProfileImg = (CircularImageView) findViewById(R.id.imgProfile);
        TextView mTxtDelivery = (TextView) findViewById(R.id.txtDelivery);
        TextView mTextViewForTimestamp = (TextView) findViewById(R.id.messagets);
        ImageView mImgSendMsg = (ImageView) findViewById(R.id.ic_send_msg);
        ImageView mImgReceivedMsg = (ImageView) findViewById(R.id.ic_received_msg);
        TextView mFromName = (TextView) findViewById(R.id.fromName);
        ImageView mAudioPlay = (ImageView) findViewById(R.id.btnPlay);
        TextView mAudioTime = (TextView) findViewById(R.id.audioTime);
        ProgressBar mProgressUploadOrDownload = (ProgressBar) findViewById(R.id.progressUploadOrDownload);
        View mAudioClickingArea = findViewById(R.id.audioClickingArea);
        ImageView mNoDownload = findViewById(R.id.noDownload);
        View mMediaDownload = findViewById(R.id.btnMediaDownload);
        View mMediaDownloadCancel = findViewById(R.id.btnMediaDownloadCancel);
        View mMaskView = findViewById(R.id.maskView);
        ProgressBar loading = findViewById(R.id.loading);
        ImageView mBtnCheck = findViewById(R.id.btn_check);
        ProgressBar mAudioProgressBar = findViewById(R.id.audioProgressBar);

        public void setOnClickListenerMediaThumbnail(final String mimeType, final String body, final Boolean show) {
            mMediaLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mActivity.isSelecteMode) {
                        if ((mMediaDownload.getVisibility() != VISIBLE) && (mMediaDownloadCancel.getVisibility() != VISIBLE)
                                && (mProgressUploadOrDownload.getVisibility() != VISIBLE)) {
                            onClickMediaIcon(mimeType, body, show);
                        }
                    } else {
                        mHolder.mRootView.callOnClick();
                    }
                }
            });
        }

        public void resetOnClickListenerMediaThumbnail() {
            mMediaLayout.setOnLongClickListener(null);
            mAudioClickingArea.setOnLongClickListener(null);
            mLayoutForMessages.setOnLongClickListener(null);

            mLayoutForMessages.setOnClickListener(null);
            mMediaLayout.setOnClickListener(null);
            mAudioClickingArea.setOnClickListener(null);

            mLayoutForMessages.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int location[] = new int[2];
                    mLayoutForMessages.getLocationInWindow(location);
                    mActivity.touchPos[0] = (int) event.getX() + location[0];
                    mActivity.touchPos[1] = (int) event.getY() + location[1];
                    return false;
                }
            });
            mMediaLayout.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int location[] = new int[2];
                    mLayoutForMessages.getLocationInWindow(location);
                    mActivity.touchPos[0] = (int) event.getX() + location[0];
                    mActivity.touchPos[1] = (int) event.getY() + location[1];
                    return false;
                }
            });
            mAudioClickingArea.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int location[] = new int[2];
                    mLayoutForMessages.getLocationInWindow(location);
                    mActivity.touchPos[0] = (int) event.getX() + location[0];
                    mActivity.touchPos[1] = (int) event.getY() + location[1];
                    return false;
                }
            });

        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHolder = (ViewHolder) getTag();
        if (mApp == null)
            mApp = ImApp.getInstance();

        if (mHolder == null) {
            mHolder = new ViewHolder();
            setTag(mHolder);
        }
    }

    public URLSpan[] getMessageLinks() {
        return mHolder.mTextViewForMessages.getUrls();
    }

    public String getLastMessage() {
        return lastMessage.toString();
    }

    public String getGroupMemberNickName(String addr) {
        String nickName = mGroupMemberNickNameList.get(addr);
        if (nickName != null)
            return nickName;

        Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, mActivity.getChatId());
        Cursor cursor = mContext.getContentResolver().query(uri,
                new String[]{Imps.GroupMembers.NICKNAME, Imps.GroupMembers.USERNAME, Imps.GroupMembers.TYPE},
                Imps.GroupMembers.USERNAME + "=?", new String[]{addr}, null);
        if (cursor != null && cursor.moveToFirst()) {
            nickName = cursor.getString(cursor.getColumnIndex(Imps.GroupMembers.NICKNAME));
            if (!mGroupMemberNickNameList.containsKey(addr) && nickName != null)
                mGroupMemberNickNameList.put(addr, nickName);
            cursor.close();
            return nickName;
        }

        if (cursor != null)
            cursor.close();

        return null;
    }

	/*
     * author:cys created time:2014-04-01:6:22 pm download file and play, insert
	 * message to db
	 */

    private MediaPlayer mMediaPlayer = null;

    /**
     * @param mimeType
     * @param body
     */
    protected void onClickMediaIcon(final String mimeType, String body, boolean show) {

        File file = new File(body);
        if (!file.exists()) {
            GlobalFunc.showToast(getContext(), R.string.error_file_no_exist, true);
            return;
        }

        if (!show)
            return;

        if (mimeType.startsWith("image")) {
            showThumbnail(mimeType, body);
            return;
        }

        // new file - scan
        Uri mediaUri = Uri.parse(body);
        if (mediaUri.getScheme() != null) {
            showThumbnail(mimeType, body);
            return;
        }

        file = new File(mediaUri.getPath());
        final Handler handler = new Handler();

        MediaScannerConnection.scanFile(getContext(), new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, final Uri uri) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (uri != null)
                                    showThumbnail(mimeType, uri.toString());
                            }
                        });
                    }
                });

    }

    private int dpTopx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, getResources().getDisplayMetrics());
    }

    protected void showThumbnail(String mimeType, String body) {
        if (mimeType.startsWith("audio") || body.endsWith("amr")) {
            if (mMediaPlayer != null)
                mMediaPlayer.release();

            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(body);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                return;
            } catch (IOException e) {
                DebugConfig.error(ImApp.LOG_TAG, "error playing audio: " + body, e);
            }
        }

        if (mimeType.startsWith("image/")) {
            Intent intent = new Intent(mActivity, ShowChatImageActivity.class);
            intent.putExtra("filePath", body);
            intent.putExtra("chatId", mActivity.getChatId());
            mActivity.startActivity(intent);
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            // set a general mime type not specific
            if (mimeType != null) {
                intent.setDataAndType(Uri.parse(body), mimeType);
            }

            if (isIntentAvailable(mContext, intent)) {
                mContext.startActivity(intent);
            } else {
                GlobalFunc.showToast(getContext(), R.string.unknown_file_format, true);
            }
        } catch (Exception e) {
        }
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @SuppressWarnings("deprecation")
    public void bindIncomingMessage(final int id, final String address, final String nickname, final String mimeType,
                                    final String body, final Date date, boolean scrolling, boolean showContact, final String packetId,
                                    boolean showDate, final String myAddress, int thumbnailWidth, int thumbnailHeight,
                                    final String samplePath, int displaySentTime, final int err_code, final int messageType, final Date serverdate,
                                    final int deliveryState, final boolean isChecked) {

        mHolder = (ViewHolder) getTag();

        mHolder.mMessageLayout.setVisibility(View.VISIBLE);
        mHolder.mTxtDelivery.setVisibility(View.GONE);
        mHolder.mTxtDelivery.setTextColor(getResources().getColor(R.color.blue));
        mHolder.mAudioClickingArea.setVisibility(View.GONE);
        mHolder.mMediaDownload.setVisibility(View.GONE);
        mHolder.mProgressUploadOrDownload.setVisibility(View.GONE);
        mHolder.mMaskView.setVisibility(View.GONE);
        mHolder.mMediaVideoIcon.setVisibility(View.GONE);
        mHolder.mMediaDownloadCancel.setVisibility(View.GONE);
        mHolder.mCallIcon.setVisibility(View.GONE);
        mHolder.mNoDownload.setVisibility(View.GONE);
        mHolder.loading.setVisibility(GONE);
        mHolder.mBtnCheck.setVisibility(mActivity.isSelecteMode ? VISIBLE : GONE);
        mHolder.resetOnClickListenerMediaThumbnail();

        mHolder.mRootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.isSelecteMode) {
                    mHolder.mBtnCheck.setSelected(!mHolder.mBtnCheck.isSelected());
                    MessageItem item = new MessageItem();
                    item.id = String.valueOf(id);
                    item.packetID = packetId;
                    item.type = messageType;
                    if (mHolder.mBtnCheck.isSelected()) {
                        mActivity.addMessage(item);
                    } else {
                        mActivity.removeMessage(item);
                    }
                    mActivity.mLytDeleteSel.setEnabled(mActivity.selectedMessages.size() > 0);
                }
            }
        });


        ImageLoaderUtil.loadAvatarImage(mContext, address, mHolder.mProfileImg, 0);
        // showAvatar(address, true);

        mHolder.mProfileImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FriendProfileChattingActivity.class);
                FriendProfileChattingActivity.contact = null;
                intent.putExtra("mFlag", false);
                intent.putExtra("username", address);
                mActivity.startActivity(intent);
            }
        });

        if (nickname != null) {
            String[] nickParts = nickname.split("/");

            String addr = nickParts[nickParts.length - 1];
            mHolder.mFromName.setVisibility(View.VISIBLE);

            String nickName = getGroupMemberNickName(addr + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain));

            if (nickName != null)
                addr = nickName;

            mHolder.mFromName.setText(addr);

        } else
            mHolder.mFromName.setText("");

        mHolder.mFromName.setSelected(true);
        mHolder.mBtnCheck.setSelected(isChecked);

        if (mimeType != null) {
            mUpDownProgressViews.put(mHolder.mProgressUploadOrDownload, packetId);

            int type = 0;
            if (mimeType.startsWith("image/"))
                type = MESSAGE_TYPE_IMAGE;
            else if (mimeType.startsWith("video/"))
                type = MESSAGE_TYPE_VIDEO;
            else if (mimeType.startsWith("audio/"))
                type = MESSAGE_TYPE_AUDIO;

            mHolder.mMediaLayout.setVisibility(View.VISIBLE);
            mHolder.mLayoutForMessages.setVisibility(View.GONE);
            mHolder.mMediaLayout.setBackgroundResource(R.drawable.chat_from_item_bg);
            mHolder.mMediaLayout.setPadding(dpTopx(0), dpTopx(0), dpTopx(0), dpTopx(0));

            if (type == MESSAGE_TYPE_IMAGE)
                mHolder.mNoDownload.setImageResource(R.drawable.download_image);
            else if (type == MESSAGE_TYPE_VIDEO)
                mHolder.mNoDownload.setImageResource(R.drawable.download_video);
            else if (type == MESSAGE_TYPE_AUDIO)
                mHolder.mNoDownload.setImageResource(R.drawable.download_audio);

            if (err_code == Imps.FileErrorCode.DOWNLOADSUCCESS) {

                if (type == MESSAGE_TYPE_IMAGE || type == MESSAGE_TYPE_VIDEO) {
                    showProgressCursor(packetId);
                    if (samplePath == null) {
                        mHolder.mMediaThumbnail.setImageDrawable(null);
                        mHolder.mNoDownload.setVisibility(View.GONE);
                        if (thumbnailWidth > 0 && thumbnailHeight > 0) {
                            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mHolder.mMediaThumbnail.getLayoutParams();
                            if (thumbnailWidth > thumbnailHeight) {
                                params1.width = MessageView.image_height;
                                params1.height = params1.width * thumbnailHeight / thumbnailWidth;
                            } else {
                                params1.height = MessageView.image_height;
                                params1.width = params1.height * thumbnailWidth / thumbnailHeight;
                            }
                            mHolder.mMediaThumbnail.setLayoutParams(params1);
                        } else {
                            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mHolder.mMediaThumbnail.getLayoutParams();
                            params1.width = MessageView.image_width;
                            params1.height = MessageView.image_height;
                            mHolder.mMediaThumbnail.setLayoutParams(params1);
                        }
                    } else {
                        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mHolder.mMediaThumbnail.getLayoutParams();
                        params1.width = thumbnailWidth;
                        params1.height = thumbnailHeight;
                        mHolder.mMediaThumbnail.setLayoutParams(params1);
                        mSampleImageBackgroundLoader.loadBitmap(samplePath, mHolder.mMediaThumbnail, null, true);
                    }
                } else { // type == MESSAGE_TYPE_AUDIO
                    mHolder.mMediaLayout.setVisibility(View.GONE);
                    mHolder.mAudioClickingArea.setVisibility(View.VISIBLE);
                    showAudio(mHolder, packetId, body, thumbnailWidth, true, displaySentTime);
                }

                if (type == MESSAGE_TYPE_VIDEO)
                    mHolder.mMediaVideoIcon.setVisibility(VISIBLE);

            } else if (err_code == Imps.FileErrorCode.DOWNLOADING) {

                mHolder.mMediaThumbnail.setImageDrawable(null);
                RelativeLayout.LayoutParams imageSize = (RelativeLayout.LayoutParams) mHolder.mMediaThumbnail.getLayoutParams();
                imageSize.width = MessageView.image_width;
                imageSize.height = MessageView.image_height;
                mHolder.mMediaThumbnail.setLayoutParams(imageSize);
                mHolder.mMediaDownload.setVisibility(View.GONE);
                mHolder.mNoDownload.setVisibility(View.VISIBLE);
                if (deliveryState == 2) {
                    showProgressCursor(packetId);
                } else {
                    mHolder.mTxtDelivery.setText(mContext.getString(R.string.inReady));
                    mHolder.mTxtDelivery.setVisibility(VISIBLE);
                    mHolder.loading.setVisibility(VISIBLE);
                }

            } else if ((err_code == Imps.FileErrorCode.DOWNLOADFAILED) || (err_code == Imps.FileErrorCode.DOWNLOADCANCELLED)) {
                mHolder.mMediaDownload.setVisibility(View.VISIBLE);
                mHolder.mMediaDownload.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mActivity.isSelecteMode) {
                            if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
                                GlobalFunc.showToast(mContext, R.string.network_error, false);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                return;
                            }
                            if (GlobalFunc.getDownloadingStatus() == GlobalConstrants.NO_DOWNLOADING) {
                                Imps.updateDeliveryStateInDb(mContext.getContentResolver(), packetId, 0);
                                Imps.updateOperMessageError(mContext.getContentResolver(), packetId, Imps.FileErrorCode.DOWNLOADING);
                                mActivity.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
                                mHolder.mProgressUploadOrDownload.setProgress(0);
                                showProgressCursor(packetId);
                            } else {
                                Imps.updateOperMessageError(mContext.getContentResolver(), packetId, Imps.FileErrorCode.DOWNLOADING);
                                Imps.updateDeliveryStateInDb(mContext.getContentResolver(), packetId, 0);
                            }
                            mHolder.mMediaDownload.setVisibility(View.GONE);
                        } else {
                            mHolder.mRootView.callOnClick();
                        }
                    }
                });
                mHolder.mMediaThumbnail.setImageDrawable(null);
                RelativeLayout.LayoutParams imageSize = (RelativeLayout.LayoutParams) mHolder.mMediaThumbnail.getLayoutParams();
                imageSize.width = MessageView.image_width;
                imageSize.height = MessageView.image_height;
                mHolder.mMediaThumbnail.setLayoutParams(imageSize);
                mHolder.mNoDownload.setVisibility(View.VISIBLE);

                if (err_code == Imps.FileErrorCode.DOWNLOADCANCELLED) {
                    mHolder.mTxtDelivery.setText(mContext.getString(R.string.file_upload_cancelled));
                    mHolder.mTxtDelivery.setTextColor(getResources().getColor(R.color.red));
                    mHolder.mTxtDelivery.setVisibility(VISIBLE);
                }
            }

            if (mHolder.mMediaLayout.getVisibility() == View.VISIBLE) {
                boolean showThumb;
                showThumb = !mApp.containDownloadThread(packetId);
                mHolder.setOnClickListenerMediaThumbnail(mimeType, body, showThumb);
            }
            mHolder.mMediaLayout.setTag(null);
        } else {
            mHolder.mTxtDelivery.setVisibility(View.GONE);
            mHolder.mMediaLayout.setVisibility(View.GONE);

            if (android.os.Build.VERSION.SDK_INT >= 16)
                mHolder.mLayoutForMessages
                        .setBackground(mContext.getResources().getDrawable(R.drawable.chat_from_item_bg));
            else
                mHolder.mLayoutForMessages
                        .setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.chat_from_item_bg));

            if (packetId.startsWith(VIDEO_LOG)) {
                lastMessage = body.substring(String.format("[%s] ", mContext.getString(R.string.video_call)).length());
                mHolder.mCallIcon.setImageResource(R.drawable.callvideo_normal);
                mHolder.mCallIcon.setVisibility(View.VISIBLE);
            } else if (packetId.startsWith(AUDIO_LOG)) {
                lastMessage = body.substring(String.format("[%s] ", mContext.getString(R.string.audio_call)).length());
                mHolder.mCallIcon.setImageResource(R.drawable.callvoice_normal);
                mHolder.mCallIcon.setVisibility(View.VISIBLE);
            } else
                lastMessage = body;

            SpannableString spannablecontent;// =new
            if (err_code == Imps.MessageErrorCode.MODIFIED) {
                spannablecontent = new SpannableString(
                        SmileyParser.getInstance(mContext).addSmileySpans(lastMessage + "    ", mHolder.mTextViewForMessages.getLineHeight()));
                Drawable d = getResources().getDrawable(R.drawable.ic_pen_left);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                spannablecontent.setSpan(span, spannablecontent.length() - 3, spannablecontent.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannablecontent = new SpannableString(SmileyParser.getInstance(mContext).addSmileySpans(lastMessage, mHolder.mTextViewForMessages.getLineHeight()));
            }

            mHolder.mLayoutForMessages.setVisibility(View.VISIBLE);

            mHolder.mTextViewForMessages.setVisibility(View.VISIBLE);
            mHolder.mTextViewForMessages.setText(spannablecontent);
            mHolder.mMediaLayout.setTag(null);

        }

        if (date != null) {
            CharSequence tsText = null;
            DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(getContext());
//            TimeZone timeZone = TimeZone.getTimeZone(GlobalConstrants.DEFAULT_TIMEZONE);
//            dateFormat.setTimeZone(timeZone);
            //tsText = formatTimeStamp(date,messageType,dateFormat, null, body);
            tsText = formatTimeStamp(date, dateFormat);
            mHolder.mTextViewForTimestamp.setText(tsText);
        }else
            mHolder.mTextViewForTimestamp.setText("");

        OnLongClickListener showMenuListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mActivity.isSelecteMode) {
                    showMessageOptionMenu(mimeType, id, packetId, body, samplePath, messageType, err_code, false, nickname, serverdate, date);
                }
                return false;
            }
        };

        mHolder.mLayoutForMessages.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.isSelecteMode)
                    mHolder.mRootView.callOnClick();
                if (packetId.startsWith(VIDEO_LOG))
                    mActivity.startChooseVideo();
                else if (packetId.startsWith(AUDIO_LOG))
                    mActivity.startChooseVoice();
            }
        });

        mHolder.mLayoutForMessages.setOnLongClickListener(showMenuListener);
        mHolder.mMediaLayout.setOnLongClickListener(showMenuListener);
        mHolder.mAudioClickingArea.setOnLongClickListener(showMenuListener);

        if (showDate) {
            mHolder.mDateLayout.setVisibility(View.VISIBLE);
            TextView timeContent = (TextView) mHolder.mDateLayout.findViewById(R.id.txtContent);
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
//            TimeZone timeZone = TimeZone.getTimeZone(GlobalConstrants.DEFAULT_TIMEZONE);
//            dateFormat.setTimeZone(timeZone);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E", Resources.getSystem().getConfiguration().locale);
            String dayInString = simpleDateFormat.format(date);
            timeContent.setText(dateFormat.format(date) +  ", " + dayInString);
        } else {
            mHolder.mDateLayout.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("deprecation")
    public void bindOutgoingMessage(final int id, final String address, final String nickname, final String mimeType, final String body,
                                    final Date date, boolean scrolling, final int delivery, final String packetId, boolean showDate,
                                    String myAddress, boolean isGroup, int thumbnailWidth, int thumbnailHeight, final String samplePath,
                                    final int err_code, final int messageType, final Date serverdate, final boolean isChecked) {

        mHolder = (ViewHolder) getTag();
        mHolder.mMessageLayout.setVisibility(View.VISIBLE);
        mHolder.mProgressUploadOrDownload.setVisibility(View.GONE);
        mHolder.mMediaDownload.setVisibility(View.GONE);
        mHolder.mMediaDownloadCancel.setVisibility(View.GONE);
        mHolder.mLayoutForMessages.setVisibility(View.VISIBLE);
        mHolder.mAudioClickingArea.setVisibility(View.GONE);
        mHolder.mMaskView.setVisibility(View.GONE);
        mHolder.mMediaVideoIcon.setVisibility(View.GONE);
        mHolder.mCallIcon.setVisibility(View.GONE);
        mHolder.loading.setVisibility(GONE);
        mHolder.mImgSendMsg.setVisibility(GONE);
        mHolder.mImgReceivedMsg.setVisibility(GONE);
        mHolder.mTxtDelivery.setVisibility(View.GONE);
        mHolder.mTxtDelivery.setTextColor(getResources().getColor(R.color.blue));
        mHolder.mNoDownload.setVisibility(GONE);
        mUpDownProgressViews.put(mHolder.mProgressUploadOrDownload, packetId);
        mHolder.mBtnCheck.setVisibility(mActivity.isSelecteMode ? VISIBLE : GONE);
        mHolder.resetOnClickListenerMediaThumbnail();

        mHolder.mRootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.isSelecteMode) {
                    mHolder.mBtnCheck.setSelected(!mHolder.mBtnCheck.isSelected());
                    MessageItem item = new MessageItem();
                    item.id = String.valueOf(id);
                    item.packetID = packetId;
                    item.type = messageType;
                    if (mHolder.mBtnCheck.isSelected()) {
                        mActivity.addMessage(item);
                    } else {
                        mActivity.removeMessage(item);
                    }
                    mActivity.mLytDeleteSel.setEnabled(mActivity.selectedMessages.size() > 0);
                }
            }
        });

        mHolder.mBtnCheck.setSelected(isChecked);

        mHolder.mProfileImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, MyProfileActivity.class);
                mContext.startActivity(i);
            }
        });

        if (mimeType != null) {
            mUpDownProgressViews.put(mHolder.mProgressUploadOrDownload, packetId);

            int type = 0;
            if (mimeType.startsWith("image/")) {
                type = MESSAGE_TYPE_IMAGE;
                mHolder.mNoDownload.setImageResource(R.drawable.download_image);
            } else if (mimeType.startsWith("video/")) {
                type = MESSAGE_TYPE_VIDEO;
                mHolder.mNoDownload.setImageResource(R.drawable.download_video);
            } else if (mimeType.startsWith("audio/")) {
                type = MESSAGE_TYPE_AUDIO;
                mHolder.mNoDownload.setImageResource(R.drawable.download_audio);
            }

            mHolder.mMediaLayout.setVisibility(View.VISIBLE);
            mHolder.mLayoutForMessages.setVisibility(View.GONE);
            mHolder.mMediaLayout.setBackgroundResource(R.drawable.chat_to_item_bg);
            mHolder.mMediaLayout.setPadding(dpTopx(0), dpTopx(0), dpTopx(0), dpTopx(0));
            mHolder.mTxtDelivery.setVisibility(View.VISIBLE);

            Uri mediaUri = Uri.parse(body);
            if (messageType == Imps.MessageType.POSTPONED) {
                mHolder.mProgressUploadOrDownload.setVisibility(View.GONE);
                mHolder.mTxtDelivery.setText(mContext.getString(R.string.inReady));
                mHolder.mMediaDownloadCancel.setVisibility(View.GONE);
                mHolder.loading.setVisibility(VISIBLE);
                mHolder.mNoDownload.setVisibility(VISIBLE);
            } else if (err_code == Imps.FileErrorCode.COMPRESSING){
                mHolder.mProgressUploadOrDownload.setVisibility(View.GONE);
                mHolder.mTxtDelivery.setText(mContext.getString(R.string.inCompress));
                mHolder.mMediaDownloadCancel.setVisibility(View.GONE);
                mHolder.loading.setVisibility(VISIBLE);
            } else if (err_code == Imps.FileErrorCode.UPLOADING) {
                if (type == MESSAGE_TYPE_VIDEO) {
                    mHolder.mMediaVideoIcon.setVisibility(VISIBLE);
                }
                ProgressValue pv = ImApp.getInstance().getUploadProgress(packetId);
                if (pv != null) {
                    mHolder.mProgressUploadOrDownload.setVisibility(View.VISIBLE);
                    mHolder.mMediaVideoIcon.setVisibility(View.GONE);
                    mHolder.mTxtDelivery.setText(mContext.getString(R.string.inProgress));
                    mHolder.mMediaDownloadCancel.setVisibility(View.VISIBLE);
                    mHolder.mMediaDownloadCancel.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ImApp.getInstance().containUploadThread(packetId)) {
                                Imps.updateOperMessageError(mContext.getContentResolver(), packetId, Imps.FileErrorCode.UPLOADCANCELLED);
                                ImApp.getInstance().deleteUploadHandler(packetId);
                                Imps.updateDeliveryStateInDb(mContext.getContentResolver(), packetId, 0);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
                                Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
                                mActivity.sendBroadcast(intent);
                                mHolder.mMediaDownloadCancel.setVisibility(View.GONE);
                                mHolder.mProgressUploadOrDownload.setVisibility(View.GONE);
                                mHolder.mProgressUploadOrDownload.setProgress(0);
                            }
                        }
                    });
                } else {
                    mHolder.loading.setVisibility(GONE);
                    mHolder.mTxtDelivery.setText(mContext.getString(R.string.not_sent));
                    mHolder.mTxtDelivery.setTextColor(getResources().getColor(R.color.red));
                }
            } else if (err_code == Imps.FileErrorCode.UPLOADSUCCESS) {
                mHolder.mTxtDelivery.setVisibility(GONE);
                switch (delivery) {
                    case 0:
                        mHolder.mTxtDelivery.setVisibility(VISIBLE);
                        mHolder.mTxtDelivery.setText(mContext.getString(R.string.inProgress));
                        mHolder.loading.setVisibility(VISIBLE);
                        break;
                    case 1:
                        mHolder.mImgSendMsg.setVisibility(VISIBLE);
                        mHolder.mImgReceivedMsg.setVisibility(VISIBLE);
                    case 2:
                        mHolder.mImgSendMsg.setVisibility(VISIBLE);
                        break;
                    default:break;
                }
                if (type == MESSAGE_TYPE_VIDEO) {
                    mHolder.mMediaVideoIcon.setVisibility(VISIBLE);
                }
            } else if (err_code == Imps.FileErrorCode.UPLOADCANCELLED){
                if (type == MESSAGE_TYPE_VIDEO) {
                    mHolder.mMediaVideoIcon.setVisibility(VISIBLE);
                }
                mHolder.mTxtDelivery.setText(mContext.getString(R.string.file_upload_cancelled));
                mHolder.mTxtDelivery.setTextColor(getResources().getColor(R.color.red));
            } else { // err_code == Imps.FileErrorCode.UPLOADFAILED || (err_code >= 11 && err_code <= 15)
                if (type == MESSAGE_TYPE_VIDEO) {
                    mHolder.mMediaVideoIcon.setVisibility(VISIBLE);
                }
                mHolder.mTxtDelivery.setText(mContext.getString(R.string.not_sent));
                mHolder.mTxtDelivery.setTextColor(getResources().getColor(R.color.red));
            }

            mHolder.setOnClickListenerMediaThumbnail(mimeType, mediaUri.toString(), true);
            mHolder.mLayoutForMessages.setVisibility(View.GONE);
            mHolder.mMediaLayout.setVisibility(View.VISIBLE);

            if (type == MESSAGE_TYPE_IMAGE || type == MESSAGE_TYPE_VIDEO) {
                if (samplePath == null) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHolder.mMediaThumbnail.getLayoutParams();
                    params.width = MessageView.image_width;
                    params.height = MessageView.image_height;
                    mHolder.mMediaThumbnail.setLayoutParams(params);
                    mHolder.mMediaThumbnail.setImageDrawable(null);
                    mHolder.mNoDownload.setVisibility(VISIBLE);
                } else {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHolder.mMediaThumbnail.getLayoutParams();
                    params.width = thumbnailWidth;
                    params.height = thumbnailHeight;
                    mHolder.mMediaThumbnail.setLayoutParams(params);
                    mSampleImageBackgroundLoader.loadBitmap(samplePath, mHolder.mMediaThumbnail, null, false);
                    mHolder.mNoDownload.setVisibility(GONE);
                }
            } else { // type == MESSAGE_TYPE_AUDIO
                if (err_code == Imps.FileErrorCode.UPLOADSUCCESS) {
                    mHolder.mMediaLayout.setVisibility(View.GONE);
                    mHolder.mAudioClickingArea.setVisibility(View.VISIBLE);
                    showAudio(mHolder, packetId, mediaUri.toString(), thumbnailWidth, false, 1);
                } else {
                    mHolder.mAudioClickingArea.setVisibility(GONE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHolder.mMediaThumbnail.getLayoutParams();
                    params.width = MessageView.image_width;
                    params.height = MessageView.image_height;
                    mHolder.mMediaThumbnail.setLayoutParams(params);
                    mHolder.mMediaThumbnail.setImageDrawable(null);
                    mHolder.mNoDownload.setVisibility(VISIBLE);
                }
            }

            mHolder.mMediaLayout.setTag(null);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 16)
                mHolder.mLayoutForMessages
                        .setBackground(mContext.getResources().getDrawable(R.drawable.chat_to_item_bg));
            else
                mHolder.mLayoutForMessages
                        .setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.chat_to_item_bg));

            mHolder.mMediaLayout.setVisibility(View.GONE);
            if (packetId.startsWith(VIDEO_LOG)) {
                lastMessage = body.substring(String.format("[%s] ", mContext.getString(R.string.video_call)).length());
                mHolder.mCallIcon.setImageResource(R.drawable.callvideo_normal);
                mHolder.mCallIcon.setVisibility(View.VISIBLE);
                mHolder.mImgSendMsg.setVisibility(GONE);
                mHolder.mImgReceivedMsg.setVisibility(GONE);
                mHolder.mLayoutForMessages.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActivity.startChooseVideo();
                    }
                });
            } else if (packetId.startsWith(AUDIO_LOG)) {
                lastMessage = body.substring(String.format("[%s] ", mContext.getString(R.string.audio_call)).length());
                mHolder.mCallIcon.setImageResource(R.drawable.callvoice_normal);
                mHolder.mCallIcon.setVisibility(View.VISIBLE);
                mHolder.mImgSendMsg.setVisibility(GONE);
                mHolder.mImgReceivedMsg.setVisibility(GONE);
                mHolder.mLayoutForMessages.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActivity.startChooseVoice();
                    }
                });
            } else {
                lastMessage = body;
                if (messageType == Imps.MessageType.POSTPONED) {
                    mHolder.mTxtDelivery.setText(mContext.getString(R.string.inReady));
                    mHolder.mTxtDelivery.setVisibility(VISIBLE);
                    mHolder.loading.setVisibility(VISIBLE);
                } else if (messageType == Imps.MessageType.OUTGOING && (err_code == 0 || err_code == 1) && delivery == 0) {
                    mHolder.mImgSendMsg.setVisibility(VISIBLE);
                } else if (messageType == Imps.MessageType.OUTGOING && (err_code == 0 || err_code == 1) && delivery == 1) {
                    mHolder.mImgSendMsg.setVisibility(VISIBLE);
                    mHolder.mImgReceivedMsg.setVisibility(VISIBLE);
                } else { //(err_code >= 11 && err_code <= 15)
                    mHolder.mTxtDelivery.setText(mContext.getString(R.string.not_sent));
                    mHolder.mTxtDelivery.setVisibility(VISIBLE);
                    mHolder.mTxtDelivery.setTextColor(getResources().getColor(R.color.red));
                }
            }
            SpannableString spannablecontent;

            if (err_code == Imps.MessageErrorCode.MODIFIED) {
                spannablecontent = new SpannableString(
                        SmileyParser.getInstance(mContext).addSmileySpans(lastMessage + "    ", mHolder.mTextViewForMessages.getLineHeight()));
                Drawable d = getResources().getDrawable(R.drawable.ic_pen_right);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                spannablecontent.setSpan(span, spannablecontent.length() - 3, spannablecontent.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannablecontent = new SpannableString(SmileyParser.getInstance(mContext).addSmileySpans(lastMessage, mHolder.mTextViewForMessages.getLineHeight()));
            }

            mHolder.mTextViewForMessages.setVisibility(View.VISIBLE);
            mHolder.mTextViewForMessages.setText(spannablecontent);
        }

        switch (err_code){
            case  Imps.MessageErrorCode.NO_FRIEND:
            case  Imps.MessageErrorCode.UNAVAILABLE_CHAT:
            case  Imps.MessageErrorCode.NO_CALLABLE:
            case  Imps.MessageErrorCode.BLOCK_ME:
            case  Imps.MessageErrorCode.BLOCK_FRIEND:
            case  Imps.MessageErrorCode.IRREGULAR_WORD:
                mHolder.mTxtDelivery.setVisibility(View.VISIBLE);
                mHolder.mTxtDelivery.setText(mContext.getString(R.string.not_sent));

                if (date != null) {
                    DateFormat dateFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
                    mHolder.mTextViewForTimestamp.setText(dateFormat.format(date));
                } else {
                    mHolder.mTextViewForTimestamp.setText("");
                }
                break;
            default:
                if (date != null) {
                    CharSequence tsText = null;
                    DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(getContext());
                    //tsText = formatTimeStamp(date, messageType, dateFormat, delivery, body);
                    tsText = formatTimeStamp(date, dateFormat);
                    mHolder.mTextViewForTimestamp.setText(tsText);
                } else {
                    mHolder.mTextViewForTimestamp.setText("");
                }
                break;
        }

        OnLongClickListener showMenuListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mActivity.isSelecteMode) {
                    ProgressValue pv = ImApp.getInstance().getUploadProgress(packetId);
                    if ((err_code >= 11 && err_code <= 15) || err_code == Imps.FileErrorCode.UPLOADFAILED || err_code == Imps.FileErrorCode.UPLOADCANCELLED || (err_code == Imps.FileErrorCode.UPLOADING && pv == null))
                        showMessageOptionMenu(mimeType, id, packetId, body, samplePath, messageType, err_code, true, nickname, date, serverdate);
                    else
                        showMessageOptionMenu(mimeType, id, packetId, body, samplePath, messageType, err_code, false, nickname, date, serverdate);
                }
                return false;
            }
        };

        mHolder.mLayoutForMessages.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.isSelecteMode)
                    mHolder.mRootView.callOnClick();
                if (packetId.startsWith(VIDEO_LOG))
                    mActivity.startChooseVideo();
                else if (packetId.startsWith(AUDIO_LOG))
                    mActivity.startChooseVoice();
            }
        });

        mHolder.mLayoutForMessages.setOnLongClickListener(showMenuListener);
        mHolder.mMediaLayout.setOnLongClickListener(showMenuListener);
        mHolder.mAudioClickingArea.setOnLongClickListener(showMenuListener);

        if (showDate) {
            mHolder.mDateLayout.setVisibility(View.VISIBLE);
            TextView timeContent = (TextView) mHolder.mDateLayout.findViewById(R.id.txtContent);
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E", Resources.getSystem().getConfiguration().locale);
            String dayInString = simpleDateFormat.format(date);
            timeContent.setText(dateFormat.format(date) +  ", " + dayInString);
        } else {
            mHolder.mDateLayout.setVisibility(View.GONE);
        }

    }

    public void bindPresenceMessage(String contact, int type, boolean isGroupChat, boolean scrolling, Date date, final int id, final String packetId, final int messageType, boolean isChecked) {
        mHolder = (ViewHolder) getTag();
        mHolder.mDateLayout.setVisibility(VISIBLE);
        mHolder.mBtnCheck.setVisibility(GONE);
        CharSequence message = formatPresenceUpdates(contact, type, isGroupChat, scrolling);
        TextView timeContent = (TextView) mHolder.mDateLayout.findViewById(R.id.txtContent);
        DateFormat dateFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        timeContent.setText(dateFormat.format(date) + "  " + message);

        mHolder.mMessageLayout.setVisibility(View.GONE);
    }

    public void bindInviteMessage(String body, final int id, final String packetId, final int messageType, boolean isChecked) {
        mHolder = (ViewHolder) getTag();
        mHolder.mDateLayout.setVisibility(View.VISIBLE);
        mHolder.mBtnCheck.setVisibility(GONE);
        TextView timeContent = (TextView) mHolder.mDateLayout.findViewById(R.id.txtContent);
        timeContent.setText(body);
        mHolder.mMessageLayout.setVisibility(View.GONE);
    }

    public void bindOperMessage() {
        mHolder = (ViewHolder) getTag();
        mHolder.mMessageLayout.setVisibility(View.GONE);
        mHolder.mDateLayout.setVisibility(View.GONE);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showAudio(final ViewHolder aHolder, final String packetId, final String body, final int voiceLength,
                           boolean isLeft, int displaySentTime) {
        aHolder.mAudioProgressBar.setTag(packetId);

        if (displaySentTime == 0)
            aHolder.mAudioPlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chatbox_soundmessage_new));
        else
            aHolder.mAudioPlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chatbox_soundmessage_opened));

        aHolder.mAudioProgressBar.setProgress(0);

        if (packetId == null)
            return;

        if (voiceLength == -1) {
            aHolder.mAudioTime.setText(getResources().getString(R.string.message_view_audio) + " 00:00");
            return;
        } else if (voiceLength == 0) {
            aHolder.mAudioTime.setText(getResources().getString(R.string.message_view_audio) + " 00:00");
            GetAudioLengthTask mTask = new GetAudioLengthTask();
            mTask.setPacketId(packetId);
            mTask.setTextView(mHolder.mAudioTime);
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, body);
        } else {
            aHolder.mAudioTime.setText(mContext.getResources().getString(R.string.message_view_audio) + String.format(" %02d", voiceLength/60) + ":" + String.format("%02d", voiceLength%60));
        }

        if (mPlayingVoicePacketId == null || !mPlayingVoicePacketId.equals(packetId)) {
            aHolder.mAudioProgressBar.setProgress(0);
        } else {
            aHolder.mAudioProgressBar.setProgress(mVoiceProgress);
            if (mShowAudioProgress != null) {
                mShowAudioProgress.setViews(aHolder.mAudioTime, aHolder.mAudioProgressBar, aHolder.mAudioPlay);
            }
        }

        aHolder.mAudioClickingArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoicePlayer != null && mVoicePlayer.isPlaying()) {
                    mVoicePlayer.stop();
                    mVoicePlayer = null;
                }

                if (packetId.equals(mPlayingVoicePacketId)) {
                    aHolder.mAudioProgressBar.setProgress(0);

                    mPlayingVoicePacketId = null;
                    mVoiceProgress = 0;
                    return;
                }

                mPlayingVoicePacketId = null;
                mVoiceProgress = 0;

                try {
                    mVoicePlayer = new MediaPlayer();

                    mVoicePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mVoicePlayer.setDataSource(body);
                    mVoicePlayer.prepare();
                    mVoicePlayer.start();
                    mVoicePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            String s = (String) aHolder.mAudioProgressBar.getTag();
                            if (s != null && s.equals(mPlayingVoicePacketId)) {
                                aHolder.mAudioProgressBar.setProgress(100);
                            } else {
                                aHolder.mAudioProgressBar.setProgress(0);
                            }
                            mPlayingVoicePacketId = null;
                            mVoiceProgress = 0;
                            if (mVoicePlayer != null) {
                                try {
                                    mVoicePlayer.release();
                                    mVoicePlayer = null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    mPlayingVoicePacketId = packetId;
                    if (mShowAudioProgress != null) {
                        mShowAudioProgress.cancel(true);
                    }
                    mVoiceLength = mVoicePlayer.getDuration();
                    mShowAudioProgress = new ShowAudioProgress();
                    mShowAudioProgress.setViews(aHolder.mAudioTime, aHolder.mAudioProgressBar, aHolder.mAudioPlay);
                    mShowAudioProgress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, packetId);
                } catch (Exception e) {
                    e.printStackTrace();
                    GlobalFunc.showToast(getContext(), R.string.sorry_we_cannot_play_this_audio_file, false);

                    mPlayingVoicePacketId = null;
                    mVoiceProgress = 0;
                }
            }
        });
    }

    private SpannableString formatTimeStamp(Date date, DateFormat format) {
        StringBuilder deliveryText = new StringBuilder();
        deliveryText.append(format.format(date));
        deliveryText.append(' ');
        SpannableString spanText = new SpannableString(deliveryText.toString());
        return spanText;
    }

    private CharSequence formatPresenceUpdates(String contact, int type, boolean isGroupChat, boolean scrolling) {
        String body;

        Resources resources = mContext.getResources();

        switch (type) {
            case Imps.MessageType.PRESENCE_AVAILABLE:
                body = resources.getString(isGroupChat ? R.string.contact_joined : R.string.contact_online, contact);
                break;

            case Imps.MessageType.PRESENCE_AWAY:
                body = resources.getString(R.string.contact_away, contact);
                break;

            case Imps.MessageType.PRESENCE_DND:
                body = resources.getString(R.string.contact_busy, contact);
                break;

            case Imps.MessageType.PRESENCE_UNAVAILABLE:
                body = resources.getString(isGroupChat ? R.string.contact_left : R.string.contact_offline, contact);
                break;

            default:
                return null;
        }

        if (scrolling) {
            return body;
        } else {
			/*
			 * SpannableString spanText = new SpannableString(body); int len =
			 * spanText.length(); spanText.setSpan(new
			 * StyleSpan(Typeface.NORMAL), 0, len,
			 * Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); spanText.setSpan(new
			 * RelativeSizeSpan((float) 0.8), 0, len,
			 * Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			 */
            return body;
        }
    }

    private void doUpload(String packetId) throws Exception {
        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showToast(mContext, R.string.network_error, false);
            return;
        }

        Imps.updateMessageTypeInDb(mContext.getContentResolver(), packetId, Imps.MessageType.POSTPONED);
        Imps.updateOperMessageError(mContext.getContentResolver(), packetId, Imps.FileErrorCode.UPLOADING);
        Imps.updateMessageTimeInDb(mContext.getContentResolver(), packetId, System.currentTimeMillis());

        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
        mActivity.refreshChatList();

        Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
        mContext.sendBroadcast(intent);
    }

    /**
     * Message Edit Menu
     */
    /**
     * Message Edit Menu
     */
    private void showMessageOptionMenu(final String mimeType, final int id, final String packetId, final String message,
                                       final String thumbnailPath, final int messageType, final int errorCode,
                                       final boolean isRetry, final String nickname, final Date date, final Date serverDate) {

        AndroidUtility.hideKeyboard(mActivity.mChatView);

        QuickActionPopup menu = null;
        if (messageType == Imps.MessageType.INCOMING)
            menu = new QuickActionPopup(mContext, QuickActionPopup.VERTICAL, QuickActionPopup.ANIM_GROW_FROM_LEFT);
        else
            menu = new QuickActionPopup(mContext, QuickActionPopup.VERTICAL, QuickActionPopup.ANIM_GROW_FROM_RIGHT);

        if (mimeType == null && !packetId.startsWith(VIDEO_LOG) && !packetId.startsWith(AUDIO_LOG)) {
            menu.addActionItem(new QuickActionItem(ID_COPY_MESSAGE, getResources().getString(R.string.chat_menu_copy_msg), null, getResources().getColor(R.color.white)));
        }

        if(isRetry)
            menu.addActionItem(new QuickActionItem(ID_RETRY, getResources().getString(R.string.chat_menu_resend), null, getResources().getColor(R.color.white)));

        if (mimeType != null) {
            if (messageType == Imps.MessageType.INCOMING && errorCode == Imps.FileErrorCode.DOWNLOADSUCCESS) {
                menu.addActionItem(new QuickActionItem(ID_SAVE_FILE, getResources().getString(R.string.save), null, getResources().getColor(R.color.white)));
            }
        }

        menu.addActionItem(new QuickActionItem(ID_DELETE_MESSAGE, getResources().getString(R.string.delete), null, getResources().getColor(R.color.white)));

        if (!isRetry)
            menu.addActionItem(new QuickActionItem(ID_PROPERTY, getResources().getString(R.string.show_property), null, getResources().getColor(R.color.white)));

        menu.setOnActionItemClickListener(new QuickActionPopup.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickActionPopup source, int pos, int actionId) {
                Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.slide_out_right_to_left_message);
                switch (actionId) {
                    case ID_DELETE_MESSAGE:
                        if (ImApp.getInstance().containDownloadThread(packetId))
                            ImApp.getInstance().deleteDownloadHandler(packetId);
                        if (ImApp.getInstance().containUploadThread(packetId))
                            ImApp.getInstance().deleteUploadHandler(packetId);
                        mActivity.deleteMessage(packetId);
                        break;
                    case ID_COPY_MESSAGE:
                        mActivity.copyMessage(message);
                        break;
                    case ID_RETRY:
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (mimeType != null) {
                                    try {
                                        doUpload(packetId);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    mActivity.resendMessage(packetId, message);
                                }
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                                mActivity.mChatView.mHistory.smoothScrollToPosition(mActivity.mChatView.getChatCount());
                            }
                        });
                        startAnimation(animation);
                        break;
                    case ID_QUOTE_MESSAGE:
                        mActivity.quoteMessage(message);
                        break;
                    case ID_SAVE_FILE:
                        if (!GlobalFunc.isStorageWrittable()) {
                            GlobalFunc.showToast(mContext, R.string.cannot_use_storage, false);
                        } else {
                            mActivity.copyFile(message, mimeType);
                        }
                        break;
                    case ID_PROPERTY:
                        showProperty(message, messageType, mimeType, date, serverDate, packetId, errorCode);
                        break;
                    default:
                        break;
                }
            }
        });

        final ViewGroup root = (ViewGroup) mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        final View view = new View(mActivity);
        view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        view.setBackgroundColor(Color.TRANSPARENT);
        root.addView(view);
        int location[] = new int[2];
        root.getLocationInWindow(location);
        view.setX(mActivity.touchPos[0] - location[0]);
        view.setY(mActivity.touchPos[1] - location[1]);
        menu.show(view);
    }

    private class GetAudioLengthTask extends AsyncTask<String, Void, String> {

        String packetId;
        TextView mTxtTime;

        public void setPacketId(String _packetId) {
            packetId = _packetId;
        }

        public void setTextView(TextView textView) {
            mTxtTime = textView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String audioLength = null;
            int len = 0;
            MediaPlayer player = null;
            try {
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(params[0]);
                player.prepare();
                len = player.getDuration();
                if (len > 0)
                    audioLength = String.valueOf(len);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (player != null) {
                    player.release();
                }
            }

            return audioLength;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                int len = 0;
                try {
                    len = Integer.valueOf(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (len >= 1000)
                    len = len / 1000;
                else if (len > 100)
                    len = 1;

                mTxtTime.setText(mContext.getResources().getString(R.string.message_view_audio) + String.format(" %02d", len/60) + ":" + String.format("%02d", len%60));
                if (len != 0) {
                    Imps.updateMessageInDb(mContext.getContentResolver(), packetId, null, len, 0);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                } else {
                    Imps.updateMessageInDb(mContext.getContentResolver(), packetId, null, -1, 0);
                }
            }
        }
    }

    public static class ShowUpDownProgress extends AsyncTask<String, ProgressData, Void> {
        private boolean isStopped = false;

        public void setStop(boolean value) {
            isStopped = value;
        }

        public boolean isStopped() {
            return isStopped;
        }

        @Override
        protected void onPreExecute() {
            isStopped = false;
            super.onPreExecute();
        }

        @SuppressWarnings("static-access")
        @Override
        protected Void doInBackground(String... params) {
            while (!isStopped) {
                try {
                    for (Map.Entry<ProgressBar, String> entry : mUpDownProgressViews.entrySet()) {
                        String packetId = entry.getValue();
                        ProgressValue pv = ImApp.getInstance().getDownloadProgress(packetId);
                        ProgressData progressData = new ProgressData();
                        progressData.progressBar = entry.getKey();
                        if (pv != null) {
                            if (pv.progressValue != 0) {
                                progressData.progressValue = pv.progressValue;
                                publishProgress(progressData);
                            }
                        } else {
                            pv = ImApp.getInstance().getUploadProgress(packetId);
                            if (pv != null) {
                                if (pv.progressValue != 0) {
                                    progressData.progressValue = pv.progressValue;
                                    publishProgress(progressData);
                                }
                            }
                        }
                    }
                    Thread.currentThread().sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ProgressData... params) {
            params[0].progressBar.setProgress(params[0].progressValue);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isStopped = true;
        }
    }

    private void showProgressCursor(final String packetId) {
        ProgressValue pv = ImApp.getInstance().getDownloadProgress(packetId);
        if (pv != null) {
            mHolder.mProgressUploadOrDownload.setVisibility(View.VISIBLE);
            mHolder.mMediaDownloadCancel.setVisibility(View.VISIBLE);
            mHolder.mMediaDownloadCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ImApp.getInstance().containDownloadThread(packetId)) {
                        ImApp.getInstance().deleteDownloadHandler(packetId);
                        Imps.updateOperMessageError(mContext.getContentResolver(), packetId, Imps.FileErrorCode.DOWNLOADCANCELLED);
                        GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
                        mActivity.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
                        mHolder.mMediaDownload.setVisibility(View.VISIBLE);
                        mHolder.mMediaDownloadCancel.setVisibility(View.GONE);
                        mHolder.mProgressUploadOrDownload.setVisibility(View.GONE);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
                    }
                }
            });
        } else {
            mHolder.mProgressUploadOrDownload.setVisibility(View.GONE);
            mHolder.mMediaDownload.setVisibility(View.GONE);
            mHolder.mMediaDownloadCancel.setVisibility(View.GONE);
        }

        mHolder.mMaskView.setVisibility(View.VISIBLE);
    }

    private void showProperty(String message,int messageType, String mimeType, Date sendDate, Date recvDate, String packetId, int errorCode) {
        final Dialog dlg = GlobalFunc.createDialog(mContext, R.layout.message_edit_option_menu_dialog, true);

        View headerView = dlg.findViewById(R.id.layout_header);
        View View1 = dlg.findViewById(R.id.layout1);
        View View2 = dlg.findViewById(R.id.layout2);
        View View3 = dlg.findViewById(R.id.layout3);

        headerView.setVisibility(View.VISIBLE);
        TextView msgtitle = dlg.findViewById(R.id.msgtitle);
        msgtitle.setText(R.string.show_property);

        TextView textView1 = (TextView) dlg.findViewById(R.id.text1);
        TextView textView2 = (TextView) dlg.findViewById(R.id.text2);
        TextView textView3 = (TextView) dlg.findViewById(R.id.text3);
        TextView detail1 = (TextView) dlg.findViewById(R.id.detail1);
        TextView detail2 = (TextView) dlg.findViewById(R.id.detail2);
        TextView detail3 = (TextView) dlg.findViewById(R.id.detail3);

        String send_time = "";
        String recv_time = "";
        CharSequence tsText = null;

        if (sendDate != null) {
            tsText = formatTimeStamp(sendDate, android.text.format.DateFormat.getTimeFormat(getContext()));
            send_time = formatDate(sendDate) + " " + tsText;
            if (!packetId.startsWith(GlobalConstrants.AUDIO_LOG) && !packetId.startsWith(GlobalConstrants.VIDEO_LOG)) {
                textView1.setText(getResources().getString(R.string.send_time));
                detail1.setText(send_time);
            } else {
                textView1.setText(getResources().getString(R.string.call_start_time));
                detail1.setText(send_time);
            }
            View1.setVisibility(View.VISIBLE);
        } else {
            View1.setVisibility(View.GONE);
        }

        if (recvDate != null) {
            dlg.findViewById(R.id.divider1).setVisibility(VISIBLE);
            tsText = formatTimeStamp(recvDate, android.text.format.DateFormat.getTimeFormat(getContext()));
            recv_time = formatDate(recvDate) + " " + tsText;
            if (!packetId.startsWith(GlobalConstrants.AUDIO_LOG) && !packetId.startsWith(GlobalConstrants.VIDEO_LOG)) {
                textView2.setText(getResources().getString(R.string.received_time));
                detail2.setText(recv_time);
            } else {
                textView2.setText(getResources().getString(R.string.call_end_time));
                detail2.setText(recv_time);
            }
            View2.setVisibility(View.VISIBLE);
        } else {
            View2.setVisibility(View.GONE);
        }

        if (mimeType != null) {
            if ((messageType == Imps.MessageType.INCOMING && errorCode == Imps.FileErrorCode.DOWNLOADSUCCESS) || (messageType != Imps.MessageType.INCOMING)) {
                dlg.findViewById(R.id.divider2).setVisibility(VISIBLE);
                View3.setVisibility(View.VISIBLE);
                File file = new File(message);
                if (file.exists()) {
                    textView3.setText(getResources().getString(R.string.file_size));
                    detail3.setText(Utilities.formatFileSize(file.length()));
                } else {
                    textView3.setText(getResources().getString(R.string.file_no_exist));
                }
            }
        }

        if ((packetId.startsWith(GlobalConstrants.AUDIO_LOG) || packetId.startsWith(GlobalConstrants.VIDEO_LOG)) &&
                !message.contains(getResources().getString(R.string.duration))) {
            textView1.setText(getResources().getString(R.string.videocall_time));
            detail1.setText(send_time);
            textView2.setText(getResources().getString(R.string.videocall_state));
            if (packetId.startsWith(GlobalConstrants.AUDIO_LOG)) {
                detail2.setText(message.substring(String.format("[%s] ", mContext.getString(R.string.audio_call)).length()));
            } else {
                detail2.setText(message.substring(String.format("[%s] ", mContext.getString(R.string.video_call)).length()));
            }
            View1.setVisibility(VISIBLE);
            View2.setVisibility(VISIBLE);
        }

        detail1.setSelected(true);
        detail2.setSelected(true);
        detail3.setSelected(true);

        dlg.show();
    }

    private void showAvatar(final String address, final boolean isLeft) {
        if (address != null) {
            if (isLeft) {
                try {
                    String samplePath = Imps.getAvatarPath(mContext.getContentResolver(), address);
                    if (!samplePath.equals(null) && !samplePath.isEmpty()) {
                        File avatar = new File(samplePath);
                        if (!avatar.exists()) {
                            samplePath = null;
                            Imps.updateContactsInDb(mContext.getContentResolver(), address, "");
                        }
                    }
                    if (!samplePath.isEmpty()) {
                        Glide.with(mContext).setDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).
                            format(DecodeFormat.PREFER_RGB_565).placeholder(R.drawable.profilephoto).error(R.drawable.profilephoto)).load(samplePath).into(mHolder.mProfileImg);
                    } else if (!address.startsWith("pls")) {
                        ImageLoaderUtil.loadAvatarImage(mContext, address, mHolder.mProfileImg, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String formatDate(Date date) {
        return MESSAGE_DATE_FORMAT.format(date);
    }

    private class ShowAudioProgress extends AsyncTask<String, Void, Void> {
        // TextView mTxtTime;
        ProgressBar mProgressBar;
        ImageView mPlayBtn;
        int playbuttonindex = 0;
        String packetId;

        public void setViews(TextView textView, ProgressBar _mProgressBar, ImageView _mPlayBtn) {
            // mTxtTime = textView;
            mProgressBar = _mProgressBar;
            mPlayBtn = _mPlayBtn;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("static-access")
        @Override
        protected Void doInBackground(String... params) {
            packetId = params[0];
            playbuttonindex = 0;
            try {
                while (mVoicePlayer != null && mVoicePlayer.isPlaying()) {
                    try {
                        int position = mVoicePlayer.getCurrentPosition();
                        int length = mVoiceLength;

                        if (length == 0)
                            break;

                        mVoiceProgress = position * 100 / length;

                        if (length < 2600) {
                            if (mVoiceProgress > 90)
                                mVoiceProgress = 100;
                        } else {
                            if (mVoiceProgress > 95)
                                mVoiceProgress = 100;
                        }

                        publishProgress();
                        playbuttonindex = (playbuttonindex+1)%100;
                        Thread.currentThread().sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(mVoiceProgress);
            mProgressBar.invalidate();

            String s = (String) mProgressBar.getTag();
            if (s != null && s.equals(mPlayingVoicePacketId)) {
                mProgressBar.setProgress(mVoiceProgress);
                int index = playbuttonindex/25;
                switch (index)
                {
                    case 0:
                        mPlayBtn.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chatbox_play01));
                        break;
                    case 1:
                        mPlayBtn.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chatbox_play02));
                        break;
                    case 2:
                        mPlayBtn.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chatbox_play03));
                        break;
                    case 3:
                        mPlayBtn.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chatbox_play04));
                        break;
                }
                return;
            }

            mProgressBar.setProgress(0);
            mPlayBtn.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chatbox_soundmessage_new));
            return;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mProgressBar.setProgress(0);
            mPlayBtn.setImageResource(R.drawable.chatbox_soundmessage_opened);
            Imps.updateMessageDisplaySentTimeInDb(mContext.getContentResolver(), packetId, 1);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
        }
    }
}
