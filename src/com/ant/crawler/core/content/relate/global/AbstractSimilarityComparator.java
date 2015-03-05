package com.ant.crawler.core.content.relate.global;

import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractSimilarityComparator implements SimilarityComparator {
	protected float bThres;
	protected float sameThres;
	protected float relateThres;

	public AbstractSimilarityComparator(int maxNum, float bThres, float sameThres, float relateThres) {
		this.bThres = bThres;
		this.sameThres = sameThres;
		this.relateThres = relateThres;
	}

	public static float cosine(Map<String, Float> vectorSrc, Map<String, Float> vector) {
		float multiVector = 0;
		String word = null;
		Float tmpW = null;
		for (Entry<String, Float> entry : vectorSrc.entrySet()) {
			word = entry.getKey();
			if ((tmpW = vector.get(word)) != null) {
				multiVector += tmpW * entry.getValue();
			}
		}
		return (float) (multiVector);
	}

}