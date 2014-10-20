package com.ant.crawler.core.content.tag;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ant.crawler.core.utils.PrismConstants;


public class CacheData {
	public static final String SQL_CONF = "conf/sqldatabase.properties";
	private static CacheData cacheData = null ;
	private Map<String, Double> idfData;
	
	public static CacheData getInstance (String tableName) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (cacheData == null) {
			cacheData = new CacheData(tableName);
		}
		
		return cacheData;
	}
	
	public CacheData(String tableName) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		
		loadIDFdata();	
	}
	
	private void loadIDFdata (Connector con,String tableName) throws SQLException {
		
		String sql = "select * from " + tableName;
		ResultSet re = con.executeQuery(sql);
		idfData = new HashMap<String, Double>();
		while (re.next()) {
			idfData.put(re.getString("term"), re.getDouble("idf"));
		}
	}
	
	private void loadIDFdata () throws IOException {
		idfData = new HashMap<String, Double>();
		BufferedReader reader = new BufferedReader(new FileReader(PrismConstants.VOBCAB_TAG));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String []	splits = line.split("\t");
			if (splits.length == 2) {
				idfData.put(splits[0], Double.parseDouble(splits[1]));
			}
		}
	}
	
	public Map<String, Double> getIdfData () {
		return idfData;
	}
	
	public Double getIdfOfWord (String word) {
		return idfData.get(word);
	}
}
