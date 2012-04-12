package org.brain2.test.crawler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.FileUtils;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class I2TreeCrawler {
	
	//static final String BASE_URL = "http://tantrieuf31.byethost7.com/i2tree/index.php/mydata/post";
	static final String BASE_URL = "http://localhost/i2tree/index.php/unit-tests/crawler_api/post";
	
	public static void main(String[] args) throws Exception {
		crawlingData();
	}

	public static void postSampleData() {		
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("url", "http://lh6.googleusercontent.com/-_YOw8FvJAXY/TanAhrdWcpI/AAAAAAAABes/44V-f9K8Lv0/s600/2011-04-10_17-21-05_840_Ho%2520Chi%2520Minh%2520City.jpg");
		params.put("content", "<img src='http://lh6.googleusercontent.com/-_YOw8FvJAXY/TanAhrdWcpI/AAAAAAAABes/44V-f9K8Lv0/s600/2011-04-10_17-21-05_840_Ho%2520Chi%2520Minh%2520City.jpg' />");
		String rs = HttpClientUtil.executePost(BASE_URL, params , "");
		System.out.println(rs);
	}
	
	public static void crawlingData() throws InterruptedException {
		String url = "http://www.thongtintuyensinh.vn/Truong-Dai-hoc-Cong-nghe-thong-tin-Gia-Dinh_C51_D673.htm";
		String html = HttpClientUtil.executeGet( url );
		Document doc = Jsoup.parse(html, HTTP.UTF_8);
		Elements nodes = doc.select("div[id=tabContent]");
		
		for (Element node : nodes) {	
			String content = StringEscapeUtils.unescapeHtml4(node.html());
			//System.out.println( content );
			FileUtils.writeStringToFile("Thong-tin-tuyen-sinh_C51_D826.htm", content );			
						
			Map<String, String> params = new HashMap<String, String>();
			params.put("content", content);
			params.put("url", url);
			String rs = HttpClientUtil.executePost(BASE_URL, params, "");
			System.out.println("###rs: \n" + rs );
			break;
		}
		Thread.sleep(2000);
		

	}

}
