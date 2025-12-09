package com.multimediachat.app.im;

import android.content.Context;

public interface ImService {
    void showToast(CharSequence text, int duration);
    Context getApplicationContext();
}
