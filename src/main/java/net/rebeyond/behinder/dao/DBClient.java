package net.rebeyond.behinder.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBClient {
   private static final String Class_Name = "org.sqlite.JDBC";
   private static String DB_PATH = "data.db";
   private static String DB_URL;
   private static Connection connection;

   public static Connection getConnection() throws Exception {
      if (connection == null) {
         DB_PATH = "./" + DB_PATH;
         DB_URL = "jdbc:sqlite:" + DB_PATH;
         if (!(new File(DB_PATH)).exists()) {
            throw new Exception("数据库文件丢失，无法启动。");
         } else {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            return connection;
         }
      } else {
         return connection;
      }
   }

   static {
      DB_URL = "jdbc:sqlite:" + DB_PATH;
      connection = null;
   }
}
