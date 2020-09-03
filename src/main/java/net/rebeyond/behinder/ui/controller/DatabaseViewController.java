// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.control.TablePosition;
import java.io.File;
import java.io.FileOutputStream;
import javafx.stage.FileChooser;
import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.beans.value.ObservableValue;
import java.util.Iterator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.ArrayList;
import java.util.Collection;
import javafx.scene.control.TableColumn;
import java.io.InputStream;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import net.rebeyond.behinder.utils.Utils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import java.util.List;
import org.json.JSONObject;
import net.rebeyond.behinder.core.ShellService;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import net.rebeyond.behinder.dao.ShellManager;

public class DatabaseViewController
{
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
    private List<Thread> workList;
    private Label statusLabel;

    public void init(final ShellService shellService, final List<Thread> workList, final Label statusLabel) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.initDatabaseView();
    }

    private void initDatabaseView() {
        this.schemaTree.setOnMouseClicked(event -> {
            final TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
            if (currentTreeItem.isExpanded()) {
                currentTreeItem.setExpanded(false);
            }
            else if (event.getButton() == MouseButton.PRIMARY && !currentTreeItem.isExpanded()) {
                if (currentTreeItem.getGraphic().getUserData().toString().equals("database")) {
                    try {
                        this.showTables(currentTreeItem);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (currentTreeItem.getGraphic().getUserData().toString().equals("table")) {
                    try {
                        this.showColumns(currentTreeItem);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.executeSqlBtn.setOnAction(event -> {
            try {
                final Map<String, String> connParams = this.parseConnURI(this.connStrText.getText());

                final Runnable runner = () -> {
                    try {
                        String resultText = this.executeSQL(connParams, this.sqlText.getText());
                        Platform.runLater(() -> {
                            try {
                                this.fillTable(resultText);
                                this.statusLabel.setText("SQL执行成功。");
                            }
                            catch (Exception e) {
                                this.statusLabel.setText("SQL执行失败:" + e.getMessage());
                            }
                        });
                    }
                    catch (Exception e2) {
                        Platform.runLater(() -> this.statusLabel.setText("SQL执行失败:" + e2.getMessage()));
                    }
                    return;
                };
                final Thread worker = new Thread(runner);
                this.workList.add(worker);
                worker.start();
            }
            catch (Exception ex) {
                this.statusLabel.setText(ex.getMessage());
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
        final ContextMenu treeContextMenu = new ContextMenu();
        final MenuItem queryHeadBtn = new MenuItem("查询前10条");
        treeContextMenu.getItems().add(queryHeadBtn);
        final MenuItem queryAllBtn = new MenuItem("查询全部");
        treeContextMenu.getItems().add(queryAllBtn);
        final MenuItem exportBtn = new MenuItem("导出当前表");
        treeContextMenu.getItems().add(exportBtn);
        queryHeadBtn.setOnAction(event -> {
            final TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
            final String tableName = currentTreeItem.getValue().toString();
            final String dataBaseName = currentTreeItem.getParent().getValue().toString();
            final Runnable runner = () -> {
                Map<String, String> connParams;
                String databaseType;
                String sql;
                String resultText;
                try {
                    connParams = this.parseConnURI(this.connStrText.getText());
                    databaseType = connParams.get("type");
                    sql = null;
                    if (databaseType.equals("mysql")) {
                        sql = String.format("select * from %s.%s limit 10", dataBaseName, tableName);
                    }
                    else if (databaseType.equals("sqlserver")) {
                        sql = String.format("select top 10 * from %s..%s", dataBaseName, tableName);
                    }
                    else if (databaseType.equals("oracle")) {
                        sql = String.format("select * from %s where rownum<=10", tableName);
                    }
                    resultText = this.executeSQL(connParams, sql);
                    Platform.runLater(() -> {
                        try {
                            this.fillTable(resultText);
                        }
                        catch (Exception e) {
                            this.statusLabel.setText(e.getMessage());
                        }
                    });
                }
                catch (Exception e2) {
                    Platform.runLater(() -> this.statusLabel.setText(e2.getMessage()));
                }
                return;
            };
            final Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
        });
        queryAllBtn.setOnAction(event -> {
            final TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
            final String tableName = currentTreeItem.getValue().toString();
            final String dataBaseName = currentTreeItem.getParent().getValue().toString();
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认");
            alert.setHeaderText("");
            alert.setContentText("查询所有记录可能耗时较长，确定查询所有记录？");
            final Optional<ButtonType> result = (Optional<ButtonType>)alert.showAndWait();
            if (result.get() == ButtonType.OK) {

                final Runnable runner = () -> {
                    Map<String, String> connParams;
                    String databaseType;
                    String sql;
                    try {
                        connParams = this.parseConnURI(this.connStrText.getText());
                        databaseType = connParams.get("type");
                        sql = null;
                        if (databaseType.equals("mysql")) {
                            sql = String.format("select * from %s.%s", dataBaseName, tableName);
                        }
                        else if (databaseType.equals("sqlserver")) {
                            sql = String.format("select * from %s..%s", dataBaseName, tableName);
                        }
                        else if (databaseType.equals("oracle")) {
                            sql = String.format("select * from %s", tableName);
                        }
                        String resultText = this.executeSQL(connParams, sql);
                        Platform.runLater(() -> {
                            try {
                                this.fillTable(resultText);
                            }
                            catch (Exception e) {
                                this.statusLabel.setText(e.getMessage());
                            }
                        });
                    }
                    catch (Exception e2) {
                        Platform.runLater(() -> this.statusLabel.setText(e2.getMessage()));
                    }
                    return;
                };
                final Thread worker = new Thread(runner);
                this.workList.add(worker);
                worker.start();
            }
        });
        exportBtn.setOnAction(event -> {
            final TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
            final String tableName = currentTreeItem.getValue().toString();
            final String dataBaseName = currentTreeItem.getParent().getValue().toString();
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("请选择保存路径");
            fileChooser.setInitialFileName("export_table.csv");
            final File selectedFile = fileChooser.showSaveDialog(this.schemaTree.getScene().getWindow());
            final String selected = selectedFile.getAbsolutePath();
            if (selected == null || selected.equals("")) {
                return;
            }

            final Runnable runner = () -> {
                try {

                    Map<String, String> connParams;
                    String databaseType;
                    String sql;
                    Object o = dataBaseName;
                    Object o2 = tableName;
                    String resultText;
                    StringBuilder rows;
                    JSONArray arr;
                    String colsLine;
                    JSONArray cols;
                    int i;
                    JSONObject colObj;
                    int j;
                    JSONArray cells;
                    int k;
                    FileOutputStream fso;

                    connParams = this.parseConnURI(this.connStrText.getText());
                    databaseType = connParams.get("type");
                    sql = null;
                    if (databaseType.equals("mysql")) {
                        sql = String.format("select * from %s.%s", o, o2);
                    }
                    else if (databaseType.equals("sqlserver")) {
                        sql = String.format("select * from %s..%s", o, o2);
                    }
                    else if (databaseType.equals("oracle")) {
                        sql = String.format("select * from %s", o2);
                    }
                    resultText = this.executeSQL(connParams, sql);
                    rows = new StringBuilder();
                    arr = new JSONArray(resultText);
                    colsLine = "";
                    for (cols = arr.getJSONArray(0), i = 0; i < cols.length(); ++i) {
                        colObj = cols.getJSONObject(i);
                        colsLine = colsLine + colObj.getString("name") + ",";
                    }
                    rows.append(colsLine + "\n");
                    for (j = 1; j < arr.length(); ++j) {
                        for (cells = arr.getJSONArray(j), k = 0; k < cells.length(); ++k) {
                            rows.append(cells.get(k) + ",");
                        }
                        rows.append("\n");
                    }
                    fso = new FileOutputStream(selected);
                    fso.write(rows.toString().getBytes());
                    fso.flush();
                    fso.close();
                    Platform.runLater(() -> this.statusLabel.setText("导出完成，文件已保存至" + selected));
                }
                catch (Exception e) {
                    Platform.runLater(() -> this.statusLabel.setText(e.getMessage()));
                    e.printStackTrace();
                }
                return;
            };
            final Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
        });
        this.schemaTree.setOnContextMenuRequested(event -> {
            final TreeItem currentTreeItem = (TreeItem)this.schemaTree.getSelectionModel().getSelectedItem();
            if (currentTreeItem.getGraphic().getUserData().toString().equals("table")) {
                treeContextMenu.show(this.schemaTree.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void loadTableContextMenu() {
        final ContextMenu tableContextMenu = new ContextMenu();
        final MenuItem copyCellBtn = new MenuItem("复制单元格");
        tableContextMenu.getItems().add(copyCellBtn);
        final MenuItem copyRowBtn = new MenuItem("复制整行");
        tableContextMenu.getItems().add(copyRowBtn);
        final MenuItem exportBtn = new MenuItem("导出全部查询结果");
        tableContextMenu.getItems().add(exportBtn);
        copyCellBtn.setOnAction(event -> {
            final TablePosition position = (TablePosition)this.dataTable.getSelectionModel().getSelectedCells().get(0);
            final int row = position.getRow();
            final int column = position.getColumn();
            String selectedValue = "";
            if (this.dataTable.getItems().size() > row && ((List)this.dataTable.getItems().get(row)).size() > column) {
                selectedValue = ((List)this.dataTable.getItems().get(row)).get(column).toString();//.getValue();
                Utils.setClipboardString(selectedValue);
            }
        });
        copyRowBtn.setOnAction(event -> {
            final TablePosition position = (TablePosition)this.dataTable.getSelectionModel().getSelectedCells().get(0);
            final int row = position.getRow();
            final int column = position.getColumn();
            String selectedValue = "";
            final int rowSize = this.dataTable.getItems().size();
            final int columnSize = ((List)this.dataTable.getItems().get(row)).size();
            if (rowSize > row && columnSize > column) {
                String lineContent = "";
                for (int i = 0; i < columnSize; ++i) {
                    selectedValue = ((List)this.dataTable.getItems().get(row)).get(i).toString();//.getValue();
                    lineContent = lineContent + selectedValue + "|";
                }
                Utils.setClipboardString(lineContent);
            }
        });
        exportBtn.setOnAction(event -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("请选择保存路径");
            fileChooser.setInitialFileName("export_table.csv");
            final File selectedFile = fileChooser.showSaveDialog(this.schemaTree.getScene().getWindow());
            final String selected = selectedFile.getAbsolutePath();
            if (selected == null || selected.equals("")) {
                return;
            }
            final int rowSize = this.dataTable.getItems().size();
            final int columnSize = ((List)this.dataTable.getItems().get(0)).size();
            this.statusLabel.setText("正在准备数据……");

            final Runnable runner = () -> {

                StringBuilder sb;
                int i;
                int n = columnSize;
                TableColumn col;
                int j;
                int n2 = rowSize;
                int k;
                String cellString;
                String str = selected;
                FileOutputStream fso;

                sb = new StringBuilder();
                for (i = 0; i < n; ++i) {
                    col = (TableColumn)this.dataTable.getColumns().get(i);
                    sb.append(col.getText() + ",");
                }
                sb.append("\n");
                for (j = 0; j < n2; ++j) {
                    for (k = 0; k < n; ++k) {
                        cellString = ((List)this.dataTable.getItems().get(j)).get(k).toString();//.getValue();
                        sb.append(cellString + ",");
                    }
                    sb.append("\n");
                }
                Platform.runLater(() -> this.statusLabel.setText("正在写入文件……" + str));
                try {
                    fso = new FileOutputStream(str);
                    fso.write(sb.toString().getBytes());
                    fso.flush();
                    fso.close();
                    Platform.runLater(() -> this.statusLabel.setText("导出完成，文件已保存至" + str));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> this.statusLabel.setText("导出失败:" + e.getMessage()));
                }
                return;
            };
            final Thread worker = new Thread(runner);
            this.workList.add(worker);
            worker.start();
        });
        this.dataTable.setContextMenu(tableContextMenu);
    }

    private void initDatabaseType() {
        final ObservableList<String> typeList = (ObservableList<String>)FXCollections.observableArrayList("MySQL", "SQLServer", "Oracle");
        this.databaseTypeCombo.setItems((ObservableList)typeList);
        this.databaseTypeCombo.setOnAction(event -> {
            final String type = this.databaseTypeCombo.getValue().toString();
            final String connStr = this.formatConnectString(type);
            this.connStrText.setText(connStr);
        });
        this.connectBtn.setOnAction(event -> {
            try {
                this.showDatabases(this.connStrText.getText());
            }
            catch (Exception e) {
                e.printStackTrace();
                this.statusLabel.setText("连接失败:" + e.getMessage());
            }
        });
    }

    private String formatConnectString(final String type) {
        String result = "%s://%s:password@127.0.0.1:%s/%s";
        switch (type) {
            case "MySQL": {
                result = String.format(result, "mysql", "root", "3306", "mysql");
                break;
            }
            case "SQLServer": {
                result = String.format(result, "sqlserver", "sa", "1433", "master");
                break;
            }
            case "Oracle": {
                result = String.format(result, "oracle", "sys", "1521", "orcl");
                break;
            }
        }
        return result;
    }

    private void showTables(final TreeItem currentTreeItem) throws Exception {
        final Map<String, String> connParams = this.parseConnURI(this.connStrText.getText());
        String sql = null;
        final String databaseName = currentTreeItem.getValue().toString();
        final String databaseType = connParams.get("type");
        if (databaseType.equals("mysql")) {
            sql = String.format("select table_name,a.* from information_schema.tables as a where table_schema='%s' and table_type='base table'", databaseName);
        }
        else if (databaseType.equals("sqlserver")) {
            sql = String.format("select name,* from %s..sysobjects  where xtype='U'", databaseName);
        }
        else if (databaseType.equals("oracle")) {
            sql = "select table_name,num_rows from user_tables";
        }
        final String finalSql = sql;

        final Runnable runner = () -> {
            try {
                String resultText = this.executeSQL(connParams, finalSql);
                Platform.runLater(() -> {
                    try {
                        this.fillTable(resultText);
                        this.fillTree(resultText, currentTreeItem);
                    }
                    catch (Exception e) {
                        this.statusLabel.setText(e.getMessage());
                    }
                });
            }
            catch (Exception e2) {
                this.statusLabel.setText(e2.getMessage());
            }
            return;
        };
        final Thread worker = new Thread(runner);
        this.workList.add(worker);
        worker.start();
    }

    private void showColumns(final TreeItem currentTreeItem) {
        try {
            final String tableName = currentTreeItem.getValue().toString();
            final String databaseName = currentTreeItem.getParent().getValue().toString();
            final Map<String, String> connParams = this.parseConnURI(this.connStrText.getText());
            String sql = null;
            final String databaseType = connParams.get("type");
            if (databaseType.equals("mysql")) {
                sql = String.format("select COLUMN_NAME,a.* from information_schema.columns as a where table_schema='%s' and table_name='%s'", databaseName, tableName);
            }
            else if (databaseType.equals("sqlserver")) {
                sql = String.format("SELECT Name,* FROM %s..SysColumns WHERE id=Object_Id('%s')", databaseName, tableName);
            }
            else if (databaseType.equals("oracle")) {
                sql = String.format("select COLUMN_NAME,a.* from user_tab_columns a where Table_Name='%s' ", tableName);
            }
            final String resultText = this.executeSQL(connParams, sql);
            this.fillTable(resultText);
            this.fillTree(resultText, currentTreeItem);
        }
        catch (Exception ex) {
            this.statusLabel.setText(ex.getMessage());
        }
    }

    private void showDatabases(final String connString) throws Exception {
        final TreeItem<String> rootItem = (TreeItem<String>)new TreeItem("数据库列表", (Node)new ImageView());
        rootItem.getGraphic().setUserData("root");
        this.schemaTree.setRoot((TreeItem)rootItem);
        this.schemaTree.setShowRoot(false);
        final String shellType = this.currentShellService.getShellEntity().getString("type");
        final Map<String, String> connParams = this.parseConnURI(connString);
        final String databaseType = connParams.get("type").toLowerCase();
        String sql = null;
        if (databaseType.equals("mysql")) {
            sql = "show databases";
        }
        else if (databaseType.equals("sqlserver")) {
            sql = "SELECT name FROM  master..sysdatabases";
        }
        else if (databaseType.equals("oracle")) {
            sql = "select sys_context('userenv','db_name') as db_name from dual";
        }
        final String newSql = sql;
        final Runnable runner = () -> {
            try {
                if (shellType.equals("aspx")) {
                    this.loadDriver("aspx", "mysql");
                    this.loadDriver("aspx", "oracle");
                }
                String resultText = this.executeSQL(connParams, newSql);
                if (resultText.equals("NoDriver")) {
                    this.loadDriver(shellType, connParams.get("type"));
                }
                else {
                    Platform.runLater(() -> {
                        try {
                            this.fillTable(resultText);
                            this.fillTree(resultText, rootItem);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            this.statusLabel.setText(e.getMessage());
                        }
                    });
                }
            }
            catch (Exception e2) {
                Platform.runLater(() -> this.statusLabel.setText(e2.getMessage()));
            }
            return;
        };
        final Thread worker = new Thread(runner);
        this.workList.add(worker);
        worker.start();
    }

    private Map<String, String> parseConnURI(final String url) throws Exception {
        final Map<String, String> connParams = new HashMap<String, String>();
        final URI connUrl = new URI(url);
        final String type = connUrl.getScheme();
        final String host = connUrl.getHost();
        final String port = connUrl.getPort() + "";
        final String authority = connUrl.getUserInfo();
        final String user = authority.substring(0, authority.indexOf(":"));
        final String pass = authority.substring(authority.indexOf(":") + 1);
        final String database = connUrl.getPath().replaceFirst("/", "");
        String coding = "UTF-8";
        if (connUrl.getQuery() != null && connUrl.getQuery().indexOf("coding=") >= 0) {
            coding = connUrl.getQuery();
            final Pattern p = Pattern.compile("([a-zA-Z]*)=([a-zA-Z0-9\\-]*)");
            final Matcher m = p.matcher(connUrl.getQuery());
            while (m.find()) {
                final String key = m.group(1).toLowerCase();
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

    private void loadDriver(final String scriptType, final String databaseType) throws Exception {
        final String driverPath = "net/rebeyond/behinder/resource/driver/";
        Platform.runLater(() -> this.statusLabel.setText("正在上传数据库驱动……"));
        final String os = this.currentShellService.shellEntity.getString("os").toLowerCase();
        final String remoteDir = (os.indexOf("windows") >= 0) ? "c:/windows/temp/" : "/tmp/";
        String libName = null;
        if (scriptType.equals("jsp")) {
            if (databaseType.equals("sqlserver")) {
                libName = "sqljdbc41.jar";
            }
            else if (databaseType.equals("mysql")) {
                libName = "mysql-connector-java-5.1.36.jar";
            }
            else if (databaseType.equals("oracle")) {
                libName = "ojdbc5.jar";
            }
        }
        else if (scriptType.equals("aspx")) {
            if (databaseType.equals("mysql")) {
                libName = "mysql.data.dll";
            }
            else if (databaseType.equals("oracle")) {
                libName = "Oracle.ManagedDataAccess.dll";
            }
        }
        final byte[] driverFileContent = Utils.getResourceData(driverPath + libName);
        final String remotePath = remoteDir + libName;
        this.currentShellService.uploadFile(remotePath, driverFileContent, true);
        Platform.runLater(() -> this.statusLabel.setText("驱动上传成功，正在加载驱动……"));
        final JSONObject loadRes = this.currentShellService.loadJar(remotePath);
        if (loadRes.getString("status").equals("fail")) {
            throw new Exception("驱动加载失败:" + loadRes.getString("msg"));
        }
        Platform.runLater(() -> {
            if (scriptType.equals("jsp")) {
                this.statusLabel.setText("驱动加载成功，请再次点击“连接”。");
            }
            this.statusLabel.setText("驱动加载成功。");
        });
    }

    private String executeSQL(final Map<String, String> connParams, final String sql) throws Exception {
        Platform.runLater(() -> {
            this.statusLabel.setText("正在查询，请稍后……");
            this.sqlText.setText(sql);
            return;
        });
        final String type = connParams.get("type");
        final String host = connParams.get("host");
        final String port = connParams.get("port");
        final String user = connParams.get("user");
        final String pass = connParams.get("pass");
        final String database = connParams.get("database");
        final JSONObject resultObj = this.currentShellService.execSQL(type, host, port, user, pass, database, sql);
        final String status = resultObj.getString("status");
        final String msg = resultObj.getString("msg");
        Platform.runLater(() -> {
            if (status.equals("success")) {
                this.statusLabel.setText("查询完成。");
            }
            else if (status.equals("fail") && !msg.equals("NoDriver")) {
                this.statusLabel.setText("查询失败:" + msg);
            }
            return;
        });
        return msg;
    }

    private void fillTree(final String resultText, final TreeItem currentTreeItem) throws Exception {
        currentTreeItem.getChildren().clear();
        final JSONArray result = new JSONArray(resultText);
        final int childNums = result.length() - 1;
        String childIconPath = "";
        String childType = "";
        final String string = currentTreeItem.getGraphic().getUserData().toString();
        switch (string) {
            case "root": {
                childIconPath = "net/rebeyond/behinder/resource/database.png";
                childType = "database";
                break;
            }
            case "database": {
                childIconPath = "net/rebeyond/behinder/resource/database_table.png";
                childType = "table";
                break;
            }
            case "table": {
                childIconPath = "net/rebeyond/behinder/resource/database_column.png";
                childType = "column";
                break;
            }
        }
        final Image icon = new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData(childIconPath)));
        for (int i = 1; i <= childNums; ++i) {
            final JSONArray row = result.getJSONArray(i);
            final String childName = row.get(0).toString();
            final TreeItem<String> treeItem = (TreeItem<String>)new TreeItem(childName, (Node)new ImageView(icon));
            treeItem.getGraphic().setUserData(childType);
            treeItem.setValue(childName);
            currentTreeItem.getChildren().add(treeItem);
        }
        currentTreeItem.setExpanded(true);
    }

    private void fillTable(final String resultText) throws Exception {
        JSONArray result;
        try {
            result = new JSONArray(resultText);
        }
        catch (Exception e) {
            throw new Exception(resultText);
        }
        if (!result.get(0).getClass().toString().equals("class org.json.JSONArray")) {
            return;
        }
        final JSONArray fieldArray = result.getJSONArray(0);
        final int rows = result.length() - 1;
        final ObservableList<TableColumn> tableViewColumns = FXCollections.observableArrayList();
        for (final Object field : fieldArray) {
            final String fieldName = ((JSONObject)field).get("name").toString();
            final TableColumn<List<StringProperty>, String> col = (TableColumn<List<StringProperty>, String>)new TableColumn(fieldName);
            tableViewColumns.add(col);
            col.setCellValueFactory(data -> (ObservableValue<String>) ((List)data.getValue()).get(0));
        }
        this.dataTable.getColumns().setAll((Collection)tableViewColumns);
        final ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
        for (int i = 1; i < rows; ++i) {
            final JSONArray rowArr = result.getJSONArray(i);
            final List<StringProperty> row = new ArrayList<StringProperty>();
            for (int j = 0; j < rowArr.length(); ++j) {
                row.add(j, (StringProperty)new SimpleStringProperty(rowArr.get(j).toString()));
            }
            data.add(row);
        }
        this.dataTable.setItems((ObservableList)data);
    }
}
