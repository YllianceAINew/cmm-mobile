package com.multimediachat.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpXmlConnect {
    public String mXmlString = "";
    public InputStream mInStream = null;
    
    public HttpXmlConnect(String strHttpUrl) {
    	httpConnect(strHttpUrl);
    }
    
    private void httpConnect(String strHttpUrl)
    {
    	try {
    		URL url = new URL(strHttpUrl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        conn.connect();	// Start the query
	        mInStream = conn.getInputStream();
	        
	        if (mInStream == null)
	        	return;

	        Reader reader = null;
	        reader = new InputStreamReader(mInStream, "UTF-8");
	        char[] buffer = new char[500];
	        reader.read(buffer);
	        mXmlString = new String(buffer);
	    } catch (IOException e) {
	    	
	    }
    }
    @Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		mInStream.close();
		super.finalize();
	}
   
}
