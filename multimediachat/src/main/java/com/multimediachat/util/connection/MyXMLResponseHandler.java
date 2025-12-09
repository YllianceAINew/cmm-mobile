package com.multimediachat.util.connection;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.util.XmlParser;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018.02.23.
 */

public abstract class MyXMLResponseHandler extends AsyncHttpResponseHandler {
    public MyXMLResponseHandler()
    {
        super();
    }

    public MyXMLResponseHandler(boolean usePoolThread)
    {
        super();
        setUsePoolThread(usePoolThread);
    }

    @Override
    public void onSuccess(int i, Header[] headers, byte[] bytes) {
        if ( bytes == null ) {
            onSuccess("");
        } else {
            String content = new String(bytes);

            onSuccess(content);
        }
    }

    public void onSuccess(String content) {
        JSONObject jsonObject = null;

        try {
            jsonObject = XmlParser.parseXmlToJSONObject(content);

            if (jsonObject == null) {
                onMyFailure(-1);
                return;
            }

            if (jsonObject.has("result")) {
                jsonObject = jsonObject.getJSONObject("result");
            } else {
                onMyFailure(-1);
                return;
            }

            if ( jsonObject.has("code") && !jsonObject.getString("code").equals("Y") ) {
                if (jsonObject.has("apikey")) {
                    String apikey = jsonObject.getString("apikey");
                    if (!apikey.isEmpty()) {
                        ImApp.getInstance().api_key = apikey;
                        mPref.putString("apikey", apikey);
                    }
                }
                onMyFailure(jsonObject.getInt("errcode"));
            } else if ( jsonObject.has("success") && !jsonObject.getString("success").equals("true") )
                onMyFailure(jsonObject.getInt("errcode"));
            else if ( !jsonObject.has("code") && !jsonObject.has("success") )
                onMyFailure(-1);
            else
                onMySuccess(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            onMyFailure(-1);
        }
    }

    @Override
    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
        onFailure(throwable);
    }

    public void onFailure(Throwable error) {
        onMyFailure(-1);
    }

    public abstract void onMySuccess(JSONObject response);

    public abstract void onMyFailure(int errcode);
}
