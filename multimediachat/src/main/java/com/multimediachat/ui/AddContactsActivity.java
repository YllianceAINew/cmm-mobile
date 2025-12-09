package com.multimediachat.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.qrcode.client.android.CaptureActivity;


public class AddContactsActivity extends BaseActivity {
    TextView txt_receivedreq_desc;
    private ImageView mQRCodeView;

    private IntentFilter filter;
    private UpdaterBroadcastReceiver updateBroadcaseReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

        filter = new IntentFilter(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET);
        updateBroadcaseReceiver = new UpdaterBroadcastReceiver();
        registerReceiver(updateBroadcaseReceiver, filter);
    }

    private void initUI() {
        setContentView(R.layout.add_contacts_activity);
        setActionBarTitle(getString(R.string.title_add_contacts));

        findViewById(R.id.img_qrcode).setOnClickListener(this);
        findViewById(R.id.lyt_mobilecontacts).setOnClickListener(this);
        findViewById(R.id.lyt_searchid).setOnClickListener(this);
        findViewById(R.id.lyt_receivedreq).setOnClickListener(this);
        findViewById(R.id.lyt_scanqrcode).setOnClickListener(this);

        TextView mID = (TextView) findViewById(R.id.my_id);
        mID.setText(DatabaseUtils.getUserID(getContentResolver()));

        mQRCodeView = (ImageView) findViewById(R.id.img_qrcode);
        txt_receivedreq_desc = (TextView) findViewById(R.id.txt_receivedreq_desc);

        String address = getIntent().getStringExtra("address");
        if ( address == null )
            address = mPref.getString("username", "") + "@" + GlobalConstrants.server_domain;

        GlobalFunc.showQRCode(address, mQRCodeView);

        updateRequestCount();
    }

    public class UpdaterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(MainTabNavigationActivity.BROADCAST_UPDATE_WIDGET)) {
                updateRequestCount();
            }
        }
    }

    @Override
    protected void onResume() {
        updateRequestCount();
        super.onResume();
    }

    private void updateRequestCount() {
        txt_receivedreq_desc.setText(String.format("%d %s", Imps.Contacts.getReceivedRequestsCount(cr), getString(R.string.friend_requests_title_receive)));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.img_qrcode:
                intent = new Intent(AddContactsActivity.this, MyQRCodeActivity.class);
                startActivity(intent);
                break;
            case R.id.lyt_mobilecontacts:
                intent = new Intent(AddContactsActivity.this, MobileContactsActivity.class);
                startActivity(intent);
                break;
            case R.id.lyt_searchid:
                intent = new Intent(AddContactsActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.lyt_receivedreq:
                intent = new Intent(AddContactsActivity.this, SubscriptionActivity.class);
                startActivity(intent);
                break;
            case R.id.lyt_scanqrcode:
                checkCameraPermission(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AddContactsActivity.this, CaptureActivity.class);
                        startActivity(intent);
                    }
                });
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateBroadcaseReceiver != null) {
            unregisterReceiver(updateBroadcaseReceiver);
        }
    }

}
