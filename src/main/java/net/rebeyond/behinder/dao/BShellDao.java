package net.rebeyond.behinder.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import net.rebeyond.behinder.entity.BShell;

public class BShellDao extends BaseDao {
   public List findBShellByShellId(int shellId) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from BShell where hostId in (select id from Host where shellId=?)");
      statement.setInt(1, shellId);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         BShell bShell = new BShell();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(bShell, column_name, rs.getObject(column_name));
         }

         result.add(bShell);
      }

      statement.close();
      return result;
   }

   public int deleteById(int bShellId) throws Exception {
      String delSQL = "delete from BShell where id= ?";
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setInt(1, bShellId);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }
}
