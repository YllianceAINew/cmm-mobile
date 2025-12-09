package com.multimediachat.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.Utils;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.BitmapUtil;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class CreateProfileActivity extends BaseActivity implements View.OnClickListener {
    private final int REQUEST_CODE_PHOTO_CROP = 1001;

    CircularImageView imgProfile;

    private MainProgress mProgressDlg;

    private Handler mHandler;

    boolean isPhotoChanged = false;
    String m_sProfileImageTempPath = "";

    Bitmap croppedPhotoBmp;

    LinearLayout lytPhoneNum = null;
    LinearLayout lytInputPhoneNum = null;
    LinearLayout lytInputPassword = null;
    LinearLayout lytInputUserInfo = null;

    TextView mTxtPhoneNum = null;

    EditText mEditPhoneNum = null;
    EditText mEditPwd = null;
    EditText mEditCPwd = null;
    EditText mEditName = null;
    EditText mEditUserId = null;
    EditText mEditBirthNum = null;

    String mStrImei = null;
    String mStrImsi = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalFunc.makeLocalDir();

        mApp.resetProviderSettings(); // clear cached provider list

        mPref.putInt(GlobalConstrants.store_step, GlobalConstrants.STEP_NEED_LOGIN);

        mHandler = new Handler();

        initUI();
    }

    private void initUI() {
        setContentView(R.layout.create_profile_activity);

        String[] strCountries = getResources().getStringArray(R.array.countries);
        GlobalVariable.Region = strCountries[0];

        imgProfile = (CircularImageView) findViewById(R.id.imgProfile);
        imgProfile.setBorderColor(r.getColor(R.color.transparent));
        imgProfile.setOnClickListener(this);

        mProgressDlg = new MainProgress(this);
        mProgressDlg.setMessage("");

        GlobalVariable.PhoneNumber = Utils.getPhoneNumber();

        /*   Author: Im Chung Il (2022.02.)   */

        findViewById(R.id.btn_sign_up_start).setOnClickListener(this);
        findViewById(R.id.btn_password_next).setOnClickListener(this);
        findViewById(R.id.btn_sign_up).setOnClickListener(this);

        lytPhoneNum = findViewById(R.id.lyt_phone_number);
        lytInputPhoneNum = findViewById(R.id.lyt_input_phone_number);
        lytInputPassword = findViewById(R.id.lyt_input_password);
        lytInputUserInfo = findViewById(R.id.lyt_input_user_info);

        setActionBarTitle(getString(R.string.sign_up_title_phone_number));

        lytPhoneNum.setVisibility(View.GONE);
        lytInputPhoneNum.setVisibility(View.VISIBLE);
        lytInputPassword.setVisibility(View.GONE);
        lytInputUserInfo.setVisibility(View.GONE);

        mTxtPhoneNum = findViewById(R.id.txt_phone_number);

        mEditPhoneNum = findViewById(R.id.input_phone_number);
        mEditPhoneNum.setText(Utils.getPhoneNumber());

        mEditPwd = findViewById(R.id.input_password);
        mEditCPwd = findViewById(R.id.input_password_confirm);
        mEditName = findViewById(R.id.input_username);
        mEditUserId = findViewById(R.id.input_user_id);
        mEditBirthNum = findViewById(R.id.input_birth_number);

        mStrImei = Utils.getIMEI();
        mStrImsi = Utils.getIMSI();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgProfile:
                try {
                    try {
                        Intent intent = new Intent(this, PhotoCropActivity.class);
                        intent.putExtra("photoPath", m_sProfileImageTempPath);
                        startActivityForResult(intent, REQUEST_CODE_PHOTO_CROP);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_sign_up_start:
                onSignUpStart();
                break;
            case R.id.btn_password_next:
                onPasswordNext();
                break;
            case R.id.btn_sign_up:
                onSignUp();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void onSignUpStart() {

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showErrorMessageToast(CreateProfileActivity.this, 200, false);
            return;
        }

        try {
            final String strPhoneNum = mEditPhoneNum.getText().toString();
            if (strPhoneNum.isEmpty() || (!strPhoneNum.startsWith("191") && !strPhoneNum.startsWith("195")) || strPhoneNum.length() != 10) {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_WRONG_PHONENUMBER);
                return;
            }

            mProgressDlg.show();

            PicaApiUtility.checkPhoneNumber(this, strPhoneNum, mStrImei, new MyXMLResponseHandler() {
                @Override
                public void onMySuccess(JSONObject response) {

                    mProgressDlg.dismiss();

                    try {
                        ImApp.getInstance().api_key = response.getString("apikey");
                        mPref.putString("apikey", ImApp.getInstance().api_key);

                        mTxtPhoneNum.setText(strPhoneNum);
                        GlobalVariable.PhoneNumber = strPhoneNum;

                        lytInputPhoneNum.setVisibility(View.GONE);
                        lytInputPassword.setVisibility(View.VISIBLE);
                        lytPhoneNum.setVisibility(View.VISIBLE);
                        setActionBarTitle(getString(R.string.sign_up_title_password));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMyFailure(int errcode) {
                    mProgressDlg.dismiss();
                    try {
                        if (errcode == -1)
                            GlobalFunc.showErrorMessageToast(CreateProfileActivity.this, 200, false);
                        else
                            AndroidUtility.showErrorMessage(CreateProfileActivity.this, AndroidUtility.ERROR_WRONG_PHONENUMBER);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onPasswordNext() {

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showErrorMessageToast(CreateProfileActivity.this, 200, false);
            return;
        }

        try {
            String strPwd = mEditPwd.getText().toString();
            String strCPwd = mEditCPwd.getText().toString();

            if (strPwd.length() < 6 || strPwd.length() > 20) {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_PASSWORD_LENGTH_NOT_ENOUGH);
                return;
            } else if (!strPwd.equals(strCPwd)) {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_PASSWORD_NOT_MATCH);
                return;
            }

            lytInputPassword.setVisibility(View.GONE);
            lytInputUserInfo.setVisibility(View.VISIBLE);
            setActionBarTitle(getString(R.string.sign_up_title_user_info));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSignUp() {
        try {
            if (mStrImsi.isEmpty() || mStrImei.isEmpty()) {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_NO_SIMCARD);
                return;
            }

            String strUserId = mEditUserId.getText().toString().trim();
            if (!Utils.isValidChatID(strUserId)) {
                    AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_USERID_FORMAT);
                return;
            }

            String strUserName = mEditName.getText().toString().trim();
            if (strUserName.isEmpty()) {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_EMPTY_USERNAME);
                return;
            }

            String strBirthNum = mEditBirthNum.getText().toString().trim();
            if (strBirthNum.isEmpty()) {
                AndroidUtility.showErrorMessage(this, AndroidUtility.ERROR_CODE_EMPTY_BIRTHNUM);
                return;
            }

            doUploadProfileInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PHOTO_CROP) {
            if (resultCode == RESULT_OK) {
                imgProfile.setImageDrawable(null);

                if (croppedPhotoBmp != null && !croppedPhotoBmp.isRecycled()) {
                    croppedPhotoBmp.recycle();
                    croppedPhotoBmp = null;
                }

                String filePath = data.getStringExtra("croppedPhotoPath");
                if (filePath == null) {
                    imgProfile.setImageResource(R.drawable.profilephoto);

                    m_sProfileImageTempPath = "";
                    croppedPhotoBmp = null;
                    isPhotoChanged = true;
                    return;
                }

                croppedPhotoBmp = BitmapUtil.getBitmapFromFile(filePath);

                final File fcropped = new File(filePath);
                if (fcropped.exists()) {
                    fcropped.delete();
                }

                if (croppedPhotoBmp != null) {
                    m_sProfileImageTempPath = filePath;
                    imgProfile.setImageBitmap(croppedPhotoBmp);
                    isPhotoChanged = true;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void onProfileUploaded() {
        String strPwd = mEditPwd.getText().toString();
        String strUserName = mEditName.getText().toString().trim();
        String strUserId = mEditUserId.getText().toString().trim();
        String strBirthNum = mEditBirthNum.getText().toString();

        PicaApiUtility.createNewAccount(this, strUserName, strUserId, strPwd, strBirthNum, GlobalFunc.getDeviceNum(mStrImei + mStrImsi),
                mStrImei, mStrImsi, GlobalVariable.PhoneNumber, new MyXMLResponseHandler() {
                    @Override
                    public void onMySuccess(JSONObject response) {
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

                        doSaveProfileInfo();

                        if (mProgressDlg != null && mProgressDlg.isShowing()) {
                            mProgressDlg.dismiss();
                        }

                        Toast.makeText(mApp, R.string.signup_success, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        if (error.getMessage().endsWith("refused")) {
                            if (mProgressDlg != null && mProgressDlg.isShowing()) {
                                mProgressDlg.dismiss();
                            }
                            AndroidUtility.showErrorMessage(CreateProfileActivity.this, AndroidUtility.ERROR_CONNECTION);
                        } else if (error.getMessage().endsWith("timed out")) {
                            if (mProgressDlg != null && mProgressDlg.isShowing()) {
                                mProgressDlg.dismiss();
                            }
                            AndroidUtility.showErrorMessage(CreateProfileActivity.this, AndroidUtility.ERROR_CONNECTION);
                        } else {
                            super.onFailure(error);
                        }
                    }

                    @Override
                    public void onMyFailure(int errcode) {
                        if (mProgressDlg != null && mProgressDlg.isShowing()) {
                            mProgressDlg.dismiss();
                        }
                        try {
                            if (errcode == 101) {
                                AndroidUtility.showErrorMessage(CreateProfileActivity.this, AndroidUtility.ERROR_CODE_DUPLICATE_USERID);
                            } else {
                                GlobalFunc.showErrorMessageToast(CreateProfileActivity.this, errcode, false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    void doSaveProfileInfo() {
        // If upload profile suceess, save data
        if (isPhotoChanged) // profile image changed.
        {
            try {
                File fileSrc = new File(GlobalConstrants.AVATAR_DIR_PATH + "myprofile_photo_tmp");
                File fileDest = new File(GlobalConstrants.USER_PHOTO_PATH);
                if (fileDest.exists())
                    fileDest.delete();

                fileSrc.renameTo(fileDest);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doUploadProfileInfo() {

        if (!ImApp.getInstance().isNetworkAvailableAndConnected()) {
            GlobalFunc.showErrorMessageToast(CreateProfileActivity.this, 200, false);
            return;
        }

        if (!CreateProfileActivity.this.isFinishing())
            mProgressDlg.show();

        Bitmap photoBmp = null;
        boolean photoneedrecyle = false;
        if (isPhotoChanged) {
            if ( croppedPhotoBmp != null && ( croppedPhotoBmp.getWidth() > GlobalConstrants.IMAGE_SIZE || croppedPhotoBmp.getHeight() > GlobalConstrants.IMAGE_SIZE ) )
                photoBmp = BitmapUtil.resizeBitmap(croppedPhotoBmp, GlobalConstrants.IMAGE_SIZE,
                        GlobalConstrants.IMAGE_SIZE);
            else
                photoBmp = croppedPhotoBmp;
            if (photoBmp != croppedPhotoBmp) {
                photoneedrecyle = true;
            }

            final File f = new File(GlobalConstrants.AVATAR_DIR_PATH + "myprofile_photo_tmp");

            try {
                if (photoBmp == null) {
                    if (f.exists()) {
                        f.delete();
                    }

                    PicaApiUtility.removeProfilePhoto(this, new MyXMLResponseHandler() {
                        @Override
                        public void onMySuccess(JSONObject response) {
                            onProfileUploaded();
                        }

                        @Override
                        public void onMyFailure(int errcode) {
                            onProfileUploaded();
                        }
                    });

                } else {
                    f.createNewFile();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    photoBmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    byte[] bitmapdata = bos.toByteArray();

                    // write the bytes in file
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                    fos = null;

                    String strUserId = mEditUserId.getText().toString().trim();

                    PicaApiUtility.uploadProfilePhoto(this, strUserId, f.getAbsolutePath(), new MyXMLResponseHandler() {
                        @Override
                        public void onMySuccess(JSONObject response) {
                            onProfileUploaded();
                        }

                        @Override
                        public void onMyFailure(int errcode) {
                            if (f.exists()) {
                                f.delete();
                            }

                            onProfileUploaded();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (photoneedrecyle) {
                    if (photoBmp != null && !photoBmp.isRecycled()) {
                        photoBmp.recycle();
                        photoBmp = null;
                    }
                }
            }
        }
        else
        {
            PicaApiUtility.removeProfilePhoto(this, new MyXMLResponseHandler() {
                @Override
                public void onMySuccess(JSONObject response) {
                    onProfileUploaded();
                }

                @Override
                public void onMyFailure(int errcode) {
                    onProfileUploaded();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (croppedPhotoBmp != null && !croppedPhotoBmp.isRecycled()) {
            croppedPhotoBmp.recycle();
            croppedPhotoBmp = null;
        }

        if (imgProfile != null) {
            imgProfile.setImageDrawable(null);
            imgProfile.destroyDrawingCache();
        }
        try {
            unbindDrawables(findViewById(R.id.rootView));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

}