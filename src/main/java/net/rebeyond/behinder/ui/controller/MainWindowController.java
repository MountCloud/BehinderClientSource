// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import net.rebeyond.behinder.utils.Utils;
import java.security.SecureRandom;
import javafx.event.ActionEvent;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import net.rebeyond.behinder.core.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.AnchorPane;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.core.ShellService;
import org.json.JSONObject;
import javafx.scene.control.Tab;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.scene.control.TabPane;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class MainWindowController
{
    @FXML
    private GridPane mainGridPane;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private WebView basicInfoView;
    @FXML
    private TextField urlText;
    @FXML
    private Label statusLabel;
    @FXML
    private Label connStatusLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private TextArea sourceCodeTextArea;
    @FXML
    private TextArea sourceResultArea;
    @FXML
    private Button runCodeBtn;
    @FXML
    private Tab realCmdTab;
    private JSONObject shellEntity;
    private ShellService currentShellService;
    private ShellManager shellManager;
    @FXML
    private AnchorPane pluginView;
    @FXML
    private PluginViewController pluginViewController;
    @FXML
    private FileManagerViewController fileManagerViewController;
    @FXML
    private ReverseViewController reverseViewController;
    @FXML
    private DatabaseViewController databaseViewController;
    @FXML
    private CmdViewController cmdViewController;
    @FXML
    private RealCmdViewController realCmdViewController;
    @FXML
    private TunnelViewController tunnelViewController;
    @FXML
    private UpdateInfoViewController updateInfoViewController;
    private Map<String, String> basicInfoMap;
    private List<Thread> workList;
    
    public MainWindowController() {
        this.basicInfoMap = new HashMap<String, String>();
        this.workList = new ArrayList<Thread>();
    }
    
    public void initialize() {
        this.initControls();
    }
    
    public List<Thread> getWorkList() {
        return this.workList;
    }
    
    private void initControls() {
        this.versionLabel.setText(String.format(this.versionLabel.getText(), Constants.VERSION));
        this.urlText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.statusLabel.setText("\u6b63\u5728\u83b7\u53d6\u57fa\u672c\u4fe1\u606f\uff0c\u8bf7\u7a0d\u540e\u2026\u2026");
                this.connStatusLabel.setText("\u6b63\u5728\u8fde\u63a5");
                final WebEngine webengine = this.basicInfoView.getEngine();
                final Runnable runner = () -> {
                    try {
                        this.doConnect();
                        Integer randStringLength = new SecureRandom().nextInt(3000);
                        String randString = Utils.getRandomString(randStringLength);
                        JSONObject basicInfoObj = new JSONObject(this.currentShellService.getBasicInfo(randString));
                        String basicInfoStr = new String(Base64.decode(basicInfoObj.getString("basicInfo")), "UTF-8");
                        String driveList = new String(Base64.decode(basicInfoObj.getString("driveList")), "UTF-8").replace(":\\", ":/");
                        String currentPath = new String(Base64.decode(basicInfoObj.getString("currentPath")), "UTF-8");
                        String osInfo = new String(Base64.decode(basicInfoObj.getString("osInfo")), "UTF-8").toLowerCase();
                        this.basicInfoMap.put("basicInfo", basicInfoStr);
                        this.basicInfoMap.put("driveList", driveList);
                        this.basicInfoMap.put("currentPath", currentPath);
                        this.basicInfoMap.put("osInfo", osInfo.replace("winnt", "windows"));
                        this.shellManager.updateOsInfo(this.shellEntity.getInt("id"), osInfo);
                        Platform.runLater((Runnable)new Runnable() {
                            
                            @Override
                            public void run() {
                                webengine.loadContent(basicInfoStr);
                                try {
                                    MainWindowController.this.cmdViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                                    MainWindowController.this.realCmdViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                                    MainWindowController.this.initSourceCodeView();
                                    MainWindowController.this.pluginViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.shellManager);
                                    MainWindowController.this.fileManagerViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                                    MainWindowController.this.reverseViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                                    MainWindowController.this.databaseViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                                    MainWindowController.this.tunnelViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                                    MainWindowController.this.updateInfoViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                MainWindowController.this.connStatusLabel.setText("\u5df2\u8fde\u63a5");
                                MainWindowController.this.connStatusLabel.setTextFill((Paint)Color.BLUE);
                                MainWindowController.this.statusLabel.setText("[OK]\u8fde\u63a5\u6210\u529f\uff0c\u57fa\u672c\u4fe1\u606f\u83b7\u53d6\u5b8c\u6210\u3002");
                            }
                        });
                        this.currentShellService.keepAlive();
                    }
                    catch (Exception e) {
                        Platform.runLater((Runnable)new Runnable() {
                            
                            @Override
                            public void run() {
                                e.printStackTrace();
                                MainWindowController.this.connStatusLabel.setText("\u8fde\u63a5\u5931\u8d25");
                                MainWindowController.this.connStatusLabel.setTextFill((Paint)Color.RED);
                                MainWindowController.this.statusLabel.setText("[ERROR]\u8fde\u63a5\u5931\u8d25\uff1a" + e.getMessage());
                            }
                        });
                    }
                    return;
                };
                final Thread workThrad = new Thread(runner);
                this.workList.add(workThrad);
                workThrad.start();
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        });
        this.mainTabPane.getSelectionModel().selectedItemProperty().addListener((ChangeListener)new ChangeListener<Tab>() {
            public void changed(final ObservableValue<? extends Tab> observable, final Tab oldTab, final Tab newTab) {
                final String id;
                final String tabId = id = newTab.getId();
                switch (id) {
                }
            }
        });
    }
    
    private void initSourceCodeView() {
        this.runCodeBtn.setOnAction(event -> this.runSourceCode());
    }
    
    private void runSourceCode() {
        this.statusLabel.setText("\u6b63\u5728\u6267\u884c\u2026\u2026");
        final Runnable runner = () -> {
            try {
                String result = this.currentShellService.eval(this.sourceCodeTextArea.getText());
                Platform.runLater(() -> {
                    this.sourceResultArea.setText(result);
                    this.statusLabel.setText("\u5b8c\u6210\u3002");
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    this.statusLabel.setText("\u8fd0\u884c\u5931\u8d25:" + e.getMessage());
                    this.sourceResultArea.setText(e.getMessage());
                });
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    private void doConnect() throws Exception {
        final boolean connectResult = this.currentShellService.doConnect();
    }
    
    public void init(final JSONObject shellEntity, final ShellManager shellManager, final Map<String, Object> currentProxy) throws Exception {
        this.shellEntity = shellEntity;
        this.shellManager = shellManager;
        this.currentShellService = new ShellService(shellEntity);
        final ShellService currentShellService = this.currentShellService;
        ShellService.setProxy(currentProxy);
        this.urlText.setText(shellEntity.getString("url"));
        this.initTabs();
    }
    
    private void initTabs() {
        if (this.shellEntity.getString("type").equals("asp")) {
            for (final Tab tab : this.mainTabPane.getTabs()) {
                if (tab.getId().equals("realCmdTab") || tab.getId().equals("tunnelTab") || tab.getId().equals("reverseTab")) {
                    tab.setDisable(true);
                }
            }
        }
    }
}
