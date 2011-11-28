package org.brain2.test.vneappcrawler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Article {
	private String id;
	private long ID;
	private String authorID;
	private String headline;
	private String abstractS;
	private String content;
	private int isDeleted;
	private Date creationDate;
	private Date updateDate;
	private String sharedURL;
	private String thumbnailMD5;
	private String thumbnailURL;
	private List<Topic> topics=new ArrayList<Topic>();
	private String catID;
	private String postBy;
	private List<Topic> refTopics=new ArrayList<Topic>();
	private List<Comment> comments=new ArrayList<Comment>();
	private List<ReferenceObject> refObj=new ArrayList<ReferenceObject>();
	
	private boolean generalParsed = false;
	
	public Article() {
		super();
	}
	
	public Article(String id,String postBy, String headline, String abstractS, String sharedURL,int isDeleted,
			Date creationDate, Date updateDate) {
		super();
		this.id = id;
		this.headline = headline;
		this.postBy = postBy;
		this.abstractS = abstractS;
		this.isDeleted = isDeleted;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.sharedURL = sharedURL;
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

	public String getCatID() {
		return catID;
	}

	public void setCatID(String catID) {
		this.catID = catID;
	}
	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public List<Topic> getRefTopics() {
		return refTopics;
	}

	public void setRefTopics(List<Topic> refTopics) {
		this.refTopics = refTopics;
	}

	public String getPostBy() {
		return postBy;
	}

	public void setPostBy(String postBy) {
		this.postBy = postBy;
	}


	public String getSharedURL() {
		return sharedURL;
	}

	public void setSharedURL(String sharedURL) {
		this.sharedURL = sharedURL;
	}
	public String getThumbnailMD5() {
		return thumbnailMD5;
	}

	public void setThumbnailMD5(String thumbnailMD5) {
		this.thumbnailMD5 = thumbnailMD5;
	}

	public String getThumbnailURL() {
		return thumbnailURL;
	}

	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
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

	public List<ReferenceObject> getRefObj() {
		return refObj;
	}

	public void setRefObj(List<ReferenceObject> refObj) {
		this.refObj = refObj;
	}
	public void addRefObj(ReferenceObject obj){
		this.refObj.add(obj);
	}

	public void addAll(List<ReferenceObject> refObj2) {
		this.refObj.addAll(refObj2);
	}

	public boolean isGeneralParsed() {
		return generalParsed;
	}

	public void setGeneralParsed(boolean generalParsed) {
		this.generalParsed = generalParsed;
	}
	
	@Override
	public String toString() {		
		return new Gson().toJson(this);
	}

	public void addRefTopic(Topic topic) {
		this.refTopics.add(topic);
	}
	public void addTopic(Topic topic) {
		this.topics.add(topic);
	}

	public void removeRefTopic(Topic t) {
		this.refTopics.remove(t);
	}

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}
	
}
