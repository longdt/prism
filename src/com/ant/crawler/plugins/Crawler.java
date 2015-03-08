package com.ant.crawler.plugins;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.dao.Persistencer;

public interface Crawler {
	
	public void init(EntityConf conf, Wrapper wrapper, Persistencer persistencer) throws Exception;
	
	public void crawl() throws Exception;
	
	public void shutdown();
	
	public boolean isShutdowned();
}