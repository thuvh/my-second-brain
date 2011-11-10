package org.brain2.test.vneappcrawler;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VnExpressParser {
	public static Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao) throws Exception {
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
		final String lead = article.getAbstractS();
		// System.out.println("BEGIN #####################");

		Elements contents = doc.select(".content");

		if (contents.size() > 0) {
			Element content = contents.get(0);

			Elements cpms_content = content.select("div[cpms_content=true]");

			if (cpms_content.size() > 0) {
				Element cpms = cpms_content.get(0);
				/**
				 * remove script links in content
				 */
				cpms.select("script").remove();

				/**
				 * split related link from lead
				 */
				Elements _related_links = null;

				if (lead != null) {
					String[] leadParts = lead.split("<BR>>", 2);
					String _abstract = Jsoup.parse(leadParts[0]).text();
					article.setAbstractS(_abstract);

					if (leadParts.length > 1) {
						_related_links = Jsoup.parse(leadParts[1]).select("a");
					}
				}

				/**
				 * remove title and lead from content
				 */
				cpms.select("h1.Title").remove();
				cpms.select("h2.Lead").remove();

				/**
				 * Extract content
				 */
				// Images
				Elements _images = cpms.select("img");

				if (_images.size() > 0) {
					cpms.select("img").parents().get(2).remove();
				}

				/**
				 * Related links
				 */

				/**
				 * Detect related link in article
				 */
				if (_related_links != null) {
					_related_links.addAll(cpms.select("a[class!=Normal]"));
				} else {
					_related_links = cpms.select("a[class!=Normal]");
				}

				/**
				 * Detect "Theo dong su kien"
				 */
				Elements tdskTopicTitle = content.select("a.TopicTitle");
				Elements tdskOther = content.select("a.Other");

				if (tdskTopicTitle.size() > 0) {
					_related_links.addAll(tdskTopicTitle);
					tdskTopicTitle.remove();
				}

				if (tdskOther.size() > 0) {
					_related_links.addAll(tdskOther);
					tdskOther.remove();
				}

				cpms.select("a[class!=Normal]").remove();

				for (Element p : cpms.select("p")) {
					p.html(Jsoup.parse(p.html()).text());
				}

				// System.out.println("Title: " + title);
				// System.out.println("Abstract: " + _abstract);
				// System.out.println("Content: " + cpms.html());
				article.setContent(cpms.text());
				// System.out.println("List images:");

				// for(Element image : _images)
				// {
				// System.out.println(theLink + " img source: " +
				// image.attr("src") + " with caption: " + image.attr("alt"));
				// }

				// System.out.println("List links:");

				// for(Element link : _related_links)
				// {
				// System.out.println("link source: " + link.attr("href"));
				// }

				/**
				 * Comment
				 */
				Elements _comments = null;
				Elements boxComments = content.select(".box-item");

				if (boxComments.size() > 0) {
					Element boxComment = boxComments.get(0);
					// Count pages
					int totalPages = boxComment.select("a.Paging").size() + 1;

					// Get comment of current page (1)
					_comments = boxComment.select(".comment_ct");

					if (totalPages > 1) {
						for (int p = 2; p <= totalPages; p++) {
							String commentPages = HttpClientUtil
									.executeGet(theLink + "?p=" + p);
							_comments.addAll(Jsoup.parse(commentPages).select(
									".comment_ct"));
						}
					}
				}

				if (_comments != null && _comments.size() > 0) {
					ResultSet dbComments = _vnExpressDao.getComment(article
							.getSharedURL());
					int i = 0;
					List<Comment> comments = new ArrayList<Comment>();
					while (dbComments.next()) {
						Comment comment = new Comment(
								dbComments.getString("ID"), article.getId(),
								dbComments.getString("Title"), _comments.get(i)
										.select(".Normal").html(), "0",
								dbComments.getString("Name"),
								dbComments.getString("Email"),
								dbComments.getInt("Status"),
								dbComments.getDate("PublishDate"),
								dbComments.getDate("Date"),
								dbComments.getDate("Modified"));
						comments.add(comment);
						i++;
					}
					article.setComments(comments);
				} else {
					// System.out.println("don't allow comment :(( --- " +
					// theLink);
				}
			} else {
				System.out.println("NO CMPS " + theLink);
			}
		}
		return article;
	}
}
