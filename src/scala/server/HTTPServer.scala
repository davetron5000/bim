package bim.server

import java.net._

class HTTPServer(port:Int, inetAddress:InetAddress, serverSocketFactory: (Int,InetAddress) => ServerSocket) {
  def start:Unit = {
    val serverSocket = serverSocketFactory(port,inetAddress)

    serverSocket.accept()
  }
}

/** Factory for creating HTTPServer instances */
object HTTPServer {
  def apply(port                : Int,
            inetAddress         : InetAddress,
            serverSocketFactory : (Int,InetAddress) => ServerSocket) = new HTTPServer(port,inetAddress,serverSocketFactory)
}
