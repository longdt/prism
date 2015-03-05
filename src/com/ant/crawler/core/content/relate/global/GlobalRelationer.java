package com.ant.crawler.core.content.relate.global;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.ConfigurationException;
import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.content.relate.DocSimilar;
import com.ant.crawler.core.content.relate.Relationer;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.utils.PrismConstants;

public class GlobalRelationer implements Relationer {
	private static final Logger logger = Logger
			.getLogger(GlobalRelationer.class);
	private static Relationer instance = new GlobalRelationer();
	private SimilarityComparator comparator;
	private VectorNormalizor normalizor;
	private VectorDocDAO vectorDAO;
	private VectorDoc currentVec;
	private int maxStoreNum;
	/**
	 * @throws IOException
	 * 
	 */
	private GlobalRelationer() {
		try {
			normalizor = new VectorNormalizor();
			vectorDAO = new VectorDocDAOImpl();
			Configuration conf = PrismConfiguration.getInstance();
			float bThres = conf.getFloat(PrismConstants.ENITY_RELATE_BOOLEAN_SIMILAR_THRESHOLD, -1);
			float simiThres = conf.getFloat(PrismConstants.ENTITY_RELATE_SIMILAR_THRESHOLD, -1);
			float relateThres = conf.getFloat(PrismConstants.ENITY_RELATE_THRESHOLD, -1);
			int maxNum = conf.getInt(PrismConstants.ENTITY_RELATE_COMPARE_NUM, Integer.MAX_VALUE);
			maxStoreNum = conf.getInt(PrismConstants.ENTITY_RELATE_MAX_STORE_NUM, Integer.MAX_VALUE);
			if (simiThres == -1) {
				throw new ConfigurationException("invalid " + PrismConstants.ENTITY_RELATE_SIMILAR_THRESHOLD + " configuration");
			}
			if (relateThres == -1) {
				throw new ConfigurationException("invalid " + PrismConstants.ENITY_RELATE_THRESHOLD + " configuration");
			}
			comparator = new RAMSimilarityComparator(vectorDAO.getRecentVectorDocs(maxNum), maxNum, bThres, simiThres, relateThres);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Relationer getInstance() {
		return instance;
	}

	@Override
	public List<DocSimilar> relate(EntityBuilder entity) {
		VectorDoc vecDoc = normalizor.normalize(entity.getIndexDatas());
		List<DocSimilar> relates = null;
		try {
			relates = comparator.similar(vecDoc);
		} catch (Exception e) {
			logger.error("error when compute similar vectors", e);
		}
		currentVec = vecDoc;
		return relates;
	}

	@Override
	public void storeCurrEntityWithID(long newsID) {
		currentVec.setDocid(newsID);
		vectorDAO.storeVectorDoc(currentVec);
		comparator.add(currentVec);
		if (vectorDAO.count() > maxStoreNum) {
			vectorDAO.removeOldest(vectorDAO.count() - maxStoreNum);
		}
	}

	@Override
	public void close() {
		vectorDAO.close();
	}

	@Override
	public void sync() {
		vectorDAO.sync();
	}

}
