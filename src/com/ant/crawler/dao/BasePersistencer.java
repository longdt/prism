package com.ant.crawler.dao;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.content.relate.DocSimilar;
import com.ant.crawler.core.content.relate.RelationImpl;
import com.ant.crawler.core.content.relate.RelationTrainer;
import com.ant.crawler.core.content.relate.Relationer;
import com.ant.crawler.core.download.ImageDownloader;
import com.ant.crawler.core.entity.EntityBuilder;
import com.ant.crawler.core.utils.PrismConstants;

public abstract class BasePersistencer implements Persistencer {
	private Relationer relation;
	private String imgSavePath;


	public BasePersistencer() {
		try {
			boolean train = PrismConfiguration.getInstance().getBoolean(
					PrismConstants.NEWS_RELATE_TRAIN, false);
			relation = train ? RelationTrainer.getInstance() : RelationImpl.getInstance();
			imgSavePath = PrismConfiguration.getInstance().get(
					PrismConstants.NEWS_DOWNLOAD_IMAGE_SAVEPATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void store(EntityBuilder entity, String pkField) {
		try {
			long entityID = -1;
			synchronized (this) {
				List<DocSimilar> relateNews = relation.relate(entity);
				if (relateNews != null) {
					entityID = insertEntity(entity.getEntity(), relateNews, pkField);
				}
				if (entityID != -1) {
					relation.storeCurrEntityWithID(entityID);
				}
			}
			if (entityID != -1) {
				downloadImgs(entity.getDownloadImgs());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract long insertEntity(Object entity, List<DocSimilar> relateEntities, String pkName);

	private void downloadImgs(Map<URL, String> downloadImgs) throws InterruptedException {
		for (Entry<URL, String> entry : downloadImgs.entrySet()) {
			ImageDownloader.download(entry.getKey(), imgSavePath + "/" + entry.getValue());
		}
	}

	@Override
	public void close() {
		relation.close();
	}

	@Override
	public void sync() {
		relation.sync();
	}
}
