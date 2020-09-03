// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import net.rebeyond.behinder.core.Constants;
import javafx.scene.web.WebView;
import javafx.scene.control.Label;
import java.util.List;
import org.json.JSONObject;
import net.rebeyond.behinder.core.ShellService;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.rebeyond.behinder.dao.ShellManager;

public class UpdateInfoViewController
{
    private ShellManager shellManager;
    @FXML
    private TextArea realCmdTextArea;
    @FXML
    private TextField shellPathText;
    @FXML
    private Button realCmdBtn;
    private ShellService currentShellService;
    private JSONObject shellEntity;
    private List<Thread> workList;
    private Label statusLabel;
    @FXML
    private WebView updateInfoWebview;

    public void init(final ShellService shellService, final List<Thread> workList, final Label statusLabel) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.workList = workList;
        this.statusLabel = statusLabel;
        this.initUpdateInfoView();
    }

    private void initUpdateInfoView() {
        final String updateUrl = "https://www.rebeyond.net/Behinder/update.html?ver=" + Constants.VERSION;
        this.updateInfoWebview.getEngine().load(updateUrl);
    }
}
