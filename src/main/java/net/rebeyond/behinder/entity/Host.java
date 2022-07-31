package net.rebeyond.behinder.entity;

import java.sql.Timestamp;
import net.rebeyond.behinder.dao.PrimaryKey;

public class Host {
   private int id;
   @PrimaryKey
   private int shellId;
   @PrimaryKey
   private String ip;
   private String os;
   private int status;
   private String comment;
   private Timestamp accessTime;
   private Timestamp updateTime;
   private Timestamp addTime;

   public int getShellId() {
      return this.shellId;
   }

   public void setShellId(int shellId) {
      this.shellId = shellId;
   }

   public Timestamp getAccessTime() {
      return this.accessTime;
   }

   public void setAccessTime(Timestamp accessTime) {
      this.accessTime = accessTime;
   }

   public Timestamp getUpdateTime() {
      return this.updateTime;
   }

   public void setUpdateTime(Timestamp updateTime) {
      this.updateTime = updateTime;
   }

   public Timestamp getAddTime() {
      return this.addTime;
   }

   public void setAddTime(Timestamp addTime) {
      this.addTime = addTime;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getIp() {
      return this.ip;
   }

   public void setIp(String ip) {
      this.ip = ip;
   }

   public String getOs() {
      return this.os;
   }

   public void setOs(String os) {
      this.os = os;
   }

   public int getStatus() {
      return this.status;
   }

   public void setStatus(int status) {
      this.status = status;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }
}
