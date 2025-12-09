package com.multimediachat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.multimediachat.app.ImApp;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.global.GlobalConstrants;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.core.LinphoneCore;

public class SettingIPTestActivity extends BaseActivity{
    private LinphonePreferences mPrefs;
    TextView txt_stun;
    EditText edit_api_server;
    EditText edit_voip_server;

    CheckBox btn_tlssecurity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI()
    {
        setContentView(R.layout.set_ip_test);
        setActionBarTitle(getString(R.string.set_server));
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        mPrefs = LinphonePreferences.instance();
        txt_stun = (TextView) findViewById(R.id.txt_stun);
        if (mPrefs.getStunServer() != null && !mPrefs.getStunServer().isEmpty())
            txt_stun.setText(mPrefs.getStunServer());
        else
            txt_stun.setText(GlobalConstrants.stun_server_domain);
        btn_tlssecurity = (CheckBox)findViewById(R.id.btn_tlssecurity);
        btn_tlssecurity.setChecked(DebugConfig.SECURITY);
        edit_api_server = (EditText)findViewById(R.id.edit_api_server);
        edit_voip_server = (EditText)findViewById(R.id.edit_voip_server);
        edit_api_server.setText(mPref.getString(GlobalConstrants.API_SERVER, GlobalConstrants.server_domain));
        edit_voip_server.setText(mPref.getString(GlobalConstrants.VOIP_SERVER, GlobalConstrants.voip_server_domain));

        findViewById(R.id.btn_set_server).setOnClickListener(this);
    }

    private void setApiServer(String apiServer, String voipServer)
    {
        GlobalConstrants.server_domain = apiServer;
        GlobalConstrants.voip_server_domain = voipServer;

        mPref.putString(GlobalConstrants.API_SERVER, apiServer);
        mPref.putString(GlobalConstrants.VOIP_SERVER, voipServer);
        mPref.putInt(GlobalConstrants.TLS_SECURITY, btn_tlssecurity.isChecked()?1:0);

        DebugConfig.SECURITY = btn_tlssecurity.isChecked();

        if (btn_tlssecurity.isChecked())
            mPref.putString(GlobalConstrants.API_HEADER, "https://");
        else
            mPref.putString(GlobalConstrants.API_HEADER, GlobalConstrants.api_header);

        ImApp.getInstance().initTestConfig();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_set_server:
                setApiServer(edit_api_server.getText().toString(), edit_voip_server.getText().toString());

                GlobalConstrants.stun_server_domain = txt_stun.getText().toString();
                mPrefs.setStunServer(txt_stun.getText().toString());

                Toast.makeText(SettingIPTestActivity.this, getString(R.string.set_successfully), Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

}
