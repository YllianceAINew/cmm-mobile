package com.multimediachat.ui;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.Utils;
import com.multimediachat.global.ErrorManager;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.ImPluginHelper;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.util.connection.MyJSONResponseHandler;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;


public class LoginActivity extends BaseActivity implements OnClickListener {

    private MainProgress progressDlg;

    String mUserName = "";
    String mApiKey = "";
    String realPN = "";
    String strPassword = "";
    String strUserId = "";
    String strLoginKey = "";

    EditText mEditUserID;
    EditText mEditPassword;

    ImageView mImgSaveInfo;

    final Context context = this;

    Handler mHandler;

    private LoginWaitThread mLoginThread;
    private View mBtnLogin;

    public static LoginActivity instance = null;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        GlobalFunc.makeLocalDir();

        mApp.resetProviderSettings(); // clear cached provider list
        mHandler = new Handler();

        initUI();

        saveStep(GlobalConstrants.STEP_NEED_LOGIN);

        // init progress dialog
        progressDlg = new MainProgress(this);
        progressDlg.setMessage("");

        instance = this;

    }

    private void saveStep(int step) {
        mPref.putInt(GlobalConstrants.store_step, step);
    }

    private void initUI() {
        setContentView(R.layout.login_activity);
        setActionBarTitle(getString(R.string.login));

        mEditUserID = findViewById(R.id.edit_user_id);
        mEditUserID.addTextChangedListener(mTxtWatcher);

        mEditPassword = findViewById(R.id.edit_password_login);
        mEditPassword.addTextChangedListener(mTxtWatcher);

        mImgSaveInfo = findViewById(R.id.img_save_info);
        mImgSaveInfo.setOnClickListener(this);
        findViewById(R.id.txt_save_info).setOnClickListener(this);

        mBtnLogin = findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(this);

        findViewById(R.id.rootView_login).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    InputMethodManager imm = (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        mImgSaveInfo.setSelected(mPref.getBoolean("save_login_info"));
        if (mImgSaveInfo.isSelected()) {
            mEditUserID.setText(mPref.getString("username", ""));
            mEditPassword.setText(mPref.getString("password", ""));
        }

        findViewById(R.id.lyt_forgot_password).setOnClickListener(this);

        updateLoginButton();

    }

    private void onBtnLogin() {
        strUserId = mEditUserID.getText().toString();
        strPassword = mEditPassword.getText().toString();

        File keyFile = new File(GlobalConstrants.LOGIN_KEY_PATH);
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(keyFile));
            byte[] buf = new byte[1024];
            int len = bis.read(buf);
            strLoginKey = new String(buf, 0, len);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!strUserId.isEmpty() && !strPassword.isEmpty()) {
            loginAccount();
        }
    }

    private void onLoginSuccess(JSONObject jsonObject) {
        try {
            mUserName = jsonObject.getString("username");
            mApiKey = jsonObject.getString("apikey");
            String strBirthnum = jsonObject.getString("birthnum");

            String username = mPref.getString("username", "");
            if (!username.isEmpty() && !username.equals(mUserName)) {  // Truncate chats, tags, contacts, inmemorymessages table
                getContentResolver().delete(Imps.Chats.CONTENT_URI, null, null);
                getContentResolver().delete(Imps.Messages.CONTENT_URI, null, null);
                getContentResolver().delete(Imps.Tags.CONTENT_URI, null, null);
                getContentResolver().delete(Imps.Contacts.CONTENT_URI, null, null);
            }

            ImApp.getInstance().userName = mUserName;
            ImApp.getInstance().api_key = mApiKey;

            String fileServer = jsonObject.getString("fileserver");
            if (!fileServer.isEmpty()) {
                GlobalConstrants.file_server_domain = fileServer;
                GlobalVariable.FILE_SERVER_URL = mPref.getString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header) + fileServer + "/rsvcs/";
                mPref.putString(GlobalConstrants.FILE_SERVER, fileServer);
                mPref.putString(GlobalConstrants.FILE_SERVER_URL, GlobalVariable.FILE_SERVER_URL);
            }

            mPref.putString("apikey", ImApp.getInstance().api_key);
            mPref.putString("username", ImApp.getInstance().userName);
            mPref.putString("birthnum", strBirthnum);
            mPref.putString("password", strPassword);

            // providerid, accountid, accounturi setting
            GlobalVariable.account_id = Long.toString(ImApp.insertOrUpdateAccount(cr, 0, mUserName, ""));
            DatabaseUtils.mAccountID = GlobalVariable.account_id;
            Imps.mAccountID = GlobalVariable.account_id;

            ImPluginHelper helper = ImPluginHelper.getInstance(LoginActivity.this);
            long mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
            if (mProviderId < 1) // needless
                mProviderId = helper.createAdditionalProvider(helper.getProviderNames().get(0)); // xmpp
            mPref.putLong(GlobalConstrants.store_picaProviderId, mProviderId);
            mPref.putLong(GlobalConstrants.store_picaAccountId, Long.parseLong(GlobalVariable.account_id));

            doDownLoadProfileInfo();

        } catch (JSONException e) {
            e.printStackTrace();
            onLoginFailure(-1);
        }
    }

    private void doDownLoadProfileInfo() {
        if (!LoginActivity.this.isFinishing() && progressDlg != null && !progressDlg.isShowing())
            progressDlg.show();

        PicaApiUtility.getCurrentUserInformation(this, new MyJSONResponseHandler() {
            @Override
            public void onMySuccess(JSONObject response) {
                mLoginThread = new LoginWaitThread();
                mLoginThread.start();
            }

            @Override
            public void onMyFailure(int errcode) {
                if (progressDlg != null && progressDlg.isShowing()) {
                    progressDlg.dismiss();
                }
                GlobalFunc.showErrorMessageToast(LoginActivity.this, errcode, false);
            }
        });
    }

    public void onLoginFailure(int errcode) {
        if (errcode == 12)
            AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_NOT_ALLOWED);
        else if (errcode == 8)
            AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_UNREGISTERED_PHONENUMBER);
        else if (errcode == 14)
            AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_UNREGISTERED_USERID);
        else if (errcode == 15)
            AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_PASSWORD);
        else if (errcode == 16)
            AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_API_KEY);
        else
            GlobalFunc.showErrorMessageToast(LoginActivity.this, errcode, false);
    }

    public void loginAccount() {

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showErrorMessageToast(LoginActivity.this, 200, false);
            return;
        }

        try {
            progressDlg.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PicaApiUtility.loginAccount(this, strUserId, Utils.getPhoneNumber(),
            Utils.getIMSI(), strPassword, strLoginKey, new MyXMLResponseHandler() {
                @Override
                public void onMySuccess(JSONObject response) {
                    onLoginSuccess(response);
                }

                @Override
                public void onMyFailure(int errcode) {
                    if (progressDlg != null && progressDlg.isShowing()) {
                        progressDlg.dismiss();
                    }

                    if (errcode == 10) {
                        mPref.putString("username", strUserId);
                        startActivity(new Intent(LoginActivity.this, CheckUserInfoActivity.class));
                    } else
                        onLoginFailure(errcode);
                }
            });
    }

    private class LoginWaitThread extends Thread {
        long startTime = System.currentTimeMillis();
        public void run() {
            GlobalVariable.authorizedState = 0;
            while (!ImApp.getInstance().isServerLogined()) {
                if ((System.currentTimeMillis() - startTime) > GlobalConstrants.XMPP_LOGIN_TIMEOUT) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDlg != null && progressDlg.isShowing()) {
                                progressDlg.dismiss();
                            }
                            AndroidUtility.showErrorMessage(LoginActivity.this, AndroidUtility.ERROR_XMPP_LOGIN);
                        }
                    });
                    break;
                }
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
                if (GlobalVariable.authorizedState != 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDlg != null && progressDlg.isShowing()) {
                                progressDlg.dismiss();
                            }
                            /*if (GlobalVariable.authorizedState == 1)
                                AndroidUtility.showErrorMessage(LoginActivity.this, AndroidUtility.ERROR_TOKEN_EXPIRED);
                            else
                                AndroidUtility.showErrorMessage(LoginActivity.this, AndroidUtility.ERROR_TOKEN_WRONG);*/
                        }
                    });
                    break;
                }
            }

            if (ImApp.getInstance().isServerLogined()) {
                mPref.putInt(GlobalConstrants.store_step, GlobalConstrants.STEP_LOGGED_IN);
                Uri mAccountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, Long.parseLong(GlobalVariable.account_id));
                ContentValues values = new ContentValues();
                values.put(Imps.AccountColumns.KEEP_SIGNED_IN, 1);
                context.getContentResolver().update(mAccountUri, values, null, null);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onLoginReady();
                    }
                });
            }
            mLoginThread = null;
        }
    }

    protected void onLoginReady() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (progressDlg != null && progressDlg.isShowing()) {
                    progressDlg.dismiss();
                }

                GlobalFunc.doSipLogin();

                Intent intent = new Intent(LoginActivity.this, MainTabNavigationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                onBtnLogin();
                break;
            case R.id.img_save_info:
            case R.id.txt_save_info:
                changeSaveInfoState();
                break;
            case R.id.lyt_forgot_password:
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void changeSaveInfoState() {
        mImgSaveInfo.setSelected(!mImgSaveInfo.isSelected());
        mPref.putBoolean("save_login_info", mImgSaveInfo.isSelected());
    }

    TextWatcher mTxtWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateLoginButton();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void updateLoginButton() {
        mBtnLogin.setEnabled(!mEditUserID.getText().toString().isEmpty() && !mEditPassword.getText().toString().isEmpty());
    }
}
