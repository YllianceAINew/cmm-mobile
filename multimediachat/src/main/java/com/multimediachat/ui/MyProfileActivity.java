package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.multimediachat.app.ImApp;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.dialog.CustomDialog;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.BitmapUtil;
import com.multimediachat.util.ImageLoaderUtil;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("ResourceAsColor")
public class MyProfileActivity extends BaseActivity implements View.OnClickListener {
    public final static int REQUEST_CODE_PHOTO_CROP = 1008;

    CircularImageView mImgProfile;
    ImageView mQrCodeView;

    TextView mTxtName;
    TextView mTxtUserId;
    TextView mTxtPhoneNum;
    TextView mTxtBirthNum;
    TextView mTxtPwdFriend;

    private long mProviderId;

    private String photoPath;
    private String name;
    private String status;
    private String gender = "0";
    private String phone;
    private String region;
    private String userid;
    private String photohash;
    private String autoFriend;

    IImConnection conn = null;

    boolean isPhotoChanged = false;
    Bitmap croppedPhotoBmp;

    private MainProgress mProgressDlg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myprofile);
        setActionBarTitle(getString(R.string.my_profile));

        mProviderId = mPref.getLong(GlobalConstrants.store_picaProviderId, 0);

        conn = ImApp.getInstance().getConnection(mProviderId);

        mTxtName = findViewById(R.id.txt_name);
        mTxtUserId = findViewById(R.id.txt_user_id);
        mTxtPhoneNum = findViewById(R.id.txt_phone_number);
        mTxtBirthNum = findViewById(R.id.txt_birth_num);
        mImgProfile = findViewById(R.id.imgProfile);
        mTxtPwdFriend = findViewById(R.id.txt_password_friend);
        mQrCodeView = findViewById(R.id.img_qrcode);

        mImgProfile.setOnClickListener(this);
        mQrCodeView.setOnClickListener(this);

        findViewById(R.id.lyt_password).setOnClickListener(this);
        findViewById(R.id.lyt_password_friend).setOnClickListener(this);

        mProgressDlg = new MainProgress(this);
        mProgressDlg.setMessage("");

        loadProfile();

        String address = mPref.getString("username", "") + "@" + GlobalConstrants.server_domain;
        GlobalFunc.showQRCode(address, mQrCodeView);

    }

    public void loadProfile() {
        Map<String, String> profileInfo = DatabaseUtils.getUserInfo(cr);
        if (profileInfo == null) {
            finish();
            return;
        }

        name = profileInfo.get(Imps.Profile.NICKNAME);
        userid = profileInfo.get(Imps.Profile.USERID);
        region = profileInfo.get(Imps.Profile.REGION);
        photohash = profileInfo.get(Imps.Profile.HASH);
        gender = profileInfo.get(Imps.Profile.GENDER);
        phone = profileInfo.get(Imps.Profile.PHONE);

        mTxtName.setText(name);
        mTxtUserId.setText(String.format("%s: %s", getString(R.string.user_id), userid));
        mTxtPhoneNum.setText(phone);
        mTxtBirthNum.setText(mPref.getString("birthnum", ""));
        // mTxtPwdFriend.setText(mPref.getString("password_friend_username", ""));

        ImageLoaderUtil.loadMyAvatarImage(this, mImgProfile);
    }

    @Override
    public void onBackPressed() {
        if (isPhotoChanged)
        {
            final CustomDialog dlg = new CustomDialog(this, getString(R.string.app_name), getString(R.string.save_this_change), getString(R.string.save), getString(R.string.don_t_save));

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
                    saveProfile();
                }
            });

            dlg.show();

            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PHOTO_CROP) {
            if (resultCode == RESULT_OK) {
                if (data == null)
                    return;

                mImgProfile.setImageDrawable(null);

                if (croppedPhotoBmp != null && !croppedPhotoBmp.isRecycled()) {
                    croppedPhotoBmp.recycle();
                    croppedPhotoBmp = null;
                }

                String filePath = data.getStringExtra("croppedPhotoPath");
                if (filePath == null) {
                    mImgProfile.setImageResource(R.drawable.profilephoto);

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
                    mImgProfile.setImageBitmap(croppedPhotoBmp);
                    isPhotoChanged = true;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        Intent intent;
        switch (v.getId()) {
            case R.id.imgProfile:
                try {
                    intent = new Intent(this, PhotoCropActivity.class);
                    intent.putExtra("photoPath", Uri.fromFile(new File(GlobalConstrants.USER_PHOTO_PATH)).toString());
                    startActivityForResult(intent, REQUEST_CODE_PHOTO_CROP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.img_qrcode:
        		intent = new Intent(this, MyQRCodeActivity.class);
                startActivity(intent);
                break;
            case R.id.lyt_password:
                intent = new Intent(this, EnterCurrentPasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.lyt_password_friend:
                intent = new Intent(this, ChoosePwdFriendActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImgProfile != null) {
            mImgProfile.setImageDrawable(null);
            mImgProfile.destroyDrawingCache();
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

    private void saveProfile(){
        if (isPhotoChanged) {
            doUploadProfilePhoto();
        } else {
            finish();
        }
    }

    private boolean doUploadProfilePhoto() {
        if (!MyProfileActivity.this.isFinishing())
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
                            onPhotoUploaded(0);
                        }

                        @Override
                        public void onMyFailure(int errcode) {
                            onPhotoUploaded(errcode);
                        }
                    });
                } else {
                    f.createNewFile();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    photoBmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    photohash = BitmapUtil.hashBitmap(bitmapdata);
                    // write the bytes in file
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();

                    PicaApiUtility.uploadProfilePhoto(this, userid,
                            f.getAbsolutePath(), new MyXMLResponseHandler() {
                                @Override
                                public void onMySuccess(JSONObject response) {
                                    onPhotoUploaded(0);
                                }

                                @Override
                                public void onMyFailure(int errcode) {
                                    if (f.exists()) {
                                        f.delete();
                                    }
                                    onPhotoUploaded(errcode);
                                }
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (photoneedrecyle) {
                    if (photoBmp != null && !photoBmp.isRecycled()) {
                        photoBmp.recycle();
                    }
                }
            }
        }
        else
        {
            onPhotoUploaded(0);
        }

        return false;
    }

    boolean sendUpdatePresence() {
        long providerId = mPref.getLong(GlobalConstrants.store_picaProviderId, -1);
        if (providerId == -1)
            return false;
        IImConnection conn = mApp.getConnection(providerId);
        if (conn == null)
            return false;
        Map<String, String> inParams = new HashMap<String, String>();
        if (isPhotoChanged)
            inParams.put("hash", photohash);
        boolean ret = false;
        try {
            ret = conn.getFindFriendManager().sendVCardUpdatePresence(GlobalFunc.mapToStringArray(inParams));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void onPhotoUploaded(int photoUploadres)
    {
        if (isPhotoChanged) {
            if (photoUploadres == 0) {
                doSaveProfileInfo();
                sendUpdatePresence();
                onUpdateProfileSuccess();
            } else {
                GlobalFunc.showErrorMessageToast(this, 100, false);
                if (mProgressDlg != null && mProgressDlg.isShowing()) {
                    mProgressDlg.dismiss();
                }
            }
        } else
            onUpdateProfileSuccess();
    }

    void doSaveProfileInfo() {
        if (isPhotoChanged) // profile image changed.
        {
            try {
                File fileSrc = new File(GlobalConstrants.AVATAR_DIR_PATH + "myprofile_photo_tmp");
                File fileDest = new File(GlobalConstrants.USER_PHOTO_PATH);
                if (fileDest.exists())
                    fileDest.delete();

                if (fileSrc.exists())
                    fileSrc.renameTo(fileDest);

            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, String> userInfo = new HashMap<String, String>();
            userInfo.put(Imps.Profile.NICKNAME, name);
            userInfo.put(Imps.Profile.PHONE, phone);
            userInfo.put(Imps.Profile.REGION, region);
            userInfo.put(Imps.Profile.USERID, userid);
            userInfo.put(Imps.Profile.HASH, photohash);
            userInfo.put(Imps.Profile.GENDER, gender);
            DatabaseUtils.insertOrUpdateUserInfo(cr, userInfo);
        }
    }

    private void onUpdateProfileSuccess()
    {
        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            mProgressDlg.dismiss();
        }

        if (isPhotoChanged) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.user_photo_changed, 0);
        }

        finish();
    }
}