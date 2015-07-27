package com.ant.crawler.core;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.conf.entity.Field;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.parse.WrapperFactory;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.DomNode;

public class ListSiteCrawler extends AbstractCrawler {
	protected URL currSourceUrl;
	protected Integer currCat;
	protected int index = -1;
	private List<DomNode> currentList;
	private String itemXpath;
	private Wrapper listWrapper;
	private boolean noDetailSite;
	
	@Override
	public void init(EntityConf entityConf, Wrapper detailWrapper,
			Persistencer persistencer) throws Exception {
		super.init(entityConf, detailWrapper, persistencer);
		itemXpath = entityConf.getEntityFields().getListSite().getItemXpath();
		List<Field> fields = entityConf.getEntityFields().getListSite().getField();
		listWrapper = WrapperFactory.createDefaultWrapper(conf, fields);
		noDetailSite = entityConf.getEntityFields().getDetailSite().getField().isEmpty();
	}
	
	@Override
	protected boolean initTask(EntityBuilder entity,
			Iterator<Entry<URL, Integer>> urlCatsIter)
			throws NoSuchElementException {
		if (currentList == null || index == -1) {
			Entry<URL, Integer> urlEntry = urlCatsIter.next();
			currSourceUrl = urlEntry.getKey();
			currCat = urlEntry.getValue();
			currentList = retrieveItemList(urlEntry.getKey());
			if (currentList == null || currentList.isEmpty()) {
				return false;
			}
			index = currentList.size() - 1;
		}

		try {
			entity.setSourceUrl(currSourceUrl);
			entity.set(categoryFieldName, currCat);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean result = listWrapper.extract(currentList.get(index), entity);
		--index;
		return result && noDetailSite;
	}

	private List<DomNode> retrieveItemList(URL url) throws NoSuchElementException {
		try {
			DomNode html = pageFetcher.retrieve(url);
			if (html != null) {
				return (List<DomNode>) html.getByXPath(itemXpath);
			}
			return null;
		} catch (RuntimeException e) {
			throw new NoSuchElementException();
		}
	}

}
