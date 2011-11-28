package org.brain2.test.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.brain2.test.vneappcrawler.Article;
import org.brain2.test.vneappcrawler.ImporterConfigs;
import org.brain2.test.vneappcrawler.Parser;
import org.brain2.test.vneappcrawler.VnExpressUtils;
import org.brain2.ws.core.utils.HttpClientUtil;

public class VneSQLserverDao {

	private static Connection con = null;
	private static HashMap<Long,String> articles = new HashMap<Long, String>();
	private static boolean forceUpdateContent = false;

	protected static void ConnectWithDriver() throws Exception {		
		ImporterConfigs configs = ImporterConfigs.loadFromFile("/importer-mssql-configs.json");		
		if("sqlserver".equals(configs.getDbdriver())){
			Class.forName(configs.getDbdriverclasspath());
			con = DriverManager.getConnection(configs.getSQLServerConnectionUrl(), configs.getUsername(), configs.getPassword());
			System.out.println("Connection Catalog: "+con.getCatalog());			
		} else {		
			throw new IllegalArgumentException("importer-mssqls-configs.json was not config correctly!");
		}
	}

	public final static boolean updateArticleContent(Article article) throws SQLException {
		String sql = "UPDATE Subject0 SET Content = ? WHERE ID = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setString(1, article.getContent());
		ps.setLong(2, article.getID());
		boolean rs =  ps.executeUpdate() > 0;
		return rs;
	}

	public static ResultSet getSubjectPaths(long maxID, int limit)
			throws SQLException {
		// SELECT TOP 10 ID,Path FROM Subject0 WHERE ID <= (SELECT MAX(ID) FROM
		// Subject0) ORDER BY ID DESC
		// SELECT TOP 10 ID,Path FROM Subject0 WHERE ID < 1001438991 ORDER BY ID
		// DESC

		ResultSet rs = null;
		Statement stmt = con.createStatement();
		String sql = "SELECT TOP " + limit
				+ " ID,Path,Content FROM Subject0 WHERE ID < [maxID] ORDER BY ID DESC";
		if (maxID == 0) {
			sql = "SELECT TOP "
					+ limit
					+ " ID,Path,Content FROM Subject0 WHERE ID <= (SELECT MAX(ID) FROM Subject0) ORDER BY ID DESC";
		} else {
			sql = sql.replace("[maxID]", maxID + "");
		}
		rs = stmt.executeQuery(sql);
		return rs;
	}
	
	public static int getTotalArticle()
			throws SQLException {
		ResultSet rs = null;
		Statement stmt = con.createStatement();
		String sql = "SELECT COUNT(ID) as total FROM Subject0";
		int total = 0;		
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			total = rs.getInt("total");
		}
		rs.close();
		return total;
	}
	
	public static long fetchArticle(long maxId, int limit) throws Exception{
		ResultSet rs = getSubjectPaths(maxId, limit);
		while (rs.next()) {
			maxId = rs.getLong("ID");
			String path = rs.getString("Path");
			System.out.println(maxId + " - " + path + "- Content= "+rs.getString("Content"));
			
			String content = null;
			if( ! forceUpdateContent){
				content = rs.getString("Content");
			}			
			
			if(content == null || "".equals(content)){
				String fulLink = VnExpressUtils.getFullLink(path);			
				if(!fulLink.isEmpty()){
					String html = HttpClientUtil.executeGet(fulLink);
					if (html.isEmpty()||html.equals("500")) {	
						System.err.println("http get fail, 500 server error");				
					} else if(html.equals("404")){
						System.err.println("Link die!!!");
					} else {
						Parser parser = VnExpressUtils.getParser(path);
						if(parser!=null){
							Article oldArticle = new Article();
							oldArticle.setID(maxId);
							final Article newArticle = parser.parseHtmlToArticle(fulLink, html, oldArticle , null);
							new Thread(new Runnable() {								
								@Override
								public void run() {
									boolean updated;
									try {
										updated = updateArticleContent(newArticle);
										articles.put(newArticle.getID(), newArticle.getHeadline());
										System.out.println(newArticle.getID() + " #### updated = " + updated);
										System.out.println("### content \n " + newArticle.getContent());
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}																		
								}
							}).start();
						}
					}					
				}
			} else {
				System.out.println("### content \n " + content);
			}
		}		
		rs.close();
		return maxId;
	}
	
	public static void Close() throws Exception {
		con.close();
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Kiểm tra kết nối ...");
		ConnectWithDriver();
		

		long maxId = 0;
		int limit = 5;
		int total = getTotalArticle();
		int numTest = 5, jobIndex = 0;
		forceUpdateContent = true;
		
		System.out.println("total: "+total);
		
		while(jobIndex < numTest){
			maxId = fetchArticle(maxId, limit);
			jobIndex += limit;
		}
		
		Close();
	}

}
