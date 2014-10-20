package com.ant.crawler.core.content.tag;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Connector {
	private String driver = "com.mysql.jdbc.Driver";
	private String host;
	private int port;
	private String dbName;
	private String user;
	private String password;
	private Connection con;
	private Statement state;

	public Connector() {

	}

	public Connector(Connection con) throws SQLException {
		this.con = con;
		state = con.createStatement();
	}

	public Connector(String host, int port, String dbName, String user,
			String password) {
		this.host = host;
		this.port = port;
		this.dbName = dbName;
		this.user = user;
		this.password = password;
	}

	public Connector(Properties info) {
		host = info.getProperty("host");
		port = Integer.parseInt(info.getProperty("port"));
		dbName = info.getProperty("dbName");
		user = info.getProperty("user");
		password = info.getProperty("password");
	}

	public void connect() throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class.forName(driver).newInstance();
		String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
				+ "?autoReconnect=true&useUnicode=true&characterEncoding=utf8";
		con = DriverManager.getConnection(url, user, password);
		state = con.createStatement();
	}

	public Connector reconnect() throws SQLException {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
				+ "?autoReconnect=true&useUnicode=true&characterEncoding=utf8";
		Connection con = DriverManager.getConnection(url, user, password);
		return new Connector(con);
	}

	public PreparedStatement executeSQL(String sql) throws SQLException {
		return con.prepareStatement(sql);
	}

	public PreparedStatement executeSQL(String sql, int typeScrollSensitive,
			int concurUpdatable) throws SQLException {
		return con.prepareStatement(sql, typeScrollSensitive, concurUpdatable);
	}

	public ResultSet executeQuery(String query) throws SQLException {
		return state.executeQuery(query);
	}

	public int executeUpdate(String sql) throws SQLException {
		return state.executeUpdate(sql);
	}

	public void close() {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {

		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	public static void main(String[] args) {
		
	}

}
