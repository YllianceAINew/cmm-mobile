package com.multimediachat.ui.views;

import com.multimediachat.R;
import com.multimediachat.app.SimpleAlertHandler;
import com.multimediachat.app.im.IContactListManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.app.adapter.ConnectionListenerAdapter;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.SubscriptionActivity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SubscriptionFriendListView extends LinearLayout {
	
	private final int			CONFIRM_ACCEPT = 0;
	private final int			CONFIRM_DECLINE = 1;
	
	
    private AbsListView mFilterList;
    private SubscriptionFriendListAdapter mSubscriptionFriendListAdapter;
    //private TextView mEmptyView = null;
    private final Context mContext;
    private final SimpleAlertHandler mHandler;
    private final ConnectionListenerAdapter mConnectionListener;
    
    SubscriptionActivity mActivity;
    private IImConnection mConn;
    private int subscription_type = Imps.Contacts.SUBSCRIPTION_TYPE_FROM; //0:subscription_from, 1:subscription_to 
    
    private MyLoaderCallbacks mLoaderCallbacks;
    private LoaderManager mLoaderManager;
    private int mLoaderId;
    
    public SubscriptionFriendListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (SubscriptionActivity)context;
        mContext = context;
        mHandler = new SimpleAlertHandler((Activity)context);
        mConnectionListener = new ConnectionListenerAdapter(mHandler) {
            @Override
            public void onConnectionStateChange(IImConnection connection, int state,
                    ImErrorInfo error) {
            }

            @Override
            public void onUpdateSelfPresenceError(IImConnection connection, ImErrorInfo error) {
                super.onUpdateSelfPresenceError(connection, error);
            }
            @Override
            public void onSelfPresenceUpdated(IImConnection connection) {
                super.onSelfPresenceUpdated(connection);
            }  
        };
    }
    
    @Override
    protected void onFinishInflate() {

        mFilterList = (AbsListView) findViewById(R.id.filteredList);
        mFilterList.setTextFilterEnabled(true);

        TextView mEmptyView = (TextView) findViewById(R.id.emptyView);
        mFilterList.setEmptyView(mEmptyView);
    }

    public AbsListView getListView() {
        return mFilterList;
    }

    public Cursor getContactAtPosition(int position) {
        return (Cursor) mSubscriptionFriendListAdapter.getItem(position);
    }

    public void setConnection(IImConnection conn) {
       
        if (mConn != conn) {
            if (mConn != null) {
                unregisterListeners();
            }
            
            mConn = conn;

            if (conn != null) {
                registerListeners();
            }
        }
    }

    private void registerListeners() {
        try {
            mConn.registerConnectionListener(mConnectionListener);
        } catch (Exception e) {
            mHandler.showServiceErrorAlert(e.getLocalizedMessage());
        }
    }

    private void unregisterListeners() {
        try {
            mConn.unregisterConnectionListener(mConnectionListener);
        } catch (Exception e) {
            mHandler.showServiceErrorAlert(e.getLocalizedMessage());
        }
    }
    
    public void setLoaderManager(LoaderManager loaderManager, int loaderId) {
        mLoaderManager = loaderManager;
        mLoaderId = loaderId;
    }
    
	public void showList() {
    	if (mSubscriptionFriendListAdapter == null) {
        	mSubscriptionFriendListAdapter = new SubscriptionFriendListAdapter(mContext, R.layout.subscription_friend_view);
            mFilterList.setAdapter(mSubscriptionFriendListAdapter);
            mLoaderCallbacks = new MyLoaderCallbacks();
            mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);
    	} else {
    		mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
    	}
	}
	
	public void setSubscriptionType(int sub_type){
		subscription_type = sub_type;
	}
    
	private class SubscriptionFriendListAdapter extends ResourceCursorAdapter {
        public SubscriptionFriendListAdapter(Context context, int view) {
            super(context, view, null, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            SubscriptionFriendView v = (SubscriptionFriendView) view;
            v.bind(cursor);
            
        }
    }
	
	class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        	
            CursorLoader loader = new CursorLoader(getContext(), 
            		Imps.Contacts.CONTENT_URI,
            		SubscriptionFriendView.CONTACT_PROJECTION,
                    Imps.Contacts.SUBSCRIPTION_TYPE + "=? AND " + Imps.Contacts.ACCOUNT + "=?",
                    new String[]{String.valueOf(subscription_type), GlobalVariable.account_id},
                    Imps.Contacts.DEFAULT_SORT_ORDER);
            
            loader.setUpdateThrottle(100L);
            
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            mSubscriptionFriendListAdapter.swapCursor(newCursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mSubscriptionFriendListAdapter.swapCursor(null);
        }
        
    }
    
}

