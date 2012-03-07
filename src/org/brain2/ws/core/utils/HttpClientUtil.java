package org.brain2.ws.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;



public class HttpClientUtil {	
	
	static HttpClient defaultHttpClient; 

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:9.0) Gecko/20100101 Firefox/9.0";
	public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; DROID2 GLOBAL Build/S273) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
	
	public static final HttpClient getThreadSafeClient() {
		if(defaultHttpClient == null){
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			
			ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager( new BasicHttpParams(), schemeRegistry);		 
			HttpClient httpClient = new DefaultHttpClient();
		    HttpParams params = httpClient.getParams();
		    defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
		}
	    return defaultHttpClient;
	}
	
	public static final HttpClient makeThreadSafeClient() {
		HttpClient defaultHttpClient; 
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		
		ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager( new BasicHttpParams(), schemeRegistry);		 
		HttpClient httpClient = new DefaultHttpClient();
	    HttpParams params = httpClient.getParams();
	    defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
	
	    return defaultHttpClient;
	}
	
	public static final HttpClient theThreadSafeHttpClient() {
		HttpParams params = new BasicHttpParams();
	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    ClientConnectionManager cm = new ThreadSafeClientConnManager(params, registry);
	    HttpClient client = new DefaultHttpClient(cm, params);
	    return client;
	}
	
	public static String executePost(String url,Map<String, String> params, String accessTokens) {
		try {
			HttpClient httpClient = getThreadSafeClient();
			HttpPost postRequest = new HttpPost(url);			
			postRequest.addHeader("Accept-Charset", HTTP.UTF_8);
			postRequest.addHeader("User-Agent", MOBILE_USER_AGENT);
			postRequest.setHeader("Authorization", "OAuth oauth_token="+accessTokens);

			Set<String> names = params.keySet();
			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(names.size());					
			for (String name : names) {
				System.out.println( name + "=" + params.get(name));
				postParameters.add(new BasicNameValuePair(name, params.get(name)));
			}			
			postRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			
			HttpResponse response = httpClient.execute(postRequest);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				httpClient.getConnectionManager().closeExpiredConnections();
				return EntityUtils.toString(entity, HTTP.UTF_8);
			}
		} catch (HttpResponseException e) {
			System.err.println(e.getMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String executeMultipartPost(String postUrl,Map<String, Object> params, String accessTokens)
			throws Exception {
		try {
			HttpClient httpClient = getThreadSafeClient();
			HttpPost postRequest = new HttpPost(postUrl);			
			postRequest.addHeader("Accept-Charset", HTTP.UTF_8);
			postRequest.addHeader("User-Agent", MOBILE_USER_AGENT);			
			postRequest.setHeader("Cache-Control", "max-age=3, must-revalidate, private");	
			postRequest.setHeader("Authorization", "OAuth oauth_token="+accessTokens+", oauth_consumer_key=a324957217164fd1d76b4b60d037abec, oauth_version=1.0, oauth_signature_method=HMAC-SHA1, oauth_timestamp=1322049404, oauth_nonce=-5195915877644743836, oauth_signature=wggOr1ia7juVbG%2FZ2ydImmiC%2Ft4%3D");

			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			Set<String> names = params.keySet();
			for (String name : names) {
				Object value = params.get(name);
				if (value instanceof StringBody) {
					reqEntity.addPart(name, (StringBody) value);
				} else if (value instanceof File) {
					reqEntity.addPart("media", new FileBody((File) value));
				}
			}
			postRequest.setEntity(reqEntity);

			HttpResponse response = httpClient.execute(postRequest);
			return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
		} catch (Exception e) {
			// handle exception here
			e.printStackTrace();
		}
		return "";
	}
	
	public static String executePost(String url){
		try {	
			HttpPost httppost = new HttpPost(url);
			
			httppost.setHeader("User-Agent", USER_AGENT);
			httppost.setHeader("Accept-Charset", "utf-8");			
			httppost.setHeader("Cache-Control", "max-age=3, must-revalidate, private");	
			httppost.setHeader("Authorization", "OAuth oauth_token=2d62f7b3de642cdd402f62e42fba0b25, oauth_consumer_key=a324957217164fd1d76b4b60d037abec, oauth_version=1.0, oauth_signature_method=HMAC-SHA1, oauth_timestamp=1322049404, oauth_nonce=-5195915877644743836, oauth_signature=wggOr1ia7juVbG%2FZ2ydImmiC%2Ft4%3D");

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
	
	
	public static String executeGet(final String url, boolean safeThread){
		try {		
			
			HttpGet httpget = new HttpGet(url);			
			httpget.setHeader("User-Agent", USER_AGENT);
			httpget.setHeader("Accept-Charset", "utf-8");	
			
			httpget.setHeader("Cache-Control", "max-age=3, must-revalidate, private");	
			//httpget.setHeader("Authorization", "OAuth oauth_token=223a363ea1fd0a13b44e52663b97a255, oauth_consumer_key=a324957217164fd1d76b4b60d037abec, oauth_version=1.0, oauth_signature_method=HMAC-SHA1, oauth_timestamp=1322049404, oauth_nonce=-5195915877644743836, oauth_signature=wggOr1ia7juVbG%2FZ2ydImmiC%2Ft4%3D");
			

			HttpResponse response;
			if(safeThread){
				response = getThreadSafeClient().execute(httpget);
			} else { 
				response = makeThreadSafeClient().execute(httpget);
			}
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					//getThreadSafeClient().getConnectionManager().closeExpiredConnections();
					return EntityUtils.toString(entity, HTTP.UTF_8);
				}
			} else if(code == 404) {
				return "404";
			} else {
				return "500";
			}
		}  catch (Throwable e) {
			if( e instanceof java.net.ConnectException){
				return "444";
			} 
			e.printStackTrace(); 
		}
		return "";
	}
	
	
	public static String executeGet(final String url){		
		return executeGet(url, true);
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
		
//		String json = executePost("http://mapi.vnexpress.net/comments/?method=send&article_id=1000419498&title=hello1&content=world1");
//		System.out.println("\ncontent:\n "+json);

//		String json2 = executePost("http://trieunt.mapi.vnexpress.net/comments/?method=send&article_id=1000718986&title=hell888o6&content=world888");
//		System.out.println("\ncontent:\n "+json2);
		
		String json3 = executeGet("http://mapi.vnexpress.net/user/?method=get");
		System.out.println("\ncontent:\n "+json3);
	}
	
	
}
