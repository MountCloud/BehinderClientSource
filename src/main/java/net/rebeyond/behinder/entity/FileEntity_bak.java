package net.rebeyond.behinder.entity;

import java.sql.Timestamp;

public class FileEntity_bak {
   private String name;
   private int type;
   private long size;
   private String permission;
   private Timestamp createTime;
   private Timestamp lastModifyTime;
   private Timestamp lastAccessTime;

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

   public long getSize() {
      return this.size;
   }

   public void setSize(long size) {
      this.size = size;
   }

   public String getPermission() {
      return this.permission;
   }

   public void setPermission(String permission) {
      this.permission = permission;
   }

   public Timestamp getCreateTime() {
      return this.createTime;
   }

   public void setCreateTime(Timestamp createTime) {
      this.createTime = createTime;
   }

   public Timestamp getLastModifyTime() {
      return this.lastModifyTime;
   }

   public void setLastModifyTime(Timestamp lastModifyTime) {
      this.lastModifyTime = lastModifyTime;
   }

   public Timestamp getLastAccessTime() {
      return this.lastAccessTime;
   }

   public void setLastAccessTime(Timestamp lastAccessTime) {
      this.lastAccessTime = lastAccessTime;
   }
}
