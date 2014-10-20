package com.ant.crawler.core.parse;

import java.util.List;

import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.entity.Field;
import com.ant.crawler.plugins.Wrapper;

public class WrapperFactory {
	public static Wrapper createNewsWrapper(Configuration conf, List<Field> fields) {
		XPathWrapper wrapper = new XPathWrapper();
		wrapper.setConf(conf);
		wrapper.init(fields);
		return wrapper;
	}
}
