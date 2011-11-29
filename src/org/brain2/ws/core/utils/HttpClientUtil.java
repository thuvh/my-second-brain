package org.brain2.ws.core.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

public class HttpClientUtil {
	
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:9.0) Gecko/20100101 Firefox/9.0";
	public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; DROID2 GLOBAL Build/S273) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
	
	public static DefaultHttpClient getThreadSafeClient() {
	    DefaultHttpClient client = new DefaultHttpClient();
	    ClientConnectionManager mgr = client.getConnectionManager();
	    HttpParams params = client.getParams();	 
	    client = new DefaultHttpClient(new ThreadSafeClientConnManager(mgr.getSchemeRegistry()), params);	    
	    return client;
	}
	
	
	public static String executePost(String url){
		try {	
			HttpPost httppost = new HttpPost(url);
			
			httppost.setHeader("User-Agent", USER_AGENT);
			httppost.setHeader("Accept-Charset", "utf-8");			
			httppost.setHeader("Cache-Control", "max-age=3, must-revalidate, private");	
			httppost.setHeader("Authorization", "OAuth oauth_token=0bd47489d18fc4e9496f4872fe83e505, oauth_consumer_key=a324957217164fd1d76b4b60d037abec, oauth_version=1.0, oauth_signature_method=HMAC-SHA1, oauth_timestamp=1322049404, oauth_nonce=-5195915877644743836, oauth_signature=wggOr1ia7juVbG%2FZ2ydImmiC%2Ft4%3D");

			HttpResponse response = getThreadSafeClient().execute(httppost);
			HttpEntity entity = response.getEntity();				
			if (entity != null) {
				getThreadSafeClient().getConnectionManager().closeExpiredConnections();
				return EntityUtils.toString(entity, HTTP.UTF_8);
			}
		
		}  catch (HttpResponseException e) {
		    System.err.println(e.getMessage());		  
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return "";
	}
	
	public static String executeGet(String url){
		try {
		
			
			HttpGet httpget = new HttpGet(url);
			
			httpget.setHeader("User-Agent", USER_AGENT);
			httpget.setHeader("Accept-Charset", "utf-8");			
			httpget.setHeader("Cache-Control", "max-age=3, must-revalidate, private");			

			HttpResponse response = getThreadSafeClient().execute(httpget);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				HttpEntity entity = response.getEntity();				
				if (entity != null) {
					getThreadSafeClient().getConnectionManager().closeExpiredConnections();
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
	
	public static void main(String[] args) throws JSONException {
//		String url = "http://vnexpress.net/tin/ban-doc-viet/2011/11/bong-da/";
//		String html = executeGet(url);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//		Elements nodes = doc.select(".lgsg");		
//		System.out.println(nodes.attr("title"));
		
//		String url = "http://mapi.vnexpress.net/articles?method=get&ids=1000419497";
//		
//		
//		String json = executeGet(url);
//		System.out.println(json);
//		
//		JSONObject jsonObject = new JSONObject(json);
//		JSONObject article = jsonObject.getJSONObject("body").getJSONArray("articles").getJSONObject(0); 
//		String content = article.getString("content");		
//		System.out.println("\ncontent:\n "+content);
		
		String json = executePost("http://mapi.vnexpress.net/comments/?method=send&article_id=1000419498&title=hello1&content=world1");
		System.out.println("\ncontent:\n "+json);

//		String json2 = executePost("http://trieunt.mapi.vnexpress.net/comments/?method=send&article_id=1000718986&title=hell888o6&content=world888");
//		System.out.println("\ncontent:\n "+json2);
		
//		String json3 = executeGet("http://trieunt.mapi.vnexpress.net/comments/?method=get&article_id=1000718986");
//		System.out.println("\ncontent:\n "+json3);
	}
	
	
}
