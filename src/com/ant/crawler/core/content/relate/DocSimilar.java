package com.ant.crawler.core.content.relate;

public class DocSimilar implements Comparable<DocSimilar> {
	private long docid;
	private float percent;

	
	public DocSimilar() {
		
	}
	
	public DocSimilar(long docid, float percent) {
		this.docid = docid;
		this.percent = percent;
	}
	
	@Override
	public int compareTo(DocSimilar docSimilar) {
		return Float.compare(docSimilar.percent, percent);
	}
	
	public boolean equals(Object other) {
		if (other instanceof DocSimilar) {
			DocSimilar docsimilar = (DocSimilar) other;
			return (docid == docsimilar.docid);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int)docid;
	}

	public long getDocid() {
		return docid;
	}

	public void setDocid(long docid) {
		this.docid = docid;
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}
}
