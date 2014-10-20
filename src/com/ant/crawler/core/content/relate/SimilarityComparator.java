package com.ant.crawler.core.content.relate;

import java.util.List;

public interface SimilarityComparator {

	public abstract List<DocSimilar> similar(VectorDoc vector) throws InterruptedException;

	public abstract void add(VectorDoc currentVec);

}