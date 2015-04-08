package com.ant.crawler.plugins;

import java.net.URL;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.dao.Persistencer;

public interface Crawler {
	
	public void init(EntityConf conf, Wrapper wrapper, Persistencer persistencer) throws Exception;
	
	public void crawl() throws Exception;
	
	public boolean crawl(URL detailURL, EntityBuilder entity);
	
	public void shutdown();
	
	public boolean isShutdowned();
}