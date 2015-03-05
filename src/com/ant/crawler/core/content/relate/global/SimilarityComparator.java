package com.ant.crawler.core.content.relate.global;

import java.util.List;

import com.ant.crawler.core.content.relate.DocSimilar;

public interface SimilarityComparator {

	public abstract List<DocSimilar> similar(VectorDoc vector) throws Exception;

	public abstract void add(VectorDoc currentVec);

}