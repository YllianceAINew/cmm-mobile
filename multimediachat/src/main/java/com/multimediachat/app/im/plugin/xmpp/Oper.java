package com.multimediachat.app.im.plugin.xmpp;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.xmlpull.v1.XmlPullParser;

public class Oper implements PacketExtension{
    public static final String NAMESPACE = "urn:xmpp:oper";
    public static final String TYPE_SEEN = "seen";
    public static final String TYPE_DELETE = "delete";
    public static final String TYPE_MODIFY = "modify";
    public static final String TYPE_ERROR= "error";
    
    private String type;
    private String message;
    private String msgid;
    
    public Oper(String _type, String _message, String _msgid){
    	type = _type;
    	message = _message;
    	msgid = _msgid;
    }
    
    public String getElementName() {
        return "oper";
    }

    public String getNamespace() {
        return NAMESPACE;
    }
    
    public void setType(String _type) {
    	type = _type;
    }
    
    public void setMessage(String _message){
    	message = _message;
    }
    
    public void setMsgid(String _msgid) {
    	msgid = _msgid;
    }
    
    public String getType(){
    	return type;
    }
    
    public String getMessage(){
    	return message;
    }
    
    public String getMsgid(){
    	return msgid;
    }

    public String toXML() {
    	String xml = "<oper xmlns='" + NAMESPACE + "' type='" + type + "'";
    	
    	if ( msgid != null )
    		xml = xml + " msgid='" + msgid + "'";
    	
    	/*if ( message != null )
    		xml = xml + " message='" + message + "'";*/
    	
    	xml = xml + ">";
    	
    	if ( message != null )
    		xml = xml + message;
    	
    	xml = xml + "</oper>";
    	return xml;
    }
    
    /*
    static public class OperProvider extends EmbeddedExtensionProvider {
        @Override
        protected PacketExtension createReturnExtension(String currentElement,
                String currentNamespace, Map<String, String> attributeMap,
                List<? extends PacketExtension> content) {
            return new Oper(attributeMap.get("type"), attributeMap.get("message"), attributeMap.get("msgid"));
        }

    }*/
    
    static class OperProvider implements PacketExtensionProvider {
        public OperProvider() {
        }

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        	String type = parser.getAttributeValue(null, "type");
        	String msgid = parser.getAttributeValue(null, "msgid");
        	String message = parser.nextText();
        	Oper oper = new Oper(type, message, msgid);
            return oper;
        }
    }

    static void addExtensionProviders() {
        ProviderManager pm = ProviderManager.getInstance();
        // add IQ handling
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());
        // add delivery receipts
        pm.addExtensionProvider("oper", NAMESPACE, new OperProvider());
    }
}
