package com.multimediachat.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.multimediachat.app.ImApp;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.im.IContactListManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;

/**
 * Created by jack on 1/13/2018.
 */

public class FriendRequestActivity extends BaseActivity {
    public static Contact contact = null;
    EditText edit_greeting;
    MainProgress mProgressDlg;
    long mProviderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendrequest);
        setActionBarTitle(getString(R.string.friend_request));

        mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
        initUI();
    }

    private void initUI()
    {
        mProgressDlg = new MainProgress(this);
        mProgressDlg.setMessage("");

        edit_greeting = (EditText) findViewById(R.id.txt_greeting);

        addTextButton(getString(R.string.send), R.id.btn_send, POS_RIGHT);
    }

    private void doRequest(){
        new AsyncTask<String, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mProgressDlg != null) {
                    if (!mProgressDlg.isShowing() && !FriendRequestActivity.this.isFinishing())
                        mProgressDlg.show();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                if ( contact == null || mProviderId < 1)
                    return null;

                String greeting = params[0];
                if (greeting.isEmpty()) {
                    greeting = " ";
                }

                IImConnection conn = ((ImApp)getApplication()).getConnection(mProviderId);

                if (conn==null)
                    return null;

                try
                {
                    IContactListManager contactListMgr = conn.getContactListManager();
                    if (contactListMgr==null)
                        return null;
                    contactListMgr.requestSubscription(contact, greeting);
                    return "ok";
                }catch(Exception e){
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (mProgressDlg != null && mProgressDlg.isShowing() && !FriendRequestActivity.this.isFinishing())
                    mProgressDlg.dismiss();
                if ( result == null || !result.equals("ok") )
                    GlobalFunc.showToast(FriendRequestActivity.this, R.string.error_message_network_connect, false);
                else {
                    Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD);
                    sendBroadcast(intent);
                    Intent intent1 = new Intent(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
                    sendBroadcast(intent1);

                    finish();
                }
            }
        }.execute(edit_greeting.getText().toString());
    }

    private void saveName()
    {
//        if ( !edit_name.getText().toString().equals(contact.getName() )) {
//            DatabaseUtils.updateNickName(cr, contact.getAddress().getAddress(), edit_name.getText().toString());
//        }
        doRequest();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_send:
                mProgressDlg.show();
                saveName();
                break;
            default:
                super.onClick(v);
        }
    }
}
