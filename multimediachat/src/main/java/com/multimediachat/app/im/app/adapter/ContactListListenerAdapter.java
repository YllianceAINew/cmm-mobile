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

import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.SimpleAlertHandler;
import com.multimediachat.app.im.IContactList;
import com.multimediachat.app.im.IContactListListener;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ImErrorInfo;

public class ContactListListenerAdapter extends IContactListListener.Stub {

    private static final String TAG = ImApp.LOG_TAG;

    //private final SimpleAlertHandler mHandler;

    public ContactListListenerAdapter(SimpleAlertHandler handler) {
        //mHandler = handler;
    }

    public void onContactChange(int type, IContactList list, Contact contact) {
    	DebugConfig.debug(TAG,"onContactListChanged(" + type + ", " + list + ", " + contact + ")");
    }

    public void onAllContactListsLoaded() {
    	DebugConfig.debug(TAG,"onAllContactListsLoaded");
    }

    public void onContactsPresenceUpdate(Contact[] contacts) {
    	DebugConfig.debug(TAG, "onContactsPresenceUpdate(" + contacts.length + ")");
    }

    public void onContactError(int errorType, ImErrorInfo error, String listName, Contact contact) {
    	DebugConfig.debug(TAG,"onContactError(" + errorType + ", " + error + ", " + listName + ", "
                       + contact + ")");
    }
}
