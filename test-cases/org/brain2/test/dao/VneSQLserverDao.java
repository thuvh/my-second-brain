package org.brain2.test.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import org.brain2.test.vneappcrawler.Article;
import org.brain2.test.vneappcrawler.ImporterConfigs;
import org.brain2.test.vneappcrawler.Parser;
import org.brain2.test.vneappcrawler.VnExpressUtils;
import org.brain2.ws.core.utils.HttpClientUtil;

public class VneSQLserverDao {
	
	private static RecordManager recordManager;
	private static PrimaryTreeMap<String,String> cachedArticlesDB;
	static final String recordManagerName = "cache/vne_crawling";
	static final String vneCrawlingDBName = "vne_crawling_cache";
	
	private static Connection con = null;
	private static HashMap<Long,String> articles = new HashMap<Long, String>();
	private static boolean forceUpdateContent = false;
	
	static {		
		if(cachedArticlesDB == null || recordManager==null ){
			try {
				/** create (or open existing) database */				
				recordManager = RecordManagerFactory.createRecordManager(recordManagerName);
				
				/** Creates TreeMap which stores data in database.  
				 *  Constructor method takes recordName (something like SQL table name)*/
				
				cachedArticlesDB = recordManager.treeMap(vneCrawlingDBName);				
			} catch (IOException e) {			
				e.printStackTrace();
			}	
		}
	}

	protected static void ConnectWithDriver() throws Exception {		
		ImporterConfigs configs = ImporterConfigs.loadFromFile("/importer-mssql-configs.json");	
		System.out.println("DATABASE SQLServerConnectionUrl: "+configs.getSQLServerConnectionUrl());	
		
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
		return ps.executeUpdate() > 0;
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
				+ " ID,Path,Content FROM Subject0 WHERE ID < [maxID] AND Content IS NULL ORDER BY ID DESC";
		if (maxID == 0) {
			sql = "SELECT TOP "
					+ limit
					+ " ID,Path,Content FROM Subject0 WHERE ID <= (SELECT MAX(ID) FROM Subject0) AND Content IS NULL ORDER BY ID DESC";
		} else {
			sql = sql.replace("[maxID]", maxID + "");
		}
		rs = stmt.executeQuery(sql);
		return rs;
	}
	
	
	
	public static int getTotalNotEmptyArticle()
			throws SQLException {
		ResultSet rs = null;
		Statement stmt = con.createStatement();
		String sql = "SELECT COUNT(ID) as total FROM Subject0 WHERE Content IS NOT NULL";
		int total = 0;		
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			total = rs.getInt("total");
		}
		rs.close();
		return total;
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
			System.out.println(maxId + " - " + path + "- Content FROM DB: "+(rs.getString("Content") == null));
			
			String content = null;
			if( ! forceUpdateContent){
				content = rs.getString("Content");
			}			
			
			if(content == null || "".equals(content)){
				String fulLink = VnExpressUtils.getFullLink(path);
				if(!fulLink.isEmpty()){
					System.out.println("### update content ");
					final String cachedkey = maxId + "-" + fulLink;
					cachedArticlesDB.put(cachedkey, "");
					
					String html = HttpClientUtil.executeGet(fulLink);
					if (html.isEmpty()||html.equals("500")) {						
						cachedArticlesDB.put(cachedkey, "500");
						System.err.println("http get fail, 500 server error");				
					} else if(html.equals("404")){
						System.err.println("Link die!!!");
					} else {
						cachedArticlesDB.put(cachedkey, "200");
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
										cachedArticlesDB.put(cachedkey, ""+updated);
										try {
											recordManager.commit();			
										} catch (Exception e) {			
											e.printStackTrace();
										}
										articles.put(newArticle.getID(), newArticle.getHeadline());
										System.out.println(newArticle.getID() + " #### updated = " + updated);
										//System.out.println("### content \n " + newArticle.getContent());
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}																		
								}
							}).start();
						}
					}
					try {						
						recordManager.commit();			
					} catch (Exception e) {			
						e.printStackTrace();
					}
				}
			} else {
				//System.out.println("### content \n " + content);
				System.out.println("### Skip ");
			}
		}		
		rs.close();
		int totalNotEmpty = getTotalNotEmptyArticle();
		System.out.println(" ###---------------------------------------totalNotEmpty: "+totalNotEmpty);
		return maxId;
	}
	
	public static void Close() throws Exception {
		con.close();
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Kiểm tra kết nối ...");
		ImporterConfigs configs = ImporterConfigs.loadFromFile("/importer-mssql-configs.json");	
		System.out.println("DATABASE SQLServerConnectionUrl: "+configs.getSQLServerConnectionUrl());	
		
		if("sqlserver".equals(configs.getDbdriver())){
			Class.forName(configs.getDbdriverclasspath());
			con = DriverManager.getConnection(configs.getSQLServerConnectionUrl(), configs.getUsername(), configs.getPassword());
			System.out.println("Connection Catalog: "+con.getCatalog());			
		} else {		
			throw new IllegalArgumentException("importer-mssqls-configs.json was not config correctly!");
		}		

		long maxId = Integer.parseInt(args[0]);
		int limit = 20;
		int total = getTotalArticle();
		int numTest = total, jobIndex = 0;
		forceUpdateContent = false;
		
		System.out.println("total: "+total);
		System.out.println("maxId: "+maxId);
		
		while(jobIndex < numTest){
			maxId = fetchArticle(maxId, limit);
			jobIndex += limit;
		}
		
		//Close();
	}

}
