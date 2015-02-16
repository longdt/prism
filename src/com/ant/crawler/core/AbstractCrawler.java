package com.ant.crawler.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
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
import com.ant.crawler.core.download.PageFetcher;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.entity.EntityBuilderFactory;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.dao.dyna.DynaPersistencer;
import com.ant.crawler.plugins.Crawler;
import com.ant.crawler.plugins.PluginException;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.DomNode;

public abstract class AbstractCrawler implements Crawler, Configurable {
	private static final Logger logger = Logger
			.getLogger(AbstractCrawler.class);
	private static final boolean debugMode = PrismConfiguration.getInstance()
			.getBoolean(PrismConstants.CRAWL_DEBUG_MODE, false);
	private static final boolean useCrawlTime = PrismConfiguration
			.getInstance().getBoolean(
					PrismConstants.ENTITY_PUBDATE_USING_CRAWLTIME, false);
	private Iterator<Entry<URL, Integer>> urlCatsIter;
	private Map<URL, Integer> urlCats;
	protected PageFetcher pageFetcher;
	protected volatile boolean shutdowned;
	protected Configuration conf;
	protected EntityConf entityConf;
	protected URL homeSite;
	protected Date lastAccessTime;
	protected Date nextAccessTime;
	private DateFormat formater = DateFormat.getDateTimeInstance();
	private String lastTimeFile;
	private Wrapper detailWrapper;
	private Persistencer persistencer;
	private EntityBuilderFactory factory;
	protected String categoryFieldName;
	private String idField;
	private long sleepMillisTime;

	public AbstractCrawler() {
		pageFetcher = new PageFetcher();
	}

	@Override
	public void init(EntityConf entityConf, Wrapper wrapper,
			Persistencer persistencer) throws PluginException {
		if (conf != null) {
			pageFetcher.init(conf);
		}
		this.entityConf = entityConf;
		this.detailWrapper = wrapper;
		this.persistencer = persistencer;
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
		lastTimeFile = conf.get(PrismConstants.PLUGIN_DIR) + File.separatorChar
				+ PrismConstants.PLUGIN_ACCESS_TIME_FILE;
		lastAccessTime = getLastAcessTime(lastTimeFile);
		nextAccessTime = new Date(lastAccessTime.getTime());
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

	/**
	 * @param string
	 * @return
	 */
	private Date getLastAcessTime(String timeFile) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(timeFile));
			String timeString = reader.readLine();
			if (timeString != null
					&& !((timeString = timeString.trim()).isEmpty())) {
				return formater.parse(timeString);
			}
		} catch (IOException e) {
		} catch (ParseException e) {
			logger.warn(timeFile + " contain invalid time", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new Date(0);
	}

	private void setLastAcessTime(String timeFile) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(timeFile));
			String timeString = formater.format(lastAccessTime);
			writer.println(timeString);
		} catch (IOException e) {
			logger.warn("error when writting time to " + timeFile, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	@Override
	public void crawl() throws InterruptedException {
		DomNode htmlDom = null;
		Date createTime = null;
		while (!shutdowned) {
			EntityBuilder entity = factory.newEntityBuilder();
			if (!makeCrawlTask(entity)) {
				htmlDom = pageFetcher.retrieve(entity.getDetailUrl());
				if (htmlDom == null
						|| (htmlDom != null && !detailWrapper.extract(htmlDom, entity))) {
					continue;
				}
			}
			
			createTime = entity.getCreateTime();
			if (createTime == null) {
				logger.debug("can't detect createTime field of entity: "
						+ entity);
			} else if (!createTime.after(lastAccessTime)) {
				continue;
			} else if (createTime.after(nextAccessTime)) {
				nextAccessTime.setTime(createTime.getTime());
			}
			
			if (useCrawlTime) {
				entity.setCreateTime(new Date());
			}
			if (!debugMode) {
				persistencer.store(entity, idField);
			}
		}
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
			lastAccessTime.setTime(nextAccessTime.getTime());
			setLastAcessTime(lastTimeFile);
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
