package org.brain2.test.vneappcrawler.junit;

import static org.junit.Assert.assertEquals;

import org.apache.http.protocol.HTTP;
import org.brain2.test.vneappcrawler.NhaDepVneParser;
import org.brain2.test.vneappcrawler.Parser;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class NhaDepVneParserTest {
	private String domain ="http://nhadep.vnexpress.net";
	Parser nhaDepVneParser = new NhaDepVneParser();
//	@Test
//	public void testProcessContact(){
//		
//		String theLink = "/GL/Nha-dep/Khong-gian-song/2009/01/3BA0C7E1/";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//
//		Elements contents = doc.select(".content-left");
//
//		if (contents.size() > 0) {
//			Element content = contents.get(0);
//
//			Elements cpms_content = content.select(".PT-top-c3");
//
//			if (cpms_content.size() > 0) {
//				Element cpms = cpms_content.get(0);
//				nhaDepVneParser.processContact(cpms, "/contactus/?id=");
//				System.out.println("Content :" + cpms.html());
//			}
//		}
//	}
//	@Test
//	public void testProcessExtraPageLink(){
//		String theLink = "/GL/Nha-dep/Khong-gian-song/2009/01/3BA0C7E1/";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//		
//		Elements contents = doc.select(".content-left");
//			
//		if (contents.size() > 0) {
//			
//			Element content = contents.get(0);
//
//			Elements cpms_content = content.select(".PT-top-c3");
//
//			if (cpms_content.size() > 0) {
//				Element cpms = cpms_content.get(0);
//				Article exArticle = new Article();
//				exArticle.setId("1");
//				nhaDepVneParser.processExtraPageLink(cpms,exArticle,".content-left",".PT-top-c3");
//				for(ReferenceObject obj : exArticle.getRefObj()){
//					System.out.println("URL:"+obj.getUrl());
//				}
//				assertEquals(0, exArticle.getRefObj().size());
//			}
//		}
//	}
//	@Test
//	public void testGetThumbnail(){
//		String theLink ="/GL/Nha-dep/Khong-gian-song/2009/01/3BA0C7E1/";
//		Article article =new Article();
//		article.setId("2");
//		article.setSharedURL(theLink);
//		nhaDepVneParser.getThumbnail(domain+theLink,article,".PT-top-c3",130,100);
//		System.out.println("THUMBNIAL MD5 :"+ article.getThumbnailMD5());
//		assertEquals("/Files/Subject/3B/A0/C7/E1/Mado.jpg", article.getThumbnailURL());
//	}
//	@Test
//	public void testExtractIMG(){
//		
//		String theLink = "/GL/Nha-dep/Khong-gian-song/2009/01/3BA0C7E1/";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//
//		Elements contents = doc.select(".content-left");
//
//		if (contents.size() > 0) {
//			Element content = contents.get(0);
//
//			Elements cpms_content = content.select(".PT-top-c3");
//
//			if (cpms_content.size() > 0) {
//				Element cpms = cpms_content.get(0);
//				Article article =new Article();
//				article.setId("1");
//				article.setSharedURL(theLink);
//				nhaDepVneParser.extractIMG(cpms, article);
//				for(ReferenceObject obj : article.getRefObj()){
//					System.out.println("IMG URL :"+obj.getUrl());
//				}
//				assertEquals(11, article.getRefObj().size());
//			}
//		}
//	}
//	@Test
//	public void testParseHtmlToArticle(){
//		String theLink="/GL/Nha-dep/Khong-gian-song/2009/01/3BA0C7E1/";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			Article article =new Article();
//			article.setId("2");
//			article.setSharedURL(theLink);
//			nhaDepVneParser.parseHtmlToArticle(domain+theLink, html, article, _vnExpressDao);
//			_vnExpressDao.saveArticle(article);
//			System.out.println("Article Content : "+article.getContent());
//			_vnExpressDao.closeConnection();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
