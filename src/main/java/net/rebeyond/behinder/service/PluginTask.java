package net.rebeyond.behinder.service;

import org.json.JSONObject;

public class PluginTask extends Task {
   private PluginService pluginService;

   public PluginTask(String name, JSONObject paramObj) {
      super(name, paramObj);
   }

   public void update(int progress) {
   }

   public void stop() throws Exception {
      String pluginName = this.paramObj.getString("pluginName");
      JSONObject result = this.pluginService.stopTask(pluginName);
      if (!result.getString("status").equals("success")) {
         String errorMsg = result.getString("msg");
         throw new Exception(errorMsg);
      }
   }

   public void pause() {
   }

   public void resume() {
   }
}
