package bim
package server

import java.net._
import java.util.concurrent.atomic._

/** A simple HTTP server.
 * @param port port on which to listen
 * @param inetAddress InetAddress on which to listen
 * @param backlog backlog queue for creating underlying ServerSocket
 * @param timeout How long to wait for connections
 * @param dispatcher object to dispatch received requests
 */
class HTTPServer(port        : Int,
                 inetAddress : InetAddress,
                 backlog     : Int = 50,
                 timeout     : Int = 50,
                 dispatcher  : Dispatcher = InlineDispatcher) {

  private val running = new AtomicBoolean(false)

  def start:Unit = {
    val serverSocket = setupSocket
    running.set(true)
    while(running.get) {
      try {
        dispatch(serverSocket.accept)
      }
      catch {
        case ex:SocketTimeoutException => {}
      }
    }
    serverSocket.close
  }

  def stop:Unit = {
    running.set(false)
  }

  private def setupSocket:ServerSocket = (new ServerSocket(port,backlog,inetAddress)).tap { serverSocket =>
    println(s"Listening on $inetAddress/$port")
    serverSocket.setSoTimeout(timeout)
  }

  private def dispatch(socket:Socket):Unit = {
    println("Dispatching request")
    socket.setSoTimeout(timeout)
    dispatcher.dispatch(socket)
  }
}
