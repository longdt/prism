package com.ant.crawler.core;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.conf.entity.Field;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.parse.WrapperFactory;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.plugins.PluginException;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.DomNode;

@PluginImplementation
public class ListSiteCrawler extends AbstractCrawler {
	protected XPath xpath = XPathFactory.newInstance().newXPath();
	protected Integer currCat;
	protected int index = -1;
	protected List<DomNode> currentList;
	private String itemXpath;
	private List<Field> fields;
	private Wrapper listWrapper;
	private boolean hasDetailSite;
	
	@Override
	public void init(EntityConf entityConf, Wrapper detailWrapper,
			Persistencer persistencer) throws PluginException {
		super.init(entityConf, detailWrapper, persistencer);
		itemXpath = entityConf.getEntityFields().getListSite().getItemXpath();
		fields = entityConf.getEntityFields().getListSite().getField();
		listWrapper = WrapperFactory.createNewsWrapper(conf, fields);
		hasDetailSite = entityConf.getEntityFields().getDetailSite().getField().isEmpty();
	}
	
	@Override
	protected boolean initTask(EntityBuilder entity,
			Iterator<Entry<URL, Integer>> urlCatsIter)
			throws NoSuchElementException {
		if (currentList == null || index == -1) {
			Entry<URL, Integer> urlEntry = urlCatsIter.next();
			currCat = urlEntry.getValue();
			currentList = retrieveItemList(urlEntry.getKey());
			if (currentList == null || currentList.isEmpty()) {
				return false;
			}
			index = currentList.size() - 1;
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
		boolean result = listWrapper.extract(currentList.get(index), entity);
		--index;
		if (entity.getCreateTime() != null && !entity.getCreateTime().after(lastAccessTime)) {
			entity.setSourceUrl(null);
			return false;
		}
		return result && hasDetailSite;
	}

	private List<DomNode> retrieveItemList(URL url) {
		DomNode html = pageFetcher.retrieve(url);
		if (html != null) {
			return (List<DomNode>) html.getByXPath(itemXpath);
		}
		return null;
	}

}
