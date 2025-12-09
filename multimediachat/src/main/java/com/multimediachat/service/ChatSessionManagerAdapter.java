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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionListener;
import com.multimediachat.app.im.engine.Address;
import com.multimediachat.app.im.engine.ChatGroup;
import com.multimediachat.app.im.engine.ChatGroupManager;
import com.multimediachat.app.im.engine.ChatSession;
import com.multimediachat.app.im.engine.ChatSessionListener;
import com.multimediachat.app.im.engine.ChatSessionManager;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.GroupListener;
import com.multimediachat.app.im.engine.ImConnection;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.plugin.xmpp.XmppAddress;
import com.multimediachat.ui.MainTabNavigationActivity;

import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

/** manages the chat sessions for a given protocol */
public class ChatSessionManagerAdapter extends
        com.multimediachat.app.im.IChatSessionManager.Stub {

    ImConnectionAdapter mConnection;
    ChatSessionManager mChatSessionManager;
    ChatGroupManager mGroupManager;
    HashMap<String, ChatSessionAdapter> mActiveChatSessionAdapters;
    ChatSessionListenerAdapter mSessionListenerAdapter;
    final RemoteCallbackList<IChatSessionListener> mRemoteListeners = new RemoteCallbackList<IChatSessionListener>();

    public ChatSessionManagerAdapter(ImConnectionAdapter connection) {
        mConnection = connection;
        ImConnection connAdaptee = connection.getAdaptee();
        mChatSessionManager = connAdaptee.getChatSessionManager();
        mActiveChatSessionAdapters = new HashMap<String, ChatSessionAdapter>();
        mSessionListenerAdapter = new ChatSessionListenerAdapter();
        mChatSessionManager.addChatSessionListener(mSessionListenerAdapter);
     
        if ((connAdaptee.getCapability() & ImConnection.CAPABILITY_GROUP_CHAT) != 0) {
            mGroupManager = connAdaptee.getChatGroupManager();
            mGroupManager.addGroupListener(new ChatGroupListenerAdapter());
        }
        
        showNotifyCount();
    }

    public ChatSessionManager getChatSessionManager() {
        return mChatSessionManager;
    }
    
    public ChatGroupManager getChatGroupManager(){
    	return mGroupManager;
    }

    public IChatSession createChatSession(String contactAddress) {
        ContactListManagerAdapter listManager = (ContactListManagerAdapter) mConnection
                .getContactListManager();
        
        Contact contact = listManager.getContactByAddress(contactAddress);
        if (contact == null) {
            try {
                String[] address = {contactAddress};
                contact = listManager.createTemporaryContacts(address)[0];
                
                
            } catch (IllegalArgumentException e) {
                mSessionListenerAdapter.notifyChatSessionCreateFailed(contactAddress,
                        new ImErrorInfo(ImErrorInfo.ILLEGAL_CONTACT_ADDRESS,
                                "Invalid contact address:" + contactAddress));
                return null;
            } catch (Exception e) {
            	return null;
            }
        }
        
        ChatSession session = mChatSessionManager.createChatSession(contact);
        
        return getChatSessionAdapter(session);
    }
    
    public IChatSession createMultiUserChatSession(String roomName, String roomAddress) 
    {
        
        ChatGroupManager groupMan = mConnection.getAdaptee().getChatGroupManager();
        try
        {
            groupMan.createChatGroupAsync(roomName, roomAddress);
            
            ChatGroup chatGroup = groupMan.getChatGroup(roomAddress);
            
            if (chatGroup != null)
            {
                ChatSession session = mChatSessionManager.createChatSession(chatGroup);
                return getChatSessionAdapter(session);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            return null;
        }
    }
    
    public IChatSession joinMultiUserChatSession(String roomName, String roomAddress)
    {
    	ChatGroupManager groupMan = mConnection.getAdaptee().getChatGroupManager();
    	
    	try
    	{
    		Address address = new XmppAddress(roomAddress);
    		groupMan.joinChatGroupAsync(roomName, address, null, null);
    		
    		
    		ChatGroup chatGroup = groupMan.getChatGroup(roomAddress);
            
            if (chatGroup != null)
            {
                ChatSession session = mChatSessionManager.createChatSession(chatGroup);
                return getChatSessionAdapter(session);
            }
            else
            {
                return null;
            }
    	}catch ( Exception e)
    	{
    		return null;
    	}
    }

    public void closeChatSession(ChatSessionAdapter adapter) {
        synchronized (mActiveChatSessionAdapters) {
            ChatSession session = adapter.getAdaptee();
            mChatSessionManager.closeChatSession(session);
            mActiveChatSessionAdapters.remove(adapter.getAddress());
        }
    }

    public void showNotifyCount(){
		Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
		mConnection.mService.sendBroadcast(intent);
    }
    
    public void closeAllChatSessions() {
        synchronized (mActiveChatSessionAdapters) {
            ArrayList<ChatSessionAdapter> adapters = new ArrayList<ChatSessionAdapter>(
                    mActiveChatSessionAdapters.values());
            for (ChatSessionAdapter adapter : adapters) {
                adapter.leave();
            }
        }
    }

    public void updateChatSession(String oldAddress, ChatSessionAdapter adapter) {
        synchronized (mActiveChatSessionAdapters) {
            mActiveChatSessionAdapters.remove(oldAddress);
            mActiveChatSessionAdapters.put(adapter.getAddress(), adapter);
        }
    }

    public IChatSession getChatSession(String address) {
        synchronized (mActiveChatSessionAdapters) {
            return mActiveChatSessionAdapters.get(address);
        }
    }

    public List<ChatSessionAdapter> getActiveChatSessions() {
        synchronized (mActiveChatSessionAdapters) {
            return new ArrayList<ChatSessionAdapter>(mActiveChatSessionAdapters.values());
        }
    }

    public int getChatSessionCount() {
        synchronized (mActiveChatSessionAdapters) {
            return mActiveChatSessionAdapters.size();
        }
    }

    public void registerChatSessionListener(IChatSessionListener listener) {
        if (listener != null) {
            mRemoteListeners.register(listener);
        }
    }

    public void unregisterChatSessionListener(IChatSessionListener listener) {
        if (listener != null) {
            mRemoteListeners.unregister(listener);
        }
    }

    ChatSessionAdapter getChatSessionAdapter(ChatSession session) {
        synchronized (mActiveChatSessionAdapters) {
            Address participantAddress = session.getParticipant().getAddress();
            String key = participantAddress.getBareAddress();
            ChatSessionAdapter adapter = mActiveChatSessionAdapters.get(key);
            if (adapter == null) {
                adapter = new ChatSessionAdapter(session, mConnection);
                mActiveChatSessionAdapters.put(key, adapter);
                
                //if ChatSessionAdapter is created after connection is logged in, we may need to sendPostponedMessage
                if ( mConnection != null && mConnection.getAdaptee().getState() == ImConnection.LOGGED_IN ) {
                	adapter.sendPostponedMessages();
                }
            }
            return adapter;
        }
    }

    class ChatSessionListenerAdapter implements ChatSessionListener {

        public void onChatSessionCreated(ChatSession session) {
            final IChatSession sessionAdapter = getChatSessionAdapter(session);
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IChatSessionListener listener = mRemoteListeners.getBroadcastItem(i);
                try {
                    listener.onChatSessionCreated(sessionAdapter);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
        }

        public void notifyChatSessionCreateFailed(final String name, final ImErrorInfo error) {
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IChatSessionListener listener = mRemoteListeners.getBroadcastItem(i);
                try {
                    listener.onChatSessionCreateError(name, error);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
        }
    }

    class ChatGroupListenerAdapter implements GroupListener {
        public void onGroupCreated(ChatGroup group) {
        }

        public void onGroupDeleted(ChatGroup group) {
            closeSession(group);
        }

        public void onGroupError(int errorType, String name, ImErrorInfo error) {
            if (errorType == ERROR_CREATING_GROUP) {
                mSessionListenerAdapter.notifyChatSessionCreateFailed(name, error);
            }
        }

        public void onJoinedGroup(ChatGroup group) {
            mChatSessionManager.createChatSession(group);
        }

        public void onLeftGroup(ChatGroup group) {
            closeSession(group);
        }

        private void closeSession(ChatGroup group) {
            String address = group.getAddress().getAddress();
            IChatSession session = getChatSession(address);
            if (session != null) {
                closeChatSession((ChatSessionAdapter) session);
            }
        }
    }
}
