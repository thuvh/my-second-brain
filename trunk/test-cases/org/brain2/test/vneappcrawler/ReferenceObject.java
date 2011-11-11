package org.brain2.test.vneappcrawler;

import java.util.Date;

public class ReferenceObject {
	private String objectID;
	private String articleID;
	private String md5;
	private String url;
	private String credit;
	private String caption;
	private ReferenceType type;
	private Date updateTime;
	public ReferenceObject() {
		super();
	}
	
	public ReferenceObject(String articleID, String md5, String url,
			ReferenceType type, Date updateTime) {
		super();
		this.articleID = articleID;
		this.md5 = md5;
		this.url = url;
		this.type = type;
		this.updateTime = updateTime;
	}

	public static enum ReferenceType {
	    IMG(1),
	    VIDEO(2),
	    RELATED_LINK(3);
	    private final int index;   

	    ReferenceType(int index) {
	        this.index = index;
	    }

	    public int index() { 
	        return index; 
	    }
	}
	public String getArticleID() {
		return articleID;
	}
	public void setArticleID(String articleID) {
		this.articleID = articleID;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public ReferenceType getType() {
		return type;
	}
	public void setType(ReferenceType type) {
		this.type = type;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getObjectID() {
		return objectID;
	}
	public void setObjectID(String objectID) {
		this.objectID = objectID;
	}
	public String getCredit() {
		return credit;
	}
	public void setCredit(String credit) {
		this.credit = credit;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}

}
