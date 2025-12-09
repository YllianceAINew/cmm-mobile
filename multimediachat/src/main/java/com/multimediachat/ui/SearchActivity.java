package com.multimediachat.ui;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.MenuItem;

import com.multimediachat.R;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.plugin.xmpp.XmppAddress;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;
import com.multimediachat.ui.views.FindFriendView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends BaseActivity implements OnClickListener{
	
	long							  mProviderId = -1;
	long						      mAccountId = -1;
	MainProgress    				  mProgressDlg;
	MenuItem						  mSearchMenuItem;
	MenuItem 						  searchConfirmItem;
	Context							  mContext;
	TextView 						  mEmptyView;
	ListView						  mListView;
	FindFriendAdapter	 			  mFindFriendAdapter;  //listview adapter
	List<Contact> 					  resultFriendList = new ArrayList<Contact>();
	EditText						  mTxtSearch;
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_find_by_id_or_name);
		setActionBarTitle(getString(R.string.friend_find_title));
		mContext = this;
		mListView = findViewById(R.id.listView);
		mListView.setEmptyView(mEmptyView);
		mListView.setFocusable(false);
		mEmptyView = findViewById(R.id.emptyView);
		mEmptyView.setText("");
		
		mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
		mAccountId = mPref.getLong(GlobalConstrants.store_picaAccountId, -1);
		
		IImConnection conn = mApp.getConnection(mProviderId);
		
		if ( conn == null && mAccountId > 0 && mProviderId > 0 ) {
			try{
				conn = mApp.createConnection(mProviderId, mAccountId);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		findViewById(R.id.btnClose).setOnClickListener(this);
		mTxtSearch = findViewById(R.id.txtSearch);
		mTxtSearch.setFocusableInTouchMode(true);

		mListView.setOnTouchListener(new View.OnTouchListener() {
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
		
		mTxtSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});

		mTxtSearch.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_ENTER:
						if (event.getAction() == KeyEvent.ACTION_UP)
							startFindFriendsByNameOrId(mTxtSearch.getText().toString());
						return true;
					default:
				}
				return false;
			}
		});
		
		mFindFriendAdapter = new FindFriendAdapter(this);
		mListView.setAdapter(mFindFriendAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Contact contact = (Contact)mFindFriendAdapter.getItem(position);
				String selection = Imps.Contacts.USERNAME + "=?";
				String selectedName = contact.getAddress().getAddress();
				String[] selectionArgs = {selectedName};
				Cursor cursor = mContext.getContentResolver().query(Imps.Contacts.CONTENT_URI, null, selection, selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
					contact.sub_type = cursor.getInt(cursor.getColumnIndex(Imps.Contacts.SUBSCRIPTION_TYPE));
					if (contact.sub_type != Imps.Contacts.SUBSCRIPTION_TYPE_BOTH) {
						showMiniProfile(contact);
					} else {
						Intent intent = new Intent(mContext, FriendProfileChattingActivity.class);
						FriendProfileChattingActivity.contact = contact;
						intent.putExtra("mFlag", false);
						mContext.startActivity(intent);
					}
				}
            	else
            		showMiniProfile(contact);
			}
		});
		
		mProgressDlg = new MainProgress(this);
		mProgressDlg.setMessage(getString(R.string.friend_find_by_id_or_name_finding_friends));
	}

	@Override
	protected void onResume() {
		super.onResume();
		startFindFriendsByNameOrId(mTxtSearch.getText().toString());
	}

	public void startFindFriendsByNameOrId(final String name) {
		if ( this == null || this.isFinishing() )
			return;

		if ( name == null || name.isEmpty() ) {
			mFindFriendAdapter.removeAll();
			mFindFriendAdapter.notifyDataSetChanged();
			return;
		}
		
		if (resultFriendList == null)
			resultFriendList = new ArrayList<Contact>();

		resultFriendList.clear();
		mFindFriendAdapter.removeAll();

		mProgressDlg.show();

		PicaApiUtility.findFriendByIdOrNumber(this, name, new MyXMLResponseHandler() {
			@Override
			public void onMySuccess(JSONObject response) {
				try {
					JSONObject jsonObject = response;
					if (jsonObject != null) {
						JSONObject jsonRecords = null;

						jsonRecords = jsonObject.getJSONObject(Imps.Params.RECORDS);

						if (jsonRecords.has(Imps.Params.RECORD)) {
							JSONObject jsonRecord = null;
							Contact contact = null;

							try {
								jsonRecord = jsonRecords.getJSONObject(Imps.Params.RECORD);
							} catch (JSONException e1) {
								e1.printStackTrace();
							}

							JSONArray jsonRecordArray = new JSONArray();

							if ( jsonRecord != null )
								jsonRecordArray.put(jsonRecord);
							else
								jsonRecordArray = jsonRecords.getJSONArray(Imps.Params.RECORD);

							String username = mPref.getString("username", "");

							for (int i = 0; i < jsonRecordArray.length(); i++) {
								jsonRecord = jsonRecordArray.getJSONObject(i);
								contact = new Contact(new XmppAddress(jsonRecord.getString("username")+"@"+mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain)), jsonRecord.getString("name"));

								if ( contact == null )
									continue;

								String address = contact.getAddress().getAddress();

								if ( address == null )
									continue;

								if ( StringUtils.parseName(address).equals(username) )
									continue;

								//Check who is the member.
								Cursor cursor = null;
								String nickName = null;
								String greeting = null;

								String selection = Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
								String[] selectionArgs = {GlobalVariable.account_id, address};
								cursor = getContentResolver().query(Imps.Contacts.CONTENT_URI,
										new String[]{Imps.Contacts.SUBSCRIPTION_TYPE, Imps.Contacts.NICKNAME, Imps.Contacts.TYPE, Imps.Contacts.SUBSCRIPTIONMESSAGE},
										selection,
										selectionArgs, null);

								if ( cursor != null && cursor.moveToFirst() ) {
									int sub_type = cursor.getInt(0);
									nickName = cursor.getString(1);
									greeting = cursor.getString(3);
									contact.sub_type = sub_type;
									if ( sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM ) {
										contact.setGreeting(greeting);
									}
									if ( nickName != null )
										contact.setName(nickName);

									if ( greeting != null )
										contact.setGreeting(greeting);
								}else{
									contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;
								}

								if ( cursor != null )
									cursor.close();

								mFindFriendAdapter.addItem(contact);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				mFindFriendAdapter.notifyDataSetChanged();

				if ( mProgressDlg.isShowing() )
					mProgressDlg.dismiss();

				if ( mFindFriendAdapter.getCount() == 0 ) {
					GlobalFunc.showToast(SearchActivity.this, R.string.msg_for_find_friends_failure, false);
				}
			}

			@Override
			public void onMyFailure(int errcode) {
				if ( mProgressDlg.isShowing() )
					mProgressDlg.dismiss();

				GlobalFunc.showToast(SearchActivity.this, R.string.msg_for_find_friends_failure, false);
			}
		});
	}

	private class FindFriendAdapter extends BaseAdapter {

        List<Contact> mData = new ArrayList<Contact>();

        public FindFriendAdapter(Context context) {
        }

        public void addItem(Contact contact) {
            mData.add(contact);
        }

        public void removeAll() {
            mData.clear();
        }

        public int getCount() {
            if (mData == null)
                return 0;
            else
                return mData.size();
        }

        public Object getItem(int position) {
            if (mData == null)
                return null;

            return mData.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FindFriendView view = null;
            if (convertView != null) {
                view = (FindFriendView) convertView;
            } else {
                view = (FindFriendView) SearchActivity.this.getLayoutInflater().inflate(R.layout.item_nearby, parent, false);
            }

            Contact contact = null;
            try {
                if (mData != null && mData.size() > 0) {
                    contact = mData.get(position);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            view.bind(contact);

			if ( contact != null && contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_BOTH ) {
				view.findViewById(R.id.btn_plus).setEnabled(false);
			} else {
				view.findViewById(R.id.btn_plus).setEnabled(true);
			}
            return view;
        }
    }

	private void showMiniProfile(Contact contact) {
        Intent intent = new Intent(this, FriendProfileChattingActivity.class);
        FriendProfileChattingActivity.contact = contact;
		intent.putExtra("mFlag", true);
		startActivity(intent);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnClose:
			mTxtSearch.setText("");
			break;
		default:
			super.onClick(v);
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
		case android.R.id.home:
			finish();
			break;
			
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
        	view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            	unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		try{
			unbindDrawables(findViewById(R.id.rootView));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
