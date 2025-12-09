/*
 * Copyright (C) 2007 Esmertec AG. Copyright (C) 2007 The Android Open Source
 * Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.multimediachat.app.im.app.adapter;

import android.os.Handler;

import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.IConnectionListener;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.engine.ImErrorInfo;

public class ConnectionListenerAdapter extends IConnectionListener.Stub {

    private static final String TAG = ImApp.LOG_TAG;
    private Handler mHandler;

    public ConnectionListenerAdapter(Handler handler) {
        mHandler = handler;
    }

    public void onConnectionStateChange(IImConnection connection, int state, ImErrorInfo error) {
    	DebugConfig.debug(TAG,"onConnectionStateChange(" + state + ", " + error + ")");
    }

    public void onUpdateSelfPresenceError(IImConnection connection, ImErrorInfo error) {
    	DebugConfig.debug(TAG,"onUpdateSelfPresenceError(" + error + ")");
    }

    public void onSelfPresenceUpdated(IImConnection connection) {
    	DebugConfig.debug(TAG,"onSelfPresenceUpdated()");
    }

    final public void onStateChanged(final IImConnection conn, final int state,
            final ImErrorInfo error) {
        mHandler.post(new Runnable() {
            public void run() {
                onConnectionStateChange(conn, state, error);
            }
        });
    }

    final public void onUpdatePresenceError(final IImConnection conn, final ImErrorInfo error) {
        mHandler.post(new Runnable() {
            public void run() {
                onUpdateSelfPresenceError(conn, error);
            }
        });
    }

    final public void onUserPresenceUpdated(final IImConnection conn) {
        mHandler.post(new Runnable() {
            public void run() {
                onSelfPresenceUpdated(conn);
            }
        });
    }
}
