package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.dialog.CustomDialog;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.ui.views.HorizontalListView;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.datamodel.FriendItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("DefaultLocale")
public class ChooseFriendsActivity extends BaseActivity
		implements OnClickListener {

	private Context mContext = null;
	public static String CALL_FROM_GROUP_CHAT = "group_chat";
	public static String CALL_FROM_SELECT_ONLY = "select_only";

	final String[] CONTACT_PROJECTION = { Imps.Contacts._ID, Imps.Contacts.PROVIDER, Imps.Contacts.USERNAME,
			Imps.Contacts.NICKNAME, Imps.Presence.PRESENCE_STATUS, Imps.Contacts.STATUSMESSAGE
	};

	final int ERROR_SELECT_CONTACT = 0;

	// UI variable
	long mProviderId = -1;
	long mAccountId = -1;
	MainProgress mProgressDlg;

	TextView mEmptyView;
	ListView mGridView;
	GridAdapter mGridAdapter;

	private HorizontalAdapter mHorizontalAdapter;
	private HorizontalListView mHorizontalListView;

	// Data variable
	ShowFriendListAsyncTask mAsyncTask;
	String lastFilterString;
	String mCallFrom = "";
	String mTagMemberList = "";
	ArrayList<String> mMemberList = new ArrayList<>();

	private MyLoaderCallbacks mLoaderCallbacks;
	private LoaderManager mLoaderManager;
	private int mLoaderId;
	
	EditText mTxtSearch;

	private TextView mOkButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_list_choose_list);
		setActionBarTitle(getString(R.string.select_friends_title));

		mContext = this;
		mGridView = (ListView) findViewById(R.id.gridView);
		mEmptyView = (TextView) findViewById(R.id.emptyView);
		mGridView.setEmptyView(mEmptyView);
		mEmptyView.setText(r.getString(R.string.no_search_result));

		mTxtSearch = (EditText)findViewById(R.id.txtSearch);

		mTxtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				mGridAdapter.getFilter().filter(mTxtSearch.getText().toString());
			}
		});

		findViewById(R.id.btnClose).setOnClickListener(this);

		addTextButton(getString(R.string.confirm), R.id.btnOK, POS_RIGHT);

		mOkButton = (TextView) findViewById(R.id.btnOK);
		mOkButton.setEnabled(false);

		mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontalListView);
		mHorizontalAdapter = new HorizontalAdapter(this, 0);
		mHorizontalListView.setAdapter(mHorizontalAdapter);
		mHorizontalListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FriendItem item = mHorizontalAdapter.getItem(arg2);
				if (item != null && mHorizontalAdapter.removeItem(item)) {
					mHorizontalAdapter.notifyDataSetChanged();
				}
			}
		});

		mGridView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				try {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		});

		Intent intent = getIntent();
		if (intent != null) {
			mCallFrom = intent.getStringExtra("call_from");
			if (intent.hasExtra("tag_member_list")) {
				mTagMemberList = intent.getStringExtra("tag_member_list");
			}
			mMemberList = intent.getStringArrayListExtra("member_list");
		}

		mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
		mAccountId = mPref.getLong(GlobalConstrants.store_picaAccountId, -1);

		progressDlg = new MainProgress(this);

		mLoaderId = 4567;
		mLoaderManager = getSupportLoaderManager();

		cr = getContentResolver();

		showFriendList("");

	}

	@Override
	public void onBackPressed() {
		InputMethodManager ime = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		if (ime.hideSoftInputFromWindow(mTxtSearch.getWindowToken(), 0))
			return;
		super.onBackPressed();
	}

	public void showFriendList(String filterString) {

		lastFilterString = filterString;

		if (mGridAdapter == null) {
			mGridAdapter = new GridAdapter(this);
			mGridView.setAdapter(mGridAdapter);
			mLoaderCallbacks = new MyLoaderCallbacks();
			mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);
		} else {
			mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
		}
	}

	private class ShowFriendListAsyncTask extends AsyncTask<Cursor, FriendItem, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Cursor... params) {
			IImConnection conn = null;
			conn = mApp.getConnection(mProviderId);

			if (conn == null) {
				mProgressDlg.dismiss();
				return null;
			}

			Cursor cursor = params[0];

			while (cursor.moveToNext()) {
				FriendItem item = new FriendItem();
				item.userName = cursor.getString(cursor.getColumnIndex(Imps.Contacts.USERNAME));
				item.nickName = cursor.getString(cursor.getColumnIndex(Imps.Contacts.NICKNAME));
				item.isOnline = (cursor.getInt(cursor.getColumnIndex(Imps.Contacts.PRESENCE_STATUS))!=Imps.Presence.OFFLINE);
				publishProgress(item);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(FriendItem... item) {
			mGridAdapter.addItem(item[0]);
			super.onProgressUpdate(item);
		}
	}

	private boolean isExistInMemberList(String username) {
		if (mMemberList != null && mMemberList.size() > 0) {
			for(String item : mMemberList)
				if(item.equals(username))
					return true;
		}
		return false;
	}

	public class GridAdapter extends ArrayAdapter<FriendItem> {

		private List<FriendItem> mData = new ArrayList<FriendItem>();
		private List<FriendItem> m_items = new ArrayList<FriendItem>();

		public GridAdapter(Activity context) {
			super(context, 0);
		}

		public void addItem(FriendItem item) {
			m_items.add(item);
			notifyDataSetChanged();
		}

		public void removeAll() {
			m_items.clear();
		}

		@Override
		public int getCount() {
			if (m_items != null)
				return m_items.size();
			return 0;
		}

		@Override
		public FriendItem getItem(int position) {
			if (m_items != null)
				return m_items.get(position);
			return null;
		}

		@Override
		public long getItemId(int position) {
			if (m_items != null)
				return m_items.get(position).hashCode();
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final ViewHolder viewHolder;
			if (convertView == null) {

				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				convertView = inflater.inflate(R.layout.friend_list_item, null, true);

				viewHolder = new ViewHolder();

				viewHolder.mImgProfile = (CircularImageView) convertView.findViewById(R.id.imgProfile);
				viewHolder.mTxtNickName = (TextView) convertView.findViewById(R.id.txtNickName);
				viewHolder.mImgCheck = (ImageView) convertView.findViewById(R.id.imgCheck);
				viewHolder.mItemLayout = convertView.findViewById(R.id.itemLayout);
				viewHolder.mOnlineStatus = (ImageView) convertView.findViewById(R.id.onlineStatus);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			final FriendItem item = m_items.get(position);

			String name = item.nickName;

			if (name != null) {
				String strFilter = mTxtSearch.getText().toString();
				strFilter = strFilter.toLowerCase();
				String strLower = name.toLowerCase();
				SpannableString spannableContent = new SpannableString(name);
				if(strLower.contains(strFilter)) {
					spannableContent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.item_store_tab_indicator_color)), strLower.indexOf(strFilter), strLower.indexOf(strFilter) + strFilter.length(), 0);
					spannableContent.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dlg_trans_bg)), strLower.indexOf(strFilter), strLower.indexOf(strFilter) + strFilter.length(), 0);
				}
				viewHolder.mTxtNickName.setText(spannableContent);
			} else
				viewHolder.mTxtNickName.setText("");
			viewHolder.mTxtNickName.setSelected(true);

			boolean isExistMember = isExistInMemberList(item.userName);

			if (isExistMember) {
				viewHolder.mItemLayout.setOnClickListener(null);
			} else {
				viewHolder.mItemLayout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						if (mHorizontalAdapter.containsItem(item)) {
							mHorizontalAdapter.removeItem(item);
						} else if (mCallFrom.equals(CALL_FROM_GROUP_CHAT) && mHorizontalAdapter.getCount() >= 9) {
							Toast.makeText(mContext, R.string.group_chat_limit, Toast.LENGTH_SHORT).show();
						} else {
							mHorizontalAdapter.addItem(item);
						}
						notifyDataSetChanged();
					}
				});
			}

			viewHolder.mImgProfile.setBorderWidth(1);
			viewHolder.mImgProfile.setBorderColor(r.getColor(R.color.profile_border_color));
			GlobalFunc.showAvatar(mContext, item.userName, viewHolder.mImgProfile);

			if ( isExistMember ) {
				viewHolder.mImgCheck.setVisibility(View.VISIBLE);
			} else if (mHorizontalAdapter.containsItem(item)) {
				viewHolder.mImgCheck.setVisibility(View.VISIBLE);
			} else {
				viewHolder.mImgCheck.setVisibility(View.INVISIBLE);
			}

			if (item.isOnline) {
				viewHolder.mOnlineStatus.setVisibility(View.VISIBLE);
			} else {
				viewHolder.mOnlineStatus.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

		@Override
		public Filter getFilter() {
			return new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					final FilterResults oReturn = new FilterResults();

					if (mData.size() == 0) {
						mData = m_items;
					}

					if (constraint == null || constraint.length() == 0) {
						oReturn.count = mData.size();
						oReturn.values = mData;
					} else {
						final ArrayList<FriendItem> results = new ArrayList<FriendItem>();

						if (mData != null && mData.size() > 0) {
							for (final FriendItem item : mData) {
								if (item.nickName.toLowerCase().contains(constraint.toString()))
									results.add(item);
							}
						}
						oReturn.values = results;
					}
					return oReturn;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					@SuppressWarnings("unchecked")
					ArrayList<FriendItem> values = (ArrayList<FriendItem>) results.values;
					m_items = values;
					notifyDataSetChanged();
				}
			};
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		class ViewHolder {
			ImageView mImgCheck;
			CircularImageView mImgProfile;
			TextView mTxtNickName;
			View mItemLayout;
			ImageView mOnlineStatus;
		}
	}

	public class HorizontalAdapter extends ArrayAdapter<FriendItem> {
		private List<FriendItem> m_items = new ArrayList<FriendItem>();

		public HorizontalAdapter(Context _context, int itemHorMargin) {
			super(_context, 0);
		}

		public void addItem(FriendItem item) {
			m_items.add(m_items.size(), item);
			notifyDataSetChanged();
			int count = mHorizontalAdapter.getCount();

			if (count > 0) {
				mOkButton.setEnabled(true);
			} else {
				mOkButton.setEnabled(false);
			}
		}

		public boolean containsItem(FriendItem _item) {
			for (int i = 0; i < m_items.size(); i++) {
				FriendItem item = m_items.get(i);
				if (item.userName.equals(_item.userName)) {
					return true;
				}
			}
			return false;
		}

		public boolean removeItem(FriendItem _item) {
			for (int i = 0; i < m_items.size(); i++) {
				FriendItem item = m_items.get(i);
				if (item.userName.equals(_item.userName)) {
					m_items.remove(i);
					notifyDataSetChanged();
					int count = mHorizontalAdapter.getCount();

					if (count > 0) {
						mOkButton.setEnabled(true);
					} else {
						mOkButton.setEnabled(false);
					}

					return true;
				}
			}

			return false;
		}

		public void removeAll() {
			m_items.clear();
		}

		public List<FriendItem> getAllItem() {
			return m_items;
		}

		@Override
		public int getCount() {
			int count = 0;

			if (m_items != null)
				count = m_items.size();

			return count;
		}

		@Override
		public FriendItem getItem(int position) {
			if (m_items != null)
				return m_items.get(position);
			return null;
		}

		@Override
		public long getItemId(int position) {
			if (m_items != null)
				return m_items.get(position).hashCode();
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder;

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_list_edit_horizontal_list_item,
						null);

				viewHolder = new ViewHolder();
				viewHolder.mImgProfile = (CircularImageView) convertView.findViewById(R.id.imgProfile);

				convertView.setTag(viewHolder);
			} else
				viewHolder = (ViewHolder) convertView.getTag();

			viewHolder.mImgProfile.setBorderWidth(1);
			viewHolder.mImgProfile.setBorderColor(r.getColor(R.color.profile_border_color));
			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		class ViewHolder {
			CircularImageView mImgProfile;
		}
	}

	/*
	 * show chatlistactivity
	 */
	private void showChat(long requestedChatId, String requestedUsername, long requestedProviderId, String nickName) {
		Intent i = new Intent(this, ChatRoomActivity.class);
		i.putExtra("chatContactId", requestedChatId);
		i.putExtra("nickname", nickName);
		i.putExtra("contactName", requestedUsername);
		i.putExtra("providerId", requestedProviderId);
		i.putExtra("isGroupChat", 1);
		startActivity(i);
		finish();
	}

	private void showGroupChatDialog(final List<FriendItem> invite_friends_array) {

		ContentResolver cr = getContentResolver();

		long mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
		Cursor pCursor = cr.query(Imps.ProviderSettings.CONTENT_URI,
				new String[] { Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE },
				Imps.ProviderSettings.PROVIDER + "=?", new String[] { Long.toString(mProviderId) }, null);

		Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(pCursor, cr, mProviderId, true,
				null);

		final String chatDomain = "conference." + settings.getDomain();

		settings.close();

		String chatRoomName = DatabaseUtils.getUserNickName(getContentResolver());

		for (int i = 0; i < invite_friends_array.size(); i++) {
			FriendItem item = invite_friends_array.get(i);
			Cursor cursor = null;

			cursor = cr.query(Imps.Contacts.CONTENT_URI, new String[] { Imps.Contacts.NICKNAME },
					Imps.Contacts.USERNAME + "='" + item.userName + "'", null, null);
			String nickName = null;
			if (cursor != null && cursor.moveToFirst())
				nickName = cursor.getString(0);

			if (cursor != null)
				cursor.close();

			if (nickName != null)
				chatRoomName += "," + nickName;
		}

		final Dialog dialog = new CustomDialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.group_name_dialog);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setCanceledOnTouchOutside(true);
		final EditText groupName = dialog.findViewById(R.id.group_name);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		groupName.setText(chatRoomName);
		groupName.setSelection(chatRoomName.length());
		dialog.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomName = groupName.getText().toString();
				if (roomName.trim().isEmpty()) {
					GlobalFunc.showToast(mContext, R.string.input_group_name, false);
					return;
				}
				startGroupChat(roomName, chatDomain, invite_friends_array);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private MainProgress progressDlg;

	public void startGroupChat(final String roomName, String serverName, final List<FriendItem> invite_friend_array) {
		IImConnection conn = null;

		conn = mApp.getConnection(mProviderId);

		if (conn == null) {
			GlobalFunc.showToast(this, R.string.error_message_network_connect, false);
			return;
		}

		final IImConnection connFinal = conn;

		progressDlg.setMessage("");

		new AsyncTask<String, Void, String>() {
			@Override
			protected void onPreExecute() {
				if (ChooseFriendsActivity.this != null && !ChooseFriendsActivity.this.isFinishing())
					progressDlg.show();
			}

			@Override

			public String doInBackground(String... params) {

				String roomName = params[0];
				String roomAddress = ("G" + System.currentTimeMillis() + '@' + params[1]).toLowerCase().replace(' ',
						'_');

				try {
					IChatSessionManager manager = connFinal.getChatSessionManager();
					IChatSession session = manager.getChatSession(roomAddress);

					if (session == null) {
						session = manager.createMultiUserChatSession(roomName, roomAddress);
						if (session != null) {
							String reason = "";
							for (int i = 0; i < invite_friend_array.size(); i++) {
								FriendItem item = invite_friend_array.get(i);
								reason += item.userName + "," + item.nickName;

								if ( i != invite_friend_array.size() - 1 )
									reason += ",";
							}

							for (int i = 0; i < invite_friend_array.size(); i++) {
								FriendItem item = invite_friend_array.get(i);
								session.inviteContact("", item.userName);
							}

							String username = mPref.getString("username", "");
							String nickName = DatabaseUtils.getUserNickName(getContentResolver());
							reason = username + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain) + "," + nickName + "," + reason;
							session.sendInviteMsg(reason);

							manager.joinMultiUserChatSession(roomName, roomAddress);
							long mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);

							showChat(session.getId(), roomAddress, mProviderId, roomName);

						} else {
							// manager.getChatSession(roomAddress).leave();
							return getString(R.string.unable_to_create_or_join_group_chat);
						}
					} else {
						long mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
						showChat(session.getId(), roomAddress, mProviderId, session.getName());
					}

					return null;

				} catch (Exception e) {
					return e.toString();
				}

			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);

				if (progressDlg != null)
					progressDlg.dismiss();

				if (result != null) {
					if (result != null)
						GlobalFunc.showToast(ChooseFriendsActivity.this, result, false);
				}
			}
		}.execute(roomName, serverName);
	}

	private void showErrorDialog(int res) {
		if (this != null && !this.isFinishing()) {
			final Dialog dlg = GlobalFunc.createDialog(this, R.layout.msgdialog, true);

			TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
			title.setText(R.string.information);

			TextView content = (TextView) dlg.findViewById(R.id.msgcontent);

			if (res == ERROR_SELECT_CONTACT) {
				content.setText(R.string.select_contact);
			}

			Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
			dlg_btn_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						dlg.dismiss();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dlg.setCanceledOnTouchOutside(false);
			dlg.show();
		}
	}

	class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
		CursorLoader loader = null;

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			StringBuilder buf = new StringBuilder();
			if (lastFilterString != null) {
				buf.append(Imps.Contacts.NICKNAME);
				buf.append(" LIKE ");
				android.database.DatabaseUtils.appendValueToSql(buf, "%" + lastFilterString + "%");
			} else
				buf.append(" trim(" + Imps.Contacts.NICKNAME + ") != '' ");

			buf.append(" And " + Imps.Contacts.TYPE + "=" + Imps.Contacts.TYPE_NORMAL + " And "
					+ Imps.Contacts.SUBSCRIPTION_TYPE + "=" + Imps.Contacts.SUBSCRIPTION_TYPE_BOTH);
			buf.append(" And " + Imps.Contacts.USERNAME + " LIKE " + "'%" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain) + "%'");

			String select = Imps.Contacts.ACCOUNT + "=? AND " + buf.toString();
			String[] selectionArgs = {GlobalVariable.account_id};
            String orderby = null;
            orderby = Imps.Contacts.ORDER_BY_LOCALIZED;

			loader = new CursorLoader(ChooseFriendsActivity.this, Imps.Contacts.CONTENT_URI, CONTACT_PROJECTION,
					select, selectionArgs, orderby);

			loader.setUpdateThrottle(2000L);
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, final Cursor newCursor) {
			if (mAsyncTask != null && !mAsyncTask.isCancelled()) {
				mAsyncTask.cancel(true);
			}
			mGridAdapter.removeAll();
			while (newCursor.moveToNext()) {
				FriendItem item = new FriendItem();

				item.userName = newCursor.getString(newCursor.getColumnIndex(Imps.Contacts.USERNAME));
				if (!mTagMemberList.isEmpty() && !mTagMemberList.contains(item.userName)) {
					continue;
				}

				item.nickName = newCursor.getString(newCursor.getColumnIndex(Imps.Contacts.NICKNAME));

				mGridAdapter.addItem(item);
			}
			mLoaderManager.destroyLoader(mLoaderId);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnClose:
				mTxtSearch.setText("");
				break;
			case R.id.btnOK:

				List<FriendItem> list = mHorizontalAdapter.getAllItem();

				if (list == null || list.isEmpty()) {
					showErrorDialog(ERROR_SELECT_CONTACT);
					return;
				}

				if (mCallFrom != null && mCallFrom.equals(CALL_FROM_GROUP_CHAT)) {
					if (list.size() == 1) {
						FriendItem item = list.get(0);
						Cursor cursor;
						String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
						String[] selectionArgs = {GlobalVariable.account_id, item.userName};

						cursor = cr.query(Imps.Contacts.CONTENT_URI,
								new String[] { Imps.Contacts.PROVIDER, Imps.Contacts.NICKNAME, Imps.Contacts._ID,
										Imps.Contacts.ACCOUNT },
								selection, selectionArgs, null);

						long providerId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
						long contactId = -1;
						String nickName = "";
						if (cursor != null && cursor.moveToFirst()) {
							providerId = cursor.getLong(0);
							nickName = cursor.getString(1);
							contactId = cursor.getLong(2);
						}
						if (cursor != null)
							cursor.close();

						Intent i = new Intent(this, ChatRoomActivity.class);
						i.putExtra("chatContactId", contactId);
						i.putExtra("nickname", nickName);
						i.putExtra("contactName", item.userName);
						i.putExtra("providerId", providerId);
						startActivity(i);
						finish();
					} else {

						showGroupChatDialog(list);
					}
				} else if (mCallFrom != null && (mCallFrom.equals(CALL_FROM_SELECT_ONLY))) {
					List<FriendItem> selectedList = mHorizontalAdapter.getAllItem();

					if (selectedList != null && !selectedList.isEmpty()) {
						ArrayList<FriendItem> result = new ArrayList<FriendItem>(selectedList);

						Intent intent = new Intent();
						intent.putParcelableArrayListExtra("result.content", result);
						intent.putExtra("call_from", mCallFrom);
						setResult(RESULT_OK, intent);
						finish();
					} else {
						showErrorDialog(ERROR_SELECT_CONTACT);
					}
					break;
				}

				break;
			default:
				super.onClick(v);
				break;
		}
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

			} else {
				((ViewGroup) view).removeAllViews();
			}
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

}
