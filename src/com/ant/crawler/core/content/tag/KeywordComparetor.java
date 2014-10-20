package com.ant.crawler.core.content.tag;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map.Entry;

public class KeywordComparetor implements Comparator<Entry<String, Double>> {
	@Override
	public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
		if (o1.getValue() > o2.getValue()) {
			return -1;
		}
		
		if (o1.getValue() < o2.getValue()) {
			return 1;
		}
		
		return 0;
	}
	
	public static void main(String[] args) throws IOException {
		String dung ="";
		System.out.println(dung.substring(0, 0));
	}
}
