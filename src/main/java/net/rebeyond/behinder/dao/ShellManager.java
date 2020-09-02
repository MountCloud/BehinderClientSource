// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.dao;

import java.sql.Timestamp;
import java.net.InetAddress;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONObject;
import org.json.JSONArray;
import java.sql.DriverManager;
import java.io.File;
import java.net.URLDecoder;
import net.rebeyond.behinder.utils.Utils;
import java.sql.Connection;

public class ShellManager
{
    private static final String Class_Name = "org.sqlite.JDBC";
    private static String DB_PATH;
    private static String DB_URL;
    private static Connection connection;
    
    public ShellManager() throws Exception {
        ShellManager.DB_PATH = URLDecoder.decode(Utils.getSelfPath(), "UTF-8") + File.separator + ShellManager.DB_PATH;
        ShellManager.DB_URL = "jdbc:sqlite:" + ShellManager.DB_PATH;
        if (!new File(ShellManager.DB_PATH).exists()) {
            throw new Exception("\u6570\u636e\u5e93\u6587\u4ef6\u4e22\u5931\uff0c\u65e0\u6cd5\u542f\u52a8\u3002");
        }
        Class.forName("org.sqlite.JDBC");
        (ShellManager.connection = DriverManager.getConnection(ShellManager.DB_URL)).setAutoCommit(true);
    }
    
    public void closeConnection() {
        try {
            if (ShellManager.connection != null && !ShellManager.connection.isClosed()) {
                ShellManager.connection.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public JSONArray listShell() throws Exception {
        final JSONArray result = new JSONArray();
        final Statement statement = ShellManager.connection.createStatement();
        final ResultSet rs = statement.executeQuery("select * from shells");
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return result;
    }
    
    public JSONArray findShellByCatagory(final String catagoryName) throws Exception {
        final JSONArray result = new JSONArray();
        final PreparedStatement statement = ShellManager.connection.prepareStatement("select * from shells where catagory=?");
        statement.setString(1, catagoryName);
        final ResultSet rs = statement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return result;
    }
    
    public JSONArray listCatagory() throws Exception {
        final JSONArray result = new JSONArray();
        final Statement statement = ShellManager.connection.createStatement();
        final ResultSet rs = statement.executeQuery("select * from catagory");
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return result;
    }
    
    public JSONObject findShell(final int shellID) throws Exception {
        final JSONArray result = new JSONArray();
        final PreparedStatement statement = ShellManager.connection.prepareStatement("select * from shells where id=?");
        statement.setInt(1, shellID);
        final ResultSet rs = statement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return (result.length() == 0) ? null : result.getJSONObject(0);
    }
    
    public JSONObject findHostByIP(final int shellID, final String ip) throws Exception {
        final JSONArray result = new JSONArray();
        final PreparedStatement statement = ShellManager.connection.prepareStatement("select * from hosts where shellid=? and ip=?");
        statement.setInt(1, shellID);
        statement.setString(2, ip);
        final ResultSet rs = statement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return (result.length() == 0) ? null : result.getJSONObject(0);
    }
    
    public int addShell(final String url, final String password, final String type, final String catagory, final String comment, final String headers) throws Exception {
        PreparedStatement statement = ShellManager.connection.prepareStatement("select count(*) from shells where url=?");
        statement.setString(1, url);
        final int num = statement.executeQuery().getInt(1);
        if (num > 0) {
            throw new Exception("\u8be5URL\u5df2\u5b58\u5728");
        }
        statement = ShellManager.connection.prepareStatement("insert into shells(url,ip,password,type,catagory,os,comment,headers,addtime,updatetime,accesstime) values (?,?,?,?,?,?,?,?,?,?,?)");
        statement.setString(1, url);
        statement.setString(2, InetAddress.getByName(new URL(url).getHost()).getHostAddress());
        statement.setString(3, password);
        statement.setString(4, type);
        statement.setString(5, catagory);
        statement.setString(6, "");
        statement.setString(7, comment);
        statement.setString(8, headers);
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        statement.setTimestamp(9, now);
        statement.setTimestamp(10, now);
        statement.setTimestamp(11, now);
        return statement.executeUpdate();
    }
    
    public int addCatagory(final String name, final String comment) throws Exception {
        PreparedStatement statement = ShellManager.connection.prepareStatement("select count(*) from catagory where name=?");
        statement.setString(1, name);
        final int num = statement.executeQuery().getInt(1);
        if (num > 0) {
            throw new Exception("\u8be5\u5206\u7c7b\u5df2\u5b58\u5728");
        }
        statement = ShellManager.connection.prepareStatement("insert into catagory(name,comment) values (?,?)");
        statement.setString(1, name);
        return statement.executeUpdate();
    }
    
    public int addHost(final int shellID, final String ip, final String os, final String comment) throws Exception {
        PreparedStatement statement = ShellManager.connection.prepareStatement("select count(*) from hosts where shellid=? and  ip=?");
        statement.setInt(1, shellID);
        statement.setString(2, ip);
        final int num = statement.executeQuery().getInt(1);
        if (num > 0) {
            throw new Exception("\u8be5\u8d44\u4ea7\u5df2\u5b58\u5728");
        }
        statement = ShellManager.connection.prepareStatement("insert into hosts(shellID,ip,os,comment) values (?,?,?,?)");
        statement.setInt(1, shellID);
        statement.setString(2, ip);
        statement.setString(3, os);
        statement.setString(4, comment);
        return statement.executeUpdate();
    }
    
    public int addService(final int hostID, final String port, final String name, final String banner, final String comment) throws Exception {
        PreparedStatement statement = ShellManager.connection.prepareStatement("select count(*) from services where hostid=? and  port=?");
        statement.setInt(1, hostID);
        statement.setString(2, port);
        final int num = statement.executeQuery().getInt(1);
        if (num > 0) {
            throw new Exception("\u8be5\u7aef\u53e3\u5df2\u5b58\u5728");
        }
        statement = ShellManager.connection.prepareStatement("insert into services(hostid,name,port,banner,comment) values (?,?,?,?)");
        statement.setInt(1, hostID);
        statement.setString(2, name);
        statement.setString(3, port);
        statement.setString(4, banner);
        statement.setString(5, comment);
        return statement.executeUpdate();
    }
    
    public int addPlugin(final String name, final String version, final String entryFile, final String scriptType, final String type, final int isGetShell, final String icon, final String author, final String link, final String qrcode, final String comment) throws Exception {
        PreparedStatement statement = ShellManager.connection.prepareStatement("select count(*) from plugins where name=? and scripttype=?");
        statement.setString(1, name);
        statement.setString(2, scriptType);
        final int num = statement.executeQuery().getInt(1);
        if (num > 0) {
            throw new Exception("\u8be5\u63d2\u4ef6\u5df2\u5b58\u5728");
        }
        statement = ShellManager.connection.prepareStatement("insert into plugins(name,version,entryFile,scriptType,type,isGetShell,icon,author,link,qrcode,comment) values (?,?,?,?,?,?,?,?,?,?,?)");
        statement.setString(1, name);
        statement.setString(2, version);
        statement.setString(3, entryFile);
        statement.setString(4, scriptType);
        statement.setString(5, type);
        statement.setInt(6, isGetShell);
        statement.setString(7, icon);
        statement.setString(8, author);
        statement.setString(9, link);
        statement.setString(10, qrcode);
        statement.setString(11, comment);
        return statement.executeUpdate();
    }
    
    public int updateShell(final int shellID, final String url, final String password, final String type, final String catagory, final String comment, final String headers) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("update shells set url=?,ip=?,password=?,type=?,catagory=?,comment=?,headers=?,updatetime=? where id=?");
        statement.setString(1, url);
        statement.setString(2, InetAddress.getByName(new URL(url).getHost()).getHostAddress());
        statement.setString(3, password);
        statement.setString(4, type);
        statement.setString(5, catagory);
        statement.setString(6, comment);
        statement.setString(7, headers);
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        statement.setTimestamp(8, now);
        statement.setInt(9, shellID);
        return statement.executeUpdate();
    }
    
    public int deleteShell(final int shellId) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("delete from shells where id=?");
        statement.setInt(1, shellId);
        return statement.executeUpdate();
    }
    
    public int deleteCatagory(final String cataGoryName) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("delete from catagory where name=?");
        statement.setString(1, cataGoryName);
        return statement.executeUpdate();
    }
    
    public int addPlugin(final String name, final String type, final String code) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("insert into plugins(name,type,code) values (?,?,?)");
        statement.setString(0, name);
        statement.setString(1, type);
        statement.setString(2, code);
        return statement.executeUpdate();
    }
    
    public int addProxy(final String name, final String type, final String ip, final int port, final String username, final String password, final int status) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("insert into proxys(name,type,ip,port,username,password,status) values (?,?,?,?,?,?,?)");
        statement.setString(1, name);
        statement.setString(2, type);
        statement.setString(3, ip);
        statement.setInt(4, port);
        statement.setString(5, username);
        statement.setString(6, password);
        statement.setInt(7, status);
        return statement.executeUpdate();
    }
    
    public int updateProxy(final String name, final String type, final String ip, final String port, final String username, final String password, final int status) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("update proxys set type=?,ip=?,port=?,username=?,password=?,status=? where name=?");
        statement.setString(1, type);
        statement.setString(2, ip);
        statement.setString(3, port);
        statement.setString(4, username);
        statement.setString(5, password);
        statement.setInt(6, status);
        statement.setString(7, name);
        return statement.executeUpdate();
    }
    
    public JSONObject findProxy(final String name) throws Exception {
        final JSONArray result = new JSONArray();
        final PreparedStatement statement = ShellManager.connection.prepareStatement("select * from  proxys  where name=?");
        statement.setString(1, name);
        final ResultSet rs = statement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return (result.length() == 0) ? null : result.getJSONObject(0);
    }
    
    public JSONObject findPluginByName(final String scriptType, final String name) throws Exception {
        final JSONArray result = new JSONArray();
        final PreparedStatement statement = ShellManager.connection.prepareStatement("select * from  plugins  where scripttype=? and name=?");
        statement.setString(1, scriptType);
        statement.setString(2, name);
        final ResultSet rs = statement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return (result.length() == 0) ? null : result.getJSONObject(0);
    }
    
    public int updatePlugin(final int pluginID, final String name, final String type, final String code) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("update plugins set name=?,type=?,code=? where id=?");
        statement.setString(0, name);
        statement.setString(1, type);
        statement.setString(2, code);
        statement.setInt(3, pluginID);
        return statement.executeUpdate();
    }
    
    public int delPlugin(final int pluginID) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("delete from plugins where id=?");
        statement.setInt(1, pluginID);
        return statement.executeUpdate();
    }
    
    public int delHost(final int hostID) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("delete from hosts where id=?");
        statement.setInt(1, hostID);
        return statement.executeUpdate();
    }
    
    public int delService(final int serviceID) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("delete from service where id=?");
        statement.setInt(1, serviceID);
        return statement.executeUpdate();
    }
    
    public JSONArray listPlugin(final String scriptType) throws Exception {
        final JSONArray result = new JSONArray();
        final PreparedStatement statement = ShellManager.connection.prepareStatement("select * from plugins where scripttype=? or scripttype='all'");
        statement.setString(1, scriptType);
        final ResultSet rs = statement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return result;
    }
    
    public JSONArray listPlugin() throws Exception {
        final JSONArray result = new JSONArray();
        final Statement statement = ShellManager.connection.createStatement();
        final ResultSet rs = statement.executeQuery("select * from plugins");
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return result;
    }
    
    public JSONArray listHost(final int shellID) throws Exception {
        final JSONArray result = new JSONArray();
        final PreparedStatement statement = ShellManager.connection.prepareStatement("select * from hosts where shellid=?");
        statement.setInt(1, shellID);
        final ResultSet rs = statement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return result;
    }
    
    public JSONArray listService(final int hostID) throws Exception {
        final JSONArray result = new JSONArray();
        final PreparedStatement statement = ShellManager.connection.prepareStatement("select * from service where hostid=?");
        statement.setInt(1, hostID);
        final ResultSet rs = statement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            final int numColumns = rsmd.getColumnCount();
            final JSONObject obj = new JSONObject();
            for (int i = 1; i <= numColumns; ++i) {
                final String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            result.put((Object)obj);
        }
        return result;
    }
    
    public int updateOsInfo(final int shellID, final String osInfo) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("update shells set os=? where id=?");
        statement.setString(1, osInfo);
        statement.setInt(2, shellID);
        return statement.executeUpdate();
    }
    
    public int updateMemo(final int shellID, final String memo) throws Exception {
        final PreparedStatement statement = ShellManager.connection.prepareStatement("update shells set memo=? where id=?");
        statement.setString(1, memo);
        statement.setInt(2, shellID);
        return statement.executeUpdate();
    }
    
    static {
        ShellManager.DB_PATH = "data.db";
        ShellManager.DB_URL = "jdbc:sqlite:" + ShellManager.DB_PATH;
        ShellManager.connection = null;
    }
}
