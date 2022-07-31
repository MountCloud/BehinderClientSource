package net.rebeyond.behinder.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import net.rebeyond.behinder.entity.Shell;

public class ShellDao extends BaseDao {
   public List findShellByTransProtocolName(String transProtocolName) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select a.* from Shell as a,TransProtocol as b  where a.transProtocolId =b.id and b.name=?");
      statement.setString(1, transProtocolName);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Shell shell = new Shell();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(shell, column_name, rs.getObject(column_name));
         }

         result.add(shell);
      }

      statement.close();
      return result;
   }

   public List findShellByTransProtocolNameAndType(String transProtocolName, String type) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select a.* from shells as a,TransProtocol as b  where a.transProtocolId =b.id and b.name=? and a.type=?");
      statement.setString(1, transProtocolName);
      statement.setString(2, type);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Shell shell = new Shell();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(shell, column_name, rs.getObject(column_name));
         }

         result.add(shell);
      }

      statement.close();
      return result;
   }

   public Shell findShellById(int id) throws Exception {
      Shell shell = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from Shell where id = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         shell = new Shell();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(shell, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return shell;
   }

   public Shell findShellByUrl(String url) throws Exception {
      Shell shell = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from Shell where url = ?");
      statement.setString(1, url);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         shell = new Shell();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(shell, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return shell;
   }
}
