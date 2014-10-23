package com.ant.crawler.core.parse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ant.crawler.core.conf.Configurable;
import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.conf.entity.Field;
import com.ant.crawler.core.conf.entity.Filter;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.utils.NodeUtils;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;

@PluginImplementation
public class XPathWrapper implements Wrapper, Configurable {
	private static final Logger logger = Logger.getLogger(XPathWrapper.class);
	private static final String IMG_TAG = "img";
	private static final String A_TAG = "a";
	private static final String SCRIPT_TAG = "script";
	private XPath xpath;
	private Configuration conf;
	private List<Field> fields;
	private boolean autoThumbnail;
	private String thumbnailField;
	private static boolean downImg;
	private static String imgLinkOnSite;
	private Map<String, List<FilterEngine>> filterOne;
	private Map<String, List<FilterEngine>> filterAll;
	private Set<String> fieldIndexs;
	
	static {
		downImg = PrismConfiguration.getInstance().getBoolean(
				PrismConstants.CONTENT_DOWNLOAD_IMAGE, false);
		//test write sample file
		if (downImg) {
			validateImgSavePath();			
		}
		imgLinkOnSite = PrismConfiguration.getInstance().get(
				PrismConstants.CONTENT_DOWNLOAD_IMAGE_LINKONSITE);
	}

	public XPathWrapper() {
		xpath = XPathFactory.newInstance().newXPath();
		filterOne = new HashMap<String, List<FilterEngine>>();
		filterAll = new HashMap<String, List<FilterEngine>>();
	}
	
	private static void validateImgSavePath() {
		String imgSavePath = PrismConfiguration.getInstance().get(
				PrismConstants.CONTENT_DOWNLOAD_IMAGE_SAVEPATH);
		File savePath = new File(imgSavePath, PrismConstants.CONTENT_IMAGE_SOURCES_FOLDER);
		if (!savePath.isDirectory()) {
			if (savePath.mkdirs()) {
				return;
			}
			logger.error("can't create directory: " + savePath.getAbsolutePath());
			System.exit(1);
		}
		Writer out = null;
		try {
			out = new FileWriter(new File(savePath, "test.txt"));
			out.write("test write file");
		} catch (Exception e) {
			logger.error("cant write test file", e);
			System.exit(1);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void init(List<Field> fields) {
		this.fields = fields;
		initFilter();
		autoThumbnail = PrismConfiguration.getInstance().getBoolean(
				PrismConstants.ENTITY_AUTO_THUMBNAIL, false);
		thumbnailField = conf.get(PrismConstants.ENTITY_THUMBNAIL_FIELD);
		fieldIndexs = new HashSet<String>(PrismConfiguration.getInstance()
				.getStringCollection(PrismConstants.ENTITY_RELATE_FIELDS));
	}

	private void initFilter() {
		List<Filter> filters = null;
		List<FilterEngine> one = null;
		List<FilterEngine> all = null;
		FilterEngine engine = null;
		for (Field field : fields) {
			filters = field.getFilter();
			one = filterOne.get(field.getName());
			if (one == null) {
				one = new ArrayList<FilterEngine>();
				filterOne.put(field.getName(), one);
			}
			all = filterAll.get(field.getName());
			if (all == null) {
				all = new ArrayList<FilterEngine>();
				filterAll.put(field.getName(), all);
			}
			for (Filter filter : filters) {
				if (filter.getType().equals("regexone")) {
					engine = new RegexFilterEngine();
					one.add(engine);
				} else if (filter.getType().equals("scriptone")) {
					engine = new ScriptFilterEngine(conf);
					one.add(engine);
				} else if (filter.getType().equals("regexall")) {
					engine = new RegexFilterEngine();
					all.add(engine);
				} else if (filter.getType().equals("scriptall")) {
					engine = new ScriptFilterEngine(conf);
					all.add(engine);
				}
				engine.init(filter.getValue(), filter.getReplace());
			}
		}
	}

	private void refineImgNode(DomElement imgNode, EntityBuilder entity) {
		String uri = imgNode.getAttribute("src");
		if (uri != null) {
			try {
				URL url = new URL(entity.getSourceUrl(), uri);
				String link = url.toString();
				if (downImg) {
					String filePath = url.getFile();
					int fileIdx = filePath.lastIndexOf('/');
					if (fileIdx != -1) {
						filePath = filePath.substring(fileIdx + 1);
					}
					if (filePath.contains("?")) {
						filePath = String.valueOf(filePath.hashCode());
					}
					
					filePath = PrismConstants.CONTENT_IMAGE_SOURCES_FOLDER + "/"
							+ System.currentTimeMillis() + "_" + URLDecoder.decode(filePath, "UTF-8");
					link = imgLinkOnSite + filePath;
					entity.addDownloadImg(url, filePath);
				}

				imgNode.setAttribute("src", link);
				if (thumbnailField != null && autoThumbnail
						&& entity.get(thumbnailField) == null) {
					entity.set(thumbnailField, link);
				}
				NodeUtils.styleDescImage(imgNode);
			} catch (Exception e) {
				logger.warn("can't refine node: " + imgNode.asXml(), e);
			}
		}

	}

	protected void refineHyperLinkNode(DomElement aNode, URL url) {
		String uri = aNode.getAttribute("href");
		if (uri != null) {
			try {
				aNode.setAttribute("href",
						NodeUtils.getURLOnSite(new URL(url, uri)));
			} catch (Exception e) {
				logger.debug("can't refine node: " + aNode.asXml(), e);
			}
		}
		return;

	}

	protected void removeScriptNode(DomElement scriptNode) {
		Node parent = scriptNode.getParentNode();
		parent.removeChild(scriptNode);
	}

	/**
	 * refine a given node. the node should normalize urls inside node.
	 * 
	 * @param node
	 * @param url
	 * @return true if node's ready to using, false if node should was ignore
	 */
	protected boolean refineNode(DomNode node, EntityBuilder entity) {
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			return true;
		}
		DomElement eleNode = (DomElement) node;
		URL url = entity.getSourceUrl();
		if (eleNode.getTagName().equalsIgnoreCase(SCRIPT_TAG)) {
			return false;
		} else if (eleNode.getTagName().equalsIgnoreCase(IMG_TAG)) {
			refineImgNode(eleNode, entity);
		} else if (eleNode.getTagName().equalsIgnoreCase(A_TAG)) {
			refineHyperLinkNode(eleNode, url);
		}
		NodeList nodeList = eleNode.getElementsByTagName(IMG_TAG);
		for (int i = 0; i < nodeList.getLength(); ++i) {
			refineImgNode((DomElement) nodeList.item(i), entity);
		}
		nodeList = eleNode.getElementsByTagName(A_TAG);
		for (int i = 0; i < nodeList.getLength(); ++i) {
			refineHyperLinkNode((DomElement) nodeList.item(i), url);
		}
		nodeList = eleNode.getElementsByTagName(SCRIPT_TAG);
		for (int i = 0; i < nodeList.getLength(); ++i) {
			removeScriptNode((DomElement) nodeList.item(i));
		}
		return true;
	}

	protected boolean fillDataWithFilter(EntityBuilder entity,
			String fieldName, List<DomNode> itemNode) throws IllegalAccessException,
			InvocationTargetException {
		StringBuilder value = new StringBuilder();
		StringBuilder plainText = new StringBuilder();
		DomNode node = null;
		String nodeContent = null;
		for (int i = 0; i < itemNode.size(); ++i) {
			node = itemNode.get(i);
			if (refineNode(node, entity)) {
				nodeContent = filterOneContent(fieldName,
						NodeUtils.getContent(node), entity, node);
				if (nodeContent == null) {
					return false;
				}
				value.append(nodeContent).append('\n');
				if (fieldIndexs.contains(fieldName)) {
					plainText.append(NodeUtils.getTextContent(node)).append(
							'\n');
				}
			}
			
		}
		if (value.length() > 0) {
			value.deleteCharAt(value.length() - 1);
		}
		String result = filterAllContent(filterAll, fieldName,
				value.toString(), entity);
		if (result == null) {
			return false;
		} else if (fieldName.equals(PrismConstants.ENTITY_TEMP_FIELD)) {
			return true;
		}

		entity.set(fieldName, result);
		if (fieldIndexs.contains(fieldName)) {
			entity.addIndexData(plainText.toString());
		}
		return true;
	}

	private String filterOneContent(String fieldName, String textContent,
			EntityBuilder entity, Node node) {
		List<FilterEngine> engines = filterOne.get(fieldName);
		for (int i = 0, n = engines.size(); i < n && textContent != null; ++i) {
			textContent = engines.get(i).refine(textContent, entity, node);
		}
		return textContent;
	}

	public static String filterAllContent(
			Map<String, List<FilterEngine>> filters, String fieldName,
			String textContent, EntityBuilder entity) {
		List<FilterEngine> engines = filters.get(fieldName);
		for (int i = 0, n = engines.size(); i < n && textContent != null; ++i) {
			textContent = engines.get(i).refine(textContent, entity);
		}
		return textContent;
	}

	@Override
	public boolean extract(DomNode htmlDom, EntityBuilder entity) {
		for (Field field : fields) {
			try {
				 List<DomNode> itemNode = (List<DomNode>) htmlDom.getByXPath(field.getXpath());
				if (itemNode == null || itemNode.isEmpty()) {
					if (field.isRequired()) {
						return false;
					} else {
						continue;
					}
				}
				if (!fillDataWithFilter(entity, field.getName(), itemNode)
						&& field.isRequired()) {
					return false;
				}
			} catch (Exception e) {
				logger.error("error when extract data: " + entity, e);
				return false;
			}
		}
		return true;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	@Override
	public Configuration getConf() {
		return conf;
	}
}
