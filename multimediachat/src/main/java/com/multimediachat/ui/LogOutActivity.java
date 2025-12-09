package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.multimediachat.R;

import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.views.CircularImageView;
import com.multimediachat.util.ImageLoaderUtil;

import java.util.Map;

public class LogOutActivity extends BaseActivity implements OnClickListener{

    // ui variable
    CircularImageView				    mImgAvatar;
    TextView						    btnLogout;
    TextView                            mTxtName;
    TextView                            mTxtUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_out);
        setActionBarTitle(getString(R.string.setting_title_log_out));

        mImgAvatar = (CircularImageView) findViewById(R.id.imgProfile);
        btnLogout = (TextView) findViewById(R.id.btnLogOut);
        btnLogout.setOnClickListener(this);

        mTxtName = (TextView) findViewById(R.id.txtName);
        mTxtUserId = (TextView) findViewById(R.id.txtId);

        loadProfile();

    }

    @SuppressLint("SetTextI18n")
    public void loadProfile() {
        Map<String, String> profileInfo = DatabaseUtils.getUserInfo(cr);
        if (profileInfo == null) {
            finish();
            return;
        }

        mTxtName.setText(profileInfo.get(Imps.Profile.NICKNAME));
        mTxtUserId.setText(getString(R.string.user_id) + ": " + profileInfo.get(Imps.Profile.USERID));

        ImageLoaderUtil.loadMyAvatarImage(this, mImgAvatar);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnLogOut:
                GlobalFunc.doLogOut(this);
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    private void unbindDrawables(View view) {
        if (view == null)
            return;
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            unbindDrawables(findViewById(R.id.rootView));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

}