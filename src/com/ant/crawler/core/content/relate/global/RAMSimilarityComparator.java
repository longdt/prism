package com.ant.crawler.core.content.relate.global;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ant.crawler.core.content.relate.DocSimilar;

public class RAMSimilarityComparator extends AbstractSimilarityComparator implements SimilarityComparator {
	private static Logger logger = Logger.getLogger(RAMSimilarityComparator.class);
	private VectorQueue vectorDocs;
	/**
	 * @param args
	 * @throws Exception
	 */
	public RAMSimilarityComparator(Collection<VectorDoc> vecDocs, int maxNum, float bThres, float sameThres, float relateThres) throws Exception {
		super(maxNum, bThres, sameThres, relateThres);
		this.vectorDocs = new VectorQueue(maxNum);
		for (VectorDoc vector : vecDocs) {
			vectorDocs.put(vector.getDocid(), vector);
		}
	}


	/* (non-Javadoc)
	 * @see com.ant.crawler.core.similar.SimilarComparator#similar(com.ant.crawler.core.similar.VectorDoc)
	 */
	@Override
	public List<DocSimilar> similar(VectorDoc vector) throws InterruptedException {
		Map<String, Float> vectorW = vector.getVectorWeight();
		List<DocSimilar> relateNews = new ArrayList<DocSimilar>();
		float cosAngel = 0;
		int bSize = vector.getVectorBool().cardinality();
		for (VectorDoc element : vectorDocs.values()) {
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
					relateNews.add(new DocSimilar(element.getDocid(), cosAngel));
				}
			}
		}
		return relateNews;
	}

	/* (non-Javadoc)
	 * @see com.ant.crawler.core.similar.SimilarComparator#add(com.ant.crawler.core.similar.VectorDoc)
	 */
	@Override
	public void add(VectorDoc currentVec) {
		vectorDocs.put(currentVec.getDocid(), currentVec);
	}

	private static class VectorQueue extends LinkedHashMap<Long, VectorDoc> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int maxNum;
		public VectorQueue(int maxNum) {
			this.maxNum = maxNum;
		}
		
		@Override
		protected boolean removeEldestEntry(
				java.util.Map.Entry<Long, VectorDoc> eldest) {
			return size() > maxNum;
		}
	}
	
}
