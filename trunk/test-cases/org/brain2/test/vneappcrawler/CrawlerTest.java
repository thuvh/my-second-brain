package org.brain2.test.vneappcrawler;

import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlerTest {
	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		try {
//			VnExpressDao vnExpressDao = VnExpressDao.getInstance();
//			int total =  vnExpressDao.getTotalCount();
//			
//			System.out.println("total article:" + total);
//			
//			ResultSet result = vnExpressDao.getSubjectPath(400000, 41);
//			
//			System.out.println("SIZE :"+result.getFetchSize());
//			
//			ExecutorService executor = Executors.newFixedThreadPool(10);
//			
//			// Wait until all threads are finish
//			VneCrawler.httpGetArticles(result,executor,vnExpressDao);
//			while (!executor.isTerminated()) {
//			}
//			System.out.println("Finished all threads");
//			if(VneCrawler.articleQueue.size()>0)
//				vnExpressDao.saveArticle(VneCrawler.articleQueue);
//			vnExpressDao.closeConnection();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
}
