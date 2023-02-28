package net.rebeyond.behinder.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseProbesNative {
   private SocketChannel socketChannel;
   private Socket socket;
   private String action;
   private static Set props = new HashSet();

   static native void sendQuitTo(int var0) throws IOException;

   public static String getFileType(String fileName) {
      int extIndex = fileName.lastIndexOf(".");
      return extIndex >= 0 ? fileName.substring(extIndex + 1).toLowerCase() : "";
   }

   public static void main(String[] args) throws Throwable {
      props.addAll(Arrays.asList("totalwaitms", "tcpwrappedms", "ports", "sslports", "rarity"));
      String lines = new String(Utils.getFileData("d:/tmp/nmap-service-probes"));
      List probeArr = new ArrayList();
      Map probeObj = null;
      List matcheArr = null;
      String[] var5 = lines.split("\n");
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String line = var5[var7];
         Map actionMap = getAction(line);
         String action = (String)actionMap.get("action");
         String service;
         String regex;
         if (action.equals("Probe")) {
            probeObj = new HashMap();
            matcheArr = new ArrayList();
            Pattern pattern = Pattern.compile("Probe (TCP|UDP) ([^\\s]*) q([^\\s])([\\s\\S]*?)(\\3)");
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
               String type = m.group(1);
               service = m.group(2);
               regex = m.group(4);
               probeObj.put("type", type);
               probeObj.put("name", service);
               probeObj.put("challenge", regex);
            }

            probeObj.put("matches", matcheArr);
            probeArr.add(probeObj);
         } else if (!action.equals("match") && !action.equals("softmatch")) {
            if (props.contains(action)) {
               String value = (String)actionMap.get("value");
               probeObj.put(action, value);
            } else if (!action.equals("#")) {
            }
         } else {
            Map matchObj = new HashMap();
            Pattern pattern = Pattern.compile(action + " ([\\S]*?) m([^\\s])([\\s\\S]*?)(\\2)([is]{0,2})([\\s\\S]*)");
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
               service = m.group(1);
               regex = m.group(3);
               String option = m.group(5);
               String version = m.group(6);
               matchObj.put("service", service);
               matchObj.put("regex", regex);
               matchObj.put("option", option);
               matchObj.put("version", version);
               matchObj.put("version", parseVersion(version));
               matcheArr.add(matchObj);
            }
         }
      }

      System.out.println(probeArr.size());
      List filterProbeList = new ArrayList();
      Iterator var20 = probeArr.iterator();

      while(var20.hasNext()) {
         Object probe = var20.next();
         filterProbeList.add(probe);
      }

      FileOutputStream fileOutputStream = new FileOutputStream("d:/tmp/probes.json");
      fileOutputStream.write(filterProbeList.toString().getBytes());
      fileOutputStream.flush();
      fileOutputStream.close();

      try {
         System.out.println("filterProbeList size:" + filterProbeList.size());
         FileOutputStream fos = new FileOutputStream("d:/tmp/probes.ser");
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(filterProbeList);
         oos.close();
         fos.close();
         System.out.printf("Serialized HashMap data is saved in hashmap.ser");
      } catch (IOException var18) {
         var18.printStackTrace();
      }

   }

   private static Map parseVersion(String version) {
      Map result = new HashMap();
      String regex = "([pvihod])/([^/]*)/";
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(version);

      while(matcher.find()) {
         String key = matcher.group(1);
         String value = matcher.group(2);
         result.put(key, value);
      }

      return result;
   }

   private static Map getAction(String line) {
      Map result = new HashMap();
      Pattern pattern = Pattern.compile("^([^\\s]*)[ ]?([\\S\\s]*)");
      Matcher m = pattern.matcher(line);
      if (m.matches()) {
         result.put("action", m.group(1));
         result.put("value", m.group(2));
      } else {
         System.out.println("get action error:" + line);
      }

      return result;
   }

   public String getInnerIp() {
      String ips = "";

      try {
         Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
         InetAddress ip = null;

         while(netInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface)netInterfaces.nextElement();
            Enumeration addresses = netInterface.getInetAddresses();

            while(addresses.hasMoreElements()) {
               ip = (InetAddress)addresses.nextElement();
               if (ip != null && ip instanceof Inet4Address) {
                  ips = ips + ip.getHostAddress() + " ";
               }
            }
         }
      } catch (Exception var6) {
      }

      ips = ips.replace("127.0.0.1", "").trim();
      return ips;
   }

   private void getParentPath(String currentPath) throws Exception {
      Field f = this.getClass().getDeclaredField("normalizedURI");
      f.setAccessible(true);
      Base64.Decoder d = Base64.getDecoder();
      ClassLoader loader = this.getClass().getClassLoader();
      Class Base64 = loader.loadClass("java.util.Base64");
      Method m = Base64.getDeclaredMethod("getDecoder");
      Object Decoder = m.invoke((Object)null);
      System.out.println(Decoder);
   }
}
