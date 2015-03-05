package com.ant.crawler.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.commons.beanutils.DynaClass;
import org.apache.log4j.Logger;

import com.ant.crawler.core.conf.Configurable;
import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.conf.entity.Category;
import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.conf.entity.Expand;
import com.ant.crawler.core.content.relate.DuplicateChecker;
import com.ant.crawler.core.download.PageFetcher;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.entity.EntityBuilderFactory;
import com.ant.crawler.core.parse.WrapperFactory;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.dao.dyna.DynaPersistencer;
import com.ant.crawler.plugins.Crawler;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.DomNode;

public abstract class AbstractCrawler implements Crawler, Configurable {
	private static final Logger logger = Logger
			.getLogger(AbstractCrawler.class);
	private static final boolean debugMode = PrismConfiguration.getInstance()
			.getBoolean(PrismConstants.CRAWL_DEBUG_MODE, false);
	private Iterator<Entry<URL, Integer>> urlCatsIter;
	private Map<URL, Integer> urlCats;
	protected PageFetcher pageFetcher;
	protected volatile boolean shutdowned;
	protected Configuration conf;
	protected EntityConf entityConf;
	protected URL homeSite;
	private Wrapper detailWrapper;
	private List<Expand> expands;
	private List<Wrapper> expandWrappers;
	private Persistencer persistencer;
	private DuplicateChecker duplicateChecker;
	private EntityBuilderFactory factory;
	protected String categoryFieldName;
	private String idField;
	private long sleepMillisTime;

	public AbstractCrawler() {
		pageFetcher = new PageFetcher();
		expandWrappers = new ArrayList<Wrapper>();
	}

	@Override
	public void init(EntityConf entityConf, Wrapper wrapper,
			Persistencer persistencer) throws Exception {
		if (conf != null) {
			pageFetcher.init(conf);
		}
		this.entityConf = entityConf;
		this.detailWrapper = wrapper;
		expands = entityConf.getEntityFields().getDetailSite().getExpand();
		Class<Wrapper> wrapperClass = (Class<Wrapper>) wrapper.getClass();
		for (Expand expand : expands) {
			expandWrappers.add(WrapperFactory.createWrapper(wrapperClass, conf, expand.getField()));
		}
		this.persistencer = persistencer;
		String pluginDir = conf.get(PrismConstants.PLUGIN_DIR);
		int maxUrlCheck = conf.getInt(PrismConstants.ENTITY_DUPLICATE_MAXURL, 0);
		duplicateChecker = new DuplicateChecker(pluginDir, maxUrlCheck);
		String mapField = entityConf.getCategories().getMappingField();
		if (mapField != null && !mapField.isEmpty()) {
			categoryFieldName = mapField;
		}
		idField = conf.get(PrismConstants.ENTITY_ID_FIELD);
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
		if (entityClass.startsWith("dyna#")) {
			String tableName = entityClass.substring("dyna#".length());
			DynaClass dynaClass = ((DynaPersistencer) persistencer).createClass(tableName);
			return EntityBuilderFactory.newInstance(dynaClass);
		}
		try {
			Class cl = Class.forName(entityClass);
			return EntityBuilderFactory.newInstance(cl);
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
	public void crawl() throws InterruptedException {
		DomNode htmlDom = null;
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
				if (htmlDom == null || !detailWrapper.extract(htmlDom, entity)
								|| !expand(entity, htmlDom)) {
					continue;
				}
			}
			
			if (!debugMode) {
				persistencer.store(entity, idField);
				duplicateChecker.accept(detailURL);
			}
		}
	}


	private boolean expand(EntityBuilder entity, DomNode htmlDom) {
		if (expandWrappers.isEmpty()) {
			return true;
		}
		DomNode expandPage = null;
		URL url = null;
		URL origURL = entity.getSourceUrl();
		for (int i = 0, n = expands.size(); i < n; ++i) {
			Wrapper wrapper = expandWrappers.get(i);
			Expand expand = expands.get(i);
			try {
				url = getExpandUrl(origURL, htmlDom, expand.getLink());
				expandPage = pageFetcher.retrieve(url);
				entity.setSourceUrl(url);
				if (!wrapper.extract(expandPage, entity) && expand.isRequired()) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (expand.isRequired()) {
					return false;
				}
			}
		}
		entity.setSourceUrl(origURL);
		return true;
	}

	private URL getExpandUrl(URL url, DomNode htmlDom, String linkXpath) throws MalformedURLException {
		 List<DomNode> itemNode = (List<DomNode>) htmlDom.getByXPath(linkXpath);
		 return (itemNode != null && !itemNode.isEmpty()) ? new URL(url, itemNode.get(0).getNodeValue()) : null;
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
