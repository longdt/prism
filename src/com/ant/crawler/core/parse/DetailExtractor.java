package com.ant.crawler.core.parse;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.entity.DetailSite;
import com.ant.crawler.core.conf.entity.Expand;
import com.ant.crawler.core.download.PageFetcher;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.plugins.Wrapper;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class DetailExtractor {
	protected Wrapper detailWrapper;
	private List<Expand> expands;
	private List<Wrapper> expandWrappers;
	
	public DetailExtractor(Wrapper wrapper, Configuration conf, DetailSite detailConf) throws InstantiationException, IllegalAccessException {
		this.detailWrapper = wrapper;
		wrapper.init(detailConf.getField());
		expands = detailConf.getExpand();
		Class<Wrapper> wrapperClass = (Class<Wrapper>) wrapper.getClass();
		expandWrappers = new ArrayList<Wrapper>();
		for (Expand expand : expands) {
			expandWrappers.add(WrapperFactory.createWrapper(wrapperClass, conf, expand.getField()));
		}
	}
	
	public boolean extract(HtmlPage htmlDom, EntityBuilder entity) {
		return detailWrapper.extract(htmlDom, entity) && expand(entity, htmlDom);
	}
	
	protected boolean expand(EntityBuilder entity, HtmlPage htmlDom) {
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
				expandPage = PageFetcher.click(htmlDom, expand.getLink());
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
}
