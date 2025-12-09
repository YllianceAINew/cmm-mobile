package com.multimediachat.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.webkit.WebView;
import android.widget.Toast;

import com.multimediachat.app.DebugConfig;

public class CustomWebView extends WebView {
    public CustomWebView(Context context) {
        super(context);
    }
    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        try{
            super.onWindowFocusChanged(hasWindowFocus);
        }catch(Exception e){
        	DebugConfig.error("CustomWebView", "onWindowFocusChanged", e);
        }
    }
}
