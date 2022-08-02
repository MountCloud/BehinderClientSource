package net.rebeyond.behinder.utils.jc;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

public class PackageInternalsFinder {
   private ClassLoader classLoader;
   private static final String CLASS_FILE_EXTENSION = ".class";

   public PackageInternalsFinder(ClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   public List find(String packageName) throws IOException {
      String javaPackageName = packageName.replaceAll("\\.", "/");
      List result = new ArrayList();
      Enumeration urlEnumeration = this.classLoader.getResources(javaPackageName);

      while(urlEnumeration.hasMoreElements()) {
         URL packageFolderURL = (URL)urlEnumeration.nextElement();
         if (packageFolderURL.toString().startsWith("jar")) {
            result.addAll(this.listUnder(packageName, packageFolderURL));
         }
      }

      return result;
   }

   private Collection listUnder(String packageName, URL packageFolderURL) {
      File directory = new File(packageFolderURL.getFile());
      return directory.isDirectory() ? this.processDir(packageName, directory) : this.processJar(packageFolderURL);
   }

   private List processJar(URL packageFolderURL) {
      ArrayList result = new ArrayList();

      try {
         String jarUri = packageFolderURL.toExternalForm().split("!")[0];
         JarURLConnection jarConn = (JarURLConnection)packageFolderURL.openConnection();
         String rootEntryName = jarConn.getEntryName();
         int rootEnd = rootEntryName.length() + 1;
         Enumeration entryEnum = jarConn.getJarFile().entries();

         while(entryEnum.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry)entryEnum.nextElement();
            String name = jarEntry.getName();
            if (name.startsWith(rootEntryName) && name.indexOf(47, rootEnd) == -1 && name.endsWith(".class")) {
               URI uri = URI.create(jarUri + "!/" + name);
               String binaryName = name.replaceAll("/", ".");
               binaryName = binaryName.replaceAll(".class$", "");
               result.add(new CustomJavaFileObject(binaryName, uri));
            }
         }

         jarConn.setDefaultUseCaches(false);
         return result;
      } catch (Exception var12) {
         throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file", var12);
      }
   }

   private List processRsrc(URL packageFolderURL) {
      ArrayList result = new ArrayList();

      try {
         String jarUri = packageFolderURL.toExternalForm().split("!")[0];
         JarURLConnection jarConn = (JarURLConnection)packageFolderURL.openConnection();
         String rootEntryName = jarConn.getEntryName();
         int rootEnd = rootEntryName.length() + 1;
         Enumeration entryEnum = jarConn.getJarFile().entries();

         while(entryEnum.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry)entryEnum.nextElement();
            String name = jarEntry.getName();
            if (name.startsWith(rootEntryName) && name.indexOf(47, rootEnd) == -1 && name.endsWith(".class")) {
               URI uri = URI.create(jarUri + "!/" + name);
               String binaryName = name.replaceAll("/", ".");
               binaryName = binaryName.replaceAll(".class$", "");
               result.add(new CustomJavaFileObject(binaryName, uri));
            }
         }

         return result;
      } catch (Exception var12) {
         throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file", var12);
      }
   }

   private List processDir(String packageName, File directory) {
      List result = new ArrayList();
      File[] childFiles = directory.listFiles();
      File[] var5 = childFiles;
      int var6 = childFiles.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         File childFile = var5[var7];
         if (childFile.isFile() && childFile.getName().endsWith(".class")) {
            String binaryName = packageName + "." + childFile.getName();
            binaryName = binaryName.replaceAll(".class$", "");
            result.add(new CustomJavaFileObject(binaryName, childFile.toURI()));
         }
      }

      return result;
   }
}
