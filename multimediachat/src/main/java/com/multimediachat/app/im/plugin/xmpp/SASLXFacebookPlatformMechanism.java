package com.multimediachat.app.im.plugin.xmpp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.util.StringUtils;
 
public class SASLXFacebookPlatformMechanism extends SASLMechanism {
 
    public static final String NAME = "X-FACEBOOK-PLATFORM";
 
    public SASLXFacebookPlatformMechanism(SASLAuthentication saslAuthentication) {
        super(saslAuthentication);
    }
 
    @Override
    protected void authenticate() throws IOException, XMPPException {
        getSASLAuthentication().send(new AuthMechanism(getName(), ""));
    }
 
    @Override
    protected String getName() {
        return NAME;
    }
 
    @Override
    public void challengeReceived(String challenge) throws IOException {
        byte decodedChallenge[];
 
        if(challenge != null) {
           decodedChallenge = StringUtils.decodeBase64(challenge);
        } else {
           decodedChallenge = new byte[0];
        }
 
        Map<String, String> parsedChallenge = getQueryMap(new String(decodedChallenge));
 
        Map<String, String> responseParams = new HashMap<String, String>();
        responseParams.put("api_key", this.authenticationId);
        responseParams.put("call_id", Long.toString(new GregorianCalendar().getTimeInMillis() / 1000L));
        responseParams.put("method", parsedChallenge.get("method"));
        responseParams.put("nonce", parsedChallenge.get("nonce"));
        responseParams.put("access_token", this.password);
        responseParams.put("v", "1");
 
 
        byte response[] = encodeMap(responseParams).getBytes("utf-8");
 
        Packet responseStanza = new Response(StringUtils.encodeBase64(response, false));
 
        // Send the authentication to the server
        getSASLAuthentication().send(responseStanza);
    }
 
    private Map < String, String > getQueryMap(String query) {
        Map < String, String > map = new HashMap < String, String > ();
        String[] params = query.split("&");
 
        for (String param: params) {
            String[] fields = param.split("=", 2);
            map.put(fields[0], (fields.length > 1 ? fields[1] : null));
        }
        return map;
    }
 
    private String encodeMap(Map<String, String> m) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<String,String> entry : m.entrySet()) {
            builder.append("&" + entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8"));
        }
        return builder.substring(1);
    }
}
