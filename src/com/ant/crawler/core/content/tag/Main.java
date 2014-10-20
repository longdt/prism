package com.ant.crawler.core.content.tag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Map.Entry;

import com.ant.crawler.dao.imp.SqlConnectionPool;



public class Main {

	public static final String LASTID_FILE = "data/lastid.data";
	/*public static final String GET_UPDATE_RECORD_SQL = "SELECT news_id AS id ,news_title AS title, news_InitialContent AS descr, news_content AS content FROM news "
			+ "WHERE news_id > %s " + "ORDER BY news_id";*/
	/*public static final String GET_UPDATE_RECORD_SQL = "SELECT top 100 news_id AS id ,news_title AS title, news_InitialContent AS descr, news_content AS content,News_CreateDate AS time FROM news "
		+ "WHERE News_CreateDate > '%s' " + "ORDER BY News_CreateDate";*/
	
	public static final String GET_UPDATE_RECORD_SQL = "SELECT top 100 informationID AS id ,Title AS title, Abstract AS descr, Content AS content, DateCreate AS time FROM Information "
		+ "WHERE DateCreate > '%s' " + "ORDER BY DateCreate";
	
	public static final String UPDATE_TAG_SQL = "IF EXISTS "
			+ "(SELECT TagName FROM Tag WHERE TagName=?) "
			+ "SELECT TagId FROM Tag WHERE TagName=? "
			+ "ELSE " + "INSERT INTO Tag (TagName) VALUES (?) ";

	public static final String BOUND_TAGID_CATID_SQL = "IF NOT EXISTS "
			+ "(SELECT InformationID,TagId FROM TagInfor WHERE InformationID=? AND TagId=?) "
			+ "INSERT INTO TagInfor (InformationID, TagId) VALUES (?,?)";

	/**
	 * @param args
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException, InterruptedException {

		System.out.println("connecting to SQLServer...");
		Connection conn = SqlConnectionPool.getSqlServerConnection();
		System.out.println("Conneted to SQLServer..");

		PreparedStatement updateTagToTable = conn.prepareStatement(UPDATE_TAG_SQL);
		PreparedStatement boundTagToCatid = conn.prepareStatement(BOUND_TAGID_CATID_SQL);
		
		SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss.SSS");
		

		do {
			
			try {
				System.out.println("Load lastId from file..");
				String lastId = getLastIdFromFile(LASTID_FILE);
				System.out.println("LastId :" + lastId);
				if (lastId == null) {
					System.err.println("LastId is NULL..");
					System.err.println("Program exting..");
					System.exit(-1);
				}

				String sql1 = String.format(GET_UPDATE_RECORD_SQL, lastId);
				//sql1 = "SELECT news_id AS id ,news_title AS title, news_InitialContent AS descr, news_content AS content FROM news where news_id=20110616120522485";
				ResultSet articles = conn.prepareStatement(sql1).executeQuery();

				while (articles.next()) {
					long id = articles.getLong("id");
					//String time = "" + articles.getDate("time").getTime();
					
					String time = articles.getDate("time").toString() + " " + formater.format(new Date(articles.getTime("time").getTime())) ;
					String title = articles.getString("title");
					if (title == null) {
						title = ""	;
					}
					
					title = "." + title + ".";
					
					String descr = articles.getString("descr");
					String content = articles.getString("content");
					if (content == null) {
						content = "";
					}
					
					content.replaceAll(	"(?is)(<br>)|(</br>)|(<p>)|(</p>)", ". ");
					content = content.replaceAll("(?is)<style>.*</style>|<[^>]+>",
							"")
							+ ".";
					content = content.replace("&amp;","&");
					if ( content.length() < 50) {
						content = content.substring(0, content.length()/2);
					} else {
						content = content.substring(0, content.length() - 50);
					}
					
					if (descr != null) {
						descr = "." + descr + ".";
						title = title + " " + descr;
						content = title + " " + content;
					}

					ExtractMetadata extractor = new ExtracMeataDatabyFourGram();
					
					
					Map<String, Double> wordMeatadata = extractor.extractMetadata(
							title, content);
					
					Map<String, Double> noundWord = extractor.extractMetadata(title, "");
					
					Map<String, Double> displayNound = WordUtilities.getNounFromSource(noundWord, null, 2);
					if (displayNound.size() < 2) {
						displayNound  = WordUtilities.getNounFromSource(wordMeatadata, displayNound, 2);
					}
					
					wordMeatadata.putAll(displayNound);		

					WordUtilities.reScoreForOneWord(wordMeatadata, 1);

					
					System.out.println("@News_ID : " + id);
					
					List<Entry<String, Double>> listKeyword = sortListKeyWord(wordMeatadata);

					for (Entry<String, Double> keyword : listKeyword) {
						System.out.println(keyword);
						int tagId = updateKeywordToTagtable(keyword.getKey(),
								updateTagToTable);
						buondWeywordToCatId(id, tagId, boundTagToCatid);
					}
					
					System.out.println("@SIZE:" + listKeyword.size());
					System.out.println("@Tag: " + listKeyword);
					updateLastId(LASTID_FILE,time);

				}
				
				Thread.sleep(10 * 1000);

			} catch (SQLException e) {
				e.printStackTrace();
				if (updateTagToTable != null) {
					updateTagToTable.close();
				}
				
				if (boundTagToCatid != null) {
					boundTagToCatid.close();
				}
				
				if (conn != null) {
					conn.close();
				}
				
				conn = SqlConnectionPool.getSqlServerConnection();

				updateTagToTable = conn.prepareStatement (UPDATE_TAG_SQL);
				boundTagToCatid = conn.prepareStatement(BOUND_TAGID_CATID_SQL);
			}
			
		} while (true);

	}
	
	


	public static void updateLastId(String lastidFile, String lastId) throws IOException {
		PrintWriter writer = new PrintWriter(new File(lastidFile));
		writer.write(lastId);
		writer.close();
	}




	public static List<Entry<String, Double>> sortListKeyWord(
			Map<String, Double> wordMeatadata) {
		List<Entry<String, Double>> result = new ArrayList<Entry<String, Double>>(wordMeatadata.entrySet());
		Collections.sort(result,new KeywordComparetor());
		return result;
	}
	
	



	private static void buondWeywordToCatId(long id, int tagId,
			PreparedStatement boundTagToCatid) throws SQLException {
		boundTagToCatid.setLong(1, id);
		boundTagToCatid.setInt(2, tagId);
		boundTagToCatid.setLong(3, id);
		boundTagToCatid.setInt(4, tagId);
		boundTagToCatid.executeUpdate();
	}

	public static int updateKeywordToTagtable(String keyword,
			PreparedStatement updateTagToTable) throws SQLException {
		updateTagToTable.setString(1, keyword);
		updateTagToTable.setString(2, keyword);
		updateTagToTable.setString(3, keyword);
		updateTagToTable.execute();
		updateTagToTable.setString(1, keyword);
		updateTagToTable.setString(2, keyword);
		updateTagToTable.setString(3, keyword);
		ResultSet re = updateTagToTable.executeQuery();
		re.next();
		return re.getInt("TagId");
	}

	public static String getLastIdFromFile(String lastidFile)
			throws FileNotFoundException {
		Scanner in = new Scanner(new FileReader(LASTID_FILE));
		String lastId = in.nextLine();
		in.close();
		return lastId;
	}

}
