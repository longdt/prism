package com.ant.crawler.core.parse;

import org.w3c.dom.Node;

import bsh.EvalError;
import bsh.Interpreter;

import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.entity.EntityBuilder;

public class JavaFilterEngine implements FilterEngine {
	private Interpreter interpreter = new Interpreter();
	private Configuration conf;
	private String expresion;
	private String replace;
	
	public JavaFilterEngine(Configuration conf) {
		this.conf = conf;
	}

	@Override
	public void init(String expression, String replace) {
		this.expresion = expression;
		this.replace = replace;
	}

	@Override
	public String refine(String val, EntityBuilder entity, Node node) {
		try {
			interpreter.set("conf", conf);
			interpreter.set("field", val);
			interpreter.set("replace", replace);
			interpreter.set("entity", entity);
			interpreter.set("node", node);
			interpreter.eval(expresion);
			return (String)interpreter.get("field");
		} catch (EvalError e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String refine(String val, EntityBuilder entity) {
		try {
			interpreter.set("field", val);
			interpreter.set("replace", replace);
			interpreter.set("entity", entity);
			interpreter.eval(expresion);
			return (String)interpreter.get("field");
		} catch (EvalError e) {
			e.printStackTrace();
		}
		return null;
	}


}
