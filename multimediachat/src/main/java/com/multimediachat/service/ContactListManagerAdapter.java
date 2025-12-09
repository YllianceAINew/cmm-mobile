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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.jivesoftware.smack.util.StringUtils;

import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.IContactList;
import com.multimediachat.app.im.IContactListListener;
import com.multimediachat.app.im.ISubscriptionListener;
import com.multimediachat.app.im.engine.Address;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ContactList;
import com.multimediachat.app.im.engine.ContactListListener;
import com.multimediachat.app.im.engine.ContactListManager;
import com.multimediachat.app.im.engine.ImConnection;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.engine.ImException;
import com.multimediachat.app.im.engine.Presence;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.util.SystemServices;
import com.multimediachat.util.SystemServices.FileInfo;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class ContactListManagerAdapter extends
com.multimediachat.app.im.IContactListManager.Stub implements Runnable {

	ImConnectionAdapter mConn;
	ContentResolver mResolver;

	private ContactListManager mAdaptee;
	private ContactListListenerAdapter mContactListListenerAdapter;
	private SubscriptionRequestListenerAdapter mSubscriptionListenerAdapter;

	final RemoteCallbackList<IContactListListener> mRemoteContactListeners = new RemoteCallbackList<IContactListListener>();
	final RemoteCallbackList<ISubscriptionListener> mRemoteSubscriptionListeners = new RemoteCallbackList<ISubscriptionListener>();

	HashMap<Address, ContactListAdapter> mContactLists;
	// Temporary contacts are created when a peer is encountered, and that peer
	// is not yet on any contact list.
	HashMap<String, Contact> mTemporaryContacts;
	// Offline contacts are created from the local DB before the server contact lists
	// are loaded.
	HashMap<String, Contact> mOfflineContacts;

	HashSet<String> mValidatedContactLists;
	HashSet<String> mValidatedContacts;
	HashSet<String> mValidatedBlockedContacts;

	private long mAccountId;
	private long mProviderId;

	private Uri mAvatarUrl;
	private Uri mContactUrl;



	static final long FAKE_TEMPORARY_LIST_ID = -1;
	static final String[] CONTACT_LIST_ID_PROJECTION = { Imps.ContactList._ID };

	MessengerService mContext;


	public ContactListManagerAdapter(ImConnectionAdapter conn) {
		mAdaptee = conn.getAdaptee().getContactListManager();
		mConn = conn;
		mContext = conn.getContext();
		mResolver = mContext.getContentResolver();

		new Thread(this).start();
	}

	public void run ()
	{
		mContactListListenerAdapter = new ContactListListenerAdapter();
		mSubscriptionListenerAdapter = new SubscriptionRequestListenerAdapter();
		mContactLists = new HashMap<Address, ContactListAdapter>();
		mTemporaryContacts = new HashMap<String, Contact>();
		mOfflineContacts = new HashMap<String, Contact>();
		mValidatedContacts = new HashSet<String>();
		mValidatedContactLists = new HashSet<String>();
		mValidatedBlockedContacts = new HashSet<String>();

		mAdaptee.addContactListListener(mContactListListenerAdapter);
		mAdaptee.setSubscriptionRequestListener(mSubscriptionListenerAdapter);

		mAccountId = mConn.getAccountId();
		mProviderId = mConn.getProviderId();

		Uri.Builder builder = Imps.Avatars.CONTENT_URI_AVATARS_BY.buildUpon();
		ContentUris.appendId(builder, mProviderId);
		ContentUris.appendId(builder, mAccountId);

		mAvatarUrl = builder.build();

		builder = Imps.Contacts.CONTENT_URI_CONTACTS_BY.buildUpon();
		ContentUris.appendId(builder, mProviderId);
		ContentUris.appendId(builder, mAccountId);

		mContactUrl = builder.build();

		seedInitialPresences();
		// loadOfflineContacts();
	}

	public Contact getContact(String address) 
	{
		return mAdaptee.getContact(address);
	}

	public int getSubRequestedFriendCount(){


		String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.SUBSCRIPTION_TYPE + "=?";
		String[] selectionArgs1 = {Long.toString(mAccountId), String.valueOf(Imps.Contacts.SUBSCRIPTION_TYPE_FROM)};

		Cursor cursor = mResolver.query(Imps.Contacts.CONTENT_URI, new String[] { Imps.Contacts._ID },
				select, selectionArgs1, null);

		if ( cursor == null )
			return 0;

		if ( cursor.getCount() == 0 ) {

			if ( cursor != null)
				cursor.close();
			return 0;
		}

		int count = cursor.getCount();

		if ( cursor != null )
			cursor.close();
		return count;
	}

	/*private void loadOfflineContacts() {
        Cursor contactCursor = mResolver.query(mContactUrl, new String[] { Imps.Contacts.USERNAME },
                null, null, null);

        if ( contactCursor == null )
        	return;

        if ( contactCursor.getCount() == 0 ) {
        	contactCursor.close();
        	return;
        }

        String[] addresses = new String[contactCursor.getCount()];
        int i = 0;
        while (contactCursor.moveToNext())
        {
            addresses[i++] = contactCursor.getString(0);

        }

        Contact[] contacts = mAdaptee.createTemporaryContacts(addresses);
        for (Contact contact : contacts)
                mOfflineContacts.put(contact.getAddress().getBareAddress(), contact);

        if ( contactCursor != null)
        	contactCursor.close();
    }*/

	public int createContactList(String name, List<Contact> contacts) {
		try {
			mAdaptee.createContactListAsync(name, contacts);
		} catch (ImException e) {
			return e.getImError().getCode();
		}

		return ImErrorInfo.NO_ERROR;
	}

	public int deleteContactList(String name) {
		try {
			mAdaptee.deleteContactListAsync(name);
		} catch (ImException e) {
			return e.getImError().getCode();
		}

		return ImErrorInfo.NO_ERROR;
	}

	public List<ContactListAdapter> getContactLists() {
		synchronized (mContactLists) {
			return new ArrayList<ContactListAdapter>(mContactLists.values());
		}
	}

	public int removeContact(String address) {
		Contact contact = getContact(address);
		if (isTemporary(address)) {
			synchronized (mTemporaryContacts) {
				mTemporaryContacts.remove(address);
			}
		} else {
			synchronized (mContactLists) {
				for (ContactListAdapter list : mContactLists.values()) {
					int resCode = list.removeContact(address);
					if (ImErrorInfo.ILLEGAL_CONTACT_ADDRESS == resCode) {
						// Did not find in this list, continue to cii_remove from
						// other list.
						continue;
					}
					if (ImErrorInfo.NO_ERROR != resCode) {
						return resCode;
					}
				}

			}

		}
		
		if ( contact != null )
			deleteContactFromDataBase(contact);
		
		closeChatSession(address);
		return ImErrorInfo.NO_ERROR;
	}

	public int setContactName(String address, String name) {
		// update the server
		int res = ImErrorInfo.NO_ERROR;
		try {
			res = mAdaptee.setContactName(address,name);
		} catch (ImException e) {
			return e.getImError().getCode();
		}
		// update locally

		if ( res == ImErrorInfo.NO_ERROR ) {
			String selection = Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = { address };
			ContentValues values = new ContentValues(1);
			values.put( Imps.Contacts.NICKNAME, name);
			int updated = mResolver.update(mContactUrl, values, selection, selectionArgs);
			if( updated != 1 ) {
				return ImErrorInfo.ILLEGAL_CONTACT_ADDRESS;
			}
		}

		return ImErrorInfo.NO_ERROR;
	}

	public void requestSubscription(Contact contact, String greeting) {
		mAdaptee.requestSubscription(contact, greeting);
	}

	public void approveSubscription(Contact address) {
		mAdaptee.approveSubscriptionRequest(address);
	}

	public void declineSubscription(Contact contact) {
		mAdaptee.declineSubscriptionRequest(contact);
	}

	public void cancelRequest(Contact contact) {
		mAdaptee.cancelRequest(contact);
	}
	
	public boolean addContact(Contact contact) {
		return mAdaptee.addContact(contact);
	}
	
	public boolean addContactWithDB(Contact contact) {
		if ( mAdaptee.addContact(contact) ) {
			if ( insertTemporary(contact) == 0 )
				DatabaseUtils.updateNickName(mResolver, contact.getAddress().getAddress(), contact.getName());
			return true;
		}
		return false;
	}

	public int blockContact(String address) {
		try {
			return mAdaptee.blockContactAsync(address);
		} catch (ImException e) {
			return e.getImError().getCode();
		}
	}

	public int unBlockContact(String address) {
		try {
			return mAdaptee.unblockContactAsync(address);
		} catch (ImException e) {
			MessengerService.debug(e.getMessage());
			return e.getImError().getCode();
		}
	}

	//cys
	public int hideContact(String address) {
		try{
			return mAdaptee.hideContactAsync(address);
		}catch(ImException e) {
			return e.getImError().getCode();
		}

		//return ImErrorInfo.NO_ERROR;
	}

	//cys
	public int unHideContact(String address) {
		try{
			return mAdaptee.unHideContactAsync(address);
		}
		catch(ImException e){
			return e.getImError().getCode();
		}
	}

	public boolean isBlocked(String address) {
		try {
			return mAdaptee.isBlocked(address);
		} catch (ImException e) {
			MessengerService.debug(e.getMessage());
			return false;
		}
	}
	
	public boolean isFavorite(String address) {
		try{
			return mAdaptee.isFavorite(address);
		}catch(ImException e){
			return false;
		}
	}

	public boolean isHidden(String address) {
		try {
			return mAdaptee.isHidden(address);
		}catch( ImException e) {
			MessengerService.debug(e.getMessage());
			return false;
		}
	}

	public void registerContactListListener(IContactListListener listener) {
		if (listener != null) {
			mRemoteContactListeners.register(listener);
		}
	}

	public void unregisterContactListListener(IContactListListener listener) {
		if (listener != null) {
			mRemoteContactListeners.unregister(listener);
		}
	}

	public void registerSubscriptionListener(ISubscriptionListener listener) {
		if (listener != null) {
			mRemoteSubscriptionListeners.register(listener);
		}
	}

	public void unregisterSubscriptionListener(ISubscriptionListener listener) {
		if (listener != null) {
			mRemoteSubscriptionListeners.unregister(listener);
		}
	}

	public IContactList getContactList(String name) {
		return getContactListAdapter(name);
	}

	/*
	public void loadContactLists() {
		if (mAdaptee.getState() == ContactListManager.LISTS_NOT_LOADED) {
			clearValidatedContactsAndLists();
			mAdaptee.loadContactListsAsync(null);
		}
	}
	*/

	public void setContactsToListFromDB(){
		if (mAdaptee.getState() == ContactListManager.LISTS_NOT_LOADED) {
			mAdaptee.setContactsToListFromDB();
		}
	}

	public int getState() {
		return mAdaptee.getState();
	}

	public Contact getContactByAddress(String address) {
		if (mAdaptee.getState() == ContactListManager.LISTS_NOT_LOADED) {
			return mOfflineContacts.get(address);
		}

		Contact c = mAdaptee.getContact(address);
		if (c == null) {
			synchronized (mTemporaryContacts) {
				return mTemporaryContacts.get(address);
			}
		} else {
			return c;
		}
	}

	public Contact[] createTemporaryContacts(String[] addresses) {
		Contact[] contacts = mAdaptee.createTemporaryContacts(addresses);

		for (Contact c : contacts)
			insertTemporary(c);
		return contacts;
	}

	public long queryOrInsertContact(Contact c) {
		long result;

		String username = mAdaptee.normalizeAddress(c.getAddress().getAddress());
		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {Long.toString(mAccountId), username};
		String[] projection = { Imps.Contacts._ID };

		Cursor cursor = mResolver.query(mContactUrl, projection, selection, selectionArgs, null);

		if (cursor != null && cursor.moveToFirst()) {
			result = cursor.getLong(0);
		} else {
			result = insertTemporary(c);
		}

		if (cursor != null) {
			cursor.close();
		}
		return result;
	}

	private long insertTemporary(Contact c) {
		synchronized (mTemporaryContacts) {
			mTemporaryContacts.put(mAdaptee.normalizeAddress(c.getAddress().getBareAddress()), c);
		}
		Uri uri = insertContactContent(c, FAKE_TEMPORARY_LIST_ID);
		if ( uri == null )
			return 0;
		return ContentUris.parseId(uri);
	}

	/**
	 * Tells if a contact is a temporary one which is not in the list of
	 * contacts that we subscribe presence for. Usually created because of the
	 * user is having a chat session with this contact.
	 * 
	 * @param address the address of the contact.
	 * @return <code>true</code> if it's a temporary contact; <code>false</code>
	 *         otherwise.
	 */
	public boolean isTemporary(String address) {
		synchronized (mTemporaryContacts) {
			return mTemporaryContacts.containsKey(address);
		}
	}

	ContactListAdapter getContactListAdapter(String name) {
		synchronized (mContactLists) {
			for (ContactListAdapter list : mContactLists.values()) {
				if (name.equals(list.getName())) {
					return list;
				}
			}

			return null;
		}
	}

	ContactListAdapter getContactListAdapter(Address address) {
		synchronized (mContactLists) {
			return mContactLists.get(address);
		}
	}

	/*private class Exclusion {
        private StringBuilder mSelection;
        private List<String> mSelectionArgs;
        private String mExclusionColumn;

        Exclusion(String exclusionColumn, Collection<String> items) {
            mSelection = new StringBuilder();
            mSelectionArgs = new ArrayList<String>();
            mExclusionColumn = exclusionColumn;
            for (String s : items) {
                add(s);
            }
        }
        public void add(String exclusionColumn, String exclusionItem)
        {
        	if (mSelection.length() == 0) {
                mSelection.append(exclusionColumn + "!=?");
            } else {
                mSelection.append(" AND " + exclusionColumn + "!=?");
            }
        	mSelectionArgs.add(exclusionItem);
        }

        public void add(String exclusionItem) {
            if (mSelection.length() == 0) {
                mSelection.append(mExclusionColumn + "!=?");
            } else {
                mSelection.append(" AND " + mExclusionColumn + "!=?");
            }
            mSelectionArgs.add(exclusionItem);
        }

        public String getSelection() {
            return mSelection.toString();
        }

        public String[] getSelectionArgs() {
            return (String[]) mSelectionArgs.toArray(new String[0]);
        }
    }*/

	private void removeObsoleteContactsAndLists() {
		/*Exclusion exclusion = new Exclusion(Imps.Contacts.USERNAME, mValidatedContacts);

        exclusion.add(Imps.Contacts.TYPE, String.valueOf(Imps.Contacts.TYPE_GROUP));//cys

        mResolver.delete(mContactUrl, exclusion.getSelection(), exclusion.getSelectionArgs());
        exclusion = new Exclusion(Imps.BlockedList.USERNAME, mValidatedBlockedContacts);
        Uri.Builder builder = Imps.BlockedList.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);
        Uri uri = builder.build();
        mResolver.delete(uri, exclusion.getSelection(), exclusion.getSelectionArgs());*/
	}

	interface ContactListBroadcaster {
		void broadcast(IContactListListener listener) throws RemoteException;
	}

	interface SubscriptionBroadcaster {
		void broadcast(ISubscriptionListener listener) throws RemoteException;
	}

	final class ContactListListenerAdapter implements ContactListListener {
		private boolean mAllContactsLoaded;

		// class to hold contact changes made before mAllContactsLoaded
		private class StoredContactChange {
			int mType;
			ContactList mList;
			Contact mContact;

			StoredContactChange(int type, ContactList list, Contact contact) {
				mType = type;
				mList = list;
				mContact = contact;
			}
		}

		private Vector<StoredContactChange> mDelayedContactChanges = new Vector<StoredContactChange>();

		private void broadcast(ContactListBroadcaster callback) {
			synchronized (mRemoteContactListeners) {
				final int N = mRemoteContactListeners.beginBroadcast();
				for (int i = 0; i < N; i++) {
					IContactListListener listener = mRemoteContactListeners.getBroadcastItem(i);
					try {
						callback.broadcast(listener);
					} catch (RemoteException e) {
					}
				}
				mRemoteContactListeners.finishBroadcast();
			}
		}

		public void onContactsPresenceUpdate(final Contact[] contacts) {
			updatePresenceContent(contacts);
			broadcast(new ContactListBroadcaster() {
				public void broadcast(IContactListListener listener) throws RemoteException {
					listener.onContactsPresenceUpdate(contacts);
				}
			});
		}

		public void onContactChange(final int type, final ContactList list, final Contact contact) {
			ContactListAdapter removed = null;

			switch (type) {
			case CONTACT_STATUS_UPDATED:
				break;
			case CONTACT_AVATAR_UPDATED:
				break;
			case LIST_LOADED:
				addContactListContent(list);
				break;
			case LIST_CREATED:
				break;

			case LIST_DELETED:
				removed = removeContactListFromDataBase(list.getName());
				if (!mAllContactsLoaded) {
					if (!mValidatedContactLists.contains(list.getName())) {
						mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
					}
				}
				break;

			case LIST_CONTACT_ADDED:
				long listId = getContactListAdapter(list.getAddress()).getDataBaseId();
				if (isTemporary(mAdaptee.normalizeAddress(contact.getAddress().getAddress()))) {
					moveTemporaryContactToList(mAdaptee.normalizeAddress(contact.getAddress().getAddress()), listId);
				} else {
					boolean exists = updateContact(contact, listId);
					if (!exists){
						insertContactContent(contact, listId);
						boolean needName = false;
						if ( contact.getName() == null || contact.getName().equals("") ) {
							needName = true;
						}
						mConn.loadVcard( contact.getAddress().getAddress() , null,  needName);   
						mContext.getStatusBarNotifier().notifySubscriptionApproved(
								mAccountId,
								mProviderId, 
								Math.abs(contact.hashCode()),
								contact.getAddress().getAddress(), 
								contact.getName(), 
								false);
						Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
						mContext.sendBroadcast(intent);
					}
				}
				// handle case where a contact is added before mAllContactsLoaded
				if (!mAllContactsLoaded) {
					if (!mValidatedContactLists.contains(list.getName())) {
						mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
					}
				}

				break;
			case LIST_CONTACT_REMOVED:
				deleteContactFromDataBase(contact);
				if (!mAllContactsLoaded) {
					if (!mValidatedContactLists.contains(list.getName())) {
						mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
					}
				}

				// Clear ChatSession if any.
				String address = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
				closeChatSession(address);

				break;

			case LIST_RENAMED:
				updateListNameInDataBase(list);
				// handle case where a list is renamed before mAllContactsLoaded
				if (!mAllContactsLoaded) {
					if (!mValidatedContactLists.contains(list.getName())) {
						mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
					}
				}
				break;

			case CONTACT_ADDED_TEMPORARY:
				break;

			case CONTACT_BLOCKED:
				address = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
				ContentValues values1 = getContactContentValues(contact, -1);
				values1.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_BLOCKED);

				String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
				String[] selectionArgs = {Long.toString(mAccountId), contact.getAddress().getAddress()};

				if ( mResolver.update(Imps.Contacts.CONTENT_URI, values1, select, selectionArgs) < 1 ) {
					values1.put(Imps.Contacts.SUBSCRIPTION_TYPE, Imps.Contacts.SUBSCRIPTION_TYPE_NONE);
					values1.put(Imps.Contacts.PROVIDER, mProviderId);
					values1.put(Imps.Contacts.ACCOUNT, mAccountId);
					mResolver.insert(Imps.Contacts.CONTENT_URI, values1);
					mConn.loadVcard(address, null, true);
				}
					
				break;

			case CONTACT_UNBLOCKED:
				address = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
				int type1 = Imps.Contacts.TYPE_NORMAL;
				if ( contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_NONE ) {
					type1 = Imps.Contacts.TYPE_TEMPORARY;
				}
				updateContactType(address, type1);
				
				if (!mAllContactsLoaded) {
					if (!mValidatedBlockedContacts.contains(contact.getName())) {
						mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
					}
				}
				break;
				
			case CONTACT_HIDDEN:
				address = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
				updateContactType(address, Imps.Contacts.TYPE_HIDDEN);
				break;

			case CONTACT_UNHIDDEN:
				address = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
				type1 = Imps.Contacts.TYPE_NORMAL;
				if ( contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_NONE ) {
					type1 = Imps.Contacts.TYPE_NORMAL;
				}
				updateContactType(address, type1);

				if (!mAllContactsLoaded) {
					if (!mValidatedBlockedContacts.contains(contact.getName())) {
						mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
					}
				}
				break;
				
				
			case CONTACT_FAVORITE:
				address = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
				doFavoriteContact(address, true);
				break;

			case CONTACT_UNFAVORITE:
				address = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
				doFavoriteContact(address, false);
				break;
				
			case CONTACT_UPDATE:
				ContentValues values = new ContentValues();
				values.put(Imps.Contacts.SUBSCRIPTION_TYPE, contact.sub_type);
				values.put(Imps.Contacts.TYPE, contact.contact_type);
				updateContact(contact.getAddress().getAddress(), values);
				break;

			default:
				MessengerService.debug("Unknown list update event!");
				break;
			}

			final ContactListAdapter listAdapter;
			if (type == LIST_DELETED) {
				listAdapter = removed;
			} else {
				listAdapter = (list == null) ? null : getContactListAdapter(list.getAddress());
			}

			broadcast(new ContactListBroadcaster() {
				public void broadcast(IContactListListener listener) throws RemoteException {
					listener.onContactChange(type, listAdapter, contact);
				}
			});


			if ( contact == null || contact.getAddress() == null || contact.getAddress().getAddress() == null )
				return;

			String addr = contact.getAddress().getAddress();
			String serverName = StringUtils.parseServer( addr );

			if ( serverName == null )
				return;
		}

		public void onContactError(final int errorType, final ImErrorInfo error,
				final String listName, final Contact contact) {
			broadcast(new ContactListBroadcaster() {
				public void broadcast(IContactListListener listener) throws RemoteException {
					listener.onContactError(errorType, error, listName, contact);
				}
			});
		}

		public void handleDelayedContactChanges() {
			for (StoredContactChange change : mDelayedContactChanges) {
				onContactChange(change.mType, change.mList, change.mContact);
			}
		}

		public void onAllContactListsLoaded() {
			mAllContactsLoaded = true;

			handleDelayedContactChanges();
			removeObsoleteContactsAndLists();

			broadcast(new ContactListBroadcaster() {
				public void broadcast(IContactListListener listener) throws RemoteException {
					listener.onAllContactListsLoaded();
				}
			});
		}
	}

	final class SubscriptionRequestListenerAdapter extends ISubscriptionListener.Stub {

		public void onSubScriptionRequest(final Contact from, long providerId, long accountId) {
			broadcast(new SubscriptionBroadcaster() {
				public void broadcast(ISubscriptionListener listener) throws RemoteException {
					listener.onSubScriptionRequest(from, mProviderId, mAccountId);
				}
			});

			mContext.getStatusBarNotifier().notifySubscriptionRequest(mProviderId, mAccountId,
					Math.abs(from.hashCode()), from.getAddress().getAddress(), from.getName(), false);

			mConn.mChatSessionManager.showNotifyCount();

		}

		public void onCanceledRequest(final Contact from, long providerId, long accountId) {
			broadcast(new SubscriptionBroadcaster() {
				public void broadcast(ISubscriptionListener listener) throws RemoteException {
					listener.onCanceledRequest(from, mProviderId, mAccountId);
				}
			});

		}

		public void onUnSubScriptionRequest(final Contact from, long providerId, long accountId) {
			broadcast(new SubscriptionBroadcaster() {
				public void broadcast(ISubscriptionListener listener) throws RemoteException {
					listener.onUnSubScriptionRequest(from, mProviderId, mAccountId);
				}
			});

			mConn.mChatSessionManager.showNotifyCount();
		}


		private boolean broadcast(SubscriptionBroadcaster callback) {
			boolean hadListener = false;

			synchronized (mRemoteSubscriptionListeners) {
				final int N = mRemoteSubscriptionListeners.beginBroadcast();
				for (int i = 0; i < N; i++) {
					ISubscriptionListener listener = mRemoteSubscriptionListeners.getBroadcastItem(i);
					try {
						callback.broadcast(listener);
						hadListener = true;
					} catch (RemoteException e) {
						// The RemoteCallbackList will take care of removing the
						// dead listeners.
					}
				}
				mRemoteSubscriptionListeners.finishBroadcast();
			}

			return hadListener;
		}

		public void onSubscriptionApproved(final Contact contact, long providerId, long accountId) {

			broadcast(new SubscriptionBroadcaster() {
				public void broadcast(ISubscriptionListener listener) throws RemoteException {
					listener.onSubscriptionApproved(contact, mProviderId, mAccountId);
				}
			});
			boolean needName = false;
			if ( contact.getName() == null || contact.getName().equals("") ) {
				needName = true;
			}

			mConn.loadVcard(contact.getAddress().getAddress(), null, needName);
			mConn.mChatSessionManager.showNotifyCount();
		}

		public void onSubscriptionDeclined(final Contact contact, long providerId, long accountId) {

			broadcast(new SubscriptionBroadcaster() {
				public void broadcast(ISubscriptionListener listener) throws RemoteException {
					listener.onSubscriptionDeclined(contact, mProviderId, mAccountId);
				}
			});

			mConn.mChatSessionManager.showNotifyCount();
		}

		public void onApproveSubScriptionError(final String contact, final ImErrorInfo error) {
		}

		public void onDeclineSubScriptionError(final String contact, final ImErrorInfo error) {
		}
	}

	void insertBlockedContactToDataBase(Contact contact) {
		// Remove the blocked contact if it already exists, to avoid duplicates and
		// handle the odd case where a blocked contact's nickname has changed
		removeBlockedContactFromDataBase(contact);

		Uri.Builder builder = Imps.BlockedList.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, mProviderId);
		ContentUris.appendId(builder, mAccountId);
		Uri uri = builder.build();

		String username = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
		ContentValues values = new ContentValues(2);
		values.put(Imps.BlockedList.USERNAME, username);
		values.put(Imps.BlockedList.NICKNAME, contact.getName());

		mResolver.insert(uri, values);

		mValidatedBlockedContacts.add(username);
	}

	void removeBlockedContactFromDataBase(Contact contact) {
		String address = mAdaptee.normalizeAddress(contact.getAddress().getAddress());

		Uri.Builder builder = Imps.BlockedList.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, mProviderId);
		ContentUris.appendId(builder, mAccountId);

		Uri uri = builder.build();
		mResolver.delete(uri, Imps.BlockedList.USERNAME + "=?", new String[] { address });

		int type = isTemporary(address) ? Imps.Contacts.TYPE_TEMPORARY : Imps.Contacts.TYPE_NORMAL;
		updateContactType(address, type);
	}

	void moveTemporaryContactToList(String address, long listId) {
		synchronized (mTemporaryContacts) {
			mTemporaryContacts.remove(address);
		}

		ContentValues values = new ContentValues(2);
		values.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_NORMAL);
		values.put(Imps.Contacts.CONTACTLIST, listId);

		String selection = Imps.Contacts.USERNAME + "=? AND " + Imps.Contacts.TYPE + "="
				+ Imps.Contacts.TYPE_TEMPORARY;
		String[] selectionArgs = { address };

		mResolver.update(mContactUrl, values, selection, selectionArgs);
	}

	boolean  updateContactType(String address, int type) {
		ContentValues values = new ContentValues(1);
		values.put(Imps.Contacts.TYPE, type);
		return updateContact(address, values);
	}
	
	void doFavoriteContact(String address, boolean isFavor) {
		ContentValues values = new ContentValues(1);
		if ( isFavor )
			values.put(Imps.Contacts.FAVORITE, 1);
		else
			values.put(Imps.Contacts.FAVORITE, 0);

		String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {Long.toString(mAccountId), address};

		mResolver.update(Imps.Contacts.CONTENT_URI, values, select, selectionArgs);
	}

	boolean updateContact(Contact contact, long listId)
	{
		ContentValues values = getContactContentValues(contact, listId);
		return updateContact(mAdaptee.normalizeAddress(contact.getAddress().getAddress()),values);

	}
	boolean updateContact(String username, ContentValues values) {
		String selection = Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = { username };
		return (mResolver.update(mContactUrl, values, selection, selectionArgs)) > 0;
	}

	void updatePresenceContent(Contact[] contacts) {
		ArrayList<String> usernames = new ArrayList<String>();
		ArrayList<String> statusArray = new ArrayList<String>();
		//ArrayList<String> customStatusArray = new ArrayList<String>();
		ArrayList<String> clientTypeArray = new ArrayList<String>();


		for (Contact c : contacts) {
			if ( mConn.getAdaptee().getState() == ImConnection.DISCONNECTED ) {
				return;
			}

			String username = mAdaptee.normalizeAddress(c.getAddress().getAddress());

			Presence p = c.getPresence();
			int status = convertPresenceStatus(p);
			//String customStatus = p.getStatusText();
			int clientType = translateClientType(p);

			usernames.add(username);
			statusArray.add(String.valueOf(status));
			//customStatusArray.add(customStatus);
			clientTypeArray.add(String.valueOf(clientType));
		}

		ContentValues values = new ContentValues();
		values.put(Imps.Contacts.ACCOUNT, mAccountId);
		putStringArrayList(values, Imps.Contacts.USERNAME, usernames);
		putStringArrayList(values, Imps.Presence.PRESENCE_STATUS, statusArray);
		//putStringArrayList(values, Imps.Presence.PRESENCE_CUSTOM_STATUS, customStatusArray);
		putStringArrayList(values, Imps.Presence.CONTENT_TYPE, clientTypeArray);

		if ( mResolver.update(Imps.Presence.BULK_CONTENT_URI, values, null, null) < 1 ) {
			for ( Contact c : contacts ) {
				updatePresence(c);
			}
		}
	}

	void updatePresence(Contact contact) {

		Cursor cursor = null;
		try{
			String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs1 = {Long.toString(mAccountId), contact.getAddress().getAddress()};

			cursor = mResolver.query(Imps.Contacts.CONTENT_URI,
					new String[]{Imps.Contacts._ID}, 
					select,
					selectionArgs1, null);
		}catch(Exception e){
			e.printStackTrace();
		}

		if ( cursor == null || !cursor.moveToFirst()) {
			if ( cursor != null )
				cursor.close();
			return;
		}

		int contactId = 0;

		try{
			contactId = cursor.getInt(cursor.getColumnIndexOrThrow(Imps.Contacts._ID));
		}catch(Exception e) {}

		if ( contactId < 1 )
		{
			cursor.close();
			return;
		}
		cursor.close();

		ContentValues presenceValues = getPresenceValues( contactId,
				contact.getPresence());

		if ( mResolver.update(Imps.Presence.CONTENT_URI, presenceValues,Imps.Presence.CONTACT_ID + "=" + contactId,null) < 1 )
		{
			mResolver.insert(Imps.Presence.CONTENT_URI, presenceValues);
		}
	}

	void updateAvatarsContent(Contact[] contacts) {
		ArrayList<ContentValues> avatars = new ArrayList<ContentValues>();
		ArrayList<String> usernames = new ArrayList<String>();

		for (Contact contact : contacts) {
			byte[] avatarData = contact.getPresence().getAvatarData();
			if (avatarData == null) {
				continue;
			}

			String username = mAdaptee.normalizeAddress(contact.getAddress().getAddress());

			ContentValues values = new ContentValues(2);
			values.put(Imps.Avatars.CONTACT, username);
			values.put(Imps.Avatars.DATA, avatarData);
			avatars.add(values);
			usernames.add(username);
		}
		if (avatars.size() > 0) {
			// ImProvider will replace the avatar content if it already exist.
			mResolver.bulkInsert(mAvatarUrl, avatars.toArray(new ContentValues[avatars.size()]));

			// notify avatar changed
			Intent i = new Intent(ImServiceConstants.ACTION_AVATAR_CHANGED);
			i.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, usernames);
			i.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, mProviderId);
			i.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, mAccountId);
			mContext.sendBroadcast(i);
		}
	}

	ContactListAdapter removeContactListFromDataBase(String name) {
		ContactListAdapter listAdapter = getContactListAdapter(name);
		if (listAdapter == null) {
			return null;
		}
		long id = listAdapter.getDataBaseId();

		// delete contacts of this list first
		mResolver.delete(mContactUrl, Imps.Contacts.CONTACTLIST + "=?",
				new String[] { Long.toString(id) });

		mResolver.delete(ContentUris.withAppendedId(Imps.ContactList.CONTENT_URI, id), null, null);
		synchronized (mContactLists) {
			return mContactLists.remove(listAdapter.getAddress());
		}
	}

	public boolean addContactListContent(ContactList list){

		String selection = Imps.ContactList.NAME + "=? AND " + Imps.ContactList.PROVIDER
				+ "=? AND " + Imps.ContactList.ACCOUNT + "=?";
		String[] selectionArgs = { list.getName(), Long.toString(mProviderId),
				Long.toString(mAccountId) };
		Cursor cursor = mResolver.query(Imps.ContactList.CONTENT_URI, CONTACT_LIST_ID_PROJECTION,
				selection, selectionArgs, null); // no sort order

		long listId = 0;
		Uri uri = null;
		try {
			if (cursor.moveToFirst()) {
				listId = cursor.getLong(0);
				uri = ContentUris.withAppendedId(Imps.ContactList.CONTENT_URI, listId);
			}
		} finally {
			cursor.close();
		}
		if (uri == null) {
			ContentValues contactListValues = new ContentValues(3);
			contactListValues.put(Imps.ContactList.NAME, list.getName());
			contactListValues.put(Imps.ContactList.PROVIDER, mProviderId);
			contactListValues.put(Imps.ContactList.ACCOUNT, mAccountId);

			uri = mResolver.insert(Imps.ContactList.CONTENT_URI, contactListValues);
			listId = ContentUris.parseId(uri);
		}

		mValidatedContactLists.add(list.getName());
		synchronized (mContactLists) {
			mContactLists.put(list.getAddress(), new ContactListAdapter(list, listId));
		}

		Collection<Contact> contacts = list.getContacts();
		if (contacts == null || contacts.size() == 0) {
			return false;
		}

		ArrayList<String> usernames = new ArrayList<String>();
		ArrayList<String> nicknames = new ArrayList<String>();
		ArrayList<String> userId = new ArrayList<String>();
		ArrayList<String> phones = new ArrayList<String>();
		ArrayList<String> contactTypeArray = new ArrayList<String>();
		ArrayList<String> subscriptionTypeArray = new ArrayList<String>();
		ArrayList<String> favoriteArray = new ArrayList<String>();
		String[] projection = {Imps.Contacts._ID, Imps.Contacts.REGION, Imps.Contacts.TYPE, Imps.Contacts.SUBSCRIPTION_TYPE};

		boolean needReloadFriendList = false;
		for (Contact c : contacts) {
			if ( mConn.getAdaptee().getState() != ImConnection.LOGGED_IN ) {
				return false;
			}

			String username = mAdaptee.normalizeAddress(c.getAddress().getAddress());
			if ( username.startsWith("pls") ) {
				c.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_BOTH;
				c.contact_type = Imps.Contacts.TYPE_PLUS_FRIEND;
			}
			String nickname = c.getName();
			mValidatedContacts.add(username);

			String phone = c.getPhoneNum();

			Cursor cursor2 = null;
			try{
				String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
				String[] selectionArgs1 = {Long.toString(mAccountId), username};

				cursor2 = mResolver.query(Imps.Contacts.CONTENT_URI,
						projection,
						select,
						selectionArgs1 , null );
				if ( cursor2 != null && cursor2.moveToFirst() ) {
					String region = cursor2.getString(1);
					int contactType = cursor2.getInt(2);
					int subType = cursor2.getInt(3);
					if ( region == null ) {
						mConn.loadVcard( username, null, false);
					}
					
					
					if ( contactType != c.contact_type || subType != c.sub_type ) {
						needReloadFriendList = true;
					}

					ContentValues values = new ContentValues(5);
					values.put(Imps.Contacts.TYPE, c.contact_type);
					values.put(Imps.Contacts.SUBSCRIPTION_TYPE, c.sub_type);
					values.put(Imps.Contacts.PHONE_NUMBER, c.phoneNum);
					values.put(Imps.Contacts.CONTACTLIST, listId);
					values.put(Imps.Contacts.PROVIDER, mProviderId);
					values.put(Imps.Contacts.ACCOUNT, mAccountId);
					mResolver.update(Imps.Contacts.CONTENT_URI, values, select, selectionArgs1);
					
					//needReloadFriendList = true;
					continue;
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if ( cursor2 != null )
					cursor2.close();
				cursor2 = null;
			}
			
			usernames.add(username);
			nicknames.add(nickname);
			phones.add(phone);
			subscriptionTypeArray.add(String.valueOf(c.sub_type));
			favoriteArray.add(String.valueOf(c.favor));
			contactTypeArray.add(String.valueOf(c.contact_type));
			userId.add(c.userid);
			mConn.loadVcard( username , null, false);
		}

		if ( usernames.size() > 0 ) {
			ContentValues values = new ContentValues(7);
			values.put(Imps.Contacts.PROVIDER, mProviderId);
			values.put(Imps.Contacts.ACCOUNT, mAccountId);
			values.put(Imps.Contacts.CONTACTLIST, listId);
			putStringArrayList(values, Imps.Contacts.USERNAME, usernames);
			putStringArrayList(values, Imps.Contacts.NICKNAME, nicknames);
			putStringArrayList(values, Imps.Contacts.PHONE_NUMBER, phones);
			putStringArrayList(values, Imps.Contacts.TYPE, contactTypeArray);
			putStringArrayList(values, Imps.Contacts.SUBSCRIPTION_TYPE, subscriptionTypeArray);
			putStringArrayList(values, Imps.Contacts.FAVORITE, favoriteArray);
			putStringArrayList(values, Imps.Contacts.USERID, userId);
			mResolver.insert(Imps.Contacts.BULK_CONTENT_URI, values);

			needReloadFriendList = true;

			for(int i =0; i < usernames.size(); i ++ ) {
				String address = usernames.get(i);
				mConn.loadVcard( address, null, true);
			}
		}

		//ì¹œêµ¬ëª©ë¡�ì�„ ìž¬ í˜„ì‹œí•  í•„ìš”ê°€ ìžˆì�„ë•Œ í†µë³´ë¥¼ ë³´ë‚¸ë‹¤.
		if ( needReloadFriendList ) {
			Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
			mContext.sendBroadcast(intent);
		}

		return false;
	}

	private void putStringArrayList(ContentValues values, String key, ArrayList<String> nicknames) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(nicknames);
			os.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		values.put(key, bos.toByteArray());

	}

	void updateListNameInDataBase(ContactList list) {
		ContactListAdapter listAdapter = getContactListAdapter(list.getAddress());

		Uri uri = ContentUris.withAppendedId(Imps.ContactList.CONTENT_URI,
				listAdapter.getDataBaseId());
		ContentValues values = new ContentValues(1);
		values.put(Imps.ContactList.NAME, list.getName());

		mResolver.update(uri, values, null, null);
	}

	private void deleteContactFromDataBase(Contact contact) {
		//GlobalFunc.getDbFile(mContext);
		String username = mAdaptee.normalizeAddress(contact.getAddress().getAddress());

		Cursor cursor = null;
		try{
			String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {Long.toString(mAccountId), contact.getAddress().getAddress()};

			cursor = mResolver.query(mContactUrl, 
					new String[]{Imps.Contacts._ID}, 
					select,
					selectionArgs,
					null);

			if ( cursor != null && cursor.moveToFirst() ) {

				int contactId = cursor.getInt(0);
				if ( contactId > 0 ) {

					int res = Imps.Notifications.removeNotificationCount(mResolver, 
							Imps.Notifications.CAT_CHATTING, 
							Imps.Notifications.FIELD_CHAT, 
							contactId);

					if ( res > 0 ) {
						Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
						mContext.sendBroadcast(intent);
					}

                    GlobalFunc.clearHistoryMessages(mContext, contactId);

					NotificationManager nMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
					nMgr.cancel(StatusBarNotifier.chat_notify_start_id + contactId);
					nMgr.cancel(StatusBarNotifier.sub_notify_start_id + contact.hashCode());
				}
			}

		}catch(Exception e){}
		finally{
			if ( cursor != null )
				cursor.close();
		}

		String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
		String[] selectionArgs = {Long.toString(mAccountId), username};
		mResolver.delete(mContactUrl, selection, selectionArgs);
	}

	Uri insertContactContent(Contact contact, long listId) {
		ContentValues values = getContactContentValues(contact, listId);
		Cursor tmpCursor = null;
		boolean isExistContact = false;

		try{
			String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
			String[] selectionArgs = {Long.toString(mAccountId), contact.getAddress().getAddress()};

			tmpCursor = mResolver.query(mContactUrl, new String[]{Imps.Contacts._ID}, select, selectionArgs, null);
			if ( tmpCursor.moveToFirst() )
				isExistContact = true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( tmpCursor != null )
				tmpCursor.close();
			tmpCursor = null;
		}
		if ( isExistContact )
			return null;
		
		Uri uri = mResolver.insert(mContactUrl, values);
		
		ContentValues presenceValues = getPresenceValues(ContentUris.parseId(uri),
				contact.getPresence());
		Cursor cursor = mResolver.query(Imps.Presence.CONTENT_URI, new String[] { Imps.Presence.CONTACT_ID },
				Imps.Presence.CONTACT_ID + "=" + ContentUris.parseId(uri),null,null);
		int contactId = 0;
		if ( cursor != null && cursor.moveToFirst()) {
			contactId = cursor.getInt(cursor.getColumnIndex(Imps.Presence.CONTACT_ID));
		}

		if ( cursor != null )
			cursor.close();


		if ( contactId == 0 ) 
			return uri;


		if ( mResolver.update(Imps.Presence.CONTENT_URI, presenceValues,Imps.Presence.CONTACT_ID + "=" + contactId,null) < 1 ) {
			mResolver.insert(Imps.Presence.CONTENT_URI, presenceValues);
		}

		return uri;
	}

	private ContentValues getContactContentValues(Contact contact, long listId) {
		final String username = mAdaptee.normalizeAddress(contact.getAddress().getAddress());
		final String nickname = contact.getName();
		int type = Imps.Contacts.TYPE_NORMAL;
		//int sub_type = contact.sub_type;
		
		if (isTemporary(username)) {
			type = Imps.Contacts.TYPE_TEMPORARY;
		}
		if (isBlocked(username)) {
			type = Imps.Contacts.TYPE_BLOCKED;
		}

		if (isHidden(username)) {
			type = Imps.Contacts.TYPE_HIDDEN;
		}

		ContentValues values = new ContentValues(4);
		values.put(Imps.Contacts.USERNAME, username);
		values.put(Imps.Contacts.NICKNAME, nickname);
		//values.put(Imps.Contacts.SUBSCRIPTION_TYPE, sub_type);
		values.put(Imps.Contacts.CONTACTLIST, listId);
		values.put(Imps.Contacts.TYPE, type);
		return values;
	}

	private ContentValues getPresenceValues(long contactId, Presence p) {
		ContentValues values = new ContentValues(3);
		values.put(Imps.Presence.CONTACT_ID, contactId);
		values.put(Imps.Contacts.PRESENCE_STATUS, convertPresenceStatus(p));
		values.put(Imps.Contacts.PRESENCE_CUSTOM_STATUS, p.getStatusText());
		values.put(Imps.Presence.CLIENT_TYPE, translateClientType(p));
		return values;
	}

	private int translateClientType(Presence presence) {
		int clientType = presence.getClientType();
		switch (clientType) {
		case Presence.CLIENT_TYPE_MOBILE:
			return Imps.Presence.CLIENT_TYPE_MOBILE;
		default:
			return Imps.Presence.CLIENT_TYPE_DEFAULT;
		}
	}

	/**
	 * Converts the presence status to the value defined for ImProvider.
	 * 
	 * @param presence The presence from the IM engine.
	 * @return The status value defined in for ImProvider.
	 */
	public static int convertPresenceStatus(Presence presence) {
		switch (presence.getStatus()) {
		case Presence.AVAILABLE:
			return Imps.Presence.AVAILABLE;

		case Presence.IDLE:
			return Imps.Presence.IDLE;

		case Presence.AWAY:
			return Imps.Presence.AWAY;

		case Presence.DO_NOT_DISTURB:
			return Imps.Presence.DO_NOT_DISTURB;

		case Presence.OFFLINE:
			return Imps.Presence.OFFLINE;
		}

		// impossible...
		MessengerService.debug("Illegal presence status value " + presence.getStatus());
		return Imps.Presence.AVAILABLE;
	}

	public void clearOnLogout() {
		clearValidatedContactsAndLists();
		clearTemporaryContacts();
		clearPresence();
	}

	/**
	 * Clears the list of validated contacts and contact lists. As contacts and
	 * contacts lists are added after login, contacts and contact lists are
	 * stored as "validated contacts". After initial download of contacts is
	 * complete, any contacts and contact lists that remain in the database, but
	 * are not in the validated list, are obsolete and should be removed. This
	 * function resets that list for use upon login.
	 */
	private void clearValidatedContactsAndLists() {
		// clear the list of validated contacts, contact lists, and blocked contacts
		mValidatedContacts.clear();
		mValidatedContactLists.clear();
		mValidatedBlockedContacts.clear();
	}

	/**
	 * Clear the temporary contacts in the database. As contacts are persist
	 * between IM sessions, the temporary contacts need to be cleared after
	 * logout.
	 */
	private void clearTemporaryContacts() {
		String selection = Imps.Contacts.CONTACTLIST + "=" + FAKE_TEMPORARY_LIST_ID;
		mResolver.delete(mContactUrl, selection, null);
	}

	/**
	 * Clears the presence of the all contacts. As contacts are persist between
	 * IM sessions, the presence need to be cleared after logout.
	 */
	void clearPresence() {
		StringBuilder where = new StringBuilder();
		where.append(Imps.Presence.CONTACT_ID);
		where.append(" in (select _id from contacts where ");
		where.append(Imps.Contacts.ACCOUNT);
		where.append("=");
		where.append(mAccountId);
		where.append(")");
		mResolver.delete(Imps.Presence.CONTENT_URI, where.toString(), null);
	}

	void closeChatSession(String address) {
		ChatSessionManagerAdapter chatSessionManager = (ChatSessionManagerAdapter) mConn
				.getChatSessionManager();
		ChatSessionAdapter session = (ChatSessionAdapter) chatSessionManager
				.getChatSession(address);
		if (session != null) {
			session.leave();
		}
	}

	void updateChatPresence(String address, String nickname, Presence p) {
		ChatSessionManagerAdapter sessionManager = (ChatSessionManagerAdapter) mConn
				.getChatSessionManager();
		// TODO: This only find single chat sessions, we need to go through all
		// active chat sessions and find if the contact is a participant of the
		// session.
		ChatSessionAdapter session = (ChatSessionAdapter) sessionManager.getChatSession(address);
		if (session != null) {
			session.insertPresenceUpdatesMsg(nickname, p);
		}
	}

	private void seedInitialPresences() {
		Builder builder = Imps.Presence.SEED_PRESENCE_BY_ACCOUNT_CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, mAccountId);
		mResolver.insert(builder.build(), new ContentValues(0));
	}

	@Override
	public boolean loadMoreFriends() throws RemoteException {

		List<ContactListAdapter> adapterLists = getContactLists();

		if ( adapterLists == null )
			return false;

		for(int i=0; i < adapterLists.size(); i++) {
			ContactListAdapter listAdapter = adapterLists.get(i);
			if ( listAdapter == null )
				continue;

			if ( listAdapter.isDefault() ) {

				ContactList list = listAdapter.getList();

				if ( list == null )
					continue;

				return addContactListContent(list);
			}
		}
		return false;
	}

	@Override
	public int favoriteContact(String address) throws RemoteException {
		try{
			return mAdaptee.favoriteContact(address);
		}catch(ImException e) {
			return e.getImError().getCode();
		}
	}

	@Override
	public int unFavoriteContact(String address) throws RemoteException {
		try{
			return mAdaptee.unFavoriteContact(address);
		}catch(ImException e) {
			return e.getImError().getCode();
		}
	}
}
