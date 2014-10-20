package com.ant.crawler.core.content.relate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.utils.PrismConstants;

/**
 * This module perform extracting word to make a vocabulary.
 * 
 * @author ThienLong
 * 
 */
public class VocabBuilder {
	private static final String LOG_CONF_FILE = "conf/log4j.xml";
	private static Logger logger = Logger.getLogger(VocabBuilder.class);
	private ExecutorService pool;
	private List<File> dataFiles;
	private Map<String, WordCounter> wordBag;
	/**
	 * the handler class perform extracting word asynchronously
	 * localWordBag
	 * @author thienlong
	 * 
	 */
	private static class ExtractHandler implements Callable<Map<String, WordCounter>> {
		private List<File> files;
		private Map<String, WordCounter> localWordBag;

		public ExtractHandler(List<File> files) {
			this.files = files;
			localWordBag = new HashMap<String, WordCounter>();
		}

		public Map<String, WordCounter> call() {
			String content = null;
			String excludeRegex = PrismConfiguration.getInstance().get(
					PrismConstants.NEWS_REFINE_EXCLUDE_REGEX);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_33);
			for (File f : files) {
				content = getContentFile(f);
				content = refineDoc(content, excludeRegex);
				extractWord(content, analyzer);
			}
			return localWordBag;
		}

		/**
		 * Extracting word from a given content.
		 * 
		 * @param content
		 * @param analyzer
		 */
		public void extractWord(String content, Analyzer analyzer) {
			Set<String> wordsContent = new HashSet<String>();
			TokenStream token = analyzer.tokenStream("content",
					new StringReader(content));
			CharTermAttribute termAttr = token
					.addAttribute(CharTermAttribute.class);
			String word = null;
			WordCounter counter = null;
			try {
				while (token.incrementToken()) {
					word = termAttr.toString();
					if (wordsContent.add(word)) {
						// if this word not in wordContent yet.
						if ((counter = localWordBag.get(word)) != null) {
							counter.increaseCountWords();
							counter.increaseNumDocs();
						} else {
							localWordBag.put(word, new WordCounter());
						}
					} else {
						// if this word already in wordContent.
						counter = localWordBag.get(word);
						counter.increaseCountWords();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * @param f
		 * @return
		 */
		private String getContentFile(File f) {
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = reader.readLine()) != null) {
					builder.append(line).append('\n');
				}
				return builder.toString();
			} catch (IOException e) {
				logger.error("error occur when get content file: " + f, e );
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						logger.error("can't to close file: " + f, e );
					}
				}
			}
			return null;
		}
	}

	public VocabBuilder(String dataDir) throws Exception {
		pool = Executors.newFixedThreadPool(4);
		dataFiles = new ArrayList<File>();
		wordBag = new HashMap<String, WordCounter>();
		File dir = new File(dataDir);
		Queue<File> dirs = new LinkedList<File>();
		dirs.add(dir);
		while (!dirs.isEmpty()) {
			dir = dirs.poll();
			File[] files = dir.listFiles();
			for (File f : files) {
				if (f.isDirectory()) {
					dirs.add(f);
				} else {
					dataFiles.add(f);
				}
			}
		}
	}

	/**
	 * perform the task.
	 * 
	 * @throws SQLException
	 * @throws ExecutionException
	 */
	public void execute() throws SQLException, InterruptedException,
			ExecutionException {
		// if configure to run on all categories.
		int binSize = dataFiles.size() / PrismConstants.THREAD_NUM_DEFAULT;
		int bonus = dataFiles.size() % PrismConstants.THREAD_NUM_DEFAULT;
		int start = 0;
		int end = binSize;
		List<Callable<Map<String, WordCounter>>> tasks = new ArrayList<Callable<Map<String, WordCounter>>>();
		for (int i = 0; i < PrismConstants.THREAD_NUM_DEFAULT; ++i) {
			if (bonus > 0) {
				++end;
				--bonus;
			}
			tasks.add(new ExtractHandler(dataFiles.subList(start, end)));
			start = end;
			end += binSize;
		}
		List<Future<Map<String, WordCounter>>> results = pool.invokeAll(tasks);
		pool.shutdown();
		for (Future<Map<String, WordCounter>> result : results) {
			updateVocabulary(result.get());
		}
		buildVocabulary();
	}

	/**
	 * @param local
	 */
	private void updateVocabulary(Map<String, WordCounter> local) {
		WordCounter totalCounter = null;
		WordCounter localCounter = null;
		for (Entry<String, WordCounter> entry : local.entrySet()) {
			localCounter = entry.getValue();
			if ((totalCounter = wordBag.get(entry.getKey())) != null) {
				totalCounter.increaseCountWords(localCounter.getCountWords());
				totalCounter.increaseNumDocs(localCounter.getNumDocs());
			} else {
				wordBag.put(entry.getKey(), localCounter);
			}
		}
	}

	/**
	 * refine a given document by using regular expression.
	 * 
	 * @param content
	 * @param regex
	 * @return refined document.
	 */
	public static String refineDoc(String content, String regex) {
		return regex != null ? content
				.replaceAll(
						"((<)\\s*?/*((span)|(div)|(br)|(p)|(td)|(tr)|(h1)|(h2)|(h3)|(em)|(a)|(b)|(i)|(strong)|(font)|(ul)|(li)|(img)|(style)|(script)|(objects))\\s*?.*?(>))|(\")+|(\\?|%|#|(&#33;)+|_|@|\\^|&|;|:|,|\\.|&|\\+|-|\\(|\\)|`|~|/)",
						" ")
				: content;
	}

	/**
	 * Build a Vocabulary.
	 * 
	 * @throws SQLException
	 */
	private void buildVocabulary() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(PrismConstants.VOBCAB_FILE)), true);
			WordCounter counter = null;
			int countWords = 0;
			int numDocs = 0;
			float idf = 0;
			int totalNumDocs = dataFiles.size();
			int freMin = PrismConfiguration.getInstance().getInt(PrismConstants.TERM_FREQUENCE_MIN, 0);
			for (Entry<String, WordCounter> entry : wordBag.entrySet()) {
				counter = entry.getValue();
				countWords = counter.getCountWords();
				if (countWords >= freMin) {
					numDocs = counter.getNumDocs();
					idf = (float) Math.log(totalNumDocs / (float) numDocs);
					writer.println(entry.getKey() + "\t" + idf);
				}
			}
		} catch (IOException e) {
			logger.error("cant' write to vobcab file", e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	/**
	 * load a vocabulary in database.
	 * 
	 * @param connector
	 * @param vectorSpace
	 * @param prefixTable
	 * @throws SQLException
	 */
	public static void loadVocab(Map<String, Float> vocab) throws SQLException {
		Scanner reader = null;
		try {
			reader = new Scanner(new BufferedReader(new FileReader(PrismConstants.VOBCAB_FILE)));
			while (reader.hasNext()) {
				vocab.put(reader.next(), reader.nextFloat());
			}
		} catch (IOException e) {
			logger.error("error while loading vocab from file", e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure(LOG_CONF_FILE);
		long timer = System.currentTimeMillis();

		logger.info("["
				+ new Date()
				+ "] Starting the job extracting word to build the vocabulary for application domain.");
		try {
			VocabBuilder extractor = new VocabBuilder(args[0]);
			extractor.execute();
		} catch (Exception e) {
			logger.error("Occur the Error when extracting word", e);
		}
		timer = System.currentTimeMillis() - timer;
		logger.info("finished the job in " + timer + "ms");
	}
}
