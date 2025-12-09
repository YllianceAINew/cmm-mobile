package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.multimediachat.R;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.ImageLoaderUtil;
import com.multimediachat.ui.views.CircularImageView;

import java.io.File;

@SuppressLint("NewApi")
public class FriendProfileImageActivity extends BaseActivity implements OnClickListener {
    CircularImageView mImgProfile;
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile_image);
        setActionBarTitle(getString(R.string.profile));

        mImgProfile = (CircularImageView) findViewById(R.id.imgProfile);
        String address = getIntent().getStringExtra("address");
        GlobalFunc.showAvatar(this, address, mImgProfile);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            if (view instanceof AdapterView) {

            } else {
                ((ViewGroup) view).removeAllViews();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindDrawables(findViewById(R.id.rootView));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
