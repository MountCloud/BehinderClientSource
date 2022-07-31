package net.rebeyond.behinder.service;

import org.json.JSONObject;

public abstract class Task {
   protected String name;
   protected double progress = 0.5;
   protected JSONObject paramObj;

   public Task(String name, JSONObject paramObj) {
      this.name = name;
      this.paramObj = paramObj;
   }

   public abstract void update(int var1);

   public abstract void stop() throws Exception;

   public abstract void pause();

   public abstract void resume();

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public double getProgress() {
      return this.progress;
   }

   public void setProgress(double progress) {
      this.progress = progress;
   }

   public JSONObject getParamObj() {
      return this.paramObj;
   }

   public void setParamObj(JSONObject paramObj) {
      this.paramObj = paramObj;
   }
}
