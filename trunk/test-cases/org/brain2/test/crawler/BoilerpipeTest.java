package org.brain2.test.crawler;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.DefaultExtractor;

public class BoilerpipeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final String theLink = "http://vnexpress.net/gl/xa-hoi/2011/11/xe-container-tong-oto-khach-10-nguoi-chet-chay/";
			
			final String html = HttpClientUtil.executeGet(theLink);
			final Document doc = Jsoup.parse(html, HTTP.UTF_8);
			final String mainContentNodeId = "#content"; 
			final String baseURL = "http://vnexpress.net";
			
			Runnable thread = new Runnable() {

				@Override
				public void run() {
					System.out.println("\n ==> theLink: " + theLink);
										

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
						Elements contentNode;
						if (mainContentNodeId.isEmpty()) {
							contentNode = doc.select("body");						
						} else {						
							contentNode = doc.select(mainContentNodeId);
						}
						
						//System.out.println(" HTML: "+contentNode.html());
						
						//get images in content
						Elements imgs = contentNode.select("img[src]");
						for (Element img : imgs) {
							String src = img.attr("src");
							//FIXME
							if(src.startsWith("/Files/Subject/")){
								System.out.println(" #img[src] = " + baseURL + src);
							}
						}
						
						Elements comments = contentNode.select("div.comment_ct");
						for (Element comment : comments) {
							String commentText = comment.html();
							//FIXME
							System.out.println(commentText + "\n");
						}		
						
						final Elements linkNodes = contentNode.select("a[href]");
						for (Element linkNode : linkNodes) {							
							String href = linkNode.attr("href");
							if(href.endsWith("#aComment")){
								System.out.println(" #a[href] = " + href);
							}
						}
						
						final Elements cpms_content = contentNode.select("div[cpms_content]");
						System.out.println(" #cpms_content = " + cpms_content.size());		
						for (Element node : cpms_content) {						
							String text = node.text();							
							System.out.println(" #cpms_content = " + text);							
						}
						
						

//						String content = DefaultExtractor.INSTANCE.getText(contentNode.html());
//						System.out.println(content);
//						org.apache.lucene.document.Document newDoc = MetaDataUtil
//								.createDocumentForLink(theLink, titleTxt,
//										descriptionTxt, keywordsTxt, content);
//						indexWriter.addDocument(newDoc);
//						indexWriter.commit();
						System.out.println(" END #####################");
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

}
