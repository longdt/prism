package com.ant.crawler.core;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.ant.crawler.core.entity.EntityBuilder;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public abstract class AbstractRssCrawler extends AbstractCrawler {
	protected FeedReader fetcher;
	protected SyndFeed currentFeed;
	protected Integer currCat;
	protected int index;

	public AbstractRssCrawler() {
		index = -1;
		fetcher = new FeedReader();
	}

	@Override
	protected boolean initTask(EntityBuilder entity,
			Iterator<Entry<URL, Integer>> urlCatsIter) throws NoSuchElementException {
		if (currentFeed == null || index == -1) {
			Entry<URL, Integer> urlEntry = urlCatsIter.next();
			currCat = urlEntry.getValue();
			currentFeed = fetcher.retrieveFeed(urlEntry.getKey());
			if (currentFeed == null || currentFeed.getEntries().size() == 0) {
				return false;
			}
			index = currentFeed.getEntries().size() - 1;
		}
		try {
			entity.set(categoryFieldName, currCat);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean result = populate(entity, (SyndEntry) currentFeed.getEntries().get(index));
		--index;
		return result;
	}

	protected abstract boolean populate(EntityBuilder entity, SyndEntry item);
	
	public Integer getCurrCat() {
		return currCat;
	}

}
