package com.ant.crawler.core.content.relate;


import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;



public class VectorSpace {
	private LinkedHashMap<String, Float> bagWord = new LinkedHashMap<String, Float>();
	private LinkedList<VectorDoc> vectorDocs;
	
	public VectorSpace() {

	}
	
	public boolean hasBagWord() {
		return (bagWord != null && !bagWord.isEmpty());
	}
	
	public void setBagWord(LinkedHashMap<String, Float> bagWord) {
		this.bagWord = bagWord;
	}
	
	public Map<String, Float> getBagWord() {
		return bagWord;
	}

	public LinkedList<VectorDoc> getVectorDocs() {
		return vectorDocs;
	}

	public void setVectorDocs(LinkedList<VectorDoc> queue) {
		this.vectorDocs = queue;
	}

}
