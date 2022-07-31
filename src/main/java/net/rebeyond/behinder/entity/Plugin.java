package net.rebeyond.behinder.entity;

import net.rebeyond.behinder.dao.PrimaryKey;

public class Plugin {
   private int id;
   @PrimaryKey
   private String name;
   private String version;
   private String entryFile;
   @PrimaryKey
   private String scriptType;
   private int type;
   private String icon;
   private String author;
   private String link;
   private String qrcode;
   private int os;
   private String comment;
   private int status;

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getScriptType() {
      return this.scriptType;
   }

   public void setScriptType(String scriptType) {
      this.scriptType = scriptType;
   }

   public int getType() {
      return this.type;
   }

   public String getIcon() {
      return this.icon;
   }

   public void setIcon(String icon) {
      this.icon = icon;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getEntryFile() {
      return this.entryFile;
   }

   public void setEntryFile(String entryFile) {
      this.entryFile = entryFile;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getVersion() {
      return this.version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public String getLink() {
      return this.link;
   }

   public void setLink(String link) {
      this.link = link;
   }

   public String getQrcode() {
      return this.qrcode;
   }

   public void setQrcode(String qrcode) {
      this.qrcode = qrcode;
   }

   public int getOs() {
      return this.os;
   }

   public void setOs(int os) {
      this.os = os;
   }

   public int getStatus() {
      return this.status;
   }

   public void setStatus(int status) {
      this.status = status;
   }
}
