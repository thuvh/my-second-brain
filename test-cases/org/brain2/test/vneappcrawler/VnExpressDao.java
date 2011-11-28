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

import org.brain2.ws.core.utils.Log;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class VnExpressDao implements VneDataManager {
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

	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#closeConnection()
	 */
	@Override
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
			this.conn = DriverManager.getConnection(configs.getMySQLConnectionUrl(), configs.getUsername(), configs.getPassword());
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

	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#getTotalCountInVnExpress()
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#isExistedArticle(org.brain2.test.vneappcrawler.Article)
	 */
	@Override
	public boolean isExistedArticle(Article article) throws SQLException {
		String sql = "SELECT count(`article_id`) as total FROM article WHERE article_id = ? OR share_url = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, article.getId());
		ps.setString(2, article.getSharedURL());
		ResultSet rs = ps.executeQuery();
		int total = 0;
		while (rs.next()) {
			total = rs.getInt("total");
		}
		rs.close();
		ps.close();
		return total == 1;
	}
	
	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#isExistedArticleByID(java.lang.String)
	 */
	@Override
	public boolean isExistedArticleByID(String id) throws SQLException {
		String sql = "SELECT count(`article_id`) as total FROM article WHERE article_id = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, id);		
		ResultSet rs = ps.executeQuery();
		int total = 0;
		while (rs.next()) {
			total = rs.getInt("total");
		}
		rs.close();
		ps.close();
		return total == 1;
	}
	
	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#isExistedArticle(java.lang.String)
	 */
	@Override
	public boolean isExistedArticle(String path) throws SQLException {
		String sql = "SELECT count(`article_id`) as total FROM article WHERE share_url = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, path);
		ResultSet rs = ps.executeQuery();
		int total = 0;
		while (rs.next()) {
			total = rs.getInt("total");
		}
		rs.close();
		ps.close();
		return total > 0;
	}
	
	/**
	 * generate temp ID for hot news (mostly not in DB VnExpress)
	 * 
	 * @param path
	 * @return
	 * @throws SQLException
	 */
	public int getHotArticleId(String path) throws SQLException {
		if(isExistedArticle(path)){
			return 0;
		}		
		String sql = "SELECT MAX(article.article_id)+1 as nextHotArticleId FROM article WHERE article.article_id < 1000000000";
		PreparedStatement ps = conn.prepareStatement(sql);		
		ResultSet rs = ps.executeQuery();
		int id = 0;
		if(rs.next()) {
//			System.out.println("ss");
			id = rs.getInt("nextHotArticleId");
		} else {
//			System.out.println("ss4");
			return 1;
		}
		rs.close();
		ps.close();		
		return id;
	}
	
	
	public Article getOldSubjectByPath(String path) throws SQLException {
		String sql = "SELECT ID,Title,Lead,PostBy,Date,Modified,Path FROM subject0 WHERE subject0.Path = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, path);		
		
		ResultSet resultSet = ps.executeQuery();
		Article article = null;
		while (resultSet.next()) {
			article = new Article(resultSet.getString("ID"),resultSet.getString("PostBy") ,resultSet.getString("Title"), resultSet.getString("Lead"),resultSet.getString("Path"),0,resultSet.getDate("Date"),resultSet.getDate("Modified"));
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
	
	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#getArticleIdByPath(java.lang.String)
	 */
	@Override
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
		String sql = "SELECT ID,Title,Lead,PostBy,Date,Modified,Path FROM subject0 WHERE Path LIKE '/gl/%' OR Path LIKE '/tin/%' LIMIT ?,? ";
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
	
	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#updateArticle(org.brain2.test.vneappcrawler.Article)
	 */
	@Override
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
		saveTopicArticle(article.getTopics(),article.getId(),article.getCreationDate(),article.getCreationDate());
		saveArticleTopic(article.getRefTopics(),article.getId(),article.getCreationDate(),article.getCreationDate());
		saveArticleCate(article.getCatID(), article.getId(), article.getCreationDate());
		return rs;
	}
	
	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#saveArticle(org.brain2.test.vneappcrawler.Article)
	 */
	@Override
	public boolean saveArticle(Article article) throws SQLException{		
		conn.setAutoCommit(false);
		String sql = "INSERT INTO article VALUES(?,?,?,?,?,?,?,?,?,null,?,?,null) ";
		PreparedStatement ps = conn.prepareStatement(sql);			
		ps.setString(1, article.getId());
		ps.setString(2, article.getAuthorID());
		ps.setString(3, article.getHeadline());
		ps.setString(4, article.getAbstractS());
		ps.setString(5, article.getContent());
		ps.setInt(6, 0);
		ps.setString(7, article.getSharedURL());
		ps.setString(8, article.getThumbnailMD5());
		ps.setString(9, article.getThumbnailURL());
		ps.setInt(10, article.getCreationDate()!=null ? VnExpressUtils.getIntTimeInSecond(article.getCreationDate().getTime()):0);
		ps.setInt(11, article.getUpdateDate()!=null ? VnExpressUtils.getIntTimeInSecond(article.getUpdateDate().getTime()):0);
		boolean rs = ps.execute();			
		conn.commit();
		saveRefObj(article.getRefObj());
		saveComment(new ArrayList<Comment>(article.getComments()));
		try {		
			//can skip this if save fail
//			List<Topic> reftopics = article.getRefTopics();
			
			saveTopicArticle(article.getTopics(),article.getId(),article.getCreationDate(),article.getCreationDate());
			saveArticleTopic(article.getRefTopics(),article.getId(),article.getCreationDate(),article.getCreationDate());
			saveArticleCate(article.getCatID(), article.getId(), article.getCreationDate());
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return rs;
	}
	
	public boolean saveTopicArticle(List<Topic> topics,String articleId,Date createTime,Date updateTime){
		try {
			if(topics.size() <=0 || articleId == null){
				return false;
			}
			for(Topic t: topics){
				if(!existTopic(t.getId())){
//					saveTopicDetail(t.getId(), t.getTitle());
					topics.remove(t);
				}
			}
			conn.setAutoCommit(false);
			String sql = "INSERT INTO topic_article VALUES(?,?,?,?,?) ";
			PreparedStatement ps = conn.prepareStatement(sql);
			for(int i=0,n=topics.size();i<n;i++){
				Topic topic = topics.get(i);
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
			conn.commit();
		} catch (MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
			Log.println("::::EXCEPTION MySQLIntegrityConstraintViolationException SAVE TOPIC ARTICLE");
//			saveTopicDetail(topicID, title);
//			saveTopicArticle(topicID, articleId, title,createTime,updateTime);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			Log.println("::::EXCEPTION SAVE TOPIC ARTICLE");
			return false;
		}
		return true;
	}
	public boolean saveArticleTopic(List<Topic> refTopics,String articleId,Date createTime,Date updateTime){
		try {
			if(refTopics.size() <=0 || articleId == null){
				return false;
			}
			conn.setAutoCommit(false);
			String sql = "INSERT INTO article_topic VALUES(?,?,?,?,?) ";
			PreparedStatement ps = conn.prepareStatement(sql);
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
			conn.commit();
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
	public boolean saveTopicDetail(String topicId,String title){
		try {
			String sql = "INSERT INTO topic_detail VALUES(?,?,null,null) ";
			PreparedStatement ps = conn.prepareStatement(sql);			
			ps.setString(1, topicId);
			ps.setString(2, title);
			boolean rs = ps.execute();			
			conn.commit();
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("::::EXCEPTION SAVE TOPIC DETAIL");
			return false;
		}
	}
	public boolean saveArticleCate(String cateId, String articleId,Date createDate){
		try {
			if(cateId == null || articleId == null){
				return false;
			}
			String sql = "INSERT INTO article_cate VALUES(?,?,null,0,?,?,?) ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, cateId);
			ps.setString(2, articleId);
			long time = (createDate != null)? createDate.getTime():0;
			ps.setInt(3,VnExpressUtils.getIntTimeInSecond(time));
			ps.setInt(4,VnExpressUtils.getIntTimeInSecond(time));
			ps.setInt(5, VnExpressUtils.getIntTimeInSecond(time));
			boolean rs = ps.execute();			
			conn.commit();
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("::::EXCEPTION SAVE ARTICLE CATE");
			return false;
		}
	}

	public void saveArticle(final Queue<Article> articles) throws SQLException{
//		System.out.println("BEGIN SAVE DB COMMENT");
		conn.setAutoCommit(false);
		String sql = "INSERT INTO article(article_id,headline, abstract,content,is_delete,share_url,thumbnail_md5,thumbnail_url,creation_time,update_time) VALUES(?,?,?,?,?,?,?,?,?,?) ";
		PreparedStatement ps = conn.prepareStatement(sql);
		List<Comment> comments = new ArrayList<Comment>();
		for(int i=0;i<=10&&i<articles.size();i++){
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
//		System.out.println("END SAVE DB ARTICLE");
	}
	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#getComment(java.lang.String)
	 */
	@Override
	public ResultSet getComment(String url) throws SQLException {
		String sql = "SELECT * FROM comment_vne where Link=? order by PublishDate asc";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, url);		
		ResultSet rs = ps.executeQuery();
		return rs;
	}
	
	public void saveComment(List<Comment> comments) throws SQLException{
//		System.out.println("BEGIN SAVE DB COMMENT");
		conn.setAutoCommit(false);
		String sql = "INSERT INTO comment VALUES(?,?,?,?,?,?,?,?,?,?,?);";
		PreparedStatement ps = conn.prepareStatement(sql);
		for(int i=0,n=comments.size();i<n;i++){
			Comment comment = comments.get(i);
			ps.setString(1, comment.getCommentId());
			ps.setString(2, comment.getArticleId());
			ps.setString(3, comment.getTitle());
			ps.setString(4, comment.getContent());
			ps.setString(5, comment.getUserId());
			ps.setString(6, comment.getFullname());
			ps.setString(7, comment.getEmail());
			ps.setInt(8, comment.getStatus());
			ps.setInt(9, VnExpressUtils.getIntTimeInSecond(comment.getPublicTime().getTime()));
			ps.setInt(10, VnExpressUtils.getIntTimeInSecond(comment.getCreationTime().getTime()));
			ps.setInt(11, VnExpressUtils.getIntTimeInSecond(comment.getUpdateDate().getTime()));
			ps.addBatch();
		}
		ps.executeBatch();
		conn.commit();
		
		//conn.setAutoCommit(true);
//		System.out.println("END SAVE DB COMMENT");
	}
	public void saveRefObj(List<ReferenceObject> objs) throws SQLException{
//		System.out.println("BEGIN SAVE DB ReferenceObject");
		conn.setAutoCommit(false);
		String sql = "INSERT INTO object_reference(article_id,md5,url,credit,caption,type,update_time) VALUES(?,?,?,?,?,?,?);";
		PreparedStatement ps = conn.prepareStatement(sql);
		for(int i=0,n=objs.size();i<n;i++){
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
			ps.setInt(7, obj.getUpdateTime()!=null ?VnExpressUtils.getIntTimeInSecond(obj.getUpdateTime().getTime()):null);
			ps.addBatch();
		}
		ps.executeBatch();
		conn.commit();
		
		//conn.setAutoCommit(true);
//		System.out.println("END SAVE DB ReferenceObject");
	}

	public String getCateID(String path) throws SQLException {
		String sql = "SELECT Id FROM folder0 WHERE Path=?";
		PreparedStatement ps = conn.prepareStatement(sql);			
		ps.setString(1, path);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
			return rs.getString("Id");
		
		return null;
	}

	public String getAuthorID(String postBy) throws SQLException {
		String sql = "SELECT author_id FROM author WHERE author_name=?";
		PreparedStatement ps = conn.prepareStatement(sql);			
		ps.setString(1, postBy);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
			return rs.getString("author_id");
		return null;
	}
	/* (non-Javadoc)
	 * @see org.brain2.test.vneappcrawler.VneDataManager#getTopics(java.lang.String)
	 */
	@Override
	public List<Topic> getTopics(String articleID) throws SQLException {
		String sql = "SELECT Topic FROM topicitem WHERE Subject=?";
		PreparedStatement ps = conn.prepareStatement(sql);			
		ps.setString(1, articleID);
		ResultSet rs = ps.executeQuery();
		List<Topic> topics =new ArrayList<Topic>();
		while(rs.next()){
			Topic topic= new Topic();
			topic.setId(rs.getString("Topic"));
			topics.add(topic);
		}
		return topics;
	}
	public boolean existTopic(String topicID) throws SQLException {
		String sql = "SELECT count(*) as total FROM topic_detail WHERE topic_id=?";
		PreparedStatement ps = conn.prepareStatement(sql);			
		ps.setString(1, topicID);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
			if(rs.getInt("total")>0)
				return true;
		Log.println("NO ESIST TOPIC:"+topicID);
		return false;
	}
}
