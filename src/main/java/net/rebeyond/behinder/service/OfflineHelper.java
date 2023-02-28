package net.rebeyond.behinder.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class OfflineHelper {
   private int shellId;
   private String rootPath;
   private Map fileOutputStreamMap;

   public OfflineHelper(int shellId) throws Exception {
      this.shellId = shellId;
      this.rootPath = this.getRootPath();
      File rootPathFile = new File(this.rootPath);
      if (!rootPathFile.exists()) {
         rootPathFile.mkdirs();
      }

      String cmdFile = this.rootPath + "cmd.json";
      this.fileOutputStreamMap = new HashMap();
   }

   private String getRootPath() throws Exception {
      String rootPath = Utils.getSelfPath("UTF-8") + File.separator + "offline" + File.separator + this.shellId + File.separator;
      return rootPath;
   }

   public void addRecord(String ip, Map params, JSONObject result) {
      try {
         String shellAction = Utils.getShellAction(4);
         this.doAddRecord(ip, shellAction, params, result);
      } catch (Exception var5) {
      }

   }

   private void doAddRecord(String ip, String shellAction, Map params, JSONObject result) throws Exception {
      JSONObject record = new JSONObject();
      record.put("shellId", this.shellId);
      record.put("target", ip);
      record.put("shellAction", shellAction);
      record.put("params", params);
      record.put("result", result);
      switch (shellAction) {
         case "runCmd":
         case "getBasicInfo":
         case "listFiles":
         case "showFile":
            this.addCommonRecord(record);
         default:
      }
   }

   private void addCommonRecord(JSONObject record) throws Exception {
      String shellAction = record.getString("shellAction");
      FileOutputStream fos = (FileOutputStream)this.fileOutputStreamMap.get(shellAction);
      if (fos == null) {
         String filePath = this.rootPath + shellAction + ".json";
         fos = new FileOutputStream(filePath, true);
         this.fileOutputStreamMap.put(shellAction, fos);
      }

      fos.write((record.toString() + "\n").getBytes());
      fos.flush();
   }

   private void addRunCmd(JSONObject record) throws Exception {
      String shellAction = record.getString("shellAction");
      FileOutputStream fos = (FileOutputStream)this.fileOutputStreamMap.get(shellAction);
      if (fos == null) {
         String filePath = this.rootPath + shellAction + ".json";
         fos = new FileOutputStream(filePath);
         this.fileOutputStreamMap.put(shellAction, fos);
      }

      fos.write((record.toString() + "\n").getBytes());
      fos.flush();
   }

   private void addBasicInfo(JSONObject record) throws Exception {
      String shellAction = record.getString("shellAction");
      FileOutputStream fos = (FileOutputStream)this.fileOutputStreamMap.get(shellAction);
      if (fos == null) {
         String filePath = this.rootPath + shellAction + ".json";
         fos = new FileOutputStream(filePath);
         this.fileOutputStreamMap.put(shellAction, fos);
      }

      fos.write((record.toString() + "\n").getBytes());
      fos.flush();
   }

   public void finalize() {
      Iterator var1 = this.fileOutputStreamMap.keySet().iterator();

      while(var1.hasNext()) {
         String key = (String)var1.next();
         FileOutputStream fos = (FileOutputStream)this.fileOutputStreamMap.get(key);

         try {
            fos.close();
         } catch (IOException var5) {
            var5.printStackTrace();
         }
      }

   }

   public byte[] readFile(String filePath) throws IOException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      FileInputStream fis = new FileInputStream(new File(filePath));
      byte[] buffer = new byte[10240000];
      int length = 0;

      while((length = fis.read(buffer)) > 0) {
         output.write(Arrays.copyOfRange(buffer, 0, length));
      }

      fis.close();
      return output.toByteArray();
   }

   public JSONObject getRecord(String shellAction, String target, Map params) throws Exception {
      JSONObject result = null;
      switch (shellAction) {
         case "runCmd":
         case "getBasicInfo":
         case "listFiles":
         case "showFile":
            result = this.getCommonRecord(shellAction, target, params);
            break;
         default:
            result = this.getCommonRecord(shellAction, target, params);
      }

      return result;
   }

   public JSONObject getCommonRecord(String shellAction, String target, Map params) throws Exception {
      String lines = new String(this.readFile(this.rootPath + shellAction + ".json"));
      JSONObject record = null;
      String[] lineArr = lines.split("\n");

      for(int i = lineArr.length - 1; i >= 0; --i) {
         record = new JSONObject(lineArr[i]);
         if (record.get("params").toString().equals((new JSONObject(params)).toString())) {
            return record.getJSONObject("result");
         }
      }

      record.put("status", "failed");
      record.put("msg", "该记录未在离线模式中缓存。");
      throw new Exception("该记录未在离线模式中缓存。");
   }
}
