package com.multimediachat.ui;

import android.os.Bundle;

import com.multimediachat.R;

public class AppInfoActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        setActionBarTitle(getString(R.string.app_info));
    }
}
