package org.brain2.ws.core.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {
	
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.106 Safari/535.2";
	
	public static DefaultHttpClient getThreadSafeClient() {
	    DefaultHttpClient client = new DefaultHttpClient();
	    ClientConnectionManager mgr = client.getConnectionManager();
	    HttpParams params = client.getParams();	 
	    client = new DefaultHttpClient(new ThreadSafeClientConnManager(mgr.getSchemeRegistry()), params);	 
	    return client;
	}
	
	public static String executeGet(String url){
		try {
			HttpGet httpget = new HttpGet(url);
			httpget.setHeader("User-Agent", USER_AGENT);
			httpget.setHeader("Accept-Charset", "utf-8");
			httpget.setHeader("Cache-Control", "no-cache");			

			HttpResponse response = getThreadSafeClient().execute(httpget);
			int code = response.getStatusLine().getStatusCode(); 
			System.out.println(code);
			if (code == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, HTTP.UTF_8);
				}
			} else if(code == 404) {
				return "404";
			} else {
				return "500";
			}
		}  catch (HttpResponseException e) {
		    System.err.println(e.getMessage());		  
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return "";
	}
}
