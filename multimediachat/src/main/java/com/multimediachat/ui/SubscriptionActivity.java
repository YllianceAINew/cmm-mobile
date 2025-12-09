package com.multimediachat.ui;

import com.multimediachat.R;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.ui.views.QuickActionItem;
import com.multimediachat.ui.views.QuickActionPopup;
import com.multimediachat.ui.views.SubscriptionFriendListView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import static com.multimediachat.ui.views.QuickActionPopup.ANIM_GROW_FROM_RIGHT;

public class SubscriptionActivity extends BaseActivity implements OnClickListener {
	TextView findEmptyView;
	long mProviderId = -1;
	long mAccountId = -1;

	int FRIEND_LIST_LOADER_ID = 4467;
	SubscriptionFriendListView mSubscriptionFilterView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_requests);
		setActionBarTitle(getString(R.string.friend_requests_title));

		mSubscriptionFilterView = (SubscriptionFriendListView) findViewById(R.id.subfriendListView);
		findEmptyView = (TextView) findViewById(R.id.emptyView);
		findEmptyView.setText(getResources().getString(R.string.friend_requests_no_received_friends));

		mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
		mAccountId = mPref.getLong(GlobalConstrants.store_picaAccountId, -1);

		IImConnection conn = mApp.getConnection(mProviderId);

		if (conn == null && mAccountId > 0 && mProviderId > 0) {
			try {
				conn = mApp.createConnection(mProviderId, mAccountId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (conn != null) {
			mSubscriptionFilterView.setConnection(conn);
		}

		mSubscriptionFilterView.setLoaderManager(getSupportLoaderManager(), FRIEND_LIST_LOADER_ID);

		mSubscriptionFilterView.setSubscriptionType(Imps.Contacts.SUBSCRIPTION_TYPE_FROM);
		mSubscriptionFilterView.showList();

	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			if (view instanceof AdapterView) {

			} else
				((ViewGroup) view).removeAllViews();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unbindDrawables(findViewById(R.id.rootView));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			super.onClick(v);
			break;
		}
	}
	
}
