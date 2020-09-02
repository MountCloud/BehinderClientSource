// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.entity;

import java.sql.Timestamp;

public class FileEntity_bak
{
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
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public int getType() {
        return this.type;
    }
    
    public void setType(final int type) {
        this.type = type;
    }
    
    public long getSize() {
        return this.size;
    }
    
    public void setSize(final long size) {
        this.size = size;
    }
    
    public String getPermission() {
        return this.permission;
    }
    
    public void setPermission(final String permission) {
        this.permission = permission;
    }
    
    public Timestamp getCreateTime() {
        return this.createTime;
    }
    
    public void setCreateTime(final Timestamp createTime) {
        this.createTime = createTime;
    }
    
    public Timestamp getLastModifyTime() {
        return this.lastModifyTime;
    }
    
    public void setLastModifyTime(final Timestamp lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }
    
    public Timestamp getLastAccessTime() {
        return this.lastAccessTime;
    }
    
    public void setLastAccessTime(final Timestamp lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
