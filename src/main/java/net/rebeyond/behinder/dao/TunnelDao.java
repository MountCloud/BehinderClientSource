package net.rebeyond.behinder.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import net.rebeyond.behinder.entity.Tunnel;

public class TunnelDao extends BaseDao {
   public List findTunnelByShellId(int shellId) throws Exception {
      List result = new ArrayList();
      PreparedStatement statement = this.connection.prepareStatement("select * from Tunnel where shellId = ?");
      statement.setInt(1, shellId);
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next()) {
         int numColumns = rsmd.getColumnCount();
         Tunnel tunnel = new Tunnel();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(tunnel, column_name, rs.getObject(column_name));
         }

         result.add(tunnel);
      }

      statement.close();
      return result;
   }

   public Tunnel findTunnelByShellIdAndTunnel(int shellId, Tunnel tunnel) throws Exception {
      PreparedStatement statement = this.connection.prepareStatement("select * from Tunnel where shellId= ? and type = ? and targetIp=? and targetPort =? and remoteIp=? and remotePort = ?");
      statement.setInt(1, shellId);
      statement.setInt(2, tunnel.getType());
      statement.setString(3, tunnel.getTargetIp());
      statement.setString(4, tunnel.getTargetPort());
      statement.setString(5, tunnel.getRemoteIp());
      statement.setString(6, tunnel.getRemotePort());
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rs.next()) {
         int numColumns = rsmd.getColumnCount();

         for(int i = 1; i <= numColumns; ++i) {
            String column_name = rsmd.getColumnName(i);
            this.setField(tunnel, column_name, rs.getObject(column_name));
         }
      }

      statement.close();
      return tunnel;
   }

   public int updateStatus(int shellId, Tunnel tunnel) throws Exception {
      String updateSQL = "update Tunnel set status =? where shellId= ? and type = ? and targetIp=? and targetPort =? and remoteIp=? and remotePort = ?";
      PreparedStatement statement = this.connection.prepareStatement(updateSQL);
      statement.setInt(1, tunnel.getStatus());
      statement.setInt(2, shellId);
      statement.setInt(3, tunnel.getType());
      statement.setString(4, tunnel.getTargetIp());
      statement.setString(5, tunnel.getTargetPort());
      statement.setString(6, tunnel.getRemoteIp());
      statement.setString(7, tunnel.getRemotePort());
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   public int delete(int shellId, Tunnel tunnel) throws Exception {
      String updateSQL = "delete from Tunnel where shellId= ? and type = ? and targetIp=? and targetPort =? and remoteIp=? and remotePort = ?";
      PreparedStatement statement = this.connection.prepareStatement(updateSQL);
      statement.setInt(1, shellId);
      statement.setInt(2, tunnel.getType());
      statement.setString(3, tunnel.getTargetIp());
      statement.setString(4, tunnel.getTargetPort());
      statement.setString(5, tunnel.getRemoteIp());
      statement.setString(6, tunnel.getRemotePort());
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   public int deleteByShellId(int shellId) throws Exception {
      String updateSQL = "delete from Tunnel where shellId= ? ";
      PreparedStatement statement = this.connection.prepareStatement(updateSQL);
      statement.setInt(1, shellId);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   public int updateRemotePort(int id, String remotePort) throws Exception {
      String updateSQL = "update Tunnel set remotePort = ? where id = ?";
      PreparedStatement statement = this.connection.prepareStatement(updateSQL);
      statement.setString(1, remotePort);
      statement.setInt(2, id);
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }
}
