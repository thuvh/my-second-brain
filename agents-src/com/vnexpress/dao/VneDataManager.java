package com.vnexpress.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.vnexpress.model.Article;
import com.vnexpress.model.Topic;


public interface VneDataManager {

	public abstract void closeConnection();

	/**
	 * total records of vnexpress subject, domain vnexpress.net only
	 * 
	 * @return int
	 * @throws Exception
	 */
	public abstract int getTotalCountInVnExpress() throws SQLException;

	public abstract boolean isExistedArticle(Article article)
			throws SQLException;

	public abstract boolean isExistedArticleByID(String id) throws SQLException;

	public abstract boolean isExistedArticle(String path) throws SQLException;

	public abstract long getArticleIdByPath(String path) throws SQLException;

	public abstract boolean updateArticle(Article article) throws SQLException;

	public abstract boolean saveArticle(Article article) throws SQLException;

	public abstract ResultSet getComment(String url) throws SQLException;

	public abstract List<Topic> getTopics(String articleID) throws SQLException;
	
	public abstract Article getOldSubjectByPath(String path) throws SQLException;
	
	public abstract ResultSet getSubjectPathInVnExpress(int begin, int total) throws SQLException;

}