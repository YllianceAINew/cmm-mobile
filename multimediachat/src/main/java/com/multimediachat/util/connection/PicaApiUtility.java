package com.multimediachat.util.connection;

import android.content.Context;
import android.media.MediaFile;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.util.EncDecDes;
import com.multimediachat.util.PrefUtil.mPref;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;


public class PicaApiUtility {

    private static int DEFAULT_TIMEOUT = 30 * 1000;
    private static AsyncHttpClient m_asyncClient = null;
    private static SyncHttpClient m_syncClient = null;

    private static RequestHandle post(AsyncHttpClient client, Context context, String url, RequestParams params, final AsyncHttpResponseHandler res, String requestMethod, boolean useJsonStreamer, String packetId) {

        if (ImApp.getInstance().api_key == null) {
            if (mPref.getString("apikey", null) != null) {
                ImApp.getInstance().api_key = mPref.getString("apikey", null);
            }
        }
        if (ImApp.getInstance().api_key != null) {
            params.put(Imps.Params.APIKEY, ImApp.getInstance().api_key);
        }

        url = mPref.getString(GlobalConstrants.FILE_SERVER_URL, GlobalVariable.FILE_SERVER_URL) + url;
        RequestHandle handle = null;

        try {
            if (requestMethod.equals("POST")) {
                if (params != null) {
                    handle = client.post(context, url, params, res, packetId);
                } else {
                    handle = client.post(context, url, new RequestParams(), res);
                }
            } else if (requestMethod.equals("GET")) {
                if (params != null) {
                    handle = client.get(context, url, params, res);
                } else {
                    handle = client.get(context, url, res);
                }
            } else if (requestMethod.equals("PUT")) {
                if (params != null) {
                    handle = client.put(context, url, params, res);
                } else {
                    handle = client.put(context, url, new RequestParams(), res);
                }
            } else if (requestMethod.equals("DELETE")) {
                if (params != null) {
                    handle = client.delete(context, url, null, params, res);
                } else {
                    handle = client.delete(context, url, res);
                }
            }
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }

        return handle;
    }

    private static RequestHandle post(AsyncHttpClient client, Context context, String url, RequestParams params, final AsyncHttpResponseHandler res, String requestMethod, boolean useJsonStreamer) {

        if (ImApp.getInstance().api_key == null) {
            if (mPref.getString("apikey", null) != null) {
                ImApp.getInstance().api_key = mPref.getString("apikey", null);
            }
        }
        if (ImApp.getInstance().api_key != null) {
            params.put(Imps.Params.APIKEY, ImApp.getInstance().api_key);
        }

        url = mPref.getString(GlobalConstrants.API_URL, GlobalVariable.WEBAPI_URL) + url;
        RequestHandle handle = null;

        try {
            if (requestMethod.equals("POST")) {
                if (params != null) {
                    handle = client.post(context, url, params, res);
                } else {
                    handle = client.post(context, url, new RequestParams(), res);
                }
            } else if (requestMethod.equals("GET")) {
                if (params != null) {
                    handle = client.get(context, url, params, res);
                } else {
                    handle = client.get(context, url, res);
                }
            } else if (requestMethod.equals("PUT")) {
                if (params != null) {
                    handle = client.put(context, url, params, res);
                } else {
                    handle = client.put(context, url, new RequestParams(), res);
                }
            } else if (requestMethod.equals("DELETE")) {
                if (params != null) {
                    handle = client.delete(context, url, null, params, res);
                } else {
                    handle = client.delete(context, url, res);
                }
            }
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }

        return handle;
    }

    private static RequestHandle post(Context context, String url, RequestParams params, final AsyncHttpResponseHandler res, String requestMethod, boolean useJsonStreamer) {
        if (m_asyncClient == null) {
            m_asyncClient = new AsyncHttpClient();
            m_asyncClient.setTimeout(DEFAULT_TIMEOUT);
        }

        return post(m_asyncClient, context, url, params, res, requestMethod, useJsonStreamer);
    }

    private static RequestHandle post(Context context, String url, RequestParams params, final AsyncHttpResponseHandler res, String requestMethod, boolean useJsonStreamer, String packetId) {
        if (m_asyncClient == null) {
            m_asyncClient = new AsyncHttpClient();
            m_asyncClient.setTimeout(DEFAULT_TIMEOUT);
        }

        return post(m_asyncClient, context, url, params, res, requestMethod, useJsonStreamer, packetId);
    }

    private static RequestHandle post(Context context, String url, RequestParams params, final AsyncHttpResponseHandler res, String requestMethod) {
        return post(context, url, params, res, requestMethod, true);
    }

    private static RequestHandle post(Context context, String url, RequestParams params, final AsyncHttpResponseHandler res, String requestMethod, String packetId) {
        return post(context, url, params, res, requestMethod, true, packetId);
    }

    private static RequestHandle syncpost(Context context, String url, RequestParams params, final AsyncHttpResponseHandler res, String requestMethod) {
        if (m_syncClient == null) {
            m_syncClient = new SyncHttpClient();
            m_syncClient.setTimeout(DEFAULT_TIMEOUT);
        }

        return post(m_syncClient, context, url, params, res, requestMethod, true);
    }

    public static void cancelRequests(Context context) {
        if (m_asyncClient != null)
            m_asyncClient.cancelRequests(context, true);
    }

    public static RequestHandle findFriendByIdOrNumber(Context context, String keyword, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put("keyword", keyword);

        return post(context, GlobalConstrants.SEARCH_BY_ID_AND_PHONENUMBER_URL, params, res, "POST");
    }

    public static RequestHandle loginAccount(Context context, String userid, String phone, String imsi, String password, String loginkey, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put(Imps.Params.USERID, userid);
        params.put(Imps.Params.IMSI, imsi);
        params.put(Imps.Params.PASSWORD, password);
        params.put(Imps.Params.LOGINKEYFILE, loginkey);

        return post(context, GlobalConstrants.LOGIN_ACCOUNT_URL, params, res, "POST");
    }

    public static RequestHandle getMobileContacts(Context context, List<String> contacts, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put(Imps.Params.CONTACTS, TextUtils.join(",", contacts));

        return post(context, GlobalConstrants.GET_MOBILE_CONTACTS, params, res, "POST");
    }

    public static RequestHandle createNewAccount(Context context, String nickname, String userid, String password,
                                                 String birthnum, String deviceNum, String imei,
                                                 String imsi, String phone, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put(Imps.Params.DEVICENUM, deviceNum);
        params.put(Imps.Params.NICKNAME, nickname);
        params.put(Imps.Params.USERID, userid);
        params.put(Imps.Params.PHONE, phone);
        params.put(Imps.Params.PASSWORD, password);
        params.put(Imps.Params.IMEI, imei);
        params.put(Imps.Params.IMSI, imsi);
        params.put(Imps.Params.BIRTHNUMBER, birthnum);

        return post(context, GlobalConstrants.CREATE_ACCOUNT, params, res, "POST");
    }

    public static String  getAvatarImageUrl(String address, int sizeType) {
        String ret = "";

        if (address == null || address.isEmpty())
            return ret;

        if (address.contains("@"))
            address = GlobalFunc.parseName(address);

        String strType = "small";

        ret = GlobalVariable.WEBAPI_URL + GlobalConstrants.GET_PROFILE_PHOTO_DOWNLOAD_URL + "/" + address + "?username=" + address + "&type=" + strType;

        return ret;
    }

    public static RequestHandle getMyProfile(Context context, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put(Imps.Params.USERNAME, ImApp.getInstance().userName);

        return post(context, GlobalConstrants.GET_PROFILE, params, res, "POST");
    }

    public static RequestHandle getProfile(Context context, String userName, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put("username", userName);

        return post(context, GlobalConstrants.GET_PROFILE, params, res, "POST");
    }

    public static RequestHandle getProfileInSyncMode(Context context, String userName, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put("username", userName);

        return syncpost(context, GlobalConstrants.GET_PROFILE, params, res, "POST");
    }

    public static RequestHandle uploadProfilePhoto(Context context, String username, String croppedPhotoFile, AsyncHttpResponseHandler res) {
        // delete small cache
        String encname = EncDecDes.getInstance().generateFileName(username + "_small");
        File file = new File(GlobalConstrants.AVATAR_DIR_PATH + encname);
        if (file.exists()) {
            file.delete();
        }

        // delete medium cache
        encname = EncDecDes.getInstance().generateFileName(username + "_medium");
        file = new File(GlobalConstrants.AVATAR_DIR_PATH + encname);
        if (file.exists()) {
            file.delete();
        }

        // delete big cache
        encname = EncDecDes.getInstance().generateFileName(username + "_big");
        file = new File(GlobalConstrants.AVATAR_DIR_PATH + encname);
        if (file.exists()) {
            file.delete();
        }

        RequestParams params = new RequestParams();

        try {
            params.put("uploadFile1", new File(croppedPhotoFile));
            params.put("username", username);
        } catch (FileNotFoundException filenotfoundexception) {
            filenotfoundexception.printStackTrace();
            res.onFailure(0, null, null, null);
        }

        return post(context, GlobalConstrants.UPLOAD_PHOTO, params, res, "POST", false);
    }

    public static RequestHandle removeProfilePhoto(Context context, MyXMLResponseHandler res) {
        RequestParams params = new RequestParams();

        return post(context, GlobalConstrants.REMOVE_PHOTO, params, res, "POST");
    }

    public static AsyncHttpClient uploadFile(Context context, final String fromUser, final String toUser, final int chatType, final int msgId, final String packetId, final String filePath, final String thumbnailPath,
                                             final int type, final long providerId, final String contactName, AsyncHttpResponseHandler res, int sendCount, int totalCount) {
        RequestParams requestparams = new RequestParams();

        try {
            requestparams.put("from", fromUser);
            requestparams.put("to", toUser);
            requestparams.put("chattype", String.valueOf(chatType));//ccj
            requestparams.put("filetype", String.valueOf(type));
            requestparams.put("kind", "a");
            requestparams.put("sendcount", sendCount);
            requestparams.put("totalcount", totalCount);
            requestparams.put("packetid", packetId);
            requestparams.put("uploadFile1", new File(filePath));
        } catch (FileNotFoundException filenotfoundexception) {
            filenotfoundexception.printStackTrace();
            return null;
        }

        post(context, GlobalConstrants.UPLOAD_FILE, requestparams, res, "POST", packetId);

        return m_asyncClient;
    }

    public static RequestHandle checkPhoneNumber(Context context, String phonenumber, String imei, MyXMLResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put("phonenumber", phonenumber);
        params.put("imei", imei);

         return post(context, GlobalConstrants.CHECK_PHONENUMBER, params, res, "POST");
    }

    public static RequestHandle confirmPassword(Context context, String pwd, AsyncHttpResponseHandler res){
        RequestParams params = new RequestParams();

        params.put(Imps.Params.PASSWORD, pwd);

        return post(context, GlobalConstrants.CONFIRM_PWD, params, res, "POST");
    }

    public static RequestHandle changePassword(Context context, String oldPassword, String newPassword, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put(Imps.Params.PASSWORD, newPassword);
        params.put(Imps.Params.OLDPASSWORD, oldPassword);

        return post(context, GlobalConstrants.CHANGE_PWD, params, res, "POST");
    }

    public static void getCurrentUserInformation(final Context context, final MyJSONResponseHandler res) {
        getMyProfile(context, new MyXMLResponseHandler() {
            @Override
            public void onMySuccess(JSONObject response) {
                DatabaseUtils.insertOrUpdateUserInfo(context.getContentResolver(), response);
                res.onMySuccess(response);
            }

            @Override
            public void onMyFailure(int errcode) {
                res.onMyFailure(errcode);
            }
        });
    }

    public static RequestHandle checkMimetype(Context context, String filePath, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        File file = new File(filePath);
        String mimetype = MediaFile.getMimeTypeForFile(filePath);
        long filesize = file.length()/1024/1024/1024; // MByte
        if (filesize == 0) filesize = 1;

        params.put(Imps.Params.FILESIZE, filesize);
        params.put(Imps.Params.MIMETYPE, mimetype);

        return post(context, GlobalConstrants.CHECK_MIMETYPE, params, res, "POST", null);
    }

    public static RequestHandle checkBirthnum(Context context, String birthnum, String devicenum, AsyncHttpResponseHandler res) {
        RequestParams params = new RequestParams();

        params.put(Imps.Params.BIRTHNUMBER, birthnum);
        params.put(Imps.Params.DEVICENUM, devicenum);

        return post(context, GlobalConstrants.CHECK_BIRTHNUM, params, res, "POST");
    }

}
