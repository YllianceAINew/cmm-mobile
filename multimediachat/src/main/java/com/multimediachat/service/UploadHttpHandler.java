package com.multimediachat.service;

import com.multimediachat.app.ImApp;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.XmlParser;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.content.Intent;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class UploadHttpHandler extends AsyncHttpResponseHandler {
	public final static long FILE_PACKET_SIZE = 4096;
	private FtpService mService = null;
	private String packetId = "";
	private int msgId = 0;
	private String downloadpath = "";
	private String filePath = "";
	private long providerId;
	private String contactName;
	private int sendCount;
	private int totalCount;

	public int progress;
	public boolean isUploading = true;

	public UploadHttpHandler(FtpService svc, int _msgId, String _packetId, long _providerId, String _contactName,
			String _filePath, int _sendCount, int _totalCount) {
		super();
		mService = svc;
		msgId = _msgId;
		packetId = _packetId;
		progress = 0;
		providerId = _providerId;
		contactName = _contactName;
		filePath = _filePath;
		sendCount = _sendCount;
		totalCount = _totalCount;
	}

	@Override
	public void onProgress(long bytesWritten, long totalSize) {
		super.onProgress(bytesWritten, totalSize);
		int curProgress = (sendCount * 100 + (int) (bytesWritten * 100 / totalSize)) / totalCount;
		if (progress != curProgress) {
			String msgHeader = GlobalConstrants.UPLOAD_PROGRESS;
			String[] params = new String[3];
			params[0] = msgHeader;
			params[1] = packetId;
			params[2] = String.valueOf(curProgress);

			Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FILE_UP_DOWN_LOAD);
			intent.putExtra("msg", msgHeader);
			intent.putExtra("content", params);
			mService.sendBroadcast(intent);
			progress = curProgress;
		}
	}

	@Override
	public void onSuccess(int i, Header[] headers, byte[] bytes) {
		onSuccess(new String(bytes));
	}

	public void onSuccess(String response) {
		int ret = 0;
		JSONObject jsonObject = null;

		try {
			jsonObject = XmlParser.parseXmlToJSONObject(response);
			jsonObject = jsonObject.getJSONObject(Imps.Params.RESULT);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (jsonObject != null) {
			try {
				if ( (jsonObject.has(Imps.Params.CODE) && jsonObject.getString(Imps.Params.CODE).equals("Y")) ||
					 (jsonObject.has(Imps.Params.SUCCESS) && jsonObject.getString(Imps.Params.SUCCESS).equals("true")) ) {
					ret = 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!mService.containUploadThread(packetId) || ret == 0) {
			onFailed();
			return;
		}

		if ((Imps.getErrorByPacketId(packetId) == Imps.FileErrorCode.UPLOADCANCELLED) || (Imps.getErrorByPacketId(packetId) == -1))
			return;

		if (sendCount == (totalCount - 1)){
			try {
				downloadpath = jsonObject.getString(Imps.Params.DOWNLOADPATH);
				String msgHeader = GlobalConstrants.UPLOAD_SUCCESS;
				String[] params = new String[7];
				params[0] = msgHeader;
				params[1] = packetId;
				params[2] = downloadpath;
				params[3] = String.valueOf(providerId);
				params[4] = contactName;
				params[5] = mPref.getString(GlobalConstrants.ORIGIN_FILE_PATH_BEFORE_SPLIT, filePath);
				params[6] = String.valueOf(msgId);

				Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FILE_UP_DOWN_LOAD);
				intent.putExtra("msg", msgHeader);
				intent.putExtra("content", params);
				Imps.updateOperMessageError(mService.getContentResolver(), params[1], Imps.FileErrorCode.UPLOADSUCCESS);
				if (Imps.updateMessageBody(mService.getContentResolver(), params[1], params[5]) > 0) {
					GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
					long providerId = 0;
					try {
						providerId = Integer.valueOf(params[3]);
						IImConnection conn = ImApp.getInstance().getConnection(providerId);
						if (conn != null) {
							IChatSession session = conn.getChatSessionManager().getChatSession(params[4]);
							if (session == null) {
								session = conn.getChatSessionManager().createChatSession(params[4]);
							}
							if (session != null) {
								String messageBody = String.format("%s_%d", params[2], totalCount);
								session.sendMessage(messageBody, params[1], params[5]);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (!ImApp.applicationUIInited) {
						NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
					} else {
						mService.sendBroadcast(intent);
					}

					Intent intent1 = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
					mService.sendBroadcast(intent1);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else{
			Imps.updateMessageSendCount(mService.getContentResolver(), packetId, sendCount + 1);
			Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
			intent.putExtra("packetId", packetId);
			mService.sendBroadcast(intent);
		}
	}

	@Override
	public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
		onFailed();
	}

	private void onFailed() {
		String msgHeader = GlobalConstrants.UPLOAD_FAILED;
		String[] params = new String[4];
		params[0] = msgHeader;
		params[1] = packetId;
		params[2] = String.valueOf(msgId);
		params[3] = filePath;
		Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FILE_UP_DOWN_LOAD);
		intent.putExtra("msg", msgHeader);
		intent.putExtra("content", params);

		if (Imps.getErrorByPacketId(packetId) != Imps.FileErrorCode.UPLOADCANCELLED)
			Imps.updateOperMessageError(mService.getContentResolver(), params[1], Imps.FileErrorCode.UPLOADFAILED);
		mService.sendBroadcast(intent);

		GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
		Intent intent1 = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
		mService.sendBroadcast(intent1);
	}
}
