package com.ant.crawler.core.entity;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;


public class EntityBuilder {
	private Map<URL, String> downloadImgs;
	private List<String> indexDatas;
	private Object entity;
	private URL sourceUrl;
	private URL detailUrl;
	private EntityBuilder parrent;
	private Set<EntityBuilder> subEntities;
	private EntityBuilderFactory factory;
	private String subID;
	private String idField;
	
	EntityBuilder(Object entity, String idField) {
		this(entity, idField, null);
	}
	
	EntityBuilder(Object entity, String idField, EntityBuilderFactory factory) {
		this.entity = entity;
		this.idField = idField;
		this.factory = factory;
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
	
	public EntityBuilder newSubEntity() {
		if (factory == null) {
			return null;
		}
		EntityBuilder builder = factory.newSubEntityBuilder();
		if (subEntities == null) {
			subEntities = new LinkedHashSet<>();
		}
		builder.parrent = this;
		subEntities.add(builder);
		return builder;
	}


	public URL getSourceUrl() {
		return sourceUrl;
	}


	public void setSourceUrl(URL sourceUrl) {
		this.sourceUrl = sourceUrl;
	}


	public URL getDetailUrl() {
		return detailUrl;
	}

	public void setDetailUrl(URL detailUrl) {
		this.detailUrl = detailUrl;
	}

	public void addIndexData(String data) {
		indexDatas.add(data);
	}
	
	public List<String> getIndexDatas() {
		return indexDatas;
	}

	public Set<EntityBuilder> getSubEntities() {
		return subEntities;
	}

	public String getSubID() {
		return subID;
	}
	
	public void setSubID(String subID) {
		this.subID = subID;
	}

	public String getIDField() {
		return idField;
	}

	public Object getID() {
		try {
			return get(idField);
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
		}
		//never occur
		return null;
	}

	public void remove() {
		parrent.subEntities.remove(this);
	}
}
