// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui;

import javafx.scene.Scene;
import java.io.InputStream;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.net.URL;

import net.rebeyond.behinder.utils.Utils;
import net.rebeyond.behinder.core.Constants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.application.Application;

public class Main extends Application
{
    public void start(final Stage primaryStage) throws Exception {
        URL url = this.getClass().getResource("Main.fxml");
        final Parent root = (Parent)FXMLLoader.load(url);
        primaryStage.setTitle(String.format("这不是冰蝎[%s]动态二进制加密Web远程管理客户端", Constants.VERSION));
        primaryStage.getIcons().add(new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.png"))));
        primaryStage.setScene(new Scene(root, 1100.0, 600.0));
        primaryStage.show();
    }

    public static void main(final String[] args) {
        launch(new String[0]);
    }
}
