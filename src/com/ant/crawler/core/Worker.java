package com.ant.crawler.core;

import com.ant.crawler.plugins.Crawler;

public class Worker implements Runnable {
	private Crawler crawler;
	
	public Worker(Crawler crawler) {
		this.crawler = crawler;
	}
	@Override
	public void run() {
		try {
			crawler.crawl();
		} catch (InterruptedException e) {
		}
	}
	
	public void shutdown() {
		crawler.shutdown();
	}
	
}
