package com.ant.crawler.core.utils;

public class PrismConstants {
	
	public static final String CRAWL_LISTEN_PORT = "crawl.listen.port";

	public static final String CRAWL_IMAGIC_PATH = "crawl.imagic.path";
	
	public static final String PERSISTENCER_BACKEND = "persistencer.backend";
	
	public static final String ENTITY_TEMP_FIELD = "temp";
	
	public static final String PLUGIN_HOME_DIR = "conf/sites";

	public static final String PRIM_CONFIG_FILE = "conf/crawler.xml";
	
	public static final String PLUGIN_CONFIG_FILE = "configuration.xml";
	
	public static final String ENTITY_CONFIG_FILE = "econf.xml";
	
	public static final String PLUGIN_ID = "plugin.id";
	
	public static final String PLUGIN_DIR = "plugin.dir";
	
	public static final String PLUGIN_HOME_SITE = "plugin.home.site";
	
	public static final String PLUGIN_ACCESS_TIME_FILE = "lasttime.dat";
	
	public static final String PLUGIN_VERSION = "plugin.version";
	
	public static final String PLUGIN_WRAPPER_CLASS = "plugin.wrapper.class";
	
	public static final String PLUGIN_CRAWLER_CLASS = "plugin.crawler.class";
	
	public static final String CRAWL_CYCLE_MILLISTIME = "crawler.cycle.millistime";
	
	public static final String CRAWL_DEBUG_MODE = "crawler.debug.mode";
	
	public static final String CRAWL_PLUGIN_WHITELIST = "crawler.plugin.whitelist";
	
	public static final String CRAWL_PLUGIN_BLACKLIST = "crawler.plugin.blacklist";
	
	public static final int THREAD_NUM_DEFAULT = 4;
	
	public static final String WEBCLIENT_JAVASCRIPT_ENABLED = "webclient.enableJavascript";
	
	public static final String WEBCLIENT_COOKIES = "webclient.cookies";
	
	@Deprecated
	public static final String SQL_QUERY = "sql.query";
	
	@Deprecated
	public static final String SQL_LOGIN_URL = "sql.login.url";

	public static final String CONTENT_IGNORE_HTML_STYLE_SCRIPT = "content.ignore.html.style.script";

	public static final String CONTENT_IMG_STYLE = "content.img.style";
	
	public static final String CONTENT_IGNORE_HTML_STYLE_SCRIPT_WHITELIST = "content.ignore.html.style.script.whitelist";
	
	public static final String CONTENT_IGNORE_HTML_STYLE_SCRIPT_BLACKLIST = "content.ignore.html.style.script.blacklist";

	public static final String CONTENT_DOWNLOAD_IMAGE_LINKONSITE = "content.download.image.linkOnSite";

	public static final String CONTENT_IMAGE_SOURCES_FOLDER = "/sources";

	public static final String CONTENT_REFINE_EXCLUDE_REGEX = "content.refine.excludeRegex";
	
	public static final String CONTENT_DOWNLOAD_IMAGE = "content.download.image";
	
	public static final String CONTENT_DOWNLOAD_IMAGE_SAVEPATH = "content.download.image.savepath";
	
	public static final String CONTENT_LINK_PREFIX = "content.link.prefix";
	
	public static final String ENTITY_CLASS = "entity.class";
	
	public static final String SUB_ENTITY_CLASS= "subentity.class";

	public static final String ENTITY_THUMBNAIL_FIELD = "entity.thumbnail.field";

	public static final String ENTITY_ID_FIELD = "entity.id.field";
	
	public static final String ENTITY_PUBDATE_USING_CRAWLTIME = "entity.pubdate.using.crawltime";
	
	public static final String ENTITY_AUTO_THUMBNAIL = "entity.auto.thumbnail";

	public static final String ENTITY_RELATE_FIELDS = "entity.relate.fields";

	public static final String ENTITY_RELATE_INSERT_SQL = "entity.relate.insert.sql";
	
	public static final String ENTITY_RELATE_TRAIN_NUM = "entity.relate.train.num";

	public static final String ENTITY_RELATE_COMPARE_NUM = "entity.relate.compare.num";

	public static final String ENTITY_RELATE_MAX_STORE_NUM = "entity.relate.max.store.num";
	
	public static final String ENTITY_RELATE_SIMILAR_THRESHOLD = "entity.relate.similar.threshold";
	
	public static final String ENITY_RELATE_THRESHOLD = "entity.relate.threshold";
	
	public static final String ENITY_RELATE_BOOLEAN_SIMILAR_THRESHOLD = "entity.relate.bool.similar.threshold";

	public static final String ENTITY_RELATE_TRAIN_HOME = "entity.relate.traindata";
	
	public static final String ENTITY_RELATE = "entity.relate";
	
	public static final String TERM_FREQUENCE_MIN = "term.frequence.min";
	
	public static final String STOP_WORD_LIST = "term.stopwords";
	
	public static final String VOBCAB_FILE = "data/vobcab.dat";
	
	public static final String VOBCAB_TAG = "data/vobtag.dat";
	
	public static final String VECTOR_HOME = "data/vectors";
	
	public static final String VECTOR_STORE = "vector";
	
	public  static final String TOP_WORD = "data/topword.data";
	
	public static final String TAG_CONF = "data/appConfig.properties";
	
	public static final String VECTOR_STORAGE_CONF = "data/dbvec.properties";

	public static final String ENTITY_DUPLICATE_MAXURL = "entity.duplicate.maxurl";
}
