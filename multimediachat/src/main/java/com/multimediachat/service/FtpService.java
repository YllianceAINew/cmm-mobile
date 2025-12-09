package com.multimediachat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.loopj.android.http.AsyncHttpClient;
import com.multimediachat.app.ImApp;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.util.connection.PicaApiUtility;
import com.loopj.android.http.RequestParams;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FtpService extends Service{
	public String TAG = "FtpService";
	public Map<String, AsyncHttpClient> mapFileUploaders = new HashMap<String, AsyncHttpClient>();
	public Map<String, AsyncHttpClient> mapFileDownloaders = new HashMap<String, AsyncHttpClient>();
	public Map<String, AsyncHttpClient> mapThumbnailDownloaders = new HashMap<String, AsyncHttpClient>();
	
	final int DEFAULT_TIMEOUT = 10*60*1000;
	
	private final IPlaybackService.Stub myFtpServiceStub = new IPlaybackService.Stub() {
		
		@Override
		public void uploadHandler(String fromUser, String toUser, int chatType,int msgId,  String packetId, String filePath, String thumbnailPath, int type, long providerId, String contactName, int sendCount, int totalCount)  throws RemoteException{
			fileUploadThread(fromUser, toUser, chatType, msgId, packetId, filePath, thumbnailPath, type, providerId, contactName, sendCount, totalCount);
		}
		
		@Override
		public void deleteUploadHandler(String msgId)  throws RemoteException{
			deleteThread(msgId);
		}
		
		@Override
		public void downloadHandler(String chatId, String _msgid, String _filePath, String _type,String to, String nickName, String thumbnailPath, int recvCount, int totalCount)  throws RemoteException{
			fileDownloadThread(chatId, _msgid, _filePath, _type,to, nickName, thumbnailPath, recvCount, totalCount);
		}
		
		@Override
		public void deleteDownloadHandler(String msgId)  throws RemoteException{
			deleteDownloadThread(msgId);
		}

		@Override
		public void removeConnection() throws RemoteException {
			FtpService.this.removeConnection();
		}

	};

    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	return START_STICKY;
    }
    
    private void removeConnection(){
    	Collection<IImConnection> activeConns =  ImApp.getInstance().getActiveConnections();
    	if ( activeConns != null )
    		activeConns.clear();
    }
    
    public void fileUploadThread(final String fromUser, final String toUser, final int chatType, final int msgId, final String packetId, final String filePath,
								 final String thumbnailPath,  final int type, final long providerId, final String contactName, final int sendCount, final int totalCount){
		UploadHttpHandler uploadhttphandler = new UploadHttpHandler(FtpService.this, msgId, packetId, providerId, contactName, filePath, sendCount, totalCount);

		uploadhttphandler.setUsePoolThread(true);

		try
		{
			AsyncHttpClient asynchttpclient = PicaApiUtility.uploadFile(null, fromUser, toUser, chatType, msgId, packetId, filePath, thumbnailPath,  type, providerId, contactName, uploadhttphandler, sendCount, totalCount);
			mapFileUploaders.put(packetId, asynchttpclient);
			return;
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
    }
    
    public boolean containUploadThread(String packetId) {
		return mapFileUploaders.containsKey(packetId);
	}
    
    public void deleteThread(String msgid){
    	AsyncHttpClient uploader = mapFileUploaders.get(msgid);
    	if (uploader != null){
			uploader.cancelRequest(getApplicationContext(), true, msgid);
    		mapFileUploaders.remove(msgid);
    	}
    }
    
    
    
    public void fileDownloadThread(String chatId, String _msgId, String _filePath, String _type, String to, String nickName, String thumbnailPath, int recvCount, int totalCount){
    	String httpUrl = "";
		RequestParams params = new RequestParams();
		params.put("recvcount", recvCount);
		if (_filePath.startsWith("file://")) {
    		httpUrl = mPref.getString(GlobalConstrants.API_URL, GlobalVariable.FILE_SERVER_URL) + (GlobalConstrants.DOWNLOAD_FILE) + "?to=" + to + "&filename=" + _filePath.substring(7) + "&thumb=0.1";
    	} else
    		httpUrl = _filePath;
    	DownloadHttpHandler downloadHttpHandler = new DownloadHttpHandler(this, chatId, _msgId, _filePath, _type, nickName, thumbnailPath, recvCount, totalCount);
    	AsyncHttpClient downloadClient = new AsyncHttpClient();
    	
    	try {
    		downloadClient.setTimeout(DEFAULT_TIMEOUT);
    		downloadClient.get(httpUrl, params, downloadHttpHandler, _msgId);
    		mapFileDownloaders.put(_msgId, downloadClient);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void deleteDownloadThread(String msgId){
    	AsyncHttpClient downloader = mapFileDownloaders.get(msgId);
    	if (downloader != null){
			downloader.cancelRequest(getApplicationContext(), true, msgId);
    		mapFileDownloaders.remove(msgId);
    	}
    }
    
    public boolean containDownloadThread(String packetId) {
		return mapFileDownloaders.containsKey(packetId);
	}

    @Override
	 public boolean onUnbind(Intent intent) {
	 	return super.onUnbind(intent);
	 }


	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent("com.multimediachat.start");
        sendBroadcast(intent);
	}


	@Override
	public IBinder onBind(Intent arg0) {
		return myFtpServiceStub;
	}
	
	

}