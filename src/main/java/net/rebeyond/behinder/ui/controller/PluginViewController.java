// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import netscape.javascript.JSObject;
import javafx.concurrent.Worker;
import javafx.beans.value.ObservableValue;
import javafx.stage.WindowEvent;
import javafx.scene.input.MouseEvent;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Window;
import javafx.scene.control.Alert;
import org.json.JSONArray;
import javafx.scene.image.Image;
import net.rebeyond.behinder.utils.Utils;
import javafx.scene.web.WebEngine;
import net.rebeyond.behinder.core.PluginTools;
import java.util.List;
import net.rebeyond.behinder.core.ShellService;
import org.json.JSONObject;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import net.rebeyond.behinder.dao.ShellManager;

public class PluginViewController
{
    private ShellManager shellManager;
    @FXML
    private WebView pluginWebView;
    @FXML
    private Button installLocalBtn;
    @FXML
    private Button installNetBtn;
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
    private ShellService currentShellService;
    private List<Thread> workList;
    private Label statusLabel;

    public void init(final ShellService shellService, final List<Thread> workList, final Label statusLabel, final ShellManager shellManager) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.shellManager = shellManager;
        this.initPluginView();
    }

    private void initPluginView() {
        this.initPluginInstall();
        final PluginTools pluginTools = new PluginTools(this.currentShellService, this.pluginWebView, this.statusLabel, this.workList);
        final WebEngine webEngine = this.pluginWebView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                final JSObject win = (JSObject)webEngine.executeScript("window");
                win.setMember("PluginTools", pluginTools);
            }
        });
        try {
            this.loadPlugins();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.pluginDetailGridPane.setOpacity(0.0);
    }

    private void loadPluginDetail(final JSONObject pluginObj) {
        this.pluginNameLabel.setText(String.format(this.pluginNameLabel.getText(), pluginObj.getString("name"), pluginObj.getString("version")));
        this.pluginAuthorLabel.setText(String.format(this.pluginAuthorLabel.getText(), pluginObj.getString("author")));
        this.pluginLinkLabel.setText(String.format(this.pluginLinkLabel.getText(), pluginObj.getString("link")));
        this.pluginCommentLabel.setText(String.format(this.pluginCommentLabel.getText(), pluginObj.getString("comment")));
        final String pathFormat = "file://%s/Plugins/%s/%s";
        try {
            final String qrcodeFilePath = String.format(pathFormat, Utils.getSelfPath(), pluginObj.getString("name"), pluginObj.getString("qrcode"));
            this.qrcodeImageView.setImage(new Image(qrcodeFilePath));
        }
        catch (Exception e) {
            this.statusLabel.setText("插件开发者赞赏二维码加载失败");
            e.printStackTrace();
        }
    }

    private void loadPlugins() throws Exception {
        final String scriptType = this.shellEntity.getString("type");
        final JSONArray pluginList = this.shellManager.listPlugin(scriptType);
        for (int i = 0; i < pluginList.length(); ++i) {
            final JSONObject pluginObj = pluginList.getJSONObject(i);
            this.addPluginBox(pluginObj);
        }
    }

    private boolean checkPluginExist(final JSONObject pluginObj) throws Exception {
        final String pluginName = pluginObj.getString("name");
        final String scriptType = pluginObj.getString("scriptType");
        return this.shellManager.findPluginByName(scriptType, pluginName) != null;
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

    private void addPluginBox(final JSONObject pluginObj) throws Exception {
        final String pluginName = pluginObj.getString("name");
        final String pluginCommnet = pluginObj.getString("comment");
        final String pathFormat = "file://%s/Plugins/%s/%s";
        final String entryFilePath = String.format(pathFormat, Utils.getSelfPath(), pluginName, pluginObj.getString("entryFile"));
        final String iconFilePath = String.format(pathFormat, Utils.getSelfPath(), pluginName, pluginObj.getString("icon"));
        int type = 0;
        final String string = pluginObj.getString("type");
        switch (string) {
            case "scan": {
                type = 0;
                break;
            }
            case "exploit": {
                type = 1;
                break;
            }
            case "tool": {
                type = 2;
                break;
            }
            case "other": {
                type = 3;
                break;
            }
        }
        final FlowPane flowPane = (FlowPane)((AnchorPane)((TitledPane)this.pluginFlowPane.getPanes().get(type)).getContent()).getChildren().get(0);
        final VBox box = new VBox();
        final ImageView pluginIcon = new ImageView(new Image(iconFilePath));
        pluginIcon.setFitHeight(30.0);
        pluginIcon.setPreserveRatio(true);
        final Label pluginLabel = new Label(pluginName);
        box.getChildren().add(pluginIcon);
        box.getChildren().add(pluginLabel);
        box.setPadding(new Insets(5.0));
        box.setAlignment(Pos.CENTER);
        final Tooltip tip = new Tooltip();
        tip.setText(pluginCommnet);
        Tooltip.install((Node)box, tip);
        box.setOnMouseClicked(e -> {
            try {
                this.pluginWebView.getEngine().load(entryFilePath);
                this.pluginDetailGridPane.setOpacity(1.0);
                this.loadPluginDetail(pluginObj);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        box.setOnMouseEntered(e -> {
            final VBox v = (VBox)e.getSource();
            v.setStyle("-fx-background-color:blue");
        });
        box.setOnMouseExited(e -> {
            final VBox v = (VBox)e.getSource();
            v.setStyle("-fx-background-color:transparent");
        });
        flowPane.getChildren().add(box);
        ((TitledPane)this.pluginFlowPane.getPanes().get(type)).setExpanded(true);
    }

    private void initPluginInstall() {
        this.installLocalBtn.setOnAction(event -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("请选择需要安装的插件包");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter[] { new FileChooser.ExtensionFilter("All ZIP Files", new String[] { "*.zip" }) });
            final File pluginFile = fileChooser.showOpenDialog(this.pluginFlowPane.getScene().getWindow());
            try {
                final JSONObject pluginEntity = Utils.parsePluginZip(pluginFile.getAbsolutePath());
                if (this.checkPluginExist(pluginEntity)) {
                    this.showErrorMessage("错误", "安装失败，插件已存在");
                    return;
                }
                this.addPluginBox(pluginEntity);
                this.shellManager.addPlugin(pluginEntity.getString("name"), pluginEntity.getString("version"), pluginEntity.getString("entryFile"), pluginEntity.getString("scriptType"), pluginEntity.getString("type"), pluginEntity.getInt("isGetShell"), pluginEntity.getString("icon"), pluginEntity.getString("author"), pluginEntity.getString("link"), pluginEntity.getString("qrcode"), pluginEntity.getString("comment"));
                this.statusLabel.setText("插件安装成功。");
            }
            catch (Exception e) {
                e.printStackTrace();
                this.statusLabel.setText("插件安装失败:" + e.getMessage());
            }
        });
    }
}
