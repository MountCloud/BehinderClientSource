package net.rebeyond.behinder.entity;

public class ShellCode {
   private int id;
   private String name;
   private String body;
   private int platform;
   private int scriptType;
   private int type;
   private String key;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getBody() {
      return this.body;
   }

   public void setBody(String body) {
      this.body = body;
   }

   public int getPlatform() {
      return this.platform;
   }

   public void setPlatform(int platform) {
      this.platform = platform;
   }

   public int getScriptType() {
      return this.scriptType;
   }

   public void setScriptType(int scriptType) {
      this.scriptType = scriptType;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getKey() {
      return this.key;
   }

   public void setKey(String key) {
      this.key = key;
   }
}
