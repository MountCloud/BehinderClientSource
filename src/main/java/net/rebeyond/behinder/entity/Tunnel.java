package net.rebeyond.behinder.entity;

import java.sql.Timestamp;
import net.rebeyond.behinder.dao.PrimaryKey;

public class Tunnel {
   private int id;
   @PrimaryKey
   private int type;
   @PrimaryKey
   private String targetIp;
   @PrimaryKey
   private String targetPort;
   @PrimaryKey
   private String remoteIp;
   @PrimaryKey
   private String remotePort;
   private int status;
   @PrimaryKey
   private int shellId;
   private Timestamp addTime;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getTargetIp() {
      return this.targetIp;
   }

   public void setTargetIp(String targetIp) {
      this.targetIp = targetIp;
   }

   public String getTargetPort() {
      return this.targetPort;
   }

   public void setTargetPort(String targetPort) {
      this.targetPort = targetPort;
   }

   public String getRemoteIp() {
      return this.remoteIp;
   }

   public void setRemoteIp(String remoteIp) {
      this.remoteIp = remoteIp;
   }

   public String getRemotePort() {
      return this.remotePort;
   }

   public void setRemotePort(String remotePort) {
      this.remotePort = remotePort;
   }

   public int getStatus() {
      return this.status;
   }

   public void setStatus(int status) {
      this.status = status;
   }

   public int getShellId() {
      return this.shellId;
   }

   public void setShellId(int shellId) {
      this.shellId = shellId;
   }

   public Timestamp getAddTime() {
      return this.addTime;
   }

   public void setAddTime(Timestamp addTime) {
      this.addTime = addTime;
   }
}
