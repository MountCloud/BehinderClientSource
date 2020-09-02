// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.entity;

public class PluginEntity_bak
{
    private String name;
    private String entryFile;
    private String scriptType;
    private int type;
    private boolean isGetShell;
    private String icon;
    private String comment;
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getScriptType() {
        return this.scriptType;
    }
    
    public void setScriptType(final String scriptType) {
        this.scriptType = scriptType;
    }
    
    public int getType() {
        return this.type;
    }
    
    public String getIcon() {
        return this.icon;
    }
    
    public void setIcon(final String icon) {
        this.icon = icon;
    }
    
    public void setType(final int type) {
        this.type = type;
    }
    
    public String getEntryFile() {
        return this.entryFile;
    }
    
    public void setEntryFile(final String entryFile) {
        this.entryFile = entryFile;
    }
    
    public boolean isGetShell() {
        return this.isGetShell;
    }
    
    public void setGetShell(final boolean getShell) {
        this.isGetShell = getShell;
    }
    
    public String getComment() {
        return this.comment;
    }
    
    public void setComment(final String comment) {
        this.comment = comment;
    }
}
