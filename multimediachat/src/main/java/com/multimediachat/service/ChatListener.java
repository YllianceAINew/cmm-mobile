package com.multimediachat.service;

import com.multimediachat.app.im.engine.ChatSession;
import com.multimediachat.app.im.engine.ImErrorInfo;
import com.multimediachat.app.im.engine.Message;
import com.multimediachat.app.im.engine.MessageListener;

public class ChatListener implements MessageListener {
	private MessageListener mMessageListener;
    public ChatListener( MessageListener listener) {
        this.mMessageListener = listener;
    }

    @Override
    public boolean onIncomingMessage(ChatSession session, Message msg) {
        String body = msg.getBody();
        try {

            if (body != null) {
                msg.setBody(body);
//                int pos = body.indexOf("Club had invited");
//                String content = body.substring(pos);
//                if (content.equals("Club had invited you for Student Event")) {
//                	String phone = body.substring(0, pos).trim();
//                }
//                NotificationCenter.getInstance().postNotificationName(NotificationCenter.event_invited, 0);
                mMessageListener.onIncomingMessage(session, msg);
            }
        
        } catch (Exception e) {
        }
        return true;
    }
    

    @Override
    public void onSendMessageError(ChatSession session, Message msg, ImErrorInfo error) {
        mMessageListener.onSendMessageError(session, msg, error);
    }

    @Override
    public void onIncomingOper(ChatSession ses, String id, String operType, String operMessage, String operMsgId) {
        mMessageListener.onIncomingOper(ses, id, operType, operMessage, operMsgId);
    }

    @Override
    public void onMessagePostponed(ChatSession ses, String id) {
        mMessageListener.onMessagePostponed(ses, id);
    }
    
    @Override
    public void onReceiptsExpected(ChatSession ses) {
        mMessageListener.onReceiptsExpected(ses);
    }

	@Override
	public void onMessageOperPostponed(ChatSession ses, String id, String operType, String operMsgId, String operMessage) {
		mMessageListener.onMessageOperPostponed(ses, id, operType, operMsgId, operMessage);
	}

	@Override
	public void onSentMessage(ChatSession ses, String packetId) {
		mMessageListener.onSentMessage(ses, packetId);
	}
}
