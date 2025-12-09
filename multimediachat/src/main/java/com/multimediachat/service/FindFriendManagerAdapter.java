/*
 * Copyright (C) 2007-2008 Esmertec AG. Copyright (C) 2007-2008 The Android Open
 * Source Project
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

package com.multimediachat.service;

import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;

import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.FindFriendManager;

public class FindFriendManagerAdapter extends
        com.multimediachat.app.im.IFindFriendManager.Stub{

    ImConnectionAdapter mConn;
    ContentResolver mResolver;

    private FindFriendManager mAdaptee;

    HashMap<String, Contact> mTemporaryContacts;

    MessengerService mContext;

    public FindFriendManagerAdapter(ImConnectionAdapter conn) {
        mAdaptee = conn.getAdaptee().getFindFriendManager();
        mConn = conn;
        mContext = conn.getContext();
        mResolver = mContext.getContentResolver();
    }
    
    public List<Contact> getFindedFriends(String name, String arg0, String latitude, String longtitude)
    {
    	return mAdaptee.getFindFriends(name, arg0, latitude, longtitude);
    }
    
    
    public String sendDataToServer(String param[]) {
    	return mAdaptee.sendDataInManager(param);
    }
    public String[] setQueryForResult(String param[]) {
    	return mAdaptee.setQueryForResultInManager(param);
    }
    public String[] getQueryResult(String param[]) {
    	return mAdaptee.getQueryResultInManager(param);
    }
    public void setQuery(String param[]) {
    	mAdaptee.setQueryInManager(param);
    }
    public boolean sendVCardUpdatePresence(String param[]) {
    	return mAdaptee.sendVCardUpdatePresence(param);
    }
}
