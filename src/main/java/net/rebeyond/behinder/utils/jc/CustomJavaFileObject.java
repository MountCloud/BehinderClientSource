// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils.jc;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import java.io.Writer;
import java.io.Reader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.tools.JavaFileObject;

public class CustomJavaFileObject implements JavaFileObject
{
    private final String binaryName;
    private final URI uri;
    private final String name;
    
    public CustomJavaFileObject(final String binaryName, final URI uri) {
        this.uri = uri;
        this.binaryName = binaryName;
        this.name = ((uri.getPath() == null) ? uri.getSchemeSpecificPart() : uri.getPath());
    }
    
    @Override
    public URI toUri() {
        return this.uri;
    }
    
    @Override
    public InputStream openInputStream() throws IOException {
        return this.uri.toURL().openStream();
    }
    
    @Override
    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public Reader openReader(final boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Writer openWriter() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long getLastModified() {
        return 0L;
    }
    
    @Override
    public boolean delete() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Kind getKind() {
        return Kind.CLASS;
    }
    
    @Override
    public boolean isNameCompatible(final String simpleName, final Kind kind) {
        final String baseName = simpleName + kind.extension;
        return kind.equals(this.getKind()) && (baseName.equals(this.getName()) || this.getName().endsWith("/" + baseName));
    }
    
    @Override
    public NestingKind getNestingKind() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Modifier getAccessLevel() {
        throw new UnsupportedOperationException();
    }
    
    public String binaryName() {
        return this.binaryName;
    }
    
    @Override
    public String toString() {
        return "CustomJavaFileObject{uri=" + this.uri + '}';
    }
}
