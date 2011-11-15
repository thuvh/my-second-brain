package org.brain2.test.vneappcrawler;

import java.util.Date;

import org.apache.http.protocol.HTTP;
import org.brain2.test.vneappcrawler.ReferenceObject.ReferenceType;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.brain2.ws.core.utils.Log;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class VnExpressParser extends MainParser{
	public static  final String BASE_URL = "http://vnexpress.net";  
	
	public static Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao) throws Exception {
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
		final String lead = article.getAbstractS();

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
				
				processLead(lead,article);

				/**
				 * remove title and lead from content
				 */
				cpms.select(".Title").remove();
				cpms.select(".Lead").remove();

				/**
				 * Extract content
				 */
				
				/**
				 * remove Danh Gia IMGS
				 */
				
				Elements pointImg = cpms.select(".Point");
				if(pointImg.size()>0)
					pointImg.remove();
				
				/**
				 * Process page_2.asp and page_1.asp is a link in content
				 * Get Image from page_2.asp ,remove * Clip + video link
				 */
				Article exArticle = new Article();
				exArticle.setId(article.getId());
				processExtraPageLink(cpms,exArticle);
				
				/**
				 * Images
				 */
				
				extractIMG(cpms,article);
				article.addAll(exArticle.getRefObj());
				exArticle=null;
				/**
				 * Remove ContactUs voi link Tai Day
				 * a[href$=/ContactUs/?id=cuoi@vnexpress.net],
				 * /ContactUs/?id=cuoi@vnexpress.net
				 */
				processContact(cpms,"/contactus/?id=");
				/**
				 * Remove
				 * mailto:vitinh@vnexpress.net
				 */
				processContact(cpms,"mailto:");
				
				
				/**
				 * Detect "Theo dong su kien"
				 */
				processTDSK(cpms,article);
				
				
				cpms.select("a[class!=Normal]").remove();

				/**
				 * process page_1.asp
				 * Get thumbnail 
				 * TODO : case : page_2.asp luu thumnail
				 */
				getThumbnail(theLink,article,"div[cpms_content=true]",130);
				
				/**
				 * Remove all , just get <p>
				 */
				for (Element p : cpms.select("p")) {
					p.html(Jsoup.parse(p.html()).text());
				}
				Whitelist whiteList = new Whitelist();
				whiteList.addTags("p");
				String newContent = Jsoup.clean(cpms.html(), whiteList);
				article.setContent(newContent);
				
				/**
				 * Comment
				 */
				Elements boxItems = content.select(".box-item");
				if(boxItems.size()>0)
					getComment(boxItems.get(0),article,_vnExpressDao,theLink);
				
			} else {
				Log.println("NO CMPS " + theLink);
			}
		}
		return article;
	}
	public static void processContact(Element element,String pattern){
		try {
			Elements contactLinks = element.select("a[href*="+pattern+"]");
			for(Element lnk: contactLinks){
				Element pEle = lnk.parent();
				Validate.notNull(pEle);
				pEle.remove();
			}
		} catch (java.lang.IllegalArgumentException e) {
			//skip
		}
	}
	public static void processExtraPageLink(Element element,Article article){
		try {
			Elements exPageLinks= element.select("a[href~=page_[1,2,3].asp]");
			Log.println("exPageLinks: "+exPageLinks.size());
			if(exPageLinks.size()>0){
				for(Element exElement:exPageLinks){
					String pageHtml = HttpClientUtil.executeGet(exElement.attr("href"));
					if (!pageHtml.isEmpty()&& !pageHtml.equals("500")&&!pageHtml.equals("404")) {
						Document docPage2 = Jsoup.parse(pageHtml, HTTP.UTF_8);
						Elements contents = docPage2.select(".content");
						if(contents.size()>0){
							Elements cpmsExPage = contents.select("div[cpms_content=true]");
							if(cpmsExPage.size()>0){
								Article extraArticle = new Article();
								extraArticle.setId(article.getId());
								
								processExtraPageLink(cpmsExPage.get(0),extraArticle);
								
								extractIMG(cpmsExPage.get(0),article);
								article.addAll(extraArticle.getRefObj());
								extraArticle=null;
								
							}
						}
						
					}
					exElement.parent().remove();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.println("::::processExtraPageLink EXCEPTION ::::: ");
		}
	}
	public static void extractIMG(Element element, Article article){
		try {
			Elements imgs = element.select("img");
			if(imgs != null && imgs.size()>0){
				Log.println("IMGS SIZE"+imgs.size());
				for(Element img : imgs){
					ReferenceObject obj = new ReferenceObject();
					obj.setArticleID(article.getId());
					obj.setType(ReferenceType.IMG);
					obj.setUpdateTime(new Date());
					String src = img.attr("src");
					obj.setUrl(src);
					obj.setMd5(StringUtil.md5(src));
					if(img.parents()!=null &&img.parents().size()>=2){
						Element trNext = img.parents().get(1).nextElementSibling(); 
						if(trNext != null && (trNext.select(".Image").size()>0||trNext.select("img").size()==0)){
							obj.setCaption(trNext.text());
							trNext.remove();
						}
						img.remove();
						article.addRefObj(obj);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("EXTRACT IMG EXCEPTION :"+e.getMessage());
		}
	}
	public static void processLead(String lead,Article article){
		
		try {
			Elements _related_links = null;

			if (lead != null) {
				String[] leadParts = lead.split("<BR>>", 2);
				String _abstract = Jsoup.parse(leadParts[0]).text();
				article.setAbstractS(_abstract);

				if (leadParts.length > 1) {
					_related_links = Jsoup.parse(leadParts[1]).select("a");
				}
			}
			if(_related_links!=null &&_related_links.size()>0){
				for(Element ele: _related_links){
					String href =  ele.attr("href");
					ReferenceObject obj = new ReferenceObject(article.getId(), StringUtil.md5(href),href.replaceAll("http://vnexpress.net", ""), ReferenceType.RELATED_LINK, new Date());
					article.addRefObj(obj);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.println("::::::PROCESS LEAD EXCEPTION::::::");
		}
	}
	public static void processTDSK(Element element,Article article){
		try {
			Elements tdElement = element.select("td[id*=tdTopic_]");
			if(tdElement.size()>0){
				String[] idParts = tdElement.attr("id").split("_");
				if(idParts.length>=3)
					article.setTopicID(idParts[1]);
			}
			Elements tdskTopicTitle = element.select("a.TopicTitle");
			Elements tdskOther = element.select("a.Other");
			tdskTopicTitle.remove();
			tdskOther.remove();
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("::::PROCESS TDSK EXCEPTION:::::");
		}
	}
	public static void getThumbnail(String theLink,Article article){
		try {
			String thumbnailPage = HttpClientUtil.executeGet(theLink + "/page_1.asp");
			if(!thumbnailPage.isEmpty()){
				Elements cpmsThumbnailPage = Jsoup.parse(thumbnailPage).select("div[cpms_content=true]");
				if(cpmsThumbnailPage !=null && cpmsThumbnailPage.size()>0){
					Elements thumbs = cpmsThumbnailPage.get(0).select("img");
					if(thumbs.size()>=2){
						String urlThumbnail = thumbs.get(1).attr("src");
						article.setThumbnailURL(urlThumbnail);
						article.setThumbnailMD5(StringUtil.md5(urlThumbnail));
					} else if(thumbs.size()>=1){
						String urlThumbnail = thumbs.get(0).attr("src");
						article.setThumbnailURL(urlThumbnail);
						article.setThumbnailMD5(StringUtil.md5(urlThumbnail));
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.println("::::GET THUMBNAIL EXCEPTION ::::");
		}
	}
}
