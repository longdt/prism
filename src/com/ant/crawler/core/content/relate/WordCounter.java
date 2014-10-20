package com.ant.crawler.core.content.relate;

public class WordCounter {
	public void increaseCountWords() {
		++countWords;
	}
	
	public void increaseNumDocs() {
		++numDocs;
	}
	
	public void increaseCountWords(int val) {
		countWords += val;
	}
	
	public void increaseNumDocs(int val) {
		numDocs += val;
	}
	
	public int getCountWords() {
		return countWords;
	}
	public void setCountWords(int countWords) {
		this.countWords = countWords;
	}
	public int getNumDocs() {
		return numDocs;
	}
	public void setNumDocs(int numDocs) {
		this.numDocs = numDocs;
	}
	private volatile int countWords = 1;
	private volatile int numDocs = 1;
}
