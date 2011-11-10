package org.brain2.test.vneappcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
	
	
	public Article getArticleByPath(String path) throws Exception {
		String sql = "SELECT ID,Title,Lead,PostBy,Date,Modified,Path FROM vnemobile.subject0 WHERE subject0.Path = ?";
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
	
	public ResultSet getSubjectPath(int begin, int total) throws Exception {
		String sql = "SELECT ID,Title,Lead,PostBy,Date,Modified,Path FROM vnemobile.subject0 LIMIT ?,? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, begin);
		ps.setInt(2, total);
		
		ResultSet rs = ps.executeQuery();
		//ps.close();				
		return rs;
	}
	
	public boolean saveArticle(Article article) throws SQLException{
		System.out.println("SAVE DB article"+article.getSharedURL());
		conn.setAutoCommit(false);
		String sql = "INSERT INTO article(article_id,headline, abstract,content,share_url,creation_time,update_time) VALUES(?,?,?,?,?,?,?) ";
		PreparedStatement ps = conn.prepareStatement(sql);
		List<Comment> comments = new ArrayList<Comment>(article.getComments());			
		ps.setString(1, article.getId());
		ps.setString(2, article.getHeadline());
		ps.setString(3, article.getAbstractS());
		ps.setString(4, article.getContent());
		ps.setString(5, article.getSharedURL());
		ps.setLong(6, article.getCreationDate().getTime());
		ps.setLong(7, article.getUpdateDate().getTime());
		boolean rs = ps.execute();			
		conn.commit();		
		saveComment(comments);				
		return rs;
	}
	
	public void saveArticle(final Queue<Article> articles) throws SQLException{
		System.out.println("BEGIN SAVE DB COMMENT");
		conn.setAutoCommit(false);
		String sql = "INSERT INTO article(article_id,headline, abstract,content,share_url,creation_time,update_time) VALUES(?,?,?,?,?,?,?) ";
		PreparedStatement ps = conn.prepareStatement(sql);
		List<Comment> comments = new ArrayList<Comment>();
		for(int i=0;i<=10&&i<articles.size();i++){
			System.out.println("i :" +i);
			Article article = articles.poll();
			ps.setString(1, article.getId());
			ps.setString(2, article.getHeadline());
			ps.setString(3, article.getAbstractS());
			ps.setString(4, article.getContent());
			ps.setString(5, article.getSharedURL());
			ps.setLong(6, article.getCreationDate().getTime());
			ps.setLong(7, article.getUpdateDate().getTime());
			ps.addBatch();
			comments.addAll(article.getComments());
		}
		
		ps.executeBatch();
		conn.commit();
		
		saveComment(comments);
		
		//conn.setAutoCommit(true);
		System.out.println("END SAVE DB ARTICLE");
	}
	public ResultSet getComment(String url) throws Exception {
		String sql = "SELECT * FROM vnemobile.comment_vne where Link=? order by PublishDate asc";
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
}
