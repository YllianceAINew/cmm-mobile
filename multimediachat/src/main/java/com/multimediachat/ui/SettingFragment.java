package com.multimediachat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.multimediachat.R;
import com.multimediachat.global.GlobalFunc;

public class SettingFragment extends Fragment implements View.OnClickListener {

    View mFragmentView = null;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.fragment_setting, container, false);

        mFragmentView.findViewById(R.id.lyt_my_profile).setOnClickListener(this);
        mFragmentView.findViewById(R.id.lyt_app_info).setOnClickListener(this);
        mFragmentView.findViewById(R.id.lyt_help).setOnClickListener(this);
        mFragmentView.findViewById(R.id.lyt_logout).setOnClickListener(this);
        mFragmentView.findViewById(R.id.btn_bps_test).setOnClickListener(this);

        return mFragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lyt_my_profile:
                startActivity(new Intent(getContext(), MyProfileActivity.class));
                break;
            case R.id.lyt_app_info:
                startActivity(new Intent(getContext(), AppInfoActivity.class));
                break;
            case R.id.lyt_help:
                startActivity(new Intent(getContext(), WebActivity.class));
                break;
            case R.id.lyt_logout:
                startActivity(new Intent(getContext(), LogOutActivity.class));
                break;
        }
    }
}
