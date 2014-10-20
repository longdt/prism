package com.ant.crawler.core.content.tag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;


public class ExtracMeataDatabyFourGram implements ExtractMetadata {
	public static final String SQL_CONF = "conf/sqldatabase.properties";
	/**Cau truc du lieu dung de cache tap tu vung load tu db*/
	private CacheData idfData;
	/** counter dung de xac dinh tu nao co tan xuat lon nhat trong 1 bai viet dang duoc phan tich*/
	private int MAX_FREQUENCE = 0;

	public ExtracMeataDatabyFourGram() {
		try {
			idfData = CacheData.getInstance(ApplicationConfig.getDicTable());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, Double> extractMetadata(String title, String body) {
		Map<String, Double> bodyMetadata = getMetadataOfContent(title, title
				+ " " + body);

		return bodyMetadata;
	}

	/**
	 * Trich tag tu title va descreption
	 * @param title noi dung cua title
	 * @param result Tap tag se dc gan cho 1 ban viet
	 * @return
	 */

	private Map<String, Double> getMetadataOfTitle(String title,
			Map<String, Double> result) {
		String lastTerm = "";
		String term = null;
		String lastBiterm = "";
		String biTerm = null;
		String lastTriterm = "";
		String triTerm = null;
		String fourTerm = null;

		Map<String, Double> caculatedData = new HashMap<String, Double>();
		StringTokenizer in = new StringTokenizer(title);
		Map<String, WordMetadata> dictionary = new HashMap<String, WordMetadata>();
		while (in.hasMoreTokens()) {
			boolean flag;
			term = in.nextToken();
			if (term.length() > 15 || term.length() == 0) {
				continue;
			}

			flag = WordUtilities.checkHasDeilimiterInEnd(term);
			term = WordUtilities.refineEndorStartWord(term);

			if (term == null) {
				continue;
			}

			updateBagWord(term, dictionary);
			biTerm = lastTerm + " " + term;
			updateBagWord(biTerm, dictionary);

			triTerm = lastBiterm + " " + term;
			updateBagWord(triTerm, dictionary);

			fourTerm = lastTriterm + " " + term;
			updateBagWord(fourTerm, dictionary);

			if (!flag) {
				lastTerm = term;
				lastBiterm = biTerm;
				lastTriterm = triTerm;
			} else {
				lastTerm = "";
				lastBiterm = "";
				lastTriterm = "";
			}

		}

		for (Entry<String, WordMetadata> entry : dictionary.entrySet()) {
			if (WordUtilities.isTopWord(entry.getKey())) {
				continue;
			}

			double idf;
			if (idfData.getIdfOfWord(entry.getKey()) == null) {
				idf = 0;
			} else {
				idf = 2.7 + idfData.getIdfOfWord(entry.getKey());
			}

			double tf = 0.5d + (double) entry.getValue().getFrequence()
					/ (double) MAX_FREQUENCE;
			double tfidf = tf * idf;
			if (tfidf > ApplicationConfig.getThreshold()) {
				result.put(entry.getKey(), tfidf);
			}
			caculatedData.put(entry.getKey(), tfidf);
		}

	//	System.out.println("CaculateData from title :" + caculatedData);
		MAX_FREQUENCE = 0;
		return caculatedData;
	}

	/**
	 * Trich tag tu noi dung bai viet
	 * @param title noi dung cua tieu de
	 * @param content noi dung cua bai viet
	 * @return Tap cac tag trich dc tu bai viet kem theo trong so cua no
	 */

	private Map<String, Double> getMetadataOfContent(String title,
			String content) {
		String lastTerm = "";
		String term = null;
		String lastBiterm = "";
		String biTerm = null;
		String lastTriterm = "";
		String triTerm = null;
		String fourTerm = null;
		Map<String, Double> result = new HashMap<String, Double>();
		Map<String, Double> caculatedData = new HashMap<String, Double>();
		Map<String, WordMetadata> dictionary = new HashMap<String, WordMetadata>();
		StringTokenizer in = new StringTokenizer(content);
		Map<String, Double> listNoun = new HashMap<String, Double>();
		Map<String, Double> listInQuotes = new HashMap<String, Double>();
		StringBuilder cureentQouter = new StringBuilder();
		StringBuilder noun = new StringBuilder();

		caculatedData.putAll(getMetadataOfTitle(title, result));
		/*System.out.println("tinh toan tu tieu de :");
		for (Entry<String, Double> entry : getMetadataOfTitle(title, result).entrySet()) {
			if (entry.getValue() > 0) {
				System.out.println(entry);
			}
		}*/
		while (in.hasMoreTokens()) {
			boolean flag;
			term = in.nextToken();

			if (upDatateQuotesList(listInQuotes, cureentQouter, term)) {
				cureentQouter = new StringBuilder();
			}

			if (term.length() > 15) {
				continue;
			}

			if (noun.length() == 0) {
				noun.append(term);
				noun.append(" ");
			} else if (WordUtilities.isNoud(term)) {
				noun.append(term);
				noun.append(" ");
			} else {

				String nount = WordUtilities.getNoun(noun);
				if (nount != null && nount.split(" ").length < 6 && !WordUtilities.isTopWord(nount)) {
					listNoun.put(WordUtilities.refineEndorStartWord(nount),
							new Double(0));
				}

				noun = new StringBuilder();
				noun.append(term);
				noun.append(" ");
			}

			flag = WordUtilities.checkHasDeilimiterInEnd(term);
			term = WordUtilities.refineEndorStartWord(term);
			if (term == null) {
				continue;
			}
			updateBagWord(term, dictionary);
			biTerm = lastTerm + " " + term;
			updateBagWord(biTerm, dictionary);

			triTerm = lastBiterm + " " + term;
			updateBagWord(triTerm, dictionary);

			fourTerm = lastTriterm + " " + term;
			updateBagWord(fourTerm, dictionary);

			if (!flag) {
				lastTerm = term;
				lastBiterm = biTerm;
				lastTriterm = triTerm;
			} else {
				boolean flag2 = false;
				if (noun.toString().trim().charAt(
						noun.toString().trim().length() - 1) == ',') {
					flag2 = true;
				}
				String nount = WordUtilities.getNoun(noun);
				if (nount != null && nount.split(" ").length < 6 && !WordUtilities.isTopWord(nount)) {
					listNoun.put(WordUtilities.refineEndorStartWord(nount),
							new Double(0));
				}

				noun = new StringBuilder();
				if (flag2) {
					noun.append("start ");
				}

				lastTerm = "";
				biTerm = "";
				triTerm = "";
			}
		}

		// System.out.println("List danh tu:" + listNoun);

		for (Entry<String, WordMetadata> entry : dictionary.entrySet()) {
			if (WordUtilities.isTopWord(entry.getKey())) {
				continue;
			}

			double idf;
			if (idfData.getIdfOfWord(entry.getKey()) == null) {
				idf = 0;
			} else {
				idf = 5d + idfData.getIdfOfWord(entry.getKey());
			}

			double tf = 0.4d + (double) entry.getValue().getFrequence()
					/ (double) MAX_FREQUENCE;
			double tfidf = tf * idf;
			if (tfidf > ApplicationConfig.getThreshold()) {
				result.put(entry.getKey(), tfidf);
			}
			
			if (listNoun.get(entry.getKey()) != null) {
				listNoun.put(entry.getKey(), tfidf);
			}
			
			if (listInQuotes.get(entry.getKey()) != null) {
				listInQuotes.put(entry.getKey(), tfidf);
			}

			if (caculatedData.get(entry.getKey()) == null) {
				caculatedData.put(entry.getKey(), tfidf);
			} else if (caculatedData.get(entry.getKey()) < tfidf) {
				caculatedData.put(entry.getKey(), tfidf);
			}
		}

	//	System.out.println("CaulateData from Content :" + caculatedData);
		
		WordUtilities.refineResulst(result);
		float newThreshold = WordUtilities.chooseMinResult(result,
				caculatedData, ApplicationConfig.getThreshold());

		WordUtilities.chooseMaxResult(result, caculatedData, newThreshold);

		// update Noun to result
		
	//	System.out.println("List Noun:" + listNoun);
		
		if (listNoun.size() <= ApplicationConfig.getMinResult()) {
			result.putAll(listNoun);
		} else {
			for (Entry<String, Double> entry : listNoun.entrySet()) {

				if ((result.size() <= ApplicationConfig.getMaxResult())) {
					if (entry.getKey().split(" ").length > 2) {
						result.put(entry.getKey(), entry.getValue());
					} else if (entry.getKey().split(" ").length == 2) {
						WordMetadata metadata = dictionary.get(entry.getKey());
						if (metadata != null && metadata.getFrequence() > 1) {
							result.put(entry.getKey(), entry.getValue());
						}
					}
				}

			}
		}
		
		// update quotes to result

		int remain = ApplicationConfig.getMaxResult() - result.size();
		
		for (Entry<String, Double> entry : listInQuotes.entrySet()) {
			if (remain > 0) {
				if (entry.getKey().length() > 1) {
					result.put(entry.getKey(), entry.getValue());
					--remain;
				}
			} else {
				break;
			}
		}

		WordUtilities.refineResulst(result);

		return result;
	}

	/**
	 * Update  quote vao danh sach quote trich duoc tu bai viet
	 * @param listQuotes danh sach chua cac quote trich duoc tu bai viet
	 * @param currentQuotes quote dac duoc khoi tao
	 * @param term tu vung dang dc xet tu bai viet
	 * @return
	 */
	private boolean upDatateQuotesList(Map<String, Double> listQuotes,
			StringBuilder currentQuotes, String term) {
		if (currentQuotes.length() == 0) {

			if (WordUtilities.isEndOfQuotes(term)
					&& WordUtilities.isStartOfQuotes(term)) {
				return false;
			} else if (WordUtilities.isStartOfQuotes(term)) {
				term = WordUtilities.refineEndorStartWord(term);
				currentQuotes.append(term);
				currentQuotes.append(" ");
				return false;
			}

			return false;

		} else {
			if (WordUtilities.checkHasDeilimiterInEnd(term)) {
				term = term.replaceAll("\\.|,|\\?|!||;", "");
			}
			if (WordUtilities.isEndOfQuotes(term)) {
				term = WordUtilities.refineEndorStartWord(term);
				currentQuotes.append(term);
				if (currentQuotes.toString().split(" ").length < 6) {
					listQuotes.put(currentQuotes.toString(), new Double(0));
				}

				return true;
			}

			term = WordUtilities.refineEndorStartWord(term);
			currentQuotes.append(term);
			currentQuotes.append(" ");

			return false;
		}
	}


	/**
	 * Tinh toan metadata cho tu va cum tu trong van ban
	 * @param term 
	 * @param dictionary
	 */

	private void updateBagWord(String term, Map<String, WordMetadata> dictionary) {

		WordMetadata metadata = dictionary.get(term);

		if (metadata == null) {
			metadata = new WordMetadata();
			metadata.increFrequence();
		} else {
			metadata.increFrequence();
		}

		if (metadata.getFrequence() > MAX_FREQUENCE) {
			MAX_FREQUENCE = metadata.getFrequence();
		}

		dictionary.put(term, metadata);
	}

	public static void main(String[] args) {
		System.out.println(ApplicationConfig.getThreshold() - 1.5f);
	}
}
