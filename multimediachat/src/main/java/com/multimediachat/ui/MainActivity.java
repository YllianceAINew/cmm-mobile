package com.multimediachat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.multimediachat.app.ImApp;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.ImPluginHelper;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.linphone.LinphoneService;


import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.SSLSocketFactory;

import static android.content.Intent.ACTION_MAIN;

public class MainActivity extends Activity {
    private int stepNo;

    MainProgress progressDlg;

    //Linphone
    private Handler mHandler;
    private XmppLoginThread mXmppLoginThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initUI();
            }
        }, 1000);
    }

    private void initUI() {
        stepNo = mPref.getInt(GlobalConstrants.store_step, GlobalConstrants.STEP_NEED_LOGIN);
        progressDlg = new MainProgress(this);

        if (!LinphoneService.isReady()) {
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
        }

        ImPluginHelper.getInstance(this).loadAvailablePlugins();

        loadInfo();
    }

    private class XmppLoginThread extends Thread {
        public void run() {
            while (!ImApp.getInstance().isServerLogined()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            mXmppLoginThread = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindDrawables(findViewById(R.id.layoutMain));
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

    private void loadInfo() {
        try {
            TrustManager[] victimizedManager = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                        return myTrustedAnchors;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, victimizedManager, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (stepNo == GlobalConstrants.STEP_NEED_LOGIN) {
            showTutorialActivity();
        } else {
            mXmppLoginThread = new XmppLoginThread();
            mXmppLoginThread.start();

            GlobalFunc.doSipLogin();

            Intent intent = new Intent(this, MainTabNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void showTutorialActivity() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
        finish();
    }

}
