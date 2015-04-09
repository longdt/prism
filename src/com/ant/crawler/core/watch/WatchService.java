package com.ant.crawler.core.watch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.tuple.Pair;

import com.ant.crawler.core.AbstractCrawler;
import com.ant.crawler.core.SimpleRssCrawler;
import com.ant.crawler.core.conf.Configurable;
import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.entity.EntityBuilderFactory;
import com.ant.crawler.core.parse.XPathWrapper;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.dao.PersistencerFactory;
import com.ant.crawler.plugins.Crawler;
import com.ant.crawler.plugins.Wrapper;

public class WatchService {
	private static BlockingQueue<Pair<String, EntityBuilder>> queue = new LinkedBlockingQueue<>();
	private static Watcher worker = new Watcher();

	public static void add(String pluginID, EntityBuilder entity) {
		queue.offer(Pair.of(pluginID, entity));
	}

	public static void loadJobs(AbstractCrawler crawler) {
		Configuration conf = crawler.getConf();
		String pluginDir = conf.get(PrismConstants.PLUGIN_DIR);
		Persistencer persistencer = crawler.getPersistencer();
		EntityBuilderFactory factory = crawler.getEntityBuilderFactory();
		String parrentIDField = conf.get(PrismConstants.SUB_ENTITY_PARRENTID_FIELD);
		String pluginID = conf.get(PrismConstants.PLUGIN_ID);
		worker.addCrawlerMeta(conf, crawler.getEntityConf());
		try (BufferedReader in = new BufferedReader(new FileReader(pluginDir + "/watched.dat"))) {
			String line = null;
			while ((line = in.readLine()) != null) {
				String[] fields = line.split("\t");
				String[] id = fields[1].split(" ");
				EntityBuilder entity = factory.newEntityBuilder();
				if(!persistencer.find(id[0], entity)) {
					continue;
				}
				for (int i = 1; i < id.length; ++i) {
					EntityBuilder sub = entity.newSubEntity();;
					sub.setSubID(id[i]);
					sub.set(parrentIDField, id[0]);
				}
				entity.setDetailUrl(new URL(fields[0]));
				add(pluginID, entity);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static void start() {
		worker.start();
	}

	public static void stop() {
		worker.interrupt();
	}

	static class Watcher extends Thread {
		private Collection<Pair<Configuration, EntityConf>> crawlerMetas; 
		private Collection<Pair<AbstractCrawler, EntityBuilder>> jobs;
		private Map<String, AbstractCrawler> crawlers;

		public Watcher() {
			jobs = new ArrayList<Pair<AbstractCrawler, EntityBuilder>>();
			crawlerMetas = new ArrayList<>();
			crawlers = new HashMap<>();
		}
		
		public void addCrawlerMeta(Configuration conf, EntityConf entityConf) {
			crawlerMetas.add(Pair.of(conf, entityConf));
		}

		private void loadMetas() throws Exception {
			for (Pair<Configuration, EntityConf> meta : crawlerMetas) {
				AbstractCrawler crawler = createCrawler(meta.getKey(), meta.getValue());
				crawlers.put(meta.getKey().get(PrismConstants.PLUGIN_ID), crawler);
			}
		}

		private AbstractCrawler createCrawler(Configuration conf,
				EntityConf entityConf) throws Exception {
			String backend = entityConf.getBackend();
			if (backend == null || backend.trim().isEmpty()) {
				backend = SimpleRssCrawler.class.getName();
			}
			
			Wrapper wrapper = new XPathWrapper();
			if (wrapper instanceof Configurable) {
				((Configurable) wrapper).setConf(conf);
			}
			
			Crawler crawler = (Crawler) Class.forName(backend).newInstance();
			if (crawler instanceof Configurable) {
				((Configurable) crawler).setConf(conf);
			}
			
			crawler.init(entityConf, wrapper, PersistencerFactory.getDefaultPersistencer());
			return (AbstractCrawler) crawler;
		}

		@Override
		public void run() {
			int sleepTime = PrismConfiguration.getInstance().getInt(PrismConstants.WATCH_CYCLE_MILLISTIME, 60000);
			try {
				loadMetas();
				Pair<String, EntityBuilder> pair = null;
				AbstractCrawler crawler = null;
				while (true) {
					while ((pair = queue.poll()) != null) {
						crawler = crawlers.get(pair.getKey());
						jobs.add(Pair.of(crawler, pair.getValue()));
					}
					Thread.sleep(sleepTime);
					EntityBuilder entity = null;
					
					Persistencer persistencer = null;
					String parrentIDField = null;
					Pair<AbstractCrawler, EntityBuilder> job = null;
					Iterator<Pair<AbstractCrawler, EntityBuilder>> iter = jobs.iterator();
					while (iter.hasNext()) {
						job = iter.next();
						crawler = job.getKey();
						entity = job.getValue();
						parrentIDField = crawler.getConf().get(
								PrismConstants.SUB_ENTITY_PARRENTID_FIELD);
						persistencer = crawler.getPersistencer();
						if (crawler.crawl(entity.getDetailUrl(), entity)) {
							updateEntity(persistencer, entity, parrentIDField);
							if (!entity.isWatch()) {
								iter.remove();
							}
						}
					}
				}
			} catch (InterruptedException e) {
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				saveJobs();
			}
		}

		private void updateEntity(Persistencer persistencer,
				EntityBuilder entity, String parrentIDField)
				throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//			persistencer.store(entity);
			Collection<EntityBuilder> subEntities = entity.getSubEntities();
			if (subEntities == null || subEntities.isEmpty()) {
				return;
			}
			Object id = entity.getID();
			for (EntityBuilder sub : subEntities) {
				if (sub.get(parrentIDField) == null) {
					sub.set(parrentIDField, id);
					persistencer.store(sub);
				}
			}
		}

		private void saveJobs() {
			Map<String, Writer> writers = new HashMap<>();
			try {
				Pair<String, EntityBuilder> pair = null;
				AbstractCrawler crawler = null;
				while ((pair = queue.poll()) != null) {
					crawler = crawlers.get(pair.getKey());
					jobs.add(Pair.of(crawler, pair.getValue()));
				}
				for (Pair<AbstractCrawler, EntityBuilder> job : jobs) {
					String pluginDir = job.getKey().getConf()
							.get(PrismConstants.PLUGIN_DIR);
					saveJob(writers, pluginDir, job.getValue());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				for (Entry<String, Writer> entry : writers.entrySet()) {
					try {
						entry.getValue().close();
					} catch (IOException e) {
					}
				}
			}
		}

		private void saveJob(Map<String, Writer> writers, String pluginDir,
				EntityBuilder entity) throws IOException {
			Writer writer = writers.get(pluginDir);
			if (writer == null) {
				writer = new PrintWriter(new File(pluginDir
						+ "/watched.dat"));
				writers.put(pluginDir, writer);
			}
			writer.write(entity.getDetailUrl() + "\t" + entity.getID());
			Collection<EntityBuilder> subs = entity.getSubEntities();
			if (subs != null && !subs.isEmpty()) {
				for (EntityBuilder sub : subs) {
					writer.write(" " + sub.getSubID());
				}
			}
			writer.write('\n');
		}

	}
}
