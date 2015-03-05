package com.ant.crawler.core.content.relate;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class DuplicateChecker {
	private Map<URL, Boolean> urls;
	private int maxUrls;
	
	public DuplicateChecker(String pluginDir, int maxUrls) {
		this.maxUrls = maxUrls;
		urls = new LinkedHashMap<URL, Boolean>() {
			@Override
			protected boolean removeEldestEntry(
					java.util.Map.Entry<URL, Boolean> eldest) {
				return size() > DuplicateChecker.this.maxUrls;
			}
		};
	}

	public void accept(URL url) {
		urls.put(url, Boolean.TRUE);
	}

	public boolean test(URL url) {
		return maxUrls > 0 && urls.containsKey(url);
	}

}
