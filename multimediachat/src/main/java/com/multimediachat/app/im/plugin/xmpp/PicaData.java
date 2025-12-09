package com.multimediachat.app.im.plugin.xmpp;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;

public class PicaData extends IQ {
	Map<String,String> attributes;
    public PicaData() {
    }
    public String getChildElementXML() {
    	StringBuilder buf = new StringBuilder();
    	buf.append("<query xmlns='urn:xmpp:picatalk'");
    	for (String key : attributes.keySet()) {
    		buf.append(" ").append(key).append("=\"")
    		.append(StringUtils.escapeForXML(attributes.get(key))).append("\"");
    	}
    	buf.append("/>");
        return buf.toString();
    }
    /**
     * Returns the map of String key/value pairs of account attributes.
     *
     * @return the account attributes.
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Sets the account attributes. The map must only contain String key/value pairs.
     *
     * @param attributes the account attributes.
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    public static class Provider implements IQProvider {

        public Provider() {
            super();
        }
        
        

        public IQ parseIQ(XmlPullParser parser) throws Exception {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException("Parser not in proper position, or bad XML.");
            }
           
            PicaData picaData = new PicaData();
            try {
            	Map<String,String> params = new HashMap<String,String>();
            	int i = 0;
            	int attSize = parser.getAttributeCount();
            	while( i < attSize) {
            		params.put(parser.getAttributeName(i), parser.getAttributeValue(i));
            		i++;
            	}
            	picaData.setAttributes(params);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return picaData;
        }
    }
}

