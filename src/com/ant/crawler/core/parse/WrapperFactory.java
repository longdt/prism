package com.ant.crawler.core.parse;

import java.util.List;

import com.ant.crawler.core.conf.Configurable;
import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.entity.Field;
import com.ant.crawler.plugins.Wrapper;

public class WrapperFactory {
	public static Wrapper createDefaultWrapper(Configuration conf, List<Field> fields) {
		XPathWrapper wrapper = new XPathWrapper();
		wrapper.setConf(conf);
		wrapper.init(fields);
		return wrapper;
	}
	
	public static Wrapper createWrapper(Class<? extends Wrapper> wrapperClass, Configuration conf, List<Field> fields) throws InstantiationException, IllegalAccessException {
		Wrapper wrapper = wrapperClass.newInstance();
		if (wrapper instanceof Configurable) {
			((Configurable) wrapper).setConf(conf);
		}
		wrapper.init(fields);
		return wrapper;
	}

	public static Wrapper createWrapper(Class<? extends Wrapper> wrapperClass, Configuration conf) throws InstantiationException, IllegalAccessException {
		Wrapper wrapper = wrapperClass.newInstance();
		if (wrapper instanceof Configurable) {
			((Configurable) wrapper).setConf(conf);
		}
		return wrapper;
	}
}
