package net.rebeyond.behinder.ui.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import net.rebeyond.behinder.core.ShellService;
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
   private ShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private List localList = new ArrayList();
   private Label statusLabel;
   private TunnelViewController.ProxyUtils proxyUtils;
   private List ReversePortMapWorkerList = new ArrayList();
   private ServerSocket localPortMapSocket;

   public void init(ShellService shellService, List workList, Label statusLabel) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.initTunnelView();
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
                  TunnelViewController.this.portMapIPText.setText("8.8.8.8");
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
               } else if (portMapType.equals("remote")) {
                  TunnelViewController.this.socksDescLabel.setText("*提供基于VPS中转的全局socks代理，将远程目标内网的socks代理服务开到外网VPS，目标机器需要能出网。");
                  TunnelViewController.this.socksListenIPLabel.setText("VPS监听IP地址：");
                  TunnelViewController.this.socksListenPortLabel.setText("VPS监听端口：");
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

   private void createLocalPortMap() {
      this.createPortMapBtn.setText("关闭");
      String targetIP = this.portMapTargetIPText.getText();
      String targetPort = this.portMapTargetPortText.getText();
      Runnable creater = () -> {
         try {
            Runnable runner = () -> {
               try {
                  String host = this.portMapIPText.getText();
                  int port = Integer.parseInt(this.portMapPortText.getText());
                  ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
                  serverSocket.setReuseAddress(true);
                  this.localPortMapSocket = serverSocket;
                  Platform.runLater(() -> {
                     this.tunnelLogTextarea.appendText("[INFO]正在监听本地端口:" + port + "\n");
                  });

                  while(true) {
                     Socket socket = serverSocket.accept();
                     String socketHash = Utils.getMD5("" + socket.getInetAddress() + socket.getPort() + "");
                     this.currentShellService.createPortMap(targetIP, targetPort, socketHash);
                     Platform.runLater(() -> {
                        this.tunnelLogTextarea.appendText("[INFO]隧道创建成功。\n");
                     });
                     Runnable reader = () -> {
                        while(true) {
                           if (socket != null) {
                              try {
                                 byte[] data = this.currentShellService.readPortMapData(targetIP, targetPort, socketHash);
                                 if (data == null) {
                                    continue;
                                 }

                                 if (data.length == 0) {
                                    Thread.sleep(10L);
                                    continue;
                                 }

                                 socket.getOutputStream().write(data);
                                 socket.getOutputStream().flush();
                                 continue;
                              } catch (Exception var6) {
                                 if (!(var6 instanceof SocketException)) {
                                    Platform.runLater(() -> {
                                       this.tunnelLogTextarea.appendText("[ERROR]数据读取异常:" + var6.getMessage() + "\n");
                                    });
                                    continue;
                                 }
                              }
                           }

                           return;
                        }
                     };
                     Runnable writer = () -> {
                        while(true) {
                           if (socket != null) {
                              try {
                                 socket.setSoTimeout(10000);
                                 byte[] data = new byte['\uffff'];
                                 int length = socket.getInputStream().read(data);
                                 if (length != -1) {
                                    data = Arrays.copyOfRange(data, 0, length);
                                    this.currentShellService.writePortMapData(data, targetIP, targetPort, socketHash);
                                    continue;
                                 }
                              } catch (SocketTimeoutException var8) {
                                 continue;
                              } catch (Exception var9) {
                                 Platform.runLater(() -> {
                                    this.tunnelLogTextarea.appendText("[ERROR]数据写入异常:" + var9.getMessage() + "\n");
                                 });
                              }
                           }

                           try {
                              this.currentShellService.closeLocalPortMap(targetIP, targetPort);
                              Platform.runLater(() -> {
                                 this.tunnelLogTextarea.appendText("[INFO]隧道关闭成功。\n");
                              });
                              socket.close();
                           } catch (Exception var7) {
                              Platform.runLater(() -> {
                                 this.tunnelLogTextarea.appendText("[ERROR]隧道关闭失败:" + var7.getMessage() + "\n");
                              });
                           }

                           return;
                        }
                     };
                     Thread readWorker = new Thread(reader);
                     this.workList.add(readWorker);
                     readWorker.start();
                     Thread writeWorker = new Thread(writer);
                     this.workList.add(writeWorker);
                     writeWorker.start();
                     this.localList.add(readWorker);
                     this.localList.add(writeWorker);
                  }
               } catch (Exception var12) {
               }
            };
            Thread worker = new Thread(runner);
            this.workList.add(worker);
            this.localList.add(worker);
            worker.start();
         } catch (Exception var5) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]隧道创建失败:" + var5.getMessage() + "\n");
            });
         }

      };
      Thread worker = new Thread(creater);
      this.workList.add(worker);
      worker.start();
   }

   private void stoplocalPortMap() {
      this.createPortMapBtn.setText("开启");
      String targetIP = this.portMapTargetIPText.getText();
      String targetPort = this.portMapTargetPortText.getText();
      Runnable runner = () -> {
         try {
            Iterator var3 = this.localList.iterator();

            while(var3.hasNext()) {
               Thread thread = (Thread)var3.next();
               thread.interrupt();
            }

            this.currentShellService.closeLocalPortMap(targetIP, targetPort);
            if (this.localPortMapSocket != null && !this.localPortMapSocket.isClosed()) {
               try {
                  this.localPortMapSocket.close();
               } catch (IOException var5) {
               }
            }

            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[INFO]本地监听端口已关闭。\n");
            });
         } catch (Exception var6) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]隧道关闭失败:" + var6.getMessage() + "\n");
            });
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private void stopRemotePortMap() {
      this.createPortMapBtn.setText("开启");
      Runnable runner = () -> {
         try {
            this.currentShellService.closeRemotePortMap();
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[INFO]隧道已关闭，远端相关资源已释放。\n");
            });
         } catch (Exception var2) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]隧道关闭失败:" + var2.getMessage() + "\n");
            });
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private void createRemotePortMap() {
      this.createPortMapBtn.setText("关闭");
      String remoteTargetIP = this.portMapTargetIPText.getText();
      String remoteTargetPort = this.portMapTargetPortText.getText();
      String remoteIP = this.portMapIPText.getText();
      String remotePort = this.portMapPortText.getText();
      Runnable runner = () -> {
         try {
            this.currentShellService.createRemotePortMap(remoteTargetIP, remoteTargetPort, remoteIP, remotePort);
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[INFO]隧道建立成功，请连接VPS。\n");
            });
         } catch (Exception var6) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]隧道建立失败:" + var6.getMessage() + "\n");
            });
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private void createLocalSocks() {
      this.createSocksBtn.setText("关闭");
      this.proxyUtils = new TunnelViewController.ProxyUtils();
      this.proxyUtils.start();
   }

   private void stopLocalSocks() {
      this.proxyUtils.shutdown();
      this.createSocksBtn.setText("开启");
   }

   private void createRemoteSocks() {
      this.createSocksBtn.setText("关闭");
      String remoteIP = this.socksIPText.getText();
      String remotePort = this.socksPortText.getText();
      Runnable runner = () -> {
         try {
            this.currentShellService.createVPSSocks(remoteIP, remotePort);
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[INFO]隧道建立成功，请使用SOCKS5客户端连接VPS。\n");
            });
         } catch (Exception var4) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]隧道建立失败:" + var4.getMessage() + "\n");
            });
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private void stopRemoteSocks() {
      this.createSocksBtn.setText("开启");
      Runnable runner = () -> {
         try {
            JSONObject result = this.currentShellService.stopVPSSocks();
            if (result.getString("status").equals("success")) {
               Platform.runLater(() -> {
                  this.tunnelLogTextarea.appendText("[INFO]隧道关闭成功，服务侧资源已释放。\n");
               });
            } else {
               Platform.runLater(() -> {
                  this.tunnelLogTextarea.appendText("[ERROR]隧道关闭失败：" + result.getString("msg") + "\n");
               });
            }
         } catch (Exception var2) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]隧道关闭失败:" + var2.getMessage() + "\n");
            });
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private void startReversePortMap(String listenIP, String listenPort) {
      Runnable worker = () -> {
         try {
            JSONObject result = this.currentShellService.createReversePortMap(listenPort);
            if (result.get("status").equals("success")) {
               result = this.currentShellService.listReversePortMap();
               Map paramMap = new HashMap();
               paramMap.put("listenIP", listenIP);
               paramMap.put("listenPort", listenPort);
               TunnelViewController.ReversePortMapWorker reversePortMapWorkerDaemon = new TunnelViewController.ReversePortMapWorker("daemon", paramMap);
               this.ReversePortMapWorkerList.add(reversePortMapWorkerDaemon);
               Thread reversePortMapWorker = new Thread(reversePortMapWorkerDaemon);
               reversePortMapWorker.start();
               this.workList.add(reversePortMapWorker);
               Platform.runLater(() -> {
                  this.tunnelLogTextarea.appendText("[INFO]通信隧道创建成功。\n");
               });
            } else {
               String msg = result.getString("msg");
               Platform.runLater(() -> {
                  this.tunnelLogTextarea.appendText("[ERROR]通信隧道创建失败：" + msg + "\n");
               });
            }
         } catch (Exception var7) {
            Platform.runLater(() -> {
               this.tunnelLogTextarea.appendText("[ERROR]通信隧道创建失败：" + var7.getMessage());
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
               TunnelViewController.ReversePortMapWorker reversePortMapWorker = (TunnelViewController.ReversePortMapWorker)var2.next();
               reversePortMapWorker.stop();
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

   class ProxyUtils extends Thread {
      private Thread r;
      private Thread w;
      private Thread proxy;
      private ServerSocket serverSocket;
      private int bufSize = 65535;

      private void log(String type, String log) {
         String logLine = "[" + type + "]" + log + "\n";
         Platform.runLater(() -> {
            TunnelViewController.this.tunnelLogTextarea.appendText(logLine);
         });
      }

      public void shutdown() {
         this.log("INFO", "正在关闭代理服务");

         try {
            if (this.r != null) {
               this.r.stop();
            }

            if (this.w != null) {
               this.w.stop();
            }

            if (this.proxy != null) {
               this.proxy.stop();
            }

            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
               this.serverSocket.close();
            }
         } catch (IOException var2) {
            this.log("ERROR", "代理服务关闭异常:" + var2.getMessage());
         }

         this.log("INFO", "代理服务已停止");
         TunnelViewController.this.createSocksBtn.setText("开启");
      }

      public void run() {
         try {
            String socksPort = TunnelViewController.this.socksPortText.getText();
            String socksIP = TunnelViewController.this.socksIPText.getText();
            this.proxy = Thread.currentThread();
            this.serverSocket = new ServerSocket(Integer.parseInt(socksPort), 50, InetAddress.getByName(socksIP));
            this.serverSocket.setReuseAddress(true);
            this.log("INFO", "正在监听端口" + socksPort);

            while(true) {
               while(true) {
                  try {
                     Socket socket = this.serverSocket.accept();
                     this.log("INFO", "收到客户端连接请求.");
                     (new TunnelViewController.ProxyUtils.Session(socket)).start();
                  } catch (NoSuchAlgorithmException var4) {
                     this.log("ERROR", "会话启动失败：" + var4.getMessage());
                  } catch (SocketException var5) {
                     this.log("INFO", "本地监听端口已关闭.");
                     return;
                  }
               }
            }
         } catch (IOException var6) {
            this.log("ERROR", "端口监听失败：" + var6.getMessage() + var6.getClass());
         }
      }

      private class Session extends Thread {
         private Socket socket;
         private String socketHash;

         public Session(Socket socket) throws NoSuchAlgorithmException {
            this.socket = socket;
            this.socketHash = Utils.getMD5("" + socket.getInetAddress() + socket.getPort() + "");
         }

         public void run() {
            try {
               if (this.handleSocks(this.socket)) {
                  ProxyUtils.this.log("INFO", "正在通信...");
                  ProxyUtils.this.r = new TunnelViewController.ProxyUtils.Session.Reader();
                  ProxyUtils.this.w = new TunnelViewController.ProxyUtils.Session.Writer();
                  ProxyUtils.this.r.start();
                  ProxyUtils.this.w.start();
                  ProxyUtils.this.r.join();
                  ProxyUtils.this.w.join();
               }
            } catch (Exception var4) {
               try {
                  TunnelViewController.this.currentShellService.closeProxy(this.socketHash);
               } catch (Exception var3) {
               }
            }

         }

         private boolean handleSocks(Socket socket) throws Exception {
            int ver = socket.getInputStream().read();
            if (ver == 5) {
               return this.parseSocks5(socket);
            } else {
               return ver == 4 ? this.parseSocks4(socket) : false;
            }
         }

         private boolean parseSocks5(Socket socket) throws Exception {
            DataInputStream ins = new DataInputStream(socket.getInputStream());
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            int nmethods = ins.read();

            for(int ix = 0; ix < nmethods; ++ix) {
               int var10 = ins.read();
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
               for(int i = 0; i < target.length; ++i) {
                  temp = target[i] & 255;
                  tempArray[i] = temp + "";
               }

               String[] var21 = tempArray;
               temp = tempArray.length;

               for(int var16 = 0; var16 < temp; ++var16) {
                  String tempx = var21[var16];
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
                  if (TunnelViewController.this.currentShellService.openProxy(host, port + "", this.socketHash)) {
                     os.write(CipherUtils.mergeByteArray(new byte[]{5, 0, 0, 1}, InetAddress.getByName(host).getAddress(), targetPort));
                     ProxyUtils.this.log("INFO", "隧道建立成功，请求远程地址" + host + ":" + port);
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

         private boolean parseSocks4(Socket socket) {
            return false;
         }

         private class Writer extends Thread {
            private Writer() {
            }

            public void run() {
               while(true) {
                  if (Session.this.socket != null) {
                     try {
                        Session.this.socket.setSoTimeout(10000);
                        byte[] data = new byte[ProxyUtils.this.bufSize];
                        int length = Session.this.socket.getInputStream().read(data);
                        if (length != -1) {
                           data = Arrays.copyOfRange(data, 0, length);
                           TunnelViewController.this.currentShellService.writeProxyData(data, Session.this.socketHash);
                           continue;
                        }
                     } catch (SocketTimeoutException var4) {
                        continue;
                     } catch (Exception var5) {
                        ProxyUtils.this.log("ERROR", "数据写入异常:" + var5.getMessage());
                     }
                  }

                  try {
                     TunnelViewController.this.currentShellService.closeProxy(Session.this.socketHash);
                     ProxyUtils.this.log("INFO", "隧道关闭成功。");
                     Session.this.socket.close();
                  } catch (Exception var3) {
                     ProxyUtils.this.log("ERROR", "隧道关闭失败:" + var3.getMessage());
                  }

                  return;
               }
            }

            // $FF: synthetic method
            Writer(Object x1) {
               this();
            }
         }

         private class Reader extends Thread {
            private Reader() {
            }

            public void run() {
               while(true) {
                  if (Session.this.socket != null) {
                     try {
                        byte[] data = TunnelViewController.this.currentShellService.readProxyData(Session.this.socketHash);
                        if (data != null) {
                           if (data.length != 0) {
                              Session.this.socket.getOutputStream().write(data);
                              Session.this.socket.getOutputStream().flush();
                           }
                           continue;
                        }
                     } catch (Exception var2) {
                        continue;
                     }
                  }

                  return;
               }
            }

            // $FF: synthetic method
            Reader(Object x1) {
               this();
            }
         }
      }
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

                  for(bytesRead = 0; bytesRead < socketArr.length(); ++bytesRead) {
                     JSONObject socketObj = socketArr.getJSONObject(bytesRead);
                     String socketHash = socketObj.getString("socketHash");
                     if (socketHash.startsWith("reverseportmap_socket") && !this.socketMetaList.containsKey(socketHash)) {
                        Map socketMeta = new HashMap();
                        socketMeta.put("status", "ready");
                        Socket socketx = new Socket(listenIP, listenPort);
                        socketMeta.put("status", "connected");
                        socketMeta.put("socket", socketx);
                        socketMeta.put("socketHash", socketHash);
                        this.socketMetaList.put(socketHash, socketMeta);
                        Map paramMap = new HashMap();
                        paramMap.put("socketMeta", socketMeta);
                        TunnelViewController.ReversePortMapWorker reversePortMapWorkerReader = TunnelViewController.this.new ReversePortMapWorker("read", paramMap);
                        TunnelViewController.ReversePortMapWorker reversePortMapWorkerWriter = TunnelViewController.this.new ReversePortMapWorker("write", paramMap);
                        TunnelViewController.this.ReversePortMapWorkerList.add(reversePortMapWorkerReader);
                        TunnelViewController.this.ReversePortMapWorkerList.add(reversePortMapWorkerWriter);
                        Thread reader = new Thread(reversePortMapWorkerReader);
                        Thread writer = new Thread(reversePortMapWorkerWriter);
                        TunnelViewController.this.workList.add(reader);
                        TunnelViewController.this.workList.add(writer);
                        reader.start();
                        writer.start();
                     }
                  }

                  Thread.sleep(3000L);
               } catch (Exception var19) {
                  break;
               }
            }
         } else {
            Map socketMetax;
            String socketHashx;
            Socket socket;
            byte[] buf;
            JSONObject var24;
            if (this.threadType.equals("read")) {
               socketMetax = (Map)this.paramMap.get("socketMeta");
               socketHashx = socketMetax.get("socketHash").toString();
               socket = (Socket)socketMetax.get("socket");

               while(true) {
                  try {
                     Thread.sleep(100L);
                     buf = TunnelViewController.this.currentShellService.readReversePortMapData(socketHashx);
                     socket.getOutputStream().write(buf);
                     socket.getOutputStream().flush();
                  } catch (Exception var18) {
                     try {
                        var24 = TunnelViewController.this.currentShellService.closeReversePortMap(socketHashx);
                     } catch (Exception var16) {
                     }
                     break;
                  }
               }
            } else if (this.threadType.equals("write")) {
               socketMetax = (Map)this.paramMap.get("socketMeta");
               socketHashx = socketMetax.get("socketHash").toString();
               socket = (Socket)socketMetax.get("socket");

               try {
                  buf = new byte[20480];

                  for(bytesRead = socket.getInputStream().read(buf); bytesRead > 0; bytesRead = socket.getInputStream().read(buf)) {
                     TunnelViewController.this.currentShellService.writeReversePortMapData(Arrays.copyOfRange(buf, 0, bytesRead), socketHashx);
                  }
               } catch (Exception var17) {
                  try {
                     var24 = TunnelViewController.this.currentShellService.closeReversePortMap(socketHashx);
                  } catch (Exception var15) {
                  }
               }
            }
         }

      }

      public void close(String listenIP, String listenPort) {
      }
   }
}
