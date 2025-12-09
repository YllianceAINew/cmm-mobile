package com.multimediachat.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.plugin.xmpp.XmppAddress;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.util.connection.MyJSONResponseHandler;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.multimediachat.ui.MainTabNavigationActivity.BROADCAST_FRIEND_LIST_RELOAD;

/**
 * Created by jack on 12/8/2017.
 */

public class MobileContactsActivity extends BaseActivity {
    ListView mListView;
    ArrayList<Contact> mContacts = new ArrayList<>();
    ContactListAdapter mListAdapter;
    MainProgress mProgressDlg;
    TextView mEmptyView;

    UpdaterBroadcastReceiver updateBroadcaseReceiver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI()
    {
        setContentView(R.layout.listview_activity);
        setActionBarTitle(getString(R.string.mobile_contacts));
        mListView = (ListView) findViewById(R.id.listview);
        mEmptyView = (TextView) findViewById(R.id.empty);
        mListView.setEmptyView(mEmptyView);

        mListAdapter = new ContactListAdapter();
        mListView.setAdapter(mListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Contact contact = mContacts.get(position);

                Contact c = DatabaseUtils.getContactInfo(cr, contact.getAddress().getAddress());

                if ( c != null && c.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_BOTH)
                {
                    Intent intent = new Intent(MobileContactsActivity.this, FriendProfileChattingActivity.class);
                    intent.putExtra("mFlag", false);
                    FriendProfileChattingActivity.contact = c;
                    startActivity(intent);
                }
                else
                {
                    mProgressDlg.show();
                    PicaApiUtility.getProfile(MobileContactsActivity.this, contact.getAddress().getUser(), new MyXMLResponseHandler() {
                        @Override
                        public void onMySuccess(JSONObject response) {
                            mProgressDlg.dismiss();
                            String region = null;
                            try {
                                region = response.getString(Imps.Contacts.REGION);

                            if ( region == null )
                                contact.region = GlobalVariable.Region;
                            else
                                contact.region = region;

                            contact.status = response.getString(Imps.Contacts.STATUS);
                            contact.setHash(response.getString(Imps.Contacts.HASH));
                            contact.gender = String.valueOf(response.getString(Imps.Contacts.GENDER));
                            contact.userid = response.getString(Imps.Contacts.USERID);
                            contact.phoneNum = response.getString(Imps.Contacts.PHONE_NUMBER);

                            DatabaseUtils.insertOrUpdateContactInfo(cr, contact);

                            Intent intent = new Intent(MobileContactsActivity.this, FriendProfileChattingActivity.class);
                            intent.putExtra("mFlag", true);
                            FriendProfileChattingActivity.contact = contact;
                            startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onMyFailure(int errcode) {
                            mProgressDlg.dismiss();
                            Toast.makeText(MobileContactsActivity.this, getResources().getString(R.string.get_profile_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        mProgressDlg = new MainProgress(this);

        mProgressDlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    onBackPressed();
                }
                return false;
            }
        });

        IntentFilter filter = new IntentFilter(BROADCAST_FRIEND_LIST_RELOAD);
        updateBroadcaseReceiver = new UpdaterBroadcastReceiver();
        registerReceiver(updateBroadcaseReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetContactsIntoArrayList();
    }

    @Override
    public void onBackPressed() {
        if (mProgressDlg != null && mProgressDlg.isShowing() && !isFinishing()) {
            mProgressDlg.dismiss();
            mProgressDlg = null;
        }

        super.onBackPressed();
    }

    public void GetContactsIntoArrayList(){
        mProgressDlg.show();

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        final ArrayList<JSONObject> contacts = new ArrayList<>();
        ArrayList<String> phones = new ArrayList<>();

        while (cursor.moveToNext()) {

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

            phonenumber = phonenumber.replace(" ", "");
            //phonenumber = phonenumber.replace("+", "");

            JSONObject obj = new JSONObject();
            try {
                obj.put("name", name);
                obj.put("phonenumber", phonenumber);
                obj.put("photouri", photoUri);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            contacts.add(obj);

            phones.add(phonenumber);
        }

        cursor.close();

        final ArrayList<Contact> result = new ArrayList<Contact>();
        PicaApiUtility.getMobileContacts(this, phones, new MyXMLResponseHandler() {
            @Override
            public void onMySuccess(JSONObject jsonObject) {
                try {
                    JSONObject jsonRecords = null;
                    jsonRecords = jsonObject.getJSONObject("data");

                    if (!jsonRecords.has("record")) return;

                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonRecord = null;

                    try {
                        jsonRecord = jsonRecords.getJSONObject("record");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    if (jsonRecord != null) {
                        jsonArray.put(jsonRecord);
                    } else
                        jsonArray = jsonRecords.getJSONArray("record");

                    String username = mPref.getString("username", "") + "@" + mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);

                    for ( int i = 0; i < jsonArray.length(); i ++ )
                    {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String address = object.getString("uid")+"@"+ mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain);
                        String fullname = object.getString("fullname");
                        String phone = object.getString(Imps.Contacts.PHONE_NUMBER);
                        String mobileName = "";
                        String photoUri = "";

                        if ( address.equals(username) )
                            continue;

                        Contact contact = DatabaseUtils.getContactInfo(cr, address);

                        if ( contact == null )
                        {
                            contact = new Contact(new XmppAddress(address), fullname);
                            contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;
                            contact.phoneNum = phone;
                        }

                        for ( int j = 0; j < contacts.size(); j ++ )
                        {
                            JSONObject obj = contacts.get(j);

                            if ( obj.getString("phonenumber").equals(phone) )
                            {
                                mobileName = obj.getString("name");
                                if (obj.has("photouri")) {
                                    photoUri = obj.getString("photouri");
                                }
                                contacts.remove(obj);
                                break;
                            }
                        }
                        contact.mMobileName = mobileName;
                        contact.mFullName = fullname;
                        contact.hash = photoUri;

                        result.add(contact);
                    }

                    mContacts = result;
                    mListAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {

                }
                if (mProgressDlg != null) {
                    mProgressDlg.dismiss();
                }
            }

            @Override
            public void onMyFailure(int errcode) {
                if (mProgressDlg != null) {
                    mProgressDlg.dismiss();
                }
            }
        });
    }

    private void showMiniProfile(Contact contact) {
        Intent intent = new Intent(this, FriendProfileChattingActivity.class);
        FriendProfileChattingActivity.contact = contact;
        startActivity(intent);
    }

    private class ContactListAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return mContacts.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if ( convertView == null )
            {
                convertView = MobileContactsActivity.this.getLayoutInflater().inflate(R.layout.mobile_contacts_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.img_profile = (CircularImageView) convertView.findViewById(R.id.img_profile);
                viewHolder.txt_contactname = (TextView)convertView.findViewById(R.id.txt_contactname);
                viewHolder.txt_chatname = (TextView)convertView.findViewById(R.id.txt_chatname);
                viewHolder.btn_plus = (ImageView)convertView.findViewById(R.id.btn_plus);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            final Contact contact = mContacts.get(position);
           /* Bitmap image = null;
            try {
                if (contact.hash != null && !contact.hash.isEmpty()) {
                    image = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(contact.hash));

                    if (image != null) {
                        viewHolder.img_profile.setImageBitmap(image);
                    }
                } else {
                    viewHolder.img_profile.setImageResource(R.drawable.profilephoto);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
*/
            GlobalFunc.setProfileImage(viewHolder.img_profile, MobileContactsActivity.this, contact.getAddress().getAddress());

            viewHolder.txt_contactname.setText(contact.mMobileName);
            viewHolder.txt_contactname.setSelected(true);
            viewHolder.txt_chatname.setText(contact.getName());
            viewHolder.txt_chatname.setSelected(true);

            if ( contact.sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_BOTH ) {
                viewHolder.btn_plus.setEnabled(false);
            } else {
                viewHolder.btn_plus.setEnabled(true);
            }

            return convertView;
        }

        private class ViewHolder
        {
            CircularImageView img_profile;
            TextView txt_contactname;
            TextView txt_chatname;
            ImageView btn_plus;
        }
    }

    public class UpdaterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(BROADCAST_FRIEND_LIST_RELOAD)) {
                for ( int i = 0; i < mContacts.size(); i ++ )
                {
                    Contact contact = mContacts.get(i);
                    Contact c = DatabaseUtils.getContactInfo(cr, contact.getAddress().getAddress());

                    if ( c != null ) {
                        mContacts.remove(i);
                        c.mMobileName = contact.mMobileName;
                        mContacts.add(i, c);
                    } else {
                        if ( contact.sub_type != Imps.Contacts.SUBSCRIPTION_TYPE_NONE )
                            contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;
                    }
                }

                mListAdapter.notifyDataSetChanged();
            }
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
