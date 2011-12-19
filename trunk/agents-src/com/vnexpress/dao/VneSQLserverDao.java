package com.vnexpress.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.brain2.ws.core.utils.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.vnexpress.manager.ImporterConfigs;
import com.vnexpress.manager.VnExpressUtils;
import com.vnexpress.model.Article;
import com.vnexpress.model.Comment;
import com.vnexpress.model.ReferenceObject;
import com.vnexpress.model.Topic;
import com.vnexpress.parser.Parser;

public class VneSQLserverDao {

	private static Connection con = null;
	//private static HashMap<Long,String> articles = new HashMap<Long, String>();
	private static boolean forceUpdateContent = false;
	private static volatile int totalJob = 0;
	private static volatile int totalJobDone = 0;
	private static volatile int jobCount = 0;
	
	public static synchronized int getTotalJob() {
		return totalJob;
	}

	public static synchronized int getTotalJobDone() {
		return totalJobDone;
	}

	public static synchronized int getJobCount() {
		return jobCount;
	}

	protected static Connection ConnectWithDriver() throws Exception {	
		if(con == null ){
			ImporterConfigs configs = ImporterConfigs.loadFromFile("/importer-mssql-configs.json");
			//jdbc:sqlserver://10.254.53.101;databaseName=VNEAPP
			//jdbc:sqlserver://10.254.53.101;databaseName=VNEAPP

			System.out.println("DATABASE SQLServerConnectionUrl: "+configs.getSQLServerConnectionUrl());	
			if("sqlserver".equals(configs.getDbdriver())){				
				Class.forName(configs.getDbdriverclasspath());
				con = DriverManager.getConnection(configs.getSQLServerConnectionUrl(), configs.getUsername(), configs.getPassword());
				//System.out.println("Connection Catalog: "+con.getCatalog());			
			} else {
				throw new IllegalArgumentException("importer-mssqls-configs.json was not config correctly!");
			}
		}
		return con;
	}

	public final static boolean updateArticleContent(Article article) throws SQLException {
		if("".equals(article.getContent())) {
			return false;
		}
		totalJobDone++;
		String sql = "UPDATE Subject0 SET Content = ? WHERE ID = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setString(1, article.getContent());
		ps.setLong(2, article.getID());
		boolean rs =  ps.executeUpdate() > 0;
		deleteObjectReference(article.getID());
		saveRefObj(article);
		return rs;
	}
	
	public final static boolean updateCommentArticleContent(String content, long id) throws Exception {
		if("".equals(content)) {
			return false;
		}		
		String sql = "UPDATE Comment SET Content = ? WHERE ID = ?";		
		PreparedStatement ps = ConnectWithDriver().prepareStatement(sql);
		ps.setString(1, content);
		ps.setLong(2, id);
		boolean rs =  ps.executeUpdate() > 0;
		System.out.println("updateCommentArticleContent: id = " + id  + " rs " + rs);
		return rs;
	}
	
	public final static boolean deleteObjectReference(long articleId) throws SQLException {
		String sql = "DELETE FROM ObjectReference";
		if(articleId > 0){
			sql = "DELETE FROM ObjectReference WHERE ArticleID = "  + articleId;
		}		
		PreparedStatement ps = con.prepareStatement(sql);		
		boolean rs =  ps.executeUpdate() > 0;		
		return rs;
	}
	
	public static void saveRefObj(Article article) throws SQLException{
//		System.out.println("BEGIN SAVE DB ReferenceObject");
		con.setAutoCommit(false);
		String sql = "INSERT INTO ObjectReference(ArticleID,Md5,Url,Credit,Caption,Type,Status,CreationTime,UpdateTime) VALUES(?,?,?,?,?,?,?,?,?);";
		PreparedStatement ps = con.prepareStatement(sql);
		List<ReferenceObject> objs = article.getRefObj();
		for(int i=0,n=objs.size();i<n;i++){
			ReferenceObject obj = objs.get(i);
			ps.setLong(1, article.getID());
			ps.setString(2, obj.getMd5());
						
			String url = obj.getUrl().toLowerCase();			
			if(url.startsWith("/gl/")){						
				if(url.endsWith("/")){
					url = url.substring(0, url.lastIndexOf("/") - 1);
				}
				ps.setString(3, getOldSubjectIdByPath(url) );
			} else {
				ps.setString(3, url);	
			}			
			ps.setString(4, obj.getCredit());
			ps.setString(5, obj.getCaption());
			ps.setInt(6, obj.getType().index());
			ps.setInt(7, ReferenceObject.STATUS_VISIBLE);
			ps.setInt(8, 0 );
			ps.setInt(9, 0 );
			ps.addBatch();
		}
		ps.executeBatch();
		con.commit();
		//conn.setAutoCommit(true);
//		System.out.println("END SAVE DB ReferenceObject");
	}
	
	public boolean existTopic(String topicID) throws SQLException {
		String sql = "SELECT count(*) as total FROM topic_detail WHERE topic_id=?";
		PreparedStatement ps = con.prepareStatement(sql);			
		ps.setString(1, topicID);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
			if(rs.getInt("total")>0)
				return true;
		Log.println("NO ESIST TOPIC:"+topicID);
		return false;
	}
	
	public boolean saveArticleTopic(List<Topic> refTopics,String articleId,Date createTime,Date updateTime){
		try {
			if(refTopics.size() <=0 || articleId == null){
				return false;
			}
			con.setAutoCommit(false);
			String sql = "INSERT INTO article_topic VALUES(?,?,?,?,?) ";
			PreparedStatement ps = con.prepareStatement(sql);
			for(int i=0,n=refTopics.size();i<n;i++){
				Topic topic = refTopics.get(i);
				if(existTopic(topic.getId())){
					ps.setString(1, topic.getId());
					ps.setString(2, articleId);
					ps.setInt(3, 0);
					ps.setInt(4,VnExpressUtils.getIntTimeInSecond(createTime.getTime()));
					ps.setInt(5,VnExpressUtils.getIntTimeInSecond(updateTime.getTime()));
					ps.addBatch();
				}
			}
			ps.executeBatch();
			con.commit();
		} catch (MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
			Log.println("::::EXCEPTION MySQLIntegrityConstraintViolationException SAVE TOPIC ARTICLE");
			return true;
		}catch(Exception e){
			e.printStackTrace();
			Log.println("::::EXCEPTION SAVE TOPIC ARTICLE");
			return false;
		}
		return true;
	}
	
	private static String getOldSubjectIdByPath(String path) throws SQLException {
		String sql = "SELECT ID FROM subject0 WHERE subject0.Path = ? ";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setString(1, path);		
		
		ResultSet resultSet = ps.executeQuery();
		String id = path;
		while (resultSet.next()) {			
			id = resultSet.getString("ID");			
		}		
		ps.close();
		resultSet.close();		
		return id;
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
				+ " ID,Path,Content,Date,Modified,Folder FROM Subject0 WHERE ID < [maxID] ORDER BY ID DESC";
		if (maxID == 0) {
			sql = "SELECT TOP "
					+ limit
					+ " ID,Path,Content,Date,Modified,Folder FROM Subject0 WHERE ID <= (SELECT MAX(ID) FROM Subject0) ORDER BY ID DESC";
		} else {
			sql = sql.replace("[maxID]", maxID + "");
		}
		rs = stmt.executeQuery(sql);
		return rs;
	}
	
	public static ResultSet getSubjectPath(long ID)
			throws SQLException {
		ResultSet rs = null;
		Statement stmt = con.createStatement();
		String sql = "SELECT ID,Path,Content,Date,Modified,Folder FROM Subject0 WHERE ID = [ID]";
		sql = sql.replace("[ID]", ID + "");		
		rs = stmt.executeQuery(sql);
		return rs;
	}
	
	public static List<Comment> fetchAllCommentsArticle(long articleId)	{
		List<Comment> comments = new ArrayList<Comment>();;
		try {
			
			String sql = "SELECT ID, Path, Content FROM Comment WHERE intID = " + articleId;
			System.out.println(sql);			
			PreparedStatement stmt = ConnectWithDriver().prepareStatement(sql);		
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String content = rs.getString("Content");
				String path = rs.getString("Path");
				long commentId = rs.getLong("ID");
				System.out.println("...starting update comment id "+commentId + " content " + content);
				//if( content == null )
				{
					parseCommentArticle(path, commentId);					
				}
			}
			rs.close();
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return comments;
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
	
	public static Article fetchArticle(long articleID, int limit) throws Exception{
		ResultSet results = null; 
		Article lastParsedArticle = null;
		if(limit == 1){
			results = getSubjectPath(articleID);
		} else if(limit > 1) {
			getSubjectPaths(articleID, limit);
		} 
		if(results == null){
			return null;
		}
		while (results.next()) {
			jobCount++;
			articleID = results.getLong("ID");
			String path = results.getString("Path");
						
			String content = null;
			if( ! forceUpdateContent){
				content = results.getString("Content");
			}
			System.out.println(articleID + " - " + path + "- fetchArticled OK");
			
			if(content == null || "".equals(content)){
				String fulLink = VnExpressUtils.getFullLinkOfVNEDomain(path);			
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
							oldArticle.setID(articleID);
							oldArticle.setCreationDate(results.getDate("Date"));
							oldArticle.setUpdateDate(results.getDate("Modified"));
							oldArticle.setCatID(results.getInt("Folder"));
							
							final Article newArticle = parser.parseHtmlToArticle(fulLink, html, oldArticle , null);
							lastParsedArticle = newArticle;
							new Thread(new Runnable() {								
								@Override
								public void run() {
									boolean updated;
									try {
										updated = updateArticleContent(newArticle);	
										fetchAllCommentsArticle(newArticle.getID());
										//articles.put(newArticle.getID(), newArticle.getHeadline());
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
				}
			} else {
				System.out.println("### content \n " + content);
			}
		}		
		results.close();
		return lastParsedArticle;
	}
	
	public static void Close()  {
		try {
			if( ! con.isClosed()){
				con.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String parseCommentArticle(final String path, final long commentId){
		String commentContent = "";
		String url = "http://vnexpress.net" + path;		
		String html = HttpClientUtil.executeGet(url);		
		if (html.isEmpty()||html.equals("500")) {	
			System.err.println("http get fail, 500 server error");				
		} else if(html.equals("404")){
			System.err.println("Link die!!!");
		} else {
			final Document doc = Jsoup.parse(html, HTTP.UTF_8);			
			Elements contents = doc.select(".content");			
			if (contents.size() > 0) {
				Element content = contents.get(0);

				Elements cpms_content = content.select("div[cpms_content=true]");
				if (cpms_content.size() > 0) {
					Element cpms = cpms_content.get(0);
					cpms.select("script").remove();
					final Elements nodes = cpms.select(".Normal");
					if (nodes.size() > 1) {
						commentContent = nodes.get(0).text();
						final String commentContent2 = commentContent;
						new Thread(new Runnable() {								
							@Override
							public void run() {						
								try {			
									System.out.println("updateCommentArticleContent: " + commentContent2.length());
									updateCommentArticleContent(commentContent2, commentId);
								} catch (Exception e) {									
									e.printStackTrace();
								}																		
							}
						}).start();	
					}
				}
			}
		}
		
		return commentContent;
	}
	
	public static String parseArticle(final String path, final long artilceId, final boolean forceUpdate){
		String content = "";
		String fulLink = VnExpressUtils.getFullLinkOfVNEDomain(path);
		Parser parser = VnExpressUtils.getParser(path);
		
		if(parser!=null){
			String html = HttpClientUtil.executeGet(fulLink);
			if (html.isEmpty()||html.equals("500")) {	
				System.err.println("http get fail, 500 server error");				
			} else if(html.equals("404")){
				System.err.println("Link die!!!");
			} else {
				Article oldArticle = new Article();
				oldArticle.setID(artilceId);			
				
				try {
					Article newArticle = parser.parseHtmlToArticle(fulLink, html, oldArticle , null);
					content = newArticle.getContent();
				} catch (Exception e) {
					e.printStackTrace();
				}
				//asyn call
				if(artilceId > 0){
					new Thread(new Runnable() {								
						@Override
						public void run() {						
							try {
								System.out.println("fetchArticle: " + path);
								ConnectWithDriver();
								forceUpdateContent = forceUpdate;
								fetchArticle(artilceId, 1);
								//Close();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}																		
						}
					}).start();	
				}
			}
		}
		
		
		return content;
	}
	
	public static void parseOldArticle(final int limitRecord,final boolean forceUpdate){
		try {
			System.out.println("Kiểm tra kết nối ...");
			ConnectWithDriver();

			long maxId = 0;
			int limit = 5;
			int total = getTotalArticle();
			int jobIndex = 0;
			forceUpdateContent = forceUpdate;
			
			System.out.println("total: "+total);
			
			totalJob = limitRecord;
			while(jobIndex <= limitRecord){
				Article a = fetchArticle(maxId, limit);
				if(a == null){
					Close();
					System.out.println("Not found");
					return;
				}
				maxId = a.getID();
				jobIndex += limit;
			}
			//Close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		ConnectWithDriver();
		forceUpdateContent = true;
//		long artilceId = 1001322413;
//		fetchArticle(artilceId , 1);
		fetchAllCommentsArticle(1000498556);
		//System.out.println(updateCommentArticleContent("ok", 1001098487)); ;
		//ConnectWithDriver();
	}

}
