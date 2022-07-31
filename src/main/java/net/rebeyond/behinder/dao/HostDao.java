package net.rebeyond.behinder.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import net.rebeyond.behinder.entity.Host;

public class HostDao extends BaseDao {
   public List findHostByShellId(int shellId) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Host where shellId = ?");
      statement.setInt(1, shellId);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Host host = new Host();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(host, column_name, rs.getObject(column_name));
         }

         result.add(host);
      }

      statement.close();
      return result;
   }

   public List searchHostByShellIdAndIpOrComment(int shellId, String keyword) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Host where shellId = ? and ( ip like ? or comment like ?)");
      statement.setInt(1, shellId);
      statement.setString(2, "%" + keyword + "%");
      statement.setString(3, "%" + keyword + "%");
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Host host = new Host();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(host, column_name, rs.getObject(column_name));
         }

         result.add(host);
      }

      statement.close();
      return result;
   }

   public Host findHostByShellIdAndIp(int shellId, String ip) throws Exception {
      Host host = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from Host where shellId = ? and ip = ?");
      statement.setInt(1, shellId);
      statement.setString(2, ip);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         host = new Host();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(host, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return host;
   }

   public Host findHostById(int id) throws Exception {
      Host host = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from Host where id = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         host = new Host();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(host, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return host;
   }

   public Host findHostByIp(String ip) throws Exception {
      Host host = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from Host where ip = ?");
      statement.setString(1, ip);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         host = new Host();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(host, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return host;
   }

   public int deleteHostByIp(String ip) throws Exception {
      String delSQL = "delete from Host where ip= ?";
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setString(1, ip);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   public int deleteHostByShellId(int shellId) throws Exception {
      String delSQL = "delete from Host where shellId= ?";
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setInt(1, shellId);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   public int updateComment(String ip, String comment) throws Exception {
      String delSQL = "update Host set comment=? where ip= ?";
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setString(1, comment);
      statement.setString(2, ip);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }
}
