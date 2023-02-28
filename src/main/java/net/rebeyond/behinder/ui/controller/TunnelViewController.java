package net.rebeyond.behinder.ui.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.service.TunnelService;
import net.rebeyond.behinder.service.callback.ITunnelCallBack;
import net.rebeyond.behinder.utils.CipherUtils;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class TunnelViewController {
   @FXML
   private Button createPortMapBtn;
   @FXML
   private Button createSocksBtn;
   @FXML
   private Button createReversePortMapBtn;
   @FXML
   private Label portMapListenIPLabel;
   @FXML
   private Label portMapListenPortLabel;
   @FXML
   private Label portMapDescLabel;
   @FXML
   private Label socksListenIPLabel;
   @FXML
   private Label socksListenPortLabel;
   @FXML
   private Label socksDescLabel;
   @FXML
   private TextArea tunnelLogTextarea;
   @FXML
   private RadioButton portmapVPSRadio;
   @FXML
   private RadioButton portmapHTTPRadio;
   @FXML
   private RadioButton socksVPSRadio;
   @FXML
   private RadioButton socksHTTPRadio;
   @FXML
   private TextField portMapTargetIPText;
   @FXML
   private TextField portMapTargetPortText;
   @FXML
   private TextField portMapIPText;
   @FXML
   private TextField portMapPortText;
   @FXML
   private TextField socksIPText;
   @FXML
   private TextField socksPortText;
   @FXML
   private TextField reversePortMapIPText;
   @FXML
   private TextField reversePortMapPortText;
   private IShellService currentShellService;
   private JSONObject shellEntity;
   private JSONObject effectShellEntity;
   private Map basicInfoMap;
   private List workList;
   private List reversePortMapThreadList = new ArrayList();
   private Label statusLabel;
   private ProxyTunnelWorker proxyTunnelWorker;
   private ITunnelCallBack callBack;
   private TunnelService tunnelService;
   private List ReversePortMapWorkerList = new ArrayList();
   private BlockingQueue logRecordQueue = new LinkedBlockingQueue(30);
   private ServerSocketChannel localPortMapSocket;

   public void init(IShellService shellService, List workList, Label statusLabel, Map basicInfoMap) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.effectShellEntity = shellService.getEffectShellEntity();
      this.basicInfoMap = basicInfoMap;
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.tunnelService = new TunnelService(this.currentShellService, this.workList, this.prepareCallBack());
      this.initTunnelView();
   }

   private ITunnelCallBack prepareCallBack() {
      ITunnelCallBack callBack = new ITunnelCallBack() {
         public void onInfo(String message) {
            TunnelViewController.this.logRecordQueue.add("[INFO]" + message + "\n");
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < TunnelViewController.this.logRecordQueue.size(); ++i) {
               sb.append(TunnelViewController.this.logRecordQueue.poll());
            }

            Platform.runLater(() -> {
               TunnelViewController.this.appendLogRecord("[INFO]" + message + "\n");
            });
         }

         public void onError(String message) {
            TunnelViewController.this.logRecordQueue.add("[ERROR]" + message + "\n");
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < TunnelViewController.this.logRecordQueue.size(); ++i) {
               sb.append(TunnelViewController.this.logRecordQueue.poll());
            }

            Platform.runLater(() -> {
               TunnelViewController.this.appendLogRecord("[ERROR]" + message + "\n");
            });
         }
      };
      return callBack;
   }

   private void initTunnelView() {
      final ToggleGroup portmapTypeGroup = new ToggleGroup();
      this.portmapVPSRadio.setToggleGroup(portmapTypeGroup);
      this.portmapHTTPRadio.setToggleGroup(portmapTypeGroup);
      this.portmapVPSRadio.setUserData("remote");
      this.portmapHTTPRadio.setUserData("local");
      ToggleGroup socksTypeGroup = new ToggleGroup();
      this.socksVPSRadio.setToggleGroup(socksTypeGroup);
      this.socksHTTPRadio.setToggleGroup(socksTypeGroup);
      this.socksVPSRadio.setUserData("remote");
      this.socksHTTPRadio.setUserData("local");
      portmapTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
         public void changed(ObservableValue ov, Toggle oldToggle, Toggle newToggle) {
            if (portmapTypeGroup.getSelectedToggle() != null) {
               String portMapType = newToggle.getUserData().toString();
               if (portMapType.equals("local")) {
                  TunnelViewController.this.portMapDescLabel.setText("*提供基于HTTP隧道的单端口映射，将远程目标内网端口映射到本地，适用于目标不能出网的情况。");
                  TunnelViewController.this.portMapListenIPLabel.setText("本地监听IP地址：");
                  TunnelViewController.this.portMapListenPortLabel.setText("本地监听端口：");
                  TunnelViewController.this.portMapIPText.setText("0.0.0.0");
               } else if (portMapType.equals("remote")) {
                  TunnelViewController.this.portMapDescLabel.setText("*提供基于VPS中转的单端口映射，将远程目标内网端口映射到VPS，目标机器需要能出网。");
                  TunnelViewController.this.portMapListenIPLabel.setText("VPS监听IP地址：");
                  TunnelViewController.this.portMapListenPortLabel.setText("VPS监听端口：");
                  TunnelViewController.this.portMapIPText.setText("124.70.138.134");
               }
            }

         }
      });
      this.portMapListenIPLabel.setText("VPS监听IP地址：");
      this.portMapListenPortLabel.setText("VPS监听端口：");
      socksTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
         public void changed(ObservableValue ov, Toggle oldToggle, Toggle newToggle) {
            if (portmapTypeGroup.getSelectedToggle() != null) {
               String portMapType = newToggle.getUserData().toString();
               if (portMapType.equals("local")) {
                  TunnelViewController.this.socksDescLabel.setText("*提供基于HTTP隧道的全局socks代理，将远程目标内网的socks代理服务开到本地，适用于目标不能出网的情况。");
                  TunnelViewController.this.socksListenIPLabel.setText("本地监听IP地址：");
                  TunnelViewController.this.socksListenPortLabel.setText("本地监听端口：");
                  TunnelViewController.this.socksIPText.setText("0.0.0.0");
               } else if (portMapType.equals("remote")) {
                  TunnelViewController.this.socksDescLabel.setText("*提供基于VPS中转的全局socks代理，将远程目标内网的socks代理服务开到外网VPS，目标机器需要能出网。");
                  TunnelViewController.this.socksListenIPLabel.setText("VPS监听IP地址：");
                  TunnelViewController.this.socksListenPortLabel.setText("VPS监听端口：");
                  TunnelViewController.this.socksIPText.setText("124.70.138.134");
               }
            }

         }
      });
      this.createPortMapBtn.setOnAction((event) -> {
         RadioButton currentTypeRadio;
         if (this.createPortMapBtn.getText().equals("开启")) {
            currentTypeRadio = (RadioButton)portmapTypeGroup.getSelectedToggle();
            if (currentTypeRadio.getUserData().toString().equals("local")) {
               this.createLocalPortMap();
            } else if (currentTypeRadio.getUserData().toString().equals("remote")) {
               this.createRemotePortMap();
            }
         } else {
            currentTypeRadio = (RadioButton)portmapTypeGroup.getSelectedToggle();
            if (currentTypeRadio.getUserData().toString().equals("local")) {
               this.stoplocalPortMap();
            } else if (currentTypeRadio.getUserData().toString().equals("remote")) {
               this.stopRemotePortMap();
            }
         }

      });
      this.createReversePortMapBtn.setOnAction((event) -> {
         String listenIP = this.reversePortMapIPText.getText().trim();
         String listenPort = this.reversePortMapPortText.getText().trim();
         if (this.createReversePortMapBtn.getText().equals("开启")) {
            this.createReversePortMapBtn.setText("关闭");
            this.startReversePortMap(listenIP, listenPort);
         } else {
            this.createReversePortMapBtn.setText("开启");
            this.stopReversePortMap(listenIP, listenPort);
         }

      });
      this.createSocksBtn.setOnAction((event) -> {
         RadioButton currentTypeRadio;
         if (this.createSocksBtn.getText().equals("开启")) {
            currentTypeRadio = (RadioButton)socksTypeGroup.getSelectedToggle();
            if (currentTypeRadio.getUserData().toString().equals("local")) {
               this.createLocalSocks();
            } else if (currentTypeRadio.getUserData().toString().equals("remote")) {
               this.createRemoteSocks();
            }
         } else {
            currentTypeRadio = (RadioButton)socksTypeGroup.getSelectedToggle();
            if (currentTypeRadio.getUserData().toString().equals("local")) {
               this.stopLocalSocks();
            } else if (currentTypeRadio.getUserData().toString().equals("remote")) {
               this.stopRemoteSocks();
            }
         }

      });
   }

   private void miniLogView() {
      int lines = this.tunnelLogTextarea.getText().split("\n").length;
      if (lines > 30) {
         this.tunnelLogTextarea.deleteText(0, this.tunnelLogTextarea.getText().indexOf("\n"));
      }

   }

   private void appendLogRecord(String message) {
      synchronized(this.tunnelLogTextarea) {
         int lines = this.tunnelLogTextarea.getText().split("\n").length;
         if (lines > 30) {
            this.tunnelLogTextarea.deleteText(0, this.tunnelLogTextarea.getText().indexOf("\n"));
            this.tunnelLogTextarea.appendText(message);
         } else {
            this.tunnelLogTextarea.appendText(message);
         }

      }
   }

   private void createLocalPortMap() {
      this.createPortMapBtn.setText("关闭");
      String targetIp = this.portMapTargetIPText.getText();
      String targetPort = this.portMapTargetPortText.getText();
      String localIp = this.portMapIPText.getText();
      String localPort = this.portMapPortText.getText();
      this.tunnelService.createLocalPortMap(localIp, localPort, targetIp, targetPort);
   }

   private void stoplocalPortMap() {
      this.createPortMapBtn.setText("开启");
      String targetIp = this.portMapTargetIPText.getText();
      String targetPort = this.portMapTargetPortText.getText();
      String localPort = this.portMapPortText.getText();
      this.tunnelService.stoplocalPortMap(localPort, targetIp, targetPort);
   }

   private void stopRemotePortMap() {
      this.createPortMapBtn.setText("开启");
      this.tunnelService.stopRemotePortMap();
   }

   private void createRemotePortMap() {
      this.createPortMapBtn.setText("关闭");
      String targetIp = this.portMapTargetIPText.getText();
      String targetPort = this.portMapTargetPortText.getText();
      String remoteIp = this.portMapIPText.getText();
      String remotePort = this.portMapPortText.getText();
      this.tunnelService.createRemotePortMap(remoteIp, remotePort, targetIp, targetPort);
   }

   private void createLocalSocks() {
      this.createSocksBtn.setText("关闭");
      new HashMap();
      String socksIP = this.socksIPText.getText();
      String socksPort = this.socksPortText.getText();
      this.tunnelService.createLocalSocksProxy(socksIP, socksPort);
   }

   private void stopLocalSocks() {
      String socksPort = this.socksPortText.getText();
      this.tunnelService.stopLocalSocksProxy(socksPort);
      this.createSocksBtn.setText("开启");
   }

   private void createRemoteSocks() {
      this.createSocksBtn.setText("关闭");
      String remoteIp = this.socksIPText.getText();
      String remotePort = this.socksPortText.getText();
      this.tunnelService.createRemoteSocks(remoteIp, remotePort);
   }

   private void stopRemoteSocks() {
      this.createSocksBtn.setText("开启");
      this.tunnelService.stopRemoteSocks();
   }

   private void startReversePortMap(String listenIP, String listenPort) {
      Runnable worker = () -> {
         try {
            Runnable runner = () -> {
               JSONObject result = null;

               try {
                  this.currentShellService.createReversePortMap(listenPort);
               } catch (Exception var4) {
                  var4.printStackTrace();
               }

            };
            Thread createWorker = new Thread(runner);
            createWorker.start();
            Thread.sleep(1000L);
            this.currentShellService.listReversePortMap();
            Map paramMap = new HashMap();
            paramMap.put("listenIP", listenIP);
            paramMap.put("listenPort", listenPort);
            ReversePortMapWorker reversePortMapWorkerDaemon = new ReversePortMapWorker("daemon", paramMap);
            Thread reversePortMapWorker = new Thread(reversePortMapWorkerDaemon);
            reversePortMapWorker.start();
            this.ReversePortMapWorkerList.add(reversePortMapWorkerDaemon);
            this.reversePortMapThreadList.add(reversePortMapWorker);
            this.workList.add(reversePortMapWorker);
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[INFO]通信隧道创建成功。\n");
            });
         } catch (Exception var8) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]通信隧道创建失败：" + var8.getMessage());
            });
         }

      };
      Thread woker = new Thread(worker);
      woker.start();
      this.workList.add(woker);
   }

   private void stopReversePortMap(String listenIP, String listenPort) {
      this.tunnelLogTextarea.appendText("[INFO]正在关闭通信隧道……\n");
      Runnable worker = () -> {
         try {
            Iterator var2 = this.ReversePortMapWorkerList.iterator();

            while(var2.hasNext()) {
               ReversePortMapWorker reversePortMapWorker = (ReversePortMapWorker)var2.next();
               reversePortMapWorker.stop();
            }

            var2 = this.reversePortMapThreadList.iterator();

            while(var2.hasNext()) {
               Thread thread = (Thread)var2.next();
               thread.stop();
            }

            this.ReversePortMapWorkerList.clear();
            JSONObject result = this.currentShellService.stopReversePortMap(listenPort);
            if (result.get("status").equals("success")) {
               Platform.runLater(() -> {
                  this.tunnelLogTextarea.appendText("[INFO]通信隧道关闭成功。\n");
               });
            } else {
               String msg = result.getString("msg");
               Platform.runLater(() -> {
                  this.tunnelLogTextarea.appendText("[ERROR]通信隧道关闭失败：" + msg + "\n");
               });
            }
         } catch (Exception var4) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]通信隧道关闭失败：" + var4.getMessage() + "\n");
            });
         }

      };
      Thread woker = new Thread(worker);
      woker.start();
      this.workList.add(woker);
   }

   class ReversePortMapWorker implements Runnable {
      private String threadType;
      private Map paramMap;
      private Map socketMetaList = new HashMap();

      public ReversePortMapWorker(String threadType, Map paramMap) {
         this.threadType = threadType;
         this.paramMap = paramMap;
      }

      public void stop() {
         Iterator var1 = this.socketMetaList.keySet().iterator();

         while(var1.hasNext()) {
            String key = (String)var1.next();
            Socket socket = (Socket)((Map)this.socketMetaList.get(key)).get("socket");

            try {
               socket.close();
            } catch (Exception var5) {
            }
         }

         this.socketMetaList = null;
      }

      public void run() {
         int bytesRead;
         if (this.threadType.equals("daemon")) {
            String listenIP = this.paramMap.get("listenIP").toString();
            int listenPort = Integer.parseInt(this.paramMap.get("listenPort").toString());

            while(true) {
               try {
                  JSONObject result = TunnelViewController.this.currentShellService.listReversePortMap();
                  JSONArray socketArr = new JSONArray(result.getString("msg"));
                  System.out.println(socketArr);

                  for(bytesRead = 0; bytesRead < socketArr.length(); ++bytesRead) {
                     JSONObject socketObj = socketArr.getJSONObject(bytesRead);
                     String socketHashx = socketObj.getString("socketHash");
                     if (socketHashx.startsWith("reverseportmap_socket") && !this.socketMetaList.containsKey(socketHashx)) {
                        Map socketMetax = new HashMap();
                        socketMetax.put("status", "ready");
                        Socket socketx = new Socket();
                        socketx.connect(new InetSocketAddress(listenIP, listenPort), 5000);
                        socketx.setSoTimeout(5000);
                        socketMetax.put("status", "connected");
                        socketMetax.put("socket", socketx);
                        socketMetax.put("socketHash", socketHashx);
                        this.socketMetaList.put(socketHashx, socketMetax);
                        Map paramMap = new HashMap();
                        paramMap.put("socketMeta", socketMetax);
                        ReversePortMapWorker reversePortMapWorkerReader = TunnelViewController.this.new ReversePortMapWorker("read", paramMap);
                        ReversePortMapWorker reversePortMapWorkerWriter = TunnelViewController.this.new ReversePortMapWorker("write", paramMap);
                        TunnelViewController.this.ReversePortMapWorkerList.add(reversePortMapWorkerReader);
                        TunnelViewController.this.ReversePortMapWorkerList.add(reversePortMapWorkerWriter);
                        Thread reader = new Thread(reversePortMapWorkerReader);
                        Thread writer = new Thread(reversePortMapWorkerWriter);
                        TunnelViewController.this.reversePortMapThreadList.add(reader);
                        TunnelViewController.this.reversePortMapThreadList.add(writer);
                        TunnelViewController.this.workList.add(reader);
                        TunnelViewController.this.workList.add(writer);
                        reader.start();
                        writer.start();
                     }
                  }

                  Thread.sleep(3000L);
               } catch (Exception var21) {
                  break;
               }
            }
         } else {
            Map socketMeta;
            String socketHash;
            Socket socket;
            JSONObject var27;
            if (this.threadType.equals("read")) {
               socketMeta = (Map)this.paramMap.get("socketMeta");
               socketHash = socketMeta.get("socketHash").toString();
               socket = (Socket)socketMeta.get("socket");

               while(true) {
                  try {
                     Thread.sleep(100L);
                     JSONObject responseObj = TunnelViewController.this.currentShellService.readReversePortMapData(socketHash);
                     if (!responseObj.getString("status").equals("success")) {
                        try {
                           var27 = TunnelViewController.this.currentShellService.closeReversePortMap(socketHash);
                        } catch (Exception var17) {
                        }
                        break;
                     }

                     String msg = responseObj.getString("msg");
                     byte[] data = Base64.getDecoder().decode(msg);
                     socket.getOutputStream().write(data);
                     socket.getOutputStream().flush();
                  } catch (Exception var18) {
                     var18.printStackTrace();

                     try {
                        var27 = TunnelViewController.this.currentShellService.closeReversePortMap(socketHash);
                     } catch (Exception var16) {
                     }
                     break;
                  }
               }
            } else if (this.threadType.equals("write")) {
               socketMeta = (Map)this.paramMap.get("socketMeta");
               socketHash = socketMeta.get("socketHash").toString();
               socket = (Socket)socketMeta.get("socket");

               while(true) {
                  try {
                     byte[] buf = new byte[20480];

                     for(bytesRead = socket.getInputStream().read(buf); bytesRead > 0; bytesRead = socket.getInputStream().read(buf)) {
                        TunnelViewController.this.currentShellService.writeReversePortMapData(Arrays.copyOfRange(buf, 0, bytesRead), socketHash);
                     }
                  } catch (SocketTimeoutException var19) {
                  } catch (Exception var20) {
                     var20.printStackTrace();

                     try {
                        var27 = TunnelViewController.this.currentShellService.closeReversePortMap(socketHash);
                     } catch (Exception var15) {
                     }
                     break;
                  }
               }
            }
         }

      }

      public void close(String listenIP, String listenPort) {
      }
   }

   class ProxyTunnelWorker implements Runnable {
      private String threadType;
      private Map paramMap;
      private ServerSocket serverSocket;
      private Map proxyTunnelWorkerListMap = new HashMap();

      public ProxyTunnelWorker(String threadType, Map paramMap) {
         this.threadType = threadType;
         this.paramMap = paramMap;
      }

      private void log(String type, String log) {
         String logLine = "[" + type + "]" + log + "\n";
         Platform.runLater(() -> {
            TunnelViewController.this.tunnelLogTextarea.appendText(logLine);
         });
      }

      private void shutDown() {
         this.log("INFO", "正在关闭代理服务");
         Iterator var1 = this.proxyTunnelWorkerListMap.keySet().iterator();

         while(var1.hasNext()) {
            String socketHash = (String)var1.next();
            List workerList = (List)this.proxyTunnelWorkerListMap.get(socketHash);
            Iterator var4 = workerList.iterator();

            while(var4.hasNext()) {
               Thread thread = (Thread)var4.next();
               thread.stop();
            }
         }

         if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            try {
               this.serverSocket.close();
               this.log("INFO", "代理服务已停止");
            } catch (IOException var6) {
               var6.printStackTrace();
            }
         }

      }

      public void run() {
         String targetIp;
         if (this.threadType.equals("daemon")) {
            try {
               String socksIP = this.paramMap.get("socksIP").toString();
               targetIp = this.paramMap.get("socksPort").toString();
               ServerSocket serverSocket = new ServerSocket(Integer.parseInt(targetIp), 50, InetAddress.getByName(socksIP));
               serverSocket.setReuseAddress(true);
               this.serverSocket = serverSocket;
               this.log("INFO", "正在监听端口" + targetIp);

               while(true) {
                  while(true) {
                     try {
                        Socket socket = serverSocket.accept();
                        this.log("INFO", "收到客户端连接请求.");
                        this.paramMap.put("socket", socket);
                        String socketHashx = Utils.getMD5("" + socket.getInetAddress() + socket.getPort() + "");
                        this.paramMap.put("socketHash", socketHashx);
                        List proxyTunnelWorkListx = new ArrayList();
                        this.paramMap.put("workerList", proxyTunnelWorkListx);
                        ProxyTunnelWorker sessionWorker = TunnelViewController.this.new ProxyTunnelWorker("session", this.paramMap);
                        Thread sessionWorkerThread = new Thread(sessionWorker);
                        sessionWorkerThread.start();
                        proxyTunnelWorkListx.add(sessionWorkerThread);
                        this.proxyTunnelWorkerListMap.put(socketHashx, proxyTunnelWorkListx);
                     } catch (NoSuchAlgorithmException var14) {
                        this.log("ERROR", "会话启动失败：" + var14.getMessage());
                     } catch (SocketException var15) {
                        this.log("INFO", "本地监听端口已关闭.");
                        return;
                     }
                  }
               }
            } catch (IOException var18) {
               this.log("ERROR", "端口监听失败：" + var18.getMessage() + var18.getClass());
            }
         } else {
            Socket socketx;
            if (this.threadType.equals("session")) {
               socketx = (Socket)this.paramMap.get("socket");
               targetIp = this.paramMap.get("socketHash").toString();
               List proxyTunnelWorkerList = (List)this.paramMap.get("workerList");

               try {
                  targetIp = Utils.getMD5("" + socketx.getInetAddress() + socketx.getPort() + "");
                  if (this.handleSocks(socketx, targetIp)) {
                     this.log("INFO", "正在通信...");
                     this.paramMap.put("idleCount", 0);
                     ProxyTunnelWorker readWorker = TunnelViewController.this.new ProxyTunnelWorker("read", this.paramMap);
                     Thread readWorkerThread = new Thread(readWorker);
                     TunnelViewController.this.workList.add(readWorkerThread);
                     readWorkerThread.start();
                     proxyTunnelWorkerList.add(readWorkerThread);
                     ProxyTunnelWorker writeWorker = TunnelViewController.this.new ProxyTunnelWorker("write", this.paramMap);
                     Thread writeWorkerThread = new Thread(writeWorker);
                     TunnelViewController.this.workList.add(writeWorkerThread);
                     writeWorkerThread.start();
                     proxyTunnelWorkerList.add(writeWorkerThread);
                  }
               } catch (Exception var11) {
                  try {
                     TunnelViewController.this.currentShellService.closeProxy(targetIp);
                  } catch (Exception var10) {
                  }
               }
            } else {
               String targetPort;
               String socketHash;
               List proxyTunnelWorkList;
               if (this.threadType.equals("read")) {
                  socketx = (Socket)this.paramMap.get("socket");
                  targetIp = this.paramMap.get("targetIp").toString();
                  targetPort = this.paramMap.get("targetPort").toString();
                  socketHash = this.paramMap.get("socketHash").toString();
                  proxyTunnelWorkList = (List)this.paramMap.get("workerList");
                  int idleCount = (Integer)this.paramMap.get("idleCount");

                  try {
                     while(true) {
                        if (socketx == null) {
                           this.stopSession(proxyTunnelWorkList);
                           break;
                        }

                        try {
                           JSONObject responseObj = TunnelViewController.this.currentShellService.readProxyData(socketHash);
                           if (responseObj.getString("status").equals("success")) {
                              byte[] datax = Base64.getDecoder().decode(responseObj.getString("msg"));
                              if (datax == null) {
                                 this.stopSession(proxyTunnelWorkList);
                                 break;
                              }

                              if (datax.length == 0) {
                                 ++idleCount;
                                 if (idleCount > 300) {
                                    Thread.sleep(3000L);
                                 } else if (idleCount > 150) {
                                    Thread.sleep(1000L);
                                 }
                              } else {
                                 idleCount = 0;
                                 socketx.getOutputStream().write(datax);
                                 socketx.getOutputStream().flush();
                              }
                           } else {
                              this.log("ERROR", "数据读取异常:" + responseObj.getString("msg"));
                           }
                        } catch (Exception var16) {
                           var16.printStackTrace();
                           this.log("ERROR", "数据读取请求异常:" + var16.getMessage());
                           this.stopSession(proxyTunnelWorkList);
                        }
                     }
                  } catch (Exception var17) {
                     this.stopSession(proxyTunnelWorkList);
                  }
               } else if (this.threadType.equals("write")) {
                  socketx = (Socket)this.paramMap.get("socket");
                  targetIp = this.paramMap.get("targetIp").toString();
                  targetPort = this.paramMap.get("targetPort").toString();
                  socketHash = this.paramMap.get("socketHash").toString();
                  proxyTunnelWorkList = (List)this.paramMap.get("workerList");

                  label93: {
                     while(socketx != null) {
                        try {
                           socketx.setSoTimeout(10000);
                           byte[] data = new byte['\uffff'];
                           int length = socketx.getInputStream().read(data);
                           if (length == -1) {
                              this.stopSession(proxyTunnelWorkList);
                              break label93;
                           }

                           this.paramMap.put("idleCount", 0);
                           data = Arrays.copyOfRange(data, 0, length);
                           TunnelViewController.this.currentShellService.writeProxyData(data, socketHash);
                        } catch (SocketTimeoutException var12) {
                        } catch (Exception var13) {
                           this.log("ERROR", "数据写入异常:" + var13.getMessage());
                           break label93;
                        }
                     }

                     this.stopSession(proxyTunnelWorkList);
                  }

                  try {
                     TunnelViewController.this.currentShellService.closeProxy(socketHash);
                     socketx.close();
                     this.stopSession(proxyTunnelWorkList);
                  } catch (Exception var9) {
                     this.log("ERROR", "隧道关闭失败:" + var9.getMessage());
                  }
               }
            }
         }

      }

      private void stopSession(List proxyTunnelWorkList) {
         Runnable runner = () -> {
            Iterator var1 = proxyTunnelWorkList.iterator();

            while(var1.hasNext()) {
               Thread thread = (Thread)var1.next();
               thread.stop();
            }

         };
         (new Thread(runner)).start();
         this.log("INFO", "会话关闭成功。");
      }

      private boolean handleSocks(Socket socket, String socketHash) throws Exception {
         int ver = socket.getInputStream().read();
         if (ver == 5) {
            return this.parseSocks5(socket, socketHash);
         } else {
            return ver == 4 ? this.parseSocks4(socket, socketHash) : false;
         }
      }

      private boolean parseSocks5(Socket socket, String socketHash) throws Exception {
         DataInputStream ins = new DataInputStream(socket.getInputStream());
         DataOutputStream os = new DataOutputStream(socket.getOutputStream());
         int nmethods = ins.read();

         for(int i = 0; i < nmethods; ++i) {
            int var11 = ins.read();
         }

         os.write(new byte[]{5, 0});
         int version = ins.read();
         int cmd;
         int rsv;
         int atyp;
         if (version == 2) {
            version = ins.read();
            cmd = ins.read();
            rsv = ins.read();
            atyp = ins.read();
         } else {
            cmd = ins.read();
            rsv = ins.read();
            atyp = ins.read();
         }

         byte[] targetPort = new byte[2];
         String host = "";
         byte[] target;
         if (atyp == 1) {
            target = new byte[4];
            ins.readFully(target);
            ins.readFully(targetPort);
            String[] tempArray = new String[4];

            int temp;
            for(int ix = 0; ix < target.length; ++ix) {
               temp = target[ix] & 255;
               tempArray[ix] = temp + "";
            }

            String[] var23 = tempArray;
            temp = tempArray.length;

            for(int var17 = 0; var17 < temp; ++var17) {
               String tempx = var23[var17];
               host = host + tempx + ".";
            }

            host = host.substring(0, host.length() - 1);
         } else if (atyp == 3) {
            int targetLen = ins.read();
            target = new byte[targetLen];
            ins.readFully(target);
            ins.readFully(targetPort);
            host = new String(target);
         } else if (atyp == 4) {
            target = new byte[16];
            ins.readFully(target);
            ins.readFully(targetPort);
            host = new String(target);
         }

         int port = (targetPort[0] & 255) * 256 + (targetPort[1] & 255);
         if (cmd != 2 && cmd != 3) {
            if (cmd == 1) {
               host = InetAddress.getByName(host).getHostAddress();
               this.paramMap.put("targetIp", host);
               this.paramMap.put("targetPort", port);
               JSONObject responseObj;
               if (TunnelViewController.this.effectShellEntity.getString("type").equals("php")) {
                  responseObj = TunnelViewController.this.currentShellService.openProxyAsyc(host, port + "", socketHash);
                  Thread.sleep(2000L);
               } else {
                  responseObj = TunnelViewController.this.currentShellService.openProxy(host, port + "", socketHash);
               }

               if (responseObj.getString("status").equals("success")) {
                  os.write(CipherUtils.mergeByteArray(new byte[]{5, 0, 0, 1}, InetAddress.getByName(host).getAddress(), targetPort));
                  this.log("INFO", "隧道建立成功，请求远程地址" + host + ":" + port);
                  return true;
               } else {
                  os.write(CipherUtils.mergeByteArray(new byte[]{5, 5, 0, 1}, InetAddress.getByName(host).getAddress(), targetPort));
                  throw new Exception(String.format("[%s:%d] Remote failed", host, port));
               }
            } else {
               throw new Exception("Socks5 - Unknown CMD");
            }
         } else {
            throw new Exception("not implemented");
         }
      }

      private boolean parseSocks4(Socket socket, String socketHash) {
         return false;
      }
   }
}
