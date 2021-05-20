package net.rebeyond.behinder.ui.controller;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class MainWindowController {
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
   @FXML
   private UserCodeViewController userCodeViewController;
   @FXML
   private MemoViewController memoViewController;
   private Map basicInfoMap = new HashMap();
   private List workList = new ArrayList();

   public void initialize() {
      this.initControls();
   }

   public List getWorkList() {
      return this.workList;
   }

   private void initControls() {
      this.statusLabel.textProperty().addListener(new ChangeListener<String>() {
         public void changed(ObservableValue ov, String t, String t1) {
            MainWindowController.this.statusLabel.setTooltip(new Tooltip(t1));
         }
      });
      this.versionLabel.setText(String.format(this.versionLabel.getText(), Constants.VERSION));
      this.urlText.textProperty().addListener((observable, oldValue, newValue) -> {
         try {
            this.statusLabel.setText("正在获取基本信息，请稍后……");
            this.connStatusLabel.setText("正在连接");
            WebEngine webengine = this.basicInfoView.getEngine();
            Runnable runner = () -> {
               try {
                  this.doConnect();
                  int randStringLength = (new SecureRandom()).nextInt(3000);
                  String randString = Utils.getRandomString(randStringLength);
                  JSONObject basicInfoObj = new JSONObject(this.currentShellService.getBasicInfo(randString));
                  final String basicInfoStr = new String(Base64.decode(basicInfoObj.getString("basicInfo")), "UTF-8");
                  String driveList = (new String(Base64.decode(basicInfoObj.getString("driveList")), "UTF-8")).replace(":\\", ":/");
                  String currentPath = new String(Base64.decode(basicInfoObj.getString("currentPath")), "UTF-8");
                  String osInfo = (new String(Base64.decode(basicInfoObj.getString("osInfo")), "UTF-8")).toLowerCase();
                  String arch = (new String(Base64.decode(basicInfoObj.getString("arch")), "UTF-8")).toLowerCase();
                  this.basicInfoMap.put("basicInfo", basicInfoStr);
                  this.basicInfoMap.put("driveList", driveList);
                  this.basicInfoMap.put("currentPath", Utils.formatPath(currentPath));
                  this.basicInfoMap.put("workPath", Utils.formatPath(currentPath));
                  this.basicInfoMap.put("osInfo", osInfo.replace("winnt", "windows"));
                  this.basicInfoMap.put("arch", arch);
                  this.shellManager.updateOsInfo(this.shellEntity.getInt("id"), osInfo);
                  Platform.runLater(new Runnable() {
                     public void run() {
                        webengine.loadContent(basicInfoStr);

                        try {
                           MainWindowController.this.cmdViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.realCmdViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.pluginViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.shellManager);
                           MainWindowController.this.fileManagerViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.reverseViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.databaseViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                           MainWindowController.this.tunnelViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.updateInfoViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                           MainWindowController.this.userCodeViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                           MainWindowController.this.memoViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.shellManager);
                        } catch (Exception var2) {
                        }

                        MainWindowController.this.connStatusLabel.setText("已连接");
                        MainWindowController.this.connStatusLabel.setTextFill(Color.BLUE);
                        MainWindowController.this.statusLabel.setText("[OK]连接成功，基本信息获取完成。");
                     }
                  });
                  this.shellManager.setShellStatus(this.shellEntity.getInt("id"), Constants.SHELL_STATUS_ALIVE);
                  Runnable worker = new Runnable() {
                     public void run() {
                        while(true) {
                           try {
                              Thread.sleep((long)(((new Random()).nextInt(5) + 5) * 60 * 1000));
                              int randomStringLength = (new SecureRandom()).nextInt(3000);
                              MainWindowController.this.currentShellService.echo(Utils.getRandomString(randomStringLength));
                           } catch (Exception var2) {
                              if (var2 instanceof InterruptedException) {
                                 return;
                              }

                              Platform.runLater(() -> {
                                 Utils.showErrorMessage("提示", "由于您长时间未操作，当前连接会话已超时，请重新打开该网站。");
                              });
                              return;
                           }
                        }
                     }
                  };
                  Thread keepAliveWorker = new Thread(worker);
                  keepAliveWorker.start();
                  this.workList.add(keepAliveWorker);
               } catch (final Exception var12) {
                  Platform.runLater(new Runnable() {
                     public void run() {
                        MainWindowController.this.connStatusLabel.setText("连接失败");
                        MainWindowController.this.connStatusLabel.setTextFill(Color.RED);
                        MainWindowController.this.statusLabel.setText("[ERROR]连接失败：" + var12.getClass().getName() + ":" + var12.getMessage());

                        try {
                           MainWindowController.this.shellManager.setShellStatus(MainWindowController.this.shellEntity.getInt("id"), Constants.SHELL_STATUS_DEAD);
                        } catch (Exception var2) {
                        }

                     }
                  });
               }

            };
            Thread workThrad = new Thread(runner);
            this.workList.add(workThrad);
            workThrad.start();
         } catch (Exception var7) {
         }

      });
      this.mainTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
         public void changed(ObservableValue observable, Tab oldTab, Tab newTab) {
            String tabId = newTab.getId();
            byte var6 = -1;
            switch(tabId.hashCode()) {
            case -1356954629:
               if (tabId.equals("cmdTab")) {
                  var6 = 0;
               }
               break;
            case 0:
               if (tabId.equals("")) {
                  var6 = 1;
               }
            }

            switch(var6) {
            case 0:
            case 1:
            default:
            }
         }
      });
   }

   private void doConnect() throws Exception {
      boolean connectResult = this.currentShellService.doConnect();
   }

   public void init(JSONObject shellEntity, ShellManager shellManager, Map currentProxy) throws Exception {
      this.shellEntity = shellEntity;
      this.shellManager = shellManager;
      this.currentShellService = new ShellService(shellEntity);
      ShellService var10000 = this.currentShellService;
      ShellService.setProxy(currentProxy);
      this.urlText.setText(shellEntity.getString("url"));
      this.initTabs();
   }

   private void initTabs() {
      if (this.shellEntity.getString("type").equals("asp")) {
         Iterator var1 = this.mainTabPane.getTabs().iterator();

         while(true) {
            Tab tab;
            do {
               if (!var1.hasNext()) {
                  return;
               }

               tab = (Tab)var1.next();
            } while(!tab.getId().equals("realCmdTab") && !tab.getId().equals("tunnelTab") && !tab.getId().equals("reverseTab"));

            tab.setDisable(true);
         }
      }
   }
}
