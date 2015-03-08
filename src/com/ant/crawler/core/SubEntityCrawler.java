package com.ant.crawler.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.conf.entity.SubEntity;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.parse.DetailExtractor;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class SubEntityCrawler extends ListSiteCrawler {
	private String navigateXpath;
	private boolean noDetailSite;
	private DetailExtractor extractor;
	@Override
	public void init(EntityConf entityConf, Wrapper detailWrapper,
			Persistencer persistencer) throws Exception {
		super.init(entityConf, detailWrapper, persistencer);
		SubEntity subentity = entityConf.getEntityFields().getSubEntity();
		navigateXpath = subentity.getLink();
		extractor = new DetailExtractor(detailWrapper, conf, subentity.getDetailSite());
		noDetailSite = subentity.getDetailSite().getField().isEmpty();
	}

	@Override
	protected void doExtThing(EntityBuilder entity, HtmlPage htmlDom) {
		if (navigateXpath != null) {
			try {
				htmlDom = pageFetcher.navigate(htmlDom, navigateXpath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		loadSubEntities(htmlDom, entity);
	}

	protected void loadSubEntities(HtmlPage htmlDom, EntityBuilder entity) {
		createSubEntity(htmlDom, entity);
		List<EntityBuilder> subEntities = entity.getSubEntities();
		if (subEntities == null) {
			return;
		}
		Iterator<EntityBuilder> subIter = subEntities.iterator();
		URL detailURL = null;
		while (subIter.hasNext()) {
			EntityBuilder sub = subIter.next();
			String subId = sub.getSubID();
			try {
				URL checkURL = new URL(entity.getDetailUrl(), "#" + subId);
				if (duplicateChecker.test(checkURL)) {
					subIter.remove();
					continue;
				}
				detailURL = sub.getDetailUrl();
				if (!noDetailSite && (detailURL == null || extractor.extract(detailURL, sub))) {
					continue;
				}
				persistencer.store(sub, "id");
				duplicateChecker.accept(checkURL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	protected abstract void createSubEntity(HtmlPage htmlDom, EntityBuilder entity);
}
