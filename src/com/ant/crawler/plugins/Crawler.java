package com.ant.crawler.plugins;

import net.xeoh.plugins.base.Plugin;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.dao.Persistencer;

public interface Crawler extends Plugin {
	
	public void init(EntityConf conf, Wrapper wrapper, Persistencer persistencer) throws Exception;
	
	public void crawl() throws InterruptedException;
	
	public void shutdown();
	
	public boolean isShutdowned();
}