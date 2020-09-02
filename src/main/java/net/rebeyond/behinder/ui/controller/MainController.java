// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import java.util.HashMap;
import java.net.PasswordAuthentication;
import java.net.Authenticator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TableRow;
import javafx.beans.value.ObservableValue;
import java.util.Iterator;
import javafx.stage.WindowEvent;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Clipboard;
import javafx.beans.property.SimpleStringProperty;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import org.json.JSONArray;
import javafx.stage.Window;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.io.InputStream;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import net.rebeyond.behinder.utils.Utils;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.net.URL;
import javafx.beans.property.StringProperty;
import java.util.List;
import javafx.collections.ObservableList;
import org.json.JSONObject;
import java.net.SocketAddress;
import java.net.Proxy;
import java.net.InetSocketAddress;
import net.rebeyond.behinder.core.Constants;
import java.util.Map;
import net.rebeyond.behinder.dao.ShellManager;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.fxml.FXML;
import javafx.scene.control.TreeView;

public class MainController
{
    @FXML
    private TreeView treeview;
    @FXML
    private TableView shellListTable;
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
    private MenuItem proxySetupBtn;
    @FXML
    private Label statusLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Label proxyStatusLabel;
    @FXML
    private TreeView catagoryTreeView;
    private ShellManager shellManager;
    public static Map<String, Object> currentProxy;
    
    public MainController() {
        try {
            this.shellManager = new ShellManager();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            this.showErrorMessage("\u9519\u8bef", "\u6570\u636e\u5e93\u6587\u4ef6\u4e22\u5931");
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initBottomBar() {
        this.versionLabel.setText(String.format(this.versionLabel.getText(), Constants.VERSION));
    }
    
    private void loadProxy() throws Exception {
        final JSONObject proxyObj = this.shellManager.findProxy("default");
        final int status = proxyObj.getInt("status");
        final String type = proxyObj.getString("type");
        final String ip = proxyObj.getString("ip");
        final String port = proxyObj.get("port").toString();
        final String username = proxyObj.getString("username");
        final String password = proxyObj.getString("password");
        if (status == Constants.PROXY_ENABLE) {
            MainController.currentProxy.put("username", username);
            MainController.currentProxy.put("password", password);
            final InetSocketAddress proxyAddr = new InetSocketAddress(ip, Integer.parseInt(port));
            if (type.equals("HTTP")) {
                final Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
                MainController.currentProxy.put("proxy", proxy);
            }
            else if (type.equals("SOCKS")) {
                final Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
                MainController.currentProxy.put("proxy", proxy);
            }
            this.proxyStatusLabel.setText("\u4ee3\u7406\u751f\u6548\u4e2d");
        }
    }
    
    private void initToolbar() {
        this.proxySetupBtn.setOnAction(event -> {
            final Alert inputDialog = new Alert(Alert.AlertType.NONE);
            final Window window = inputDialog.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(e -> window.hide());
            final ToggleGroup statusGroup = new ToggleGroup();
            final RadioButton enableRadio = new RadioButton("\u542f\u7528");
            final RadioButton disableRadio = new RadioButton("\u7981\u7528");
            enableRadio.setToggleGroup(statusGroup);
            disableRadio.setToggleGroup(statusGroup);
            final HBox statusHbox = new HBox();
            statusHbox.setSpacing(10.0);
            statusHbox.getChildren().add(enableRadio);
            statusHbox.getChildren().add(disableRadio);
            final GridPane proxyGridPane = new GridPane();
            proxyGridPane.setVgap(15.0);
            proxyGridPane.setPadding(new Insets(20.0, 20.0, 0.0, 10.0));
            final Label typeLabel = new Label("\u7c7b\u578b\uff1a");
            final ComboBox typeCombo = new ComboBox();
            typeCombo.setItems(FXCollections.observableArrayList((Object[])new String[] { "HTTP", "SOCKS" }));
            typeCombo.getSelectionModel().select(0);
            final Label IPLabel = new Label("IP\u5730\u5740\uff1a");
            final TextField IPText = new TextField();
            final Label PortLabel = new Label("\u7aef\u53e3\uff1a");
            final TextField PortText = new TextField();
            final Label userNameLabel = new Label("\u7528\u6237\u540d\uff1a");
            final TextField userNameText = new TextField();
            final Label passwordLabel = new Label("\u5bc6\u7801\uff1a");
            final TextField passwordText = new TextField();
            final Button cancelBtn = new Button("\u53d6\u6d88");
            final Button saveBtn = new Button("\u4fdd\u5b58");
            try {
                final JSONObject proxyObj = this.shellManager.findProxy("default");
                if (proxyObj != null) {
                    final int status = proxyObj.getInt("status");
                    if (status == Constants.PROXY_ENABLE) {
                        enableRadio.setSelected(true);
                    }
                    else if (status == Constants.PROXY_DISABLE) {
                        disableRadio.setSelected(true);
                    }
                    final String type = proxyObj.getString("type");
                    if (type.equals("HTTP")) {
                        typeCombo.getSelectionModel().select(0);
                    }
                    else if (type.equals("SOCKS")) {
                        typeCombo.getSelectionModel().select(1);
                    }
                    final String ip = proxyObj.getString("ip");
                    final String port = proxyObj.get("port").toString();
                    IPText.setText(ip);
                    PortText.setText(port);
                    final String username = proxyObj.getString("username");
                    final String password = proxyObj.getString("password");
                    userNameText.setText(username);
                    passwordText.setText(password);
                }
            }
            catch (Exception e) {
                this.statusLabel.setText("\u4ee3\u7406\u670d\u52a1\u5668\u914d\u7f6e\u52a0\u8f7d\u5931\u8d25\u3002");
                e.printStackTrace();
            }
            saveBtn.setOnAction(e -> {
                if (disableRadio.isSelected()) {
                    MainController.currentProxy.put("proxy", null);
                    this.proxyStatusLabel.setText("");
                    try {
                        this.shellManager.updateProxy("default", typeCombo.getSelectionModel().getSelectedItem().toString(), IPText.getText(), PortText.getText(), userNameText.getText(), passwordText.getText(), Constants.PROXY_DISABLE);
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    inputDialog.getDialogPane().getScene().getWindow().hide();
                    return;
                }
                try {
                    this.shellManager.updateProxy("default", typeCombo.getSelectionModel().getSelectedItem().toString(), IPText.getText(), PortText.getText(), userNameText.getText(), passwordText.getText(), Constants.PROXY_ENABLE);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (!userNameText.getText().trim().equals("")) {
                    final String proxyUser = userNameText.getText().trim();
                    final String proxyPassword = passwordText.getText();
                    Authenticator.setDefault(new Authenticator() {
                        
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                        }
                    });
                }
                else {
                    Authenticator.setDefault(null);
                }
                MainController.currentProxy.put("username", userNameText.getText());
                MainController.currentProxy.put("password", passwordText.getText());
                final InetSocketAddress proxyAddr = new InetSocketAddress(IPText.getText(), Integer.parseInt(PortText.getText()));
                final String type = typeCombo.getValue().toString();
                if (type.equals("HTTP")) {
                    final Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
                    MainController.currentProxy.put("proxy", proxy);
                }
                else if (type.equals("SOCKS")) {
                    final Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
                    MainController.currentProxy.put("proxy", proxy);
                }
                this.proxyStatusLabel.setText("\u4ee3\u7406\u751f\u6548\u4e2d");
                inputDialog.getDialogPane().getScene().getWindow().hide();
            });
            cancelBtn.setOnAction(e -> inputDialog.getDialogPane().getScene().getWindow().hide());
            proxyGridPane.add((Node)statusHbox, 1, 0);
            proxyGridPane.add((Node)typeLabel, 0, 1);
            proxyGridPane.add((Node)typeCombo, 1, 1);
            proxyGridPane.add((Node)IPLabel, 0, 2);
            proxyGridPane.add((Node)IPText, 1, 2);
            proxyGridPane.add((Node)PortLabel, 0, 3);
            proxyGridPane.add((Node)PortText, 1, 3);
            proxyGridPane.add((Node)userNameLabel, 0, 4);
            proxyGridPane.add((Node)userNameText, 1, 4);
            proxyGridPane.add((Node)passwordLabel, 0, 5);
            proxyGridPane.add((Node)passwordText, 1, 5);
            final HBox buttonBox = new HBox();
            buttonBox.setSpacing(20.0);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().add(cancelBtn);
            buttonBox.getChildren().add(saveBtn);
            GridPane.setColumnSpan((Node)buttonBox, Integer.valueOf(2));
            proxyGridPane.add((Node)buttonBox, 0, 6);
            inputDialog.getDialogPane().setContent((Node)proxyGridPane);
            inputDialog.showAndWait();
        });
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
        final ObservableList<TableColumn<List<StringProperty>, ?>> tcs = (ObservableList<TableColumn<List<StringProperty>, ?>>)this.shellListTable.getColumns();
        for (int i = 0; i < tcs.size(); ++i) {
            final int j = i;
            ((TableColumn)tcs.get(i)).setCellValueFactory(data -> ((List)((TableColumn.CellDataFeatures)data).getValue()).get(j));
        }
        this.shellListTable.setRowFactory(tv -> {
            final TableRow<List<StringProperty>> row = (TableRow<List<StringProperty>>)new TableRow();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    final String url = ((List)row.getItem()).get(0).toString();
                    final String shellID = ((List)row.getItem()).get(6).toString();
                    this.openShell(url, shellID);
                }
            });
            return row;
        });
    }
    
    private boolean checkUrl(final String urlString) {
        try {
            final URL url = new URL(urlString.trim());
            return true;
        }
        catch (Exception e) {
            this.showErrorMessage("\u9519\u8bef", "URL\u683c\u5f0f\u9519\u8bef");
            return false;
        }
    }
    
    private boolean checkPassword(final String password) {
        if (password.length() > 255) {
            this.showErrorMessage("\u9519\u8bef", "\u5bc6\u7801\u957f\u5ea6\u4e0d\u5e94\u5927\u4e8e255\u4e2a\u5b57\u7b26");
            return false;
        }
        if (password.length() < 1) {
            this.showErrorMessage("\u9519\u8bef", "\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a\uff0c\u8bf7\u8f93\u5165\u5bc6\u7801");
            return false;
        }
        return true;
    }
    
    private void showShellDialog(final int shellID) throws Exception {
        final Alert alert = new Alert(Alert.AlertType.NONE);
        final Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> window.hide());
        alert.setTitle("\u65b0\u589eShell");
        final Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
        alert.setHeaderText("");
        final TextField urlText = new TextField();
        final TextField passText = new TextField();
        final ComboBox shellType = new ComboBox();
        final ObservableList<String> typeList = FXCollections.observableArrayList("jsp", "php", "aspx", "asp" );
        shellType.setItems((ObservableList)typeList);
        final ComboBox shellCatagory = new ComboBox();
        try {
            final JSONArray catagoryArr = this.shellManager.listCatagory();
            final ObservableList<String> catagoryList = FXCollections.observableArrayList();
            for (int i = 0; i < catagoryArr.length(); ++i) {
                final JSONObject catagoryObj = catagoryArr.getJSONObject(i);
                catagoryList.add(catagoryObj.getString("name"));
            }
            shellCatagory.setItems((ObservableList)catagoryList);
            shellCatagory.getSelectionModel().select(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        final TextArea header = new TextArea();
        final TextArea commnet = new TextArea();
        urlText.textProperty().addListener((observable, oldValue, newValue) -> {
            URL url;
            try {
                url = new URL(urlText.getText().trim());
            }
            catch (Exception e) {
                return;
            }
            final String extension = url.getPath().substring(url.getPath().lastIndexOf(".") + 1).toLowerCase();
            for (int i = 0; i < shellType.getItems().size(); ++i) {
                if (extension.toLowerCase().equals(shellType.getItems().get(i))) {
                    shellType.getSelectionModel().select(i);
                }
            }
        });
        final Button saveBtn = new Button("\u4fdd\u5b58");
        final Button cancelBtn = new Button("\u53d6\u6d88");
        final GridPane vpsInfoPane = new GridPane();
        GridPane.setMargin((Node)vpsInfoPane, new Insets(20.0, 0.0, 0.0, 0.0));
        vpsInfoPane.setVgap(10.0);
        vpsInfoPane.setMaxWidth(Double.MAX_VALUE);
        vpsInfoPane.add((Node)new Label("URL\uff1a"), 0, 0);
        vpsInfoPane.add((Node)urlText, 1, 0);
        vpsInfoPane.add((Node)new Label("\u5bc6\u7801\uff1a"), 0, 1);
        vpsInfoPane.add((Node)passText, 1, 1);
        vpsInfoPane.add((Node)new Label("\u811a\u672c\u7c7b\u578b\uff1a"), 0, 2);
        vpsInfoPane.add((Node)shellType, 1, 2);
        vpsInfoPane.add((Node)new Label("\u5206\u7c7b\uff1a"), 0, 3);
        vpsInfoPane.add((Node)shellCatagory, 1, 3);
        vpsInfoPane.add((Node)new Label("\u81ea\u5b9a\u4e49\u8bf7\u6c42\u5934\uff1a"), 0, 4);
        vpsInfoPane.add((Node)header, 1, 4);
        vpsInfoPane.add((Node)new Label("\u5907\u6ce8\uff1a"), 0, 5);
        vpsInfoPane.add((Node)commnet, 1, 5);
        final HBox buttonBox = new HBox();
        buttonBox.setSpacing(20.0);
        buttonBox.getChildren().addAll((Node)cancelBtn, (Node)saveBtn);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        vpsInfoPane.add((Node)buttonBox, 0, 8);
        GridPane.setColumnSpan((Node)buttonBox, Integer.valueOf(2));
        alert.getDialogPane().setContent((Node)vpsInfoPane);
        if (shellID != -1) {
            final JSONObject shellObj = this.shellManager.findShell(shellID);
            urlText.setText(shellObj.getString("url"));
            passText.setText(shellObj.getString("password"));
            shellType.setValue(shellObj.getString("type"));
            shellCatagory.setValue(shellObj.getString("catagory"));
            header.setText(shellObj.getString("headers"));
            commnet.setText(shellObj.getString("comment"));
        }
        saveBtn.setOnAction(e -> {
            final String url = urlText.getText().trim();
            final String password = passText.getText();
            if (!this.checkUrl(url) || !this.checkPassword(password)) {
                return;
            }
            final String type = shellType.getValue().toString();
            final String catagory = shellCatagory.getValue().toString();
            final String comment = commnet.getText();
            final String headers = header.getText();
            try {
                if (shellID == -1) {
                    this.shellManager.addShell(url, password, type, catagory, comment, headers);
                }
                else {
                    this.shellManager.updateShell(shellID, url, password, type, catagory, comment, headers);
                }
                this.loadShellList();
            }
            catch (Exception e2) {
                e2.printStackTrace();
                this.showErrorMessage("\u4fdd\u5b58\u5931\u8d25", e2.getMessage());
            }
            finally {
                alert.getDialogPane().getScene().getWindow().hide();
            }
        });
        cancelBtn.setOnAction(e -> alert.getDialogPane().getScene().getWindow().hide());
        alert.showAndWait();
    }
    
    private void openShell(final String url, final String shellID) {
        try {
            final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/net/rebeyond/behinder/ui/MainWindow.fxml"));
            final Parent mainWindow = (Parent)loader.load();
            final MainWindowController mainWindowController = (MainWindowController)loader.getController();
            mainWindowController.init(this.shellManager.findShell(Integer.parseInt(shellID)), this.shellManager, MainController.currentProxy);
            final Stage stage = new Stage();
            stage.setTitle(url);
            stage.getIcons().add(new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
            stage.setUserData(url);
            stage.setScene(new Scene(mainWindow));
            stage.setOnCloseRequest(e -> {
                for (final Thread worker : mainWindowController.getWorkList()) {
                    worker.interrupt();
                }
            });
            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadContextMenu() {
        final ContextMenu cm = new ContextMenu();
        final MenuItem openBtn = new MenuItem("\u6253\u5f00");
        cm.getItems().add(openBtn);
        final MenuItem addBtn = new MenuItem("\u65b0\u589e");
        cm.getItems().add(addBtn);
        final MenuItem editBtn = new MenuItem("\u7f16\u8f91");
        cm.getItems().add(editBtn);
        final MenuItem delBtn = new MenuItem("\u5220\u9664");
        cm.getItems().add(delBtn);
        final MenuItem copyBtn = new MenuItem("\u590d\u5236URL");
        cm.getItems().add(copyBtn);
        final SeparatorMenuItem separatorBtn = new SeparatorMenuItem();
        cm.getItems().add(separatorBtn);
        final MenuItem refreshBtn = new MenuItem("\u5237\u65b0");
        cm.getItems().add(refreshBtn);
        this.shellListTable.setContextMenu(cm);
        openBtn.setOnAction(event -> {
            final String url = ((SimpleStringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(0)).getValue();
            final String shellID = ((SimpleStringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(6)).getValue();
            try {
                final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/net/rebeyond/behinder/ui/MainWindow.fxml"));
                final Parent mainWindow = (Parent)loader.load();
                final MainWindowController mainWindowController = (MainWindowController)loader.getController();
                mainWindowController.init(this.shellManager.findShell(Integer.parseInt(shellID)), this.shellManager, MainController.currentProxy);
                final Stage stage = new Stage();
                stage.setTitle(url);
                stage.getIcons().add(new Image((InputStream)new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
                stage.setUserData(url);
                stage.setScene(new Scene(mainWindow));
                stage.setOnCloseRequest(e -> {
                    for (final Thread worker : mainWindowController.getWorkList()) {
                        worker.interrupt();
                    }
                });
                stage.show();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        addBtn.setOnAction(event -> {
            try {
                this.showShellDialog(-1);
            }
            catch (Exception e) {
                this.showErrorMessage("\u9519\u8bef", "\u65b0\u589e\u5931\u8d25\uff1a" + e.getMessage());
                e.printStackTrace();
            }
        });
        editBtn.setOnAction(event -> {
            final String shellID = ((SimpleStringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(6)).getValue();
            try {
                this.showShellDialog(Integer.parseInt(shellID));
            }
            catch (Exception e) {
                this.showErrorMessage("\u9519\u8bef", "\u7f16\u8f91\u5931\u8d25\uff1a" + e.getMessage());
                e.printStackTrace();
            }
        });
        delBtn.setOnAction(event -> {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("");
            alert.setContentText("\u8bf7\u786e\u8ba4\u662f\u5426\u5220\u9664\uff1f");
            final Optional<ButtonType> result = (Optional<ButtonType>)alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                final String shellID = ((SimpleStringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(6)).getValue();
                try {
                    this.shellManager.deleteShell(Integer.parseInt(shellID));
                    this.loadShellList();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        copyBtn.setOnAction(event -> {
            final String url = ((SimpleStringProperty)((List)this.shellListTable.getSelectionModel().getSelectedItem()).get(0)).getValue();
            this.copyString(url);
        });
        refreshBtn.setOnAction(event -> {
            try {
                this.loadShellList();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void loadShellList() throws Exception {
        this.shellListTable.getItems().clear();
        final JSONArray shellList = this.shellManager.listShell();
        this.fillShellRows(shellList);
    }
    
    private void fillShellRows(final JSONArray jsonArray) {
        final ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
        for (int i = 0; i < jsonArray.length(); ++i) {
            final JSONObject rowObj = jsonArray.getJSONObject(i);
            try {
                final int id = rowObj.getInt("id");
                final String url = rowObj.getString("url");
                final String ip = rowObj.getString("ip");
                final String type = rowObj.getString("type");
                final String os = rowObj.getString("os");
                final String comment = rowObj.getString("comment");
                final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                final String addTime = df.format(new Timestamp(rowObj.getLong("addtime")));
                final List<StringProperty> row = new ArrayList<StringProperty>();
                row.add(0, (StringProperty)new SimpleStringProperty(url));
                row.add(1, (StringProperty)new SimpleStringProperty(ip));
                row.add(2, (StringProperty)new SimpleStringProperty(type));
                row.add(3, (StringProperty)new SimpleStringProperty(os));
                row.add(4, (StringProperty)new SimpleStringProperty(comment));
                row.add(5, (StringProperty)new SimpleStringProperty(addTime));
                row.add(6, (StringProperty)new SimpleStringProperty(id + ""));
                data.add(row);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.shellListTable.setItems((ObservableList)data);
    }
    
    private void copyString(final String str) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(str);
        clipboard.setContent((Map)content);
    }
    
    private void showErrorMessage(final String title, final String msg) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        final Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(msg);
        alert.show();
    }
    
    private void initCatagoryMenu() {
        final ContextMenu treeContextMenu = new ContextMenu();
        final MenuItem addCatagoryBtn = new MenuItem("\u65b0\u589e");
        treeContextMenu.getItems().add(addCatagoryBtn);
        final MenuItem delCatagoryBtn = new MenuItem("\u5220\u9664");
        treeContextMenu.getItems().add(delCatagoryBtn);
        addCatagoryBtn.setOnAction(event -> {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("\u65b0\u589e\u5206\u7c7b");
            alert.setHeaderText("");
            final GridPane panel = new GridPane();
            final Label cataGoryNameLable = new Label("\u8bf7\u8f93\u5165\u5206\u7c7b\u540d\u79f0\uff1a");
            final TextField cataGoryNameTxt = new TextField();
            final Label cataGoryCommentLable = new Label("\u8bf7\u8f93\u5165\u5206\u7c7b\u63cf\u8ff0\uff1a");
            final TextField cataGoryCommentTxt = new TextField();
            panel.add((Node)cataGoryNameLable, 0, 0);
            panel.add((Node)cataGoryNameTxt, 1, 0);
            panel.add((Node)cataGoryCommentLable, 0, 1);
            panel.add((Node)cataGoryCommentTxt, 1, 1);
            panel.setVgap(20.0);
            alert.getDialogPane().setContent((Node)panel);
            final Optional<ButtonType> result = (Optional<ButtonType>)alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    if (this.shellManager.addCatagory(cataGoryNameTxt.getText(), cataGoryCommentTxt.getText()) > 0) {
                        this.statusLabel.setText("\u5206\u7c7b\u65b0\u589e\u5b8c\u6210");
                        this.initCatagoryTree();
                    }
                }
                catch (Exception e) {
                    this.statusLabel.setText("\u5206\u7c7b\u65b0\u589e\u5931\u8d25\uff1a" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        delCatagoryBtn.setOnAction(event -> {
            if (this.catagoryTreeView.getSelectionModel().getSelectedItem() == null) {
                return;
            }
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("");
            alert.setContentText("\u8bf7\u786e\u8ba4\u662f\u5426\u5220\u9664\uff1f\u4ec5\u5220\u9664\u5206\u7c7b\u4fe1\u606f\uff0c\u4e0d\u4f1a\u5220\u9664\u8be5\u5206\u7c7b\u4e0b\u7684\u7f51\u7ad9\u3002");
            final Optional<ButtonType> result = (Optional<ButtonType>)alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    final String cataGoryName = ((TreeItem)this.catagoryTreeView.getSelectionModel().getSelectedItem()).getValue().toString();
                    if (this.shellManager.deleteCatagory(cataGoryName) > 0) {
                        this.statusLabel.setText("\u5206\u7c7b\u5220\u9664\u5b8c\u6210");
                        this.initCatagoryTree();
                    }
                }
                catch (Exception e) {
                    this.statusLabel.setText("\u5206\u7c7b\u5220\u9664\u5931\u8d25\uff1a" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        this.catagoryTreeView.setContextMenu(treeContextMenu);
        this.catagoryTreeView.setOnMouseClicked(event -> {
            final TreeItem currentTreeItem = (TreeItem)this.catagoryTreeView.getSelectionModel().getSelectedItem();
            if (currentTreeItem.isLeaf()) {
                final String catagoryName = currentTreeItem.getValue().toString();
                try {
                    this.shellListTable.getItems().clear();
                    final JSONArray shellList = this.shellManager.findShellByCatagory(catagoryName);
                    this.fillShellRows(shellList);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    this.shellListTable.getItems().clear();
                    this.loadShellList();
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });
    }
    
    private void initCatagoryTree() throws Exception {
        final JSONArray catagoryList = this.shellManager.listCatagory();
        final TreeItem<String> rootItem = (TreeItem<String>)new TreeItem("\u5206\u7c7b\u5217\u8868", (Node)new ImageView());
        for (int i = 0; i < catagoryList.length(); ++i) {
            final JSONObject catagoryObj = catagoryList.getJSONObject(i);
            final TreeItem<String> treeItem = (TreeItem<String>)new TreeItem(catagoryObj.getString("name"));
            rootItem.getChildren().add(treeItem);
        }
        rootItem.setExpanded(true);
        this.catagoryTreeView.setRoot((TreeItem)rootItem);
        this.catagoryTreeView.getSelectionModel().select(rootItem);
    }
    
    static {
        MainController.currentProxy = new HashMap<String, Object>();
    }
}
