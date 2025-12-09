package com.multimediachat.service;


interface IPlaybackService {
	   void uploadHandler(String fromUser, String toUser, int chatType,int msgId, String packetId, String filePath, String thumbnailPath, int type, long providerId, String contactName, int sendCount, int totalCount);
	   void deleteUploadHandler(String msgId);
	   void downloadHandler(String _chatid, String _msgid, String _filePath, String _type, String _to, String nickName, String thumbnailPath, int recvCount, int totalCount);
	   void deleteDownloadHandler(String _msgId);
	   void removeConnection();
}