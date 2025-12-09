package com.multimediachat.ui;

import android.os.Bundle;

import com.multimediachat.R;

public class ForgotPasswordActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setActionBarTitle(getString(R.string.password_recovery));
    }
}
