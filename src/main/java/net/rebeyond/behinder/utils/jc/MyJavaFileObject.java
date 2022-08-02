package net.rebeyond.behinder.utils.jc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class MyJavaFileObject extends SimpleJavaFileObject {
   private String source;
   private ByteArrayOutputStream outPutStream;

   public MyJavaFileObject(String name, String source) {
      super(URI.create("String:///" + name + Kind.SOURCE.extension), Kind.SOURCE);
      this.source = source;
   }

   public MyJavaFileObject(String name, Kind kind) {
      super(URI.create("String:///" + name + kind.extension), kind);
      this.source = null;
   }

   public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      if (this.source == null) {
         throw new IllegalArgumentException("source == null");
      } else {
         return this.source;
      }
   }

   public OutputStream openOutputStream() throws IOException {
      this.outPutStream = new ByteArrayOutputStream();
      return this.outPutStream;
   }

   public byte[] getCompiledBytes() {
      return this.outPutStream.toByteArray();
   }
}
