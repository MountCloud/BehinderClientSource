package net.rebeyond.behinder.service;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.TunnelDao;
import net.rebeyond.behinder.entity.Tunnel;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class BShellService {
   private ShellService currentShellService;
   private PluginService pluginService;
   private TunnelService tunnelService;
   private TunnelDao tunnelDao = new TunnelDao();

   public JSONObject execCommand(String cmd) throws Exception {
      JSONObject result = this.currentShellService.runCmd(cmd, ".");
      return result;
   }

   public JSONObject execPlugin(String pluginName, JSONObject params) throws Exception {
      return null;
   }

   public JSONObject info() throws Exception {
      String randStr = Utils.getRandomString((new Random()).nextInt(300));
      JSONObject result = new JSONObject(this.currentShellService.getBasicInfo(randStr));
      return result;
   }

   public JSONObject listFiles(String path) throws Exception {
      JSONObject result = this.currentShellService.listFiles(path);
      return result;
   }

   public JSONObject showFile(String path, String charset) throws Exception {
      JSONObject result = this.currentShellService.showFile(path, charset);
      return result;
   }

   public void download(String remotePath, String localPath) throws Exception {
      this.currentShellService.downloadFile(remotePath, localPath);
   }

   public JSONObject upload(String localPath, String remotePath) throws Exception {
      byte[] fileContent = Utils.getFileData(localPath);
      JSONObject result;
      if (fileContent.length > Constants.FILE_BLOCK_MAX_SIZE) {
         result = this.currentShellService.uploadFile(remotePath, fileContent, true);
      } else {
         result = this.currentShellService.uploadFile(remotePath, fileContent);
      }

      return result;
   }

   public boolean portmap(String targetIp, String targetPort, String remoteIp, String remotePort, String type) throws Exception {
      boolean result = false;
      Tunnel tunnel = new Tunnel();
      tunnel.setRemoteIp(remoteIp);
      tunnel.setRemotePort(remotePort);
      tunnel.setTargetIp(targetIp);
      tunnel.setTargetPort(targetPort);
      tunnel.setAddTime(Utils.getCurrentDate());
      if (type.equals("remote")) {
         JSONObject resObj = this.currentShellService.createRemotePortMap(targetIp, targetPort, remoteIp, remotePort);
         result = resObj.getString("status").equals("success");
         tunnel.setType(Constants.TUNNEL_TYPE_PORTMAP_REMOTE);
         tunnel.setStatus(Constants.TUNNEL_STATUS_ALIVE);
      } else if (type.equals("local")) {
         this.tunnelService.createLocalPortMap(remoteIp, remotePort, targetIp, targetPort);
         tunnel.setType(Constants.TUNNEL_TYPE_PORTMAP_LOCAL);
         tunnel.setStatus(Constants.TUNNEL_STATUS_ALIVE);
      }

      this.tunnelDao.addEntity(tunnel);
      return result;
   }

   public String help() {
      String content = "";
      return content;
   }

   public JSONObject invoke(String line) {
      Pattern pattern = Pattern.compile("^([^\\s]*)[\\s]*([^\\s]*[\\s]*)*$");
      Matcher matcher = pattern.matcher(line);

      while(matcher.find()) {
      }

      return null;
   }
}
