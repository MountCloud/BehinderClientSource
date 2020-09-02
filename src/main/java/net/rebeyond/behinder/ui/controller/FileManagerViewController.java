// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import javafx.scene.input.MouseEvent;
import javafx.scene.control.TableRow;
import javafx.util.StringConverter;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ContextMenu;
import java.nio.file.Paths;
import javafx.beans.property.SimpleStringProperty;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.json.JSONArray;
import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.nio.file.Path;
import javafx.beans.property.StringProperty;
import java.io.UnsupportedEncodingException;
import org.json.JSONObject;
import java.io.File;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.io.InputStream;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import net.rebeyond.behinder.utils.Utils;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import java.util.Map;
import java.util.List;
import net.rebeyond.behinder.core.ShellService;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import net.rebeyond.behinder.dao.ShellManager;

public class FileManagerViewController
{
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
    private List<Thread> workList;
    Map<String, String> basicInfoMap;
    private Label statusLabel;
    
    public void init(final ShellService shellService, final List<Thread> workList, final Label statusLabel, final Map<String, String> basicInfoMap) {
        this.currentShellService = shellService;
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.basicInfoMap = basicInfoMap;
        try {
            this.initFileManagerView();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initFileManagerView() throws Exception {
        this.initFileListTableColumns();
        this.initCharsetCombo();
        final String driveList = this.basicInfoMap.get("driveList");
        final TreeItem<String> rootItem = (TreeItem<String>)new TreeItem("\u6587\u4ef6\u7cfb\u7edf", (Node)new ImageView());
        rootItem.getGraphic().setUserData("base");
        final Image icon = new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/drive.png")));
        for (final String drive : driveList.split(";")) {
            final TreeItem<String> driveItem = (TreeItem<String>)new TreeItem(drive, (Node)new ImageView(icon));
            driveItem.getGraphic().setUserData("root");
            driveItem.setValue(drive);
            rootItem.getChildren().add(driveItem);
            this.dirTree.setRoot((TreeItem)rootItem);
        }
        final String currentPath = this.basicInfoMap.get("currentPath");
        final ObservableList<String> pathList = FXCollections.observableArrayList(currentPath);
        this.currentPathCombo.setItems((ObservableList)pathList);
        this.currentPathCombo.getSelectionModel().select(0);
        this.loadContextMenu();
        this.dirTree.getSelectionModel().selectedItemProperty().addListener((ChangeListener)new ChangeListener() {
            public void changed(final ObservableValue observable, final Object oldValue, final Object newValue) {
                final TreeItem<String> currentTreeItem = (TreeItem<String>)newValue;
                final String pathString = FileManagerViewController.this.getFullPath(currentTreeItem);
                FileManagerViewController.this.expandByPath(pathString);
            }
        });
        this.expandByPath(currentPath);
        this.charsetCombo.setItems(FXCollections.observableArrayList((Object[])new String[] { "\u81ea\u52a8", "GBK", "UTF-8" }));
        this.cancelFileContentBtn.setOnAction(event -> this.switchPane("list"));
        this.saveFileContentBtn.setOnAction(event -> {
            final String filePath = this.filePathText.getText();
            try {
                this.saveFileContent(filePath);
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        this.fileListTableView.setEditable(true);
        this.fileNameCol.setOnEditCommit((EventHandler)new EventHandler<TableColumn.CellEditEvent>() {
            public void handle(final TableColumn.CellEditEvent cellEditEvent) {
                final String oldFileName = cellEditEvent.getOldValue().toString();
                final String newFileName = cellEditEvent.getNewValue().toString();
                FileManagerViewController.this.rename(oldFileName, newFileName);
            }
        });
        this.fileNameCol.setOnEditCancel((EventHandler)new EventHandler<TableColumn.CellEditEvent>() {
            public void handle(final TableColumn.CellEditEvent event) {
                FileManagerViewController.this.expandByPath(FileManagerViewController.this.currentPathCombo.getValue().toString());
            }
        });
        this.openPathBtn.setOnAction(event -> this.expandByPath(this.currentPathCombo.getValue().toString()));
        this.switchPane("list");
    }
    
    private void initCharsetCombo() {
        this.charsetCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            final String filePath = this.filePathText.getText();
            final String charset = newValue.toString().equals("\u81ea\u52a8") ? null : this.charsetCombo.getValue().toString();
            this.showFile(filePath, charset);
        });
    }
    
    private void uploadFile() throws Exception {
        final String currentPath = this.currentPathCombo.getValue().toString();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("\u8bf7\u9009\u62e9\u9700\u8981\u4e0a\u4f20\u7684\u6587\u4ef6");
        final File selectdFile = fileChooser.showOpenDialog(this.fileListGridPane.getScene().getWindow());
        if (selectdFile == null) {
            return;
        }
        final String fileName = selectdFile.getName();
        final byte[] fileContent = Utils.getFileData(selectdFile.getAbsolutePath());
        final int bufSize = this.currentShellService.currentType.equals("aspx") ? 524288 : 46080;
        this.statusLabel.setText("\u6b63\u5728\u4e0a\u4f20\u2026\u2026");

        final Runnable runner = () -> {


            byte[] array = fileContent;
            int size = bufSize;
            String str = currentPath;
            String str2 = fileName;
            JSONObject resultObj;
            String status;
            String msg = "";
            List<byte[]> blocks;
            int i;
            JSONObject resultObj2;
            JSONObject resultObj3;

            try {
                if (array.length < size) {
                    resultObj = this.currentShellService.uploadFile(str + str2, array);
                    status = resultObj.getString("status");
                    msg = resultObj.getString("msg");
                    if (status.equals("fail")) {
                        String finalMsg = msg;
                        Platform.runLater(() -> this.statusLabel.setText("\u6587\u4ef6\u4e0a\u4f20\u5931\u8d25:" + finalMsg));
                        return;
                    }
                }
                else {
                    for (blocks = Utils.splitBytes(array, size), i = 0; i < blocks.size(); ++i) {
                        if (i == 0) {
                            resultObj2 = this.currentShellService.uploadFile(str + str2, blocks.get(i));
                            status = resultObj2.getString("status");
                            msg = resultObj2.getString("msg");
                            if (status.equals("fail")) {
                                String finalMsg1 = msg;
                                Platform.runLater(() -> this.statusLabel.setText("\u6587\u4ef6\u4e0a\u4f20\u5931\u8d25:" + finalMsg1));
                                return;
                            }
                        }
                        else {
                            resultObj3 = this.currentShellService.appendFile(str + str2, blocks.get(i));
                            status = resultObj3.getString("status");
                            msg = resultObj3.getString("msg");
                            final int currentBlockIndex = i;
                            String finalStatus = status;
                            Platform.runLater(() -> {
                                if (finalStatus.equals("fail")) {
                                    this.statusLabel.setText("\u6587\u4ef6\u4e0a\u4f20\u5931\u8d25:" + str);
                                    return;
                                }
                                else {
                                    this.statusLabel.setText(String.format("\u6b63\u5728\u4e0a\u4f20\u2026\u2026%skb/%skb", (int)(size * currentBlockIndex / 1024), fileContent.length / 1024));
                                    return;
                                }
                            });
                            if (status.equals("fail")) {
                                return;
                            }
                        }
                    }
                }
                Platform.runLater(() -> {
                    this.statusLabel.setText("\u4e0a\u4f20\u5b8c\u6210");
                    this.expandByPath(currentPath);
                });
            }
            catch (Exception e) {
                Platform.runLater(() -> this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25:" + e.getMessage()));
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    private void rename(final String oldFileName, final String newFileName) {
        final String currentDir = this.currentPathCombo.getValue().toString();
        final String oldFullName = currentDir + oldFileName;
        final String newFullName = currentDir + newFileName;
        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.renameFile(oldFullName, newFullName);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    this.expandByPath(this.currentPathCombo.getValue().toString());
                    if (status.equals("fail")) {
                        this.statusLabel.setText(msg);
                    }
                    else {
                        this.statusLabel.setText(msg);
                    }
                });
            }
            catch (Exception e) {
                this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25:" + e.getMessage());
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    private void saveFileContent(final String pathString) throws UnsupportedEncodingException {
        String charset = null;
        if (this.charsetCombo.getSelectionModel().getSelectedIndex() > 0) {
            charset = this.charsetCombo.getValue().toString();
        }
        final byte[] fileContent = (charset == null) ? this.fileContentTextArea.getText().getBytes() : this.fileContentTextArea.getText().getBytes(charset);
        this.statusLabel.setText("\u6b63\u5728\u4fdd\u5b58\u2026\u2026");

        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.uploadFile(pathString, fileContent, true);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("success")) {
                        this.statusLabel.setText("\u4fdd\u5b58\u6210\u529f\u3002");
                    }
                    else {
                        this.statusLabel.setText("\u4fdd\u5b58\u5931\u8d25:" + msg);
                    }
                });
            }
            catch (Exception e) {
                Platform.runLater(() -> this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25:" + e.getMessage()));
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    private void switchPane(final String show) {
        if (show.equals("list")) {
            this.fileListGridPane.setOpacity(1.0);
            this.fileContentGridPane.setOpacity(0.0);
            this.fileListGridPane.toFront();
        }
        else if (show.equals("content")) {
            this.fileListGridPane.toBack();
            this.fileListGridPane.setOpacity(0.0);
            this.fileContentGridPane.setOpacity(1.0);
            this.fileContentGridPane.toFront();
        }
    }
    
    private String getFullPath(final TreeItem currentTreeItem) {
        final String fileSep = "/";
        String currentPath = currentTreeItem.getValue().toString();
        TreeItem parent = currentTreeItem;
        while (!(parent = parent.getParent()).getGraphic().getUserData().equals("base")) {
            final String parentText = parent.getValue().toString();
            if (parent.getGraphic().getUserData().equals("root")) {
                currentPath = parentText + currentPath;
            }
            else {
                currentPath = parentText + fileSep + currentPath;
            }
        }
        if (!parent.getGraphic().getUserData().equals("directory") && !currentPath.endsWith(fileSep)) {
            currentPath += fileSep;
        }
        return currentPath;
    }
    
    private void initFileListTableColumns() {
        final ObservableList<TableColumn<List<StringProperty>, ?>> tcs = (ObservableList<TableColumn<List<StringProperty>, ?>>)this.fileListTableView.getColumns();
        ((TableColumn)tcs.get(0)).setCellValueFactory(data -> ((List)((TableColumn.CellDataFeatures)data).getValue()).get(0));
        ((TableColumn)tcs.get(1)).setCellValueFactory(data -> ((List)((TableColumn.CellDataFeatures)data).getValue()).get(1));
        ((TableColumn)tcs.get(2)).setCellValueFactory(data -> ((List)((TableColumn.CellDataFeatures)data).getValue()).get(2));
        this.fileListTableView.setRowFactory(tv -> {
            final TableRow<List<StringProperty>> row = (TableRow<List<StringProperty>>)new TableRow();
            row.setOnMouseClicked(event -> {
                event.consume();
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    String path = this.currentPathCombo.getValue().toString();
                    final String name = ((List)row.getItem()).get(0).toString();
                    final String type = ((List)row.getItem()).get(3).toString();
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                    if (type.equals("file")) {
                        final String fileName = path + name;
                        this.filePathText.setText(fileName);
                        this.showFile(fileName, null);
                        this.switchPane("content");
                    }
                    else if (type.equals("directory")) {
                        this.expandByPath(path + name);
                    }
                }
            });
            return row;
        });
        this.fileNameCol.setCellFactory(column -> new TextFieldTableCell<StringProperty, String>(new DefaultStringConverter()) {
            public void updateItem(final String item, final boolean empty) {
                super.updateItem(item, empty);
                if (!(item == null | empty)) {
                    String type = null;
                    try {
                        type = (String)((List)this.getTableRow().getItem()).get(3);
                    }
                    catch (Exception e) {
                        return;
                    }
                    if (type.equals("directory")) {
                        try {
                            final Image icon = new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/folder.png")));
                            this.setGraphic((Node)new ImageView(icon));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else if (type.equals("file")) {
                        try {
                            final Image icon = new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/file.png")));
                            this.setGraphic((Node)new ImageView(icon));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    this.setText(item);
                }
            }
        });
    }
    
    private TreeItem findTreeItemByPath(final Path path) {
        final String osInfo = this.basicInfoMap.get("osInfo");
        TreeItem currentItem = null;
        final List<String> pathParts = new ArrayList<String>();
        final String pathString = path.toString().replace("\\", "/");
        if (pathString.equals("/")) {
            pathParts.add("/");
        }
        else {
            pathParts.addAll(Arrays.asList(pathString.split("/|\\\\")));
            if (osInfo.indexOf("linux") >= 0) {
                pathParts.set(0, "/");
            }
            else {
                pathParts.set(0, pathParts.get(0) + "/");
            }
        }
        Image icon = null;
        try {
            icon = new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/folder.png")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (final String childPath : pathParts) {
            if (currentItem == null) {
                final TreeItem childItem = currentItem = this.findTreeItem(this.dirTree.getRoot(), childPath);
            }
            else {
                TreeItem childItem = this.findTreeItem(currentItem, childPath);
                if (childItem == null) {
                    childItem = new TreeItem(childPath, (Node)new ImageView(icon));
                    childItem.getGraphic().setUserData("directory");
                    currentItem.getChildren().add(childItem);
                }
                currentItem = childItem;
            }
            currentItem.setExpanded(true);
        }
        this.dirTree.getSelectionModel().select(currentItem);
        return currentItem;
    }
    
    private void insertTreeItems(final JSONArray rows, final TreeItem currentTreeItem) {
        currentTreeItem.getChildren().clear();
        for (int i = 0; i < rows.length(); ++i) {
            try {
                final JSONObject fileObj = rows.getJSONObject(i);
                final String type = new String(Base64.decode(fileObj.getString("type")), "UTF-8");
                final String name = new String(Base64.decode(fileObj.getString("name")), "UTF-8");
                if (!name.equals(".") && !name.equals("..")) {
                    if (type.equals("directory")) {
                        final Image icon = new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/folder.png")));
                        final TreeItem<String> treeItem = (TreeItem<String>)new TreeItem(name, (Node)new ImageView(icon));
                        treeItem.getGraphic().setUserData("directory");
                        currentTreeItem.getChildren().add(treeItem);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentTreeItem.setExpanded(true);
        this.dirTree.getSelectionModel().select(currentTreeItem);
    }
    
    private TreeItem findTreeItem(final TreeItem treeItem, final String text) {
        final ObservableList childItemList = treeItem.getChildren();
        for (final Object childItem : childItemList) {
            if (((TreeItem)childItem).getValue().toString().equals(text)) {
                return (TreeItem)childItem;
            }
        }
        return null;
    }
    
    private void insertFileRows(final JSONArray jsonArray) {
        final ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
        for (int i = 0; i < jsonArray.length(); ++i) {
            final JSONObject rowObj = jsonArray.getJSONObject(i);
            try {
                final String type = new String(Base64.decode(rowObj.getString("type")), "UTF-8");
                final String name = new String(Base64.decode(rowObj.getString("name")), "UTF-8");
                final String size = new String(Base64.decode(rowObj.getString("size")), "UTF-8");
                final String lastModified = new String(Base64.decode(rowObj.getString("lastModified")));
                final List<StringProperty> row = new ArrayList<StringProperty>();
                row.add(0, (StringProperty)new SimpleStringProperty(name));
                row.add(1, (StringProperty)new SimpleStringProperty(size));
                row.add(2, (StringProperty)new SimpleStringProperty(lastModified));
                row.add(3, (StringProperty)new SimpleStringProperty(type));
                data.add(row);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.fileListTableView.setItems((ObservableList)data);
    }
    
    private void expandByPath(String pathStr) {
        final Path path = Paths.get(pathStr, new String[0]).normalize();
        pathStr = path.toString().replace("\\", "/");
        final String pathString = pathStr.endsWith("/") ? pathStr : (pathStr + "/");
        final TreeItem currentTreeItem = this.findTreeItemByPath(path);
        this.currentPathCombo.setValue(pathString);
        this.statusLabel.setText("\u6b63\u5728\u52a0\u8f7d\u76ee\u5f55\u2026\u2026");

        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.listFiles(pathString);
                Platform.runLater(() -> {
                    try {
                        String status = resultObj.getString("status");
                        String msg = resultObj.getString("msg");
                        if (status.equals("fail")) {
                            this.statusLabel.setText("\u76ee\u5f55\u8bfb\u53d6\u5931\u8d25:" + msg);
                        }
                        else {
                            this.statusLabel.setText("\u76ee\u5f55\u52a0\u8f7d\u6210\u529f");
                            String msg2 = msg.replace("},]", "}]");
                            JSONArray objArr = new JSONArray(msg2.trim());
                            this.insertFileRows(objArr);
                            this.insertTreeItems(objArr, currentTreeItem);
                        }
                    }
                    catch (Exception e) {
                        this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25\uff1a" + e.getMessage());
                    }
                });
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    private void showFile(final String filePath, final String charset) {
        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.showFile(filePath, charset);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> {
                    if (status.equals("fail")) {
                        this.statusLabel.setText("\u6587\u4ef6\u6253\u5f00\u5931\u8d25:" + msg);
                    }
                    else {
                        this.fileContentTextArea.setText(msg);
                        this.switchPane("content");
                    }
                });
            }
            catch (Exception e) {
                this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25:" + e.getMessage());
                e.printStackTrace();
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    private void loadContextMenu() {
        final ContextMenu cm = new ContextMenu();
        final MenuItem refreshBtn = new MenuItem("\u5237\u65b0");
        cm.getItems().add(refreshBtn);
        final MenuItem openBtn = new MenuItem("\u6253\u5f00");
        cm.getItems().add(openBtn);
        final MenuItem renameBtn = new MenuItem("\u91cd\u547d\u540d");
        cm.getItems().add(renameBtn);
        final MenuItem delBtn = new MenuItem("\u5220\u9664");
        cm.getItems().add(delBtn);
        cm.getItems().add(new SeparatorMenuItem());
        final MenuItem downloadBtn = new MenuItem("\u4e0b\u8f7d");
        cm.getItems().add(downloadBtn);
        final MenuItem uploadBtn = new MenuItem("\u4e0a\u4f20");
        cm.getItems().add(uploadBtn);
        final Menu createMenu = new Menu("\u65b0\u5efa");
        final MenuItem createFileBtn = new MenuItem("\u6587\u4ef6...");
        final MenuItem createDirectoryBtn = new MenuItem("\u6587\u4ef6\u5939");
        createMenu.getItems().add(createFileBtn);
        createMenu.getItems().add(createDirectoryBtn);
        cm.getItems().add(createMenu);
        cm.getItems().add(new SeparatorMenuItem());
        final MenuItem changeTimeStampBtn = new MenuItem("\u4fee\u6539\u65f6\u95f4\u6233");
        cm.getItems().add(changeTimeStampBtn);
        final MenuItem cloneTimeStampBtn = new MenuItem("\u514b\u9686\u65f6\u95f4\u6233");
        cm.getItems().add(cloneTimeStampBtn);
        this.fileListTableView.setContextMenu(cm);
        openBtn.setOnAction(event -> {
            final String type = ((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(3).toString();
            final String name = ((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(0).toString();
            String pathString = this.currentPathCombo.getValue().toString();
            pathString = Paths.get(pathString, new String[0]).normalize().toString();
            if (!pathString.endsWith("/")) {
                pathString += "/";
            }
            pathString += name;
            if (type.equals("directory")) {
                this.expandByPath(pathString);
            }
            else {
                final String filePathString = pathString;
                this.filePathText.setText(pathString);
                this.showFile(filePathString, null);
            }
        });
        refreshBtn.setOnAction(event -> {
            this.statusLabel.setText("\u6b63\u5728\u5237\u65b0\u2026\u2026");
            this.expandByPath(this.currentPathCombo.getValue().toString());
            this.statusLabel.setText("\u5237\u65b0\u5b8c\u6210\u3002");
        });
        renameBtn.setOnAction(event -> {
            final int row = this.fileListTableView.getSelectionModel().getSelectedIndex();
            this.fileListTableView.edit(row, this.fileNameCol);
        });
        delBtn.setOnAction(event -> {
            final String name = ((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(0).toString();
            final String fileFullPath = this.currentPathCombo.getValue().toString() + name;
            final Runnable runner = () -> {
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
                }
                catch (Exception e) {
                    Platform.runLater(() -> this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25:" + e.getMessage()));
                }
                return;
            };
            final Thread workThrad = new Thread(runner);
            this.workList.add(workThrad);
            workThrad.start();
        });
        uploadBtn.setOnAction(event -> {
            try {
                this.uploadFile();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        downloadBtn.setOnAction(event -> this.downloadFile());
    }
    
    private void downloadFile() {
        final String fileName = ((List)this.fileListTableView.getSelectionModel().getSelectedItem()).get(0).toString();
        final String fileFullPath = this.currentPathCombo.getValue().toString() + fileName;
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("\u8bf7\u9009\u62e9\u4fdd\u5b58\u8def\u5f84");
        fileChooser.setInitialFileName(fileName);
        final File selectedFile = fileChooser.showSaveDialog(this.fileListGridPane.getScene().getWindow());
        final String localFilePath = selectedFile.getAbsolutePath();
        if (selectedFile == null || selectedFile.equals("")) {
            return;
        }
        this.statusLabel.setText("\u6b63\u5728\u4e0b\u8f7d" + fileFullPath + "\u2026\u2026");
        final Runnable runner = () -> {
            try {
                this.currentShellService.downloadFile(fileName, localFilePath);
                String result = selectedFile.getName() + "\u4e0b\u8f7d\u5b8c\u6210,\u6587\u4ef6\u5927\u5c0f:" + selectedFile.length();
                Platform.runLater(() -> this.statusLabel.setText(result));
            }
            catch (Exception e) {
                Platform.runLater(() -> this.statusLabel.setText("\u64cd\u4f5c\u5931\u8d25:" + e.getMessage()));
                e.printStackTrace();
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
}
