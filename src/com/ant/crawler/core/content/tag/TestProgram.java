package com.ant.crawler.core.content.tag;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.ant.crawler.dao.imp.SqlConnectionPool;


public class TestProgram {

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
/*		DataLoader dataloader = new LoadDataFromFile();
		List<StringBuilder> data = dataloader.getContenData();
		ExtractMetadata metadataExtractor = new ExtracMeataDatabyFourGram();
		Map<String, Double> result = metadataExtractor.extractMetadata(""," Dầu tiếp tục giảm giá ngày thứ 2 liên tiếp");
		System.out.println(result);
		System.out.println(result.size());
*/
		String sqlF = "select " + "informationID AS id, " + "Title AS title, "
				+ "Abstract AS descr, " + "Content AS content, DateCreate AS time "
				+ "from " + "Information " + "where informationID=1126;" ;
		String sql = String.format(sqlF, "");
		
		Connection conn = SqlConnectionPool.getSqlServerConnection();
		PreparedStatement pre = conn.prepareStatement(sql);
		ResultSet rs = pre.executeQuery();
		rs.next();
		String title = "." + rs.getString("title") + ".";
		String content = rs.getString("content");
		System.out.println(rs.getTime("time"));
		long time = rs.getTime("time").getTime(); 
		SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss.SSS");
		System.out.println(rs.getDate("time").toString() + " " + formater.format( new Date(time)));;
		
		content = content.replaceAll("(?is)(<br>)|(</br>)|(<p>)|(</p>)", ". ");
		//System.out.println(content);
	//	System.out.println(content);
		content = content.replaceAll("(?is)<style>.*</style>|<[^>]+>", "") + ". ";;
		content = content.substring(0, content.length() -50);
		content = content.replace("&amp;","&");
		String desc = rs.getString("descr");
		if (desc != null) {
			desc = "." +desc + ".";
			desc = desc.replaceAll("<[^>]+>", "") + ". ";
			title = title + " " + desc	;
			content = title  + " " + content;
		} else {
			content = title + content	;
		}
		
		
		System.out.println("@title : " + title);

		System.out.println("@conten: " + content);
		//System.out.println(content);
		
		
		ExtractMetadata metadataExtractor = new ExtracMeataDatabyFourGram();
		Map<String, Double> result = metadataExtractor.extractMetadata(title , content);
		
		Map<String, Double> noundWord = metadataExtractor.extractMetadata(title, "");
		
		Map<String, Double> displayNound = WordUtilities.getNounFromSource(noundWord, null, 2);
		if (displayNound.size() < 2) {
			displayNound  = WordUtilities.getNounFromSource(result, displayNound, 2);
		}
		
		result.putAll(displayNound);		

		WordUtilities.reScoreForOneWord(result, 1);
		
		List<Entry<String, Double>> newresutl = Main.sortListKeyWord(result);
		
		for (Entry<String, Double> entry: newresutl) {
			System.out.println(entry);
		}
		
		System.out.println(System.getProperty("file.encoding"));;

		System.out.println(result);
		System.out.println(result.size());
		System.out.println(CacheData.getInstance("").getIdfOfWord("ân ái"));
		System.out.println(CacheData.getInstance("").getIdfOfWord("phòng the"));
		System.out.println(CacheData.getInstance("").getIdfOfWord("mạch máu"));
		System.out.println(CacheData.getInstance("").getIdfOfWord("hàm răng"));
		System.out.println( CacheData.getInstance("").getIdfData().size());
		
		
		
	}
}
