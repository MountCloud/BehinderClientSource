package net.rebeyond.behinder.ui.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseViewController {
   private ShellManager shellManager;
   @FXML
   private GridPane databaseGridPane;
   @FXML
   private ComboBox databaseTypeCombo;
   @FXML
   private ComboBox connStrCombo;
   @FXML
   private TextArea sqlText;
   @FXML
   private TreeView schemaTree;
   @FXML
   private TableView dataTable;
   @FXML
   private Button connectBtn;
   @FXML
   private Button executeSqlBtn;
   private IShellService currentShellService;
   private JSONObject shellEntity;
   private JSONObject effectShellEntity;
   private List workList;
   private Label statusLabel;
   private int shellId;

   public void init(IShellService shellService, List workList, Label statusLabel, ShellManager shellManager) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.shellId = this.shellEntity.getInt("id");
      this.effectShellEntity = shellService.getEffectShellEntity();
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.shellManager = shellManager;
      this.initDatabaseView();
   }

   private void initDatabaseView() {
      this.initConnStrCombo();
      this.schemaTree.setOnMouseClicked((event) -> {
         TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
         if (currentTreeItem != null) {
            if (currentTreeItem.isExpanded()) {
               currentTreeItem.setExpanded(false);
            } else if (event.getButton() == MouseButton.PRIMARY && !currentTreeItem.isExpanded()) {
               if (currentTreeItem.getGraphic().getUserData().toString().equals("database")) {
                  try {
                     this.showTables(currentTreeItem);
                  } catch (Exception var5) {
                     var5.printStackTrace();
                  }
               } else if (currentTreeItem.getGraphic().getUserData().toString().equals("table")) {
                  try {
                     this.showColumns(currentTreeItem);
                  } catch (Exception var4) {
                     var4.printStackTrace();
                  }
               }
            }

         }
      });
      this.executeSqlBtn.setOnAction((event) -> {
         try {
            Map connParams = this.parseConnURI(this.connStrCombo.getValue().toString());
            Runnable runner = () -> {
               try {
                  String resultText = this.executeSQL(connParams, this.sqlText.getText());
                  Platform.runLater(() -> {
                     try {
                        this.fillTable(resultText);
                        this.statusLabel.setText("SQL执行成功。");
                     } catch (Exception var3) {
                        this.statusLabel.setText("SQL执行失败:" + var3.getMessage());
                     }

                  });
               } catch (Exception var3) {
                  Platform.runLater(() -> {
                     this.statusLabel.setText("SQL执行失败:" + var3.getMessage());
                  });
               }

            };
            Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
         } catch (Exception var5) {
            this.statusLabel.setText(var5.getMessage());
         }

      });
      this.initDatabaseType();
      this.loadContextMenu();
   }

   private void loadContextMenu() {
      this.loadTreeContextMenu();
      this.loadTableContextMenu();
   }

   private void initConnStrCombo() {
      this.connStrCombo = new ComboBox();
      this.connStrCombo.setEditable(true);
      this.connStrCombo.setPromptText("连接密码中如有特殊字符可将密码进行URL编码");
      this.connStrCombo.setMaxWidth(Double.MAX_VALUE);

      try {
         JSONObject shell = this.shellManager.findShell(this.shellId);
         if (shell.has("config")) {
            JSONObject configObj = new JSONObject(shell.getString("config"));
            if (configObj.has("dbConfig")) {
               JSONArray dbConfigArr = configObj.getJSONArray("dbConfig");
               this.connStrCombo.getItems().addAll(dbConfigArr.toList());

               for(int i = 0; i < dbConfigArr.length(); ++i) {
               }
            }
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }

      Class skinCls = null;

      try {
         skinCls = Class.forName("javafx.scene.control.skin.ComboBoxListViewSkin");
      } catch (UnsupportedClassVersionError | ClassNotFoundException var10) {
         byte[] skinClsBytes = Base64.getDecoder().decode("yv66vgAAADQAFAoAAwARBwASBwATAQAGPGluaXQ+AQAiKExqYXZhZngvc2NlbmUvY29udHJvbC9Db21ib0JveDspVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAaTEN1c3RDb21ib0JveExpc3RWaWV3U2tpbjsBAAhjb21ib0JveAEAH0xqYXZhZngvc2NlbmUvY29udHJvbC9Db21ib0JveDsBABRpc0hpZGVPbkNsaWNrRW5hYmxlZAEAAygpWgEAClNvdXJjZUZpbGUBAB1DdXN0Q29tYm9Cb3hMaXN0Vmlld1NraW4uamF2YQwABAAFAQAYQ3VzdENvbWJvQm94TGlzdFZpZXdTa2luAQA2Y29tL3N1bi9qYXZhZngvc2NlbmUvY29udHJvbC9za2luL0NvbWJvQm94TGlzdFZpZXdTa2luACEAAgADAAAAAAACAAEABAAFAAEABgAAAD4AAgACAAAABiortwABsQAAAAIABwAAAAoAAgAAAAwABQANAAgAAAAWAAIAAAAGAAkACgAAAAAABgALAAwAAQAEAA0ADgABAAYAAAAsAAEAAQAAAAIDrAAAAAIABwAAAAYAAQAAABEACAAAAAwAAQAAAAIACQAKAAAAAQAPAAAAAgAQ");

         try {
            ClassLoader loader = this.getClass().getClassLoader();
            Method defineMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, ByteBuffer.class, ProtectionDomain.class);
            defineMethod.setAccessible(true);
            Constructor constructor = SecureClassLoader.class.getDeclaredConstructor(ClassLoader.class);
            constructor.setAccessible(true);
            ClassLoader cl = (ClassLoader)constructor.newInstance(loader);
            skinCls = (Class)defineMethod.invoke(cl, null, ByteBuffer.wrap(skinClsBytes), null);
         } catch (Throwable var9) {
            var9.printStackTrace();
         }
      }

      if (skinCls != null) {
         try {
            Object skinObj = skinCls.getDeclaredConstructor(ComboBox.class).newInstance(this.connStrCombo);
            this.connStrCombo.setSkin((Skin)skinObj);
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      }

      this.connStrCombo.setCellFactory((lv) -> {
         return new ListCell() {
            private HBox graphic;

            {
               Label label = new Label();
               label.textProperty().bind(this.itemProperty());
               label.setMaxWidth(Double.POSITIVE_INFINITY);
               label.setOnMouseClicked((event) -> {
                  DatabaseViewController.this.connStrCombo.hide();
                  String value = label.getText();
                  DatabaseViewController.this.connStrCombo.getEditor().setText(value);
               });
               Hyperlink cross = new Hyperlink("X");
               cross.setVisited(true);
               cross.setOnAction((event) -> {
                  String item = (String)this.getItem();
                  if (this.isSelected()) {
                  }

                  DatabaseViewController.this.removeConnStr(item);
               });
               this.graphic = new HBox(new Node[]{label, cross});
               HBox var10000 = this.graphic;
               HBox.setHgrow(label, Priority.ALWAYS);
               this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            protected void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               if (empty) {
                  this.setGraphic((Node)null);
               } else {
                  this.setGraphic(this.graphic);
               }

            }
         };
      });
      this.databaseGridPane.add(this.connStrCombo, 3, 0);
   }

   private boolean addDBConfig(String dbConfig) throws Exception {
      JSONObject shell = this.shellManager.findShell(this.shellId);
      JSONObject configObj;
      if (!shell.has("config")) {
         configObj = new JSONObject();
      } else {
         String config = shell.getString("config");
         configObj = new JSONObject(config);
      }

      if (configObj.has("dbConfig")) {
         configObj.getJSONArray("dbConfig").put(dbConfig);
      } else {
         JSONArray dbConfigArr = new JSONArray();
         dbConfigArr.put(dbConfig);
         configObj.put("dbConfig", dbConfigArr);
      }

      return this.shellManager.updateConfig(this.shellId, configObj.toString()) > 0;
   }

   private void delDBConfig(String dbConfig) throws Exception {
      int shellId = this.shellEntity.getInt("id");
      JSONObject shell = this.shellManager.findShell(shellId);
      if (shell.has("config")) {
         String config = shell.getString("config");
         JSONObject configObj = new JSONObject(config);
         if (configObj.has("dbConfig")) {
            JSONArray dbConfigArr = configObj.getJSONArray("dbConfig");

            for(int i = 0; i < dbConfigArr.length(); ++i) {
               if (dbConfigArr.getString(i).equals(dbConfig)) {
                  dbConfigArr.remove(i);
                  this.shellManager.updateConfig(shellId, configObj.toString());
                  return;
               }
            }
         }

      }
   }

   private void loadTreeContextMenu() {
      ContextMenu treeContextMenu = new ContextMenu();
      MenuItem queryHeadBtn = new MenuItem("查询前10条");
      treeContextMenu.getItems().add(queryHeadBtn);
      MenuItem queryAllBtn = new MenuItem("查询全部");
      treeContextMenu.getItems().add(queryAllBtn);
      MenuItem exportBtn = new MenuItem("导出当前表");
      treeContextMenu.getItems().add(exportBtn);
      queryHeadBtn.setOnAction((event) -> {
         TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
         String tableName = currentTreeItem.getValue().toString();
         String dataBaseName = currentTreeItem.getParent().getValue().toString();
         Runnable runner = () -> {
            try {
               Map connParams = this.parseConnURI(this.connStrCombo.getValue().toString());
               String databaseType = (String)connParams.get("type");
               String sql = null;
               if (databaseType.equals("mysql")) {
                  sql = String.format("select * from %s.%s limit 10", dataBaseName, tableName);
               } else if (databaseType.equals("sqlserver")) {
                  sql = String.format("select top 10 * from %s..%s", dataBaseName, tableName);
               } else if (databaseType.equals("oracle")) {
                  sql = String.format("select * from %s where rownum<=10", tableName);
               }

               String resultText = this.executeSQL(connParams, sql);
               Platform.runLater(() -> {
                  try {
                     this.fillTable(resultText);
                  } catch (Exception var3) {
                     this.statusLabel.setText(var3.getMessage());
                  }

               });
            } catch (Exception var7) {
               Platform.runLater(() -> {
                  this.statusLabel.setText(var7.getMessage());
               });
            }

         };
         Thread worker = new Thread(runner);
         this.workList.add(worker);
         worker.start();
      });
      queryAllBtn.setOnAction((event) -> {
         TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
         String tableName = currentTreeItem.getValue().toString();
         String dataBaseName = currentTreeItem.getParent().getValue().toString();
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("确认");
         alert.setHeaderText("");
         alert.setContentText("查询所有记录可能耗时较长，确定查询所有记录？");
         Optional result = alert.showAndWait();
         if (result.get() == ButtonType.OK) {
            Runnable runner = () -> {
               try {
                  Map connParams = this.parseConnURI(this.connStrCombo.getValue().toString());
                  String databaseType = (String)connParams.get("type");
                  String sql = null;
                  if (databaseType.equals("mysql")) {
                     sql = String.format("select * from %s.%s", dataBaseName, tableName);
                  } else if (databaseType.equals("sqlserver")) {
                     sql = String.format("select * from %s..%s", dataBaseName, tableName);
                  } else if (databaseType.equals("oracle")) {
                     sql = String.format("select * from %s", tableName);
                  }

                  String resultText = this.executeSQL(connParams, sql);
                  Platform.runLater(() -> {
                     try {
                        this.fillTable(resultText);
                     } catch (Exception var3) {
                        this.statusLabel.setText(var3.getMessage());
                     }

                  });
               } catch (Exception var7) {
                  Platform.runLater(() -> {
                     this.statusLabel.setText(var7.getMessage());
                  });
               }

            };
            Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
         }

      });
      exportBtn.setOnAction((event) -> {
         TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
         String tableName = currentTreeItem.getValue().toString();
         String dataBaseName = currentTreeItem.getParent().getValue().toString();
         FileChooser fileChooser = new FileChooser();
         fileChooser.setTitle("请选择保存路径");
         fileChooser.setInitialFileName("export_table.csv");
         File selectedFile = fileChooser.showSaveDialog(this.schemaTree.getScene().getWindow());
         String selected = selectedFile.getAbsolutePath();
         if (selected != null && !selected.equals("")) {
            Runnable runner = () -> {
               try {
                  Map connParams = this.parseConnURI(this.connStrCombo.getValue().toString());
                  String databaseType = (String)connParams.get("type");
                  String sql = null;
                  if (databaseType.equals("mysql")) {
                     sql = String.format("select * from %s.%s", dataBaseName, tableName);
                  } else if (databaseType.equals("sqlserver")) {
                     sql = String.format("select * from %s..%s", dataBaseName, tableName);
                  } else if (databaseType.equals("oracle")) {
                     sql = String.format("select * from %s", tableName);
                  }

                  String resultText = this.executeSQL(connParams, sql);
                  StringBuilder rows = new StringBuilder();
                  JSONArray arr = new JSONArray(resultText);
                  String colsLine = "";
                  JSONArray cols = arr.getJSONArray(0);

                  int i;
                  for(i = 0; i < cols.length(); ++i) {
                     JSONObject colObj = cols.getJSONObject(i);
                     colsLine = colsLine + colObj.getString("name") + ",";
                  }

                  rows.append(colsLine + "\n");

                  for(i = 1; i < arr.length(); ++i) {
                     JSONArray cells = arr.getJSONArray(i);

                     for(int j = 0; j < cells.length(); ++j) {
                        rows.append(cells.get(j) + ",");
                     }

                     rows.append("\n");
                  }

                  FileOutputStream fso = new FileOutputStream(selected);
                  fso.write(rows.toString().getBytes());
                  fso.flush();
                  fso.close();
                  Platform.runLater(() -> {
                     this.statusLabel.setText("导出完成，文件已保存至" + selected);
                  });
               } catch (Exception var15) {
                  Platform.runLater(() -> {
                     this.statusLabel.setText(var15.getMessage());
                  });
                  var15.printStackTrace();
               }

            };
            Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
         }
      });
      this.schemaTree.setOnContextMenuRequested((event) -> {
         TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
         if (currentTreeItem.getGraphic().getUserData().toString().equals("table")) {
            treeContextMenu.show(this.schemaTree.getScene().getWindow(), event.getScreenX(), event.getScreenY());
         }

      });
   }

   private void loadTableContextMenu() {
      ContextMenu tableContextMenu = new ContextMenu();
      MenuItem copyCellBtn = new MenuItem("复制单元格");
      tableContextMenu.getItems().add(copyCellBtn);
      MenuItem copyRowBtn = new MenuItem("复制整行");
      tableContextMenu.getItems().add(copyRowBtn);
      MenuItem exportBtn = new MenuItem("导出全部查询结果");
      tableContextMenu.getItems().add(exportBtn);
      copyCellBtn.setOnAction((event) -> {
         TablePosition position = (TablePosition)this.dataTable.getSelectionModel().getSelectedCells().get(0);
         int row = position.getRow();
         int column = position.getColumn();
         String selectedValue = "";
         if (this.dataTable.getItems().size() > row && ((List)this.dataTable.getItems().get(row)).size() > column) {
            selectedValue = ((StringProperty)((List)this.dataTable.getItems().get(row)).get(column)).getValue();
            Utils.setClipboardString(selectedValue);
         }

      });
      copyRowBtn.setOnAction((event) -> {
         TablePosition position = (TablePosition)this.dataTable.getSelectionModel().getSelectedCells().get(0);
         int row = position.getRow();
         int column = position.getColumn();
         String selectedValue = "";
         int rowSize = this.dataTable.getItems().size();
         int columnSize = ((List)this.dataTable.getItems().get(row)).size();
         if (rowSize > row && columnSize > column) {
            String lineContent = "";

            for(int i = 0; i < columnSize; ++i) {
               selectedValue = ((StringProperty)((List)this.dataTable.getItems().get(row)).get(i)).getValue();
               lineContent = lineContent + selectedValue + "|";
            }

            Utils.setClipboardString(lineContent);
         }

      });
      exportBtn.setOnAction((event) -> {
         FileChooser fileChooser = new FileChooser();
         fileChooser.setTitle("请选择保存路径");
         fileChooser.setInitialFileName("export_table.csv");
         File selectedFile = fileChooser.showSaveDialog(this.schemaTree.getScene().getWindow());
         String selected = selectedFile.getAbsolutePath();
         if (selected != null && !selected.equals("")) {
            int rowSize = this.dataTable.getItems().size();
            int columnSize = ((List)this.dataTable.getItems().get(0)).size();
            this.statusLabel.setText("正在准备数据……");
            Runnable runner = () -> {
               StringBuilder sb = new StringBuilder();

               int i;
               for(i = 0; i < columnSize; ++i) {
                  TableColumn col = (TableColumn)this.dataTable.getColumns().get(i);
                  sb.append(col.getText() + ",");
               }

               sb.append("\n");

               for(i = 0; i < rowSize; ++i) {
                  for(int j = 0; j < columnSize; ++j) {
                     String cellString = ((StringProperty)((List)this.dataTable.getItems().get(i)).get(j)).getValue();
                     sb.append(cellString + ",");
                  }

                  sb.append("\n");
               }

               Platform.runLater(() -> {
                  this.statusLabel.setText("正在写入文件……" + selected);
               });

               try {
                  FileOutputStream fso = new FileOutputStream(selected);
                  fso.write(sb.toString().getBytes());
                  fso.flush();
                  fso.close();
                  Platform.runLater(() -> {
                     this.statusLabel.setText("导出完成，文件已保存至" + selected);
                  });
               } catch (Exception var8) {
                  var8.printStackTrace();
                  Platform.runLater(() -> {
                     this.statusLabel.setText("导出失败:" + var8.getMessage());
                  });
               }

            };
            Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
         }
      });
      this.dataTable.setContextMenu(tableContextMenu);
   }

   private void initDatabaseType() {
      ObservableList typeList = FXCollections.observableArrayList(new String[]{"MySQL", "SQLServer", "Oracle"});
      this.databaseTypeCombo.setItems(typeList);
      this.databaseTypeCombo.setOnAction((event) -> {
         String type = this.databaseTypeCombo.getValue().toString();
         String connStr = this.formatConnectString(type);
         this.connStrCombo.setValue(connStr);
      });
      this.connectBtn.setOnAction((event) -> {
         try {
            String currentConnStr = this.connStrCombo.getValue().toString();
            this.saveConnStr(currentConnStr);
            this.showDatabases(currentConnStr);
         } catch (Exception var3) {
            var3.printStackTrace();
            Utils.showErrorMessage("错误", "连接失败，请检查连接字符串");
            this.statusLabel.setText("连接失败:" + var3.getMessage());
         }

      });
   }

   private void saveConnStr(String connStr) {
      if (!this.connStrCombo.getItems().contains(connStr)) {
         this.connStrCombo.getItems().add(connStr);

         try {
            this.addDBConfig(connStr);
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }

   }

   private void removeConnStr(String connStr) {
      if (this.connStrCombo.getItems().contains(connStr)) {
         this.connStrCombo.getItems().remove(connStr);
      }

      try {
         this.delDBConfig(connStr);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   private String formatConnectString(String type) {
      String result = "%s://%s:password@127.0.0.1:%s/%s";
      switch (type) {
         case "MySQL":
            result = String.format(result, "mysql", "root", "3306", "mysql");
            break;
         case "SQLServer":
            result = String.format(result, "sqlserver", "sa", "1433", "master");
            break;
         case "Oracle":
            result = String.format(result, "oracle", "sys", "1521", "orcl");
      }

      return result;
   }

   private void showTables(TreeItem currentTreeItem) throws Exception {
      Map connParams = this.parseConnURI(this.connStrCombo.getValue().toString());
      String sql = null;
      String databaseName = currentTreeItem.getValue().toString();
      String databaseType = (String)connParams.get("type");
      if (databaseType.equals("mysql")) {
         sql = String.format("select table_name,a.* from information_schema.tables as a where table_schema='%s' and table_type='base table'", databaseName);
      } else if (databaseType.equals("sqlserver")) {
         sql = String.format("select name,* from %s..sysobjects  where xtype='U'", databaseName);
      } else if (databaseType.equals("oracle")) {
         sql = "select table_name,num_rows from user_tables";
      }

      final String finalsql = sql;
      Runnable runner = () -> {
         try {
            String resultText = this.executeSQL(connParams, finalsql);
            Platform.runLater(() -> {
               try {
                  this.fillTable(resultText);
                  this.fillTree(resultText, currentTreeItem);
               } catch (Exception var4) {
                  this.statusLabel.setText(var4.getMessage());
               }

            });
         } catch (Exception var5) {
            Platform.runLater(() -> {
               this.statusLabel.setText(var5.getMessage());
            });
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private void showColumns(TreeItem currentTreeItem) {
      try {
         String tableName = currentTreeItem.getValue().toString();
         String databaseName = currentTreeItem.getParent().getValue().toString();
         Map connParams = this.parseConnURI(this.connStrCombo.getValue().toString());
         String sql = null;
         String databaseType = (String)connParams.get("type");
         if (databaseType.equals("mysql")) {
            sql = String.format("select COLUMN_NAME,a.* from information_schema.columns as a where table_schema='%s' and table_name='%s'", databaseName, tableName);
         } else if (databaseType.equals("sqlserver")) {
            sql = String.format("SELECT Name,* FROM %s..SysColumns WHERE id=Object_Id('%s')", databaseName, tableName);
         } else if (databaseType.equals("oracle")) {
            sql = String.format("select COLUMN_NAME,a.* from user_tab_columns a where Table_Name='%s' ", tableName);
         }

         String resultText = this.executeSQL(connParams, sql);
         this.fillTable(resultText);
         this.fillTree(resultText, currentTreeItem);
      } catch (Exception var8) {
         this.statusLabel.setText(var8.getMessage());
      }

   }

   private void showDatabases(String connString) throws Exception {
      TreeItem rootItem = new TreeItem("数据库列表", new ImageView());
      rootItem.getGraphic().setUserData("root");
      this.schemaTree.setRoot(rootItem);
      this.schemaTree.setShowRoot(false);
      String shellType = this.currentShellService.getShellEntity().getString("type");
      Map connParams = this.parseConnURI(connString);
      String databaseType = ((String)connParams.get("type")).toLowerCase();
      String sql = null;
      if (databaseType.equals("mysql")) {
         sql = "show databases";
      } else if (databaseType.equals("sqlserver")) {
         sql = "SELECT name FROM  master..sysdatabases";
      } else if (databaseType.equals("oracle")) {
         sql = "select sys_context('userenv','db_name') as db_name from dual";
      }

      final String finalsql = sql;
      Runnable runner = () -> {
         try {
            if (shellType.equals("aspx")) {
               this.loadDriver("aspx", "mysql");
               this.loadDriver("aspx", "oracle");
            }

            String resultText = this.executeSQL(connParams, finalsql);
            if (resultText.equals("NoDriver")) {
               this.loadDriver(shellType, (String)connParams.get("type"));
               return;
            }

            Platform.runLater(() -> {
               try {
                  this.fillTable(resultText);
                  this.fillTree(resultText, rootItem);
               } catch (Exception var4) {
                  var4.printStackTrace();
                  this.statusLabel.setText(var4.getMessage());
               }

            });
         } catch (Exception var6) {
            var6.printStackTrace();
            Platform.runLater(() -> {
               Utils.showErrorMessage("错误", var6.getMessage());
               this.statusLabel.setText(var6.getMessage());
            });
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private Map parseConnURI(String url) throws Exception {
      Map connParams = new HashMap();
      URI connUrl = new URI(url);
      String type = connUrl.getScheme();
      String host = connUrl.getHost();
      String port = connUrl.getPort() + "";
      String authority = connUrl.getUserInfo();
      String user = authority.substring(0, authority.indexOf(":"));
      String pass = authority.substring(authority.indexOf(":") + 1);
      String database = connUrl.getPath().replaceFirst("/", "");
      String coding = "UTF-8";
      if (connUrl.getQuery() != null && connUrl.getQuery().indexOf("coding=") >= 0) {
         coding = connUrl.getQuery();
         Pattern p = Pattern.compile("([a-zA-Z]*)=([a-zA-Z0-9\\-]*)");
         Matcher m = p.matcher(connUrl.getQuery());

         while(m.find()) {
            String key = m.group(1).toLowerCase();
            if (key.equals("coding")) {
               coding = m.group(2).trim();
            }
         }
      }

      connParams.put("type", type);
      connParams.put("host", host);
      connParams.put("port", port);
      connParams.put("user", user);
      connParams.put("pass", pass);
      connParams.put("database", database);
      connParams.put("coding", coding);
      return connParams;
   }

   private void loadDriver(String scriptType, String databaseType) throws Exception {
      String driverPath = "net/rebeyond/behinder/resource/driver/";
      Platform.runLater(() -> {
         this.statusLabel.setText("正在上传数据库驱动……");
      });
      String os = this.effectShellEntity.getString("os").toLowerCase();
      String remoteDir = os.indexOf("windows") >= 0 ? "c:/windows/temp/" : "/tmp/";
      String libName = null;
      if (scriptType.equals("jsp")) {
         if (databaseType.equals("sqlserver")) {
            libName = "sqljdbc41.jar";
            libName = "sqljdbc4-3.0.jar";
         } else if (databaseType.equals("mysql")) {
            libName = "mysql-connector-java-5.1.36.jar";
         } else if (databaseType.equals("oracle")) {
            libName = "ojdbc5.jar";
         }
      } else if (scriptType.equals("aspx")) {
         if (databaseType.equals("mysql")) {
            libName = "mysql.data.dll";
         } else if (databaseType.equals("oracle")) {
            libName = "Oracle.ManagedDataAccess.dll";
         }
      }

      byte[] driverFileContent = Utils.getResourceData(driverPath + libName);
      String remotePath = remoteDir + libName;
      this.currentShellService.uploadFile(remotePath, driverFileContent, true);
      Platform.runLater(() -> {
         this.statusLabel.setText("驱动上传成功，正在加载驱动……");
      });
      JSONObject loadRes = this.currentShellService.loadJar(remotePath);
      if (loadRes.getString("status").equals("fail")) {
         throw new Exception("驱动加载失败:" + loadRes.getString("msg"));
      } else {
         Platform.runLater(() -> {
            if (scriptType.equals("jsp")) {
               this.statusLabel.setText("驱动加载成功，请再次点击“连接”。");
            }

            this.statusLabel.setText("驱动加载成功。");
         });
      }
   }

   private String executeSQL(Map connParams, String sql) throws Exception {
      Platform.runLater(() -> {
         this.statusLabel.setText("正在查询，请稍后……");
         this.sqlText.setText(sql);
      });
      String type = (String)connParams.get("type");
      String host = (String)connParams.get("host");
      String port = (String)connParams.get("port");
      String user = (String)connParams.get("user");
      String pass = (String)connParams.get("pass");
      String database = (String)connParams.get("database");
      JSONObject resultObj = this.currentShellService.execSQL(type, host, port, user, pass, database, sql);
      String status = resultObj.getString("status");
      String msg = resultObj.getString("msg");
      Platform.runLater(() -> {
         if (status.equals("success")) {
            this.statusLabel.setText("查询完成。");
         } else if (status.equals("fail") && !msg.equals("NoDriver")) {
            this.statusLabel.setText("查询失败:" + msg);
         }

      });
      return msg;
   }

   private void fillTree(String resultText, TreeItem currentTreeItem) throws Exception {
      currentTreeItem.getChildren().clear();
      JSONArray result = new JSONArray(resultText);
      int childNums = result.length() - 1;
      String childIconPath = "";
      String childType = "";
      switch (currentTreeItem.getGraphic().getUserData().toString()) {
         case "root":
            childIconPath = "net/rebeyond/behinder/resource/database.png";
            childType = "database";
            break;
         case "database":
            childIconPath = "net/rebeyond/behinder/resource/database_table.png";
            childType = "table";
            break;
         case "table":
            childIconPath = "net/rebeyond/behinder/resource/database_column.png";
            childType = "column";
      }

      Image icon = new Image(new ByteArrayInputStream(Utils.getResourceData(childIconPath)));

      for(int i = 1; i <= childNums; ++i) {
         JSONArray row = result.getJSONArray(i);
         String childName = row.get(0).toString();
         TreeItem treeItem = new TreeItem(childName, new ImageView(icon));
         treeItem.getGraphic().setUserData(childType);
         treeItem.setValue(childName);
         currentTreeItem.getChildren().add(treeItem);
      }

      currentTreeItem.setExpanded(true);
   }

   private void fillTable(String resultText) throws Exception {
      JSONArray result;
      try {
         result = new JSONArray(resultText);
      } catch (Exception var11) {
         throw new Exception(resultText);
      }

      if (result.get(0).getClass().toString().equals("class org.json.JSONArray")) {
         JSONArray fieldArray = result.getJSONArray(0);
         int rows = result.length() - 1;
         ObservableList tableViewColumns = FXCollections.observableArrayList();

         for(int i = 0; i < fieldArray.length(); ++i) {
            JSONObject field = fieldArray.getJSONObject(i);
            String fieldName = field.get("name").toString();
            TableColumn col = new TableColumn(fieldName);
            tableViewColumns.add(col);
            final int finali = i;
            col.setCellValueFactory((datax) -> {
               //return (ObservableValue)((List)datax.getValue()).get(i);
               return (StringProperty) ((List) ((TableColumn.CellDataFeatures) datax).getValue()).get(finali);
            });
         }

         this.dataTable.getColumns().setAll(tableViewColumns);
         ObservableList data = FXCollections.observableArrayList();

         for(int i = 1; i < rows + 1; ++i) {
            JSONArray rowArr = result.getJSONArray(i);
            List row = new ArrayList();

            for(int j = 0; j < rowArr.length(); ++j) {
               row.add(j, new SimpleStringProperty(rowArr.get(j).toString()));
            }

            data.add(row);
         }

         this.dataTable.setItems(data);
      }
   }
}
