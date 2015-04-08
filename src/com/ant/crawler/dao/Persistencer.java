package com.ant.crawler.dao;

import com.ant.crawler.core.entity.EntityBuilder;

public interface Persistencer {
	
	public void store(EntityBuilder entity);
	
	public void close();
	
	public void sync();

	public boolean find(String id, EntityBuilder entity);
	
	public boolean find(String id, String parrentIDField, EntityBuilder entity);

}
