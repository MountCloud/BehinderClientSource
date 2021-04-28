package net.rebeyond.behinder.ui.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class UpdateInfoViewController {
   private ShellManager shellManager;
   @FXML
   private TextArea realCmdTextArea;
   @FXML
   private TextField shellPathText;
   @FXML
   private Button realCmdBtn;
   private ShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private Label statusLabel;
   @FXML
   private WebView updateInfoWebview;

   public void init(ShellService shellService, List workList, Label statusLabel) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.initUpdateInfoView();
   }

   private void initUpdateInfoView() {
      this.checkUpdate();
   }

   private void checkUpdate() {
      Runnable runner = () -> {
         try {
            String updateInfoText = Utils.sendGetRequest("https://www.rebeyond.net/Behinder/update.htm?ver=" + URLEncoder.encode(Constants.VERSION, "utf-8"), "");
            JSONObject updateInfoObj = new JSONObject(updateInfoText);
            if (updateInfoObj.getString("needUpdate").equals("true")) {
               String latestVersion = updateInfoObj.getString("latestVersion");
               Platform.runLater(() -> {
                  this.statusLabel.setText("发现新版本：" + latestVersion + "，点击下载");
                  this.statusLabel.setOnMouseClicked((event) -> {
                     if (this.statusLabel.getText().startsWith("发现新版本：")) {
                        try {
                           Utils.openWebpage(new URI("https://github.com/rebeyond/Behinder/releases"));
                        } catch (Exception var3) {
                           var3.printStackTrace();
                        }
                     }

                  });
               });
            }

            Platform.runLater(() -> {
               this.updateInfoWebview.getEngine().loadContent(updateInfoObj.getString("body"));
            });
         } catch (Exception var4) {
            var4.printStackTrace();
            Platform.runLater(() -> {
               this.statusLabel.setText("检查更新出错。");
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }
}
