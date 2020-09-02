// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import net.rebeyond.behinder.core.ShellService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.rebeyond.behinder.dao.ShellManager;

public class CmdViewController
{
    private ShellManager shellManager;
    @FXML
    private TextArea cmdTextArea;
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
        this.initCmdView();
    }
    
    private void initCmdView() {
        final String currentPath = this.basicInfoMap.get("currentPath");
        this.cmdTextArea.setText(currentPath + " >");
    }
    
    public void onCMDKeyPressed(final KeyEvent keyEvent) {
        final KeyCode keyCode = keyEvent.getCode();
        final int lineCount = this.cmdTextArea.getParagraphs().size();
        final String lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 1)).toString();
        if (keyCode == KeyCode.ENTER) {
            this.statusLabel.setText("[!]\u6b63\u5728\u6267\u884c\u547d\u4ee4\uff0c\u8bf7\u7a0d\u540e\u2026\u2026");
            final int cmdStart = lastLine.indexOf(">") + 1;
            final String cmd = lastLine.substring(cmdStart).trim();
            final Runnable runner = () -> {
                try {
                    JSONObject resultObj = this.currentShellService.runCmd(cmd);
                    String statusText = (resultObj.getString("status").equals("success") ? "[+]\u547d\u4ee4\u6267\u884c\u6210\u529f\u3002" : "[-]\u547d\u4ee4\u6267\u884c\u5931\u8d25\u3002");
                    Platform.runLater((Runnable)new Runnable() {
                        
                        @Override
                        public void run() {
                            CmdViewController.this.statusLabel.setText(statusText);
                            CmdViewController.this.cmdTextArea.appendText("\n" + resultObj.getString("msg") + "\n");
                            CmdViewController.this.cmdTextArea.appendText(CmdViewController.this.basicInfoMap.get("currentPath") + " >");
                        }
                    });
                }
                catch (Exception e) {
                    Platform.runLater((Runnable)new Runnable() {
                        @Override
                        public void run() {
                            CmdViewController.this.statusLabel.setText("[-]\u64cd\u4f5c\u5931\u8d25:" + e.getMessage());
                        }
                    });
                }
                return;
            };
            final Thread workThrad = new Thread(runner);
            this.workList.add(workThrad);
            workThrad.start();
            keyEvent.consume();
        }
    }
}
