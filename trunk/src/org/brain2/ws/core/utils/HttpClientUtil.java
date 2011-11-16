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
import org.brain2.test.vneappcrawler.VnExpressParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
	
	public static String executeGet(String url){
		try {
			if( ! url.startsWith(VnExpressParser.BASE_URL)){
				url = VnExpressParser.BASE_URL + url;
			}
			
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
	
	public static void main(String[] args) {
		String url = "http://vnexpress.net/tin/ban-doc-viet/2011/11/bong-da/";
		String html = executeGet(url);
		Document doc = Jsoup.parse(html, HTTP.UTF_8);
		Elements nodes = doc.select(".lgsg");		
		System.out.println(nodes.attr("title"));
	}
}
