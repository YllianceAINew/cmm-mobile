/*
 * Copyright (C) 2007-2008 Esmertec AG. Copyright (C) 2007-2008 The Android Open
 * Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.multimediachat.ui.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.emotic.SmileyParser;
import com.multimediachat.util.PrefUtil.mPref;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.multimediachat.app.im.provider.Imps.ChatsColumns.GROUP_LEAVE;


@SuppressLint("NewApi")
public class ChatListItemView extends LinearLayout {
    public static final String[] CONTACT_PROJECTION = {Imps.Contacts._ID, Imps.Contacts.PROVIDER,
            Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME,
            Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
            Imps.Contacts.SUBSCRIPTION_TYPE,
            Imps.Contacts.SUBSCRIPTION_STATUS,
            Imps.Presence.PRESENCE_STATUS,
            Imps.Chats.LAST_MESSAGE_DATE,
            Imps.Chats.LAST_UNREAD_MESSAGE,
            Imps.Chats.GROUP_CHAT,
            Imps.Contacts.AVATAR_DATA
    };

    private Context mContext;
    Resources r;
    ImApp mApp;

    public ChatListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        r = mContext.getResources();
        mApp = (ImApp) ((Activity) mContext).getApplication();
    }

    private ViewHolder mHolder = null;

    class ViewHolder {
        View mBodyView;
        TextView mChatRoomName;
        TextView mTimeStamp;
        TextView mLastMsg;
        ImageView mImgLastMsg;
        CircularImageView mAvatar;
        ImageView mOnlineStatus;
        TextView mDirtyCount;
        View mSeparatorLine;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHolder = (ViewHolder) getTag();
        if (mHolder == null) {
            mHolder = new ViewHolder();

            mHolder.mBodyView = findViewById(R.id.lyt_body);
            mHolder.mChatRoomName = (TextView) findViewById(R.id.roomName);
            mHolder.mTimeStamp = (TextView) findViewById(R.id.timestamp);
            mHolder.mAvatar = (CircularImageView) findViewById(R.id.imgPropile);
            mHolder.mLastMsg = (TextView) findViewById(R.id.lastMsg);
            mHolder.mOnlineStatus = (ImageView) findViewById(R.id.onlineStatus);
            mHolder.mDirtyCount = (TextView) findViewById(R.id.txtNoDirtyCnt);
            mHolder.mImgLastMsg = (ImageView) findViewById(R.id.imgLastMsg);
            mHolder.mSeparatorLine = findViewById(R.id.separator);

            setTag(mHolder);
        }
    }

    public void bind(Cursor cursor) {
        mHolder = (ViewHolder) getTag();
        String address = cursor.getString(cursor.getColumnIndex(Imps.Contacts.USERNAME));
        String nickname = cursor.getString(cursor.getColumnIndex(Imps.Contacts.NICKNAME));
        int type = cursor.getInt(cursor.getColumnIndex(Imps.Contacts.TYPE));
        int isGroup = cursor.getInt(cursor.getColumnIndex(Imps.Chats.GROUP_CHAT));
        int presence = cursor.getInt(cursor.getColumnIndex(Imps.Contacts.PRESENCE_STATUS));
        int contactId = cursor.getInt(cursor.getColumnIndex(Imps.Contacts._ID));
        long timestamp = cursor.getLong(cursor.getColumnIndex(Imps.Contacts.LAST_MESSAGE_DATE));
        Date date = new Date(timestamp);

        parseDate(date);

        int dirtyCount = 0;

        try {
            dirtyCount = Imps.Notifications.getNotificationCount(
                    mContext.getContentResolver(),
                    Imps.Notifications.CAT_CHATTING,
                    Imps.Notifications.FIELD_CHAT,
                    contactId);
        } catch (Exception e) {
        }

        try {
            SpannableString spannablecontent = new SpannableString(SmileyParser.getInstance(mContext).addSmileySpans(getLastMsg(contactId), mHolder.mLastMsg.getLineHeight()));
            mHolder.mLastMsg.setText(spannablecontent);
            mHolder.mLastMsg.setSelected(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dirtyCount > 0) {
            mHolder.mDirtyCount.setVisibility(View.VISIBLE);
            if (dirtyCount > 99) {
                mHolder.mDirtyCount.setText("99+");
                mHolder.mDirtyCount.setTextSize(10);
            } else {
                mHolder.mDirtyCount.setText(String.valueOf(dirtyCount));
                mHolder.mDirtyCount.setTextSize(12);
            }
        } else
            mHolder.mDirtyCount.setVisibility(View.INVISIBLE);


        if (Imps.Contacts.TYPE_GROUP == type) {
            if (isGroup == GROUP_LEAVE)
                mHolder.mOnlineStatus.setVisibility(View.GONE);
            else
                mHolder.mOnlineStatus.setVisibility(View.VISIBLE);
        } else {
            if (presence == Imps.Presence.OFFLINE)
                mHolder.mOnlineStatus.setVisibility(View.GONE);
            else
                mHolder.mOnlineStatus.setVisibility(View.VISIBLE);
        }

        if (address != null && address.equals(mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain))) {
            mHolder.mChatRoomName.setText(r.getString(R.string.chat_list_picatalk_team_name));
        } else {
            mHolder.mChatRoomName.setText(nickname);
        }
        mHolder.mChatRoomName.setSelected(true);

        if (mHolder.mAvatar != null) {
            try {
                if (Imps.Contacts.TYPE_GROUP == type) {
                    mHolder.mAvatar.setImageResource(R.drawable.groupicon_small);
                } else {
                    // GlobalFunc.setProfileImage(mHolder.mAvatar, mContext, address);
                    GlobalFunc.showAvatar(mContext, address, mHolder.mAvatar);
                }
            } catch (Exception e) {
            }
        }
    }

    private String getLastMsg(int contactId) {
        String lastMsg = "";
        Uri uri = Imps.Messages.getContentUriByThreadId(contactId);
        Cursor cursor = mContext.getContentResolver().query(uri, null, Imps.Messages.BODY + " IS NOT NULL", null, null);
        if (cursor != null) {
            if (cursor.moveToLast()) {
                String mimeType = cursor.getString(cursor.getColumnIndex(Imps.Messages.MIME_TYPE));
                String message = cursor.getString(cursor.getColumnIndex(Imps.Messages.BODY));

                mHolder.mImgLastMsg.setVisibility(View.VISIBLE);
                if (mimeType != null) {
                    if (mimeType.startsWith("image/")) {
                        lastMsg = mContext.getResources().getString(R.string.status_bar_sent_image);
                        mHolder.mImgLastMsg.setImageDrawable(r.getDrawable(R.drawable.chat_list_photo));
                    } else if (mimeType.startsWith("audio/")) {
                        lastMsg = mContext.getResources().getString(R.string.status_bar_sent_audio);
                        mHolder.mImgLastMsg.setImageDrawable(r.getDrawable(R.drawable.chat_list_voice));
                    } else if (mimeType.startsWith("video/")) {
                        lastMsg = mContext.getResources().getString(R.string.status_bar_sent_video);
                        mHolder.mImgLastMsg.setImageDrawable(r.getDrawable(R.drawable.chat_list_movie));
                    }
                } else if (message.startsWith(String.format("[%s] ", mContext.getString(R.string.video_call)))) {
                    mHolder.mImgLastMsg.setImageDrawable(r.getDrawable(R.drawable.last_msg_icon_video));
                    return message.substring(String.format("[%s] ", mContext.getString(R.string.video_call)).length());
                } else if (message.startsWith(String.format("[%s] ", mContext.getString(R.string.audio_call)))) {
                    mHolder.mImgLastMsg.setImageDrawable(r.getDrawable(R.drawable.last_msg_icon_audio));
                    return message.substring(String.format("[%s] ", mContext.getString(R.string.audio_call)).length());
                } else {
                    lastMsg = message;
                    mHolder.mImgLastMsg.setVisibility(View.GONE);
                }
            }
            cursor.close();
        }

		return lastMsg;
    }

    private void parseDate(Date date) {
        if (DateUtils.isToday(date.getTime())) {
            mHolder.mTimeStamp.setText(android.text.format.DateFormat.getTimeFormat(getContext()).format(date));
        } else {
            mHolder.mTimeStamp.setText(SimpleDateFormat.getDateInstance(DateFormat.LONG).format(date));
        }
    }

}
