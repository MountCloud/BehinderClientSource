package net.rebeyond.behinder.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import net.rebeyond.behinder.entity.Plugin;

public class PluginDao extends BaseDao {
   public List findPluginByType(int type) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Plugin where type = ?");
      statement.setInt(1, type);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Plugin plugin = new Plugin();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(plugin, column_name, rs.getObject(column_name));
         }

         result.add(plugin);
      }

      statement.close();
      return result;
   }

   public List findPluginByScriptType(String scriptType) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Plugin where scriptType like ?");
      statement.setString(1, "%" + scriptType + "%");
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Plugin plugin = new Plugin();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(plugin, column_name, rs.getObject(column_name));
         }

         result.add(plugin);
      }

      statement.close();
      return result;
   }

   public List findAllPlugins() throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Plugin");
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Plugin plugin = new Plugin();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(plugin, column_name, rs.getObject(column_name));
         }

         result.add(plugin);
      }

      statement.close();
      return result;
   }

   public List findPluginByOs(int os) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Plugin where os = ?");
      statement.setInt(1, os);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Plugin plugin = new Plugin();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(plugin, column_name, rs.getObject(column_name));
         }

         result.add(plugin);
      }

      statement.close();
      return result;
   }

   public List findPluginByLanguageAndOs(int language, int os) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Plugin where language=? and os = ?");
      statement.setInt(1, language);
      statement.setInt(2, os);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Plugin plugin = new Plugin();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(plugin, column_name, rs.getObject(column_name));
         }

         result.add(plugin);
      }

      statement.close();
      return result;
   }

   public Plugin findPluginById(int id) throws Exception {
      Plugin plugin = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from Plugin where id = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         plugin = new Plugin();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(plugin, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return plugin;
   }

   public static void main(String[] args) throws Exception {
   }
}
