package com.ant.crawler.core.content.tag;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import com.ant.crawler.core.utils.PrismConstants;

/**
 * Luu tru thong tin cau hinh cua ung dung, cac cau hianh nay duoc load 1 duy nhat khi ung dung 
 * bat dau chay
 * @author gon
 *
 */
public class ApplicationConfig {
	/**tong so resouce ma tool se dung lam du lieu mau*/
	private static float totalResource;
	/**tan xuat xuat hien toi thieu ma 1 tu phai co de duoc chap nhan ghi vao bo tu dien */
	private static int minBigram;
	/**tan xuat xuat hien toi da ma 1 tu phai co de duoc chap nhan ghi vao bo tu dien */
	private static int maxBigram;
	/**Trong so toi thieu de mot tu co the duoc dua ra lam key word*/
	private static int threshold;
	/**so tag toi hieu ma tool can phai dua ra cho moi bai bao*/
	private static int minResult;
	/**so tag toi da ma tool co the dua ra cho 1 bai viet*/
	private static int maxResult;
	/**He so tang giam de lay cac tag cho 1 bai viet*/
	private static float factor;
	
	private static String dicTable ;
	
	public static final String regex = "((<)\\s*?/*((xml)|(span)|(div)|(br)|(p)|(td)|(tr)|(h1)|(h2)|(h3)|(em)|(a)|(b)|(i)|(strong)|(font)|(ul)|(li)|(img)|(style)|(script)|(objects))\\s*?.*?(>))|(\")+|(\\?|%|#|(&gt;)|(&#33;)+|_|@|\\^|&|;|:|,|\\.|&|\\+|-|\\(|\\)|`|~|/)";
	static {
		Properties properties = new Properties();
		try {
			Reader reader = new FileReader(PrismConstants.TAG_CONF);
			properties.load(reader);
			totalResource = Float.parseFloat(properties.getProperty("totalResource","50000"));
			minBigram = Integer.parseInt(properties.getProperty("minBigram","3"));
			maxBigram = Integer.parseInt(properties.getProperty("maxBigram","300"));
			threshold = Integer.parseInt(properties.getProperty("threshold","7"));
			minResult = Integer.parseInt(properties.getProperty("minResult","5"));
			maxResult = Integer.parseInt(properties.getProperty("maxResult","20"));
			factor = Float.parseFloat(properties.getProperty("factor","0.08"));
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	public static String getDicTable () {
		return dicTable;
	}
	
	public static float getFactor() {
		return factor;
	}

	public static int getMaxResult() {
		return maxResult;
	}

	public static int getMinResult() {
		return minResult;
	}

	public static int getThreshold() {
		return threshold;
	}

	public static float getTotalResouce () {
		return totalResource;
	}

	public static int getMinBigram() {
		return minBigram;
	}

	public static int getMaxBigram() {
		return maxBigram;
	}
	
	public static void setMinResult (int minResult) {
		ApplicationConfig.minResult = minResult;
	}
	
	
}
