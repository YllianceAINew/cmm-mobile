package com.multimediachat.ui;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.multimediachat.util.qrcode.client.android.CaptureActivity;
import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.service.ImServiceConstants;
import com.multimediachat.ui.adapter.carouseltab.ThemeColor;
import com.multimediachat.ui.views.MessageView;
import com.multimediachat.ui.views.PagerSlidingTabStrip;
import com.multimediachat.ui.views.PagerSlidingTabStrip.ViewTabProvider;
import com.multimediachat.ui.views.QuickActionItem;
import com.multimediachat.ui.views.QuickActionPopup;
import com.multimediachat.util.PrefUtil.mPref;

import org.linphone.LinphoneManager;
import org.linphone.LinphoneService;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.content.Intent.ACTION_MAIN;
import static com.multimediachat.ui.views.QuickActionPopup.ANIM_GROW_FROM_TOP;

public class MainTabNavigationActivity extends BaseActivity implements OnPageChangeListener,
        NotificationCenter.NotificationCenterDelegate, View.OnClickListener {

    public static final int REQUEST_SELECT_CHAT_LIST = 1000;
    public static final int REQUEST_TAG = 100;
    private static final int PAGE_MARGIN_WIDTH = 8;

    public ViewPager mViewPager;
    private PagerSlidingTabStrip tabs;
    private TabsAdapter mTabsAdapter;

    Integer intNumOfPages = 3;

    private static final int ID_GROUP_CHAT = 1;
    private static final int ID_ADD_CONTACTS = 2;
    private static final int ID_LOGOUT = 5;
    private static final int ID_CHATSEARCH = 6;
    private static final int ID_ADD_TAG = 7;
    private static final int ID_QRCODESCAN = 8;

    private QuickActionItem addContactItem = null;
    private QuickActionItem chatSearch = null;
    private QuickActionItem qrCodeScan = null;
    private QuickActionItem newChat = null;

    UpdaterBroadcastReceiver updateBroadcaseReceiver = null;
    public static final String BROADCAST_UPDATE_WIDGET = "broadcast_update_widget";
    public static final String BROADCAST_CHANGE_ACCOUNT = "CHANGE_ACCOUNT";
    public static final String BROADCAST_DELETE_ACCOUNT = "DELETE_ACCOUNT";
    public static final String BROADCAST_FRIEND_LIST_RELOAD = "com.multimediachat.FRIEND_LIST_RELOAD";
    public static final String BROADCAST_FILE_UP_DOWN_LOAD = "file_up_down_load";

    private boolean b_delaccount = false;
    private static final int CONFIRM_GROUP_CHAT_INVITE = 0;

    static final String[] CHAT_PROJECTION = {Imps.Contacts._ID, Imps.Contacts.ACCOUNT, Imps.Contacts.PROVIDER,
            Imps.Contacts.USERNAME, Imps.Contacts.NICKNAME, Imps.Contacts.TYPE, Imps.Presence.PRESENCE_STATUS,
            Imps.Chats.LAST_UNREAD_MESSAGE, Imps.Chats._ID, Imps.Chats.GROUP_CHAT};

    private ChatListFragment chatListFragment;
    private FriendListFragment friendListFragment;

    private TextView tvNotifyChatListTab;
    private TextView tvNotifyFriendListTab;

    private Menu mOptionMenu = null;
    private boolean mOptionMenuState = false;

    private boolean fromOnNewIntent = false;

    private QuickActionPopup chatOptionQuickActionPopup = null;

    private static MainTabNavigationActivity instance;

    public EditText mEditSearchBar = null;

    public static final MainTabNavigationActivity instance() {
        if (instance != null)
            return instance;
        throw new RuntimeException("MainTabNavigationActivity not instantiated yet");
    }

    public static final boolean isInstanciated() {
        return instance != null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImApp.getInstance().initApplicationUI();

        String username = mPref.getString("username", "");
        if (username.isEmpty()) {
            finish();
            startActivity(getIntent().setClass(this, MainActivity.class));
            return;
        }

        long accountid = mPref.getLong(GlobalConstrants.store_picaAccountId, -1);

        if (accountid < 1) {
            finish();
            startActivity(getIntent().setClass(this, MainActivity.class));
            return;
        }

        GlobalVariable.account_id = Long.toString(accountid);
        DatabaseUtils.mAccountID = GlobalVariable.account_id;
        Imps.mAccountID = GlobalVariable.account_id;

        initUI();

        instance = this;

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setPageMargin(PAGE_MARGIN_WIDTH);
        int color = ThemeColor.getColor(ThemeColor.PAGE_MARGIN_COLOR);
        mViewPager.setPageMarginDrawable(new ColorDrawable(color));

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        addTabs();

        tabs.setViewPager(mViewPager);
        tabs.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (i != 1)
                    hideSearchBar();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mViewPager.setCurrentItem(1);

        findViewById(R.id.btn_cancel_search).setOnClickListener(this);
        mEditSearchBar = findViewById(R.id.edit_search_bar);
        mEditSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (friendListFragment!= null && friendListFragment.mFilterView != null)
                    friendListFragment.mFilterView.showList(mEditSearchBar.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        IntentFilter filter = new IntentFilter(BROADCAST_FRIEND_LIST_RELOAD);
        filter.addAction(BROADCAST_DELETE_ACCOUNT);
        filter.addAction(BROADCAST_CHANGE_ACCOUNT);
        filter.addAction(BROADCAST_UPDATE_WIDGET);
        updateBroadcaseReceiver = new UpdaterBroadcastReceiver();
        registerReceiver(updateBroadcaseReceiver, filter);

        Intent intent = getIntent();
        if (intent != null) {
            fromOnNewIntent = false;
            resolveIntent(intent);
        }

        if (mApp.isServerLogined()) {
            onConnectionLogined();
        }

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.appNeedFinish);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.group_chat_invited);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.pica_connection_logined);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.friend_list_reload);
    }

    private void initUI() {
        setContentView(R.layout.main_tab_navigation);
        hideBackButton();
        addImageButtonWithPadding(R.drawable.btn_search, R.id.btn_contact_search, POS_RIGHT,
                AndroidUtility.dp(10), AndroidUtility.dp(0), AndroidUtility.dp(0), AndroidUtility.dp(0));

        addImageButton(R.drawable.hm_listmenu, R.id.btn_more_setting, POS_LEFT);

        updateActionBar(1);

        initChatOptionPopWindow();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPref.putInt(GlobalConstrants.LOADED_MAINTABACTIVITY, 1);
                try {
                    Imps.Notifications.removeChatNotificationIfNeed(getContentResolver());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onConnectionLogined() {
        final Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateTabWidget();
            }
        });
    }

    private void addTabs() {
        mTabsAdapter.addTab(ChatListFragment.class, null);
        mTabsAdapter.addTab(FriendListFragment.class, null);
//        mTabsAdapter.addTab(TagFragment.class, null);
        mTabsAdapter.addTab(SettingFragment.class, null);
    }

    private ChatListFragment getChatListFragment() {
        if (chatListFragment == null) {
            chatListFragment = (ChatListFragment) mTabsAdapter.getFragment(0);
        }
        return chatListFragment;
    }

    private FriendListFragment getFriendListFragment() {
        if (friendListFragment == null) {
            friendListFragment = (FriendListFragment) mTabsAdapter.getFragment(1);
        }
        return friendListFragment;
    }

    private TextView getTVNotifyChatListTab() {
        if (tvNotifyChatListTab == null) {
            if (tabs != null) {
                View chatTabView = null;
                chatTabView = tabs.getTab(0);
                if (chatTabView != null)
                    tvNotifyChatListTab = (TextView) chatTabView.findViewById(R.id.noti_img);
            }
        }
        return tvNotifyChatListTab;
    }

    private TextView getContactsTab() {
        if (tvNotifyFriendListTab == null) {
            if (tabs != null) {
                View contactTabView = null;
                contactTabView = tabs.getTab(1);
                if (contactTabView != null) {
                     tvNotifyFriendListTab = (TextView) contactTabView.findViewById(R.id.noti_img);
//                    tvNotifyFriendListTab = (TextView) contactTabView.findViewById(R.id.noti_add_contacts);
                }
            }
        }
        return tvNotifyFriendListTab;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    public void updateActionBar(int tabIndex) {
        if (tabIndex == 0) {
            setActionBarTitle(getString(R.string.title_chat_list));
            findViewById(R.id.btn_contact_search).setVisibility(View.VISIBLE);
        } else if (tabIndex == 1) {
            setActionBarTitle(getString(R.string.title_contacts));
            findViewById(R.id.btn_contact_search).setVisibility(View.VISIBLE);
        } else if (tabIndex == 2) {
            setActionBarTitle(getString(R.string.title_settings));
            findViewById(R.id.btn_contact_search).setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        for (int i = 0; i < intNumOfPages; i++) {
            View tabView = tabs.getTab(i);

            if (tabView == null)
                continue;

            if (i == arg0) {
                tabView.setSelected(true);
            } else {
                tabView.setSelected(false);
            }
        }

        updateActionBar(arg0);
        AndroidUtility.hideKeyboard(tabs);
    }

    private void initChatOptionPopWindow() {
        int textColor = getResources().getColor(R.color.white);

        chatOptionQuickActionPopup = new QuickActionPopup(this, QuickActionPopup.VERTICAL, ANIM_GROW_FROM_TOP);

        newChat = new QuickActionItem(ID_GROUP_CHAT, getString(R.string.new_chat),
                getResources().getDrawable(R.drawable.li_chat), getResources().getColor(R.color.white));
        addContactItem = new QuickActionItem(ID_ADD_CONTACTS, getString(R.string.str_add_friend),
                getResources().getDrawable(R.drawable.li_addcontact), getResources().getColor(R.color.white));
        qrCodeScan = new QuickActionItem(ID_QRCODESCAN, getString(R.string.qrcode_scan),
                getResources().getDrawable(R.drawable.li_scanqr), getResources().getColor(R.color.white));
        chatSearch = new QuickActionItem(ID_CHATSEARCH, getString(R.string.chat_search),
                getResources().getDrawable(R.drawable.li_search), getResources().getColor(R.color.white));
        
        chatOptionQuickActionPopup.addActionItem(newChat);
        chatOptionQuickActionPopup.addActionItem(addContactItem);
        chatOptionQuickActionPopup.addActionItem(qrCodeScan);
        chatOptionQuickActionPopup.addActionItem(chatSearch);

        chatOptionQuickActionPopup.setOnActionItemClickListener(new QuickActionPopup.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickActionPopup source, int pos, int actionId) {
                if (actionId == ID_GROUP_CHAT) {
                    Intent groupChatIntent = new Intent(MainTabNavigationActivity.this, ChooseFriendsActivity.class);
                    groupChatIntent.putExtra("call_from", ChooseFriendsActivity.CALL_FROM_GROUP_CHAT);
                    startActivity(groupChatIntent);
                } else if (actionId == ID_ADD_CONTACTS) {
                    Intent intent = new Intent(MainTabNavigationActivity.this, AddContactsActivity.class);
                    startActivity(intent);
                } else if (actionId == ID_ADD_TAG) {
                    Intent intent = new Intent(MainTabNavigationActivity.this, NewTagActivity.class);
                    startActivityForResult(intent, REQUEST_TAG);
                } else if (actionId == ID_LOGOUT) {
                    GlobalFunc.doLogOut(MainTabNavigationActivity.this);
                } else if (actionId == ID_CHATSEARCH) {
                    Intent intent = new Intent(MainTabNavigationActivity.this, SearchChatHistoryActivity.class);
                    startActivity(intent);
                } else if (actionId == ID_QRCODESCAN) {
                    checkCameraPermission(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent scanQrActivity = new Intent(MainTabNavigationActivity.this, CaptureActivity.class);
                            startActivity(scanQrActivity);
                        }
                    });
                }
            }
        });

        chatOptionQuickActionPopup.setOnDismissListener(new QuickActionPopup.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
    }

    private class TabsAdapter extends FragmentStatePagerAdapter implements ViewTabProvider {
        private Context mContext;
        private LayoutInflater mInflater;
        private ViewPager mViewPager;
        private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        private final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        @Override
        public View getPageView(int position) {
            View view = null;
            if (position == 0) {
                view = mInflater.inflate(R.layout.tab_chat, null);
            } else if (position == 1) {
                view = mInflater.inflate(R.layout.tab_friends, null);
            } else if (position == 2) {
                view = mInflater.inflate(R.layout.tab_setting, null);
            }
            return view;
        }

        private TabsAdapter(FragmentActivity fa, ViewPager pager) {
            super(fa.getSupportFragmentManager());
            mContext = fa;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private void addTab(Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            mTabs.add(info);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            Fragment fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            return fragment;
        }

        private Fragment getFragment(int fragmentPos) {
            try {
                Field f = FragmentStatePagerAdapter.class.getDeclaredField("mFragments");
                f.setAccessible(true);
                ArrayList<Fragment> fragments = (ArrayList<Fragment>) f.get(this);
                if (fragments.size() > fragmentPos) {
                    return fragments.get(fragmentPos);
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == REQUEST_SELECT_CHAT_LIST) {
            if (mViewPager != null)
                mViewPager.setCurrentItem(0);
        }
    }

    public void onBackPressed() {

        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateBroadcaseReceiver != null) {
            unregisterReceiver(updateBroadcaseReceiver);
        }

        try {
            if (MessageView.mSampleImageBackgroundLoader != null) {
                MessageView.mSampleImageBackgroundLoader.recycleCache();
                MessageView.mSampleImageBackgroundLoader.Reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // delete account
        if (b_delaccount) {
            resetApp();
        }

        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appNeedFinish);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.group_chat_invited);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.pica_connection_logined);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.friend_list_reload);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!LinphoneManager.isInstanciated()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (!LinphoneService.isReady()) {
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
        }

        int accountError = mPref.getInt("AccountError", 0);
        if (accountError > 0 && accountError < 3) {
            deleteAppData(accountError);
            return;
        }

        updateActionBar(mViewPager.getCurrentItem());

        // updateLoginStatus(GlobalFunc.getSipConnStatus(), GlobalFunc.getXmppConnStatus());
//        updateViewMoreMenuState();

        /*
         * added by JHK(2019.11.07)
         * send broadcast MainTabNavigationActivity to reload list due to notify avatar change state
         */
        Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
        this.sendBroadcast(intent);
    }

    private void resetApp() {
        try {
            // stop service
            mApp.stopService();
            mApp.onTerminate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mApp.clearApplicationData();
        mApp.truncateDatabase();

        GlobalFunc.updateBadgeCount(this, 0);

        mApp.restartAppSoon(this);

    }

    boolean needCallUpdateTabWdiget = false;
    boolean isUpdateTabWidgetRunning = false;

    public synchronized void updateTabWidget() {
        if (isUpdateTabWidgetRunning) {
            needCallUpdateTabWdiget = true;
            return;
        }
        isUpdateTabWidgetRunning = true;

        int chatCount = 0;
        int contactCount = 0;

        chatCount = Imps.Notifications.getNotificationCount(getContentResolver(),
                Imps.Notifications.CAT_CHATTING, Imps.Notifications.FIELD_CHAT, 0);
        contactCount = Imps.Contacts.getReceivedRequestsCount(getContentResolver());

        int badgeCount = chatCount + contactCount;
        GlobalFunc.updateBadgeCount(this, badgeCount);

        TextView tv = getTVNotifyChatListTab();
        if (tv != null) {
            if (chatCount > 0) {
                tv.setVisibility(View.VISIBLE);
                if (chatCount > 99) {
                    tv.setText("99+");
                    tv.setTextSize(10);
                } else {
                    tv.setText(String.valueOf(chatCount));
                    tv.setTextSize(12);
                }
            } else
                tv.setVisibility(View.GONE);
        }

        tv = getContactsTab();
        if (tv != null) {
            if (contactCount > 0) {
                tv.setVisibility(View.VISIBLE);
                if (contactCount > 99) {
                    tv.setText("99+");
                    tv.setTextSize(10);
                } else {
                    tv.setText(String.valueOf(contactCount));
                    tv.setTextSize(12);
                }
            } else
                tv.setVisibility(View.GONE);
        }

        ChatListFragment chatListFragment = getChatListFragment();
        if (chatListFragment != null)
            chatListFragment.showChatRoomList();

        isUpdateTabWidgetRunning = false;
        if (needCallUpdateTabWdiget) {
            needCallUpdateTabWdiget = false;
            updateTabWidget();
        }
    }

    public void showInvitationDialog(final long id) {
        if (!this.isFinishing()) {
            ImApp.getInstance().RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ContentResolver resolver = getContentResolver();

                    Uri uri = ContentUris.withAppendedId(Imps.Invitation.CONTENT_URI, id);
                    Cursor cursor = resolver.query(uri,
                            new String[]{Imps.Invitation.GROUP_NAME, Imps.Invitation.SENDER, Imps.Invitation.NOTE},
                            null, null, null);

                    if (cursor.moveToFirst()) {
                        String groupName = cursor.getString(cursor.getColumnIndex(Imps.Invitation.GROUP_NAME));
                        String senderName = cursor.getString(cursor.getColumnIndex(Imps.Invitation.SENDER));
                        cursor.close();

                        showConfirmDialog(CONFIRM_GROUP_CHAT_INVITE, id, senderName, groupName);
                    }
                }
            }, 500);
        }
    }

    /*
     * start chat
     *
     * @params contactId, address, nickName, providerId
     */
    private void startChat(long contactId, String address, String nickName, long providerId, int isGroup) {
        Intent i = new Intent(this, ChatRoomActivity.class);
        i.putExtra("chatContactId", contactId);
        i.putExtra("contactName", address);
        i.putExtra("nickname", nickName);
        i.putExtra("providerId", providerId);
        i.putExtra("isGroupChat", isGroup);
        startActivity(i);

        if (mViewPager != null) {
            mViewPager.setCurrentItem(0);
        }
    }

    private void resolveIntent(Intent intent) {
        doResolveIntent(intent);
        setIntent(null);
    }

    private void doResolveIntent(Intent intent) {
        int isLoadedMainTab = mPref.getInt(GlobalConstrants.LOADED_MAINTABACTIVITY, 0);
        if (ImServiceConstants.ACTION_MANAGE_SUBSCRIPTION.equals(intent.getAction())) {
            if (isLoadedMainTab == 0) {
                finish();
                return;
            }

            long providerId = intent.getLongExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, -1);
            String fromAddress = intent.getStringExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS);
            int sub_type = intent.getIntExtra(ImServiceConstants.EXTRA_INTENT_SUB_TYPE, -1);

            if ((providerId == -1) || (fromAddress == null)) {
                finish();
            } else {
                if (sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM) {
                    if (!fromOnNewIntent) {
                        GlobalFunc.doSipLogin();
                    }
                    Intent i = new Intent(MainTabNavigationActivity.this, SubscriptionActivity.class);
                    startActivity(i);
                } else if (sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_NONE) {
                    mViewPager.setCurrentItem(1);
                }
            }
        } else {
            Uri data = intent.getData();
            if (data != null) {
                if (isLoadedMainTab == 0) {
                    finish();
                    return;
                }

                String type = getContentResolver().getType(data);
                if (Imps.Chats.CONTENT_ITEM_TYPE.equals(type)) {
                    long requestedContactId = ContentUris.parseId(data);
                    Uri.Builder builder = Imps.Contacts.CONTENT_URI.buildUpon();
                    ContentUris.appendId(builder, requestedContactId);
                    Cursor cursor = getContentResolver().query(builder.build(), CHAT_PROJECTION, null, null, null);
                    try {
                        if (cursor != null && cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            long contactId = cursor.getLong(cursor.getColumnIndexOrThrow(Imps.Contacts._ID));
                            String address = cursor.getString(cursor.getColumnIndexOrThrow(Imps.Contacts.USERNAME));
                            String nickname = cursor
                                    .getString(cursor.getColumnIndexOrThrow(Imps.Contacts.NICKNAME));
                            long providerId = cursor.getLong(cursor.getColumnIndexOrThrow(Imps.Contacts.PROVIDER));
                            int isGroup = cursor.getInt(cursor.getColumnIndexOrThrow(Imps.Chats.GROUP_CHAT));

                            if (!fromOnNewIntent) {
                                GlobalFunc.doSipLogin();
                            }

                            startChat(contactId, address, nickname, providerId, isGroup);

                        }
                    } finally {
                        cursor.close();
                    }
                }
            } else if (intent.hasExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID)) {
                if (isLoadedMainTab == 0) {
                    finish();
                    return;
                }

                mViewPager.setCurrentItem(0);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        fromOnNewIntent = true;
        resolveIntent(intent);
    }

    public void showConfirmDialog(int res, final long id, String senderName, String groupName) {

        if (this == null || this.isFinishing())
            return;

        // custom dialog
        try {
            final Dialog dlg = GlobalFunc.createDialog(this, R.layout.confirm_dialog, true);

            TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
            title.setText(R.string.information);

            TextView content = (TextView) dlg.findViewById(R.id.msgcontent);
            Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
            Button dlg_btn_cancel = (Button) dlg.findViewById(R.id.btn_cancel);
            dlg_btn_cancel.setVisibility(View.VISIBLE);

            String contentStr = "";
            if (res == CONFIRM_GROUP_CHAT_INVITE) {
                dlg_btn_ok.setText(R.string.accept_invitation);
                dlg_btn_cancel.setText(R.string.decline_invitation);

                title.setText(R.string.notify_groupchat_label);
                contentStr = getResources().getString(R.string.group_chat_invite_notify_text, senderName);
                dlg_btn_ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
                        IImConnection conn = mApp.getConnection(mProviderId);
                        try {
                            conn.acceptInvitation(id);
                        } catch (Exception e) {
                        }

                        try {
                            dlg.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                dlg_btn_cancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
                        IImConnection conn = mApp.getConnection(mProviderId);
                        try {
                            conn.rejectInvitation(id);
                        } catch (Exception e) {
                        }
                        try {
                            dlg.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            content.setText(contentStr);
            dlg.setCanceledOnTouchOutside(false);
            dlg.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAppData(int errorType) {
        if (errorType < 1 || errorType > 2)
            return;
        b_delaccount = true;
        mPref.putInt("AccountError", 0);
        try {
            // stop service
            mApp.stopService();
        } catch (Exception e) {
            DebugConfig.debug(this.getClass().getName(),
                    "DeleteActivity.java / resetApp() / Exception : " + e.toString());
        }

        try {
            showErrorMessage(errorType - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorMessage(int res) throws Exception {
        final Dialog dlg = GlobalFunc.createDialog(this, R.layout.msgdialog, true);

        TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
        title.setText(R.string.title_server_error);

        TextView content = (TextView) dlg.findViewById(R.id.msgcontent);
        content.setText(R.string.content_server_error);
        if (res == 1) {
            content.setText(R.string.change_account);
            title.setText(R.string.information);
        } else if (res == 2){
            content.setText(R.string.error_8);
            title.setText(R.string.error);
        }

        Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
        dlg_btn_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

        if (res == 2) {
            Button dlg_btn_cancel = (Button) dlg.findViewById(R.id.btn_cancel);
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
        }
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);

        if (!isFinishing())
            dlg.show();
    }

    public class UpdaterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(BROADCAST_FRIEND_LIST_RELOAD)) {
                FriendListFragment fragment = getFriendListFragment();
                if (fragment != null) {
                    fragment.showFriendList();
                }
            } else if (action.equals(BROADCAST_DELETE_ACCOUNT)) {
                deleteAppData(1);
            } else if (action.equals(BROADCAST_CHANGE_ACCOUNT)) {
                deleteAppData(2);
            } else if (action.equals(BROADCAST_UPDATE_WIDGET)) {
                updateTabWidget();
            }
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.appNeedFinish) {
            finish();
        } else if (id == NotificationCenter.group_chat_invited) {
            try {
                if (args != null && args.length > 0 && args[0] != null) {
                    showInvitationDialog((Long) args[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == NotificationCenter.pica_connection_logined) {
            onConnectionLogined();
        } else if (id == NotificationCenter.friend_list_reload) {
            AndroidUtility.RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    FriendListFragment fragment = getFriendListFragment();
                    if (fragment != null) {
                        fragment.showFriendList();
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_more_setting:
                if (chatOptionQuickActionPopup != null) {
                    View chatOptionMenuView = findViewById(R.id.actionbar);
                    if (chatOptionMenuView != null)
                        chatOptionQuickActionPopup.show(chatOptionMenuView);
                }
                break;

            case R.id.btn_contact_search:
                if (mViewPager.getCurrentItem() == 0) {
                    Intent intent = new Intent(MainTabNavigationActivity.this, SearchChatHistoryActivity.class);
                    startActivity(intent);
                } else if (mViewPager.getCurrentItem() == 1) {
                    showSearchBar();
                }
                break;

            case R.id.btn_cancel_search:
                mEditSearchBar.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditSearchBar.getWindowToken(), 0);
                hideSearchBar();
                break;

            default:
                super.onClick(v);
                break;
        }
    }
}
