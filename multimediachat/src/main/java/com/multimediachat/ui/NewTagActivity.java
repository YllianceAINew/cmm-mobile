package com.multimediachat.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.plugin.xmpp.XmppAddress;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.dialog.CustomDialog;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.datamodel.FriendItem;

import java.util.ArrayList;


public class NewTagActivity extends BaseActivity {
    private static final int REQUEST_CHOOSE = 1;
    private static final int ERROR_CODE_GROUP_NAME = 0;

    ArrayList<Contact> mContacts = new ArrayList<>();
    GridListAdapter mListAdapter;
    GridView mGridView;
    boolean isDeleteMode = false;
    boolean isEditMode = false;
    EditText mEditName;
    private Button mCancelButton;
    private TextView mOkButton;
    private LinearLayout rootView;
    private Context mContext = null;

    String mTagname = null;
    ArrayList<String> mTagUsers = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.new_tag_activity);
        addTextButton(getString(R.string.save), R.id.btnOK, POS_RIGHT);

        mContext = this;
        mEditName = (EditText) findViewById(R.id.editTagName);
        rootView = (LinearLayout) findViewById(R.id.rootView);

        isEditMode = getIntent().getBooleanExtra("isEdit", false);
        if (isEditMode) {
            setActionBarTitle(getString(R.string.edit_tag));
            mTagname = getIntent().getStringExtra("tagname");
            mTagUsers = getIntent().getStringArrayListExtra("tagusers");

            for (int i = 0; i < mTagUsers.size(); i++) {
                Contact contact = DatabaseUtils.getContactInfo(cr, mTagUsers.get(i));
                mContacts.add(contact);
            }

            mEditName.setText(mTagname);
            mEditName.setSelection(mTagname.length());
            findViewById(R.id.btn_delete_tag).setVisibility(View.VISIBLE);
        } else {
            setActionBarTitle(getString(R.string.new_tag));
            findViewById(R.id.btn_delete_tag).setVisibility(View.GONE);
        }

        mOkButton = findViewById(R.id.btnOK);
        mOkButton.setEnabled(false);

        findViewById(R.id.btn_plus).setOnClickListener(this);
        findViewById(R.id.btn_minus).setOnClickListener(this);
        findViewById(R.id.btn_delete_tag).setOnClickListener(this);

        if ( !isEditMode )
            findViewById(R.id.btn_delete_tag).setVisibility(View.GONE);

        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mOkButton.setEnabled(!mEditName.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mGridView = (GridView) findViewById(R.id.gridView);
        mListAdapter = new GridListAdapter();
        mGridView.setAdapter(mListAdapter);

        //add this code to fix bug that gridview disappear while fast scrolling
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

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

        rootView.setOnTouchListener(new View.OnTouchListener() {
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

    }

    @Override
    public void onBackPressed() {
        if (mOkButton.isEnabled()) {
            if (!isEditMode){
                final CustomDialog dlg = new CustomDialog(this, getString(R.string.information), getString(R.string.tag_remove), getString(R.string.ok), getString(R.string.cancel));
                dlg.setOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });
                dlg.setOnOKClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                        NewTagActivity.super.onBackPressed();
                    }
                });
                dlg.show();
            } else {
                final CustomDialog dlg = new CustomDialog(this, getString(R.string.confirm), getString(R.string.save_this_change), getString(R.string.yes), getString(R.string.no));

                dlg.setOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                        finish();
                    }
                });

                dlg.setOnOKClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                        SaveTag();
                    }
                });

                dlg.show();
            }
            return;
        }

        InputMethodManager ime = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (ime.hideSoftInputFromWindow(mEditName.getWindowToken(), 0))
            return;

        super.onBackPressed();
    }

    private void SaveTagToDb(ArrayList<String> users) {
        if (isEditMode) {
            DatabaseUtils.editTagUsers(cr, mTagname, mEditName.getText().toString().trim(), users);
        } else {
            DatabaseUtils.insertOrUpdateTagUsers(cr, mEditName.getText().toString().trim(), users);
        }

        Intent intent = new Intent();
        intent.putExtra("tagname", mEditName.getText().toString().trim());
        intent.putExtra("tagusers", users);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void SaveTag() {
        final ArrayList<String> users = new ArrayList<>();

        for (int i = 0; i < mContacts.size(); i++) {
            Contact contact = mContacts.get(i);
            users.add(contact.getAddress().getAddress());
        }

        if (DatabaseUtils.isExistTag(cr, mEditName.getText().toString().trim())) {
            if (!isEditMode || !mEditName.getText().toString().trim().equals(mTagname)) {
                Toast.makeText(this, getString(R.string.tag_already_exist), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String mCurrentEditName = mEditName.getText().toString().trim();
        if (!mCurrentEditName.isEmpty() && isValidGroupName(mCurrentEditName)) {
            SaveTagToDb(users);
        }
    }
    public boolean isValidGroupName(String str) {

        if (!str.contains(","))
            return true;
        else {
            try {
                showErrorMessage(ERROR_CODE_GROUP_NAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
    private void showErrorMessage(int res) throws Exception {
        // custom dialog
        final Dialog dlg = GlobalFunc.createDialog(this, R.layout.msgdialog, true);

        TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
        title.setText(R.string.information);

        TextView content = (TextView) dlg.findViewById(R.id.msgcontent);
        if (res == ERROR_CODE_GROUP_NAME) {
            content.setText(R.string.error_message_failed_input_tagname);
        }

        Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
        dlg_btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dlg.setCanceledOnTouchOutside(false);
        dlg.show();
    }

    private void deleteTag() {
        DatabaseUtils.deleteTag(cr, mTagname);
        setResult(RESULT_OK);
        finish();
    }

    private void delTag() {
        final CustomDialog dlg = new CustomDialog(this, getString(R.string.delete_tag), getString(R.string.delete_tag_description), getString(R.string.delete), getString(R.string.cancel));

        dlg.setOnCancelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        dlg.setOnOKClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTag();
            }
        });

        dlg.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOK:
                SaveTag();
                break;

            case R.id.btn_delete_tag:
                delTag();
                break;

            case R.id.btn_plus:
                Intent intent = new Intent(this, ChooseFriendsActivity.class);
                ArrayList<String> members = new ArrayList<>();
                for (int i = 0; i < mContacts.size(); i++) {
                    Contact contact = mContacts.get(i);
                    members.add(contact.getAddress().getAddress());
                }
                intent.putExtra("member_list", members);
                intent.putExtra("call_from", ChooseFriendsActivity.CALL_FROM_SELECT_ONLY);
                startActivityForResult(intent, REQUEST_CHOOSE);

                break;

            case R.id.btn_minus:
                isDeleteMode = !isDeleteMode;
                mListAdapter.notifyDataSetChanged();
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE) {
            if (resultCode == RESULT_OK) {
                ArrayList<FriendItem> selectedList = data.getParcelableArrayListExtra("result.content");

                for (int i = 0; i < selectedList.size(); i++) {
                    FriendItem item = selectedList.get(i);
                    Contact contact = new Contact(new XmppAddress(item.userName), item.nickName);
                    mContacts.add(contact);
                }
                mOkButton.setEnabled(!mEditName.getText().toString().trim().isEmpty());
                isDeleteMode = false;
                mListAdapter.notifyDataSetChanged();
            }
        }
    }

    private class GridListAdapter extends BaseAdapter {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = NewTagActivity.this.getLayoutInflater().inflate(R.layout.new_tag_list_item, parent, false);
                viewHolder.img_profile = (CircularImageView) convertView.findViewById(R.id.img_profile);
                viewHolder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
                viewHolder.btn_delete = (ImageView) convertView.findViewById(R.id.btn_delete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Contact contact = mContacts.get(position);
            viewHolder.txt_name.setText(contact.getName());
            viewHolder.btn_delete.setVisibility(isDeleteMode ? View.VISIBLE : View.GONE);

            viewHolder.img_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDeleteMode) {
                        mOkButton.setEnabled(!mEditName.getText().toString().trim().isEmpty());
                        mContacts.remove(position);
                        mListAdapter.notifyDataSetChanged();
                    }
                }
            });
            // GlobalFunc.setProfileImage(viewHolder.img_profile, mContext, contact.getAddress().getAddress());
            GlobalFunc.showAvatar(mContext, contact.getAddress().getAddress(), viewHolder.img_profile);
            return convertView;
        }

        private class ViewHolder {
            CircularImageView img_profile;
            TextView txt_name;
            ImageView btn_delete;
        }
    }
}
