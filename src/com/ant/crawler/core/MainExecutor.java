package com.ant.crawler.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.ant.crawler.core.conf.Configurable;
import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.parse.XPathWrapper;
import com.ant.crawler.core.utils.ClassPathHacker;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.dao.PersistencerFactory;
import com.ant.crawler.plugins.Crawler;
import com.ant.crawler.plugins.PluginException;
import com.ant.crawler.plugins.Wrapper;

public class MainExecutor {
	private static final Logger logger = Logger.getLogger(MainExecutor.class);
	private static final int DEFAULT_PORT = 8080;
	private ExecutorService executor;
	private List<Worker> workers;
	private Configuration conf;
	private Persistencer persistencer;
	private SocketServer server;

	public MainExecutor() throws IOException {
		executor = Executors.newCachedThreadPool();
		conf = PrismConfiguration.getInstance();
		persistencer = PersistencerFactory.getDefaultPersistencer();
		workers = new ArrayList<Worker>();
		loadPlugins();
		int port = conf.getInt(PrismConstants.CRAWL_LISTEN_PORT, DEFAULT_PORT);
		server = new SocketServer(this, port);
		server.execute();
	}

	private void loadPlugins() {
		File dir = new File(PrismConstants.PLUGIN_HOME_DIR);
		Set<String> whileList = new HashSet<String>(conf.getStringCollection(PrismConstants.CRAWL_PLUGIN_WHITELIST));
		Set<String> blackList =  new HashSet<String>(conf.getStringCollection(PrismConstants.CRAWL_PLUGIN_BLACKLIST));
		boolean allowAll = false;
		if (blackList.contains("*")) {
			return;
		} else if (whileList.contains("*")) {
			allowAll = true;
		}
		File[] plugins = dir.listFiles();
		for (File plugin : plugins) {
			try {
				if ((allowAll || whileList.contains(plugin.getName())) && !blackList.contains(plugin.getName()))
					loadPlugin(plugin);
			} catch (PluginException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadPlugin(File pluginDir) throws PluginException {
		try {
			EntityConf entityConf = loadEntityConf(pluginDir);
			if (entityConf == null) {
				return;
			}
			Configuration conf = loadConf(pluginDir);
			File[] list = pluginDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			});
			for (File file : list) {
				ClassPathHacker.addFile(file);
			}
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

			wrapper.init(entityConf.getEntityFields().getDetailSite().getField());
			crawler.init(entityConf, wrapper, persistencer);
			workers.add(new Worker(crawler));
		} catch (Exception e) {
		}
	}

	private Configuration loadConf(File pluginDir) {
		Configuration conf = new Configuration();
		File pluginConf = new File(pluginDir, PrismConstants.PLUGIN_CONFIG_FILE);
		if (pluginConf.isFile()) {
			try {
				conf.addResource(pluginConf.toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		conf.set(PrismConstants.PLUGIN_DIR, pluginDir.getAbsolutePath());
		conf.set(PrismConstants.PLUGIN_ID, pluginDir.getName());
		return conf;
	}

	private EntityConf loadEntityConf(File pluginDir) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(EntityConf.class.getPackage().getName());
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			File newsConf = new File(pluginDir, PrismConstants.ENTITY_CONFIG_FILE);
			if (newsConf.isFile()) {
				return (EntityConf) unmarshaller.unmarshal(newsConf);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void execute() throws InterruptedException {
		for (Worker worker : workers) {
			executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.awaitTermination(1000, TimeUnit.MINUTES)) {

		}
		persistencer.close();
	}
	
	public List<Worker> getWorkers() {
		return workers;
	}
	
	public void shutdown() {
		executor.shutdownNow();
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		DOMConfigurator.configure("conf/log4j.xml");
		MainExecutor executor = new MainExecutor();
		logger.info("crawler was started... " + new Date());
		executor.execute();
		logger.info("crawler was shutdown... " + new Date());
	}
}