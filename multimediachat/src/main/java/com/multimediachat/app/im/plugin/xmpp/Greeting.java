package com.multimediachat.app.im.plugin.xmpp;

import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;

public class Greeting implements PacketExtension{
    public static final String NAMESPACE = "urn:xmpp:greeting";
    private String message;
    
    public Greeting(String aGreeting){
    	message = aGreeting;
    }
    
    public String getElementName() {
        return "greeting";
    }

    public String getNamespace() {
        return NAMESPACE;
    }
    
    public void setGreeting(String aGreeting){
    	message = aGreeting;
    }
    
    public String getGreeting(){
    	return message;
    }

    public String toXML() {
        return "<greeting xmlns='" + NAMESPACE + "' message='" + message + "'/>";
    }
    
    static public class GreetingProvider extends EmbeddedExtensionProvider {

        @Override
        protected PacketExtension createReturnExtension(String currentElement,
                String currentNamespace, Map<String, String> attributeMap,
                List<? extends PacketExtension> content) {
            return new Greeting(attributeMap.get("message"));
        }

    }

    static void addExtensionProviders() {
        ProviderManager pm = ProviderManager.getInstance();
        // add IQ handling
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());
        // add delivery receipts
        pm.addExtensionProvider("greeting", NAMESPACE, new GreetingProvider());
    }
}
