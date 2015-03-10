package com.ant.crawler.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.log4j.Logger;

import com.ant.crawler.core.conf.Configurable;
import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.conf.entity.Category;
import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.content.relate.DuplicateChecker;
import com.ant.crawler.core.download.PageFetcher;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.entity.EntityBuilderFactory;
import com.ant.crawler.core.parse.DetailExtractor;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.dao.dyna.DynaPersistencer;
import com.ant.crawler.plugins.Crawler;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class AbstractCrawler implements Crawler, Configurable {
	private static final Logger logger = Logger
			.getLogger(AbstractCrawler.class);
	private Iterator<Entry<URL, Integer>> urlCatsIter;
	private Map<URL, Integer> urlCats;
	protected PageFetcher pageFetcher;
	protected volatile boolean shutdowned;
	protected Configuration conf;
	protected EntityConf entityConf;
	protected URL homeSite;
	protected Persistencer persistencer;
	protected DuplicateChecker duplicateChecker;
	protected EntityBuilderFactory factory;
	protected String categoryFieldName;
	private long sleepMillisTime;
	private DetailExtractor extractor;
	private boolean extThing;

	public AbstractCrawler() {
		pageFetcher = new PageFetcher();
		extThing = needDoExtThing();
	}

	@Override
	public void init(EntityConf entityConf, Wrapper wrapper,
			Persistencer persistencer) throws Exception {
		if (conf != null) {
			pageFetcher.init(conf);
		}
		this.entityConf = entityConf;
		extractor = new DetailExtractor(wrapper, conf, entityConf.getEntityFields().getDetailSite());
		this.persistencer = persistencer;
		String pluginDir = conf.get(PrismConstants.PLUGIN_DIR);
		int maxUrlCheck = PrismConfiguration.getInstance().getInt(PrismConstants.ENTITY_DUPLICATE_MAXURL, 0);
		duplicateChecker = new DuplicateChecker(pluginDir, maxUrlCheck);
		String mapField = entityConf.getCategories().getMappingField();
		if (mapField != null && !mapField.isEmpty()) {
			categoryFieldName = mapField;
		}
		List<Category> cats = entityConf.getCategories().getCategory();
		if (cats.isEmpty() || (urlCats = parseUrlCats(cats)).isEmpty()) {
			shutdown();
			return;
		}

		try {
			URL url = urlCats.keySet().iterator().next();
			homeSite = new URL(url, "/");
			conf.set(PrismConstants.PLUGIN_HOME_SITE, homeSite.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		urlCatsIter = urlCats.entrySet().iterator();
		sleepMillisTime = PrismConfiguration.getInstance().getLong(
				PrismConstants.CRAWL_CYCLE_MILLISTIME, -1);
		if (sleepMillisTime < 0) {
			sleepMillisTime = -1;
		}
		factory = initEntityBuilderFactory();
	}

	private EntityBuilderFactory initEntityBuilderFactory() {
		String entityClass = conf.get(PrismConstants.ENTITY_CLASS);
		String subentityClass = conf.get(PrismConstants.SUB_ENTITY_CLASS);
		if (entityClass.startsWith("dyna#")) {
			String tableName = entityClass.substring("dyna#".length());
			SqlDynaClass dynaClass = ((DynaPersistencer) persistencer).createClass(tableName);
			SqlDynaClass subEClass = null;
			if (subentityClass != null && subentityClass.startsWith("dyna#")) {
				tableName = subentityClass.substring("dyna#".length());
				subEClass = ((DynaPersistencer) persistencer).createClass(tableName);
			}
			return EntityBuilderFactory.newInstance(dynaClass, subEClass);
		}
		try {
			Class cl = Class.forName(entityClass);
			Class subClass = null;
			if (subentityClass != null && !subentityClass.isEmpty()) {
				subClass = Class.forName(subentityClass);
			}
			return EntityBuilderFactory.newInstance(cl, subClass);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Map<URL, Integer> parseUrlCats(List<Category> cats) {
		Map<URL, Integer> urlCats = new LinkedHashMap<URL, Integer>();
		String[] urls = null;
		int catId = 0;
		for (Category cat : cats) {
			urls = cat.getValue().split("\\s+");
			catId = cat.getId();
			for (String url : urls) {
				if (!url.isEmpty()) {
					try {
						urlCats.put(new URL(url), catId);
					} catch (MalformedURLException e) {
						logger.error("malformed url: " + url, e);
					}
				}
			}
		}
		return urlCats;
	}

	@Override
	public void crawl() throws Exception {
		HtmlPage htmlDom = null;
		URL detailURL = null;
		while (!shutdowned) {
			EntityBuilder entity = factory.newEntityBuilder();
			if (!makeCrawlTask(entity)) {
				detailURL = entity.getDetailUrl();
				if (detailURL == null || duplicateChecker.test(detailURL)) {
					continue;
				}
				entity.setSourceUrl(detailURL);
				htmlDom = pageFetcher.retrieve(detailURL);
				if (htmlDom == null || !extractor.extract(htmlDom, entity)) {
					continue;
				}
			}
			
			if (extThing) {
				doExtThing(entity, htmlDom);
			} else {
				persistencer.store(entity);
				duplicateChecker.accept(detailURL);				
			}
		}
	}
	
	protected boolean needDoExtThing() {
		return false;
	}


	protected void doExtThing(EntityBuilder entity, HtmlPage htmlDom) throws Exception {
		
	}

	/**
	 * chu yeu phim url source, category bai bao
	 * 
	 * @param entity
	 * @return
	 * @throws InterruptedException
	 */
	public boolean makeCrawlTask(EntityBuilder entity) throws InterruptedException {
		try {
			return initTask(entity, urlCatsIter);
		} catch (NoSuchElementException e) {
			persistencer.sync();
			if (sleepMillisTime != -1) {
				Thread.sleep(sleepMillisTime);
			}
			urlCatsIter = urlCats.entrySet().iterator();
		}
		return false;
	}

	protected abstract boolean initTask(EntityBuilder entity,
			Iterator<Entry<URL, Integer>> urlCatsIter)
			throws NoSuchElementException;

	@Override
	public void shutdown() {
		shutdowned = true;
		try {
			duplicateChecker.save();
		} catch (IOException e) {
			logger.error("can't save visited urls: " + conf.get(PrismConstants.PLUGIN_ID), e);
		}
	}

	@Override
	public boolean isShutdowned() {
		return shutdowned;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

	public EntityConf getEntityConf() {
		return entityConf;
	}

	public URL getHomeSite() {
		return homeSite;
	}
}
