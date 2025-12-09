package com.multimediachat.ui.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.SimpleAlertHandler;
import com.multimediachat.app.im.IContactListManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.app.adapter.ConnectionListenerAdapter;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.service.StatusBarNotifier;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.util.LogCleaner;

import static com.multimediachat.ui.ChatListFragment.mListTouchPosition;


public class ChatListFilterView extends LinearLayout {

	private AbsListView mFilterList;
	private ChatRoomListAdapter mChatRoomListAdapter;

	private Uri mUri;
	private String mFilterString;
	private final Context mContext;
	private final SimpleAlertHandler mHandler;
	private final ConnectionListenerAdapter mConnectionListener;

	MainTabNavigationActivity 	mActivity;
	private IImConnection 		mConn;
	private MyLoaderCallbacks 	mLoaderCallbacks;
	private LoaderManager 		mLoaderManager;
	private Resources			r;     
	ImApp mApp;


	public ChatListFilterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mActivity = (MainTabNavigationActivity)context;
		mContext = context;
		r = mContext.getResources();
		mHandler = new SimpleAlertHandler((Activity)context);
		mApp = (ImApp) mActivity.getApplication();
		mConnectionListener = new ConnectionListenerAdapter(mHandler) {
			@Override
			public void onConnectionStateChange(IImConnection connection, int state,
					ImErrorInfo error) {
			}

			@Override
			public void onUpdateSelfPresenceError(IImConnection connection, ImErrorInfo error) {
				super.onUpdateSelfPresenceError(connection, error);
			}

			@Override
			public void onSelfPresenceUpdated(IImConnection connection) {
				super.onSelfPresenceUpdated(connection);
			}  


		};
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mFilterList = (AbsListView) findViewById(R.id.filteredList);
		mFilterList.setTextFilterEnabled(true);
		mFilterList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor c = (Cursor) mFilterList.getItemAtPosition(position);
				if (mListener != null)
					mListener.startChat(c);
			}
		});

		mFilterList.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
				try{
					final ViewGroup root = (ViewGroup) mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

					final View view = new View(mActivity);
					view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
					view.setBackgroundColor(Color.TRANSPARENT);

					root.addView(view);

					int location[] = new int[2];
					int location1[] = new int[2];
					root.getLocationInWindow(location1);
					mFilterList.getLocationInWindow(location);

					view.setX(mListTouchPosition[0]+location[0]-location1[0]);
					view.setY(mListTouchPosition[1]+location[1]-location1[1]);

					PopupMenu menu = new PopupMenu(mContext, view);

					Cursor cursor = (Cursor) mFilterList.getItemAtPosition(position);
					if (cursor.getInt(cursor.getColumnIndex(Imps.Chats.GROUP_CHAT)) != Imps.Chats.SINGLE_CHAT)
						menu.getMenu().add(Menu.NONE, 2, 0, R.string.change_group_name);
					menu.getMenu().add(Menu.NONE, 1, 1, R.string.delete_conversation);
					menu.show();

					menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
						@Override
						public void onDismiss(PopupMenu menu) {
							root.removeView(view);
						}
					});

					menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							if (item.getItemId() == 1)
								deleteChat(position);
							else
								changeName(position);
							return false;
						}
					});

				}catch(Exception e){
					e.printStackTrace();
				}
				return true;
			}
		});

	}

	private void changeName(final int position) {
		Cursor cursor = (Cursor) mFilterList.getItemAtPosition(position);
		if (cursor == null)
			return;

		String prevName = cursor.getString(cursor.getColumnIndex(Imps.Contacts.NICKNAME));
		final int contactId = cursor.getInt(cursor.getColumnIndex(Imps.Contacts._ID));

		final Dialog dlg = new Dialog(mContext);
		dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlg.setContentView(R.layout.group_name_dialog);
		dlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		final EditText groupName = dlg.findViewById(R.id.group_name);
		groupName.setText(prevName);
		groupName.setSelection(prevName.length());

		dlg.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = groupName.getText().toString();
				if (name.trim().length() == 0) {
					GlobalFunc.showToast(mContext, R.string.input_group_name, false);
					return;
				}
				ContentValues values = new ContentValues(1);
				values.put(Imps.Contacts.NICKNAME, name);
				mActivity.getContentResolver().update(Imps.Contacts.CONTENT_URI, values, Imps.Contacts._ID + "=?", new String[]{String.valueOf(contactId)});
				Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
				mActivity.sendBroadcast(intent);
				dlg.dismiss();
			}
		});

		dlg.show();
	}

	private void deleteChat(final int position) {
		final Dialog dlg = GlobalFunc.createDialog(mContext, R.layout.confirm_dialog, true);

		TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
		title.setText(mContext.getString(R.string.delete));
		TextView content = (TextView) dlg.findViewById(R.id.msgcontent);
		content.setText(mContext.getString(R.string.delete_chat_room));
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
				if (!outRoom(position)) {
					AndroidUtility.showErrorMessage(mActivity, AndroidUtility.ERROR_CODE_FAILED_DELETE_ROOM);
				}
			}
		});

		dlg.show();
	}

	public AbsListView getListView() {
		return mFilterList;
	}

	public void setConnection(IImConnection conn) {

		if (mConn != conn) {
			if (mConn != null) {
				unregisterListeners();
			}

			mConn = conn;

			if (conn != null) {
				registerListeners();
			}
		}
	}

	private void registerListeners() {
		try {
			mConn.registerConnectionListener(mConnectionListener);
		} catch (Exception e) {
			mHandler.showServiceErrorAlert(e.getLocalizedMessage());
			LogCleaner.error(ImApp.LOG_TAG, "remote error",e);
		}
	}

	private void unregisterListeners() {
		try {
			mConn.unregisterConnectionListener(mConnectionListener);
		} catch (Exception e) {
			mHandler.showServiceErrorAlert(e.getLocalizedMessage());
			LogCleaner.error(ImApp.LOG_TAG, "remote error",e);
		}
	}


	public void doFilter(Uri uri) {
		if (uri != null && !uri.equals(mUri)) {
			mUri = uri;
		}

		if (mChatRoomListAdapter == null) {
			mChatRoomListAdapter = new ChatRoomListAdapter(mContext, R.layout.chat_list_item);
			mFilterList.setAdapter(mChatRoomListAdapter);
			mLoaderCallbacks = new MyLoaderCallbacks();
			mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);
		} else {
			mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
		}
	}

	private class ChatRoomListAdapter extends ResourceCursorAdapter {
        public ChatRoomListAdapter(Context context, int view) {
            super(context, view, null, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ChatListItemView v = (ChatListItemView) view;
            v.bind(cursor);
        }
    }

	public interface ContactListListener {
		void startChat(Cursor c);
	}


	private ContactListListener mListener = null;
	private int mLoaderId;

	public void setListener (ContactListListener listener)
	{
		mListener = listener;
	}

	public boolean outRoom(final int aPosition)
	{
		Cursor cursor = null;
		try{
			cursor = (Cursor)mFilterList.getItemAtPosition(aPosition);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if (cursor == null || cursor.getCount() == 0) {
			return false;
		}

		final long   contactId = cursor.getLong(cursor.getColumnIndex(Imps.Contacts._ID));
		int isGroup = cursor.getInt(cursor.getColumnIndex(Imps.Chats.GROUP_CHAT));

		Imps.Notifications.removeNotificationCount(mActivity.getContentResolver(), Imps.Notifications.CAT_CHATTING,
				Imps.Notifications.FIELD_CHAT, (int) contactId);
		Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
		mActivity.sendBroadcast(intent);

		GlobalFunc.clearHistoryMessages(mActivity, (int) contactId);

		deleteChat(contactId, isGroup);

		NotificationManager nMgr = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		nMgr.cancel(StatusBarNotifier.chat_notify_start_id + (int) contactId);
		return true;
	}

	private void deleteChat (long contactId, int isGroup)
	{
		Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, contactId);
		Uri messageUri = ContentUris.withAppendedId(Imps.Messages.CONTENT_URI, contactId);
		mActivity.getContentResolver().delete(chatUri,null,null);
		mActivity.getContentResolver().delete(messageUri,null,null);
		if ( isGroup == Imps.Chats.GROUP_LEAVE ) {
			Uri contactUri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId);
			Uri groupMemberUri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, contactId);
			mActivity.getContentResolver().delete(contactUri,null,null);
			mActivity.getContentResolver().delete(groupMemberUri,null,null);
		}
	}


	public void setLoaderManager(LoaderManager loaderManager, int loaderId) {
		mLoaderManager = loaderManager;
		mLoaderId = loaderId;
	}

	private class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
		@Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();
			String select = Imps.Contacts.ACCOUNT + "=?";
			String[] selectionArgs = {GlobalVariable.account_id};
			buf.append(" trim(" + Imps.Contacts.NICKNAME + ") != '' ");
			select += " AND " + buf.toString();
            CursorLoader loader = new CursorLoader(getContext(), mUri, ChatListItemView.CONTACT_PROJECTION,
                    select, selectionArgs, Imps.Contacts.CHAT_SORT_ORDER);
            loader.setUpdateThrottle(500L);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
			mChatRoomListAdapter.swapCursor(newCursor);
			if (newCursor.getCount() == 0) {
				findViewById(R.id.empty).setVisibility(View.VISIBLE);
				findViewById(R.id.filteredList).setVisibility(View.GONE);
			} else {
				findViewById(R.id.filteredList).setVisibility(View.VISIBLE);
				findViewById(R.id.empty).setVisibility(View.GONE);
			}
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mChatRoomListAdapter.swapCursor(null);
        }
        
    }
}
