package com.ant.crawler.core.content.tag;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.imp.SqlConnectionPool;

/**
 * 
 * @author gon xay dung tap tu vung cho ung dung, thua ke tu bo tu vung co san
 *         cua he thong, class nay se tinh toan cho tap tu vung trong csdl
 */
public class VocabularyBuilderFromDictionary {
	
	private static final String LOG_CONF_FILE = "conf/log4j.xml";
	private static Logger logger = Logger.getLogger(VocabularyBuilderFromDictionary.class);

	public static final String DELIMITERS = ",. ";
	/* Connector dung de ket noi co so du lieu */
	private Connector connector;
	/*
	 * Luu tru tat ca cac tu vung trong DB kem theo cac du lieu metadata lien
	 * quan den tu do
	 */
	private Map<String, WordMetadata> dictionary;
	/* file chua cau hinh cua tool */
	public static final String SQL_CONF = "conf/sqldatabase.properties";
	/* Giao dien dung de load du lieu, co the la tu file hoac Msql */
	private DataLoader dataLoader;

	public VocabularyBuilderFromDictionary() throws IOException, SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		dataLoader = new LoadDataFromFile();
		dictionary = new HashMap<String, WordMetadata>();
		connector = new Connector(SqlConnectionPool.getConnection());
		loadictionary();
		System.out.println("Finish load dictionary site: " + dictionary.size());

	}

	private void loadictionary() throws SQLException {
		String sqlCompoundwords = "select word from compoundwords";
		String sqlKeyword = "select word from keyword";
		String sqlNamewords = "select word from namewords";
		String sqlNationalzones = "select word from nationalzones";
		String sqlOneword = "select word from oneword";
		String sqlPrefixcorenamewords = "select word from prefixcorenamewords";
		String sqlPrefixwordgroups = "select description from prefixwordgroups";
		String sqlPrefixwords = "select word from prefixwords";
		String sqlCat = "select catname from cat";

		updateDictionFromTable(sqlCompoundwords);
		updateDictionFromTable(sqlKeyword);
		updateDictionFromTable(sqlNamewords);
		updateDictionFromTable(sqlNationalzones);
		updateDictionFromTable(sqlOneword);
		updateDictionFromTable(sqlPrefixcorenamewords);
		updateDictionFromTable(sqlPrefixwordgroups);
		updateDictionFromTable(sqlPrefixwords);
		updateDictionFromTable(sqlCat);
		
	}

	private void updateDictionFromTable(String sql) throws SQLException {
		ResultSet re = connector.executeQuery(sql);
		while (re.next()) {
			String word = re.getString(1);
			if (word != null && word.split(" ").length < 5) {
				word = word.toLowerCase().trim();
				WordMetadata metadata = new WordMetadata();
				dictionary.put(word, metadata);
			}

		}
	}

	public void execute() {
		System.out.println("start load example data !");
		List<StringBuilder> resources = dataLoader.getContenData();
		for (StringBuilder resource : resources) {
			updateResouceToDictionNary(resource.toString());
		}

		System.out.println("size of new dic: " + dictionary.size());

		insertIdfDictionaryToDB();

	}
	
	
	

	private void insertIdfDictionaryToDB() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(PrismConstants.VOBCAB_TAG)));
			for (Entry<String, WordMetadata> entry : dictionary.entrySet()) {
				writer.println(entry.getKey() + "\t" + computerIdf(entry.getValue()));
			}
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}

	}

	public void updateResouceToDictionNary(String resource) {
		String lastTerm = "";
		String term = null;
		String lastBiterm = "";
		String biTerm = null;
		String lastTriterm = "";
		String triTerm = null;
		String fourTerm = null;
		Set<String> wasCount = new HashSet<String>();
		StringTokenizer in = new StringTokenizer(resource, DELIMITERS);

		while (in.hasMoreTokens()) {

			term = in.nextToken();
			term = WordUtilities.refineEndorStartWord(term);
			if (term == null) {
				continue;
			}
			updateBagWord(term, dictionary, wasCount);
			biTerm = lastTerm + " " + term;
			updateBagWord(biTerm, dictionary, wasCount);

			triTerm = lastBiterm + " " + term;
			updateBagWord(triTerm, dictionary, wasCount);

			fourTerm = lastTriterm + " " + term;
			updateBagWord(fourTerm, dictionary, wasCount);

			lastTerm = term;
			lastBiterm = biTerm;
			lastTriterm = triTerm;
		}
	}

	private double computerIdf(WordMetadata metadata) {
		if (metadata.getDeFrequence() != 0) {
			return Math.log((ApplicationConfig.getTotalResouce() / metadata
					.getDeFrequence()) + 1);
		}

		return 5;
	}

	private void updateBagWord(String term,
			Map<String, WordMetadata> dictionary, Set<String> wasCount) {
		WordMetadata metadata = dictionary.get(term);

		if (metadata == null) {
			return;
		} else {
			metadata.increFrequence();
			if (wasCount.add(term)) {
				metadata.increDefrequence();
			}
		}

		dictionary.put(term, metadata);
	}

	/**
	 * @param args
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		DOMConfigurator.configure(LOG_CONF_FILE);
		VocabularyBuilderFromDictionary dictionnaryBuilder = new VocabularyBuilderFromDictionary();
		dictionnaryBuilder.execute();
	}

}
