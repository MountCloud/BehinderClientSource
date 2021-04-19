package net.rebeyond.behinder.ui.controller;

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
      String updateUrl = "https://www.rebeyond.net/Behinder/update.html?ver=" + Constants.VERSION;
      this.updateInfoWebview.getEngine().load(updateUrl);
   }

   private void checkUpdate() {
      Runnable runner = () -> {
         try {
            String updateInfoText = Utils.sendGetRequest("https://www.rebeyond.net/Behinder/update.html?ver=" + Constants.VERSION, "");
            JSONObject updateInfoObj = new JSONObject(updateInfoText);
            if (updateInfoObj.getString("needUpdate").equals("true")) {
               String latestVersion = updateInfoObj.getString("latestVersion");
               Platform.runLater(() -> {
                  this.statusLabel.setText("发现新版本：" + latestVersion);
               });
            } else {
               Platform.runLater(() -> {
                  this.updateInfoWebview.getEngine().loadContent(updateInfoObj.getString("body"));
               });
            }
         } catch (Exception var4) {
            this.statusLabel.setText("检查更新出错。");
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }
}
