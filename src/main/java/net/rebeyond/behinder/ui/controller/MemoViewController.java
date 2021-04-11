package net.rebeyond.behinder.ui.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import org.json.JSONObject;

public class MemoViewController {
   private ShellManager shellManager;
   @FXML
   private TextArea memoTextArea;
   private ShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private Label statusLabel;

   public void init(ShellService shellService, List workList, Label statusLabel, ShellManager shellManager) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.shellManager = shellManager;
      this.initMemoView();
   }

   private void initMemoView() {
      this.loadMemoContent();
      this.memoTextArea.textProperty().addListener(new ChangeListener<String>() {
         public void changed(ObservableValue observableValue, String s, String memoContent) {
            try {
               MemoViewController.this.statusLabel.setText("正在保存备忘录……");
               MemoViewController.this.shellManager.updateMemo(MemoViewController.this.shellEntity.getInt("id"), memoContent);
               MemoViewController.this.statusLabel.setText("备忘录已自动保存");
            } catch (Exception var5) {
               MemoViewController.this.statusLabel.setText("备忘录自动保存失败：" + var5.getMessage());
            }

         }
      });
   }

   private void loadMemoContent() {
      String memoContent = this.shellEntity.getString("memo");
      this.memoTextArea.setText(memoContent);
   }
}
