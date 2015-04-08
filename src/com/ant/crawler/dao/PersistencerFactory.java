package com.ant.crawler.dao;

import java.io.IOException;

import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.dyna.DynaPersistencer;


public class PersistencerFactory {
	private static Persistencer persistencer;
	public static Persistencer getSqlPersistencer() {
		return null; //new SqlPersistencer();
	}
	
	static {
		String backend = PrismConfiguration.getInstance().get(PrismConstants.PERSISTENCER_BACKEND);
		if (backend == null || backend.equals("dyna")) {
			try {
				persistencer = new DynaPersistencer();
			} catch (IOException e) {
			}
		}
	}
	
	public static Persistencer getDefaultPersistencer() {
		return persistencer;
	}
}
