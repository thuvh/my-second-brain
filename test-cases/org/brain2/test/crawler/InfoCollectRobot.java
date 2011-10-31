package org.brain2.test.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.brain2.ws.core.search.MetaDataUtil;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.l3s.boilerpipe.extractors.DefaultExtractor;

public class InfoCollectRobot {

	private final String root;
	private final Map<String, Boolean> vertices;
	private Queue<String> urlQueue;

	private final IndexWriter indexWriter;

	private static final String DISALLOW = "Disallow:";
	private int maxNumberUrls;
	private String urlRuleShouldNotMatch = "";
	private String urlRuleShouldMatch = "";
	private String mainContentNodeId = "";
	private String mainNavigationNodeId = "";

	public InfoCollectRobot(String root, int maxNumberUrls) {
		this.root = root;
		this.maxNumberUrls = maxNumberUrls;
		vertices = new HashMap<String, Boolean>(this.maxNumberUrls);
		try {
			indexWriter = MetaDataUtil.getIndexWriter();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Visit a link in Info Graph
	 * 
	 * @param link
	 * @return String html
	 */
	protected String visitingLink(String link) {
		vertices.put(link, true);
		return HttpClientUtil.executeGet(link);
	}

	/**
	 * Seed all href in page
	 * 
	 * @param theLink
	 */
	protected void seedLinks(final String theLink) {
		final String html = visitingLink(theLink);
		// System.out.println(html);

		System.out.println();
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
		String baseURL = "http://" + this.root;
		doc.setBaseUri(baseURL);

		final Elements linkNodes;
		if (mainNavigationNodeId.isEmpty()) {
			linkNodes = doc.select("a[href]");
		} else {
			linkNodes = doc.select(mainNavigationNodeId).select("a[href]");
		}

		for (Element linkNode : linkNodes) {
			String href = linkNode.attr("href");
			if (isValidLink(href)) {
				if (!href.startsWith("http")) {
					href = baseURL + href;
				}
				System.out.print("Found link: " + href);
				if (shouldAddToQueue(href)) {					
					if (!vertices.containsKey(href)) {
						vertices.put(href, false);
						urlQueue.add(href);
						System.out.println(" , PUSH to queue ");
					} else {
						System.out.println(" , SKIP to queue");
					}
				} else {
					System.out.println(" not matched rules");
				}
			}
		}

		Runnable thread = new Runnable() {

			@Override
			public void run() {
				System.out.println("index link: " + theLink);

				Elements metas = doc.select("meta");
				String descriptionTxt = "";
				String keywordsTxt = "";
				String robotsTxt = "";

				for (Element meta : metas) {
					String metaName = meta.attr("name");
					String metaContent = meta.attr("content");
					// System.out.println("meta name: " + metaName);

					if (metaName.equals("description")) {
						descriptionTxt = metaContent;
						System.out.println("descriptionTxt: " + descriptionTxt);
					} else if (metaName.equals("keywords")) {
						keywordsTxt = metaContent;
						// System.out.println("descriptionTxt: " + keywordsTxt);
					} else if (metaName.equals("robots")) {
						robotsTxt = metaContent;
						// System.out.println("robotsTxt: " + robotsTxt);
					}
				}

				Elements title = doc.select("title");
				String titleTxt = "";
				if (title.size() > 0) {
					titleTxt = title.get(0).text();
					System.out.println("titleTxt: " + titleTxt);
				}

				try {
					System.out.println(" BEGIN #####################");
					Elements contentNodes;
					if (mainContentNodeId.isEmpty()) {
						contentNodes = doc.select("body");
					} else {						
						contentNodes = doc.select(mainContentNodeId);						
//						System.out.println(contentNodes.html());					
						
					}

					String content = DefaultExtractor.INSTANCE.getText(contentNodes.html());
					System.out.println(content);
					org.apache.lucene.document.Document newDoc = MetaDataUtil
							.createDocumentForLink(theLink, titleTxt,
									descriptionTxt, keywordsTxt, content);
					indexWriter.addDocument(newDoc);
					indexWriter.commit();
					System.out.println(" END #####################");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(thread).start();
	}

	protected boolean shouldAddToQueue(String link) {
		boolean rs = false;
		try {
			URL url = new URL(link);
			rs = url.getHost().contains(root);
			if( ! urlRuleShouldNotMatch.isEmpty() ){
				rs = rs && !(link.matches(urlRuleShouldNotMatch));
			}
			if( ! urlRuleShouldMatch.isEmpty() ){
				rs = rs && (link.matches(urlRuleShouldMatch));
			}

		} catch (MalformedURLException e) {
		}
		return rs;
	}

	protected boolean isValidLink(String s) {
		if (s.matches("javascript:.*|mailto:.*|#.*")) {
			return false;
		}
		return true;
	}

	public void crawleNews(String seedUrl) throws InterruptedException,
			CorruptIndexException, IOException {
		urlQueue = new LinkedList<String>();

		this.seedLinks(seedUrl);
		while (!urlQueue.isEmpty() && urlQueue.size() < this.maxNumberUrls) {
			System.out.println("## urlQueue size : " + urlQueue.size());
			String link = urlQueue.remove();
			System.out.println("enqueue a link: " + link);
			if (!this.vertices.get(link)) {
				this.seedLinks(link);
				Thread.sleep(550);
			} else {
				System.out.println("## visited, skip : " + link);
			}
		}
		indexWriter.optimize();
	}

	public int getMaxNumberUrls() {
		return maxNumberUrls;
	}

	public void setMaxNumberUrls(int maxNumberUrls) {
		this.maxNumberUrls = maxNumberUrls;
	}

	public String getUrlRuleShouldNotMatch() {
		return urlRuleShouldNotMatch;
	}

	public void setUrlRuleShouldNotMatch(String urlRuleShouldNotMatch) {
		this.urlRuleShouldNotMatch = urlRuleShouldNotMatch;
	}

	public String getUrlRuleShouldMatch() {
		return urlRuleShouldMatch;
	}

	public void setUrlRuleShouldMatch(String urlRuleShouldMatch) {
		this.urlRuleShouldMatch = urlRuleShouldMatch;
	}

	public String getMainContentNodeId() {
		return mainContentNodeId;
	}

	public void setMainContentNodeId(String mainContentNodeId) {
		this.mainContentNodeId = mainContentNodeId;
	}

	public String getMainNavigationNodeId() {
		return mainNavigationNodeId;
	}

	public void setMainNavigationNodeId(String mainNavigationNodeId) {
		this.mainNavigationNodeId = mainNavigationNodeId;
	}

	public String getRoot() {
		return root;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		long start = System.nanoTime();

		
		test_asmarterplanet_com();

		long end = System.nanoTime();
		long miliseconds = (end - start) / 10000000;
		System.out.println(" \n === Test done === in miliseconds:" + miliseconds);
	}

	protected static void test_tantrieuf31_blogspot_com() throws Exception {
		int maxQueueSize = 10000;
		String domain = "tantrieuf31.blogspot.com";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);
		robot.setMainNavigationNodeId("#BlogArchive1_ArchiveList");
		robot.setUrlRuleShouldMatch(".*.html");
		robot.setUrlRuleShouldNotMatch(".*_archive.html");
		robot.crawleNews("http://tantrieuf31.blogspot.com/");
	}

	protected static void test_vnexpress_net() throws Exception {
		int maxQueueSize = 10000;
		String domain = "vnexpress.net";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);
		robot.setMainContentNodeId("#content .content-center");
		robot.setUrlRuleShouldMatch("http://vnexpress.net/gl/vi-tinh/.*");
		robot.crawleNews("http://vnexpress.net/gl/vi-tinh/");
	}
	
	protected static void test_asmarterplanet_com() throws Exception {
		int maxQueueSize = 10000;
		String domain = "asmarterplanet.com";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);
		robot.setMainContentNodeId("#content");
		robot.setUrlRuleShouldMatch("http://asmarterplanet.com/blog/2011/10/.*");
		robot.crawleNews("http://asmarterplanet.com/blog/2011/10/smarter-silhouettes-a-curriculum-of-analytics.html");
	}
	
	

}
