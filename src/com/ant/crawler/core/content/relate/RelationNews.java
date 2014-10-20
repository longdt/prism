package com.ant.crawler.core.content.relate;

import java.util.List;

import com.ant.crawler.core.entity.News;

public interface RelationNews {

	/**
	 * detect whether a given news matching a exist news. If not compute
	 * relations news.
	 * 
	 * @param news
	 * @return null if matching exist news otherwise list of relation news.
	 */
	public  List<DocSimilar> relateNews(News news);

	/**
	 * store previous news's metadata of last call {@link #relateNews(News)}with a given newsID
	 * @param newsID
	 */
	public void storeCurrNewsWithID(long newsID);
	
	/**
	 * close storage
	 */
	public void close();

	/**
	 * flush all news's metadata to disk. void lose data due unexpected situation
	 */
	public void sync();
}
