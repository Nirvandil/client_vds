package cf.nirvandil.clientvds.util;

import com.jcraft.jsch.SocketFactory;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketFactoryWithTimeout implements SocketFactory {

    @SneakyThrows
    public Socket createSocket(String host, int port) {
        Socket socket = new Socket();
        int timeout = 5_000;
        socket.connect(new InetSocketAddress(host, port), timeout);
        return socket;
    }

    public InputStream getInputStream(Socket socket) throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream(Socket socket) throws IOException {
        return socket.getOutputStream();
    }
}
