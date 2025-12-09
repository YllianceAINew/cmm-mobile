package com.multimediachat.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlParser {
	public static JSONObject parseXmlToJSONObject(String xmlString) {
		JSONObject jsonObj = null;
		try {
		    jsonObj = XML.toJSONObject(xmlString);
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		
		return jsonObj;
	}
	
	public static Map<String, String> parseXmlToMap(String xmlString) {
		Map<String, String> result = new HashMap<String, String>();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new StringReader(xmlString));
			int eventType = xpp.getEventType();

			String startTag = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
				} else if (eventType == XmlPullParser.END_DOCUMENT) {
					startTag = null;
				} else if (eventType == XmlPullParser.START_TAG) {
					try {
						startTag = xpp.getName();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					startTag = null;
				} else if (eventType == XmlPullParser.TEXT) {
					try {
						if (startTag != null)
							result.put(startTag, xpp.getText());
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					startTag = null;
				}
				eventType = xpp.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
