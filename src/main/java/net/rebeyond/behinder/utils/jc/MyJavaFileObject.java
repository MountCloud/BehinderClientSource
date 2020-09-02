// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils.jc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.JavaFileObject;
import java.io.ByteArrayOutputStream;
import javax.tools.SimpleJavaFileObject;

public class MyJavaFileObject extends SimpleJavaFileObject
{
    private String source;
    private ByteArrayOutputStream outPutStream;
    
    public MyJavaFileObject(final String name, final String source) {
        super(URI.create("String:///" + name + Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }
    
    public MyJavaFileObject(final String name, final Kind kind) {
        super(URI.create("String:///" + name + kind.extension), kind);
        this.source = null;
    }
    
    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
        if (this.source == null) {
            throw new IllegalArgumentException("source == null");
        }
        return this.source;
    }
    
    @Override
    public OutputStream openOutputStream() throws IOException {
        return this.outPutStream = new ByteArrayOutputStream();
    }
    
    public byte[] getCompiledBytes() {
        return this.outPutStream.toByteArray();
    }
}
