package net.rebeyond.behinder.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ICrypt;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.entity.BShell;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class OfflineService implements IShellService {
   public String currentUrl;
   public String currentPassword;
   public String currentKey;
   public String currentType;
   public String childType;
   public String effectType;
   public Map currentHeaders;
   public int encryptType;
   public int beginIndex;
   public int endIndex;
   public JSONObject shellEntity;
   public static int BUFFSIZE = 46080;
   public static Map currentProxy;
   private boolean needTransfer;
   private BShell currentBShell;
   private JSONObject effectShellEntity;
   private OfflineHelper offlineHelper;
   private List childList;
   private List shellChains;

   public void setProxy(Map proxy) {
   }

   public Map getProxy(Map proxy) {
      return null;
   }

   public List getChildList() {
      return this.childList;
   }

   public void setChildList(List childList) {
      this.childList = childList;
   }

   public ICrypt getCryptor() {
      return null;
   }

   public OfflineService(JSONObject shellEntity) throws Exception {
      this.encryptType = Constants.ENCRYPT_TYPE_AES;
      this.beginIndex = 0;
      this.endIndex = 0;
      this.needTransfer = false;
      this.shellChains = new ArrayList();
      this.shellEntity = shellEntity;
      this.effectShellEntity = shellEntity;
      this.currentUrl = shellEntity.getString("url");
      this.currentType = shellEntity.getString("type");
      this.currentPassword = shellEntity.getString("password");
      this.currentHeaders = new HashMap();
      this.effectType = this.currentType;
      this.offlineHelper = new OfflineHelper(shellEntity.getInt("id"));
   }

   public OfflineService(JSONObject shellEntity, List childList) throws Exception {
      this.encryptType = Constants.ENCRYPT_TYPE_AES;
      this.beginIndex = 0;
      this.endIndex = 0;
      this.needTransfer = false;
      this.shellChains = new ArrayList();
      this.shellEntity = shellEntity;
      this.currentUrl = shellEntity.getString("url");
      this.currentType = shellEntity.getString("type");
      this.currentPassword = shellEntity.getString("password");
      this.currentHeaders = new HashMap();
      this.needTransfer = true;
      this.childList = childList;
      this.shellChains.add(shellEntity);
      Iterator var3 = childList.iterator();

      while(var3.hasNext()) {
         Map childObj = (Map)var3.next();
         this.shellChains.add((JSONObject)childObj.get("childShellEntity"));
      }

      this.effectShellEntity = (JSONObject)((Map)Utils.getLastOfList(childList)).get("childShellEntity");
      this.effectType = this.effectShellEntity.getString("type");
      this.currentBShell = (BShell)this.effectShellEntity.get("bShell");
      this.offlineHelper = new OfflineHelper(shellEntity.getInt("id"));
   }

   public JSONObject getShellEntity() {
      return this.shellEntity;
   }

   public JSONObject getEffectShellEntity() {
      return this.effectShellEntity;
   }

   public boolean doConnect() throws Exception {
      return true;
   }

   public String evalWithTransProtocol(String sourceCode) throws Exception {
      return null;
   }

   public String eval(String sourceCode) throws Exception {
      Map params = new LinkedHashMap();
      params.put("sourceCode", sourceCode);
      JSONObject record = this.offlineHelper.getRecord("eval", this.effectShellEntity.getString("url"), params);
      return record.getString("msg");
   }

   public JSONObject runCmd(String cmd, String path) throws Exception {
      Map params = new LinkedHashMap();
      params.put("cmd", cmd);
      params.put("path", path);
      JSONObject record = this.offlineHelper.getRecord("runCmd", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject createBShell(String target) throws Exception {
      return null;
   }

   public JSONObject listBShell() throws Exception {
      return null;
   }

   public JSONObject listReverseBShell() throws Exception {
      return null;
   }

   public JSONObject listenBShell(String listenPort) throws Exception {
      return null;
   }

   public JSONObject closeBShell(String target, String type) throws Exception {
      return null;
   }

   public JSONObject stopReverseBShell() throws Exception {
      return null;
   }

   public JSONObject sendBShellCommand(String target, String action, String actionParams) throws Exception {
      return null;
   }

   public JSONObject submitPluginTask(String taskID, String payloadPath, Map pluginParams) throws Exception {
      return null;
   }

   public JSONObject execPluginTask(String taskID, String payloadPath, Map pluginParams) throws Exception {
      Map params = new HashMap();
      params.put("taskID", taskID);
      params.put("action", "exec");
      byte[] pluginData = Utils.getPluginData(this.currentKey, payloadPath, pluginParams, this.currentType);
      params.put("payload", Base64.getEncoder().encodeToString(pluginData));
      JSONObject record = this.offlineHelper.getRecord("execPluginTask", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject getPluginTaskResult(String taskID) throws Exception {
      Map params = new LinkedHashMap();
      params.put("taskID", taskID);
      params.put("action", "getResult");
      JSONObject record = this.offlineHelper.getRecord("getPluginTaskResult", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject stopPluginTask(String taskID) throws Exception {
      return null;
   }

   public JSONObject loadJar(String libPath) throws Exception {
      return null;
   }

   public JSONObject createRealCMD(String bashPath) throws Exception {
      return null;
   }

   public JSONObject stopRealCMD() throws Exception {
      return null;
   }

   public JSONObject readRealCMD() throws Exception {
      return null;
   }

   public JSONObject writeRealCMD(String cmd) throws Exception {
      return null;
   }

   public JSONObject listFiles(String path) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "list");
      params.put("path", path);
      JSONObject record = this.offlineHelper.getRecord("listFiles", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject checkFileHash(String path, String hash) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "check");
      params.put("path", path);
      params.put("hash", hash);
      JSONObject record = this.offlineHelper.getRecord("checkFileHash", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject getTimeStamp(String path) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "getTimeStamp");
      params.put("path", path);
      JSONObject record = this.offlineHelper.getRecord("getTimeStamp", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject updateTimeStamp(String path, String createTimeStamp, String modifyTimeStamp, String accessTimeStamp) throws Exception {
      return null;
   }

   public JSONObject updateModifyTimeStamp(String path, String modifyTimeStamp) throws Exception {
      return null;
   }

   public JSONObject compress(String path) throws Exception {
      return null;
   }

   public JSONObject deleteFile(String path) throws Exception {
      return null;
   }

   public JSONObject showFile(String path, String charset) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "show");
      params.put("path", path);
      if (charset != null) {
         params.put("charset", charset);
      }

      JSONObject record = this.offlineHelper.getRecord("showFile", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject checkFileExist(String path) throws Exception {
      Map params = new LinkedHashMap();
      params.put("mode", "checkExist");
      params.put("path", path);
      JSONObject record = this.offlineHelper.getRecord("checkFileExist", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject renameFile(String oldName, String newName) throws Exception {
      return null;
   }

   public JSONObject createFile(String fileName) throws Exception {
      return null;
   }

   public JSONObject createDirectory(String dirName) throws Exception {
      return null;
   }

   public void downloadFile(String remotePath, String localPath) throws Exception {
   }

   public JSONObject downFilePart(String remotePath, long blockIndex, long blockSize) throws Exception {
      return null;
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
      JSONObject record = this.offlineHelper.getRecord("execSQL", this.effectShellEntity.getString("url"), params);
      return record;
   }

   public JSONObject uploadFile(String remotePath, byte[] fileContent, boolean useBlock) throws Exception {
      return null;
   }

   public JSONObject uploadFile(String remotePath, byte[] fileContent) throws Exception {
      return null;
   }

   public JSONObject appendFile(String remotePath, byte[] fileContent) throws Exception {
      return null;
   }

   public JSONObject uploadFilePart(String remotePath, byte[] fileContent, long blockIndex, long blockSize) throws Exception {
      return null;
   }

   public boolean checkClassExist(String className) throws Exception {
      return false;
   }

   public JSONObject createRemotePortMap(String targetIP, String targetPort, String remoteIP, String remotePort) throws Exception {
      return null;
   }

   public JSONObject createRemoteSocks(String targetIP, String targetPort, String remoteIP, String remotePort) throws Exception {
      return null;
   }

   public JSONObject createVPSSocks(String remoteIP, String remotePort) throws Exception {
      return null;
   }

   public JSONObject stopVPSSocks() throws Exception {
      return null;
   }

   public JSONObject createPortMap(String targetIP, String targetPort, String socketHash) throws Exception {
      return null;
   }

   public JSONObject readPortMapData(String targetIP, String targetPort, String socketHash) throws Exception {
      return null;
   }

   public JSONObject writePortMapData(byte[] proxyData, String targetIP, String targetPort, String socketHash) throws Exception {
      return null;
   }

   public JSONObject closeLocalPortMap(String targetIP, String targetPort) throws Exception {
      return null;
   }

   public boolean closeLocalPortMapWorker(String socketHash) throws Exception {
      return true;
   }

   public boolean closeRemotePortMap() throws Exception {
      return true;
   }

   public JSONObject readProxyData(String socketHash) throws Exception {
      return null;
   }

   public JSONObject writeProxyData(byte[] proxyData, String socketHash) throws Exception {
      return null;
   }

   public JSONObject closeProxy(String socketHash) throws Exception {
      return null;
   }

   public JSONObject clearProxy() throws Exception {
      return null;
   }

   public JSONObject openProxy(String destHost, String destPort, String socketHash) throws Exception {
      return null;
   }

   public JSONObject openProxyAsyc(String destHost, String destPort, String socketHash) throws Exception {
      return null;
   }

   public JSONObject echo(String content) throws Exception {
      return null;
   }

   public JSONObject getBasicInfo(String whatever) throws Exception {
      Map params = new LinkedHashMap();
      params.put("whatever", whatever);
      JSONObject record = this.offlineHelper.getRecord("getBasicInfo", this.effectShellEntity.getString("url"), (Map)null);
      return record;
   }

   public void keepAlive() throws Exception {
   }

   public JSONObject connectBack(String type, String ip, String port) throws Exception {
      return null;
   }

   public JSONObject loadNativeLibrary(String libraryPath) throws Exception {
      return null;
   }

   public JSONObject executePayload(String uploadLibPath, String payload) throws Exception {
      return null;
   }

   public JSONObject loadLibraryAndexecutePayload(String fileContent, String payload) throws Exception {
      return null;
   }

   public JSONObject loadLibraryAndfreeFile(String fileContent, String filePath) throws Exception {
      return null;
   }

   public JSONObject freeFile(String uploadLibPath, String filePath) throws Exception {
      return null;
   }

   public JSONObject loadLibraryAndAntiAgent(String fileContent) throws Exception {
      return null;
   }

   public JSONObject antiAgent(String uploadLibPath) throws Exception {
      return null;
   }

   public JSONObject loadLibraryAndtest() throws Exception {
      return null;
   }

   public JSONObject getMemShellTargetClass() throws Exception {
      return null;
   }

   public JSONObject injectAgentNoFileMemShell(String className, String classBody, boolean isAntiAgent) throws Exception {
      return null;
   }

   public JSONObject injectAgentMemShell(String libPath, String path, String password, boolean isAntiAgent) throws Exception {
      return null;
   }

   public JSONObject createReversePortMap(String listenPort) throws Exception {
      return null;
   }

   public JSONObject readReversePortMapData(String socketHash) throws Exception {
      return null;
   }

   public boolean writeReversePortMapData(byte[] proxyData, String socketHash) throws Exception {
      return false;
   }

   public JSONObject listReversePortMap() throws Exception {
      return null;
   }

   public JSONObject stopReversePortMap(String listenPort) throws Exception {
      return null;
   }

   public JSONObject closeReversePortMap(String socketHash) throws Exception {
      return null;
   }

   public byte[] warpTransferPayload(byte[] payloadBody, String scriptType, String target, String type, String direction, String effectHeaders) throws Exception {
      return null;
   }

   public Map transferPayload(byte[] payloadBody) throws Exception {
      return null;
   }

   public JSONObject doProxy(String type, String target, String payloadBody) throws Exception {
      return null;
   }
}
