package org.brain2.test.vneappcrawler.junit;

import static org.junit.Assert.assertEquals;

import org.apache.http.protocol.HTTP;
import org.brain2.test.vneappcrawler.EbankVneParser;
import org.brain2.test.vneappcrawler.Parser;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class EbankVneParserTest {
	private String domain ="http://ebank.vnexpress.net";
	Parser ebankVneParser = new EbankVneParser();
//	@Test
//	public void testProcessContact(){
//		
//		String theLink = "/gl/ebank/thi-truong/2011/11/sot-ty-gia-ngan-hang-dang-ha-nhiet";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//
//		Elements contents = doc.select(".content");
//
//		if (contents.size() > 0) {
//			Element content = contents.get(0);
//
//			Elements cpms_content = content.select("div[cpms_content=true]");
//
//			if (cpms_content.size() > 0) {
//				Element cpms = cpms_content.get(0);
//				ebankVneParser.processContact(cpms, "/contactus/?id=");
//				System.out.println("Content :" + cpms.html());
//			}
//		}
//	}
//	@Test
//	public void testProcessTDSK(){
//		String theLink = "/gl/ebank/thi-truong/2011/11/sot-ty-gia-ngan-hang-dang-ha-nhiet";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//
//		Elements contents = doc.select(".content");
//
//		if (contents.size() > 0) {
//			Element content = contents.get(0);
//
//			Elements cpms_content = content.select("div[cpms_content=true]");
//
//			if (cpms_content.size() > 0) {
//				Element cpms = cpms_content.get(0);
//				Article article =new Article();
//				article.setId("4");
//				article.setSharedURL(theLink);
//				ebankVneParser.processTDSK(cpms,article);
//				System.out.println(article.getTopicID());
//				assertEquals("6755", article.getTopicID());
//			}
//		}
//	}
//	@Test
//	public void testProcessExtraPageLink(){
//		String theLink = "/gl/ebank/thi-truong/2011/11/sot-ty-gia-ngan-hang-dang-ha-nhiet";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//		
//		Elements contents = doc.select(".content");
//			
//		if (contents.size() > 0) {
//			
//			Element content = contents.get(0);
//
//			Elements cpms_content = content.select("div[cpms_content=true]");
//
//			if (cpms_content.size() > 0) {
//				Element cpms = cpms_content.get(0);
//				Article exArticle = new Article();
//				exArticle.setId("1");
//				ebankVneParser.processExtraPageLink(cpms,exArticle,".content","div[cpms_content=true]");
//				for(ReferenceObject obj : exArticle.getRefObj()){
//					System.out.println("URL:"+obj.getUrl());
//				}
//				assertEquals(0, exArticle.getRefObj().size());
//			}
//		}
//	}
//	@Test
//	public void testGetThumbnail(){
//		String theLink ="/gl/ebank/thi-truong/2011/11/sot-ty-gia-ngan-hang-dang-ha-nhiet";
//		Article article =new Article();
//		article.setId("2");
//		article.setSharedURL(theLink);
//		ebankVneParser.getThumbnail(domain+theLink,article,"div[cpms_content=true]",130,100);
//		System.out.println("THUMBNIAL MD5 :"+ article.getThumbnailMD5());
//		assertEquals("/Files/Subject/3b/bb/c2/3d/dola_2.jpg", article.getThumbnailURL());
//	}
//	@Test 
//	public void testGetComment(){
//		
//		String theLink = "/GL/Ebank/Tu-van/Cong-dong-Ebank/2009/09/3BA13940";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Article article =new Article();
//		article.setId("2");
//		article.setSharedURL(theLink);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//
//		Elements contents = doc.select(".content");
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			if (contents.size() > 0) {
//				Element content = contents.get(0);
//				Elements boxItems = content.select(".bgComment");
//				if(boxItems.size()>0){
//					ebankVneParser.getComment(boxItems.get(0),article,_vnExpressDao,domain+theLink);
//					for(Comment c: article.getComments()){
//						System.out.println("Tile:"+c.getTitle());
//						System.out.println("Content:"+c.getContent());
//						System.out.println("Author:"+c.getFullname());
//					}
//					assertEquals(8, article.getComments().size());
//					
//				}
//			}
//			_vnExpressDao.closeConnection();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	@Test
//	public void testExtractIMG(){
//		
//		String theLink = "/gl/ebank/tu-van/2011/11/cach-tranh-bi-danh-cap-du-lieu-khi-giao-dich-atm/";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//
//		Elements contents = doc.select(".content");
//
//		if (contents.size() > 0) {
//			Element content = contents.get(0);
//
//			Elements cpms_content = content.select("div[cpms_content=true]");
//
//			if (cpms_content.size() > 0) {
//				Element cpms = cpms_content.get(0);
//				Article article =new Article();
//				article.setId("1");
//				article.setSharedURL(theLink);
//				ebankVneParser.extractIMG(cpms, article);
//				for(ReferenceObject obj : article.getRefObj()){
//					System.out.println("IMG URL :"+obj.getUrl());
//				}
//				assertEquals(2, article.getRefObj().size());
//			}
//		}
//	}
//	@Test
//	public void testParseHtmlToArticle(){
//		String theLink="/GL/Ebank/Tu-van/Cong-dong-Ebank/2009/09/3BA13940";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			Article article =new Article();
//			article.setId("2");
//			article.setSharedURL(theLink);
//			ebankVneParser.parseHtmlToArticle(domain+theLink, html, article, _vnExpressDao);
//			_vnExpressDao.saveArticle(article);
//			System.out.println("Article Content : "+article.getContent());
//			_vnExpressDao.closeConnection();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
