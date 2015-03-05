package com.ant.crawler.core.content.relate.global;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.utils.PrismConstants;

/**
 * This module perform convert documents to vectors.
 * 
 * @author iCream
 * 
 */
public class VectorNormalizor {
	private static final String LOG_CONF_FILE = "conf/log4j.xml";
	private static Logger logger = Logger.getLogger(VectorNormalizor.class);
	private static final VectorDoc EMPTY_VECTOR = new VectorDoc();
	private Map<String, Float> vocab;
	private String excludeRegex;
	private Analyzer analyzer;


	private class NormalizeHandler implements Runnable {
		private ResultSet rs;
		private VectorDocDAO vectorDAO;

		public NormalizeHandler(ResultSet rs, VectorDocDAO vectorDAO) {
			this.rs = rs;
			this.vectorDAO = vectorDAO;
		}

		public void run() {
			long docid = 0;
			String content = null;
			VectorDoc vector = null;
			try {
				do {
					synchronized (rs) {
						if (rs.next()) {
							content = rs.getString("content");
							docid = rs.getLong("docid");
						} else {
							break;
						}
					}
					vector = normalize(content);
					// if vertor is empty
					if (vector.equals(EMPTY_VECTOR)) {
						continue;
					}
					vector.setDocid(docid);
					// store in database.
					vectorDAO.storeVectorDoc(vector);
				} while (true);
			} catch (SQLException e) {
				logger.error("Error when normalize document!!!", e);
			}
		}
	}

	/**
	 * @throws Exception
	 * 
	 */
	public VectorNormalizor() throws Exception {
		this(new StandardAnalyzer(Version.LUCENE_33));
	}

	public VectorNormalizor(Analyzer analyzer) throws Exception {
		excludeRegex = PrismConfiguration.getInstance().get(
				PrismConstants.CONTENT_REFINE_EXCLUDE_REGEX);
		this.analyzer = analyzer;
		vocab = new HashMap<String, Float>();
		VocabBuilder.loadVocab(vocab);
	}

	/**
	 * normalize a given content to boolean vector and weight vector.
	 * 
	 * @param content
	 * @param bagWord
	 * @param vector
	 * @param vectorWeight
	 * @return the size of weight vector.
	 */
	public VectorDoc normalize(String content) {
		Map<String, Float> vectorW = new HashMap<String, Float>();
		BitSet vectorB = new BitSet(vocab.size());
		int tfTotal = parseWord(content, vectorW);
		float size = 0f;
		int counter = 0;

		// compute tf * idf
		Iterator<Entry<String, Float>> iter = vectorW.entrySet().iterator();
		Entry<String, Float> entry = null;
		while (iter.hasNext()) {
			entry = iter.next();
			String word = entry.getKey();
			if (vocab.containsKey(word)) {
				float weight = entry.getValue() * vocab.get(word) / tfTotal;
				size += weight * weight;
				entry.setValue(weight);
				vectorB.set(counter);
			} else {
				iter.remove();
			}
			++counter;
		}

		// if vector is empty.
		if (vectorB.isEmpty()) {
			return EMPTY_VECTOR;
		}

		// normalize vectorWeight
		float normalSize = (float) Math.sqrt(size);
		iter = vectorW.entrySet().iterator();

		while (iter.hasNext()) {
			entry = iter.next();
			Float value = entry.getValue();
			entry.setValue(value / normalSize);
		}

		return new VectorDoc(vectorB, vectorW);
	}


	/**
	 * normalize a given contents list to boolean vector and weight vector.
	 * @param contents
	 * @return
	 */
	public VectorDoc normalize(List<String> contents) {
		Map<String, Float> vectorW = new HashMap<String, Float>();
		BitSet vectorB = new BitSet(vocab.size());
		int tfTotal = 0;
		for (String content : contents) {
			tfTotal += parseWord(content, vectorW);
		}
		float size = 0f;
		int counter = 0;

		// compute tf * idf
		Iterator<Entry<String, Float>> iter = vectorW.entrySet().iterator();
		Entry<String, Float> entry = null;
		while (iter.hasNext()) {
			entry = iter.next();
			String word = entry.getKey();
			if (vocab.containsKey(word)) {
				float weight = entry.getValue() * vocab.get(word) / tfTotal;
				size += weight * weight;
				entry.setValue(weight);
				vectorB.set(counter);
			} else {
				iter.remove();
			}
			++counter;
		}

		// if vector is empty.
		if (vectorB.isEmpty()) {
			return EMPTY_VECTOR;
		}

		// normalize vectorWeight
		float normalSize = (float) Math.sqrt(size);
		iter = vectorW.entrySet().iterator();

		while (iter.hasNext()) {
			entry = iter.next();
			Float value = entry.getValue();
			entry.setValue(value / normalSize);
		}

		return new VectorDoc(vectorB, vectorW);
	}

	/**
	 * parse word from a given content then associate with a given map.
	 * 
	 * @param content
	 * @param wordDocs
	 * @return tfTotal of given content.
	 */
	public int parseWord(String content, Map<String, Float> wordDocs) {
		content = VocabBuilder.refineDoc(content, excludeRegex);
		TokenStream token = analyzer.tokenStream("content", new StringReader(
				content));
		CharTermAttribute termAttr = token.addAttribute(CharTermAttribute.class);
		String word = null;
		int tfTotal = 0;
		float tf = 0;
		try {
			while (token.incrementToken()) {
				word =termAttr.toString();
				++tfTotal;
				// if this word not in wordContent yet.
				if (wordDocs.containsKey(word)) {
					tf = wordDocs.get(word) + 1;
					wordDocs.put(word, tf);
				} else {
					wordDocs.put(word, 1f);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tfTotal;
	}

	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		DOMConfigurator.configure(LOG_CONF_FILE);
		long timer = System.currentTimeMillis();

		logger.info("[" + new Date()
				+ "] Starting the job normalize documents.");
		Configuration conf = PrismConfiguration.getInstance();
		String dbUrl = conf.get(PrismConstants.SQL_LOGIN_URL);
		Connection conn = null;
		try {
			ExecutorService pool = Executors
					.newFixedThreadPool(PrismConstants.THREAD_NUM_DEFAULT);
			conn = DriverManager.getConnection(dbUrl);
			Statement stmt = conn.createStatement();
			String queryRecord = conf.get(PrismConstants.SQL_QUERY);
			ResultSet rs = stmt.executeQuery(queryRecord);
			VectorNormalizor normalizor = new VectorNormalizor();
			VectorDocDAO vectorDAO = new VectorDocDAOImpl();
			for (int i = 0; i < PrismConstants.THREAD_NUM_DEFAULT; ++i) {
				pool.execute(normalizor.new NormalizeHandler(rs, vectorDAO));
			}
			pool.shutdown();
			while (!pool.awaitTermination(1, TimeUnit.HOURS)) {
				System.out
						.println("Please wait for normailizing vector. It take so long time!");
			}
			vectorDAO.close();
		} catch (Exception e) {
			logger.error("Occur the Error when normalize documents to vectors",
					e);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		timer = System.currentTimeMillis() - timer;
		logger.info("finished the job in " + timer + "ms");
	}

}
