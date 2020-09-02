// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.core;

import java.util.Base64;
import javafx.application.Platform;
import net.rebeyond.behinder.utils.Utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import javafx.scene.web.WebView;
import javafx.scene.control.Label;

public class PluginTools
{
    private ShellService currentShellService;
    private Label statusLabel;
    private WebView pluginWebview;
    private JSONObject shellEntity;
    private Map<String, String> taskMap;
    private List<Thread> workList;
    
    public PluginTools(final ShellService shellService, final WebView pluginWebview, final Label statusLabel, final List<Thread> workList) {
        this.taskMap = new HashMap<String, String>();
        this.currentShellService = shellService;
        this.shellEntity = shellService.shellEntity;
        this.workList = workList;
        this.pluginWebview = pluginWebview;
        this.statusLabel = statusLabel;
    }
    
    public PluginTools(final ShellService shellService, final Label statusLabel, final List<Thread> workList) {
        this.taskMap = new HashMap<String, String>();
        this.currentShellService = shellService;
        this.shellEntity = shellService.shellEntity;
        this.workList = workList;
        this.statusLabel = statusLabel;
    }
    
    public void sendTask(final String pluginName, final String paramStr) throws Exception {
        String type = this.shellEntity.getString("type");
        if (type.equals("jsp")) {
            type = "java";
        }
        final String payloadPath = String.format("/Users/rebeyond/Documents/Behinder/plugin/%s/payload/%s.payload", pluginName, type);
        final JSONObject paramObj = new JSONObject(paramStr);
        final Map<String, String> params = Utils.jsonToMap(paramObj);
        final String taskID = pluginName;
        params.put("taskID", taskID);
        this.statusLabel.setText("\u6b63\u5728\u6267\u884c\u63d2\u4ef6\u2026\u2026");
        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.submitPluginTask(taskID, payloadPath, params);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                Platform.runLater(() -> this.statusLabel.setText(msg));
            }
            catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> this.statusLabel.setText("\u63d2\u4ef6\u8fd0\u884c\u5931\u8d25"));
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    public void sendTaskBackground(final String pluginName, final Map<String, String> params, final PluginSubmitCallBack callBack) throws Exception {
        String type = this.shellEntity.getString("type");
        if (type.equals("jsp")) {
            type = "java";
        }
        final String payloadPath = String.format("/Users/rebeyond/Documents/Behinder/plugin/%s/payload/%s.payload", pluginName, type);
        final String taskID = pluginName;
        params.put("taskID", taskID);
        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.submitPluginTask(taskID, payloadPath, params);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                callBack.onPluginSubmit(status, msg);
            }
            catch (Exception e) {
                e.printStackTrace();
                callBack.onPluginSubmit("fail", e.getMessage());
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    public String queryTaskList() {
        final String result = "";
        return result;
    }
    
    public String queryTask(final String taskName) {
        final String result = "";
        return result;
    }
    
    public void getTaskResult(final String pluginName) {
        this.statusLabel.setText("\u6b63\u5728\u5237\u65b0\u4efb\u52a1\u6267\u884c\u7ed3\u679c\u2026\u2026");
        final Runnable runner = () -> {
            try {
                JSONObject resultObj = this.currentShellService.getPluginTaskResult(pluginName);
                String status = resultObj.getString("status");
                String msg = resultObj.getString("msg");
                JSONObject msgObj = new JSONObject(msg);
                String pluginResult = new String(Base64.getDecoder().decode(msgObj.getString("result")), "UTF-8");
                String pluginRunning = msgObj.getString("running");
                Platform.runLater(() -> {
                    if (status.equals("success")) {
                        this.statusLabel.setText("\u7ed3\u679c\u5237\u65b0\u6210\u529f");
                        try {
                            this.pluginWebview.getEngine().executeScript(String.format("onResult('%s','%s','%s')", status, pluginResult, pluginRunning));
                        }
                        catch (Exception e) {
                            this.statusLabel.setText("\u7ed3\u679c\u5237\u65b0\u6210\u529f\uff0c\u4f46\u662f\u63d2\u4ef6\u89e3\u6790\u7ed3\u679c\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u63d2\u4ef6:" + e.getMessage());
                        }
                    }
                    else {
                        this.statusLabel.setText("\u7ed3\u679c\u5237\u65b0\u5931\u8d25");
                    }
                });
            }
            catch (Exception e2) {
                e2.printStackTrace();
                Platform.runLater(() -> this.statusLabel.setText("\u7ed3\u679c\u5237\u65b0\u5931\u8d25:" + e2.getMessage()));
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
    
    public void getTaskResultBackground(final String pluginName, final PluginResultCallBack callBack) {
        final Runnable runner = () -> {
            String running = "true";
            try {
                while (running.equals("true")) {
                    JSONObject resultObj = this.currentShellService.getPluginTaskResult(pluginName);
                    String status = resultObj.getString("status");
                    String msg = resultObj.getString("msg");
                    JSONObject msgObj = new JSONObject(msg);
                    String pluginResult = new String(Base64.getDecoder().decode(msgObj.getString("result")), "UTF-8");
                    String pluginRunning = (running = msgObj.getString("running"));
                    callBack.onPluginResult(status, pluginResult, pluginRunning);
                    Thread.sleep(3000L);
                }
            }
            catch (Exception e) {
                callBack.onPluginResult("fail", e.getMessage(), "false");
            }
            return;
        };
        final Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }
}
