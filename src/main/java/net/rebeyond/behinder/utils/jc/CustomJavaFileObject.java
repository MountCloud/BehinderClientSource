package net.rebeyond.behinder.utils.jc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class CustomJavaFileObject implements JavaFileObject {
   private final String binaryName;
   private final URI uri;
   private final String name;

   public CustomJavaFileObject(String binaryName, URI uri) {
      this.uri = uri;
      this.binaryName = binaryName;
      this.name = uri.getPath() == null ? uri.getSchemeSpecificPart() : uri.getPath();
   }

   public URI toUri() {
      return this.uri;
   }

   public InputStream openInputStream() throws IOException {
      return this.uri.toURL().openStream();
   }

   public OutputStream openOutputStream() throws IOException {
      throw new UnsupportedOperationException();
   }

   public String getName() {
      return this.name;
   }

   public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
      throw new UnsupportedOperationException();
   }

   public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
      throw new UnsupportedOperationException();
   }

   public Writer openWriter() throws IOException {
      throw new UnsupportedOperationException();
   }

   public long getLastModified() {
      return 0L;
   }

   public boolean delete() {
      throw new UnsupportedOperationException();
   }

   public Kind getKind() {
      return Kind.CLASS;
   }

   public boolean isNameCompatible(String simpleName, Kind kind) {
      String baseName = simpleName + kind.extension;
      return kind.equals(this.getKind()) && (baseName.equals(this.getName()) || this.getName().endsWith("/" + baseName));
   }

   public NestingKind getNestingKind() {
      throw new UnsupportedOperationException();
   }

   public Modifier getAccessLevel() {
      throw new UnsupportedOperationException();
   }

   public String binaryName() {
      return this.binaryName;
   }

   public String toString() {
      return "CustomJavaFileObject{uri=" + this.uri + '}';
   }
}
