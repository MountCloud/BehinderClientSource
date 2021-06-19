package net.rebeyond.behinder.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.net.URI;
import javax.swing.JOptionPane;
import net.rebeyond.behinder.utils.Utils;

public class Launcher {
   public static void main(String[] args) {
      try {
         ClassLoader.getSystemClassLoader().loadClass("javafx.application.Application");
         Main.main(args);
      } catch (ClassNotFoundException var9) {
         try {
            String selfPath = Utils.getSelfPath();
            String javafxPath = selfPath + File.separator + "lib";
            String cmd = "\"" + System.getProperty("java.home") + File.separator + "bin" + File.separator + "java\" --module-path \"" + javafxPath + "\" --add-modules=javafx.controls --add-modules=javafx.fxml --add-modules=javafx.base --add-modules=javafx.graphics --add-modules=javafx.web -jar";
            cmd = cmd + " " + Utils.getSelfJarPath();
            Process p = null;
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
               Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", cmd});
            } else {
               p = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd});
            }

            if (p.waitFor() == 1) {
               Utils.setClipboardString(cmd);
               int response = JOptionPane.showConfirmDialog((Component)null, "本地未检测到JavaFX环境，Java11以后的版本不再集成Javafx，需要单独下载\n下载后可将javaFX SDK的lib目录拷贝至冰蝎同目录下，冰蝎会自动调用；也可通过命令行手动指定SDK目录(命令已拷贝至系统剪切板)\n是否打开网页下载？", "错误", 0);
               if (response == 0) {
                  String url = "https://openjfx.cn/dl/";
                  openWebpage(new URI(url));
               }
            }
         } catch (Exception var8) {
         }
      }

   }

   public static boolean openWebpage(URI uri) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Action.BROWSE)) {
         try {
            desktop.browse(uri);
            return true;
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }

      return false;
   }
}
