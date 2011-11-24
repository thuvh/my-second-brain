package org.brain2.test.vneappcrawler;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.brain2.test.vneappcrawler.ReferenceObject.ReferenceType;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.brain2.ws.core.utils.Log;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class MainParser implements Parser{
	public void processLead(String lead,Article article){
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
					ReferenceObject obj = new ReferenceObject(article.getId(), VnExpressUtils.md5(href),href, ReferenceType.RELATED_LINK, new Date());
					article.addRefObj(obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("::::::PROCESS LEAD EXCEPTION::::::");
		}
	}
	public void processContact(Element element,String pattern){
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
	
	public void extractIMG(Element element, Article article){
		try {
			System.out.println("GET IMG ");
			Elements imgs = element.select("img");
			if(imgs != null && imgs.size()>0){
				System.out.println("IMGS SIZE"+imgs.size());
				for(Element img : imgs){
					String src = img.attr("src");
					if(src.startsWith("/Files/Subject/")){
						ReferenceObject obj = new ReferenceObject();
						obj.setArticleID(article.getId());
						obj.setType(ReferenceType.IMG);
						obj.setUpdateTime(new Date());
						
						obj.setUrl(src);
						obj.setMd5(VnExpressUtils.md5(src));
						if(img.parents()!=null &&img.parents().size()>=2){
							if(img.parents().get(1)!=null){
								Element trNext = img.parents().get(1).nextElementSibling();
								System.out.println("Paent : "+trNext);
								if(trNext != null && trNext.select("img").size()==0){
									obj.setCaption(trNext.text());
									trNext.remove();
								}
							}
							img.remove();
							article.addRefObj(obj);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("EXTRACT IMG EXCEPTION :"+e.getMessage());
		}
	}
	public void getThumbnail(String thumbnailPageURL,String theLink,Article article,String parentSelectPaterrn, int widthImg,int heightImg){
		try {
			String thumbnailPage = HttpClientUtil.executeGet(thumbnailPageURL);
			if(!thumbnailPage.isEmpty()){
				Elements cpmsThumbnailPage = Jsoup.parse(thumbnailPage).select(parentSelectPaterrn);
				if(cpmsThumbnailPage !=null && cpmsThumbnailPage.size()>0){
					Elements thumbs = cpmsThumbnailPage.get(0).select("img");
					for(Element thum : thumbs){
						if(String.valueOf(widthImg).equals(thum.attr("width"))){
							String urlThumbnail = thum.attr("src");
							article.setThumbnailURL(urlThumbnail);
							article.setThumbnailMD5(VnExpressUtils.md5(urlThumbnail));
							break;
						}
						if(String.valueOf(heightImg).equals(thum.attr("height"))){
							String urlThumbnail = thum.attr("src");
							article.setThumbnailURL(urlThumbnail);
							article.setThumbnailMD5(VnExpressUtils.md5(urlThumbnail));
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("::::GET THUMBNAIL EXCEPTION ::::");
		}
	}
	public void getComment(Element boxComment,Article article,VnExpressDao _vnExpressDao,String theLink){
		try {
			Elements _comments = null;

			// Count pages
			int totalPages = boxComment.select("a.Paging").size() + 1;

			// Get comment of current page (1)
			_comments = boxComment.select(".comment_ct");
			if (totalPages > 1) {
				for (int p = 2; p <= totalPages; p++) {
					
					String commentPages = HttpClientUtil.executeGet(theLink + "?p=" + p);
					_comments.addAll(Jsoup.parse(commentPages).select(
							".comment_ct"));
				}
			}

			if (_comments != null && _comments.size() > 0) {
				ResultSet dbComments = _vnExpressDao.getComment(article
						.getSharedURL());
				int i = 0;
				List<Comment> comments = new ArrayList<Comment>();
				while (dbComments.next()) {
					if(i<_comments.size()){
						System.out.println("i "+i);
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
				}
				article.setComments(comments);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			Log.println(":::COMMENT EXCEPTION:::");
		}
	}
	public void processTDSK(Element element,Article article){
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
	public List<String> processExtraPageLink(Element element,Article article,String parentContent,String parent,List<String> hrefList){
		try {
			Elements exPageLinks= element.select("a[href~=[p,P]age_([1-9])*.asp]");
			System.out.println("HREF SIZE :"+exPageLinks.size());
			if(exPageLinks.size()>0){
				for(int i=0,n=exPageLinks.size();i<n;i++){
					Element exPageLink = exPageLinks.get(i);
					String hrefLink = exPageLink.attr("href");
					System.out.println("Href :"+hrefLink);
					if(!hrefLink.startsWith("http://"))
						hrefLink = VnExpressUtils.getFullLink(hrefLink);
					if(!hrefLink.isEmpty()){
						if(!hrefList.contains(hrefLink.toLowerCase())){
							String pageHtml = HttpClientUtil.executeGet(hrefLink);
							if (!pageHtml.isEmpty()&& !pageHtml.equals("500")&&!pageHtml.equals("404")) {
								Document docPage2 = Jsoup.parse(pageHtml, HTTP.UTF_8);
								Elements contents = docPage2.select(parentContent);
								if(contents.size()>0){
									Elements cpmsExPage = contents.select(parent);
									
									if(cpmsExPage.size()>0){
										Element cpmExPage = cpmsExPage.get(0);
										cpmExPage = removeElement(cpmExPage);
										Article extraArticle = new Article();
										extraArticle.setId(article.getId());
										if(!hrefList.contains(hrefLink.toLowerCase()))
											hrefList.add(hrefLink.toLowerCase());
										processExtraPageLink(cpmExPage,extraArticle,parentContent,parent,hrefList);
										
										extractIMG(cpmExPage,article);
										article.addAll(extraArticle.getRefObj());
										extraArticle=null;
										
									}
								}
								
							}
							
							/**
							 * remove text *Clip:
							 */
							Element parentLink = exPageLink.parent();
							System.out.println("hreflink :"+hrefLink);
							
							exPageLink.remove();
							try{
								if(parentLink != null){
//									String newHtml = parentLink.html().replaceAll("Clip", "");
//									newHtml= newHtml.replaceAll("\\*", "");
//									newHtml= newHtml.replaceAll(":", "");
//									parentLink.html(newHtml+" ");
									parentLink.html(parentLink.html().replaceAll("\\*<em>Clip:</em>", "")+" ");
								}
							} catch (IndexOutOfBoundsException e) {
								e.printStackTrace();
								//TODO
								//skip
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("::::processExtraPageLink EXCEPTION ::::: ");
		}
		return hrefList;
	}
	public Element removeElement(Element cpmExPage) {
		return cpmExPage;
	}
	public void processLead(Element content,Article article,String leadPatern){
		try {
			Elements leads = content.select(leadPatern);
			
			if(leads.size()>0){
				
				String leadHtml = leads.get(0).html();
				String[] parts = leadHtml.split("<br",2);
				if(parts.length>0)
					leadHtml = parts[0];
				if(parts.length>=2){
					Document docPart2= Jsoup.parse(parts[1]);
					Elements relatedLinks= docPart2.select("a.Lead");
					if(relatedLinks.size()>0){
						for(Element reElement: relatedLinks){
							String href =  reElement.attr("href");
							ReferenceObject obj = new ReferenceObject(article.getId(), VnExpressUtils.md5(href),href, ReferenceType.RELATED_LINK, new Date());
							article.addRefObj(obj);
						}
						relatedLinks.remove();
					}
					String htmlPart2=docPart2.text().replaceAll("/", "");
					htmlPart2 = htmlPart2.replaceAll(">", "");
					leadHtml += htmlPart2;
				}
				
				article.setAbstractS(Jsoup.parse(leadHtml).text());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("::::::PROCESS LEAD EXCEPTION::::::");
		}
	}
	public static String getThumbnailPageURL(String theLink, List<String> extraPageURL){
		String thumbnailPage = theLink + "/page_1.asp";
		try {
			if(extraPageURL.contains(theLink.toLowerCase()+ "/page_1.asp")){
				Comparator<String> c = new ExtraPageComparator();
				Collections.sort(extraPageURL, c);
				String lastLink = extraPageURL.get(extraPageURL.size()-1);
				int nextIndex = Integer.parseInt(lastLink.substring(lastLink.lastIndexOf("_")+1,lastLink.lastIndexOf(".")))+1;
				thumbnailPage = theLink + "/page_"+nextIndex+".asp";
			}
		} catch (Exception e) {
			return thumbnailPage;
		}
		return thumbnailPage;
	}
	public static void main(String[] args) {
		List<String> list =new ArrayList<String>();
		list.add("http://vnexpress.net/gl/van-hoa/2011/11/my-nhan-miss-world-toa-sang-trong-trang-phuc-da-hoi/page_3.asp");
		list.add("http://vnexpress.net/gl/van-hoa/2011/11/my-nhan-miss-world-toa-sang-trong-trang-phuc-da-hoi/page_11.asp");
		list.add("http://vnexpress.net/gl/van-hoa/2011/11/my-nhan-miss-world-toa-sang-trong-trang-phuc-da-hoi/page_1.asp");
//		System.out.println(list.contains("http://vnexpress.net/gl/van-hoa/2011/11/my-nhan-miss-world-toa-sang-trong-trang-phuc-da-hoi/page_3.asp"));
		String result = MainParser.getThumbnailPageURL("http://vnexpress.net/gl/van-hoa/2011/11/my-nhan-miss-world-toa-sang-trong-trang-phuc-da-hoi", list);
		System.out.println(result);
	}

}
