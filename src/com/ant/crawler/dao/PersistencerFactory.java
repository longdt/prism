package com.ant.crawler.dao;

import java.io.IOException;

import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.dyna.DynaPersistencer;


public class PersistencerFactory {
	public static Persistencer getSqlPersistencer() {
		return null; //new SqlPersistencer();
	}
	
	public static Persistencer getDefaultPersistencer() {
		String backend = PrismConfiguration.getInstance().get(PrismConstants.PERSISTENCER_BACKEND);
		if (backend == null || backend.equals("dyna")) {
			try {
				return new DynaPersistencer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (backend.equals("hibernate")) {
			return null;
		}
		return null;
	
	}
}
