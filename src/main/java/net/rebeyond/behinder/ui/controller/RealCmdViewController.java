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
            if (this.realCmdBtn.getText().equals("启动")) {
                this.createRealCmd();
            }
            else {
                this.stopRealCmd();
            }
        });
    }

    @FXML
    private void createRealCmd() {
        this.statusLabel.setText("正在启动虚拟终端……");
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
                        this.statusLabel.setText("虚拟终端启动完成。");
                        this.realCmdTextArea.requestFocus();
                        this.currentPos = this.realCmdTextArea.getLength();
                        this.realCmdBtn.setText("停止");
                        this.running = Constants.REALCMD_RUNNING;
                    }
                    else {
                        this.statusLabel.setText("虚拟终端启动失败:" + msg);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> this.statusLabel.setText("虚拟终端启动失败:" + e.getMessage()));
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }

    private void stopRealCmd() {
        this.statusLabel.setText("正在停止虚拟终端……");
        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.stopRealCMD();
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("success")) {
                        this.statusLabel.setText("虚拟终端已停止。");
                        this.realCmdBtn.setText("启动");
                        this.running = Constants.REALCMD_STOPPED;
                    }
                    else {
                        this.statusLabel.setText("虚拟终端启动失败:" + msg);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> this.statusLabel.setText("操作失败:" + e.getMessage()));
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
            this.statusLabel.setText("虚拟终端尚未启动，请先启动虚拟终端。");
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
        this.statusLabel.setText("请稍后……");
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
                    Platform.runLater(() -> this.statusLabel.setText("完成。"));
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
