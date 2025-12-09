package com.multimediachat.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.ImApp;
import com.multimediachat.service.StatusBarNotifier;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IContactListManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.sideindexers.IndexableListView;
import com.multimediachat.ui.ChatRoomActivity;
import com.multimediachat.ui.FriendProfileChattingActivity;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.ui.MyProfileActivity;
import com.multimediachat.util.LogCleaner;
import com.multimediachat.util.StringMatcher;
import com.multimediachat.util.datamodel.FriendItem;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.linphone.LinphoneManager;

import static com.multimediachat.ui.FriendListFragment.mListTouchPosition;


public class FriendListFilterView extends LinearLayout {
	public static final String[] CONTACT_PROJECTION = { Imps.Contacts._ID, Imps.Contacts.PROVIDER,
			Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME, Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
			Imps.Contacts.SUBSCRIPTION_TYPE, Imps.Contacts.SUBSCRIPTION_STATUS, Imps.Presence.PRESENCE_STATUS,
			Imps.Chats.LAST_MESSAGE_DATE, Imps.Chats.LAST_UNREAD_MESSAGE, Imps.Contacts.AVATAR_DATA,
			Imps.Contacts.STATUSMESSAGE, Imps.Contacts.GENDER, Imps.Contacts.REGION,
			Imps.Contacts.FAVORITE, Imps.Contacts.SUBSCRIPTIONMESSAGE, Imps.Contacts.PHONE_NUMBER

	};

	private MyLoaderCallbacks mLoaderCallbacks;
	private LoaderManager mLoaderManager;
	private int mLoaderId;

	View mLoadingProgressbar;
	TextView mTxtEmpty;
	private IndexableListView mFilterList;

	public MyCustomAdapter mContactsAdapter = null;

	public static FriendItem mSelectedFriendItem = null;

	private final Context mContext;
	private ContentResolver cr;
	private String lastFilteredString;
	private String tagFilterString = null;
	private Activity mActivity;
	private int activityMode = -1; // 0:friend list, 1: friend list edit , 2:
									// hidden friends, 3: blocked friends

	private int FRIEND_STATE = Imps.Contacts.TYPE_NORMAL;

	private Resources r;
	ImApp mApp = null;

	boolean needLoad = false;
	boolean needFilter = false;
	boolean isLoading = false;
	int totalCount = 0;

	public FriendListFilterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mActivity = (Activity) context;
		mApp = (ImApp) mActivity.getApplication();
		mContext = context;
		cr = mContext.getContentResolver();
		r = context.getResources();
	}

	public void setMode(int mode) {
		activityMode = mode;
		onFinishInflate();
	}

	public void showMyProfile() {
		Intent i = new Intent(mContext, MyProfileActivity.class);
		mContext.startActivity(i);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		if (mFilterList == null) {
			mFilterList = (IndexableListView) findViewById(R.id.filteredList);
			mFilterList.setTextFilterEnabled(true);
			mFilterList.setFastScrollEnabled(false);
		}

		mTxtEmpty = (TextView) findViewById(R.id.empty);
		mLoadingProgressbar = findViewById(R.id.progressLoading);

		OnItemClickListener listener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				FriendItem item = mContactsAdapter.getItem(position);

				if (item == null)
					return;

				if (item.userName.equals("me")) {
					showMyProfile();
					return;
				}

				if (item.userName.startsWith("adminevent")) {
					showCampaignChat();
					return;
				}

				mSelectedFriendItem = item;

				if (activityMode == 0)
				{
					GlobalFunc.showFriendProfile(mContext, item);
				} else if (activityMode == 3)
				{
					Intent intent = new Intent(mActivity, FriendProfileChattingActivity.class);
					FriendProfileChattingActivity.contact = null;
					intent.putExtra("mFlag", false);
					intent.putExtra("username", item.userName);
					mActivity.startActivity(intent);
				}
			}
		};

		mFilterList.setOnItemClickListener(listener);

		if (activityMode == 0)
		{
			OnItemLongClickListener longListener = new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
					final FriendItem item = mContactsAdapter.getItem(position);
					try {
						final ViewGroup root = (ViewGroup) mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

						final View view = new View(mActivity);
						view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
						view.setBackgroundColor(Color.TRANSPARENT);

						root.addView(view);

						int location[] = new int[2];
						int location1[] = new int[2];
						root.getLocationInWindow(location1);
						mFilterList.getLocationInWindow(location);

						view.setX(mListTouchPosition[0] + location[0] - location1[0]);
						view.setY(mListTouchPosition[1] + location[1] - location1[1]);

						PopupMenu menu = new PopupMenu(mContext, view);
						menu.getMenu().add(Menu.NONE, 1, 0, R.string.delete_contact);
						menu.show();

						menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
							@Override
							public void onDismiss(PopupMenu menu) {
								root.removeView(view);
							}
						});

						menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem menuItem) {
								try {
									deleteFriend(item);
								} catch (Exception e) {
									e.printStackTrace();
								}
								return false;
							}
						});

					} catch (Exception e) {
						e.printStackTrace();
					}


					return true;
				}

			};

			mFilterList.setOnItemLongClickListener(longListener);
		}
	}

	private void showCampaignChat() {
		IImConnection conn = mApp.getConnection(mPref.getLong(GlobalConstrants.store_picaProviderId, -1));
		if (conn != null) {
			try {
				IChatSessionManager manager = conn.getChatSessionManager();

				if (manager != null) {

					String userAddr = "adminevent@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
					IChatSession session = manager.getChatSession(userAddr);

					if (session == null) {
						session = manager.createChatSession(userAddr);
					}
					if (session != null) {

						/*
						 * IContactListManager contactListMgr = null; try{
						 * contactListMgr = conn.getContactListManager();
						 * }catch(Exception e){ e.printStackTrace(); }
						 */

						Intent i = new Intent(mContext, ChatRoomActivity.class);

						i.putExtra("chatContactId", session.getId());

						i.putExtra("contactName", userAddr);

						i.putExtra("providerId", mPref.getLong(GlobalConstrants.store_picaProviderId, -1));

						mContext.startActivity(i);
					}
				}

			} catch (Exception e) {
			}
		}
	}

	public AbsListView getListView() {
		return mFilterList;
	}

	public void showList(final String filterString) {
		lastFilteredString = filterString;

		if (isLoading) {
			needLoad = true;
			return;
		}

		if (mContactsAdapter == null) {
			mContactsAdapter = new MyCustomAdapter(false);
			mFilterList.setAdapter(mContactsAdapter);
			mLoaderCallbacks = new MyLoaderCallbacks();
			mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);
		} else {
			mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
		}
	}

	public void filterListByTag(String strTag) {
		tagFilterString = strTag;

		if (isLoading) {
			needLoad = true;
			return;
		}

		if (mContactsAdapter == null) {
			mContactsAdapter = new MyCustomAdapter(false);
			mFilterList.setAdapter(mContactsAdapter);
			mLoaderCallbacks = new MyLoaderCallbacks();
			mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);
		} else {
			mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
		}
	}

	private class MyCustomAdapter extends BaseAdapter implements SectionIndexer {

		private List<FriendItem> mFilterData = new ArrayList<FriendItem>();
		private LayoutInflater mInflater;

		private String mSections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
		private int favor_count = 0;
		private boolean isForGrid = false;

		public MyCustomAdapter(boolean isForGrid) {
			this.isForGrid = isForGrid;
			mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addItem(final FriendItem item) {
			mFilterData.add(item);
		}

		public void removeAllItem() {
			mFilterData.clear();

			favor_count = 0;
			mSections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
			mFilterList.setSections(getSections());
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mFilterData == null)
				return 0;
			return mFilterData.size();
		}

		@Override
		public FriendItem getItem(int position) {
			try {
				if (mFilterData.size() > position)
					return mFilterData.get(position);
				else
					return null;
			} catch (Exception e) {
				DebugConfig.error("FriendListFilterView", "FriendListFilterView(getItem)", e);
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				if (isForGrid) {
					convertView = mInflater.inflate(R.layout.friend_view_for_grid, null);
				} else {
					convertView = mInflater.inflate(R.layout.friend_view, null);
				}
			}

			((FriendView) convertView).bind(mFilterData.get(position), true, lastFilteredString.toLowerCase(Locale.ENGLISH));
			return convertView;
		}

		@Override
		public int getPositionForSection(int section) {

			for (int i = section; i >= 0; i--) {
				for (int j = 0; j < getCount(); j++) {

					FriendItem item = getItem(j);
					if (item == null)
						continue;
					try {
						String firstNickName = String.valueOf(item.nickName.charAt(0)).toLowerCase(Locale.ENGLISH);
						String sectionName = String.valueOf(mSections.charAt(i)).toLowerCase(Locale.ENGLISH);

						if (StringMatcher.match(firstNickName, sectionName)) {
							if (item != null && item.userName != null && item.userName.equals("me"))
								continue;
							if (item != null && item.userName != null && item.userName.equals("plus_friend"))
								continue;
							if (item != null && item.userName != null && item.userName.startsWith("adminevent"))
								continue;
							return j;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			String[] sections = new String[mSections.length()];
			for (int i = 0; i < mSections.length(); i++)
				sections[i] = String.valueOf(mSections.charAt(i));
			return sections;
		}
	}

	public interface ContactListListener {
		void setTitle(String txt);
	}

	private ContactListListener mListener = null;

	public void setListener(ContactListListener listener) {
		mListener = listener;
	}

	private void deleteFriend(final FriendItem item) {
		final Dialog dlg = GlobalFunc.createDialog(mContext, R.layout.confirm_dialog, true);

		TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
		title.setText(mContext.getString(R.string.delete));
		TextView content = (TextView) dlg.findViewById(R.id.msgcontent);
		content.setText(mContext.getString(R.string.delete_contact_msg));
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
				deleteContact(item);
			}
		});

		dlg.show();
	}

	private void deleteContact(FriendItem item) {
		try {
			long providerId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
			IImConnection conn = null;
			conn = mApp.getConnection(providerId);
			if (conn != null) {
				IContactListManager manager = conn.getContactListManager();
				int res = -1;
				if (LinphoneManager.getLc().getCurrentCall() != null) {
					String deleteWarning = getResources().getString(R.string.delete_warning);
					GlobalFunc.showToast(mContext, deleteWarning, false);
					return;
				}
				int contactID = GlobalFunc.getContactId(mContext, item.userName);
				if (manager != null)
					res = manager.removeContact(item.userName);
				if (res != ImErrorInfo.NO_ERROR) {
					if (res == ImErrorInfo.CANT_CONNECT_TO_SERVER)
						GlobalFunc.showToast(mContext, R.string.error_message_network_connect, true);
					else
						AndroidUtility.showErrorMessage(mActivity, AndroidUtility.ERROR_CODE_FAILED_DELETE_CONTACT);
				} else {
					DatabaseUtils.deleteMembersFromContact(cr, item.userName);
					try {
						Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
						mContext.sendBroadcast(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						IChatSessionManager chatSessionManager = conn.getChatSessionManager();
						if (chatSessionManager != null) {
							IChatSession session = chatSessionManager.getChatSession(item.userName);
							if (session != null) {
								session.leave();
							}
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}

					NotificationManager nMgr = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
					nMgr.cancel(StatusBarNotifier.chat_notify_start_id + contactID);
				}
			}
		} catch (Exception e) {
			LogCleaner.error(ImApp.LOG_TAG, "remote error", e);
		}
	}

	public void setLoaderManager(LoaderManager loaderManager, int loaderId) {
		mLoaderManager = loaderManager;
		mLoaderId = loaderId;
	}


	private class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
		CursorLoader loader = null;

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			isLoading = true;
			needFilter = true;

			Uri uri = null;
			StringBuilder buf = new StringBuilder();
			uri = Imps.Contacts.CONTENT_URI;
			if (lastFilteredString != null) {
				buf.append(Imps.Contacts.NICKNAME);
				buf.append(" LIKE ");
				android.database.DatabaseUtils.appendValueToSql(buf, "%" + lastFilteredString + "%");
			} else
				buf.append(" trim(" + Imps.Contacts.NICKNAME + ") != '' ");

			if (FRIEND_STATE == Imps.Contacts.TYPE_BLOCKED) {
				buf.append(" And " + Imps.Contacts.TYPE + "=" + FRIEND_STATE);
			} else {
				buf.append(" And " + Imps.Contacts.TYPE + "=" + FRIEND_STATE + " And "
						+ Imps.Contacts.SUBSCRIPTION_TYPE + "=" + Imps.Contacts.SUBSCRIPTION_TYPE_BOTH);
			}

			buf.append(" And " + Imps.Contacts.USERNAME + " LIKE '%" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain) + "%'");

			String orderby = null;

			/*orderby = "IfNull" + "(" + Imps.Contacts.FAVORITE + ", 0)" + " DESC, (case when "
					+ Imps.Contacts.NICKNAME + " >='A' and " + Imps.Contacts.NICKNAME
					+ " < '[' then 1 else (case when " + Imps.Contacts.NICKNAME + " >= 'a' and "
					+ Imps.Contacts.NICKNAME + " < '{' then 1 else 2 end) end), " + Imps.Contacts.NICKNAME
					+ " COLLATE NOCASE";*/
			orderby = Imps.Contacts.ORDER_BY_LOCALIZED;

			String select = Imps.Contacts.ACCOUNT + "=? AND " + buf.toString();
			String[] selectionArgs = {GlobalVariable.account_id};

			loader = new CursorLoader(getContext(), uri,
					FriendView.CONTACT_PROJECTION , select, selectionArgs, orderby);
			loader.setUpdateThrottle(1500L);
			mTxtEmpty.setVisibility(GONE);
			mLoadingProgressbar.setVisibility(View.VISIBLE);
			DebugConfig.error("*****", "loading friend list...");
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, final Cursor newCursor) {
			DebugConfig.error("*****", "finished loading friend list.");
			if (newCursor == null) {
				isLoading = false;
				return;
			}
			loadFriendList(newCursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
		}
	}

	private void loadFriendList(final Cursor newCursor) {
		mContactsAdapter.removeAllItem();

		try {
			while (newCursor.moveToNext()) {
				FriendItem item = new FriendItem();

				item.userName = newCursor.getString(newCursor.getColumnIndex(Imps.Contacts.USERNAME));
				item.nickName = newCursor.getString(newCursor.getColumnIndex(Imps.Contacts.NICKNAME));
				item.status = newCursor.getString(newCursor.getColumnIndex(Imps.Contacts.STATUSMESSAGE));
				item.isOnline = newCursor.getInt(newCursor.getColumnIndex(Imps.Contacts.PRESENCE_STATUS)) != Imps.Presence.OFFLINE;

				if (tagFilterString != null) {
					ArrayList<String> tags = DatabaseUtils.getUserTags(cr, item.userName);
					if (tags.indexOf(tagFilterString) < 0)
						continue;
				}

				String nickName = null;
				FriendItem preItem = null;
				String preNickName = null;
				int preFavor = 0;
				int favor = 0;

				if (item != null) {
					nickName = item.nickName;
				}

				int currentCount = mContactsAdapter.getCount();

				if (currentCount > 0) {
					preItem = mContactsAdapter.getItem(currentCount - 1);
				}
				if (preItem != null) {
					preNickName = preItem.nickName;
				}

				boolean showItemHeader = false;
				char firstChar = '1';
				if (preNickName != null) {
					if (preItem.userName.equals("me") || preItem.userName.equals("plus_friend")) {
						showItemHeader = true;
						if (nickName == null || nickName.equals(""))
							nickName = " ";

						firstChar = nickName.charAt(0);
						if (StringMatcher.isKorean(firstChar)) {
							firstChar = StringMatcher.getInitialSound(firstChar);
						}
					} else {
						if (favor == 1 && preFavor != 1) {
							showItemHeader = true;
						} else if (favor != 1 && preFavor == 1) {
							showItemHeader = true;

							firstChar = nickName.charAt(0);

							if (!GlobalFunc.isEnglishChar(firstChar)) {
								firstChar = '#';
							}
						} else if (favor != 1) {

							if (preNickName.equals(""))
								preNickName = " ";
							if (nickName == null || nickName.equals(""))
								nickName = " ";

							char preFirstChar = preNickName.charAt(0);

							if (!GlobalFunc.isEnglishChar(preFirstChar)) {
								preFirstChar = '#';
							}

							firstChar = nickName.charAt(0);

							if (!GlobalFunc.isEnglishChar(firstChar)) {
								firstChar = '#';
							}

							if (!String.valueOf(firstChar).equals("")
									&& !String.valueOf(preFirstChar).equals("")) {
								if (!StringMatcher.match(String.valueOf(firstChar).toUpperCase(Locale.ENGLISH),
										String.valueOf(preFirstChar).toUpperCase(Locale.ENGLISH))) {
									showItemHeader = true;
								}
							}
						}
					}
				} else {
					if (currentCount == 0) {
						if (!item.userName.equals("me") && !item.userName.equals("plus_friend")) {
							firstChar = item.nickName.charAt(0);
							if (!GlobalFunc.isEnglishChar(firstChar)) {
								firstChar = '#';
							}
							showItemHeader = true;
						}
					}
				}

				item.firstChar = firstChar;
				mContactsAdapter.addItem(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		mContactsAdapter.notifyDataSetChanged();
		isLoading = false;

		mLoadingProgressbar.setVisibility(View.GONE);
		mTxtEmpty.setVisibility(VISIBLE);

		if (lastFilteredString.equals(""))
			totalCount = mContactsAdapter.getCount();

		if (mContactsAdapter.getCount() == 0) {
			mTxtEmpty.setVisibility(View.VISIBLE);
			if (lastFilteredString.equals("") || totalCount == 0)
				mTxtEmpty.setText(R.string.friend_list_edit_no_friends);
			else
				mTxtEmpty.setText(R.string.no_search_result);
			mFilterList.setVisibility(View.GONE);
		} else {
			mFilterList.setVisibility(View.VISIBLE);
		}

		if (needLoad) {
			needLoad = false;
			showList(lastFilteredString);
		}
	}
}
