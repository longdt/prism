package com.ant.crawler.core.entity;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class News {
	private Integer newsID;
	private String title;
	private String description;
	private String htmlContent;
	private String textContent;
	private Date createTime;
	private String author;
	private URL sourceUrl;
	private String imgUrl;
	private Integer catId;
	private Date modifiedDate;
	private Integer wasVerify;
	private Integer setionId ;
	private Map<URL, String> downloadImgs = new HashMap<URL, String>();
	
	
	public Integer getNewsID() {
		return newsID;
	}
	public void setNewsID(Integer newsID) {
		this.newsID = newsID;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}

	public Integer getCatId() {
		return catId;
	}
	public void setCatId(Integer catId) {
		this.catId = catId;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public Integer getWasVerify() {
		return wasVerify;
	}
	public void setWasVerify(Integer wasVerify) {
		this.wasVerify = wasVerify;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	
	public String getImgUrl() {
		return imgUrl;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "@category='" + catId + "', @title='" + title + "', @description='" + description + "', @sourceUrl='" + getSourceUrl();
	}
	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	public String getHtmlContent() {
		return htmlContent;
	}
	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}
	public String getTextContent() {
		return textContent;
	}
	
	
	public Integer getSetionId() {
		return setionId;
	}
	public void setSectionId(int setionId) {
		this.setionId = setionId;
	}
	public URL getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(URL sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	public Map<URL, String> getDownloadImgs() {
		return downloadImgs;
	}
	public void addDownloadImg(URL url, String relateFilePath) {
		downloadImgs.put(url, relateFilePath);
	}
}