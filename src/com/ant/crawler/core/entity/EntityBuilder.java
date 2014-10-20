package com.ant.crawler.core.entity;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;


public class EntityBuilder {
	private Map<URL, String> downloadImgs;
	private List<String> indexDatas;
	private Object entity;
	private URL sourceUrl;
	private Date createTime;
	EntityBuilder(Object entity) {
		this.entity = entity;
		indexDatas = new ArrayList<String>();
		downloadImgs = new HashMap<URL, String>();
	}

	public void set(String name, Object value) throws IllegalAccessException, InvocationTargetException {
		if (name == null) return;
		BeanUtils.setProperty(entity, name, value);
	}
	
	public String getSimpleProperty(String name) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return BeanUtils.getProperty(entity, name);
	}
	
	public Object get(String name) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return PropertyUtils.getProperty(entity, name);
	}
	
	public Map<URL, String> getDownloadImgs() {
		return downloadImgs;
	}
	public void addDownloadImg(URL url, String relateFilePath) {
		downloadImgs.put(url, relateFilePath);
	}


	public Object getEntity() {
		return entity;
	}


	public URL getSourceUrl() {
		return sourceUrl;
	}


	public void setSourceUrl(URL sourceUrl) {
		this.sourceUrl = sourceUrl;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void addIndexData(String data) {
		indexDatas.add(data);
	}
	
	public List<String> getIndexDatas() {
		return indexDatas;
	}
}
