/**
 * 
 */
package com.ant.crawler.core;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.commons.beanutils.BeanUtils;

import com.ant.crawler.core.conf.entity.EntityConf;
import com.ant.crawler.core.conf.entity.Filter;
import com.ant.crawler.core.conf.entity.MappingField;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.parse.FilterEngine;
import com.ant.crawler.core.parse.JSFilterEngine;
import com.ant.crawler.core.parse.RegexFilterEngine;
import com.ant.crawler.core.parse.XPathWrapper;
import com.ant.crawler.dao.Persistencer;
import com.ant.crawler.plugins.PluginException;
import com.ant.crawler.plugins.Wrapper;
import com.rometools.rome.feed.synd.SyndEntry;

/**
 * @author thienlong
 * 
 */
@PluginImplementation
public class SimpleRssCrawler extends AbstractRssCrawler {
	private Map<String, List<FilterEngine>> filterAll;
	private List<MappingField> fields;

	@Override
	public void init(EntityConf entityConf, Wrapper wrapper,
			Persistencer persistencer) throws PluginException {
		super.init(entityConf, wrapper, persistencer);
		fields = entityConf.getEntityFields().getRssSite().getMappingField();
		filterAll = new HashMap<String, List<FilterEngine>>();
		initFilter();
	}

	private void initFilter() {
		List<Filter> filters = null;
		List<FilterEngine> filterEngines = null;
		FilterEngine engine = null;
		String filterType = null;
		for (MappingField field : fields) {
			filters = field.getFilter();
			filterEngines = filterAll.get(field.getEntityField());
			if (filterEngines == null) {
				filterEngines = new ArrayList<FilterEngine>();
				filterAll.put(field.getEntityField(), filterEngines);
			}
			for (Filter filter : filters) {
				filterType = filter.getType();
				switch (filterType) {
				case "regexall":
					engine = new RegexFilterEngine();
					filterEngines.add(engine);
					break;
				case "scriptall":
					engine = new JSFilterEngine(conf);
					filterEngines.add(engine);
					break;
				default:
					continue;
				}
				engine.init(filter.getValue(), filter.getReplace());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ant.crawler.core.AbstractRssCrawler#populate(com.ant.crawler.core
	 * .entity .News, com.sun.syndication.feed.synd.SyndEntry)
	 */
	@Override
	protected boolean populate(EntityBuilder entity, SyndEntry item) {
		Date creatTime = item.getPublishedDate();
		if (creatTime != null && !creatTime.after(lastAccessTime)) {
			return false;
		}
		entity.setCreateTime(creatTime);

		try {
			entity.setDetailUrl(new URL(item.getLink().trim()));

			for (MappingField field : fields) {
				String val = BeanUtils.getProperty(item, field.getRssField());
				if (val == null && !field.isRequired()) {
					continue;
				}
				val = XPathWrapper.filterAllContent(filterAll,
						field.getEntityField(), val, entity);
				entity.set(field.getEntityField(), val);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
