package com.ant.crawler.core.parse;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.w3c.dom.Node;

import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.entity.EntityBuilder;

public class JSFilterEngine implements FilterEngine {
	private ScriptEngine engine;
	private Configuration conf;
	private String expresion;
	private String replace;
	
	public JSFilterEngine(Configuration conf) {
		this.conf = conf;
		engine = new ScriptEngineManager().getEngineByName("nashorn");
	}

	@Override
	public void init(String expression, String replace) {
		this.expresion = "(function (){" + expression + "})();";
		this.replace = replace;
	}

	@Override
	public String refine(String val, EntityBuilder entity, Node node) {
		try {
			engine.put("conf", conf);
			engine.put("field", val);
			engine.put("replace", replace);
			engine.put("entity", entity);
			engine.put("node", node);
			engine.eval(expresion);
			return (String) engine.get("field");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String refine(String val, EntityBuilder entity) {
		try {
			engine.put("field", val);
			engine.put("replace", replace);
			engine.put("entity", entity);
			engine.eval(expresion);
			return (String)engine.get("field");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}


}
