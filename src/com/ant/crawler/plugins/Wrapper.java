package com.ant.crawler.plugins;

import java.util.List;

import net.xeoh.plugins.base.Plugin;

import com.ant.crawler.core.conf.entity.Field;
import com.ant.crawler.core.entity.EntityBuilder;
import com.gargoylesoftware.htmlunit.html.DomNode;

public interface Wrapper extends Plugin {
		
	public void init(List<Field> fields);
	
	
	/**
	 *news truoc no da fill 1 phan du lieu rui
	 * trich cay dom truyen vao, fill du lieu vao doi tuong news
	 * 
	 * @param htmlDom
	 * @param news
	 * @return
	 */
	public boolean extract(DomNode node, EntityBuilder news);
	
}