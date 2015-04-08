package com.ant.crawler.core;

import java.lang.reflect.InvocationTargetException;
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
	protected void doExtThing(EntityBuilder entity, HtmlPage htmlDom) throws Exception {
		if (navigateXpath != null) {
			htmlDom = pageFetcher.navigate(htmlDom, navigateXpath);
		}
		if (htmlDom != null) {
			loadSubEntities(htmlDom, entity);
		}
	}
	
	@Override
	protected void store(EntityBuilder entity) throws Exception {
		Collection<EntityBuilder> subEntities = entity.getSubEntities();
		if (subEntities == null || subEntities.isEmpty()) {
			return;
		}
		super.store(entity);
		Object id = entity.getID();
		if (id != null) {
			for (EntityBuilder sub : subEntities) {
				sub.set(parrentIDField, id);
				persistencer.store(sub);
			}
		}
	}

	protected void loadSubEntities(HtmlPage htmlDom, EntityBuilder entity) throws Exception {
		createSubEntity(htmlDom, entity);
		Collection<EntityBuilder> subEntities = entity.getSubEntities();
		if (subEntities == null) {
			return;
		}
		Iterator<EntityBuilder> subIter = subEntities.iterator();
		URL detailURL = null;
		while (subIter.hasNext()) {
			if (Thread.interrupted()) {
        		throw new InterruptedException();
        	}
			EntityBuilder sub = subIter.next();
			if (sub.get(parrentIDField) != null)
				continue;
			detailURL = sub.getDetailUrl();
			if (!noDetailSite
					&& (detailURL == null
							|| (htmlDom = pageFetcher.retrieve(detailURL)) == null || !extractor
								.extract(htmlDom, sub))) {
				subIter.remove();
			}
		}
	}

	protected abstract void createSubEntity(HtmlPage htmlDom,
			EntityBuilder entity);
}
