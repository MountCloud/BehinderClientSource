package net.rebeyond.behinder.service;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rebeyond.behinder.core.IShellService;
import org.json.JSONObject;

public class PortScanService implements Runnable {
   public static String hostList = "127.0.0.1";
   public static String portList = "79-82";
   public static String probeFilePath = "/Users/rebeyond/probes.ser";
   public static String threadSize = "1";
   private BlockingQueue blockingQueue = new LinkedBlockingQueue();
   private Map taskResult;
   private IShellService currentShellService;
   public static List probeArr;
   private static int SOCKET_READ_TIME_OUT = 5000;
   private static int SOCKET_NULL_PROBE_READ_TIME_OUT = 6000;

   public PortScanService(BlockingQueue blockingQueue, Map taskResult, IShellService shellService) {
      this.blockingQueue = blockingQueue;
      this.taskResult = taskResult;
      this.currentShellService = shellService;
   }

   public PortScanService() {
   }

   public void dispatch() {
      portList = this.extendPorts(portList);
      String[] var1 = hostList.split(",");
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String host = var1[var3];
         String[] var5 = portList.split(",");
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String port = var5[var7];
            String taskLine = host.trim() + ":" + port.trim();

            try {
               this.blockingQueue.put(taskLine);
            } catch (InterruptedException var11) {
               var11.printStackTrace();
            }
         }
      }

   }

   public void execute(IShellService shellService, Map taskResult) throws Exception {
      this.dispatch();

      try {
         this.initProbes();
      } catch (Exception var9) {
         throw new Exception("服务指纹初始化异常:" + var9.getMessage());
      }

      List serviceList = new ArrayList();
      taskResult.put("running", "true");
      taskResult.put("result", serviceList);
      int taskQueueSize = this.blockingQueue.size();
      ExecutorService executorService = Executors.newCachedThreadPool();
      int threadSizeInt = Integer.parseInt(threadSize);
      if (threadSizeInt > taskQueueSize) {
         threadSizeInt = taskQueueSize;
      }

      PortScanService serviceScan = new PortScanService(this.blockingQueue, taskResult, shellService);

      for(int i = 0; i < threadSizeInt; ++i) {
         executorService.submit(serviceScan);
      }

      executorService.shutdown();
      executorService.awaitTermination(24L, TimeUnit.HOURS);
      taskResult.put("running", "false");
   }

   private void initProbes() throws Exception {
      FileInputStream fis = new FileInputStream(probeFilePath);
      ObjectInputStream ois = new ObjectInputStream(fis);
      probeArr = (List)ois.readObject();
      ois.close();
      fis.close();
   }

   public static void main(String[] args) throws Exception {
      new PortScanService();
   }

   private Map checkProbe(String type, String target, Map probe) throws Exception {
      Map result = new HashMap();
      result.put("status", "fail");
      String challenge = probe.get("challenge").toString();
      List matchArr = (List)probe.get("matches");
      byte[] data = this.warpChallenge(challenge);
      JSONObject responseObj = this.currentShellService.doProxy(type, target, Base64.getEncoder().encodeToString(data));
      if (responseObj.getString("status").equals("success")) {
         byte[] resData = Base64.getDecoder().decode(responseObj.getString("msg"));
         String content = new String(resData, "ISO-8859-1");
         result.put("banner", content);

         for(int i = 0; i < matchArr.size(); ++i) {
            Map matchObj = (Map)matchArr.get(i);
            String regex = matchObj.get("regex").toString();
            String option = matchObj.get("option").toString();

            try {
               Pattern pattern = null;
               if (option.equals("i")) {
                  pattern = Pattern.compile(regex, 2);
               } else if (option.equals("s")) {
                  pattern = Pattern.compile(regex, 32);
               } else if (!option.equals("si") && !option.equals("is")) {
                  pattern = Pattern.compile(regex);
               } else {
                  pattern = Pattern.compile(regex, 34);
               }

               Matcher matcher = pattern.matcher(content);
               if (matcher.find()) {
                  result.put("status", "success");
                  result.put("service", matchObj.get("service").toString());
                  result.put("version", this.fillVersion(matcher, (Map)matchObj.get("version")));
                  return result;
               }
            } catch (Exception var17) {
            }
         }
      } else {
         result.put("status", "fail");
         result.put("msg", responseObj.getString("msg"));
      }

      return result;
   }

   private byte[] warpChallenge(String challenge) throws UnsupportedEncodingException {
      challenge = challenge.replace("\\r", "\r").replace("\\n", "\n");
      Pattern pattern = Pattern.compile("\\\\x([0-9a-fA-F]{2})");
      Matcher matcher = pattern.matcher(challenge);

      while(matcher.find()) {
         String hex = matcher.group(1);
         int c = Integer.parseInt(hex, 16);

         try {
            challenge = challenge.replace("\\x" + hex, new String(new byte[]{(byte)c}, "ISO-8859-1"));
         } catch (UnsupportedEncodingException var7) {
            var7.printStackTrace();
         }
      }

      return challenge.getBytes("ISO-8859-1");
   }

   private List buildProbeIndexList(List probeArr, int port) {
      List result = new ArrayList();

      for(int i = 0; i < probeArr.size(); ++i) {
         Map probeObj = (Map)probeArr.get(i);
         if (probeObj.containsKey("ports") || probeObj.containsKey("sslports")) {
            String ports = "," + probeObj.get("ports") + "," + probeObj.get("sslports") + ",";
            ports = this.extendPorts(ports);
            if (ports.indexOf("," + port + ",") >= 0) {
               result.add(0, i);
               continue;
            }
         }

         result.add(i);
      }

      return result;
   }

   private String extendPorts(String portList) {
      if (portList.indexOf("-") > 0) {
         Pattern pattern = Pattern.compile("(\\d*)-(\\d*)");

         StringBuilder sb;
         for(Matcher matcher = pattern.matcher(portList); matcher.find(); portList = portList.replace(matcher.group(), sb.toString())) {
            int start = Integer.parseInt(matcher.group(1));
            int stop = Integer.parseInt(matcher.group(2));

            for(sb = new StringBuilder(); start <= stop; ++start) {
               sb.append(start + ",");
            }
         }
      }

      return portList;
   }

   private String fillVersion(Matcher matcher, Map versionObj) {
      Iterator var3 = versionObj.keySet().iterator();

      while(true) {
         String key;
         String value;
         int index;
         do {
            if (!var3.hasNext()) {
               return versionObj.toString();
            }

            key = (String)var3.next();
            value = ((String)versionObj.get(key)).toString();
            index = value.indexOf("$");
         } while(index < 0);

         Pattern pattern = Pattern.compile("\\$(\\d*)");
         Matcher m = pattern.matcher(value);

         while(m.find()) {
            int seq = Integer.parseInt(m.group(1));
            value = value.replace("$" + m.group(1), matcher.group(seq));
            versionObj.put(key, value);
         }
      }
   }

   public void run() {
      while(!this.taskResult.get("running").equals("false")) {
         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var21) {
            var21.printStackTrace();
         }

         String taskLine = null;

         try {
            taskLine = (String)this.blockingQueue.poll(1500L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException var20) {
            continue;
         }

         if (taskLine == null) {
            return;
         }

         String host = taskLine.split(":")[0];
         int port = Integer.parseInt(taskLine.split(":")[1]);
         String target = taskLine;
         SocketAddress socketAddress = new InetSocketAddress(host, port);
         List probeIndexList = this.buildProbeIndexList(probeArr, port);
         String finalBanner = "";
         boolean skipTCP = false;
         boolean skipUDP = false;
         Iterator var10 = probeIndexList.iterator();

         while(var10.hasNext()) {
            int index = (Integer)var10.next();
            Map probeObj = (Map)probeArr.get(index);
            String type = probeObj.get("type").toString();
            if ((!type.equals("TCP") || !skipTCP) && (!type.equals("UDP") || !skipUDP)) {
               try {
                  Map result = this.checkProbe(type, target, probeObj);
                  result.put("type", type);
                  if (((String)result.get("status")).equals("success")) {
                     result.put("host", ((InetSocketAddress)socketAddress).getHostString());
                     result.put("port", ((InetSocketAddress)socketAddress).getPort() + "");
                     synchronized(this) {
                        ((List)this.taskResult.get("result")).add(result);
                        break;
                     }
                  }

                  String msg = ((String)result.get("msg")).toString();
                  if (msg.equals("closed")) {
                     if (type.equals("TCP")) {
                        skipTCP = true;
                     }

                     if (type.equals("UDF")) {
                        skipUDP = true;
                     }
                  } else if (result.containsKey("banner") && !((String)result.get("banner")).equals("")) {
                     finalBanner = (String)result.get("banner");
                  }
               } catch (Exception var22) {
                  var22.printStackTrace();
               }
            }
         }

         Map fallBackResult = new HashMap();
         fallBackResult.put("status", "fail");
         fallBackResult.put("banner", finalBanner);
         fallBackResult.put("host", ((InetSocketAddress)socketAddress).getHostString());
         fallBackResult.put("port", ((InetSocketAddress)socketAddress).getPort() + "");
         synchronized(this) {
            ((List)this.taskResult.get("result")).add(fallBackResult);
         }
      }

   }
}
