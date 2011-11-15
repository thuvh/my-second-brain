package org.brain2.test.vneappcrawler;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.brain2.test.vneappcrawler.ReferenceObject.ReferenceType;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class MainParser implements Parser{
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
			System.out.println("::::::PROCESS LEAD EXCEPTION::::::");
		}
	}
	
	public static void extractIMG(Element element, Article article){
		try {
			System.out.println("GET IMG ");
			Elements imgs = element.select("img");
			if(imgs != null && imgs.size()>0){
				System.out.println("IMGS SIZE"+imgs.size());
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
			System.out.println("EXTRACT IMG EXCEPTION :"+e.getMessage());
		}
	}
	public static void getThumbnail(String theLink,Article article,String parentSelectPaterrn, int widthImg){
		try {
			String thumbnailPage = HttpClientUtil.executeGet(theLink + "/page_1.asp");
			if(!thumbnailPage.isEmpty()){
				Elements cpmsThumbnailPage = Jsoup.parse(thumbnailPage).select(parentSelectPaterrn);
				if(cpmsThumbnailPage !=null && cpmsThumbnailPage.size()>0){
					Elements thumbs = cpmsThumbnailPage.get(0).select("img");
					for(Element thum : thumbs){
						if(String.valueOf(widthImg).equals(thum.attr("width"))){
							String urlThumbnail = thum.attr("src");
							article.setThumbnailURL(urlThumbnail);
							article.setThumbnailMD5(StringUtil.md5(urlThumbnail));
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("::::GET THUMBNAIL EXCEPTION ::::");
		}
	}
	public static void getComment(Element boxComment,Article article,VnExpressDao _vnExpressDao,String theLink){
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
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(":::COMMENT EXCEPTION:::");
		}
	}

}
