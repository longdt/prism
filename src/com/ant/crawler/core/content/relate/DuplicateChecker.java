package com.ant.crawler.core.content.relate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DuplicateChecker {
	private String dataFile;
	private Map<URL, Boolean> urls;
	private int maxUrls;
	
	public DuplicateChecker(String pluginDir, int maxUrls) throws IOException {
		dataFile = pluginDir + "/visited.urls";
		this.maxUrls = maxUrls;
		urls = new LinkedHashMap<URL, Boolean>() {
			@Override
			protected boolean removeEldestEntry(
					java.util.Map.Entry<URL, Boolean> eldest) {
				return size() > DuplicateChecker.this.maxUrls;
			}
		};
		loadVisitedUrls();
	}
	
	

	private void loadVisitedUrls() throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				urls.put(new URL(line), Boolean.TRUE);
			}
		} catch (FileNotFoundException | MalformedURLException e) {
		}
	}



	public void accept(URL url) {
		urls.put(url, Boolean.TRUE);
	}

	public boolean test(URL url) {
		return maxUrls > 0 && urls.containsKey(url);
	}
	
	public void save() throws IOException {
		try (PrintWriter writer = new PrintWriter(dataFile)) {
			for (Entry<URL, Boolean> entry : urls.entrySet()) {
				writer.println(entry.getKey());
			}
		}
	}

}
