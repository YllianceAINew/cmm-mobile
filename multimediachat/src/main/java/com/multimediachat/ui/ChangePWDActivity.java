package com.multimediachat.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.ImApp;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;
import org.json.JSONObject;

public class ChangePWDActivity extends BaseActivity implements OnClickListener{
    EditText editPWD;
    EditText editConfirmPassword;
    String mOldPassword;

    private MainProgress dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOldPassword = getIntent().getStringExtra("password");
        initUI();
    }

    private void initUI(){
        setContentView(R.layout.activity_change_pwd);

        setActionBarTitle(getString(R.string.set_pwd));

        dialog = new MainProgress(this);

        editPWD = (EditText) findViewById(R.id.edit_new_pwd);
        editConfirmPassword = (EditText) findViewById(R.id.edit_confirm_pwd);

        findViewById(R.id.btn_change).setOnClickListener(this);

        ((TextView)findViewById(R.id.txt_user_id)).setText(mPref.getString("username", ""));
    }

    private void ChangePWD() {

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showErrorMessageToast(ChangePWDActivity.this, 200, false);
            return;
        }

        String password = editPWD.getText().toString();
        final String confirmPassword = editConfirmPassword.getText().toString();
        if ( password.length() == 0) {
            try {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_PASSWORD_EMPTY);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if (!password.equals(confirmPassword)) {
            try {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_PASSWORD_NOT_MATCH);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if ( password.length() < 6 || password.length() > 20) {
            try {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_PASSWORD_LENGTH_NOT_ENOUGH);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if (dialog != null) {
            try {
                dialog.setMessage("");
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PicaApiUtility.changePassword(this, mOldPassword, confirmPassword, new MyXMLResponseHandler() {
            @Override
            public void onMySuccess(JSONObject response) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();

                mPref.putString("password", confirmPassword);
                GlobalFunc.doSipLogin();
                onBackPressed();

                GlobalFunc.showToast(ChangePWDActivity.this, R.string.change_pwd_success, false);
            }

            @Override
            public void onMyFailure(int errcode) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                AndroidUtility.showErrorMessage(ChangePWDActivity.this, AndroidUtility.ERROR_CODE_FAILED_CHANGE_PWD);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_change:
                ChangePWD();
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}