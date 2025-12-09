package com.multimediachat.service;

import android.content.Intent;

import java.io.File;

import com.multimediachat.R;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.im.IChatSession;
import com.multimediachat.app.im.IChatSessionManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.XmlParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

public class ChatManager {
	public static void sendFile(final long providerId, long accountId, final String friendAddress, String filePath, final String splitPath, final String thumbnailPath, final int type, final String offerId, final int sendCount, final int totalCount) {
		if ( !ImApp.getInstance().isServerLogined() ){
			GlobalFunc.showToast(ImApp.applicationContext, R.string.network_error, false);
			NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
			return;
		}
		try {
			File fileSend = new File(splitPath);
			if ( fileSend != null && fileSend.exists() ) {
				IImConnection mConn =  ImApp.getInstance().getConnection(providerId);
				IChatSessionManager manager = mConn.getChatSessionManager();
				final IChatSession session = manager.getChatSession(friendAddress);
				if (session != null) {
					int chatType = 1;
					if (session.isGroupChatSession())
						chatType = 2;
					final String toAddress = friendAddress.split("@")[0];
					final String userAddress = Imps.Account.getUserName(ImApp.getInstance().getContentResolver(), accountId);
					mPref.putString(GlobalConstrants.ORIGIN_FILE_PATH_BEFORE_SPLIT, filePath);

					AsyncHttpClient httpClient = new AsyncHttpClient();
					final int finalChatType = chatType;
					AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
							JSONObject jsonObject;
							String response = new String((binaryData));

							try {
								jsonObject = XmlParser.parseXmlToJSONObject(response);
								jsonObject = jsonObject.getJSONObject(Imps.Params.RESULT);

								if (jsonObject != null) {
									String result = "";
									if (jsonObject.has(Imps.Params.ERRCODE))
										result = jsonObject.getString(Imps.Params.ERRCODE);
									if (result.equals("0")) {
										ImApp.getInstance().uploadHandler(userAddress, toAddress, finalChatType, (int) session.getId(), offerId,
												splitPath, thumbnailPath, type, providerId, friendAddress, sendCount, totalCount);
										return;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							Imps.updateMessageTypeInDb(ImApp.getInstance().getContentResolver(), offerId, Imps.MessageType.OUTGOING);
							Imps.updateOperMessageError(ImApp.applicationContext.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
							GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
							NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
							Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
							ImApp.applicationContext.sendBroadcast(intent);
						}

						@Override
						public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
							Imps.updateMessageTypeInDb(ImApp.getInstance().getContentResolver(), offerId, Imps.MessageType.OUTGOING);
							Imps.updateOperMessageError(ImApp.applicationContext.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
							GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
							NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
							Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
							ImApp.applicationContext.sendBroadcast(intent);
						}
					};
					responseHandler.setUsePoolThread(true);

					String httpUrl = mPref.getString(GlobalConstrants.API_URL, GlobalVariable.WEBAPI_URL) + GlobalConstrants.CHECK_CALLABLE;

					RequestParams params = new RequestParams();
					params.put("from", userAddress);
					params.put("to", toAddress);

					try {
						httpClient.setTimeout(10 * 1000);
						httpClient.post(httpUrl, params, responseHandler);
					} catch (Exception e) {
						e.printStackTrace();
					}

					NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
				}
			}
			else{
				ImApp.getInstance().deleteUploadHandler(offerId);
				GlobalFunc.showToast(ImApp.applicationContext, R.string.error_message_failed_send_file, false);
				Imps.updateOperMessageError(ImApp.applicationContext.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
				GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
				Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
				ImApp.applicationContext.sendBroadcast(intent);
				NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
			}

		} catch (Exception e) {
			ImApp.getInstance().deleteUploadHandler(offerId);
			GlobalFunc.showToast(ImApp.applicationContext,R.string.error_message_failed_send_file, false);
			Imps.updateOperMessageError(ImApp.applicationContext.getContentResolver(), offerId, Imps.FileErrorCode.UPLOADFAILED);
			GlobalFunc.setUploadingStatus(GlobalConstrants.NO_UPLOADING);
			Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_UPLOAD_FINISHED);
			ImApp.applicationContext.sendBroadcast(intent);
			NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_list_update);
			return;
		}
	}
}
