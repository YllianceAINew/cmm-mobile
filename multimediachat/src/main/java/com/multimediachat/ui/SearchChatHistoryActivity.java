package com.multimediachat.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.LogCleaner;
import com.multimediachat.util.PrefUtil.mPref;

public class SearchChatHistoryActivity extends BaseActivity implements View.OnClickListener{

    private static SearchChatHistoryActivity instance = null;
    private static LoaderManager mLoadManager = null;
    ImApp mApp;
    private EditText mSearchInput = null;
    private ListView mSearchList = null;
    private TextView emptyView = null;

    private int mLoaderId = 1000;
    private Cursor mSearchCursor = null;
    private SearchLoadCallback mLoadCallback = null;
    private SearchListAdapter mSearchAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        mLoadManager = getLoaderManager();
        mApp = (ImApp) getApplication();

        initUI();
        initData();

    }

    private void initUI() {
        setContentView(R.layout.search_chat_history_activity);
        setActionBarTitle(getString(R.string.title_search_chat_history));

        mSearchInput = findViewById(R.id.txtSearch);
        mSearchList = findViewById(R.id.search_list);
        emptyView = findViewById(R.id.searchEmpty);

        findViewById(R.id.btnClose).setOnClickListener(this);

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = mSearchInput.getText().toString();
                if (!str.equals("")) {
                    emptyView.setText(R.string.no_search_result);
                } else {
                    emptyView.setText("");
                }
                mLoadManager.restartLoader(mLoaderId, null, mLoadCallback);
            }
        });
    }

    private void initData() {
        mSearchAdapter = new SearchListAdapter(instance, R.layout.search_list_item);
        mSearchList.setAdapter(mSearchAdapter);
        mSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cr = (Cursor) mSearchList.getItemAtPosition(position);
                int threadID = cr.getInt(cr.getColumnIndex(Imps.Messages.THREAD_ID));
                int msgID = cr.getInt(cr.getColumnIndex(Imps.Messages._ID));
                Uri conUri = Uri.withAppendedPath(Imps.Contacts.CONTENT_URI, String.valueOf(threadID));
                Cursor contactCur = getContentResolver().query(conUri, null, null, null, Imps.Contacts.DEFAULT_SORT_ORDER);
                if (contactCur != null && contactCur.moveToFirst()) {
                    Uri uri = Imps.Messages.getContentUriByThreadId(threadID);
                    Cursor roomCur = getContentResolver().query(uri, null, null, null, Imps.Messages.DATE);
                    int scrollPos = -1;
                    if (roomCur != null && roomCur.moveToFirst()) {
                        do {
                            if (roomCur.getInt(cr.getColumnIndex(Imps.Messages._ID)) == msgID)
                                scrollPos = roomCur.getPosition();
                        }while (roomCur.moveToNext());
                    }
                    startChat(contactCur, scrollPos);
                }
            }
        });
        mSearchList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager)instance.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });
        emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager)instance.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        mLoadCallback = new SearchLoadCallback();
        mLoadManager.initLoader(mLoaderId, null, mLoadCallback);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                mSearchInput.setText("");
                break;
            case R.id.btn_back:
                finish();
                break;
            default:
        }
    }

    public class SearchLoadCallback implements LoaderManager.LoaderCallbacks<Cursor>{

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String strFilter = mSearchInput.getText().toString();
            String whereClause;
            if (strFilter.isEmpty())
                whereClause = "0 == 1";
            else {
                whereClause = Imps.Messages.BODY + " LIKE '%" + strFilter + "%' AND ";
                whereClause += Imps.Messages.MIME_TYPE + " ISNULL AND ";
                whereClause += Imps.Messages.PACKET_ID + " NOT LIKE 'audiolog%' AND ";
                whereClause += Imps.Messages.PACKET_ID + " NOT LIKE 'videolog%'";
            }
            CursorLoader loader = new CursorLoader(instance, Imps.Messages.CONTENT_URI, null, whereClause, null, null);
            loader.setUpdateThrottle(500L);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mSearchCursor = data;
            if (data == null || data.getCount() == 0) {
                mSearchList.setVisibility(View.GONE);
            } else {
                mSearchList.setVisibility(View.VISIBLE);
                mSearchAdapter.swapCursor(data);    ///     Refresh search list with data cursor
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mSearchAdapter.swapCursor(null);
        }
    }

    public class SearchListAdapter extends ResourceCursorAdapter {

        public SearchListAdapter(Context context, int resId) {
            super(context, resId, null, 0);
        }

        @Override
        public int getCount() {
            if (mSearchCursor == null)
                return 0;
            return mSearchCursor.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            mSearchAdapter.swapCursor(mSearchCursor);
            return super.getView(position, convertView, parent);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String strFilter = mSearchInput.getText().toString();
            strFilter = strFilter.toLowerCase();
            TextView textContent = view.findViewById(R.id.search_item_content);
            String strContent, strLowerContent;
            strContent = cursor.getString(cursor.getColumnIndex(Imps.Messages.BODY));
            strLowerContent = strContent.toLowerCase();
            SpannableString spannableContent = new SpannableString(strContent);
            if(strLowerContent.contains(strFilter)) {
                spannableContent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.item_store_tab_indicator_color)), strLowerContent.indexOf(strFilter), strLowerContent.indexOf(strFilter) + strFilter.length(), 0);
                spannableContent.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dlg_trans_bg)), strLowerContent.indexOf(strFilter), strLowerContent.indexOf(strFilter) + strFilter.length(), 0);
            }
            textContent.setText(spannableContent);

            int threadID = cursor.getInt(cursor.getColumnIndex(Imps.Messages.THREAD_ID));
            Uri conUri = Uri.withAppendedPath(Imps.Contacts.CONTENT_URI, String.valueOf(threadID));
            Cursor contactCur = getContentResolver().query(conUri, null, null, null, Imps.Contacts.DEFAULT_SORT_ORDER);
            if (contactCur != null && contactCur.moveToFirst()) {
                String strNickName = contactCur.getString(contactCur.getColumnIndex(Imps.Contacts.NICKNAME));
                String strUsername = contactCur.getString(contactCur.getColumnIndex(Imps.Contacts.USERNAME));
                long type = contactCur.getLong(contactCur.getColumnIndex(Imps.Contacts.TYPE));
                TextView textName = view.findViewById(R.id.search_item_name);
                textName.setText(strNickName);

                CircularImageView avatar = view.findViewById(R.id.imgPropile);
                if (type == Imps.Contacts.TYPE_GROUP)
                    avatar.setImageResource(R.drawable.groupicon_small);
                else
                    GlobalFunc.setProfileImage(avatar, instance, strUsername);
            }
        }
    }

    public void startChat(Cursor contactCur, int scrollPos) {
        if (contactCur != null && (!  contactCur.isAfterLast())) {
            final long chatContactId = contactCur.getLong(contactCur.getColumnIndexOrThrow(Imps.Contacts._ID));
            final String username = contactCur.getString(contactCur.getColumnIndexOrThrow(Imps.Contacts.USERNAME));
            final String nickname = contactCur.getString(contactCur.getColumnIndexOrThrow(Imps.Contacts.NICKNAME));
            final long providerId = contactCur.getLong(contactCur.getColumnIndexOrThrow(Imps.Contacts.PROVIDER));
            int isGroup = contactCur.getInt(contactCur.getColumnIndex(Imps.Chats.GROUP_CHAT));

            IImConnection conn = mApp.getConnection(providerId);

            if (conn != null)
            {
                try {
                    IChatSessionManager manager = conn.getChatSessionManager();

                    IChatSession session = null;

                    if ( manager == null )
                        return;

                    session = manager.getChatSession(username);

                    if (session == null) {
                        if ( isGroup == Imps.Chats.SINGLE_CHAT ){
                            try{
                                session = manager.createChatSession(username);
                            }catch(Exception e){}
                        }
                        else if (isGroup == Imps.Chats.GROUP_LIVE){
                            try{
                                session = manager.joinMultiUserChatSession(nickname, username);

                                if ( session == null )
                                    session = manager.createMultiUserChatSession(nickname, username);
                            }catch(Exception e){}
                        }
                    }

                    /*if ( session == null && isGroup == Imps.Contacts.TYPE_GROUP ) {
                        //showConfirmDialog(CONFIRM_GROUP_CHAT_DELETE, username, nickname, chatContactId);
                    } else {*/
                        showChat( chatContactId, username, providerId , nickname, scrollPos, isGroup);
                    //}
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

    public boolean showChat (long requestedChatId, String requestedUsername, long requestedProviderId, String nickName, int scrollPos, int isGroup)
    {
        Intent i = new Intent(this, ChatRoomActivity.class);
        i.putExtra("chatContactId", requestedChatId);
        i.putExtra("contactName", requestedUsername);
        if ( requestedUsername != null && requestedUsername.equals(mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain)) )
            i.putExtra("nickname", getString(R.string.chat_list_picatalk_team_name));
        else
            i.putExtra("nickname", nickName);
        i.putExtra("providerId", requestedProviderId);
        i.putExtra("scrollPos", scrollPos);
        i.putExtra("isGroupChat", isGroup);
        startActivity(i);
        return false;
    }
}