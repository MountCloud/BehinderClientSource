// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils.jc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Set;
import java.io.IOException;
import javax.tools.StandardLocation;
import java.util.Iterator;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.util.Map;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileManager;

public class CustomClassloaderJavaFileManager implements JavaFileManager
{
    private final ClassLoader classLoader;
    private final StandardJavaFileManager standardFileManager;
    private final PackageInternalsFinder finder;
    public static Map<String, JavaFileObject> fileObjects;
    
    public CustomClassloaderJavaFileManager(final ClassLoader classLoader, final StandardJavaFileManager standardFileManager) {
        this.classLoader = classLoader;
        this.standardFileManager = standardFileManager;
        this.finder = new PackageInternalsFinder(classLoader);
    }
    
    @Override
    public ClassLoader getClassLoader(final Location location) {
        return this.standardFileManager.getClassLoader(location);
    }
    
    @Override
    public String inferBinaryName(final Location location, final JavaFileObject file) {
        if (file instanceof CustomJavaFileObject) {
            return ((CustomJavaFileObject)file).binaryName();
        }
        return this.standardFileManager.inferBinaryName(location, file);
    }
    
    @Override
    public boolean isSameFile(final FileObject a, final FileObject b) {
        return this.standardFileManager.isSameFile(a, b);
    }
    
    @Override
    public boolean handleOption(final String current, final Iterator<String> remaining) {
        return this.standardFileManager.handleOption(current, remaining);
    }
    
    @Override
    public boolean hasLocation(final Location location) {
        return location == StandardLocation.CLASS_PATH || location == StandardLocation.PLATFORM_CLASS_PATH;
    }
    
    @Override
    public JavaFileObject getJavaFileForInput(final Location location, final String className, final JavaFileObject.Kind kind) throws IOException {
        final JavaFileObject javaFileObject = CustomClassloaderJavaFileManager.fileObjects.get(className);
        if (javaFileObject == null) {
            this.standardFileManager.getJavaFileForInput(location, className, kind);
        }
        return javaFileObject;
    }
    
    @Override
    public JavaFileObject getJavaFileForOutput(final Location location, final String className, final JavaFileObject.Kind kind, final FileObject sibling) throws IOException {
        final JavaFileObject javaFileObject = new MyJavaFileObject(className, kind);
        CustomClassloaderJavaFileManager.fileObjects.put(className, javaFileObject);
        return javaFileObject;
    }
    
    @Override
    public FileObject getFileForInput(final Location location, final String packageName, final String relativeName) throws IOException {
        return this.standardFileManager.getFileForInput(location, packageName, relativeName);
    }
    
    @Override
    public FileObject getFileForOutput(final Location location, final String packageName, final String relativeName, final FileObject sibling) throws IOException {
        return this.standardFileManager.getFileForOutput(location, packageName, relativeName, sibling);
    }
    
    @Override
    public void flush() throws IOException {
        this.standardFileManager.flush();
    }
    
    @Override
    public void close() throws IOException {
        this.standardFileManager.close();
    }
    
    @Override
    public Iterable<JavaFileObject> list(final Location location, final String packageName, final Set<JavaFileObject.Kind> kinds, final boolean recurse) throws IOException {
        if (location == StandardLocation.PLATFORM_CLASS_PATH) {
            return this.standardFileManager.list(location, packageName, kinds, recurse);
        }
        if (location != StandardLocation.CLASS_PATH || !kinds.contains(JavaFileObject.Kind.CLASS)) {
            return Collections.emptyList();
        }
        if (packageName.startsWith("java.")) {
            return this.standardFileManager.list(location, packageName, kinds, recurse);
        }
        return this.finder.find(packageName);
    }
    
    @Override
    public int isSupportedOption(final String option) {
        return -1;
    }
    
    static {
        CustomClassloaderJavaFileManager.fileObjects = new ConcurrentHashMap<String, JavaFileObject>();
    }
}
