package org.brain2.test.utils;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

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

	public static void main(String[] args) {

		String url = "http://vnexpress.net/gl/vi-tinh/san-pham-moi/2011/10/dien-thoai-la-voi-man-hinh-uon-cong-cua-nokia/";
		String html = HttpClientUtil.executeGet(url);
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
			if (valid(href)) {
				if (!href.startsWith("http")) {
					href = "http://vnexpress.net" + href;
				}
				System.out.println(href);
				try {
					URL urlObj = new URL(href);
					System.out.println(urlObj.getHost());
				} catch (Exception e) {
				}
			}
		}

		Elements contents = doc.select("div");// a with href
		for (Element content : contents) {
			System.out.println(content.html());
		}
		try {

			// This can also be done in one line:
			System.out.println(ArticleExtractor.INSTANCE.getText(html));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// final Process p = new ProcessBuilder("java", "-jar",
			// "D:/Researchs/JsOptimizer/dist/JsOptimizer.jar").start();
			Logger.getLogger("com.mongodb").setLevel(Level.OFF); 
			boolean ok = false;
			try {
				Mongo m = new Mongo();
				DB dbCrawler = m.connect(new DBAddress("127.0.0.1", "crawler"));
				
				DBCollection coll = dbCrawler.getCollection("testCollection");
				BasicDBObject basicDBObject = new BasicDBObject();

				basicDBObject.put("name", "MongoDB");
				basicDBObject.put("type", "database");
				basicDBObject.put("count", 1);

		        BasicDBObject info = new BasicDBObject();

		        info.put("x", 203);
		        info.put("y", 102);

		        basicDBObject.put("info", info);

		        coll.insert(basicDBObject);
				if (!m.isLocked()) {
					ok = true;		

					System.out.println("mongod has started");
					Thread.sleep(2000);
					DB db = m.getDB("admin");
					System.out.println("shutdown Mongo");
					db.command("shutdown", 1);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			}
			//System.exit(1);

			if (!ok) {
				final Process p = new ProcessBuilder(
						"D:/Researchs/mongodb-win32-i386-2.0.1/bin/mongod",
						"--dbpath",
						"D:/Researchs/mongodb-win32-i386-2.0.1/data/db")
						.start();

				new Thread(new Runnable() {
					public void run() {
						try {
							IOUtils.copy(p.getInputStream(), System.out);
						} catch (IOException e) {							
						}
					}
				}).start();
			} else {				
				System.out.println("mongod has started");
				int c = 0; 
				while (ok) {
					Thread.sleep(2000);
					System.out.println("working ...");
					c++;
					if(c >2){
						System.out.println("Bye...");
						System.exit(1);
					}					
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Thread hook = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("exit ....");
			}
		});
		Runtime.getRuntime().addShutdownHook(hook);
	}
}
