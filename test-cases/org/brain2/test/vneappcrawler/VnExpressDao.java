package org.brain2.test.vneappcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VnExpressDao {

	protected Connection conn = null;
	private static VnExpressDao _theInstance = null;

	public static VnExpressDao getInstance() throws Exception {
		if (_theInstance == null) {
			_theInstance = new VnExpressDao();
		}
		return _theInstance;
	}

	protected VnExpressDao() throws Exception {
		initConnection();
	}

	public void closeConnection() {
		try {
			finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected Connection initConnection() throws Exception {
		String userName = "vnemobile";
		String password = "vnemobile@123";
		String url = "jdbc:mysql://10.254.53.216/vnemobile";
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		this.conn = DriverManager.getConnection(url, userName, password);
		System.out.println("Database connection established");
		return this.conn;
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if (conn != null) {
			try {
				conn.close();
				System.out.println("Database connection terminated");
				conn = null;
			} catch (Exception e) { /* ignore close errors */
			}
		}
	}

	/**
	 * total records of vnexpress subject
	 * 
	 * @return int
	 * @throws Exception
	 */
	public int getTotalCount() throws Exception {
		String sql = "SELECT count(`ID`) as total FROM `vnemobile`.`subject0`";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		int total = 0;
		while (rs.next()) {
			total = rs.getInt("total");
		}
		rs.close();
		ps.close();
		return total;
	}
	
	public ResultSet getSubjectPath(int begin, int total) throws Exception {
		String sql = "SELECT ID,Title,Lead,CONCAT(\"http://vnexpress.net\", Path) as Path FROM vnemobile.subject0 LIMIT ?,? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, begin);
		ps.setInt(2, total);
		
		ResultSet rs = ps.executeQuery();
		//ps.close();
		//rs.close();		
		return rs;
//		List<String> list = new ArrayList<String>(1000);
//		while (rs.next()) {
//			list.add("http://vnexpress.net" + rs.getString("Path"));
//		}
//		rs.close();
//		ps.close();
//		return list;
	}
	
	
	public Runnable httpGetArticle(final String theLink){
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				//System.out.println("\n ==> theLink: " + theLink);
				final String html = HttpClientUtil.executeGet(theLink);
				final Document doc = Jsoup.parse(html, HTTP.UTF_8);
				final String mainContentNodeId = "#content"; 
				final String baseURL = "http://vnexpress.net";										

				Elements metas = doc.select("meta");
				String descriptionTxt = "";
				String keywordsTxt = "";
				String robotsTxt = "";

				for (Element meta : metas) {
					String metaName = meta.attr("name");
					String metaContent = meta.attr("content");
					// System.out.println("meta name: " + metaName);
					if (metaName.equals("description")) {
						descriptionTxt = metaContent;
						System.out.println("descriptionTxt: " + descriptionTxt);
					} else if (metaName.equals("keywords")) {
						keywordsTxt = metaContent;
						// System.out.println("descriptionTxt: " + keywordsTxt);
					} else if (metaName.equals("robots")) {
						robotsTxt = metaContent;
						// System.out.println("robotsTxt: " + robotsTxt);
					}
				}

				Elements title = doc.select("title");
				String titleTxt = "";
				if (title.size() > 0) {
					titleTxt = title.get(0).text();
					System.out.println("titleTxt: " + titleTxt);
				}

				try {
					System.out.println(" BEGIN #####################");
					Elements contentNode;
					if (mainContentNodeId.isEmpty()) {
						contentNode = doc.select("body");						
					} else {						
						contentNode = doc.select(mainContentNodeId);
					}
					
					//get images in content
					Elements imgs = contentNode.select("img[src]");
					for (Element img : imgs) {
						String src = img.attr("src");
						//FIXME
						if(src.startsWith("/Files/Subject/")){
							System.out.println(" #img[src] = " + baseURL + src);
						}
					}
					
					Elements comments = contentNode.select("div.comment_ct");
					for (Element comment : comments) {
						String commentText = comment.html();
						//FIXME
						System.out.println(commentText + "\n");
					}		
					
					final Elements linkNodes = contentNode.select("a[href]");
					for (Element linkNode : linkNodes) {							
						String href = linkNode.attr("href");
						if(href.endsWith("#aComment")){
							System.out.println(" #a[href] = " + href);
						}
					}
					
					final Elements cpms_content = contentNode.select("div[cpms_content]");
					System.out.println(" #cpms_content = " + cpms_content.size());		
					for (Element node : cpms_content) {						
						String text = node.text();							
						System.out.println(" #cpms_content = " + text);							
					}

					System.out.println(" END #####################");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		return thread;		
	}

	private static final int NTHREDS = 10;
	public static void main(String[] args) {
		try {
			VnExpressDao vnExpressDao = VnExpressDao.getInstance();
			int total =  vnExpressDao.getTotalCount();
			
			//System.out.println("total article:" + total);
			
			ResultSet result = vnExpressDao.getSubjectPath(400000, NTHREDS);
			ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
			
			Runnable worker = VneCrawler.httpGetArticle("http://vnexpress.net/gl/xa-hoi/2011/11/xe-container-tong-oto-khach-10-nguoi-chet-chay/", "title hji", "Leading");
			executor.execute(worker);
//			while (result.next()) {								
//				Runnable worker = VneCrawler.httpGetArticle(result.getString("Path"), result.getString("Title"), result.getString("Lead"));
//				executor.execute(worker);			
//			}
			
			// This will make the executor accept no new threads
			// and finish all existing threads in the queue	
			executor.shutdown();			
			
			// Wait until all threads are finish
			while (!executor.isTerminated()) {
			}
			System.out.println("Finished all threads");

			vnExpressDao.closeConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
