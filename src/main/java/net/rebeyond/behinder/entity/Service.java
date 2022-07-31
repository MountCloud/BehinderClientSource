package net.rebeyond.behinder.entity;

import java.sql.Timestamp;
import net.rebeyond.behinder.dao.PrimaryKey;

public class Service {
   private int id;
   @PrimaryKey
   private int hostId;
   private String name;
   private String version;
   @PrimaryKey
   private int port;
   private String banner;
   private Timestamp addTime;
   private int status;
   private String comment;

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

   public String getVersion() {
      return this.version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public int getPort() {
      return this.port;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public String getBanner() {
      return this.banner;
   }

   public void setBanner(String banner) {
      this.banner = banner;
   }

   public Timestamp getAddTime() {
      return this.addTime;
   }

   public void setAddTime(Timestamp addTime) {
      this.addTime = addTime;
   }

   public int getHostId() {
      return this.hostId;
   }

   public void setHostId(int hostId) {
      this.hostId = hostId;
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
