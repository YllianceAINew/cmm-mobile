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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.datamodel.FriendItem;

import java.util.Locale;


public class FriendView extends LinearLayout {
    static final String[] CONTACT_PROJECTION = {
            Imps.Contacts._ID,
            Imps.Contacts.PROVIDER,
            Imps.Contacts.USERNAME,
            Imps.Contacts.NICKNAME,
            Imps.Presence.PRESENCE_STATUS,
            Imps.Contacts.STATUSMESSAGE,
            Imps.Contacts.FAVORITE

    };

    private Context mContext;
    private Resources r;
    private ImApp mApp;

    public FriendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        r = mContext.getResources();
        if (mApp == null)
            mApp = (ImApp) ((Activity) mContext).getApplication();
    }

    private ViewHolder mHolder = null;

    class ViewHolder {
        View mRootView;
        View mHeader;
        TextView mHeaderText;
        View mBodyView;
        TextView mNickName;
        CircularImageView mAvatar;
        ImageView mFriendMark;
        TextView mStatusMessage;
        View mSeparatorLine;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHolder = (ViewHolder) getTag();
        if (mHolder == null) {
            mHolder = new ViewHolder();
            mHolder.mRootView = findViewById(R.id.rootView);
            mHolder.mBodyView = findViewById(R.id.lyt_item);
            mHolder.mHeader = findViewById(R.id.header);
            mHolder.mHeaderText = (TextView) findViewById(R.id.headerText);
            mHolder.mNickName = (TextView) findViewById(R.id.contactNickName);
            mHolder.mAvatar = (CircularImageView) findViewById(R.id.imgPropile);
            mHolder.mFriendMark = (ImageView) findViewById(R.id.img_friend_mark);
            mHolder.mStatusMessage = (TextView) findViewById(R.id.statusMessage);
            mHolder.mSeparatorLine = findViewById(R.id.separatorLine);
            setTag(mHolder);
        }
    }

    public void bind(FriendItem item, boolean showSeparator, String filterString) {

        mHolder = (ViewHolder) getTag();
        String nickName = item.nickName;
        String statusText = item.status;
        String userName = item.userName;

        if (item.nickName != null) {
            if (filterString.isEmpty())
                mHolder.mNickName.setText(nickName);
            else {
                String strLowerContent = nickName.toLowerCase(Locale.ENGLISH);
                SpannableString spannableContent = new SpannableString(nickName);
                if(strLowerContent.contains(filterString)) {
                    spannableContent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.item_store_tab_indicator_color)),
                            strLowerContent.indexOf(filterString), strLowerContent.indexOf(filterString) + filterString.length(), 0);
                    spannableContent.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dlg_trans_bg)),
                            strLowerContent.indexOf(filterString), strLowerContent.indexOf(filterString) + filterString.length(), 0);
                }
                mHolder.mNickName.setText(spannableContent);
            }
        } else
            mHolder.mNickName.setText("Pica User");
        mHolder.mNickName.setSelected(true);

        if (!item.isOnline) {
            mHolder.mFriendMark.setVisibility(View.GONE);
        } else {
            mHolder.mFriendMark.setVisibility(View.VISIBLE);
        }

        if (statusText != null && statusText.length() > 0) {
            mHolder.mStatusMessage.setVisibility(View.VISIBLE);
            mHolder.mStatusMessage.setText(statusText);
        } else {
            mHolder.mStatusMessage.setVisibility(View.GONE);
        }

        mHolder.mHeader.setVisibility(View.GONE);

        if (showSeparator) {
            mHolder.mSeparatorLine.setVisibility(View.VISIBLE);
        } else
            mHolder.mSeparatorLine.setVisibility(View.GONE);

        // GlobalFunc.setProfileImage(mHolder.mAvatar, mContext, userName);
        GlobalFunc.showAvatar(mContext, userName, mHolder.mAvatar);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            mHolder = (ViewHolder) getTag();
            if (mHolder != null && mHolder instanceof ViewHolder) {
                if (mHolder.mRootView != null)
                    unbindDrawables(mHolder.mRootView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * viewë“¤ì—� ëŒ€í•œ í•´ë°©ì�„ ì§„í–‰í•œë‹¤.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }

        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(null);
            view.destroyDrawingCache();
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
