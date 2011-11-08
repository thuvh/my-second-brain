package org.brain2.test.vneappcrawler;

import java.sql.DriverManager;

import java.sql.Connection;

import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


public class VneCrawler {
//	public static void main(String[] args) {
//		try {
//			Class.forName("com.mysql.jdbc.Driver");
//			String database = "jdbc:mysql://10.254.53.216";
//			String user = "vnemobile";
//			String password = "vnemobile@123";
//			Connection conn = DriverManager.getConnection(database, user, password);
//			Statement state = conn.
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public static void main(String[] args) {
		try {
			final String theLink = "http://vnexpress.net/gl/phap-luat/2011/11/ke-tham-nhung-doi-pho-tinh-vi-voi-viec-bi-to-cao/";
			

			
			Runnable thread = new Runnable() {

				@Override
				public void run() {
					System.out.println("\n ==> theLink: " + theLink);
					final String html = HttpClientUtil.executeGet(theLink);					//				
					final Document doc = Jsoup.parse(html, HTTP.UTF_8);
					final String baseURL = "http://vnexpress.net";										

					try {
						//System.out.println(" BEGIN #####################");
						Elements contentNode;
						contentNode = doc.select("#content");
						
						Elements cpms_content = contentNode.select("div[cpms_content=true]");						
						
						if(cpms_content.size() > 0)
						{							
							Element cpms = cpms_content.get(0);							
							
							//Get title
							Element title = cpms.select("h1.Title").get(0);
							
							//Get lead
							Element lead = cpms.select("h2.Lead").get(0);
							
							cpms.select("h1.Title").remove();
							cpms.select("h2.Lead").remove();																	
							
							/**
							 * Extract content
							 */
							//Images
							Elements images = cpms.select("img");
							cpms.select("img").remove();																									
							
							//Related links
							Elements relatedLinks = lead.select("a");														
							relatedLinks.addAll(cpms.select("a[class!=Normal]"));
							lead.select("a").remove();
							cpms.select("a[class!=Normal]").remove();
							
//							for(Element link : cpms.select("a[class=Normal]"))
//							{																
//								link.replaceWith(DataNode.createFromEncoded(link.html(), ""));
//							}							
							
							for(Element p : cpms.select("p"))
							{
								p.html(Jsoup.parse(p.html()).text());
							}								
							
							System.out.println("Title: " + title.text());
							System.out.println("Lead: " + lead.text());
							System.out.println("Content: " + cpms.html());
							System.out.println("List images:");
							for(Element image : images)
							{								
								System.out.println("img source: " + image.attr("src"));
							}
							System.out.println("List links:");
							for(Element link : relatedLinks)
							{
								System.out.println("link source: " + link.attr("href"));
							}
							System.exit(1);
							
//							//get images in content
//							Elements imgs = contentNode.select("img[src]");
//							for (Element img : imgs) {
//								String src = img.attr("src");
//								//FIXME
//								if(src.startsWith("/Files/Subject/")){
//									System.out.println(" #img[src] = " + baseURL + src);
//								}
//							}
//							
//							Elements comments = contentNode.select("div.comment_ct");
//							for (Element comment : comments) {
//								String commentText = comment.html();
//								//FIXME
//								System.out.println(commentText + "\n");
//							}		
//							
//							final Elements linkNodes = contentNode.select("a[href]");
//							for (Element linkNode : linkNodes) {							
//								String href = linkNode.attr("href");
//								if(href.endsWith("#aComment")){
//									System.out.println(" #a[href] = " + href);
//								}
//							}
//							
//							final Elements cpms_content = contentNode.select("div[cpms_content]");
//							System.out.println(" #cpms_content = " + cpms_content.size());		
//							for (Element node : cpms_content) {						
//								String text = node.text();							
//								System.out.println(" #cpms_content = " + text);							
//							}
						}
						
						
						
						

//						String content = DefaultExtractor.INSTANCE.getText(contentNode.html());
//						System.out.println(content);
//						org.apache.lucene.document.Document newDoc = MetaDataUtil
//								.createDocumentForLink(theLink, titleTxt,
//										descriptionTxt, keywordsTxt, content);
//						indexWriter.addDocument(newDoc);
//						indexWriter.commit();
						//System.out.println(" END #####################");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			new Thread(thread).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Runnable httpGetArticle(final String theLink, final String title, final String lead){
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				//System.out.println("\n ==> theLink: " + theLink);
				final String html = HttpClientUtil.executeGet(theLink);					//				
				final Document doc = Jsoup.parse(html, HTTP.UTF_8);				
				final String baseURL = "http://vnexpress.net";										

				try {
					System.out.println("BEGIN #####################");					
					Element content = doc.select(".content").get(0);
					
					Elements cpms_content = content.select("div[cpms_content=true]");						
					
					if(cpms_content.size() > 0)
					{							
						Element cpms = cpms_content.get(0);							
						cpms.select("script").remove();												
						
						String[] leadParts = lead.split("<BR>>",2);
						String _abstract = Jsoup.parse(leadParts[0]).text();						
						
						Elements _related_links = null;
						
						if(leadParts.length > 1)
						{							
							_related_links = Jsoup.parse(leadParts[1]).select("a");												
						}												
																
						cpms.select("h1.Title").remove();
						cpms.select("h2.Lead").remove();																	
						
						/**
						 * Extract content
						 */
						//Images
						Elements _images = cpms.select("img");
						
						if(_images.size() > 0)
						{
							cpms.select("img").parents().get(2).remove();
						}																														
						
						/**
						 * Related links		
						 */
						
						/**
						 * Detect related link in article
						 */
						if(_related_links != null)
						{
							_related_links.addAll(cpms.select("a[class!=Normal]"));
						}
						else 
						{
							_related_links = cpms.select("a[class!=Normal]");
						}
						
						/**
						 * Detect "Theo dong su kien"
						 */
						Elements tdskTopicTitle = content.select("a.TopicTitle");
						Elements tdskOther = content.select("a.Other");
						
						if(tdskTopicTitle.size() > 0)
						{
							_related_links.addAll(tdskTopicTitle);
							tdskTopicTitle.remove();
						}					
						
						if(tdskOther.size() > 0)
						{
							_related_links.addAll(tdskOther);
							tdskOther.remove();
						}
											
													
						cpms.select("a[class!=Normal]").remove();												
						
						for(Element p : cpms.select("p"))
						{
							p.html(Jsoup.parse(p.html()).text());
						}								
						
						System.out.println("Title: " + title);
						System.out.println("Abstract: " + _abstract);
						System.out.println("Content: " + cpms.html());
						System.out.println("List images:");
						
						for(Element image : _images)
						{								
							System.out.println(theLink + " img source: " + image.attr("src") + " with caption: " + image.attr("alt"));
						}
						
						System.out.println("List links:");
						
						for(Element link : _related_links)
						{
							System.out.println("link source: " + link.attr("href"));
						}
						
						/**
						 * Comment
						 */
						Elements _comments = null;
						Elements boxComments = content.select(".box-item"); 
						
						if(boxComments.size() > 0)
						{
							Element boxComment = boxComments.get(0);							
							//Count pages
							int totalPages = boxComment.select("a.Paging").size() + 1;
													
							//Get comment of current page (1)
							_comments = boxComment.select(".comment_ct");
							
							if(totalPages > 1)
							{
								for(int p=2; p<=totalPages; p++)
								{
									String commentPages = HttpClientUtil.executeGet(theLink + "?p=" + p);									
									_comments.addAll(Jsoup.parse(commentPages).select(".comment_ct")) ; 
								}
							}
						}
						
						if(_comments != null)
						{
							System.out.println("total of comments: " + _comments.size() + " --- " + theLink);
						}
						else
						{
							System.out.println("don't allow comment :(( --- " + theLink);
						}
						
						
//						//get images in content
//						Elements imgs = contentNode.select("img[src]");
//						for (Element img : imgs) {
//							String src = img.attr("src");
//							//FIXME
//							if(src.startsWith("/Files/Subject/")){
//								System.out.println(" #img[src] = " + baseURL + src);
//							}
//						}
//						
//						Elements comments = contentNode.select("div.comment_ct");
//						for (Element comment : comments) {
//							String commentText = comment.html();
//							//FIXME
//							System.out.println(commentText + "\n");
//						}		
//						
//						final Elements linkNodes = contentNode.select("a[href]");
//						for (Element linkNode : linkNodes) {							
//							String href = linkNode.attr("href");
//							if(href.endsWith("#aComment")){
//								System.out.println(" #a[href] = " + href);
//							}
//						}
//						
//						final Elements cpms_content = contentNode.select("div[cpms_content]");
//						System.out.println(" #cpms_content = " + cpms_content.size());		
//						for (Element node : cpms_content) {						
//							String text = node.text();							
//							System.out.println(" #cpms_content = " + text);							
//						}
					}
//					String content = DefaultExtractor.INSTANCE.getText(contentNode.html());
//					System.out.println(content);
//					org.apache.lucene.document.Document newDoc = MetaDataUtil
//							.createDocumentForLink(theLink, titleTxt,
//									descriptionTxt, keywordsTxt, content);
//					indexWriter.addDocument(newDoc);
//					indexWriter.commit();
					System.out.println(" END #####################");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		return thread;
		
	}

}
