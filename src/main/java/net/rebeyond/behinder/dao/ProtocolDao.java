package net.rebeyond.behinder.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import net.rebeyond.behinder.entity.Protocol;

public class ProtocolDao extends BaseDao {
   public ProtocolDao() throws Exception {
   }

   public Protocol getProtocolByName(String name) throws Exception {
      Protocol protocol = null;
      PreparedStatement statement = this.connection.prepareStatement("select * from Procotols where name = ?");
      statement.setString(1, name);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();
         protocol = new Protocol();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(protocol, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return protocol;
   }
}
