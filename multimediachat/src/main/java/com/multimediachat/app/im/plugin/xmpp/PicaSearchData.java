package com.multimediachat.app.im.plugin.xmpp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;

public class PicaSearchData extends IQ {
	Map<String,String> attributes;
	PicaSearchResult result;
    public PicaSearchData() {
    }
    public PicaSearchResult getSearchResult()
    {
    	return result;
    }
    public void setSearchResult(PicaSearchResult result) {
    	this.result = result;
    }
    public String getChildElementXML() {
    	StringBuilder buf = new StringBuilder();
    	buf.append("<query xmlns='urn:xmpp:picasearch'");
    	if (attributes != null) {
    		for (String key : attributes.keySet()) {
        		buf.append(" ").append(key).append("=\"")
        		.append(StringUtils.escapeForXML(attributes.get(key))).append("\"");
        	}
    	}
    	buf.append(">");
    	if (result != null) {
    		for(PicaSearchResult.Row row: result.getRows()) {
    			buf.append("<vcard ");
    			for (PicaSearchResult.Field field : row.getFields()) {
    				buf.append(" ").append(field.getVariable()).append("=\"").append(StringUtils.escapeForXML(field.getValue()) + "\"");
    			}
    			buf.append("/>");
    		}
    	}
    	buf.append("</query>");
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
           
            PicaSearchData picaData = new PicaSearchData();
            PicaSearchResult searchResult = new PicaSearchResult();
            try {
            	boolean done = false;
            	 while (!done) {
                     int eventType = parser.next();
                     if (eventType == XmlPullParser.START_TAG && parser.getName().equals("vcard")) {
                    	int attSize = parser.getAttributeCount();
                    	int i = 0;
                    	List<PicaSearchResult.Field> fields = new ArrayList<PicaSearchResult.Field>(); 
                     	while( i < attSize) {
                     		PicaSearchResult.Field field = new PicaSearchResult.Field(parser.getAttributeName(i), parser.getAttributeValue(i));
                     		fields.add(field);
                     		i++;
                     	}
                     	searchResult.addRow(new PicaSearchResult.Row(fields));
                     }
                     else if (eventType == XmlPullParser.END_TAG) {
                         if (parser.getName().equals("query")) {
                             done = true;
                         }
                     }
                 }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            picaData.setSearchResult(searchResult);
            return picaData;
        }
    }
}

