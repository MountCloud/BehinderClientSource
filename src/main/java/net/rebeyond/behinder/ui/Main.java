package net.rebeyond.behinder.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.utils.Utils;

import java.io.ByteArrayInputStream;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        primaryStage.setTitle(String.format("冰蝎%s动态二进制加密Web远程管理客户端", Constants.VERSION));
        primaryStage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
        primaryStage.setScene(new Scene(root, 1200, 600));
        primaryStage.show();
        //System.out.println("dis start");
        Utils.disableSslVerification();
        //System.out.println("dis end");
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }
    public static void main(String[] args) {

        launch();
    }
}
