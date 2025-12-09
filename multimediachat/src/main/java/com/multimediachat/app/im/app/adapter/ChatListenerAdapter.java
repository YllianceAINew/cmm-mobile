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

package com.multimediachat.app.im.app.adapter;

import android.os.RemoteException;

import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.IChatListener;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.engine.Message;

public class ChatListenerAdapter extends IChatListener.Stub {

    private static final String TAG = ImApp.LOG_TAG;

    public void onContactJoined(IChatSession ses, Contact contact) {
        DebugConfig.debug(TAG, "onContactJoined(" + ses + ", " + contact + ")");
    }

    public void onContactLeft(IChatSession ses, Contact contact) {
    	DebugConfig.debug(TAG, "onContactLeft(" + ses + ", " + contact + ")");
    }

    public boolean onIncomingMessage(IChatSession ses, Message msg) {
    	
//    	String body = msg.getBody();
//		msg.setBody(body);
//        int pos = body.indexOf("Club had invited");
//        String content = body.substring(pos);
//        if (content.equals("Club had invited you for Student Event")) {
//        	String phone = body.substring(0, pos).trim();
//        	msg.setBody(phone + "_ChatListenAdapter_" + body);
//        }
//        msg.setBody("_ChatListenAdapter_" + body);
//        
    	DebugConfig.debug(TAG, "onIncomingMessage(" + ses + ", " + msg + ")");
        return true;
    }

    public void onIncomingData(IChatSession ses, byte[] data) {
    	DebugConfig.debug(TAG,"onIncomingMessage(" + ses + ", len=" + data.length + ")");
    }

    public void onSendMessageError(IChatSession ses, Message msg, ImErrorInfo error) {
    	DebugConfig.debug(TAG,"onSendMessageError(" + ses + ", " + msg + ", " + error + ")");
    }

    public void onInviteError(IChatSession ses, ImErrorInfo error) {
    	DebugConfig.debug(TAG, "onInviteError(" + ses + ", " + error + ")");
    }

    public void onConvertedToGroupChat(IChatSession ses) {
    	DebugConfig.debug(TAG, "onConvertedToGroupChat(" + ses + ")");
    }

    @Override
    public void onIncomingOper(IChatSession ses, String packetId, String operType, String operMessage, String operMsgId) throws RemoteException {
    	DebugConfig.debug(TAG, "onIncomingReceipt(" + ses + "," + packetId + ")");
    }

    @Override
    public void onStatusChanged(IChatSession ses) throws RemoteException {
    	DebugConfig.debug(TAG, "onStatusChanged(" + ses + ")");
    }

	@Override
	public void onMessagePostPoned(IChatSession ses, String packetId)
			throws RemoteException {
		DebugConfig.debug(TAG, "onMessagePostponed(" + ses + ")");
	}

	@Override
	public void onMessageOperPostPoned(IChatSession ses, String packetId)
			throws RemoteException {
		
	}

	@Override
	public void onSentMessage(IChatSession ses, String packetId)
			throws RemoteException {
	}
    
    
}
