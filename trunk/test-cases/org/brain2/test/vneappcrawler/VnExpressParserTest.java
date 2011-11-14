package org.brain2.test.vneappcrawler;

import static org.junit.Assert.assertEquals;

import org.brain2.ws.core.utils.HttpClientUtil;
import org.junit.Test;

public class VnExpressParserTest {
//	@Test
//	public void testRemoveContactUs(){
//		String theLink1="/gl/the-gioi/nguoi-viet-5-chau/2011/11/hungary-thanh-binh/";
//		String theLink2="/gl/cuoi/2011/11/ai-la-nan-nhan/";
//		String html = HttpClientUtil.executeGet("http://vnexpress.net"+theLink1);
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			Article article =new Article();
//			article.setId("1");
//			article.setSharedURL("/gl/the-gioi/nguoi-viet-5-chau/2011/11/hungary-thanh-binh/");
//			VnExpressParser.parseHtmlToArticle("http://vnexpress.net"+theLink1, html, article, _vnExpressDao);
//			_vnExpressDao.saveArticle(article);
//			System.out.println("Article Content : "+article.getContent());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	@Test
//	public void testExtrsctImg(){
//		/**
//		 * Case 1 :
//		 * <table><tbody><tr><td><img/></td></tr><tr><td>caption text</td></tr>....</tbody><table>
//		 * 
//		 */
//		String theLink1= "/gl/vi-tinh/san-pham-moi/2011/11/can-canh-samsung-galaxy-tab-7-plus-o-viet-nam";
//		/**
//		 * Case 2 :
//		 * <table><tbody><tr><td><img/></td></tr><tr><td class="Image">caption text</tr></tbody><table>
//		 */
//		String theLink2 = "/gl/the-gioi/nguoi-viet-5-chau/2011/11/hungary-thanh-binh/";
//		String theLink4= "/gl/vi-tinh/kinh-nghiem/2011/03/6-buoc-chup-anh-nguoc-sang-silhouette";
//		String theLink5 = "/gl/vi-tinh/2011/03/30-bo-may-tinh-do-ban-nhat";
//		String html = HttpClientUtil.executeGet("http://vnexpress.net"+theLink5);
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			Article article =new Article();
//			article.setId("4");
//			article.setSharedURL("/gl/vi-tinh/2011/03/30-bo-may-tinh-do-ban-nhat");
//			VnExpressParser.parseHtmlToArticle("http://vnexpress.net"+theLink5, html, article, _vnExpressDao);
//			_vnExpressDao.saveArticle(article);
//			System.out.println("Size IMGS : "+article.getRefObj().size());
//			assertEquals(30, article.getRefObj().size());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	@Test
//	public void testExtractThumbnail(){
//		String theLink1 ="/gl/xa-hoi/2011/11/un-xe-keo-xe-3-km-tren-tinh-lo";
//		String html = HttpClientUtil.executeGet("http://vnexpress.net"+theLink1);
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			Article article =new Article();
//			article.setId("4");
//			article.setSharedURL("/gl/xa-hoi/2011/11/un-xe-keo-xe-3-km-tren-tinh-lo");
//			VnExpressParser.parseHtmlToArticle("http://vnexpress.net"+theLink1, html, article, _vnExpressDao);
//			System.out.println("Thumbnail URL : "+article.getThumbnailURL());
//			System.out.println("Thumbnail MD5 : "+article.getThumbnailMD5());
//			assertEquals("/Files/Subject/3b/bb/c0/47/ket_xe_top2.jpg",article.getThumbnailURL());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	@Test 
//	public void removeVideo(){
//		String theLink1 = "/gl/xa-hoi/nhip-dieu-tre/2011/11/thao-my-gianh-ngoi-vi-miss-teen-2011/";
//		String html = HttpClientUtil.executeGet("http://vnexpress.net"+theLink1);
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			Article article =new Article();
//			article.setId("4");
//			article.setSharedURL("/gl/xa-hoi/nhip-dieu-tre/2011/11/thao-my-gianh-ngoi-vi-miss-teen-2011/");
//			VnExpressParser.parseHtmlToArticle("http://vnexpress.net"+theLink1, html, article, _vnExpressDao);
//			_vnExpressDao.saveArticle(article);
//			System.out.println("Size IMGS : "+article.getContent());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	public void testComment(){
//		
//	}
	@Test
	public void testGetTopic(){
		String theLink1 = "/gl/xa-hoi/2011/11/buc-kham-trai-ve-vua-bao-dai-va-hoang-hau-nam-phuong/";
		String html = HttpClientUtil.executeGet("http://vnexpress.net"+theLink1);
		VnExpressDao _vnExpressDao;
		try {
			_vnExpressDao = VnExpressDao.getInstance();
			Article article =new Article();
			article.setId("4");
			article.setSharedURL("/gl/xa-hoi/2011/11/buc-kham-trai-ve-vua-bao-dai-va-hoang-hau-nam-phuong/");
			VnExpressParser.parseHtmlToArticle("http://vnexpress.net"+theLink1, html, article, _vnExpressDao);
			_vnExpressDao.saveArticle(article);
			assertEquals("6759", article.getTopicID());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
