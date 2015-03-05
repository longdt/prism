package com.ant.crawler.core.content.relate.global;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;


public class VectorDoc implements Serializable {
	private BitSet vectorBool;
	private Map<String, Float> vectorWeight;
	private long docid;
	
	public VectorDoc() {
		docid = -1;
		vectorBool = new BitSet();
		vectorWeight = new HashMap<String, Float>();
	}
	
	public VectorDoc(BitSet vectorBool, Map<String, Float> vectorWeight) {
		this.vectorBool = vectorBool;
		this.vectorWeight = vectorWeight;
	}
	
	public VectorDoc(long docid, BitSet vectorBool, Map<String, Float> vectorWeight) {
		this.docid = docid;
		this.vectorBool = vectorBool;
		this.vectorWeight = vectorWeight;
	}
	
	public BitSet getVectorBool() {
		return vectorBool;
	}
	public void setVectorBool(BitSet vectorBool) {
		this.vectorBool = vectorBool;
	}
	public Map<String, Float> getVectorWeight() {
		return vectorWeight;
	}
	public void setVectorWeight(Map<String, Float> vectorWeight) {
		this.vectorWeight = vectorWeight;
	}
	public long getDocid() {
		return docid;
	}
	public void setDocid(long docid) {
		this.docid = docid;
	}
}
