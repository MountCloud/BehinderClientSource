package net.rebeyond.behinder.utils.jc;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;

public class CustomClassloaderJavaFileManager implements JavaFileManager {
   private final ClassLoader classLoader;
   private final StandardJavaFileManager standardFileManager;
   private final PackageInternalsFinder finder;
   public static Map fileObjects = new ConcurrentHashMap();

   public CustomClassloaderJavaFileManager(ClassLoader classLoader, StandardJavaFileManager standardFileManager) {
      this.classLoader = classLoader;
      this.standardFileManager = standardFileManager;
      this.finder = new PackageInternalsFinder(classLoader);
   }

   public ClassLoader getClassLoader(JavaFileManager.Location location) {
      return this.standardFileManager.getClassLoader(location);
   }

   public String inferBinaryName(JavaFileManager.Location location, JavaFileObject file) {
      return file instanceof CustomJavaFileObject ? ((CustomJavaFileObject)file).binaryName() : this.standardFileManager.inferBinaryName(location, file);
   }

   public boolean isSameFile(FileObject a, FileObject b) {
      return this.standardFileManager.isSameFile(a, b);
   }

   public boolean handleOption(String current, Iterator remaining) {
      return this.standardFileManager.handleOption(current, remaining);
   }

   public boolean hasLocation(JavaFileManager.Location location) {
      return location == StandardLocation.CLASS_PATH || location == StandardLocation.PLATFORM_CLASS_PATH;
   }

   public JavaFileObject getJavaFileForInput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind) throws IOException {
      JavaFileObject javaFileObject = (JavaFileObject)fileObjects.get(className);
      if (javaFileObject == null) {
         this.standardFileManager.getJavaFileForInput(location, className, kind);
      }

      return javaFileObject;
   }

   public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
      JavaFileObject javaFileObject = new MyJavaFileObject(className, kind);
      fileObjects.put(className, javaFileObject);
      return javaFileObject;
   }

   public FileObject getFileForInput(JavaFileManager.Location location, String packageName, String relativeName) throws IOException {
      return this.standardFileManager.getFileForInput(location, packageName, relativeName);
   }

   public FileObject getFileForOutput(JavaFileManager.Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
      return this.standardFileManager.getFileForOutput(location, packageName, relativeName, sibling);
   }

   public void flush() throws IOException {
      this.standardFileManager.flush();
   }

   public void close() throws IOException {
      this.standardFileManager.close();
   }

   public Iterable list(JavaFileManager.Location location, String packageName, Set kinds, boolean recurse) throws IOException {
      if (location == StandardLocation.PLATFORM_CLASS_PATH) {
         return this.standardFileManager.list(location, packageName, kinds, recurse);
      } else if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS)) {
         return (Iterable)(packageName.startsWith("java.") ? this.standardFileManager.list(location, packageName, kinds, recurse) : this.finder.find(packageName));
      } else {
         return Collections.emptyList();
      }
   }

   public int isSupportedOption(String option) {
      return -1;
   }
}
