package net.rebeyond.behinder.ui;

import java.io.ByteArrayInputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.utils.Utils;

public class Main extends Application {
   public void start(Stage primaryStage) {
      try {
         Parent root = (Parent)FXMLLoader.load(this.getClass().getResource("Main.fxml"));
         primaryStage.setTitle(String.format("冰蝎%s动态二进制加密Web远程管理客户端", Constants.VERSION));
         primaryStage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
         Scene scene = new Scene(root, 1200.0, 600.0);
         scene.getRoot().setStyle("-fx-font-family: 'Arial'");
         primaryStage.setScene(scene);
         primaryStage.show();
      } catch (Exception var4) {
         var4.printStackTrace();
      } catch (Error var5) {
         var5.printStackTrace();
      }

   }

   private static void detectJDK() throws Exception {
      Utils.getCompiler();
   }

   public void stop() throws Exception {
      super.stop();
      System.exit(0);
   }

   public static void main(String[] args) {
      try {
         launch(new String[0]);
      } catch (Throwable var2) {
         var2.printStackTrace();
      }

   }
}
