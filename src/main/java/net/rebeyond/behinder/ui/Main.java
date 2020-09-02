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
        primaryStage.setTitle(String.format("\u51b0\u874e%s\u52a8\u6001\u4e8c\u8fdb\u5236\u52a0\u5bc6Web\u8fdc\u7a0b\u7ba1\u7406\u5ba2\u6237\u7aef", Constants.VERSION));
        primaryStage.getIcons().add(new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
        primaryStage.setScene(new Scene(root, 1100.0, 600.0));
        primaryStage.show();
    }
    
    public static void main(final String[] args) {
        launch(new String[0]);
    }
}
