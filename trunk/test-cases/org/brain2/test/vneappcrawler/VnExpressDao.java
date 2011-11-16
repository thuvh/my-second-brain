package org.brain2.test.vneappcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

public class VnExpressDao {
	//DELETE FROM `comment`; DELETE FROM `article`;

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
		ImporterConfigs configs = ImporterConfigs.loadFromFile("/importer-configs.json");		
		if("mysql".equals(configs.getDbdriver())){
			Class.forName(configs.getDbdriverclasspath()).newInstance();
			this.conn = DriverManager.getConnection(configs.toConnectionUrl(), configs.getUsername(), configs.getPassword());
			System.out.println("Database connection established");
			return this.conn;
		}
		throw new IllegalArgumentException("importer-configs.json was not config correctly!");
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
	 * total records of vnexpress subject, domain vnexpress.net only
	 * 
	 * @return int
	 * @throws Exception
	 */
	public int getTotalCountInVnExpress() throws SQLException {
		String sql = "SELECT count(ID) as total FROM subject0 WHERE subject0.Path LIKE '/gl/%'";
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
	
	public boolean isExistArticle(Article article) throws SQLException {
		String sql = "SELECT count(`article_id`) as total FROM article WHERE article_id = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, article.getId());
		ResultSet rs = ps.executeQuery();
		int total = 0;
		while (rs.next()) {
			total = rs.getInt("total");
		}
		rs.close();
		ps.close();
		return total == 1;
	}
	
	
	public Article getOldSubjectByPath(String path) throws SQLException {
		String sql = "SELECT ID,Title,Lead,PostBy,Date,Modified,Path FROM subject0 WHERE subject0.Path = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, path);		
		
		ResultSet resultSet = ps.executeQuery();
		Article article = null;
		while (resultSet.next()) {
			article = new Article(resultSet.getString("ID"), resultSet.getString("Title"), resultSet.getString("Lead"),resultSet.getString("Path"),0,resultSet.getDate("Date"),resultSet.getDate("Modified"));
			break;
		}
		
		ps.close();
		resultSet.close();		
		return article;
	}
	
	public String getOldSubjectIdByPath(String path) throws SQLException {
		String sql = "SELECT ID FROM subject0 WHERE subject0.Path = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
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
	
	public long getArticleIdByPath(String path) throws SQLException {
		String sql = "SELECT article_id FROM article WHERE share_url = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, path);		
		
		ResultSet resultSet = ps.executeQuery();
		long id = 0;
		while (resultSet.next()) {			
			id = resultSet.getLong("article_id");			
		}		
		ps.close();
		resultSet.close();		
		return id;
	}
	
	/**
	 * 
	 * all links in domain vnexpress.net only
	 * 
	 * @param begin
	 * @param total
	 * @return
	 * @throws SQLException
	 */
	public ResultSet getSubjectPathInVnExpress(int begin, int total) throws SQLException {
		String sql = "SELECT ID,Title,Lead,PostBy,Date,Modified,Path FROM subject0 WHERE Path LIKE '/gl/%' LIMIT ?,? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, begin);
		ps.setInt(2, total);
		
		ResultSet rs = ps.executeQuery();
		//ps.close();				
		return rs;
	}
	
	public boolean deleteReferDataOfArticle(Article article) throws SQLException{
		conn.setAutoCommit(false);
		
		String sql = "DELETE FROM comment WHERE article_id = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, article.getId());
		ps.execute();	
		
		String sql2 = "DELETE FROM object_reference WHERE article_id = ? ";
		PreparedStatement ps2 = conn.prepareStatement(sql2);
		ps2.setString(1, article.getId());
		ps2.execute();	
		
		String sql3 = "DELETE FROM topic_article WHERE article_id = ? ";
		PreparedStatement ps3 = conn.prepareStatement(sql3);
		ps3.setString(1, article.getId());
		ps3.execute();
		
		conn.commit();
		ps.close();
		ps2.close();
		ps3.close();
		return true;
	}
	
	public boolean updateArticle(Article article) throws SQLException{
		//FIXME
		deleteReferDataOfArticle(article);
		System.out.println("UPDATE DB article: "+article.getSharedURL());
		conn.setAutoCommit(false);
		String sql = "UPDATE article SET thumbnail_md5 = ?,thumbnail_url = ?,content=?  WHERE article_id = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);		
		ps.setString(1, article.getThumbnailMD5());
		ps.setString(2, article.getThumbnailURL());
		ps.setString(3, article.getContent());
		ps.setString(4, article.getId());
		boolean rs = ps.execute();			
		conn.commit();
		saveComment(new ArrayList<Comment>(article.getComments()));		
		saveRefObj(article.getRefObj());
		saveTopicArticle(article.getTopicID(),article.getId());
		return rs;
	}
	
	public boolean saveArticle(Article article) throws SQLException{
		System.out.println("SAVE DB article: "+article.getSharedURL());
		conn.setAutoCommit(false);
		String sql = "INSERT INTO article(article_id,headline, abstract,content,is_delete,share_url,thumbnail_md5,thumbnail_url,creation_time,update_time) VALUES(?,?,?,?,?,?,?,?,?,?) ";
		PreparedStatement ps = conn.prepareStatement(sql);			
		ps.setString(1, article.getId());
		ps.setString(2, article.getHeadline());
		ps.setString(3, article.getAbstractS());
		ps.setString(4, article.getContent());
		ps.setInt(5, 0);
		ps.setString(6, article.getSharedURL());
		ps.setString(7, article.getThumbnailMD5());
		ps.setString(8, article.getThumbnailURL());
		ps.setLong(9, article.getCreationDate()!=null ? article.getCreationDate().getTime():0);
		ps.setLong(10, article.getUpdateDate()!=null ? article.getUpdateDate().getTime():0);
		boolean rs = ps.execute();			
		conn.commit();		
		saveComment(new ArrayList<Comment>(article.getComments()));		
		saveRefObj(article.getRefObj());
		saveTopicArticle(article.getTopicID(),article.getId());
		return rs;
	}
	
	public boolean saveTopicArticle(String topicID, String articleId) throws SQLException {
		if(topicID == null || articleId == null){
			return false;
		}
		String sql = "INSERT INTO topic_article VALUES(?,?,?,?,?) ";
		PreparedStatement ps = conn.prepareStatement(sql);			
		ps.setString(1, topicID);
		ps.setString(2, articleId);
		ps.setInt(3, 0);
		ps.setLong(4, new Date().getTime());
		ps.setLong(5, new Date().getTime());
		boolean rs = ps.execute();			
		conn.commit();
		return rs;
	}

	public void saveArticle(final Queue<Article> articles) throws SQLException{
		System.out.println("BEGIN SAVE DB COMMENT");
		conn.setAutoCommit(false);
		String sql = "INSERT INTO article(article_id,headline, abstract,content,is_delete,share_url,thumbnail_md5,thumbnail_url,creation_time,update_time) VALUES(?,?,?,?,?,?,?,?,?,?) ";
		PreparedStatement ps = conn.prepareStatement(sql);
		List<Comment> comments = new ArrayList<Comment>();
		for(int i=0;i<=10&&i<articles.size();i++){
			System.out.println("i :" +i);
			Article article = articles.poll();
			ps.setString(1, article.getId());
			ps.setString(2, article.getHeadline());
			ps.setString(3, article.getAbstractS());
			ps.setString(4, article.getContent());
			ps.setInt(5, 0);
			ps.setString(6, article.getSharedURL());
			ps.setString(7, article.getThumbnailMD5());
			ps.setString(8, article.getThumbnailURL());
			ps.setLong(9, article.getCreationDate()!=null ? article.getCreationDate().getTime():0);
			ps.setLong(10, article.getUpdateDate()!=null ? article.getUpdateDate().getTime():0);
			ps.addBatch();
			comments.addAll(article.getComments());
		}
		
		ps.executeBatch();
		conn.commit();
		
		saveComment(comments);
		
		//conn.setAutoCommit(true);
		System.out.println("END SAVE DB ARTICLE");
	}
	public ResultSet getComment(String url) throws SQLException {
		String sql = "SELECT * FROM comment_vne where Link=? order by PublishDate asc";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, url);		
		ResultSet rs = ps.executeQuery();
		return rs;
	}
	
	public void saveComment(List<Comment> comments) throws SQLException{
		System.out.println("BEGIN SAVE DB COMMENT");
		System.out.println("COMMENT SIZE: " + comments.size());
		conn.setAutoCommit(false);
		String sql = "INSERT INTO comment VALUES(?,?,?,?,?,?,?,?,?,?,?);";
		PreparedStatement ps = conn.prepareStatement(sql);
		for(int i=0,n=comments.size();i<n;i++){
			System.out.println("ci :" +i);
			Comment comment = comments.get(i);
			ps.setString(1, comment.getCommentId());
			ps.setString(2, comment.getArticleId());
			ps.setString(3, comment.getTitle());
			ps.setString(4, comment.getContent());
			ps.setString(5, comment.getUserId());
			ps.setString(6, comment.getFullname());
			ps.setString(7, comment.getEmail());
			ps.setInt(8, comment.getStatus());
			ps.setLong(9, comment.getPublicTime().getTime());
			ps.setLong(10, comment.getCreationTime().getTime());
			ps.setLong(11, comment.getUpdateDate().getTime());
			ps.addBatch();
		}
		ps.executeBatch();
		conn.commit();
		
		//conn.setAutoCommit(true);
		System.out.println("END SAVE DB COMMENT");
	}
	public void saveRefObj(List<ReferenceObject> objs) throws SQLException{
		System.out.println("BEGIN SAVE DB ReferenceObject");
		System.out.println("ReferenceObject SIZE: " + objs.size());
		conn.setAutoCommit(false);
		String sql = "INSERT INTO object_reference(article_id,md5,url,credit,caption,type,update_time) VALUES(?,?,?,?,?,?,?);";
		PreparedStatement ps = conn.prepareStatement(sql);
		for(int i=0,n=objs.size();i<n;i++){
			System.out.println("oi :" +i);
			ReferenceObject obj = objs.get(i);
			ps.setString(1, obj.getArticleID());
			ps.setString(2, obj.getMd5());
			
			
			String url = obj.getUrl();
			
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
			ps.setLong(7, obj.getUpdateTime()!=null ?obj.getUpdateTime().getTime():null);
			ps.addBatch();
		}
		ps.executeBatch();
		conn.commit();
		
		//conn.setAutoCommit(true);
		System.out.println("END SAVE DB ReferenceObject");
	}
}
