package org.brain2.test.vneappcrawler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Article {
	private String id;
	private String authorID;
	private String headline;
	private String abstractS;
	private String content;
	private int isDeleted;
	private Date creationDate;
	private Date updateDate;
	private String sharedURL;
	private List<Comment> comments=new ArrayList<Comment>();
	
	public Article() {
		super();
	}
	
	public Article(String id, String headline, String abstractS, String sharedURL,int isDeleted,
			Date creationDate, Date updateDate) {
		super();
		this.id = id;
		this.headline = headline;
		this.abstractS = abstractS;
		this.isDeleted = isDeleted;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.sharedURL = sharedURL;
	}
	

	public String getSharedURL() {
		return sharedURL;
	}

	public void setSharedURL(String sharedURL) {
		this.sharedURL = sharedURL;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAuthorID() {
		return authorID;
	}
	public void setAuthorID(String authorID) {
		this.authorID = authorID;
	}
	public String getHeadline() {
		return headline;
	}
	public void setHeadline(String headline) {
		this.headline = headline;
	}
	public String getAbstractS() {
		return abstractS;
	}
	public void setAbstractS(String abstractS) {
		this.abstractS = abstractS;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
	

}
