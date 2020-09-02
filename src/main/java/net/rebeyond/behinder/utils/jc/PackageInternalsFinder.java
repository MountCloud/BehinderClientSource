// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils.jc;

import java.net.URI;
import java.util.jar.JarEntry;
import java.net.JarURLConnection;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Collection;
import java.net.URL;
import java.util.ArrayList;
import javax.tools.JavaFileObject;
import java.util.List;

public class PackageInternalsFinder
{
    private ClassLoader classLoader;
    private static final String CLASS_FILE_EXTENSION = ".class";
    
    public PackageInternalsFinder(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public List<JavaFileObject> find(final String packageName) throws IOException {
        final String javaPackageName = packageName.replaceAll("\\.", "/");
        final List<JavaFileObject> result = new ArrayList<JavaFileObject>();
        final Enumeration<URL> urlEnumeration = this.classLoader.getResources(javaPackageName);
        while (urlEnumeration.hasMoreElements()) {
            final URL packageFolderURL = urlEnumeration.nextElement();
            if (packageFolderURL.toString().startsWith("jar")) {
                result.addAll(this.listUnder(packageName, packageFolderURL));
            }
        }
        return result;
    }
    
    private Collection<JavaFileObject> listUnder(final String packageName, final URL packageFolderURL) {
        final File directory = new File(packageFolderURL.getFile());
        if (directory.isDirectory()) {
            return this.processDir(packageName, directory);
        }
        return this.processJar(packageFolderURL);
    }
    
    private List<JavaFileObject> processJar(final URL packageFolderURL) {
        final List<JavaFileObject> result = new ArrayList<JavaFileObject>();
        try {
            final String jarUri = packageFolderURL.toExternalForm().split("!")[0];
            final JarURLConnection jarConn = (JarURLConnection)packageFolderURL.openConnection();
            final String rootEntryName = jarConn.getEntryName();
            final int rootEnd = rootEntryName.length() + 1;
            final Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
            while (entryEnum.hasMoreElements()) {
                final JarEntry jarEntry = entryEnum.nextElement();
                final String name = jarEntry.getName();
                if (name.startsWith(rootEntryName) && name.indexOf(47, rootEnd) == -1 && name.endsWith(".class")) {
                    final URI uri = URI.create(jarUri + "!/" + name);
                    String binaryName = name.replaceAll("/", ".");
                    binaryName = binaryName.replaceAll(".class$", "");
                    result.add(new CustomJavaFileObject(binaryName, uri));
                }
            }
            jarConn.setDefaultUseCaches(false);
        }
        catch (Exception e) {
            throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file", e);
        }
        return result;
    }
    
    private List<JavaFileObject> processRsrc(final URL packageFolderURL) {
        final List<JavaFileObject> result = new ArrayList<JavaFileObject>();
        try {
            final String jarUri = packageFolderURL.toExternalForm().split("!")[0];
            final JarURLConnection jarConn = (JarURLConnection)packageFolderURL.openConnection();
            final String rootEntryName = jarConn.getEntryName();
            final int rootEnd = rootEntryName.length() + 1;
            final Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
            while (entryEnum.hasMoreElements()) {
                final JarEntry jarEntry = entryEnum.nextElement();
                final String name = jarEntry.getName();
                if (name.startsWith(rootEntryName) && name.indexOf(47, rootEnd) == -1 && name.endsWith(".class")) {
                    final URI uri = URI.create(jarUri + "!/" + name);
                    String binaryName = name.replaceAll("/", ".");
                    binaryName = binaryName.replaceAll(".class$", "");
                    result.add(new CustomJavaFileObject(binaryName, uri));
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file", e);
        }
        return result;
    }
    
    private List<JavaFileObject> processDir(final String packageName, final File directory) {
        final List<JavaFileObject> result = new ArrayList<JavaFileObject>();
        final File[] listFiles;
        final File[] childFiles = listFiles = directory.listFiles();
        for (final File childFile : listFiles) {
            if (childFile.isFile() && childFile.getName().endsWith(".class")) {
                String binaryName = packageName + "." + childFile.getName();
                binaryName = binaryName.replaceAll(".class$", "");
                result.add(new CustomJavaFileObject(binaryName, childFile.toURI()));
            }
        }
        return result;
    }
}
