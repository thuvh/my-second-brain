package org.brain2.test.vneappcrawler.junit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.apache.http.protocol.HTTP;
import org.brain2.test.vneappcrawler.Article;
import org.brain2.test.vneappcrawler.Comment;
import org.brain2.test.vneappcrawler.DomainName;
import org.brain2.test.vneappcrawler.Parser;
import org.brain2.test.vneappcrawler.ReferenceObject;
import org.brain2.test.vneappcrawler.VnExpressDao;
import org.brain2.test.vneappcrawler.VnExpressParser;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.brain2.ws.core.utils.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class VnExpressParserTest {
	Parser vnexpressParser = new VnExpressParser();
//	@Test
//	public void testProcessContact(){
//		/**
//		 * /gl/the-gioi/nguoi-viet-5-chau/2011/11/hungary-thanh-binh/
//		 * /gl/cuoi/2011/11/ai-la-nan-nhan/
//		 * 
//		 */
//		String theLink = "/gl/the-gioi/nguoi-viet-5-chau/2011/11/hungary-thanh-binh/";
//		String html = HttpClientUtil.executeGet(DomainName.VNEXPRESS+theLink);
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
//				vnexpressParser.processContact(cpms, "/contactus/?id=");
//				System.out.println("Content :" + cpms.html());
//			}
//		}
//	}
//	@Test
//	public void testProcessTDSK(){
//		String theLink = "/gl/the-thao/bong-da/2011/11/bi-mat-vu-alex-ferguson-ve-man-utd";
//		String html = HttpClientUtil.executeGet(DomainName.VNEXPRESS+theLink);
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
//				vnexpressParser.processTDSK(cpms,article);
//				System.out.println(article.getTopicID());
//				assertEquals("6761", article.getTopicID());
//			}
//		}
//	}
//	@Test
//	public void testProcessExtraPageLink(){
//		/**
//		 * /gl/vi-tinh/san-pham-moi/2011/11/can-canh-samsung-galaxy-tab-7-plus-o-viet-nam
//		 * /gl/oto-xe-may/tu-van/2011/11/he-thong-treo-tren-sieu-xe-mclaren-mp4-12c/
//		 * Test remove video : /gl/xa-hoi/nhip-dieu-tre/2011/11/thao-my-gianh-ngoi-vi-miss-teen-2011/
//		 */
//		String theLink = "/gl/xa-hoi/2011/11/nguoi-gia-nhay-hiphop-ben-ho-guom/";
//		String html = HttpClientUtil.executeGet(DomainName.VNEXPRESS+theLink);
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
//				vnexpressParser.processExtraPageLink(cpms,exArticle,".content","div[cpms_content=true]", new ArrayList<String>());
//				for(ReferenceObject obj : exArticle.getRefObj()){
//					System.out.println("URL:"+obj.getUrl());
//				}
//				System.out.println("Content :"+ exArticle.getContent());
//				assertEquals(6, exArticle.getRefObj().size());
//			}
//		}
//	}
//	@Test
//	public void testGetThumbnail(){
//		String theLink ="/gl/xa-hoi/2011/11/un-xe-keo-xe-3-km-tren-tinh-lo";
//		Article article =new Article();
//		article.setId("2");
//		article.setSharedURL(theLink);
//		
//		vnexpressParser.getThumbnail(DomainName.VNEXPRESS+theLink+"/page_1.asp",DomainName.VNEXPRESS+theLink,article,"div[cpms_content=true]",130,100);
//		System.out.println("THUMBNIAL MD5 :"+ article.getThumbnailMD5());
//		assertEquals("/Files/Subject/3b/bb/c0/47/ket_xe_top2.jpg", article.getThumbnailURL());
//	}
//	@Test 
//	public void testGetComment(){
//		
//		String theLink = "/gl/xa-hoi/2011/11/nuoc-mat-hoc-tro-tien-dua-2-be-tu-nan-do-no-khi-gas-1";
//		String html = HttpClientUtil.executeGet(DomainName.VNEXPRESS+theLink);
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
//				Elements boxItems = content.select(".box-item");
//				if(boxItems.size()>0){
//					vnexpressParser.getComment(boxItems.get(0),article,_vnExpressDao,DomainName.VNEXPRESS+theLink);
//					System.out.println("COMMET SIZE :"+article.getComments().size());
//					for(Comment c: article.getComments()){
//						System.out.println("Tile:"+c.getTitle());
//						System.out.println("Content:"+c.getContent());
//						System.out.println("Author:"+c.getFullname());
//					}
//					assertEquals(33, article.getComments().size());
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
//		/**
//		 * /gl/vi-tinh/san-pham-moi/2011/11/can-canh-samsung-galaxy-tab-7-plus-o-viet-nam
//		 * /gl/the-gioi/nguoi-viet-5-chau/2011/11/hungary-thanh-binh/
//		 * 
//		 * /gl/vi-tinh/2011/03/30-bo-may-tinh-do-ban-nhat
//		 */
//		String theLink = "/gl/van-hoa/2011/11/man-kich-khoa-than-tai-le-trao-giai-emas";
//		String html = HttpClientUtil.executeGet(DomainName.VNEXPRESS+theLink);
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
//				vnexpressParser.extractIMG(cpms, article);
//				for(ReferenceObject obj : article.getRefObj()){
//					System.out.println("IMG URL :"+obj.getUrl());
//				}
//				assertEquals(3, article.getRefObj().size());
//			}
//		}
//	}
	@Test
	public void testParseHtmlToArticle(){
		/**
		 * /gl/van-hoa/2011/11/my-nhan-miss-world-toa-sang-trong-trang-phuc-da-hoi/
		 */
		String theLink="/gl/xa-hoi/2011/11/hang-nghin-nguoi-quay-sau-tran-u23-vn-thang-dam/";
		String html = HttpClientUtil.executeGet(DomainName.VNEXPRESS+theLink);
		VnExpressDao _vnExpressDao;
		try {
			_vnExpressDao = VnExpressDao.getInstance();
			Article article =new Article();
			article.setId("8");
			article.setSharedURL(theLink);
			vnexpressParser.parseHtmlToArticle(DomainName.VNEXPRESS+theLink, html, article, _vnExpressDao);
//			_vnExpressDao.saveArticle(article);
			System.out.println("Article Content : "+article.getContent());
//			_vnExpressDao.closeConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	
//	@Test
//	public void testGetImagesWithVideo(){
//		String theLink="/gl/the-thao/bong-da/2011/11/20-pha-bong-bat-ngo-va-hai-huoc-nhat-trong-tuan/";
//		String html = HttpClientUtil.executeGet(DomainName.VNEXPRESS+theLink);
//		System.out.println(html);
//		System.out.println("\n");
//		if(true) {
//			return;
//		}
//		try {
//		
//			Log.MODE = Log.PRINT_CONSOLE;
//			vnexpressParser.parseHtmlToArticle(DomainName.VNEXPRESS+theLink, html, new Article(), VnExpressDao.getInstance());
//		
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
