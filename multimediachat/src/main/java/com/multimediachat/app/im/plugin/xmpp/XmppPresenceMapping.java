package com.multimediachat.app.im.plugin.xmpp;

import java.util.Map;

import com.multimediachat.app.im.plugin.ImPluginConstants;
import com.multimediachat.app.im.plugin.PresenceMapping;

/** A simple implementation of PresenceMaping for the provider. */
public class XmppPresenceMapping implements PresenceMapping {

    public int[] getSupportedPresenceStatus() {
        return new int[] { ImPluginConstants.PRESENCE_AVAILABLE,
                          ImPluginConstants.PRESENCE_DO_NOT_DISTURB,
                          ImPluginConstants.PRESENCE_OFFLINE };
    }

    public boolean getOnlineStatus(int status) {
        return status != ImPluginConstants.PRESENCE_OFFLINE;
    }

    public String getUserAvaibility(int status) {
        switch (status) {
        case ImPluginConstants.PRESENCE_AVAILABLE:
            return ImPluginConstants.PA_AVAILABLE;

        case ImPluginConstants.PRESENCE_DO_NOT_DISTURB:
            return ImPluginConstants.PA_DISCREET;

        case ImPluginConstants.PRESENCE_OFFLINE:
            return ImPluginConstants.PA_NOT_AVAILABLE;

        default:
            return null;
        }
    }

    public Map<String, Object> getExtra(int status) {
        // We don't have extra values except OnlineStatus and UserAvaibility
        // need to be sent to the server. If we do need other values to the server,
        // return a map the values structured the same as they are defined in the spec.
        //
        // e.g.
        // Map<String, Object> extra = new HashMap<String, Object>();
        //
        // HashMap<String, Object> commCap = new HashMap<String, Object>();
        //
        // HashMap<String, Object> commC = new HashMap<String, Object>();
        // commC.put("Qualifier", "T");
        // commC.put("Cap", "IM");
        // commC.put("Status", "Open");
        //
        // commCap.put("Qualifier", "T");
        // commCap.put("CommC", commC);
        //
        // extra.put("CommCap", commCap);
        // return extra;
        return null;
    }

    public boolean requireAllPresenceValues() {
        // Return false since we don't need all values received from the server
        // when map it to the predefined presence status.
        return false;
    }

    public int getPresenceStatus(boolean onlineStatus, String userAvailability, @SuppressWarnings("rawtypes") Map allValues) {
        if (!onlineStatus) {
            return ImPluginConstants.PRESENCE_OFFLINE;
        }
        if (ImPluginConstants.PA_NOT_AVAILABLE.equals(userAvailability)) {
            return ImPluginConstants.PRESENCE_AWAY;
        } else if (ImPluginConstants.PA_DISCREET.equals(userAvailability)) {
            return ImPluginConstants.PRESENCE_DO_NOT_DISTURB;
        } else {
            return ImPluginConstants.PRESENCE_AVAILABLE;
        }
    }

}
