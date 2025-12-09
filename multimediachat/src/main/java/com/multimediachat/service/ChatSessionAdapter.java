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

package com.multimediachat.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.packet.Packet;

import com.multimediachat.app.NotificationCenter;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.MediaController;
import com.multimediachat.app.im.IChatListener;
import com.multimediachat.app.im.engine.ChatGroup;
import com.multimediachat.app.im.engine.ChatGroupManager;
import com.multimediachat.app.im.engine.ChatSession;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.GroupListener;
import com.multimediachat.app.im.engine.GroupMemberListener;
import com.multimediachat.app.im.engine.ImConnection;
import com.multimediachat.app.im.engine.ImEntity;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.engine.Message;
import com.multimediachat.app.im.engine.MessageListener;
import com.multimediachat.app.im.engine.Presence;
import com.multimediachat.app.im.plugin.xmpp.Oper;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.ChatRoomActivity;
import com.multimediachat.ui.MainTabNavigationActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.BaseColumns;

public class ChatSessionAdapter extends com.multimediachat.app.im.IChatSession.Stub {

	private static final String NON_CHAT_MESSAGE_SELECTION = Imps.Messages.TYPE + "!="
			+ Imps.MessageType.INCOMING + " AND "
			+ Imps.Messages.TYPE + "!="
			+ Imps.MessageType.OUTGOING;

	/** The registered remote listeners. */
	final RemoteCallbackList<IChatListener> mRemoteListeners = new RemoteCallbackList<IChatListener>();

	ImConnectionAdapter mConnection;
	ChatSessionManagerAdapter mChatSessionManager;


	ChatSession mChatSession;
	ListenerAdapter mListenerAdapter;
	boolean mIsGroupChat;
	StatusBarNotifier mStatusBarNotifier;

	private ContentResolver mContentResolver;
	/*package*/Uri mChatURI;

	private Uri mMessageURI;

	private boolean mConvertingToGroupChat;

	private static final int MAX_HISTORY_COPY_COUNT = 10;

	private HashMap<String, Integer> mContactStatusMap = new HashMap<String, Integer>();

	private MessengerService service = null;
	private long mContactId;

	public ChatSessionAdapter(ChatSession chatSession, ImConnectionAdapter connection) {

		mChatSession = chatSession;
		mConnection = connection;
		service = connection.getContext();
		mContentResolver = service.getContentResolver();
		mStatusBarNotifier = service.getStatusBarNotifier();
		mChatSessionManager = (ChatSessionManagerAdapter) connection.getChatSessionManager();
		mListenerAdapter = new ListenerAdapter();
		
		mChatSession.addMessageListener(new ChatListener(mListenerAdapter));
		ImEntity participant = mChatSession.getParticipant();
		if (participant instanceof ChatGroup) {
			init((ChatGroup) participant);
		} else {
			init((Contact) participant);
		}
	}

	private void init(ChatGroup group) {
		mIsGroupChat = true;
		mContactId = insertGroupContactInDb(group);
		group.addMemberListener(mListenerAdapter);

		mMessageURI = Imps.Messages.getContentUriByThreadId(mContactId);

		mChatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, mContactId);

		//insertOrUpdateChat(null);

		for (Contact c : group.getMembers()) {
			mContactStatusMap.put(c.getName(), c.getPresence().getStatus());
		}
	}

	private void init(Contact contact) {
		mIsGroupChat = false;
		ContactListManagerAdapter listManager = (ContactListManagerAdapter) mConnection
				.getContactListManager();
		mContactId = listManager.queryOrInsertContact(contact);

		mMessageURI = Imps.Messages.getContentUriByThreadId(mContactId);

		mChatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, mContactId);
		//insertOrUpdateChat(null);

		mContactStatusMap.put(contact.getName(), contact.getPresence().getStatus());
	}

	private ChatGroupManager getGroupManager() {
		return mConnection.getAdaptee().getChatGroupManager();
	}

	public ChatSession getAdaptee() {
		return mChatSession;
	}

	public Uri getChatUri() {
		return mChatURI;
	}

	public List<Contact> getParticipants() { //modified by cys 2014/04/9 10:18 pm

		if (mIsGroupChat) {
			ChatGroup group = (ChatGroup) mChatSession.getParticipant();

			List<Contact> members = group.getMembers();
			if ( members.isEmpty() ) return null;

			return members;
		} else {
			List<Contact> members = new ArrayList<Contact>();
			Contact contact = (Contact) mChatSession.getParticipant();
			if ( contact != null )
				members.add(contact);
			return members;
		}
	}

	/**
	 * Convert this chat session to a group chat. If it's already a group chat,
	 * nothing will happen. The method works in async mode and the registered
	 * listener will be notified when it's converted to group chat successfully.
	 * 
	 * Note that the method is not thread-safe since it's always called from the
	 * UI and Android uses single thread mode for UI.
	 */
	public void convertToGroupChat() {
		if (mIsGroupChat || mConvertingToGroupChat) {
			return;
		}

		mConvertingToGroupChat = true;
		new ChatConvertor().convertToGroupChat();
	}

	public boolean isGroupChatSession() {
		return mIsGroupChat;
	}

	public String getName() {
		return mChatSession.getParticipant().getAddress().getUser();
	}

	public String getAddress() {
		return mChatSession.getParticipant().getAddress().getAddress();
	}

	public long getId() {
		return ContentUris.parseId(mChatURI);
	}

	public void inviteContact(String reason, String contact) {
		if (!mIsGroupChat) {
			return;
		}

		//insertOrUpdateChat(service.getResources().getString(R.string.chat_msg_you_invite_friend, contact));

		ContactListManagerAdapter listManager = (ContactListManagerAdapter) mConnection
				.getContactListManager();
		Contact invitee = listManager.getContactByAddress(contact);
		if (invitee == null) {
			ImErrorInfo error = new ImErrorInfo(ImErrorInfo.ILLEGAL_CONTACT_ADDRESS,
					"Cannot find contact with address: " + contact);
			mListenerAdapter.onError((ChatGroup) mChatSession.getParticipant(), error);
		} else {

			getGroupManager().inviteUserAsync(reason, (ChatGroup) mChatSession.getParticipant(), invitee);

			/*String nickName = DatabaseUtils.getUserNickName(mContentResolver);
			if (!nickName.equals(invitee.getName()))
				insertGroupMemberInDb(invitee, true);
			else
				insertGroupMemberInDb(invitee, false);*/
			insertGroupMemberInDb(invitee, false);
			//below code is only to show invitation message in the history view.
/*
			String userNickName = DatabaseUtils.getUserNickName(mContentResolver);

			String text = service.getString(R.string.group_chat_invite_msg, userNickName , invitee.getName());
			//String text = service.getString(R.string.group_chat_invite_msg, invitee.getName());
			Message msg = new Message(text);
			msg.setFrom(mConnection.getLoginUser().getAddress());
			msg.setType(Imps.MessageType.FRIEND_INVITE);
			mChatSession.sendMessageAsync(msg);
*/

			//insertMessageInDb(null, text, now, msg.getType(), 0, msg.getID());
		}
	}

	public void sendInviteMsg(String text) {
		Message msg = new Message(text);
		msg.setFrom(mConnection.getLoginUser().getAddress());
		msg.setType(Imps.MessageType.FRIEND_INVITE);
		mChatSession.sendMessageAsync(msg);
	}

	public int leave() {
		if (mIsGroupChat) {
			int res = getGroupManager().leaveChatGroupAsync((ChatGroup) mChatSession.getParticipant());
			if ( res != ImErrorInfo.NO_ERROR )
				return res;

			ContentValues value = new ContentValues();
			value.put(Imps.Chats.GROUP_CHAT, Imps.Chats.GROUP_LEAVE);
			Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, mContactId);
			mContentResolver.update(chatUri, value, null, null);

			/*Uri contactUri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mContactId);
			mContentResolver.delete(contactUri, null, null);*/
		}

		//mContentResolver.delete(mChatURI, null, null);
		mStatusBarNotifier.dismissChatNotification(mConnection.getProviderId(), getAddress());
		mChatSessionManager.closeChatSession(this);
		try{
			Imps.Notifications.removeNotificationCount(mContentResolver, Imps.Notifications.CAT_CHATTING, Imps.Notifications.FIELD_CHAT, (int)mContactId);
			Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
			service.sendBroadcast(intent);
		}catch(Exception e){
			e.printStackTrace();
		}
		return ImErrorInfo.NO_ERROR;
	}

	public void leaveIfInactive() {
		if (mChatSession.getHistoryMessages().isEmpty()) {
			leave();
		}
	}

	public void sendMessage(final String message, String offerId, String filePath) {
		long nowUTC = GlobalFunc.getCurrentUTCTime();
		insertOrUpdateChat(message);
		String msgPacketId;

		if (mConnection.getState() != ImConnection.LOGGED_IN) {  
			if ( offerId == null ) {
				msgPacketId = org.jivesoftware.smack.packet.Message.nextID();
				insertMessageInDb(null, message, nowUTC , Imps.MessageType.POSTPONED, 0, msgPacketId, mPref.getLong(GlobalConstrants.TIME_DIFF_WITH_SERVER, 1));
			}
			return;
		}
		if ( offerId != null ) {
			long messageDate = Imps.getMessageDate(mContentResolver, offerId);
			if ( messageDate > 0 )
				nowUTC = messageDate;
		}

		Message msg = new Message(message);
		msg.setFrom(mConnection.getLoginUser().getAddress());
		msg.setType(Imps.MessageType.OUTGOING);
		msg.setDateTime(new Date(GlobalFunc.convertUTCToLocalTime(nowUTC)));
		msg.setTimeDiff(mPref.getLong(GlobalConstrants.TIME_DIFF_WITH_SERVER, 1));
		msg.setID(offerId);

		String resultPacketId = mChatSession.sendMessageAsync(msg);

		{
			if ( offerId == null ) {
				insertMessageInDb(null, message, nowUTC, Imps.MessageType.POSTPONED, 0, resultPacketId, null);
			} else if (filePath == null) {
				Imps.updateMessagePacketIdInDb(mContentResolver, offerId, resultPacketId);
			}
		}
	}

	public void insertCallMessageInDb(String message, int messageType, boolean isVideo, int callDuration) {
		long nowUTC = GlobalFunc.getCurrentUTCTime();
		insertOrUpdateChat(message);
		String msgPacketId;

		msgPacketId = org.jivesoftware.smack.packet.Message.nextID();
		if (isVideo)
			msgPacketId = GlobalConstrants.VIDEO_LOG + "-" + msgPacketId;
		else
			msgPacketId = GlobalConstrants.AUDIO_LOG + "-" + msgPacketId;

		long date_field = 0;
		long servertime_field = 0;

		if (messageType == Imps.MessageType.OUTGOING) {
			date_field = nowUTC - callDuration * 1000;
			servertime_field = nowUTC;
		} else {
			date_field = nowUTC;
			servertime_field = nowUTC - callDuration * 1000;
		}

		/*if (mConnection.getState() != ImConnection.LOGGED_IN) {
			insertMessageInDb(null, message, date_field, Imps.MessageType.POSTPONED, 0, msgPacketId, mPref.getLong(GlobalConstrants.TIME_DIFF_WITH_SERVER, 1));
			Imps.updateMessageServerTimeInDb(mContentResolver, msgPacketId, servertime_field);
			return;
		}*/

		insertMessageInDb(null, message, date_field, messageType, 0, msgPacketId, null);
		Imps.updateMessageServerTimeInDb(mContentResolver, msgPacketId, servertime_field);
		if (messageType == Imps.MessageType.INCOMING)
			Imps.updateDeliveryStateInDb(mContentResolver, msgPacketId, 1);
	}

	void sendPostponedMessages() {
		//MediaController.uploadFailedAudios();
		
		String[] projection = new String[] { BaseColumns._ID, Imps.Messages.BODY,
				Imps.Messages.PACKET_ID,
				Imps.Messages.DATE, Imps.Messages.TYPE, Imps.Messages.TIME_DELAY};
		String selection = Imps.Messages.TYPE + "=? AND " + Imps.Messages.MIME_TYPE + " ISNULL";

		Cursor c =  mContentResolver.query(mMessageURI, projection, selection,
				new String[] { Integer.toString(Imps.MessageType.POSTPONED) }, null);

		if (c == null) {
			MessengerService.debug("Query error while querying postponed messages");
			return;
		}

		while (c.moveToNext()) {
			try{
				String body = c.getString(1);
				String id = c.getString(2);
				long nTime = c.getLong(3);
				Message msg = new Message(body);
				msg.setFrom(mConnection.getLoginUser().getAddress());
				msg.setID(id);
				msg.setDateTime(new Date(nTime));
				msg.setTimeDiff(c.getLong(5));
				msg.setType(Imps.MessageType.OUTGOING);
				if (mConnection.getState() == ImConnection.LOGGED_IN) {  
					mChatSession.sendMessageAsync(msg);   
					//updateMessageInDb(id, Imps.MessageType.OUTGOING, nTime);
					mListenerAdapter.onSentMessage(mChatSession, id);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		c.close();
		
		Cursor cursor2= null;
		try{
			cursor2 = mContentResolver.query(mMessageURI, 
					new String[]{BaseColumns._ID,  Imps.Messages.BODY, Imps.Messages.PACKET_ID,  Imps.Messages.TYPE},
					Imps.Messages.TYPE + "='" + Imps.MessageType.OPER_DELETE + "' or " + 
					Imps.Messages.TYPE + "='" + Imps.MessageType.OPER_SEEN + "' or " + 
					Imps.Messages.TYPE + "='" + Imps.MessageType.OPER_MODIFY + "'",
					null,
					null);
			while(cursor2.moveToNext()) {
				try{
					String body = cursor2.getString(1);
					String myPacketId = cursor2.getString(2);
					int    type = cursor2.getInt(3);
					mContentResolver.delete(mMessageURI, Imps.Messages.PACKET_ID + "='" + myPacketId + "'", null);
					
					Message msg = new Message("");
					msg.setFrom(mConnection.getLoginUser().getAddress());
					
					if ( type == Imps.MessageType.OPER_DELETE ){
						msg.setType(Imps.MessageType.OPER_DELETE);
						msg.setOperType(Oper.TYPE_DELETE);
						msg.setOperMessage("");
					}
					else if ( type == Imps.MessageType.OPER_SEEN ) {
						msg.setType(Imps.MessageType.OPER_SEEN);
						msg.setOperType(Oper.TYPE_SEEN);
						msg.setOperMessage("");
					}
					else if ( type == Imps.MessageType.OPER_MODIFY ) {
						String message = Imps.getMessageBody(mContentResolver, body);
						if ( message == null )
							msg.setOperMessage("");
						else
							msg.setOperMessage(message);
					}
					
					msg.setOperMsgId(body);
					msg.setID(myPacketId);
					mChatSession.sendMessageAsync(msg);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor2 != null )
				cursor2.close();
			cursor2 = null;
		}

		GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
		GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
		service.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED));
		service.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
	}

	public void registerChatListener(IChatListener listener) {
		if (listener != null) {
			mRemoteListeners.register(listener);
		}
	}

	public void unregisterChatListener(IChatListener listener) {
		if (listener != null) {
			mRemoteListeners.unregister(listener);
		}
	}

	String getNickName(String username) {
		ImEntity participant = mChatSession.getParticipant();
		if (mIsGroupChat) {
			ChatGroup group = (ChatGroup) participant;
			List<Contact> members = group.getMembers();
			for (Contact c : members) {
				if (username.equals(c.getAddress().getAddress())) {
					return c.getName();
				}
			}
			// not found, impossible
			return username;
		} else {
			return ((Contact) participant).getName();
		}
	}

	void onConvertToGroupChatSuccess(ChatGroup group) {
		Contact oldParticipant = (Contact) mChatSession.getParticipant();
		String oldAddress = getAddress();
		mChatSession.setParticipant(group);
		mChatSessionManager.updateChatSession(oldAddress, this);

		Uri oldChatUri = mChatURI;
		Uri oldMessageUri = mMessageURI;
		init(group);
		copyHistoryMessages(oldParticipant);

		mContentResolver.delete(oldMessageUri, NON_CHAT_MESSAGE_SELECTION, null);
		mContentResolver.delete(oldChatUri, null, null);

		mListenerAdapter.notifyChatSessionConverted();
		mConvertingToGroupChat = false;
	}

	private void copyHistoryMessages(Contact oldParticipant) {
		List<Message> historyMessages = mChatSession.getHistoryMessages();
		int total = historyMessages.size();
		int start = total > MAX_HISTORY_COPY_COUNT ? total - MAX_HISTORY_COPY_COUNT : 0;
		for (int i = start; i < total; i++) {
			Message msg = historyMessages.get(i);
			boolean incoming = msg.getFrom().equals(oldParticipant.getAddress());
			String contact = incoming ? oldParticipant.getName() : null;
			long time = msg.getDateTime().getTime();
			insertMessageInDb(contact, msg.getBody(), time, incoming ? Imps.MessageType.INCOMING
					: Imps.MessageType.OUTGOING, Packet.nextID());
		}
	}

	void insertOrUpdateChat(String message) {
		ContentValues values = new ContentValues(2);

		values.put(Imps.Chats.LAST_MESSAGE_DATE, GlobalFunc.getCurrentUTCTime());
		values.put(Imps.Chats.LAST_UNREAD_MESSAGE, message);
		values.put(Imps.Chats.GROUP_CHAT, isGroupChatSession());


		Cursor cursor = mContentResolver.query(mChatURI, new String[]{Imps.Chats._ID}, null, null, null);

		if (cursor.moveToFirst()) {
			if ( message != null )
				mContentResolver.update(mChatURI, values, null, null);
			cursor.close();
			return;
		}
		cursor.close();
		mContentResolver.insert(mChatURI, values);
		return;
	}

	private long insertGroupContactInDb(ChatGroup group) {
		ContentValues values = new ContentValues(4);
		values.put(Imps.Contacts.USERNAME, group.getAddress().getAddress());
		values.put(Imps.Contacts.NICKNAME, group.getName());
		values.put(Imps.Contacts.CONTACTLIST, ContactListManagerAdapter.FAKE_TEMPORARY_LIST_ID);
		values.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_GROUP);

		Uri contactUri = ContentUris.withAppendedId(
				ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mConnection.mProviderId),
				mConnection.mAccountId);

		Cursor cursor = mContentResolver.query(contactUri, new String[]{Imps.Contacts.NICKNAME, Imps.Contacts._ID}, Imps.Contacts.USERNAME + "=?", new String[]{group.getAddress().getAddress()}, null);

		if ( cursor.moveToFirst() )
		{
			long id = cursor.getLong(cursor.getColumnIndex(Imps.Contacts._ID));
			cursor.close();
			return id;
		}

		cursor.close();

		long id = ContentUris.parseId(mContentResolver.insert(contactUri, values));
		ArrayList<ContentValues> memberValues = new ArrayList<ContentValues>();
		Contact self = mConnection.getLoginUser();
		for (Contact member : group.getMembers()) {
			if (!member.equals(self)) { // avoid to insert the user himself
				ContentValues memberValue = new ContentValues(2);
				memberValue.put(Imps.GroupMembers.USERNAME, member.getAddress().getAddress());
				memberValue.put(Imps.GroupMembers.NICKNAME, member.getName());
				memberValues.add(memberValue);
			}
		}
		if (!memberValues.isEmpty()) {
			ContentValues[] result = new ContentValues[memberValues.size()];
			memberValues.toArray(result);
			Uri memberUri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, id);
			mContentResolver.bulkInsert(memberUri, result);
		}
		return id;
	}

	public String getTitle()
	{
		if ( mIsGroupChat )
		{
			ChatGroup group = (ChatGroup) mChatSession.getParticipant();

			return group.getName();
		}

		Contact contact = (Contact) mChatSession.getParticipant();
		return contact.getName();
	}

	void updateGroupNickNameInDb(String nickName) {
		ChatGroup group = (ChatGroup) mChatSession.getParticipant();

		Uri contactUri = ContentUris.withAppendedId(
				ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mConnection.mProviderId),
				mConnection.mAccountId);

		Cursor cursor = mContentResolver.query(contactUri, new String[]{Imps.Contacts.NICKNAME, Imps.Contacts._ID}, Imps.Contacts.USERNAME + "=?", new String[]{group.getAddress().getAddress()}, null);

		if ( cursor.moveToFirst() )
		{
			long id = cursor.getLong(cursor.getColumnIndex(Imps.Contacts._ID));
			cursor.close();
			ContentValues values = new ContentValues(1);
			values.put(Imps.Contacts.NICKNAME, nickName);
			mContentResolver.update(contactUri, values, Imps.Contacts.USERNAME + "=?", new String[]{group.getAddress().getAddress()});
			group.setName(nickName);
		}

		cursor.close();

	}

	void updateGroupMemberInDb(Contact member) {
		long groupId = ContentUris.parseId(mChatURI);
		Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);

		Cursor cursor = mContentResolver.query(uri, 
				new String[]{Imps.GroupMembers.USERNAME}, 
				Imps.GroupMembers.USERNAME + "=?", 
						new String[]{member.getAddress().getAddress()}, 
						null);

		if ( cursor.moveToFirst() )
		{
			cursor.close();
			ContentValues values = new ContentValues(1);
			values.put(Imps.GroupMembers.NICKNAME, member.getName());
			mContentResolver.update(uri, values, Imps.GroupMembers.USERNAME + "='" + member.getAddress().getAddress() + "'", null);
		}
		else{
			ContentValues values = new ContentValues(3);
			values.put(Imps.GroupMembers.USERNAME, member.getAddress().getAddress());
			values.put(Imps.GroupMembers.NICKNAME, member.getName());
			values.put(Imps.GroupMembers.TYPE, -1);
			
			cursor.close();
			mContentResolver.insert(uri, values);
		}
	}


	void insertGroupMemberInDb(Contact member, boolean updateRoomName) {
		ContentValues values1 = new ContentValues(3);
		values1.put(Imps.GroupMembers.USERNAME, member.getAddress().getAddress());
		values1.put(Imps.GroupMembers.NICKNAME, member.getName());
		values1.put(Imps.GroupMembers.TYPE, Imps.GroupMembers.TYPE_NORMAL);
		
		ContentValues values2 = new ContentValues(2);
		values2.put(Imps.GroupMembers.NICKNAME, member.getName());
		values2.put(Imps.GroupMembers.TYPE, Imps.GroupMembers.TYPE_NORMAL);

		long groupId = ContentUris.parseId(mChatURI);
		Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);

		Cursor cursor = null;
		
		try{
			cursor = mContentResolver.query(uri, 
					new String[]{Imps.GroupMembers.USERNAME, Imps.GroupMembers.TYPE}, 
					Imps.GroupMembers.USERNAME + "=?", 
							new String[]{member.getAddress().getAddress()}, 
							null);
			
			if ( cursor.moveToFirst() ) {
				int type = cursor.getInt(1);
				cursor.close();
				cursor = null;
				mContentResolver.update(uri, values2, Imps.GroupMembers.USERNAME + "='" + member.getAddress().getAddress() + "'", null);
				if ( type == Imps.GroupMembers.TYPE_NORMAL ) {
					return;
				}
			}else {
				mContentResolver.insert(uri, values1);
				cursor.close();
				cursor = null;

				ChatGroup group = (ChatGroup) mChatSession.getParticipant();

				String nickName = group.getName();
				if ( nickName.isEmpty() )
					nickName = member.getName();
				else
					nickName += ","+member.getName();

				if (updateRoomName)
					updateGroupNickNameInDb(nickName);
			}
			insertMessageInDb(member.getName(), null, GlobalFunc.getCurrentUTCTime(),Imps.MessageType.PRESENCE_AVAILABLE, Packet.nextID());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null )
				cursor.close();
		}
	}

	void deleteGroupMemberInDb(Contact member) {
		String where = Imps.GroupMembers.USERNAME + "=?";
		String[] selectionArgs = { member.getAddress().getAddress() };
		long groupId = ContentUris.parseId(mChatURI);
		Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
		mContentResolver.delete(uri, where, selectionArgs);

		insertMessageInDb(member.getName(), null, GlobalFunc.getCurrentUTCTime(),
				Imps.MessageType.PRESENCE_UNAVAILABLE, Packet.nextID());
	}
	
	void updateGroupMemberStatusInDb(Contact member, boolean isLeft) {
		if ( isLeft ) {
			String where = Imps.GroupMembers.USERNAME + "=?";
			String[] selectionArgs = { member.getAddress().getAddress() };
			long groupId = ContentUris.parseId(mChatURI);
			Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
			//mContentResolver.delete(uri, where, selectionArgs);
			
			ContentValues values = new ContentValues(1);
			values.put(Imps.GroupMemberColumns.TYPE, Imps.GroupMemberColumns.TYPE_LEFT);
			mContentResolver.update(uri, values, where, selectionArgs);
	
			insertMessageInDb(member.getName(), null, GlobalFunc.getCurrentUTCTime(),
					Imps.MessageType.PRESENCE_UNAVAILABLE, Packet.nextID());
		}
	}

	void insertPresenceUpdatesMsg(String contact, Presence presence) {
		int status = presence.getStatus();

		Integer previousStatus = mContactStatusMap.get(contact);
		if (previousStatus != null && previousStatus == status) {
			// don't insert the presence message if it's the same status
			// with the previous presence update notification
			return;
		}

		mContactStatusMap.put(contact, status);
		int messageType;
		switch (status) {
		case Presence.AVAILABLE:
			messageType = Imps.MessageType.PRESENCE_AVAILABLE;
			break;

		case Presence.AWAY:
		case Presence.IDLE:
			messageType = Imps.MessageType.PRESENCE_AWAY;
			break;

		case Presence.DO_NOT_DISTURB:
			messageType = Imps.MessageType.PRESENCE_DND;
			break;

		default:
			messageType = Imps.MessageType.PRESENCE_UNAVAILABLE;
			break;
		}

		if (mIsGroupChat) {
			insertMessageInDb(contact, null, GlobalFunc.getCurrentUTCTime(), messageType, Packet.nextID());
		} else {
			insertMessageInDb(null, null, GlobalFunc.getCurrentUTCTime(), messageType, Packet.nextID());
		}
	}

	void removeMessageInDb(int type) {
		mContentResolver.delete(mMessageURI, Imps.Messages.TYPE + "=?",
				new String[] { Integer.toString(type) });
	}

	private Uri insertMessageInDb(String contact, String body, long time, int type, String packetId) {
		return insertMessageInDb(contact, body, time, type, 0/*No error*/, packetId, null);
	}

	private Uri insertMessageInDb(String contact, String body, long time, int type, int errCode, String id, String mimeType) {
		boolean isEncrypted = false;
		return Imps.insertMessageInDb(mContentResolver, mIsGroupChat, mContactId, isEncrypted, contact, body, time, type, errCode, id, mimeType);
	}

	private Uri insertMessageInDb(String contact, String body, long time, int type, int errCode, String id, String mimeType, int totalCount) {
		boolean isEncrypted = false;
		return Imps.insertMessageInDb(mContentResolver, mIsGroupChat, mContactId, isEncrypted, contact, body, time, type, errCode, id, mimeType, totalCount);
	}

	private Uri insertMessageInDb(String contact, String body, long time, int type, int errCode, String id, long timeDiff) {
		boolean isEncrypted = false;
		/*try {
			isEncrypted = mOtrChatSession.isChatEncrypted();
		} catch (RemoteException e) {
		}*/
		return Imps.insertMessageInDb(mContentResolver, mIsGroupChat, mContactId, isEncrypted, contact, body, time, type, errCode, id, null,timeDiff);
	}

	private int updateMessageInDb(String id, int type, long time) {

		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(id);
		ContentValues values = new ContentValues(2);
		values.put(Imps.Messages.TYPE, type);
		if ( time > 0 )
			values.put(Imps.Messages.DATE, time);
		return mContentResolver.update(builder.build(), values, null, null);
	}
	
	private int updateMessageModifiedInDb(String packetId, boolean modified, String message) {

		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues(2);
		values.put(Imps.Messages.ERROR_CODE, modified?Imps.MessageErrorCode.MODIFIED:Imps.MessageErrorCode.NORMAL);
		values.put(Imps.Messages.BODY, message);
		return mContentResolver.update(builder.build(), values, null, null);
	}
	
	private int updateMessageSeenInDb(String packetId, boolean isSeen) {

		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues(1);
		values.put(Imps.Messages.IS_DELIVERED, isSeen?1:0);
		return mContentResolver.update(builder.build(), values, null, null);
	}

	private int updateMessageInDb(String id, int type, long time, long time_delay) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(id);

		ContentValues values = new ContentValues(1);
		values.put(Imps.Messages.TYPE, type);
		values.put(Imps.Messages.DATE, time);
		values.put(Imps.Messages.TIME_DELAY, time_delay);
		return mContentResolver.update(builder.build(), values, null, null);
	}

	private class ListenerAdapter implements MessageListener, GroupMemberListener {

		public boolean  onIncomingMessage(ChatSession ses, final Message msg) {
			String body = msg.getBody();
			String username = msg.getFrom().getAddress();
			String bareUsername = msg.getFrom().getBareAddress();
			String nickname = getNickName(username);
			String packetId = msg.getID();
			long time = msg.getDateTime().getTime();
            long mServerTime =  msg.getServerTime();

			if ( msg.getType() == Imps.MessageType.FRIEND_INVITE )
			{

			}

			if ( body != null ) {
				String resultMessage = Imps.getMessageBody(mContentResolver, packetId);
				if ( resultMessage != null )
					return true;
			}

			insertOrUpdateChat(body);
			
			String mimeType = MediaController.getMessageType(body);
			if ( mimeType != null ) {
				String to;
				if ( isGroupChatSession() )
					to = username;
				else
					to = msg.getTo().getAddress();
				to = to.split("@")[0];

				int totalCount = Integer.parseInt(body.substring(body.lastIndexOf("_") + 1));
				body = body.substring(0, body.lastIndexOf("_"));

				String httpUrl;
				if (body.startsWith("file://")) {
					httpUrl = mPref.getString(GlobalConstrants.FILE_SERVER_URL, GlobalVariable.FILE_SERVER_URL) + (GlobalConstrants.DOWNLOAD_FILE) +"?to=" + to + "&filename=" + body.substring(7);
				} else {
					httpUrl = body;
				}

				try
				{
					GlobalFunc.makeChatDir();
					String chatPath = GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME + "/" + DatabaseUtils.mAccountID + "/" + getId() + "/";

					File file = new File(chatPath);
					if ( !file.exists() )
						file.mkdirs();

                    if ( mimeType.equals(GlobalConstrants.FILE_TYPE_IMAGE) ) {
                        insertMessageInDb(username, httpUrl, time, msg.getType(), Imps.FileErrorCode.DOWNLOADING, packetId, "image/1", totalCount);
                    } else if ( mimeType.equals(GlobalConstrants.FILE_TYPE_AUDIO) ) {
                        insertMessageInDb(username, httpUrl, time, msg.getType(), Imps.FileErrorCode.DOWNLOADING, packetId, "audio/2", totalCount);
                    } else if ( mimeType.equals(GlobalConstrants.FILE_TYPE_VIDEO) ) {
                        insertMessageInDb(username, httpUrl, time, msg.getType(), Imps.FileErrorCode.DOWNLOADING, packetId, "video/3", totalCount);
                    }
                    Imps.updateMessageServerTimeInDb(mContentResolver, packetId, mServerTime);

					if (GlobalFunc.hasCalling()) {
						GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
					} else {
						if (GlobalFunc.getDownloadingStatus() == GlobalConstrants.NO_DOWNLOADING) {
							service.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
						}
					}
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
			} else {
				if ( Imps.updateMessageBody(mContentResolver, packetId, body) < 1 ) {
                    insertMessageInDb(username, body, time, msg.getType(), 0, packetId, null);
                    Imps.updateMessageServerTimeInDb(mContentResolver, packetId, mServerTime/*GlobalFunc.getCurrentUTCTime()*/);
                }
			}

			if (ChatRoomActivity.instance() == null) {
				mStatusBarNotifier.notifyChat(mConnection.getProviderId(), mConnection.getAccountId(),
						getId(), bareUsername, nickname, body, false);
				Imps.Notifications.addNotificationCount(mContentResolver, Imps.Notifications.CAT_CHATTING, Imps.Notifications.FIELD_CHAT, (int) mContactId, 1);
				mChatSessionManager.showNotifyCount();
			} else {
				if (!bareUsername.equals(ChatRoomActivity.instance().contactName)) {
					mStatusBarNotifier.notifyChat(mConnection.getProviderId(), mConnection.getAccountId(),
							getId(), bareUsername, nickname, body, false);
					Imps.Notifications.addNotificationCount(mContentResolver, Imps.Notifications.CAT_CHATTING, Imps.Notifications.FIELD_CHAT, (int) mContactId, 1);
					mChatSessionManager.showNotifyCount(); //show notification count on app icon.
				} else {
					if (ChatRoomActivity.instance().isbackground) {
						mStatusBarNotifier.notifyChat(mConnection.getProviderId(), mConnection.getAccountId(),
								getId(), bareUsername, nickname, body, false);
						Imps.Notifications.addNotificationCount(mContentResolver, Imps.Notifications.CAT_CHATTING, Imps.Notifications.FIELD_CHAT, (int) mContactId, 1);
						mChatSessionManager.showNotifyCount(); //show notification count on app icon.
					}
				}
			}

			try {
				sendMessagesSeen();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (ChatRoomActivity.instance() != null) {
				NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
				ChatRoomActivity.instance().mChatView.setFocusHistoryView(500);
			}

			return true;
		}

		public void onSendMessageError(ChatSession ses, final Message msg, final ImErrorInfo error) {

			if ( msg.getID() != null && msg.getBody() != null ) {

			}
			else{
				insertMessageInDb(null, null, GlobalFunc.getCurrentUTCTime(), Imps.MessageType.OUTGOING,
						error.getCode(), null, null);
			}

			final int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onSendMessageError(ChatSessionAdapter.this, null, error);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mRemoteListeners.finishBroadcast();
		}

		public void onMemberInvited(ChatGroup group, final Contact contact) {
			try{
				mConnection.mContactListManager.insertContactContent(contact, -1);  //temporaryÃ¬â€”ï¿½ Ã«â€žÂ£Ã«Å â€�Ã«â€¹Â¤.
			}catch(Exception e){
				e.printStackTrace();
			}

			if ( contact.getPresence() == null ) {
				updateGroupMemberInDb(contact);
				return;
			}

			String nickName = DatabaseUtils.getUserNickName(mContentResolver);
			if (!nickName.equals(contact.getName()))
				insertGroupMemberInDb(contact, true);
			/*else
				insertGroupMemberInDb(contact, false);*/


			final int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onContactJoined(ChatSessionAdapter.this, contact);
				} catch (RemoteException e) {
					// The RemoteCallbackList will take care of removing the
					// dead listeners.
				}
			}
			mRemoteListeners.finishBroadcast();
		}

		public void onMemberJoined(ChatGroup group, final Contact contact) {
			try{
				mConnection.mContactListManager.insertContactContent(contact, -1);  //temporaryÃ¬â€”ï¿½ Ã«â€žÂ£Ã«Å â€�Ã«â€¹Â¤.
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if ( contact.getPresence() == null ) {
				updateGroupMemberInDb(contact);
				return;
			}

			String nickName = DatabaseUtils.getUserNickName(mContentResolver);
			if (!nickName.equals(contact.getName()))
				insertGroupMemberInDb(contact, true);
			/*else
				insertGroupMemberInDb(contact, false);*/

			final int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onContactJoined(ChatSessionAdapter.this, contact);
				} catch (RemoteException e) {
					// The RemoteCallbackList will take care of removing the
					// dead listeners.
				}
			}
			mRemoteListeners.finishBroadcast();
		}

		public void onMemberLeft(ChatGroup group, final Contact contact) {
			//deleteGroupMemberInDb(contact);
			updateGroupMemberStatusInDb(contact, true);
			final int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onContactLeft(ChatSessionAdapter.this, contact);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mRemoteListeners.finishBroadcast();
		}

		public void onError(ChatGroup group, final ImErrorInfo error) {
			final int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onInviteError(ChatSessionAdapter.this, error);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mRemoteListeners.finishBroadcast();
		}

		public void notifyChatSessionConverted() {
			final int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onConvertedToGroupChat(ChatSessionAdapter.this);
				} catch (RemoteException e) {
					// The RemoteCallbackList will take care of removing the
					// dead listeners.
				}
			}
			mRemoteListeners.finishBroadcast();
		}

		@Override
		public void onIncomingOper(ChatSession ses, String id, String operType, String operMessage, String operMsgId) {
			if ( operType != null ) {
				if ( operType.equals(Oper.TYPE_SEEN) ) {
                    if (Imps.getMessageType(mContentResolver, operMsgId) != Imps.MessageType.INCOMING) {
                        Imps.updateMessageTypeInDb(mContentResolver, operMsgId, Imps.MessageType.OUTGOING);
                        Imps.updateConfirmInDb(mContentResolver, operMsgId, true);
                        Imps.updateMessageServerTimeInDb(mContentResolver, operMsgId, GlobalFunc.getCurrentUTCTime());
                    }

					///////         Send opermessage to server    /////////
					Message msg = new Message("");
					msg.setFrom(mConnection.getLoginUser().getAddress());
					msg.setID(Packet.nextID());
					msg.setOperType(Oper.TYPE_SEEN);
					msg.setOperMsgId(operMsgId);
					msg.setOperMessage("100");
					mChatSession.sendMessageAsync(msg);
					////------------------------------------------////

					NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
				} else if( operType.equals(Oper.TYPE_ERROR)) {
					int errorCode = Integer.parseInt(operMessage);
					if (errorCode == Imps.MessageErrorCode.SERVER_RECEIVED) {
						Imps.updateOperMessageServerReceived(mContentResolver, operMsgId);
					} else {
						Imps.updateOperMessageError(mContentResolver, operMsgId, Integer.parseInt(operMessage));
						Imps.updateMessageTypeInDb(mContentResolver, operMsgId, Imps.MessageType.OUTGOING);
					}
				}
			}

			int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onIncomingOper(ChatSessionAdapter.this, id, operType, operMessage, operMsgId);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mRemoteListeners.finishBroadcast();
		}

		@Override
		public void onMessagePostponed(ChatSession ses, String id) {

			long diffTime = mPref.getLong(GlobalConstrants.TIME_DIFF_WITH_SERVER, 0);

			/*int res = */
			updateMessageInDb(id, Imps.MessageType.POSTPONED, GlobalFunc.getCurrentUTCTime(), diffTime);

			int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onMessagePostPoned(ChatSessionAdapter.this, id);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mRemoteListeners.finishBroadcast();
		}

		/*  Sent Confirmed Message */
		@Override
		public void onReceiptsExpected(ChatSession ses) {

		}

		@Override
		public void onMessageOperPostponed(ChatSession ses, String id, String operType, String operMsgId, String operMessage) {
			if ( operType != null && operType.equals(Oper.TYPE_SEEN) ) { 
				insertMessageInDb(null, operMsgId,0, Imps.MessageType.OPER_SEEN, id);
			}
			else if ( operType != null && operType.equals(Oper.TYPE_DELETE) ) {
				insertMessageInDb(null, operMsgId,0, Imps.MessageType.OPER_DELETE, id);
			}
			else if ( operType != null && operType.equals(Oper.TYPE_MODIFY) ) {
				insertMessageInDb(null, operMsgId,0, Imps.MessageType.OPER_MODIFY, id);
			}
		}

		@Override
		public void onSentMessage(ChatSession ses, String packetId) {
			int N = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < N; i++) {
				IChatListener listener = mRemoteListeners.getBroadcastItem(i);
				try {
					listener.onSentMessage(ChatSessionAdapter.this, packetId);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mRemoteListeners.finishBroadcast();
		}
	}

	class ChatConvertor implements GroupListener, GroupMemberListener {
		private ChatGroupManager mGroupMgr;
		private String mGroupName;
		private String mGroupAddress;

		public ChatConvertor() {
			mGroupMgr = mConnection.mGroupManager;
		}

		public void convertToGroupChat() {
			mGroupMgr.addGroupListener(this);
			mGroupName = "";
			mGroupAddress = "G" + System.currentTimeMillis();
			try
			{
				mGroupMgr.createChatGroupAsync(mGroupName, mGroupAddress);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}

		public void onGroupCreated(ChatGroup group) {
			/*if (mGroupName.equalsIgnoreCase(group.getName())) {
                mGroupMgr.removeGroupListener(this);
                group.addMemberListener(this);
                mGroupMgr.inviteUserAsync(group.getName(), group, (Contact) mChatSession.getParticipant());
            }*/
		}

		public void onMemberInvited(ChatGroup group, Contact contact) {
			if (mChatSession.getParticipant().equals(contact)) {
				onConvertToGroupChatSuccess(group);
			}

			mContactStatusMap.put(contact.getName(), contact.getPresence().getStatus());
		}

		public void onMemberJoined(ChatGroup group, Contact contact) {
			if (mChatSession.getParticipant().equals(contact)) {
				onConvertToGroupChatSuccess(group);
			}

			mContactStatusMap.put(contact.getName(), contact.getPresence().getStatus());
		}

		public void onGroupDeleted(ChatGroup group) {
		}

		public void onGroupError(int errorType, String groupName, ImErrorInfo error) {
		}

		public void onJoinedGroup(ChatGroup group) {
		}

		public void onLeftGroup(ChatGroup group) {
		}

		public void onError(ChatGroup group, ImErrorInfo error) {
		}

		public void onMemberLeft(ChatGroup group, Contact contact) {
			mContactStatusMap.remove(contact.getName());
		}
	}

	@Override
	public void sendMessageSeen(String packetId, String myPacketId) throws RemoteException {

		Cursor cursor = mContentResolver.query(mMessageURI, new String[]{Imps.Messages._ID, Imps.Messages.ERROR_CODE}, Imps.Messages.PACKET_ID + "=? AND " + Imps.Messages.TYPE + "=?",
				new String[]{packetId, String.valueOf(Imps.MessageType.INCOMING)}, null);
		if (cursor != null) {
			cursor.moveToFirst();
			if (cursor.getInt(cursor.getColumnIndex(Imps.Messages.ERROR_CODE)) == Imps.FileErrorCode.DOWNLOADING)
				return;
			cursor.close();
		}

		if ( myPacketId == null )
			myPacketId = Packet.nextID();
		
		if ( mConnection.getState() != ImConnection.LOGGED_IN ) {
			insertMessageInDb(null, packetId, 0, Imps.MessageType.OPER_SEEN, myPacketId);
			return;
		}
		
		Message msg = new Message("");
		msg.setFrom(mConnection.getLoginUser().getAddress());
		msg.setID(myPacketId);
		msg.setOperType(Oper.TYPE_SEEN);
		msg.setOperMsgId(packetId);
		msg.setOperMessage("");
		String id = mChatSession.sendMessageAsync(msg);
		if (id != null) {
			updateMessageSeenInDb(packetId, true);
		}
	}

	@Override
	public void sendMessagesSeen() throws RemoteException {
		String[] projection = new String[] { BaseColumns._ID, 
				Imps.Messages.PACKET_ID,
				Imps.Messages.TYPE, Imps.Messages.IS_DELIVERED};
		
		String selection = Imps.Messages.TYPE + "='" + Imps.MessageType.INCOMING + "' And " + Imps.Messages.IS_DELIVERED + "='0' AND "
				+ Imps.Messages.ERROR_CODE + "!=" + Imps.FileErrorCode.DOWNLOADING
				+ " AND " + Imps.Messages.ERROR_CODE + "!=" + Imps.FileErrorCode.DOWNLOADFAILED
				+ " AND " + Imps.Messages.ERROR_CODE + "!=" + Imps.FileErrorCode.DOWNLOADCANCELLED;
		Cursor c =  mContentResolver.query(mMessageURI, projection, selection,
				null, null);
		if (c == null) {
			MessengerService.debug("Query error while querying postponed messages");
			return;
		}

		while (c.moveToNext()) {
			try{
				String packetId = c.getString(1);
				sendMessageSeen(packetId, null);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		c.close();
	}

	@Override
	public void deleteMessage(String packetId, String myPacketId) throws RemoteException {
		updateMessageInDb(packetId, Imps.MessageType.DELETED_OUTGOING, 0);
		
		if ( myPacketId == null )
			myPacketId = Packet.nextID();
		
		if (mConnection.getState() != ImConnection.LOGGED_IN) {  
			insertMessageInDb(null, packetId, 0, Imps.MessageType.OPER_DELETE, myPacketId);
			return;
		}
		
		
		Message msg = new Message("");
		msg.setFrom(mConnection.getLoginUser().getAddress());
		msg.setType(Imps.MessageType.OPER_DELETE);
		msg.setOperType(Oper.TYPE_DELETE);
		msg.setOperMsgId(packetId);
		msg.setOperMessage("");
		msg.setID(myPacketId);
		mChatSession.sendMessageAsync(msg);
	}

	@Override
	public void editMessage(String packetId, String myPacketId, String message)
			throws RemoteException {
		updateMessageModifiedInDb(packetId, true, message);
		if ( myPacketId == null )
			myPacketId = Packet.nextID();
		
		if (mConnection.getState() != ImConnection.LOGGED_IN) {  
			insertMessageInDb(null, packetId, 0, Imps.MessageType.OPER_MODIFY, myPacketId);
			return;
		}
		
		Message msg = new Message("");
		msg.setFrom(mConnection.getLoginUser().getAddress());
		msg.setType(Imps.MessageType.OPER_MODIFY);
		msg.setOperType(Oper.TYPE_MODIFY);
		msg.setOperMsgId(packetId);
		msg.setOperMessage(message);
		msg.setID(myPacketId);
		mChatSession.sendMessageAsync(msg);
	}
}
