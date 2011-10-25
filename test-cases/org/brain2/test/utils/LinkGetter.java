package org.brain2.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkGetter {

	private Pattern htmltag;
	private Pattern link;
	private final String root;

	public LinkGetter(String root) {
		this.root = root;
		htmltag = Pattern.compile("<a\\b[^>]*href=\"[^>]*>");
		link = Pattern.compile("href=\"[^>]*\">");
	}

	public String loadHtmlPage(String url) {
		String html = "";
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new URL(url).openStream()));
			String s;
			StringBuilder builder = new StringBuilder();
			while ((s = bufferedReader.readLine()) != null) {
				builder.append(s);
			}
			html = builder.toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return html;
	}

	public Set<String> getLinks(String url) {
		Map<String, Boolean> linkSet = new HashMap<String, Boolean>();

		String html = loadHtmlPage(url);
		// System.out.println(html);

		Matcher tagmatch = htmltag.matcher(html);
		while (tagmatch.find()) {
			Matcher matcher = link.matcher(tagmatch.group());
			matcher.find();
			String link = "";
			try {
				link = matcher.group().replaceFirst("href=\"", "").replaceFirst("\">", "");
			} catch (java.lang.IllegalStateException e) {
				
			}

			if (valid(link)) {
//				int i = link.indexOf("\"") - 1;
//				if (i > 0) {
//					link = link.substring(0, i);
//				}
				String theLink = makeAbsolute(url, link);
				if (!linkSet.containsKey(theLink) && !theLink.isEmpty() ) {
					linkSet.put(theLink, true);
				}
			}
		}

		return linkSet.keySet();
	}

	private boolean valid(String s) {		
		if (s.matches("javascript:.*|mailto:.*|#.*")) {
			return false;
		}
		return true;
	}

	private String makeAbsolute(String url, String link) {
		if (link.matches("http://.*")) {
			return link;
		}
		if (link.matches("/.*") && url.matches(".*$[^/]")) {
			return url + "/" + link;
		}
		if (link.matches("[^/].*") && url.matches(".*[^/]")) {
			return url + "/" + link;
		}
		if (link.matches("/.*") && url.matches(".*[/]")) {
			return url + link;
		}
		if (link.matches("/.*") && url.matches(".*[^/]")) {
			return url + link;
		}
		System.err.println("Cannot make the link absolute. Url: " + url		+ " Link " + link);
		return "";
		//throw new RuntimeException("Cannot make the link absolute. Url: " + url		+ " Link " + link);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// String domain = "vnexpress.net";
		// String domain = "tantrieuf31.blogspot.com";
		String seedLink = "book.pdfchm.net/processing-a-programming-handbook-for-visual-designers-and-artists/9780262182621/";
		LinkGetter linkGetter = new LinkGetter("pdfchm.net");
		Set<String> links = linkGetter.getLinks("http://" + seedLink);
		for (String link : links) {
			System.out.println(link);
			Set<String> links2 = linkGetter.getLinks(link);
			for (String link2 : links2) {
				System.out.println("\t"+link2);
			}
		}
		
	}

}
