package com.ant.crawler.core;

import java.util.List;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.conf.entity.ListSite;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.parse.WrapperFactory;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ListSubEntityCrawler extends SubEntityCrawler {
	private String itemXpath;
	private Wrapper listWrapper;
	
	@Override
	public void init(EntityConf entityConf, Wrapper detailWrapper,
			Persistencer persistencer) throws Exception {
		super.init(entityConf, detailWrapper, persistencer);
		ListSite listConf = entityConf.getEntityFields().getSubEntity().getListSite();
		itemXpath = listConf.getItemXpath();
		listWrapper = WrapperFactory.createDefaultWrapper(conf, listConf.getField());
	}

	@Override
	protected void createSubEntity(HtmlPage htmlDom, EntityBuilder entity) {
		List<DomNode> items = (List<DomNode>) htmlDom.getByXPath(itemXpath);
		for (DomNode item : items) {
			EntityBuilder sub = entity.newSubEntity();
			sub.setSourceUrl(htmlDom.getUrl());
			if (!listWrapper.extract(item, sub)) {
				sub.remove();
			}
		}
	}

}
