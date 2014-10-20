package com.ant.crawler.core.content.tag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import com.ant.crawler.core.utils.PrismConstants;


public class WordUtilities {
	public static Set<String> topwords;
	
	static {
		topwords = new HashSet<String>();
		loadToopWord(topwords);
	
	}
	
	public static Map<String, Double> getNounFromSource (Map<String, Double> source, Map<String, Double> result , int numNound) {
		if (result == null) {
			result = new HashMap<String, Double>() ;
		}
		
		for (Entry<String, Double> nuEntry : source.entrySet()) {
			if (nuEntry.getValue() == 0.0d && nuEntry.getKey().split(" ").length <5) {
				if (result.size() == numNound) {
					break;
				}
				result.put(nuEntry.getKey(), 100d);
			}
		}
		
		return result;
	}
	
	public static void reScoreForOneWord (Map<String, Double>  source, float factor) {
		for (Entry<String, Double>  entry : source.entrySet()) {
			if (entry.getKey().split(" ").length == 1) {
				source.put(entry.getKey(), entry.getValue() - factor);
			}
		}
	}
	
	private static void loadToopWord (Set<String > topwords ) {
		try {
			Reader reader = new FileReader(new File(PrismConstants.TOP_WORD));
			Scanner in = new Scanner(reader);
			while (in.hasNext()) {
				String topWord = in.next();
				topwords.add(topWord.replaceAll("_", " "));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean isTopWord (String word) {
		return topwords.contains(word);
	}
	
	public static String refineWord(String word) {
		if (word.length() > 7) {
			return null;
		}

		char c = 0;
		StringBuilder refineWord = new StringBuilder();
		for (int i = 0, n = word.length(); i < n; ++i) {
			c = Character.toLowerCase(word.charAt(i));
			if ((c >= 'a' && c <= 'z') || (c >= '\u00C0' && c <= '\u1EF9')
					|| (c >= '0' && c <= '9')) {
				refineWord.append(c);
			}
		}
		return refineWord.toString();
	}
	
	public static boolean checkHasDeilimiterInEnd (String term) {
		if (term.length() == 0) {
			return false;
		}
		char c = term.charAt(term.length() - 1);
		if ( isSpecialCharacter(c)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isSpecialCharacter (char c) {
		if ( c == '.' || c == ',' || c =='!' || c=='?' || c==':' || c=='"' || c==')' || c==';' || c=='\n' ) {
			return true;
		}
		return false;
	}
	
	public static boolean isStartOfQuotes (String term) {
		char c = term.charAt(0);
		if ( c == '"' || c=='“' || c=='\'' || c=='‘') {
			return true;
		}
		return false ;
	}
	
	public static boolean isEndOfQuotes (String term) {
		if (term.length() == 0) {
			return false;
		}
		char c = term.charAt(term.length() - 1);
		if (c == '"' || c=='”' || c=='\'' || c=='’') {
			return true;
		}
		
		return false;
	}
	
	public static String refineEndorStartWord (String word) {
		
	/*	
		if (word.length() > 15) {
			return null;
		}*/
		
		try {
			boolean flag = false;
			word = word.toLowerCase().trim();
			char start = word.charAt(0);
			char end = word.charAt(word.length() -1);
			if (!((start >= 'a' && start <= 'z') || (start >= '\u00C0' && start <= '\u1EF9')
					|| (start >= '0' && start <= '9') ) ) {
				word = word.substring(1, word.length());
				flag = true;
			}
			
			if (!((end >= 'a' && end <= 'z') || (end >= '\u00C0' && end <= '\u1EF9')
					|| (end >= '0' && end <= '9')) ) {
				word = word.substring(0, word.length()-1);
				flag = true;
			}
			
			if (flag) {
				word = refineEndorStartWord(word);
			}
			
		} catch (StringIndexOutOfBoundsException e) {
			return null;
		}
		
		
		return word;
	}
	
	public static void updateBadWord(String word , Map<String, WordMetadata> bagWord,Set<String> wasCount) {
		
		WordMetadata metadata = bagWord.get(word);
		
		if (metadata == null) {
			metadata = new WordMetadata();
			metadata.setWord(word);
			metadata.increDefrequence();
			metadata.increFrequence();
			wasCount.add(word);
		} else {
			metadata.increFrequence();
			if (wasCount.add(word)) {
				metadata.increDefrequence();
			}
		}
		
		bagWord.put(word, metadata);
	}
	
	public static boolean notInTopWord(String word) {
		return (!topwords.contains(word)) &&  (word != null);
	}
	
    public static float chooseMinResult (Map<String, Double> result, Map<String, Double> caculatedData,float threshold) {
		
		float newThreshold = threshold - ApplicationConfig.getFactor();
		Map<String, Double> tempResult = new HashMap<String, Double>();
		WordUtilities.choseKeywordByThreshold(tempResult, caculatedData, newThreshold);
		if (result.size() < ApplicationConfig.getMinResult() && newThreshold > 0) {
			choseKeywordByThreshold(result, caculatedData, newThreshold);
			refineResulst(result, caculatedData);
			threshold = chooseMinResult(result, caculatedData, newThreshold);
		} else if ( tempResult.size()  > result.size() ) {
			choseKeywordByThreshold(result, caculatedData, newThreshold);
			refineResulst(result, caculatedData);
			threshold = chooseMinResult(result, caculatedData, newThreshold);
		} else {
			return threshold;
		}
		
		return threshold;
	}
    
    
    public static void choseKeywordByThreshold (Map<String, Double> result, Map<String, Double> caculatedData, float threshold) {
    	result.clear();
    	for (Entry<String, Double> entry : caculatedData.entrySet()) {
			if (entry.getValue() > threshold) {
				result.put(entry.getKey(), entry.getValue());
			} 
		}
    }
    
    /**
	 * luoc cac tag bi chua boi cac tag khac
	 * @param result danh sach cac ket qua vua trich duoc tu bai viet
	 */
	public static Set<String> refineResulst(Map<String, Double> result) {
		Set<String> listDelete = new HashSet<String>();
		for (Entry<String, Double> entry : result.entrySet()) {

			String[] splits = entry.getKey().split(" ");
			String lastTerm = "";
			String term = null;
			String lastBiTerm = "";
			String biTerm = null;
			String triTerm = null;
			if (splits.length > 2) {
				for (int i = 0; i < splits.length; ++i) {
					term = splits[i];
					biTerm = (lastTerm + " " + term).trim();
					triTerm = (lastBiTerm + " " + term).trim();

					if (biTerm.split(" ").length > 1
							&& biTerm.split(" ").length < splits.length
							&& result.containsKey(biTerm)) {
						listDelete.add(biTerm);
					}

					if (triTerm.split(" ").length > 1
							&& triTerm.split(" ").length < splits.length
							&& result.containsKey(triTerm)) {
						listDelete.add(triTerm);
					}

					lastTerm = term;
					lastBiTerm = biTerm;
				}
			}
		}

		for (String delete : listDelete) {
			result.remove(delete);
		}
		
		return listDelete;
	}
	
	public static void refineResulst(Map<String, Double> result,Map<String, Double> caculateData ) {
		Set<String> listDelete = refineResulst(result);
		for (String delete : listDelete) {
			caculateData.remove(delete);
		}
	}

	public static void chooseMaxResult (Map<String, Double> result, Map<String, Double> caculatedData,float threshold) {
    	float newThreshold = threshold + ApplicationConfig.getFactor();
    	if (result.size() > ApplicationConfig.getMaxResult()) {
    		
    		result.clear();
    		
    		for (Entry<String, Double> entry : caculatedData.entrySet()) {
				if (entry.getValue() > newThreshold) {
					result.put(entry.getKey(), entry.getValue());
				} 
			}
    		
    		chooseMaxResult(result, caculatedData, newThreshold);
    	} else {
    		return;
    	}
    	
    	
    }
	
    
	public static boolean isNoud(String word) {
		if(Character.isUpperCase( word.charAt(0)) ){
			return true;
		}
		
		return false;
	}
	


	public static String getNoun(StringBuilder noun) {
		String temp = noun.toString().trim();
		String [] splits = temp.split(" ");
		
		if (splits.length < 2) {
			return null;
		}
		
		String lastTerm = splits[splits.length - 1];
		if (isSpecialCharacter(lastTerm.charAt(lastTerm.length() - 1))) {
			lastTerm = lastTerm.replaceAll(ApplicationConfig.regex, "");
			splits[splits.length - 1] = WordUtilities.refineEndorStartWord(lastTerm);
			if (splits.length > 2) {
				String result = "";
				for ( int i = 1 ; i < splits.length; ++ i) {
					splits[i] = WordUtilities.refineEndorStartWord(splits[i]);
					result  = result + " " + splits[i];
				}
				
				return result.trim();
			}
		} else {
			if (splits.length > 2) {
				String result = "";
				for ( int i = 1 ; i < splits.length; ++ i) {
					splits[i] = WordUtilities.refineEndorStartWord(splits[i]);
					result  = result + " " + splits[i];
				}
				
				
				return result.trim();
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(isTopWord("sở tài"));
	}
}
