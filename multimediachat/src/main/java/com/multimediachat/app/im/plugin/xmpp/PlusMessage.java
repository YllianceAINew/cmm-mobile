package com.multimediachat.app.im.plugin.xmpp;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;

public class PlusMessage implements PacketExtension{
    public static final String NAMESPACE = "urn:xmpp:plusmessage";
    private String type = null;
	private String thumbnail = null;
	private String count = null;
	private String urllink = null;
	
	public PlusMessage(String type,String thumbnail, String count,String urllink) {
		super();
		this.type = type;
		this.thumbnail = thumbnail;
		this.count = count;
		this.urllink = urllink;
	}
    public String getElementName() {
        return "x";
    }

    public String getNamespace() {
        return NAMESPACE;
    }
    public String getType() {
    	return type;
    }
    public String getThumbnail()
    {
    	return thumbnail;
    }
    public String getCount()
    {
    	return count;
    }
    public String getUrllink(){
    	return urllink;
    }
    public void setType(String type)
    {
    	this.type = type;
    }
    public void setThumbnail(String thumbnail)
    {
    	this.thumbnail = thumbnail;
    }
    public void setCount(String count)
    {
    	this.count = count;
    }
    public void setUrllink(String urllink)
    {
    	this.urllink = urllink;
    }
    
    public String toXML() {
        String message =  "<x xmlns='" + NAMESPACE + "' ";
        if (type != null && type.trim().length() > 0)
        	message += " type='" + type + "' ";
        if (thumbnail != null && thumbnail.trim().length() > 0)
        	message += " thumbnail='"+thumbnail+"'";
        if (count != null && count.trim().length() > 0)
        	message += " count='" + count + "'";
        if (urllink != null && urllink.trim().length() > 0)
        	message += " urllink='"+ urllink +"'";
        message += " />";
        return message;
    }
    
    static class PlusMessageProvider implements PacketExtensionProvider {
        public PlusMessageProvider() {
        }

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        	String type = parser.getAttributeValue(null, "type");
        	String thumbnail = parser.getAttributeValue(null, "thumbnail");
        	String count = parser.getAttributeValue(null, "count");
        	String urllink = parser.getAttributeValue(null, "urllink");
        	
            PlusMessage plusMessage = new PlusMessage(type, thumbnail, count, urllink);
            return plusMessage;
        }
    }
    
    static void addExtensionProviders() {
        ProviderManager pm = ProviderManager.getInstance();
        pm.addExtensionProvider("x", NAMESPACE, new PlusMessageProvider());
    }
}
