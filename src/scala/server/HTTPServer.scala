package bim
package server

import java.net._
import java.util.concurrent.atomic._

class HTTPServer(port        : Int,
                 inetAddress : InetAddress,
                 backlog     : Int = 50,
                 timeout     : Int = 50) {

  private val running = new AtomicBoolean(false)

  def start:Unit = {
    val serverSocket = setupSocket
    running.set(true)
    while(running.get) {
      try {
        dispatch(serverSocket.accept)
      }
      catch {
        case ex:SocketTimeoutException => println(ex.getMessage)
      }
    }
    serverSocket.close
  }

  def stop:Unit = {
    running.set(false)
  }

  private def setupSocket:ServerSocket = (new ServerSocket(port,backlog,inetAddress)).tap { serverSocket =>
    println(s"ServerSocket ${if (serverSocket.isBound) 'bound else 'notbound} to port ${serverSocket.getLocalPort}, ${serverSocket.getInetAddress}")
    serverSocket.setSoTimeout(timeout)
  }

  private def dispatch(socket:Socket):Unit = try {
    socket.setSoTimeout(timeout)

    val inputStream  = socket.getInputStream
    val outputStream = socket.getOutputStream
    val request      = HTTPRequestParser.parse(inputStream)
    val response     = HTTPResponse(status = "200", reasonPhrase = "OK")

    HTTPResponseSerializer.serialize(response,outputStream)

    outputStream.flush
    outputStream.close
  }
  catch {
    case ex:SocketTimeoutException => {
      println(s"Client socket exception: ${ex.getMessage}")
      socket.close
    }
  }
}
