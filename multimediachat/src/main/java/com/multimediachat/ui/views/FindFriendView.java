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

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.global.GlobalFunc;

public class FindFriendView extends LinearLayout {

	ImApp mApp;
	Context mContext;

	public FindFriendView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mApp = (ImApp) ((Activity) context).getApplication();
		mContext = context;
	}

	private ViewHolder holder = null;

	class ViewHolder {
		View mRootView;
		CircularImageView avartar;
		TextView nicknameView;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		holder = (ViewHolder) getTag();

		if (holder == null) {
			holder = new ViewHolder();
			holder.mRootView = findViewById(R.id.rootView);
			holder.avartar = (CircularImageView) findViewById(R.id.imgPropile);
			holder.nicknameView = (TextView) findViewById(R.id.contactNickName);
			setTag(holder);
		}
	}

	public void bind(Contact contact) {
		holder = (ViewHolder) getTag();
		if (contact == null)
			return;
		String address = contact.getAddress().getAddress();
		String name = contact.getName();

		if (name == null || address == null || name.trim().equals(""))
			return;

		//ImageLoaderUtil.loadAvatarImage(mContext, address, holder.avartar, 1);
		holder.nicknameView.setText(name);
		holder.nicknameView.setSelected(true);
		GlobalFunc.setProfileImage(holder.avartar, mContext, address);
	}

	@Override
	protected void onDetachedFromWindow() {
		try {
			holder = (ViewHolder) getTag();
			if (holder != null && holder instanceof ViewHolder) {
				if (holder.mRootView != null)
					unbindDrawables(holder.mRootView);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDetachedFromWindow();
	}

	/**
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
