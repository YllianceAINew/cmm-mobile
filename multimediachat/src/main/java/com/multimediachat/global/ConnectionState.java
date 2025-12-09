package com.multimediachat.global;

/**
 * Created by admin on 2018.10.22.
 */

public class ConnectionState {

    public final static int eCONNSTAT_MOBILECOM_CONNECTED = 0;
    public final static int eCONNSTAT_MOBILECOM_CONNECTING = -10;
    public final static int eCONNSTAT_MOBILECOM_DISCONNECTED = -20;
    public final static int eCONNSTAT_MOBILECOM_CONNFAILED = -30;

    public final static int eCONNSTAT_XMPP_CONNECTED = 1;
    public final static int eCONNSTAT_XMPP_CONNECTING = -11;
    public final static int eCONNSTAT_XMPP_DISCONNECTED = -21;
    public final static int eCONNSTAT_XMPP_CONNFAILED = -31;

    public final static int eCONNSTAT_SIP_CONNECTED = 2;
    public final static int eCONNSTAT_SIP_CONNECTING = -12;
    public final static int eCONNSTAT_SIP_DISCONNECTED = -22;
    public final static int eCONNSTAT_SIP_CONNFAILED = -32;

    public final static int eCONNSTAT_LOGIN_CONNECTED = 3;
    public final static int eCONNSTAT_LOGIN_CONNECTING = -13;
    public final static int eCONNSTAT_LOGIN_DISCONNECTED = -23;
    public final static int eCONNSTAT_LOGIN_CONNFAILED = -33;

}
