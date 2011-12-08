package com.vnexpress.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vnexpress.dao.VnExpressDao;
import com.vnexpress.model.Article;

public class NhaDepVneParser extends MainParser{
	public Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao) throws Exception {
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);

		Elements contents = doc.select(".content-left");
		if(_vnExpressDao != null){
			processCate(article,_vnExpressDao);
			article.setAuthorID(_vnExpressDao.getAuthorID(article.getPostBy()));
			setTopics(article, _vnExpressDao);
		}
		if (contents.size() > 0) {
			
			Element content = contents.get(0);
			
			Elements cpms_content = content.select(".PT-top-c3");

			if (cpms_content.size() > 0) {
				Element cpms = cpms_content.get(0);
				/**
				 * remove script links in content
				 */
				cpms.select("script").remove();
				cpms.select("#flashContent").remove();
				/**
				 * split related link from lead
				 */
				
				processLead(cpms,article,"p.Lead");

				/**
				 * remove title and lead from content
				 */
				cpms.select(".Title").remove();
				cpms.select(".Lead").remove();

				/**
				 * Extract content
				 */
				
				/**
				 * Process page_2.asp and page_1.asp is a link in content
				 * Get Image from page_2.asp ,remove * Clip + video link
				 */
				Article exArticle = new Article();
				exArticle.setId(article.getId());
				List<String> extraHrefs = processExtraPageLink(cpms,exArticle,".content-left",".PT-top-c3",new ArrayList<String>());
				
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
				
				cpms.select("a[class!=Normal]").remove();

				/**
				 * process page_1.asp
				 * Get thumbnail 
				 * TODO : case : page_2.asp luu thumnail
				 */
				getThumbnail(getThumbnailPageURL(theLink, extraHrefs),theLink,article,".PT-top-c3",130,100);
				
				/**
				 * Remove all , just get <p>
				 */
				filterContent(cpms, article);
				
			} else {
				Log.println("NO CMPS " + theLink);
			}
		}
		return article;
	}
}
