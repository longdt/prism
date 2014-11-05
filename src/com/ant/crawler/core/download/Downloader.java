package com.ant.crawler.core.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.ant.crawler.core.utils.NodeUtils;

public class Downloader {
	private static final Logger logger = Logger.getLogger(Downloader.class);
	
	public static boolean download(String url, String saveFilePath) throws InterruptedException {
		try {
			return download(new URL(url), saveFilePath);
		} catch (MalformedURLException e) {
			logger.error("url: " + url + " is invalid", e);
		}
		return false;
	}
	
	public static boolean download(URL url, String saveFilePath) throws InterruptedException {
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
            return true;
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
		return false;
	}
	
	public static String getRemoteFilename(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		String filenameHeader = conn.getHeaderField("Content-Disposition");
		conn.disconnect();
		if (filenameHeader != null && filenameHeader.startsWith("attachment; filename=\"")) {
			return filenameHeader.substring("attachment; filename=\"".length(), filenameHeader.length());
		}
		String filePath = url.getFile();
		int index = filePath.lastIndexOf('/');
		return index == -1 ? filePath : filePath.substring(index + 1);
	}
	
	public static boolean isGreaterLength(URL url, long contentLength) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		long length = conn.getContentLengthLong();
		conn.disconnect();
		return length > contentLength;
	}
}
