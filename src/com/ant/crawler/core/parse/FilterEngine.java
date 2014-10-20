package com.ant.crawler.core.parse;

import org.w3c.dom.Node;

import com.ant.crawler.core.entity.EntityBuilder;

public interface FilterEngine {
	public void init(String expression, String replace);
	
	public String refine(String val, EntityBuilder entity, Node node);
	
	public String refine(String val, EntityBuilder entity);
}
