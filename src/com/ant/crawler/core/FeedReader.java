/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ant.crawler.core;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherListener;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;

/**
 * Reads and prints any RSS/Atom feed type. Converted from the original Rome
 * sample FeedReader
 * <p>
 * 
 * @author Alejandro Abdelnur
 * @author Nick Lothian
 * 
 */
public class FeedReader {
	private static final Logger logger = Logger.getLogger(FeedReader.class);
	private FeedFetcherCache feedInfoCache;
	private FeedFetcher fetcher;
	private boolean unchange;
	public FeedReader() {
		feedInfoCache = HashMapFeedInfoCache.getInstance();
		fetcher = new HttpURLFeedFetcher(feedInfoCache);
		FetcherEventListenerImpl listener = new FetcherEventListenerImpl();
		fetcher.addFetcherEventListener(listener);
	}

	public SyndFeed retrieveFeed(URL feedUrl) {
		unchange = false;
		try {
			SyndFeed feed = fetcher.retrieveFeed(feedUrl);
			return unchange ? null : feed;
		} catch (Exception ex) {
			logger.error("Can't retrieve feed from URL:	" + feedUrl, ex);
		}
		return null;
	}
	
	public SyndFeed retrieveFeed(String feedUrl) {
		try {
			return retrieveFeed(new URL(feedUrl));
		} catch (MalformedURLException e) {
			logger.error("Can't retrieve feed from URL:	" + feedUrl, e);
		}
		return null;
	}

	class FetcherEventListenerImpl implements FetcherListener {
		/**
		 * @see com.sun.syndication.fetcher.FetcherListener#fetcherEvent(com.sun.syndication.fetcher.FetcherEvent)
		 */
		public void fetcherEvent(FetcherEvent event) {
			String eventType = event.getEventType();
			if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(eventType)) {
				unchange = true;
			}
		}
	}
}
