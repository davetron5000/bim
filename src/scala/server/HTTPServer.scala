package bim.server

import java.net._
import java.util.concurrent.atomic._

class HTTPServer(port:Int, inetAddress:InetAddress, backlog:Int = 50, timeout:Int = 50) {
  private val running = new AtomicBoolean(false)

  def start:Unit = {
    val serverSocket = new ServerSocket(port,backlog,inetAddress)
    println(s"ServerSocket ${if (serverSocket.isBound) 'bound else 'notbound} to port ${serverSocket.getLocalPort}, ${serverSocket.getInetAddress}")
    serverSocket.setSoTimeout(timeout)
    running.set(true)
    while(running.get) {
      try {
        val socket = serverSocket.accept
        val inputStream = socket.getInputStream
        val outputStream = socket.getOutputStream

        val request = HTTPRequestParser.parse(inputStream)
        val response = HTTPResponse(status = "200", reasonPhrase = "OK")

        HTTPResponseSerializer.serialize(response,outputStream)
        outputStream.flush
        outputStream.close
      }
      catch {
        case ex:SocketTimeoutException => {
          println(ex.getMessage)
          // ignore
        }
      }
    }
  }

  def stop:Unit = {
    running.set(false)
  }
}
