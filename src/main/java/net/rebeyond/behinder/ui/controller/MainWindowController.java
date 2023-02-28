package net.rebeyond.behinder.ui.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.entity.BShell;
import net.rebeyond.behinder.entity.DecryptException;
import net.rebeyond.behinder.service.OfflineService;
import net.rebeyond.behinder.service.PluginService;
import net.rebeyond.behinder.service.Task;
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
   private Label authorLabel;
   @FXML
   private TextArea sourceCodeTextArea;
   @FXML
   private TextArea sourceResultArea;
   @FXML
   private Button runCodeBtn;
   @FXML
   private Tab realCmdTab;
   private JSONObject shellEntity;
   private JSONObject effectShellEntity;
   private IShellService currentShellService;
   private PluginService pluginService;
   private ShellManager shellManager;
   private boolean offline;
   @FXML
   private AnchorPane pluginView;
   @FXML
   private PluginViewController pluginViewController;
   @FXML
   private FileManagerViewController fileManagerViewController;
   @FXML
   private ParallelViewController parallelViewController;
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
   private List taskList = new ArrayList();

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
      int labelCount = ((GridPane)this.statusLabel.getParent()).getChildren().size();
      if (labelCount > 1) {
         Label taskCenterLabel = (Label)((HBox)((GridPane)this.statusLabel.getParent()).getChildren().get(1)).getChildren().get(0);
         taskCenterLabel.setOnMouseClicked((event) -> {
            this.showTaskCenter();
         });
      }

      if (this.versionLabel != null) {
         this.versionLabel.setText(String.format(this.versionLabel.getText(), Constants.VERSION));
      }

      this.authorLabel.setText(Constants.AUTHOR);
      String TIP_FOR_VERSION = "保留版权是对原创基本的尊重：）";
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

                  JSONObject basicInfoObj;
                  try {
                     basicInfoObj = this.currentShellService.getBasicInfo(randString);
                  } catch (Exception var15) {
                     this.currentShellService.setCompareMode(Constants.COMPARE_MODE_BYTES);
                     basicInfoObj = this.currentShellService.getBasicInfo(randString);
                  }

                  if (basicInfoObj.has("msg")) {
                     basicInfoObj = new JSONObject(basicInfoObj.getString("msg"));
                  }

                  final String basicInfoStr = new String(Base64.getDecoder().decode(basicInfoObj.getString("basicInfo")), "UTF-8");
                  String driveList = (new String(Base64.getDecoder().decode(basicInfoObj.getString("driveList")), "UTF-8")).replace(":\\", ":/");
                  String currentPath = new String(Base64.getDecoder().decode(basicInfoObj.getString("currentPath")), "UTF-8");
                  String osInfo = (new String(Base64.getDecoder().decode(basicInfoObj.getString("osInfo")), "UTF-8")).toLowerCase();
                  String arch = (new String(Base64.getDecoder().decode(basicInfoObj.getString("arch")), "UTF-8")).toLowerCase();
                  String localIp = (new String(Base64.getDecoder().decode(basicInfoObj.optString("localIp", "")), "UTF-8")).toLowerCase();
                  this.basicInfoMap.put("basicInfo", basicInfoStr);
                  this.basicInfoMap.put("driveList", driveList);
                  this.basicInfoMap.put("currentPath", Utils.formatPath(currentPath));
                  this.basicInfoMap.put("workPath", Utils.formatPath(currentPath));
                  this.basicInfoMap.put("osInfo", osInfo.replace("winnt", "windows"));
                  this.basicInfoMap.put("arch", arch);
                  this.basicInfoMap.put("localIp", localIp);
                  this.shellManager.updateOsInfo(this.shellEntity.getInt("id"), osInfo);
                  Platform.runLater(new Runnable() {
                     public void run() {
                        webengine.loadContent(basicInfoStr);

                        try {
                           MainWindowController.this.cmdViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.realCmdViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.pluginViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.pluginService, MainWindowController.this.workList, MainWindowController.this.taskList, MainWindowController.this.statusLabel, MainWindowController.this.shellManager, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.fileManagerViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.parallelViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.pluginViewController, MainWindowController.this.pluginService, MainWindowController.this.workList, MainWindowController.this.taskList, MainWindowController.this.statusLabel, MainWindowController.this.shellManager, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.reverseViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.databaseViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.shellManager);
                           MainWindowController.this.tunnelViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.basicInfoMap);
                           MainWindowController.this.updateInfoViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                           MainWindowController.this.userCodeViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel);
                           MainWindowController.this.memoViewController.init(MainWindowController.this.currentShellService, MainWindowController.this.workList, MainWindowController.this.statusLabel, MainWindowController.this.shellManager);
                        } catch (Exception var2) {
                           var2.printStackTrace();
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
               } catch (DecryptException var16) {
                  Platform.runLater(() -> {
                     this.connStatusLabel.setText("连接失败");
                     this.connStatusLabel.setTextFill(Color.RED);
                     this.statusLabel.setText(var16.getMessage());
                     Hyperlink detailLink = new Hyperlink("点击查看响应详情");
                     detailLink.setOnMouseClicked((event) -> {
                        Utils.showErrorMessage("响应内容", var16.getResponseBody());
                     });
                     Separator separator = new Separator();
                     separator.setOrientation(Orientation.VERTICAL);
                     separator.setPrefHeight(20.0);
                     HBox statusContainer = (HBox)((GridPane)this.statusLabel.getParent()).getChildren().get(1);
                     statusContainer.getChildren().add(0, separator);
                     statusContainer.getChildren().add(0, detailLink);
                  });

                  try {
                     this.shellManager.setShellStatus(this.shellEntity.getInt("id"), Constants.SHELL_STATUS_DEAD);
                  } catch (Exception var14) {
                  }
               } catch (Exception var17) {
                  var17.printStackTrace();
                  Platform.runLater(() -> {
                     this.connStatusLabel.setText("连接失败");
                     this.connStatusLabel.setTextFill(Color.RED);
                     this.statusLabel.setText("[ERROR]连接失败：" + var17.getClass().getName() + ":" + var17.getMessage());
                  });

                  try {
                     this.shellManager.setShellStatus(this.shellEntity.getInt("id"), Constants.SHELL_STATUS_DEAD);
                  } catch (Exception var13) {
                  }
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
            switch (newTab.getId()) {
               case "cmdTab":
               case "":
               default:
            }
         }
      });
   }

   private void doConnect() throws Exception {
      boolean connectResult = this.currentShellService.doConnect();
   }

   public void init(JSONObject shellEntity, ShellManager shellManager, Map currentProxy, boolean offline) throws Exception {
      this.shellEntity = shellEntity;
      this.effectShellEntity = shellEntity;
      this.shellManager = shellManager;
      this.offline = offline;
      if (offline) {
         this.currentShellService = new OfflineService(shellEntity);
      } else {
         this.currentShellService = new ShellService(shellEntity);
      }

      this.pluginService = new PluginService(this.currentShellService, shellEntity, this.workList);
      this.currentShellService.setProxy(currentProxy);
      this.urlText.setText(shellEntity.getString("url"));
      this.initTabs();
   }

   public void init(JSONObject shellEntity, BShell bShell, ShellManager shellManager, Map currentProxy, List childList) throws Exception {
      this.shellEntity = shellEntity;
      this.effectShellEntity = (JSONObject)((Map)Utils.getLastOfList(childList)).get("childShellEntity");
      this.shellManager = shellManager;
      this.currentShellService = new ShellService(shellEntity, childList);
      this.pluginService = new PluginService(this.currentShellService, shellEntity, this.workList);
      this.currentShellService.setProxy(currentProxy);
      this.urlText.setText(this.effectShellEntity.getString("url"));
      this.initTabs();
   }

   private void initTabs() {
      Iterator var1;
      Tab tab;
      if (this.effectShellEntity.getString("type").equals("asp")) {
         var1 = this.mainTabPane.getTabs().iterator();

         label82:
         while(true) {
            do {
               if (!var1.hasNext()) {
                  break label82;
               }

               tab = (Tab)var1.next();
            } while(!tab.getId().equals("realCmdTab") && !tab.getId().equals("tunnelTab") && !tab.getId().equals("parallelViewTab") && !tab.getId().equals("reverseTab"));

            tab.setDisable(true);
         }
      } else if (this.effectShellEntity.getString("type").equals("native")) {
         var1 = this.mainTabPane.getTabs().iterator();

         label66:
         while(true) {
            do {
               if (!var1.hasNext()) {
                  break label66;
               }

               tab = (Tab)var1.next();
            } while(!tab.getId().equals("tunnelTab") && !tab.getId().equals("reverseTab") && !tab.getId().equals("parallelViewTab") && !tab.getId().equals("pluginViewTab") && !tab.getId().equals("databaseTab") && !tab.getId().equals("sourceCodeTab"));

            tab.setDisable(true);
         }
      }

      if (this.offline) {
         var1 = this.mainTabPane.getTabs().iterator();

         while(true) {
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

   private void showTaskCenter() {
      Alert inputDialog = Utils.getAlert(AlertType.NONE);
      inputDialog.setTitle("运行中的任务");
      inputDialog.setWidth(800.0);
      inputDialog.setResizable(true);
      Window window = inputDialog.getDialogPane().getScene().getWindow();
      window.setOnCloseRequest((e) -> {
         window.hide();
      });
      VBox taskBox = new VBox();
      taskBox.setPrefWidth(600.0);
      taskBox.setFillWidth(true);
      taskBox.setSpacing(10.0);
      Iterator var4 = this.taskList.iterator();

      while(var4.hasNext()) {
         Task task = (Task)var4.next();
         if (!(task.getProgress() >= 1.0)) {
            VBox taskContainer = new VBox();
            taskContainer.setSpacing(10.0);
            HBox taskItem = new HBox();
            taskItem.setSpacing(20.0);
            Label taskName = new Label("任务名称：" + task.getName());
            StackPane progressPane = new StackPane();
            ProgressBar taskProgress = new ProgressBar(task.getProgress());
            taskProgress.setPrefWidth(300.0);
            Label taskPercent = new Label((int)task.getProgress() * 100 + "%");
            taskPercent.textProperty().bind((new SimpleIntegerProperty((int)task.getProgress() * 100)).asString());
            progressPane.getChildren().addAll(new Node[]{taskProgress, taskPercent});
            Label taskStatus = new Label("");
            taskStatus.setTextFill(Color.RED);
            Label taskDetail = new Label("任务参数：" + task.getParamObj().toString());
            taskDetail.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PAUSE));
            taskStatus.setOnMouseClicked((event) -> {
               Optional buttonType = Utils.showConfirmMessage("提示", "是否确认终止该任务");
               if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                  try {
                     JSONObject stopResonse = this.pluginService.stopTask(task.getName());
                     if (stopResonse.getString("status").equals("success")) {
                        VBox currentTaskBox = (VBox)((Label)event.getSource()).getParent().getParent();
                        this.taskList.remove(task);
                        taskBox.getChildren().remove(currentTaskBox);
                     }
                  } catch (Exception var7) {
                     var7.printStackTrace();
                  }
               }

            });
            taskItem.getChildren().addAll(new Node[]{taskName, progressPane, taskStatus});
            Separator separator = new Separator();
            taskContainer.getChildren().addAll(new Node[]{taskItem, taskDetail, separator});
            taskBox.getChildren().add(taskContainer);
         }
      }

      inputDialog.getDialogPane().setContent(taskBox);
      inputDialog.showAndWait();
   }
}
