package net.rebeyond.behinder.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.service.callback.ITunnelCallBack;
import net.rebeyond.behinder.utils.CipherUtils;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class TunnelService {
   private Map localPortMapServerSocket = new HashMap();
   private Map localSocksProxyServerSocket = new HashMap();
   private IShellService currentShellService;
   private JSONObject shellEntity;
   private JSONObject effectShellEntity;
   private Map basicInfoMap;
   private Map localPortMapThreadMap = new HashMap();
   private Map localSocksProxyThreadMap = new HashMap();
   private List workList;
   private ITunnelCallBack callBack;
   private Map localPortMapSocketList = new HashMap();
   private Map localSocksProxySocketList = new HashMap();

   public TunnelService(IShellService shellService, List workList, ITunnelCallBack callBack) {
      this.currentShellService = shellService;
      this.effectShellEntity = this.currentShellService.getEffectShellEntity();
      this.workList = workList;
      this.callBack = callBack;
   }

   public void createLocalSocksProxy(String localIp, String localPort) {
      List serviceThreadList = (List)this.localSocksProxyThreadMap.get(localPort);
      if (serviceThreadList == null) {
         serviceThreadList = new ArrayList();
         this.localSocksProxyThreadMap.put(localPort, serviceThreadList);
      }

      Runnable runner = () -> {
         try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(InetAddress.getByName(localIp), Integer.parseInt(localPort)), 50);
            serverSocketChannel.socket().setReuseAddress(true);
            this.localSocksProxyServerSocket.put(localPort, serverSocketChannel);
            this.callBack.onInfo("正在监听本地端口:" + localPort);

            while(true) {
               SocketChannel socketChannel = serverSocketChannel.accept();
               List singleTaskList = new ArrayList();
               this.callBack.onInfo("收到客户端连接请求.");
               List socketChannelList = (List)this.localSocksProxySocketList.get(localPort);
               if (socketChannelList == null) {
                  socketChannelList = new ArrayList();
                  this.localSocksProxySocketList.put(localPort, socketChannelList);
               }

               ((List)socketChannelList).add(socketChannel);
               String socketHash = Utils.getMD5("" + socketChannel.socket().getInetAddress() + socketChannel.socket().getPort() + "");
               Runnable singeTunnelRunner = () -> {
                  try {
                     if (this.handleSocks(socketChannel.socket(), socketHash)) {
                        this.callBack.onInfo("正在通信...");
                        Runnable reader = () -> {
                           while(true) {
                              while(true) {
                                 while(true) {
                                    try {
                                       JSONObject responseObj = this.currentShellService.readProxyData(socketHash);
                                       if (responseObj.getString("status").equals("success")) {
                                          byte[] data = Base64.getDecoder().decode(responseObj.getString("msg"));
                                          if (data != null) {
                                             if (data.length == 0) {
                                                Thread.sleep(100L);
                                                continue;
                                             }

                                             socketChannel.write(ByteBuffer.wrap(data));
                                             continue;
                                          }
                                       } else {
                                          this.callBack.onInfo("远程TCP已完成，关闭本地通道。");
                                       }
                                       break;
                                    } catch (AsynchronousCloseException var7) {
                                    } catch (Exception var8) {
                                       if (var8 instanceof SocketException) {
                                          break;
                                       }

                                       var8.printStackTrace();
                                       this.callBack.onError("数据读取请求异常:" + var8.getMessage());
                                    }
                                 }

                                 try {
                                    socketChannel.close();
                                    this.currentShellService.closeProxy(socketHash);
                                    this.stopWorkers(singleTaskList);
                                 } catch (Exception var6) {
                                    var6.printStackTrace();
                                 }

                                 return;
                              }
                           }
                        };
                        Runnable writer = () -> {
                           label44:
                           while(true) {
                              while(true) {
                                 try {
                                    ByteBuffer buf = ByteBuffer.allocate(65535);
                                    int length = socketChannel.read(buf);
                                    if (length < 0) {
                                       break label44;
                                    }

                                    if (length != 0) {
                                       byte[] data = Arrays.copyOfRange(buf.array(), 0, length);
                                       this.currentShellService.writeProxyData(data, socketHash);
                                    }
                                 } catch (SocketTimeoutException var8) {
                                 } catch (AsynchronousCloseException var9) {
                                 } catch (ClosedChannelException var10) {
                                 } catch (Exception var11) {
                                    var11.printStackTrace();
                                    this.callBack.onError("数据写入请求异常:" + var11.getMessage());
                                    break label44;
                                 }
                              }
                           }

                           try {
                              this.currentShellService.closeProxy(socketHash);
                              socketChannel.close();
                              this.stopWorkers(singleTaskList);
                              this.callBack.onInfo("隧道关闭成功。");
                           } catch (Exception var7) {
                              this.callBack.onError("隧道关闭失败:" + var7.getMessage());
                              var7.printStackTrace();
                           }

                        };
                        Thread readWorker = new Thread(reader);
                        readWorker.setName("localSocksProxyWorker");
                        this.workList.add(readWorker);
                        readWorker.start();
                        Thread writeWorker = new Thread(writer);
                        writeWorker.setName("localSocksProxyWorker");
                        this.workList.add(writeWorker);
                        writeWorker.start();
                        singleTaskList.add(readWorker);
                        singleTaskList.add(writeWorker);
                        ((List) this.localSocksProxyThreadMap.get(localPort)).add(readWorker);
                        ((List) this.localSocksProxyThreadMap.get(localPort)).add(writeWorker);
                     }
                  } catch (Exception var9) {
                     var9.printStackTrace();
                  }

               };
               Thread worker = new Thread(singeTunnelRunner);
               worker.setName("localSocksProxyServer");
               this.workList.add(worker);
               worker.start();
               ((List) this.localSocksProxyThreadMap.get(localPort)).add(worker);
            }
         } catch (AsynchronousCloseException var11) {
         } catch (Exception var12) {
            this.callBack.onError("隧道创建失败:" + var12.getMessage());
            var12.printStackTrace();
         }

      };
      Thread worker = new Thread(runner);
      worker.setName("localSocksProxyServer");
      this.workList.add(worker);
      worker.start();
   }

   public void stopLocalSocksProxy(String localPort) {
      Runnable runner = () -> {
         try {
            this.stopLocalSocksProxyAllWorkers(localPort);
            this.currentShellService.clearProxy();
            this.callBack.onInfo("本地监听端口已关闭。");
         } catch (Exception var3) {
            var3.printStackTrace();
            this.callBack.onError("隧道关闭失败:" + var3.getMessage());
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   public void createLocalPortMap(String localIp, String localPort, String targetIp, String targetPort) {
      List threadList = new ArrayList();
      this.localPortMapThreadMap.put(localPort, threadList);
      Runnable runner = () -> {
         try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(InetAddress.getByName(localIp), Integer.parseInt(localPort)), 50);
            serverSocketChannel.socket().setReuseAddress(true);
            this.localPortMapServerSocket.put(localPort, serverSocketChannel);
            this.callBack.onInfo("正在监听本地端口:" + localPort);

            while(true) {
               SocketChannel socketChannel = serverSocketChannel.accept();
               List socketChannelList = (List)this.localPortMapSocketList.get(localPort);
               if (socketChannelList == null) {
                  socketChannelList = new ArrayList();
                  this.localPortMapSocketList.put(localPort, socketChannelList);
               }

               ((List)socketChannelList).add(socketChannel);
               List singleTaskList = new ArrayList();
               socketChannel.configureBlocking(true);
               String socketHash = Utils.getMD5("" + socketChannel.socket().getInetAddress() + socketChannel.socket().getPort() + "");
               Runnable reader = () -> {
                  int retry = 10;

                  label54:
                  while(true) {
                     while(true) {
                        try {
                           JSONObject responseObj = this.currentShellService.readPortMapData(targetIp, targetPort, socketHash);
                           if (responseObj.getString("status").equals("success")) {
                              byte[] data = Base64.getDecoder().decode(responseObj.getString("msg"));
                              if (data.length == 0) {
                                 Thread.sleep(100L);
                              } else {
                                 if (data == null) {
                                    break label54;
                                 }

                                 socketChannel.write(ByteBuffer.wrap(data));
                              }
                           } else {
                              String msg = responseObj.getString("msg");
                              if (!msg.equals("tunnel is not running in server") || retry <= 0) {
                                 this.callBack.onError("数据读取异常:" + responseObj.getString("msg"));
                                 break label54;
                              }

                              Thread.sleep(200L);
                              --retry;
                           }
                        } catch (Exception var11) {
                           var11.printStackTrace();
                           break label54;
                        }
                     }
                  }

                  try {
                     this.currentShellService.closeLocalPortMapWorker(socketHash);

                     try {
                        socketChannel.close();
                     } catch (Exception var9) {
                     }

                     this.stopWorkers(singleTaskList);
                  } catch (Exception var10) {
                     var10.printStackTrace();
                  }

               };
               Runnable writer = () -> {
                  label54:
                  while(true) {
                     while(true) {
                        try {
                           if (!socketChannel.isOpen() || !socketChannel.isConnected()) {
                              return;
                           }

                           ByteBuffer buf = ByteBuffer.allocate(65535);
                           int length = socketChannel.read(buf);
                           if (length < 0) {
                              break label54;
                           }

                           if (length != 0) {
                              byte[] data = Arrays.copyOfRange(buf.array(), 0, length);
                              this.currentShellService.writePortMapData(data, targetIp, targetPort, socketHash);
                           }
                        } catch (SocketTimeoutException var11) {
                        } catch (Exception var12) {
                           var12.printStackTrace();
                           this.callBack.onError("数据写入请求异常:" + var12.getMessage());
                           break label54;
                        }
                     }
                  }

                  try {
                     this.currentShellService.closeLocalPortMapWorker(socketHash);
                     this.callBack.onInfo("隧道关闭成功。");

                     try {
                        socketChannel.close();
                     } catch (Exception var9) {
                     }

                     this.stopWorkers(singleTaskList);
                  } catch (Exception var10) {
                     this.callBack.onError("隧道关闭失败:" + var10.getMessage());
                  }

               };
               Runnable backgroundRunner = () -> {
                  try {
                     (new Thread() {
                        public void run() {
                           try {
                              TunnelService.this.currentShellService.createPortMap(targetIp, targetPort, socketHash);
                           } catch (Exception var2) {
                           }

                        }
                     }).start();
                     Thread.sleep(200L);
                  } catch (Exception var10) {
                     var10.printStackTrace();
                  }

                  this.callBack.onInfo("隧道创建成功。");
                  Thread readWorker = new Thread(reader);
                  readWorker.setName("localPortMapWorker");
                  singleTaskList.add(readWorker);
                  threadList.add(readWorker);
                  this.workList.add(readWorker);
                  readWorker.start();
                  Thread writeWorker = new Thread(writer);
                  writeWorker.setName("localPortMapWorker");
                  singleTaskList.add(writeWorker);
                  threadList.add(writeWorker);
                  this.workList.add(writeWorker);
                  writeWorker.start();
               };
               (new Thread(backgroundRunner)).start();
            }
         } catch (AsynchronousCloseException var14) {
         } catch (Exception var15) {
            var15.printStackTrace();
         }

      };
      Thread worker = new Thread(runner);
      worker.setName("localPortMapServer");
      threadList.add(worker);
      this.workList.add(worker);
      worker.start();
   }

   public void stoplocalPortMap(String localPort, String targetIp, String targetProt) {
      Runnable runner = () -> {
         try {
            this.stopLocalPortMapAllWorkers(localPort);
            this.currentShellService.closeLocalPortMap(targetIp, targetProt);
            this.callBack.onInfo("本地监听端口已关闭。");
         } catch (Exception var5) {
            this.callBack.onError("隧道关闭失败:" + var5.getMessage());
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private void stopWorkers(List workList) {
      Iterator var2 = workList.iterator();

      while(var2.hasNext()) {
         Thread worker = (Thread)var2.next();
         worker.stop();
      }

   }

   private void stopLocalSocksProxyAllSockets(String localPort) {
      try {
         Iterator var2 = ((List)this.localSocksProxySocketList.get(localPort)).iterator();

         while(var2.hasNext()) {
            SocketChannel socketChannel = (SocketChannel)var2.next();

            try {
               socketChannel.close();
            } catch (Exception var9) {
            }
         }

         ((ServerSocketChannel)this.localSocksProxyServerSocket.get(localPort)).close();
      } catch (Exception var10) {
         var10.printStackTrace();
      } finally {
         this.localSocksProxySocketList.remove(localPort);
         this.localSocksProxyServerSocket.remove(localPort);
      }

   }

   private void stopLocalSocksProxyAllWorkers(String localPort) {
      this.stopLocalSocksProxyAllSockets(localPort);
      List threadList = (List)this.localSocksProxyThreadMap.get(localPort);
      Iterator var3 = threadList.iterator();

      while(var3.hasNext()) {
         Thread worker = (Thread)var3.next();
         worker.stop();
      }

   }

   private void stopLocalPortMapAllWorkers(String localPort) {
      this.stopLocalPortMapAllSockets(localPort);
      List threadList = (List)this.localPortMapThreadMap.get(localPort);
      Iterator var3 = threadList.iterator();

      while(var3.hasNext()) {
         Thread worker = (Thread)var3.next();
         worker.stop();
      }

   }

   private void stopLocalPortMapAllSockets(String localPort) {
      if (this.localPortMapSocketList != null && this.localPortMapSocketList.size() != 0) {
         try {
            Iterator var2 = ((List)this.localPortMapSocketList.get(localPort)).iterator();

            while(var2.hasNext()) {
               SocketChannel socketChannel = (SocketChannel)var2.next();
               socketChannel.close();
            }

            ((ServerSocketChannel)this.localPortMapServerSocket.get(localPort)).close();
         } catch (Exception var4) {
            var4.printStackTrace();
         }

      }
   }

   public void createRemotePortMap(String remoteIp, String remotePort, String targetIp, String targetPort) {
      Runnable runner = () -> {
         try {
            this.currentShellService.createRemotePortMap(targetIp, targetPort, remoteIp, remotePort);
            this.callBack.onInfo("隧道建立成功，请连接VPS。");
         } catch (Exception var6) {
            var6.printStackTrace();
            this.callBack.onError("隧道建立失败:" + var6.getMessage());
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   public void stopRemotePortMap() {
      Runnable runner = () -> {
         try {
            this.currentShellService.closeRemotePortMap();
            this.callBack.onInfo("隧道已关闭，远端相关资源已释放。");
         } catch (Exception var2) {
            var2.printStackTrace();
            this.callBack.onError("隧道关闭失败:" + var2.getMessage());
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   public void createRemoteSocks(String remoteIp, String remotePort) {
      Runnable runner = () -> {
         try {
            this.currentShellService.createVPSSocks(remoteIp, remotePort);
            this.callBack.onInfo("隧道建立成功，请使用SOCKS5客户端连接VPS。");
         } catch (Exception var4) {
            var4.printStackTrace();
            this.callBack.onError("隧道建立失败:" + var4.getMessage());
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   public void stopRemoteSocks() {
      Runnable runner = () -> {
         try {
            JSONObject result = this.currentShellService.stopVPSSocks();
            if (result.getString("status").equals("success")) {
               this.callBack.onInfo("隧道关闭成功，服务侧资源已释放。");
            } else {
               this.callBack.onError("隧道关闭失败：" + result.getString("msg"));
            }
         } catch (Exception var2) {
            this.callBack.onError("隧道关闭失败:" + var2.getMessage());
         }

      };
      Thread worker = new Thread(runner);
      this.workList.add(worker);
      worker.start();
   }

   private boolean handleSocks(Socket socket, String socketHash) throws Exception {
      int ver = socket.getInputStream().read();
      if (ver == 5) {
         return this.parseSocks5(socket, socketHash);
      } else {
         if (ver == 4) {
         }

         return false;
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
         for(int i = 0; i < target.length; ++i) {
            temp = target[i] & 255;
            tempArray[i] = temp + "";
         }

         String[] var23 = tempArray;
         temp = tempArray.length;

         for(int var17 = 0; var17 < temp; ++var17) {
            String tempstr = var23[var17];
            host = host + tempstr + ".";
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
            JSONObject responseObj;
            if (this.effectShellEntity.getString("type").equals("php")) {
               responseObj = this.currentShellService.openProxyAsyc(host, port + "", socketHash);
               Thread.sleep(2000L);
            } else {
               responseObj = this.currentShellService.openProxy(host, port + "", socketHash);
            }

            if (responseObj.getString("status").equals("success")) {
               os.write(CipherUtils.mergeByteArray(new byte[]{5, 0, 0, 1}, InetAddress.getByName(host).getAddress(), targetPort));
               this.callBack.onInfo("隧道建立成功，请求远程地址" + host + ":" + port);
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
}
