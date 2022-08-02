package net.rebeyond.behinder.ui.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.dao.PluginDao;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.entity.AlreadyExistException;
import net.rebeyond.behinder.entity.Host;
import net.rebeyond.behinder.entity.Plugin;
import net.rebeyond.behinder.entity.Service;
import net.rebeyond.behinder.service.PluginService;
import net.rebeyond.behinder.service.PluginTask;
import net.rebeyond.behinder.service.Task;
import net.rebeyond.behinder.service.callback.IPluginCallBack;
import net.rebeyond.behinder.utils.Utils;
import net.rebeyond.behinder.utils.ZipUtil;
import netscape.javascript.JSObject;
import org.json.JSONObject;

public class PluginViewController {
   private ShellManager shellManager;
   @FXML
   private WebView pluginWebView;
   @FXML
   private Hyperlink installLocalBtn;
   @FXML
   private Hyperlink installNetBtn;
   @FXML
   private Accordion pluginFlowPane;
   @FXML
   private GridPane pluginDetailGridPane;
   @FXML
   private Label pluginNameLabel;
   @FXML
   private Label pluginAuthorLabel;
   @FXML
   private Label pluginLinkLabel;
   @FXML
   private Label pluginCommentLabel;
   @FXML
   private ImageView qrcodeImageView;
   private JSONObject shellEntity;
   private JSONObject effectShellEntity;
   private Map basicInfoMap;
   private IShellService currentShellService;
   private PluginService pluginService;
   private List workList;
   private List taskList;
   private Label statusLabel;
   private String PluginBasePath;
   private String currentPluginName;
   private PluginDao pluginDao = new PluginDao();
   private String currentHost;
   private String currentPort;

   public void init(IShellService shellService, PluginService pluginService, List workList, List taskList, Label statusLabel, ShellManager shellManager, Map basicInfoMap) {
      this.currentShellService = shellService;
      this.basicInfoMap = basicInfoMap;
      this.pluginService = pluginService;
      this.shellEntity = shellService.getShellEntity();
      this.effectShellEntity = shellService.getEffectShellEntity();
      this.workList = workList;
      this.taskList = taskList;
      this.statusLabel = statusLabel;
      this.shellManager = shellManager;
      this.initPluginView();
   }

   private void initPluginView() {
      this.initPluginInstall();
      WebEngine webEngine = this.pluginWebView.getEngine();
      webEngine.documentProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue != null) {
            JSObject win = (JSObject)webEngine.executeScript("window");
            win.setMember("PluginTools", new PluginViewController.PluginHelper());
            if (this.currentHost != null && !this.currentHost.equals("")) {
               webEngine.executeScript(String.format("$('#host').val('%s');", this.currentHost));
               win.call("onSetHost", new Object[]{this.currentHost});
            }

            if (this.currentPort != null && !this.currentPort.equals("")) {
               webEngine.executeScript(String.format("$('#port').val('%s');", this.currentPort));
               win.call("onSetPort", new Object[]{this.currentPort});
            }

         }
      });

      try {
         this.PluginBasePath = Utils.getSelfPath("UTF-8") + "/Plugins/";
         this.loadPlugins();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      this.pluginDetailGridPane.setOpacity(0.0D);
   }

   private void loadPluginPage(Plugin plugin) {
      String entryFilePath = this.expandPluginPath(plugin.getName(), plugin.getEntryFile());
      entryFilePath = (new File(entryFilePath)).toURI().toString();
      this.pluginWebView.getEngine().load(entryFilePath);
      this.pluginDetailGridPane.setOpacity(1.0D);
      this.statusLabel.setText("插件加载完成。");
   }

   private void loadPluginMetaInfo(Plugin plugin) {
      this.pluginNameLabel.setText(String.format(this.pluginNameLabel.getText(), plugin.getName(), plugin.getVersion()));
      this.pluginAuthorLabel.setText(String.format(this.pluginAuthorLabel.getText(), plugin.getAuthor()));
      this.pluginLinkLabel.setText(String.format(this.pluginLinkLabel.getText(), plugin.getLink()));
      this.pluginCommentLabel.setText(String.format(this.pluginCommentLabel.getText(), plugin.getComment()));

      try {
         String qrcodeFilePath = this.expandPluginPath(plugin.getName(), plugin.getQrcode());
         qrcodeFilePath = (new File(qrcodeFilePath)).toURI().toString();
         this.qrcodeImageView.setImage(new Image(qrcodeFilePath));
      } catch (Exception var3) {
         this.statusLabel.setText("插件开发者赞赏二维码加载失败");
         var3.printStackTrace();
      }

   }

   private void loadPlugins() throws Exception {
      String scriptType = this.effectShellEntity.getString("type");
      List pluginList = this.pluginDao.findPluginByScriptType(scriptType);
      Iterator var3 = pluginList.iterator();

      while(var3.hasNext()) {
         Plugin plugin = (Plugin)var3.next();
         this.addPluginBox(plugin);
      }

   }

   public void loadPluginFromHost(Plugin plugin, Host host) {
      this.currentHost = host.getIp();
      int type = plugin.getType();
      ((TitledPane)this.pluginFlowPane.getPanes().get(type)).setExpanded(true);
      this.loadPlugin(plugin);
   }

   public void loadPluginFromService(Plugin plugin, Service service) {
      this.currentPort = service.getPort() + "";
      int type = plugin.getType();
      ((TitledPane)this.pluginFlowPane.getPanes().get(type)).setExpanded(true);
      this.loadPlugin(plugin);
   }

   private boolean checkPluginExist(JSONObject pluginObj) throws Exception {
      String pluginName = pluginObj.getString("name");
      String scriptType = pluginObj.getString("scriptType");
      return this.shellManager.findPluginByName(scriptType, pluginName) != null;
   }

   private void loadPlugin(Plugin plugin) {
      this.statusLabel.setText("正在加载插件……");
      this.loadPluginPage(plugin);
      this.loadPluginMetaInfo(plugin);
   }

   private String expandPluginPath(String pluginName, String shortPath) {
      this.currentPluginName = pluginName;
      String finalPath = "";

      try {
         String fullPath = String.format(this.PluginBasePath + "%s/%s", pluginName, shortPath);
         finalPath = (new File(fullPath)).getCanonicalFile().getAbsolutePath();
      } catch (Exception var5) {
      }

      return finalPath;
   }

   private void addPluginBox(Plugin plugin) throws Exception {
      String pluginName = plugin.getName();
      String pluginCommnet = plugin.getComment();
      this.expandPluginPath(pluginName, plugin.getEntryFile());
      String iconFilePath = this.expandPluginPath(pluginName, plugin.getIcon());
      iconFilePath = (new File(iconFilePath)).toURI().toString();
      int type = plugin.getType();
      FlowPane flowPane = (FlowPane)((AnchorPane)((TitledPane)this.pluginFlowPane.getPanes().get(type)).getContent()).getChildren().get(0);
      flowPane.setHgap(20.0D);
      flowPane.setVgap(10.0D);
      VBox box = new VBox();
      ImageView pluginIcon = new ImageView(new Image(iconFilePath));
      pluginIcon.setFitHeight(30.0D);
      pluginIcon.setPreserveRatio(true);
      Label pluginLabel = new Label(pluginName);
      box.getChildren().add(pluginIcon);
      box.getChildren().add(pluginLabel);
      box.setPadding(new Insets(5.0D));
      box.setAlignment(Pos.CENTER);
      box.setSpacing(10.0D);
      Tooltip tip = new Tooltip();
      tip.setText(pluginCommnet);
      Tooltip.install(box, tip);
      box.setOnMouseClicked((e) -> {
         try {
            if (pluginLabel.getText().indexOf("HTTP") >= 0) {
               this.pluginWebView.getEngine().load("file://D:\\工作区\\workspace\\IdeaProjects\\Behinder\\Plugins\\HTTP助手\\index.htm");
            } else if (pluginLabel.getText().indexOf("SSH") >= 0) {
               this.pluginWebView.getEngine().load("file://D:\\工作区\\workspace\\IdeaProjects\\Behinder\\Plugins\\SSH助手\\index.htm");
            } else if (pluginLabel.getText().indexOf("端口") >= 0) {
               this.pluginWebView.getEngine().load("file://D:\\工作区\\workspace\\IdeaProjects\\Behinder\\Plugins\\端口扫描\\Scan.htm");
            }

            this.loadPlugin(plugin);
         } catch (Exception var5) {
         }

      });
      box.hoverProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue) {
            box.setStyle("-fx-background-color:green");
         } else {
            box.setStyle("-fx-background-color:transparent");
         }

      });
      flowPane.getChildren().add(box);
      ((TitledPane)this.pluginFlowPane.getPanes().get(type)).setExpanded(true);
   }

   private void initPluginInstall() {
      this.installLocalBtn.setOnAction((event) -> {
         FileChooser fileChooser = new FileChooser();
         fileChooser.setTitle("请选择需要安装的插件包");
         fileChooser.getExtensionFilters().addAll(new ExtensionFilter[]{new ExtensionFilter("All ZIP Files", new String[]{"*.zip"})});
         File pluginFile = fileChooser.showOpenDialog(this.pluginFlowPane.getScene().getWindow());
         if (pluginFile != null) {
            try {
               Plugin plugin = this.parsePluginZip(pluginFile.getAbsolutePath());

               try {
                  this.pluginDao.addEntity(plugin);
                  this.addPluginBox(plugin);
               } catch (AlreadyExistException var6) {
                  Utils.showErrorMessage("错误", "安装失败，插件已存在");
                  return;
               }

               this.statusLabel.setText("插件安装成功。");
            } catch (Exception var7) {
               var7.printStackTrace();
               this.statusLabel.setText("插件安装失败:" + var7.getMessage());
            }

         }
      });
      this.installNetBtn.setOnAction((event) -> {
         this.pluginWebView.getEngine().load("https://www.rebeyond.net/Behinder/plugins.html");
      });
   }

   private Plugin parsePluginZip(String zipFilePath) throws Exception {
      String pluginRootPath = Utils.getSelfPath() + "/Plugins";
      String pluginName = "";
      ZipFile zf = new ZipFile(zipFilePath);
      InputStream in = new BufferedInputStream(new FileInputStream(zipFilePath));
      ZipInputStream zin = new ZipInputStream(in);

      ZipEntry ze;
      while((ze = zin.getNextEntry()) != null) {
         if (ze.getName().equals("plugin.config")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
            Properties pluginConfig = new Properties();
            pluginConfig.load(br);
            pluginName = pluginConfig.getProperty("name");
            br.close();
         }
      }

      zin.closeEntry();
      String pluginPath = pluginRootPath + "/" + pluginName;
      ZipUtil.unZipFiles(zipFilePath, pluginPath);
      FileInputStream fis = new FileInputStream(pluginPath + "/plugin.config");
      Properties pluginConfig = new Properties();
      pluginConfig.load(fis);
      Plugin plugin = new Plugin();
      plugin.setName(pluginName);
      plugin.setVersion(pluginConfig.getProperty("version", "v1.0"));
      plugin.setEntryFile(pluginConfig.getProperty("entry", "index.htm"));
      plugin.setIcon(pluginConfig.getProperty("icon", "/Users/rebeyond/host.png"));
      plugin.setScriptType(pluginConfig.getProperty("scriptType"));
      plugin.setType(Integer.parseInt(pluginConfig.getProperty("type")));
      plugin.setAuthor(pluginConfig.getProperty("author"));
      plugin.setLink(pluginConfig.getProperty("link"));
      plugin.setQrcode(pluginConfig.getProperty("qrcode"));
      plugin.setComment(pluginConfig.getProperty("comment"));
      return plugin;
   }

   public class PluginHelper {
      private JSObject pluginWindow;

      public PluginHelper() {
         this.pluginWindow = (JSObject)PluginViewController.this.pluginWebView.getEngine().executeScript("window");
      }

      public JSONObject uploadFile(String remotePath, String localPath) {
         String fullLocalPath = PluginViewController.this.expandPluginPath(PluginViewController.this.currentPluginName, "file" + File.separator + localPath);
         JSONObject responseObj = null;

         try {
            byte[] content = Utils.getFileData(fullLocalPath);
            responseObj = PluginViewController.this.currentShellService.uploadFile(remotePath, content, true);
         } catch (Exception var6) {
            var6.printStackTrace();
         }

         return responseObj;
      }

      public JSONObject loadJar(String remotePath) {
         JSONObject responseObj = null;

         try {
            responseObj = PluginViewController.this.currentShellService.loadJar(remotePath);
         } catch (Exception var4) {
            var4.printStackTrace();
         }

         return responseObj;
      }

      public String getCurrentDecryptClsBytes() throws Exception {
         return Base64.getEncoder().encodeToString(PluginViewController.this.currentShellService.getCryptor().getDecodeClsBytes());
      }

      public void sendTask(String paramStr) throws Exception {
         String pluginName = PluginViewController.this.currentPluginName;
         Task pluginTask = new PluginTask(pluginName, new JSONObject(paramStr));
         IPluginCallBack callBack = new IPluginCallBack() {
            public void onSuccess(String status, String message) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText(message);
                  PluginHelper.this.pluginWindow.call("onResult", new Object[]{Base64.getEncoder().encodeToString(URLEncoder.encode(message).getBytes())});
               });
            }

            public void onFail(String message) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText("插件运行失败:" + message);
                  PluginHelper.this.pluginWindow.call("onResult", new Object[]{Base64.getEncoder().encodeToString(URLEncoder.encode(message).getBytes())});
               });
            }
         };
         PluginViewController.this.pluginService.sendTask(pluginName, paramStr, callBack);
         PluginViewController.this.taskList.add(pluginTask);
      }

      public String execTask(String paramStr) throws Exception {
         String pluginName = PluginViewController.this.currentPluginName;
         PluginViewController.this.statusLabel.setText("正在执行插件：" + pluginName + "...");
         JSONObject resObj = PluginViewController.this.pluginService.execTask(pluginName, paramStr);
         String status = resObj.getString("status");
         String msg = resObj.getString("msg");
         if (status.equals("success")) {
            PluginViewController.this.statusLabel.setText("插件执行成功。");
            JSONObject taskResultObj = Utils.DecodeJsonObj(new JSONObject(msg));
            return taskResultObj.getString("result");
         } else {
            PluginViewController.this.statusLabel.setText("插件运行失败:" + msg);
            return null;
         }
      }

      public boolean checkClassExist(String className) throws Exception {
         return PluginViewController.this.currentShellService.checkClassExist(className);
      }

      public void sendHTTP(String host, String port, String body) throws Exception {
         PluginViewController.this.statusLabel.setText(String.format("正在发送请求至%s:%s...", host, port));
         IPluginCallBack callBack = new IPluginCallBack() {
            public void onSuccess(String status, String message) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText(message);

                  try {
                     PluginViewController.this.statusLabel.setText("请求已完成。");
                     PluginViewController.this.pluginWebView.getEngine().executeScript(String.format("onResult('%s')", message));
                  } catch (Exception var3) {
                     PluginViewController.this.statusLabel.setText("结果刷新成功，但是插件解析结果失败，请检查插件:" + var3.getMessage());
                  }

               });
            }

            public void onFail(String message) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText("插件运行失败:" + message);
               });
            }
         };
         Thread runner = new Thread(() -> {
            try {
               JSONObject resObj = PluginViewController.this.currentShellService.doProxy("TCP", host + ":" + port, Base64.getEncoder().encodeToString(body.replace("\n", "\r\n").getBytes()));
               String status = resObj.getString("status");
               String msg = resObj.getString("msg");
               if (status.equals("success")) {
                  callBack.onSuccess(status, msg);
               } else {
                  callBack.onFail(msg);
               }
            } catch (Exception var8) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText("请求失败:" + var8.getMessage());
               });
               var8.printStackTrace();
            }

         });
         runner.start();
         PluginViewController.this.workList.add(runner);
      }

      public void sendHTTPS(String host, String port, String body) throws Exception {
         PluginViewController.this.statusLabel.setText(String.format("正在发送请求至%s:%s...", host, port));
         IPluginCallBack callBack = new IPluginCallBack() {
            public void onSuccess(String status, String message) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText(message);

                  try {
                     PluginViewController.this.statusLabel.setText("请求已完成。");
                     PluginViewController.this.pluginWebView.getEngine().executeScript(String.format("onResult('%s')", message));
                  } catch (Exception var3) {
                     PluginViewController.this.statusLabel.setText("结果刷新成功，但是插件解析结果失败，请检查插件:" + var3.getMessage());
                  }

               });
            }

            public void onFail(String message) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText("插件运行失败:" + message);
               });
            }
         };
         Thread runner = new Thread(() -> {
            try {
               JSONObject resObj = PluginViewController.this.currentShellService.doProxy("SSL", host + ":" + port, Base64.getEncoder().encodeToString(body.replace("\n", "\r\n").getBytes()));
               String status = resObj.getString("status");
               String msg = resObj.getString("msg");
               if (status.equals("success")) {
                  callBack.onSuccess(status, msg);
               } else {
                  callBack.onFail(msg);
               }
            } catch (Exception var8) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText("请求失败:" + var8.getMessage());
               });
               var8.printStackTrace();
            }

         });
         runner.start();
         PluginViewController.this.workList.add(runner);
      }

      public String getTaskResultSync() throws Exception {
         String pluginName = PluginViewController.this.currentPluginName;
         JSONObject responseObj = PluginViewController.this.pluginService.getTaskResultSync(pluginName);
         return responseObj.toString();
      }

      public void getTaskResult() {
         String pluginName = PluginViewController.this.currentPluginName;
         IPluginCallBack callBack = new IPluginCallBack() {
            public void onSuccess(String status, String message) {
               JSONObject msgObj = new JSONObject(message);

               try {
                  String pluginResult = new String(Base64.getDecoder().decode(msgObj.getString("result")), "UTF-8");
                  String pluginRunning = new String(Base64.getDecoder().decode(msgObj.getString("running")), "UTF-8");
                  Platform.runLater(() -> {
                     if (status.equals("success")) {
                        PluginViewController.this.statusLabel.setText("结果刷新成功");

                        try {
                           PluginViewController.this.pluginWebView.getEngine().executeScript(String.format("onResult('%s','%s','%s')", status, pluginResult, pluginRunning));
                        } catch (Exception var5) {
                           PluginViewController.this.statusLabel.setText("结果刷新成功，但是插件解析结果失败，请检查插件:" + var5.getMessage());
                        }
                     } else {
                        PluginViewController.this.statusLabel.setText("结果刷新失败");
                     }

                  });
               } catch (UnsupportedEncodingException var6) {
                  var6.printStackTrace();
               }

            }

            public void onFail(String message) {
               Platform.runLater(() -> {
                  PluginViewController.this.statusLabel.setText("插件运行失败:" + message);
               });
            }
         };
         PluginViewController.this.pluginService.getTaskResult(pluginName, callBack);
      }
   }
}
