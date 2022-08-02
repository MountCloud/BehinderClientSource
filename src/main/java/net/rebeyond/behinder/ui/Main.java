package net.rebeyond.behinder.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.utils.Utils;

public class Main extends Application {
   public void start(Stage primaryStage) throws Exception {
      try {
         Parent root = (Parent)FXMLLoader.load(this.getClass().getResource("Main.fxml"));
         primaryStage.setTitle(String.format("冰蝎%s动态二进制加密Web远程管理客户端", Constants.VERSION));
         primaryStage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
         primaryStage.setScene(new Scene(root, 1200.0D, 600.0D));
         primaryStage.show();
         Utils.disableSslVerification();
      } catch (Exception var3) {
         var3.printStackTrace();
      } catch (Error var4) {
         var4.printStackTrace();
      }

   }

   private static void copyToolsJar(String jdkPath, String jrePath) throws IOException {
      String toolJarPath = jdkPath + File.separator + "lib" + File.separator + "tools.jar";
      String desPath = jrePath + File.separator + "lib" + File.separator + "tools.jar";
      if ((new File(toolJarPath)).exists()) {
         JOptionPane.showMessageDialog((Component)null, "本地需要JDK环境，请手动将JDK lib目录中的tools.jar复制到JRE lib目录中。\n点击确定自动打开文件管理器。", "错误", 0);
         Desktop.getDesktop().open(new File(jdkPath + File.separator + "lib"));
         Desktop.getDesktop().open(new File(jrePath + File.separator + "lib"));
      }

   }

   private static void detectJDK() throws Exception {
      JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
      if (jc == null) {
         String javaHome = System.getProperty("java.home");
         Path jreHomePath = Paths.get((new File(javaHome)).toURI());
         String jreName = jreHomePath.getFileName().toString();
         if (jreName.toLowerCase().indexOf("jre") >= 0) {
            Path parentPath = jreHomePath.getParent();
            if (parentPath.getFileName().toString().toLowerCase().indexOf("jdk") >= 0) {
               copyToolsJar(parentPath.toString(), jreHomePath.toString());
               System.exit(0);
            } else {
               String jdkPath = parentPath.toString() + File.separator + jreName.replaceFirst("jre", "jdk");
               if ((new File(jdkPath)).exists()) {
                  copyToolsJar(jdkPath, jreHomePath.toString());
                  System.exit(0);
               }
            }
         }

         throw new Exception("本地机器上没有找到编译环境，请确认:1.是否安装了JDK环境;2." + System.getProperty("java.home") + File.separator + "lib目录下是否有tools.jar.");
      }
   }

   public void stop() throws Exception {
      super.stop();
      System.exit(0);
   }

   public static void main(String[] args) {
      try {
         detectJDK();
         launch(new String[0]);
      } catch (Exception var2) {
         var2.printStackTrace();
         JOptionPane.showMessageDialog((Component)null, var2.getMessage(), "错误", 0);
         System.exit(0);
      }

   }
}
