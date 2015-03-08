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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;


public class PageFetcher {
	private static final String COOKIE_PREFIX = "Set-Cookie: ";
	private static final String COOKIE_DOMAIN = "domain=";
	private static final String COOKIE_EXPIRE = "expires=";
	private static final String COOKIE_PATH = "path=";
	private static final String COOKIE_HTTP_ONLY = "httponly";
	private static final String COOKIE_SECURE = "secure";
	private static final ThreadLocal<DateFormat> GMT_FORMATER;
	private static final ThreadLocal<DateFormat> GMT1_FORMATER;
	private WebClient client;
	
	static {
		GMT_FORMATER = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			}
		};

		GMT1_FORMATER = new ThreadLocal<DateFormat>() {
			protected DateFormat initialValue() {
				return new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
			};
		};
	}
	
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
		DateFormat gmtFormater = GMT_FORMATER.get();
		DateFormat gmt1Formater = GMT1_FORMATER.get();
		try {
			for (int i = 1; i < parts.length; ++i) {
				String component = parts[i].toLowerCase();
				if (component.startsWith(COOKIE_DOMAIN)) {
					domain = component.substring(COOKIE_DOMAIN.length());
				} else if (component.startsWith(COOKIE_PATH)) {
					path = component.substring(COOKIE_PATH.length());
				} else if (component.startsWith(COOKIE_EXPIRE)) {
					component = component.substring(COOKIE_EXPIRE.length());
					expire = gmtFormater.parse(component);
					if (expire == null) {
						expire = gmt1Formater.parse(component);
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

	public HtmlPage retrieve(String url) {
		if (url != null) {
			try {
				return client.getPage(url);
			} catch (FailingHttpStatusCodeException | IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public HtmlPage retrieve(URL url) {
		if (url != null) {
			try {
				return client.getPage(url);
			} catch (FailingHttpStatusCodeException | IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public HtmlPage navigate(HtmlPage page, String xpath) throws IOException {
		HtmlAnchor anchor = (HtmlAnchor) page.getFirstByXPath(xpath);
		URL origURL = page.getUrl();
		URL distURL = new URL(origURL, anchor.getHrefAttribute());
		return retrieve(distURL);
	}
	
	public static HtmlPage click(HtmlPage page, String xpath) throws IOException {
		HtmlAnchor anchor = (HtmlAnchor) page.getFirstByXPath(xpath);
		return (anchor != null) ? anchor.<HtmlPage>click() : null;
	}
}