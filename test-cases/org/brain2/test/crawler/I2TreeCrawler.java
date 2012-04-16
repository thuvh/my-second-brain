package org.brain2.test.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.FileUtils;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class I2TreeCrawler {

	// static final String BASE_URL =
	// "http://tantrieuf31.byethost7.com/i2tree/index.php/mydata/post";
	static final String BASE_URL = "http://localhost/index.php/unit-tests/crawler_api/post";

	public static void main(String[] args) throws Exception {
		crawlingData("http://www.thongtintuyensinh.vn/Thong-tin-tuyen-sinh_C51_D1702.htm");
	}

	public static void postSampleStructuredData() throws Exception {
		JSONArray array = new JSONArray();
		array.put("a. Đi dự tiệc");
		array.put("b. Ở nhà và lướt web");
		array.put("c. Ghép hình, nghe nhạc");
		array.put("d. Đi xem phim");

		JSONObject obj = new JSONObject();
		obj.put("question",
				"1. Nếu có một buổi tối rảnh rỗi, bạn thích làm gì?");
		obj.put("options", array);

		Map<String, String> params = new HashMap<String, String>(2);
		params.put("json_data", obj.toString());

		String url = "http://localhost/index.php/unit-tests/crawler_api/post_structured_data";
		String rs = HttpClientUtil.executePost(url, params, "");
		System.out.println(rs);
	}

	public static void postSampleData() {
		Map<String, String> params = new HashMap<String, String>(2);
		params.put(
				"url",
				"http://lh6.googleusercontent.com/-_YOw8FvJAXY/TanAhrdWcpI/AAAAAAAABes/44V-f9K8Lv0/s600/2011-04-10_17-21-05_840_Ho%2520Chi%2520Minh%2520City.jpg");
		params.put(
				"content",
				"<img src='http://lh6.googleusercontent.com/-_YOw8FvJAXY/TanAhrdWcpI/AAAAAAAABes/44V-f9K8Lv0/s600/2011-04-10_17-21-05_840_Ho%2520Chi%2520Minh%2520City.jpg' />");
		String rs = HttpClientUtil.executePost(BASE_URL, params, "");
		System.out.println(rs);
	}
	
	public static void crawlingData(String rootUrl)  {		
		String html = HttpClientUtil.executeGet(rootUrl);
		Document doc = Jsoup.parse(html, HTTP.UTF_8);
		Elements nodes = doc.select("div[id=tabContent] a[href]");

		int poolSize = 10;
		ExecutorService executor = Executors.newFixedThreadPool(poolSize);
		for (Element node : nodes) {
			String href = node.attr("href");
			final String title = node.text();
			if( ! href.startsWith("http")){
				href = "http://www.thongtintuyensinh.vn/" + href;
			} 
			final String url =  href;
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					System.out.println(title+" = " +url);
					crawlingDataLink(url, title);					
				}
			});
		}
		executor.shutdown();
		while ( ! executor.isTerminated()) {}
		System.out.println("process links: " + nodes.size());
	}
	

	public static void crawlingDataLink(String url, String title)  {		
		String html = HttpClientUtil.executeGet(url);
		Document doc = Jsoup.parse(html, HTTP.UTF_8);
		Elements nodes = doc.select("div[id=tabContent]");

		if(nodes.size() > 0){
			String content = StringEscapeUtils.unescapeHtml4(nodes.get(0).html());
//			System.out.println(content);
//			FileUtils.writeStringToFile("Thong-tin-tuyen-sinh_C51_D826.htm",content);

			Map<String, String> params = new HashMap<String, String>();
			params.put("content", content);
			params.put("url", url);
			params.put("title", title);
			String rs = HttpClientUtil.executePost(BASE_URL, params, "");
			//System.out.println("###rs: \n" + rs );
		}
	}

}
