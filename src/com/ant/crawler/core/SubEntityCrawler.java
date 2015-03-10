package com.ant.crawler.core;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.conf.entity.SubEntity;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.parse.DetailExtractor;
import com.ant.crawler.core.parse.WrapperFactory;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class SubEntityCrawler extends ListSiteCrawler {
	private String navigateXpath;
	private boolean noDetailSite;
	private DetailExtractor extractor;
	private String parrentIDField;

	@Override
	public void init(EntityConf entityConf, Wrapper detailWrapper,
			Persistencer persistencer) throws Exception {
		super.init(entityConf, detailWrapper, persistencer);
		SubEntity subentity = entityConf.getEntityFields().getSubEntity();
		navigateXpath = subentity.getLink();
		detailWrapper = WrapperFactory.createWrapper(detailWrapper.getClass(), conf);
		extractor = new DetailExtractor(detailWrapper, conf,
				subentity.getDetailSite());
		noDetailSite = subentity.getDetailSite().getField().isEmpty();
		parrentIDField = conf.get(PrismConstants.SUB_ENTITY_PARRENTID_FIELD);
	}
	
	@Override
	protected boolean needDoExtThing() {
		return true;
	}

	@Override
	protected void doExtThing(EntityBuilder entity, HtmlPage htmlDom) throws Exception {
		if (navigateXpath != null) {
			htmlDom = pageFetcher.navigate(htmlDom, navigateXpath);
		}
		if (htmlDom != null) {
			loadSubEntities(htmlDom, entity);
		}
	}

	protected void loadSubEntities(HtmlPage htmlDom, EntityBuilder entity) throws IllegalAccessException, InvocationTargetException {
		createSubEntity(htmlDom, entity);
		Collection<EntityBuilder> subEntities = entity.getSubEntities();
		if (subEntities == null) {
			return;
		}
		Iterator<EntityBuilder> subIter = subEntities.iterator();
		URL detailURL = null;
		URL checkURL = null;
		while (subIter.hasNext()) {
			EntityBuilder sub = subIter.next();
			String subId = sub.getSubID();
			try {
				checkURL = new URL(entity.getDetailUrl(), "#" + subId);
				if (duplicateChecker.test(checkURL)) {
					subIter.remove();
					continue;
				}
				detailURL = sub.getDetailUrl();
				if (!noDetailSite
						&& (detailURL == null
								|| (htmlDom = pageFetcher.retrieve(detailURL)) == null || !extractor
									.extract(htmlDom, sub))) {
					subIter.remove();
					continue;
				}
				sub.setDetailUrl(checkURL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (subEntities.isEmpty()) {
			return;
		}
		persistencer.store(entity);
		Object id = entity.getID();
		if (id != null) {
			duplicateChecker.accept(entity.getDetailUrl());
			for (EntityBuilder sub : subEntities) {
				sub.set(parrentIDField, id);
				persistencer.store(sub);
				duplicateChecker.accept(sub.getDetailUrl());
			}
		}
	}

	protected abstract void createSubEntity(HtmlPage htmlDom,
			EntityBuilder entity);
}
