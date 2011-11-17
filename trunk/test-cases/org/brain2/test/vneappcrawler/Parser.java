package org.brain2.test.vneappcrawler;

import org.jsoup.nodes.Element;

public interface Parser {
	public Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao)throws Exception;
	public void processLead(String lead,Article article);
	public void processContact(Element element,String pattern);
	public void extractIMG(Element element, Article article);
	public void getThumbnail(String theLink,Article article,String parentSelectPaterrn, int widthImg,int heightImg);
	public void getComment(Element boxComment,Article article,VnExpressDao _vnExpressDao,String theLink);
	public void processExtraPageLink(Element element,Article article,String parentContent,String parent);
	public Element removeElement(Element element);
	public void processTDSK(Element element,Article article);
	public void processLead(Element content,Article article,String leadPatern);
}
