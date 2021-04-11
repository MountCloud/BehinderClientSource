package net.rebeyond.behinder.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
   private static final int BUFFER_SIZE = 2048;
   private static final boolean KeepDirStructure = true;

   public static void main(String[] args) {
      try {
         unZipFiles("/Users/rebeyond/newScan.zip", "/Users/rebeyond/newScan");
      } catch (Exception var2) {
      }

   }

   public static void toZip(String srcDir, String outPathFile, boolean isDelSrcFile) throws Exception {
      long start = System.currentTimeMillis();
      FileOutputStream out = null;
      ZipOutputStream zos = null;

      try {
         out = new FileOutputStream(new File(outPathFile));
         zos = new ZipOutputStream(out);
         File sourceFile = new File(srcDir);
         if (!sourceFile.exists()) {
            throw new Exception("需压缩文件或者文件夹不存在");
         }

         compress(sourceFile, zos, sourceFile.getName());
         if (isDelSrcFile) {
            delDir(srcDir);
         }
      } catch (Exception var15) {
         throw new Exception("zip error from ZipUtils");
      } finally {
         try {
            if (zos != null) {
               zos.close();
            }

            if (out != null) {
               out.close();
            }
         } catch (Exception var14) {
         }

      }

   }

   private static void compress(File sourceFile, ZipOutputStream zos, String name) throws Exception {
      byte[] buf = new byte[2048];
      if (sourceFile.isFile()) {
         zos.putNextEntry(new ZipEntry(name));
         FileInputStream in = new FileInputStream(sourceFile);

         int len;
         while((len = in.read(buf)) != -1) {
            zos.write(buf, 0, len);
         }

         zos.closeEntry();
         in.close();
      } else {
         File[] listFiles = sourceFile.listFiles();
         if (listFiles != null && listFiles.length != 0) {
            File[] var10 = listFiles;
            int var6 = listFiles.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               File file = var10[var7];
               compress(file, zos, name + "/" + file.getName());
            }
         } else {
            zos.putNextEntry(new ZipEntry(name + "/"));
            zos.closeEntry();
         }
      }

   }

   public static void unZipFiles(String zipPath, String descDir) throws IOException {
      long var2 = System.currentTimeMillis();

      try {
         File zipFile = new File(zipPath);
         if (!zipFile.exists()) {
            throw new IOException("需解压文件不存在.");
         } else {
            File pathFile = new File(descDir);
            if (!pathFile.exists()) {
               pathFile.mkdirs();
            }

            ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));
            Enumeration entries = zip.entries();

            while(true) {
               InputStream in;
               String outPath;
               do {
                  if (!entries.hasMoreElements()) {
                     return;
                  }

                  ZipEntry entry = (ZipEntry)entries.nextElement();
                  String zipEntryName = entry.getName();
                  in = zip.getInputStream(entry);
                  outPath = (descDir + File.separator + zipEntryName).replaceAll("\\*", "/");
                  File file = new File(outPath.substring(0, outPath.lastIndexOf(47)));
                  if (!file.exists()) {
                     file.mkdirs();
                  }
               } while((new File(outPath)).isDirectory());

               OutputStream out = new FileOutputStream(outPath);
               byte[] buf1 = new byte[1024];

               int len;
               while((len = in.read(buf1)) > 0) {
                  out.write(buf1, 0, len);
               }

               in.close();
               out.close();
            }
         }
      } catch (Exception var16) {
         throw new IOException(var16);
      }
   }

   public static void delDir(String dirPath) throws IOException {
      long var1 = System.currentTimeMillis();

      try {
         File dirFile = new File(dirPath);
         if (dirFile.exists()) {
            if (dirFile.isFile()) {
               dirFile.delete();
            } else {
               File[] files = dirFile.listFiles();
               if (files != null) {
                  for(int i = 0; i < files.length; ++i) {
                     delDir(files[i].toString());
                  }

                  dirFile.delete();
               }
            }
         }
      } catch (Exception var6) {
         throw new IOException("删除文件异常.");
      }
   }
}
