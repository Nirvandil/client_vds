package cf.nirvandil.clientvds.util

import com.jcraft.jsch.SocketFactory
import lombok.SneakyThrows

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class SocketFactoryWithTimeout : SocketFactory {

    @SneakyThrows
    override fun createSocket(host: String, port: Int): Socket {
        return Socket().apply { connect(InetSocketAddress(host, port), TIMEOUT) }
    }

    @Throws(IOException::class)
    override fun getInputStream(socket: Socket): InputStream {
        return socket.getInputStream()
    }

    @Throws(IOException::class)
    override fun getOutputStream(socket: Socket): OutputStream {
        return socket.getOutputStream()
    }
}
