package com.multimediachat.app.im.plugin.xmpp;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.im.engine.Address;
import com.multimediachat.app.im.engine.ChatGroup;
import com.multimediachat.app.im.engine.ChatGroupManager;
import com.multimediachat.app.im.engine.ChatSession;
import com.multimediachat.app.im.engine.ChatSessionManager;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ContactList;
import com.multimediachat.app.im.engine.ContactListListener;
import com.multimediachat.app.im.engine.ContactListManager;
import com.multimediachat.app.im.engine.FindFriendManager;
import com.multimediachat.app.im.engine.ImConnection;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.engine.ImException;
import com.multimediachat.app.im.engine.Invitation;
import com.multimediachat.app.im.engine.Message;
import com.multimediachat.app.im.engine.Presence;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.app.im.provider.ImpsErrorInfo;
import com.multimediachat.global.ConnectionState;
import com.multimediachat.global.DownloadData;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.global.TorProxyInfo;
import com.multimediachat.service.MessengerService;
import com.multimediachat.service.StatusBarNotifier;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message.Body;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.proxy.ProxyInfo.ProxyType;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.LinphoneManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import static java.lang.Thread.sleep;

@SuppressLint("TrulyRandom")
public class XmppConnection extends ImConnection implements CallbackHandler {
    final static String TAG = "GB.XmppConnection";
    private final static boolean PING_ENABLED = true;
    private XmppContactListManager mContactListManager; // manage contact list
    private XmppFindFriendManager mFindFriendManager; // find people by nearby,
    private XmppChatGroupManager mChatGroupManager; // manage group chat
    private XmppChatSessionManager mSessionManager; // manage 1:1chat
    private Contact mUser;

    public MyXMPPConnection mConnection;
    public static Hashtable<String, XmppConnection> mConnections = new Hashtable<String, XmppConnection>();

    private XmppStreamHandler mStreamHandler;

    private Roster mRoster; // list variable contains roster.
    private ConnectionConfiguration mConfig; // connection configuration

    // True if we are in the process of reconnecting. Reconnection is retried
    // once per heartbeat.
    // Synchronized by executor thread.
    private boolean mNeedReconnect; // dedicate that connection need to
    // reconnect.
    private boolean mRetryLogin; // dedicate that connection retry to login
    private ThreadPoolExecutor mExecutor;

    private ProxyInfo mProxyInfo = null;

    private long mAccountId = -1;
    private long mProviderId = -1;

    private final static String SSLCONTEXT_TYPE = "TLS";

    private X509TrustManager mTrustManager;

    private SSLContext sslContext;

    private Context aContext;

    private final static int SOTIMEOUT = 60000;

    private PacketCollector mPingCollector;
    private String mUsername;
    private String mPassword;
    private String mResource;
    private int mPriority;

    private boolean mLoadingAvatars = false;
    private final Random rndForTorCircuits = new Random();

    // Maintains a sequence counting up to the user configured heartbeat
    // interval
    private int heartbeatSequence = 0; // time count for heartbeat

    private int timedelaySequence = 0; // time count for calculate time
    // difference between server and local.
    private int timedelayInterval = 60; // 60*heartbeatInterval(1ì‹œê°„) //time
    // limit for calculate time difference
    // between server and local.

    private ContentResolver mContentResolver;

    HashMap<String, String> avatarHashMap = new HashMap<String, String>();
    LinkedBlockingQueue<String> qAvatar = new LinkedBlockingQueue<String>(); // queue
    // that
    // contains
    // Vcard
    // load
    // requests.

    public XmppConnection(Context context)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        super(context);
        aContext = context;
        // setup SSL managers
        SmackConfiguration.setPacketReplyTimeout(SOTIMEOUT);
        // Create a single threaded executor. This will serialize actions on the
        // underlying connection.
        createExecutor();
        addProviderManagerExtensions();
        XmppStreamHandler.addExtensionProviders();
        DeliveryReceipts.addExtensionProviders();
        Greeting.addExtensionProviders();
        Oper.addExtensionProviders();
        PlusMessage.addExtensionProviders();
        ServiceDiscoveryManager.setIdentityName("PicaTalk");
        ServiceDiscoveryManager.setIdentityType("phone");
        mContentResolver = aContext.getContentResolver();
    }

    public static XmppConnection getXmppConnection(String domain) {
        if (mConnections != null) {
            return mConnections.get(domain);
        } else
            return null;
    }

    public void initUser(long providerId, long accountId) throws ImException {
        Cursor cursor = mContentResolver.query(Imps.ProviderSettings.CONTENT_URI,
                new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE},
                Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(providerId)}, null);

        if (cursor == null)
            throw new ImException("unable to query settings");

        Imps.ProviderSettings.QueryMap providerSettings = new Imps.ProviderSettings.QueryMap(cursor, mContentResolver,
                providerId, false, null);

        mProviderId = providerId;
        mAccountId = accountId;
        DatabaseUtils.mAccountID = Long.toString(mAccountId);
        Imps.mAccountID = Long.toString(mAccountId);
        mUser = makeUser(providerSettings, mContentResolver);
        providerSettings.close();

        getContactListManager().setContactsToListFromDB();
    }

    private Contact makeUser(Imps.ProviderSettings.QueryMap providerSettings, ContentResolver contentResolver) {

        String userName = Imps.Account.getUserName(contentResolver, mAccountId);
        String domain = providerSettings.getDomain();
        String xmppName = userName + '@' + domain + '/' + providerSettings.getXmppResource();

        return new Contact(new XmppAddress(xmppName), userName);
    }

    private void createExecutor() {
        if (mExecutor == null)
            mExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        else {
            mExecutor.shutdownNow();
            mExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        }
    }

    private boolean execute(Runnable runnable) {

        if (mExecutor == null)
            createExecutor(); // if we disconnected, will need to recreate
        // executor here, because join() made it null

        try {
            mExecutor.execute(runnable);
        } catch (RejectedExecutionException ex) {
            return false;
        }
        return true;
    }

    // Execute a runnable only if we are idle
    private boolean executeIfIdle(Runnable runnable) {
        if (mExecutor == null) {
            createExecutor();
        }

        if (mExecutor.getActiveCount() + mExecutor.getQueue().size() == 0) {
            return execute(runnable);
        }
        return false;
    }

    // This runs in executor thread, and since there is only one such thread, we
    // will definitely
    // succeed in shutting down the executor if we get here.
    public void join() {
        final ExecutorService executor = mExecutor;
        mExecutor = null;
        // This will send us an interrupt, which we will ignore. We will
        // terminate
        // anyway after the caller is done. This also drains the executor queue.
        if (executor != null)
            executor.shutdownNow();
    }

    // For testing
    boolean joinGracefully() throws InterruptedException {
        final ExecutorService executor = mExecutor;
        mExecutor = null;
        // This will send us an interrupt, which we will ignore. We will
        // terminate
        // anyway after the caller is done. This also drains the executor queue.
        if (executor != null) {
            executor.shutdown();
            return executor.awaitTermination(1, TimeUnit.SECONDS);
        }

        return false;
    }

    public void sendMessageOperPacket(final org.jivesoftware.smack.packet.Packet packet, String operType,
                                      String operMsgId, String operMessage) {
        if (mConnection == null || !mConnection.isConnected()) {
            postponedMessageOper(packet, operType, operMsgId, operMessage);
            return;
        }

        try {
            mConnection.sendPacket(packet);
        } catch (IllegalStateException ex) {
            postpone(packet);
        }
    }

    public void postponedMessageOper(final org.jivesoftware.smack.packet.Packet packet, String operType,
                                     String operMsgId, String operMessage) {
        if (packet instanceof org.jivesoftware.smack.packet.Message) {
            ChatSession session = findOrCreateSession(packet.getTo());
            if (session != null)
                session.onMessageOperPostponed(packet.getPacketID(), operType, operMsgId, operMessage);
        }
    }

    public void sendPacket(final org.jivesoftware.smack.packet.Packet packet) {

        if (mConnection == null || !mConnection.isConnected()) {
            postpone(packet);
            return;
        }

        try {
            mConnection.sendPacket(packet);
        } catch (IllegalStateException ex) {
            postpone(packet);
        }
    }

    public void sendPacketDirect(final org.jivesoftware.smack.packet.Packet packet) {
        if (mConnection == null || !mConnection.isConnected()) {
            postpone(packet);
            return;
        }

        try {
            mConnection.sendPacket(packet);
        } catch (IllegalStateException ex) {
            postpone(packet);
        }
    }

    void postpone(final org.jivesoftware.smack.packet.Packet packet) {
        if (packet instanceof org.jivesoftware.smack.packet.Message) {
            ChatSession session = findOrCreateSession(packet.getTo());
            if (session != null)
                session.onMessagePostponed(packet.getPacketID());
        }
    }

    @Override
    protected void doUpdateUserPresenceAsync(Presence presence) {

        org.jivesoftware.smack.packet.Presence packet = makePresencePacket(presence);
        sendPacket(packet);
        mUserPresence = presence;
        notifyUserPresenceUpdated();
    }

    @Override
    protected void doNotifyContactsPresenceUpdated(Contact contact) {
        try {
            mContactListManager.notifyContactsPresenceUpdated(new Contact[]{contact});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private org.jivesoftware.smack.packet.Presence makePresencePacket(Presence presence) {
        String statusText = presence.getStatusText();
        Type type = Type.available;
        Mode mode = Mode.available;
        int priority = mPriority;
        final int status = presence.getStatus();
        if (status == Presence.AWAY) {
            priority = 10;
            mode = Mode.away;
        } else if (status == Presence.IDLE) {
            priority = 15;
            mode = Mode.away;
        } else if (status == Presence.DO_NOT_DISTURB) {
            priority = 5;
            mode = Mode.dnd;
        } else if (status == Presence.OFFLINE) {
            priority = 0;
            type = Type.unavailable;
            statusText = "Offline";
        }

        // The user set priority is the maximum allowed
        if (priority > mPriority)
            priority = mPriority;

        org.jivesoftware.smack.packet.Presence packet = new org.jivesoftware.smack.packet.Presence(type, statusText,
                priority, mode);
        return packet;
    }

    @Override
    public int getCapability() {
        return ImConnection.CAPABILITY_SESSION_REESTABLISHMENT | ImConnection.CAPABILITY_GROUP_CHAT;
    }

    @Override
    public synchronized ChatGroupManager getChatGroupManager() {

        if (mChatGroupManager == null)
            mChatGroupManager = new XmppChatGroupManager();

        return mChatGroupManager;
    }

    public class XmppChatGroupManager extends ChatGroupManager {

        private Hashtable<String, MultiUserChat> mMUCs = new Hashtable<String, MultiUserChat>();

        public MultiUserChat getMultiUserChat(String chatRoomJid) {
            return mMUCs.get(chatRoomJid);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean createChatGroupAsync(String roomName, String chatRoomJid) throws Exception {
            RoomInfo roomInfo = null;
            Address address = new XmppAddress(chatRoomJid);
            try {
                // first check if the room already exists
                roomInfo = MultiUserChat.getRoomInfo(mConnection, chatRoomJid);
            } catch (Exception e) {
                // who knows?
            }

            if (roomInfo == null) {
                String nickname = mUser.getName().split("@")[0];
                try {
                    // Create a MultiUserChat using a Connection for a room
                    MultiUserChat muc;
                    if ((muc = mMUCs.get(chatRoomJid)) == null) {
                        muc = new MultiUserChat(mConnection, chatRoomJid);
                    }

                    try {
                        muc.create(nickname); // cys
                    } catch (XMPPException iae) {
                        if (iae.getMessage().contains("Creation failed")) {
                            // some server's don't return the proper 201 create
                            // code, so we can just assume the room was created!
                        } else {
                            throw iae;
                        }
                    }

                    try {
                        Form form = muc.getConfigurationForm();
                        Form submitForm = form.createAnswerForm();
                        for (Iterator fields = form.getFields(); fields.hasNext(); ) {
                            FormField field = (FormField) fields.next();
                            if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
                                submitForm.setDefaultAnswer(field.getVariable());
                            }
                        }
                        submitForm.setAnswer("muc#roomconfig_publicroom", false);
                        muc.sendConfigurationForm(submitForm);
                        joinChatGroupAsync(roomName, address, null, null);
                    } catch (XMPPException xe) {
                        xe.printStackTrace();
                    }

                    return true;

                } catch (XMPPException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }

        }

        @Override
        public void deleteChatGroupAsync(ChatGroup group) {

            String chatRoomJid = group.getAddress().getAddress();

            if (mMUCs.containsKey(chatRoomJid)) {
                MultiUserChat muc = mMUCs.get(chatRoomJid);

                try {
                    muc.destroy("", null);
                    mMUCs.remove(chatRoomJid);
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }

        }

        protected void setGroupMemberBlocked(ChatGroup group, String address, boolean isBlocked) {

        }

        @Override
        protected void addGroupMemberAsync(ChatGroup group, Contact contact) {
        }

        @Override
        protected void removeGroupMemberAsync(ChatGroup group, Contact contact) {
        }

        @Override
        protected synchronized void notifyGroupInvitation(Invitation invitation) {
            super.notifyGroupInvitation(invitation);
        }


        public void leaveRoom(String chatRoomJid) {

            ChatSession session = mSessionManager.findSession(chatRoomJid);
            leaveChatGroupAsync((ChatGroup) session.getParticipant());

            Cursor cursor = null;
            long groupId = -1;
            try {
                String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=? And " + Imps.Contacts.TYPE + "=?";
                String[] selectionArgs = {Long.toString(mAccountId), chatRoomJid, String.valueOf(Imps.Contacts.TYPE_GROUP)};

                cursor = mContentResolver.query(Imps.Contacts.CONTENT_URI, new String[]{Imps.Contacts._ID},
                        select,
                        selectionArgs, null);
                if (cursor.moveToFirst())
                    groupId = cursor.getLong(cursor.getColumnIndex(Imps.Contacts._ID));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }

            if (groupId <= 0)
                return;

            ContentValues value = new ContentValues();
            value.put("groupchat", Imps.Chats.GROUP_LEAVE);
            Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, groupId);
            mContentResolver.update(chatUri, value, null, null);

        }

        private List<Contact> getGroupChatMembersInDB(String chatRoomJid) {
            List<Contact> memberList = new ArrayList<Contact>();
            Cursor cursor = null;
            long groupId = -1;
            try {
                String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=? And " + Imps.Contacts.TYPE + "=?";
                String[] selectionArgs = {Long.toString(mAccountId), chatRoomJid, String.valueOf(Imps.Contacts.TYPE_GROUP)};

                cursor = mContentResolver.query(Imps.Contacts.CONTENT_URI, new String[]{Imps.Contacts._ID},
                        select,
                        selectionArgs, null);
                if (cursor.moveToFirst())
                    groupId = cursor.getLong(cursor.getColumnIndex(Imps.Contacts._ID));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
                cursor = null;
            }
            if (groupId > 0) {
                Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
                try {
                    cursor = mContentResolver.query(uri, new String[]{Imps.GroupMembers.USERNAME,
                            Imps.GroupMembers.NICKNAME, Imps.GroupMembers.TYPE}, null, null, null);
                    while (cursor.moveToNext()) {
                        String addr = cursor.getString(cursor.getColumnIndexOrThrow(Imps.GroupMembers.USERNAME));
                        String nickName = cursor.getString(cursor.getColumnIndexOrThrow(Imps.GroupMembers.NICKNAME));
                        long type = cursor.getLong(cursor.getColumnIndexOrThrow(Imps.GroupMembers.TYPE));
                        if (type == Imps.GroupMembers.TYPE_NORMAL) {
                            Contact contact = new Contact(new XmppAddress(addr), nickName);
                            memberList.add(contact);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null)
                        cursor.close();
                    cursor = null;
                }
            }
            return memberList;
        }

        private List<Contact> getGroupChatMembers(MultiUserChat muc, String chatRoomJid, RoomInfo roomInfo) {
            List<Contact> memberList = new ArrayList<Contact>();

            if (roomInfo != null) {
                Collection<Affiliate> ownersCollection = null;
                try {
                    ownersCollection = muc.getOwners();
                    if (ownersCollection != null) {
                        for (Affiliate aff : ownersCollection) {
                            String jid = aff.getJid();
                            if (jid.equals(mUser.getAddress().getBareAddress()))
                                continue;

                            String nickName = aff.getNick();
                            if (nickName == null || nickName.equals(StringUtils.parseName(jid))) {
                                final String[] tempNickName = {null};

                                PicaApiUtility.getProfileInSyncMode(mContext, StringUtils.parseName(jid), new MyXMLResponseHandler() {
                                    @Override
                                    public void onMySuccess(JSONObject response) {
                                        try {
                                            tempNickName[0] = response.getString(Imps.Contacts.NICKNAME);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onMyFailure(int errcode) {

                                    }
                                });

                                nickName = tempNickName[0];
                            }

                            Contact contact = new Contact(new XmppAddress(jid), nickName);
                            memberList.add(contact);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Collection<Affiliate> membersCollection = null;
                try {
                    membersCollection = muc.getMembers();
                    if (membersCollection != null) {
                        for (Affiliate aff : membersCollection) {
                            String jid = aff.getJid();
                            if (jid.equals(mUser.getAddress().getBareAddress()))
                                continue;

                            String nickName = aff.getNick();
                            if (nickName == null || nickName.equals(StringUtils.parseName(jid))) {
                                final String[] tempNickName = {null};

                                PicaApiUtility.getProfileInSyncMode(mContext, StringUtils.parseName(jid), new MyXMLResponseHandler() {
                                    @Override
                                    public void onMySuccess(JSONObject response) {
                                        try {
                                            tempNickName[0] = response.getString(Imps.Contacts.NICKNAME);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onMyFailure(int errcode) {

                                    }
                                });

                                if (tempNickName[0] == null)
                                    continue;

                                nickName = tempNickName[0];
                            }

                            Contact contact = new Contact(new XmppAddress(jid), nickName);
                            memberList.add(contact);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return memberList;
        }

        @Override
        public void joinChatGroupAsync(String roomName, Address address, String senderAddr, final String lastPacketId) {
            String chatRoomJid = address.getAddress();
            String nickname = mUser.getName().split("@")[0];
            RoomInfo roomInfo = null;
            try {
                roomInfo = MultiUserChat.getRoomInfo(mConnection, chatRoomJid);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mConnection != null && mConnection.isConnected() && roomInfo == null) {
                mGroups.remove(chatRoomJid);
                ChatSession session = mSessionManager.findSession(chatRoomJid);
                if (session != null) {
                    mSessionManager.closeChatSession(session);
                }
                return;
            }

            try {
                // Create a MultiUserChat using a Connection for a room
                MultiUserChat muc = null;
                if (roomInfo != null) {
                    if ((muc = mMUCs.get(chatRoomJid)) == null) {
                        muc = new MultiUserChat(mConnection, chatRoomJid);
                        PacketInterceptor packetInterceptor = new PacketInterceptor() {
                            @Override
                            public void interceptPacket(Packet packet) {
                                if (lastPacketId != null)
                                    packet.setProperty("last_packet", lastPacketId);
                            }
                        };
                        muc.addPresenceInterceptor(packetInterceptor);
                    }

                    if (!muc.isJoined()) {
                        muc.join(nickname);
                    }
                    if (mMUCs.get(chatRoomJid) == null) {
                        mMUCs.put(chatRoomJid, muc);
                    }
                }

                List<Contact> memberList = null;
/*
                if ( isGroupChatExistInDB(chatRoomJid) )
					memberList = getGroupChatMembersInDB(chatRoomJid);
				else
					memberList = getGroupChatMembers(muc, chatRoomJid, roomInfo);
*/

                if (roomInfo != null)
                    memberList = getGroupChatMembers(muc, chatRoomJid, roomInfo);
                else
                    memberList = getGroupChatMembersInDB(chatRoomJid);


                ChatGroup chatGroup = mGroups.get(chatRoomJid);
/*
				int index = 0;
				for (int i = 0; i < memberList.size(); i++) {
					Contact contact = memberList.get(i);
					if (!contact.getAddress().getAddress().equals(mUser.getAddress().getBareAddress())) {
						if (index == 0)
							roomName = contact.getName();
						else if (index < 3) {
							roomName = roomName + "," + contact.getName();
						}
						index++;
					}
				}
*/
                if (chatGroup == null) {
                    // ChatGroup
/*
					if (roomName == null || roomName.equals("")) {
						int index = 0;
						for (int i = 0; i < memberList.size(); i++) {
							Contact contact = memberList.get(i);
							if (!contact.getAddress().getAddress().equals(mUser.getAddress().getBareAddress())) {
								if (index == 0)
									roomName = contact.getName();
								else if (index < 3) {
									roomName = roomName + "," + contact.getName();
								}
								index++;
							}
						}
					}
*/
                    chatGroup = new ChatGroup(address, roomName, null, this);
                    mChatGroupManager.addChatGroup(chatRoomJid, chatGroup);
                    findOrCreateGroupSession(chatRoomJid);
                }
//				else
//				{
//					chatGroup.setName(roomName);
//				}

                Contact groupContact = getContactListManager().getContact(address.getAddress());
                if (groupContact == null) {
                    groupContact = new Contact(address, roomName);
                    groupContact.contact_type = Imps.Contacts.TYPE_GROUP;
                    groupContact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_BOTH;
                    getContactListManager().addContact(groupContact);
                }

                List<Contact> oldMemberList = chatGroup.getMembers();
                if (oldMemberList != null) {
                    for (int i = 0; i < oldMemberList.size(); i++) {
                        Contact member = oldMemberList.get(i);
                        if (member != null) {
                            if (!memberList.contains(member)) {
                                chatGroup.removeMemberAsync(member);
                            }
                        }
                    }
                }
                chatGroup.removeAllMembers();
                chatGroup.addMembers(memberList);
                for (int i = 0; i < memberList.size(); i++) {
                    Contact contact = memberList.get(i);
                    chatGroup.addMemberAsync(contact);
                    Contact c = getContactListManager().getContact(contact.getAddress().getAddress());
                    if (c == null) {
                        mContactListManager
                                .createTemporaryContacts(new String[]{contact.getAddress().getBareAddress()});
                    }
                }
            } catch (XMPPException e) {
                DebugConfig.error(ImApp.LOG_TAG, "error joining MUC", e);
            }

        }

        @Override
        public int leaveChatGroupAsync(ChatGroup group) {
            String chatRoomJid = group.getAddress().getAddress();
            if (mMUCs.containsKey(chatRoomJid)) {
                Map<String, String> inParams = new HashMap<String, String>();
                inParams.put("class", "groupchat");
                inParams.put("cmd", "leaveroom");
                inParams.put("roomid", StringUtils.parseName(chatRoomJid));

                String[] params = GlobalFunc.mapToStringArray(inParams);
                String[] result = getFindFriendManager().setQueryForResult(params);
                if (result == null)
                    return ImErrorInfo.ILLEGAL_SERVER_RESPONSE;

                Map<String, String> outParams = GlobalFunc.stringArrayToMap(result);
                if (outParams == null)
                    return ImErrorInfo.UNKNOWN_ERROR;

                String res = outParams.get("result");

                if (res == null || !res.equals("ok"))
                    return ImErrorInfo.UNKNOWN_ERROR;

				/*getContactListManager().removeContact(chatRoomJid);
				mGroups.cii_remove(chatRoomJid);
				mMUCs.cii_remove(chatRoomJid);*/
                return ImErrorInfo.NO_ERROR;
            }

            return ImErrorInfo.UNKNOWN_ERROR;
        }

        @Override
        public void inviteUserAsync(String reason, ChatGroup group, Contact invitee) {
            String chatRoomJid = group.getAddress().getAddress();
            if (mMUCs.containsKey(chatRoomJid)) {
                MultiUserChat muc = mMUCs.get(chatRoomJid);
                muc.invite(invitee.getAddress().getAddress(), StringUtils.escapeForXML(reason));
                // joinChatGroupAsync(group.getName(), group.getAddress(),
                // null);
            }
        }

        @Override
        public void acceptInvitationAsync(Invitation invitation) {
            Address addressGroup = invitation.getGroupAddress();
            String senderAddr = invitation.getSender().getAddress();
            joinChatGroupAsync(null, addressGroup, senderAddr, null);
        }

        @Override
        public void rejectInvitationAsync(Invitation invitation) {
            Address addressGroup = invitation.getGroupAddress();
            String reason = ""; // no reason for now
            MultiUserChat.decline(mConnection, addressGroup.getAddress(), invitation.getSender().getAddress(), reason);
        }

        @Override
        public void autoJoinMuc() {
            if (mMUCs != null)
                mMUCs.clear();

			/*
			 * new Thread(new Runnable() {
			 *
			 * @Override public void run() {
			 */
            Uri baseUri = Imps.Contacts.CONTENT_URI_CHAT_CONTACTS;
            Uri.Builder builder = baseUri.buildUpon();
            Cursor cursor = null;
            try {
                String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.TYPE + "='" + Imps.Contacts.TYPE_GROUP + "'";
                String[] selectionArgs = {Long.toString(mAccountId)};
                cursor = mContext.getContentResolver().query(builder.build(),
                        new String[]{Imps.Contacts.NICKNAME, Imps.Contacts.USERNAME, Imps.Contacts.TYPE, Imps.Chats.GROUP_CHAT, Imps.Contacts._ID},
                        select, selectionArgs, null);
                while (cursor.moveToNext()) {
                    String nickName = cursor.getString(0);
                    final String userName = cursor.getString(1);
                    int type = cursor.getInt(3);

                    if (type == Imps.Chats.GROUP_LIVE) {
                        try {
                            ChatSession session = findSession(userName);
                            Address address = new XmppAddress(userName);

                            int _id = cursor.getInt(4);
                            Uri msg_uri = Imps.Messages.getContentUriByThreadId(_id);
                            msg_uri = msg_uri.buildUpon().appendQueryParameter("LIMIT", String.valueOf(1)).build();
                            Cursor msg_cur = null;
                            String lastMsgId = "";
                            try {
                                msg_cur = mContext.getContentResolver().query(msg_uri, new String[]{Imps.Messages.DATE, Imps.Messages.PACKET_ID}, null, null, Imps.Messages.DATE + " DESC");
                                if (msg_cur != null && msg_cur.moveToFirst())
                                    lastMsgId = msg_cur.getString(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (msg_cur != null)
                                    msg_cur.close();
                            }

                            getChatGroupManager().joinChatGroupAsync(nickName, address, null, lastMsgId);
                            ChatGroup chatGroup = getChatGroupManager().getChatGroup(userName);
                            if (session == null && chatGroup != null)
                                getChatSessionManager().createChatSession(chatGroup);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
                cursor = null;
            }
			/*
			 * } }).start();
			 */
        }

    }

    @Override
    public synchronized ChatSessionManager getChatSessionManager() {

        if (mSessionManager == null)
            mSessionManager = new XmppChatSessionManager();

        return mSessionManager;
    }

    @Override
    public synchronized XmppContactListManager getContactListManager() {

        if (mContactListManager == null)
            mContactListManager = new XmppContactListManager();

        return mContactListManager;
    }

    @Override
    public synchronized XmppFindFriendManager getFindFriendManager() {

        if (mFindFriendManager == null) {
            mFindFriendManager = new XmppFindFriendManager();
        }

        return mFindFriendManager;
    }

    @Override
    public Contact getLoginUser() {
        return mUser;
    }

    @Override
    public Map<String, String> getSessionContext() {
        // Empty state for now (but must have at least one key)
        return Collections.singletonMap("state", "empty");
    }

    @Override
    public int[] getSupportedPresenceStatus() {
        return new int[]{Presence.AVAILABLE, Presence.AWAY, Presence.IDLE, Presence.OFFLINE,
                Presence.DO_NOT_DISTURB,};
    }

    @Override
    public void loginAsync(long accountId, String passwordTemp, long providerId, boolean retry) {
        mAccountId = accountId;
        DatabaseUtils.mAccountID = Long.toString(mAccountId);
        Imps.mAccountID = Long.toString(mAccountId);
        mProviderId = providerId;
        mRetryLogin = retry;

        Cursor cursor = mContentResolver.query(Imps.ProviderSettings.CONTENT_URI,
                new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE},
                Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(mProviderId)}, null);

        if (cursor == null)
            return;

        Imps.ProviderSettings.QueryMap providerSettings = new Imps.ProviderSettings.QueryMap(cursor, mContentResolver,
                mProviderId, false, null);

        mUser = makeUser(providerSettings, mContentResolver);

        providerSettings.close();

        execute(new Runnable() {
            @Override
            public void run() {
                do_login();
            }
        });
    }

    // Runs in executor thread
    private void do_login() {
        if (mConnection != null) {
            setState(getState(), new ImErrorInfo(ImErrorInfo.CANT_CONNECT_TO_SERVER, "still trying..."));
            return;
        }

        Cursor cursor = null;

        try {
            cursor = mContentResolver.query(Imps.ProviderSettings.CONTENT_URI,
                    new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE},
                    Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(mProviderId)}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor == null)
            return; // not going to work
        Imps.ProviderSettings.QueryMap providerSettings = new Imps.ProviderSettings.QueryMap(cursor, mContentResolver,
                mProviderId, false, null);

        // providerSettings is closed in initConnection();
        String userName = Imps.Account.getUserName(mContentResolver, mAccountId);
        String password = Imps.Account.getPassword(mContentResolver, mAccountId);

        String defaultStatus = null;
        mNeedReconnect = true;
        setState(LOGGING_IN, null);
        mUserPresence = new Presence(Presence.AVAILABLE, defaultStatus, Presence.CLIENT_TYPE_MOBILE);

        try {
            if (userName == null || userName.length() == 0)
                throw new XMPPException("empty username not allowed");

            initConnectionAndLogin(providerSettings, userName, password);

            setState(LOGGED_IN, null);
        } catch (Exception e) {

            e.printStackTrace();
            mConnection = null;
            ImErrorInfo info = new ImErrorInfo(ImErrorInfo.CANT_CONNECT_TO_SERVER, e.getMessage());

            if (e == null || e.getMessage() == null) {
                info = new ImErrorInfo(ImErrorInfo.INVALID_USERNAME, "unknown error");
                disconnected(info);
                mRetryLogin = false;
            } else if (e.getMessage().contains("not-authorized") || e.getMessage().contains("authentication failed")) {
                info = new ImErrorInfo(ImErrorInfo.INVALID_USERNAME, "invalid user/password");
                disconnected(info);
                mRetryLogin = false;
                Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_DELETE_ACCOUNT);
                aContext.sendBroadcast(intent);
                mPref.putInt("AccountError", 3);
            } else if (e.getMessage().contains("SASL authentication failed")) {

            } else if (mRetryLogin) {
                setState(LOGGING_IN, info);
            } else {
                mConnection = null;
                disconnected(info);
            }

            return;

        } finally {
            mNeedReconnect = false;
            providerSettings.close();
        }

    }

    public void setProxy(String type, String host, int port) {
        if (type == null) {
            mProxyInfo = ProxyInfo.forNoProxy();
        } else {

            ProxyInfo.ProxyType pType = ProxyType.valueOf(type);
            String username = null;
            String password = null;

            if (type.equals(TorProxyInfo.PROXY_TYPE) // socks5
                    && host.equals(TorProxyInfo.PROXY_HOST) // 127.0.0.1
                    && port == TorProxyInfo.PROXY_PORT) // 9050
            {
                // if the proxy is for Orbot/Tor then generate random usr/pwd to
                // isolate Tor streams
                username = rndForTorCircuits.nextInt(100000) + "";
                password = rndForTorCircuits.nextInt(100000) + "";

            }
            mProxyInfo = new ProxyInfo(pType, host, port, username, password);
        }
    }

    public void initConnection(MyXMPPConnection connection, Contact user, int state) {
        mConnection = connection;
        mRoster = mConnection.getRoster();
        mUser = user;
        setState(state, null);
    }

    private void initConnectionAndLogin(Imps.ProviderSettings.QueryMap providerSettings, String userName,
                                        String password) throws Exception {
/*
		if (mPasswordTemp != null)
			password = mPasswordTemp;
*/
        mPassword = password;
        mResource = providerSettings.getXmppResource();

        initConnection(providerSettings, userName);

        if (mConnection == null)
            return;

        mConfig.setCompressionEnabled(false);
        String serviceName = mConnection.getServiceName();

        if (!serviceName.equals("") && serviceName.contains("facebook")) {
            mConnection.login(GlobalConstrants.APP_ID, mPassword, mResource);
        } else {
            mConnection.login(mUsername, mPassword, mResource);
        }

        mStreamHandler.notifyInitialLogin();
        mRoster = mConnection.getRoster();
        mRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);

        getContactListManager().loadContactListsAsync(null);
        getContactListManager().listenToRoster(mRoster);

        getContactListManager().doProcessUndeliveredContactsAsync();
        getContactListManager().doProcessCorrectProfileInfoAsync();
        getContactListManager().doProcessNonFriends();
        // getChatGroupManager().autoJoinMuc();

        // send presence Packet for offline message
        org.jivesoftware.smack.packet.Presence presence = new org.jivesoftware.smack.packet.Presence(
                org.jivesoftware.smack.packet.Presence.Type.available);
        presence.setPriority(1);
        presence.setStatus("online");
        sendPacket(presence);
    }

    // Runs in executor thread
    @SuppressLint({"NewApi", "TrulyRandom"})
    private void initConnection(Imps.ProviderSettings.QueryMap providerSettings, String userName) throws Exception {
        if (mConnection == null) {
            // boolean allowPlainAuth = providerSettings.getAllowPlainAuth();
            // boolean requireTls = providerSettings.getRequireTls();
            boolean doDnsSrv = providerSettings.getDoDnsSrv();
            // boolean tlsCertVerify = providerSettings.getTlsCertVerify();

            boolean useSASL = true;// !allowPlainAuth;

            String domain = providerSettings.getDomain();
            String requestedServer = providerSettings.getServer();
            if ("".equals(requestedServer))
                requestedServer = null;
            mPriority = providerSettings.getXmppResourcePrio();
            int serverPort = providerSettings.getPort();

            String server = requestedServer;

            serverPort = 5222; // ccj
            domain = mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain); // ccj

            if (mProxyInfo == null)
                mProxyInfo = ProxyInfo.forNoProxy();
            setProxy(null, null, -1);
            // If user did not specify a server, and SRV requested then
            // lookup SRV
            server = domain;

/*			if (doDnsSrv && requestedServer == null) {

				DNSUtil.HostAddress srvHost = null;
				try {
					srvHost = DNSUtil.resolveXMPPDomain(domain);
				} catch (Exception e) {
					DebugConfig.error(TAG, "resolveXMPPDomain", e);
				}
				if (srvHost != null)
					server = srvHost.getHost();
				else
					server = domain;

				if (serverPort <= 0) {
					// If user did not override port, use port from SRV record
					if (srvHost != null)
						serverPort = srvHost.getPort();
					else
						serverPort = 5223;
				}
			}*/

            if (serverPort == 0) // if serverPort is set to 0 then use 5222 as
                // default
                serverPort = 5223;

            // No server requested and SRV lookup wasn't requested or
            // returned nothing - use domain
            if (server == null) {
                if (mProxyInfo == null)
                    mConfig = new ConnectionConfiguration(domain, serverPort);
                else
                    mConfig = new ConnectionConfiguration(domain, serverPort, mProxyInfo);
                server = domain;
            } else {
                if (mProxyInfo == null)
                    mConfig = new ConnectionConfiguration(server, serverPort, domain);
                else
                    mConfig = new ConnectionConfiguration(server, serverPort, domain, mProxyInfo);
            }
            serverPort = 5222; // ccj
            domain = mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain); // ccj
            mConfig = new ConnectionConfiguration(domain, serverPort); // ccj
            providerSettings.setDomain(domain);

            mConfig.setSASLAuthenticationEnabled(useSASL);
            SASLAuthentication.unregisterSASLMechanism("KERBEROS_V4");
            SASLAuthentication.unregisterSASLMechanism("GSSAPI");

            SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 2);

            if (DebugConfig.SECURITY)
                mConfig.setSecurityMode(SecurityMode.enabled);
            else
                mConfig.setSecurityMode(SecurityMode.disabled);

//			mConfig.setSocketFactory(new DummySSLSocketFactory());

            mConfig.setVerifyChainEnabled(false);
            mConfig.setVerifyRootCAEnabled(false);
            mConfig.setExpiredCertificatesCheckEnabled(false);
            mConfig.setNotMatchingDomainCheckEnabled(false);
            mConfig.setSelfSignedCertificateEnabled(true);
            if (sslContext == null) {
                sslContext = SSLContext.getInstance(SSLCONTEXT_TYPE);
                mTrustManager = getDummyTrustManager();
                SecureRandom mSecureRandom = new java.security.SecureRandom();
                sslContext.init(null, new javax.net.ssl.TrustManager[]{mTrustManager}, mSecureRandom);
                try {
                    sslContext.getDefaultSSLParameters().setCipherSuites(XMPPCertPins.SSL_IDEAL_CIPHER_SUITES);
                } catch (Exception e) {
                    DebugConfig.error("do_log/setCipherSuites", "error=" + e.toString());
                }
            }
            mConfig.setCustomSSLContext(sslContext);
            mConfig.setSendPresence(true);
            mConfig.setReconnectionAllowed(true);
            mConfig.setRosterLoadedAtLogin(true);

            mConnection = new MyXMPPConnection(mConfig);

            mConnection.addPacketInterceptor(new PacketInterceptor() {
                @Override
                public void interceptPacket(Packet packet)
                {
                    final String endStr = packet.toXML().substring(6);
                    packet =new Packet(){
                        @Override
                        public String toXML(){
                            return "<auth authtoken=\"AUTHTOKEN\" " + endStr;
                        }
                    };
                }

            }, new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    DebugConfig.debug("DJH", packet.toXML());

                    if (packet.toXML().startsWith("<auth ") && !packet.toXML().contains("authtoken="))
                    {
                        mConnection.sendPacket(new Packet(){
                            @Override
                            public String toXML(){
                                File loginKey = new File(GlobalConstrants.LOGIN_KEY_PATH);
                                int len = 0;
                                byte[] token = new byte[1024];
                                String strToken = "";
                                try {
                                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(loginKey));
                                    len = inputStream.read(token);
                                    inputStream.close();
                                    strToken = new String(token, 0, len);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                return "<authtoken token=\"AUTHTOKEN\">" + strToken + "</authtoken>"; //xmlns="urn:ietf:params:xml:ns:xmpp-sasl"
                            }
                        });
                        return false;
                    }
                    return false;
                }
            });

            mConnection.addPacketListener(new PacketListener() {
                @Override
                public void processPacket(Packet packet) {
                    String strXml = packet.toXML();
                    if (strXml.contains("credentials-expired")) {
                        GlobalVariable.authorizedState = 1;
                    } else if(strXml.contains("not-authorized")) {
                        GlobalVariable.authorizedState = 2;
                    }
                }
            }, new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    String strXml = packet.toXML();
                    return strXml.startsWith("<failure");
                }
            });


//			mMultiUserInvitatioinListener = new MultiUserInvitationListenerAdapter();
//			MultiUserChat.addInvitationListener(mConnection, mMultiUserInvitatioinListener);

            mConnection.addPacketListener(new PacketListener() {

                @Override
                public void processPacket(Packet packet) {

                    org.jivesoftware.smack.packet.Message smackMessage = (org.jivesoftware.smack.packet.Message) packet;
                    String address = smackMessage.getFrom();
                    String bareAddress = StringUtils.parseBareAddress(address);
                    String body = null;

                    String serverName = StringUtils.parseServer(address);
                    String friend_invite = "";
                    Greeting greeting = null;
                    Oper oper = null;
                    PlusMessage plusmessage = null;


                    try {
                        oper = (Oper) smackMessage.getExtension("oper", Oper.NAMESPACE);
                        if (oper != null) {
                            String type = oper.getType();
                            String opermessage = oper.getMessage();
                            String msgid = oper.getMsgid();
                            if (!type.equalsIgnoreCase(Oper.TYPE_ERROR) && !type.equalsIgnoreCase(Oper.TYPE_SEEN))
                                sendReceipt(smackMessage);

                            if (smackMessage.getType() == org.jivesoftware.smack.packet.Message.Type.groupchat
                                    || serverName.contains("conference")) {
                                ChatSession session = findOrCreateGroupSession(address);
                                if (session != null)
                                    session.onMessageOperReceipt(smackMessage.getPacketID(), type, opermessage, msgid);
                            } else {
                                ChatSession session = findOrCreateSession(address);
                                session.onMessageOperReceipt(smackMessage.getPacketID(), type, opermessage, msgid);
                            }
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (bareAddress.equals(mUser.getAddress().getBareAddress()))
                            return;
						/*
						 * if ( bareAddress.startsWith("admin@") ) return; if (
						 * bareAddress.startsWith("adminevent@") ) return;
						 *
						 */
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (serverName.contains("conference")) {
                            if (smackMessage.getExtension("x", NameSpace.CONFERENCE) != null) // by
                            // cys
                            {
                                MUCUser mucUser = (MUCUser) packet.getExtension("x",
                                        "http://jabber.org/protocol/muc#user");
                                if (mucUser.getInvite() != null && (smackMessage
                                        .getType() != org.jivesoftware.smack.packet.Message.Type.error)) {

                                    XmppAddress groupAddr = new XmppAddress(smackMessage.getFrom());
                                    XmppAddress senderAddr = new XmppAddress(mucUser.getInvite().getFrom());
//									String reason = mucUser.getInvite().getReason();
//									String groupMembers[] = new String[0];
//
//									if ( reason != null && !reason.isEmpty() )
//										groupMembers = reason.split(",");

                                    Contact senderContact = getContactListManager().getContact(senderAddr.getAddress());

                                    String username = mPref.getString("username", "");
                                    String nickName = DatabaseUtils.getUserNickName(mContentResolver);
                                    String roomName = "";

                                    getChatGroupManager().joinChatGroupAsync(roomName, groupAddr, senderAddr.getAddress(), null);
//									ChatGroup chatGroup = getChatGroupManager().getChatGroup(groupAddr.getAddress());
//									Contact contact;
//									if ( chatGroup != null )
//									{
//										for ( int i = 0; i < groupMembers.length / 2;  i ++ )
//										{
//											if ( groupMembers[i*2].startsWith(username) )	//exclude me in group chat member db
//												continue;
//
//											contact = getContactListManager().getContact(groupMembers[i*2]);
//
//											if (contact == null) {
//												XmppAddress xAddr = new XmppAddress(groupMembers[i*2]);
//												contact = new Contact(xAddr, groupMembers[i*2+1]);
//											}
//
//											chatGroup.addMemberAsync(contact);
//										}
//									}
                                    sendReceipt(smackMessage);
                                    return;
                                }
                            }

                            String resource = StringUtils.parseResource(address);
                            if (resource == null || resource.trim().length() == 0) {
                                return;
                            }

                            try {
                                if (resource.equals(StringUtils.parseName(smackMessage.getTo()))) {
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                    }

					/*
					 * DeliveryReceipts.DeliveryReceipt dr =
					 * (DeliveryReceipts.DeliveryReceipt) smackMessage
					 * .getExtension("received", DeliveryReceipts.NAMESPACE); if
					 * (dr != null) { if ( smackMessage.getType () ==
					 * org.jivesoftware.smack.packet.Message.Type.groupchat ||
					 * serverName.contains("conference") ) { ChatSession session
					 * = findOrCreateGroupSession(address);
					 * session.onMessageReceipt(dr.getId()); } else{ ChatSession
					 * session = findOrCreateSession(address);
					 * session.onMessageReceipt(dr.getId()); } }
					 */

                    try {
                        if (getContactListManager().isBlocked(bareAddress)) {
                            return;
                        }
                    } catch (Exception e) {
                    }

                    body = smackMessage.getBody();
                    if (body == null) {
                        Collection<Body> mColl = smackMessage.getBodies();
                        for (Body bodyPart : mColl) {
                            String msg = bodyPart.getMessage();
                            if (msg != null) {
                                body = msg;
                                break;
                            }
                        }
						/*
						 * if ( body != null ) { try{ body =
						 * Html.fromHtml(body).toString(); }catch(Exception e){
						 * e.printStackTrace(); } }
						 */
                    }

                    try {
                        friend_invite = (String) smackMessage.getProperty("friend_invite");
                        greeting = (Greeting) smackMessage.getExtension("greeting", Greeting.NAMESPACE);
                        plusmessage = (PlusMessage) smackMessage.getExtension("x", PlusMessage.NAMESPACE);
                    } catch (Exception e) {
                    }

                    if (plusmessage != null) {
                    }

					/*
					 * String fromUser = "nnnn"; if (body != null) { XmppAddress
					 * fromUserAddress = new
					 * XmppAddress(smackMessage.getFrom()); fromUser =
					 * fromUserAddress.getUser(); if
					 * (fromUser.equals("adminevent")) {
					 * ImApp.getInstance().postNotificationStudentEvent("invite"
					 * ); NotificationCenter.getInstance().postNotificationName(
					 * NotificationCenter.studentevent_invited, body); //
					 * NotificationCenter.getInstance().addObserver(this,
					 * NotificationCenter.appNeedFinish); //
					 * NotificationCenter.getInstance().removeObserver(this,
					 * NotificationCenter.appNeedFinish); }
					 * DebugConfig.error("do_log/setCipherSuites",
					 * "-------------------body=" + body + ":fromUser=" +
					 * fromUser); } DebugConfig.error("do_log/setCipherSuites",
					 * "-------------------body=" + body + ":fromUser=" +
					 * fromUser);
					 */
                    if (body != null) {
                        XmppAddress aFrom = new XmppAddress(smackMessage.getFrom());

                        Message rec = new Message(body);
                        rec.setTo(mUser.getAddress());
                        rec.setFrom(aFrom);

                        String packetId = smackMessage.getPacketID();
                        if (packetId != null)
                            rec.setID(packetId);
                        else
                            rec.setID(Packet.nextID());

                        // use delay extension and calculate real packet sent
                        // time.
                        DelayInformation delayInfo = null;
                        try {
                            delayInfo = (DelayInformation) smackMessage.getExtension("x", "jabber:x:delay");

                            if (delayInfo == null) {
                                delayInfo = (DelayInformation) smackMessage.getExtension("x", "urn:xmpp:delay");
                            }

                            if (delayInfo != null) {
                                Date delayTimeStamp = delayInfo.getStamp();
                                long serverTime = GlobalFunc.convertLocalToUTCTime(delayTimeStamp.getTime());
                                rec.setServerTime(serverTime);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        rec.setDateTime(new Date());
                        rec.setType(Imps.MessageType.INCOMING);

                        if (smackMessage.getType() != org.jivesoftware.smack.packet.Message.Type.groupchat) {
                            // 1:1 chat

                            ChatSession session = findOrCreateSession(address);

                            Contact rContact = mContactListManager.getContact(aFrom);

                            if (rContact == null) {
                                Contact[] contacts = mContactListManager
                                        .createTemporaryContacts(new String[]{aFrom.getBareAddress()});

                                try {
                                    if (contacts != null && contacts.length > 0) {
                                        Contact contact = contacts[0];
                                        mContactListManager.getDefaultContactList().addExistingContact(contact);
                                        String nickName = null;
                                        if (bareAddress.startsWith("pls")) {

                                        } else {
                                            try {
                                                if (bareAddress.startsWith("admin@")) {
                                                    nickName = "multimediachat Team";
                                                } else if (bareAddress.startsWith("adminevent@")) {
                                                    nickName = mContext.getString(R.string.event_name);
                                                } else {
                                                    final String[] tempNickName = {null};
                                                    PicaApiUtility.getProfileInSyncMode(mContext, StringUtils.parseName(bareAddress), new MyXMLResponseHandler() {
                                                        @Override
                                                        public void onMySuccess(JSONObject response) {
                                                            try {
                                                                tempNickName[0] = response.getString(Imps.Contacts.NICKNAME);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        @Override
                                                        public void onMyFailure(int errcode) {

                                                        }
                                                    });

                                                    nickName = tempNickName[0];
                                                }

                                                if (nickName != null) {
                                                    aFrom.setName(nickName);
                                                    rec.setFrom(aFrom);
                                                    contact.setName(nickName);
                                                    contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;
                                                    contact.contact_type = Imps.Contacts.TYPE_TEMPORARY;
                                                    session.setParticipant(contact);
                                                    mContactListManager.setContactName(aFrom.getBareAddress(),
                                                            nickName);
                                                    DatabaseUtils.updateNickName(mContentResolver,
                                                            aFrom.getBareAddress(), nickName);
                                                    if (!qAvatar.contains(bareAddress)) {
                                                        qAvatar.put(address);
                                                        loadVCardsAsync(false);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                rContact = mContactListManager.getContact(aFrom);
                            }

                            if (rContact != null) // if we have not added them
                            // as a contact, don't
                            // receive message
                            {

                                boolean good = session.onReceiveMessage(rec);
                                handlePresenceChanged(mRoster.getPresence(smackMessage.getFrom()), null);

                                if (smackMessage.getExtension("request", DeliveryReceipts.NAMESPACE) != null) {
                                    if (good) {
                                        DebugConfig.debug(TAG, "sending delivery receipt");
                                        // got XEP-0184 request, send receipt
                                        sendReceipt(smackMessage);
                                        session.onReceiptsExpected();
                                    } else {
                                        DebugConfig.debug(TAG, "not sending delivery receipt due to processing error");
                                    }
                                } else if (!good) {
                                    DebugConfig.debug(TAG, "packet processing error");
                                }
                            }
                        } else {
                            // group chat
                            ChatSession session = findOrCreateGroupSession(aFrom.getBareAddress()); // soch
                            if (session != null) {
                                if (friend_invite != null && friend_invite.equals("yes")) {
                                    rec.setType(Imps.MessageType.FRIEND_INVITE);
                                    ChatGroup chatGroup = getChatGroupManager().getChatGroup(aFrom.getBareAddress());
                                    if (chatGroup != null) {
                                        String reason = smackMessage.getBody();

                                        String groupMembers[] = new String[0];

                                        if (reason != null && !reason.isEmpty())
                                            groupMembers = reason.split(",");

                                        try {
                                            if (groupMembers.length >= 4) //senderid, sendername, inviteduserid, invitedusername, ...
                                            {
                                                String roomName = chatGroup.getName();
                                                String username = mPref.getString("username", "");
                                                String nickName = DatabaseUtils.getUserNickName(mContentResolver);

                                                Contact senderContact = getContactListManager().getContact(groupMembers[0]);

                                                if (senderContact == null) {
                                                    XmppAddress xAddr = new XmppAddress(groupMembers[0]);
                                                    senderContact = new Contact(xAddr, groupMembers[1]);
                                                }

                                                Contact contact;

                                                String parseMsg = senderContact.getName() + mContext.getString(R.string.user_) + " ";
                                                String members = "";
                                                boolean isIncluedMeInMembers = false;
                                                int invitecount = 0;

                                                for (int i = 0; i < groupMembers.length / 2; i++) {
                                                    contact = getContactListManager().getContact(groupMembers[i * 2]);

                                                    if (contact == null) {
                                                        XmppAddress xAddr = new XmppAddress(groupMembers[i * 2]);

                                                        if (groupMembers[i * 2].startsWith(username))    //if it's me
                                                            contact = new Contact(xAddr, nickName);
                                                        else
                                                            contact = new Contact(xAddr, groupMembers[i * 2 + 1]);
                                                    }

                                                    if (i > 0)    //first is sender, so do not inclued in invited members
                                                    {
                                                        if (groupMembers[i * 2].startsWith(username)) //if it's me, use 'you' instead of name
                                                        {
                                                            isIncluedMeInMembers = true;
                                                        } else {
                                                            if (invitecount == 0)
                                                                members += contact.getName();
                                                            else
                                                                members += "," + contact.getName();

                                                            invitecount++;
                                                        }
                                                    }

                                                    if (chatGroup.getMember(groupMembers[i * 2]) != null)
                                                        continue;

                                                    chatGroup.addMemberAsync(contact);
                                                }

                                                if (isIncluedMeInMembers) {
                                                    if (invitecount > 0)
                                                        parseMsg += mContext.getString(R.string.you_and) + " " + members + mContext.getString(R.string.to_user) + " ";
                                                    else
                                                        parseMsg += mContext.getString(R.string.to_you) + " ";
                                                } else {
                                                    if (invitecount > 0)
                                                        parseMsg += members + mContext.getString(R.string.to_user) + " ";
                                                }

                                                parseMsg += mContext.getString(R.string.sent_request_for_group_chat);

                                                if (isIncluedMeInMembers || invitecount > 0)
                                                    rec.setBody(parseMsg);
                                            }
                                        } catch (Exception e) {

                                        }

//										getChatGroupManager().joinChatGroupAsync(chatGroup.getName(),
//												new XmppAddress(aFrom.getBareAddress()), null);
                                    }
                                } else {
                                    // Detect if this was said by us, and mark
                                    // message as outgoing
                                    if (smackMessage.getType() == org.jivesoftware.smack.packet.Message.Type.groupchat
                                            && rec.getFrom().getResource().equals(rec.getTo().getUser())) {
                                        return;
                                    }
                                }

                                boolean good = session.onReceiveMessage(rec);

								/*
								 * try{
								 * handleMUCPresence(smackMessage.getFrom().
								 * split("/"), null); }catch(Exception e){
								 * e.printStackTrace(); }
								 */

                                // check whether this packet requests receipts
                                // then send receipt.
                                if (smackMessage.getExtension("request", DeliveryReceipts.NAMESPACE) != null) {
                                    if (good) {
                                        DebugConfig.debug(TAG, "sending delivery receipt");
                                        // got XEP-0184 request, send receipt
                                        sendReceipt(smackMessage);
                                        session.onReceiptsExpected();
                                    } else {
                                        DebugConfig.debug(TAG, "not sending delivery receipt due to processing error");
                                    }

                                } else if (!good) {
                                    DebugConfig.debug(TAG, "packet processing error");
                                }
                            }

                        }

                    }

                    if (greeting != null) { // receive greeting message and
                        // insert contact to susscription
                        // list

                        int sub_type = -1;
                        sub_type = DatabaseUtils.getSubscriptionType(mContentResolver, bareAddress);
                        String msg = greeting.getGreeting();

                        while (sub_type == -1) {
                            try {
                                sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sub_type = DatabaseUtils.getSubscriptionType(mContentResolver, bareAddress);
                        }

                        if (sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM) {
                            DatabaseUtils.updateSubscriptionMessage(mContentResolver, mAccountId, msg, bareAddress);
                        } else if (sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_BOTH) {
                            org.jivesoftware.smack.packet.Presence presence = new org.jivesoftware.smack.packet.Presence(
                                    Type.subscribe);
                            presence.setFrom(bareAddress);
                            handlePresenceChanged(presence, msg);
                            sendReceipt(smackMessage);
                        } else if (sub_type == -1) {
                            /*ContactList defaultList = null;
                            while (defaultList == null) {
                                try {
                                    defaultList = getContactListManager().getDefaultContactList();
                                } catch (ImException e) {
                                }
                            }*/

							/*DatabaseUtils.insertOrUpdateSubscription(mContentResolver, defaultList, mProviderId,
									mAccountId, bareAddress, bareAddress, Imps.Contacts.SUBSCRIPTION_TYPE_NONE,
									Imps.Contacts.SUBSCRIPTION_STATUS_NONE, msg);*/
                        }
                    }
                }
            }, new PacketTypeFilter(org.jivesoftware.smack.packet.Message.class));

            mConnection.addPacketListener(new PacketListener() {
                @Override
                public void processPacket(Packet packet) {

                    org.jivesoftware.smack.packet.Presence presence = (org.jivesoftware.smack.packet.Presence) packet;
                    handlePresenceChanged(presence, null);

                }
            }, new PacketTypeFilter(org.jivesoftware.smack.packet.Presence.class));

            ConnectionListener connectionListener = new ConnectionListener() {
                /**
                 * Called from smack when connect() is fully successful
                 *
                 * This is called on the executor thread while we are in
                 * reconnect()
                 */
                @Override
                public void reconnectionSuccessful() {
                    if (mStreamHandler == null || !mStreamHandler.isResumePending()) {
                        DebugConfig.debug(TAG, "Reconnection success");
                        onReconnectionSuccessful();
                    } else {
                        DebugConfig.debug(TAG, "Ignoring reconnection callback due to pending resume");
                    }
                }

                @Override
                public void reconnectionFailed(Exception e) {
                    // We are not using the reconnection manager
                    throw new UnsupportedOperationException();
                }

                @Override
                public void reconnectingIn(int seconds) {
                    // // We are not using the reconnection manager
                    // throw new UnsupportedOperationException();
                }

                @Override
                public void connectionClosedOnError(final Exception e) {
					/*
					 * This fires when: - Packet reader or writer detect an
					 * error - Stream compression failed - TLS fails but is
					 * required - Network error - We forced a socket shutdown
					 */
                    DebugConfig.debug(TAG, "reconnect on error: " + e.getMessage());
                    if (e.getMessage().contains("conflict")) {
                        execute(new Runnable() {
                            @Override
                            public void run() {
                                disconnect();
                                disconnected(new ImErrorInfo(ImpsErrorInfo.ALREADY_LOGGED,
                                        "logged in from another location"));
                            }
                        });
                    } else if (e.getMessage().contains("system-shutdown")) {
                        execute(new Runnable() {
                            @Override
                            public void run() {
                                setState(DISCONNECTED, new ImErrorInfo(ImErrorInfo.UNKNOWN_ERROR, e.getMessage()));
                                mRetryLogin = true;
                                maybe_reconnect();
                            }
                        });
                    }

                    if (GlobalFunc.getXmppConnStatus() != ConnectionState.eCONNSTAT_XMPP_DISCONNECTED) {
                        GlobalFunc.SetXmppConnectStatPref(mContext, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED);
                        GlobalFunc.SetXmppConnectErrPref(-1, e.getMessage());
//						StatusBarNotifier.notifyServerState(mContext, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED, mContext.getString(R.string.server_connection_error));
                    }
                }

                @Override
                public void connectionClosed() {
                    DebugConfig.debug(TAG, "connection closed");
                    if (GlobalFunc.getXmppConnStatus() != ConnectionState.eCONNSTAT_XMPP_DISCONNECTED) {
                        GlobalFunc.SetXmppConnectStatPref(mContext, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED);
                        GlobalFunc.SetXmppConnectErrPref(-1, "connection closed");
//						StatusBarNotifier.notifyServerState(mContext, ConnectionState.eCONNSTAT_XMPP_DISCONNECTED, mContext.getString(R.string.server_connection_error));
                    }
                }
            };

            mConnection.addConnectionListener(connectionListener);
            mStreamHandler = new XmppStreamHandler(mConnection, connectionListener);

        }
        mUsername = userName;
        if (!mConnection.isConnected()) {
            try {
                mConnection.connect();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @SuppressWarnings("unused")
    private void sendPresencePacket() {
        org.jivesoftware.smack.packet.Presence presence = makePresencePacket(mUserPresence);
        sendPacket(presence);
    }

    public void sendReceipt(org.jivesoftware.smack.packet.Message msg) {
        org.jivesoftware.smack.packet.Message ack = new org.jivesoftware.smack.packet.Message(
                StringUtils.parseBareAddress(msg.getFrom()), msg.getType());
        ack.addExtension(new DeliveryReceipts.DeliveryReceipt(msg.getPacketID()));
        sendPacket(ack);
    }

    public synchronized X509TrustManager getDummyTrustManager() {
        if (mTrustManager == null) {
            mTrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        }

        return mTrustManager;
    }

    protected int parsePresence(org.jivesoftware.smack.packet.Presence presence) {
        int type = Presence.AVAILABLE;
        Mode rmode = presence.getMode();
        Type rtype = presence.getType();

        //if a device sends something other than available, check if there is a higher priority one available on the server
        if (rmode == Mode.chat)
            type = Presence.AVAILABLE;
        else if (rmode == Mode.away || rmode == Mode.xa)
            type = Presence.AWAY;
        else if (rmode == Mode.dnd)
            type = Presence.DO_NOT_DISTURB;
        else if (rtype == Type.unavailable || rtype == Type.error)
            type = Presence.OFFLINE;
        else if (rtype == Type.unsubscribed)
            type = Presence.OFFLINE;

        return type;
    }

    // We must release resources here, because we will not be reused
    void disconnected(ImErrorInfo info) {
        DebugConfig.debug(TAG, "disconnected");
        join();
        setState(DISCONNECTED, info);
    }

    @Override
    public void logoutAsync() {

        execute(new Runnable() {
            @Override
            public void run() {
                do_logout();
            }
        });

    }

    // Force immediate logout
    public void logout() {
        // logoutAsync();
        do_logout();
    }

    // Usually runs in executor thread, unless called from logout()
    private void do_logout() {
        setState(LOGGING_OUT, null);
        disconnect();
        disconnected(null);
    }

    // Runs in executor thread
    private void disconnect() {

        clearPing();
        XMPPConnection conn = mConnection;
        mConnection = null;
        try {
            conn.disconnect();
        } catch (Throwable th) {
            // ignore
        }
        mNeedReconnect = false;
        mRetryLogin = false;
    }

    @Override
    public void reestablishSessionAsync(Map<String, String> sessionContext) {
        execute(new Runnable() {
            @Override
            public void run() {
                if (getState() == SUSPENDED) {
                    DebugConfig.debug(TAG, "reestablish");
                    setState(LOGGING_IN, null);
                    maybe_reconnect();
                }
            }
        });
    }

    @Override
    public void suspend() {
        execute(new Runnable() {
            @Override
            public void run() {
                DebugConfig.debug(TAG, "connection suspend");
                setState(SUSPENDED, null);
                mNeedReconnect = false;
                clearPing();
                // Do not try to reconnect anymore if we were asked to suspend
                mStreamHandler.quickShutdown();
            }
        });
    }

    private ChatSession findOrCreateSession(String address) {
        ChatSession session = mSessionManager.findSession(address);

        if (session == null) {
            Contact contact = findOrCreateContact(address);
            session = mSessionManager.createChatSession(contact);
        }
        return session;
    }

    Contact findOrCreateContact(String address) {
        Contact contact = null;
        while (contact == null) {
            try {
                sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contact = mContactListManager.getContact(address);
        }

        return contact;
    }

    private Contact makeContact(String address) {

        Contact contact = null;

        // load from roster if we don't have the contact
        RosterEntry rEntry = null;

        if (mConnection != null)
            rEntry = mRoster.getEntry(address);

        if (rEntry != null) {
            XmppAddress xAddress = new XmppAddress(rEntry.getUser());

            String name = rEntry.getName();
            if (name == null)
                name = xAddress.getUser();

            contact = new Contact(xAddress, name);
        } else {
            XmppAddress xAddress = new XmppAddress(address);

            contact = new Contact(xAddress, xAddress.getUser());
        }

        return contact;
    }

    private ChatSession findOrCreateGroupSession(String address) {
        String bareAddress = StringUtils.parseBareAddress(address);

        if (bareAddress == null)
            return null;

        ChatSession session = mSessionManager.findSession(bareAddress);

        if (session == null) {
            ChatGroup chatGroup = findChatGroup(bareAddress);
            if (chatGroup != null)
                session = mSessionManager.createChatSession(chatGroup);
        }
        return session;
    }

    ChatGroup findChatGroup(String address) {
        ChatGroup chatGroup = mChatGroupManager.getChatGroup(address);
        if (chatGroup == null) {
            chatGroup = makeChatGroup(address);
            mChatGroupManager.addChatGroup(address, chatGroup);
        }

        return chatGroup;
    }

    String getGroupNickName(String address) {
        String nickName = "";
        Uri contactUri = ContentUris.withAppendedId(
                ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mProviderId),
                mAccountId);

        Cursor cursor = mContentResolver.query(contactUri, new String[]{Imps.Contacts.NICKNAME, Imps.Contacts._ID}, Imps.Contacts.USERNAME + "=?", new String[]{address}, null);

        if (cursor.moveToFirst()) {
            nickName = cursor.getString(cursor.getColumnIndex(Imps.Contacts.NICKNAME));
        }

        return nickName;
    }

    private ChatGroup makeChatGroup(String address) {

        ChatGroup chatGroup = null;
        String[] parts = address.split("@");
        String room = parts[0];
        XmppAddress xAddress = new XmppAddress(address);

        chatGroup = new ChatGroup(xAddress, getGroupNickName(address), mChatGroupManager);

        return chatGroup;
    }

    private final class XmppChatSessionManager extends ChatSessionManager {
        @Override
        public String sendMessageAsync(ChatSession session, Message message) {
            String chatRoomJid = message.getTo().getAddress();
            String operType = message.getOperType();
            String operMsgId = message.getOperMsgId();
            String operMessage = message.getOperMessage();

            MultiUserChat muc = ((XmppChatGroupManager) getChatGroupManager()).getMultiUserChat(chatRoomJid);
            if (chatRoomJid.contains("conference")) {
                org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message(
                        message.getTo().getAddress(), org.jivesoftware.smack.packet.Message.Type.groupchat);
                msg.addExtension(new DeliveryReceipts.DeliveryReceiptRequest());

                if (operType == null) {
                    // msg.setBody(StringUtils.escapeForXML(message.getBody().trim()));
                    msg.setBody(message.getBody());
                    if (message.getType() == Imps.MessageType.FRIEND_INVITE)
                        msg.setProperty("friend_invite", "yes");
                    if (message.getID() != null && !message.getID().equals("")) {
                        msg.setPacketID(message.getID());
                    } else
                        message.setID(msg.getPacketID());

                    if (message.getTimeDiff() != 0) {
                        Date messageTime = new Date(message.getDateTime().getTime() - message.getTimeDiff());
                        DelayInformation delayInfo = new DelayInformation(messageTime);
                        msg.addExtension(delayInfo);
                    }
                    if (muc == null)
                        postpone(msg);
                    else
                        sendPacket(msg);
                } else {
                    msg.addExtension(new Oper(operType, operMessage, operMsgId));
                    msg.setPacketID(message.getID());
                    if (muc == null)
                        postponedMessageOper(msg, operType, operMsgId, operMessage);
                    else
                        sendMessageOperPacket(msg, operType, operMsgId, operMessage);
                }

                return msg.getPacketID();
            } else {
                org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message(
                        message.getTo().getAddress(), org.jivesoftware.smack.packet.Message.Type.chat);

                msg.addExtension(new DeliveryReceipts.DeliveryReceiptRequest());

                if (operType == null) {
                    // msg.setBody(StringUtils.escapeForXML(message.getBody().trim()));
                    msg.setBody(message.getBody());
                    DebugConfig.debug(TAG, "sending packet ID " + msg.getPacketID());

                    if (message.getID() != null && !message.getID().equals("")) {
                        msg.setPacketID(message.getID());
                    } else
                        message.setID(msg.getPacketID());

                    if (message.getTimeDiff() != 0) {
                        Date messageTime = new Date(message.getDateTime().getTime() - message.getTimeDiff());
                        DelayInformation delayInfo = new DelayInformation(messageTime);
                        msg.addExtension(delayInfo);
                    }
                    sendPacket(msg);
                } else {
                    msg.addExtension(new Oper(operType, operMessage, operMsgId));
                    msg.setPacketID(message.getID());
                    sendMessageOperPacket(msg, operType, operMsgId, operMessage);
                }
                return msg.getPacketID();
            }
        }

        ChatSession findSession(String address) {
            return mSessions.get(Address.stripResource(address));
        }

    }

    public ChatSession findSession(String address) {
        return mSessionManager.findSession(address);
    }

    public ChatSession createChatSession(Contact contact) {
        return mSessionManager.createChatSession(contact);
    }

    public class XmppFindFriendManager extends FindFriendManager {
        @Override
        protected List<Contact> findFriends(String aKey, String arg0, String latitude, String longtitude) {
            if (mConnection == null || !mConnection.isConnected())
                return null;

            Map<String, String> inParams = new HashMap<String, String>();
            if (aKey.contains("Nearby")) {
                inParams.put("cmd", "nearby");
                inParams.put("kind", arg0);
                inParams.put("lat", latitude);
                inParams.put("lng", longtitude);
            } else if (aKey.equals("Shake")) {
                inParams.put("cmd", "shake");
                inParams.put("lat", latitude);
                inParams.put("lng", longtitude);
            } else if (aKey.equals("Search")) {
                inParams.put("cmd", "id");
                inParams.put("key", arg0);
            } else if (aKey.equals("SearchByPhone")) {
                inParams.put("cmd", "vcard");
                inParams.put("username", arg0);
            } else
                return null;

            List<Contact> contacts = new ArrayList<Contact>();
            PicaSearchResult searchResultList = getSearchResult(inParams);

            if (searchResultList == null)
                return null;

            String serverName = StringUtils.parseServer(mUser.getAddress().getBareAddress());
            for (PicaSearchResult.Row row : searchResultList.getRows()) {
                String address = row.getValue("username") + "@" + serverName;
                String name = row.getValue("nickname");

                if (address == null || address.trim().equals(""))
                    continue;

                if (name == null || name.trim().equals(""))
                    continue;

                XmppAddress xAddress = new XmppAddress(address);
                Contact contact = new Contact(xAddress, name);
                if (aKey.equals("Nearby") || aKey.equals("Shake"))
                    contact.setDistance(row.getValue("distance"));

                try {
                    contact.setProfile(row.getValue("status"), row.getValue("gender"), row.getValue("region"));
                } catch (Exception e) {
                    DebugConfig.error("xmpp/findfriends", "error=" + e.toString());
                }
                contacts.add(contact);
            }
            if (aKey.contains("Nearby")) {
                Collections.sort(contacts, new Comparator<Contact>() {

                    @Override
                    public int compare(Contact lhs, Contact rhs) {
                        return Double.compare(lhs.getDistance(), rhs.getDistance());
                    }

                });
            }

            return contacts;
        }

        @Override
        protected String sendData(String param[]) {
            String res = "success";
            Map<String, String> attributes = new HashMap<String, String>();

            Registration reg = new Registration();
            reg.setType(IQ.Type.SET);
            reg.setTo(mConnection.getServiceName());

            if (param[0] != null)
                attributes.put("token", StringUtils.escapeForXML(param[0].trim()));
            else
                attributes.put("token", "");
            if (param[1] != null)
                attributes.put("email", StringUtils.escapeForXML(param[1].trim()));
            else
                attributes.put("email", "");
            if (param[2] != null)
                attributes.put("password", StringUtils.escapeForXML(param[2].trim()));
            else
                attributes.put("password", "");

            reg.setAttributes(attributes);
            PacketFilter filter = new AndFilter(new PacketIDFilter(reg.getPacketID()), new PacketTypeFilter(IQ.class));
            PacketCollector collector = mConnection.createPacketCollector(filter);
            sendPacketDirect(reg);
            IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());

            // Stop queuing results
            collector.cancel();
            if (result == null) {
                res = "No response from server.";
            } else if (result.getType() == IQ.Type.ERROR) {
                String err = result.getError().toXML();

                if (err.contains("gone"))
                    res = "success";
                else if (err.contains("not-allowed"))
                    res = "not-allowed";
                else
                    res = err.toString();
            }

            if (param[0].equals("get_facebook_account") || param[0].equals("get_pica_account")
                    || param[0].equals("get_my_facebook_account")) {
                res += "," + result.toXML(); // success,
            }

            return res;
        }

        @Override
        protected String[] setQueryForResult(String[] params) {
            if (mConnection == null || !mConnection.isConnected())
                return null;

            final Map<String, String> inParams = GlobalFunc.stringArrayToMap(params);
            PicaData sendIQ = new PicaData();
            if (inParams.containsKey("resource")) {
                sendIQ.setFrom(mUser.getAddress().getAddress());
                inParams.remove("resource");
            }
            sendIQ.setAttributes(inParams);
            sendIQ.setType(IQ.Type.SET);
            sendIQ.setPacketID(Packet.nextID());
            DebugConfig.println("communicate " + sendIQ.toXML());
            PacketFilter filter = new AndFilter(new PacketIDFilter(sendIQ.getPacketID()),
                    new PacketTypeFilter(IQ.class));
            PacketCollector collector = mConnection.createPacketCollector(filter);
            long cur = System.currentTimeMillis();
            sendPacketDirect(sendIQ);
            PicaData result = (PicaData) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            DebugConfig.println("command: " + inParams.get("cmd") + ", setQuery, communicate takes time:"
                    + (System.currentTimeMillis() - cur));
            collector.cancel();
            collector = null;
            Map<String, String> resultParams = null;
            if (result != null) {
                resultParams = result.getAttributes();
                if (result.getError() != null) {
                    resultParams.put("error", "error");
                }
            } else
                resultParams = new HashMap<String, String>();
            return GlobalFunc.mapToStringArray(resultParams);
        }

        @Override
        protected String[] getQueryResult(String[] params) {

            if (mConnection == null || !mConnection.isConnected())
                return null;

            final Map<String, String> inParams = GlobalFunc.stringArrayToMap(params);
            PicaData sendIQ = new PicaData();
            if (inParams.containsKey("resource")) {
                sendIQ.setFrom(mUser.getAddress().getAddress());
                inParams.remove("resource");
            }

            sendIQ.setAttributes(inParams);
            sendIQ.setType(IQ.Type.GET);
            sendIQ.setPacketID(Packet.nextID());
            DebugConfig.println("communicate " + sendIQ.toXML());
            // PacketFilter filter = new AndFilter(new
            // PacketIDFilter(sendIQ.getPacketID()), new
            // PacketTypeFilter(IQ.class));
            PacketFilter filter = new PacketIDFilter(sendIQ.getPacketID());
            PacketCollector collector = mConnection.createPacketCollector(filter);
            long cur = System.currentTimeMillis();
            sendPacketDirect(sendIQ);
            PicaData result = (PicaData) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            DebugConfig.println("command: " + inParams.get("cmd") + ",getQuery communicate takes time:"
                    + (System.currentTimeMillis() - cur));
            collector.cancel();
            collector = null;
            Map<String, String> resultParams = null;
            if (result != null) {
                resultParams = result.getAttributes();
                if (result.getError() != null) {
                    resultParams.put("error", "error");
                }
            } else
                resultParams = new HashMap<String, String>();
            return GlobalFunc.mapToStringArray(resultParams);
        }

        @Override
        protected void setQuery(String[] params) {

            if (mConnection == null || !mConnection.isConnected())
                return;

            final Map<String, String> inParams = GlobalFunc.stringArrayToMap(params);
            PicaData sendIQ = new PicaData();
            if (inParams.containsKey("resource")) {
                sendIQ.setFrom(mUser.getAddress().getAddress());
                inParams.remove("resource");
            }
            sendIQ.setAttributes(inParams);
            sendIQ.setType(IQ.Type.SET);
            sendIQ.setPacketID(Packet.nextID());
            DebugConfig.println("communicate " + sendIQ.toXML());
            sendPacket(sendIQ);
        }

        @Override
        protected PicaSearchResult getSearchResult(final Map<String, String> inParams) {
            PicaSearchData sendIQ = new PicaSearchData();
            sendIQ.setAttributes(inParams);
            sendIQ.setType(IQ.Type.GET);
            sendIQ.setPacketID(Packet.nextID());
            DebugConfig.println("communicate " + sendIQ.toXML());
            PacketFilter filter = new AndFilter(new PacketIDFilter(sendIQ.getPacketID()),
                    new PacketTypeFilter(IQ.class));
            PacketCollector collector = mConnection.createPacketCollector(filter);
            sendPacketDirect(sendIQ);
            PicaSearchData result = (PicaSearchData) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            // Stop queuing results
            collector.cancel();
            DebugConfig.println("search xml " + result.getChildElementXML());
            if (result.getError() != null)
                return null;

            PicaSearchResult outResult = result.getSearchResult();
            return outResult;
        }

        @Override
        public boolean sendVCardUpdatePresence(String[] params) {
            org.jivesoftware.smack.packet.Presence presence = new org.jivesoftware.smack.packet.Presence(
                    org.jivesoftware.smack.packet.Presence.Type.available);
            final Map<String, String> inParams = GlobalFunc.stringArrayToMap(params);
            if (inParams != null) {
                VCardUpdateExtension ve = new VCardUpdateExtension();
                if (inParams.get("hash") != null) {
                    ve.setPhotoHash(inParams.get("hash"));
                }
                // if (inParams.get("status") != null ) {
                // ve.setStatus(inParams.get("status"));
                // }
                presence.addExtension(ve);
                sendPacket(presence);
                return true;
            } else
                return false;

        }

    }

    public class XmppContactListManager extends ContactListManager {

        @Override
        public RosterEntry getRosterEntry(String address) {
            if (address == null)
                return null;

            return mRoster.getEntry(address);
        }

        @Override
        protected void setListNameAsync(final String name, final ContactList list) {
            if (name == null || list == null)
                return;

            execute(new Runnable() {
                @Override
                public void run() {
                    do_setListName(name, list);
                }
            });
        }

        // Runs in executor thread

        private void do_setListName(String name, ContactList list) {

            String listName = list.getName();

            if (listName == null)
                return;

            RosterGroup rg = mRoster.getGroup(listName);

            if (rg != null) {
                rg.setName(name);
                notifyContactListNameUpdated(list, name);
            }
        }

        @Override
        public String normalizeAddress(String address) {
            if (address == null)
                return null;

            return Address.stripResource(address);
        }

        @Override
        public void loadContactListsAsync(final Collection<String> addresses) {
            execute(new Runnable() {
                @SuppressWarnings("static-access")
                @Override
                public void run() {
                    do_loadContactLists(addresses);
                }
            });

        }

        public boolean addContact(Contact contact) {
            ContactList cl;
            try {
                cl = getContactListManager().getDefaultContactList();
            } catch (ImException e1) {
                cl = null;
                return false;
            }

            Contact c = cl.getContact(contact.getAddress().getAddress());
            if (c != null) {
                c.setName(contact.getName());
                return true;
            }

            if (cl != null && !cl.containsContact(contact)) {
                try {
                    cl.addExistingContact(contact);
                } catch (ImException e) {
                    DebugConfig.debug(TAG, "could not add contact to list: " + e.getLocalizedMessage());
                }
            }
            return true;
        }

        // Runs in executor thread

        private void do_loadContactLists(Collection<String> addresses) {

            Roster roster = null;

            if (mConnection != null && mConnection.isConnected()) {
                roster = mRoster; // mConnection.getRoster();
            }

            ContactList cl;

            try {
                cl = getContactListManager().getDefaultContactList();
            } catch (ImException e1) {
                DebugConfig.debug(TAG, "couldn't read default list");
                cl = null;
            }

            if (cl == null) {
                String generalGroupName = "buddies";
                Collection<Contact> contacts = new ArrayList<Contact>();
                XmppAddress groupAddress = new XmppAddress(generalGroupName);
                cl = new ContactList(groupAddress, generalGroupName, true, contacts, this);
                notifyContactListCreated(cl);
            }

            if (roster != null) {
                for (RosterEntry rEntry : roster.getEntries()) {
                    if (rEntry == null)
                        continue;

                    String address = rEntry.getUser();

                    if (address == null)
                        continue;

                    String name = rEntry.getName();

                    String picaname = "";
                    int pos = 0;

                    try {
                        if (name != null) {
                            pos = name.indexOf(",");
                            if (pos != -1 && pos != 0) {
                                picaname = name.substring(pos + 1);
                                name = name.substring(0, pos);
                            }
                        }
                    } catch (Exception e) {
                    }

                    if (mUser.getAddress().getBareAddress().equals(address)) // don't
                        // load
                        // a
                        // roster
                        // for
                        // yourself
                        continue;

                    Contact contact = getContactListManager().getContact(address);

                    if (contact == null) {
                        XmppAddress xAddr = new XmppAddress(address);
                        if (name == null)
                            name = "Unknown";

                        contact = new Contact(xAddr, name);
                    }

                    if (contact != null) {
                        try {
                            name = contact.getName();
                            if (name == null)
                                name = "Unknown";

                            //if (address.startsWith(name) || name.equals("Unknown")) {
                            String phoneName = GlobalFunc.getContactName(mContext, name);
                            if (phoneName != null && !phoneName.equals("")) {
                                name = phoneName;
                                rEntry.setName(name);
                            } else {
                                if (picaname != null && !picaname.equals("")) {
                                    name = picaname;
                                } else {
                                    final String[] tempNickName = {null};
                                    final String[] userid = {null};
                                    final String[] phone = {null};
                                    PicaApiUtility.getProfileInSyncMode(mContext, StringUtils.parseName(address), new MyXMLResponseHandler() {
                                        @Override
                                        public void onMySuccess(JSONObject response) {
                                            try {
                                                tempNickName[0] = response.getString(Imps.Contacts.NICKNAME);
                                                userid[0] = response.getString(Imps.Contacts.USERID);
                                                phone[0] = response.getString(Imps.Contacts.PHONE_NUMBER);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onMyFailure(int errcode) {

                                        }
                                    });

                                    if (tempNickName[0] != null)
                                        name = tempNickName[0];
                                    else
                                        name = "Unknown";
                                    contact.setUserid(userid[0]);
                                    contact.setPhoneNum(phone[0]);
                                }
                                rEntry.setName(name);
                            }
                            contact.setName(name);
                            //}
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        org.jivesoftware.smack.packet.Presence p = roster
                                .getPresence(contact.getAddress().getBareAddress());
                        contact.setPresence(new Presence(parsePresence(p), p.getStatus(), null, null,
                                Presence.CLIENT_TYPE_DEFAULT));

                        ItemType type = rEntry.getType();
                        String str_type = null;
                        if (type != null)
                            str_type = type.toString();

                        if (str_type != null && str_type.equals("none")) {
                            ItemStatus status = rEntry.getStatus();
                            if (status == null) {
                                continue;
                            } else {
                                if (status.toString().equals("subscribe")) {
                                    contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_TO;
                                }
                            }
                        } else if (str_type.equals("from")) {
                            contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_FROM;
                        } else if (str_type.equals("to")) {
                            contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_TO;
                        } else {
                            if (contact.contact_type == Imps.Contacts.TYPE_TEMPORARY)
                                contact.contact_type = Imps.Contacts.TYPE_NORMAL;

                            contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_BOTH;
                        }
                        if (!cl.containsContact(contact)) {
							/*
							 * try{ if ( isHidden(contact) ) { contact.type = 3;
							 * } }catch(Exception e){ e.printStackTrace(); }
							 *
							 *
							 * try{ if (
							 * isFavorite(contact.getAddress().getAddress()) ) {
							 * contact.type = 2; } }catch(Exception e){
							 * e.printStackTrace(); }
							 *
							 * try{ if ( isBlocked(contact) ) { contact.type =
							 * 1; } }catch(Exception e){ e.printStackTrace(); }
							 */

                            try {
                                cl.addExistingContact(contact);
                            } catch (ImException e) {
                                DebugConfig.debug(TAG, "could not add contact to list: " + e.getLocalizedMessage());
                            }
                        }
                    }
                }
            }

            if (addresses == null)
                getContactListManager().fillHiddenBlockedFavoriteList();

            notifyContactListLoaded(cl);
            notifyContactListsLoaded();

            try {
                notifyContactsPresenceUpdated(cl.getContacts().toArray(new Contact[cl.getContacts().size()]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

		/*
		 * class AddContactsRunnable implements Runnable { ContactList cl;
		 * public void setArg(ContactList _cl) { cl = _cl; }
		 *
		 * @Override public void run(){ if ( cl != null )
		 * notifyContactListLoaded(cl);
		 *
		 * notifyContactListsLoaded(); if ( cl != null )
		 * notifyContactsPresenceUpdated(cl.getContacts().toArray(new
		 * Contact[cl.getContacts().size()])); } }
		 */

        private String[] contact_projection = new String[]{Imps.Contacts.NICKNAME, Imps.Contacts.USERNAME,
                Imps.Contacts.TYPE, Imps.Contacts.SUBSCRIPTION_TYPE, Imps.Contacts.FAVORITE};

        public void setContactsToListFromDB() {
            ContactList cl;
            try {
                cl = getContactListManager().getDefaultContactList();
            } catch (ImException e1) {
                DebugConfig.debug(TAG, "couldn't read default list");
                cl = null;
            }

            if (cl == null) {
                String generalGroupName = "buddies";
                Collection<Contact> contacts = new ArrayList<Contact>();
                XmppAddress groupAddress = new XmppAddress(generalGroupName);
                cl = new ContactList(groupAddress, generalGroupName, true, contacts, this);
                notifyContactListCreated(cl);
            }

            Cursor cursor = null;

            try {
                String select = Imps.Contacts.ACCOUNT + "=?";
                String[] selectionArgs = {Long.toString(mAccountId)};

                cursor = mContentResolver.query(Imps.Contacts.CONTENT_URI, contact_projection,
                        select, selectionArgs, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String nickName = cursor.getString(0);
                        String userName = cursor.getString(1);
                        int type = cursor.getInt(2);
                        int sub_type = cursor.getInt(3);
                        int favor = cursor.getInt(4);

                        if (userName == null || userName.equals(""))
                            continue;

                        if (nickName == null)
                            continue;

                        Contact contact = getContactListManager().getContact(userName);
                        if (contact == null) {
                            XmppAddress xAddr = null;
                            try {
                                xAddr = new XmppAddress(userName);
                                contact = new Contact(xAddr, nickName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        contact.sub_type = sub_type;
                        contact.contact_type = type;

                        if (contact != null && !cl.containsContact(userName)) {
                            try {
                                if (type == Imps.Contacts.TYPE_HIDDEN) {
                                    if (!getContactListManager().mHiddenList.contains(contact))
                                        getContactListManager().mHiddenList.add(contact);
                                } else if (type == Imps.Contacts.TYPE_BLOCKED) {
                                    if (!getContactListManager().mBlockedList.contains(contact))
                                        getContactListManager().mBlockedList.add(contact);
                                }

                                if (favor == 1)
                                    getContactListManager().mFavoriteList.add(contact);

                                cl.addExistingContact(contact);
                            } catch (ImException e) {
                                DebugConfig.debug(TAG, "could not add contact to list: " + e.getLocalizedMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cursor != null)
                cursor.close();

            // notifyContactListLoaded(cl);
            notifyContactListsLoaded();
        }

        public void listenToRoster(final Roster roster) {

            roster.addRosterListener(rListener);
        }

        RosterListener rListener = new RosterListener() {
            @Override
            public void presenceChanged(org.jivesoftware.smack.packet.Presence presence) {
                // we are already monitoring all presence packets so this is
                // over kill
                // handlePresenceChanged(presence, null);
            }

            @Override
            public void entriesUpdated(Collection<String> addresses) {
            }

            @Override
            public void entriesDeleted(Collection<String> addresses) {
                ContactList cl;
                try {
                    cl = mContactListManager.getDefaultContactList();
                    NotificationManager nMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    for (String address : addresses) {
                        Contact contact = cl.getContact(address);
                        if (contact != null) {
                            cl.removeContact(contact);
                            GlobalFunc.clearHistoryMessages(mContext, contact.hashCode());
                            notifyContactListUpdated(cl, ContactListListener.LIST_CONTACT_REMOVED, contact);
                            nMgr.cancel(StatusBarNotifier.sub_notify_start_id + contact.hashCode());

                        }
                    }

                } catch (ImException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void entriesAdded(Collection<String> addresses) {
                loadContactListsAsync(addresses);
            }
        };

        class UpdateContactsRunnable implements Runnable {

            private ContactListManager mConMgr;
            private Collection<String> mAddresses;

            public UpdateContactsRunnable(ContactListManager conMgr, Collection<String> addresses) {
                mConMgr = conMgr;
                mAddresses = addresses;
            }

            public void run() {
                Collection<Contact> contacts = new ArrayList<Contact>();
                for (String address : mAddresses)
                    contacts.add(findOrCreateContact(address));
                mConMgr.notifyContactsPresenceUpdated(contacts.toArray(new Contact[contacts.size()]));
            }
        }

        @Override
        protected ImConnection getConnection() {
            return XmppConnection.this;
        }

        @Override
        protected void doDeleteContactListAsync(ContactList list) {
            DebugConfig.debug(TAG, "delete contact list " + list.getName());
        }

        @Override
        protected void doCreateContactListAsync(String name, Collection<Contact> contacts, boolean isDefault) {
            DebugConfig.debug(TAG, "create contact list " + name + " default " + isDefault);
        }

        @Override
        protected int doBlockContactAsync(String address, boolean block) {
            if (mConnection == null || !mConnection.isConnected())
                return ImErrorInfo.NETWORK_ERROR;

            if (mConnection != null && mConnection.isConnected()) {
                if (address.equals(mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain)) || address.startsWith("pls"))
                    return ImErrorInfo.INVALID_USERNAME;

                Contact contact = mContactListManager.getContact(address);

                if (address.startsWith("g"))
                    return ImErrorInfo.INVALID_USERNAME;

                if (block) {
                    Map<String, String> inParams = new HashMap<String, String>();
                    inParams.put("class", "buddy");
                    inParams.put("cmd", "insert_buddy_status");
                    inParams.put("jid", StringUtils.parseName(address));
                    inParams.put("type", "1");

                    String[] params = GlobalFunc.mapToStringArray(inParams);
                    String[] result = getFindFriendManager().setQueryForResult(params);
                    if (result == null)
                        return ImErrorInfo.ILLEGAL_SERVER_RESPONSE;

                    Map<String, String> outParams = GlobalFunc.stringArrayToMap(result);
                    if (outParams == null)
                        return ImErrorInfo.UNKNOWN_ERROR;

                    String res = outParams.get("result");

                    if (res == null || !res.equals("ok"))
                        return ImErrorInfo.UNKNOWN_ERROR;

                    try {
                        if (contact != null && !isBlocked(contact)) {
                            this.notifyBlockContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Map<String, String> inParams = new HashMap<String, String>();
                    inParams.put("class", "buddy");
                    inParams.put("cmd", "delete_buddy_status");
                    inParams.put("jid", StringUtils.parseName(address));
                    inParams.put("type", "1");

                    String[] params = GlobalFunc.mapToStringArray(inParams);
                    String[] result = getFindFriendManager().setQueryForResult(params);
                    if (result == null)
                        return ImErrorInfo.ILLEGAL_SERVER_RESPONSE;

                    Map<String, String> outParams = GlobalFunc.stringArrayToMap(result);
                    if (outParams == null)
                        return ImErrorInfo.UNKNOWN_ERROR;

                    String res = outParams.get("result");

                    if (res == null || !res.equals("ok"))
                        return ImErrorInfo.UNKNOWN_ERROR;
                    try {
                        if (contact != null && isBlocked(contact)) {
                            notifyBlockContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // offline ì²˜ë¦¬
                Contact contact = mContactListManager.getContact(address);
                if (contact == null)
                    return ImErrorInfo.ILLEGAL_CONTACT_ADDRESS;

                try {
                    if (block) {
                        if (isBlocked(contact)) {
                            this.notifyBlockContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }

                        int deliver_type = DatabaseUtils.getDeliverType(mContentResolver, address);

                        if ((deliver_type
                                & Imps.Contacts.DELIVER_TYPE_UNBLOCKED) == Imps.Contacts.DELIVER_TYPE_UNBLOCKED) {
                            deliver_type -= Imps.Contacts.DELIVER_TYPE_UNBLOCKED;
                        } else
                            deliver_type += Imps.Contacts.DELIVER_TYPE_BLOCKED;

                        if (DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type)) {
                            notifyBlockContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } else {
                        if (!isBlocked(contact)) {
                            this.notifyBlockContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }

                        int deliver_type = DatabaseUtils.getDeliverType(mContentResolver, address);

                        if ((deliver_type & Imps.Contacts.DELIVER_TYPE_BLOCKED) == Imps.Contacts.DELIVER_TYPE_BLOCKED) {
                            deliver_type -= Imps.Contacts.DELIVER_TYPE_BLOCKED;
                        } else
                            deliver_type += Imps.Contacts.DELIVER_TYPE_UNBLOCKED;

                        if (DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type)) {
                            notifyBlockContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ImErrorInfo.UNKNOWN_ERROR;
        }

        @Override
        protected void fillHiddenBlockedFavoriteList() {
            if (mConnection == null || !mConnection.isConnected())
                return;

            Map<String, String> inParams = new HashMap<String, String>();
            inParams.put("cmd", "buddy_status");
            String[] resultArray = getFindFriendManager().getQueryResult(GlobalFunc.mapToStringArray(inParams));
            if (resultArray == null)
                return;
            Map<String, String> outParams = GlobalFunc.stringArrayToMap(resultArray);
            if (outParams == null)
                return;

            String hiddenList = outParams.get("hidden_list");
            if (hiddenList != null) {
                try {
                    String[] sepHiddenList = hiddenList.split(",");
                    List<String> hiddenArrayList = new ArrayList<String>();
                    for (int i = 0; i < sepHiddenList.length; i++) {
                        String address = sepHiddenList[i] + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
                        hiddenArrayList.add(address);
                        Contact contact = getContact(address);

                        if (contact != null && !isHidden(contact)) {
                            contact.contact_type = Imps.Contacts.TYPE_HIDDEN;
                            this.notifyHideContact(contact, true);
                        }
                    }

                    for (int i = 0; i < mHiddenList.size(); i++) {
                        try {
                            Contact contact = mHiddenList.get(i);
                            if (contact != null && !hiddenArrayList.contains(contact.getAddress().getAddress())) {
                                this.notifyHideContact(contact, false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String blockedList = outParams.get("blocked_list");
            if (blockedList != null) {
                try {
                    String[] sepBlockedList = blockedList.split(",");
                    List<String> blockedArrayList = new ArrayList<String>();
                    for (int i = 0; i < sepBlockedList.length; i++) {
                        String name = sepBlockedList[i];
                        if (name == null || name.trim().length() == 0 || name.contains("@"))
                            continue;
                        String address = name + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
                        blockedArrayList.add(address);
                        Contact contact = getContact(address);

                        if (contact != null && !isBlocked(contact)) {
                            contact.contact_type = Imps.Contacts.TYPE_BLOCKED;
                            this.notifyBlockContact(contact, true);
                        } else if (contact == null) {
                            contact = makeContact(address);
                            try {
                                final Contact finalContact = contact;
                                PicaApiUtility.getProfileInSyncMode(mContext, StringUtils.parseName(address), new MyXMLResponseHandler() {
                                    @Override
                                    public void onMySuccess(JSONObject response) {
                                        try {
                                            String nickName;
                                            nickName = response.getString(Imps.Contacts.NICKNAME);
                                            if (nickName != null) {
                                                finalContact.setName(nickName);
                                            }

                                            String gender = response.getString(Imps.Contacts.GENDER);
                                            String region = response.getString(Imps.Contacts.REGION);
                                            String status = response.getString(Imps.Contacts.STATUS);

                                            finalContact.setProfile(status, gender, region);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onMyFailure(int errcode) {

                                    }
                                });

                                finalContact.contact_type = Imps.Contacts.TYPE_BLOCKED;
                                finalContact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;

                                if (!getContactListManager().mBlockedList.contains(finalContact))
                                    mBlockedList.add(finalContact);

                                ContactList cl = getDefaultContactList();
                                if (cl != null && cl.containsContact(address))
                                    cl.addExistingContact(finalContact);

                                this.notifyBlockContact(finalContact, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    for (int i = 0; i < mBlockedList.size(); i++) {
                        try {
                            Contact contact = mBlockedList.get(i);
                            if (contact != null && !blockedArrayList.contains(contact.getAddress().getAddress())) {
                                this.notifyBlockContact(contact, false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String favorList = outParams.get("favorite_list");
            if (favorList != null) {
                try {
                    String[] sepFavorList = favorList.split(",");
                    List<String> favorArrayList = new ArrayList<String>();
                    for (int i = 0; i < sepFavorList.length; i++) {
                        String address = sepFavorList[i] + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
                        favorArrayList.add(address);
                        Contact contact = getContact(address);

                        if (contact != null) {
                            contact.favor = 1;
                            this.notifyFavoriteContact(contact, true);
                        }
                    }

                    for (int i = 0; i < mFavoriteList.size(); i++) {
                        try {
                            Contact contact = mFavoriteList.get(i);
                            if (contact != null && !favorArrayList.contains(contact.getAddress().getAddress())) {
                                this.notifyFavoriteContact(contact, false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected int doFavoriteContact(String address, boolean favorite) {
            if (mConnection == null || !mConnection.isConnected())
                return ImErrorInfo.NETWORK_ERROR;

            if (mConnection != null && mConnection.isConnected()) {
                if (favorite) {
                    Map<String, String> inParams = new HashMap<String, String>();
                    inParams.put("class", "buddy");
                    inParams.put("cmd", "insert_buddy_status");
                    inParams.put("jid", StringUtils.parseName(address));
                    inParams.put("type", "2");

                    String[] params = GlobalFunc.mapToStringArray(inParams);
                    String[] result = getFindFriendManager().setQueryForResult(params);
                    if (result == null)
                        return ImErrorInfo.ILLEGAL_SERVER_RESPONSE;

                    Map<String, String> outParams = GlobalFunc.stringArrayToMap(result);
                    if (outParams == null)
                        return ImErrorInfo.UNKNOWN_ERROR;

                    String res = outParams.get("result");

                    if (res == null || !res.equals("ok"))
                        return ImErrorInfo.UNKNOWN_ERROR;

                    Contact contact = mContactListManager.getContact(address);

                    try {
                        if (contact != null && !isFavorite(address)) {
                            this.notifyFavoriteContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Map<String, String> inParams = new HashMap<String, String>();
                    inParams.put("class", "buddy");
                    inParams.put("cmd", "delete_buddy_status");
                    inParams.put("jid", StringUtils.parseName(address));
                    inParams.put("type", "2");

                    String[] params = GlobalFunc.mapToStringArray(inParams);
                    String[] result = getFindFriendManager().setQueryForResult(params);
                    if (result == null)
                        return ImErrorInfo.ILLEGAL_SERVER_RESPONSE;

                    Map<String, String> outParams = GlobalFunc.stringArrayToMap(result);
                    if (outParams == null)
                        return ImErrorInfo.UNKNOWN_ERROR;

                    String res = outParams.get("result");

                    if (res == null || !res.equals("ok"))
                        return ImErrorInfo.UNKNOWN_ERROR;

                    Contact contact = mContactListManager.getContact(address);
                    try {
                        if (contact != null && isFavorite(address)) {
                            this.notifyFavoriteContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // offline ì²˜ë¦¬
                Contact contact = mContactListManager.getContact(address);
                if (contact == null)
                    return ImErrorInfo.ILLEGAL_CONTACT_ADDRESS;

                try {
                    if (favorite) {
                        if (isFavorite(address)) {
                            this.notifyFavoriteContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }

                        int deliver_type = DatabaseUtils.getDeliverType(mContentResolver, address);

                        if ((deliver_type
                                & Imps.Contacts.DELIVER_TYPE_UNFAVORITE) == Imps.Contacts.DELIVER_TYPE_UNFAVORITE) {
                            deliver_type -= Imps.Contacts.DELIVER_TYPE_UNFAVORITE;
                        } else
                            deliver_type += Imps.Contacts.DELIVER_TYPE_FAVORITE;

                        if (DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type)) {
                            notifyFavoriteContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } else {
                        if (!isFavorite(address)) {
                            notifyFavoriteContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }

                        int deliver_type = DatabaseUtils.getDeliverType(mContentResolver, address);

                        if ((deliver_type
                                & Imps.Contacts.DELIVER_TYPE_FAVORITE) == Imps.Contacts.DELIVER_TYPE_FAVORITE) {
                            deliver_type -= Imps.Contacts.DELIVER_TYPE_FAVORITE;
                        } else
                            deliver_type += Imps.Contacts.DELIVER_TYPE_UNFAVORITE;

                        if (DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type)) {
                            notifyFavoriteContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return ImErrorInfo.UNKNOWN_ERROR;
        }

        @Override
        protected int doHideContactAsync(String address, boolean hide) {

            if (mConnection == null || !mConnection.isConnected())
                return ImErrorInfo.NETWORK_ERROR;

            if (mConnection != null && mConnection.isConnected()) {
                if (hide) {
                    Map<String, String> inParams = new HashMap<String, String>();
                    inParams.put("class", "buddy");
                    inParams.put("cmd", "insert_buddy_status");
                    inParams.put("jid", StringUtils.parseName(address));
                    inParams.put("type", "3");

                    String[] params = GlobalFunc.mapToStringArray(inParams);
                    String[] result = getFindFriendManager().setQueryForResult(params);
                    if (result == null)
                        return ImErrorInfo.ILLEGAL_SERVER_RESPONSE;

                    Map<String, String> outParams = GlobalFunc.stringArrayToMap(result);
                    if (outParams == null)
                        return ImErrorInfo.UNKNOWN_ERROR;

                    String res = outParams.get("result");

                    if (res == null || !res.equals("ok"))
                        return ImErrorInfo.UNKNOWN_ERROR;

                    Contact contact = mContactListManager.getContact(address);

                    try {
                        if (contact != null && !isHidden(contact)) {
                            this.notifyHideContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Map<String, String> inParams = new HashMap<String, String>();
                    inParams.put("class", "buddy");
                    inParams.put("cmd", "delete_buddy_status");
                    inParams.put("jid", StringUtils.parseName(address));
                    inParams.put("type", "3");

                    String[] params = GlobalFunc.mapToStringArray(inParams);
                    String[] result = getFindFriendManager().setQueryForResult(params);
                    if (result == null)
                        return ImErrorInfo.ILLEGAL_SERVER_RESPONSE;

                    Map<String, String> outParams = GlobalFunc.stringArrayToMap(result);
                    if (outParams == null)
                        return ImErrorInfo.UNKNOWN_ERROR;

                    String res = outParams.get("result");

                    if (res == null || !res.equals("ok"))
                        return ImErrorInfo.UNKNOWN_ERROR;

                    Contact contact = mContactListManager.getContact(address);
                    try {
                        if (contact != null && isHidden(contact)) {
                            this.notifyHideContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // offline ì²˜ë¦¬
                Contact contact = mContactListManager.getContact(address);

                if (contact == null)
                    return ImErrorInfo.ILLEGAL_CONTACT_ADDRESS;

                try {
                    if (hide) {
                        if (isHidden(contact)) {
                            this.notifyHideContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }

                        int deliver_type = DatabaseUtils.getDeliverType(mContentResolver, address);

                        if ((deliver_type
                                & Imps.Contacts.DELIVER_TYPE_UNHIDDEN) == Imps.Contacts.DELIVER_TYPE_UNHIDDEN) {
                            deliver_type -= Imps.Contacts.DELIVER_TYPE_UNHIDDEN;
                        } else
                            deliver_type += Imps.Contacts.DELIVER_TYPE_HIDDEN;

                        if (DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type)) {
                            notifyHideContact(contact, true);
                            return ImErrorInfo.NO_ERROR;
                        }
                    } else {
                        if (!isHidden(contact)) {
                            this.notifyHideContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }

                        int deliver_type = DatabaseUtils.getDeliverType(mContentResolver, address);

                        if ((deliver_type & Imps.Contacts.DELIVER_TYPE_HIDDEN) == Imps.Contacts.DELIVER_TYPE_HIDDEN) {
                            deliver_type -= Imps.Contacts.DELIVER_TYPE_HIDDEN;
                        } else
                            deliver_type += Imps.Contacts.DELIVER_TYPE_UNHIDDEN;

                        if (DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type)) {
                            notifyHideContact(contact, false);
                            return ImErrorInfo.NO_ERROR;
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ImErrorInfo.UNKNOWN_ERROR;
        }

        @Override
        protected void doAddContactToListAsync(Contact contact, ContactList list) throws ImException {
            DebugConfig.debug(TAG, "add contact to " + list.getName());
            Roster roster = mRoster;

            String[] groups = new String[]{list.getName()};
            try {
                roster.createEntry(contact.getAddress().getBareAddress(), contact.getName(), groups);
                // If contact exists locally, don't create another copy
                if (!list.containsContact(contact))
                    notifyContactListUpdated(list, ContactListListener.LIST_CONTACT_ADDED, contact);
                else
                    DebugConfig.debug(TAG, "skip adding existing contact locally " + contact.getName());
            } catch (XMPPException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void doRemoveContactFromListAsync(Contact contact, ContactList list) {
            if (mConnection == null)
                return;

            if (contact == null || list == null)
                return;

            Roster roster = mRoster;
            String address = contact.getAddress().getAddress();

            try {
                if (address != null) {
                    RosterEntry entry = roster.getEntry(address);
                    if (entry != null) {
                        if (list.containsContact(address)) {
                            list.removeContact(contact);
                            return;
                        }
                        roster.removeEntry(entry);
                        notifyContactListUpdated(list, ContactListListener.LIST_CONTACT_REMOVED, contact);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

		/*
		 * description:ì¹œêµ¬ìš”ì²­ì�„ ë³´ë‚¸ë‹¤. ì¹œêµ¬ëª©ë¡�ì—� ë„£ê³ 
		 * ì¹œêµ¬ì�˜ subscriptiontypeì™€ statusë¥¼ ì„¤ì •í•œë‹¤.
		 *
		 * @see
		 * com.multimediachat.app.im.engine.ContactListManager#requestSubscription
		 * (com.multimediachat.app.im.engine.Contact)
		 */

        @Override
        public void requestSubscription(Contact contact, String greeting) {
            ContactList defaultList = null;
            try {
                defaultList = getContactListManager().getDefaultContactList();
            } catch (ImException e) {
            }

            if (defaultList == null)
                return;

            // try
            {
                try {
                    Roster roster = mRoster;
                    String[] groups = new String[]{defaultList.getName()};
                    roster.createEntry(contact.getAddress().getBareAddress(), contact.getName(), groups);
                } catch (Exception e) {
                    e.printStackTrace();
                }

				/*
				 * org.jivesoftware.smack.packet.Presence response = new
				 * org.jivesoftware.smack.packet.Presence(
				 * org.jivesoftware.smack.packet.Presence.Type.subscribe);
				 * response.setFrom(getLoginUserName());
				 * response.setTo(contact.getAddress().getBareAddress());
				 * sendPacket(response);
				 */

                org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message(
                        contact.getAddress().getAddress(), org.jivesoftware.smack.packet.Message.Type.chat);
                msg.setBody(null);
                msg.setPacketID(Packet.nextID());

                if (greeting != null && !greeting.equals("")) {
                    Greeting greetingExtension = new Greeting(StringUtils.escapeForXML(greeting));
                    msg.addExtension(greetingExtension);
                }
                sendPacket(msg);
                DebugConfig.error("*****", String.format("requestSubscription:sendPacket:%s",contact.getAddress().getAddress()));

                try {
                    if (!defaultList.containsContact(contact)) {
                        contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_TO;
                        defaultList.addExistingContact(contact);
                    }
                } catch (Exception e) {
                }

                DatabaseUtils.insertOrUpdateSubscription(mContentResolver, defaultList, mProviderId, mAccountId,
                        contact.getAddress().getAddress(), contact.getName(), Imps.Contacts.SUBSCRIPTION_TYPE_TO,
                        Imps.Contacts.SUBSCRIPTION_STATUS_SUBSCRIBE_PENDING, greeting, contact.userid, contact.region, contact.phoneNum);
                try {
                    String address = contact.getAddress().getAddress();
                    if (!qAvatar.contains(address)) {
                        qAvatar.put(address);
                        if (avatarHashMap.containsKey(address))
                            avatarHashMap.remove(address);
                        loadVCardsAsync(true);
                    }
                } catch (InterruptedException ie) {
                }

            }
            // catch (XMPPException e){

            // }
        }

        @Override
        public void declineSubscriptionRequest(Contact contact) {
            org.jivesoftware.smack.packet.Presence response = new org.jivesoftware.smack.packet.Presence(
                    org.jivesoftware.smack.packet.Presence.Type.unsubscribe);
            response.setTo(contact.getAddress().getBareAddress());
            response.setProperty("request", "declined");
            sendPacket(response);

            mContactListManager.removeContact(contact.getAddress().getAddress());
            try {
                DatabaseUtils.deleteSubscription(mContentResolver, contact.getAddress().getAddress());

                mContactListManager.getSubscriptionRequestListener().onSubscriptionDeclined(contact, mProviderId,
                        mAccountId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void cancelRequest(Contact contact) {
            try {
                org.jivesoftware.smack.packet.Presence response = new org.jivesoftware.smack.packet.Presence(
                        org.jivesoftware.smack.packet.Presence.Type.unsubscribe);
                response.setProperty("request", "canceled");
                response.setFrom(getLoginUserName());
                response.setTo(contact.getAddress().getBareAddress());
                sendPacket(response);

                mContactListManager.removeContact(contact.getAddress().getAddress());

                if (DatabaseUtils.deleteSubscription(mContentResolver, contact.getAddress().getAddress()))
                    mContactListManager.getSubscriptionRequestListener().onCanceledRequest(contact, mProviderId,
                            mAccountId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void approveSubscriptionRequest(Contact contact) {

            try {
                if (contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM) {

                    RosterPacket packet = new RosterPacket();
                    packet.setType(IQ.Type.SET);
                    RosterPacket.Item item = new RosterPacket.Item(contact.getAddress().getAddress(),
                            contact.getName());
                    item.addGroupName(mContactListManager.getDefaultContactList().getName());
                    packet.addRosterItem(item);
                    sendPacket(packet);

                    /*org.jivesoftware.smack.packet.Presence response = new org.jivesoftware.smack.packet.Presence(
                            org.jivesoftware.smack.packet.Presence.Type.subscribed);
                    response.setFrom(getLoginUserName());
                    response.setTo(contact.getAddress().getBareAddress());
                    sendPacket(response);*/

                    org.jivesoftware.smack.packet.Presence response = new org.jivesoftware.smack.packet.Presence(
                            org.jivesoftware.smack.packet.Presence.Type.subscribe);
                    response.setFrom(getLoginUserName());
                    response.setTo(contact.getAddress().getBareAddress());
                    sendPacket(response);

                    try {
                        DatabaseUtils.insertOrUpdateSubscription(mContentResolver,
                                mContactListManager.getDefaultContactList(), mProviderId, mAccountId,
                                contact.getAddress().getAddress(), contact.getName(),
                                Imps.Contacts.SUBSCRIPTION_TYPE_BOTH, Imps.Contacts.SUBSCRIPTION_STATUS_NONE, null, contact.userid, contact.region, contact.phoneNum);

                        contact = mContactListManager.getContact(contact.getAddress().getAddress());
                        contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_BOTH;

                        if (contact.contact_type == Imps.Contacts.TYPE_TEMPORARY)
                            contact.contact_type = Imps.Contacts.TYPE_NORMAL;

                    } catch (Exception e) {
                    }
                } else if (contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_BOTH) {
                    org.jivesoftware.smack.packet.Presence response = new org.jivesoftware.smack.packet.Presence(
                            org.jivesoftware.smack.packet.Presence.Type.subscribed);
                    response.setFrom(getLoginUserName());
                    response.setTo(contact.getAddress().getBareAddress());
                    sendPacket(response);

                    return;
                } else if (contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_TO) {
                    org.jivesoftware.smack.packet.Presence response = new org.jivesoftware.smack.packet.Presence(
                            org.jivesoftware.smack.packet.Presence.Type.subscribed);
                    response.setFrom(getLoginUserName());
                    response.setTo(contact.getAddress().getBareAddress());
                    sendPacket(response);

                    org.jivesoftware.smack.packet.Presence presence = new org.jivesoftware.smack.packet.Presence(
                            Type.available);
                    String status = presence.getStatus();
                    Presence p = new Presence(parsePresence(presence), status, null, null,
                            Presence.CLIENT_TYPE_DEFAULT);
                    handleSubscribePresence2(contact, p);
                    return;
                }

                try {
                    mContactListManager.getSubscriptionRequestListener().onSubscriptionApproved(contact, mProviderId,
                            mAccountId);
                } catch (Exception e) {
                }

                Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
                mContext.sendBroadcast(intent);
            } catch (Exception e) {
                DebugConfig.debug(TAG, "error responding to subscription approval: " + e.getLocalizedMessage());
            }
        }

        @Override
        public Contact[] createTemporaryContacts(String[] addresses) {

            if (addresses.length < 1)
                return null;

            Contact[] contacts = new Contact[addresses.length];

            int i = 0;

            for (String address : addresses) {
                loadVcard(address, null, true);
                contacts[i++] = makeContact(address);
            }

            notifyContactsPresenceUpdated(contacts);
            return contacts;
        }

        @Override
        protected int doSetContactName(String address, String name) throws ImException {

            if (mConnection != null && mConnection.isConnected()) {
                Roster roster = mRoster;
                RosterEntry entry = roster.getEntry(address);
                // confirm entry still exists
                if (entry == null) {
                    return ImErrorInfo.ILLEGAL_CONTACT_ADDRESS;
                }
                // set name
                entry.setName(name);
            } else {
                int deliver_type = DatabaseUtils.getDeliverType(mContentResolver, address);

                if ((deliver_type & Imps.Contacts.DELIVER_TYPE_RENAMED) != Imps.Contacts.DELIVER_TYPE_RENAMED) {
                    deliver_type += Imps.Contacts.DELIVER_TYPE_RENAMED;
                    if (!DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type)) {
                        return ImErrorInfo.UNKNOWN_ERROR;
                    }
                }
            }
            return ImErrorInfo.NO_ERROR;
        }

        public void doProcessCorrectProfileInfoAsync() {
			/*
			 * execute(new Runnable() {
			 *
			 * @Override public void run() { try{
			 * Thread.currentThread().sleep(10000); }catch(Exception e){
			 * e.printStackTrace(); } doProcessCorrectProfileInfo(); } });
			 */
        }

        public void doProcessUndeliveredContactsAsync() {

            execute(new Runnable() {
                @SuppressWarnings("static-access")
                @Override
                public void run() {
                    try {
                        Thread.currentThread().sleep(10000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    doProcessUndeliveredContacts();
                }
            });
        }

        public void doProcessUndeliveredContacts() {
            Cursor cursor = null;
            try {
                String select = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.ISDELIVERD + "=?";
                String[] selectionArgs = {Long.toString(mAccountId), "-1"};

                cursor = mContentResolver.query(Imps.Contacts.CONTENT_URI,
                        new String[]{Imps.Contacts.USERNAME, Imps.Contacts.DELIVERTYPE, Imps.Contacts.NICKNAME},
                        select, selectionArgs, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String address = null;
                        String nickName = null;
                        int deliver_type = Imps.Contacts.DELIVER_TYPE_NORMAL;

                        try {
                            address = cursor.getString(0);
                            deliver_type = cursor.getInt(1);
                            nickName = cursor.getString(2);
                        } catch (Exception e) {
                        }

                        if (address != null) {
                            if ((deliver_type
                                    & Imps.Contacts.DELIVER_TYPE_HIDDEN) == Imps.Contacts.DELIVER_TYPE_HIDDEN) {
                                int res = doHideContactAsync(address, true);
                                if (res == ImErrorInfo.NO_ERROR && mConnection != null && mConnection.isConnected()) {
                                    deliver_type -= Imps.Contacts.DELIVER_TYPE_HIDDEN;
                                    DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type);
                                }
                            } else if ((deliver_type
                                    & Imps.Contacts.DELIVER_TYPE_UNHIDDEN) == Imps.Contacts.DELIVER_TYPE_UNHIDDEN) {
                                int res = doHideContactAsync(address, false);

                                if (res == ImErrorInfo.NO_ERROR && mConnection != null && mConnection.isConnected()) {
                                    deliver_type -= Imps.Contacts.DELIVER_TYPE_UNHIDDEN;
                                    DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type);
                                }
                            }

                            if ((deliver_type
                                    & Imps.Contacts.DELIVER_TYPE_BLOCKED) == Imps.Contacts.DELIVER_TYPE_BLOCKED) {
                                int res = doBlockContactAsync(address, true);
                                if (res == ImErrorInfo.NO_ERROR && mConnection != null && mConnection.isConnected()) {
                                    deliver_type -= Imps.Contacts.DELIVER_TYPE_BLOCKED;
                                    DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type);
                                }
                            } else if ((deliver_type
                                    & Imps.Contacts.DELIVER_TYPE_UNBLOCKED) == Imps.Contacts.DELIVER_TYPE_UNBLOCKED) {
                                int res = doBlockContactAsync(address, false);

                                if (res == ImErrorInfo.NO_ERROR && mConnection != null && mConnection.isConnected()) {
                                    deliver_type -= Imps.Contacts.DELIVER_TYPE_UNBLOCKED;
                                    DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type);
                                }
                            }

                            if ((deliver_type
                                    & Imps.Contacts.DELIVER_TYPE_FAVORITE) == Imps.Contacts.DELIVER_TYPE_FAVORITE) {
                                int res = doFavoriteContact(address, true);
                                if (res == ImErrorInfo.NO_ERROR && mConnection != null && mConnection.isConnected()) {
                                    deliver_type -= Imps.Contacts.DELIVER_TYPE_FAVORITE;
                                    DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type);
                                }
                            } else if ((deliver_type
                                    & Imps.Contacts.DELIVER_TYPE_UNFAVORITE) == Imps.Contacts.DELIVER_TYPE_UNFAVORITE) {
                                int res = doFavoriteContact(address, false);
                                if (res == ImErrorInfo.NO_ERROR && mConnection != null && mConnection.isConnected()) {
                                    deliver_type -= Imps.Contacts.DELIVER_TYPE_UNFAVORITE;
                                    DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type);
                                }
                            }

                            if ((deliver_type
                                    & Imps.Contacts.DELIVER_TYPE_RENAMED) == Imps.Contacts.DELIVER_TYPE_RENAMED) {
                                int res = ImErrorInfo.UNKNOWN_ERROR;
                                if (nickName != null && !nickName.equals("")) {
                                    res = setContactName(address, nickName);
                                }
                                if (res == ImErrorInfo.NO_ERROR && mConnection != null && mConnection.isConnected()) {
                                    deliver_type -= Imps.Contacts.DELIVER_TYPE_RENAMED;
                                    DatabaseUtils.updateDeliverType(mContentResolver, address, deliver_type);
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cursor != null)
                cursor.close();
        }

        public void doProcessNonFriends() {
            if (mRoster == null)
                return;

            ContactList cl = null;
            try {
                cl = getDefaultContactList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cl == null)
                return;

            Contact[] contacts = cl.getContacts().toArray(new Contact[cl.getContacts().size()]);
            if (contacts == null)
                return;

            for (int i = 0; i < contacts.length; i++) {
                Contact contact = contacts[i];
                String contactAddr = contact.getAddress().getAddress();

                if (contact.contact_type == Imps.Contacts.TYPE_GROUP
                        || contact.contact_type == Imps.Contacts.TYPE_PLUS_FRIEND)
                    continue;

                RosterEntry entry = mRoster.getEntry(contactAddr);
                if (entry == null) {
                    if (contact.sub_type != Imps.Contacts.SUBSCRIPTION_TYPE_FROM) {
                        contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;
                        notifyContactListUpdated(cl, ContactListListener.CONTACT_UPDATE, contact);
                    }
                }
            }

			/*
			 * Cursor cursor = null; try{ cursor =
			 * mContentResolver.query(Imps.Contacts.CONTENT_URI, new
			 * String[]{Imps.Contacts.USERNAME, Imps.Contacts.SUBSCRIPTION_TYPE,
			 * Imps.Contacts.TYPE}, null, null, null);
			 * while(cursor.moveToNext()){ String username =
			 * cursor.getString(0); int sub_type = cursor.getInt(1); int type =
			 * cursor.getInt(2);
			 *
			 *
			 * } }catch(Exception e){ e.printStackTrace(); }finally{ if ( cursor
			 * != null ) { cursor.close(); cursor = null; } }
			 */
        }
    }

    public void sendHeartbeat(final long heartbeatInterval) {
        // Don't let heartbeats queue up if we have long running tasks - only
        // do the heartbeat if executor is idle.
        boolean success = executeIfIdle(new Runnable() {
            @Override
            public void run() {
                DebugConfig.debug(TAG, "heartbeat state = " + getState());
                doHeartbeat(heartbeatInterval);
            }
        });

        if (!success) {
            DebugConfig.debug(TAG, "failed to schedule heartbeat state = " + getState());
        }
    }

    // Runs in executor thread
    public void doHeartbeat(long heartbeatInterval) {
        DebugConfig.debug(TAG, "XmppConnection.java / doHeartbeat()");
        heartbeatSequence++;
        timedelaySequence++;

        if (mConnection == null && mRetryLogin) {
            DebugConfig.debug(TAG, "reconnect with login");
            do_login();
        }

        if (mConnection == null)
            return;

        if (getState() == SUSPENDED) {
            DebugConfig.debug(TAG, "heartbeat during suspend");
            if (((MessengerService) mContext).isNetworkAvailable()) {
                setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, "network disconnected"));
                force_reconnect();
            }
            return;
        }

        if (mNeedReconnect) {
            reconnect();
        } else if (!mConnection.isConnected() && getState() == LOGGED_IN) {
            // Smack failed to tell us about a disconnect
            DebugConfig.debug(TAG, "reconnect on unreported state change");
            setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, "network disconnected"));
            force_reconnect();
        } else if (getState() == LOGGED_IN) {
            if (PING_ENABLED) {
                // Check ping on every heartbeat. checkPing() will return true
                // immediately if we already checked.
                if (!checkPing()) {
                    DebugConfig.debug(TAG, "reconnect on ping failed");
                    setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, "network timeout"));
                    force_reconnect();
                } else {
                    // Send pings only at intervals configured by the user
                    if (heartbeatSequence >= heartbeatInterval) {
                        heartbeatSequence = 0;
                        DebugConfig.debug(TAG, "ping");
                        sendPing();
                    }
                }
            }

            if (timedelaySequence >= timedelayInterval) {
                timedelaySequence = 0;
            }

        }
        if (LinphoneManager.isInstanciated()) {
            if (LinphoneManager.getLc().getCallsNb() == 0)
                LinphoneManager.getLc().refreshRegisters();
        }
    }

    private void clearPing() {
        mPingCollector = null;
        heartbeatSequence = 0;
    }

    // Runs in executor thread
    private void sendPing() {

        IQ req = new IQ() {
            public String getChildElementXML() {
                return "<ping xmlns='urn:xmpp:ping'/>";
            }
        };
        req.setType(IQ.Type.GET);
        PacketFilter filter = new AndFilter(new PacketIDFilter(req.getPacketID()), new PacketTypeFilter(IQ.class));
        try {
            mPingCollector = mConnection.createPacketCollector(filter);
            mConnection.sendPacket(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Runs in executor thread
    private boolean checkPing() {

        if (mPingCollector != null) {
            IQ result = (IQ) mPingCollector.pollResult();
            mPingCollector.cancel();
            mPingCollector = null;
            if (result == null) {
                return false;
            } else if (result.getType() == IQ.Type.ERROR) { // lightsky
                // cii_remove
                String error = result.getError().toString();

                if (error.equals("recipient-unavailable(404)")) {
                    Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_DELETE_ACCOUNT);
                    aContext.sendBroadcast(intent);
                    mPref.putInt("AccountError", 1);
                } else if (error.contains("redirect")) {
                    Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_CHANGE_ACCOUNT);
                    aContext.sendBroadcast(intent);
                    mPref.putInt("AccountError", 2);
                }
            }
        }
        return true;
    }

    public static class MyXMPPConnection extends XMPPConnection {

        public MyXMPPConnection(ConnectionConfiguration config) {
            super(config);
        }

        public void shutdown() {

            try {
                // Be forceful in shutting down since SSL can get stuck
                try {
                    socket.shutdownInput();
                    socket.close();

                } catch (Exception e) {
                }

                shutdown(new org.jivesoftware.smack.packet.Presence(
                        org.jivesoftware.smack.packet.Presence.Type.unavailable));

            } catch (Exception e) {
                DebugConfig.error(TAG, "error on shutdown()", e);
            }
        }

        // void sendPacket(StreamHandlingPacket resumePacket) {
        // if (connection == null)
        // return;
        // try {
        // org.jivesoftware.smack.packet.Message message = new
        // org.jivesoftware.smack.packet.Message();
        // message.setBody(chatMsg.getMsg());
        // message.setTo(chatMsg.getUser());
        // connection.sendPacket(message);
        // } catch (NotConnectedException e) {
        // e.printStackTrace();
        // }
        // }
    }

    @Override
    public void networkTypeChanged() {
        super.networkTypeChanged();

        // this.maybe_reconnect();
    }

    /*
	 * Force a shutdown and reconnect, unless we are already reconnecting.
	 *
	 * Runs in executor thread
	 */
    private void force_reconnect() {
        DebugConfig.debug(TAG, "force_reconnect need=" + mNeedReconnect);
        if (mConnection == null)
            return;
        if (mNeedReconnect)
            return;

        mNeedReconnect = true;

        try {
            if (mConnection != null && mConnection.isConnected()) {
                mStreamHandler.quickShutdown();
            }
        } catch (Exception e) {
            DebugConfig.debug(TAG, "problem disconnecting on force_reconnect: ", e);
        }

        reconnect();
    }

    /*
	 * Reconnect unless we are already in the process of doing so.
	 *
	 * Runs in executor thread.
	 */
    private void maybe_reconnect() {
        DebugConfig.debug(TAG, "maybe_reconnect mNeedReconnect=" + mNeedReconnect + " state=" + getState()
                + " connection?=" + (mConnection != null));

        // This is checking whether we are already in the process of
        // reconnecting. If we are,
        // doHeartbeat will take care of reconnecting.
        if (mNeedReconnect)
            return;

        if (getState() == SUSPENDED)
            return;

        if (mConnection == null)
            return;

        mNeedReconnect = true;
        reconnect();
    }

    /*
	 * Retry connecting
	 *
	 * Runs in executor thread
	 */
    @SuppressWarnings("static-access")
    private void reconnect() {
        if (getState() == SUSPENDED) {
            DebugConfig.debug(TAG, "reconnect during suspend, ignoring");
            return;
        }

        try {
            if (!((MessengerService) mContext).isNetworkAvailable()) {
                Thread.currentThread().sleep(2000); // Wait for network to
                // settle
            }
        } catch (InterruptedException e) { /* ignore */
        }

        if (mConnection != null) {
            if (mConnection.isConnected()) {
                DebugConfig.debug(TAG, "reconnect while already connected, assuming good");
                mNeedReconnect = false;
                setState(LOGGED_IN, null);
                return;
            }
            clearPing();
            try {
                if (mStreamHandler.isResumePossible()) {
                    // mConnection.connect(); //ccj
                    mConnection.connect(false);
                } else {
                    mConnection = null;
                    do_login();
                }
            } catch (Exception e) {
                mStreamHandler.quickShutdown();
                DebugConfig.debug(TAG, "reconnection attempt failed", e);
                // Smack incorrectly notified us that reconnection was
                // successful, reset in case it fails
                mNeedReconnect = true;
                setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, e.getMessage()));
            }
        } else {
            mNeedReconnect = true;

            DebugConfig.debug(TAG, "reconnection on network change failed");

            setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, "reconnection on network change failed"));
        }
    }

    @Override
    protected void setState(int state, ImErrorInfo error) {
        DebugConfig.debug(TAG, "setState to " + state);
        String stateStr = "";
        int connstate = ConnectionState.eCONNSTAT_XMPP_DISCONNECTED;
        switch (state) {
            case DISCONNECTED:
                stateStr = "Disconnected";
                break;
            case LOGGING_IN:
                stateStr = "LoggingIn";
                break;
            case LOGGED_IN:
                stateStr = "LoggedIn";
                connstate = ConnectionState.eCONNSTAT_XMPP_CONNECTED;
                break;
            case LOGGING_OUT:
                stateStr = "LogginOut";
                break;
            case SUSPENDING:
                stateStr = "Suspending";
                break;
            case SUSPENDED:
                stateStr = "Suspended";
                break;
        }

        if (GlobalFunc.getXmppConnStatus() != connstate) {
            GlobalFunc.SetXmppConnectStatPref(mContext, connstate);
//			StatusBarNotifier.notifyServerState(mContext, connstate, null);
        }
        super.setState(state, error);
    }

    @Override
    public void handle(Callback[] arg0) throws IOException {

        for (Callback cb : arg0) {
            DebugConfig.debug(TAG, cb.toString());
        }

    }

    private void onReconnectionSuccessful() {
        DebugConfig.println(" ------- reconnection successful ------- ");
        mNeedReconnect = false;
        setState(LOGGED_IN, null);
    }

    private void addProviderManagerExtensions() {

        ProviderManager pm = ProviderManager.getInstance();
        // PicaData
        pm.addIQProvider("query", "urn:xmpp:picatalk", new PicaData.Provider());
        pm.addIQProvider("query", "urn:xmpp:picasearch", new PicaSearchData.Provider());
        // Private Data Storage
        pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());

        // Time
        try {
            pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
        }

        // Roster Exchange
        pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());

        // Message Events
        pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());

        // Chat State
        pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

        // XHTML
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

        // Group Chat Invitations
        pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());

        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());

        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

        // Data Forms
        pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

        // MUC User
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());

        // MUC Admin
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

        // MUC Owner
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

        // Delayed Delivery
        pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());
        pm.addExtensionProvider("delay", "urn:xmpp:delay", new DelayInformationProvider());

        // Version
        try {
            pm.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }

        // VCard
        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        // Offline Message Requests
        pm.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());

        // Offline Message Indicator
        pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());

        // Last Activity
        pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

        // User Search
        pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

        // SharedGroupsInfo
        pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup",
                new SharedGroupsInfo.Provider());

        // JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());

        // FileTransfer
        pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
        pm.addIQProvider("open", "http://jabber.org/protocol/ibb", new OpenIQProvider());
        pm.addIQProvider("close", "http://jabber.org/protocol/ibb", new CloseIQProvider());
        pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new DataPacketProvider());

        // Privacy
        pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        pm.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
        pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.MalformedActionError());
        pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadLocaleError());
        pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadPayloadError());
        pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadSessionIDError());
        pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.SessionExpiredError());

    }

    class NameSpace {

        // public static final String DISCO_INFO =
        // "http://jabber.org/protocol/disco#info";
        // public static final String DISCO_ITEMS =
        // "http://jabber.org/protocol/disco#items";
        // public static final String IQ_GATEWAY = "jabber:iq:gateway";
        // public static final String IQ_GATEWAY_REGISTER =
        // "jabber:iq:gateway:register";
        // public static final String IQ_LAST = "jabber:iq:last";
        // public static final String IQ_REGISTER = "jabber:iq:register";
        // public static final String IQ_REGISTERED = "jabber:iq:registered";
        // public static final String IQ_ROSTER = "jabber:iq:roster";
        // public static final String IQ_VERSION = "jabber:iq:version";
        // public static final String CHATSTATES =
        // "http://jabber.org/protocol/chatstates";
        // public static final String XEVENT = "jabber:x:event";
        // public static final String XDATA = "jabber:x:data";
        // public static final String MUC = "http://jabber.org/protocol/muc";
        // public static final String MUC_USER = MUC + "#user";
        // public static final String MUC_ADMIN = MUC + "#admin";
        // public static final String SPARKNS =
        // "http://www.jivesoftware.com/spark";
        // public static final String DELAY = "urn:xmpp:delay";
        // public static final String OFFLINE =
        // "http://jabber.org/protocol/offline";
        // public static final String X_DELAY = "jabber:x:delay";
        // public static final String VCARD_TEMP = "vcard-temp";
        public static final String VCARD_TEMP_X_UPDATE = "vcard-temp:x:update";
        // public static final String ATTENTIONNS = "urn:xmpp:attention:0";
        public static final String CONFERENCE = "jabber:x:conference";

    }

    public boolean registerAccount(Imps.ProviderSettings.QueryMap providerSettings, String username, String password)
            throws Exception {

        initConnection(providerSettings, username);

        if (mConnection.getAccountManager().supportsAccountCreation()) {
            mConnection.getAccountManager().createAccount(username, password);

            return true;

        } else {
            return false;// not supported
        }

    }

	/*
	 * light
	 */

    /**
     * @param providerSettings
     * @param username
     * @param password
     * @param token
     * @param tokenValue
     * @param vtoken
     * @param vtokenValue
     * @return
     * @throws Exception
     */
    public String[] signinAccount(Imps.ProviderSettings.QueryMap providerSettings, String username, String password,
                                  String token, String tokenValue, Vector<String> vtoken, Vector<String> vtokenValue) throws Exception {

        initConnection(providerSettings, username);

        if (mConnection.getAccountManager().supportsAccountCreation()) {
            return communicate(username, password, token, tokenValue, vtoken, vtokenValue);
        } else {
            return null;
        }
    }

    /**
     * @param username    : xmpp user Jid, here is phone nummber
     * @param password    : initial password
     * @param token       :
     * @param tokenValue  :
     * @param vtoken      :
     * @param vtokenValue
     * @throws XMPPException
     */
    public String[] communicate(String username, String password, String token, String tokenValue,
                                Vector<String> vtoken, Vector<String> vtokenValue) throws XMPPException {

        Map<String, String> attributes = new HashMap<String, String>();

        Registration reg = new Registration();
        reg.setType(IQ.Type.SET);
        reg.setTo(mConnection.getServiceName());
        attributes.put("username", StringUtils.escapeForXML(username.trim()));
        attributes.put("password", StringUtils.escapeForXML(password.trim()));

        attributes.put("token", StringUtils.escapeForXML(token.trim()));
        attributes.put("tokenValue", StringUtils.escapeForXML(tokenValue.trim()));

        if (vtoken != null) {
            for (int i = 0; i < vtoken.size(); i++) {
                attributes.put(StringUtils.escapeForXML(vtoken.elementAt(i)),
                        StringUtils.escapeForXML(vtokenValue.elementAt(i)));
            }
        }

        reg.setAttributes(attributes);
        PacketFilter filter = new AndFilter(new PacketIDFilter(reg.getPacketID()), new PacketTypeFilter(IQ.class));
        PacketCollector collector = mConnection.createPacketCollector(filter);

        if (mConnection == null || !mConnection.isConnected()) {
            return null;
        }

        sendPacketDirect(reg);
        IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        // Stop queuing results
        collector.cancel();
        String rlt[] = new String[3]; // token 0, tokenValue 1, state 2
        if (result.getType() == IQ.Type.ERROR) {
            rlt[0] = result.getError().toString();
        } else if (result.getType() == IQ.Type.RESULT) {
            rlt[0] = DownloadData.getTagValue("token", result.toXML());
            try {
                rlt[1] = DownloadData.getTagValue("tokenValue", result.toXML());
                rlt[2] = DownloadData.getTagValue("state", result.toXML());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rlt;
    }

    public Map<String, String> communicate(final Map<String, String> params) throws XMPPException {
        IQ sendIQ = new IQ() {
            public String getChildElementXML() {
                if (params.isEmpty())
                    return "<query xmlns='urn:xmpp:picatalk'/>";
                else {
                    StringBuilder buf = new StringBuilder();
                    buf.append("<query xmlns='urn:xmpp:picatalk'");
                    for (String key : params.keySet()) {
                        buf.append(" ").append(key).append("=\"").append(StringUtils.escapeForXML(params.get(key)))
                                .append("\"");
                    }
                    buf.append("/>");
                    return buf.toString();
                }
            }
        };

        sendIQ.setPacketID(Packet.nextID());
        sendIQ.setType(IQ.Type.SET);
        DebugConfig.println("communicate " + sendIQ.toXML());
        PacketFilter filter = new AndFilter(new PacketIDFilter(sendIQ.getPacketID()), new PacketTypeFilter(IQ.class));
        PacketCollector collector = mConnection.createPacketCollector(filter);
        sendPacketDirect(sendIQ);
        IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        // Stop queuing results
        collector.cancel();
        Map<String, String> resultParams = new HashMap<String, String>();
        if (result.getError() != null) {
            resultParams.put("error", "error");
        } else {
            Collection<String> resultKeys = result.getPropertyNames();
            for (String key : resultKeys) {
                resultParams.put(key, (String) result.getProperty(key));
            }
        }
        return resultParams;
    }

    private void handlePresenceChanged(org.jivesoftware.smack.packet.Presence presence, String greeting) {
        if (mConnection == null)
            return; // sometimes presence changes are queued, and get called
        // after we sign off

        if (presence.getError() != null) {
            return;
        }
        XmppAddress xaddress = new XmppAddress(presence.getFrom());

        String bareAddr = xaddress.getBareAddress();
        if (mUser.getAddress().getBareAddress().equals(bareAddr)) // ignore
        // presence
        // from
        // yourself
        {
            return;
        }

        try {
            if (mContactListManager.isBlocked(xaddress.getBareAddress()))
                return;
        } catch (Exception e) {
        }
        String status = presence.getStatus();
        Contact contact = mContactListManager.getContact(bareAddr);
        Presence p = new Presence(parsePresence(presence), status, null, null, Presence.CLIENT_TYPE_DEFAULT);

        String[] presenceParts = presence.getFrom().split("/");
        if (presenceParts.length > 1)
            p.setResource(presenceParts[1]);

        if (presence.getExtension("x", "http://jabber.org/protocol/muc#user") != null) {
            handleMUCPresence(presenceParts, presence);
            return;
        }

        if ((contact == null || contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_NONE || contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM) && presence.getType() == Type.subscribe) {
            contact = handleSubscribePresence(presence, p);
            org.jivesoftware.smack.packet.Presence response = new org.jivesoftware.smack.packet.Presence(
                    org.jivesoftware.smack.packet.Presence.Type.subscribed);
            response.setFrom(getLoginUserName());
            response.setTo(contact.getAddress().getBareAddress());
            sendPacket(response);
        } else if (contact == null) {
        } else if (presence.getType() == Type.subscribe) {
            getContactListManager().approveSubscriptionRequest(contact);
        } else if (presence.getType() == Type.subscribed && contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM) {
            handleSubscribePresence2(contact, p);
        } else if (presence.getType() == Type.unsubscribe) {
            handleUnsubscribePresence(presence, contact);
        } else if (presence.getType() == Type.unsubscribed) {
            handleUnsubscribedPresence(contact);
        }

        Intent i = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
        mContext.sendBroadcast(i);

        if (contact == null)
            return;

        contact.setPresence(p);
        mContactListManager.notifyContactsPresenceUpdated(new Contact[]{contact});
        PacketExtension pe = presence.getExtension("x", NameSpace.VCARD_TEMP_X_UPDATE);
        if (pe != null) {
            try {
                // String hash = ((DefaultPacketExtension)pe).getValue("photo");

                if (!qAvatar.contains(bareAddr)) {
                    qAvatar.put(bareAddr);

					/*
					 * if ( avatarHashMap.containsKey(bareAddr) )
					 * avatarHashMap.cii_remove(bareAddr); if ( hash != null )
					 * avatarHashMap.put(bareAddr, hash);
					 */

                    loadVCardsAsync(false);
                }
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    private void handleMUCPresence(String[] presenceParts, org.jivesoftware.smack.packet.Presence presence) {

        if (presence.getStatus().equals("closeroom")) {
            mChatGroupManager.leaveRoom(presenceParts[0]);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.group_room_leave);
            return;
        }

        String str[] = presenceParts[0].split("@");
        String jid = presenceParts[1] + "@" + str[1].split("conference.")[1];

        if (presenceParts[0] == null || presenceParts[1] == null)
            return;

        if (presenceParts[1].equals(mUser.getAddress().getUser()))
            return;

        MUCUser mucUser = (MUCUser) presence.getExtension("x", "http://jabber.org/protocol/muc#user");
        if (mucUser != null) {
            //MUCUser.Item item = mucUser.getItem();
            // String aff = item.getAffiliation();
            // String role = item.getRole();

            if (presence.getStatus().equals("leaveroom")) {
                ChatGroup chatGroup = mChatGroupManager.getChatGroup(presenceParts[0]);
                if (chatGroup == null)
                    return;

                Contact groupMember = chatGroup.getMember(jid);
                if (groupMember != null) {
                    chatGroup.removeMemberAsync(groupMember);
                }
            } else {

                final ChatGroup chatGroup = mChatGroupManager.getChatGroup(presenceParts[0]);
                if (chatGroup == null)
                    return;

                Contact groupMember = chatGroup.getMember(jid);
                if (groupMember != null) {
                    String status = presence.getStatus();
                    Presence p = new Presence(parsePresence(presence), status, null, null, Presence.CLIENT_TYPE_DEFAULT);
                    groupMember.setPresence(p);
                } else {
                    final Address address = new XmppAddress(jid);
                    PicaApiUtility.getProfile(mContext, StringUtils.parseName(address.getAddress()), new MyXMLResponseHandler() {
                        @Override
                        public void onMySuccess(JSONObject response) {
                            String nickName = "";

                            try {
                                nickName = response.getString(Imps.Contacts.NICKNAME);
                                Contact groupMember = new Contact(address, nickName);
                                if (groupMember != null) {
                                    chatGroup.addMemberAsync(groupMember);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onMyFailure(int errcode) {

                        }
                    });
                }
            }
        }

		/*
		 * ChatGroup chatGroup =
		 * mChatGroupManager.getChatGroup(presenceParts[0]); if ( chatGroup ==
		 * null ) return;
		 *
		 * if ( presence == null ) { Contact groupMember =
		 * chatGroup.getMember(jid); if ( groupMember == null ) { groupMember =
		 * makeGroupMember(jid); groupMember.setPresence(null);
		 * chatGroup.addMemberAsync(groupMember); } return; }
		 *
		 *
		 * if ( presence.getType() == Type.unavailable ) // by cys {
		 *
		 * XmppAddress xAddr = new XmppAddress(jid); Contact groupMember =
		 * chatGroup.getMember(xAddr.getAddress()); if ( groupMember != null)
		 * chatGroup.updateGroupMemberLeft(groupMember); } else{ try { String
		 * userName = mConnection.getUser(); String userBareName =
		 * userName.split("@")[0];
		 *
		 * if ( presenceParts[1] != null && userBareName != null) {
		 *
		 * if ( presenceParts[1].equals(userBareName)) // do not process
		 * yourself. { return; } } }catch(Exception e){ e.printStackTrace(); }
		 * Contact groupMember = chatGroup.getMember(jid); if ( groupMember ==
		 * null ) { groupMember = makeGroupMember(jid); }
		 *
		 * String status = presence.getStatus(); Presence p = new
		 * Presence(parsePresence(presence), status, null, null,
		 * Presence.CLIENT_TYPE_DEFAULT); groupMember.setPresence(p);
		 * chatGroup.addMemberAsync(groupMember); }
		 */
    }

    @SuppressWarnings("unused")
    private Contact makeGroupMember(String jid) {
        String userName = null;
        try {
            userName = jid.split("@")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        XmppAddress xAddr = new XmppAddress(jid);
        RosterEntry rEntry = null;
        if (mRoster != null)
            rEntry = mRoster.getEntry(jid);
        String name = null;
        if (rEntry != null)
            name = rEntry.getName();
        if (name == null || name.trim().equals("") || name.startsWith(userName)) {
            try {
                final String[] tempNickName = {null};

                PicaApiUtility.getProfileInSyncMode(mContext, StringUtils.parseName(jid), new MyXMLResponseHandler() {
                    @Override
                    public void onMySuccess(JSONObject response) {
                        try {
                            tempNickName[0] = response.getString(Imps.Contacts.NICKNAME);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onMyFailure(int errcode) {

                    }
                });

                if (tempNickName[0] != null)
                    name = tempNickName[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (name == null || name.length() == 0)
            name = "Unknown";

        Contact groupMember = new Contact(xAddr, name);
        try {
            if (!qAvatar.contains(jid)) {
                qAvatar.put(jid);
                if (avatarHashMap.containsKey(jid))
                    avatarHashMap.remove(jid);
            }
        } catch (InterruptedException ie) {
        }

        return groupMember;
    }

    private Contact handleSubscribePresence(org.jivesoftware.smack.packet.Presence presence, Presence p) {
        Contact contact = null;

        XmppAddress xAddr = new XmppAddress(presence.getFrom());

        RosterEntry rEntry = null;

        if (mRoster != null) {
            rEntry = mRoster.getEntry(xAddr.getBareAddress());
        }

        String name = null;
        String address = xAddr.getAddress();

        if (rEntry != null)
            name = rEntry.getName();

        if (name == null || name.length() == 0 || name.equals(StringUtils.parseName(address))) {
            name = "Unknown";
        }

        contact = new Contact(xAddr, name);
        contact.setPresence(p);

        try {
            ContactList defaultList = null;
            while (defaultList == null) {
                try {
                    defaultList = getContactListManager().getDefaultContactList();
                } catch (ImException e) {
                }
            }

            if (defaultList != null) {
                //자료기지에 친구의 subscription상태를 반영(요청받음)
                try {
                    final Contact finalContact = contact;
                    final ContactList finalDefaultList = defaultList;
                    PicaApiUtility.getProfile(mContext, StringUtils.parseName(contact.getAddress().getAddress()), new MyXMLResponseHandler(true) {
                        @Override
                        public void onMySuccess(JSONObject response) {
                            String nickName = null;
                            try {
                                nickName = response.getString(Imps.Contacts.NICKNAME);
                                if (nickName != null) {
                                    finalContact.setName(nickName);
                                }

                                String gender = response.getString(Imps.Contacts.GENDER);
                                String region = response.getString(Imps.Contacts.REGION);
                                String status = response.getString(Imps.Contacts.STATUS);
                                String userid = response.getString(Imps.Contacts.USERID);
                                String hash = response.getString(Imps.Contacts.HASH);
                                String phone = response.getString(Imps.Contacts.PHONE_NUMBER);

                                finalContact.setProfile(status, gender, region);
                                finalContact.setHash(hash);
                                finalContact.setUserid(userid);
                                finalContact.setPhoneNum(phone);

                                if (!finalDefaultList.containsContact(finalContact.getAddress())) {
                                    finalContact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_FROM;
                                    try {
                                        finalDefaultList.addExistingContact(finalContact);
                                    } catch (ImException e) {
                                        e.printStackTrace();
                                    }
                                }

                                int res = -1;
                                res = DatabaseUtils.insertOrUpdateSubscription(mContentResolver, finalDefaultList, mProviderId, mAccountId,
                                        finalContact.getAddress().getAddress(), finalContact.getName(), Imps.Contacts.SUBSCRIPTION_TYPE_FROM,
                                        Imps.Contacts.SUBSCRIPTION_STATUS_SUBSCRIBE_FROM_PENDING, null, finalContact.userid, finalContact.region, finalContact.phoneNum);
                                try {
                                    if (res == 1) { // add new subscription
                                        getContactListManager().getSubscriptionRequestListener().onSubScriptionRequest(finalContact,
                                                mProviderId, mAccountId);
                                        Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
                                        mContext.sendBroadcast(intent);
                                    }
                                } catch (Exception e) {
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onMyFailure(int errcode) {
                            GlobalFunc.showErrorMessageToast(mContext, errcode, false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            DebugConfig.debug(TAG, "unable to add new contact to default list: " + e.getLocalizedMessage());
        }

        return contact;
    }

    private Contact handleSubscribePresence2(Contact contact, Presence p) {
        contact.setPresence(p);
        ContactList defaultList = null;

        try {
            defaultList = getContactListManager().getDefaultContactList();
        } catch (ImException e) {
        }

        if (defaultList != null) {
            DatabaseUtils.insertOrUpdateSubscription(mContentResolver, defaultList, mProviderId, mAccountId,
                    contact.getAddress().getAddress(), contact.getName(), Imps.Contacts.SUBSCRIPTION_TYPE_BOTH,
                    Imps.Contacts.SUBSCRIPTION_STATUS_NONE, null, contact.userid, contact.region, contact.phoneNum);

            contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_BOTH;

            Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
            mContext.sendBroadcast(intent);

            try {
                mContactListManager.getSubscriptionRequestListener().onSubscriptionApproved(contact, mProviderId,
                        mAccountId);
            } catch (Exception e) {
            }

            ((MessengerService) aContext).getStatusBarNotifier().notifySubscriptionApproved(mProviderId, mAccountId, Math.abs(contact.hashCode()),
                    contact.getAddress().getAddress(), contact.getName(), false);

            intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
            mContext.sendBroadcast(intent);

        }

        return contact;
    }

    private void handleUnsubscribePresence(org.jivesoftware.smack.packet.Presence presence, Contact contact) {
        try {
            ContactList defaultList = null;
            while (defaultList == null) {
                defaultList = getContactListManager().getDefaultContactList();
            }

			/*
			 * RosterEntry entry = null; if ( mRoster != null) entry =
			 * mRoster.getEntry(address);
			 *
			 * if (entry == null) { } else { RosterPacket packet = new
			 * RosterPacket(); packet.setType(IQ.Type.SET); RosterPacket.Item
			 * item = new RosterPacket.Item(entry.getUser(), entry.getName());
			 * item.setItemType(RosterPacket.ItemType.cii_remove);
			 * packet.addRosterItem(item); sendPacket(packet); }
			 */

            defaultList.removeContact(contact);
            // mContactListManager.doRemoveContactFromListAsync(contact,
            // defaultList);
            DatabaseUtils.deleteSubscription(mContentResolver, contact.getAddress().getAddress());
            Imps.Tags.deleteTagMember(mContext.getContentResolver(), contact.getAddress().getAddress());       //added by JHK

            String property = (String) presence.getProperty("request");
            if (property == null || property.equals("")) {
                ((MessengerService) aContext).getStatusBarNotifier().notifyUnSubscriptionRequest(mProviderId, mAccountId,
                        Math.abs(contact.getAddress().getAddress().hashCode()), contact.getAddress().getAddress(), contact.getName(), false);
            } else if (property.equals("canceled")) {
                ((MessengerService) aContext).getStatusBarNotifier().notifySubscriptionCanceled(mProviderId, mAccountId,
                        0, contact.getAddress().getAddress(), contact.getName(), false);

                mContactListManager.getSubscriptionRequestListener().onUnSubScriptionRequest(contact, mProviderId,
                        mAccountId);
            } else if (property.equals("declined")) {
                ((MessengerService) aContext).getStatusBarNotifier().notifySubscriptionDeclined(mProviderId, mAccountId,
                        0, contact.getAddress().getAddress(), contact.getName(), false);
            }
            Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
            String deletedAddress = contact.getAddress().getAddress();
            intent.putExtra(GlobalConstrants.DELETED_CONTACT_ADDRESS, deletedAddress);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
        }
    }

    private void handleUnsubscribedPresence(Contact contact) {
        try {
            mContactListManager.doRemoveContactFromListAsync(contact, mContactListManager.getDefaultContactList());
            DatabaseUtils.deleteSubscription(mContentResolver, contact.getAddress().getAddress());
            mContactListManager.getSubscriptionRequestListener().onUnSubScriptionRequest(contact, mProviderId,
                    mAccountId);

            Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
            mContext.sendBroadcast(intent);

        } catch (Exception e) {
        }
    }

    @Override
    public void sendLocation(final double latitude, final double longitude) {

    }

    @Override
    public boolean isPicaConnection() {
        if (mConnection == null)
            return false;

        String serviceName = mConnection.getServiceName();
        return serviceName != null && !serviceName.contains("facebook");
    }

    protected boolean sendData(String param[]) {
        boolean res = true;
        Map<String, String> attributes = new HashMap<String, String>();
        Registration reg = new Registration();
        reg.setType(IQ.Type.SET);
        reg.setTo(mConnection.getServiceName());
        if (param[0] != null)
            attributes.put("token", StringUtils.escapeForXML(param[0].trim()));
        else
            attributes.put("token", "");
        if (param[1] != null)
            attributes.put("email", StringUtils.escapeForXML(param[1].trim()));
        else
            attributes.put("email", "");
        if (param[2] != null)
            attributes.put("password", StringUtils.escapeForXML(param[2].trim()));
        else
            attributes.put("password", "");
        reg.setAttributes(attributes);
        sendPacket(reg);
        return res;
    }

    @Override
    public void loadVcard(String address, String hash, boolean needName) {
        try {
            if (!qAvatar.contains(address)) {
                qAvatar.put(address);
                if (avatarHashMap.containsKey(address))
                    avatarHashMap.remove(address);
                if (hash != null)
                    avatarHashMap.put(address, hash);
                loadVCardsAsync(needName);
            }
        } catch (InterruptedException ie) {
        }
    }

    class LoadRunnable implements Runnable {

        String hashValue;
        boolean needName;

        public void setArg(boolean _needName) {
            needName = _needName;
        }

        @Override
        public void run() {
            mLoadingAvatars = true;
            String jid = null;
            try {
                while ((jid = qAvatar.poll(1, TimeUnit.SECONDS)) != null) {
                    loadVCard(mContentResolver, jid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mLoadingAvatars = false;
            }

        }

    }

    private void loadVCardsAsync(final boolean needName) {
        if (mLoadingAvatars)
            return;
        LoadRunnable loadingAvatar = new LoadRunnable();
        loadingAvatar.setArg(needName);
        execute(loadingAvatar);
    }

    private boolean loadVCard(final ContentResolver resolver, final String address) {
        if (mConnection == null || !mConnection.isConnected())
            return false;

        /**
         * todo process photo
         */
        //this code is caused to update group chat room nick name to my nick name, so please check this code later and for now, let's do nothing here
        DebugConfig.error("*****", "loadVCard started.Address="+address);

        PicaApiUtility.getProfileInSyncMode(mContext, StringUtils.parseName(address), new MyXMLResponseHandler(true) {
            @Override
            public void onMySuccess(JSONObject response) {
//				DatabaseUtils.insertContactIfNotExist(resolver, address);
                int res = 0;
                String nickName = null;
                try {
                    nickName = response.getString(Imps.Contacts.NICKNAME);
                    Contact contact = getContactListManager().getContact(address);
                    if (nickName != null) {
                        DatabaseUtils.updateFullName(resolver, address, nickName);

                        String contactName = contact != null ? contact.getName() : null;
                        if (contactName == null || address.startsWith(contactName) || contactName.equals("Unknown")) {
                            try {
                                mContactListManager.setContactName(address, nickName);
                            } catch (ImException e) {
                            }
                            res = res + DatabaseUtils.updateNickName(resolver, address, nickName);
                        }
                    }

                    try {
                        String gender = response.getString(Imps.Contacts.GENDER);
                        if (gender != null) {
                            res = res + DatabaseUtils.updateGender(resolver, address, gender);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        String hash = response.getString(Imps.Contacts.HASH);
                        String preHash = DatabaseUtils.getHash(resolver, address);
                        res = res + DatabaseUtils.updateHash(resolver, address, hash);
                        if (preHash == null || hash == null || !preHash.equals(hash)) {
                            GlobalFunc.removeFriendAvatarFile(address, true);
                            if (contact != null)
                                getContactListManager().notifyContactAvatarUpdated(contact);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*try {
                        String region = response.getString(Imps.Contacts.REGION);
                        if (region == null)
                            region = GlobalConstrants.DEFAULT_REGION;

                        res = res + DatabaseUtils.updateRegion(resolver, address, region);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/

                    try {
                        String status = response.getString(Imps.Contacts.STATUS);
                        if (status != null) {
                            res = res + DatabaseUtils.updateStatus(resolver, address, status);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        String userid = response.getString(Imps.Contacts.USERID);
                        res = res + DatabaseUtils.updateUserId(resolver, address, userid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(int errcode) {

            }
        });

        DebugConfig.error("*****", "loadVCard stopped.Address="+address);
        return false;
    }
}
