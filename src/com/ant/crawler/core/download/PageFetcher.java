package com.ant.crawler.core.download;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.utils.PrismConstants;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.util.Cookie;


public class PageFetcher {
	private static final String COOKIE_PREFIX = "Set-Cookie: ";
	private static final String COOKIE_DOMAIN = "domain=";
	private static final String COOKIE_EXPIRE = "expires=";
	private static final String COOKIE_PATH = "path=";
	private static final String COOKIE_HTTP_ONLY = "httponly";
	private static final String COOKIE_SECURE = "secure";
	private static final DateFormat GMT_FORMATER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	private static final DateFormat GMT1_FORMATER = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
	private WebClient client;
	
	public PageFetcher() {
		client = new WebClient();
		client.getOptions().setJavaScriptEnabled(false);
	}

	public void init(Configuration conf) {
		boolean js = conf.getBoolean(PrismConstants.WEBCLIENT_JAVASCRIPT_ENABLED, false);
		client.getOptions().setJavaScriptEnabled(js);
		String cookies = conf.get(PrismConstants.WEBCLIENT_COOKIES);
		if (cookies != null) {
			setCookies(cookies.trim().split("\\s*\n\\s*"));
		}
	}

	private void setCookies(String[] cookies) {
		CookieManager cookieManager = client.getCookieManager();
		for (String cookieStr : cookies) {
			Cookie cookie = parseCookie(cookieStr);
			if (cookie != null) {
				cookieManager.addCookie(cookie);
			}
		}
	}

	private Cookie parseCookie(String cookieStr) {
		if (!cookieStr.startsWith(COOKIE_PREFIX)) {
			return null;
		}
		cookieStr = cookieStr.substring(COOKIE_PREFIX.length());
		String[] parts = cookieStr.split("; ");
		if (parts.length < 2) {
			parts = cookieStr.split(";");
			if (parts.length < 2) {
				return null;
			}
		}
		String[] keyValue = parts[0].split("=");
		String domain = null;
		String path = null;
		Date expire = null;
		boolean secure = false;
		boolean httpOnly = false;
		try {
			for (int i = 1; i < parts.length; ++i) {
				String component = parts[i].toLowerCase();
				if (component.startsWith(COOKIE_DOMAIN)) {
					domain = component.substring(COOKIE_DOMAIN.length());
				} else if (component.startsWith(COOKIE_PATH)) {
					path = component.substring(COOKIE_PATH.length());
				} else if (component.startsWith(COOKIE_EXPIRE)) {
					component = component.substring(COOKIE_EXPIRE.length());
					expire = GMT_FORMATER.parse(component);
					if (expire == null) {
						expire = GMT1_FORMATER.parse(component);
					}
				} else if (component.equals(COOKIE_HTTP_ONLY)) {
					httpOnly = true;
				} else if (component.equals(COOKIE_SECURE)) {
					secure = true;
				}
			}
		} catch (ParseException e) {
			
		}
		return new Cookie(domain, keyValue[0], keyValue[1], path, expire, secure, httpOnly);
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