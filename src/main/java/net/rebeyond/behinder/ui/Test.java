package net.rebeyond.behinder.ui;

import net.rebeyond.behinder.utils.Utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

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
    public static void main(String[] args) throws Exception {

        String cmd="java14 --module-path \"/Users/rebeyond/lib\" --add-modules=javafx.controls --add-modules=javafx.fxml --add-modules=javafx.base --add-modules=javafx.graphics --add-modules=javafx.web -jar /Users/rebeyond/Behinder.jar";
        //System.out.println(cmd);
        Runtime.getRuntime().exec(new String[]{"bash","-c",cmd});


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
