package net.rebeyond.behinder.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ParseProbes {
   private SocketChannel socketChannel;
   private Socket socket;
   private String action;
   private static Set props = new HashSet();

   private void test() throws Exception {
      String a = "{\"x\":\"\\x41\"}";
      System.out.println(a);
      new JSONObject(a);
      String bb = "servity:%s";
      String.format(bb, "333");
      System.out.println(bb);
      DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      new File("/tmp/b.txt");
      Class PathsCls = Class.forName("java.nio.file.Paths");
      Class BasicFileAttributeViewCls = Class.forName("java.nio.file.attribute.BasicFileAttributeView");
      Class FileTimeCls = Class.forName("java.nio.file.attribute.FileTime");
      Method getFileAttributeView = Class.forName("java.nio.file.Files").getMethod("getFileAttributeView", Path.class, Class.class, LinkOption[].class);
      Object attributes = getFileAttributeView.invoke(Class.forName("java.nio.file.Files"), PathsCls.getMethod("get", String.class, String[].class).invoke(PathsCls.getClass(), "/tmp/b.txt", new String[0]), BasicFileAttributeViewCls, new LinkOption[0]);
      Object createTime = FileTimeCls.getMethod("fromMillis", Long.TYPE).invoke(FileTimeCls, df.parse("2021/01/12 20:57:51").getTime());
      Object modifyTime = FileTimeCls.getMethod("fromMillis", Long.TYPE).invoke(FileTimeCls, df.parse("2021/01/12 20:57:54").getTime());
      Object accessTime = FileTimeCls.getMethod("fromMillis", Long.TYPE).invoke(FileTimeCls, df.parse("2021/01/12 20:57:57").getTime());
      BasicFileAttributeViewCls.getMethod("setTimes", FileTimeCls, FileTimeCls, FileTimeCls).invoke(attributes, modifyTime, accessTime, createTime);
   }

   static native void sendQuitTo(int var0) throws IOException;

   public static String getFileType(String fileName) {
      int extIndex = fileName.lastIndexOf(".");
      return extIndex >= 0 ? fileName.substring(extIndex + 1).toLowerCase() : "";
   }

   private static String getCurrentPID() {
      String name = ManagementFactory.getRuntimeMXBean().getName();
      String pid = name.split("@")[0];
      return pid;
   }

   private static void modifyJar(String pathToJAR, String pathToClassInsideJAR, byte[] classBytes) throws IOException, CannotCompileException, NotFoundException {
      String classFileName = pathToClassInsideJAR.replace("\\", "/").substring(0, pathToClassInsideJAR.lastIndexOf(47));
      FileOutputStream fos = new FileOutputStream(classFileName, false);
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

   public static void updateJarFile(File srcJarFile, String targetFilePath, boolean update, byte[] classBytes) throws IOException {
      File tmpJarFile = File.createTempFile("tempJar", ".tmp");
      JarFile jarFile = new JarFile(srcJarFile);
      boolean jarUpdated = false;
      List fileNames = new ArrayList();

      try {
         JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tmpJarFile));

         try {
            JarEntry entryx = new JarEntry(targetFilePath);
            fileNames.add(entryx.getName());
            tempJarOutputStream.putNextEntry(entryx);
            tempJarOutputStream.write(classBytes);
            Enumeration jarEntries = jarFile.entries();

            while(true) {
               while(jarEntries.hasMoreElements()) {
                  JarEntry entry = (JarEntry)jarEntries.nextElement();
                  String[] fileNameArray = (String[])((String[])fileNames.toArray(new String[0]));
                  Arrays.sort(fileNameArray);
                  if (Arrays.binarySearch(fileNameArray, entry.getName()) < 0) {
                     InputStream entryInputStream = jarFile.getInputStream(entry);
                     tempJarOutputStream.putNextEntry(entry);
                     byte[] buffer = new byte[1024];
                     int bytesRead = 0;

                     while((bytesRead = entryInputStream.read(buffer)) != -1) {
                        tempJarOutputStream.write(buffer, 0, bytesRead);
                     }
                  } else if (!update) {
                     throw new IOException("Jar Update Aborted: Entry " + entry.getName() + " could not be added to the jar file because it already exists and the update parameter was false");
                  }
               }

               jarUpdated = true;
               break;
            }
         } catch (Exception var24) {
            System.err.println("Unable to update jar file");
            tempJarOutputStream.putNextEntry(new JarEntry("stub"));
         } finally {
            tempJarOutputStream.close();
         }
      } finally {
         jarFile.close();
         if (!jarUpdated) {
            tmpJarFile.delete();
         }

      }

      if (jarUpdated) {
         srcJarFile.delete();
         tmpJarFile.renameTo(srcJarFile);
      }

   }

   public static void main(String[] args) throws Throwable {
      props.addAll(Arrays.asList("totalwaitms", "tcpwrappedms", "ports", "sslports", "rarity"));
      String lines = new String(Utils.getFileData("d:/tmp/nmap-service-probes"));
      JSONArray probeArr = new JSONArray();
      JSONObject probeObj = null;
      JSONArray matcheArr = null;
      String[] var5 = lines.split("\n");
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String line = var5[var7];
         Map actionMap = getAction(line);
         String action = (String)actionMap.get("action");
         String service;
         String regex;
         if (action.equals("Probe")) {
            probeObj = new JSONObject();
            matcheArr = new JSONArray();
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
            probeArr.put(probeObj);
         } else if (!action.equals("match") && !action.equals("softmatch")) {
            if (props.contains(action)) {
               String value = (String)actionMap.get("value");
               probeObj.put(action, value);
            } else if (!action.equals("#")) {
            }
         } else {
            JSONObject matchObj = new JSONObject();
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
               System.out.println("option：" + option);
               matchObj.put("version", version);
               matchObj.put("version", parseVersion(version));
               matcheArr.put(matchObj);
            }
         }
      }

      System.out.println(probeArr.length());
      FileOutputStream fileOutputStream = new FileOutputStream("d:/tmp/probes.json");
      fileOutputStream.write(probeArr.toString().getBytes());
      fileOutputStream.flush();
      fileOutputStream.close();

      try {
         FileOutputStream fos = new FileOutputStream("d:/tmp/probes.ser");
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(probeArr);
         oos.close();
         fos.close();
         System.out.printf("Serialized HashMap data is saved in hashmap.ser");
      } catch (IOException var18) {
         var18.printStackTrace();
      }

   }

   private static JSONObject parseVersion(String version) {
      JSONObject result = new JSONObject();
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
