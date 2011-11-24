package org.brain2.test.vneappcrawler;

import java.util.List;

import org.jsoup.nodes.Element;

public interface Parser {
	public Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao)throws Exception;
	public void processLead(String lead,Article article);
	public void processContact(Element element,String pattern);
	public void extractIMG(Element element, Article article);
	public void getThumbnail(String thumbnailPageURL,String theLink,Article article,String parentSelectPaterrn, int widthImg,int heightImg);
	public void getComment(Element boxComment,Article article,VnExpressDao _vnExpressDao,String theLink);
	public List<String> processExtraPageLink(Element element,Article article,String parentContent,String parent,List<String> hrefList);
	public Element removeElement(Element element);
	public void processTDSK(Element element,Article article);
	public void processLead(Element content,Article article,String leadPatern);
}
