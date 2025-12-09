package com.multimediachat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.Utils;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.Utilities;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CheckUserInfoActivity extends BaseActivity {

    EditText mEditBirthNum;
    TextView mTxtUserId;

    MainProgress mProgressDlg;

    private int mCount = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user_info);
        setActionBarTitle(getString(R.string.check_user_info));

        findViewById(R.id.btn_ok).setOnClickListener(this);

        mEditBirthNum = (EditText) findViewById(R.id.edit_birth_num);
        mTxtUserId = (TextView) findViewById(R.id.txt_user_id);
        mTxtUserId.setText(mPref.getString("username", ""));

        mProgressDlg = new MainProgress(this);
        mProgressDlg.setMessage("");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            mProgressDlg.show();
            String strImei = Utils.getIMEI();
            String strImsi = Utils.getIMSI();
            PicaApiUtility.checkBirthnum(getApplicationContext(), mEditBirthNum.getText().toString(), GlobalFunc.getDeviceNum(strImei + strImsi), new MyXMLResponseHandler() {
                @Override
                public void onMySuccess(JSONObject response) {
                    if (mProgressDlg != null && mProgressDlg.isShowing()) {
                        mProgressDlg.dismiss();
                    }

                    String strLoginkey = "";
                    try {
                        strLoginkey = response.getString("loginkey");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    File loginFile = new File(GlobalConstrants.LOGIN_KEY_PATH);
                    BufferedOutputStream bos;
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(loginFile));
                        bos.write(strLoginkey.getBytes());
                        bos.flush();
                        bos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }

                @Override
                public void onMyFailure(int errcode) {
                    if (mProgressDlg != null && mProgressDlg.isShowing()) {
                        mProgressDlg.dismiss();
                    }

                    mCount --;
                    if (mCount == 0) {
                        finish();
                        return;
                    }
                    switch (errcode) {
                        case 1:
                        case 2:
                            AndroidUtility.showErrorMessage(CheckUserInfoActivity.this, AndroidUtility.ERROR_CODE_API_KEY);
                            break;
                        case 3:
                            AndroidUtility.showErrorMessage(CheckUserInfoActivity.this, AndroidUtility.ERROR_CODE_FAILED_CHECK_USER_INFO);
                            break;
                        default:
                            GlobalFunc.showErrorMessageToast(CheckUserInfoActivity.this, 200, false);
                            break;
                    }
                }
            });
        }
        super.onClick(v);
    }
}
