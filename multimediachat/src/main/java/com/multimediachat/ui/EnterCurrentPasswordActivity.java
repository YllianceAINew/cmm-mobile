package com.multimediachat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.ImApp;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.json.JSONObject;

/**
 * Created by Administrator on 2018.02.19.
 */

public class EnterCurrentPasswordActivity extends BaseActivity {
    EditText mEditPwd;
    TextView mTxtUserId;

    MainProgress mProgressDlg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI()
    {
        setContentView(R.layout.activity_enter_current_password);
        setActionBarTitle(getString(R.string.password_change));

        findViewById(R.id.btn_next).setOnClickListener(this);

        mEditPwd = (EditText) findViewById(R.id.edit_pwd);
        mTxtUserId = (TextView) findViewById(R.id.txt_user_id);
        mTxtUserId.setText(mPref.getString("username", ""));

        mProgressDlg = new MainProgress(this);
        mProgressDlg.setMessage("");
    }

    private void onNext()
    {

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showErrorMessageToast(EnterCurrentPasswordActivity.this, 200, false);
            return;
        }

        final String pwd = mEditPwd.getText().toString();

        if ( pwd.isEmpty() )
        {
            AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_PASSWORD_EMPTY);
            return;
        }

        if (mProgressDlg != null) {
            try {
                mProgressDlg.setMessage("");
                mProgressDlg.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PicaApiUtility.confirmPassword(this, pwd, new MyXMLResponseHandler() {
            @Override
            public void onMySuccess(JSONObject response) {
                if (mProgressDlg != null && mProgressDlg.isShowing())
                    mProgressDlg.dismiss();

                Intent intent = new Intent(EnterCurrentPasswordActivity.this, ChangePWDActivity.class);
                intent.putExtra("password", pwd);
                startActivity(intent);
                finish();
            }

            @Override
            public void onMyFailure(int errcode) {
                if (mProgressDlg != null && mProgressDlg.isShowing())
                    mProgressDlg.dismiss();
                if (errcode == 15)
                    AndroidUtility.showErrorMessage(EnterCurrentPasswordActivity.this, AndroidUtility.ERROR_CODE_PASSWORD);
                else
                    GlobalFunc.showErrorMessageToast(EnterCurrentPasswordActivity.this, errcode, false);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_next:
                onNext();
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
