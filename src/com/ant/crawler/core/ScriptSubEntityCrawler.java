package com.ant.crawler.core;

import java.util.List;

import org.w3c.dom.Node;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.conf.entity.ScriptSite;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.parse.JSFilterEngine;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ScriptSubEntityCrawler extends SubEntityCrawler {
	private JSFilterEngine engine;
	private String xpath;
	
	@Override
	public void init(EntityConf entityConf, Wrapper detailWrapper,
			Persistencer persistencer) throws Exception {
		super.init(entityConf, detailWrapper, persistencer);
		engine = new JSFilterEngine(conf);
		ScriptSite scriptConf = entityConf.getEntityFields().getSubEntity().getScriptSite();
		engine.init(scriptConf.getValue(), null);
		xpath = scriptConf.getXpath();
	}
	
	@Override
	protected void createSubEntity(HtmlPage htmlDom, EntityBuilder entity) {
		if (xpath == null || xpath.isEmpty()) {
			engine.refine(null, entity, htmlDom);
			return;
		}
		List<DomNode> itemNode = (List<DomNode>) htmlDom.getByXPath(xpath);
		if (itemNode == null || itemNode.isEmpty()) {
			return;
		}
		for (DomNode node : itemNode) {
			engine.refine(node.getNodeValue(), entity, node);
		}
	}

}
