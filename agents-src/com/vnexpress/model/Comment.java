package com.vnexpress.model;

import java.sql.Date;

public class Comment {
	private String commentId;
	private long ID;
	private String path;
	private String articleId;
	private String title;
	private String content;
	private String userId;
	private String fullname;
	private String email;
	private int status;	
	private Date publicTime;
	private Date creationTime;
	private Date updateDate;	
	
	public Comment() {
		super();
	}
	

	public Comment(String commentId, String articleId, String title,
			String content, String userId, String fullname, String email,
			int status, Date publicTime, Date creationTion, Date updateDate) {
		super();
		this.commentId = commentId;
		this.articleId = articleId;
		this.title = title;
		this.content = content;
		this.userId = userId;
		this.fullname = fullname;
		this.email = email;
		this.status = status;
		this.publicTime = publicTime;
		this.creationTime = creationTion;
		this.updateDate = updateDate;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getPublicTime() {
		return publicTime;
	}

	public void setPublicTime(Date publicTime) {
		this.publicTime = publicTime;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}


	public  long getID() {
		return ID;
	}


	public  void setID(long iD) {
		ID = iD;
	}


	public  String getPath() {
		return path;
	}


	public  void setPath(String path) {
		this.path = path;
	}

}
