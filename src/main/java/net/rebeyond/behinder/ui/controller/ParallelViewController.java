// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.ui.controller;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import java.util.List;
import org.json.JSONObject;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.core.ShellService;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;

public class ParallelViewController
{
    @FXML
    private FlowPane hostFlowPane;
    @FXML
    private MenuItem addHostBtn;
    @FXML
    private MenuItem doScanBtn;
    @FXML
    private RadioButton hostViewRadio;
    @FXML
    private RadioButton serviceViewRadio;
    @FXML
    private GridPane hostDetailGridPane;
    @FXML
    private GridPane hostListGridPane;
    @FXML
    private FlowPane serviceDetailFlowPane;
    @FXML
    private Button returnListBtn;
    private ShellService currentShellService;
    private ShellManager shellManager;
    private JSONObject shellEntity;
    private List<Thread> workList;
    private Label statusLabel;
    private ContextMenu hostContextMenu;
    private ContextMenu serviceContextMenu;
}
