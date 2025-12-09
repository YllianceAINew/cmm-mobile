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

package com.multimediachat.app.im.engine;

import java.util.List;
import java.util.Map;

import com.multimediachat.app.im.plugin.xmpp.PicaSearchResult;

public abstract class FindFriendManager {
    protected FindFriendManager() {
    }
    
    protected abstract List<Contact> findFriends(String aKey, String arg0, String latitude, String longtitude);
    protected abstract String sendData(String param[]);
    protected abstract String[] setQueryForResult(String params[]);
    protected abstract String[] getQueryResult(String params[]);
    protected abstract void setQuery(String params[]);
    protected abstract PicaSearchResult getSearchResult(final Map<String,String> inParams);
    
    public String sendDataInManager(String param[]) {
    	return sendData(param);
    }
    public String[] setQueryForResultInManager(String params[]) {
    	return setQueryForResult(params);
    }
    public String[] getQueryResultInManager(String params[]) {
    	return getQueryResult(params);
    }
    public void setQueryInManager(String params[]) {
    	setQuery(params);
    }
    
    public List<Contact> getFindFriends(String aKey, String arg0, String latitude, String longtitude) {
        return findFriends(aKey, arg0, latitude, longtitude);
    }
    public boolean sendVCardUpdatePresence(String params[])    {
    	return sendVCardUpdatePresence(params);
    }
}
