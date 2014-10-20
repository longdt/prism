package com.ant.crawler.core.download;

import java.io.IOException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;


public class PageFetcher {
	private WebClient client;
	
	public PageFetcher() {
		client = new WebClient();
		client.getOptions().setJavaScriptEnabled(false);
	}

	public DomNode retrieve(String url) {
		if (url != null) {
			try {
				return client.getPage(url);
			} catch (FailingHttpStatusCodeException | IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public DomNode retrieve(URL url) {
		if (url != null) {
			try {
				return client.getPage(url);
			} catch (FailingHttpStatusCodeException | IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}