package com.ant.crawler.core.parse;

import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.ant.crawler.core.entity.EntityBuilder;

public class RegexFilterEngine implements FilterEngine {
	private Pattern pattern;
	private String replace;

	@Override
	public void init(String expression, String replace) {
		pattern = Pattern.compile(expression);
		this.replace = replace;
	}

	@Override
	public String refine(String val, EntityBuilder entity, Node node) {
		return pattern.matcher(val).replaceAll(replace);
	}

	@Override
	public String refine(String val, EntityBuilder entity) {
		return pattern.matcher(val).replaceAll(replace);
	}

}
