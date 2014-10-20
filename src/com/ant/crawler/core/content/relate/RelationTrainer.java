package com.ant.crawler.core.content.relate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.utils.PrismConstants;

public class RelationTrainer implements Relationer {
	private static final Logger logger = Logger.getLogger(RelationTrainer.class);
	private static Relationer instance = new RelationTrainer();
	private static final List<DocSimilar> EMPTY = Collections.unmodifiableList(new ArrayList<DocSimilar>());
	private String trainHome = PrismConstants.NEWS_TRAIN_HOME;
	private final int trainNum;
	private int counter;
	/**
	 * @throws IOException
	 * 
	 */
	private RelationTrainer() {
		trainNum = PrismConfiguration.getInstance().getInt(PrismConstants.NEWS_TRAIN_NUM, -1);
	}

	public static Relationer getInstance() {
		return instance;
	}
	
	public synchronized List<DocSimilar> relate(EntityBuilder entity) {
		if (counter == trainNum) {
			return EMPTY;
		}
		PrintWriter writer = null;
		try {
//			writer = new PrintWriter(new BufferedWriter(new FileWriter(trainHome + File.separatorChar + entity.getCreateTime().getTime() + String.valueOf(System.currentTimeMillis()))), true);
//			writer.print(entity.getTextContent());
			++counter;
//		} catch (IOException e) {
//			logger.error("can't write news content to file", e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		return EMPTY;
		
	}

	@Override
	public void close() {
		
	}

	@Override
	public void sync() {
		
	}

	@Override
	public void storeCurrEntityWithID(long entityID) {
		
	}
}
