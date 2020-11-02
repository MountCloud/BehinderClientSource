package net.rebeyond.behinder.ui.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import org.json.JSONObject;

public class ParallelViewController {
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
   private List workList;
   private Label statusLabel;
   private ContextMenu hostContextMenu;
   private ContextMenu serviceContextMenu;
}
