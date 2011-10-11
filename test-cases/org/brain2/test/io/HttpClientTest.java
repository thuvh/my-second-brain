package org.brain2.test.io;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * This example demonstrates the use of the {@link ResponseHandler} to simplify
 * the process of processing the HTTP response and releasing associated
 * resources.
 */
public class HttpClientTest {

	public static void loadTest1() throws Exception {
		long start = System.nanoTime();

		HttpClient httpclient = new DefaultHttpClient();
		try {
			String url = "http://trieunt.plugins.banbe.net/like?current_href=http%3A%2F%2Ftrieunt.fosp.com%2Ffosp-with-session%2Findex.php%23__&_t=1317971885233&href=http%3A%2F%2Ftrieunt.fosp.com%2Ffosp-with-session%2Findex.php%23__&title=V%C3%AC%20sao%20%E1%BA%A3nh%20c%E1%BB%A7a%20b%E1%BA%A1n%20kh%C3%B4ng%20%C4%91%C6%B0%E1%BB%A3c%20%C4%91%C4%83ng%20tr%C3%AAn%20Couple3k%3F%20%7C%20iOne.net%20-%20Couple3K&layout=standard&font-family=tahoma&color=%23000&font-size=11px&img_src=&like_text=&description=test%20H%C3%A3y%20r%C3%A0%20so%C3%A1t%20nh%E1%BB%AFng%20l%E1%BB%97i%20sau%20%C4%91%C3%A2y%20xem%20b%E1%BA%A1n%20c%C3%B3%20m%E1%BA%AFc%20ph%E1%BA%A3i%20kh%C3%B4ng%20nh%C3%A9.&show_avatar=true";
			HttpGet httpget = new HttpGet(url);

			System.out.println("executing request " + httpget.getURI());

			// Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println("----------------------------------------");
			System.out.println(responseBody);
			System.out.println("----------------------------------------");

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		// do stuff
		long end = System.nanoTime();
		long miliseconds = (end - start) / 10000000;
		System.out.println(" \n miliseconds:" + miliseconds);
	}

	public final static void main(String[] args) throws Exception {
		
	}

}
