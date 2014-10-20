package com.ant.crawler.core.content.relate;

import java.util.List;

import com.ant.crawler.core.entity.EntityBuilder;

public interface Relationer {

	/**
	 * detect whether a given news matching a exist news. If not compute
	 * relations news.
	 * 
	 * @param entity
	 * @return null if matching exist news otherwise list of relation news.
	 */
	public  List<DocSimilar> relate(EntityBuilder entity);

	/**
	 * store previous news's metadata of last call {@link #relate(Object)}with a given newsID
	 * @param entityID
	 */
	public void storeCurrEntityWithID(long entityID);
	
	/**
	 * close storage
	 */
	public void close();

	/**
	 * flush all news's metadata to disk. void lose data due unexpected situation
	 */
	public void sync();
}
