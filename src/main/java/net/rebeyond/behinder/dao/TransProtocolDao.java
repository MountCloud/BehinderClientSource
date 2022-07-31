package net.rebeyond.behinder.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import net.rebeyond.behinder.entity.TransProtocol;

public class TransProtocolDao extends BaseDao {
   public TransProtocol findTransProtocolById(int id) throws Exception {
      TransProtocol transProtocol = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from TransProtocol where id = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         transProtocol = new TransProtocol();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(transProtocol, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return transProtocol;
   }

   public List findTransProtocols() throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from TransProtocol");
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         TransProtocol transProtocol = new TransProtocol();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(transProtocol, column_name, rs.getObject(column_name));
         }

         result.add(transProtocol);
      }

      statement.close();
      return result;
   }

   public List findTransProtocolsByName(String name) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from TransProtocol where name=?");
      statement.setString(1, name);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         TransProtocol transProtocol = new TransProtocol();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(transProtocol, column_name, rs.getObject(column_name));
         }

         result.add(transProtocol);
      }

      statement.close();
      return result;
   }

   public List findTransProtocolsById(int id) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from TransProtocol where name=(select distinct(name) from TransProtocol where id=?)");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         TransProtocol transProtocol = new TransProtocol();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(transProtocol, column_name, rs.getObject(column_name));
         }

         result.add(transProtocol);
      }

      statement.close();
      return result;
   }

   public TransProtocol findTransProtocolByNameAndType(String name, String type) throws Exception {
      TransProtocol transProtocol = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from TransProtocol where name = ? and type = ?");
      statement.setString(1, name);
      statement.setString(2, type);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         transProtocol = new TransProtocol();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(transProtocol, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return transProtocol;
   }

   public List findTransProtocolByType(String type) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from TransProtocol where type = ?");
      statement.setString(1, type);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         TransProtocol transProtocol = new TransProtocol();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(transProtocol, column_name, rs.getObject(column_name));
         }

         result.add(transProtocol);
      }

      statement.close();
      return result;
   }

   public int updateTransProtocol(String name, String type, String encode, String decode) throws Exception {
      String delSQL = "update TransProtocol set encode=?,decode=? where name= ? and type = ?";
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setString(1, encode);
      statement.setString(2, decode);
      statement.setString(3, name);
      statement.setString(4, type);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   public int deleteByName(String name) throws Exception {
      String delSQL = "delete from TransProtocol where name= ?";
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setString(1, name);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }
}
