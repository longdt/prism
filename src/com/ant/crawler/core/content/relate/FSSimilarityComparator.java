package com.ant.crawler.core.content.relate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class FSSimilarityComparator extends AbstractSimilarityComparator {
	private VectorDocDAO vectorDao;

	public FSSimilarityComparator(VectorDocDAO vectorDao, int maxNum,
			float bThres, float sameThres, float relateThres) throws Exception {
		super(maxNum, bThres, sameThres, relateThres);
		this.vectorDao = vectorDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ant.crawler.core.similar.SimilarComparator#similar(com.ant.crawler.
	 * core.similar.VectorDoc)
	 */
	@Override
	public List<DocSimilar> similar(VectorDoc vector) throws InterruptedException {
		Map<String, Float> vectorW = vector.getVectorWeight();
		List<DocSimilar> relateNews = new ArrayList<DocSimilar>();
		float cosAngel = 0;
		int bSize = vector.getVectorBool().cardinality();
		VectorDocIterator iterator = null;
		try {
			iterator = vectorDao.getIterator();
			VectorDoc element = null;
			while ((element = iterator.next()) != null) {
            	if (Thread.interrupted()) {
            		throw new InterruptedException();
            	}
				BitSet vectorBool = (BitSet) element.getVectorBool().clone();
				vectorBool.and(vector.getVectorBool());
				if (vectorBool.cardinality() / (float) bSize >= bThres) {
					cosAngel = cosine(vectorW, element.getVectorWeight());
					if (cosAngel >= sameThres) {
						return null;
					} else if (cosAngel >= relateThres) {
						relateNews.add(new DocSimilar(element.getDocid(),
								cosAngel));
					}
				}
			}
		} finally {
			iterator.close();
		}
		return relateNews;
	}

	@Override
	public void add(VectorDoc currentVec) {

	}

}
