package net.rebeyond.behinder.utils.jc;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

public class Run {
   public static void main(String[] args) {
      (new Run()).test();
   }

   public void test() {
      String var1 = "\r\nimport javax.servlet.jsp.PageContext;\r\nimport javax.servlet.ServletOutputStream;\r\npublic class test\r\n{\r\n\tpublic boolean equals(Object obj){\r\n\r\n\tPageContext page = (PageContext) obj;\r\n\t\t\ttry {\r\n\t\t\t\tServletOutputStream so=page.getResponse().getOutputStream();\r\n\t\t\t\tso.write(\"afsddf\".getBytes(\"UTF-8\"));\r\n\t\t\t\tso.flush();\r\n\t\t\t\tso.close();\r\n\t\t\t\tpage.getOut().clear();  \r\n\t\t\t} catch (Exception e) {\r\n\t\t\t\t// TODO Auto-generated catch block\r\n\t\t\t\te.printStackTrace();\r\n\t\t\t} \r\n\t\treturn true;\r\n}\r\n}";

      try {
         while(true) {
            Thread.sleep(2000L);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   public static byte[] getClassFromSourceCode(String sourceCode) throws Exception {
      byte[] classBytes = null;
      Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");
      Matcher matcher = CLASS_PATTERN.matcher(sourceCode);
      if (matcher.find()) {
         String cls = matcher.group(1);
         JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
         if (jc == null) {
            throw new Exception("本地机器上没有找到编译环境，请确认:1.是否安装了JDK环境;2." + System.getProperty("java.home") + File.separator + "lib目录下是否有tools.jar.");
         } else {
            StandardJavaFileManager standardJavaFileManager = jc.getStandardFileManager((DiagnosticListener)null, (Locale)null, (Charset)null);
            JavaFileManager fileManager = new CustomClassloaderJavaFileManager(Run.class.getClassLoader(), standardJavaFileManager);
            JavaFileObject javaFileObject = new MyJavaFileObject(cls, sourceCode);
            List options = new ArrayList();
            options.add("-source");
            options.add("1.6");
            options.add("-target");
            options.add("1.6");
            DiagnosticCollector collector = new DiagnosticCollector();
            CompilationTask cTask = jc.getTask((Writer)null, fileManager, collector, options, (Iterable)null, Arrays.asList(javaFileObject));
            boolean result = cTask.call();
            if (!result) {
               List diagnostics = collector.getDiagnostics();
               Iterator var14 = diagnostics.iterator();
               if (var14.hasNext()) {
                  Diagnostic diagnostic = (Diagnostic)var14.next();
                  throw new Exception(diagnostic.getMessage((Locale)null));
               }
            }

            JavaFileObject fileObject = (JavaFileObject)CustomClassloaderJavaFileManager.fileObjects.get(cls);
            if (fileObject != null) {
               classBytes = ((MyJavaFileObject)fileObject).getCompiledBytes();
            }

            return classBytes;
         }
      } else {
         throw new IllegalArgumentException("No such class name in " + sourceCode);
      }
   }
}
