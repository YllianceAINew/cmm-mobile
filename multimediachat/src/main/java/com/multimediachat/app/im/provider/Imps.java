/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.multimediachat.app.im.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.multimediachat.app.ImApp;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.SystemServices;
import com.multimediachat.util.SystemServices.FileInfo;

import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;

/**
 * The IM provider stores all information about roster contacts, chat messages,
 * presence, etc.
 * 
 * @hide
 */
public class Imps {
	/** no public constructor since this is a utility class */
	public static String mAccountID = "";
	
	private Imps() {
	}


	/** The Columns for IM providers (i.e. AIM, Y!, GTalk) */
	public interface ProviderColumns {
		/** The name of the IM provider <P>Type: TEXT</P> */
		String NAME = "name";

		/** The full name of the provider <P>Type: TEXT</P> */
		String FULLNAME = "fullname";

		/**
		 * The category for the provider, used to form intent. <P>Type: TEXT</P>
		 */
		String CATEGORY = "category";

		/**
		 * The url users should visit to create a new account for this provider
		 * <P>Type: TEXT</P>
		 */
		String SIGNUP_URL = "signup_url";
	}

	/** Known names corresponding to the {@link ProviderColumns#NAME} column */
	public interface ProviderNames {
		//
		//NOTE: update Contacts.java with new providers when they're added.
		//
		String YAHOO = "Yahoo";
		String GTALK = "GTalk";
		String MSN = "MSN";
		String ICQ = "ICQ";
		String AIM = "AIM";
		String XMPP = "XMPP";
		String JABBER = "JABBER";
		String SKYPE = "SKYPE";
		String QQ = "QQ";
	}

	/** This table contains the IM providers */
	public static final class Provider implements BaseColumns, ProviderColumns {
		private Provider() {
		}

		public static final long getProviderIdForName(ContentResolver cr, String providerName) {


			String select = NAME + "=?";
			String[] selectionArgs = {providerName};

			Cursor cursor = cr.query(CONTENT_URI, PROVIDER_PROJECTION, select, selectionArgs, null);

			long retVal = 0;
			try {
				if (cursor.moveToFirst()) {
					retVal = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
				}
			} finally {
				if (cursor != null)
					cursor.close();

			}

			return retVal;
		}

		public static final String getProviderNameForId(ContentResolver cr, long providerId) {
			Cursor cursor = cr.query(CONTENT_URI, PROVIDER_PROJECTION, _ID + "=" + providerId,
					null, null);

			String retVal = null;
			try {
				if (cursor.moveToFirst()) {
					retVal = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
				}
			} finally {
				cursor.close();
			}

			return retVal;
		}

		private static final String[] PROVIDER_PROJECTION = new String[] { _ID, NAME };

		public static final String ACTIVE_ACCOUNT_ID = "account_id";
		public static final String ACTIVE_ACCOUNT_USERNAME = "account_username";
		public static final String ACTIVE_ACCOUNT_PW = "account_pw";
		public static final String ACTIVE_ACCOUNT_LOCKED = "account_locked";
		public static final String ACTIVE_ACCOUNT_KEEP_SIGNED_IN = "account_keepSignedIn";
		public static final String ACCOUNT_PRESENCE_STATUS = "account_presenceStatus";
		public static final String ACCOUNT_CONNECTION_STATUS = "account_connStatus";

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/providers");

		public static final Uri CONTENT_URI_WITH_ACCOUNT = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/providers/account");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * people.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-providers";

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-providers";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "name ASC";
	}

	/**
	 * The columns for IM accounts. There can be more than one account for each
	 * IM provider.
	 */
	public interface AccountColumns {
		/** The name of the account <P>Type: TEXT</P> */
		String NAME = "name";

		/** The IM provider for this account <P>Type: INTEGER</P> */
		String PROVIDER = "provider";

		/** The username for this account <P>Type: TEXT</P> */
		String USERNAME = "username";

		/** The password for this account <P>Type: TEXT</P> */
		String PASSWORD = "pw";

		/**
		 * A boolean value indicates if the account is active. <P>Type:
		 * INTEGER</P>
		 */
		String ACTIVE = "active";

		/**
		 * A boolean value indicates if the account is locked (not editable)
		 * <P>Type: INTEGER</P>
		 */
		String LOCKED = "locked";

		/**
		 * A boolean value to indicate whether this account is kept signed in.
		 * <P>Type: INTEGER</P>
		 */
		String KEEP_SIGNED_IN = "keep_signed_in";

		/**
		 * A boolean value indiciating the last login state for this account
		 * <P>Type: INTEGER</P>
		 */
		String LAST_LOGIN_STATE = "last_login_state";
	}

	/** This table contains the IM accounts. */
	public static final class Account implements BaseColumns, AccountColumns {
		private Account() {
		}

		public static final long getProviderIdForAccount(ContentResolver cr, long accountId) {
			Cursor cursor = cr.query(CONTENT_URI, PROVIDER_PROJECTION, _ID + "=" + accountId,
					null /* selection args */, null /* sort order */);

			long providerId = 0;

			try {
				if (cursor.moveToFirst()) {
					providerId = cursor.getLong(PROVIDER_COLUMN);
				}
			} finally {
				cursor.close();
			}

			return providerId;
		}

		public static final String getUserName(ContentResolver cr, long accountId) {
			Cursor cursor = cr.query(CONTENT_URI, new String[] { USERNAME }, _ID + "=" + accountId,
					null /* selection args */, null /* sort order */);
			String ret = null;
			try {
				if (cursor.moveToFirst()) {
					ret = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
				}
			} catch (Exception e){
				DebugConfig.error("Imps/getUserName", e.toString());
			} finally {
				cursor.close();
			}
			ret = mPref.getString("username", "");	//ccj
			return ret;
		}

		public static final String getPassword(ContentResolver cr, long accountId) {
			Cursor cursor = cr.query(CONTENT_URI, new String[] { PASSWORD }, _ID + "=" + accountId,
					null /* selection args */, null /* sort order */);
			String ret = null;
			try {
				if (cursor.moveToFirst()) {
					ret = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD));
				}
			} catch (Exception e) {
				DebugConfig.error("Imps/getPassword", e.toString());
			} finally {
				cursor.close();
			}

			ret = mPref.getString("apikey", "");	//ccj
			return ret;
		}

		private static final String[] PROVIDER_PROJECTION = new String[] { PROVIDER };
		private static final int PROVIDER_COLUMN = 0;

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/accounts");

		/** The content:// style URL for looking up by domain */
		public static final Uri BY_DOMAIN_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/domainAccounts");


		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * account.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-accounts";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * account.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-accounts";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "name ASC";

	}

	/** Connection status */
	public interface ConnectionStatus {
		/** The connection is offline, not logged in. */
		int OFFLINE = 0;

		/** The connection is attempting to connect. */
		int CONNECTING = 1;

		/** The connection is suspended due to network not available. */
		int SUSPENDED = 2;

		/** The connection is logged in and online. */
		int ONLINE = 3;
	}

	public interface AccountStatusColumns {
		/** account id <P>Type: INTEGER</P> */
		String ACCOUNT = "account";

		/**
		 * User's presence status, see definitions in {#link
		 * CommonPresenceColumn} <P>Type: INTEGER</P>
		 */
		String PRESENCE_STATUS = "presenceStatus";

		/**
		 * The connection status of this account, see {#link ConnectionStatus}
		 * <P>Type: INTEGER</P>
		 */
		String CONNECTION_STATUS = "connStatus";
	}
	/* Message status definition */
	public interface StatusType {
		int UNKNOWN = 0;
		int VOICE_CALL_INCOMING = 1;
		int VOICE_CALL_OUTGOING = 2;
		int VOICE_CALL_MISSED = 3;
		int VIDEO_CALL_INCOMING = 11;
		int VIDEO_CALL_OUTGOING = 12;
		int FILE_TRANSFER = 21;
		int FILE_COMPRESSS = 22;
		int MESSAGE_TRANSFER = 31;
		int CALL_STATUS_INCOMING = 41;
		int CALL_STATUS_OUTGOING = 42;
		int CALL_STATUS_MISSED = 43;
	}

	public static final class AccountStatus implements BaseColumns, AccountStatusColumns {
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/accountStatus");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * account status.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-account-status";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * account status.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-account-status";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "name ASC";
	}

	/** Columns from the Contacts table. */
	public interface ContactsColumns {
		/** The username <P>Type: TEXT</P> */
		String USERNAME = "username";
		String NICKNAME = "nickname";
		String PROVIDER = "provider";
		String ACCOUNT = "account";
		String HASH = "hash";
		String CONTACTLIST = "contactList";
		String STATUS = "status";
		String GENDER = "gender";
		String REGION = "region";
		String ISDELIVERD = "is_delivered";
		String DELIVERTYPE = "deliverType";
		String ISDELETED = "is_deleted";
		String TYPE = "type";
		String STATUSMESSAGE = "statusMessage";
		String ISLOADEDVCARD = "isloadedvcard";
		String SUBSCRIPTIONMESSAGE = "subscriptionMessage";
		String PHONE_NUMBER = "phone";
		String COVER_IMAGE_COUNT = "cover_image_count";
		String FOLLOW_COUNT = "follow_count";
		String HOME_URL	= "home_url";
		String WEBSITE = "website";
		String DESCRIPTION = "description";
		String MOBILE = "mobile";
		String FULLNAME = "fullname";
		String STAR = "star";
		String USERID = "userid";
		String THUMBNAILPATH = "thumbnail_path";

		int TYPE_NORMAL = 0;
		/**
		 * temporary contact, someone not in the list of contacts that we
		 * subscribe presence for. Usually created because of the user is having
		 * a chat session with this contact.
		 */
		int TYPE_TEMPORARY = 1;
		/** temporary contact created for group chat. */
		int TYPE_GROUP = 2;
		/** blocked contact. */
		int TYPE_BLOCKED = 3;
		/**
		 * the contact is hidden. The client should always display this contact
		 * to the user.
		 */
		int TYPE_HIDDEN = 4;
		/**
		 * the contact is pinned. The client should always display this contact
		 * to the user.
		 */
		int TYPE_PINNED = 5;
		int TYPE_PLUS_FRIEND = 6;
		

		/** Contact subscription status <P>Type: INTEGER</P> */
		String SUBSCRIPTION_STATUS = "subscriptionStatus";

		/** no pending subscription */
		int SUBSCRIPTION_STATUS_NONE = 0;
		/** requested to subscribe */
		int SUBSCRIPTION_STATUS_SUBSCRIBE_PENDING = 1;
		/** requested to unsubscribe */
		int SUBSCRIPTION_STATUS_UNSUBSCRIBE_PENDING = 2;
		/** received subscribe */
		int SUBSCRIPTION_STATUS_SUBSCRIBE_FROM_PENDING = 3;
		/** received unsubscribe */
		int SUBSCRIPTION_STATUS_UNSUBSCRIBE_FROM_PENDING = 4;

		/** Contact subscription type <P>Type: INTEGER </P> */
		String SUBSCRIPTION_TYPE = "subscriptionType";

		/** The user and contact have no interest in each other's presence. */
		int SUBSCRIPTION_TYPE_NONE = 0;
		/** The user wishes to stop receiving presence updates from the contact. */
		int SUBSCRIPTION_TYPE_REMOVE = 1;
		/**
		 * The user is interested in receiving presence updates from the
		 * contact.
		 */
		int SUBSCRIPTION_TYPE_TO = 2;
		/**
		 * The contact is interested in receiving presence updates from the
		 * user.
		 */
		int SUBSCRIPTION_TYPE_FROM = 3;
		/**
		 * The user and contact have a mutual interest in each other's presence.
		 */
		int SUBSCRIPTION_TYPE_BOTH = 4;
		/** This is a special type reserved for pending subscription requests */
		int SUBSCRIPTION_TYPE_INVITATIONS = 5;

		/**
		 * Quick Contact: derived from Google Contact Extension's
		 * "message_count" attribute. <P>Type: INTEGER</P>
		 */
		String QUICK_CONTACT = "qc";

		/**
		 * Google Contact Extension attribute
		 * 
		 * Rejected: a boolean value indicating whether a subscription request
		 * from this client was ever rejected by the user. "true" indicates that
		 * it has. This is provided so that a client can block repeated
		 * subscription requests. <P>Type: INTEGER</P>
		 */
		String REJECTED = "rejected";

		/**
		 * Off The Record status: 0 for disabled, 1 for enabled <P>Type: INTEGER
		 * </P>
		 */
		String OTR = "otr";

		int DELIVER_TYPE_NORMAL = 0;
		int DELIVER_TYPE_DELETED = 1;
		int DELIVER_TYPE_RENAMED = 2;
		int DELIVER_TYPE_HIDDEN = 4;
		int DELIVER_TYPE_UNHIDDEN = 8;
		int DELIVER_TYPE_BLOCKED = 16;
		int DELIVER_TYPE_UNBLOCKED = 32;
		int DELIVER_TYPE_FAVORITE = 64;
		int DELIVER_TYPE_UNFAVORITE = 128;


		String FAVORITE = "favor";
		int VCARD_LOAD_INFO = 5;
	}

	/** This table contains contacts. */
	public static final class UniContacts implements BaseColumns {
		/** no public constructor since this is a utility class */
		private UniContacts() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/CONTACT");
		public static final Uri CONTENT_URI_VIEW = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/unicontacts");
	}
	/** This table contains contacts. */
	public static final class Contacts implements BaseColumns, ContactsColumns, PresenceColumns,
	ChatsColumns {
		/** no public constructor since this is a utility class */
		private Contacts() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contacts");

		/** The content:// style URL for contacts joined with presence */
		public static final Uri CONTENT_URI_WITH_PRESENCE = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contactsWithPresence");

		/**
		 * The content:// style URL for barebone contacts, not joined with any
		 * other table
		 */
		public static final Uri CONTENT_URI_CONTACTS_BAREBONE = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contactsBarebone");

		/** The content:// style URL for contacts who have an open chat session */
		public static final Uri CONTENT_URI_CHAT_CONTACTS = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contacts/chatting");

		/** The content:// style URL for contacts who have been blocked */
		public static final Uri CONTENT_URI_BLOCKED_CONTACTS = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contacts/blocked");

		/** The content:// style URL for contacts by provider and account */
		public static final Uri CONTENT_URI_CONTACTS_BY = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contacts");

		/**
		 * The content:// style URL for contacts by provider and account, and
		 * who have an open chat session
		 */
		public static final Uri CONTENT_URI_CHAT_CONTACTS_BY = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contacts/chatting");

		/**
		 * The content:// style URL for contacts by provider and account, and
		 * who are online
		 */
		public static final Uri CONTENT_URI_ONLINE_CONTACTS_BY = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contacts/online");

		/**
		 * The content:// style URL for contacts by provider and account, and
		 * who are offline
		 */
		public static final Uri CONTENT_URI_OFFLINE_CONTACTS_BY = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contacts/offline");

		/** The content:// style URL for operations on bulk contacts */
		public static final Uri BULK_CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/bulk_contacts");

		/**
		 * The content:// style URL for the count of online contacts in each
		 * contact list by provider and account.
		 */
		public static final Uri CONTENT_URI_ONLINE_COUNT = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contacts/onlineCount");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * people.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-contacts";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * person.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-contacts";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "subscriptionType DESC, last_message_date DESC,"
				+ " mode DESC, nickname COLLATE UNICODE ASC";

		public static final String ORDER_BY_LOCALIZED = "nickname COLLATE LOCALIZED ASC";

		public static final String CHAT_SORT_ORDER = "last_message_date DESC," + "nickname COLLATE UNICODE ASC";

		public static final String CONTACT_ASC_SORT_ORDER = "nickname COLLATE UNICODE ASC";

		public static final String CHATS_CONTACT = "chats_contact";

		public static final String AVATAR_HASH = "avatars_hash";

		public static final String AVATAR_DATA = "avatars_data";

		public static int getReceivedRequestsCount(ContentResolver resolver)
		{
			String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.SUBSCRIPTION_TYPE + "=?";
			String[] selectionArgs = {mAccountID, String.valueOf(Imps.Contacts.SUBSCRIPTION_TYPE_FROM)};
			Cursor cursor = null;
			int ret = 0;

			try {
				cursor = resolver.query(CONTENT_URI, new String[]{"count(*) AS count"}, select, selectionArgs, null);
				if (cursor != null) {
					while(cursor.moveToNext()) {
						ret = cursor.getInt(0);
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

			return ret;
		}
	}

	public interface TagsColumns {
		String TAG = "tag";
		String USERNAME = "username";
		String ACCOUNT = "account";
	}

	/** This table contains the contact lists. */
	public static final class Tags implements BaseColumns, TagsColumns {
		private Tags() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/tags");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * people.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-tags";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * person.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-tags";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "tag COLLATE UNICODE ASC";

		public static final String PROVIDER_NAME = "provider_name";

		public static final String ACCOUNT_NAME = "account_name";

		public static void deleteTagMember (ContentResolver resolver, String userName)
		{
			String sel = Imps.Tags.USERNAME + "=?";
			String[] selArgs = {userName};
			resolver.delete(Imps.Tags.CONTENT_URI, sel, selArgs);
		}
	}
	/** Columns from the ContactList table. */
	public interface ContactListColumns {
		String NAME = "name";
		String PROVIDER = "provider";
		String ACCOUNT = "account";
	}

	/** This table contains the contact lists. */
	public static final class ContactList implements BaseColumns, ContactListColumns {
		private ContactList() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contactLists");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * people.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-contactLists";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * person.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-contactLists";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "name COLLATE UNICODE ASC";

		public static final String PROVIDER_NAME = "provider_name";

		public static final String ACCOUNT_NAME = "account_name";
	}

	/** Columns from the BlockedList table. */
	public interface BlockedListColumns {
		/** The username of the blocked contact. <P>Type: TEXT</P> */
		String USERNAME = "username";

		/** The nickname of the blocked contact. <P>Type: TEXT</P> */
		String NICKNAME = "nickname";

		/** The provider id of the blocked contact. <P>Type: INT</P> */
		String PROVIDER = "provider";

		/** The account id of the blocked contact. <P>Type: INT</P> */
		String ACCOUNT = "account";
	}

	/** This table contains blocked lists */
	public static final class BlockedList implements BaseColumns, BlockedListColumns {
		private BlockedList() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/blockedList");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * people.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-blockedList";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * person.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-blockedList";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "nickname ASC";

		public static final String PROVIDER_NAME = "provider_name";

		public static final String ACCOUNT_NAME = "account_name";

		public static final String AVATAR_DATA = "avatars_data";
	}

	/** Columns from the contactsEtag table */
	public interface ContactsEtagColumns {
		/**
		 * The roster etag, computed by the server, stored on the client. There
		 * is one etag per account roster. <P>Type: TEXT</P>
		 */
		String ETAG = "etag";

		/**
		 * The OTR etag, computed by the server, stored on the client. There is
		 * one OTR etag per account roster. <P>Type: TEXT</P>
		 */
		String OTR_ETAG = "otr_etag";

		/** The account id for the etag. <P> Type: INTEGER </P> */
		String ACCOUNT = "account";
	}

	public static final class ContactsEtag implements BaseColumns, ContactsEtagColumns {
		private ContactsEtag() {
		}

		public static final Cursor query(ContentResolver cr, String[] projection) {
			return cr.query(CONTENT_URI, projection, null, null, null);
		}

		public static final Cursor query(ContentResolver cr, String[] projection, String where,
				String orderBy) {
			return cr.query(CONTENT_URI, projection, where, null, orderBy == null ? null : orderBy);
		}

		public static final String getRosterEtag(ContentResolver resolver, long accountId) {
			String retVal = null;

			Cursor c = resolver.query(CONTENT_URI, CONTACT_ETAG_PROJECTION, ACCOUNT + "="
					+ accountId,
					null /* selection args */, null /* sort order */);

			try {
				if (c.moveToFirst()) {
					retVal = c.getString(COLUMN_ETAG);
				}
			} finally {
				c.close();
			}

			return retVal;
		}

		public static final String getOtrEtag(ContentResolver resolver, long accountId) {
			String retVal = null;

			Cursor c = resolver.query(CONTENT_URI, CONTACT_OTR_ETAG_PROJECTION, ACCOUNT + "="
					+ accountId,
					null /* selection args */, null /* sort order */);

			try {
				if (c.moveToFirst()) {
					retVal = c.getString(COLUMN_OTR_ETAG);
				}
			} finally {
				c.close();
			}

			return retVal;
		}

		private static final String[] CONTACT_ETAG_PROJECTION = new String[] { Imps.ContactsEtag.ETAG // 0
		};

		private static int COLUMN_ETAG = 0;

		private static final String[] CONTACT_OTR_ETAG_PROJECTION = new String[] { Imps.ContactsEtag.OTR_ETAG // 0
		};

		private static int COLUMN_OTR_ETAG = 0;

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/contactsEtag");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * people.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-contactsEtag";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * person.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-contactsEtag";
	}

	/** Message type definition */
	public interface MessageType {
		/* sent message */
		int OUTGOING = 0;
		/* received message */
		int INCOMING = 1;
		/* presence became available */
		int PRESENCE_AVAILABLE = 2;
		/* presence became away */
		int PRESENCE_AWAY = 3;
		/* presence became DND (busy) */
		int PRESENCE_DND = 4;
		/* presence became unavailable */
		int PRESENCE_UNAVAILABLE = 5;
		/* generic status */
		int STATUS = 7;
		/* the message cannot be sent now, but will be sent later */
		int POSTPONED = 8;

		int FRIEND_INVITE = 17;
		
		int DELETED_OUTGOING = 18;
		int DELETED_INCOMING = 19;
		
		int OPER_DELETE = 20;
		int OPER_SEEN = 21;
		int OPER_MODIFY = 22;
	}
	
	public interface MessageErrorCode {
		int NORMAL = 0;
		int MODIFIED = 1;
		int IRREGULAR_WORD = 7;
		int SERVER_RECEIVED = 9;
		int NO_FRIEND = 11;
		int UNAVAILABLE_CHAT = 12;
		int NO_CALLABLE = 13;
		int BLOCK_ME = 14;
		int BLOCK_FRIEND = 15;
	}

	public interface FileErrorCode {
		int COMPRESSING = 20;
		int UPLOADING = 21;
		int UPLOADFAILED = 22;
		int UPLOADSUCCESS = 23;
		int DOWNLOADING = 24;
		int DOWNLOADFAILED = 25;
		int DOWNLOADSUCCESS = 26;
		int STOPPEDBYCALL = 27;
		int UPLOADCANCELLED = 28;
		int DOWNLOADCANCELLED = 29;
	}

	/** The common columns for messages table */
	public interface MessageColumns {
		/**
		 * The thread_id column stores the contact id of the contact the message
		 * belongs to. For groupchat messages, the thread_id stores the group
		 * id, which is the contact id of the temporary group contact created
		 * for the groupchat. So there should be no collision between groupchat
		 * message thread id and regular message thread id.
		 */
		String THREAD_ID = "thread_id";

		/**
		 * The nickname. This is used for groupchat messages to indicate the
		 * participant's nickname. For non groupchat messages, this field should
		 * be left empty.
		 */
		String NICKNAME = "nickname";

		/** The body <P>Type: TEXT</P> */
		String BODY = "body";

		/** The date this message is sent or received <P>Type: INTEGER</P> */
		String DATE = "date";

		/** The server date this message is sent or received <P>Type: INTEGER</P> */
		String SERVER_TIME = "server_time";

		/** Message Type, see {@link MessageType} <P>Type: INTEGER</P> */
		String TYPE = "type";

		/** Error Code: 0 means no error. <P>Type: INTEGER </P> */
		String ERROR_CODE = "err_code";

		/** Error Message <P>Type: TEXT</P> */
		String ERROR_MESSAGE = "err_msg";

		/** File Send/Recv Succesed Count (Int default 0) */
		String FILE_SUB_COUNT = "file_sub_count";
		/** Total File Splited Count (Int default 0) */
		String FILE_TOTAL_COUNT = "file_total_count";

		/**
		 * Packet ID, auto assigned by the GTalkService for outgoing messages or
		 * the GTalk server for incoming messages. The packet id field is
		 * optional for messages, so it could be null. <P>Type: STRING</P>
		 */
		String PACKET_ID = "packet_id";

		/** Is groupchat message or not <P>Type: INTEGER</P> */
		String IS_GROUP_CHAT = "is_muc";

		/**
		 * A hint that the UI should show the sent time of this message <P>Type:
		 * INTEGER</P>
		 */
		String DISPLAY_SENT_TIME = "show_ts";

		/** Whether a delivery confirmation was received. <P>Type: INTEGER</P> */
		String IS_DELIVERED = "is_delivered";

		/** Mime type.  If non-null, body is a URI. */
		String MIME_TYPE = "mime_type";

		/**time interval of current locale date with server date.
		 *  
		 */
		String TIME_DELAY = "time_delay";
		String THUMBNAIL_WIDTH = "thumbnail_width"; /**INTEGER**/
		String THUMBNAIL_HEIGHT = "thumbnail_height"; /**INTEGER**/
		String SAMPLE_IMAGE_PATH = "sample_image_path"; /**String**/
		
		String PLUSIMAGE_URL = "plusimage_url"; /**String**/
		String PLUSIMAGE_NUM = "plusimage_num"; /**INTEGER**/
		String PLUSLINK_URL = "pluslink_url"; /**String**/
	}

	/** This table contains messages. */
	public static final class Messages implements BaseColumns, MessageColumns {
		/** no public constructor since this is a utility class */
		private Messages() {
		}

        /**
         * Gets the Uri to query messages by id.
         *
         * @param msgId the id of the message.
         * @return the Uri
         */
        public static final Uri getContentUriById(long msgId) {
            Uri.Builder builder = CONTENT_URI_MESSAGES_BY_ID.buildUpon();
            ContentUris.appendId(builder, msgId);
            return builder.build();
        }

		/**
		 * Gets the Uri to query messages by thread id.
		 * 
		 * @param threadId the thread id of the message.
		 * @return the Uri
		 */
		public static final Uri getContentUriByThreadId(long threadId) {
			Uri.Builder builder = CONTENT_URI_MESSAGES_BY_THREAD_ID.buildUpon();
			ContentUris.appendId(builder, threadId);
			return builder.build();
		}

		/**
		 * @deprecated
		 * 
		 *             Gets the Uri to query messages by account and contact.
		 * 
		 * @param accountId the account id of the contact.
		 * @param username the user name of the contact.
		 * @return the Uri
		 */
		public static final Uri getContentUriByContact(long accountId, String username) {
			Uri.Builder builder = CONTENT_URI_MESSAGES_BY_ACCOUNT_AND_CONTACT.buildUpon();
			ContentUris.appendId(builder, accountId);
			builder.appendPath(username);
			return builder.build();
		}

		/**
		 * Gets the Uri to query messages by provider.
		 * 
		 * @param providerId the service provider id.
		 * @return the Uri
		 */
		public static final Uri getContentUriByProvider(long providerId) {
			Uri.Builder builder = CONTENT_URI_MESSAGES_BY_PROVIDER.buildUpon();
			ContentUris.appendId(builder, providerId);
			return builder.build();
		}

		/**
		 * Gets the Uri to query off the record messages by account.
		 * 
		 * @param accountId the account id.
		 * @return the Uri
		 */
		public static final Uri getContentUriByAccount(long accountId) {
			Uri.Builder builder = CONTENT_URI_BY_ACCOUNT.buildUpon();
			ContentUris.appendId(builder, accountId);
			return builder.build();
		}

		/**
		 * Gets the Uri to query off the record messages by thread id.
		 * 
		 * @param threadId the thread id of the message.
		 * @return the Uri
		 */
		public static final Uri getOtrMessagesContentUriByThreadId(long threadId) {
			Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_THREAD_ID.buildUpon();
			ContentUris.appendId(builder, threadId);
			return builder.build();
		}

		/**
		 * @deprecated
		 * 
		 *             Gets the Uri to query off the record messages by account
		 *             and contact.
		 * 
		 * @param accountId the account id of the contact.
		 * @param username the user name of the contact.
		 * @return the Uri
		 */
		public static final Uri getOtrMessagesContentUriByContact(long accountId, String username) {
			Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_ACCOUNT_AND_CONTACT.buildUpon();
			ContentUris.appendId(builder, accountId);
			builder.appendPath(username);
			return builder.build();
		}

		/**
		 * Gets the Uri to query off the record messages by provider.
		 * 
		 * @param providerId the service provider id.
		 * @return the Uri
		 */
		public static final Uri getOtrMessagesContentUriByProvider(long providerId) {
			Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_PROVIDER.buildUpon();
			ContentUris.appendId(builder, providerId);
			return builder.build();
		}

		/**
		 * Gets the Uri to query off the record messages by account.
		 * 
		 * @param accountId the account id.
		 * @return the Uri
		 */
		public static final Uri getOtrMessagesContentUriByAccount(long accountId) {
			Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_ACCOUNT.buildUpon();
			ContentUris.appendId(builder, accountId);
			return builder.build();
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/messages");

        /** The content:// style URL for messages by thread id */
        public static final Uri CONTENT_URI_MESSAGES_BY_ID = Uri
                .parse("content://com.multimediachat.app.im.provider.Imps/messagesById");

		/** The content:// style URL for messages by thread id */
		public static final Uri CONTENT_URI_MESSAGES_BY_THREAD_ID = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/messagesByThreadId");

		/** The content:// style URL for messages by account and contact */
		public static final Uri CONTENT_URI_MESSAGES_BY_ACCOUNT_AND_CONTACT = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/messagesByAcctAndContact");

		/** The content:// style URL for messages by provider */
		public static final Uri CONTENT_URI_MESSAGES_BY_PROVIDER = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/messagesByProvider");

		/** The content:// style URL for messages by account */
		public static final Uri CONTENT_URI_BY_ACCOUNT = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/messagesByAccount");

		/** The content:// style url for off the record messages */
		public static final Uri OTR_MESSAGES_CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/otrMessages");

		/** The content:// style url for off the record messages by thread id */
		public static final Uri OTR_MESSAGES_CONTENT_URI_BY_THREAD_ID = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/otrMessagesByThreadId");

		/**
		 * The content:// style url for off the record messages by account and
		 * contact
		 */
		public static final Uri OTR_MESSAGES_CONTENT_URI_BY_ACCOUNT_AND_CONTACT = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/otrMessagesByAcctAndContact");

		/** The content:// style URL for off the record messages by provider */
		public static final Uri OTR_MESSAGES_CONTENT_URI_BY_PROVIDER = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/otrMessagesByProvider");

		/** The content:// style URL for off the record messages by account */
		public static final Uri OTR_MESSAGES_CONTENT_URI_BY_ACCOUNT = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/otrMessagesByAccount");

		public static final Uri OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/otrMessagesByPacketId");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * people.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-messages";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * person.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-messages";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "date ASC";

		/**
		 * The "contact" column. This is not a real column in the messages
		 * table, but a temoprary column created when querying for messages
		 * (joined with the contacts table)
		 */
		public static final String CONTACT = "contact";
	}

	/** Columns for the GroupMember table. */
	public interface GroupMemberColumns {
		/** The id of the group this member belongs to. <p>Type: INTEGER</p> */
		String GROUP = "groupId";

		/** The full name of this member. <p>Type: TEXT</p> */
		String USERNAME = "username";

		/** The nick name of this member. <p>Type: TEXT</p> */
		String NICKNAME = "nickname";

		/** Ã«Â©Â¤Ã«Â²â€žÃ¬ï¿½Ëœ Ã«Â¸â€�Ã«Â¡ï¿½Ã¬Æ’ï¿½Ã­Æ’Å“Ã«Â¥Â¼ Ã«â€šËœÃ­Æ’â‚¬Ã«â€šÂ¸Ã«â€¹Â¤*/
		String TYPE = "type";

		int TYPE_NORMAL = 0;
		int TYPE_BLOCKED = 1;
		int TYPE_LEFT = 2;
	}

	public final static class GroupMembers implements GroupMemberColumns {
		private GroupMembers() {
		}

		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/groupMembers");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of group
		 * members.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-groupMembers";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * group member.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-groupMembers";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "_id ASC";
	}

	/** Columns from the Invitation table. */
	public interface InvitationColumns {
		/** The provider id. <p>Type: INTEGER</p> */
		String PROVIDER = "providerId";

		/** The account id. <p>Type: INTEGER</p> */
		String ACCOUNT = "accountId";

		/** The invitation id. <p>Type: TEXT</p> */
		String INVITE_ID = "inviteId";

		/** The name of the sender of the invitation. <p>Type: TEXT</p> */
		String SENDER = "sender";

		/**
		 * The name of the group which the sender invite you to join. <p>Type:
		 * TEXT</p>
		 */
		String GROUP_NAME = "groupName";

		/** A note <p>Type: TEXT</p> */
		String NOTE = "note";

		/** The current status of the invitation. <p>Type: TEXT</p> */
		String STATUS = "status";

		int STATUS_PENDING = 0;
		int STATUS_ACCEPTED = 1;
		int STATUS_REJECTED = 2;
	}

	/** This table contains the invitations received from others. */
	public final static class Invitation implements InvitationColumns, BaseColumns {
		private Invitation() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/invitations");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * invitations.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-invitations";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * invitation.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-invitations";
	}

	/** Columns from the Avatars table */
	public interface AvatarsColumns {
		/** The contact this avatar belongs to <P>Type: TEXT</P> */
		String CONTACT = "contact";

		String PROVIDER = "provider_id";

		String ACCOUNT = "account_id";

		/** The hash of the image data <P>Type: TEXT</P> */
		String HASH = "hash";

		/** raw image data <P>Type: BLOB</P> */
		String DATA = "data";
	}

	/** This table contains avatars. */
	public static final class Avatars implements BaseColumns, AvatarsColumns {
		/** no public constructor since this is a utility class */
		private Avatars() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/avatars");

		/**
		 * The content:// style URL for avatars by provider, account and contact
		 */
		public static final Uri CONTENT_URI_AVATARS_BY = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/avatarsBy");

		/** The MIME type of {@link #CONTENT_URI} providing the avatars */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-avatars";

		/** The MIME type of a {@link #CONTENT_URI} */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-avatars";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "contact ASC";

	}

	public static final class Big_Avatars implements BaseColumns, AvatarsColumns {
		/** no public constructor since this is a utility class */
		private Big_Avatars() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/big_avatars");

		/**
		 * The content:// style URL for avatars by provider, account and contact
		 */
		public static final Uri CONTENT_URI_AVATARS_BY = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/big_avatarsBy");

		/** The MIME type of {@link #CONTENT_URI} providing the avatars */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-big_avatars";

		/** The MIME type of a {@link #CONTENT_URI} */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-big_avatars";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "contact ASC";

	}

	/** Columns from the stickers table */
	public interface StickersColumns {
		String NAME = "name";
		String CATEGORY = "category";
		String STATE = "state";
		String SCOUNT = "scount";
		String SHOW_ORDER = "show_order";

		int    STATE_INSTALLED = 1;
		int    STATE_DELETED = 2;
		/*int    STATE_DOWNLOADED = 2;*/
	}

	public static final class Stickers implements BaseColumns, StickersColumns {
		private Stickers(){

		}
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/stickers");
	}

	/** Columns from the themes table */
	public interface ThemesColumns {
		String NAME = "name";
		String CATEGORY = "category";
	}

	public static final class Themes implements BaseColumns, ThemesColumns {
		private Themes(){

		}
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/themes");
	}

	/**
	 * Common presence columns shared between the IM and contacts presence
	 * tables
	 */
	public interface CommonPresenceColumns {
		/** The priority, an integer, used by XMPP presence <P>Type: INTEGER</P> */
		String PRIORITY = "priority";

		/**
		 * The server defined status. <P>Type: INTEGER (one of the values
		 * below)</P>
		 */
		String PRESENCE_STATUS = "mode";

		/** Presence Status definition */
		int OFFLINE = 0;
		int INVISIBLE = 1;
		int AWAY = 2;
		int IDLE = 3;
		int DO_NOT_DISTURB = 4;
		int AVAILABLE = 5;

		int NEW_ACCOUNT = -99;


		/** The user defined status line. <P>Type: TEXT</P> */
		String PRESENCE_CUSTOM_STATUS = "status";
	}

	/** Columns from the Presence table. */
	public interface PresenceColumns extends CommonPresenceColumns {
		/** The contact id <P>Type: INTEGER</P> */
		String CONTACT_ID = "contact_id";

		/**
		 * The contact's JID resource, only relevant for XMPP contact <P>Type:
		 * TEXT</P>
		 */
		String JID_RESOURCE = "jid_resource";

		/** The contact's client type */
		String CLIENT_TYPE = "client_type";

		/** client type definitions */
		int CLIENT_TYPE_DEFAULT = 0;
		int CLIENT_TYPE_MOBILE = 1;
		int CLIENT_TYPE_ANDROID = 2;
	}

	/** Contains presence infomation for contacts. */
	public static final class Presence implements BaseColumns, PresenceColumns {
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/presence");

		/** The content URL for IM presences for an account */
		public static final Uri CONTENT_URI_BY_ACCOUNT = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/presence/account");

		/** The content:// style URL for operations on bulk contacts */
		public static final Uri BULK_CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/bulk_presence");

		/**
		 * The content:// style URL for seeding presences for a given account
		 * id.
		 */
		public static final Uri SEED_PRESENCE_BY_ACCOUNT_CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/seed_presence/account");

		/**
		 * The MIME type of a {@link #CONTENT_URI} providing a directory of
		 * presence
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-presence";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "mode DESC";
	}

	/** Columns from the Chats table. */
	public interface ChatsColumns {
		/**
		 * The contact ID this chat belongs to. The value is a long. <P>Type:
		 * INT</P>
		 */
		String CONTACT_ID = "contact_id";

		/** The GTalk JID resource. The value is a string. <P>Type: TEXT</P> */
		String JID_RESOURCE = "jid_resource";

		/** Whether this is a groupchat or not. <P>Type: INT</P> */
		String GROUP_CHAT = "groupchat";

		/**
		 * The last unread message. This both indicates that there is an unread
		 * message, and what the message is. <P>Type: TEXT</P>
		 */
		String LAST_UNREAD_MESSAGE = "last_unread_message";

		String CHAT_ROOM_BACKGROUND = "chat_room_background";

		/** The last message timestamp <P>Type: INT</P> */
		String LAST_MESSAGE_DATE = "last_message_date";

		/** the state that show notification settings. */
		String NOTIFICATION = "notification";

		int    notification_set = 0;
		int    notification_not_set = 1;

		int SINGLE_CHAT = 0;
		int GROUP_LIVE = 1;
		int GROUP_LEAVE = 2;

		/**
		 * A message that is being composed. This indicates that there was a
		 * message being composed when the chat screen was shutdown, and what
		 * the message is. <P>Type: TEXT</P>
		 */
		String UNSENT_COMPOSED_MESSAGE = "unsent_composed_message";

		/**
		 * A value from 0-9 indicating which quick-switch chat screen slot this
		 * chat is occupying. If none (for instance, this is the 12th active
		 * chat) then the value is -1. <P>Type: INT</P>
		 */
		String SHORTCUT = "shortcut";
	}

	/** Contains ongoing chat sessions. */
	public static final class Chats implements BaseColumns, ChatsColumns {
		/** no public constructor since this is a utility class */
		private Chats() {
		}

		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/chats");

		/** The content URL for all chats that belong to the account */
		public static final Uri CONTENT_URI_BY_ACCOUNT = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/chats/account");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of chats.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-chats";

		/**
		 * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
		 * chat.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-chats";

		/** The default sort order for this table */
		public static final String DEFAULT_SORT_ORDER = "last_message_date ASC";

		public static void deleteAll(ContentResolver resolver)
		{
			resolver.delete(CONTENT_URI, null, null);
		}

		public static void updateAllBackground(ContentResolver resolver, String path)
		{
			ContentValues values = new ContentValues(1);
			values.put(Imps.Chats.CHAT_ROOM_BACKGROUND, path);
			resolver.update(CONTENT_URI, values, null, null);
		}

		public static void deleteChat (ContentResolver resolver, long contactId)
		{
			DebugConfig.info("deleteChat", "contactId:" + contactId);
			Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, contactId);
			Uri messageUri = ContentUris.withAppendedId(Imps.Messages.CONTENT_URI, contactId);
			resolver.delete(chatUri,null,null);
			resolver.delete(messageUri,null,null);
		}
	}

	/** Columns from session cookies table. Used for IMPS. */
	public interface SessionCookiesColumns {
		String NAME = "name";
		String VALUE = "value";
		String PROVIDER = "provider";
		String ACCOUNT = "account";
	}

	/** Contains IMPS session cookies. */
	public static class SessionCookies implements SessionCookiesColumns, BaseColumns {
		private SessionCookies() {
		}

		/** The content:// style URI for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/sessionCookies");

		/** The content:// style URL for session cookies by provider and account */
		public static final Uri CONTENT_URI_SESSION_COOKIES_BY = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/sessionCookiesBy");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * people.
		 */
		public static final String CONTENT_TYPE = "vnd.android-dir/imps-sessionCookies";
	}
	public interface Params {
		String ID			= "id";
		String NAME			= "Name";
		String APIKEY		= "apikey";
		String PREFIX		= "prefix";
		String PHONE		= "phone";
		String IMEI			= "imei";
		String IMSI			= "imsi";
		String BIRTHNUMBER	= "birthnumber";
		String REGKEYFILE	= "registerkey";
		String LOGINKEYFILE	= "loginkey";
		String CODE			= "code";
		String ERRCODE		= "errcode";
		String USERNAME		= "username";
		String NICKNAME		= "nickname";
		String REGION		= "region";
		String USERID		= "userid";
		String STATUS		= "status";
		String HASH			= "hash";
		String PASSWORD		= "password";
		String OLDPASSWORD	= "oldpassword";
		String TYPE 		= "type";
		String RESULT		= "result";
		String RECORDS		= "records";
		String RECORD		= "record";
		String DOWNLOADPATH = "downloadpath";
		String CONTACTS		= "contacts";
		String LANG			= "lang";
		String GENDER		= "gender";
		String SUCCESS		= "success";
		String KIND			= "kind";
		String ALIAS			= "alias";
		String MOBILES		= "mobiles";
		String DESCRIPTION		= "description";
		String DATA		= "data";
		String MIMETYPE		= "mimetype";
		String FILESIZE		= "filesize";
		String DEVICENUM	= "devicenum";
	}
	public interface ProfileColumns {
		String DELETE_OLD_MESSAGES = "delete_old_messages";
		String CHAT_LIMIT= "chat_limit";
		String NEW_MESSAGE_ALERT = "new_message_alert";
		String VIDEO_CALL_NOTIFICATION = "video_call_notification";
		String CALL_RINGTONE = "call_ringtone";
		String NOTIFICATION_CENTER = "notification_center";
		String SOUND = "sound";
		String ALERT_SOUND = "alert_sound";
		String VIBRATE = "vibrate";
		String DO_NOT_DISTURB = "do_not_disturb";
		String START = "start";
		String END = "end";
		String TURN_OFF_SPEAKER = "turn_off_speaker";
		String PRESS_ENTER_TO_SEND = "press_enter_to_send";
		String FIND_MOBILE_CONTACTS = "find_mobile_contacts";
		String METHODS_arirangchatID = "methods_arirangchatid";
		String METHODS_PHONENUMBER = "methods_phonenumber";
		String METHODS_GROUPCHAT = "methods_groupchat";
		String METHODS_QRCODE = "methods_qrcode";
		String METHODS_CONTACTCARD = "methods_contactcard";
		String LANDSCAPE_DISPLAY = "landscape_display";
		String ENABLE_NFC = "enable_nfc";
		String AUTO_UPDATE = "auto_update";
		String TEXT_SIZE = "text_size";
		String FEATURES_PEOPLENEARBY = "features_peoplenearby";
		String NICKNAME = "nickname";
		String GENDER = "gender";
		String PUBLICID = "publicid";
		String PHONE = "phone";
		String REGION = "region";
		String HASH = "hash";
		String USERID = "userid";
		String EMAIL = "email";
		String EMAIL_PASSWORD = "email_password";
		String EMAIL_LOGINED = "email_logined";
		String ALLOW_ADD_ME = "allow_add_me";
		String IS_DELIVERED = "is_delivered";
	}
	public static class Profile implements ProfileColumns{
		private Profile () {

		}
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/profile");
		public static boolean isDoNotDisturb(ContentResolver resolver)
		{
			if ( getProfileBoolean(resolver, DO_NOT_DISTURB) )
			{
				int start = getProfileInt(resolver, START) * 60;
				int end = getProfileInt(resolver, END) * 60;
				Calendar now = Calendar.getInstance();
				int nowseconds = ( now.getTime().getHours() * 60 + now.getTime().getMinutes() ) * 60 + now.getTime().getSeconds();

				if ( nowseconds >= start || nowseconds <= end )
					return true;
			}

			return false;
		}

		public static boolean getProfileBoolean(ContentResolver resolver, String field) {
			int res = 0;

			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			Cursor c = null;
			try {
				c = resolver.query(CONTENT_URI, new String[] {field}, select, selectionArgs, null);
				if (c != null && c.moveToFirst() ) {
					res = c.getInt(0);
				}
			}catch(Exception e) {
				e.printStackTrace();
			} finally {
				if ( c != null ) {
					c.close();
				}
				c = null;
			}
			return res==1?true:false;
		}
		public static int getProfileInt(ContentResolver resolver, String field) {
			int res = 0;

			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			Cursor c = null;
			try {
				c = resolver.query(CONTENT_URI, new String[] {field}, select, selectionArgs, null);
				if (c != null && c.moveToFirst() ) {
					res = c.getInt(0);
				}
			}catch(Exception e) {
				e.printStackTrace();
			} finally {
				if ( c != null ) {
					c.close();
				}
				c = null;
			}
			return res;
		}
		public static String getProfileString(ContentResolver resolver, String field) {
			String res = "";

			String select = Imps.Profile.PUBLICID + "=?";
			String[] selectionArgs = {mAccountID};

			Cursor c = null;
			try {
				c = resolver.query(CONTENT_URI, new String[] {field}, select, selectionArgs, null);
				if (c != null && c.moveToFirst() ) {
					res = c.getString(0);
				}
			}catch(Exception e) {
				e.printStackTrace();
			} finally {
				if ( c != null ) {
					c.close();
				}
				c = null;
			}
			return res;
		}
		public static int setProfileDefaultValues(ContentResolver resolver) {
			if (resolver == null)
				return 0;
			int res = 0;
			ContentValues values = new ContentValues();
			try{
				String select = Profile.PUBLICID + "=?";
				String[] selectionArgs = {mAccountID};

				values.put(NEW_MESSAGE_ALERT, 1);
				values.put(VIDEO_CALL_NOTIFICATION, 1);
				values.put(CALL_RINGTONE, 1);
				values.put(SOUND, 1);
				values.put(NOTIFICATION_CENTER, 1);
				values.put(VIBRATE, 1);
				values.put(ALERT_SOUND, 0);
				values.put(ALERT_SOUND, 0);
				values.put(DO_NOT_DISTURB, 0);
				values.put(START, 23*60);
				values.put(END, 8*60);
				values.put(TURN_OFF_SPEAKER, 0);
				values.put(PRESS_ENTER_TO_SEND, 0);
				values.put(FIND_MOBILE_CONTACTS, 0);
				values.put(METHODS_arirangchatID, 1);
				values.put(METHODS_PHONENUMBER, 1);
				values.put(METHODS_GROUPCHAT, 0);
				values.put(METHODS_QRCODE, 1);
				values.put(METHODS_CONTACTCARD, 1);
				values.put(LANDSCAPE_DISPLAY, 0);
				values.put(ENABLE_NFC, 0);
				values.put(AUTO_UPDATE, 0);
				values.put(TEXT_SIZE, 3);
				values.put(FEATURES_PEOPLENEARBY, 1);
				values.put(DELETE_OLD_MESSAGES, 0);
				values.put(CHAT_LIMIT, 100);

				res = resolver.update(CONTENT_URI, values, select, selectionArgs);
			}catch(Exception e){
				e.printStackTrace();
			}
			return res;
		}
		public static int setProfileBoolean(ContentResolver resolver, String field, boolean value) {
			if (resolver == null)
				return 0;
			int res = 0;
			ContentValues values = new ContentValues();
			try{
				String select = Profile.PUBLICID + "=?";
				String[] selectionArgs = {mAccountID};
				values.put(field, value?1:0);
				res = resolver.update(CONTENT_URI, values, select, selectionArgs);
			}catch(Exception e){
				e.printStackTrace();
			}
			return res;
		}
		public static int setProfileInt(ContentResolver resolver, String field, int value) {
			if (resolver == null)
				return 0;
			int res = 0;
			ContentValues values = new ContentValues();
			try{
				String select = Profile.PUBLICID + "=?";
				String[] selectionArgs = {mAccountID};
				values.put(field, value);
				res = resolver.update(CONTENT_URI, values, select, selectionArgs);
			}catch(Exception e){
				e.printStackTrace();
			}
			return res;
		}
		public static int setProfileString(ContentResolver resolver, String field, String value) {
			if (resolver == null)
				return 0;
			int res = 0;
			ContentValues values = new ContentValues();
			try{
				String select = Profile.PUBLICID + "=?";
				String[] selectionArgs = {mAccountID};
				values.put(field, value);
				res = resolver.update(CONTENT_URI, values, select, selectionArgs);
			}catch(Exception e){
				e.printStackTrace();
			}
			return res;
		}
	}
/*	public static interface GroupProfileColumns {
		String GROUPID = "id";
		String GROUPNAME = "groupName";
		String USERS = "users";
		String GMARK = "gmark";
		String HASH = "hash";
		String AVATAR = "avatar";
	}
	public static class GroupProfile implements GroupProfileColumns{
		private GroupProfile () {

		}
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/groupProfile");
	}
	
*/	public interface NotificationsColumns {
		String _ID = "_id";
		String CATEGORY = "category";
		String FIELD = "field";
		String FIELD_ID = "field_id";
		String NOTIFICATION_COUNT = "nCount";
		String ACCOUNT = "account";
	}
	public static class Notifications implements NotificationsColumns {
		private Notifications () {

		} 
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/notifications");
		public static final String CAT_CHATTING = "chatting";
		public static final String CAT_CONTACTS = "contacts";

		public static final String FIELD_CHAT = "chat";
		public static final String FIELD_GROUPCHAT = "groupchat";
		public static final String FIELD_SETTING = "setting";
		public static final String FIELD_NOTICES = "notices";
		public static final String FIELD_ITEM_STORE = "itemstore";
		public static final String FIELD_REQUEST = "request";
		public static final String FIELD_POSTS = "posts";

		public static final int FIELD_ID_VERSION = 1;
		public static final int FIELD_ID_ITEM = 2;
		public static final int FIELD_ID_HELP = 3;

		public static final int FIELD_ID_ITEM_STORE_BEST = 1;
		public static final int FIELD_ID_ITEM_STORE_THEME = 2;
		public static final int FIELD_ID_ITEM_STORE_STICKER = 3;
		public static final int FIELD_ID_ITEM_STORE_FREE = 4;

		public static final String EVENT = "event";
		public static final String ADMIN_LIST = "adminlist";		
		
		public static int getNotificationCount(ContentResolver resolver,
				String category,
				String field,
				int field_id) {
			int res = 0;
			String where = "";
			boolean andFlag = false;
			if (category != null && !category.equals("")) {
				andFlag = true;
				where = CATEGORY + " = '" + category + "'";
			}
			if (field != null && !field.equals("")) {
				if (andFlag)
					where += " and ";
				where += FIELD + " = '" + field + "'";
			}
			if (field_id > 0 ) {
				if (andFlag)
					where += " and ";
				where += FIELD_ID + " = " + field_id;
			}

			String select = Imps.Notifications.ACCOUNT + "=?";
			String[] selectionArgs = {mAccountID};

			if ( !where.isEmpty() )
				select += " AND " + where;

			Cursor c = null;
			try {
				c = resolver.query(CONTENT_URI, new String[] { "sum(" + NOTIFICATION_COUNT+ ") count" }, select, selectionArgs, null);
				if (c != null && c.moveToFirst() ) {
					res = c.getInt(0);
				} 
			}catch(Exception e) {
				e.printStackTrace();
			} finally {
				if ( c != null ) {
					c.close();
				}
				c = null;
			}
			return res;
		}
		public static int addNotificationCount(ContentResolver resolver,
				String category,
				String field,
				int field_id,int notificationCount) {
			if (resolver == null)
				return 0;
			int res = 0;
			int count = getNotificationCount(resolver, category, field, field_id);
			ContentValues values = new ContentValues();

			if (count > 0) {
				String where = "";
				boolean andFlag = false;
				if (category != null && !category.equals("")) {
					andFlag = true;
					where = CATEGORY + " = '" + category + "'";
				}
				if (field != null && !field.equals("")) {
					if (andFlag)
						where += " and ";
					where += FIELD + " = '" + field + "'";
				}
				if (field_id > 0 ) {
					if (andFlag)
						where += " and ";
					where += FIELD_ID + " = " + field_id;
				}

				try{
					String select = Imps.Notifications.ACCOUNT + "=?";
					String[] selectionArgs = {mAccountID};

					if ( !where.isEmpty() )
						select += " AND " + where;

					if (notificationCount + count > 0) {
						values.put(NOTIFICATION_COUNT, notificationCount + count);
						res = resolver.update(CONTENT_URI, values, select, selectionArgs);
					} else {
						res = resolver.delete(CONTENT_URI, select, selectionArgs);
					}
				}catch(Exception e){
					e.printStackTrace();
				}

			} else {
				if (notificationCount <= 0)
					return 0;
				values.put(CATEGORY, category);
				values.put(FIELD, field);
				values.put(FIELD_ID, field_id);
				values.put(NOTIFICATION_COUNT, notificationCount);
				values.put(ACCOUNT, mAccountID);

				try{
					if (resolver.insert(CONTENT_URI, values) != null)
						res = 1;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			return res;
		}

		public static int removeNotificationCount(ContentResolver resolver,
				String category,
				String field,
				int field_id) {
			int res = 0;
			int count = getNotificationCount(resolver, category, field, field_id);

			if (count > 0) {
				String where = "";
				boolean andFlag = false;
				if (category != null && !category.equals("")) {
					andFlag = true;
					where = CATEGORY + " = '" + category + "'";
				}
				if (field != null && !field.equals("")) {
					if (andFlag)
						where += " and ";
					where += FIELD + " = '" + field + "'";
				}
				if (field_id > 0 ) {
					if (andFlag)
						where += " and ";
					where += FIELD_ID + " = " + field_id;
				}
				String select = Imps.Notifications.ACCOUNT + "=?";
				String[] selectionArgs = {mAccountID};

				if ( !where.isEmpty() )
					select += " AND " + where;
				res = resolver.delete(CONTENT_URI, select, selectionArgs);
			} 
			return res;
		}

		public static void removeChatNotificationIfNeed(ContentResolver resolver) throws Exception{

			Cursor cursor =  null;
			int[]	idList = null;
			try{
				String select = Imps.Notifications.ACCOUNT + "=? AND " + CATEGORY + "='" + CAT_CHATTING + "' And " + FIELD + "='" + FIELD_CHAT + "'";
				String[] selectionArgs = {mAccountID};

				cursor = resolver.query(CONTENT_URI, new String[]{FIELD_ID}, select, selectionArgs, null);
				if ( cursor != null ) {
					int count = cursor.getCount();
					if ( count > 0 ) {
						idList = new int[count];
					}
					int index = 0;
					while(cursor.moveToNext()) {
						idList[index] = cursor.getInt(0);
						index ++;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if ( cursor != null ) { 
					cursor.close();
					cursor = null;
				}
			}

			if ( idList != null && idList.length > 0 ) {
				for(int i =0; i < idList.length; i ++ ) {
					if ( idList[i] > 0 ) {
						Uri baseUri = Imps.Contacts.CONTENT_URI_CHAT_CONTACTS;
						Uri.Builder builder = baseUri.buildUpon();
						try{
							cursor = resolver.query(builder.build(),
									new String[]{Imps.Contacts.USERNAME}, 
									"contacts." + Imps.Contacts._ID + "='" + Long.toString(idList[i]) + "'",
									null, 
									null);
							if ( cursor == null || !cursor.moveToNext() ) {
								removeNotificationCount(resolver, CAT_CHATTING, FIELD_CHAT, idList[i]);
							}
						}catch(Exception e){
							e.printStackTrace();
						}finally{
							if ( cursor != null ) {
								cursor.close();
								cursor = null;
							}
						}
					}
				}
			}

		}
	}
	/** Columns from ProviderSettings table */
	public interface ProviderSettingsColumns {
		/**
		 * The id in database of the related provider
		 * 
		 * <P>Type: INT</P>
		 */
		String PROVIDER = "provider";

		/** The name of the setting <P>Type: TEXT</P> */
		String NAME = "name";

		/** The value of the setting <P>Type: TEXT</P> */
		String VALUE = "value";
	}

	public static class ProviderSettings implements ProviderSettingsColumns {
		// Global settings are saved with this provider ID, for backward compatibility

		public static final long PROVIDER_ID_FOR_GLOBAL_SETTINGS = 1;

		private ProviderSettings() {
		}

		/** The content:// style URI for this table */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.multimediachat.app.im.provider.Imps/providerSettings");

		/** The MIME type of {@link #CONTENT_URI} providing provider settings */
		public static final String CONTENT_TYPE = "vnd.android-dir/imps-providerSettings";

		// ACCOUNT SETTINGS (username and password are part of Account above)

		// TODO since everything is here except username/password, perhaps it should also be moved here?

		/** the domain name of the account, i.e. @gmail.com for XMPP */
		public static final String DOMAIN = "pref_account_domain";

		/** The XMPP Resource string */
		public static final String XMPP_RESOURCE = "pref_account_xmpp_resource";

		/** The XMPP Resource priority string */
		public static final String XMPP_RESOURCE_PRIO = "pref_account_xmpp_resource_prio";

		/** the port number to connect to */
		public static final String PORT = "pref_account_port";

		/** The hostname or IP of the server to connect to */
		public static final String SERVER = "pref_account_server";

		// ENCRYPTION AND ANONYMITY SETTINGS

		/** allow plain text authentication */
		public static final String ALLOW_PLAIN_AUTH = "pref_security_allow_plain_auth";

		/** boolean for the required use of a TLS connection */
		public static final String REQUIRE_TLS = "pref_security_require_tls";

		/** boolean for whether the TLS certificate should be verified */
		public static final String TLS_CERT_VERIFY = "pref_security_tls_cert_verify";

		/**
		 * Global setting controlling how OTR engine initiates: auto, force,
		 * requested, disabled
		 */
		public static final String OTR_MODE = "pref_security_otr_mode";

		/** boolean to specify whether to use Tor proxying or not */
		public static final String USE_TOR = "pref_security_use_tor";

		/**
		 * boolean to control whether DNS SRV lookups are used to find the
		 * server
		 */
		public static final String DO_DNS_SRV = "pref_security_do_dns_srv";

		// GENERAL PREFERENCES

		/** controls whether this provider should show the offline contacts */
		public static final String SHOW_OFFLINE_CONTACTS = "show_offline_contacts";

		/** controls whether the GTalk service automatically connect to server. */
		public static final String AUTOMATICALLY_CONNECT_GTALK = "gtalk_auto_connect";

		/**
		 * controls whether the IM service will be automatically started after
		 * boot
		 */
		public static final String AUTOMATICALLY_START_SERVICE = "auto_start_service";

		/**
		 * Global setting which controls whether the offline contacts will be
		 * hid.
		 */
		public static final String HIDE_OFFLINE_CONTACTS = "hide_offline_contacts";

		/** Global setting which controls whether enable the IM notification */
		public static final String ENABLE_NOTIFICATION = "enable_notification";
		public static final String ENABLE_SENOTIFICATION = "enable_senotification";

		/** Global setting which specifies whether to vibrate */
		public static final String NOTIFICATION_VIBRATE = "vibrate";

		/** Global setting which specifies the Uri string of the ringtone */
		public static final String NOTIFICATION_RINGTONE = "ringtone";

		/** Global setting which specifies the Uri of the default ringtone */
		public static final String RINGTONE_DEFAULT = "content://settings/system/notification_sound";

		/** specifies whether to show mobile indicator to friends */
		public static final String SHOW_MOBILE_INDICATOR = "mobile_indicator";

		/** specifies whether to show as away when device is idle */
		public static final String SHOW_AWAY_ON_IDLE = "show_away_on_idle";

		/** controls whether the service gets foreground priority */
		public static final String USE_FOREGROUND_PRIORITY = "use_foreground_priority";

		/** specifies whether to upload heartbeat stat upon login */
		public static final String UPLOAD_HEARTBEAT_STAT = "upload_heartbeat_stat";

		/** specifies the last heartbeat interval received from the server */
		public static final String HEARTBEAT_INTERVAL = "heartbeat_interval";

		/** specifiy the JID resource used for Google Talk connection */
		public static final String JID_RESOURCE = "jid_resource";

		/**
		 * Used for reliable message queue (RMQ). This is for storing the last
		 * rmq id received from the GTalk server
		 */
		public static final String LAST_RMQ_RECEIVED = "last_rmq_rec";

		/**
		 * use for status persistence
		 */
		public static final String PRESENCE_STATE = "presence_state";
		public static final String PRESENCE_STATUS_MESSAGE = "presence_status_message";


		/**
		 * Query the settings of the provider specified by id
		 * 
		 * @param cr the relative content resolver
		 * @param providerId the specified id of provider
		 * @return a HashMap which contains all the settings for the specified
		 *         provider
		 */
		public static HashMap<String, String> queryProviderSettings(ContentResolver cr,
				long providerId) {
			HashMap<String, String> settings = new HashMap<String, String>();

			String[] projection = { NAME, VALUE };
			Cursor c = null;
			try{
				c = cr.query(ContentUris.withAppendedId(CONTENT_URI, providerId), projection,
						null, null, null);
				if (c == null) {
					return null;
				}
				while (c.moveToNext()) {
					settings.put(c.getString(0), c.getString(1));
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if ( c != null ) {
					c.close();
					c = null;
				}
			}
			return settings;
		}

		/**
		 * Get the string value of setting which is specified by provider id and
		 * the setting name.
		 * 
		 * @param cr The ContentResolver to use to access the settings table.
		 * @param providerId The id of the provider.
		 * @param settingName The name of the setting.
		 * @return The value of the setting if the setting exist, otherwise
		 *         return null.
		 */
		public static String getStringValue(ContentResolver cr, long providerId, String settingName) {
			String ret = null;
			Cursor c = getSettingValue(cr, providerId, settingName);
			if (c != null) {
				ret = c.getString(0);
				c.close();
			}

			return ret;
		}

		/**
		 * Get the string value of setting which is specified by provider id and
		 * the setting name.
		 * 
		 * @param cr The ContentResolver to use to access the settings table.
		 * @param providerId The id of the provider.
		 * @param settingName The name of the setting.
		 * @return The value of the setting if the setting exist, otherwise
		 *         return null.
		 */
		public static int getIntValue(ContentResolver cr, long providerId, String settingName) {
			int ret = -1;

			Cursor c = getSettingValue(cr, providerId, settingName);
			if (c != null) {
				ret = c.getInt(0);
				c.close();
			}

			return ret;
		}

		public static long getLongValue(ContentResolver cr, long providerId, String settingName) {
			long ret = -1;

			Cursor c = getSettingValue(cr, providerId, settingName);
			if (c != null) {
				ret = c.getLong(0);
				c.close();
			}

			return ret;
		}

		/**
		 * Get the boolean value of setting which is specified by provider id
		 * and the setting name.
		 * 
		 * @param cr The ContentResolver to use to access the settings table.
		 * @param providerId The id of the provider.
		 * @param settingName The name of the setting.
		 * @return The value of the setting if the setting exist, otherwise
		 *         return false.
		 */
		public static boolean getBooleanValue(ContentResolver cr, long providerId,
				String settingName) {
			boolean ret = false;
			Cursor c = getSettingValue(cr, providerId, settingName);
			if (c != null) {
				String str = c.getString(0);
				ret = (str.equals("true"));
				c.close();
			}
			return ret;
		}

		public static float getFloatValue(ContentResolver cr, long providerId, String settingName) {
			float ret = 0;
			Cursor c = getSettingValue(cr, providerId, settingName);
			if (c != null) {
				ret = c.getFloat(0);
				c.close();
			}
			return ret;
		}

		private static Cursor getSettingValue(ContentResolver cr, long providerId,
				String settingName) {
			Cursor c = null;

			c = cr.query(ContentUris.withAppendedId(CONTENT_URI, providerId),
					new String[] { VALUE }, NAME + "=?", new String[] { settingName }, null);
			if (c != null) {
				if (!c.moveToFirst()) {
					c.close();
					return null;
				}
			}
			return c;
		}

		/**
		 * Save a long value of setting in the table providerSetting.
		 * 
		 * @param cr The ContentProvider used to access the providerSetting
		 *            table.
		 * @param providerId The id of the provider.
		 * @param name The name of the setting.
		 * @param value The value of the setting.
		 */
		public static void putLongValue(ContentResolver cr, long providerId, String name, long value) throws Exception{
			ContentValues v = new ContentValues(3);
			v.put(PROVIDER, providerId);
			v.put(NAME, name);
			v.put(VALUE, value);

			cr.insert(CONTENT_URI, v);
		}

		public static void putFloatValue(ContentResolver cr, float providerId, String name, float value) {
			ContentValues v = new ContentValues(3);
			v.put(PROVIDER, providerId);
			v.put(NAME, name);
			v.put(VALUE, value);

			cr.insert(CONTENT_URI, v);
		}

		/**
		 * put null value in the table providerSetting.
		 * 
		 */
		public static void putNullValue(ContentResolver cr, long providerId, String name) {
			ContentValues v = new ContentValues(3);
			v.put(PROVIDER, providerId);
			v.put(NAME, name);
			v.putNull(VALUE);
			cr.insert(CONTENT_URI, v);
		}

		/**
		 * Save a long value of setting in the table providerSetting.
		 * 
		 * @param cr The ContentProvider used to access the providerSetting
		 *            table.
		 * @param providerId The id of the provider.
		 * @param name The name of the setting.
		 * @param value The value of the setting.
		 */
		public static void putIntValue(ContentResolver cr, long providerId, String name, int value) {

			if ( name == null )
				return;

			ContentValues v = new ContentValues(3);
			v.put(PROVIDER, providerId);
			v.put(NAME, name);
			v.put(VALUE, value);

			cr.insert(CONTENT_URI, v);
		}

		/**
		 * Save a boolean value of setting in the table providerSetting.
		 * 
		 * @param cr The ContentProvider used to access the providerSetting
		 *            table.
		 * @param providerId The id of the provider.
		 * @param name The name of the setting.
		 * @param value The value of the setting.
		 */
		public static void putBooleanValue(ContentResolver cr, long providerId, String name,
				boolean value) {
			ContentValues v = new ContentValues(3);
			v.put(PROVIDER, providerId);
			v.put(NAME, name);
			v.put(VALUE, Boolean.toString(value));

			cr.insert(CONTENT_URI, v);
		}

		/**
		 * Save a string value of setting in the table providerSetting.
		 * 
		 * @param cr The ContentProvider used to access the providerSetting
		 *            table.
		 * @param providerId The id of the provider.
		 * @param name The name of the setting.
		 * @param value The value of the setting.
		 */
		public static void putStringValue(ContentResolver cr, long providerId, String name,
				String value) throws Exception{
			ContentValues v = new ContentValues(3);
			v.put(PROVIDER, providerId);
			v.put(NAME, name);
			v.put(VALUE, value);

			cr.insert(CONTENT_URI, v);


		}

		/**
		 * A convenience method to set the domain name affiliated with an
		 * account
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param domain The domain name to use for the account
		 */
		public static void setDomain(ContentResolver cr, long providerId, String domain) {
			try{
				putStringValue(cr, providerId, DOMAIN, domain);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/**
		 * A convenience method to set the XMPP Resource string
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param xmppResource the XMPP Resource string
		 */
		public static void setXmppResource(ContentResolver cr, long providerId, String xmppResource) {
			try{
				putStringValue(cr, providerId, XMPP_RESOURCE, xmppResource);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/**
		 * A convenience method to set the XMPP Resource priority
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param priority the XMPP Resource priority
		 */
		public static void setXmppResourcePrio(ContentResolver cr, long providerId, int priority) {
			try{
				putLongValue(cr, providerId, XMPP_RESOURCE_PRIO, (long) priority);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/**
		 * A convenience method to set the TCP/IP port number to connect to
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param port the TCP/IP port number to connect to
		 */
		public static void setPort(ContentResolver cr, long providerId, int port) {
			try{
				putLongValue(cr, providerId, PORT, (long) port);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/**
		 * A convenience method to set the hostname or IP of the server to
		 * connect to
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param server the hostname or IP of the server to connect to
		 */
		public static void setServer(ContentResolver cr, long providerId, String server) {
			try{
				putStringValue(cr, providerId, SERVER, server);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/**
		 * A convenience method to set whether to allow plain text auth
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param allowPlainAuth
		 */
		public static void setAllowPlainAuth(ContentResolver cr, long providerId,
				boolean allowPlainAuth) {
			putBooleanValue(cr, providerId, ALLOW_PLAIN_AUTH, allowPlainAuth);
		}

		/**
		 * A convenience method to set whether to require TLS
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param requireTls
		 */
		public static void setRequireTls(ContentResolver cr, long providerId, boolean requireTls) {
			putBooleanValue(cr, providerId, REQUIRE_TLS, requireTls);
		}

		/**
		 * A convenience method to set whether to verify the TLS cert
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param tlsCertVerify
		 */
		public static void setTlsCertVerify(ContentResolver cr, long providerId,
				boolean tlsCertVerify) {
			putBooleanValue(cr, providerId, TLS_CERT_VERIFY, tlsCertVerify);
		}

		/**
		 * A convenience method to set the mode of operation for the OTR Engine
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param otrMode OTR Engine mode (force, auto, requested, disabled)
		 */
		public static void setOtrMode(ContentResolver cr, long providerId, String otrMode) {
			try{
				putStringValue(cr, providerId, OTR_MODE, otrMode);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/**
		 * A convenience method to set whether to use Tor
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param useTor
		 */
		public static void setUseTor(ContentResolver cr, long providerId, boolean useTor) {
			putBooleanValue(cr, providerId, USE_TOR, useTor);
		}

		/**
		 * A convenience method to set whether to use DNS SRV lookups to find
		 * the server
		 * 
		 * @param cr The ContentResolver to use to access the settings table
		 * @param providerId used to identify the set of settings for a given
		 *            provider
		 * @param doDnsSrv
		 */
		public static void setDoDnsSrv(ContentResolver cr, long providerId, boolean doDnsSrv) {
			putBooleanValue(cr, providerId, DO_DNS_SRV, doDnsSrv);
		}

		/**
		 * A convenience method to set whether or not the GTalk service should
		 * be started automatically.
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            settings table
		 * @param autoConnect Whether the GTalk service should be started
		 *            automatically.
		 */
		public static void setAutomaticallyConnectGTalk(ContentResolver contentResolver,
				long providerId, boolean autoConnect) {
			putBooleanValue(contentResolver, providerId, AUTOMATICALLY_CONNECT_GTALK, autoConnect);
		}

		/**
		 * A convenience method to set whether or not the offline contacts
		 * should be hided
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table
		 * @param hideOfflineContacts Whether the offline contacts should be
		 *            hided
		 */
		public static void setHideOfflineContacts(ContentResolver contentResolver, long providerId,
				boolean hideOfflineContacts) {
			putBooleanValue(contentResolver, providerId, HIDE_OFFLINE_CONTACTS, hideOfflineContacts);
		}

		public static void setUseForegroundPriority(ContentResolver contentResolver,
				long providerId, boolean flag) {
			putBooleanValue(contentResolver, providerId, USE_FOREGROUND_PRIORITY, flag);
		}

		/**
		 * A convenience method to set whether or not enable the IM
		 * notification.
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table.
		 * @param enable Whether enable the IM notification
		 */
		public static void setEnableNotification(ContentResolver contentResolver, long providerId,
				boolean enable) {
			putBooleanValue(contentResolver, providerId, ENABLE_NOTIFICATION, enable);
		}
		
	/*	public static void setEnableSENotification(ContentResolver contentResolver, long providerId,
				boolean enable) {
			putBooleanValue(contentResolver, providerId, ENABLE_SENOTIFICATION, enable);
		}
*/
		/**
		 * A convenience method to set whether or not to vibrate.
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table.
		 * @param vibrate Whether or not to vibrate
		 */
		public static void setVibrate(ContentResolver contentResolver, long providerId,
				boolean vibrate) {
			putBooleanValue(contentResolver, providerId, NOTIFICATION_VIBRATE, vibrate);
		}

		/**
		 * A convenience method to set the Uri String of the ringtone.
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table.
		 * @param ringtoneUri The Uri String of the ringtone to be set.
		 */
		public static void setRingtoneURI(ContentResolver contentResolver, long providerId,
				String ringtoneUri) {
			try{
				putStringValue(contentResolver, providerId, NOTIFICATION_RINGTONE, ringtoneUri);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/**
		 * A convenience method to set whether or not to show mobile indicator.
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table.
		 * @param showMobileIndicator Whether or not to show mobile indicator.
		 */
		public static void setShowMobileIndicator(ContentResolver contentResolver, long providerId,
				boolean showMobileIndicator) {
			putBooleanValue(contentResolver, providerId, SHOW_MOBILE_INDICATOR, showMobileIndicator);
		}

		/**
		 * A convenience method to set whether or not to show as away when
		 * device is idle.
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table.
		 * @param showAway Whether or not to show as away when device is idle.
		 */
		public static void setShowAwayOnIdle(ContentResolver contentResolver, long providerId,
				boolean showAway) {
			putBooleanValue(contentResolver, providerId, SHOW_AWAY_ON_IDLE, showAway);
		}

		/**
		 * A convenience method to set whether or not to upload heartbeat stat.
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table.
		 * @param uploadStat Whether or not to upload heartbeat stat.
		 */
		public static void setUploadHeartbeatStat(ContentResolver contentResolver, long providerId,
				boolean uploadStat) {
			putBooleanValue(contentResolver, providerId, UPLOAD_HEARTBEAT_STAT, uploadStat);
		}

		/**
		 * A convenience method to set the heartbeat interval last received from
		 * the server.
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table.
		 * @param interval The heartbeat interval last received from the server.
		 */
		public static void setHeartbeatInterval(ContentResolver contentResolver, long providerId,
				long interval) {
			
			try{
				putLongValue(contentResolver, providerId, HEARTBEAT_INTERVAL, interval);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/**
		 * A convenience method to user configure presence state and status
		 * 
		 * @param contentResolver The ContentResolver to use to access the
		 *            setting table.
		 * @param interval The heartbeat interval last received from the server.
		 */
		public static void setPresence(ContentResolver contentResolver, long providerId,
				int state, String statusMessage) {

			if (state != -1)
				putIntValue(contentResolver, providerId, PRESENCE_STATE, state);

			if (statusMessage != null) {
				try{
					putStringValue(contentResolver, providerId, PRESENCE_STATUS_MESSAGE, statusMessage);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}




		/** A convenience method to set the jid resource. */
		public static void setJidResource(ContentResolver contentResolver, long providerId,
				String jidResource) {
			
			try{
				putStringValue(contentResolver, providerId, JID_RESOURCE, jidResource);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		public static class QueryMap extends ContentQueryMap {
			private ContentResolver mContentResolver;
			private long mProviderId;
			private Exception mStacktrace;

			/*
            public QueryMap(ContentResolver contentResolver, boolean keepUpdated,
                    Handler handlerForUpdateNotifications) {
                this(contentResolver, ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS,
                        keepUpdated, handlerForUpdateNotifications);
            }*/

			//contentResolver.query(CONTENT_URI,new String[] {NAME, VALUE},PROVIDER + "=?",new String[] { Long.toString(providerId)},null)

			public QueryMap(Cursor cursor, ContentResolver contentResolver, long providerId, boolean keepUpdated,
					Handler handlerForUpdateNotifications) {

				super(cursor, // no sort order
						NAME, keepUpdated, handlerForUpdateNotifications);

				mContentResolver = contentResolver;
				mProviderId = providerId;
				mStacktrace = new Exception();
			}
			@Override
			public synchronized void close() {
				mStacktrace = null;
				super.close();
			}

			@Override
			protected void finalize() throws Throwable {
				if (mStacktrace != null) {
					DebugConfig.warn("GB.Imps", "QueryMap cursor not closed before finalize", mStacktrace);
				}
				super.finalize();
			}
			/**
			 * Set if the GTalk service should automatically connect to server.
			 * 
			 * @param autoConnect if the GTalk service should auto connect to
			 *            server.
			 */
			public void setAutomaticallyConnectToGTalkServer(boolean autoConnect) {
				ProviderSettings.setAutomaticallyConnectGTalk(mContentResolver, mProviderId,
						autoConnect);
			}

			/**
			 * Check if the GTalk service should automatically connect to
			 * server.
			 * 
			 * @return if the GTalk service should automatically connect to
			 *         server.
			 */
			public boolean getAutomaticallyConnectToGTalkServer() {
				return getBoolean(AUTOMATICALLY_CONNECT_GTALK, true /* default to automatically sign in */);
			}

			public void setDomain(String domain) {
				ProviderSettings.setDomain(mContentResolver, mProviderId, domain);
			}

			public String getDomain() {
				return getString(DOMAIN, "");
			}

			public void setXmppResource(String resource) {
				ProviderSettings.setXmppResource(mContentResolver, mProviderId, resource);
			}

			public String getXmppResource() {
				return getString(XMPP_RESOURCE, ImApp.DEFAULT_XMPP_RESOURCE);
			}

			public void setXmppResourcePrio(int prio) {
				ProviderSettings.setXmppResourcePrio(mContentResolver, mProviderId, prio);
			}

			public int getXmppResourcePrio() {
				return (int) getLong(XMPP_RESOURCE_PRIO, ImApp.DEFAULT_XMPP_PRIORITY);
			}

			public void setPort(int port) {
				ProviderSettings.setPort(mContentResolver, mProviderId, port);
			}

			public int getPort() {
				return (int) getLong(PORT, 0 /* by default use XMPP's default port */);
			}

			public void setServer(String server) {
				ProviderSettings.setServer(mContentResolver, mProviderId, server);
			}

			public String getServer() {
				return getString(SERVER, "");
			}

			public void setAllowPlainAuth(boolean value) {
				ProviderSettings.setAllowPlainAuth(mContentResolver, mProviderId, value);
			}

			public boolean getAllowPlainAuth() {
				return getBoolean(ALLOW_PLAIN_AUTH, false /* by default do not send passwords in the clear */);
			}

			public void setRequireTls(boolean value) {
				ProviderSettings.setRequireTls(mContentResolver, mProviderId, value);
			}

			public boolean getRequireTls() {
				return getBoolean(REQUIRE_TLS, true /* by default attempt TLS but don't require */);
				//n8fr8 2011/04/20 i think we should require it by default so i set to 'true'
			}

			public void setTlsCertVerify(boolean value) {
				ProviderSettings.setTlsCertVerify(mContentResolver, mProviderId, value);
			}

			public boolean getTlsCertVerify() {
				return getBoolean(TLS_CERT_VERIFY, true /* by default try to verify the TLS Cert */);
			}

			public void setOtrMode(String otrMode) {
				ProviderSettings.setOtrMode(mContentResolver, mProviderId, otrMode);
			}

			public String getOtrMode() {
				return getString(OTR_MODE, ImApp.DEFAULT_XMPP_OTR_MODE /* by default, try to use OTR */);
			}

			public void setUseTor(boolean value) {
				ProviderSettings.setUseTor(mContentResolver, mProviderId, value);
			}

			public boolean getUseTor() {
				return getBoolean(USE_TOR, false /* by default do not use Tor */);
			}

			public void setDoDnsSrv(boolean value) {
				ProviderSettings.setDoDnsSrv(mContentResolver, mProviderId, value);
			}

			public boolean getDoDnsSrv() {
				return getBoolean(DO_DNS_SRV, true /* by default use DNS SRV to find the server */);
			}

			/**
			 * Set whether or not the offline contacts should be hided.
			 * 
			 * @param hideOfflineContacts Whether or not the offline contacts
			 *            should be hided.
			 */
			public void setHideOfflineContacts(boolean hideOfflineContacts) {
				ProviderSettings.setHideOfflineContacts(mContentResolver, mProviderId,
						hideOfflineContacts);
			}

			/**
			 * Check if the offline contacts should be hided.
			 * 
			 * @return Whether or not the offline contacts should be hided.
			 */
			public boolean getHideOfflineContacts() {
				return getBoolean(HIDE_OFFLINE_CONTACTS, false /* default*/);
			}

			public void setUseForegroundPriority(boolean flag) {
				ProviderSettings.setUseForegroundPriority(mContentResolver, mProviderId, flag);
			}

			public boolean getUseForegroundPriority() {
				return getBoolean(USE_FOREGROUND_PRIORITY, false /* default */);
			}

/*
			*/
/**
			 * Set whether or not enable the IM notification.
			 * 
			 * @param enable Whether or not enable the IM notification.
			 *//*

			public void setEnableNotification(boolean enable) {
				ProviderSettings.setEnableNotification(mContentResolver, mProviderId, enable);
			}

	*/
/*		public void setEnableSENotification(boolean enable) {
				ProviderSettings.setEnableSENotification(mContentResolver, mProviderId, enable);
			}
	*//*
		*/
/**
			 * Check if the IM notification is enabled.
			 * 
			 * @return Whether or not enable the IM notification.
			 *//*

			public boolean getEnableNotification() {
				return getBoolean(ENABLE_NOTIFICATION, true*/
/* by default enable the notification *//*
);
			}
*/

	/*		public boolean getEnableSENotification() {
				return getBoolean(ENABLE_SENOTIFICATION, true);
			}
	*/		
/*
			*/
/**
			 * Set whether or not to vibrate on IM notification.
			 * 
			 * @param vibrate Whether or not to vibrate.
			 *//*

			public void setVibrate(boolean vibrate) {
				ProviderSettings.setVibrate(mContentResolver, mProviderId, vibrate);
			}

			*/
/**
			 * Gets whether or not to vibrate on IM notification.
			 * 
			 * @return Whether or not to vibrate.
			 *//*

			public boolean getVibrate() {
				return getBoolean(NOTIFICATION_VIBRATE, true */
/* by default enable vibrate *//*
);
			}

			*/
/**
			 * Set the Uri for the ringtone.
			 * 
			 * @param ringtoneUri The Uri of the ringtone to be set.
			 *//*

			public void setRingtoneURI(String ringtoneUri) {
				ProviderSettings.setRingtoneURI(mContentResolver, mProviderId, ringtoneUri);
			}

			*/
/**
			 * Get the Uri String of the current ringtone.
			 * 
			 * @return The Uri String of the current ringtone.
			 *//*

			public String getRingtoneURI() {
				return getString(NOTIFICATION_RINGTONE, RINGTONE_DEFAULT);
			}
*/

			/**
			 * Set whether or not to show mobile indicator to friends.
			 * 
			 * @param showMobile whether or not to show mobile indicator.
			 */
			public void setShowMobileIndicator(boolean showMobile) {
				ProviderSettings.setShowMobileIndicator(mContentResolver, mProviderId, showMobile);
			}

			/**
			 * Gets whether or not to show mobile indicator.
			 * 
			 * @return Whether or not to show mobile indicator.
			 */
			public boolean getShowMobileIndicator() {
				return getBoolean(SHOW_MOBILE_INDICATOR, true /* by default show mobile indicator */);
			}

			/**
			 * Set whether or not to show as away when device is idle.
			 * 
			 * @param showAway whether or not to show as away when device is
			 *            idle.
			 */
			public void setShowAwayOnIdle(boolean showAway) {
				ProviderSettings.setShowAwayOnIdle(mContentResolver, mProviderId, showAway);
			}

			/**
			 * Get whether or not to show as away when device is idle.
			 * 
			 * @return Whether or not to show as away when device is idle.
			 */
			public boolean getShowAwayOnIdle() {
				return getBoolean(SHOW_AWAY_ON_IDLE, true /* by default show as away on idle*/);
			}

			/**
			 * Set whether or not to upload heartbeat stat.
			 * 
			 * @param uploadStat whether or not to upload heartbeat stat.
			 */
			public void setUploadHeartbeatStat(boolean uploadStat) {
				ProviderSettings.setUploadHeartbeatStat(mContentResolver, mProviderId, uploadStat);
			}

			/**
			 * Get whether or not to upload heartbeat stat.
			 * 
			 * @return Whether or not to upload heartbeat stat.
			 */
			public boolean getUploadHeartbeatStat() {
				return getBoolean(UPLOAD_HEARTBEAT_STAT, false /* by default do not upload */);
			}

			/**
			 * Set the heartbeat interval.
			 */
			public void setHeartbeatInterval(long interval) {
				if (interval <= 0) {
					interval = 1;
				} else if (interval > 99) {
					interval = 99;
				}
				ProviderSettings.setHeartbeatInterval(mContentResolver, mProviderId, interval);
			}

			/**
			 * Get the heartbeat interval, default to 1.
			 */
			public long getHeartbeatInterval() {
				return getLong(HEARTBEAT_INTERVAL, 1);
			}

			/**
			 * Set the JID resource.
			 * 
			 * @param jidResource the jid resource to be stored.
			 */
			public void setJidResource(String jidResource) {
				ProviderSettings.setJidResource(mContentResolver, mProviderId, jidResource);
			}

			/**
			 * Get the JID resource used for the Google Talk connection
			 * 
			 * @return the JID resource stored.
			 */
			public String getJidResource() {
				return getString(JID_RESOURCE, null);
			}

			/**
			 * Convenience function for retrieving a single settings value as a
			 * boolean.
			 * 
			 * @param name The name of the setting to retrieve.
			 * @param def Value to return if the setting is not defined.
			 * @return The setting's current value, or 'def' if it is not
			 *         defined.
			 */
			private boolean getBoolean(String name, boolean def) {
				ContentValues values = getValues(name);
				return values != null ? values.getAsBoolean(VALUE) : def;
			}

			/**
			 * Convenience function for retrieving a single settings value as a
			 * String.
			 * 
			 * @param name The name of the setting to retrieve.
			 * @param def The value to return if the setting is not defined.
			 * @return The setting's current value or 'def' if it is not
			 *         defined.
			 */
			private String getString(String name, String def) {
				ContentValues values = getValues(name);
				return values != null ? values.getAsString(VALUE) : def;
			}

			/**
			 * Convenience function for retrieving a single settings value as a
			 * Long.
			 * 
			 * @param name The name of the setting to retrieve.
			 * @param def The value to return if the setting is not defined.
			 * @return The setting's current value or 'def' if it is not
			 *         defined.
			 */
			private long getLong(String name, long def) {
				ContentValues values = getValues(name);
				return values != null ? values.getAsLong(VALUE) : def;
			}
		}

	}

	public static Uri insertMessageInDb(ContentResolver resolver,
										boolean isGroup,
										long contactId,
										boolean isEncrypted,
										String contact,
										String body,
										long time,
										int type,
										int errCode,
										String id,
										String mimeType
	) {

		ContentValues values = new ContentValues();
		values.put(Imps.Messages.BODY, body);
		values.put(Imps.Messages.DATE, time);
		values.put(Imps.Messages.TYPE, type);
		values.put(Imps.Messages.ERROR_CODE, errCode);
		if (isGroup) {
			values.put(Imps.Messages.NICKNAME, contact);
			values.put(Imps.Messages.IS_GROUP_CHAT, 1);
		}
		values.put(Imps.Messages.IS_DELIVERED, 0);
		values.put(Imps.Messages.MIME_TYPE, mimeType);
		values.put(Imps.Messages.PACKET_ID, id);
		values.put(Messages.DISPLAY_SENT_TIME, 0);
		return resolver.insert(Messages.getOtrMessagesContentUriByThreadId(contactId), values);
	}

	public static Uri insertMessageInDb(ContentResolver resolver,
										boolean isGroup,
										long contactId,
										boolean isEncrypted,
										String contact,
										String body,
										long time,
										int type,
										int errCode,
										String id,
										String mimeType,
										int totalCount
	) {

		ContentValues values = new ContentValues();
		values.put(Imps.Messages.BODY, body);
		values.put(Imps.Messages.DATE, time);
		values.put(Imps.Messages.TYPE, type);
		values.put(Imps.Messages.ERROR_CODE, errCode);
		if (isGroup) {
			values.put(Imps.Messages.NICKNAME, contact);
			values.put(Imps.Messages.IS_GROUP_CHAT, 1);
		}
		values.put(Imps.Messages.IS_DELIVERED, 0);
		values.put(Imps.Messages.MIME_TYPE, mimeType);
		values.put(Imps.Messages.PACKET_ID, id);
		values.put(Messages.DISPLAY_SENT_TIME, 0);
		values.put(Messages.FILE_SUB_COUNT, 0);
		values.put(Messages.FILE_TOTAL_COUNT, totalCount);
		return resolver.insert(Messages.getOtrMessagesContentUriByThreadId(contactId), values);
	}

	public static Uri insertMessageInDb(ContentResolver resolver,
			boolean isGroup,
			long contactId,
			boolean isEncrypted,
			String contact,
			String body,
			long time,
			int type,
			int errCode,
			String id,
			String mimeType,
			long timeDiff
			) {

		ContentValues values = new ContentValues();
		values.put(Imps.Messages.BODY, body);
		values.put(Imps.Messages.DATE, time);
		values.put(Imps.Messages.TYPE, type);
		values.put(Imps.Messages.ERROR_CODE, errCode);
		if (isGroup) {
			values.put(Imps.Messages.NICKNAME, contact);
			values.put(Imps.Messages.IS_GROUP_CHAT, 1);
		}
		values.put(Imps.Messages.IS_DELIVERED, 0);
		values.put(Imps.Messages.MIME_TYPE, mimeType);
		values.put(Imps.Messages.PACKET_ID, id);
		values.put(Imps.Messages.TIME_DELAY, timeDiff);
		values.put(Messages.DISPLAY_SENT_TIME, 0);

		return resolver.insert(isEncrypted ? Messages.getOtrMessagesContentUriByThreadId(contactId) : Messages.getContentUriByThreadId(contactId), values);
	}

	public static int updateContactsInDb(ContentResolver cr,
										  String userAddr,
										  String avatarPath
										  ) {
		ContentValues values = new ContentValues(1);
		values.put(Contacts.THUMBNAILPATH, avatarPath);
		String select = Contacts.USERNAME + "=?";
		String[] selectionArgs = {userAddr};
		return cr.update(Contacts.CONTENT_URI, values, select, selectionArgs);
	}

	public static String getAvatarPath(ContentResolver resolver,
                                       String userAddr) {
        String thumbnail = "";
        Cursor cursor = resolver.query(Imps.Contacts.CONTENT_URI, new String[]{Contacts.THUMBNAILPATH},
                Contacts.USERNAME+ "=?", new String[]{userAddr}, null);
        if (cursor != null && cursor.moveToFirst()) {
            thumbnail = cursor.getString(0);
            if (thumbnail == null)
                thumbnail = "";
            cursor.close();
        }
        return thumbnail;
    }

	public static int updateMessageBody(ContentResolver resolver, String packetId, String body) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues();
		values.put(Imps.Messages.BODY, body);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateOperMessageServerReceived(ContentResolver resolver, String packetId) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		String[] projection = new String[]{Messages.MIME_TYPE, Messages.TYPE};
		Cursor cursor = resolver.query(builder.build(), projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			if (cursor.getString(0) == null) {
				ContentValues values = new ContentValues();
				values.put(Imps.Messages.TYPE, MessageType.OUTGOING);
				cursor.close();
				return resolver.update(builder.build(), values, null, null);
			} else {
				ContentValues values = new ContentValues();
				values.put(Messages.IS_DELIVERED, 2);
				cursor.close();
				return resolver.update(builder.build(), values, null, null);
			}
		}
		return 0;
	}

	public static int updateDeliveryStateInDb(ContentResolver resolver, String packetId, int deliveryState) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues();
		values.put(Messages.IS_DELIVERED, deliveryState);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateOperMessageError(ContentResolver resolver, String packetId, int err_code) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues();
		values.put(Imps.Messages.ERROR_CODE, err_code);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int getErrorByPacketId(String packetId) {
		int errcode = -1;
		Cursor cursor = ImApp.applicationContext.getContentResolver().query(Imps.Messages.CONTENT_URI, new String[]{Imps.Messages.ERROR_CODE},
				Imps.Messages.PACKET_ID + "=?", new String[]{packetId}, null);
		if (cursor != null) {
			if (cursor.moveToFirst())
				errcode = cursor.getInt(0);
			cursor.close();
		}
		return errcode;
	}

	public static int updateMessageSendCount(ContentResolver resolver, String packetId, int sendCount) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues();
		values.put(Messages.FILE_SUB_COUNT, sendCount);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateMessageTypeInDb(ContentResolver resolver, String id, int type) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(id);

		ContentValues values = new ContentValues(1);
		values.put(Imps.Messages.TYPE, type);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateMessageTimeInDb(ContentResolver resolver, String id, long time) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(id);

		ContentValues values = new ContentValues(1);
		values.put(Imps.Messages.DATE, time);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateMessageServerTimeInDb(ContentResolver resolver, String id, long serverTime) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(id);

		ContentValues values = new ContentValues(1);
		values.put(Imps.Messages.SERVER_TIME, serverTime);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateMessagePacketIdInDb(ContentResolver resolver, String prevId, String nextId) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(prevId);

		ContentValues values = new ContentValues(1);
		values.put(Messages.PACKET_ID, nextId);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateMessageInDb(ContentResolver resolver, String packetId, String mimeType, String message) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues(2);
		values.put(Imps.Messages.MIME_TYPE, mimeType);
		values.put(Imps.Messages.BODY, message);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateMessageInDb(ContentResolver resolver, String packetId, String samplePath, int thumbnailWidth, int thumbnailHeight) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues(3);
		values.put(Imps.Messages.SAMPLE_IMAGE_PATH, samplePath);
		values.put(Imps.Messages.THUMBNAIL_WIDTH, thumbnailWidth);
		values.put(Imps.Messages.THUMBNAIL_HEIGHT, thumbnailHeight);
		return resolver.update(builder.build(), values, null, null);
	}

	public static int updateMessageDisplaySentTimeInDb(ContentResolver resolver, String packetId, int displaySentTime) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		ContentValues values = new ContentValues(1);
		values.put(Messages.DISPLAY_SENT_TIME, displaySentTime);
		return resolver.update(builder.build(), values, null, null);
	}

	public static String getMessageBody(ContentResolver resolver, String packetId) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		String body = null;
		Cursor cr = null;
		try{
			cr = resolver.query(builder.build(), new String[]{Imps.Messages.BODY, Imps.Messages.DATE},null, null, null);
			if ( cr != null && cr.moveToFirst() ) {
				body = cr.getString(0);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cr != null ) {
				cr.close();
				cr = null;
			}
		}
		return body;
	}

	public static String getMessageSampleImagePath(ContentResolver resolver, String packetId) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		String sample_image_path = null;
		Cursor cr = null;
		try{
			cr = resolver.query(builder.build(), new String[]{Messages.SAMPLE_IMAGE_PATH},null, null, null);
			if ( cr != null && cr.moveToFirst() ) {
				sample_image_path = cr.getString(0);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cr != null ) {
				cr.close();
				cr = null;
			}
		}
		return sample_image_path;
	}

	public static long getMessageDate(ContentResolver resolver, String packetId) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		long  messageDate = 0;
		Cursor cr = null;
		try{
			cr = resolver.query(builder.build(), new String[]{Imps.Messages.DATE},null, null, null);
			if ( cr != null && cr.moveToFirst() ) {
				messageDate = cr.getLong(0);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cr != null ) {
				cr.close();
				cr = null;
			}
		}
		return messageDate;
	}

	public static int getMessageType(ContentResolver resolver, String packetId) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);

		int  messageType = 0;
		Cursor cr = null;
		try{
			cr = resolver.query(builder.build(), new String[]{Imps.Messages.TYPE},null, null, null);
			if ( cr != null && cr.moveToFirst() ) {
				messageType = cr.getInt(0);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cr != null ) {
				cr.close();
				cr = null;
			}
		}
		return messageType;
	}

	public static int getChatCount(ContentResolver cr, long chatId){
		Uri uri = Imps.Messages.getContentUriByThreadId(chatId);
		Cursor cursor = null;
		int totalCount = 0;
		try{
			cursor = cr.query(uri,
					new String[] {Messages.BODY, Messages.TYPE},
					null,
					null,
					null);

			if( null != cursor ){
				if(cursor.getCount() > 0){
					while(cursor.moveToNext()) {
						String body = cursor.getString(0);
						int type = cursor.getInt(1);
						if ((body != null) && (!body.isEmpty()) && (type < 2 || type == 8))
							totalCount ++;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ){
				cursor.close();
				cursor = null;
			}
		}
		return totalCount;
	}

	public static int getMessageCount(ContentResolver cr, long chatId){
		Uri uri = Imps.Messages.getContentUriByThreadId(chatId);
		Cursor cursor = null;
		int totalCount = 0;
		try{
			cursor = cr.query(uri,
					new String[] {"count(*) AS count"},
					null,
					null,
					null);

			if( null != cursor ){
				if(cursor.getCount() > 0){
					while(cursor.moveToNext()) {
						totalCount = totalCount + cursor.getInt(0);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ){
				cursor.close();
				cursor = null;
			}
		}
		return totalCount;
	}
	//cys
	public static int deleteMessage(Context mContext, ContentResolver resolver, String packetId) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(packetId);
		//check body and delete relative file.
		Cursor cursor = null;
		try{
			cursor = resolver.query(builder.build(), new String[]{Imps.Messages.BODY, Imps.Messages.MIME_TYPE, Imps.Messages.SAMPLE_IMAGE_PATH, Imps.Messages.DATE},
					null, null, null);
			if ( cursor != null && cursor.moveToFirst() ) {
				String body = cursor.getString(0);
				String mime_type = cursor.getString(1);
				String sample_path = cursor.getString(2);

				if ( mime_type != null && body != null ) {
					Uri mediaUri = Uri.parse(body);
					String filePath = null;
					if ( mediaUri.getScheme() != null ) {
						FileInfo info = null;
						try{
							info = SystemServices.getFileInfoFromURI(mContext, mediaUri);
						}catch(Exception e){
							e.printStackTrace();
						}
						if (info != null && info.path != null)
							filePath = info.path;
						else
							filePath = GlobalFunc.getRealPathFromURI(mContext, mediaUri);
					}
					else
						filePath = body;

					File file = new File(filePath);
					if ( file != null && file.exists() ) {
						file.delete();
					}
				}
				if ( sample_path != null ) {
					File file = new File(sample_path);
					if ( file != null && file.exists() ) 
						file.delete();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
		}

		int res = resolver.delete(builder.build(), null, null);
		return res;
	}

	public static int updateConfirmInDb(ContentResolver resolver, String id, boolean isDelivered) {
		Uri.Builder builder = Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID.buildUpon();
		builder.appendPath(id);
		ContentValues values = new ContentValues(1);
		values.put(Imps.Messages.IS_DELIVERED, isDelivered?1:0);
		return resolver.update(builder.build(), values, null, null);
	}

}
