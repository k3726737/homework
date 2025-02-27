package com.wits.clazz.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.lang.ObjectUtils;

public class DBUtils {

	/**
	 * 取得連線
	 * 
	 * @return
	 * @throws Exception
	 */
	public Connection getConnectionPool(String jdbcName) throws Exception {
		Connection connection = null;
		try {
			DataSource ds = null;
			Context ctx = new InitialContext();

			if (ctx == null) {
				throw new Exception("無法取得伺服器環境JNDI.");
			}

			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + jdbcName.toLowerCase());
			if (ds == null) {
				throw new Exception("無法取得伺服器的連線儲存池資源.");
			}
			connection = ds.getConnection();
			System.out.println("從連線儲存池取得:" + connection); // for test
			return connection;
		} catch (Exception ex) {
			String driver = "com.mysql.cj.jdbc.Driver";
			String dbUrl = "jdbc:mysql://localhost:3306/world";
			String userId = "root";
			String passWord = "r0192901929";
			try {
				Class.forName(driver);// 1.載入JDBC Driver
				try {
					connection = DriverManager.getConnection(dbUrl, userId, passWord);// 2.建立連線
					System.out.println("取得一班連線物件:" + connection.getClass().getName()); // for test
					return connection;
				} catch (SQLException e) {
					throw new Exception("建立連線失敗", e);
				}
			} catch (ClassNotFoundException e) {
				throw new Exception("載入JDBC Driver失敗:" + driver);
			}
		}
	}
	
	/**
	 * 取單一DB連線
	 * @param dbKind
	 * @param dbParams
	 * @return
	 * @throws Exception
	 */
	public Connection getConnectionDirect(String dbKind, Map<String, String> dbParams) throws Exception{
		Connection conn = null;
		try {
			// 1.載入 JDBC 驅動程式
			Class.forName(dbParams.get(dbKind + "_DRIVER"));

			// 2.提供JDBC URL
			String dbUrl = dbParams.get(dbKind + "_URL");
			String userId = dbParams.get(dbKind + "_USER");
			String passWord = dbParams.get(dbKind + "_PASS");

			// 3.取得Connection
			conn = DriverManager.getConnection(dbUrl, userId, passWord);

			if (!conn.isClosed()) {
				System.out.println("資料庫連線成功");
			}

		} catch (ClassNotFoundException e) {
			System.out.println("找不到驅動程式類別");

		} catch (SQLException se) {
			System.out.println(se.getMessage());

		}
		return conn;
	}

	/**
	 * 關閉連線
	 * 
	 * @param con
	 */
	public void closeConnection(Connection con) {
		if (null != con) {
			try {
				con.close();
			} catch (SQLException e) {
				System.out.println("關閉連線發生錯誤,, e:" + e.getMessage());
			}
		}

	}

	/**
	 * 使用 Statement 查詢資料
	 * 
	 * @param queryString
	 * @param con
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> queryDataByStatement(String queryString, Connection con) throws Exception {

		if (null == con) {
			throw new Exception("連線為空，請先取得連線後再操作..!");
		}

		Statement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> rtnList = new ArrayList<Map<String, String>>();

		try {

			stmt = con.createStatement();
			rs = stmt.executeQuery(queryString);

			ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next()) {
				Map<String, String> dataMap = new HashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					Object obj = rs.getObject(i);
					dataMap.put(rsmd.getColumnName(i), ObjectUtils.toString(obj, ""));
				}
				rtnList.add(dataMap);

			}
		} catch (Exception e) {
			throw new Exception("查詢資料發生異常, e:" + e.getMessage());

		} finally {

			if (null != rs) {
				rs.close();
			}

			if (stmt != null) {
				stmt.close();
			}

		}
		return rtnList;
	}

	/**
	 * 使用 PreparedStatement 查詢
	 * 
	 * @param queryString
	 * @param paramMap
	 * @param con
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> queryDataByPreparedStatement(String queryString,
			Map<String, String> paramMap, Connection con) throws Exception {

		if (null == con) {
			throw new Exception("連線為空，請先取得連線後再操作..!");
		}

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String, String>> rtnList = new ArrayList<Map<String, String>>();

		try {

			pstmt = con.prepareStatement(queryString);
			rs = pstmt.executeQuery(queryString);

			ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next()) {
				Map<String, String> dataMap = new HashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					Object obj = rs.getObject(i);
					dataMap.put(rsmd.getColumnName(i), ObjectUtils.toString(obj, ""));
				}
				rtnList.add(dataMap);

			}
		} catch (Exception e) {
			throw new Exception("查詢資料發生異常, e:" + e.getMessage());

		} finally {

			if (null != rs) {
				rs.close();
			}

			if (pstmt != null) {
				pstmt.close();
			}

		}
		return rtnList;
	}

}
