package net.rebeyond.behinder.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

public class ThreadTask extends Task {
   private List threadList = new ArrayList();

   public ThreadTask(String name, JSONObject paramObj) {
      super(name, paramObj);
   }

   public void update(int progress) {
   }

   public void stop() throws Exception {
      Iterator var1 = this.threadList.iterator();

      while(var1.hasNext()) {
         Thread thread = (Thread)var1.next();
         thread.stop();
      }

   }

   public void pause() {
   }

   public void resume() {
   }
}
