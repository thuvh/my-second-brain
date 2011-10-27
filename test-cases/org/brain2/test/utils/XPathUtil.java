package org.brain2.test.utils;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class XPathUtil {
	public static void print(Node node, String indent) {
		// System.out.println(indent + node.getClass().getName());
		Node child = node.getFirstChild();
		while (child != null) {
			print(child, indent + " ");
			System.out.println(child.getTextContent());
			child = child.getNextSibling();
		}
	}
	
	private static boolean valid(String s) {		
		if (s.matches("javascript:.*|mailto:.*|#.*")) {
			return false;
		}
		return true;
	}

	public static void main(String[] args)  {

		String url = "http://vnexpress.net/gl/vi-tinh/san-pham-moi/2011/10/dien-thoai-la-voi-man-hinh-uon-cong-cua-nokia/";
		String html = LinkGetter.loadHtmlPage(url);
		System.out.println(html);

		Document doc = Jsoup.parse(html);
		doc.setBaseUri("http://vnexpress.net");
		
		Elements feedDateNodes = doc.select("div.TurnTopPage");
		System.out.println(feedDateNodes.size());
		for (Element feedDateNode : feedDateNodes) {
			System.out.println(feedDateNode.attr("class"));
			System.out.println(feedDateNode.text());
		}
		
		Elements links = doc.select("a[href]");// a with href		
		for (Element link : links) {
			String href = link.attr("href");
			if(valid(href)){				
				if(!href.startsWith("http")){
					href = "http://vnexpress.net" + href;					
				}
				System.out.println(href);
				try {
					URL urlObj = new URL(href);
					System.out.println(urlObj.getHost());
				} catch (Exception e) {	}
			}
		}
		
		Elements contents = doc.select("div");// a with href		
		for (Element content : contents) {
			System.out.println(content.html());
		}
		try {		

			// This can also be done in one line:
			System.out.println(ArticleExtractor.INSTANCE.getText(html));
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			final Process p = new ProcessBuilder("java", "-jar", "D:/Researchs/JsOptimizer/dist/JsOptimizer.jar").start();
			
			new Thread(new Runnable() {public void run() {
				  try {
					IOUtils.copy(p.getInputStream(), System.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				} } ).start();
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return;
		
//		try {
//			DOMFragmentParser parser = new DOMFragmentParser();
//			HTMLDocument document = new HTMLDocumentImpl();
//
//			DocumentFragment fragment = document.createDocumentFragment();
//			parser.parse(url, fragment);			
//			print(fragment, "");
//			
//			
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}
}
