package com.multimediachat.app.im.plugin.xmpp;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.multimediachat.app.im.plugin.ImConfigNames;
import com.multimediachat.app.im.plugin.ImPlugin;
import com.multimediachat.app.im.plugin.ImpsConfigNames;

/** Simple example of writing a plug-in for the IM application. */
public class XmppImPlugin extends Service implements ImPlugin {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** The implementation of IImPlugin defined through AIDL. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getProviderConfig() {
        HashMap<String, String> config = new HashMap<String, String>();
        // The protocol name MUST be IMPS now.
        config.put(ImConfigNames.PROTOCOL_NAME, "XMPP");
        config.put(ImConfigNames.PLUGIN_VERSION, "0.1");
        config.put(ImpsConfigNames.HOST, "http://xmpp.org/services/");
        config.put(ImpsConfigNames.SUPPORT_USER_DEFINED_PRESENCE, "true");
        config.put(ImpsConfigNames.CUSTOM_PRESENCE_MAPPING,
                "com.multimediachat.app.im.plugin.xmpp.XmppPresenceMapping");
        return config;
    }

    @SuppressLint("UseSparseArrays")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getResourceMap() {
        HashMap<Integer, Integer> resMapping = new HashMap<Integer, Integer>();

//        resMapping.put(BrandingResourceIDs.STRING_MENU_VIEW_PROFILE,
//                R.string.menu_view_encrypt_chat);
        return resMapping;
    }

}
