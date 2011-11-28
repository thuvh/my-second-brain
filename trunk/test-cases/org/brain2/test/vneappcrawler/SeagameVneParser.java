package org.brain2.test.vneappcrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SeagameVneParser extends MainParser{

	public Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao) throws Exception {
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
		
		Elements contents = doc.select(".ctdt");
		
		if(_vnExpressDao != null){
			processCate(article,_vnExpressDao);
			article.setAuthorID(_vnExpressDao.getAuthorID(article.getPostBy()));
			setTopics(article, _vnExpressDao);	
		}		
		
		if (contents.size() > 0) {
			Element content = contents.get(0);
			getArticleDate(article,content,".time");
			/**
			 * split related link from lead
			 */
			
			processLead(content,article,"h2.Lead");
			

			/**
			 * remove script links in content
			 */
			content.select("script").remove();
			content.select(".bortnd").remove();
			content.select(".sharend").remove();
			content.select(".time").remove();
			content.select(".Title").remove();
			content.select(".Lead").remove();
			
			
			content.select(".tkt").remove();
			content.select(".tag").remove();
			content.select(".ctkw").remove();
			
			/**
			 * Extract content
			 */
			
			/**
			 * Comment
			 */
			Elements commentws = content.select(".commentw");
			if(commentws.size()>0)
				getComment(commentws.get(0),article,_vnExpressDao,theLink);
			commentws.remove();
			
			/**
			 * Process page_2.asp and page_1.asp is a link in content
			 * Get Image from page_2.asp ,remove * Clip + video link
			 */
			Article exArticle = new Article();
			exArticle.setId(article.getId());
			List<String> extraHrefs =processExtraPageLink(content,exArticle,".bodypage",".ctdt",new ArrayList<String>());
			
			/**
			 * Images
			 */
			
			extractIMG(content,article);
			article.addAll(exArticle.getRefObj());
			exArticle=null;

			/**
			 * process page_1.asp
			 * Get thumbnail 
			 * TODO : case : page_2.asp luu thumnail
			 */
			getThumbnail(getThumbnailPageURL(theLink, extraHrefs),theLink,article,".ctdt",130,100);
			
			/**
			 * Remove all , just get <p>
			 */
			filterContent(content, article);
			
		}
		return article;
	}
	
	
	public Element removeElement(Element element) {
		element.select("script").remove();
		element.select(".bortnd").remove();
		element.select(".sharend").remove();
		element.select(".time").remove();
		element.select(".Title").remove();
		element.select(".Lead").remove();
		
		element.select(".tkt").remove();
		element.select(".tag").remove();
		element.select(".ctkw").remove();
		element.select(".commentw").remove();
		return element;
	}

}
