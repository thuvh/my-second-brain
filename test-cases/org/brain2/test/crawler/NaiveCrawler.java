package org.brain2.test.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaiveCrawler {
	public static String ROOT_DOMAIN = "";
	
	private static final String USER_AGENT = "User-agent:";

	private static final String DISALLOW = "Disallow:";

	public static final String REGEXP_HTTP = "<a href=\"http://(.)*\">";

	public static final String REGEXP_RELATIVE = "<a href=\"(.)*\">";

	private int maxNumberUrls;

	private long delayBetweenUrls;

	private int maxDepth;

	private Pattern regexpSearchPattern;

	private Pattern httpRegexp;

	private Pattern relativeRegexp;

	private Map<String, CrawlerUrl> visitedUrls = null;

	private Map<String, Collection<String>> sitePermissions = null;

	private Queue<CrawlerUrl> urlQueue = null;

	private BufferedWriter crawlOutput = null;

	private BufferedWriter crawlStatistics = null;

	private int numberItemsSaved = 0;
	
	private String robotTxtUrl = "";

	public NaiveCrawler(Queue<CrawlerUrl> urlQueue, int maxNumberUrls,
			int maxDepth, long delayBetweenUrls, String regexpSearchPattern)
			throws Exception {
		this.urlQueue = urlQueue;
		this.maxNumberUrls = maxNumberUrls;
		this.delayBetweenUrls = delayBetweenUrls;
		this.maxDepth = maxDepth;
		this.regexpSearchPattern = Pattern.compile(regexpSearchPattern);
		this.visitedUrls = new HashMap<String, CrawlerUrl>();
		this.sitePermissions = new HashMap<String, Collection<String>>();
		this.httpRegexp = Pattern.compile(REGEXP_HTTP);
		this.relativeRegexp = Pattern.compile(REGEXP_RELATIVE);
		crawlOutput = new BufferedWriter(new FileWriter("crawl.txt"));
		crawlStatistics = new BufferedWriter(new FileWriter("crawlStatistics.txt"));
	}
	
	public void setRobotTxtUrl(String robotTxtUrl) {
		this.robotTxtUrl = robotTxtUrl;
	}

	public void crawl() throws Exception {
		while (continueCrawling()) {
			CrawlerUrl url = getNextUrl();
			if (url != null) {
				if(url.getURL().getHost().contains(ROOT_DOMAIN)){
					printCrawlInfo();
					String content = getContent(url);
					if (isContentRelevant(content, this.regexpSearchPattern)) {
						//saveContent(url, content);
						Collection<String> urlStrings = extractUrls(content, url);
						addUrlsToUrlQueue(url, urlStrings);
					} else {
						System.out.println(url + " is not relevant ignoring ...");
					}
					Thread.sleep(this.delayBetweenUrls);
				}
			}
		}
		closeOutputStream();
	}

	private boolean continueCrawling() {
		return ((!urlQueue.isEmpty()) && (getNumberOfUrlsVisited() < this.maxNumberUrls));
	}

	private CrawlerUrl getNextUrl() {
		CrawlerUrl nextUrl = null;
		while ((nextUrl == null) && (!urlQueue.isEmpty())) {
			CrawlerUrl crawlerUrl = this.urlQueue.remove();
			if (doWeHavePermissionToVisit(crawlerUrl)
					&& (!isUrlAlreadyVisited(crawlerUrl))
					&& isDepthAcceptable(crawlerUrl)) {
				nextUrl = crawlerUrl;
				// System.out.println("Next url to be visited is " + nextUrl);
			}
		}
		return nextUrl;
	}

	private void printCrawlInfo() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("Queue length = ").append(this.urlQueue.size())
				.append(" visited urls=").append(getNumberOfUrlsVisited())
				.append(" site permissions=")
				.append(this.sitePermissions.size());
		crawlStatistics.append("" + getNumberOfUrlsVisited())
				.append("," + numberItemsSaved)
				.append("," + this.urlQueue.size())
				.append("," + this.sitePermissions.size() + "\n");
		crawlStatistics.flush();
		System.out.println(sb.toString());
	}

	private int getNumberOfUrlsVisited() {
		return this.visitedUrls.size();
	}

	private void closeOutputStream() throws Exception {
		crawlOutput.flush();
		crawlOutput.close();
		crawlStatistics.flush();
		crawlStatistics.close();
	}

	private boolean isDepthAcceptable(CrawlerUrl crawlerUrl) {
		return crawlerUrl.getDepth() <= this.maxDepth;
	}

	private boolean isUrlAlreadyVisited(CrawlerUrl crawlerUrl) {
		if ((crawlerUrl.isVisited())
				|| (this.visitedUrls.containsKey(crawlerUrl.getUrlString()))) {
			return true;
		}
		return false;
	}

	// //////////////////////////////////////////////////////////

	public boolean doWeHavePermissionToVisit(CrawlerUrl crawlerUrl) {
		if (crawlerUrl == null) {
			return false;
		}
		if (!crawlerUrl.isCheckedForPermission()) {
			crawlerUrl.setAllowedToVisit(computePermissionForVisting(crawlerUrl));
		}
		// We need to check
		return crawlerUrl.isAllowedToVisit();
	}

	private boolean computePermissionForVisting(CrawlerUrl crawlerUrl) {
		URL url = crawlerUrl.getURL();
		boolean retValue = (url != null);
		if (retValue) {
			String host = url.getHost();
			Collection<String> disallowedPaths = this.sitePermissions.get(host);
			if (disallowedPaths == null) {
				disallowedPaths = parseRobotsTxtFileToGetDisallowedPaths(host);
			}
			// We iterate over all disallowed paths since we are looking at
			// starts with
			String path = url.getPath();
			for (String disallowedPath : disallowedPaths) {
				if (path.contains(disallowedPath)) {
					retValue = false;
				}
			}
		}
		return retValue;
	}

	private Collection<String> parseRobotsTxtFileToGetDisallowedPaths(String host) {
		// read the robots.txt file
		String robotFilePath;
		if(! this.robotTxtUrl.isEmpty()){
			robotFilePath = getContent(this.robotTxtUrl);
		} else {
			robotFilePath = getContent("http://" + host + "/robots.txt");
		}
		Collection<String> disallowedPaths = new ArrayList<String>();
		if (robotFilePath != null) {
			Pattern p = Pattern.compile(USER_AGENT);
			String[] permissionSets = p.split(robotFilePath);
			String permissionString = "";
			for (String permission : permissionSets) {
				if (permission.trim().startsWith("*")) {
					permissionString = permission.substring(1);
				}
			}
			p = Pattern.compile(DISALLOW);
			String[] items = p.split(permissionString);
			for (String s : items) {
				disallowedPaths.add(s.trim());
				// System.out.println(s.trim());
			}
		}
		this.sitePermissions.put(host, disallowedPaths);
		return disallowedPaths;
	}

	private String getContent(String urlString) {
		return getContent(new CrawlerUrl(urlString, 0));
	}

	private String getContent(CrawlerUrl url) {
		// FIXME
		String html = "";
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(
							new URL(url.getUrlString()).openStream()));
			String s;
			StringBuilder builder = new StringBuilder();
			while ((s = bufferedReader.readLine()) != null) {
				builder.append(s);
			}
			html = builder.toString();
			markUrlAsVisited(url);
		} catch (MalformedURLException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return html;
	}

	private static String readContentsFromStream(Reader input)
			throws IOException {
		BufferedReader bufferedReader = null;
		if (input instanceof BufferedReader) {
			bufferedReader = (BufferedReader) input;
		} else {
			bufferedReader = new BufferedReader(input);
		}
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[4 * 1024];
		int charsRead;
		while ((charsRead = bufferedReader.read(buffer)) != -1) {
			sb.append(buffer, 0, charsRead);
		}
		return sb.toString();
	}

	private void markUrlAsVisited(CrawlerUrl url) {
		this.visitedUrls.put(url.getUrlString(), url);
		url.setIsVisited();
	}

	public static boolean isContentRelevant(String content, Pattern regexpPattern) {
		boolean retValue = false;
		if (content != null) {
			Matcher m = regexpPattern.matcher(content.toLowerCase());
			retValue = m.find();
			//TODO
			return true;
		}
		return retValue;
	}

	private void saveContent(CrawlerUrl url, String content) throws Exception {
		this.crawlOutput.append(url.getUrlString()).append("\n");
		numberItemsSaved++;
	}

	public List<String> extractUrls(String text, CrawlerUrl crawlerUrl) {
		Map<String, String> urlMap = new HashMap<String, String>();
		extractHttpUrls(urlMap, text);
		extractRelativeUrls(urlMap, text, crawlerUrl);
		return new ArrayList<String>(urlMap.keySet());
	}

	private void extractHttpUrls(Map<String, String> urlMap, String text) {
		Matcher m = httpRegexp.matcher(text);
		while (m.find()) {
			String url = m.group();
			String[] terms = url.split("a href=\"");
			for (String term : terms) {
				// System.out.println("Term = " + term);
				if (term.startsWith("http")) {
					int index = term.indexOf("\"");
					if (index > 0) {
						term = term.substring(0, index);
					}
					urlMap.put(term, term);
					System.out.println("Hyperlink: " + term);
				}
			}
		}
	}

	private void extractRelativeUrls(Map<String, String> urlMap, String text,
			CrawlerUrl crawlerUrl) {
		Matcher m = relativeRegexp.matcher(text);
		URL textURL = crawlerUrl.getURL();
		String host = textURL.getHost();
		while (m.find()) {
			String url = m.group();
			String[] terms = url.split("a href=\"");
			for (String term : terms) {
				if (term.startsWith("/")) {
					int index = term.indexOf("\"");
					if (index > 0) {
						term = term.substring(0, index);
					}
					String s = "http://" + host + term;
					if(urlMap.containsKey(s)){
					urlMap.put(s, s);
					System.out.println("Relative url: " + s);
					}
				}
			}
		}

	}

	private void addUrlsToUrlQueue(CrawlerUrl url, Collection<String> urlStrings) {
		int depth = url.getDepth() + 1;
		for (String urlString : urlStrings) {
			if (!this.visitedUrls.containsKey(urlString)) {
				this.urlQueue.add(new CrawlerUrl(urlString, depth));
			}
		}
	}

	public void testRegExp(String regex, String text) {
		boolean result = Pattern.matches(regex, text);
		System.out.println(text + " matches " + regex + " =" + result);
		Pattern p = Pattern.compile(regex);
		String[] items = p.split(text);
		for (String s : items) {
			System.out.println(s.trim());
		}
	}

	public static void main(String[] args) {
		try {
			// String url = "http://www.manning.com" ;
			// String url = "http://www.amazon.com" ;
			// String url = "http://www.wikipedia.org" ;
			Queue<CrawlerUrl> urlQueue = new LinkedList<CrawlerUrl>();
//			String url = "http://en.wikipedia.org/wiki/Collective_intelligence";
//			String regexp = "collective.*intelligence";
			
			//String url = "http://book.pdfchm.net/processing-a-programming-handbook-for-visual-designers-and-artists/9780262182621/";
			String url = "http://vnexpress.net";
			String regexp = "";
			urlQueue.add(new CrawlerUrl(url, 0));
			NaiveCrawler crawler = new NaiveCrawler(urlQueue, 10000000, 10000, 800L, regexp);
			NaiveCrawler.ROOT_DOMAIN = "vnexpress.net";
			//crawler.setRobotTxtUrl("http://vnexpress.net/robots.txt");
			// boolean allowCrawl = crawler.areWeAllowedToVisit(url);
			// System.out.println("Allowed to crawl: " + url + " " +
			// allowCrawl);
			crawler.crawl();
			// crawler.testRegExp(regexp, "http://collective+intelligenceadf");
		} catch (Throwable t) {
			System.out.println(t.toString());
			t.printStackTrace();
		}
	}
}
