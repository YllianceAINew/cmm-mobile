package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.dialog.CustomDialog;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.ui.views.HorizontalListView;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.datamodel.FriendItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("DefaultLocale")
public class ChoosePwdFriendActivity extends BaseActivity
        implements OnClickListener {

    private Context mContext = null;

    final String[] CONTACT_PROJECTION = { Imps.Contacts._ID, Imps.Contacts.PROVIDER, Imps.Contacts.USERNAME,
            Imps.Contacts.NICKNAME, Imps.Presence.PRESENCE_STATUS, Imps.Contacts.STATUSMESSAGE
    };


    // UI variable
    long mProviderId = -1;
    long mAccountId = -1;
    MainProgress mProgressDlg;

    TextView mEmptyView;
    ListView mGridView;
    GridAdapter mGridAdapter;

    // Data variable
    ShowFriendListAsyncTask mAsyncTask;
    String lastFilterString;
    String mStrPwdFriend = "";

    private MyLoaderCallbacks mLoaderCallbacks;
    private LoaderManager mLoaderManager;
    private int mLoaderId;

    EditText mTxtSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pwd_friend);
        setActionBarTitle(getString(R.string.select_pwd_friend_title));

        mContext = this;
        mGridView = (ListView) findViewById(R.id.gridView);
        mEmptyView = (TextView) findViewById(R.id.emptyView);
        mGridView.setEmptyView(mEmptyView);
        mEmptyView.setText(r.getString(R.string.no_search_result));

        mTxtSearch = (EditText)findViewById(R.id.txtSearch);

        mTxtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                mGridAdapter.getFilter().filter(mTxtSearch.getText().toString());
            }
        });

        findViewById(R.id.btnClose).setOnClickListener(this);

        addTextButton(getString(R.string.confirm), R.id.btnOK, POS_RIGHT);

        mGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
        mAccountId = mPref.getLong(GlobalConstrants.store_picaAccountId, -1);

        mLoaderId = 4567;
        mLoaderManager = getSupportLoaderManager();

        cr = getContentResolver();
        mStrPwdFriend = mPref.getString("password_friend_username", "");

        showFriendList("");

    }

    @Override
    public void onBackPressed() {
        InputMethodManager ime = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (ime.hideSoftInputFromWindow(mTxtSearch.getWindowToken(), 0))
            return;
        super.onBackPressed();
    }

    public void showFriendList(String filterString) {

        lastFilterString = filterString;

        if (mGridAdapter == null) {
            mGridAdapter = new GridAdapter(this);
            mGridView.setAdapter(mGridAdapter);
            mLoaderCallbacks = new MyLoaderCallbacks();
            mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);
        } else {
            mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
        }
    }

    private class ShowFriendListAsyncTask extends AsyncTask<Cursor, FriendItem, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Cursor... params) {
            IImConnection conn = null;
            conn = mApp.getConnection(mProviderId);

            if (conn == null) {
                mProgressDlg.dismiss();
                return null;
            }

            Cursor cursor = params[0];

            while (cursor.moveToNext()) {
                FriendItem item = new FriendItem();
                item.userName = cursor.getString(cursor.getColumnIndex(Imps.Contacts.USERNAME));
                item.nickName = cursor.getString(cursor.getColumnIndex(Imps.Contacts.NICKNAME));
                item.isOnline = (cursor.getInt(cursor.getColumnIndex(Imps.Contacts.PRESENCE_STATUS))!=Imps.Presence.OFFLINE);
                publishProgress(item);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(FriendItem... item) {
            mGridAdapter.addItem(item[0]);
            super.onProgressUpdate(item);
        }
    }

    public class GridAdapter extends ArrayAdapter<FriendItem> {

        private List<FriendItem> mData = new ArrayList<FriendItem>();
        private List<FriendItem> m_items = new ArrayList<FriendItem>();

        public GridAdapter(Activity context) {
            super(context, 0);
        }

        public void addItem(FriendItem item) {
            m_items.add(item);
            notifyDataSetChanged();
        }

        public void removeAll() {
            m_items.clear();
        }

        @Override
        public int getCount() {
            if (m_items != null)
                return m_items.size();
            return 0;
        }

        @Override
        public FriendItem getItem(int position) {
            if (m_items != null)
                return m_items.get(position);
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (m_items != null)
                return m_items.get(position).hashCode();
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            if (convertView == null) {

                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.friend_list_item, null, true);

                viewHolder = new ViewHolder();

                viewHolder.mImgProfile = (CircularImageView) convertView.findViewById(R.id.imgProfile);
                viewHolder.mTxtNickName = (TextView) convertView.findViewById(R.id.txtNickName);
                viewHolder.mImgCheck = (ImageView) convertView.findViewById(R.id.imgCheck);
                viewHolder.mItemLayout = convertView.findViewById(R.id.itemLayout);
                viewHolder.mOnlineStatus = (ImageView) convertView.findViewById(R.id.onlineStatus);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final FriendItem item = m_items.get(position);

            String name = item.nickName;

            if (name != null) {
                String strFilter = mTxtSearch.getText().toString();
                strFilter = strFilter.toLowerCase();
                String strLower = name.toLowerCase();
                SpannableString spannableContent = new SpannableString(name);
                if(strLower.contains(strFilter)) {
                    spannableContent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.item_store_tab_indicator_color)), strLower.indexOf(strFilter), strLower.indexOf(strFilter) + strFilter.length(), 0);
                    spannableContent.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dlg_trans_bg)), strLower.indexOf(strFilter), strLower.indexOf(strFilter) + strFilter.length(), 0);
                }
                viewHolder.mTxtNickName.setText(spannableContent);
            } else
                viewHolder.mTxtNickName.setText("");

            viewHolder.mTxtNickName.setSelected(true);


            viewHolder.mItemLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( mStrPwdFriend.equals(item.userName)) {
                        mStrPwdFriend = "";
                    } else {
                        mStrPwdFriend = item.userName;
                    }
                    notifyDataSetChanged();
                }
            });

            viewHolder.mImgProfile.setBorderWidth(1);
            viewHolder.mImgProfile.setBorderColor(r.getColor(R.color.profile_border_color));
            GlobalFunc.showAvatar(mContext, item.userName, viewHolder.mImgProfile);

            if ( mStrPwdFriend.equals(item.userName)) {
                viewHolder.mImgCheck.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mImgCheck.setVisibility(View.INVISIBLE);
            }

            if (item.isOnline) {
                viewHolder.mOnlineStatus.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mOnlineStatus.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    final FilterResults oReturn = new FilterResults();

                    if (mData.size() == 0) {
                        mData = m_items;
                    }

                    if (constraint == null || constraint.length() == 0) {
                        oReturn.count = mData.size();
                        oReturn.values = mData;
                    } else {
                        final ArrayList<FriendItem> results = new ArrayList<FriendItem>();

                        if (mData != null && mData.size() > 0) {
                            for (final FriendItem item : mData) {
                                if (item.nickName.toLowerCase().contains(constraint.toString()))
                                    results.add(item);
                            }
                        }
                        oReturn.values = results;
                    }
                    return oReturn;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    @SuppressWarnings("unchecked")
                    ArrayList<FriendItem> values = (ArrayList<FriendItem>) results.values;
                    m_items = values;
                    notifyDataSetChanged();
                }
            };
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        class ViewHolder {
            ImageView mImgCheck;
            CircularImageView mImgProfile;
            TextView mTxtNickName;
            View mItemLayout;
            ImageView mOnlineStatus;
        }
    }

    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        CursorLoader loader = null;

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();
            if (lastFilterString != null) {
                buf.append(Imps.Contacts.NICKNAME);
                buf.append(" LIKE ");
                android.database.DatabaseUtils.appendValueToSql(buf, "%" + lastFilterString + "%");
            } else
                buf.append(" trim(" + Imps.Contacts.NICKNAME + ") != '' ");

            buf.append(" And " + Imps.Contacts.TYPE + "=" + Imps.Contacts.TYPE_NORMAL + " And "
                    + Imps.Contacts.SUBSCRIPTION_TYPE + "=" + Imps.Contacts.SUBSCRIPTION_TYPE_BOTH);
            buf.append(" And " + Imps.Contacts.USERNAME + " LIKE " + "'%" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain) + "%'");

            String select = Imps.Contacts.ACCOUNT + "=? AND " + buf.toString();
            String[] selectionArgs = {GlobalVariable.account_id};
            String orderby = null;
            orderby = Imps.Contacts.ORDER_BY_LOCALIZED;

            loader = new CursorLoader(ChoosePwdFriendActivity.this, Imps.Contacts.CONTENT_URI, CONTACT_PROJECTION,
                    select, selectionArgs, orderby);

            loader.setUpdateThrottle(2000L);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, final Cursor newCursor) {
            if (mAsyncTask != null && !mAsyncTask.isCancelled()) {
                mAsyncTask.cancel(true);
            }
            mGridAdapter.removeAll();
            while (newCursor.moveToNext()) {
                FriendItem item = new FriendItem();

                item.userName = newCursor.getString(newCursor.getColumnIndex(Imps.Contacts.USERNAME));
                item.nickName = newCursor.getString(newCursor.getColumnIndex(Imps.Contacts.NICKNAME));
                item.isOnline = newCursor.getInt(newCursor.getColumnIndex(Imps.Contacts.PRESENCE_STATUS)) != Imps.Presence.OFFLINE;;

                mGridAdapter.addItem(item);
            }
            mLoaderManager.destroyLoader(mLoaderId);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                mTxtSearch.setText("");
                break;
            case R.id.btnOK:
                mPref.putString("password_friend_username", mStrPwdFriend);
                // send password friend username to server

                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            if (view instanceof AdapterView) {

            } else {
                ((ViewGroup) view).removeAllViews();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindDrawables(findViewById(R.id.rootView));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
