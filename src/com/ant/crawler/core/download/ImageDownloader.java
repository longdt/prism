package com.ant.crawler.core.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.log4j.Logger;

import com.ant.crawler.core.utils.NodeUtils;

public class ImageDownloader {
	private static final Logger logger = Logger.getLogger(ImageDownloader.class);
	
	public static void download(String url, String saveFilePath) throws InterruptedException {
		try {
			download(new URL(url), saveFilePath);
		} catch (MalformedURLException e) {
			logger.error("url: " + url + " is invalid", e);
		}
	}
	
	public static void download(URL url, String saveFilePath) throws InterruptedException {
		InputStream in = null;
		OutputStream out = null;
		try {
			url = NodeUtils.refineURL(url);
			in = url.openStream();
			out = new FileOutputStream(saveFilePath);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
            	if (Thread.interrupted()) {
            		throw new InterruptedException();
            	}
                out.write(buf, 0, len);
            }
		} catch (Exception e) {
			logger.error("Can't downloads image on " + url, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("Error on downloading image " + url, e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Error on downloading image " + url, e);
				}
			}
		}
	}
}
