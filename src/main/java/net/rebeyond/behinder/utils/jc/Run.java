// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils.jc;

import java.util.Iterator;
import java.util.List;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaCompiler;
import java.util.regex.Matcher;
import javax.tools.Diagnostic;
import java.io.Writer;
import java.util.Arrays;
import javax.tools.DiagnosticCollector;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.util.Locale;
import javax.tools.JavaFileObject;
import javax.tools.DiagnosticListener;
import java.io.File;
import javax.tools.ToolProvider;
import java.util.regex.Pattern;

public class Run
{
    public static void main(final String[] args) {
        new Run().test();
    }
    
    public void test() {
        final String sourceCode = "\r\nimport javax.servlet.jsp.PageContext;\r\nimport javax.servlet.ServletOutputStream;\r\npublic class test\r\n{\r\n\tpublic boolean equals(Object obj){\r\n\r\n\tPageContext page = (PageContext) obj;\r\n\t\t\ttry {\r\n\t\t\t\tServletOutputStream so=page.getResponse().getOutputStream();\r\n\t\t\t\tso.write(\"afsddf\".getBytes(\"UTF-8\"));\r\n\t\t\t\tso.flush();\r\n\t\t\t\tso.close();\r\n\t\t\t\tpage.getOut().clear();  \r\n\t\t\t} catch (Exception e) {\r\n\t\t\t\t// TODO Auto-generated catch block\r\n\t\t\t\te.printStackTrace();\r\n\t\t\t} \r\n\t\treturn true;\r\n}\r\n}";
        try {
            while (true) {
                Thread.sleep(2000L);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static byte[] getClassFromSourceCode(final String sourceCode) throws Exception {
        byte[] classBytes = null;
        final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");
        final Matcher matcher = CLASS_PATTERN.matcher(sourceCode);
        if (!matcher.find()) {
            throw new IllegalArgumentException("No such class name in " + sourceCode);
        }
        final String cls = matcher.group(1);
        final JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            throw new Exception("\u672c\u5730\u673a\u5668\u4e0a\u6ca1\u6709\u627e\u5230\u7f16\u8bd1\u73af\u5883\uff0c\u8bf7\u786e\u8ba4:1.\u662f\u5426\u5b89\u88c5\u4e86JDK\u73af\u5883;2." + System.getProperty("java.home") + File.separator + "lib\u76ee\u5f55\u4e0b\u662f\u5426\u6709tools.jar.");
        }
        final StandardJavaFileManager standardJavaFileManager = jc.getStandardFileManager(null, null, null);
        final JavaFileManager fileManager = new CustomClassloaderJavaFileManager(Run.class.getClassLoader(), standardJavaFileManager);
        final JavaFileObject javaFileObject = new MyJavaFileObject(cls, sourceCode);
        final List<String> options = new ArrayList<String>();
        options.add("-source");
        options.add("1.6");
        options.add("-target");
        options.add("1.6");
        final DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
        final JavaCompiler.CompilationTask cTask = jc.getTask(null, fileManager, collector, options, null, Arrays.asList(javaFileObject));
        final boolean result = cTask.call();
        if (!result) {
            final List<Diagnostic<? extends JavaFileObject>> diagnostics = collector.getDiagnostics();
            final Iterator<Diagnostic<? extends JavaFileObject>> iterator = diagnostics.iterator();
            if (iterator.hasNext()) {
                final Diagnostic<? extends JavaFileObject> diagnostic = iterator.next();
                throw new Exception(diagnostic.getMessage(null));
            }
        }
        final JavaFileObject fileObject = CustomClassloaderJavaFileManager.fileObjects.get(cls);
        if (fileObject != null) {
            classBytes = ((MyJavaFileObject)fileObject).getCompiledBytes();
        }
        return classBytes;
    }
}
