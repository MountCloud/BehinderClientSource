// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.nio.charset.Charset;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;
import java.io.File;

public class ZipUtil
{
    private static final int BUFFER_SIZE = 2048;
    private static final boolean KeepDirStructure = true;
    
    public static void main(final String[] args) {
        try {
            unZipFiles("/Users/rebeyond/newScan.zip", "/Users/rebeyond/newScan");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void toZip(final String srcDir, final String outPathFile, final boolean isDelSrcFile) throws Exception {
        final long start = System.currentTimeMillis();
        FileOutputStream out = null;
        ZipOutputStream zos = null;
        try {
            out = new FileOutputStream(new File(outPathFile));
            zos = new ZipOutputStream(out);
            final File sourceFile = new File(srcDir);
            if (!sourceFile.exists()) {
                throw new Exception("\u9700\u538b\u7f29\u6587\u4ef6\u6216\u8005\u6587\u4ef6\u5939\u4e0d\u5b58\u5728");
            }
            compress(sourceFile, zos, sourceFile.getName());
            if (isDelSrcFile) {
                delDir(srcDir);
            }
        }
        catch (Exception e) {
            throw new Exception("zip error from ZipUtils");
        }
        finally {
            try {
                if (zos != null) {
                    zos.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            catch (Exception ex) {}
        }
    }
    
    private static void compress(final File sourceFile, final ZipOutputStream zos, final String name) throws Exception {
        final byte[] buf = new byte[2048];
        if (sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            final FileInputStream in = new FileInputStream(sourceFile);
            int len;
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            in.close();
        }
        else {
            final File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                zos.putNextEntry(new ZipEntry(name + "/"));
                zos.closeEntry();
            }
            else {
                for (final File file : listFiles) {
                    compress(file, zos, name + "/" + file.getName());
                }
            }
        }
    }
    
    public static void unZipFiles(final String zipPath, final String descDir) throws IOException {
        final long start = System.currentTimeMillis();
        try {
            final File zipFile = new File(zipPath);
            if (!zipFile.exists()) {
                throw new IOException("\u9700\u89e3\u538b\u6587\u4ef6\u4e0d\u5b58\u5728.");
            }
            final File pathFile = new File(descDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            final ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));
            final Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final String zipEntryName = entry.getName();
                final InputStream in = zip.getInputStream(entry);
                final String outPath = (descDir + File.separator + zipEntryName).replaceAll("\\*", "/");
                final File file = new File(outPath.substring(0, outPath.lastIndexOf(47)));
                if (!file.exists()) {
                    file.mkdirs();
                }
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                final OutputStream out = new FileOutputStream(outPath);
                final byte[] buf1 = new byte[1024];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    public static void delDir(final String dirPath) throws IOException {
        final long start = System.currentTimeMillis();
        try {
            final File dirFile = new File(dirPath);
            if (!dirFile.exists()) {
                return;
            }
            if (dirFile.isFile()) {
                dirFile.delete();
                return;
            }
            final File[] files = dirFile.listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; ++i) {
                delDir(files[i].toString());
            }
            dirFile.delete();
        }
        catch (Exception e) {
            throw new IOException("\u5220\u9664\u6587\u4ef6\u5f02\u5e38.");
        }
    }
}
