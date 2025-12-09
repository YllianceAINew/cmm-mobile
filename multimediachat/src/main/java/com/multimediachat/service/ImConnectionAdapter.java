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

import java.util.HashMap;
import java.util.Map;

import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IConnectionListener;
import com.multimediachat.app.im.IContactListManager;
import com.multimediachat.app.im.IFindFriendManager;
import com.multimediachat.app.im.IInvitationListener;
import com.multimediachat.app.im.engine.ChatGroupManager;
import com.multimediachat.app.im.engine.ConnectionListener;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ImConnection;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.engine.ImException;
import com.multimediachat.app.im.engine.Invitation;
import com.multimediachat.app.im.engine.InvitationListener;
import com.multimediachat.app.im.engine.Presence;
import com.multimediachat.app.im.plugin.xmpp.XmppConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.ConnectionState;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class ImConnectionAdapter extends com.multimediachat.app.im.IImConnection.Stub {

	private static final String[] SESSION_COOKIE_PROJECTION = { Imps.SessionCookies.NAME,
                                                               Imps.SessionCookies.VALUE, };

    private static final int COLUMN_SESSION_COOKIE_NAME = 0;
    private static final int COLUMN_SESSION_COOKIE_VALUE = 1;

    ImConnection mConnection;
    private ConnectionListenerAdapter mConnectionListener;
    private InvitationListenerAdapter mInvitationListener;

    final RemoteCallbackList<IConnectionListener> mRemoteConnListeners = new RemoteCallbackList<IConnectionListener>();

    ChatSessionManagerAdapter mChatSessionManager;
    ContactListManagerAdapter mContactListManager;
    FindFriendManagerAdapter mFindFriendManager; //cys
    
    ChatGroupManager mGroupManager;
    MessengerService mService;

    long mProviderId = -1;
    long mAccountId = -1;
    boolean mAutoLoadContacts;
    int mConnectionState = ImConnection.DISCONNECTED;

    public ImConnectionAdapter(long providerId, long accountId, ImConnection connection, MessengerService service) {
        mProviderId = providerId;
        mAccountId = accountId;
        mConnection = connection;
        mService = service;
        mConnectionListener = new ConnectionListenerAdapter();
        mConnection.addConnectionListener(mConnectionListener);
        if ((connection.getCapability() & ImConnection.CAPABILITY_GROUP_CHAT) != 0) {
            mGroupManager = mConnection.getChatGroupManager();
            mInvitationListener = new InvitationListenerAdapter();
            mGroupManager.setInvitationListener(mInvitationListener);
        }
        
        mChatSessionManager = new ChatSessionManagerAdapter(this);
        mContactListManager = new ContactListManagerAdapter(this);
        mFindFriendManager = new FindFriendManagerAdapter(this);
    }

    public ImConnection getAdaptee() {
        return mConnection;
    }

    public MessengerService getContext() {
        return mService;
    }

    public long getProviderId() {
        return mProviderId;
    }

    public long getAccountId() {
        return mAccountId;
    }

    public int[] getSupportedPresenceStatus() {
        return mConnection.getSupportedPresenceStatus();
    }

    public void networkTypeChanged() {
    	if ( mConnection != null )
    		mConnection.networkTypeChanged();
    }

    void reestablishSession() {
        mConnectionState = ImConnection.LOGGING_IN;

        ContentResolver cr = mService.getContentResolver();
        if ((mConnection.getCapability() & ImConnection.CAPABILITY_SESSION_REESTABLISHMENT) != 0) {
            Map<String, String> cookie = querySessionCookie(cr);
            if (cookie != null) {
                MessengerService.debug("re-establish session");
                try {
                    mConnection.reestablishSessionAsync(cookie);
                } catch (IllegalArgumentException e) {
                    MessengerService.debug("Invalid session cookie, probably modified by others.");
                    clearSessionCookie(cr);
                }
            }
        }
    }

    private Uri getSessionCookiesUri() {
        Uri.Builder builder = Imps.SessionCookies.CONTENT_URI_SESSION_COOKIES_BY.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);

        return builder.build();
    }

    public void login(final String passwordTemp, final boolean autoLoadContacts, final boolean retry) {
        //Debug.wrapExceptions(new Runnable() {
        //    @Override
        //    public void run() {
                do_login(passwordTemp, autoLoadContacts, retry);
         //   }
        //});
    }
    
    public void do_login(String passwordTemp, boolean autoLoadContacts, boolean retry) {
        
        mAutoLoadContacts = autoLoadContacts;
        //mAutoLoadContacts = true;//ccj
        mConnectionState = ImConnection.LOGGING_IN;
        mConnection.loginAsync(mAccountId, passwordTemp, mProviderId, retry);
        //mConnection.loginAsync(mAccountId, passwordTemp, userName);//ccj
    }
    
    private void loadSavedPresence ()
    {
        ContentResolver cr =  mService.getContentResolver();
        // Imps.ProviderSettings.setPresence(cr, mProviderId, status, statusText);
         int presenceState = Imps.ProviderSettings.getIntValue(cr, mProviderId, Imps.ProviderSettings.PRESENCE_STATE);
         String presenceStatusMessage = Imps.ProviderSettings.getStringValue(cr, mProviderId, Imps.ProviderSettings.PRESENCE_STATUS_MESSAGE);

         if (presenceState != -1)
         {
             Presence presence = new Presence();
             presence.setStatus(presenceState);
             presence.setStatusText(presenceStatusMessage);
             try {
                 mConnection.updateUserPresenceAsync(presence);
             } catch (ImException e) {
            	 e.printStackTrace();
             }
         }
    }

    @Override
    public void sendHeartbeat() throws RemoteException {
        mConnection.sendHeartbeat(mService.getHeartbeatInterval());
    }

    @Override
    public void setProxy(String type, String host, int port) throws RemoteException {
        mConnection.setProxy(type, host, port);
    }

    private HashMap<String, String> querySessionCookie(ContentResolver cr) {
        Cursor c = cr.query(getSessionCookiesUri(), SESSION_COOKIE_PROJECTION, null, null, null);
        if (c == null) {
            return null;
        }

        HashMap<String, String> cookie = null;
        if (c.getCount() > 0) {
            cookie = new HashMap<String, String>();
            while (c.moveToNext()) {
                cookie.put(c.getString(COLUMN_SESSION_COOKIE_NAME),
                        c.getString(COLUMN_SESSION_COOKIE_VALUE));
            }
        }

        c.close();
        return cookie;
    }

    public void logout() {
        mConnectionState = ImConnection.LOGGING_OUT;
        mConnection.logout();
    }

    public synchronized void cancelLogin() {
        if (mConnectionState >= ImConnection.LOGGED_IN) {
            // too late
            return;
        }
        mConnectionState = ImConnection.LOGGING_OUT;
        mConnection.logout();
    }

    void suspend() {
        mConnectionState = ImConnection.SUSPENDING;
        mConnection.suspend();
    }

    public void registerConnectionListener(IConnectionListener listener) {
        if (listener != null) {
            mRemoteConnListeners.register(listener);
        }
    }

    public void unregisterConnectionListener(IConnectionListener listener) {
        if (listener != null) {
            mRemoteConnListeners.unregister(listener);
        }
    }
    
    public void setInvitationListener(IInvitationListener listener) {
        if (mInvitationListener != null) {
            mInvitationListener.mRemoteListener = listener;
        }
    }

    public IFindFriendManager getFindFriendManager(){
    	return mFindFriendManager;
    }
    
    public IChatSessionManager getChatSessionManager() {
        return mChatSessionManager;
    }

    public IContactListManager getContactListManager() {
        return mContactListManager;
    }

    public int getChatSessionCount() {
        if (mChatSessionManager == null) {
            return 0;
        }
        return mChatSessionManager.getChatSessionCount();
    }

    public Contact getLoginUser() {
        return mConnection.getLoginUser();
    }

    public Presence getUserPresence() {
        return mConnection.getUserPresence();
    }

    public int updateUserPresence(Presence newPresence) {
        try {
            mConnection.updateUserPresenceAsync(newPresence);
        } catch (ImException e) {
            return e.getImError().getCode();
        }

        return ImErrorInfo.NO_ERROR;
    }

    public void notifyContactsPresenceUpdated(Contact contact)
    {
        mConnection.notifyContactsPresenceUpdated(contact);
    }

    public int getState() {
        return mConnectionState;
    }

    public void rejectInvitation(long id) {
        handleInvitation(id, false);
    }

    public void acceptInvitation(long id) {
        handleInvitation(id, true);
    }

    private void handleInvitation(long id, boolean accept) {
        if (mGroupManager == null) {
            return;
        }
        ContentResolver cr = mService.getContentResolver();
        Cursor c = cr.query(ContentUris.withAppendedId(Imps.Invitation.CONTENT_URI, id), null,
                null, null, null);
        if (c == null) {
            return;
        }
        if (c.moveToFirst()) {
            String inviteId = c.getString(c.getColumnIndexOrThrow(Imps.Invitation.INVITE_ID));
            //int status;
            if (accept) {
                mGroupManager.acceptInvitationAsync(inviteId);
               // status = Imps.Invitation.STATUS_ACCEPTED;
            } else {
                mGroupManager.rejectInvitationAsync(inviteId);
               // status = Imps.Invitation.STATUS_REJECTED;
            }
            // TODO c.updateInt(c.getColumnIndexOrThrow(Imps.Invitation.STATUS), status);
            // c.commitUpdates();
        }
        c.close();
    }

    void saveSessionCookie(ContentResolver cr) {
        Map<String, String> cookies = mConnection.getSessionContext();

        int i = 0;
        ContentValues[] valuesList = new ContentValues[cookies.size()];

        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            ContentValues values = new ContentValues(2);

            values.put(Imps.SessionCookies.NAME, entry.getKey());
            values.put(Imps.SessionCookies.VALUE, entry.getValue());

            valuesList[i++] = values;
        }

        cr.bulkInsert(getSessionCookiesUri(), valuesList);
    }

    void clearSessionCookie(ContentResolver cr) {
        cr.delete(getSessionCookiesUri(), null, null);
    }

    void updateAccountStatusInDb() {
        Presence p = getUserPresence();
        int presenceStatus = Imps.Presence.OFFLINE;
        int connectionStatus = convertConnStateForDb(mConnectionState);

        if (p != null) {
        	presenceStatus = ContactListManagerAdapter.convertPresenceStatus(p);
        }
        try {
        	ContentResolver cr = mService.getContentResolver();
        	Uri uri = Imps.AccountStatus.CONTENT_URI;
        	ContentValues values = new ContentValues();
        	values.put(Imps.AccountStatus.ACCOUNT, mAccountId);
        	values.put(Imps.AccountStatus.PRESENCE_STATUS, presenceStatus);
        	values.put(Imps.AccountStatus.CONNECTION_STATUS, connectionStatus);
        	cr.insert(uri, values);
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }

    private static int convertConnStateForDb(int state) {
        switch (state) {
        case ImConnection.DISCONNECTED:
        case ImConnection.LOGGING_OUT:
            return Imps.ConnectionStatus.OFFLINE;

        case ImConnection.LOGGING_IN:
            return Imps.ConnectionStatus.CONNECTING;

        case ImConnection.LOGGED_IN:
            return Imps.ConnectionStatus.ONLINE;

        case ImConnection.SUSPENDED:
        case ImConnection.SUSPENDING:
            return Imps.ConnectionStatus.SUSPENDED;

        default:
            return Imps.ConnectionStatus.OFFLINE;
        }
    }

    final class ConnectionListenerAdapter implements ConnectionListener {
        public void onStateChanged(final int state, final ImErrorInfo error) {

            int connstate_forLED = ConnectionState.eCONNSTAT_XMPP_DISCONNECTED;

            synchronized (this) {
                if (state == ImConnection.LOGGED_IN && mConnectionState == ImConnection.LOGGING_OUT) {

                    // A bit tricky here. The engine did login successfully
                    // but the notification comes a bit late; user has already
                    // issued a cancelLogin() and that cannot be undone. Here
                    // we have to ignore the LOGGED_IN event and wait for
                    // the upcoming DISCONNECTED.
                    return;
                }

                if (state != ImConnection.DISCONNECTED) {
                    mConnectionState = state;
                }
            }

            ContentResolver cr = mService.getContentResolver();
            if (state == ImConnection.LOGGED_IN) {
                connstate_forLED = ConnectionState.eCONNSTAT_XMPP_CONNECTED;
                if ((mConnection.getCapability() & ImConnection.CAPABILITY_SESSION_REESTABLISHMENT) != 0) {
                    saveSessionCookie(cr);
                }
                mConnection.getChatGroupManager().autoJoinMuc();
                for (ChatSessionAdapter session : mChatSessionManager.mActiveChatSessionAdapters.values()) {
                    session.sendPostponedMessages();
                }
                loadSavedPresence();
            } else if (state == ImConnection.DISCONNECTED) {
                clearSessionCookie(cr);
                // mContactListManager might still be null if we fail
                // immediately in loginAsync (say, an invalid host URL)
                // if (mContactListManager != null) {
                //     mContactListManager.clearOnLogout();
                // }

                mConnectionState = state;
            } else if (state == ImConnection.SUSPENDED && error != null) {

                // re-establish failed, schedule to retry
                mService.scheduleReconnect(5000);

            }

            updateAccountStatusInDb();

            final int N = mRemoteConnListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                try {
                    listener.onStateChanged(ImConnectionAdapter.this, state, error);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteConnListeners.finishBroadcast();
            
            if (state == ImConnection.DISCONNECTED) {
                mService.removeConnection(ImConnectionAdapter.this);
            }

            if (GlobalFunc.getXmppConnStatus() != connstate_forLED) {
                GlobalFunc.SetXmppConnectStatPref(getContext(), connstate_forLED);
//                StatusBarNotifier.notifyServerState(getContext(), connstate_forLED, null);
            }
        }

        public void onUserPresenceUpdated() {
            updateAccountStatusInDb();

            final int N = mRemoteConnListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                try {
                    listener.onUserPresenceUpdated(ImConnectionAdapter.this);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteConnListeners.finishBroadcast();
        }

        public void onUpdatePresenceError(final ImErrorInfo error) {
            final int N = mRemoteConnListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                try {
                    listener.onUpdatePresenceError(ImConnectionAdapter.this, error);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteConnListeners.finishBroadcast();
        }
    }

    final class InvitationListenerAdapter implements InvitationListener {
        IInvitationListener mRemoteListener;

        public void onGroupInvitation(Invitation invitation) {
        	
            String sender = invitation.getSender().getUser();
            ContentValues values = new ContentValues(7);
            values.put(Imps.Invitation.PROVIDER, mProviderId);
            values.put(Imps.Invitation.ACCOUNT, mAccountId);
            values.put(Imps.Invitation.INVITE_ID, invitation.getInviteID());
            values.put(Imps.Invitation.SENDER, sender);
            values.put(Imps.Invitation.GROUP_NAME, invitation.getGroupAddress().getUser());
            values.put(Imps.Invitation.NOTE, invitation.getReason());
            values.put(Imps.Invitation.STATUS, Imps.Invitation.STATUS_PENDING);
            ContentResolver resolver = mService.getContentResolver();
            Uri uri = resolver.insert(Imps.Invitation.CONTENT_URI, values);
            long id = ContentUris.parseId(uri);
            try {
                if (mRemoteListener != null) {
                    //mRemoteListener.onGroupInvitation(id);
                	mRemoteListener.onGroupInvitation(id);
                	mService.getStatusBarNotifier().notifyGroupInvitation(mProviderId, mAccountId, invitation, id);
                    return;
                }
            } catch (RemoteException e) {
                MessengerService.debug("onGroupInvitation: dead listener " + mRemoteListener
                                      + "; removing", e);
                mRemoteListener = null;
            }
            // No listener registered or failed to notify the listener, send a
            // notification instead.
        }
    }

	@Override
	public void sendLocation(double latitude, double longitude)
			throws RemoteException {
		mConnection.sendLocation(latitude,longitude);
	}
	/*@Override
	public void loadVcards() {
		mConnection.loadVcards();
	}*/
    
	@Override
	public void loadVcard(String addr, String hash, boolean needName) {
		mConnection.loadVcard(addr, hash, needName);
	}
	
	@Override
	public boolean isPicaConnection(){
		return mConnection.isPicaConnection();
	}
}
