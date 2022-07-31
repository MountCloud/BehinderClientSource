package net.rebeyond.behinder.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import net.rebeyond.behinder.entity.Service;

public class ServiceDao extends BaseDao {
   public List findServiceByHostId(int hostId) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Service where hostId = ?");
      statement.setInt(1, hostId);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Service service = new Service();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(service, column_name, rs.getObject(column_name));
         }

         result.add(service);
      }

      statement.close();
      return result;
   }

   public List findServiceByShellId(int shellId) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select a.* from Service as a,Host as b where a.hostId =b.id and b.shellId= ?");
      statement.setInt(1, shellId);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Service service = new Service();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(service, column_name, rs.getObject(column_name));
         }

         result.add(service);
      }

      statement.close();
      return result;
   }

   public int deleteServiceByShellId(int shellId) throws Exception {
      String delSQL = "delete from Service where hostId in (select id from Host where shellId = ?)";
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setInt(1, shellId);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   public List searchServiceByShellIdAndNameOrPort(int shellId, String keyword) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select a.* from Service as a,Host as b where a.hostId =b.id and b.shellId= ? and (name like ? or port = ? )");
      statement.setInt(1, shellId);
      statement.setString(2, "%" + keyword + "%");
      int port = 0;

      try {
         port = Integer.parseInt(keyword);
      } catch (Exception var12) {
      }

      statement.setInt(3, port);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Service service = new Service();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(service, column_name, rs.getObject(column_name));
         }

         result.add(service);
      }

      statement.close();
      return result;
   }

   public Service findServiceById(int id) throws Exception {
      Service service = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from Service where id = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         service = new Service();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(service, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return service;
   }
}
