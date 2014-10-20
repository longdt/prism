package com.ant.crawler.core.content.tag;

public class WordMetadata {
	private String word;
	private int frequence;
	private int deFrequence;
	private boolean isVailCompoundWord ;
	
	public WordMetadata () {
		this.word = "";
		this.frequence = 0;
		this.deFrequence = 0;
		this.isVailCompoundWord = false;
	}
	
	public boolean isVailCompoundWord() {
		return isVailCompoundWord;
	}



	public void setVailCompoundWord(boolean isVailCompoundWord) {
		this.isVailCompoundWord = isVailCompoundWord;
	}



	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getFrequence() {
		return frequence;
	}
	public void setFrequence(int frequence) {
		this.frequence = frequence;
	}
	
	public int getDeFrequence() {
		return deFrequence;
	}
	public void setDeFrequence(int deFrequence) {
		this.deFrequence = deFrequence;
	}
	public void increFrequence() {
		++frequence;
	}
	
	public void decreFrequence() {
		--frequence;
	}
	
	public void increDefrequence() {
		++deFrequence;
	}
}
