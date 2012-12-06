package bim.server

import org.junit._
import org.junit.Assert._

import scala.util.Random
import java.net._

class HTTPServerTest {

  class ServerRunner(server:HTTPServer) extends Runnable {
    def run:Unit = { server.start }
  }

  @Test
  def `basic test` = {
    val localhost = InetAddress.getLocalHost
    val server = new HTTPServer(8080,localhost)

    val thread = new Thread(new ServerRunner(server))

    thread.start

    val socket = new Socket(localhost.getHostAddress,8080)
    socket.getOutputStream.write("GET /foo/bar HTTP/1.1\r\n\r\n".getBytes("utf-8"))

    val stream = socket.getInputStream

    val buffer = new StringBuffer("")
    var ch = stream.read
    while (ch != -1) {
      buffer.append(ch.toChar)
      ch = stream.read
    }

    server.stop
    thread.join

    assertEquals("HTTP/1.1 200 OK\r\n\r\n",buffer.toString)
  }
}
// vim: set ts=2 sw=2 et:
