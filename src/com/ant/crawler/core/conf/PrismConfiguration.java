package com.ant.crawler.core.conf;

import java.io.File;
import java.net.MalformedURLException;

import com.ant.crawler.core.utils.PrismConstants;


public class PrismConfiguration extends Configuration {
	private static final File PRISM_CONFIG_FILE = new File(PrismConstants.PRIM_CONFIG_FILE);
	private static PrismConfiguration conf = new PrismConfiguration();
	
	private PrismConfiguration() {
		try {
			addResource(PRISM_CONFIG_FILE.toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public static Configuration getInstance() {
		return conf;
	}
}
