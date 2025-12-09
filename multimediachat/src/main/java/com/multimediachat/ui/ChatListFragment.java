package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.multimediachat.app.ImApp;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.util.LogCleaner;
import com.multimediachat.ui.views.ChatListFilterView;
import com.multimediachat.ui.views.ChatListFilterView.ContactListListener;

@SuppressLint("DefaultLocale")
public class ChatListFragment extends Fragment implements ContactListListener{
	private static final int 	CHAT_LIST_LOADER_ID = 4445;

	ChatListFilterView 		mFilterView = null;
	ImApp mApp;
	ContentResolver				cr;
	Resources					r;
	MainProgress 				progressDlg;
	View				        chat_list_fragment_view;
	public static int mListTouchPosition[] = new int[2];

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		chat_list_fragment_view = inflater.inflate(R.layout.chat_list_fragment, container, false);

		mFilterView = chat_list_fragment_view.findViewById(R.id.chatFilterView);
		mFilterView.setListener(this);
		mFilterView.setLoaderManager(getLoaderManager(),  CHAT_LIST_LOADER_ID);//modified

		showChatRoomList();

		mApp = (ImApp)getActivity().getApplication();
		cr = getActivity().getContentResolver();
		r = getActivity().getResources();

		mFilterView.findViewById(R.id.filteredList).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mListTouchPosition[0] = (int)event.getX();
				mListTouchPosition[1] = (int)event.getY();
				return false;
			}
		});

		progressDlg = new MainProgress(getActivity());
		return chat_list_fragment_view;
	}

	@Override
	public void startChat(Cursor c) {
		myStartChat(c);
	}

	public void showChatRoomList()
	{
		Uri baseUri = Imps.Contacts.CONTENT_URI_CHAT_CONTACTS;
		Uri.Builder builder = baseUri.buildUpon();
		mFilterView.doFilter(builder.build());
	}

	private void myStartChat(Cursor c) {
		if (c != null && (!  c.isAfterLast())) {
			final long chatContactId = c.getLong(c.getColumnIndexOrThrow(Imps.Contacts._ID));
			final String username = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.USERNAME));
			final String nickname = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.NICKNAME));
			final long providerId = c.getLong(c.getColumnIndexOrThrow(Imps.Contacts.PROVIDER));
			int isGroup = c.getInt(c.getColumnIndex(Imps.Chats.GROUP_CHAT));

			IImConnection conn = mApp.getConnection(providerId);

			if (conn != null)
			{
				try {
					IChatSessionManager manager = conn.getChatSessionManager();

					IChatSession session;

					if ( manager == null )
						return;

					session = manager.getChatSession(username);

					if (session == null) {
						if (isGroup == Imps.Chats.GROUP_LIVE){
							try{
								session = manager.joinMultiUserChatSession(nickname, username);
								if ( session == null )
									manager.createMultiUserChatSession(nickname, username);
							}catch(Exception e){e.printStackTrace();}
						} else if ( isGroup == Imps.Chats.SINGLE_CHAT ){
							try{
								manager.createChatSession(username);
							}catch(Exception e){e.printStackTrace();}
						}
					} 

					showChat( chatContactId, username, providerId , nickname, isGroup);

				} catch (Exception e) {
					LogCleaner.debug(ImApp.TAG, "remote exception starting chat");
				}

			}
			else
			{
				LogCleaner.debug(ImApp.TAG, "could not start chat as connection was null");
			}
		}
	}

	/*
	 * show chatlistactivity
	 */
	public boolean showChat (long requestedChatId, String requestedUsername, long requestedProviderId, String nickName, int isGroup)
	{
		Intent i = new Intent(getActivity(), ChatRoomActivity.class);

		i.putExtra("chatContactId", requestedChatId);
		i.putExtra("contactName", requestedUsername);

		if ( requestedUsername != null && requestedUsername.equals(mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain)) )
			i.putExtra("nickname", getString(R.string.chat_list_picatalk_team_name));
		else
			i.putExtra("nickname", nickName);

		i.putExtra("providerId", requestedProviderId);
		i.putExtra("isGroupChat", isGroup);

		startActivity(i);

		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

}