package com.ant.crawler.dao;

import com.ant.crawler.core.entity.EntityBuilder;

public interface Persistencer {
	
	public void store(EntityBuilder entity);
	
	public void close();
	
	public void sync();

}
