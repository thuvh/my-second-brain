package org.brain2.test.oauth;

import java.util.Iterator;

import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class FptIdTest {
	private static OAuthProvider provider;
	private static CommonsHttpOAuthConsumer consumer;

	private static String accessToken = "0bd47489d18fc4e9496f4872fe83e505";
	private static String secretToken = "21f57e40654114d4ae23b7f16b424c81";

	private static String CONSUMER_KEY = "a324957217164fd1d76b4b60d037abec";
	private static String CONSUMER_SECRET = "de85679251ab585ab25e72704d43d361";
	public static String CALLBACK_URL = "myapp://oauth";

	public static void main(String[] args) {		
		String log = "";
		HttpClient client = new DefaultHttpClient();
		try {
			consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
			provider = new CommonsHttpOAuthProvider("http://api.banbe.net/oauth/requesttoken",
												"http://api.banbe.net/oauth/accesstoken",
												"http://id.fpt.net/mobile");
			String authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
			System.out.println("authUrl: " + authUrl);
			
			consumer.setTokenWithSecret(accessToken, secretToken);
					
			
			HttpResponse httpResponse;
			HttpGet httpget = new HttpGet("http://api.banbe.net/user/account");
			httpget.addHeader("Accept-Charset", "UTF-8");
			httpget.addHeader("User-Agent", "FOSP API library Java version 1.0");
			consumer.sign(httpget);
			Header[] authorization = httpget.getHeaders("Authorization");
			for (int i = 0; i < authorization.length; i++) {
				System.out.println("Authorization: " + authorization[i]);
			}

			httpResponse = client.execute(httpget);
			int responseCode = httpResponse.getStatusLine().getStatusCode();

			if (responseCode == 200) {
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					String json = EntityUtils.toString(entity);

					System.out.println("json: " + json);
					JSONObject response = JsonUtil.parseResponse(json);
					if (response.getInt("error") == 0) {
						JSONObject result = JsonUtil
								.parseResponseResult(response);
						Iterator<String> keys = result.keys();
						String userid = null;
						while (keys.hasNext()) {
							userid = keys.next();
							log = log + "\n userid: " + userid;
							break;
						}
						if (userid != null) {
							JSONObject userObj = result.getJSONObject(userid);

							log = log + "\n fullname: "
									+ userObj.getString("fullname");
							log = log + "\n email: "
									+ userObj.getString("email");

							String avatar = userObj.getString("avatar")
									.replace("_50x50.jpg", "_200x200.jpg");
							log = log + "\n avatar: " + avatar;
						}

					} else {
						log = log + "\n error:" + response.get("error") + " "
								+ response.get("errorDescription");
					}
				}
			} else {
				log = log + "\n responseCode:" + responseCode;
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
