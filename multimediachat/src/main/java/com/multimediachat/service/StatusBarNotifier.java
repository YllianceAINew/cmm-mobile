/*
 * Source Project
 * Copyright (C) 2007-2008 Esmertec AG. Copyright (C) 2007-2008 The Android Open
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.Invitation;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.ui.MainTabNavigationActivity;


import java.util.HashMap;

import static com.multimediachat.global.GlobalConstrants.FILE_TYPE_AUDIO;
import static com.multimediachat.global.GlobalConstrants.FILE_TYPE_IMAGE;
import static com.multimediachat.global.GlobalConstrants.FILE_TYPE_OTHER;
import static com.multimediachat.global.GlobalConstrants.FILE_TYPE_VIDEO;

public class StatusBarNotifier {
	public static int sub_notify_start_id = 10000;
	public static int chat_notify_start_id = 20000;
	public static int group_chat_notify_start_id = 30000;
	public static int update_version_notify_start_id = 40000;

    private static final boolean DBG = DebugConfig.DEBUG;

    private static final long SUPPRESS_SOUND_INTERVAL_MS = 0L;

    private Context mContext;
    private NotificationManager mNotificationManager;

    private Imps.ProviderSettings.QueryMap mGlobalSettings;
    private Handler mHandler;
    private HashMap<String, NotificationInfo> mNotificationInfos;
    private long mLastSoundPlayedMs;

    public StatusBarNotifier(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mHandler = new Handler();
        mNotificationInfos = new HashMap<String, NotificationInfo>();
    }

    public void onServiceStop() {
        if (mGlobalSettings != null)
            mGlobalSettings.close();
    }
    
    private String parseMessage(String message) {
    	if ( message.length() >= 7 )
    	{
    		if (message.startsWith("http://") || message.startsWith("file://")) {
    			if (message.contains("type(")) {
    				int pos = message.indexOf("type(");
    				String typeStr1 = message.substring(pos, pos+5);  // must be type(
    				String typeStr2 = message.substring(pos+6, pos+7);  // must be )
    				final String typeStr3 = message.substring(pos+5, pos+6);  // must be 0 or 1 or 2 or 3
    				if ( typeStr1.equals("type(") && typeStr2.equals(")")  )
    				{
    					if ( typeStr3.equals(FILE_TYPE_IMAGE) ) {
    						return mContext.getResources().getString(R.string.status_bar_sent_image);
    					}
    					else if ( typeStr3.equals(FILE_TYPE_AUDIO) )  //audio
    	    			{
    	    				return mContext.getResources().getString(R.string.status_bar_sent_audio);
    	    			}
    	    			else if ( typeStr3.equals(FILE_TYPE_VIDEO) )  //video
    	    			{
    	    				return mContext.getResources().getString(R.string.status_bar_sent_video);
    	    			}
    	    			else if ( typeStr3.equals(FILE_TYPE_OTHER) )   //file
    	    			{
    	    				return mContext.getResources().getString(R.string.status_bar_sent_file);
    	    			}
    				}
    			}
    		}
    	}
    	
    	if ( message.contains("https://") )
    		return mContext.getResources().getString(R.string.status_bar_sent_image);
    	
        return message;
    }

    public void notifyChat(long providerId, long accountId, long chatId, String username,
            String nickname, String msg, boolean lightWeightNotify) {
        if (!isNotificationEnabled()) {
            if (DBG)
                log("notification for chat " + username + " is not enabled");
            return;
        }
        
        if (!isChatRoomNotificationEnabled(chatId))
        	return;

        mNotificationManager.cancel(chat_notify_start_id + (int)chatId);

        if (username == null)
            username = "";

        if (nickname == null)
            nickname = username;

        if (msg == null)
            msg = "";

        if ( msg.length() > 100 )
            msg = msg.substring(0, 100);

        msg = Html.fromHtml(parseMessage(msg)).toString();
        String title = mContext.getString(R.string.notification_title); //nickname;
        Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(
                Imps.Chats.CONTENT_URI, chatId));
        intent.addCategory(com.multimediachat.app.ImApp.IMPS_CATEGORY);

        if ( nickname.contains("@conference." + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain))) {
            msg = mContext.getString(R.string.status_bar_str_group_chat) + ": " + msg;
        }
        else{
            msg = nickname + ": " + msg;
        }
        notify(chat_notify_start_id + (int)chatId, nickname, title, msg, msg, providerId, accountId, intent, lightWeightNotify, R.drawable.topbar_message_notification); //cys
    }

    public void notifySubscriptionRequest(long providerId, long accountId, long contactId,
            String username, String nickname, boolean lightWeightNotify) {
    	
        if (!isNotificationEnabled()) {
            if (DBG)
                log("notification for subscription request " + username + " is not enabled");
            return;
        }
        
        if ( username == null )
        	username = "";
        
        if ( nickname == null )
        	nickname = username;
        
        String title = mContext.getString(R.string.notification_title);
        String message = mContext.getString(R.string.subscription_notify_text, nickname);
        Intent intent = new Intent(ImServiceConstants.ACTION_MANAGE_SUBSCRIPTION,
                ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId));
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, providerId);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, username);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_NICKNAME, nickname);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_SUB_TYPE, Imps.Contacts.SUBSCRIPTION_TYPE_FROM);

        notify(sub_notify_start_id + (int)contactId, username, title, message, message, providerId, accountId, intent, lightWeightNotify, R.drawable.topbar_chat_notification);
    }
    
    /**
     * date:2014/04/30
     * notify unsubscription request 
     * @param providerId
     * @param accountId
     * @param contactId
     * @param username
     * @param nickname
     */
    public void notifyUnSubscriptionRequest(long providerId, long accountId, long contactId,
            String username, String nickname, boolean isLightWeightNotify) {
        if (!isNotificationEnabled()) {
            if (DBG)
                log("notification for subscription request " + username + " is not enabled");
            return;
        }
        
        if ( username == null )
        	username = "";
        if ( nickname == null )
        	nickname = username;
        
        
        String title = mContext.getString(R.string.notification_title);
        String message = mContext.getString(R.string.unsubscription_notify_text, nickname);
        Intent intent = new Intent(ImServiceConstants.ACTION_MANAGE_SUBSCRIPTION,
                ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId));
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, providerId);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, username);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_NICKNAME, nickname);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_SUB_TYPE, Imps.Contacts.SUBSCRIPTION_TYPE_NONE);

        notify(sub_notify_start_id + (int)contactId, username, title, message, message, providerId, accountId, intent, isLightWeightNotify, R.drawable.topbar_chat_notification);
    }
    
    public void notifySubscriptionDeclined(long providerId, long accountId, long contactId, 
    		String username, String nickname, boolean isLightWeightNotify) {
    	
    	if (!isNotificationEnabled()) {
            if (DBG)
                log("notification for subscription request " + username + " is not enabled");
            return;
        }
    	
    	if ( username == null )
    		username = "";
    	if ( nickname == null )
    		nickname = username;
    	
        String title = mContext.getString(R.string.notification_title);//nickname;
        String message = mContext.getString(R.string.subscription_decline_notify_text, nickname);
        Intent intent = new Intent(ImServiceConstants.ACTION_MANAGE_SUBSCRIPTION,
                ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId));
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, providerId);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, username);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_NICKNAME, nickname);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_SUB_TYPE, Imps.Contacts.SUBSCRIPTION_TYPE_REMOVE);
        
        notify(sub_notify_start_id + (int)contactId, username, title, message, message, providerId, accountId, intent, isLightWeightNotify, R.drawable.topbar_chat_notification);
    }
    
    public void notifySubscriptionCanceled(long providerId, long accountId, long contactId, 
    		String username, String nickname, boolean isLightWeightNotify) {
    	
    	if (!isNotificationEnabled()) {
            if (DBG)
                log("notification for subscription request " + username + " is not enabled");
            return;
        }
    	
    	if ( username == null)
    		username = "";
    	
    	if ( nickname == null )
    		nickname = username;
    	
        String title = mContext.getString(R.string.notification_title);//nickname;
        String message = mContext.getString(R.string.subscription_cancel_notify_text, nickname);
        Intent intent = new Intent(ImServiceConstants.ACTION_MANAGE_SUBSCRIPTION,
                ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId));
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, providerId);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, username);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_NICKNAME, nickname);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_SUB_TYPE, Imps.Contacts.SUBSCRIPTION_TYPE_REMOVE);
        
        notify(sub_notify_start_id + (int)contactId, username, title, message, message, providerId, accountId, intent, isLightWeightNotify, R.drawable.topbar_chat_notification);
    }
    
    public void notifySubscriptionApproved(long providerId, long accountId, long contactId, 
    		String username, String nickname, boolean isLightWeightNotify) {
    	
    	if (!isNotificationEnabled()) {
            if (DBG)
                log("notification for subscription request " + username + " is not enabled");
            return;
        }
    	
    	if ( username == null )
    		username = "";
    	if ( nickname == null )
    		nickname = username;
    	if( nickname.contains("@") )
    		nickname = nickname.substring(0, nickname.indexOf("@"));

        String title = mContext.getString(R.string.app_name);
        String message = mContext.getString(R.string.subscription_approved_notify_text, nickname);
        Intent intent = new Intent(ImServiceConstants.ACTION_MANAGE_SUBSCRIPTION,
                ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId));
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, providerId);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, username);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_NICKNAME, nickname);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_SUB_TYPE, Imps.Contacts.SUBSCRIPTION_TYPE_BOTH);
        
        notify(sub_notify_start_id + (int)contactId, username, title, message, message, providerId, accountId, intent, isLightWeightNotify, R.drawable.topbar_chat_notification);
    }

    public void notifyGroupInvitation(long providerId, long accountId,Invitation invitation, long id) {

        Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(
        		Imps.Invitation.CONTENT_URI, id));

        if ( invitation == null )
        	return;

        String title = mContext.getString(R.string.notification_title);//mContext.getString(R.string.notify_groupchat_label);
        String message = mContext.getString(R.string.group_chat_invite_notify_text, invitation.getSender().getUser());
        notify(group_chat_notify_start_id + (int)id, invitation.getSender().getUser(), title, message, message, providerId, accountId, intent, false, R.drawable.topbar_chat_notification);
    }

    public void dismissNotifications(long providerId) {
      
        synchronized (mNotificationInfos) {
            NotificationInfo info = mNotificationInfos.get(providerId);
            if (info != null) {
                mNotificationManager.cancel(info.computeNotificationId());
                mNotificationInfos.remove(providerId);
            }
        }
    }

    public void dismissChatNotification(long providerId, String username) {
        NotificationInfo info;
        boolean removed;
        synchronized (mNotificationInfos) {
            info = mNotificationInfos.get(username);
            if (info == null) {
                return;
            }
            removed = info.removeItem(username);
        }

        if (removed) {
            if (info.getMessage() == null) {
            	
                mNotificationManager.cancel(info.computeNotificationId());
            } else {
            	DebugConfig.warn("StatusBarNotifier", "cancelNotify: new notification" + " mTitle=" + info.getTitle()
                        + " mMessage=" + info.getMessage() + " mIntent=" + info.getIntent());
                mNotificationManager.cancel(info.computeNotificationId());
            }
        }
    }


    private Imps.ProviderSettings.QueryMap getGlobalSettings() {
        if (mGlobalSettings == null) {
            
            ContentResolver contentResolver = mContext.getContentResolver();
            
            Cursor cursor = contentResolver.query(Imps.ProviderSettings.CONTENT_URI,new String[] {Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE},Imps.ProviderSettings.PROVIDER + "=?",new String[] { Long.toString(Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS)},null);
            
            if (cursor == null)
                return null;
            
            mGlobalSettings = new Imps.ProviderSettings.QueryMap(cursor, contentResolver, Imps.ProviderSettings.PROVIDER_ID_FOR_GLOBAL_SETTINGS, true, mHandler);
        }
        
        return mGlobalSettings;
    }

    private boolean isChatRoomNotificationEnabled(long chatId)
    {
    	Uri uri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, chatId);
        Cursor cursor = mContext.getContentResolver().query(uri, 
        		new String[]{Imps.Chats.NOTIFICATION}, 
        		null, 
        		null, 
        		null);
        
        if ( cursor.moveToFirst() )
        {
        	int notification_state = cursor.getInt(cursor.getColumnIndexOrThrow(Imps.Chats.NOTIFICATION));
        	cursor.close();

            return notification_state == Imps.Chats.notification_set;
        }
        else{
        	cursor.close();
        	return false;
        }
    }

    private boolean isNotificationEnabled() {
    	
        return Imps.Profile.getProfileBoolean(mContext.getContentResolver(), Imps.Profile.NEW_MESSAGE_ALERT);
        
    }

    private void notify(int _notifyId, String sender, String title, String tickerText, String message,
            long providerId, long accountId, Intent intent, boolean lightWeightNotify, int icon) {

        NotificationInfo info;

        synchronized (mNotificationInfos) {
            info = mNotificationInfos.get(sender);
            if (info == null) {
                info = new NotificationInfo(providerId, accountId);
                mNotificationInfos.put(sender, info);
            }
            info.addItem(sender, title, message, intent);
        }
        
        int notifyId;
        if ( _notifyId == 0 ) {
        	notifyId = info.computeNotificationId();
        }
        else{
        	notifyId = _notifyId;
        }
        mNotificationManager.notify(notifyId,
                info.createNotification(tickerText, lightWeightNotify, icon));
        
    }

    private void setRinger(long providerId, NotificationCompat.Builder builder) {
        boolean isDoNotDisturb = Imps.Profile.isDoNotDisturb(mContext.getContentResolver());

        String ringtoneUri = "";

        if ( Imps.Profile.getProfileBoolean(mContext.getContentResolver(), Imps.Profile.SOUND ) )
        {
            String[] toneIDs;
            toneIDs = mContext.getResources().getStringArray(R.array.tones_id);
            int alert_sound_index = Imps.Profile.getProfileInt(mContext.getContentResolver(), Imps.Profile.ALERT_SOUND);

            if ( alert_sound_index < 0 )
                ringtoneUri = "android.resource://" + mContext.getPackageName() + "/raw/tone1";
            else
                ringtoneUri = "android.resource://" + mContext.getPackageName() + "/raw/" + toneIDs[alert_sound_index];
        }

        boolean vibrate = Imps.Profile.getProfileBoolean(mContext.getContentResolver(), Imps.Profile.VIBRATE);

        Uri sound = TextUtils.isEmpty(ringtoneUri) ? null : Uri.parse(ringtoneUri);
        if ( isDoNotDisturb )
            builder.setSound(null);
        else
            builder.setSound(sound);
        if (sound != null) {
            mLastSoundPlayedMs = SystemClock.elapsedRealtime();
        }

        if (DBG)
            log("setRinger: notification.sound = " + sound);

        if ( isDoNotDisturb ) {
            builder.setDefaults(0);
        }
        else {
            if (vibrate) {
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
                if (DBG)
                    log("setRinger: defaults |= vibrate");
            }
        }
    }

    public static void notifyMissedCall(Context context, String username) {
        String useraddr = username+"@"+ mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
        Contact contact = DatabaseUtils.getContactInfo(context.getContentResolver(), useraddr);

        String mName =  contact.getName().isEmpty()?username: contact.getName();

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.view_missedcall_content);
        String errmsg = String.format("%s : %s", context.getResources().getString(R.string.missed_call_title), mName);
        contentView.setTextViewText(R.id.missed_title, errmsg);

        int contactId = GlobalFunc.getContactId(context, useraddr);

        NotificationManager notifyMng = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMng.cancel(chat_notify_start_id + (int)contactId);

        if (contactId != -1) {
            Imps.Notifications.addNotificationCount(context.getContentResolver(), Imps.Notifications.CAT_CHATTING, Imps.Notifications.FIELD_CHAT, contactId, 1);
            context.sendBroadcast(new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET));
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(
                Imps.Chats.CONTENT_URI, contactId));
        intent.addCategory(com.multimediachat.app.ImApp.IMPS_CATEGORY);

        Notification.Builder builder = new Notification.Builder(context);
        builder
                .setSmallIcon(R.drawable.topbar_state_notification)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentText(errmsg)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setLights(0xff00ff00, 300, 1000);

        notifyMng.notify(chat_notify_start_id + contactId, builder.getNotification());
    }

    class NotificationInfo {
        class Item {
            String mTitle;
            String mMessage;
            Intent mIntent;
            
            public Item(String title, String message, Intent intent) {
                mTitle = title;
                mMessage = message;
                mIntent = intent;
            }
        }

      // private HashMap<String, Item> mItems;

        private Item lastItem;
        private long mProviderId;
        //private long mAccountId;

        public NotificationInfo(long providerId, long accountId) {
            mProviderId = providerId;
            //mAccountId = accountId;
           // mItems = new HashMap<String, Item>();
        }

        public int computeNotificationId() {
            if (lastItem == null)
                return (int)mProviderId;
            return lastItem.mTitle.hashCode();
        }

        public synchronized void addItem(String sender, String title, String message, Intent intent) {
            /*
            Item item = mItems.get(sender);
            if (item == null) {
                item = new Item(title, message, intent);
                mItems.put(sender, item);
            } else {
                item.mTitle = title;
                item.mMessage = message;
                item.mIntent = intent;
            }*/
            lastItem = new Item(title, message, intent);
        }

        public synchronized boolean removeItem(String sender) {
            /*
            Item item = mItems.cii_remove(sender);
            if (item != null) {
                return true;
            }*/
            return true;
        }

        @SuppressWarnings("deprecation")
		public Notification createNotification(String tickerText, boolean lightWeightNotify, int icon) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
            Intent intent = getIntent();
            builder
                .setSmallIcon(icon)
                .setTicker(lightWeightNotify ? null : tickerText)
                .setWhen(System.currentTimeMillis())
                .setLights(0xff00ff00, 300, 1000)
                .setContentTitle(getTitle())
                .setContentText(getMessage())
                .setContentIntent(PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true)
                ;

            if (!(lightWeightNotify || shouldSuppressSoundNotification())) {
                setRinger(mProviderId, builder);
            }
            
            return builder.getNotification();
        }


       /* private Intent getMultipleNotificationIntent() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(mContext, MainFrameTabActivity.class);
            intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, mProviderId);
            intent.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, mAccountId);
            intent.putExtra(ImServiceConstants.EXTRA_INTENT_SHOW_MULTIPLE, true);
         //   intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            return intent;
        }*/

        public String getTitle() {
            return lastItem.mTitle;
        }

        public String getMessage() {
            return lastItem.mMessage;
        }

        public Intent getIntent() {
            /*
            int count = mItems.size();
            if (count == 0) {
                return getDefaultIntent();
            } else if (count == 1) {
                Item item = mItems.values().iterator().next();
                return item.mIntent;
            } else {
                return getMultipleNotificationIntent();
            }*/

            return lastItem.mIntent;
        }
    }

    private static void log(String msg) {
        MessengerService.debug("[StatusBarNotify] " + msg);
    }

    private boolean shouldSuppressSoundNotification() {
        return (SystemClock.elapsedRealtime() - mLastSoundPlayedMs < SUPPRESS_SOUND_INTERVAL_MS);
    }

	

}
