package com.multimediachat.ui;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.multimediachat.R;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.ui.views.CustomWebView;

public class WebViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    View mFragmentView = null;
    Context context = null;
    CustomWebView mWeb = null;

    String mStrFileName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.fragment_web_view, container, false);

        mWeb = mFragmentView.findViewById(R.id.webView);
        WebSettings set = mWeb.getSettings();
        set.setLoadsImagesAutomatically(true);
        set.setAllowFileAccess(true);
        set.setJavaScriptCanOpenWindowsAutomatically(true);
        set.setSupportMultipleWindows(true);
        mWeb.setVerticalScrollBarEnabled(false);
        mWeb.setHorizontalScrollBarEnabled(false);
        mWeb.setVerticalScrollbarOverlay(false);
        mWeb.setHorizontalScrollbarOverlay(false);

        mWeb.loadUrl(GlobalVariable.HELP_URL + mStrFileName);

        return mFragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            if ( mWeb != null ) {
                mWeb.stopLoading();
                mWeb.clearAnimation();
                mWeb.clearFormData();
                mWeb.clearDisappearingChildren();
                mWeb.clearView();
                mWeb.clearHistory();
                mWeb.destroyDrawingCache();
                mWeb.freeMemory();
                mWeb.destroy();
                mWeb = null;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStrFileName = getArguments().getString("filename");
        }
    }
}
