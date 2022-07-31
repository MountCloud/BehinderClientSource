package net.rebeyond.behinder.entity;

import java.sql.Timestamp;
import net.rebeyond.behinder.dao.PrimaryKey;

public class BShell {
   private int id;
   @PrimaryKey
   private String name;
   private int type;
   @PrimaryKey
   private String body;
   private int status;
   @PrimaryKey
   private int hostId;
   private Timestamp addTime;

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

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getBody() {
      return this.body;
   }

   public void setBody(String body) {
      this.body = body;
   }

   public int getStatus() {
      return this.status;
   }

   public void setStatus(int status) {
      this.status = status;
   }

   public int getHostId() {
      return this.hostId;
   }

   public void setHostId(int hostId) {
      this.hostId = hostId;
   }

   public Timestamp getAddTime() {
      return this.addTime;
   }

   public void setAddTime(Timestamp addTime) {
      this.addTime = addTime;
   }
}
