package org.brain2.test.utils;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.l3s.boilerpipe.extractors.DefaultExtractor;

public class LinkGetter {

	private final String root;
	private final Map<String, Boolean> vertices;
	private final Queue<String> urlQueue;

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.106 Safari/535.2";
	HttpClient httpclient = new DefaultHttpClient();

	private static final String DISALLOW = "Disallow:";

	private int maxNumberUrls;

	private final IndexWriter indexWriter;

	public LinkGetter(String root, int maxNumberUrls)  {
		this.root = root;
		this.maxNumberUrls = maxNumberUrls;
		vertices = new HashMap<String, Boolean>(this.maxNumberUrls);
		urlQueue = new LinkedList<String>();
		try {
			indexWriter = MetaDataUtil.getIndexWriter();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} 
	}

	public String visitingLink(String link) {
		String html = "";
		try {
			HttpGet httpget = new HttpGet(link);
			httpget.setHeader("User-Agent", USER_AGENT);
			httpget.setHeader("Accept-Charset", "utf-8");
			httpget.setHeader("Cache-Control", "no-cache");

			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			HttpResponse response = httpclient.execute(httpget);

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					html = EntityUtils.toString(entity, HTTP.UTF_8);
					//System.out.println(html);
				}
			}			
		} catch (Exception e) {

		}
		vertices.put(link, true);
		return html;
	}

	public Set<String> getLinks(final String url) {

		final String html = visitingLink(url);
		//System.out.println(html);

		
		System.out.println();
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
		String baseURL = "http://" + this.root;
		doc.setBaseUri(baseURL);

		Elements links = doc.select("#BlogArchive1_ArchiveList a[href]");// a with href
		for (Element link : links) {
			String href = link.attr("href");
			if (isValidLink(href)) {
				if (!href.startsWith("http")) {
					href = baseURL + href;
				}
				// System.out.println("Found link: "+href);
				if (!vertices.containsKey(href)) {
					vertices.put(href, false);
					System.out.println("Push to queue link: " + href);
				}
			}
		}		

		Runnable thread = new Runnable() {

			@Override
			public void run() {
				System.out.println("index link: " + url);

				Elements metas = doc.select("meta");
				String descriptionTxt = "";
				String keywordsTxt = "";
				String robotsTxt = "";
				
				for (Element meta : metas) {
					String metaName = meta.attr("name");
					String metaContent = meta.attr("content");
					System.out.println("meta name: " + metaName);					
					
					if(metaName.equals("description")){
						descriptionTxt = metaContent;
						System.out.println("descriptionTxt: " + descriptionTxt);	
					} else if(metaName.equals("keywords")){
						keywordsTxt = metaContent;
						System.out.println("descriptionTxt: " + keywordsTxt);	
					} else if(metaName.equals("robots")){
						robotsTxt = metaContent;
						System.out.println("robotsTxt: " + robotsTxt);	
					}	
				}

				Elements title = doc.select("title");
				String titleTxt = "";
				if (title.size() > 0) {
					titleTxt = title.get(0).text();
					System.out.println("titleTxt: " + titleTxt);
				}
				
				try {
					String content = DefaultExtractor.INSTANCE.getText(html);			
					System.out.println(content);
//					org.apache.lucene.document.Document newDoc = MetaDataUtil.createDocumentForLink(url, titleTxt, descriptionTxt, keywordsTxt, content);
//					indexWriter.addDocument(newDoc);	
//					indexWriter.commit();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(thread).start();

		return vertices.keySet();
	}

	private boolean isValidLink(String s) {
		if (s.matches("javascript:.*|mailto:.*|#.*")) {
			return false;
		}
		try {
			URL url = new URL(s);
			return url.getHost().contains(root);
		} catch (MalformedURLException e) {
			return false;
		}		
	}
	

	public void crawleNews() throws InterruptedException, CorruptIndexException, IOException {
		Set<String> links = this.getLinks("http://" + this.root);
		urlQueue.addAll(links);
		while (!urlQueue.isEmpty() && urlQueue.size() < this.maxNumberUrls) {
			String link = urlQueue.remove();
			System.out.println("enqueue : " + link);
			if (!this.vertices.get(link)) {
				Set<String> links2 = this.getLinks(link);
				urlQueue.addAll(links2);
				Thread.sleep(750);
				System.out.println("## urlQueue size : " + urlQueue.size());
			} else {
				System.out.println("## visited, skip : " + link);
			}
		}
		indexWriter.optimize();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//String domain = "vnexpress.net";
		int maxQueueSize = 10000;
		String domain = "tantrieuf31.blogspot.com";
		// String seedLink =
		// "book.pdfchm.net/processing-a-programming-handbook-for-visual-designers-and-artists/9780262182621/";

		long start = System.nanoTime();

		LinkGetter linkGetter = new LinkGetter(domain, maxQueueSize);
		linkGetter.crawleNews();

		long end = System.nanoTime();
		long miliseconds = (end - start) / 10000000;
		System.out.println(" \n miliseconds:" + miliseconds);
	}

}
