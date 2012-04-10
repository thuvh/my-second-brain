package org.brain2.test.crawler;

import java.util.HashMap;
import java.util.Map;

import org.brain2.ws.core.utils.HttpClientUtil;

public class I2TreeCrawler {

	public static void main(String[] args) {
		String url = "http://tantrieuf31.byethost7.com/i2tree/index.php/unit-tests/crawler_api/post";
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("url", "http://lh6.googleusercontent.com/-_YOw8FvJAXY/TanAhrdWcpI/AAAAAAAABes/44V-f9K8Lv0/s600/2011-04-10_17-21-05_840_Ho%2520Chi%2520Minh%2520City.jpg");
		params.put("content", "<img src='http://lh6.googleusercontent.com/-_YOw8FvJAXY/TanAhrdWcpI/AAAAAAAABes/44V-f9K8Lv0/s600/2011-04-10_17-21-05_840_Ho%2520Chi%2520Minh%2520City.jpg' />");
		String rs = HttpClientUtil.executePost(url, params , "");
		System.out.println(rs);
	}
}
