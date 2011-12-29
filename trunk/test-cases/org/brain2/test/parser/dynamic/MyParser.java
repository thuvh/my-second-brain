package org.brain2.test.parser.dynamic;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;





public class MyParser {
	public String doParsing(String url){
		String html = HttpClientUtil.executeGet(url);
		Document doc = Jsoup.parse(html, HTTP.UTF_8);
		Elements contents = doc.select(".content");
		if (contents.size() > 0) {
			Element content = contents.get(0);
			return content.text();
		}
		return "";
	}
}
