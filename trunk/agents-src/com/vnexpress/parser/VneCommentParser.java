package com.vnexpress.parser;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VneCommentParser {
	
	
	
	
	
	public static void main(String[] args) {
		String url = "http://vnexpress.net/comment/2011/11/3bb6368a";
		String html = HttpClientUtil.executeGet(url);
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
		
		Elements contents = doc.select(".content");
		String commentContent = "";
		if (contents.size() > 0) {
			Element content = contents.get(0);

			Elements cpms_content = content.select("div[cpms_content=true]");
			if (cpms_content.size() > 0) {
				Element cpms = cpms_content.get(0);
				cpms.select("script").remove();
				Elements nodes = cpms.select(".Normal");
				if (nodes.size() > 1) {
					commentContent = nodes.get(0).text();
				}
			}
		}
		System.out.println(commentContent);
	}
}
